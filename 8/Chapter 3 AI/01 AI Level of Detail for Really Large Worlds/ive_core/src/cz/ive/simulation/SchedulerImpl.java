/* 
 *
 * IVE - Inteligent Virtual Environment
 * Copyright (c) 2005-2009, IVE Team, Charles University in Prague
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, 
 *       this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the Charles University nor the names of its contributors 
 *       may be used to endorse or promote products derived from this software 
 *       without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 *
 */
 
package cz.ive.simulation;

import cz.ive.gui.Gui;
import java.util.Date;
import cz.ive.exception.*;
import java.io.Serializable;
import cz.ive.logs.*;
import cz.ive.messaging.Hook;
import java.util.concurrent.locks.*;


/**
 * Takes care of real/simulation time conversion, shifting the calendar and
 * passing control to gui.
 * It is implemented as a singleton which holds the main simulation thread
 * for most of the system running time. The simulation is then driven from
 * another thread by asynchronous calls.
 * Only methods whose ability for asynchronous call is documented should be
 * called from another thread, or the system can fail.
 * There is also a synchronous mode of run which needs only one thread, but
 * has almost no control over the simulation.
 *
 * @author pavel
 */
public class SchedulerImpl implements Scheduler {

    /** The only instance of the scheduler */
    static private SchedulerImpl scheduler;
    
    /** Registered gui */
    protected Gui gui;
    
    /** Number of milliseconds between two calls of gui */
    protected long guiDelay;
    
    /** The ratio between real and simulation times */
    protected volatile double timeRatio;

    /** The simulation calendar singleton */
    protected CalendarImpl calendar;
   
    /** State of the simulation */
    protected volatile SimulationState state;
    
    /** Duration to an automatic stop of the simulation, zero if unlimited */
    protected volatile long simulationTimeout;
    
    /** Identifier of main simulation thread */
    protected Thread simulationThread;
    
    /** Flag determining that whole system is going down */
    protected volatile boolean killed;
    
    /** Flag determining that the simulation is running synchronously (no other
     *  thread to control it is present */
    protected boolean synchronous;
    
    /** Flag determining that we are already in some execute method - avoids
     *  multiple executions. */
    protected boolean executing;
    
    /** Determines whether we are physically in the simulation loop. This helps
     *  to cover the difference between asynchronous stopSimulation() call and
     *  the physical end of simulation loop. */
    protected boolean inSimulationLoop;
    
    /** Scheduling statistics */
    protected SchedulingStatistics statistics;
    
    /**
     * Aggregates number of milliseconds spent by computing of the simulation.
     * Can be used for evaluation.
     */
    protected long timeSpentByComputing;
    
    /** 
     * This lock is used for synchronization between the Simulation and other
     * threads. Simulation is being updated only with this lock locked.
     */
    protected ReentrantLock simulationLock = new ReentrantLock(true);
    
    /**
     * Determines whether the simulation should run as fast as possible
     */
    protected volatile boolean fastSimulation;
    
    /** Creates a new instance of Scheduler */
    protected SchedulerImpl() {
        gui = null;
        guiDelay = 100;
        timeRatio = 1.0;
        calendar = CalendarImpl.instance();
        state = SimulationState.NO_SIMULATION;
        simulationTimeout = 0;
        killed = false;
        synchronous = false;
        executing = false;
        inSimulationLoop = false;
        statistics = new SchedulingStatisticsImpl(90, 10, 10, guiDelay);
        timeSpentByComputing = 0;
        fastSimulation = false;
    }
    
    /** Returns the single instance of the scheduler */
    static public synchronized SchedulerImpl instance() {
        if (scheduler == null) {
            scheduler = new SchedulerImpl();
        }
               
        return scheduler;
    }
    
    /**
     * Gets the scheduling state, that can be stored and loaded later.
     * This method is to be called asynchronously from another thread.
     * @see #setSchedulingState(SchedulingState schedulingState)
     * @return Current scheduling state
     */
    public synchronized SchedulingState getSchedulingState() {

        SchedulingState schedulingState = new SchedulingState();
        schedulingState.timeRatio = timeRatio;
        schedulingState.statistics = statistics;
        
        return schedulingState;
    }
    
    /**
     * Sets the scheduling state of the loaded simulation.
     * The state of the rest of the world (mainly the Calendar singleton) have 
     * to be loaded before.
     * The simulation must not run when setting the state, or the
     * SimulationRunningException will be thrown.
     * @param schedulingState The loaded state
     */
    public void setSchedulingState(SchedulingState schedulingState) 
            throws SimulationRunningException {
        
        if (state == SimulationState.RUNNING) {
            throw new SimulationRunningException();
        }

        timeRatio = schedulingState.timeRatio;
        statistics = schedulingState.statistics;
        calendar = CalendarImpl.instance();
    }
    
    /**
     * Runs the simulation till the end or the asynchronous stop, but maximally 
     * for the given time.
     * @param millis Number of real milliseconds to the simulation interrupt,
     *               no timeout if set to zero, one-round simulation when
     *               set to -1.
     */
    protected void simulate (long millis) {
        
        long beginSimulTime;
        long beginRealTime;
        double stepLength;
        long counter;
        long nextTime;
        long endTime;
        double oldRatio;
        double currentRatio;
        long timeBeforeStep;
        long timeBeforeComputing;
        double simulTimeCorrection;
        boolean oneRound = false;
        boolean fast;
        boolean lastFast;
        long lastPaint = 0;
       
        synchronized (this) {
            currentRatio = timeRatio;
            lastFast = fastSimulation;
        }
        counter = 1;
        beginSimulTime = calendar.getSimulationTime();
        beginRealTime = System.currentTimeMillis();
        stepLength = guiDelay / currentRatio;
        if (millis > 0) {
            endTime = beginRealTime + millis;
        } else if (millis == 0) {
            endTime = Long.MAX_VALUE;
        } else {
            oneRound = true;
            endTime = beginRealTime;
        }
        oldRatio = currentRatio;
        while (true) {

            synchronized (this) {
                currentRatio = timeRatio;
                fast = fastSimulation;
            }
            /* Handle the change of time ratio */
            if ((oldRatio != currentRatio) || (lastFast != fast)) {
                oldRatio = currentRatio;
                beginSimulTime = calendar.getSimulationTime();
                beginRealTime = System.currentTimeMillis();
                counter = 1;
                stepLength = guiDelay / currentRatio;
                statistics.clearPenalty();
            }
            lastFast = fast;
            
            /* run the simulation */
            timeBeforeStep = System.currentTimeMillis();

            /* paint the world */
            if ((gui != null) && (!fast || lastPaint<timeBeforeStep-1000)
                    && (fast || timeBeforeStep>lastPaint+guiDelay/2)) {
                lastPaint = timeBeforeStep;
                gui.paint();
            }
            
            timeBeforeComputing = System.currentTimeMillis();
            
            /* compute the next iteration time */
            simulTimeCorrection = 0;
            nextTime = beginRealTime + counter*guiDelay;
            if (nextTime > endTime) {
                /* correction of simulation time */
                simulTimeCorrection = 
                        ((nextTime - endTime)*stepLength)/guiDelay;
                if (simulTimeCorrection >= stepLength) {
                    simulTimeCorrection = stepLength-1;
                }
                    
                nextTime = endTime;
            }
           
            try {
                if (oneRound) {
                    calendar.step();
                } else {
                    calendar.step(fast ? 10000 : (beginSimulTime 
                        + (long) (counter*stepLength - simulTimeCorrection) 
                        - calendar.getSimulationTime()));
                }
            }
            catch (EmptyCalendarException e) {
                if (cz.ive.IveApplication.instance().stopOnEmpty) {
                    synchronized (this) {
                        state = SimulationState.STOPPED;
                    }
                    return;
                }
            }
            
            timeSpentByComputing += 
                    (System.currentTimeMillis() - timeBeforeComputing);
            
            /* compute the statistics */
            statistics.shift(fast ? 
                guiDelay : System.currentTimeMillis() - timeBeforeStep);
            
            long currTime;
            
            /* sleep for the rest of time */
            if (!fast) {
                synchronized (this) {
                    while ((nextTime > (currTime = System.currentTimeMillis())) 
                            && (state == SimulationState.RUNNING))
                    {
                        try {
                            wait(nextTime - currTime);
                        }
                        catch (InterruptedException e) {
                            /* somebody wants something - handled later */
                        }
                    }
                }
            }
            
            synchronized (this) {
                if (nextTime >= endTime) {
                    state = SimulationState.STOPPED;
                }
                
                if (state != SimulationState.RUNNING) {
                    /* paint the world one more time, before the stop */
                    if (gui != null) {
                        gui.paint();
                    }
                    
                    return;
                }
            }
            
            counter++;
        }
    }
    
    /**
     * Registers the gui taking care of interfacing with the user
     * @param gui Gui to register
     * @param millis Time in milliseconds between two calls of gui's paint() 
     *               method.
     */
    public void registerGui (Gui gui, long millis) {
        
        this.gui = gui;
        this.guiDelay = millis;
        statistics.setLoopTime(millis);
    }

    /**
     * Specifies the speed of the simulation.
     * This method can be called both in synchronous and asynchronous mode.
     * However, it takes effect at next frame.
     * @param ratio Number of real milliseconds per one simulation millisecond
     */
    public synchronized void setTimeRatio(double ratio) {
        timeRatio = ratio;
    }

    /**
     * Runs the simulation till the end or the stopSimulation() call. 
     * This method is to be called asynchronously from another thread and has
     * no effect if the simulation runs synchronously. The execute() method
     * has to be called before.
     * @see #execute(boolean synchronous)
     */
    public synchronized void runSimulation() {
        
        if (synchronous || (! executing)) {
            return;
        }
        
        if (state != SimulationState.RUNNING) {
            state = SimulationState.RUNNING;
            simulationTimeout = 0;
            this.notifyAll();
        }
    }

    public synchronized void stepSimulation() {
        
        if (synchronous || (! executing)) {
            return;
        }
        
        if (state != SimulationState.RUNNING) {
            state = SimulationState.RUNNING;
            simulationTimeout = -1;
            this.notifyAll();
        }
    }

    /**
     * Runs the simulation for the given time or to its end or to the 
     * stopSimulation() call.
     * This method is to be called asynchronously from another thread and has
     * no effect if the simulation runs synchronously.
     * @see #execute(boolean synchronous)
     * @param millis Number of real milliseconds till the sumulation interrupt
     */
    public synchronized void runSimulation(long millis) {
        
        if (synchronous || (!executing)) {
            return;
        }

        if (millis <= 0) return;
        if (state != SimulationState.RUNNING) {
            state = SimulationState.RUNNING;
            simulationTimeout = millis;
            this.notifyAll();
        }
    }
    
    /**
     * Interrupts the simulation.
     * This method is to be called asynchronously from another thread and has
     * no effect if the simulation runs synchronously.
     * @see #execute(boolean synchronous)
     */       
    public synchronized void stopSimulation() {
        
        if (synchronous || (!executing)) {
            return;
        }

        CalendarImpl.instance().breakSimulation();
        
        if (state == SimulationState.RUNNING) {
            state = SimulationState.STOPPED;
            this.notifyAll();
        }
    }

    /**
     * Gives the main simulation thread to the scheduler.
     * The thread then loops inside and the simulation can be controlled
     * asynchronously from another thread by runSimulation(), stopSimulation(),
     * or setTimeRatio.
     */
    public void execute() {

        boolean quit;
        long timeout;
                
        if (executing) {
            return;
        }
        
        executing = true;
        simulationThread = Thread.currentThread();
        quit = false;
        statistics.startStatistics();
        
        while (true) {
            synchronized (this) {
                while ((! killed) && (state != SimulationState.RUNNING)) {
                    try {
                        wait();
                    }
                    catch (InterruptedException e) {
                        /* it's ok, we were waiting for that */
                    }
                }
                inSimulationLoop = true;
                quit = killed;
                timeout = simulationTimeout;
            }

            if (! quit) {
                simulate(timeout);
            } else {
                statistics.stopStatistics();
                return;
            }
            inSimulationLoop = false;
        }
    }
    
    /**
     * Gives the main simulation thread to the scheduler.
     * This function is rather for developing issues and later will be 
     * deprecated.
     * If the argument is set to false, it is a synonym tu execute():
     * the thread loops inside and the simulation can be controlled
     * asynchronously from another thread by runSimulation(), stopSimulation(),
     * or setTimeRatio.
     * If the argument is set to true, it runs the simulation synchronous.
     * That means there is no other thread to control the simulation, so
     * it will be automatically run and after it's end it will return. If
     * the simulation is running this way, asynchronous methods have no
     * effect (runSimulation(), stopSimulation(), quit()).
     * @param synchronous Determines the mode of the simulation
     */
    public void execute(boolean synchronous) {
        
        if (executing) {
            return;
        }
        
        if (!synchronous) {
            execute();
            return;
        }

        executing = true;
        simulationThread = Thread.currentThread();
        runSimulation();
        this.synchronous = true;
        simulate(0);
        state = SimulationState.NO_SIMULATION;
    }

    /**
     * Quits the system - causes the call of execute() method to finish.
     * This method is to be called asynchronously from another thread and
     * has no effect if the simulation runs synchronously.
     * @see #execute(boolean synchronous)
     */
    public synchronized void quit() {
        
        if (synchronous || (! executing)) {
            return;
        }

        if (state == SimulationState.RUNNING) {
            state = SimulationState.STOPPED;
        } 

        killed = true;

        this.notifyAll();
    }
    
    /**
     * Gets the state of the simulation
     */
    public synchronized SimulationState getSimulationState() {
        return state;
    }
    
    /**
     * Checks whether the simulation is physically in the simulation loop.
     * This helps to the gui thread wait for the physical jump from the 
     * simulation loop after it called the stopSimulation.
     * @return true if the simulation is in progress;
     *  false if the simulation is physically stopped
     */
    public synchronized boolean isInSimulationLoop() {
        return inSimulationLoop;
    }
    
    public SchedulingStatistics getStatistics() {
        return statistics;
    }
    
    public Hook getCleaningHook() {
        return statistics;
    }
    
    /**
     * Locks the simualtion thread. This locking should be used with caution 
     * and for only a small amout of time, since it prevents scheduler from
     * maintaining proper time sharing between Gui and Simulation. Acquiring
     * this lock ensures, that the simulation is in a consistent state.
     */
    public void lockSimulation() {
        simulationLock.lock();
    }
    
    /**
     * Unlocks the simualtion thread. This locking should be used with caution 
     * and for only a small amout of time, since it prevents scheduler from
     * maintaining proper time sharing between Gui and Simulation. Acquiring
     * this lock ensures, that the simulation is in a consistent state.
     */
    public void unlockSimulation() {
        simulationLock.unlock();
    }

    public void updateWorld() {
        lockSimulation();
        CalendarImpl.instance().updateWorld();
        unlockSimulation();
        gui.paint();
    }

    public void clearTimeSpentByComputing() {
        timeSpentByComputing = 0;
    }
    
    public long getTimeSpentByComputing() {
        return timeSpentByComputing;
    }

    public synchronized boolean getFastSimulation() {
        return fastSimulation;
    }

    public synchronized void setFastSimulation(boolean fast) {
        fastSimulation = fast;
    }
}

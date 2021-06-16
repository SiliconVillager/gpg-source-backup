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
import cz.ive.messaging.Hook;

/**
 * Takes care of real/simulation time conversion, shifting the calendar and
 * passing control to gui.
 * The Scheduler should be a singleton which holds the main simulation thread
 * for most of the system running time. The simulation is then driven from
 * another thread by asynchronous calls.
 * Only methods whose ability for asynchronous call is documented should be
 * called from another thread, or the system can fail.
 *
 * @author pavel
 */
public interface Scheduler {
    
    /**
     * Registers the gui taking care of interfacing with the user
     * @param gui Gui to register
     * @param millis Time in milliseconds between two calls of gui's paint() 
     *               method.
     */
    void registerGui(Gui gui, long millis);

    /**
     * Specifies the speed of the simulation.
     * This method can be called both in synchronous and asynchronous mode.
     * However, it takes effect at next frame.
     * @param ratio Number of real milliseconds per one simulation millisecond
     */
    void setTimeRatio(double ratio);

    /**
     * Runs the simulation till the end or the stopSimulation() call. 
     * This method is to be called asynchronously from another thread and has
     * no effect if the simulation runs synchronously. The execute() method
     * has to be called before.
     * @see #execute(boolean synchronous)
     */
    void runSimulation();

    /**
     * Runs the simulation for the given time or to its end or to the 
     * stopSimulation() call.
     * This method is to be called asynchronously from another thread and has
     * no effect if the simulation runs synchronously.
     * @see #execute(boolean synchronous)
     * @param millis Number of real milliseconds till the sumulation interrupt
     */
    void runSimulation(long millis);
    
    /**
     * Runs one round of the simulation.
     * This method is to be called asynchronously from another thread and has
     * no effect if the simulation runs synchronously.
     */
    void stepSimulation();

    /**
     * Interrupts the simulation.
     * This method is to be called asynchronously from another thread and has
     * no effect if the simulation runs synchronously.
     * @see #execute(boolean synchronous)
     */       
    void stopSimulation();

    /**
     * Gets the fast simulation state.
     * This method is to be called asynchronously from another thread.
     * @return true iff the simulation is running as fast as possible
     */
    boolean getFastSimulation();

    /**
     * Sets fast simulation state.
     * This method is to be called asynchronously from another thread.
     * @param fast The new state - if true, the simulation will run as fast
     * as possible. If false, the simulation will run according to the current
     * time ration position.
     */
    void setFastSimulation(boolean fast);
    
    /**
     * Gives the main simulation thread to the scheduler.
     * The thread then loops inside and the simulation can be controlled
     * asynchronously from another thread by runSimulation(), stopSimulation(),
     * or setTimeRatio.
     */
    void execute();
    
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
    void execute(boolean synchronous);

    /**
     * Quits the system - causes the call of execute() method to finish.
     * This method is to be called asynchronously from another thread and
     * has no effect if the simulation runs synchronously.
     * @see #execute(boolean synchronous)
     */
    void quit();
    
    
    /**
     * Gets the state of the simulation
     */
    SimulationState getSimulationState();
    
    /**
     * Gets the scheduling statistics control
     */
    SchedulingStatistics getStatistics();
    
    /**
     * Gets the cleaning hook.
     * Listeners will be notified in order to clean their garbage
     */
    Hook getCleaningHook();
    
    /**
     * Locks the simualtion thread. This locking should be used with caution 
     * and for only a small amout of time, since it prevents scheduler from
     * maintaining proper time sharing between Gui and Simulation. Acquiring
     * this lock ensures, that the simulation is in a consistent state.
     */
    public void lockSimulation();
    
    /**
     * Unlocks the simualtion thread. This locking should be used with caution 
     * and for only a small amout of time, since it prevents scheduler from
     * maintaining proper time sharing between Gui and Simulation. Acquiring
     * this lock ensures, that the simulation is in a consistent state.
     */
    public void unlockSimulation();
    
    /**
     * Updates the world.
     * Locks the simulation, updates the world, unlocks the simulation and 
     * calls gui to repaint the world. 
     */
    public void updateWorld();
    
    /**
     * Clears the counter of the time spent by computing of the simulation.
     */
    public void clearTimeSpentByComputing();
    
    /**
     * Gets time spent by computing of the simulation in milliseconds.
     * The time is measured since the scheduler creation or since last
     * clearTimeSpentByComputing() call.
     * This is to be used for performance evaluations.
     * @return number of milliseconds
     */
    public long getTimeSpentByComputing();
}

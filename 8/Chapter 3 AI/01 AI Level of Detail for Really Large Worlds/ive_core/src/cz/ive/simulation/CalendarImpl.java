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

import java.io.Serializable;
import java.util.HashMap;
import cz.ive.messaging.Hook;
import cz.ive.messaging.SyncHook;
import cz.ive.exception.HookNotPlannedException;
import cz.ive.exception.EmptyCalendarException;
import cz.ive.lod.LodManager;
import cz.ive.manager.ManagerOfSenses;
import cz.ive.genius.GeniusList;
import cz.ive.gui.GuiCalendar;
import cz.ive.messaging.Listener;
import java.util.Arrays;
import java.util.List;

import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

/**
 * Simulation calendar. For now implemented as a public singleton.
 *
 * @author pavel
 */
public class CalendarImpl implements Calendar, Serializable {

    /** The hook for planned events */
    protected class CalendarHook extends SyncHook implements Hook, Comparable, 
            Serializable {

                /** Instance number */
                protected long instanceNumber;
                
                /** The planned time of that hook */
                protected long time;
        
                /** Creates a new instance of CalendarHook */      
                public CalendarHook(long time) {
                    this.time = time;
                    instanceNumber = calendarHookInstanceCounter++;
                } 

                /** Pulls legs of all registered listeners */
                private void pullLegs() {
                    notifyListeners();
                }
        
                /** Notify all registered listeners about hook cancel */
                private void pullLegsCancel() {
                    notifyListenersCancel();
                }

                /** Getter for the time of invoking this hook */
                public long getTime() {
                    return time;
                }

                /** Compares times of two hooks */
                public int compareTo(Object o) {
                    CalendarHook hook;

                    hook = (CalendarHook) o;
                    if (this.time < hook.getTime()) return -1;
                    if (this.time > hook.getTime()) return 1;
                    if (this.instanceNumber < hook.instanceNumber) return -1;
                    if (this.instanceNumber > hook.instanceNumber) return 1;
                    return 0;
                }
                
                /** Access to listeners list, for the Gui info extraction. */
                protected Set<Listener> getListeners() {
                    return listeners;
                }
    }   

    /** Instacnce counter of CalendarHook - used for unique compare */
    private long calendarHookInstanceCounter;
                
    /** The queue of the planned hooks, sorted by their planed time */
    protected PriorityQueue<CalendarHook> plans;

    /** The simulation time */
    protected long simulationTime;
    
    /** The static reference to the calendar singleton */
    static private CalendarImpl calendar;

    /** Hooks that is pulled in the update cycle at the end of each round */
    private HashMap<CalendarPlanner.RoundHookPosition, CalendarHook> roundHooks;
    
    /** 
     * Is to be set to true when the simulation cycle should finish 
     * immediately.
     */
    private volatile boolean brokenSimulation;
    
    /** Creates a new instance of CalendarImpl */
    protected CalendarImpl() {

        brokenSimulation = false;
        simulationTime = 0;
        calendarHookInstanceCounter = 0;
        plans = new PriorityQueue<CalendarHook>();
        roundHooks = new 
                HashMap<CalendarPlanner.RoundHookPosition, CalendarHook>();
        for (CalendarPlanner.RoundHookPosition pos : 
                    CalendarPlanner.RoundHookPosition.values()) {
            roundHooks.put(pos, new CalendarHook(0));
        }
    }
    
    /** Returns the single instance of the calendar */
    static public CalendarImpl instance() {
        if (calendar == null) {
            calendar = new CalendarImpl();
        }
        
        return calendar;
    }

    /** 
     * Returns the single instance of the calendar, or null if it does not 
     * exist yet 
     */
    static public synchronized CalendarImpl getInstance() {
        return calendar;
    }

    /** 
     * Empty whole the Calendar before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        calendar = new CalendarImpl();
    }
    
    /** 
     * Sets the static reference to the singleton. This method should be called
     * only in case of loading the singleton object from the serialized stream
     * @param instance Reference to the new instance
     */
    static public synchronized void setInstance(CalendarImpl instance) {
        calendar = instance;
    }

    /**
     * Plans Hook signaling after specified period of time.
     * This method should be used only by CalendarPlanner, which presents it
     * to others.
     * @param time period of time in milliseconds
     * @return Hook to be signaled
     */
    Hook plan(long time) {
        CalendarHook newHook;
        newHook = new CalendarHook(simulationTime + time);
        plans.add(newHook);
        
        return newHook;
    }
    
    /**
     * Replans Hook to signal after specified period of time. 
     * This method should be used only by CalendarPlanner, which presents it
     * to others.
     * @param hook Hook to be replanned, it must be a hook created by plan()
     *             method of CalendarPlanner, or the ClassCast exception will
     *             be thrown.
     * @param time period of time in milliseconds
     * @param clear Determines whether the hook should be cleared before it's 
     *              usage. If set to false, the hook will be only replanned, 
     *              all listeners remaining. If set to true, the hook will be 
     *              cleared to reuse the structure as a new hook.
     */
    void replan(Hook hook, long time, boolean clear) {

        CalendarHook cHook;
        cHook = (CalendarHook) hook;
        
        if (plans.contains(cHook)) {
            plans.remove(cHook);
        }
        
        if (clear) {
            cHook.unregisterAll();
        }
        
        cHook.time = simulationTime + time;
        
        plans.add(cHook);
    }

    /**
     * Cancels the hook planned by plan() method. Notifies all listeners
     * about the hook cancel and deletes the hook.
     * If the canceled hook was not planned by plan() method, the 
     * HookNotPlannedException will be thrown.
     * This method should be used only by CalendarPlanner, which presents it
     * to others.
     * @param hook Hook to cancel
     */
    public void cancelHook(Hook hook) throws HookNotPlannedException {
        CalendarHook calendarHook;
        
        if (hook == null) {
            throw new HookNotPlannedException();
        }
        if (! plans.remove(hook)) {
            throw new HookNotPlannedException();
        }
        
        calendarHook = (CalendarHook) hook;
        calendarHook.pullLegsCancel();
    }

    /** 
     * Updates all part of the world that need to be updated after all commits
     * were done in the current simulation milisecond.
     */
    public void updateWorld() {
        
        roundHooks.get(CalendarPlanner.RoundHookPosition.BEGINNING).pullLegs();
        
        while (true) {

            roundHooks.get(CalendarPlanner.RoundHookPosition.BEFORE_LOD).
                    pullLegs();

            if (LodManager.getJam().needUpdate()) {
                LodManager.getJam().update();
            }
            
            roundHooks.get(CalendarPlanner.RoundHookPosition.BEFORE_WORLD).
                    pullLegs();

            if (WorldInterpreter.instance().needUpdate()) {
                WorldInterpreter.instance().update();
                continue;
            }

            roundHooks.get(CalendarPlanner.RoundHookPosition.BEFORE_SENSEMGR).
                    pullLegs();
            
            if (ManagerOfSenses.getJam().needUpdate()) {
                ManagerOfSenses.getJam().update();
            }
            
            roundHooks.get(CalendarPlanner.RoundHookPosition.BEFORE_UPDATOR).
                    pullLegs();
          
            if (Updator.getRootInstance().needUpdate()) {
                Updator.getRootInstance().update();
                continue;
            }
            
            roundHooks.get(CalendarPlanner.RoundHookPosition.BEFORE_GENIUS).
                    pullLegs();
          
            if (GeniusList.instance().needUpdate()) {
                GeniusList.instance().update();
                continue;
            }

            break;
        }

        roundHooks.get(CalendarPlanner.RoundHookPosition.END).pullLegs();
    }
    
    /**
     * Run the simulation for given period of simulation time.
     * The Simulation lock of the Scheduler is locked for every time step
     * evaluation. This prevents other threads from accessing the simulation
     * when it is being modified.
     * @param time period of time in milliseconds
     */
    public boolean step(long time) throws EmptyCalendarException {

        CalendarHook hook;
        long toTime;
        long lastTime;
        long currentTime;
        boolean stepped = false;
        boolean found = false;

        synchronized(this) {
            brokenSimulation = false;
        }

        toTime = simulationTime + time;
        
        if (plans.isEmpty()) {
            // Update world to ensure it is realy empty
            SchedulerImpl.instance().lockSimulation();
            updateWorld();
            SchedulerImpl.instance().unlockSimulation();
            simulationTime = toTime;
            if (plans.isEmpty())
                throw new EmptyCalendarException();
        }
        
        /* perform all events in the given interval */
        lastTime = plans.peek().getTime();
        while ((! plans.isEmpty()) && (
                (currentTime = plans.peek().getTime()) <= toTime)) {            
            found = true;
            step();
            synchronized(this) {
                if (brokenSimulation == true) {
                    return found;
                }
            }
        }

        simulationTime = toTime;

        synchronized(this) {
            brokenSimulation = false;
        }
        
        return found;
    }
    
    /**
     * Run the simulation to the nearest event. All events in this simulation
     * time will be evaluated.
     */
    public void step() throws EmptyCalendarException {

        CalendarHook hook;
        long eventTime;

        if (plans.isEmpty()) {
            // Update world to ensure it is realy empty
            SchedulerImpl.instance().lockSimulation();
            updateWorld();
            SchedulerImpl.instance().unlockSimulation();
            if (plans.isEmpty())
                throw new EmptyCalendarException();
        }

        hook = plans.peek();
        
        simulationTime = hook.getTime();
        
        /* execute all events planned on the same time */
        SchedulerImpl.instance().lockSimulation();
        eventTime = hook.getTime();
        while (hook.getTime() == eventTime) {
            plans.poll().pullLegs();
            hook = plans.peek();
            if (hook == null) {
                break;
            }
        }

        updateWorld();

        SchedulerImpl.instance().unlockSimulation();
    }

    /** Getter for the simulation time */
    public long getSimulationTime() {
        return simulationTime;
    }
    
    public Hook getRoundHook(CalendarPlanner.RoundHookPosition pos) {
        return roundHooks.get(pos);
    }

    /**
     * This method is to be called from the gui thread with the simulation lock
     * held only.
     *
     * @return List of prefabricated information about calendar events.
     */
    public List<GuiCalendar.EventInfo> getGuiInfo() {
        Vector<GuiCalendar.EventInfo> list = 
                new Vector<GuiCalendar.EventInfo>();
        CalendarHook[] hooks = new CalendarHook[plans.size()];
        hooks = plans.toArray(hooks);
        Arrays.sort(hooks);
        
        for (CalendarHook plan : hooks) {
            for (Listener listener : plan.getListeners()) {
                list.add(new GuiCalendar.EventInfo(listener, plan.getTime()));
            }
        }
        return list;
    }
    
    /**
     * Breaks the simulation at the end of the current simulation moment.
     * If the simulation is not computed in this moment, has no effect.
     * Can be called from another thread.
     */
    public synchronized void breakSimulation() {
        brokenSimulation = true;
    }
}

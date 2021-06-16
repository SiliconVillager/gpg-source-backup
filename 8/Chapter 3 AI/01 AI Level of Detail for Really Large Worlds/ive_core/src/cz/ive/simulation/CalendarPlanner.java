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
import java.util.Date;
import cz.ive.messaging.Hook;
import cz.ive.exception.HookNotPlannedException;
import java.util.TimeZone;

/**
 * Simulation calendar access point. Implemented as a public singleton, so
 * everybody can plan its actios to the future.
 *
 * @author pavel
 */
public class CalendarPlanner implements CalendarPlanning, Serializable {
    
    /**
     * Positions of roundHook
     */
    public enum RoundHookPosition {
        /**
         * Pull before the update cycle
         */
        BEGINNING,
        
        /**
         * Pull at the beginning of the update cycle
         */
        BEFORE_LOD,
        
        /**
         * Pull between update of LodManager and WorldInterpreter
         */
        BEFORE_WORLD,
        
        /**
         * Pull after update of LodManager and WorldInterpreter, before
         *  update of ManagerOfSences
         */
        BEFORE_SENSEMGR,
        
        /**
         * Pull after update of LodManager, WorldInterpreter, and
         * ManagerOfSences, before update of Updator
         */
        BEFORE_UPDATOR,
        
        /**
         * Pull after update of LodManager, WorldInterpreter, and
         * ManagerOfSences, before update of Updator
         */
        BEFORE_GENIUS,
        
        /**
         * Pull when everything is updated
         */
        END,
    }
    
    /** Instance of the calendar itself */
    private CalendarImpl calendar;
    
    /** The only instance of the calendar planner */
    static private CalendarPlanner planner;
    
    /** Creates a new instance of CalendarPlanner */
    protected CalendarPlanner() {
        calendar = CalendarImpl.instance();
    }
    
    /** Returns the single instance of the calendar planner */
    static public CalendarPlanner instance() {
        if (planner == null) {
            planner = new CalendarPlanner();
        }
        
        return planner;
    }
    
    /**
     * Empty whole the CalendarPlaner before the XML load. We just drop
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        planner = new CalendarPlanner();
    }
    
    /**
     * Returns the single instance of the planner, or null if it does not
     * exist yet
     */
    static public synchronized CalendarPlanner getInstance() {
        return planner;
    }
    
    /**
     * Sets the static reference to the singleton. This method should be called
     * only in case of loading the singleton object from the serialized stream
     * @param instance Reference to the new instance
     */
    static public synchronized void setInstance(CalendarPlanner instance) {
        planner = instance;
    }
    
    public Hook plan(long time) {
        return calendar.plan(time);
    }
    
    public void replan(Hook hook, long time, boolean clear) {
        calendar.replan(hook, time, clear);
    }
    
    /**
     * Cancels the hook planned by plan() method. Notifies all listeners
     * about the hook cancel and deletes the hook.
     * If the canceled hook was not planned by plan() method, the
     * HookNotPlannedException will be thrown.
     * @param hook Hook to cancel
     */
    public void cancelHook(Hook hook) throws HookNotPlannedException {
        calendar.cancelHook(hook);
    }
    
    public long getSimulationTime() {
        return calendar.getSimulationTime();
    }
    
    public Date getSimulationDate() {
        return new Date(getSimulationTime());
    }
    
    public String getSimulationTimeString() {
        return simulationDateToString(getSimulationDate());
    }
    
    public Hook getRoundHook(RoundHookPosition pos) {
        return calendar.getRoundHook(pos);
    }
    
    /**
     * Translate simulation time to the String form.
     *
     * @param sTime Simulation time.
     * @return canonical String representation of the simulation time
     */
    static public String simulationTimeToString(long sTime) {
        return simulationDateToString(new Date(sTime));
    }
    
    /**
     * Translate simulation date to the String form.
     *
     * @param sDate Simulation date.
     * @return canonical String representation of the simulation time
     */
    static public String simulationDateToString(Date sDate) {
        java.util.Calendar cal = java.util.Calendar.getInstance(
                TimeZone.getTimeZone("GMT"));
        cal.setTime(sDate);
        
        return String.format("%d. %02d:%02d:%02d",
                1 + sDate.getTime() / (24*60*60*1000), cal.get(cal.HOUR_OF_DAY),
                cal.get(cal.MINUTE), cal.get(cal.SECOND));
    }
}

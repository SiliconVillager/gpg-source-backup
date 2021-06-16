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

import cz.ive.messaging.*;
import cz.ive.exception.HookNotPlannedException;
import java.util.Date;

/**
 * Simulation calendar access point. There should be a singleton
 * implementation of this interface allowing everybody to plan its actions 
 * to the future. The singleton should be public (unlike Calendar itself).
 *
 * @author Ondra
 */
public interface CalendarPlanning {
    
    /**
     * Plans Hook signaling after specified period of time.
     * @param time period of time in milliseconds
     * @return Hook to be signaled
     */
    Hook plan(long time);

    /**
     * Replans Hook to signal after specified period of time. 
     * @param hook Hook to be replanned, it must be a hook created by plan()
     *             method of CalendarPlanner, or the ClassCast exception will
     *             be thrown.
     * @param time period of time in milliseconds
     * @param clear Determines whether the hook should be cleared before it's 
     *              usage. If set to false, the hook will be only replanned, 
     *              all listeners remaining. If set to true, the hook will be 
     *              cleared to reuse the structure as a new hook.
     */
    void replan(Hook hook, long time, boolean clear);

    /**
     * Cancels the hook planned by plan() method. Notifies all listeners
     * about the hook cancel and deletes the hook.
     * If the canceled hook was not planned by plan() method, the 
     * HookNotPlannedException will be thrown.
     * @param hook Hook to cancel
     */
    void cancelHook(Hook hook) throws HookNotPlannedException;
    
    /**
     * Gets the current simulation time
     * @return The simulation time
     */
    long getSimulationTime();
    
    /**
     * Gets the current simulation time in the java Date representation.
     * @return The simulation date and time
     */
    Date getSimulationDate();
    
    /**
     * Gets the current simulation time as a String.
     * @return String representing a current simulation time
     */
    String getSimulationTimeString();
    
    /**
     * Gets the hook, which is pulled after each simlation step, in which
     * at least one event has occured.
     * @param pos Position of the roundHook
     * @return The round hook
     */
    Hook getRoundHook(CalendarPlanner.RoundHookPosition pos);
}

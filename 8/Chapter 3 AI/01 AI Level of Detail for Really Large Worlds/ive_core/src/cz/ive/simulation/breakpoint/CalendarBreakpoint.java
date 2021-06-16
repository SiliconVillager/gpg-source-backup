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
 
package cz.ive.simulation.breakpoint;

import cz.ive.exception.BreakpointActivationFailedException;
import cz.ive.exception.HookNotPlannedException;
import cz.ive.logs.Log;
import cz.ive.messaging.*;
import cz.ive.simulation.*;
import java.io.Serializable;
import java.util.*;

/**
 * Calendar breakpoint. It can stop the simulation at the given simulation time.
 *
 * @author ondra
 */
public class CalendarBreakpoint extends Breakpoint {
    
    /** Simulation time of the break point */
    protected long sTime;
    
    /** Associated calendar hook. */
    protected Hook calendarHook;
    
    /** Our calendar listener. */
    protected CalendarBreakpointListener listener =
            new CalendarBreakpointListener();
    
    /**
     * Creates a new instance of CalendarBreakpoint
     *
     * @param sTime simulation time of the break.
     */
    public CalendarBreakpoint(long sTime) {
        this.sTime = sTime;
    }
    
    /**
     * Creates a new instance of CalendarBreakpoint
     *
     * @param sDate simulation time of the break (in the Date "GMT" format).
     */
    public CalendarBreakpoint(Date sDate) {
        this.sTime = sDate.getTime();
    }
    
    /**
     * Setter for the breakpoint time.
     *
     * @param sDate New breakpoint simulation time in the "GMT" Date format.
     */
    public void setBreakpointDate(Date sDate) {
        this.sTime = sDate.getTime();
    }
    
    /**
     * Setter for the breakpoint time.
     *
     * @param sTime New breakpoint simulation time in millis.
     */
    public void setBreakpointTime(long sTime) {
        this.sTime = sTime;
    }
    
    /**
     * Getter for the breakpoint time.
     *
     * @return breakpoint simulation time in the "GMT" Date format.
     */
    public Date getBreakpointDate() {
        return new Date(sTime);
    }
    
    /**
     * Getter for the breakpoint time.
     *
     * @return breakpoint simulation time in millis.
     */
    public long getBreakpointTime() {
        return sTime;
    }
    
    /**
     * Perform the breakpoint action.
     */
    protected void performAction() {
        doBreak();
        Log.config("Calendar breakpoint.");
        BreakpointList.instance().removeBreakpoint(this);
    }
    
    /**
     * Activates the Breakpoint.
     *
     * @throws BreakpointActivationFailedException on activation failure.
     */
    public void activate() throws BreakpointActivationFailedException {
        if (!isActive()) {
            long actTime = CalendarPlanner.instance().getSimulationTime();
            if (actTime < sTime) {
                calendarHook = CalendarPlanner.instance().plan(sTime -
                        actTime);
                calendarHook.registerListener(listener);
            } else {
                throw new BreakpointActivationFailedException(
                        "Breakpoint time must be in the future.");
            }
            active = true;
            BreakpointList.instance().refreshBreakpoints();
        }
    }
    
    /**
     * Deactivates the breakpoint.
     */
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            
            calendarHook.unregisterListener(listener);
            try {
                CalendarPlanner.instance().cancelHook(calendarHook);
            } catch (HookNotPlannedException ex) {
                // Then it is no problem.
            }
            calendarHook = null;
        }
    }
    
    /**
     * Get the textual representation of this breakpoint. This method it to be
     * called from the gui thread.
     */
    synchronized public String toString() {
        return "Calendar breakpoint: time = " +
                CalendarPlanner.simulationTimeToString(sTime);
    }
    
    /** Helper class for a Calendar listener. */
    protected class CalendarBreakpointListener implements
            Listener, Serializable {
        
        public void changed(Hook initiator) {
            performAction();
        }
        
        public void canceled(Hook initiator) {
            // No need to do anything.
        }

        public String toString() {
            return CalendarBreakpoint.this.toString();
        }
    }
}
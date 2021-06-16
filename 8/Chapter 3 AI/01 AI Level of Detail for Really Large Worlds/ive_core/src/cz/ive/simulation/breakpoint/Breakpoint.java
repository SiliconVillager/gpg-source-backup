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
import cz.ive.simulation.CalendarPlanner;
import cz.ive.simulation.SchedulerImpl;
import java.io.Serializable;

/**
 * Base class for all breakpoints.
 *
 * @author ondra
 */
abstract public class Breakpoint implements Serializable {
    
    /** Is this breakpoint active? */
    protected boolean active = false;
    
    /** Creates a new instance of Breakpoint */
    public Breakpoint() {
    }
    
    /**
     * Stop the simulation.
     */
    protected void doBreak() {
        SchedulerImpl.instance().stopSimulation();
    }
    
    /** 
     * Perform the breakpoint action.
     */
    protected void performAction() {
        doBreak();
    }
    
    /**
     * Activates the Breakpoint.
     *
     * @throws BreakpointActivationFailedException on activation failure.
     */
    public void activate() throws BreakpointActivationFailedException {
        active = true;
        BreakpointList.instance().refreshBreakpoints();
    }
    
    /**
     * Deactivates the breakpoint.
     */
    public void deactivate() {
        active = false;
        BreakpointList.instance().refreshBreakpoints();
    }
    
    /**
     * Is the breakpoint active? This method is to be called also from the gui 
     * thread.
     *
     * @return <code>true</code> iff this breakpoint is currently active.
     */
    synchronized public boolean isActive() {
        return active;
    }
    
    /**
     * Get the textual representation of this breakpoint. This method it to be
     * called from the gui thread.
     */
    synchronized public String toString() {
        return "Abstract breakpoint";
    }
}
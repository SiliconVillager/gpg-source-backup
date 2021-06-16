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
import cz.ive.messaging.*;
import cz.ive.simulation.*;
import java.io.Serializable;
import java.util.*;

/**
 * This is a singleton, that holds references to all breakpoints.
 *
 * @author ondra
 */
public class BreakpointList implements Serializable {
    
    /** Reference to current singleton instance */
    private static BreakpointList instance;
    
    /** List of breakpoints */
    private List<Breakpoint> breakpoints;
    
    /** GuiHook for BreakpointList refresh. */
    GuiHook refreshHook = new GuiHook();
    
    /** Creates a new instance of BreakpointList */
    protected BreakpointList() {
        breakpoints = new LinkedList<Breakpoint>();
    }
    
    /**
     * Returns the current instance of the BreakpointList singleton.
     * This singleton can change during load process.
     *
     * @return current instance of BreakpointList singleton
     */
    static public synchronized BreakpointList instance() {
        if (instance == null) {
            instance = new BreakpointList();
        }
        return instance;
    }
    
    /**
     * Changes reference to current instance of BreakpointList singleton
     * Used with serialization - after loading.
     *
     * @param instance reference to new BreakpointList singleton
     */
    static public void setInstance(BreakpointList instance) {
        BreakpointList.instance = instance;
    }
    
    /**
     * Empty whole the BreakpointList before the XML load. We just drop
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        instance = new BreakpointList();
    }
    
    /**
     * Adds new breakpoint and activates it. This method can
     * be called from the gui thread.
     *
     * @param breakpoint New Breakpoint to be added to the list and activated.
     * @throws BreakpointActivationFailedException when the breakpoint
     *      activation fails.
     */
    synchronized public void addBreakpoint(Breakpoint breakpoint) throws
            BreakpointActivationFailedException {
        
        SchedulerImpl.instance().lockSimulation();
        
        try {
            if (!breakpoint.isActive()) {
                breakpoint.activate();
            }
            breakpoints.add(breakpoint);
        } finally {
            SchedulerImpl.instance().unlockSimulation();
        }
        
        refreshBreakpoints();
    }
    
    /**
     * Deletes the given Holdback. This method can be called from the
     * gui thread.
     *
     * @param breakpoint Breakpoint to be deactivated and discarded.
     */
    synchronized public void removeBreakpoint(Breakpoint breakpoint) {
        SchedulerImpl.instance().lockSimulation();
        
        if (breakpoint.isActive()) {
            breakpoint.deactivate();
        }
        breakpoints.remove(breakpoint);
        
        SchedulerImpl.instance().unlockSimulation();

        refreshBreakpoints();
    }
    
    /**
     * Activate/deactivate breakpoint.
     *
     * @param breakpoint Breakpoint to be Activated/deactivated.
     * @param activate <code>true</code> iff the break point should become
     *      activated, <code>false</code> for deactivation.
     * @throws BreakpointActivationFailedException when the breakpoint
     *      activation fails.
     */
    synchronized public void setActive(
            Breakpoint breakpoint, boolean activate)  throws
            BreakpointActivationFailedException {
        
        SchedulerImpl.instance().lockSimulation();
        
        try {
            if (activate && !breakpoint.isActive()) {
                breakpoint.activate();
            } else if (!activate && breakpoint.isActive()) {
                breakpoint.deactivate();
            }
        } finally {
            SchedulerImpl.instance().unlockSimulation();
        }
    }
    
    /**
     * Retrieves the Breakpoints list. This method is intended to be called from
     * the gui thread. It can even alter the breakpoints in the list, but only
     * when they are deactivated (all breakpoints are obliged to ignore all
     * attempts to their changes if active).
     *
     * @return copy of the list with all Breakpoints.
     */
    synchronized public List<Breakpoint> getBreakpoints() {
        return new Vector<Breakpoint>(breakpoints);
    }
    
    /**
     * Getter for the Gui refresh hook.
     *
     * @return GuiHook signaled on every holdback list change.
     */
    public GuiHook getRefreshHook() {
        return refreshHook;
    }
    
    /**
     * Signal the refresh hook. Some of the breakpoints were updated.
     */
    public void refreshBreakpoints() {
        refreshHook.notifyListeners();
    }
}

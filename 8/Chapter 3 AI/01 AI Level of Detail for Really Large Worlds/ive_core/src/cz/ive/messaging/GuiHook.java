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
 
package cz.ive.messaging;

import cz.ive.simulation.SchedulerImpl;
import java.io.*;
import java.util.*;
import javax.swing.SwingUtilities;

/**
 * Hook used for signalization from inside the simulation to the Gui.
 * It is Serializable, but it is left empty after the load, because
 * the Gui is not being Serialized.
 * All comunication is posponed to the Swing thread and the Simulation lock
 * is locked to ensure correct synchronization.
 *
 * @author Ondra
 */
public class GuiHook implements Hook, Serializable {
    
    /** 
     * Set of registered listeners 
     */
    transient protected HashSet<Listener> listeners;
    
    /** 
     * Creates a new instance of SyncHook 
     */
    public GuiHook() {
        listeners = new HashSet<Listener>();
    }
    
    /** 
     * Registration. Listener will be called back on
     * every change in state of this Hook
     *
     * @param  listener callback
     */
    synchronized public void registerListener(Listener listener) {
        listeners.add(listener);
    }
    
    /** 
     * Unregistration. Listener will not be called back anymore
     *
     * @param  listener callback to be removed from active listeners list
     */
    synchronized public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }
    
    /** 
     * Unregisters all registered listenners 
     */
    synchronized public void unregisterAll() {
        listeners.clear();
    }
    
    /** 
     * Pulls legs of all registered listenners 
     */
    synchronized public void notifyListeners() {

        Object[] array = listeners.toArray();
        for (Object o : array) {
            SwingUtilities.invokeLater(
                new LockedCaller((Listener) o, true));
        }
    }

    /** 
     * Notifies listeners that the hook is canceled (will no more notify
     * anything.
     */
    synchronized public void notifyListenersCancel() {
        for (Listener listener : listeners) { 
                SwingUtilities.invokeLater(
                    new LockedCaller(listener, false));
        }
    }
    
    /**
     * Writes out the object into the object stream.
     * There is a hack, because we must save id before everything
     * else if we are instance of IveIdImpl.
     * 
     * @param s stream to be filled with description of this object
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
	// Write out any hidden serialization magic
	s.defaultWriteObject();
    }

    /**
     * Loads contents of this object from the object stream.
     * There is a hack, because we must load id before everything
     * else if we are instance of IveIdImpl.
     * 
     * @param s stream to be used to load the description of this object.
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
	// Read in any hidden serialization magic
	s.defaultReadObject();
        
        listeners = new HashSet<Listener>();
    }
    
    /** 
     * This class takes care about calling the listener with the Simulation
     * lock locked.
     */
    protected class LockedCaller implements Runnable {
        
        protected Listener listener;
        protected boolean change;
        
        public LockedCaller(Listener listener, boolean change) {
            this.listener = listener;
            this.change = change;
        }
        
        public void run() {
            SchedulerImpl.instance().lockSimulation();
            if (change)
                listener.changed(GuiHook.this);
            else
                listener.changed(GuiHook.this);
            SchedulerImpl.instance().unlockSimulation();
        }
    }
}

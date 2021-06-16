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
 
package cz.ive.genius;

import cz.ive.simulation.Updateable;
import java.util.*;

/**
 * List of all active geniuses. It is to be used by a Gui in GeniusTree.
 * This class is a Singleton.
 *
 * @author ondra
 */
public class GeniusList implements java.io.Serializable, Updateable {
    
    /** The only instance of this class */
    protected static GeniusList instance;
    
    /** Does any of the geniuses need an update? */
    protected boolean dirty = false;
    
    /** List of active geniuses */
    public Set<Genius> geniuses;
    
    /** Queue of the dirty geniuses */
    public Queue<Genius> dirtyGeniuses;
    
    /** 
     * Creates a new instance of GeniusList 
     */
    private GeniusList() {
        geniuses = new HashSet<Genius>();
        dirtyGeniuses = new LinkedList<Genius>();
    }
    
    /** 
     * Geniuses list getter.
     */
    public Set<Genius> getGeniuses() {
        return geniuses;
    }
    
    /**
     * Registers a new genius.
     *
     * @param genius new active genius.
     */
    public void registerGenius(Genius genius) {
        geniuses.add(genius);
    }
    
    /**
     * Unregisters a genius that is no more active.
     *
     * @param genius genius to be unregistered.
     */
    public void unregisterGenius(Genius genius) {
        geniuses.remove(genius);
        dirtyGeniuses.remove(genius);
    }
    
    /** 
     * Singleton getter.
     */
    synchronized public static GeniusList instance() {
        return instance == null ? instance = new GeniusList() : instance;
    }
    
    /** 
     * After-load Singleton replacement 
     * 
     * @param newInstance a loaded instance to be assigned as new singleton
     */
    synchronized public static void setInstance(GeniusList newInstance) {
        instance = newInstance;
    }
    
    /** 
     * Empty whole the GeniusList before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        instance = new GeniusList();
    }
    
    /**
     * Mark genius as dirty.
     * 
     * @param genius Genius to be updated at the end of the evaluation.
     */
    public void markDirty(Genius genius) {
        if (dirtyGeniuses.isEmpty()) {
            dirty = true;
        }
        dirtyGeniuses.offer(genius);
    }
    
    /**
     * Perform posponded actions
     */
    public void update() {
        while(!dirtyGeniuses.isEmpty()) {
            Genius genius = dirtyGeniuses.poll();
            genius.update();
        }
        dirty = false;
    }
    
    /**
     * Asks if there is an reason for invoking update()
     * @return true if there is any waiting action
     */
    public boolean needUpdate() {
        return dirty;
    }
}

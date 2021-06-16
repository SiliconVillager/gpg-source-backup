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


/**
 * This class remembers set of instances of classes that implements Updateable
 * interface and if method update is invoked, it calls update methods of all 
 * remembered classes.
 * Note, that this class itself implements Updateable interface so that it's 
 * instance can be remembered too. It enables us to create tree hierarchy
 * Order of updating of remembered objects is given by order of insertions.
 * 
 * @author thorm
 */

public class Updator implements Updateable, Listener, java.io.Serializable {

    /**
     * Instance of the root updator.
     * All other updators are stored in it
     */    
    private static Updator rootInstance;
    
    /**
     * List of the objects that Updator manages
     */
    private java.util.List<Updateable> l;

    /**
     * Creates a new instance of ExprDAGSet 
     */
        
    
    public Updator() {
        l = new java.util.LinkedList<Updateable>();
    }
    
    public static Updator getRootInstance() {
        if (rootInstance == null) {
            rootInstance = new Updator();
        }
        return rootInstance;
    }
    
    public static void setRootInstance(Updator newRoot) {
        rootInstance = newRoot;
    }
    
    /**
     * Empty whole the root Updator before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyRootInstance() {
        rootInstance = new Updator();
    }
    
    /**
     * Insert d into the list of managed Updateable objects
     *
     * @param d new item
     */
    public void insert(Updateable d) {
        l.add(d);
    }
    
    /**
     * Remove d from the list of managed Updateable objects
     *
     * @param d item to be removed
     */
    public void remove(Updateable d) {
        l.remove(d);
    }
                
    public void changed(Hook initiator) {        
        update();
    }
    
    public void canceled(Hook initiator) {}
    
    /**
     * Update all managed objects
     */
    public void update() {
        for (Updateable item:l) {
            item.update();
        }
    }
    
    /**
     * @return true if any of the managed objects wants to be updated
     */
    public boolean needUpdate() {
        
        for (Updateable i:l) {
            if (i.needUpdate()) {
                return true;
            }
        }
        return false;
    }
    
}

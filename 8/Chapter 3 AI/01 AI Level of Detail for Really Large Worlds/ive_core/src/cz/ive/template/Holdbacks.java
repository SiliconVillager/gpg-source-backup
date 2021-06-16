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
 
package cz.ive.template;

import cz.ive.iveobject.*;
import cz.ive.location.*;
import cz.ive.lod.Holdback;
import cz.ive.logs.Log;
import cz.ive.messaging.*;
import cz.ive.simulation.*;
import cz.ive.xmlload.ObjectTemplate;
import java.io.Serializable;
import java.util.*;

/**
 * Holdback evidence for their easier management.
 *
 * @author ondra
 */
public class Holdbacks implements Serializable {
    
    /** Reference to current singleton instance */
    private static Holdbacks instance;
    
    /** List of holdbacks */
    private List<IveObject> holdbacks;
    
    /** Holdback id counter */
    private int holdbackId = 1;
    
    /** GuiHook for HoldbackList refresh */
    GuiHook refreshHook = new GuiHook();
    
    /** Creates a new instance of Holdbacks */
    protected Holdbacks() {
        holdbacks = new LinkedList<IveObject>();
    }
    
    /**
     * Returns the current instance of the Holdbacks singleton.
     * This singleton can change during load process.
     *
     * @return current instance of Holdbacks singleton
     */
    static public synchronized Holdbacks instance() {
        if (instance == null) {
            instance = new Holdbacks();
        }
        return instance;
    }
    
    /**
     * Changes reference to current instance of Holdbacks singleton
     * Used with serialization - after loading.
     *
     * @param instance reference to new Holdbacks singleton
     */
    static public void setInstance(Holdbacks instance) {
        Holdbacks.instance = instance;
    }
    
    /**
     * Empty whole the Holdbacks before the XML load. We just drop
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        instance = new Holdbacks();
    }
    
    /**
     * Creates Holdback at the location specified by its id. This method can
     * be called from the gui thread. Since the code is executed asynchronously,
     * there is no way how to return the possible error, so call the method
     * prevalidteHoldback fisrt to do at least the basic checks, before calling
     * this one.
     *
     * @param position of the target location.
     * @param lod minimal lod assured by the new Holdback
     */
    synchronized public boolean createHoldback(final String position,
            final int lod) {
        
        SchedulerImpl.instance().lockSimulation();
        
        // Create the holdback IveObject
        ObjectTemplate holdbackTmp =
                (ObjectTemplate)TemplateMap.instance().getTemplate(
                "Holdback");
        if (holdbackTmp == null) {
            SchedulerImpl.instance().unlockSimulation();
            Log.addMessage("Holdback template not found.",
                    Log.SEVERE, "", "", "");
            return false;
        }
        
        // Choose the id
        IveObject obj = null;
        for (;holdbackId<Integer.MAX_VALUE;) {
            obj = holdbackTmp.instantiate("Holdback"+holdbackId++);
            if (obj != null)
                break;
        }
        IveObject holdback = obj;
        
        // Initiate
        holdback.setHoldback(new Holdback(1, lod));
        
        // Find place
        IveId posId = new IveIdImpl(position);
        IveObject parent = ObjectMap.instance().getObject(position);
        if (parent == null) {
            parent = posId.getLeastActiveParent();
        }
        if (parent == null) {
            Log.addMessage("Holdback location is invalid",
                    Log.SEVERE, "", "", "");
            ObjectMap.instance().unregister(holdback.getId());
            
            SchedulerImpl.instance().unlockSimulation();
            return false;
        }
        WayPoint pos;
        if (parent instanceof WayPoint) {
            pos = (WayPoint)parent;
        } else {
            pos = parent.getPosition();
        }
        
        // Place it
        if (!pos.placeObject(holdback, null, new WayPointImpl(position,
                new float[2]))) {
            ObjectMap.instance().unregister(holdback.getId());
            refreshHook.notifyListeners();
            
            SchedulerImpl.instance().unlockSimulation();
            return false;
        }
        holdbacks.add(holdback);
        refreshHook.notifyListeners();
        
        SchedulerImpl.instance().updateWorld();
        SchedulerImpl.instance().unlockSimulation();
        return true;
    }
    
    /**
     * Deletes the given Holdback. This method can be called from the
     * gui thread.
     *
     * @param holdback Holdback to be deleted
     */
    synchronized public void removeHoldback(final IveObject holdback) {
        SchedulerImpl.instance().lockSimulation();
        
        holdbacks.remove(holdback);
        if (holdback.getPosition() != null) {
            holdback.getPosition().removeObject(holdback);
        }
        holdback.setObjectState(IveObject.ObjectState.NOT_EXIST);
        ObjectMap.instance().unregister(holdback.getId());
        refreshHook.notifyListeners();
        
        SchedulerImpl.instance().updateWorld();
        
        SchedulerImpl.instance().unlockSimulation();
    }
    
    /**
     * Prevalidates the future position of the holdback. This method can
     * be called from the gui thread.
     *
     * @param position of the target location.
     * @return <code>false</code> iff the Holdback surely cannot be placed
     *      on the given location. But <code>true</code> does not necessarily
     *      assure successfull call to createHoldback.
     */
    synchronized public boolean prevalidateHoldback(String position) {
        // Check the position (as far as we can)
        SchedulerImpl.instance().lockSimulation();
        IveId posId = new IveIdImpl(position);
        IveObject parent = ObjectMap.instance().getObject(position);
        if (parent == null) {
            parent = posId.getLeastActiveParent();
        }
        SchedulerImpl.instance().unlockSimulation();
        if (parent == null) {
            return false;
        }
        return true;
    }
    
    /**
     * Retrieves the Holdback list. This method is intended to be called from
     * the gui thread.
     *
     * @return copy of the list with all manually added Holdbacks.
     */
    synchronized public List<IveObject> getHoldbacks() {
        return new Vector<IveObject>(holdbacks);
    }
    
    /**
     * Getter for the Gui refresh hook.
     *
     * @return GuiHook signaled on every holdback list change.
     */
    public GuiHook getRefreshHook() {
        return refreshHook;
    }
}

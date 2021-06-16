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

import cz.ive.iveobject.*;
import cz.ive.location.*;
import cz.ive.logs.Log;
import cz.ive.manager.*;
import cz.ive.messaging.*;
import cz.ive.process.*;
import cz.ive.sensors.Sensor;
import java.util.*;
import java.io.Serializable;

/**
 * This class offers methods for keeping phantoms updated.
 * It tries not to duplicate passiveUpdateQuerries and to
 * unregister them when not needed.
 *
 * @author Ondra
 */
public class PhantomKeeper implements Listener, Serializable {
    
    /** Map from Object Id to actual Phantom */
    protected Map<String, PhantomInfo> phantomMap =
            new HashMap<String, PhantomInfo>();
    
    /** Map from Sources to actual Phantom */
    protected Map<Hook, SrcHookInfo> hookMap =
            new HashMap<Hook, SrcHookInfo>();
    
    /** List of sensors */
    protected List<Sensor> sensors;
    
    /** The owner genius. */
    protected Genius genius;
    
    /**
     * Creates new instance of this class
     *
     * @param genius The owner genius.
     */
    public PhantomKeeper(Genius genius) {
        this(new ArrayList<Sensor>(), genius);
    }
    
    /**
     * Creates new instance of this class
     *
     * @param sensors list of sensors available for updates
     * @param genius The owner genius.
     */
    public PhantomKeeper(List<Sensor> sensors, Genius genius) {
        this.sensors = sensors;
        this.genius = genius;
    }
    
    /**
     * Registers all elements of the given substitution for a passive update.
     *
     * @param phantoms Substitution with possibly unregistered phantoms
     */
    public void registerSubstitution(Substitution phantoms) {
        Map<String, Slot> slots = phantoms.getSlots();
        
        for (Slot slot : slots.values()) {
            List<Source> list = slot.getSourceArrayList();
            for (Source src : list) {
                IveObject srcObj = src.getObject();
                // Is the source filled? Yes - register it with the phantom
                if (srcObj != null) {
                    IveObject dstObj = registerPhantom(srcObj);
                    if (dstObj != srcObj)
                        src.setObject(dstObj);
                    
                    SrcHookInfo info = hookMap.get(src);
                    
                    if (info == null) {
                        PhantomInfo phInfo =
                                phantomMap.get(src.getObject().getId());
                        
                        assert (phInfo != null);
                        
                        info = new SrcHookInfo(phInfo, src);
                        hookMap.put(src, info);
                    } else {
                        info.inc();
                    }
                } else { // No - register only the Source
                    SrcHookInfo info = hookMap.get(src);
                    
                    if (info == null) {
                        info = new SrcHookInfo(null, src);
                        hookMap.put(src, info);
                    } else {
                        info.inc();
                    }
                }
            }
        }
    }
    
    /**
     * Unregisters all elements of the given substitution from a passive update.
     *
     * @param phantoms Substitution with registered phantoms
     */
    public void unregisterSubstitution(Substitution phantoms) {
        Map<String, Slot> slots = phantoms.getSlots();
        
        for (Slot slot : slots.values()) {
            List<Source> list = slot.getSourceArrayList();
            for (Source src : list) {
                SrcHookInfo info = hookMap.get(src);
                
                
                if (info != null && info.dec()) {
                    hookMap.remove(src);
                }
                if (src.getObject() != null) {
                    unregisterPhantom(src.getObject());
                }
            }
        }
    }
    
    /**
     * Registers given phantom for passive update.
     *
     * @param phantom possibly unregistered phantom to be registered
     * @return registered phantom, not necessarily the same instance.
     */
    public IveObject registerPhantom(IveObject phantom) {
        String id = phantom.getId();
        PhantomInfo info = phantomMap.get(id);
        if (info == null) {
            if (!phantom.isPhantom()) {
                if (phantom instanceof WayPoint) {
                    phantom = new WayPointImpl(phantom.getId(),
                            ((WayPoint)phantom).getRealPosition());
                } else {
                    phantom = new IveObjectImpl(phantom.getId());
                }
            }
            info = new PhantomInfo(phantom);
            phantomMap.put(id, info);
        } else {
            info.inc();
        }
        return info.phantom;
    }
    
    /**
     * Registers given phantom for passive update.
     *
     * @param phantom possibly unregistered phantom to be registered
     * @param refCount number of references to be added
     * @return registered phantom, not necessarily the same instance.
     */
    public IveObject registerPhantom(IveObject phantom, int refCount) {
        String id = phantom.getId();
        PhantomInfo info = phantomMap.get(id);
        if (info == null) {
            if (!phantom.isPhantom()) {
                if (phantom instanceof WayPoint) {
                    phantom = new WayPointImpl(phantom.getId(),
                            ((WayPoint)phantom).getRealPosition());
                } else {
                    phantom = new IveObjectImpl(phantom.getId());
                }
            }
            info = new PhantomInfo(phantom);
            phantomMap.put(id, info);
            info.refCount += refCount - 1;
        } else {
            info.refCount += refCount;
        }
        return info.phantom;
    }
    
    /**
     * Unregisters given phantom from a passive update.
     *
     * @param phantom previously registered phantom
     */
    public void unregisterPhantom(IveObject phantom) {
        String id = phantom.getId();
        PhantomInfo info = phantomMap.get(id);
        
        if (info != null && info.dec()) {
            phantomMap.remove(id);
        }
    }
    
    /**
     * Unregisters given phantom from a passive update.
     *
     * @param phantom previously registered phantom
     * @param refCount number of references to be removed
     */
    public void unregisterPhantom(IveObject phantom, int refCount) {
        String id = phantom.getId();
        PhantomInfo info = phantomMap.get(id);
        
        if (info != null && info.dec(refCount)) {
            phantomMap.remove(id);
        }
    }
    
    /**
     * Unregisters all passive updates.
     */
    public void unregisterAll() {
        for (PhantomInfo info : phantomMap.values()) {
            info.unregister();
        }
        phantomMap.clear();
    }
    
    /**
     * Changes usable sensors to a given list.
     *
     * @param sensors list of sensors available for passive update
     */
    public void changeSensors(List<Sensor> sensors) {
        this.sensors = sensors;
        sensorsChanged();
    }
    
    /**
     * Adds given sensor to list of usable sensors.
     *
     * @param sensor new usable sensor to be used for passive updates
     */
    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
        sensorsChanged();
    }
    
    /**
     * emoves given sensor from list of usable sensors.
     *
     * @param sensor sensor newly unusable for passive updates.
     */
    public void removeSensor(Sensor sensor) {
        sensors.remove(sensor);
        sensorsChanged();
    }
    
    /**
     * Sensors were changed, notify ManagerOfSenses.
     */
    protected void sensorsChanged() {
        for (PhantomInfo info : phantomMap.values()) {
            info.sensorsChanged();
        }
    }
    
    /**
     * Some of the sources have changed. We must unregister a register old and
     * new phantoms.
     * @param initiator origin of message
     */
    public void changed(Hook initiator) {
        SrcHookInfo info = hookMap.get(initiator);
        
        if (info == null) {
            Log.addMessage("Unexpected signal", Log.WARNING, genius.getId(),
                    "", "");
            return;
        }
        
        IveObject srcObj = ((Source)initiator).getObject();
        
        if (info.info != null) {
            unregisterPhantom(info.info.phantom, info.refCount);
        }
        
        if (srcObj != null) {
            IveObject dstObj = null;
            dstObj = registerPhantom(srcObj, info.refCount);
            info.info = phantomMap.get(dstObj.getId());
            
            if (srcObj != dstObj) {
                ((Source)initiator).setObject(dstObj);
            }
        } else {
            info.info = null;
        }
    }
    
    public void canceled(Hook initiator) {
        // This should not happen
        Log.addMessage("Unexpected cancel signal", Log.WARNING, genius.getId(),
                "", "");
    }
    
    /**
     * Helper container class storing info about
     * each updated phantom
     */
    protected class PhantomInfo implements Serializable {
        public int refCount = 1;
        public IveObject phantom;
        public QTUpdate updateToken;
        
        /**
         * Creates new instance of the PahntomInfo. It also registers given
         * phantom in the ManagerOfSenses for passive update.
         *
         * @param phantom Phantom to be stored in this class
         */
        public PhantomInfo(IveObject phantom) {
            if (null == (updateToken = ManagerOfSenses.instance().
                    updateCopyPassive(sensors, phantom))) {
                Log.addMessage("ManagerOfSenses rejected to do the " +
                        "passive update of phantom "+phantom.getId() +
                        ". This should cause no direct problems if " +
                        "the object just does not exist",
                        Log.WARNING, genius.getId(), "", "");
            }
            this.phantom = phantom;
        }
        
        /**
         * Sensors were changed, notify the ManagerOfSenses.
         */
        public void sensorsChanged() {
            if (updateToken != null) {
                updateToken.changeSensors(sensors);
            }
        }
        
        /**
         * Icreases number of references.
         */
        public void inc() {
            refCount++;
        }
        
        /**
         * Decreses the number of references. And unregisters the passive
         * update if this was the last reference.
         *
         * @return <code>true</code> iff this was the last reference
         */
        public boolean dec() {
            refCount--;
            
            if (refCount == 0) {
                unregister();
                return true;
            }
            return false;
        }
        
        /**
         * Decreases the number of references. And unregisters the passive
         * update if this was the last reference.
         *
         * @param refCount Number of references to be removed
         * @return <code>true</code> iff this was the last reference
         */
        public boolean dec(int refCount) {
            if (this.refCount <= refCount) {
                this.refCount = 0;
                unregister();
                return true;
            } else {
                this.refCount -= refCount;
                return false;
            }
        }
        
        /**
         * Unregister this passive update.
         */
        public void unregister() {
            if (updateToken != null) {
                ManagerOfSenses.instance().unregisterPassive(updateToken);
            }
        }
    }
    
    /**
     * Helper container class storing info about hooks that has to be answered.
     */
    protected class SrcHookInfo implements Serializable {
        public int refCount = 1;
        public PhantomInfo info;
        public Hook hook;
        
        /**
         * Creates new instance of the SrcHookInfo.
         *
         * @param info Phantom info associated with the given source hook
         * @param hook Source hook
         */
        public SrcHookInfo(PhantomInfo info, Hook hook) {
            this.info = info;
            this.hook = hook;
            hook.registerListener(PhantomKeeper.this);
        }
        
        /**
         * Icreases number of references.
         */
        public void inc() {
            refCount++;
        }
        
        /**
         * Decreses the number of references.
         *
         * @return <code>true</code> iff this was the last reference
         */
        public boolean dec() {
            refCount--;
            
            assert (refCount >= 0);
            
            if (refCount == 0) {
                hook.unregisterListener(PhantomKeeper.this);
                return true;
            }
            return false;
        }
    }
}

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
 
package cz.ive.lod;


import cz.ive.IveApplication;
import cz.ive.iveobject.IveId;
import cz.ive.iveobject.IveObject;
import cz.ive.location.*;
import cz.ive.logs.*;
import cz.ive.messaging.Listener;
import cz.ive.simulation.SchedulerImpl;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;


/**
 *
 * This component gets information about object movement. Using this information
 * it manages the location expansions and shrinks
 *
 * @author thorm
 */
public class LodManager implements MoveAware, java.io.Serializable, Listener {
    
    /**
     * Comparator for Marks
     * It is designed to enforce that in the Set using this comparator can not
     * be succesor and descendant at one moment.
     */
    static protected class MarkComparator implements Comparator<IveId>,
            java.io.Serializable {
        
        public MarkComparator() {}
        /**
         * Two IveIds are considered to be equal if one is parent of another or has
         * the same fully qualified name
         */
        public int compare(IveId o1, IveId o2) {
            if (o1.isParent(o2)) {
                return 0;
            }
            if (o2.isParent(o1)) {
                return 0;
            }
            if (o1.getId().equals(o2.getId()));
            return o1.getId().compareTo(o2.getId());
        }
    }
    
    
    /**
     * Represents the LOD membrane.
     * Set of marked locations. There can not appear a pair of locations such
     * that one location is descendant of another.
     */
    static class MarkStructure extends java.util.TreeSet<Area> {
        public MarkStructure() {
            super(new MarkComparator());
        }
    }
    
    /**
     * The movements are processed by the manager once per calendar event.
     * They are accumulated in the JAM between two events
     */
    private Jam j;
    
    /**
     * LOD manager is singleton
     */
    private static LodManager instance;
    
    /** Creates a new instance of LodManager */
    private LodManager() {
        reasonsToStay = new HashMap<String, HashSet<String>>();
        marks = new MarkStructure();
        visited = new HashSet<Area>();
        if (IveApplication.instance().noLod){
            j = new DummyJamImpl();
        }else{
            j = new JamImpl(this);
        }
    }
    
    /**
     * Get the singleton instance.
     * If it does not exist yet it create it and register at clean hook.
     * @return The singleton instance
     */
    public static synchronized LodManager instance() {
        if (instance == null) {
            instance = new LodManager();
            if (!IveApplication.instance().noLod){
                instance.registerAtCleanHook();
            }
        }
        return instance;
    }
    
    /**
     * Empty whole the LodManager before the XML load. We just drop
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        if (instance!=null && !IveApplication.instance().noLod)
            instance.unregisterAtCleanHook();
        instance = new LodManager();
        if (!IveApplication.instance().noLod){
                instance.registerAtCleanHook();
            }
    }
    
    /**
     * Set the instance.
     * Used to load serialized instance
     * @param newInstance new instance ( typically obtained from serialized file)
     */
    public static void setInstance(LodManager newInstance) {
        if (instance!=null) instance.unregisterAtCleanHook();
        instance = newInstance;
        if (!IveApplication.instance().noLod){
                instance.registerAtCleanHook();
            }
    }
    
    
    /**
     * Getter for the Jam instance that cummulates the movements for the Lod
     * manager
     * @return Jam instance
     */
    public static Jam getJam() {
        return instance().j;
    }
    
    
    private void unregisterAtCleanHook() {
        SchedulerImpl.instance().getCleaningHook().unregisterListener(this);
    }
    
    
    private void registerAtCleanHook() {
        SchedulerImpl.instance().getCleaningHook().registerListener(this);
    }
    
    /**
     * For each area that was passed by addobject() method remember set of the
     * objects in its subtree that forces it to stay expanded.
     * In the other words it keeps all objects in the area subtree such that
     * area lod is between object existence and view level.
     */
    private HashMap<String, HashSet<String>> reasonsToStay;
    
    /**
     * Keeps all Areas that can be shrinked without corrupting "basic" rules
     * Some of them can be prevented from shrinking for another reason
     * (influences)
     */
    private MarkStructure marks;
    
    /**
     * Temporary place for locked Areas from toShrink used during cleanup
     */
    private HashSet<Area> visited;
    
    /**
     * Insert the new object among reasons to stay expanded of the location.
     * If this is the first reason to stay for the particular location create
     * new item in the map.
     */
    private void insertIntoReasons(String a, IveObject o) {
        HashSet<String> areaReasons = reasonsToStay.get(a);
        
        if (areaReasons == null) {
            areaReasons = new HashSet<String>();
            reasonsToStay.put(a, areaReasons);
        }
        String objId = o.getId();
        areaReasons.add(objId);
    }
    
    /**
     *  Remove the object from the reasons to stay of the particular location.
     *  If this is the last reason record of this location the location is
     *  removed from the map.
     */
    private boolean removeFromReasons(String a, IveObject o) {
        HashSet<String> areaReasons = reasonsToStay.get(a);
        
        if (areaReasons != null) {
            areaReasons.remove(o.getId());
            if (areaReasons.isEmpty()){
                areaReasons = null ;
            }
            return true;
        }
        return false;
    }
    
    /**
     * This method is called time by time by the framework to shrink unnecessary
     * locations.
     *
     * DFS traversal is used to mark locations that can be shrung.
     * All marked locations are shrung
     * @param h Hook that caused this method invocation.
     *          The value is not used in the method body
     */
    public void changed(cz.ive.messaging.Hook h) {
        marks.clear();
        visited.clear();
        Area root = IveMapImpl.instance().getRoot();
        if (root==null){
            Log.warning("Cleanup: Root location does not exist");
            return;
        }
        markRecursive(root);
        for (Area a:marks) {
            if (a.getLocationState() != LocationState.ATOMIC) {
                Log.info("Lod manager : shrinking "+a.getId());
                a.shrink();
                reasonsToStay.remove(a);
            }
        }
    
        
    }
    
    
    
    /**
     * Needed to implement Listenner interface
     * Empty body
     */
    public void canceled(cz.ive.messaging.Hook h) {}
    
    /**
     * Called when object enters location.
     * Should be called only by JAM ( to enforce correct order of operations)
     *
     * In case that object holdbacks does not comply the basic rules it expands
     * the location. As result of the expansion it might be called again on the
     * one of its sublocations.
     * It checks influences too and expand their targets if needed
     *
     * It finds all location succesors that can not be shrinked until the object
     * stay on the location and adds it among 'reasons to stay' of such succesor.
     *
     * @param object Object that is new in the location
     * @param location location where the object is new
     */
    public void addObject(IveObject object, WayPoint location) {
        WayPoint root = IveMapImpl.instance().getRoot();
        addObject(object, location, root);
        
        if (isInvalidArea(object, root)){
            insertIntoReasons(root.getId(), object);
        }
        
    }
    
    /**
     * Return whether the given location can stay atomic if the object is
     * added to it.
     *
     * @param object object to be added
     * @param location location where we want to place the object
     * @return true if the location must be expanded
     */
    
    protected boolean isInvalidArea(IveObject object,IveObject  location){
        Holdback h = object.getHoldback();
        int vLevel = h.getView();
        int eLevel = h.getExistence();
        int lLod = location.getLod();
        return (eLevel<= lLod && lLod < vLevel);
    }
    
    
    /**
     *  Implementation of addObject that make changes only on the given subtree.
     *  public addObject() is than implemented by invoking this function with
     *  the root passed as subtree parameter and moveObject is implemented by
     *  invoking subtree versions of addObject and removeObject with the closest
     *  common succesor passed as subtree argument.
     *
     * @param object Object that is new in the location
     * @param location location where the object is new
     * @param subtree the changes that can be caused by the change of objects
     *        position can influence only this subtree
     */
    protected void addObject(
            IveObject object,
            WayPoint location,
            WayPoint subtree) {
        
        Holdback h = object.getHoldback();
        int existence = h.getExistence();
        int view = h.getView();
        int locLod = location.getLod();
        int subtreelod = subtree.getLod();
        
        String parent = location.getId();
        if (existence <= locLod && locLod < view) {
            if (location.getLocationState() == LocationState.ATOMIC) {
                try {
                    location.expand();
                } catch (Exception e) {
                    IveApplication.printStackTrace(e);
                    Log.severe(e.toString());
                }
            }
        }
        
        while(locLod>=view){
            parent = getParentId(parent);
            locLod--;
        }
        
        while(locLod>=existence){
            
            if (parent == null) {
                break;
            }
            
            if (locLod == subtreelod) {
                break;
            }
            
            insertIntoReasons(parent, object);
            parent = getParentId(parent);
            locLod--;
        }
        
        
        manageInfluencesforAdd(object, location, subtree);
    }
    
    /**
     * For each influence heading from the location to some target expands
     * targets succesors so that the lod of the closest expanded targets succesor
     * differs from the location lod not more thanthe value associated with
     * influence edge
     *
     * @param object Object that is new in the location
     * @param location location where the object is new
     * @param subtree the changes that can be caused by the change of objects
     *        position can influence only this subtree
     */
    protected void manageInfluencesforAdd(
            IveObject object,
            WayPoint location,
            WayPoint subtree
            ) {
        
        Holdback h = object.getHoldback();
        int existence = h.getExistence();
        int view = h.getView();
        int locLod = location.getLod();
        
        do {
            Influence[] influences = subtree.getInfluences();
            
            if (influences != null) {
                for (Influence i:influences) {
                    WayPoint target = i.target;
                    WayPoint ancestor = (WayPoint) target.getLeastActiveParentWithLODLEThan(
                            (Math.min(locLod, existence) - i.value));
                    
                    while (ancestor != target
                            && ancestor.getLod()
                            < (Math.min(locLod, view) - i.value)) {
                        if (ancestor.getLocationState() == LocationState.ATOMIC) {
                            try {
                                ancestor.expand();
                            } catch (Exception e) {
                                IveApplication.printStackTrace(e);
                                Log.severe(e.toString());
                            }
                        }
                        ancestor = (Area) ancestor.getChildPreceeding(
                                target.getId());
                    }
                }
            }
            subtree = (WayPoint) subtree.getChildPreceeding(location.getId());
        }while (subtree != location && subtree != null);
        // null appears when invoked with location == subtree
    }
    
    
    /**
     * Update structures and perform expands that are caused by object movement.
     * The update is performed only on the subtree given by the closest common
     * parent of oldPos and newPos.
     *
     * @param object Object that is new in the location
     * @param newPos the new position of the object
     * @param oldPos the old position of the object
     */
    public void moveObject(IveObject object, WayPoint oldPos, WayPoint newPos) {
        WayPoint subtree = (WayPoint) oldPos.getLeastCommonParent(newPos);
        
        removeObject(object, oldPos, subtree);
        addObject(object, newPos, subtree);
    }
    
    /**
     * Remove the object from the reasons to stay of waypoint succesors it was
     * placed in. In the consequence of it the location can be shrung during
     * next cleanup.
     *
     * @param object Object that was removed from the location
     * @param location The position the object was removed from
     */
    public void removeObject(IveObject object, WayPoint location) {
        WayPoint root = IveMapImpl.instance().getRoot();
        removeObject(object, location, root);
        IveObject locParent = location.getParent();
        
        if (isInvalidArea(object, root)){
            removeFromReasons(root.getId(), object);
        }
        
    }
    
    
    /**
     * Remove the object from the reasons to stay of all waypoint succesors that
     * are in the subtree it was placed in.
     * In the consequence of it the location can be shrung during
     * next cleanup.
     *
     * @param object Object that was removed from the location
     * @param location The position the object was removed from
     * @param subtree the changes will be bound by this subtree
     */
    protected void removeObject(IveObject object, WayPoint location, WayPoint subtree) {
        if (location == subtree) {
            return;
        } // going down
        Holdback h = object.getHoldback();
        int view = h.getView();
        int existence = h.getExistence();
        String parent = getParentId(location.getId());
        
        if (parent == null) {
            return;
        } // there are not any locations that has object between it's reasons
        int wayPointLOD = location.getLod()-1;
        int subtreeLod = subtree.getLod();
        
        if (wayPointLOD+1 < existence) {
            Log.severe(
                    "Existence level of object is higher than " +
                    "LOD of location where it lived");
        } else {
            while (wayPointLOD >= view) {
                if (wayPointLOD == subtreeLod) {
                    return;
                }
                parent = getParentId(parent);
                wayPointLOD--;
            }
            while (wayPointLOD >= existence) {
                if (wayPointLOD == subtreeLod) {
                    return;
                }
                
                removeFromReasons(parent, object);
                
                parent = getParentId(parent);
                wayPointLOD--;
            }
        }
        
    }
    
    
    
    
    /**
     * Needed to implement MoveAware interface
     * Empty body
     */
    public void expand(WayPoint location) {}
    
    /**
     * Needed to implement MoveAware interface
     * Empty body
     */
    public void shrink(WayPoint location) {}
    
    
    /**
     * This is used during cleanup to mark all locations forming LOD membrane.
     * @param a The root location
     */
    protected void markRecursive(Area a) {
        if (!visited.add(a)) {
            return;
        }
        HashSet<String> aReasons = reasonsToStay.get(a.getId());
        
        if ((aReasons == null || aReasons.isEmpty()) // holds basic condition
        && !marks.contains(a)// does not have marked descendant
        ) {
            marks.add(a);
            reasonsToStay.remove(a);
        } else {
            for (WayPoint child:a.getWayPoints()) {
                if (child instanceof Area) {
                    markRecursive((Area) child);
                }
            }
        }
        Influence[] influences = a.getInfluences();
        
        if (influences != null) {
            for (Influence i:influences) {
                WayPoint targetAncestor = i.target;
                
                for (int j = 0; j < i.value; j++) {
                    targetAncestor = (WayPoint) targetAncestor.getParent();
                }
                if (marks.contains(targetAncestor)) {
                    Area markedAncestor = marks.tailSet((Area) targetAncestor).first();
                    
                    while (markedAncestor != targetAncestor) {
                        for (WayPoint child:markedAncestor.getWayPoints()) {
                            if (child instanceof Area) {
                                markRecursive((Area) child);
                            }
                            markedAncestor.getChildPreceeding(
                                    targetAncestor.getId());
                        }
                        
                    }
                }
            }
        }
    }
    
    /**
     * Removes the last part of identifier
     * @param id identifier
     * @return parent identifier
     */
    String getParentId(String id) {
        int lSep = id.lastIndexOf(IveId.SEP);
        
        if (lSep == -1) {
            return null;
        }
        
        return id.substring(0, lSep);
    }
    
}


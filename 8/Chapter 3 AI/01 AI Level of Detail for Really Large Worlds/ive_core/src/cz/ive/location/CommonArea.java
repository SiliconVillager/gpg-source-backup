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
 
package cz.ive.location;

import cz.ive.exception.*;
import cz.ive.genius.AreaGenius;
import cz.ive.iveobject.*;
import cz.ive.iveobject.IveObjectImpl.InherentObject;
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.manager.ManagerOfSenses;
import cz.ive.simulation.*;
import cz.ive.messaging.*;
import cz.ive.logs.*;
import cz.ive.util.Pair;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import cz.ive.xmlload.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Set;

/**
 * Common predecessor of all areas implementing methods, which are 
 * WayPoint-net-implementation independent.
 *
 * @author pavel
 */
public abstract class CommonArea extends WayPointImpl implements Area {

    /**
     * Defines which objets should be placed on which WayPoints when expanding.
     * Each object will be placed on the WayPoint with the most specific class
     * id on the way from the object's id to the root.
     */
    private HashMap<ObjectClass, List<WayPoint>> objectPlaces;
    
    /**
     * Information needed to expand the location.
     */
    private ObjectExpansionInfo expansionInfo;
    
    /**
     * Determines whether inherent object were generated yet from expansionInfo.
     * This is used during expansion to decide whether generate objects from
     * inherent map or from expansionInfo.
     */
    private boolean inherentObjectsGenerated;
    
    /**
     * Information needed to generate phantoms during expand.
     */
    protected PhantomGenerationInfo phantomGenerationInfo;
    
    /**
     * GuiHook to be signaled when expanding
     */
    protected GuiHook expandHook= new GuiHook();
    
    /**
     * GuiHook to be signaled when shrinking
     */
    protected GuiHook shrinkHook= new GuiHook();

    /** Phantom wayPoints of neighbour locations */
    protected List<Joint> borderWayPoints;

    /** Area genies */
    protected AreaGenius[] genies;
    
    /**
     * Creates a new instance of CommonArea
     * @param objectId id of the WayPoint
     * @param parent Parent area
     * @param realPosition coordinates of the new WayPoint
     * @param kind Kind of the area
     * @param info Expansion info
     */
    public CommonArea(String objectId, WayPoint parent, float[] realPosition, 
            Kind kind, ObjectExpansionInfo info) 
            throws ObjectRegistrationFailedException {

        super(objectId, parent, realPosition, kind);
        expansionInfo = info;
        objectPlaces = new HashMap<ObjectClass, List<WayPoint>>();
        borderWayPoints = new Vector<Joint>();
        inherentObjectsGenerated = false;
        phantomGenerationInfo = null;
    } 
    
    public void defineObjectPlace(ObjectClass objClass, WayPoint place) {
        if (! objectPlaces.containsKey(objClass)) { 
            objectPlaces.put(objClass, new Vector<WayPoint>());
        }
        objectPlaces.get(objClass).add(place);
    }
    
    public void undefineObjectPlace(ObjectClass objClass, WayPoint place) {
        List<WayPoint> places;
        if (objectPlaces.containsKey(objClass)) {
            places = objectPlaces.get(objClass);
            places.remove(place);
            if (places.isEmpty()) {
                objectPlaces.remove(objClass);
            }
        }
    }

    public void undefineObjectPlace(ObjectClass objClass) {
        objectPlaces.remove(objClass);
    }
    
    public void clearObjectPlaces() {
        objectPlaces.clear();
    }

    /**
     * Getter for WayPoints that are <b>behind</b> the border in the Area 
     * specified by a possibly empty WayPoint (with at least valid id).
     * @param neighbour WayPoint representing an adjacent Area
     * @return array of possibly empty WayPoint that are situated in the Area
     *      represented by neighbour parameter and that are accessible in 
     *      a single step.
     */
    public List<WayPoint> getBorderWayPoints(WayPoint neighbour) {

        Vector<WayPoint> wpList;
        
        wpList = new Vector<WayPoint>();

        for (Joint joint : borderWayPoints)
        {
            if (joint.target.isParent(neighbour)) {
                wpList.add(joint.target);
            }
        }
        
        return wpList;
    }
    
    /**
     * Finds in it's direct childs WayPoint which has the same id as the given
     * one, or it's id is a parent id of the given one. This is needed to find
     * the position for an object, which remembers it's position in the subtree,
     * but the childs were newly generated.
     * @param old The remembered position
     * @return The child WayPoint, null if it was not found
     */
    public WayPoint findNewWayPoint(WayPoint old) {
        WayPoint[] wp = getWayPoints();

        if (old == null) {
            return null;
        }
                
        for (int i=0; i<wp.length; i++) {
            if ((old.getId().compareTo(wp[i].getId()) == 0) ||
                (old.isParent(wp[i]))) {
                    return wp[i];
            }
        }
        return null;
    }
    
    /**
     * This method can define special placement for some objects.
     * @param object Object to find place for
     * @return null if the standard placement should be used, WayPoint if 
     * the standard placement should be overridden (the object will be placed
     * on this WayPoint), or IveObject if the object should be placed to slaves
     * of the return object.
     */
    protected IveObject findObjectPlaceSpecial(IveObject object) {
        return null;
    }
    
    public WayPoint findObjectPlace(IveObject object) {
        ObjectClass objectClass;
        WayPoint place;
        List<WayPoint> pseudoCandidates;
        List<WayPoint> candidates = new Vector<WayPoint>();
        
        objectClass = object.getObjectClass();
        while (objectClass != null) {
            pseudoCandidates = objectPlaces.get(objectClass);
            
            if ((pseudoCandidates != null) && (pseudoCandidates.size() > 0))
            {
                for (WayPoint wp : pseudoCandidates) {
                    if (wp.matchKind(object.getKind()) &&
                            ((!object.isSubstantial()) || 
                                ((wp.getSpaceState() != 
                                    WayPoint.SpaceState.OCCUPIED) && 
                                (wp.getSpaceState() != 
                                    WayPoint.SpaceState.GOING_EMPTY)))) {
                        candidates.add(wp);
                    }
                }
                if (candidates.size() > 0) {
                    int index = (int) Math.round(
                            Math.random() * (candidates.size() - 1)); 
                    place = candidates.get(index);

                    return place;
                }
            }
            objectClass = objectClass.getSuperclass();
        }
        
        return null;
    }
    
    
    /**
     * Takes an object and places it to some sublocation according to it's class
     * id. This is only the helper function for expandPlaceObjects().
     * @param object Object to be placed
     */
    protected void expandPlaceObject(IveObject object) 
            throws NoObjectPlaceException {
        
        IveObject specialPlace = findObjectPlaceSpecial(object);
        WayPoint place = null;
        
        if (specialPlace == null) {
            place = findObjectPlace(object);
        } else if (specialPlace instanceof WayPoint) {
            place = (WayPoint) specialPlace;
        } else {
            specialPlace.addObject(object);
            return;
        }

        if (place != null) {
            place.addObject(object);
        } else {
            throw(new NoObjectPlaceException("No object place found for "+
                    object.getObjectClass().toString()+ " in location "+ id, 
                    this));
        }
    }
    
    /**
     * Generates a registerable id for a new object. 
     * @param base Base of the id (e.g. "chair")
     * @return Id, which can be registered in ObjectMap, and number of the object
     */
    protected Pair<String, Integer> generateId(String base) {
       int i = 0; 
       String id;
       
       while (true) {
           i++;
           id = getId() + "." + base + Integer.toString(i);
           if (ObjectMap.instance().canRegister(id)) {
               return new Pair<String, Integer>(id, i);
           }
       }
    }

    /**
     * Applies all information stored in expansionInfo on expanding location.
     */
    protected abstract void applyExpansionInfo() throws NoObjectPlaceException;
        
    /**
     * Generates all inherent objects and places them to the children.
     */
    protected void generateInherentObjects() throws NoObjectPlaceException {

        Pair<ObjectInstanceInfo,Integer> inherentObjects;
        IveObject object;
        String id;
        InherentObject inherentObject;
        int obj;
        
        if (inherentObjectsGenerated) {
            /* Objects were generated from the expansionInfo yet, now use
             * inherent map to generate them. */

            for (Iterator<String> j = inherent.keySet().iterator(); 
                    j.hasNext(); ) {
                
                id = j.next();
                inherentObject = inherent.get(id);
                
                for (obj=0; obj<inherentObject.count; obj++) {
                    Pair<String, Integer> generatedId = generateId(
                            inherentObject.info.name);
                    object = inherentObject.info.instantiate(
                            generatedId.first());
                    object.addAttribute("inherentObjectNumber", new AttrInteger(
                            generatedId.second()));
                    addSpecialLocationAttributes(object);
                    expandPlaceObject(object);
                }

                inherent.get(id).count = 0;
            }
            
        } else {
            /* first expansion of this location - use expansionInfo to generate 
             * inherent objects and create inherent map */
            
            for (Iterator<Pair<ObjectInstanceInfo,Integer>> i = 
                    expansionInfo.getObjectInstances().iterator(); 
                    i.hasNext(); ) {

                inherentObjects = i.next();
                for (int j=0; j<inherentObjects.second(); j++) {

                    Pair<String, Integer> generatedId = 
                            generateId(inherentObjects.first().name);
                    object = inherentObjects.first().instantiate(
                            generatedId.first());
                    object.addAttribute("inherentObjectNumber", new AttrInteger(
                            generatedId.second()));
                    addSpecialLocationAttributes(object);

                    expandPlaceObject(object);
                    
                    if (j==0) {
                        addInherentClass(object.getObjectClass().toString(), 
                                inherentObjects.first());
                    }
                }
            }
            inherentObjectsGenerated = true;
        }
    }
    
/**
 *  Adds some attributes 
 *  This is redefined in special locations
 */    
    protected void addSpecialLocationAttributes(IveObject object){
    }
    
    public void expand() throws NoObjectPlaceException {
        WayPoint[] children;
        WayPoint position;
        Iterator<IveObject> i;
        Iterator<String> j;
        IveObject object;
        IveObjectImpl newObject;
        String id;
        int count;
        int obj;
    
        if (getLocationState() != LocationState.ATOMIC) {
            Log.warning("Expand called on non-atomic location "+getId());
            return;
        }

        /* expand the location */
        applyExpansionInfo();
        
        children = getWayPoints();
        for(int k=0; k<children.length; k++) {
            children[k].setLocationState(LocationState.ATOMIC);
            if (! children[k].isSubstantial()) {
                ManagerOfSenses.getJam().addObject(children[k], children[k]);
            }
        }
        setLocationState(LocationState.EXPANDED);
        
        /* tell to the interpreter, that we are going to expand */
        for (i = getSlaves().iterator(); i.hasNext(); ) {
            WorldInterpreter.instance().preIncreaseLod(i.next());
        }
        for (i = invalid.iterator(); i.hasNext(); ) {
            WorldInterpreter.instance().preIncreaseLod(i.next());
        }
        
        /* place invalid slaves */
        Object[] invArray = invalid.toArray();
        for (Object o : invArray) {
            object = (IveObject) o;
            removeObject(object);
            
            position = findNewWayPoint(object.getPosition());
            if (position != null) {
                /* the object remembers it's position in the lower level */
                position.addObject(object);
            } else {
                expandPlaceObject(object);
            }
        }
        
        /* place stored objects */
        for (i = stored.iterator(); i.hasNext(); ) {
            object = i.next();
            ObjectMap.instance().unhide(object);
            expandPlaceObject(object);
        }
        stored.clear();
        
        /* place inherent objects */
        generateInherentObjects();

        /* place valid slaves */
        Object[] slaveArray = getSlaves().toArray();
        for (Object o : slaveArray) {
            object = (IveObject) o;
            removeObject(object);

            position = findNewWayPoint(object.getPosition());
            if (position != null) {
                /* the object remembers it's position in the lower level */
                position.addObject(object);
            } else {
                expandPlaceObject(object);
            }
        }
        
        /* Signal the gui expandHook so it can update itself */
        expandHook.notifyListeners();
    }
 
    public void shrink() {
        
        Iterator<IveObject> wpi;
        WayPoint[] children = getWayPoints();
        HashMap<IveObject, Set<IveObject>> map = 
                new HashMap<IveObject, Set<IveObject>>();
        Set<IveObject> objects;
        int i;
        
        if (getLocationState() == LocationState.ATOMIC) {
            Log.warning("Shrink called on atomic location "+getId());
            return;
        }
        
        /* get all objects, tell to the Interpreter we are going to shrink */
        for (i=0; i<children.length; i++) {
            if (children[i].getLocationState() == LocationState.EXPANDED) {
                children[i].shrink();
            }
            if (children[i] instanceof Area) {
                ((Area) children[i]).deactivateGenies();
            }
            objects = children[i].getAllObjects();
            map.put(children[i], objects);
            for (IveObject o: objects) {
                WorldInterpreter.instance().preDecreaseLod(o);
            }
            if (! children[i].isSubstantial()) {
                ManagerOfSenses.getJam().removeObject(children[i], children[i]);
            }
        }
        
        /* move all objects */
        for (i=0; i<children.length; i++) {
            objects = map.get(children[i]);
            for (IveObject o: objects) {
                children[i].removeObject(o);
                if (o.getMaster() == children[i]) {
                    o.setMaster(this);
                }
                addObject(o);
            }
            children[i].setLocationState(LocationState.NOT_EXIST);
            ObjectMap.instance().unregister(children[i].getId());
        }
        
        /* forget the children WayPoints */
        forgetWayPoints();   
        setLocationState(LocationState.ATOMIC);
        
        /* Signal the gui shrinkHook so it can update itself */
        shrinkHook.notifyListeners();
    }
    
    /**
     * Information needed for area expansion
     */
    public static class ObjectExpansionInfo implements Serializable {        
        
        /**
         * Creates a new instance of ObjectExpansionInfo.
         * @param objInstancesInfo Information about initial inherent objects
         */
        public ObjectExpansionInfo(
                List<Pair<ObjectInstanceInfo,Integer>> objInstancesInfo) {
            objectInstances = objInstancesInfo;
            if (objectInstances==null) objectInstances=new LinkedList<Pair<ObjectInstanceInfo,Integer>>();
        }

        /**
         * Initial info about inherent objects
         */
        private List<Pair<ObjectInstanceInfo,Integer>> objectInstances;

        public List<Pair<ObjectInstanceInfo, Integer>> getObjectInstances() {
            return objectInstances;
        }

    }
    
    /**
     * Used to remember influence value between items of pair.
     * The influece pays onlyd one direction - from the first to the
     * second member.
     */
    public static class InfluenceInfo extends Pair<String,String> 
            implements Serializable {
        /**
         * Creates a new instance of InfluenceInfo.
         */
        public InfluenceInfo(){}

        /**
         * Creates a new instance of InfluenceInfo.
         * @param f First WayPoint's id
         * @param s Second WayPoint's id
         * @param influence The influence value
         */
        public InfluenceInfo(String f,String s,int influence){
            super(f,s);
            this.influence= influence;
        }

        /**
         * Influence between first and second item of pair.
         */
        public int influence;
    }
    
    /**
     * Class storing info about neighbourhood of WayPoints.
     */
    public static class NeighbourInfo extends Pair<String,String> 
            implements Serializable {

        /**
         * Creates a new instance of NeighbourInfo
         */
        public NeighbourInfo(){}
        
        /**
         * Creates a new instance of NeighbourInfo
         * @param f First WayPoint's id
         * @param s Second WayPoint's id
         * @param dx x-axis difference between the real position of the 
         *           eneighbours (used only in case of border WayPoints)
         * @param dy y-axis difference between the real position of the 
         *           eneighbours (used only in case of border WayPoints)
         * @param kinds Kinds of both neighbours
         */
        public NeighbourInfo(String f,String s,float dx,float dy, 
                    Pair<Kind,Kind> kinds) {
            super(f,s);
            this.dx=dx;
            this.dy=dy;
            this.kinds=this.kinds;
        }
        
        /**
         * Difference of real coordinates of the neighbours, in x and y axis.
         * Used only in case of border WayPoints.
         */
        public float dx,dy;
        
        /**
         * Kinds for first and second neighbouring WayPoints.
         */
        public Pair<Kind,Kind> kinds;
        
    }

    /**
     * Identifies a graphic template.
     */
    public static class GraphicTemplateStrings implements Serializable {
            /**
             * Creates a new instance of GraphicTemplateStrings.
             * @param name Name of the graphic template
             * @param className Name of the java class implementing GraphicInfo
             */
            public GraphicTemplateStrings(String name,String className){
                this.className=className;
                this.name=name;
            }
            
            /** Name of the graphic template */
            public String name;
            
            /** Name of the java class implementing GraphicInfo */
            public String className;
        }
    
    public void setPhantomGenerationInfo(PhantomGenerationInfo info) {
        phantomGenerationInfo = info;
    }
    
    /**
     * Information needed to generate phantoms of neighbours in another
     * location.
     */
    public static class PhantomGenerationInfo implements Serializable {
        /**
         * Creates a new instance of PhantomGenerationInfo.
         */
        public PhantomGenerationInfo() {
            neighbours = new Vector<NeighbourInfo>();
            influences = new Vector<InfluenceInfo>();
        }
        
        /**
         * Phantom neighbours. 
         * The first String is a relative id of a child, the second is 
         * an absolute id of the phantom.
         */
        public List<NeighbourInfo> neighbours;
     
        /**
         * Influences to phantom neighbours.
         * The first String is a relative id of a child, the second is 
         * an absolute id of the phantom.
         */
        public List<InfluenceInfo> influences;
    }
    
    /** 
     * Registers new expand listener. This should be used only by u Gui,
     * since the callback is performed in the Swing thread with the Simulation
     * lock locked.
     *
     * @param listener callback to be called when expanding.
     */
    public void registerGuiExpandListener(Listener listener) {
        expandHook.registerListener(listener);
    }
    
    /** 
     * Registers new shrink listener. This should be used only by u Gui,
     * since the callback is performed in the Swing thread with the Simulation
     * lock locked.
     *
     * @param listener callback to be called when shrinking.
     */
    public void registerGuiShrinkListener(Listener listener) {
        shrinkHook.registerListener(listener);
    }
    
    
    /** 
     * Unregisters expand listener. This should be used only by u Gui,
     * since the callback is performed in the Swing thread with the Simulation
     * lock locked.
     *
     * @param listener callback not to be called anymore.
     */
    public void unregisterGuiExpandListener(Listener listener) {
        expandHook.unregisterListener(listener);
    }
    
    /** 
     * Unregisters shrink listener. This should be used only by u Gui,
     * since the callback is performed in the Swing thread with the Simulation
     * lock locked.
     *
     * @param listener callback not to be called anymore.
     */
    public void unregisterGuiShrinkListener(Listener listener) {
        shrinkHook.unregisterListener(listener);
    }
    
    public void setGenies(AreaGenius[] g){
        genies = g;
        for (AreaGenius genius : genies) {
            genius.activate();
        }
    }
    
    public void deactivateGenies() {
        for (AreaGenius genius : genies) {
            genius.deactivate();
        }
    }
}

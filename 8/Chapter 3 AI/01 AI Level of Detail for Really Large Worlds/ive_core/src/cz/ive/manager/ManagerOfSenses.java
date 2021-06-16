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
 
package cz.ive.manager;

import cz.ive.messaging.Listener;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.sensors.*;
import cz.ive.lod.LOD;
import cz.ive.location.*;
import cz.ive.ontology.OntologyToken;
import cz.ive.exception.OntologyNotSupportedException;
import cz.ive.evaltree.*;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.logs.*;
import cz.ive.process.Slot;
import cz.ive.process.Substitution;
import cz.ive.process.SubstitutionImpl;
import cz.ive.util.MultiMap;
import cz.ive.util.Pair;
import cz.ive.valueholders.FuzzyValueHolder;
import java.util.*;

/**
 * In our implementation, only one instance of manager is created and it's
 * accesible by static class member 'manager'.
 * Other instances of class can be created.
 *
 * <H2>Documentation of manager of senses implementation</H2>
 * Manager have following structures for managed objects:
 * <ul>
 * <li>multi-map of objects searchable by ID of wayPoint where it's situated
 * <br>used for getting objects applicable for query evaulation
 * <li>multi-map of passive queries searchable by ID wayPoint where the query
 * is valid - could be a lot of wayPoints
 * <li>multi-map of passive updates of objects searchable by ID of
 * updated object
 * <li>there are other three structures for managing changes caused by sensors
 * movements:
 * <ul><li>map of root wayPoints of Sensors
 * <br>only change of this wayPoints means that there is a change in view
 * of the sensor and other actions are performed
 * <li>multi-map where all passive updates for each sensor that
 * is used by them are
 * <li>multi-map where all passive queries for each sensor that
 * is used by them are
 * </ul></ul>
 *
 * <h3>Query evaulation</h3>
 * All location where query is valid are obtained by intersection of query LOD
 * and sensors LOD.
 * <br>Input objects are obtained according to locations and specified role.
 * <br>There is Set Expr as the main part of the query. Its leaves
 * are Fuzzy Expr - they are obtained and evaulated for each input
 * object - if it satisfies, it's procceded to Set Expr. The root of this Expr
 * is set of resulting objects.
 * <br>Result is compouded according to flag specifying whether all object or
 * only one should be returned and whether their copies should be empty
 * or filled according to sensors.
 * <br>For passive queries, the box containing result set is returned and it
 * notified it's listeners every time when there is a change in this set.
 *
 * @author Jirka
 */
public class ManagerOfSenses 
        implements MoveAware, Listener, java.io.Serializable {
    
    /** 
     * Flags to specify how will be filled objects returned by query to 
     * manager 
     */
    public enum ReturnSet {
        /** all objects are returned and they are filled */
        ALL_COPY,
        /** all objects are returned and they are empty */
        ALL_EMPTY,
        /** all objects are returned and only first of them is filled */
        ALL_FIRST_COPY,
        /** one random object is returned and it's filled */
        ANY_COPY,
        /** one random object is returned and it's empty */
        ANY_EMPTY;
    }
    
    /** Set of all managed IveObjects searchable by its name */
    private Map<String, IveObject> objByName;
    
    /** Set of all managed IveObjects searchable by its position */
    private MultiMap<String, IveObject> objByPos;
    
    /** Set of all passive object updates searchable by object id */
    private MultiMap<String, QTUpdate> updates;
    
    /** root waypoints of sensors */
    private Map<String, String> sensorView;
    
    /** sensors searchable by their root position */
    private MultiMap<String, String> sensorsByRootPos;

    /** All mapping of sensor to passive query */
    private MultiMap<String, QTQuery> queriesBySensor;
    
    /** Set of all passive updates using specific sensor */
    private MultiMap<String, QTUpdate> updatesBySensor;

    /** 
     * Set of all passive updates without original object searchable by object 
     * id 
     */
    private MultiMap<String, QTUpdate> orphanUpdates;

    /** Map from sensors id to the real sensors, counter of sensor usages */
    private Map<String, Pair<Sensor, Integer>> sensorCounter;
    
    
    /** The static reference to the manager singleton */
    static ManagerOfSenses manager;
    
    /** The Jam of manager of senses */
    private Jam j;
    
    /**
     * Creates, if already isn't, new instance of manager singleton.
     *
     * @return refernece to the manager singleton
     */
    static public synchronized ManagerOfSenses instance() {
        if (manager == null) {
            manager = new ManagerOfSenses();
        }
        return manager;
    }
    
    /**
     * Allows to set up current singleton of the ManagerOfSenses.
     * This is used during save and load.
     */
    static public void setInstance(ManagerOfSenses newInstance) {
        manager = newInstance;
    }
    
    /**
     * Empty whole the ManagerOfSenses before the XML load. We just drop
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        manager = new ManagerOfSenses();
    }
    
    /**
     * Constructor, creates a empty instance of the mamager
     */
    public ManagerOfSenses(){
        
        objByName = new HashMap<String,IveObject>();
        objByPos = new MultiMap<String,IveObject>();
        updates = new MultiMap<String,QTUpdate>();
        sensorView = new HashMap<String,String>();
        queriesBySensor = new MultiMap<String,QTQuery>();
        updatesBySensor = new MultiMap<String,QTUpdate>();
        orphanUpdates = new MultiMap<String,QTUpdate>();
        sensorCounter = new HashMap<String, Pair<Sensor, Integer>>();
        sensorsByRootPos = new MultiMap<String, String>();
        j = new JamImpl(this);
    }

    /** 
     * Getter for manager's jam.
     * @return jam of manager of senses 
     */
    public static Jam getJam(){
        return instance().j;
    }
    
    /**
     * Adds a sensor to all structures, registers to it, unregisters from the
     * old one. Counts usages of each sensor. Handles change of the reference
     * to new sensor with the same id.
     * @param sensor Sensor to add
     */
    void addSensor(Sensor sensor) {
        
        Pair<Sensor, Integer> sensorInfo = sensorCounter.get(sensor.getId());
        if (sensorInfo == null) {
            /* adding a new sensor */
            sensorCounter.put(sensor.getId().toString(), 
                    new Pair<Sensor, Integer>(sensor, new Integer(1)));
        } else {
            if (sensorInfo.first() == sensor) {
                /* adding some known sensor */
                sensorInfo.second = new Integer(sensorInfo.second()+1);
            } else {
                /* some known sensor, but the reference has changed */
                Sensor oldSensor = sensorInfo.first();
                oldSensor.unregisterListener(this);
                sensor.registerListener(this);
                
                Set<QTQuery> qset = queriesBySensor.get(
                        sensor.getId().toString());
                if (qset != null) {
                    for (QTQuery q : qset) {
                        q.exchangeSensor(oldSensor, sensor);
                    }
                }

                Set<QTUpdate> uset = updatesBySensor.get(
                        sensor.getId().toString());
                if (uset != null) {
                    for (QTUpdate u : uset) {
                        u.exchangeSensor(oldSensor, sensor);
                    }
                }
                
                sensorInfo.first = sensor;
                sensorInfo.second = new Integer(sensorInfo.second()+1);
            }
        }

        String root = sensor.getPosition().getRootWP(sensor.getLOD()).getId();
        if (sensorView.put(sensor.getId(), root) == null) {
            sensor.registerListener(this);
        }
        sensorsByRootPos.put(root, sensor.getId());
        
    }
    
    /**
     * Removes a sensor from all structures, unregisters from it.
     * Uses a reference counting not to remove sensors used somewhere else.
     * @param sensor Sensor to remove
     */
    void removeSensor(Sensor sensor) {
        
        Pair<Sensor, Integer> sensorInfo = sensorCounter.get(sensor.getId());
        if (sensorInfo == null) {
            return;
        }
        
        if (sensorInfo.second() == 1) {
            /* this was the last usage */
            String root = sensorView.remove(sensor.getId());
            sensorsByRootPos.removeValue(root, sensor.getId());
            sensorCounter.remove(sensor.getId());
            sensorInfo.first.unregisterListener(this);
            return;
        }
        
        sensorInfo.second = new Integer(sensorInfo.second()-1);
    }

    /**
     * Tests whether objects on a WayPoint can be sensed by a sensor.
     * Note that no object can by sensed by sensor which is not in valid state.
     * @param sensor Sensor which may sense the object
     * @param wayPoint WayPoint whose objects may be sensed by the sensor.
     * @return true iff the Object is in a radius of the sensor
     */
    public boolean isInSensorsRange(Sensor sensor, WayPoint wayPoint) {
        
        if (sensor.getObjectState() != IveObject.ObjectState.VALID) {
            return false;
        }
        
        WayPoint root = sensor.getPosition();
        while ((root.getPosition() != null) && 
                (root.getLod() > sensor.getLOD().getMin())) {
            root = root.getPosition();
        }
        
        if (! wayPoint.isParent(root)) {
            return false;
        }
        
        if ((wayPoint.getLod() < sensor.getLOD().getMin()) || 
                (wayPoint.getLod() > sensor.getLOD().getMax())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Tests whether an object can be sensed by a sensor.
     * Note that no object can by sensed by sensor which is not in valid state.
     * @param sensor Sensor which may sense the object
     * @param object Object which may be sensed by the sensor.
     *        this object should not be a phantom, or it has to have the
     *        lod parameter properly set.
     * @return true iff the Object is in a radius of the sensor
     */
    public boolean isApplicableSensor(Sensor sensor, IveObject object) {
       
        if (object.getObjectState() != IveObject.ObjectState.VALID) {
            return false;
        }
        WayPoint position = object.getPosition();
        return isInSensorsRange(sensor, position);
    }
    
    /**
     * Updates a copy of the given IveObject according to sensors
     *
     * @param sensors list of sensors to filter object
     * @param copy object to update
     * @return whether the update was successful, says nothing about changes;
     *	false is returned when no such valid object is founded or updated
     *	object is valid or object is not visible by any sensor
     */
    public boolean updateCopyActive(List<Sensor> sensors, IveObject copy) {
        
        IveObject original = objByName.get(copy.getId());
        
        if (!copy.isPhantom() || original == null) {
            return false;
        }
        
        ArrayList<Sensor> aplicableSensors = new ArrayList<Sensor>();
        for (Sensor s: sensors) {
            if (isApplicableSensor(s, original)) {
                aplicableSensors.add(s);
            }
        }
        if (aplicableSensors.isEmpty()) {
            return false; //object is not visible by any sensor
        }

        updateCopy(aplicableSensors, original, copy, null);
        return true;
    }
    
    /**
     * Updates a copy of the given IveObject according to sensors
     * and then after each other change with notifying also.
     *
     * @param sensors list of sensors to filter object
     * @param copy object to update
     * @return box - hook - where copy is stored and will be updated
     *	or null when given IveObject is not found or if it's valid object
     */
    public QTUpdate updateCopyPassive(List<Sensor> sensors,
            IveObject copy) {
        
        IveObject original = objByName.get(copy.getId());
        if (!copy.isPhantom()) {
            return null;
        }
        
        QTUpdate box = new QTUpdate(this, sensors, copy, original);

        for (Sensor s: sensors) {
            addSensor(s);
            updatesBySensor.put(s.getId(), box);
        }

        if (original == null) {
            orphanUpdates.put(copy.getId().toString(), box);
            return box;
        }
        
        original.registerListenerSpecial(box);
        updates.put(copy.getId(), box);
        
        return box;
    }
    
    /**
     * One-shot processing of query.
     *
     * @param sensors list of sensors to filter query input objects
     * @param query ontology containing all query data
     * @param sources substitution with passed variables, can be null
     * @param flags mode of creation of the return set
     * @return set of all objects which passed the query
     */
    public List<IveObject> queryActive(List<Sensor> sensors,
            OntologyToken query, Substitution sources, ReturnSet flags)
            throws OntologyNotSupportedException {
        
        Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl> e
                = (Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl>) 
                    query.getData("jBRP.queryExpr");
        HashSet<String> locations = new HashSet<String>();
        LOD l = ((QueryData) query).getLod();

        Set<IveObject> input = getInputObjects(sensors, 
                ((QueryData) query).getRole(), l);

        Set<IveObject> origin = new HashSet<IveObject>();

        for (IveObject o : input) {
            Substitution subst;

            if (sources != null) {
                subst = sources.duplicateSubstitution(true);
                subst.addSlot("setitem", new Slot(o), false);
            } else {
                subst = 
                    new SubstitutionImpl("setitem", new Slot(o), false);
            }
            e.DFSEval(subst, sensors);
            if (e.getValue().value == FuzzyValueHolder.True) { 
                origin.add(o);
            }
        }
        
        return makeResultSet(null, origin, sensors, flags);
    }
    
    /**
     * Process query, stores it and will be recounts all changes in result.
     *
     * @param sensors list of sensors to filter query input objects
     * @param query ontology containing all query data
     * @param sources substitution with passed variables, can be null
     * @param flag mode of creation of the return set
     * @return box with set of all objects which passed the query and which
     *	signalizes changes in that set
     */
    public QTQuery queryPassive(List<Sensor> sensors, OntologyToken query,
            Substitution sources, ReturnSet flag) 
            throws OntologyNotSupportedException {
        
        LOD l = ((QueryData) query).getLod();
        
        for (Sensor s: sensors) {
            addSensor(s);
        }
        
        Set<IveObject> input = getInputObjects(sensors,
                ((QueryData) query).getRole(), l);

        QTQuery box =
                new QTQuery(this, sensors, input, (QueryData) query, sources, 
                                flag);
        
        for (Sensor i: sensors) {
            queriesBySensor.put(i.getId(), box);
        }
        
        return box;
    }
    
    /**
     * Gets all queries that are relevant to the given WayPoint.
     * @param wp WayPoint to test
     * @return All queries, which have some sensor, which senses objects on 
     *         the given WayPoint.
     */
    protected Set<QTQuery> getQueriesByPos(WayPoint wp) {
        
        WayPoint base = wp;
        
        Set<QTQuery> set = new HashSet<QTQuery>();
        Set<String> sensors;
        Sensor sensor;
        while (wp != null) {
            sensors = sensorsByRootPos.get(wp.getId());
            if (sensors != null) {
                for (String sensorId : sensors) {
                    sensor = sensorCounter.get(sensorId).first();
                    if (isInSensorsRange(sensor, base)) { 
                        Set<QTQuery> queries = queriesBySensor.get(sensorId);
                        if (queries != null) {
                            set.addAll(queries);
                        }
                    }
                }
            }
            wp = wp.getPosition();
        }
        
        return set;
    }
    
    /**
     * Add reference of the object to container of all managed objects
     * and pass it on to all potencional queries according to its location.
     *
     * @param o object to register
     * @param wp location where object is/will be situated
     */
    public void addObject(IveObject o, WayPoint wp) {
        Log.addMessage("MOS: Object added (Jammed)", Log.FINEST,
                o, null, wp);
        
        if (objByPos.put(wp.getId(), o)) {
            Set<QTQuery> queries = getQueriesByPos(wp);
            
            if (queries != null) {
                for (QTQuery q: queries) {
                    q.addToInput(o);
                }
            }
        }
        objByName.put(o.getId(), o);
        
        Set<QTUpdate> orphans = orphanUpdates.get(o.getId().toString());
        if (orphans != null) {
            for (QTUpdate box : orphans) {
                box.setOriginalObject(o);

                o.registerListenerSpecial(box);
                updates.put(o.getId(), box);
            }
            orphanUpdates.remove(orphans);
        }
    }
    
    /**
     * Excluded object from all queries according to given location and
     * if there is no other location where object stay
     * then it is removed from container of all managed objects.
     *
     * @param o object to remove
     * @param wp location where object will no longer be
     */
    public void removeObject(IveObject o, WayPoint wp) {
        Log.addMessage("MOS: Object removed (Jammed)", Log.FINEST,
                o, null, wp);
        
        if (objByPos.removeValue(wp.getId(), o)) {
            Set<QTQuery> q = getQueriesByPos(wp);
            if (q != null) {
                for (QTQuery i: q) {
                    i.removeFromInput(o);
                }
            }
        }
        if (objByName.remove(o.getId()) != null) {
            Set<QTUpdate> updatesOfThisObject = updates.get(o.getId());
            if (updatesOfThisObject!=null){
                for (QTUpdate box: updatesOfThisObject) {
                    box.makeInvalid();
                }
            }
        }
    }
    
    /**
     * Change internal objects location data and add/remove object from queries.
     *
     * @param o object to move
     * @param wp1 old location where object have to be
     * @param wp2 new location where object will be now
     */
    public void moveObject(IveObject o, WayPoint wp1, WayPoint wp2) {
        Log.addMessage("MOS: Object moved (Jammed)", Log.FINEST,
                o, null, wp2);
        Set<QTQuery> q1 = getQueriesByPos(wp1);
        Set<QTQuery> q2 = getQueriesByPos(wp2);
        
        if (objByPos.removeValue(wp1.getId(), o)) {
            if (q1 != null) {
                for (QTQuery i: q1) {
                    if (!q2.contains(i)) {
                        i.removeFromInput(o);
                    }
                }
            }
        }
        if (objByPos.put(wp2.getId(), o)) {
            if (q2 != null) {
                for (QTQuery i: q2) {
                    if (!q1.contains(i)) {
                        i.addToInput(o);
                    }
                }
            }
        }
        
        Set<QTUpdate> upd = updates.get(o.getId());
        if (upd != null) {
            for (QTUpdate box: upd) {
                box.moveObject(wp2);
            }
        }
    }
    
    /**
     * Changes in sensors are catched here and propagated into passive updates
     * and queries if there is a change in set of waypoints viewed by sensor
     *
     * @param initiator hook of sensor which has been changed
     */
    public void changed(cz.ive.messaging.Hook initiator) {
        
        if (initiator instanceof Sensor) {
            //movement of the sensor
            Sensor s = (Sensor) initiator;
            WayPoint wp = s.getPosition().getRootWP(s.getLOD());
            String rootId = sensorView.get(s.getId());
            if (wp.getId().equals(rootId)) {
                //no change in view of the sensor
                return;
            }
                      
            sensorView.put(s.getId(), wp.getId());
            sensorsByRootPos.removeValue(rootId, s.getId());
            sensorsByRootPos.put(wp.getId(), s.getId());
                    
            Set<QTQuery> qSet = queriesBySensor.get(s.getId());

            if (qSet != null) {
                for (QTQuery q : qSet) {
                    Set<IveObject> objects = 
                            getInputObjects(q.sensors, null, null);                    
                    q.alterInput(objects);
                }
            }
            Set<QTUpdate> qSet2 = updatesBySensor.get(s.getId());
            if (qSet2 != null) {
                for (QTUpdate i: qSet2) {
                    i.sensorMove(s);
                }
            }
        } else {
            Log.warning("Unknown initiator of change in ManagerOfSenses.");
        }
    }
    
    /**
     * Remove trigger from relevant list
     * on case if it's QTQuery or QTUpdate
     *
     * @param trigger one of class descendants
     */
    public void unregisterPassive(QueryTrigger trigger) {
        
        if (trigger instanceof QTUpdate) {
            QTUpdate upd = (QTUpdate) trigger;
            updates.removeValue(upd.getCopy().getId(), upd);
            for (Sensor s: upd.sensors) {
                updatesBySensor.removeValue(s.getId(), upd);
                removeSensor(s);
            }
            orphanUpdates.removeValue(upd.getCopy().getId(), upd);
            upd.unregisterAll();
            upd.canceled(upd);
        } else if (trigger instanceof QTQuery) {
            QTQuery q = (QTQuery) trigger;
            for (Sensor s: q.sensors) {
                queriesBySensor.removeValue(s.getId(), q);
                removeSensor(s);
            }
            q.canceled(q);
        }
    }
    
    /**
     * Updates internal structures to reflect exchange sensors in a QuryTrigger.
     * @param qt QueryTrigger whose sensors were changed
     * @param oldSensors List of old sensors
     * @param newSensors List of new sensors
     */
    public void sensorsExchanged(QueryTrigger qt, List<Sensor> oldSensors, 
            List<Sensor> newSensors) {
        
        if (qt instanceof QTUpdate) {
            QTUpdate update = (QTUpdate) qt; 
            for (Sensor s: oldSensors) {
                updatesBySensor.removeValue(s.getId(), update);
                removeSensor(s);
            }

            for (Sensor s: newSensors) {
                updatesBySensor.put(s.getId(), update);
                addSensor(s);
            }
        }
        
        if (qt instanceof QTQuery) {
            QTQuery query = (QTQuery) qt;
            for (Sensor s: oldSensors) {
                queriesBySensor.removeValue(s.getId(), query);
                removeSensor(s);
            }

            for (Sensor s: newSensors) {
                queriesBySensor.put(s.getId(), query);
                addSensor(s);
            }
        }
    }
    
    
    /**
     * Empty body - method has no sense at this implementation of Listener
     */
    public void canceled(cz.ive.messaging.Hook initiator) {
    }
    
    /**
     * Recursively calling method to realize the getInputObjects method.
     * Recursively backtrakcs the tree of WayPoints to the depth
     * and builds the set of the objects.
     * @param root Root of the tree to backtrack
     * @param sensors Sensors to consider
     * @param role Role of the searched objects
     * @param lod Lod of the query, can be null
     * @param set Incrementally builded result set
     */
    protected void collectInputObjects(WayPoint root, 
            List<Pair<Sensor, WayPoint>> sensors,
            Link role, LOD lod, Set<IveObject> set) {
        
        if (root.isPhantom()) {
            return;
        }
        
        int rootLod = root.getLod();
        
        /* process sensors */
        boolean belongs = false;
        boolean lastLevel = true;
        int lodPos;
        for (Pair<Sensor, WayPoint> s : sensors) {
            if ((!s.second().isParent(root)) && (!root.isParent(s.second()))) {
                continue;
            }
            lodPos = -1;
            if (rootLod >= s.first().getLOD().getMin()) {
                lodPos = 0;
                if (rootLod > s.first().getLOD().getMax()) {
                    lodPos = 1;
                } else if (rootLod < s.first().getLOD().getMax()) {
                    lastLevel = false;
                }
            } else {
                lastLevel = false;
            }
            if (lodPos == 0) {
                belongs = true;
            }
        }
        
        if (lod != null) {
            if (rootLod < lod.getMin()) {
                belongs = false;
            }
            if (rootLod > lod.getMax()) {
                lastLevel = true;
            }
        }
        
        if (belongs) {
            Set<IveObject> objs = objByPos.get(root.getId());
            if (objs != null) {
                if (role != null) 
                {
                    for (IveObject o : objs) {
                        if (!o.getLinks(role.getGoal(),
                                role.getProcess(), role.getRole()).isEmpty()) {
                            set.add(o);
                        }
                    }
                } else {
                    set.addAll(objByPos.get(root.getId()));
                }
            }
        }

        /* recurse */

        if (lastLevel) {
            return;
        }
        
        if (root instanceof Area) {

            WayPoint[] children;
            int child;
            Area a = (Area) root;

            children = a.getWayPoints();

            if (children == null) {
                return;
            }

            for (child=0; child<children.length; child++) {
                collectInputObjects(children[child], sensors, role, lod, set);
            }
        }

    }

    /**
     * Gets all object for query according to the query sensors and role name.
     * Recursively searches the location tree with cutting-off.
     * @param sensors All sensors to consider
     * @param role only objects with link containing this role name are accepted
     *	or null can be passed
     * @param lod Lod range of the query, can be null
     * @return Set of all objects of the given role, wich can be sensed by
     * at least one of the given sensors.
     */
    public Set<IveObject> getInputObjects(List<Sensor> sensors, Link role, 
            LOD lod) {
        HashSet<IveObject> set = 
                new HashSet<IveObject>();
        List<Pair<Sensor, WayPoint>> sensorInfo = 
                new Vector<Pair<Sensor, WayPoint>>();
        WayPoint root;

        for (Sensor s : sensors) {
            root = s.getPosition().getRootWP(s.getLOD());
                
            sensorInfo.add(new Pair<Sensor, WayPoint>(s, root));
        }
        
        /* find the common parent of all sensor's trees */
        root = sensorInfo.get(0).second();
        for (int i=1; i<sensorInfo.size(); i++) {
            root = (WayPoint) 
                        root.getLeastCommonParent(sensorInfo.get(i).second());
        }
        
        /* build the list */
        collectInputObjects(root, sensorInfo, role, lod, set);
        
        return set;
    }

    /**
     * Gets all objects for query according to given locations and role name.
     *
     * @param locations all locations where object can appear
     * @param role only objects with link containing this role name are accepted
     *	or null can be passed
     * @return set of all suitable IveObjects
     */
    private HashSet<IveObject> getInputObjects(Set<String> locations,
            Link role) {
        
        HashSet<IveObject> input = new HashSet<IveObject>();
        
        HashSet<IveObject> possible = new HashSet<IveObject>();
        Set<IveObject> o;
        for (String location: locations) {
            if ((o = objByPos.get(location)) != null)
                possible.addAll(o);
        }
        if (role != null) {
            for (IveObject orig: possible) {
                if (!orig.getLinks(role.getGoal(),role.getProcess(),
                        role.getRole()).isEmpty()) {
                    input.add(orig);
                }
            }
        } else if (possible != null) {
            input.addAll(possible);
        }
        return input;
    }
    
    /**
     * Update copy of IveObject clipped by sensors according to original.
     *
     * @param sensors list of sensors which specified set of visible attributes
     *  and whether object is in view distance
     * @param orig original IveObject to be compared with updated copy
     * @param copy IveObject to be updated
     * @param listener if it isn't null, it's registered by each attribute
     *	which is added to copy
     * @return whether some change was made
     */
    boolean updateCopy(List<Sensor> sensors, IveObject orig, IveObject copy,
            QTUpdate listener) {
        
        boolean change = false;
        //names of attributes which will be presented in copy
        Set<String> accetable = new HashSet<String>();
        //names of attributes to be removed from copy
        Set<String> toDelete = copy.getNamesByPart(null);
        
        for (String name: orig.getNamesByPart(null)) {
            /* original attributes */
            AttributeValue attr = orig.getAttribute(name);
            for (Sensor sensor: sensors) {
                /* sensors */
                if (sensor.compareWithMask(attr.getSensorMask())) {
                    /* object compatible with sensor */
                    accetable.add(name);
                    AttributeValue copyVal;
                    if ((copyVal = copy.getAttribute(name)) == null) {
                        /* the copy has not the attribute yet */
                        change = true;
                        if (attr instanceof AttrObject) {
                            IveObject obj = ((AttrObject) attr).getValue();
                            if (obj != null) {
                                if (name.equals("position")) {
                                    WayPoint wp = (WayPoint) obj;
                                    copy.addAttribute(name,
                                            new AttrObject(wp.getPhantom()));
                                } else {
                                    copy.addAttribute(name, 
                                        new AttrObject(
                                            new IveObjectImpl(obj.getId())));
                                }
                            } else {
                                copy.addAttribute(name, new AttrObject());
                            }
                        } else if (attr instanceof AttrCollection) {
                            Set<IveObject> set = new HashSet<IveObject>();
                            for (IveObject obj: 
                                    ((AttrCollection) attr).getValue()) {
                                if (obj != null) {
                                    set.add(new IveObjectImpl(obj.getId()));
                                }
                            }
                            copy.addAttribute(name, new AttrCollection(set));
                        } else {
                            /* AttributeValue */
                            copy.addAttribute(name, 
                                    (AttributeValue) attr.clone());

                            if (listener != null) {
                                attr.registerListener(listener);
                            }
                        }
                    } else {
                        if (!copyVal.equals(attr)) {
                            change = true;
                            copy.changeAttribute(name, attr);
                        }
                    }
                
                    break;//sensors
                }
            }
        }
        
        toDelete.removeAll(accetable);
        if (!toDelete.isEmpty()) {
            change = true;
        }
        for (String id: toDelete) {
            if (listener != null) {
                orig.getAttribute(id).unregisterListener(listener);
            }
            copy.removeAttribute(id);
        }
        
        if (!orig.getLinks().equals(copy.getLinks())) {
            copy.setLinks(orig.getLinksCopy());
            change = true;
        }
        
        return change;
    }
    
    /**
     * Gets an empty phantom of the given object.
     * @param object Object whose fantom is to be crated
     * @return New phantomobject
     */
    IveObject getEmptyPhantom(IveObject object) {
        
        if (object instanceof WayPoint) {
            return ((WayPoint) object).getPhantom();
        }
        
        return new IveObjectImpl(object.getId());
    }
    
    /**
     * Construct result set of copies from given original IveObject
     * according to flag.
     *
     * @param oldRes result from previsious calling - objects are reused;
     *	null can be passed.
     * @param originSet set of valid IveObjects
     * @param sensors list of sensors to be applied while objects are copied
     * @param flag specifies how will be result formed
     * @return list of IveObject copies
     */
    List<IveObject> makeResultSet(List<IveObject> oldRes,
            Set<IveObject> originSet, List<Sensor> sensors, ReturnSet flag) {
        ArrayList<IveObject> copySet = new ArrayList<IveObject>();
        IveObject one;
        IveObject copy = null;
        if (!originSet.isEmpty()) {
            int anyInt = (int)(Math.random() * originSet.size());
            one = (IveObject) originSet.toArray()[anyInt];
        } else {
            return copySet;
        }
        
        switch (flag) {
            case ALL_COPY:
                for (IveObject o: originSet) {
                    copy = null;
                    int old;
                    if (oldRes != null && (old = oldRes.indexOf(o)) != -1) {
                        copy = oldRes.get(old);
                    }
                    if (copy == null) {
                        copy = getEmptyPhantom(o);
                    }
                    updateCopy(sensors, o, copy, null);
                    copySet.add(copy);
                }
                break;
            
            case ALL_EMPTY:
                for (IveObject o: originSet) {
                    copy = null;
                    int old;
                    if (oldRes != null && (old = oldRes.indexOf(o)) != -1) {
                        copy = oldRes.get(old);
                    }
                    if (copy != null) {
                        copySet.add(copy);
                    } else {
                        copySet.add(getEmptyPhantom(o));
                    }
                }
                break;
            
            case ALL_FIRST_COPY: 
                if (oldRes != null && !oldRes.isEmpty()) {
                    for (IveObject o: originSet) {
                        if (o.getId().toString().equals(
                                oldRes.get(0).getId().toString())) {
                            one = o;
                            copy = oldRes.get(0);
                            break;
                        }
                    }
                }
                
                if (copy == null) {
                    copy = getEmptyPhantom(one);
                }
                updateCopy(sensors, one, copy, null);
                copySet.add(copy);
                
                for (IveObject o: originSet) {
                    int old;
                    if (one.equals(o)) {
                        continue;
                    }
                    if (oldRes != null &&
                            (old = oldRes.indexOf(o)) != -1) {
                        copySet.add(oldRes.get(old));
                    } else {
                        copySet.add(getEmptyPhantom(o));
                    }
                }
                break;
            
            case ANY_COPY: 
                copy = null;
                if (oldRes != null && !oldRes.isEmpty()) {
                    copy = oldRes.get(0);
                    for (IveObject o: originSet) {
                        if (o.getId().toString().equals(
                                copy.getId().toString())) {
                            one = o;
                            break;
                        }
                    }                    
                } 
                if ((copy == null) || (!copy.getId().equals(one.getId()))) {
                    copy = getEmptyPhantom(one);
                }
                updateCopy(sensors, one, copy, null);
                copySet.add(copy);
                break;

            case ANY_EMPTY: 
                if (oldRes != null && !oldRes.isEmpty()) {
                    for (IveObject o: originSet) {
                        if (o.getId().toString().equals(
                                oldRes.get(0).getId().toString())) {
                            one = o;
                            break;
                        }
                    }
                }
                copySet.add(getEmptyPhantom(one));
                break;
        }
        
        return copySet;
    }
    
    public void expand(WayPoint location) {
    }
    
    public void shrink(WayPoint location) {
    }
    
}
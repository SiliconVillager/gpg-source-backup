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
 
package cz.ive.iveobject;

import cz.ive.lod.*;
import cz.ive.gui.*;
import cz.ive.logs.Log;
import cz.ive.messaging.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.location.WayPoint;
import cz.ive.location.Kind;
import cz.ive.exception.ObjectRegistrationFailedException;
import cz.ive.iveobject.IveObject.ObjectState;
import cz.ive.manager.ManagerOfSenses;
import cz.ive.process.IveProcess;
import cz.ive.xmlload.ObjectInstanceInfo;
import java.util.*;
import java.util.Map.Entry;
import java.util.HashMap;
import java.io.Serializable;

/**
 * Straight implementation of IveObject interface.
 *
 * @author Jirka
 */
public class IveObjectImpl extends IveIdImpl
	implements IveObject, Hook, Cloneable {
    
    /** attributes of the object, including both values and other objects */
    private HashMap<String,AttributeValue> attributes;
    
    /** set of all links to process */
    private Set<Link> links;
    
    /** Processes having this object as a source */
    private List<IveProcess> processes;
    
    /** Processes having this object as an actor */
    private List<IveProcess> actorProcesses;
    
    /** Indicate whether changes are propagated directly or they are kept off. */
    private boolean notificable;
    
    /**
     * Indicate whether change is to be signalized
     * after enabling of notification.
     */
    private boolean changeInside;
    
    /**
     * Set of attributes where change is to be signaled
     * after enabling of notification.
     */
    private Set<AttributeValue> toSignal;
    
    /** 
     * Object state 
     */
    protected IveObject.ObjectState state;
    
    /**
     * Map for inherent storing of non-existing objects.
     * Object's class is mapped to count of stored objets.
     */
    protected HashMap<String, InherentObject> inherent;
    
    /** slave objects */
    protected Set<IveObject> slaves;
    
    /** invalid objects */
    protected Set<IveObject> invalid;

    /** stored objects */
    protected Set<IveObject> stored;

    /** signal whether object is only a working copy */
    protected boolean isPhantom;
    
    /** mask of applicable sensors - each bit determine one sensor */
    protected long sensorMask;
    
    /** Holdback of this IveObject */
    protected Holdback holdback;
    
    /** GraphicInfo associated with this object */
    protected GraphicInfo grInfo;
    
    /** IveObject class of this object */
    protected ObjectClass objectClass = ObjectClassTree.getRoot();
    
    /** 
     * Determines whether the object occupy the WayPoint it is standing on.
     * In case of WayPoints, determines whether the WayPoint can be occupied -
     * whether we are on the material level. 
     */
    protected boolean substantial;
    
    /** 
     * Kind of the Object used for selective path finding and placement.
     * In case of WayPoint are applied these rules:
     * If kind is null, this WayPoint can be used in every path.
     * If kind contains an empty array, no path can use this WayPoint.
     * If any numbers are present, The WayPoint can be used in every path, 
     * which uses at least one of the listed numbers.<br>
     * In case of another object, these rules are applied:
     * If kind is null, this object can be placed on any WayPoint.
     * If kind contains an empty array, this object cannot be placed to any WayPoint.
     * If any numbers are present, the object can be placed on any WayPoint,
     * whose kind contains at least one of the listed numbers.
     */
    protected Kind kind;
    
    /** 
     * listeners who don't want to be notified about changes in 
     * object attributes 
     */
    private ArrayList<Listener> notifiedSpecial;
    
    /**
     * Create new instance of IveObject with specified id.
     * This object can be thought as a Fantom (isFantom is set to true).
     * All object containers are not being inicialized.
     * Sensor mask is implicitly set to be object caught up by EYE sensor.
     *
     * @param objectId unique string id
     */
    public IveObjectImpl(String objectId){
	super(objectId);
	isPhantom = true;
	sensorMask = 1;
	notificable = true;
	notifiedSpecial = new ArrayList<Listener>();
        substantial = false;
    }
    
    public IveObjectImpl(String objectId,ObjectClass objCls)
    {
        super(objectId);
        notificable = true;
        objectClass = objCls;
	notificable = true;
        initialize();
    }
    
    /**
     * Create new instance of IveObject with specified id.
     * With regard to valid variable, behaviour is same as in the first
     * constructor or actions from @see IveObjectImpl#validate are done.
     *
     * @param objectId unique string id
     * @param valid true determine that object will be also validated
     */
    public IveObjectImpl(String objectId, boolean valid)
    throws ObjectRegistrationFailedException {
	this(objectId);
	if (valid == true) {
	    validate();
	}
    }
    
    /**
     * Initializes all containers required by a valid WayPoint, clears
     * phantom flag.
     */
    public void initialize() {
	isPhantom = false;
	attributes = new HashMap<String,AttributeValue>();
	links = new HashSet<Link>();
        slaves = new HashSet<IveObject>();
        stored = new HashSet<IveObject>();
        invalid = new HashSet<IveObject>();
	toSignal = new HashSet<AttributeValue>();
	notifiedSpecial = new ArrayList<Listener>();	
        inherent = new HashMap<String, InherentObject>();
        processes = new LinkedList<IveProcess>();
        actorProcesses = new LinkedList<IveProcess>();
        state = ObjectState.UNKNOWN;
}
    
    
    /**
     * Set object as valid (real) IveObject with inicialized containers
     * (but no attributes or links are presented), phantom indicator
     * is set to false and object is registred at ObjectMap.
     */
    public IveObjectImpl validate() throws ObjectRegistrationFailedException {
        initialize();
	if (ObjectMap.instance().register(this) == false) {
	    throw new ObjectRegistrationFailedException(
		    "Attemp to register object with id " + id + " failed.");
	}	
	return this;
    }
    
    /**
     * Make a deep copy of the object with no listeners.
     */
    public Object clone() {
	
	IveObjectImpl o = null;
	try {
	    o = (IveObjectImpl) super.clone();
	} catch (CloneNotSupportedException e) {}
	
	o.attributes = (HashMap<String, AttributeValue>) o.attributes.clone();
	Set<Entry<String,AttributeValue>> set = o.attributes.entrySet();
	for ( Iterator<Entry<String,AttributeValue>> i = set.iterator();
	i.hasNext(); ) {
	    
	    Entry<String,AttributeValue> entry = i.next();
	    entry.setValue((AttributeValue) entry.getValue().clone(o));
	    //name - key of the attribute - stay unchanged
	}
	
	o.links = new HashSet<Link>();
	for ( Iterator<Link> i = links.iterator(); i.hasNext(); ) {
	    o.links.add( (Link) i.next().clone());
	}

        o.slaves = new HashSet<IveObject>();
	for ( Iterator<IveObject> i = slaves.iterator(); i.hasNext(); ) {
	    o.slaves.add( (IveObject) ((IveObjectImpl) i.next()).clone());
	}

        o.inherent = new HashMap<String, InherentObject>();
	for ( Entry<String, InherentObject> i : inherent.entrySet()) {
	    o.inherent.put(i.getKey(),  i.getValue().clone());
	}

        o.stored = new HashSet<IveObject>();
	for ( Iterator<IveObject> i = stored.iterator(); i.hasNext(); ) {
	    o.stored.add( (IveObject) ((IveObjectImpl) i.next()).clone());
	}
        
        o.listeners = new HashSet<Listener>();
	o.setAsPhantom();
	return o;
    }
    

    
    /**
     * Gets position of the object from it's attributes.
     *
     * @return position, stored in object's attributes or null when no one
     *  is defined
     */
    public WayPoint getPosition() {
        AttrObject pos = (AttrObject) getAttribute("position");
	return (pos == null) ? null : (WayPoint) pos.getValue();
    }
    
    /**
     * Add location defined as WayPoint into object attributes.
     */
    public void setPosition(WayPoint wp) {
	AttrObject pos = new AttrObject(wp, null);
        if (!changeAttribute("position", pos)) {
            addAttribute("position", pos);
        }
    }
    
    public IveObject getMaster() {
	AttrObject master = (AttrObject) getAttribute("master");
	return master.getValue();
    }
    
    public void setMaster(IveObject master) {
	AttrObject masterAttr = new AttrObject(master, null);
	if (!changeAttribute("master", masterAttr)) {
	    addAttribute("master", masterAttr);
	}
    }

    public Set<IveObject> getSlaves() {
        /* slaves as attribute collection - does not work due to cyclic 
           dependencies */
        /*
	AttrCollection slaves = (AttrCollection) getAttribute("slaves");
        return (slaves != null) ? slaves.getValue() : new HashSet<IveObject>();
        */
        return slaves;
    }
    
    /**
     * Set all slaves of this object.
     */
    private void setSlaves(Set<IveObject> slaves) {
        /* slaves as attribute collection - does not work due to cyclic 
           dependencies */
        /*
        AttrCollection slavesColl = new AttrCollection(slaves, null);
        if (!changeAttribute("slaves", slavesColl)) {
            addAttribute("slaves",  slavesColl);
        }
        */
        
        this.slaves = slaves;
    }
    
    /**
     * Add slave to this object.
     */
    private void addSlave(IveObject slave) {
        if (slaves == null) {
            slaves = new HashSet<IveObject>();
        }
        slaves.add(slave);
    }

    /**
     * Remove slave of this object.
     */
    private void removeSlave(IveObject slave) {
        if (slaves != null) {
            slaves.remove(slave);
        }
    }

    public void addInvalid(IveObject slave) {
        invalid.add(slave);
    }
    
    public void removeInvalid(IveObject slave) {
        invalid.remove(slave);
    }

    public boolean store(IveObject slave) { 
        
        if (getObjectState() == ObjectState.NOT_EXIST) {
            if (slave.isParent(this)) { 
                ObjectMap.instance().unregister(slave.getId());
                return true;
            } else {
                return false;
            }
        }
        String objectClass = slave.getObjectClass().toString();
        if (inherent.containsKey(objectClass)) {
            /* store as inherent */
            inherent.get(objectClass).count++;
            ObjectMap.instance().unregister(slave.getId());
        } else {
            /* store as stored */
            if (new IveIdImpl(slave.getParentId()).getParent() != this) {
                /* the object is moved outside of its home location */
                ObjectMap.instance().hide(slave.getId());
                stored.add(slave);
            } else {
                /* the object is in its home location and will be generated
                 * during shrink */
                ObjectMap.instance().unregister(slave.getId());
            }
        }
        return true;
    }
    
    public void removeStored(IveObject slave) { 
        stored.remove(slave);
    }
    
    public void loosingMaster() {
    }
    
    public void loosingSlave(IveObject slave) {
    }
    
    /**
     * Recursively moves whole subtree of slave objects
     * @param root Current root of the subtree
     * @param location Location to which we are moving whole tree
     * @param masterExists Determines whether the master exists in the new place
     */
    private void addSubObjects(IveObject root, WayPoint location, 
            boolean masterExists) {
        IveObject[] subObjects;
        Holdback holdback;
        boolean exists = true;
        
        holdback = root.getHoldback();
        if (location.getLod() >= holdback.getView()) {
            if (!masterExists) {
                root.loosingMaster();
                ((IveObjectImpl) root.getMaster()).removeSlave(root);
                location.addObject(root);
                return;
            }
            LodManager.getJam().addObject(root, location);
            ManagerOfSenses.getJam().addObject(root, location);
            location.getObjects().add(root);
            root.setPosition(location);
            root.setLod(location.getLod());
            root.setObjectState(ObjectState.VALID);
        } else if (location.getLod() >= holdback.getExistence()) {
            if (!masterExists) {
                root.loosingMaster();
                ((IveObjectImpl) root.getMaster()).removeSlave(root);
                location.addObject(root);
                return;
            }
            LodManager.getJam().addObject(root, location);
            location.addInvalid(root);
            if ((root.getPosition() != null) && 
                (!root.getPosition().isPhantom())) {
                    root.setPosition(root.getPosition().getPhantom());
            }
            root.setObjectState(ObjectState.INVALID);
        } else {
            IveObject master = root.getMaster();
            
            if (!(master instanceof WayPoint) && masterExists) {
                root.getMaster().loosingSlave(root);
                root.getMaster().removeObject(root);
            }
            
            while ((master != null) && (!master.store(root))) {
                master = master.getMaster();
            }
            if ((root.getPosition() != null) && 
                (!root.getPosition().isPhantom())) {
                    root.setPosition(root.getPosition().getPhantom());
            }
            root.setObjectState(ObjectState.NOT_EXIST);
            exists = false;
        }

        subObjects = root.getSlaves().toArray(new IveObject[0]);
        for (IveObject subObject : subObjects) {
            addSubObjects(subObject, location, exists);
        }
        
    }
    
    /**
     * Adds a slave object.
     * Implements an IveObject interface.
     * Adds the slave object to this object and changes storage of the object
     * and all it's slaves according to the LOD level. All placement-related
     * attributes are updated.
     * @param slave Object to be added
     */
    public void addObject(IveObject slave) {

        slave.setMaster(this);
        
        if (this instanceof WayPoint) {
            
            if (isSubstantial() && 
                    ObjectClassTree.instance().getObjectClass(
                        "/system/Holdback").isInside(slave) &&
                    (slave.getHoldback().getView() > getLod()) &&
                    (slave.getHoldback().getExistence() <= getLod())) {
                slave.setHoldback(
                        new Holdback(slave.getHoldback().getExistence(), 
                        getLod()));
            }
                
            addSubObjects(slave, (WayPoint) this, true);
        } else {
            WayPoint location = getPosition();
            
            // If the location == null, then we are just instantiating
            // the object template, and no subtree placement is needed
            if (location != null) {
                addSubObjects(slave, location, true);
            }
        }

        if ((slave.getObjectState() != ObjectState.NOT_EXIST)) {
            addSlave(slave);
        }
       
    }
    
    /**
     * Adds a slave object and creates a new attribute.
     * Implements an IveObject interface.
     * Adds the slave object to this object and changes storage of the object
     * and all it's slaves according to the LOD level. All placement-related
     * attributes are updated.
     * A new attribute with the given name is created, containing the new
     * slave object.
     * @param slave Object to be added
     * @param attrName Name of the new attribute with the slave object
     */
    public void addObject(IveObject slave, String attrName) {
        addObject(slave);
        addAttribute(attrName, new AttrObject(slave, null));
    }
    
    /**
     * Recursively removes whole subtree of slave objects
     * @param root Current root of the subtree
     * @param location Location from which we are moving whole tree
     */
    private void removeSubObjects(IveObject root, WayPoint location) {
        Set<IveObject> subObjects;
        IveObject subObject;
        Holdback holdback;
        
        subObjects = root.getSlaves();
        for (Iterator<IveObject> i = subObjects.iterator(); i.hasNext(); ) {
            subObject = i.next();
            removeSubObjects(subObject, location);
            
        }

        holdback = root.getHoldback();
        if (location.getLod() >= holdback.getView()) {
            LodManager.getJam().removeObject(root, location);
            ManagerOfSenses.getJam().removeObject(root, location);
            location.getObjects().remove(root);
        } else if (location.getLod() >= holdback.getExistence()) {
            LodManager.getJam().removeObject(root, location);
            location.removeInvalid(root);
        }
    }

    /**
     * Removes a slave object.
     * Implements an IveObject interface.
     * Removes the slave object and all it's slaves from this object and 
     * updates all placement-related attributes.
     */
    public void removeObject(IveObject slave) {
        removeSlave(slave);
        if (this instanceof WayPoint) {
            removeSubObjects(slave, (WayPoint) this);
        } else {
            if (! getPosition().isPhantom()) {
                removeSubObjects(slave, getPosition());
            }
        }
    }
    
    /**
     * Method fails when no lod is defined for the object.
     *
     * @return lod value
     */
    public int getLod() {
	AttrInteger lod = (AttrInteger) getAttribute("lod");
	assert (lod != null);
	return lod.getValue();
    }
    
    /**
     * Add lod into object attributes.
     *
     * @param l lod value to be set
     */
    public void setLod(int l) {
	AttrInteger lod = new AttrInteger(l, null);
	if (!changeAttribute("lod", lod)) {
	    addAttribute("lod", lod);
	}
    }
    
    /**
     * Set object as Phantom.
     */
    public void setAsPhantom() {
	isPhantom = true;
    }
    
    public boolean isPhantom() {
	return this.isPhantom;
    }
    
    public void suspendNotification() {
	if (notificable) {
	    notificable = false;
	    changeInside = false;
	}
    }
    
    public void restoreNotification() {
	if (!notificable) {
	    notificable = true;
	    if (changeInside || !toSignal.isEmpty()) {
		for (AttributeValue i: toSignal) {
		    i.notification();
		}
		notifyListeners();
	    }
	}
    }
    
    public boolean isNotificable() {
	return notificable;
    }
    /**
     * To make notifyListener visible outside the object. If notification is
     * suspended, initiator is stored. Method is called by objects attributes.
     *
     * @param initiator attribute who emits the signal
     */
    public void notification(AttributeValue initiator) {
	if (notificable) {
	    notifyListenersSpecial();
	} else {
	    toSignal.add(initiator);
	}
    }
    
    protected void notification() {
	if (notificable) {
	    notifyListeners();
	} else {
	    changeInside = true;
	}
    }
    
    /**
     * Notified only listener who are not special registered.
     */
    protected void notifyListenersSpecial() {
	for (Listener i: listeners) {
	    if (!notifiedSpecial.contains(i)) {
		i.changed(this);
	    }
	}
    }
    
    public String getId() {
	return id;
    }
    
    /**
     * Finds the minimum level of existence of all slaves.
     * This is used to set the effective level of existence appropriately,
     * not to have non-existing object with existing slaves.
     */
    private int neededExistence() {
        int min = Integer.MAX_VALUE; 
        int val;
        /* slaves as attribute collection - does not work due to cyclic 
           dependencies */
        /*
        Set<IveObject> slaves;
        AttrCollection slavesAttr;
        
        slavesAttr = (AttrCollection) getAttribute("slaves");
        if (slavesAttr == null) {
            return Integer.MAX_VALUE;
        }
            
        slaves = slavesAttr.getValue();
        */
        
        for (Iterator<IveObject> i = slaves.iterator(); i.hasNext(); ) {
            val = i.next().getHoldback().getExistence();
            if (val < min) {
                min = val;
            }
        }
        
        return min;
    }
    
    public Holdback getHoldback() {
	return holdback;
    }
    
    public void setHoldback(Holdback holdback) {

        this.holdback = holdback;
    }
    
    public long getSensorMask() {
	return sensorMask;
    }
    
    public void setSensorMask(long mask) {
	sensorMask = mask;
    }
    
    public AttributeValue getAttribute(String fullName){
	return (attributes == null) ? null : attributes.get(fullName);
    }
    
    public Set<AttributeValue> getAllAtributes() {
	Set<AttributeValue> ret = new HashSet<AttributeValue>();
	if (attributes != null && !attributes.isEmpty()) {
	    ret.addAll(attributes.values());
	}
	return ret;
    }
    
    public void addAttribute(String fullName, AttributeValue value){
	if (attributes == null) {
	    attributes = new HashMap<String,AttributeValue>();
	}
        
        removeAttribute(fullName);
	
        attributes.put(fullName, value);
	value.setName(fullName);
	value.setParent(this);
	notification();
    }
    
    public boolean changeAttribute(String fullName, AttributeValue value) {
	
	AttributeValue oldValue = ((attributes == null) ?
	    null : attributes.get(fullName));
	
	if (oldValue != null) {
	    oldValue.changeValue(value);
	    return true;
	} else {
	    return false;
	}
    }
    
    public boolean removeAttribute(String fullName) {
	
	AttributeValue attr = ((attributes == null) ?
	    null : attributes.remove(fullName));
	if (attr != null) {
	    attr.setName(null);
	    attr.setParent(null);
	    attr.unregisterAll();
	    notification();
	    return true;
	}
	return false;
    }
    
    public Set<String> getNamesByPart(String partName) {
	
	Set<String> ret = new HashSet<String>();
	if (attributes == null) {
	    return ret;
	}
	Set<String> allNames = attributes.keySet();
	
	if (partName == null) {
	    ret.addAll(allNames);
	} else {
	    for (String l: allNames) {
		if (l.startsWith(partName)) {
		    ret.add(l);
		}
	    }
	}
	return ret;
    }
    
    public Set<Link> getLinks(String goal, String process, String role) {
	
	Set<Link> ret = new HashSet<Link>();
	if (links == null) {
	    return ret;
	}
	
	for (Link l: links) {
	    if (
                    (goal.equals("") || goal.equals(l.getGoal()) || 
                    l.getGoal().equals("*")) &&
                    (process.equals("") || process.equals(l.getProcess()) ||
                    l.getProcess().equals("*")) &&
                    (role.equals("") || role.equals(l.getRole()) || 
                    l.getRole().equals("*"))) {
		ret.add(l);
	    }
	}
	return ret;
    }
    
    public Set<Link> getLinks() {
	return links;
    }
    
    public Set<Link> getLinksCopy() {
	Set<Link> ret = new HashSet<Link>();
	if (links == null) {
	    return ret;
	}
	for (Link l: links) {
	    ret.add((Link) l.clone());
	}
	return ret;
    }
    
    public void setLinks(Set<Link> l) {
	links = l;
    }
    
    public void addLink(Link link){
	if (links == null) {
	    links = new HashSet<Link>();
	}
	links.add(link);
	notification();
    }
    
    public void removeLinks(String goal, String process, String role) {
	
	boolean change = false;
	if (links == null) {
	    return;
	}
	//working copy for iteration
	Set<Link> iterSet = (Set<Link>) ((HashSet<Link>) links).clone();
	for (Link l: iterSet) {
	    if ((goal.equals("") || goal == l.getGoal())
	    && (process.equals("") || process == l.getProcess())
	    && (role.equals("") || role == l.getRole())) {
		links.remove(l);
		change = true;
	    }
	}
	if (change) {
	    notification();
	}
    }
    
    public boolean removeLink(Link link) {
	
	if (links != null && links.remove(link)) {
	    notification();
	    return true;
	} else {
	    return false;
	}
    }
    
    public void registerListenerSpecial(Listener listener) {
	registerListener(listener);
	notifiedSpecial.add(listener);
    }
    
    public void unregisterListener(Listener listener) {
	super.unregisterListener(listener);
	notifiedSpecial.remove(listener);
    }
    
    public void unregisterAll() {
	super.unregisterAll();
	notifiedSpecial.clear();
    }    
    
    /**
     * Debug function - creates string containing values stored in object.
     */
    public String debugPrint() {
	
	String out = "";
	out += "IveObject - id: " + this.getId();
	if (attributes == null && links == null) {
	    out += "; object is empty phantom.";
	    return out;
	} else if (isPhantom) {
	    out += "; object isn't valid, it's a copy.";
	}
	if (attributes != null) {
	    out += "\n\tAttributes:";
	    Set<Entry<String,AttributeValue>> set = attributes.entrySet();
	    for ( Iterator<Entry<String,AttributeValue>> i = set.iterator();
	    i.hasNext(); ) {
		Entry<String,AttributeValue> entry = i.next();
		out += "\n\t\tname: " + entry.getKey()
		+ ", value: " + entry.getValue().toString();
	    }
	}
	if (links != null) {
	    out += "\n\tLinks:";
	    for ( Iterator<Link> i = links.iterator(); i.hasNext(); ) {
		Link l = i.next();
		out += "\n\t\t- goal: " + l.getGoal() + ", process: "
			+ l.getProcess() + ", role: " + l.getRole();
	    }
	}
	return out;
    }
    
    public GraphicInfo getGraphicInfo() {
        return grInfo;
    }
    
    public void setGraphicInfo(GraphicInfo grInfo) {
        this.grInfo = grInfo;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }
    
    public ObjectClass getObjectClass() {
        return objectClass;
    }

    public void addInherentClass(String objectClass, ObjectInstanceInfo info) {
        inherent.put(objectClass,  new InherentObject(info));
    }

    public List<IveProcess> getProcesses() {
        return processes;
    }
    
    public void addProcess(IveProcess process) {
        processes.add(process);
    }
    
    public void removeProcess(IveProcess process) {
        processes.remove(process);
    }

    public List<IveProcess> getActorProcesses() {
        return actorProcesses;
    }
    
    public void addActorProcess(IveProcess process) {
        actorProcesses.add(process);
    }
    
    public void removeActorProcess(IveProcess process) {
        actorProcesses.remove(process);
    }

    /**
     * Information needed about object when it is inherently stored.
     */
    public class InherentObject implements Serializable {
        /** Number of objects of this type */
        public int count;
        /** Info needed to create the object */
        public ObjectInstanceInfo info;
        /** 
         * Creates a new instance of InherentObject 
         * @param info Information needed to create the object
         */
        public InherentObject(ObjectInstanceInfo info) {
            this.info = info;
            count = 0;
        }
        /**
         * Makes a deep copy of this object
         */
        public InherentObject clone() {
            InherentObject o = null;
            try {
                o = (InherentObject) super.clone();
            } catch (CloneNotSupportedException e) {}
            return o;
        }
    }

    public Set<IveObject> getAllObjects() {
        Set<IveObject> set = new HashSet<IveObject>();
        /* slaves as attribute collection - does not work due to cyclic 
           dependencies */
        /*
        AttrCollection slavesColl;
        
        slavesColl = (AttrCollection) getAttribute("slaves");
        if (slavesColl != null) {
            set.addAll(slavesColl.getValue());
        }
        */
        
        set.addAll(slaves);
        set.addAll(invalid);
        set.addAll(stored);
        
        return set;
    }
    
    public void setObjectState(ObjectState state) {
        Log.addMessage("State changed to "+state.toString(), Log.FINEST, 
                this, null, getPosition());
        this.state = state;
    }
    
    public ObjectState getObjectState() {
        return state;
    }

    public boolean isSubstantial() {
        return substantial;
    }
    
    public void setSubstantial(boolean value) {
        substantial = value;
    }
}

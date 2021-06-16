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

import cz.ive.messaging.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.location.*;
import cz.ive.lod.*;
import cz.ive.gui.*;
import cz.ive.process.IveProcess;
import cz.ive.template.Template;
import cz.ive.xmlload.ObjectInstanceInfo;
import java.util.*;

/**
 * Interface for IveObject.
 *
 * @author Jirka
 */
public interface IveObject extends IveId, Hook {
   
    /** States of object */
    enum ObjectState {
        
        /** Object state is not known (object was only created yet) */
        UNKNOWN, 
        
        /** Object does not exist */
        NOT_EXIST, 
                
        /** Object does exist, but is invalid */
        INVALID,
                
        /** Object exists and is valid */
        VALID,
    }
    
    /**
     * Gets attribute object with its value.
     * @param fullName name of the attribute
     * @return attribute object or null for unknown name.
     */
    public AttributeValue getAttribute(String fullName);
    
    /**
     * Gets all attributes contained in object.
     * @return set containing all attributes
     */
    public Set<AttributeValue> getAllAtributes();        
    
    /**
     * Adds new attribute object. 
     * @param fullName name of the attribute
     * @param value new attribute to be added
     */
    public void addAttribute(String fullName, AttributeValue value);
    
    /**
     * Replace original attribute object by a new one.
     * @param fullName name of the attribute
     * @param value new value which replace original
     * @return true on successful replacement
     *	and false when given name did not match any of existing.
     */
    public boolean changeAttribute(String fullName, AttributeValue value);
    
    /**
     * Remove attribute object from its set.
     * @param fullName multi level name of attribute
     * @return true if attribute was removed
     *	and false when given name did not match any of existing.
     */
    public boolean removeAttribute(String fullName);
    
    /**
     * Gets all names associated with attributes of the object
     * that have same corresponding first part of name.
     * All attribute names are returned, when null is passed.
     *
     * @param partName starting part of attribute name
     * @return set of all matching attributes names
     */
    public Set<String> getNamesByPart(String partName);
    
    /**
     * Gets all links matching given params.
     * Given string "" means that all values matching.
     * Original Link objects are returned.
     *
     * @param goal goal id
     * @param process process id
     * @param role role id
     * @return list of all matching links.
     */
    public Set<Link> getLinks(String goal, String process, String role);
    
    /** 
     * Gets all links without matching.
     *
     * @return original links container
     */
    public Set<Link> getLinks();
    
    /**
     * Gets all links without matching.
     *
     * @return copies of all links
     */
    public Set<Link> getLinksCopy();
    
    /**
     * Assign set of links to IveObject
     *
     * @param l set of all links
     */
    public void setLinks(Set<Link> l);
    
    /**
     * Add new link to IveObject.
     *
     * @param link new link to add
     */
    public void addLink(Link link);
    
    /**
     * Remove all links matching given params.
     * Given string "" means that all values matching.
     *
     * @param goal goal id
     * @param process process id
     * @param role role id
     */
    public void removeLinks(String goal, String process, String role);
    
    /**
     * Remove specified link.
     *
     * @param link link to remove
     * @return true if link was removed
     *	and false if object did not contains given link.
     */
    public boolean removeLink(Link link);
    
    /**
     * Phantoms and other objects are not registered in the ObjectMap
     * these working objects are marked as a copy.
     * @return true if object is a copy
     */
    public boolean isPhantom();
    
    /**
     * Stop signalization of object changes and changes in its attributes.
     * All notifications are stored and propagate 
     * after {@link #restoreNotification() restoreNotification} is called.
     */
    public void suspendNotification();
    
    /**
     * Enable notifications and call all one which has been caused 
     * since {@link #suspendNotification() suspendNotification} was called.
     */
    public void restoreNotification();
    
    /**
     * @return object of position
     */
    public WayPoint getPosition();
    
    /**
     * Sets position of object or changes it to new value.
     */
    public void setPosition(WayPoint wp);
    
    /**
     * Stores a non-existing object, either as ingerent or to the stored list
     * @return true if the object was stored;
     *  false if the object was not stored, try the master
     */
    public boolean store(IveObject slave);
    
    /**
     * Adds a slave object and makes all related work with aother attributes.
     * @param slave Object to be added
     */
    public void addObject(IveObject slave);

    /**
     * Adds a slave object and makes all related work with aother attributes,
     * registeres a new slave as an attribute.
     * @param slave Object to be added
     * @param attrName Name of the new attribute with the slave object
     */
    public void addObject(IveObject slave, String attrName);
    
    /**
     * Removes a slave object and makes all related work with aother attributes.
     * @param slave Object to be removed
     */
    public void removeObject(IveObject slave);

    /**
     * @return master of this object
     */
    public IveObject getMaster();
    
    /**
     * Sets master of this object or changes it to a new value.
     * Do not use it directly, use addObject method on the master object.
     */
    public void setMaster(IveObject master);

    /**
     * @return slaves of this object
     */
    public Set<IveObject> getSlaves();
  
    /**
     * Adds a slave to the invalid list
     * Do not use it directly, use addObject method instead.
     */
    public void addInvalid(IveObject slave);
    
    /**
     * Removes a slave from the invalid list
     * Do not use it directly, use removeObject method instead.
     */
    public void removeInvalid(IveObject slave);

    /**
     * Removes a slave from the stored list
     * Do not use it directly, use removeObject method instead.
     */
    public void removeStored(IveObject slave);

    /**
     * Getter for holdback associated with this IveObject.
     * Levels of View and existence values of this holdback influence
     * LOD in the area where is this object situated. For details
     * see specification.
     *
     * @return holdback of this IveObject. This value cannot be null.
     */
    public Holdback getHoldback();
    
    /**
     * Sets holdback of this object, this is supposed to be called
     * just once during its initialization. Manager of LOD relies
     * on constant value of this attribute, during whole simulation.
     *
     * @param holdback new holdback to be assigned with this object
     */
    public void setHoldback(Holdback holdback);
    
    /**
     * @return lod value
     */
    public int getLod();
    
    /**
     * @param l lod value to be set
     */
    public void setLod(int l);    
    
    /** 
     * Register listener which will be not notified about changes in attribute
     * values.
     *
     * @param listener listener to be registred
     */
    public void registerListenerSpecial(Listener listener);
    
    /**
     * Retrives GraphicInfo associated with this IveObject.
     *
     * @return GraphicInfo that can be called for visualisation of this object
     */
    public GraphicInfo getGraphicInfo();

    /**
     * Sets GraphicInfo associated with this IveObject.
     *
     * @param grInfo GraphicInfo to be associated with this object. This info
     *      can be later on used for visualisation of this object.
     */
    public void setGraphicInfo(GraphicInfo grInfo);
    
    /**
     * Gets the kind of the Object (used in locations placement).
     * @return Kind of the Object
     */
    Kind getKind();

    /**
     * Sets the kind of the Object (used in locations placement).
     * @param kind Kind of the object
     */
    void setKind(Kind kind);

    /**
     * Retrieves ObjectClass of this IveObject
     *
     * @return smallest (in the sense of inclusion) ObjectClass that this 
     *      IveObject belongs to
     */
    public ObjectClass getObjectClass();
    
    /**
     * Adds id of object class which will be stored inherently in this object.
     * @param objectClass object class id of the inherent objects to be stored
     * @param info Instance info needed to create the object
     */
    void addInherentClass(String objectClass, ObjectInstanceInfo info);

    /**
     * Getter for list of processes, which have this object as a source.
     * @return List of all processes on which this objets participates as 
     * a source
     */
    List<IveProcess> getProcesses();
    
    /**
     * Add a new process to the list of processes which have this object as 
     * a source.
     * This is to be called by WorldInterpreter.
     * @param process Process to be added to the list 
     */
    void addProcess(IveProcess process);
    
    /**
     * Remove a process from the list of processes which have this object as 
     * a source.
     * This is to be called by WorldInterpreter.
     * @param process to be removed from the list.
     */
    void removeProcess(IveProcess process);
    
    /**
     * Getter for list of processes, which have this object as an actor.
     * @return List of all processes on which this objets participates as 
     * an actor
     */
    List<IveProcess> getActorProcesses();
    
    /**
     * Add a new process to the list of processes which have this object as 
     * an actor.
     * This is to be called by WorldInterpreter.
     * @param process Process to be added to the list 
     */
    void addActorProcess(IveProcess process);
    
    /**
     * Remove a process from the list of processes which have this object as 
     * an actor.
     * This is to be called by WorldInterpreter.
     * @param process to be removed from the list.
     */
    void removeActorProcess(IveProcess process);
    
    /**
     * Gets all objets stored in this object (in all states).
     * @return the stored objects
     */
    Set<IveObject> getAllObjects();
    
    /**
     * Sets object state.
     * @param state New state
     */
    void setObjectState(ObjectState state);
    
    /**
     * Gets the object state.
     * @return current state of the object
     */
    ObjectState getObjectState();
    
    /** 
     * Checks whether the object is substantial.
     * Substantial means it can occupy a WayPoint. In case of the WayPoint 
     * substantial means it can by occupied.
     */
    boolean isSubstantial();
    
    /**
     * Sets the substantial member.
     * @param value The new value
     */
    void setSubstantial(boolean value);
    
    /**
     * Called when master becames non-existing during shrink.
     * Can be defined in descendants to perform cleaning actions.
     */
    void loosingMaster();
    
    /**
     * Called when some slave becames non-existing during shrink.
     * Can be defined in descendants to perform cleaning actions.
     * @param slave The disappearing slave
     */
    void loosingSlave(IveObject slave);
}

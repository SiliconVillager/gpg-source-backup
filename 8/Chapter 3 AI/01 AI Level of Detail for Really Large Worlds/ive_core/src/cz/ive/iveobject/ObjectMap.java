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

import cz.ive.process.*;
import java.util.HashMap;
import java.util.ArrayList;
import cz.ive.location.WayPoint;

/**
 * Singleton class to create and manage all real objects in IVE World.
 * @author Jirka
 */
public class ObjectMap implements java.io.Serializable {
    
    private static ObjectMap objectMap;
    
    private HashMap<String,IveObject> objects;
    
    /** 
     * Creates a new instance of ObjectMap 
     */
    protected ObjectMap() {
	objects = new HashMap<String, IveObject>();
    }
    
    /** 
     * Returns the instance of the ObjectMap singleton 
     */
    static public ObjectMap instance() {
        if (objectMap == null) {
            objectMap = new ObjectMap();
        }        
        return objectMap;
    }
    
    /**
     * Changes internal refence to ObjectMap singleton.
     * Used with serialization - after loading.
     *
     * @param map reference to new map
     */
    static public void setInstance(ObjectMap map) {
	objectMap = map;
    }
    
    /** 
     * Empty whole the ObjectMap before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        objectMap = new ObjectMap();
    }
    
    /**
     * Register new IveObject, controls whether its id is unique 
     * and object isn't phantom.
     *
     * @param o IveObject to be register by its Id
     * @return true on success, false when registration failed.
     */
    public boolean register(IveObject o) {
	
	String id = o.getId();
	
	if (o.isPhantom()) {
	    return false;
	}
	if (objects.containsKey(id)) {
	    return false;
	}
	objects.put(id, o);
	return true;
    }
    
    
    /**
     * Unregister IveObject from map.
     *
     * @param id Id of the IveObject to be unregistered.
     */
    public void unregister(String id) {
	objects.remove(id);
    }
    
    /**
     * Remove mapping of IveObject but preserve it's ID.
     * @param id Id of the IveObject to be unregistered.
     * @return true iff the object was unregistered and id preserved, 
     * false iff the mapping was not found.
     */
    public boolean hide(String id) {
	if (!objects.containsKey(id)) {
	    return false;
	}
	objects.put(id, null);
        return true;
    }
    
    /**
     * Reset mapping of IveObject with earlier preserved ID.
     * @param object Object with preserved id to be registered.
     *
     * @return true iff the object was registered, 
     * false iff the object's id was not preserved.
     */
    public boolean unhide(IveObject object) {
        String id;
        id = object.getId();
        
	if (!objects.containsKey(id)) {
	    return false;
	}
        if (objects.get(id) != null) {
            return false;
        }
	objects.put(id, object);
        return true;
    }

    /**
     * Tests if the given id can be registered.
     * @param id Id to test
     * @return true iff id is neither registered nor preserved
     */
    public boolean canRegister(String id) {
        return !objects.containsKey(id);
    }
    
    /**
     * @return object according to given id or null when nothing is found
     */
    public IveObject getObject(String id) {
	return objects.get(id);
    }
    
    /**
     * Transform substitution of phantoms to substitution of valid IveObjects.
     * Tranformation of some sources can fails then slot stay empty.
     * Due to this, {@link Substitution#checkSubstitution(Substitution)} method 
     * should be called after translation.
     *
     * @return new object of translated substitution      
     */
    public Substitution translate(Substitution phantoms) {
	
	Substitution newSub = new SubstitutionImpl();
	for (String i: phantoms.getSlotsKeys()) {
	    ArrayList<Source> set = new ArrayList<Source>();
	    for (Source s: phantoms.getSourceArray(i)) {

                IveObject srcContent=s.getObject();
                if (srcContent==null) {
                    set=null;
                    break;
                }
		IveObject valid = objects.get(srcContent.getId());
		if (valid == null) {
		    set = null;
		    break;
		}		
		set.add(new SourceImpl(valid));		
	    }
	    newSub.addSlot(i, set, phantoms.isMandatory(i),
                    phantoms.isVariable(i), 
		    phantoms.getActorSlotsKeys().contains(i));
	}		
	return newSub;
    } 
}
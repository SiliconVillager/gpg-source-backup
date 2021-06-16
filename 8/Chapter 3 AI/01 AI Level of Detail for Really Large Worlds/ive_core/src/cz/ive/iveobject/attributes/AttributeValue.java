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
 
package cz.ive.iveobject.attributes;

import cz.ive.messaging.*;
import cz.ive.valueholders.*;
import java.util.HashSet;
import cz.ive.iveobject.*;
import org.w3c.dom.NodeList;


/**
 * Common ancestor of all attribute values holded by IveObject.
 *
 * @author Jirka
 */
public abstract class AttributeValue
	extends SyncHook implements AttributeHook, ValueHolder, Cloneable {
    
    /** parent IveObject where attribut is stored */
    protected IveObjectImpl parent;
    
    /** name by which object is stored in IveObject */
    protected String name;
    
    /** mask of applicable sensors - each bit determine one sensor */
    protected long sensorMask;
        
    protected NodeList userInfo;
    
    /**
     * Default constructor, where sensor mask is 
     * implicitly set to be attribute caught up by EYE sensor.
     */
    protected AttributeValue() {
	sensorMask = 1;
	userInfo = null;
    }
    
    /**
     * Makes a deep copy of the object with no listeners.
     */
    public Object clone() {
	
	AttributeValue o = null;
	try {
	    o = (AttributeValue) super.clone();
	} catch (CloneNotSupportedException e) {}
	
	o.listeners = new HashSet<Listener>();
	
	return o;
    }
    
    /**
     * Call clone() to object and assign reference to parent IveObject.
     *
     * @param p parent IveObject where attribut is stored
     * @return cloned attribute
     */
    public Object clone(IveObjectImpl p) {
	
	AttributeValue o = (AttributeValue) this.clone();
	o.parent = p;
	o.name = name;
	
	return o;
    }        
    
    /**
     * Called by IveObject when attribute is added to it.
     *
     * @param p parent IveObject where attribut is stored.
     */
    public void setParent(IveObjectImpl p) {	
	parent = p;
    }  
    
    /**
     * @return parent IveObject where attribut is stored.
     */
    public IveObjectImpl getParent() {	
	return parent;
    }

    /**
     * Called by IveObject when attribute is added to it.
     *
     * @param n name by which object is stored in IveObject 
     */
    public void setName(String n) {	
	name = n;
    }  
    
    /**
     * @return name by which object is stored in IveObject 
     */
    public String getName() {	
	return name;
    }    
    
    public long getSensorMask() {
	return sensorMask;
    }
    
    /**
     * @param mask sensor mask - each bit determine one sensor. 
     */
    public void setSensorMask(long mask) {
	sensorMask = mask;
    }        
    
    public NodeList getUserInfo() {
	return userInfo;
    }
   
    protected void notifyFromAttr() {
	if (parent.isNotificable()) {
	    notifyListeners();
	}
	parent.notification(this);
    }
    
    /**
     * To make notifyListener visible outside the object
     */
    public void notification() {
	notifyListeners();
    }
    
    public abstract boolean equals(Object other);
}

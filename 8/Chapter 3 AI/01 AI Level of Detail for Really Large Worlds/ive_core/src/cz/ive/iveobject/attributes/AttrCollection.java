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

import cz.ive.valueholders.*;
import cz.ive.iveobject.*;
import cz.ive.messaging.Listener;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.NodeList;


/**
 * Atributte which holds set of IveObject encapsulated in AttrObject.
 *
 * @author Jirka
 */
public class AttrCollection extends AttributeValue {
    
    private Set<IveObject> value;
    
    /**
     * Creates new instance with empty set.
     */
    public AttrCollection(){	
	value = new HashSet<IveObject>();
    }
    
    /**
     * @param val set of attribute objects to be holded by this attribute
     */
    public AttrCollection(Set<IveObject> val) {	
	value = val;	
    }

    /**
     * @param val set of attribute objects to be holded by this attribute
     * @param ui user info in xml 
     */
    public AttrCollection(Set<IveObject> val, NodeList ui) {	
	this(val);
	userInfo = ui;
    }
        
    /**
     * Make a deep copy of the object - clone object container and copy
     * references of IveObject to them.
     */
    public Object clone() {	
	AttrCollection o = (AttrCollection) super.clone();	
	o.value = new HashSet<IveObject>();
	o.value.addAll(value);	
	return o;
    }
    
    /**
     * @param val new set of attribute objects to be holded by this attribute
     */
    public void setValue(Set<IveObject> val) {
	
	value = val;
	notifyFromAttr();
    }
    
    /**
     * @param val IveObject to be added to the collection     
     */
    public void add(IveObject val) {
	if (value.add(val)) {
	    notifyFromAttr();
	}
    }
    
    /**
     * @param val IveObject to be removed to the collection     
     */
    public void remove(IveObject val) {
        if (value.remove(val)) {
	    notifyFromAttr();	  
	}
    }
        
    public Set<IveObject> getValue() {
	return value;
    }
    
    public ValueType getType(){	
	return ValueType.COLLECTION;
    }
    
    /**
     * Change value of object.
     *
     * @param newValue new value is get from given collection
     */
    public void changeValue(ValueHolder newValue) {	
	setValue(((AttrCollection) newValue).getValue());
	userInfo = ((AttrCollection) newValue).getUserInfo();
	notifyFromAttr();
    }
    
    public boolean equals(Object other) {
	return (other instanceof AttrCollection) ?
	    value.equals(((AttrCollection) other).value) : false;
    }
    
    /**
     * @return string representation of attribute value - concatenate names 
     * of contained IveObjects
     */
    public String toString() {
	
	String ret = "[";
	for (Iterator<IveObject> i = value.iterator(); i.hasNext(); ) {
	    ret += i.next().toString();
	    if (i.hasNext()) {
		ret += ", ";
	    }
	}
	ret += "]";
	return ret;
    }
}
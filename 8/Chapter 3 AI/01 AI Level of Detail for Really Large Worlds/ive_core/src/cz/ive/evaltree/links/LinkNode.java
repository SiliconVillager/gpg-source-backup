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
 
package cz.ive.evaltree.links;


import cz.ive.evaltree.dependentnodes.SourceDependentNode;
import cz.ive.iveobject.IveObject;
import cz.ive.process.Substitution;
import cz.ive.process.Source;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.iveobject.attributes.AttributeValue;
import cz.ive.logs.Log;
import cz.ive.messaging.Hook;
import cz.ive.messaging.Listener;
import cz.ive.sensors.Sensor;
import java.util.List;


/**
 * Reference to the attribute of some source
 * @author thorm
 */
public  abstract class LinkNode<TYPE extends ValueHolderImpl> 
        extends SourceDependentNode<TYPE> implements Listener {
    
    /**
     * attribute name
     */
    private String attribute;

    /**
     * attribute used to get value of this node
     */
    protected AttributeValue attr;

    /**
     * object whose attribute is accessed
     */
    private IveObject object;
    
    /**
     * Creates a new instance of LinkNode
     * @param role      role of the source in the substitution
     * @param attribute name of attribute 
     */
    public LinkNode(String role, String attribute) {
        super(role);
        this.attribute = attribute;
    }
    
    /**
     * Binds link to the value of particular object attribute and reads the value.
     * If something gets wrong the node value is undefined
     * 
     * @param s substitution where the source is taken from
     * @param _sensors set of sensors - not needed
     */
    public void instantiate(Substitution s, List<Sensor> _sensors) {
        attr = null;
        super.instantiate(s, _sensors);
        if (source == null) {
            return;
        }
        
        object = source.getObject();
        if (object == null) {            
            return;
        }
        object.registerListener(this);
        attr = object.getAttribute(attribute);
        if (attr == null) {
            Log.warning(
                    "LinkNode :Object " + object.getId() + " in role " + role
                    + " does not contain attribute " + attribute);
        } else {
            attr.registerListener(this);        
        }
        initialUpdateValue();
    }

    public void uninstantiate() {
        if (attr != null) {
            attr.unregisterListener(this);
            attr = null;
        }
        if (object != null) {
            object.unregisterListener(this);
            object = null;
        }
        if (source != null) {
            source.unregisterListener(this);
            source = null;
        }
        
    }
    
    public void DFSEval(Substitution s, List<Sensor> sensors) {
        source = s.getSource(role);
        if (source != null) {
            object = source.getObject();

            if (object != null) {
                attr = object.getAttribute(attribute);
            }
            
        }
        initialUpdateValue();
        source = null;
        object = null;
        attr = null;
    }
    
    public int contentHashCode() {
        return super.contentHashCode() + attribute.hashCode();
    }
    
    public boolean contentEquals(Object obj) {
        return super.contentEquals(obj)
                && (attribute.equals(((LinkNode) obj).attribute));
    }
    
    protected void updateInstantiation(Hook who) {
        if (who == source) {
            if (object != null) {
                object.unregisterListener(this);
            }
            object = source.getObject();
            
            if (attr != null) {
                attr.unregisterListener(this);                    
            }
            attr = null;
            
            if (object == null) {
                return;
            }
            object.registerListener(this);
            who = object;
        }
        if (who == object) {
            if (attr != null) {
                attr.unregisterListener(this);                
            }            
            attr = object.getAttribute(attribute);
            if (attr == null) {
                return;
            }
            attr.registerListener(this);
        }
        
    }
    
    /**
     * Copy the attribute value to this node.
     * If the role is empty or assigned object does not have the specified
     * attribute the value is undefined.
     */
    protected void updateValue() {
        if (attr != null && object!= null && checkType()) {
            value.changeValue(attr);
            value.isDefined = true;
        } else {
            value.isDefined = false;
        }
    }
    
    /**
     * Check whether type of attribute this node refers to is the same as type
     * of this node
     * @return true it the types matches
     */
    abstract boolean checkType();
    
    /**
     * Return textual descritpion of parameter of this node.
     * 
     * @return  \"role.attribute\" 
     */
    public String getInfoArguments() {
        return role + "." + attribute;
    }
}

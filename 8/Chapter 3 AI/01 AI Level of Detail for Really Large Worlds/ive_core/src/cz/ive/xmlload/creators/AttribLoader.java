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
 
package cz.ive.xmlload.creators;


import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.util.Pair;
import cz.ive.xmlload.XMLDOMLoader;
import java.util.*;
import org.w3c.dom.*;


/**
 * 
 * @author thorm
 */
public class AttribLoader extends DOMSaxator implements java.io.Serializable {
    /**
     * Actual attribute path
     */
    Stack<String> path;
    
    /**
     * list of relative ObjectAtrributes - such that refers to the ovject slaves
     */
    private List<Pair<String, String>> relativeObjectAttributes;
    
    /**
     * List of all attributes except for relative ObjectAttributes
     */
    private List<Pair<String, AttributeValue>> absoluteAttributes;
    
    /**
     * Just remove the top of the path stack
     */
    void elementEnd(Element e) {
        String name = e.getNodeName();

        if (name.equals("attributes")) {
            return;
        }
        path.pop();
        if (name.equals("AttrPath")) {}
        
    }
    
    /**
     * Create the new AttribLoader instance
     */
    public AttribLoader() {
        relativeObjectAttributes = new LinkedList<Pair<String, String>>();
        absoluteAttributes = new LinkedList<Pair<String, AttributeValue>>();
        path = new Stack<String>();
        
    }
    
    /**
     * Create the dot separated string from the content of the stack
     * @return the path content
     */
    String pathToString() {
        String s = null;
        boolean count = false;

        for (String i : path) {
            if (!count) {
                count = true;
                s = i;
                continue;
            }
            s = s + IveId.SEP + i;
        }
        return s;
        
    }
    
    /**
     * Store the attribute value a to the list of attributes using current path
     * @param a value to be stored
     */
    void newAbsoluteAttrib(AttributeValue a) {
        absoluteAttributes.add(
                new Pair<String, AttributeValue>(pathToString(), a));
    }
    
    /**
     * Called for each attribute or attribute path fragment.
     * In case ot attribute it parse the value and create corresponding 
     * AttributeValue descendant.
     * @param e processed element
     * @return true if children should be processed to
     */
    boolean elementBegin(Element e) {
        
        String name = e.getNodeName();

        if (name.equals("attributes")) {
            return true;
        }
        
        path.push(e.getAttribute("name"));
        
        if (name.equals("AttrPath")) {
            return true;
        }
        
        if (name.equals("FuzzyAttribute")) {
            String val = e.getAttribute("value");

            if (val.equals("true")) {
                newAbsoluteAttrib(
                        new AttrFuzzy(AttrFuzzy.True, e.getChildNodes()));
            } else if (val.equals("false")) {
                newAbsoluteAttrib(
                        new AttrFuzzy(AttrFuzzy.False, e.getChildNodes()));
            } else {
                newAbsoluteAttrib(
                        new AttrFuzzy(Short.valueOf(val), e.getChildNodes()));
            }
        } else if (name.equals("IntAttribute")) {
            newAbsoluteAttrib(
                    new AttrInteger(Integer.valueOf(e.getAttribute("value")),
                    e.getChildNodes()));
            
        } else if (name.equals("ObjectAttribute")) {
            String value = e.getAttribute("value");

            if (value.equals("")) {
                newAbsoluteAttrib(new AttrObject(null, e.getChildNodes()));
            } else {
                if (XMLDOMLoader.readBoolean(e, "relative")) {
                    String path = pathToString();

                    relativeObjectAttributes.add(
                            new Pair<String, String>(path, value));
                } else {
                    newAbsoluteAttrib(
                            new AttrObject(new IveObjectImpl(value),
                            e.getChildNodes()));
                }
            }
            
        } else if (name.equals("CollectionAttribute")) {
            Set<IveObject> val = new HashSet<IveObject>();
            NodeList nl = e.getElementsByTagName("ObjectAttribute");

            for (int i = 0; i < nl.getLength(); i++) {
                val.add(
                        new IveObjectImpl(
                                ((Element) nl.item(i)).getAttribute("value")));
            }
            newAbsoluteAttrib(new AttrCollection(val, e.getChildNodes()));
        }
        return false;
    }
    
    /**
     * Needed to implement DOMSaxator interface
     * @return true
     */
    boolean startDocument() throws Exception {
        return true;
    }
    
    /**
     * Needed to implement DOMSaxator interface
     */    
    void endDocument() throws Exception {}
    

    /**
     * Create the remembered attributes structure on the given object.
     * @param obj IveObject to add the attributes
     */
    public void fillTheObject(IveObject obj) {
        String objectId = obj.getId();
        
        for (Pair<String, AttributeValue> absAttrib : absoluteAttributes) {
            obj.addAttribute(absAttrib.first(),
                    (AttributeValue) absAttrib.second().clone());
            
        }
        
        for (Pair<String, String> relativeObjAttrib : relativeObjectAttributes) {
            obj.addAttribute(relativeObjAttrib.first(),
                    new AttrObject(
                    new IveObjectImpl(
                            objectId + IveId.SEP + relativeObjAttrib.second())));
        }
    }

}


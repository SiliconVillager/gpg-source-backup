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
 
package cz.ive.xmlload;


import cz.ive.IveApplication;
import cz.ive.exception.ObjectRegistrationFailedException;
import cz.ive.gui.DelegatedGraphicInfo;
import cz.ive.iveobject.IveId;
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.IveObjectImpl;
import cz.ive.iveobject.Link;
import cz.ive.iveobject.ObjectClass;
import cz.ive.iveobject.ObjectClassTree;
import cz.ive.iveobject.ObjectMap;
import cz.ive.location.CommonArea.GraphicTemplateStrings;
import cz.ive.location.KindMap;
import java.lang.reflect.InvocationTargetException;
import org.w3c.dom.Element;
import cz.ive.template.*;

import static cz.ive.xmlload.XMLDOMLoader.*;
import cz.ive.location.Kind;
import cz.ive.lod.Holdback;
import cz.ive.logs.Log;
import cz.ive.xmlload.creators.AttribLoader;
import java.lang.reflect.Constructor;

import java.util.*;
import org.w3c.dom.NodeList;


/**
 * Template used to create new objects living in the world
 * @author thorm
 */
public class ObjectTemplate implements Template {
    /**
     *  Constructor obtained by java reflection that is used to create
     *  instance of the java class specified in xml.
     *  This is transient because of serialization.
     *  After load of the saved simulation it is assigned again using className
     *  member
     */
    transient Constructor ctor;
    
    /**
     *  Required signature of the constructor
     */
    Class[] ctorSignature = { String.class, ObjectClass.class};
    
    /**
     *  Name of the another template that is used as base for this one
     */
    ObjectTemplate derivedFrom;
    
    /**
     * Name of this template
     */
    String name;
    
    /**
     * Java class that will be used to represent this object.
     */
    String className;
    
    /**
     * Name of another, already loaded, template what new template is derived from
     */
    String templateName;
    
    /**
     * Kind of the object - this is list of the floor types the object can
     * step on
     */
    Kind kind;
    
    /**
     * Information used by the GUI to display the object
     */
    GraphicTemplateStrings graphicInfo;
    
    /**
     * List of the links describing the processes the object can take part in.
     */
    List<Link> links;
    
    /**
     * used to remember the attributes description and to create attribute
     * tree for each new instance
     */
    AttribLoader attribLoader;
    
    /**
     * Property of the IveObject used during placement
     */
    ObjectClass objectClass;
    
    /**
     * List of instantiation description of sensors that belongs to the each
     * instance of this template
     */
    List<SensorInstanceInfo> sensors;
    
    /**
     * List of instantiation description of all child objects that belongs
     * to the each instance of this template
     */
    List<ObjectInstanceInfo> objects;
    
    /**
     * Instance holdback
     */
    Holdback holdback;
    
    /**
     * Two substantial objects can not share one waypoint on the most detailed
     * level
     */
    boolean substantial;
    
    /**
     * Create new instance of the object described by this template
     * @param objectId structured identifier of newly created IveObject
     * @return new IveObject or null if fails
     */
    public IveObject instantiate(String objectId) {
        if (!ObjectMap.instance().canRegister(objectId)) {
            Log.severe("IveId " + name + "already exist or is reserved");
            return null;
        }
        IveObjectImpl ret = createNewInstance(objectId, objectClass);
        
        try {
            ret.validate();
        } catch (ObjectRegistrationFailedException e) {
            Log.severe("Inserting object " + name + "into ObjectMap failed");
            return null;
        }
        ret.setHoldback(
                new Holdback(holdback.getExistence(), holdback.getView()));
        ret.setKind(kind);
        ret.setSubstantial(substantial);
        new DelegatedGraphicInfo(ret, graphicInfo.className, graphicInfo.name);
        
        fillNewInstance(ret);
        return ret;
    }
    
    /**
     *
     * @param e <CODE>ObjectTemplate</CODE> element
     */
    public String load(Element e) {
        name = e.getAttribute("name");
        className = e.getAttribute("className");
        substantial = XMLDOMLoader.readBoolean(e, "substantial");
        setParentTemplate(e);
        setConstructor(className);
        
        if (ctor == null) {
            return null;
        }
        
        holdback = loadHoldback(e);
        objectClass = loadIveObjectClass(e);
        
        objects = loadObjectInstances(getOneChildElement(e, "objects"));
        sensors = loadSensorInstances(getOneChildElement(e, "sensors"));
        
        links = loadLinks(getOneChildElement(e, "links"));
        
        graphicInfo = loadGraphicTemplateStrings(
                getOneChildElement(e, "graphicInfo"));
        
        kind = loadKind(getOneChildElement(e, "Kind"));
        
        // load attributes
        attribLoader = loadAttributes(getOneChildElement(e, "attributes"));
        
        return name;
    }
    
    
    /**
     * Add all object properities that are influenced by inheritance.
     * Currently attributes, links sensors and slaves
     *
     * @param o object to add properities
     * @return true on success
     */
    boolean fillNewInstance(IveObject o) {
        // set all parent data
        if (derivedFrom != null) {
            derivedFrom.fillNewInstance(o);
        }
        
        // append to parent values
        if (links != null) {
            for (Link l:links) {
                o.addLink(l);
            }
        }
        
        instantiateSubObjects(o);
        
        if (attribLoader != null) {
            attribLoader.fillTheObject(o);
        }
        
        return true;
    }
    
    /**
     * Create the instances of subobjects and add them to the object
     *
     * @param obj the new object the instantiated subobjects belong to
     */
    void instantiateSubObjects(IveObject obj) {
        String objectId = obj.getId();
        
        if (objects != null) {
            for (ObjectInstanceInfo info:objects) {
                obj.addObject(info.instantiate(objectId + IveId.SEP + info.name));
            }
        }
        
        if (sensors != null) {
            for (SensorInstanceInfo info:sensors) {
                IveObject child = info.instantiate(
                        objectId + IveId.SEP + info.name);
                
                obj.addObject(child, "sensors." + info.name);
            }
        }
        
    }
    
    /**
     * Fills the kind member value by information what kind is this location of.
     *
     * @param e <CODE>Kind</CODE> element
     */
    Kind loadKind(Element e) {
        if (e == null) {
            return null;
        }
        LinkedList<Integer> tmpkind = new LinkedList<Integer>();
        
        for (Element kindItemElement :getChildElements(e, "KindItem")) {
            String kindItem = kindItemElement.getAttribute("kindValue");
            Integer ki = KindMap.instance().getKind(kindItem);
            
            tmpkind.add(ki);
        }
        int[] ret = new int[tmpkind.size()];
        
        for (int i = 0; i < ret.length; i++) {
            ret[i] = tmpkind.remove(0);
        }
        return new Kind(ret);
    }
    
    /**
     * Load information that need GUI to display the template instance
     * This information consist of two strings. The first refers to the
     * GraphicsTemplate that contains the image data and the second is the name
     * of java class used to display it.
     *
     *
     * @param e ObjectTemplate element
     * @return pair of strings
     */
    GraphicTemplateStrings loadGraphicTemplateStrings(Element e) {
        if (e == null) {
            return null;
        }
        String name = e.getAttribute("template");
        String graphClassName = e.getAttribute("className");
        
        return new GraphicTemplateStrings(name, graphClassName);
    }
    
    /**
     * Set the ctor member variable using given java class name.
     * This is used during XML load and after load of serialized simulation to
     * renew the transient ctor.
     *
     * @param clsName java class name
     */
    void setConstructor(String clsName) {
        
        if (derivedFrom != null) {
            ctor = derivedFrom.ctor;
        }
        if (clsName.equals("IveObjectImpl") && ctor != null) {
            return;
        }
        try {
            ctor = IveApplication.instance().loadIveClass(clsName).getConstructor(
                    ctorSignature);
            
        } catch (ClassNotFoundException ex) {
            Log.severe("Class not found exception " + clsName);
        } catch (NoSuchMethodException ex) {
            Log.severe(
                    "Class " + clsName + " does not have suitable constructor");
        }
        
        if (ctor == null) {
            Log.severe(
                    "No java class specified to keep data of " + name
                    + " template");
            
        }
        
    }
    
    /**
     *  Set the derivedFrom member value that refers to the base template.
     *  The new template inherits some properities of the base one - links,
     *  attributes, sensors,
     *
     *  @param e ObjectTemplate element
     *  @return true on success
     */
    boolean setParentTemplate(Element e) {
        
        String templateName = e.getAttribute("template");
        
        if (templateName != null && !templateName.equals("")) {
            derivedFrom = (ObjectTemplate) TemplateMap.instance().getTemplate(
                    templateName);
            if (derivedFrom == null) {
                Log.severe(
                        "Template " + templateName
                        + " used to derive new template " + name + "not found");
                return false;
            }
            
        }
        
        return true;
    }
    
    /**
     * Load list of links.
     * The link states the ablility of the template instance to take part of
     * some process in particular role.
     *
     * @param e element whose children are links
     * @return <c>LinkedList</c> of <c>Links</c>
     */
    List<Link> loadLinks(Element e) {
        if (e == null) {
            return null;
        }
        List<Link> links = new LinkedList<Link>();
        
        for (Element linkElement :getChildElements(e, "Link")) {
            NodeList ui = linkElement.getChildNodes();
            
            if (ui.getLength() == 0) {
                ui = null;
            }
            links.add(
                    new Link(linkElement.getAttribute("goal"),
                    linkElement.getAttribute("process"),
                    linkElement.getAttribute("slot"), ui));
        }
        return links;
    }
    
    /**
     * Load information about object slaves that are instantiated with him.
     * ObjectInstanceInfo contains at least name of template used to create slave
     * and its name.
     * @param e Element whose child are <CODE>Object</CODE> elements
     */
    List<ObjectInstanceInfo> loadObjectInstances(Element e) {
        String name;
        String template;
        List<ObjectInstanceInfo> ret = new LinkedList<ObjectInstanceInfo>();
        
        if (e == null) {
            return null;
        }
        for (Element objElement :getChildElements(e, "Object")) {
            ObjectInstanceInfo o = new ObjectInstanceInfo();
            
            if (o.load(objElement) == null) {
                continue;
            }
            ret.add(o);
        }
        return ret;
    }
    
    /**
     * Load information about sensors that are instantiated with him.
     * SensorInstanceInfo contains at least name of template used to create sensor,
     * its name and LOD range within the sensor percieves.
     *
     * @param e Element whose child are <CODE>Sensor</CODE> elements
     */
    
    List<SensorInstanceInfo> loadSensorInstances(Element e) {
        String name;
        String template;
        List<SensorInstanceInfo> ret = new LinkedList<SensorInstanceInfo>();
        
        if (e == null) {
            return null;
        }
        for (Element objElement :getChildElements(e, "Sensor")) {
            SensorInstanceInfo o = new SensorInstanceInfo();
            
            if (o.load(objElement) == null) {
                continue;
            }
            ret.add(o);
        }
        return ret;
    }
    
    /**
     * Load holdback of the template instances
     * @param e <CODE>ObjectTemplate</CODE> element
     */
    Holdback loadHoldback(Element e) {
        String elevelStr = e.getAttribute("existenceLevel");
        String vlevelStr = e.getAttribute("viewLevel");
        
        if (elevelStr != null && vlevelStr != null && !elevelStr.equals("")
        && !vlevelStr.equals("")) {
            int elevel = Integer.parseInt(elevelStr);
            int vlevel = Integer.parseInt(vlevelStr);
            
            return new Holdback(elevel, vlevel);
        }
        return null;
    }
    
    /**
     * Load the attributes
     * @param e <CODE>attributes</CODE> element
     * @return AttribLoader that remembers the tree structure and is able to
     *         create it on the given object.
     */
    AttribLoader loadAttributes(Element e) {
        if (e != null) {
            AttribLoader attribLoader = new AttribLoader();
            
            try {
                attribLoader.traverseDOM(e);
                return attribLoader;
            } catch (Exception exc) {
                IveApplication.printStackTrace(exc);
            }
        }
        return null;
    }
    
    /**
     * Load ObjectClass that is associated with the template instances.
     * @param e <CODE>ObjectTemplate</CODE> element
     * @return ObjectClass instance
     */
    ObjectClass loadIveObjectClass(Element e) {
        String objectClassStr = e.getAttribute("objectClass");
        
        if (objectClassStr != null) {
            return ObjectClassTree.instance().getObjectClass(objectClassStr);
        }
        return null;
    }
    
    /**
     * Use the ctor member variable to create new instance of IveObjectImpl
     * derivad class.
     * If the ctor is empty the method tryes to renew it.
     *
     * @param ctorParams arguments passed to the constructor
     * @return new instance created by ctor member variable
     */
    IveObjectImpl createNewInstance(Object... ctorParams) {
        IveObjectImpl ret;
        
        try {
            if (ctor == null) {
                setConstructor(className);
            }
            if (ctor == null) {
                return null;
            }
            ret = (IveObjectImpl) ctor.newInstance(ctorParams);
            return ret;
        } catch (InstantiationException e) {
            Log.severe(
                    "Instantiation of java class " + className
                    + " used to keep data of " + name
                    + " template instances failed");
            return null;
        } catch (IllegalAccessException e) {
            Log.severe(
                    "Instantiation of java class " + className
                    + " used to keep data of " + name
                    + " template instances failed - wrong access rights");
            return null;
        } catch (InvocationTargetException e) {
            Log.severe(
                    "Instantiation of java class " + className
                    + " used to keep data of " + name
                    + " template instances failed - wrong access rights");
            return null;
        } catch (ClassCastException e) {
            Log.severe(
                    "Java class " + className + " used to keep data of " + name
                    + " is not descendant of IveObjectImpl");
            return null;
        }
    }
}

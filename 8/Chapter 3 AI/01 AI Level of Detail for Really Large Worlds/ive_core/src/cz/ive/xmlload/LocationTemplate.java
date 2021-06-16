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
import cz.ive.location.Area;
import org.w3c.dom.Element;

import static cz.ive.xmlload.XMLDOMLoader.*;

import cz.ive.location.CommonArea.InfluenceInfo;
import cz.ive.location.CommonArea.NeighbourInfo;
import cz.ive.location.WayPoint;
import cz.ive.location.WayPointImpl;
import cz.ive.location.Kind;
import cz.ive.util.Pair;

import java.util.*;


/**
 * Common parent for all templates used to instantiate locations
 *
 * @author thorm
 */
abstract public class LocationTemplate extends ObjectTemplate {
    
    /**
     * Lod of the represented location
     */
    int lod = 1;
    
    /**
     * List of inherent objects.
     * List item says how many objects of particular instances shoud be
     * created within this location
     */
    List<Pair<ObjectInstanceInfo, Integer>> inherentObjects;
    
    /**
     * Create new instance of location described by this template
     * @param objectId structured identifier of iveObject
     * @param parent parent of new location
     * @param realPosition absolute position
     * @return new location or null
     */
    abstract public Area instantiate(
            String objectId,
            WayPoint parent,
            float[] realPosition);
    /**
     * @param e <CODE>GridLocationTemplate</CODE> or
     *          <CODE>GraphLocationTemplate</CODE>element
     */
    public String load(Element e) {
        super.load(e);
        objects = loadObjectInstances(getOneChildElement(e, "objects"));
        
        inherentObjects = loadInherentObjectInstances(
                getOneChildElement(e, "inherentObjects"));
        
        String lodStr = e.getAttribute("lod");
        
        try {
            lod = Integer.parseInt(lodStr);
        } catch (NumberFormatException ex) {
            // This should be filtered out by xsd definition
            IveApplication.printStackTrace(ex);
            return null;
        }
        return name;
    }
    
    /**
     * Creates a phantom of a location
     * @param objectId id of the location
     * @param realPosition coordinates of the location
     * @param kind kind of the loctation
     * @return the location's phantom
     */
    public WayPoint instantiatePhantom(String objectId, float[] realPosition,
            Kind kind) {
        
        return new WayPointImpl(objectId, realPosition, kind);
    }
    
    /**
     * Fills objs member function
     */
    LinkedList<Pair<ObjectInstanceInfo, Integer>> loadInherentObjectInstances(Element e) {
        String name;
        String template;
        
        if (e == null) {
            return null;
        }
        LinkedList<Pair<ObjectInstanceInfo, Integer>> ret =
                new LinkedList<Pair<ObjectInstanceInfo, Integer>>();
        
        for (Element objElement :getChildElements(e, "Object")) {
            ObjectInstanceInfo o = new ObjectInstanceInfo();
            
            if (o.load(objElement) == null) {
                continue;
            }
            ret.add(new Pair(o, new Integer(objElement.getAttribute("number"))));
        }
        return ret;
    }
    
    /**
     * Fills the Pair object with values found among attributes of e
     * @param e XML element that should contain <CODE>first</CODE> and
     * <CODE>second</CODE> attributes
     */
    boolean fillPair(Element e, Pair p) {
        p.first = e.getAttribute("first");
        p.second = e.getAttribute("second");
        return true;
    }
    
    /**
     * Fills the InfluenceInfo with values found among attributes of e
     *
     * @param e XML element that should contain <CODE>first</CODE>,
     *          <CODE>second</CODE> and <CODE>influence</CODE> attributes
     *
     */
    
    boolean fillInfluence(Element e, InfluenceInfo p) {
        try {
            p.influence = Integer.parseInt(e.getAttribute("influence"));
        } catch (Exception exc) {
            return false;
        }
        return fillPair(e, p);
    }
    
    /**
     * Fills the NeighbourInfo with values found among attributes of e
     *
     * @param e XML element that should contain <CODE>first</CODE>,
     *          <CODE>second</CODE>, <CODE>dx</CODE> <CODE>dy</CODE>
     *          attributes and <CODE>Kind</CODE> subelements
     */
    
    boolean fillNeighbourInfo(Element e, NeighbourInfo p) {
        try {
            p.dx = Float.parseFloat(e.getAttribute("dx"));
            p.dy = Float.parseFloat(e.getAttribute("dy"));
            List<Element> kinds = XMLDOMLoader.getChildElements(e, "Kind");
            
            if (kinds.size() == 2) {
                p.kinds = new Pair<Kind, Kind>(loadKind(kinds.get(0)),
                        loadKind(kinds.get(1)));
            }
        } catch (Exception exc) {
            return false;
        }
        
        return fillPair(e, p);
    }
    
}

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
import cz.ive.gui.DelegatedGraphicInfo;
import cz.ive.iveobject.IveId;
import cz.ive.iveobject.Link;
import cz.ive.location.Area;
import cz.ive.location.SimpleArea;
import cz.ive.iveobject.IveObject;
import org.w3c.dom.Element;

import static cz.ive.xmlload.XMLDOMLoader.*;

import cz.ive.location.CommonArea.InfluenceInfo;
import cz.ive.location.WayPoint;
import java.util.LinkedList;
import java.util.List;
import cz.ive.location.CommonArea.NeighbourInfo;
import cz.ive.location.Kind;
import cz.ive.logs.Log;


/**
 * This class is able to create number of identical GraphLocations to be use 
 * in various parts of IveWorld.
 *
 * @author thorm
 */
public class GraphLocationTemplate extends LocationTemplate {
    
    /**
     * Instances of sublocations (name and template name)
     */
    List<LocationInstanceInfo> locations;

    /**
     * Description of connections between sublocations 
     * ( even not-direct sublocations)
     */
    List<NeighbourInfo> neighbours;

    /**
     * Description of influences between sublocations 
     * ( even not-direct sublocations)
     */
    List<InfluenceInfo> influences;
    
    /**
     *	Keeps all preceeding member variables in one pack
     */
    SimpleArea.ExpansionInfo info;
    
    GraphLocationTemplate() {
        ctorSignature = new Class[] {
            String.class, WayPoint.class, float[].class, Kind.class,
            SimpleArea.ExpansionInfo.class};
    }
    
    
    /**
     * @param e <CODE>GraphLocationTemplate</CODE> element
     */

    public String load(Element e) {
        
        if (
                super.load(e) != null
                && loadLocationInstances(getOneChildElement(e, "subLocations"))
                && loadNeighbours(getOneChildElement(e, "neighbours"))
                && loadInfluences(getOneChildElement(e, "influences"))
                ) {
            info = new SimpleArea.ExpansionInfo(inherentObjects, locations,
                    neighbours, influences);
            return name;
        } else {
            return null;
        }
    }
    
    /**
     * Create new instance of graph location described by this template
     * @param objectId structured identifier of iveObject
     * @param parent parent of new location
     * @param realPosition absolute position
     * @return new location or null
     */
        
    public Area instantiate(String objectId, WayPoint parent, float[] realPosition) {
        Area ret;

        try {
            ret = (Area) createNewInstance(objectId, parent, realPosition, kind,
                    info);
            
        } catch (ClassCastException e) {
            Log.severe(
                    "Java class " + className + " used to keep data of " + name
                    + " is not implement Area interface");
            return null;
        }
        ret.setKind(kind);
        ret.setLod(lod);
        new DelegatedGraphicInfo(ret, graphicInfo.className, graphicInfo.name);
        
        fillNewInstance(ret);
        return ret;
    }
    
    /**
     * Fills locations member variable by sublocations instance info
     * @param e XML element "subLocations" describing sublocation instances
     * @return true on succes, false on fail
     */
    boolean loadLocationInstances(Element e) {
        String instName;

        locations = new LinkedList<LocationInstanceInfo>();
        for (Element locElement :getChildElements(e, "Location")) {
            LocationInstanceInfo o = new LocationInstanceInfo();

            if ((instName = o.load(locElement)) == null) {
                return false;
            }
            locations.add(o);
        }
        return true;
    }
    
    /**
     * Fills neighbours member variable by info about sublocations connections
     * @param e XML element "neighbours"
     * @return true on succes, false on fail
     */
    boolean loadNeighbours(Element e) {
        neighbours = new LinkedList<NeighbourInfo>();
        for (Element jointElement :getChildElements(e, "Joint")) {
            NeighbourInfo p = new NeighbourInfo();

            if (!fillNeighbourInfo(jointElement, p)) {
                return false;
            }
            neighbours.add(p);
        }
        return true;
    }
    
    /**
     * Fills influences member variable
     * @param e XML element "influences"
     * @return true on succes, false on fail
     */
    boolean loadInfluences(Element e) {
        influences = new LinkedList<InfluenceInfo>();
        for (Element influenceElement :getChildElements(e, "Influence")) {
            InfluenceInfo p = new InfluenceInfo();

            if (!fillInfluence(influenceElement, p)) {
                return false;
            }
            influences.add(p);
        }
        return true;
    }
    
}

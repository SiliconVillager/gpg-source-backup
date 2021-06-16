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


import cz.ive.gui.DelegatedGraphicInfo;
import cz.ive.iveobject.ObjectClass;
import cz.ive.iveobject.ObjectClassTree;
import cz.ive.location.Area;
import cz.ive.location.Area2DGrid;
import cz.ive.location.Kind;
import java.util.LinkedList;
import org.w3c.dom.Element;

import static cz.ive.xmlload.XMLDOMLoader.*;
import static cz.ive.location.CommonArea.ObjectExpansionInfo.*;
import static cz.ive.location.Area2DGrid.ExpansionInfo.*;
import cz.ive.location.Area2DGrid.ExpansionInfo.GridException;
import cz.ive.location.CommonArea.GraphicTemplateStrings;
import cz.ive.location.WayPoint;
import cz.ive.logs.Log;
import java.util.HashMap;
import java.util.HashSet;


/**
 *
 * @author thorm
 */
public class GridLocationTemplate extends LocationTemplate {
    
    /**
     * Informations needed during expansion - sublocations, object placement..
     * This item is shared by all instances of this template (!! read !!)
     */
    Area2DGrid.ExpansionInfo info;
    
    GridLocationTemplate() {
        ctorSignature = new Class[] {
            String.class, WayPoint.class, float[].class, Kind.class,
            Area2DGrid.ExpansionInfo.class};
    }
    
    /**
     * @param e <CODE>GridLocationTemplate</CODE> element
     */
    public String load(Element e) {
        super.load(e);
        String width = e.getAttribute("width");
        String height = e.getAttribute("height");
        
        LinkedList<GridException<ObjectClass>> objPlacement = null;
        LinkedList<GridException<Kind>> kindExceptions = null;
        LinkedList<GridException<GraphicTemplateStrings> > graphicTemplates = null;
        HashMap<String, HashSet<String>> jointExceptions = null;
        
        int x1, y1;
        int x2, y2;
        
        Kind defaultWPKind = null;
        GraphicTemplateStrings defaultWayPointsGraphTemplate = null;
        Element subLoc = getOneChildElement(e, "subLocations");

        if (subLoc != null) {
            objPlacement = new LinkedList<GridException<ObjectClass>>();
            kindExceptions = new LinkedList<GridException<Kind>>();
            graphicTemplates = new LinkedList<GridException<GraphicTemplateStrings>>();        
            jointExceptions = new HashMap<String, HashSet<String>>();
            for (Element positionElement :getChildElements(subLoc, "rectangle")) {
                try {
                    x1 = Integer.parseInt(positionElement.getAttribute("x"));
                    y1 = Integer.parseInt(positionElement.getAttribute("y"));
                    x2 = Integer.parseInt(positionElement.getAttribute("x2"));
                    y2 = Integer.parseInt(positionElement.getAttribute("y2"));
                } catch (NumberFormatException ex) {
                    continue;
                }
                
                if (x2 == 0) {
                    x2 = x1;
                }
                
                if (y2 == 0) {
                    y2 = y1;
                }
                
                if (x1 > x2) {
                    int tmp = x1;

                    x1 = x2;
                    x2 = tmp;
                }
                
                if (y1 > y2) {
                    int tmp = y1;

                    y1 = y2;
                    y2 = tmp;
                }
                
                Element kindElement;

                if ((kindElement = getOneChildElement(positionElement, "Kind"))
                        != null) {
                    kindExceptions.addFirst(
                            new GridException<Kind>(x1, y1, x2, y2,
                            loadKind(kindElement)));
                }
                for (Element objPlcElement :getChildElements(positionElement,
                        "ObjectPlacement")) {
                    objPlacement.addFirst(
                            new GridException<ObjectClass>(x1, y1, x2, y2,
                            ObjectClassTree.instance().getObjectClass(
                                    objPlcElement.getAttribute("objectTemplate"))));
                }
                Element graphicTemplateElement;

                if ((graphicTemplateElement = getOneChildElement(positionElement,
                        "graphicInfo"))
                        != null) {
                    graphicTemplates.addFirst(
                            new GridException<GraphicTemplateStrings>(x1, y1, x2,
                            y2,
                            loadGraphicTemplateStrings(graphicTemplateElement)));
                }                
            }
            
            for (Element jointExcElement :getChildElements(subLoc,
                    "jointException")) {
                String wp = jointExcElement.getAttribute("waypoint");

                jointExceptions.put(wp, loadTargets(jointExcElement));
            }
            
            Element defaultwpKindElement;

            if ((defaultwpKindElement = getOneChildElement(subLoc, "defaultKind"))
                    != null) {
                defaultWPKind = loadKind(
                        getOneChildElement(defaultwpKindElement, "Kind"));
            }
            
            defaultWayPointsGraphTemplate = loadGraphicTemplateStrings(
                    getOneChildElement(subLoc, "defaultGraphicInfo"));
            
        }
        
        try {
            info = new Area2DGrid.ExpansionInfo(inherentObjects, kindExceptions,
                    objPlacement, graphicTemplates,
                    defaultWayPointsGraphTemplate, defaultWPKind,
                    jointExceptions, Integer.parseInt(width),
                    Integer.parseInt(height));
        } catch (Exception exc) {
            return null;
        }
        return name;
    }
    
    /**
     * Create new instance of grid location described by this template
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
     * Load the set of targets of joins that starts in the waypoint.
     * This enables user to say that GridArea is not grid at all but there are
     * some joints missing in the grid or there are more than is needed to form 
     * a grid.
     * @param e <CODE>jointException</CODE> element
     */
    HashSet<String> loadTargets(Element e) {
        HashSet<String> ret = new HashSet<String>();

        for (Element targetElement :getChildElements(e, "target")) {
            String wp = targetElement.getAttribute("waypoint");

            ret.add(wp);
        }
        return ret;
    }
}

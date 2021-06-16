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


import cz.ive.genius.AreaGenius;
import cz.ive.iveobject.IveId;
import cz.ive.iveobject.ObjectClass;
import cz.ive.iveobject.ObjectClassTree;
import cz.ive.location.Area;
import cz.ive.location.WayPoint;
import cz.ive.logs.Log;
import cz.ive.template.TemplateMap;
import java.util.LinkedList;
import org.w3c.dom.Element;
import static cz.ive.xmlload.XMLDOMLoader.*;



/**
 * Contains information needed to instantiate Area.
 * The information consists of the all information needed to instantiate
 * object, relative position, description of objectPlacement and list of area
 * genies that lives in the location
 * 
 * @author thorm
 */
public class LocationInstanceInfo extends ObjectInstanceInfo {
    
    /**
     * Position relative to the parent
     */
    public float[] position;
    
    /**
     * List of ObjectClasses.
     * Only IveObjects that are members of one of ObjectClasses associated with
     * the location can be placed on the location if the location parent expands.
     */
    public LinkedList<ObjectClass> objectPlacement;
    
    /**
     * List of descriptions of AreaGenius instances.
     * The AreaGenies are created with the location.
     */
    private LinkedList<AreaGeniusInstanceInfo> geniusInstances;
    
    /**
     * @param e <CODE>Location</CODE> xml DOM element
     */
    public String load(Element e) {
        float z;
        float[] f;

        try {
            z = Float.parseFloat(e.getAttribute("z"));
            f = new float[3];
            f[2] = z;
        } catch (Exception exc) {
            f = new float[2];
        }
        
        try {
            f[0] = Float.parseFloat(e.getAttribute("x"));
            f[1] = Float.parseFloat(e.getAttribute("y"));
        } catch (Exception exc) {
            Log.warning("Wrong float format");
            return null;
        }
        position = f;
        objectPlacement = new LinkedList<ObjectClass>();
        Element placementElement = getOneChildElement(e, "placement");

        if (placementElement != null) {
            for (Element objPlcElement :getChildElements(placementElement,
                    "ObjectPlacement")) {
                String attributeValue = objPlcElement.getAttribute(
                        "objectTemplate");
                ObjectClass objClass = ObjectClassTree.instance().getObjectClass(
                        attributeValue);

                objectPlacement.add(objClass);
            }
        }
        
        geniusInstances = new LinkedList<AreaGeniusInstanceInfo>();
        
        Element geniesElement = getOneChildElement(e, "genies");

        if (geniesElement != null) {
            for (Element geniusElement :getChildElements(geniesElement, "Genius")) {
                AreaGeniusInstanceInfo gi = new AreaGeniusInstanceInfo();

                gi.load(geniusElement);
                geniusInstances.add(gi);
            }
        }
        
        return super.load(e);
    }
    

    /**
     * Create new instance of Area using informations from this class<br>
     * Identifier of parent and location name is used to form the identifier
     * of the new instance
     * Position of the parent and relative position from the InstanceInfo is 
     * used to pompute position
     * Except for the Area instance it creates AreaGeniuses if needed and 
     * binds them to the Area.
     * 
     * @param parent parent of the new instance.
     * @return new Area instance                
     */

    public Area instantiate(WayPoint parent) {
        String parentName = "";
        float[] absolutePos;
        float[] parentPos;

        if (parent != null) {
            parentPos = parent.getRealPosition();
            parentName = parent.getId() + IveId.SEP;
            int length = parentPos.length;

            absolutePos = new float[length];
            for (int i = 0; i < length; i++) {
                absolutePos[i] = parentPos[i] + position[i];
            }
        } else {
            parentPos = new float[] { 0, 0};
            absolutePos = position;
        }
        
        LocationTemplate t = (LocationTemplate) TemplateMap.instance().getTemplate(
                template);

        if (t != null) {
            String areaId = parentName + name;
            Area a = t.instantiate(areaId, parent, absolutePos);

            a.setGenies(createGenies(areaId));
            return  a;
        } else {
            Log.severe("LocationTemplate not found:" + template);
            return null;
        }
    }
    
    /**
     * Use genius instance infos to create list of new AreaGenius instances.
     * 
     * @param areaId is used to form genius identifier
     * @return array of Area genies.
     */
    private AreaGenius[] createGenies(String areaId) {
        AreaGenius[] ret = new AreaGenius[geniusInstances.size()];
        int i = 0;

        for (AreaGeniusInstanceInfo gi : geniusInstances) {
            ret[i] = gi.instantiate(areaId);
            i++;
        }
        return ret;
    }
    
}


/* 
 *
 * IVE Demo World
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
 
package cz.ive.location;


import cz.ive.exception.ObjectRegistrationFailedException;
import cz.ive.iveobject.Ent;
import cz.ive.iveobject.IveId;
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.ObjectClassTree;
import cz.ive.iveobject.ObjectMap;
import cz.ive.iveobject.attributes.AttrFuzzy;
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.iveobject.attributes.AttrObject;
import cz.ive.valueholders.FuzzyValueHolder;


/**
 * Java implementation of the location House from Estate.xml
 * HouseLocation gives advice to the standard object placement mechanism 
 * during expansion so that all actors apear in the bed.
 *
 * @author thorm
 */
public class HouseLocation extends Area2DGrid {
    
    /**
     * Counter of invocations - used to 
     */
    int counter;
    
    /** Creates a new instance of TownLocation */
    public HouseLocation(String objectId, WayPoint parent, float[] realPosition,
            Kind kind, ExpansionInfo info)
        throws ObjectRegistrationFailedException {
        super(objectId, parent, realPosition, kind, info);
    }
    
    /**
     * Return the special place for members of ObjectClass <CODE>/person</CODE>
     *
     * @param object object that should be placed to the location
     * @return Some bed from the location for the <CODE>/person</CODE> objects and 
     *         <CODE>null</CODE> for other objects
     */
    protected IveObject findObjectPlaceSpecial(IveObject object) {
        if (ObjectClassTree.instance().getObjectClass("/person").isInside(object)) {
            return findPlaceForPerson(object);
        }
        return null;
    }
    
    
    /**
     * Return some bed from the location, set the given actor to SLEEPING
     * state and set the clean empty flag of the bed.
     * @param o object that should be placed to the location
     */
    protected IveObject findPlaceForPerson(IveObject o) {
        
        AttrObject addressHouse = (AttrObject) o.getAttribute("address.house");
        
        if (!addressHouse.getValue().getId().equals(id)) return null;
        
        ObjectMap om = ObjectMap.instance();
        String bedId = getId() + IveId.SEP + "Bed" + (counter % 2 + 1);

        counter++;
        IveObject bed = om.getObject(bedId);
        
        if (bed == null) {
            return null;
        }
        
        AttrFuzzy emptyAttr = (AttrFuzzy) bed.getAttribute("empty");

        if (emptyAttr.getValue() == FuzzyValueHolder.False) {
            return null;
        }
        AttrFuzzy standingAttr = (AttrFuzzy) o.getAttribute("standing");
        AttrInteger stateAttr = (AttrInteger) o.getAttribute("state");

        emptyAttr.setValue(FuzzyValueHolder.False);
        standingAttr.setValue(FuzzyValueHolder.False);
        stateAttr.setValue(Ent.SLEEPING);
        
        return bed;
    }
    
}

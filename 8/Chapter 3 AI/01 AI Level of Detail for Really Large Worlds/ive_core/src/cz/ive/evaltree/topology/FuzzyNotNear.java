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
 
package cz.ive.evaltree.topology;


import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.ObjectMap;
import cz.ive.location.Joint;
import cz.ive.location.WayPoint;
import cz.ive.logs.Log;
import cz.ive.valueholders.FuzzyValueHolder;
import java.util.List;
import cz.ive.evaltree.leaves.*;


/**
 * Fuzzy expression leaf that takes two strings as parameter.
 * It is true when objects in substitution slots determined by strings are on
 * neighbour waypoints.
 *
 * @author thorm
 */
public class FuzzyNotNear extends BinaryTopologyProposition {
    
    /** Threshold for the WayPoint distance to be considered near */
    protected final static float NEAR_THRESHOLD = 2;
    
    /** 
     * Creates a new instance of FuzzyNotNear 
     *
     * @param obj1role role of the first object
     * @param obj2role role of the second object
     */
    public FuzzyNotNear(String obj1role, String obj2role) {
        super(obj1role, obj2role);
        position1 = new SourceToObjectPosition(obj1role, this);
        position2 = new SourceToObjectPosition(obj2role, this);
    }
    
    /**
     * Set the value to False if the positions of both object are the same 
     * or they are directly connected joint. The joint weight must be smaller 
     * than NEAR_THRESHOLD.
     * The value is True otherwise.
     *
     * The value is defined if both roles are assigned by the objects with valid
     * position attribute.
     */
    protected void updateValue() {
        IveObject wp1 = position1.getPosition();
        IveObject wp2 = position2.getPosition();
        
        if (wp1 == null || wp2 == null) {
            value.isDefined = false;
            return;
        }
        
        value.isDefined = true;
        
        if (wp1.getId().equals(wp2.getId())) {
            value.value = FuzzyValueHolder.False;
            return;
        }
        
        IveObject realWp = ObjectMap.instance().getObject(wp1.getId());

        if ((realWp == null) || !(realWp instanceof WayPoint)) {
            Log.severe(
                    "FuzzyNotNear update failed : Object " + wp1.getId()
                    + " not found in the object map");
            value.isDefined = false;
            return;
        }
        List<Joint> j = ((WayPoint) realWp).getNeighbours();
        boolean ret = false;
        
        // There is no such a rule that the waypoint have to have a neighbour.
        // Ondra
        if (j != null) {
            for (Joint joint:j) {
                if (joint.target.getId().equals(wp2.getId())) {
                    if (joint.weight < NEAR_THRESHOLD) {
                        ret = true;
                        break;
                    }
                }
            }
        } else {
            Log.warning(
                    "FuzzyNotNear waypoint: \"" + realWp.getId()
                    + "\" does not have any neighbour.");
        }
        value.value = (!ret) ? FuzzyValueHolder.True : FuzzyValueHolder.False;
    }
    
    protected FuzzyNotNear clone() {
        FuzzyNotNear ret = (FuzzyNotNear) super.clone();

        ret.position1 = new SourceToObjectPosition(role1, ret);
        ret.position2 = new SourceToObjectPosition(role2, ret);
        return ret;
    }
}

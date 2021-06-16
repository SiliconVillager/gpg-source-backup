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
import cz.ive.location.WayPoint;
import cz.ive.logs.Log;
import cz.ive.process.Substitution;


/**
 * Bind to the substitution slot determined by role and read object assigned
 * to it. The assigned object must have a possition attribute.
 *
 * @author thorm
 */
public class SourceToObjectPosition extends SourceToPosition {
    
    /** 
     * Creates a new instance of SourceToObjectPosition 
     *
     * @param roleInSubstitution objects role
     * @param btp node that is notified when the waypoint member variable changes
     */
    public SourceToObjectPosition(String roleInSubstitution, 
                                  BinaryTopologyProposition btp) {
        super(roleInSubstitution, btp);
    }
    
    /**
     * Fill the member variables by the data from substitution
     * Get the source from the substitution using roleInSubstitution
     * Get the object from the source
     * Content of waypoint is the position attribute of object.
     *
     * @param s substitution
     * @return true on success
     */
    public boolean evaluate(Substitution s) {
        source = s.getSource(roleInSubstitution);
        if (source == null) {
            Log.warning(
                    "Evaltree/SourceToObjectPosition - Empty source:"
                            + roleInSubstitution);
            return false;
        }
        object = source.getObject();
        if (object == null) {
            return true;
        }
        
        waypoint = object.getPosition();
        
        if (waypoint == null) {
            Log.warning(
                    "Evaltree/SourceToObjectPosition - Object " + object.getId()
                    + " does not have a position");
            return false;
        }
        
        return true;
    }
    
    /**
     * Update the member variables
     */
    public void changed(cz.ive.messaging.Hook initiator) {
        
        if (initiator == source) {
            if (object != null) {
                object.unregisterListener(this);
            }
            object = source.getObject();
            if (object != null) {
                object.registerListener(this);
            }
            initiator = object;
        }
        
        if (initiator == object) {
            IveObject tmp;

            if (object == null || (tmp = object.getPosition()) == null) {
                if (waypoint != null) {
                    waypoint = null;
                    btp.changed(null);
                }
                return;
            }
            waypoint = (WayPoint) ObjectMap.instance().getObject(tmp.getId());
            if (waypoint == null) {
                waypoint = (WayPoint) object.getPosition().getLeastActiveParent();
            }
        }
        
        btp.changed(null);
    }
    
    /**
     * Fill the member variables using given substitution and 
     * register as listenner on the source and object
     */
    public boolean instantiate(Substitution s) {
        if (evaluate(s)) {
            source.registerListener(this);
            if (object != null) {
                object.registerListener(this);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Unregister the listenners
     */
    public boolean uninstantiate() {
        if (source != null) {
            source.unregisterListener(this);
        }
        if (object != null) {
            object.unregisterListener(this);
        }

        source = null;
        object = null;
        waypoint = null;

        return true;
    }
}

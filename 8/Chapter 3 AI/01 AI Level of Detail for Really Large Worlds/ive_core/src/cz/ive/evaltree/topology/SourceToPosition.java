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
import cz.ive.messaging.Listener;
import cz.ive.process.Slot;
import cz.ive.process.Source;
import cz.ive.process.Substitution;


/**
 * Bind to the substitution slot determined by role and read object assigned
 * to it. The assigned object is WayPointImpl descendant.
 *
 * @author thorm
 */
public class SourceToPosition implements Listener, java.io.Serializable {
    
    /**
     * role in the substitution
     */
    String roleInSubstitution;
    
    /**
     * Source in the substitution slot
     */
    Source    source;
    
    /**
     * IveObject assigned into source
     */
    IveObject object;
    
    /**
     * objects position - or the object itself if it is a waypoint
     */
    IveObject  waypoint;
    
    /**
     * Notify the changes to this proposition
     */
    BinaryTopologyProposition btp;
    
    /**
     * @return position of the object from substitution
     */
    public IveObject getPosition() {
        return waypoint;
    }

    /** 
     * Creates a new instance of SourceToPosition 
     *
     * @param roleInSubstitution objects role
     * @param btp node that is notified when the waypoint member variable changes
     */
    public SourceToPosition(String roleInSubstitution, 
                            BinaryTopologyProposition btp) {
        this.roleInSubstitution = roleInSubstitution;
        this.btp = btp;
    }
    
    
    /**
     * Fill the member variables by the data from substitution
     * Get the source from the substitution using roleInSubstitution
     * Get the object from the source
     * Content of waypoint is the same as content of object
     *
     * @param s substitution
     * @return true on success
     */
    public boolean evaluate(Substitution s) {
        
        source = s.getSource(roleInSubstitution);
        if (source == null) {
            Log.warning(
                    "Evaltree/SourceToPosition - Empty source:"
                            + roleInSubstitution);
            return false;
        }
        object = source.getObject();
        waypoint = object;        
        return true;
    }
    
    /**
     * Fill the member variables using given substitution and 
     * register as listenner on the source
     */
    public boolean instantiate(Substitution s) {
        if (evaluate(s)) {
            source.registerListener(this);            
            return true;
        }
        return false;
    }
    
    /**
     * Remove the listenning on the source
     */
    public boolean uninstantiate() {
        source.unregisterListener(this);
        
        source = null;
        object = null;
        waypoint = null;

        return true;
    }
    
    public void canceled(cz.ive.messaging.Hook initiator) {}
    
    /**
     * Update the member variables
     */
    public void changed(cz.ive.messaging.Hook initiator) {
        
        if (initiator == source) {
            object.unregisterListener(this);
            object = source.getObject();
            object.registerListener(this);
            initiator = object;
        }
        
        if (initiator == object) {            
            waypoint = (WayPoint) ObjectMap.instance().getObject(object.getId());
            if (waypoint == null) {
                waypoint = (WayPoint) object.getLeastActiveParent();
            }
        }
        
        btp.changed(null);
    }
    
    /**
     * Two SourceToPostition objects are equal if they are represented by the
     * same class and their source member variable is the same ( reference  )
     */ 
    public int contentHashCode() {
        return (source != null) ? source.hashCode() : hashCode();
    }

    /**
     * Two SourceToPostition objects are equal if they are represented by the
     * same class and their source member variable is the same ( reference  )
     */ 
    public boolean contentEquals(Object obj) {
        if (source == null) {
            return false;
        }
        return (obj instanceof SourceToPosition)
                && ((SourceToPosition) obj).source == this.source;
    }
}

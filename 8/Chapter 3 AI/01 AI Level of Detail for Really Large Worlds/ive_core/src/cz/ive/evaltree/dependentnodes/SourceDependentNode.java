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
 
package cz.ive.evaltree.dependentnodes;


import cz.ive.evaltree.*;
import cz.ive.process.Substitution;
import cz.ive.process.Source;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.logs.Log;
import cz.ive.messaging.Listener;
import cz.ive.sensors.Sensor;
import java.util.List;


/**
 * Succesor of all evaltree nodes whose value depends on Source
 * @author thorm
 */
public abstract class SourceDependentNode<TYPE extends ValueHolderImpl> 
        extends GenericHookDependentNode<TYPE> implements Listener {
    /**
     *  Identifier of the role
     */
    protected String role;
    
    /**
     *  Source from the substitution assigned to the given role
     */
    protected Source source;
        
    /** Creates a new instance of SourceDependentNode */
    public SourceDependentNode(String role) {        
        this.role = role;        
    }

    /**
     * Register as listenner to the source from the substitution with the 
     * required role
     */
    public void instantiate(Substitution s, List<Sensor> _sensors) {
        
        source = s.getSource(role);
        if (source == null) {
            Log.warning(
                    "Evaltree/SourceDependentNode - No source for the role :"
                            + role);
        } else {
            source.registerListener(this);
        }
        initialUpdateValue();
    }
    
    public void uninstantiate() {
        if (source != null) {
            source.unregisterListener(this);
            source = null;
        }
        
    }
    
    /**
     * SourceDependentNodes are considered to be equal if they are represented
     * by the same cleass and refers to the same source.
     */
    public int contentHashCode() {
        return (this.getClass().hashCode() + source.hashCode());
    }
    
    /**
     * SourceDependentNodes are considered to be equal if they are represented
     * by the same cleass and refers to the same source.
     */
    public boolean contentEquals(Object obj) {
        return (this.getClass() == obj.getClass())
                && source == ((SourceDependentNode) obj).source;
                
    }

    /**
     * Needed to implement Listenner interface
     */
    public void canceled(cz.ive.messaging.Hook initiator) {}
    
}

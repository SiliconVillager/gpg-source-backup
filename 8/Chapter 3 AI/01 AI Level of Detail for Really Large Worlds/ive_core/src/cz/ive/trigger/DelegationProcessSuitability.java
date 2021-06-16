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
 
package cz.ive.trigger;

import cz.ive.IveApplication;
import cz.ive.exception.OntologyNotSupportedException;
import java.util.*;

import cz.ive.messaging.*;
import cz.ive.ontology.*;
import cz.ive.process.*;
import cz.ive.genius.*;
import cz.ive.sensors.*;
import cz.ive.iveobject.*;
import cz.ive.logs.Log;

/**
 * Suitability for all delegated processes. The suitability is true when there
 * is a genius registrated to the template of an actual process and the area
 * in the given sources is the same the genius registered with.
 *
 * @author honza
 */
public class DelegationProcessSuitability extends SyncHook
        implements Trigger, java.io.Serializable {
    
    /** Actual value of the trigger */
    short triggerValue = -1;
    
    /** Map of the genius registered for the associted delegated process */
    Map<String, AreaGenius> registrations;
    
    /** Sources to be used during evaluation of this trigger. */
    Substitution sources;
    
    /**
     * Suitability loaded from the XML. It is efectively ANDed with
     * the condition: There is some area genius registered to the area where
     * the actor stands
     */
    TriggerTemplate userSuitability;
    
    /** Creates a new instance of DelegationProcessSuitability */
    public DelegationProcessSuitability(Substitution sources,
            Map<String, AreaGenius> registrations,
            TriggerTemplate userSuitability) {
        super();
        this.registrations = registrations;
        this.sources = sources;
        this.userSuitability = userSuitability;
    }
    
    public void delete() {
    }
    
    public OntologyToken value() {
        return new SingleToken("java.Short", new Short(triggerValue));
    }
    
    public static short getValue(Substitution sources,
            Map<String, AreaGenius> registrations,
            TriggerTemplate userSuitability, List<Sensor> sensors) {
        // Find the actor.
        Set<String> actorSlotsKeys = sources.getActorSlotsKeys();
        if (actorSlotsKeys.isEmpty()) {
            Log.warning("No actor substituted to the delegated process.");
            return 0;
        }
        IveId area = null;
        for (String actorSlot : actorSlotsKeys) {
            IveObject actor = sources.getSource(actorSlot).getObject();
            area = actor.getPosition();
            break;
        }
        AreaGenius genius = registrations.get(area.getId());
        while (genius == null) {
            area = area.getParent();
            if (area == null) {
                return 0;
            }
            genius  = registrations.get(area.getId());
        }
        Short value = 0;
        try {
            value = (Short) userSuitability.evaluate(sources, sensors).
                    getData("java.Short");
        } catch (OntologyNotSupportedException ex) {
            IveApplication.printStackTrace(ex);
            Log.severe("Trigger does not support the \"java.Short\" ontology.");
        }
        return value;
    }
    
    public static OntologyToken getOntoValue(Substitution sources,
            Map<String, AreaGenius> registrations,
            TriggerTemplate userSuitability, List<Sensor> sensors) {
        return new SingleToken("java.Short",
                new Short(getValue(sources, registrations,
                userSuitability, sensors)));
    }
    
    public Object getData(String ontology) throws
            OntologyNotSupportedException {
        throw new OntologyNotSupportedException();
    }
    
    public String[] getOntologies() {
        return null;
    }
    
    public boolean supports(String ontology) {
        return false;
    }
    
    public void changeSensors(List<Sensor> sensors){}
}

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

import java.util.*;

import cz.ive.ontology.*;
import cz.ive.process.*;
import cz.ive.genius.*;
import cz.ive.sensors.Sensor;

/**
 *
 * Suitability template for all delegated processes.
 *
 * @author honza
 */
public class DelegationProcessSuitabilityTemplate extends SingleToken
        implements TriggerTemplate, java.io.Serializable {
    
    /** Our supported ontology */
    private static final String ontoString = "jBRP.trigger";
    
    /** Map of the genius registered for the associted delegated process */
    Map<String, AreaGenius> registrations = null;
    
    /**
     * Suitability loaded from the XML. It is effectively ANDed with
     * the condition: There is some area genius registered to the area where
     * the actor stands
     */
    TriggerTemplate userSuitability;
    
    /** Creates a new instance of DelegationProcessSuitabilityTemplate */
    public DelegationProcessSuitabilityTemplate(Map<String, AreaGenius>
            registrations, TriggerTemplate userSuitability) {
        super(ontoString);
        data = this;
        this.registrations = registrations;
        this.userSuitability = userSuitability;
    }
    
    public Trigger instantiate(Substitution sources,List<Sensor> sensors,
            Map<String, Object> parameters) {
        return new DelegationProcessSuitability(sources, registrations,
                userSuitability);
    }
    
    public OntologyToken evaluate(Substitution sources,List<Sensor> sensors) {
        return DelegationProcessSuitability.getOntoValue(sources,
                registrations, userSuitability, sensors);
    };
    
}

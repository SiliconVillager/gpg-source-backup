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

import cz.ive.messaging.*;
import cz.ive.ontology.*;
import cz.ive.sensors.Sensor;
import java.util.List;

/**
 * Instance of a Trigger. The Trigger will be evaluated if there is at
 * least one Listener registered.
 * One of supported Ontology could be "jBRP.expression", returning
 * coresponding evaltree, to be manualy merged into ExprDag of particular 
 * genius.
 *
 * @author  Ondra
 */
 
public interface Trigger extends Hook, OntologyToken, java.io.Serializable {
    /**
     * Retrives value of this Trigger as an OntologyToken.
     * The real-value must be extracted from this token
     * by using apropriate ontology.
     * @return OntologyToken storing the value of this Trigger
     */
    OntologyToken value();
    
    /**
     * Finds all references to other parts of ive, in particular 
     * hooks that are listenned by this listener, and sets them to null.
     */
    void delete();
    
    public void changeSensors(List<Sensor> sensors);
}
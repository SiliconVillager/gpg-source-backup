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
import cz.ive.evaltree.*;
import cz.ive.ontology.*;
import cz.ive.evaltree.valueholdersimpl.FuzzyValueHolderImpl;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.logs.Log;
import cz.ive.sensors.Sensor;
import java.util.*;


/**
 *
 * @author thorm
 */
public class EvalTreeTriggerTemplate
        extends SingleToken implements TriggerTemplate {
    
    private Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> root;
    private static final String ontoString = "jBRP.trigger";
    
    /**
     * Creates a new instance of TriggerTemplateImpl
     * @param root uninstantiated evaltree expression
     */
    public EvalTreeTriggerTemplate(
            Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> root
            ) {
        super(ontoString, root);
        this.root = root;
    }
    
    
    public cz.ive.ontology.OntologyToken evaluate(
            cz.ive.process.Substitution sources, List<Sensor> sensors
            ) {
        
        root.DFSEval(sources, sensors);
        if (!root.getValue().isDefined()) {
            Log.fine("EvalTreeTriggerTemplate - evaluate - undefined value.");
            return new SingleToken("java.Short", new Short((short) 0));
        }
        return new SingleToken("java.Short",
                new Short((root.getValue()).getValue()));
    }
    
    /**
     * The root member value is cloned and the copy is instantiated using given
     * sensors. The new instantiated expresion is used in the returned trigger
     */
    public Trigger instantiate(
            cz.ive.process.Substitution sources, List<Sensor> sensors,
            Map<String, Object> parameters
            ) {
        
        try {
            Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> newroot =
                    root.createCopy();
            
            newroot.instantiate(sources, sensors);
            if (!newroot.getValue().isDefined()) {
                Log.fine("EvalTreeTriggerTemplate - instantiate - " +
                        "undefined value.");
            }
            return new EvalTreeTrigger(newroot);
        } catch (Exception e) {
            IveApplication.printStackTrace(e);
            return null;
        }
        
    }
    
}

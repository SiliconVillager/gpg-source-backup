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


import cz.ive.evaltree.*;
import cz.ive.ontology.*;
import cz.ive.messaging.*;
import cz.ive.evaltree.valueholdersimpl.FuzzyValueHolderImpl;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.sensors.Sensor;
import java.util.List;


/**
 * Implementation of trigger using evaltree expressions
 * @author thorm
 */
public class EvalTreeTrigger extends SyncHook implements Trigger, Listener {

    /**
     * Impl3mentation of OntologyToken is done by delegation to this object
     */
    OntologyToken token;

    /**
     * root of evaltree expression
     */
    private Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> root;

    /**
     * DAG where evaltree expression is stored. It is null when expression isnt 
     * in any DAG
     */
    private ExprDAG exprDAG;

    /**
     * ontology identifier
     */
    private static final String ontoString = "jBRP.trigger";
    
    /**
     * Creates a new instance of EvalTreeTrigger
     * @param root Root of evaltree expression
     */
    
    public EvalTreeTrigger(
            Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> root
            ) {
        this.root = root;
        root.registerListener(this);
        token = new SingleToken(ontoString, this.root);
        changed(root);
    }
    
    /**
     * Inserts its expression into DAG. It fails when expression is already 
     * inserted in any DAG
     * @param d DAG to be inserted in
     * @return true on success
     */
    public boolean insertIntoDAG(ExprDAG d) {
        Expr newRoot = null;

        if (exprDAG == null) {
            exprDAG = d;
            newRoot = exprDAG.insertExpression(root);
            if (newRoot != root) {
                root.unregisterListener(this);
                
                root = newRoot;
                root.registerListener(this);
                token = new SingleToken(ontoString, root);
            }
            
            return true;
        }
        return false;
    }
    
    public Object getData(String ontology) 
        throws cz.ive.exception.OntologyNotSupportedException {
        return token.getData(ontology);
    }
    
    public String[] getOntologies() {
        return token.getOntologies();
    }
    
    public boolean supports(String ontology) {
        return token.supports(ontology);
    }
    
    public cz.ive.ontology.OntologyToken value() {
        FuzzyValueHolderImpl value = root.getValue();

        if (value.isDefined()) {
            return new SingleToken("java.Short", new Short((value).getValue()));
        }
        return new SingleToken("java.Short", new Short((short) 0));
    }
    
    public void canceled(Hook initiator) {}
    
    public void changed(Hook initiator) {
        notifyListeners();
    }
    
    /**
     * Call this to remove trigger from memory. It calls uninstantiate method of
     * internal Expr object or  removes it from DAG (what includes 
     * uninstantiation)
     */
    public void delete() {
        if (exprDAG != null) {
            exprDAG.removeExpression(root);

        } else {
            root.uninstantiateTree();
        }
        root.unregisterListener(this);
        root = null;
    }
    
    /**
     * Change the sensors of already instantiated trigger
     *
     * @param sensors new list of sensors
     */
    public void changeSensors(List<Sensor> sensors) {
        if (root != null) {
            root.changeSensors(sensors);
        }
    }
    
}

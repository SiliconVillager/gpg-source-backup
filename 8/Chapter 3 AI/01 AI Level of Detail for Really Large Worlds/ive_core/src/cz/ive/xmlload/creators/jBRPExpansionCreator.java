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
 
package cz.ive.xmlload.creators;


import cz.ive.logs.Log;
import cz.ive.ontology.OntologyToken;
import cz.ive.process.ExpansionProducer;
import cz.ive.process.ExpansionProducerImpl;
import cz.ive.process.GoalFactory;
import cz.ive.process.Substitution;
import cz.ive.trigger.TriggerTemplate;
import cz.ive.util.Pair;
import cz.ive.xmlload.XMLDOMLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;


/**
 * Create the ExpansionProducer class that represents process expansion to
 * subgoals described using jBRPExpansion ontology
 * @author thorm
 */
public class jBRPExpansionCreator extends Creator<ExpansionProducer> {
    
    /**
     * Create thr ExpansionProducer containing data from the given element.
     * jBRPExpansion is just a list of goals. The priority is given by order.
     * @param e <CODE>OntologyToken</CODE> element with attribute <CODE>ontology
     *          </CODE> equal to the <CODE>jBRPExpansion</CODE>
     */
    public ExpansionProducer load(Element e) {
        ExpansionProducer result = null;
        
        LinkedList<GoalFactory> goals = new LinkedList<GoalFactory>();
        
        for (Element goalElement: getSubElements(e, "Goal")) {
            GoalFactory g = loadGoal(goalElement);

            goals.addLast(g);
        }
        
        GoalFactory[] tmp = new GoalFactory[0];

        result = new ExpansionProducerImpl(goals.toArray(tmp));
        
        return result;
    }
    
    /**
     * Create the GoalFactory using data from the given element.
     * 
     * @param e <CODE>Goal</CODE>
     * @return GoalFactory instance        
     */
    GoalFactory loadGoal(Element e) {
        String goalId = e.getAttribute("goalId");
        
        // load gtrigger
        Creator<OntologyToken> oCreator = new OntologyTokenCreator();
        Element gtriggerElement = getOneChildElement(
                getOneChildElement(e, "gtrigger"), "OntologyToken");
        TriggerTemplate gtrigger = (TriggerTemplate) oCreator.load(
                gtriggerElement);
        
        // load gcontext
        TriggerTemplate gctx = null;
        Element gctxElement = getOneChildElement(e, "gcontext");

        if (XMLDOMLoader.readBoolean(gctxElement, "sameAsTrigger")) {
            gctx = gtrigger;
        } else {
            Element oTokenElement = getOneChildElement(gctxElement,
                    "OntologyToken");

            if (oTokenElement == null) {
                Log.severe("Missing gcontext");
            } else {
                gctx = (TriggerTemplate) oCreator.load(oTokenElement);
            }
        }
        
        // load substitution
        Element sourcesElement = getOneChildElement(e, "sources");
        Creator<Substitution> substC = new SubstitutionCreator();
        Substitution subst = substC.load(sourcesElement);
        
        // load propagation info
        
        List<Pair<String, String>> copySlots = new LinkedList<Pair<String, String>>();
        
        for (Element propagate: getChildElements(sourcesElement, "Slot")) {
            String valueFrom = propagate.getAttribute("valueFrom");

            if (valueFrom.equals("")) {
                continue;
            }
            copySlots.add(
                    new Pair<String, String>(valueFrom,
                    propagate.getAttribute("name")));
        }
        
        // load parameters
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        Element parametersElem = getOneChildElement(e, "parameters");

        for (Element parameter: getChildElements(parametersElem, "Parameter")) {
            parameters.put(parameter.getAttribute("name"),
                    oCreator.load(getOneChildElement(parameter, "OntologyToken")));
        }
        
        GoalFactory result = new GoalFactory(gtrigger, gctx, subst, parameters,
                copySlots, goalId);
        
        return result;
    }
    
}

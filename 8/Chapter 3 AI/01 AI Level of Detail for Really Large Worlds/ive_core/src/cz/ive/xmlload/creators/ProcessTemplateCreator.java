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


import cz.ive.IveApplication;
import cz.ive.lod.LOD;
import cz.ive.logs.Log;
import cz.ive.ontology.OntologyToken;
import cz.ive.process.CommonProcessTemplate;
import cz.ive.process.ExpansionProducer;
import cz.ive.process.ProcessTemplate;
import cz.ive.process.Substitution;
import cz.ive.trigger.TriggerTemplate;
import cz.ive.xmlload.XMLDOMLoader;
import org.w3c.dom.Element;


/**
 * Create the process template using information from the given element.
 * @author thorm
 */
public class ProcessTemplateCreator extends Creator<ProcessTemplate> {
    
    /**
     * Create the process template using information from the given element
     *
     * @param e <CODE>ProcessTemplate</CODE> element
     */
    public ProcessTemplate load(Element e) {
        CommonProcessTemplate result = null;
        
        String className = e.getAttribute("className");
        
        String goalId = e.getAttribute("goalId");
        String processId = e.getAttribute("processId");
        ExpansionProducer expansionProducer;
        Substitution substitution;
        
        String minLod = e.getAttribute("minLod");
        String maxLod = e.getAttribute("maxLod");
        
        LOD lod;
        
        try {
            lod = new LOD(Integer.parseInt(minLod), Integer.parseInt(maxLod));
        } catch (NumberFormatException ex) {
            // This should be filtered out by a xsd definition.
            IveApplication.printStackTrace(ex);
            return null;
        }
        
        Element expansionElement = getOneChildElement(e, "expansion");
        Element oToken = getOneChildElement(expansionElement, "OntologyToken");
        
        expansionProducer = null;
        if (oToken != null) {
            Creator<ExpansionProducer> c = getCreator(oToken,
                    new jBRPExpansionCreator());
            
            expansionProducer = c.load(oToken);
        }
        Element sourcesElement = getOneChildElement(e, "sources");
        Creator<Substitution> sourcesC = new SubstitutionCreator();
        
        substitution = sourcesC.load(sourcesElement);
        
        // load gtrigger
        Creator<OntologyToken> oCreator = new OntologyTokenCreator();
        Element suitabElement = getOneChildElement(
                getOneChildElement(e, "suitability"), "OntologyToken");
        TriggerTemplate suitability = (TriggerTemplate) oCreator.load(
                suitabElement);
        
        // load gcontext
        TriggerTemplate pctx = null;
        
        Element pctxElement = getOneChildElement(e, "pcontext");
        
        if (XMLDOMLoader.readBoolean(pctxElement, "sameAsSuitability")) {
            pctx = suitability;
        } else {
            Element oTokenElement = getOneChildElement(pctxElement,
                    "OntologyToken");
            
            if (oTokenElement == null) {
                Log.severe("Missing gcontext");
            } else {
                pctx = (TriggerTemplate) oCreator.load(oTokenElement);
            }
        }
        
        if (className != null) {
            try {
                Class objectClass = IveApplication.instance().loadIveClass(
                        className);
                
                result = (CommonProcessTemplate) objectClass.newInstance();
                result.initMembers(goalId, processId, substitution,
                        expansionProducer, suitability, pctx, lod);
            } catch (ClassNotFoundException exc) {
                Log.severe(
                        "Class " + className + " representing process template "
                        + processId + " of goal " + goalId + " not found");
                return null;
            } catch (ClassCastException exc) {
                Log.severe(
                        "Class " + className + " representing process template "
                        + processId + " of goal " + goalId
                        + " is not derived from CommonProcessTemplate");
                return null;
            } catch (InstantiationException exc) {
                Log.severe(
                        "Class " + className + " representing process template "
                        + processId + " of goal " + goalId
                        + " was not instantiated");
                return null;
            } catch (IllegalAccessException exc) {
                Log.severe(
                        "Class " + className + " representing process template "
                        + processId + " of goal " + goalId
                        + " was not instantiated");
                return null;
            }
        }
        
        return result;
    }
    
}

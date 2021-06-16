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


import cz.ive.evaltree.SAX2Creator;
import cz.ive.logs.Log;
import cz.ive.trigger.EvalTreeTriggerTemplate;
import cz.ive.trigger.TriggerTemplate;

import org.w3c.dom.*;


/**
 *
 * @author thorm
 */
public class jBRPExpressionCreator extends Creator<TriggerTemplate> {
    /**
     * SAXHandler that knows evaltree elements
     */
    SAX2Creator.EvalTreeSAXHandler handler;
    
    /** Creates a new instance of EvalTreeLoad */
    public jBRPExpressionCreator() {
        handler = new SAX2Creator.EvalTreeSAXHandler();
    }
    
    /**
     * Create TriggerTemplate using information from the given element
     * @param t <CODE>OntologyToken</CODE> element with attribute <CODE>ontology
     *          </CODE> equal to the <CODE>jBRPExpression</CODE>
     */
    public TriggerTemplate load(Element t) {
        
        // Bridge between DOM and SAX approach
        DOMSaxator saxator = new DOMSaxator() {
            
            boolean elementBegin(Element e) throws Exception {
                
                handler.startElement(e.getNamespaceURI(), e.getLocalName(),
                        e.getNodeName(), temporary);
                return true;
            }
            
            void elementEnd(Element e) throws Exception {
                handler.endElement(e.getNamespaceURI(), e.getLocalName(),
                        e.getNodeName());
            }
            
            boolean startDocument() throws Exception {
                handler.startDocument();
                return true;
            }
            
            void endDocument() throws Exception {}
            
        };
        
        Element expr = getOneChildElement(t, "*");
        String name = expr.getNodeName();
        
        try {
            saxator.traverseDOM(expr);
        } catch (Exception e) {
            Log.warning("Error during creating expression");
        }
        return new EvalTreeTriggerTemplate(handler.finishedNodes.pop());
    }
    
}

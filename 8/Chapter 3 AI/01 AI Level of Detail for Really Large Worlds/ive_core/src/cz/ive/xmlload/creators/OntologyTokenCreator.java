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
import cz.ive.logs.Log;
import cz.ive.ontology.OntologyToken;
import cz.ive.ontology.SingleToken;
import org.w3c.dom.Element;


/**
 * Create OntologyToken using the information from the given element.
 * @author thorm
 */
public class OntologyTokenCreator extends Creator<OntologyToken> {
    
    /**
     *
     * Use the content of <CODE>t</CODE> to create Creator used to load the
     * element subtree
     * Attribute <CODE>ontology</CODE> of <CODE>t</CODE> should contain 
     * name of the known ontology. 
     * If there is no such attribute the Creator.getCreator() is called
     *
     * @return suitable Creator
     */
    public Creator getCreator(Element t) {
        Creator byOntologyStr = null;
        String s;

        s = t.getAttribute("ontology");
        if (s != null) {
            if (s.equals("jBRP.expansion")) {
                byOntologyStr = new jBRPExpansionCreator();
            }
            if (s.equals("jBRP.expression")) {
                byOntologyStr = new jBRPExpressionCreator();
            }
        }
        return super.getCreator(t, byOntologyStr);
        
    }

    /**
     * Create the OntologyToken using data from the given element.
     * Some ontologies can be represented only by one attribute. Such cases are
     * handled within this method and the value of the ontology token is in the
     * attribute value of the child element Value.<br>
     * Currently this method supports following "one attribute" ontologies:
     *  <ol>
     *  <li>java.class</li>
     *  <li>java.int</li>
     *  <li>java.bool</li>
     *  </ol>
     *
     * If the ontology is not so simple the method getCreator is called and 
     * load method of returned Creator is invoked.
     * 
     * @param t <CODE>OntologyToken</CODE> element
     * @return new OntologyTokenInstance
     * 
     */
    public OntologyToken load(Element t){
        String ontology = t.getAttribute("ontology");
        
        if (ontology.equals("java.class")) {
            Element valueElement = getOneChildElement(t, "Value");

            return classLoader(valueElement.getAttribute("value"));        
        }
        if (ontology.equals("java.int")) {
            Element valueElement = getOneChildElement(t, "Value");            
            String attrValue = valueElement.getAttribute("value");

            try {                
                return new SingleToken("java.int", new Integer(attrValue));                
            } catch (NumberFormatException exc) {
                Log.severe(
                        "Wrong number format in ontology token: ontology: "
                                + ontology + " value: " + attrValue);
                return null;
            }
        }
        if (ontology.equals("java.bool")) {
            Element valueElement = getOneChildElement(t, "Value");
            String attrValue = valueElement.getAttribute("value");

            try {                
                return new SingleToken("java.bool", new Boolean(attrValue));                
            } catch (NumberFormatException exc) {
                Log.severe(
                        "Wrong number format in ontology token: ontology: "
                                + ontology + " value: " + attrValue);
                return null;
            }
        }
        Creator<OntologyToken> creator = getCreator(t); 

        if (creator == null) {
            Log.severe(
                    "Unknown ontology token: " + t.toString() + " ontology "
                    + ontology);
            return null;
        }
        return creator.load(t);
    }
    
    /**
     * Load the OntologyToken directly represented by some class
     * @param className name of the java class
     */
    OntologyToken classLoader(String className) {
        try {
            return (OntologyToken) IveApplication.instance().loadIveClass(className).newInstance();
        } catch (Exception e) {
            Log.warning(
                    "Class not found or could not be instantiated: " + className);
            return null;
        }
    }
}

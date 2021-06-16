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
 
package cz.ive.xmlload;

import cz.ive.IveApplication;
import cz.ive.exception.NoObjectPlaceException;
import cz.ive.gui.AnimatedTemplate;
import cz.ive.gui.StaticTemplate;
import cz.ive.location.Area;
import cz.ive.location.IveMapImpl;
import cz.ive.location.LocationState;
import cz.ive.location.WayPoint;
import cz.ive.logs.Log;
import cz.ive.process.ProcessDBImpl;
import cz.ive.process.ProcessTemplate;
import cz.ive.simulation.Scheduler;
import cz.ive.simulation.SchedulerImpl;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import cz.ive.template.*;

import cz.ive.xmlload.creators.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import org.w3c.dom.Node;


/**
 * This class contains functions related to load of ive world from it's xml
 * representation
 *
 * @author thorm
 */
public class XMLDOMLoader {
    
    /**
     * Load IveWorld from XML file.
     * It fills TemplateMap.instance() with object and location templates and
     * ProcessDB.instance() with ProcessTemplates,
     *
     * @param url	location where the valid XML document is stored
     */
    public void load(java.net.URL url) throws Exception {
        Document doc = createDocument(url.toString());
        Element root = doc.getDocumentElement();
        
        for (Element element : getChildElements(root, "*")) {
            String elementName = element.getNodeName();
            
            if (elementName.equals("include")) {
                String urlStr = element.getAttribute("url");
                URL includeURL = new URL(url, urlStr);
                
                Log.info("Including file:" + urlStr);
                load(includeURL);
                continue;
            }
            
            if (elementName.equals("classPath")) {
                Log.info("Enriching classpath");
                
                for (Element classPathItemElement : getChildElements(element,
                        "ClassPathItem")) {
                    
                    String urlStr = classPathItemElement.getAttribute("url");
                    URL classPathItem = new URL(url, urlStr);
                    
                    IveApplication.instance().addClassPathURL(classPathItem);
                    
                }
                continue;
            }
            
            if (elementName.equals("graphicTemplates")) {
                Log.info("Loading graphic templates");
                loadGraphicTemplates(element);
                continue;
            }
            
            if (elementName.equals("objectTemplates")) {
                Log.info("Loading object templates");
                loadObjectTemplates(element);
                continue;
            }
            
            if (elementName.equals("locationTemplates")) {
                Log.info("Loading location templates");
                loadLocationTemplates(element);
                continue;
            }
            
            if (elementName.equals("processTemplates")) {
                Log.info("Loading process templates");
                loadProcessTemplates(element);
                continue;
            }
            
            if (elementName.equals("rootLocation")) {
                Log.info("Loading root location instance");
                loadTopology(element);
                continue;
            }
            
            if (elementName.equals("utterances")) {
                Log.info("Loading utternaces");
                loadUtterances(element);
                continue;
            }
            
            if (elementName.equals("SimulationSettings")) {
                Log.info("Loading utternaces");
                loadSimulationSettings(element);
                continue;
            }
            
        }
    }
    
    
    
    /**
     * Fill TemplateMap.instance() with ObjectTemplates described
     * by subelements of e
     * @param e	XML element "objectTemplates"
     */
    
    void loadObjectTemplates(Element e) throws Exception {
        Template tmp;
        String name;
        
        for (Element objTempElement : getChildElements(e, "ObjectTemplate")) {
            tmp = Creator.getTemplate(objTempElement, new ObjectTemplate());
            name = tmp.load(objTempElement);
            if (name != null) {
                TemplateMap.instance().register(name, tmp);
            } else {
                Log.severe("Loading of " + name + " object template failed");
            }
        }
        
        for (Element objTempElement : getChildElements(e, "EntTemplate")) {
            tmp = Creator.getTemplate(objTempElement, new EntTemplate());
            name = tmp.load(objTempElement);
            if (name != null) {
                TemplateMap.instance().register(name, tmp);
            } else {
                Log.severe("Loading of " + name + " object template failed");
            }
        }
    }
    
    /**
     * Fills TemplateMap.instance() with LocationTemplates
     * described by subelements of e
     * @param e	XML element "locationTemplates"
     */
    void loadLocationTemplates(Element e) {
        Template tmp;
        String name;
        
        for (Element locTempElement : getChildElements(e, "GridLocation")) {
            tmp = Creator.getTemplate(locTempElement, new GridLocationTemplate());
            name = tmp.load(locTempElement);
            if (name != null) {
                TemplateMap.instance().register(name, tmp);
            } else {
                Log.severe("Loading of " + name + " location template failed");
            }
        }
        
        for (Element locTempElement : getChildElements(e, "GraphLocation")) {
            
            tmp = Creator.getTemplate(locTempElement,
                    new GraphLocationTemplate());
            name = tmp.load(locTempElement);
            if (name != null) {
                TemplateMap.instance().register(name, tmp);
            } else {
                Log.severe("Loading of " + name + " location template failed");
            }
        }
    }
    
    /**
     *  This function fills ProcessDBImpl.instance() with ProcessTemplates
     *  described by subelements of e
     *  @param e	XML element "processTemplates"
     */
    void loadProcessTemplates(Element e) throws Exception {
        for (Element procTempElement: getChildElements(e, "ProcessTemplate")) {
            Creator<ProcessTemplate> c = new ProcessTemplateCreator();
            ProcessTemplate procTemp = c.load(procTempElement);
            String procId = procTempElement.getAttribute("processId");
            String goalId = procTempElement.getAttribute("goalId");
            
            if (procTemp != null) {
                ProcessDBImpl.instance().setProcess(procId, goalId, procTemp);
            } else {
                Log.severe(
                        "Loading of process template " + procId + " of goal "
                        + goalId + " failed");
            }
        }
    }
    
    /**
     * Loads the utterances from the given Element.
     *
     * @param e DOM Element containing the utterances
     */
    void loadUtterances(Element e) {
        Utterances utterances = Utterances.instance();
        
        for (Element utt: getChildElements(e, "Utterance")) {
            String text = utt.getAttribute("text");
            
            try {
                Integer idx = new Integer(utt.getAttribute("index"));
                
                utterances.addUtterance(idx, text);
            } catch (NumberFormatException ex) {
                Log.severe("Unexpected number format: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Load the graphic templates
     *
     *@param e DOM Element containing the graphic templates
     */
    void loadGraphicTemplates(Element e) {
        Template tmp;
        String name;
        
        for (Element graphTempElement: getChildElements(e, "GraphicTemplate")) {
            tmp = Creator.getTemplate(graphTempElement, new StaticTemplate());
            name = tmp.load(graphTempElement);
            if (name != null) {
                TemplateMap.instance().register(name, tmp);
            }
        }
        for (Element graphTempElement: getChildElements(e,
                "AnimatedGraphicTemplate")) {
            tmp = Creator.getTemplate(graphTempElement, new AnimatedTemplate());
            name = tmp.load(graphTempElement);
            if (name != null) {
                TemplateMap.instance().register(name, tmp);
            }
        }
    }
    
    /**
     * Create the root loaction instance, set its state to atomic and
     * changes root stored in IveMapImpl.instance()
     */
    void loadTopology(Element e) {
        LocationInstanceInfo info = new LocationInstanceInfo();
        Element locInstanceElem = getOneChildElement(e, "Location");
        Area root;
        
        if (info.load(locInstanceElem) != null
                && (root = info.instantiate((WayPoint) null)) != null) {
            root.setLocationState(LocationState.ATOMIC);
            IveMapImpl.instance().setRoot(root);
            if (IveApplication.instance().noLod){
                expandFull(root);
            }
        } else {
            Log.severe("Root location not instantiated due the previous errors");
        }
    }
    
    
    /**
     * Load the framework settings that influence simulation
     * Currently XML supports simulation speed and cleanup settings
     */
    void loadSimulationSettings(Element e) {
        Scheduler s = SchedulerImpl.instance();
        Element speedElement = getOneChildElement(e, "Speed");
        
        if (speedElement != null) {
            s.setTimeRatio(
                    Double.parseDouble(speedElement.getAttribute("speedRatio")));
        }
        
        Element cleanUpElement = getOneChildElement(e, "CleanUp");
        
        if (cleanUpElement != null) {
            boolean loadTriggered = readBoolean(cleanUpElement, "loadTriggered");
            
            s.getStatistics().enableCleanup(loadTriggered);
            if (loadTriggered) {
                int threshold = Integer.parseInt(
                        cleanUpElement.getAttribute("threshold"));
                int window = Integer.parseInt(
                        cleanUpElement.getAttribute("windowSize"));
                int minimalGap = Integer.parseInt(
                        cleanUpElement.getAttribute("minimalLoop"));
                
                s.getStatistics().resetStatistics(threshold, window, minimalGap);
            }
        }
    }
    
    /**
     * Create DOM representation of given XML document
     * @param fileName file to load
     */
    Document createDocument(String fileName) {
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        try {
            // factory.setXIncludeAware(true);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(
                    ClassLoader.getSystemResource(
                    "cz/ive/resources/xml/IveWorld.xsd"));
            
            assert(schema != null);
            factory.setSchema(schema);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            document = builder.parse(fileName);
            
        } catch (SAXParseException spe) {
            // Error generated by the parser
            Log.severe(
                    "XMLParsing error:" + ", line " + spe.getLineNumber()
                    + ", uri " + spe.getSystemId() + "   " + spe.getMessage());
            
        } catch (SAXException sxe) {
            // Error generated during parsing
            Exception  x = sxe;
            
            if (sxe.getException() != null) {
                x = sxe.getException();
            }
            IveApplication.printStackTrace(x);
            
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            IveApplication.printStackTrace(pce);
        } catch (IOException ioe) {
            // I/O error
            IveApplication.printStackTrace(ioe);
        }
        return document;
    }
    
    /**
     * Get one childelement of given name
     * @param e	XML element whose childelement we want to obtain
     * @param s	name of desired childelement
     * @return first childelement of given name
     */
    public static Element getOneChildElement(Element e, String s) {
        NodeList nl = e.getChildNodes();
        int c = nl.getLength();
        
        for (int i = 0; i < c; i++) {
            Node n = nl.item(i);
            
            if (n.getNodeType() == n.ELEMENT_NODE) {
                Element e2 = (Element) nl.item(i);
                
                if (s.equals("*") || e2.getTagName().equals(s)) {
                    return e2;
                }
            }
        }
        return null;
    }
    
    /**
     * Get all child elements of given name
     * @param e	XML element whose childelements we want to obtain
     * @param s	name of desired childelements
     * @return list of childelements of given name
     */
    public static List<Element> getChildElements(Element e, String s) {
        NodeList nl = e.getChildNodes();
        
        List<Element> elements = new LinkedList<Element>();
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            
            if (n.getNodeType() == n.ELEMENT_NODE
                    && (s.equals("*") || ((Element) n).getTagName().equals(s))
                    ) {
                elements.add((Element) n);
            }
            
        }
        return elements;
    }
    
    /**
     * Return boolean value of given attribute
     * If there isn't such attribute in element defValue is returned.
     * @param e	XML element whose attribute we want to parse
     * @param s attribute name
     */
    public static boolean readBoolean(Element e, String s) {
        String val = e.getAttribute(s);
        
        if (val == null) {
            return false;
        }
        return Boolean.parseBoolean(val);
    }
    
    protected void expandFull(Area top){
        LinkedList<Area> l = new LinkedList<Area>();
        l.addLast(top);
        Area a;
        
        while (!l.isEmpty()){
            a = l.poll();
            try{
                a.expand();
            }
            catch (NoObjectPlaceException ex){
                ex.log();
            }
            for (WayPoint ch : a.getWayPoints()){
                if (ch instanceof Area) l.addLast((Area)ch);
            }
        }
    }
}

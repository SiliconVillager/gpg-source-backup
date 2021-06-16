/* 
 *
 * IVE Editor 
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
 
package IVE_Editor.PlugIns.Components.XMLSaver;

import IVE_Editor.Debug.XMLChecker;
import IVE_Editor.PlugIns.*;
import IVE_Editor.PlugIns.Components.Project.ProjectService;
import IVE_Editor.PlugIns.Moduls.MOL.MOLService;
import IVE_Editor.PlugIns.Moduls.MPG.MPGService;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import org.jdom.input.SAXBuilder; 
import org.jdom.output.XMLOutputter;
import org.jdom.*;

/**
 *    
 *
 * Class is used in xml save process
 *
 * @author  Martin Juhasz
 */
public class XMLSaver extends AbstractKomponent implements ActionListener {
    
    // menu and item menu
    private JMenu menu;
    private JMenuItem itemSaveXML;
    
    /** Used to hold reference for services of modul of processes ang goals */
    private MPGService serviceModulMPG;
    /** Used to hold reference to services of modul of objects and location */
    private MOLService serviceModulMOL;     
    /** Used to hold reference to services of project component */
    private ProjectService serviceComponentProject;    
    /** Data structure that keeps elements of plugin MOL */
    private ArrayList <org.jdom.Element> molElements;
    /** Data structure that keeps elements of plugin MPG */
    private ArrayList <org.jdom.Element> mpcElements;     
    /** Data structure that keeps entgenies */
    private Map<String,org.jdom.Element > entGeniuses;     
    /** Data structures that keeps locationgenies */
    private Map<String,org.jdom.Element> locationGeniuses;            
    /** Map describing which links belong to some object */
    private Map<String, ArrayList<org.jdom.Element> > objectsToLinks;    
    /** List of listeners to komponent XMLSaver. */
    private ArrayList< XMLSaverListener > _listeners;
    
    /**
     * This metod occures when this component is loading into aplication
     */
    public ComponentSettings load()
    {
        serviceModulMPG = (MPGService)pluginsServices().getMPGServices();       
        serviceModulMOL = (MOLService)pluginsServices().getMOLServices();  
        serviceComponentProject = (ProjectService)pluginsServices().getProjectServices();
        _listeners = new ArrayList< XMLSaverListener >();         
               
        ComponentSettings settings = new ComponentSettings();
        
        molElements = new ArrayList<org.jdom.Element>();
        mpcElements = new ArrayList<org.jdom.Element>();
        entGeniuses = new HashMap<String,org.jdom.Element>();
        locationGeniuses = new HashMap<String,org.jdom.Element>();
        objectsToLinks = new HashMap <String, ArrayList<org.jdom.Element> >();
        
        return settings;
    }  
    
  
    
    /**
     * Main method which create coplete xml file
     * @param file Created file describes complete xml world 
     */
    private void createXML(File file) {        
        XMLOutputter outputter = 
            new XMLOutputter( org.jdom.output.Format.getPrettyFormat() );   
        
        Element IveWorld = new Element("IveWorld");                
        
        // Add classpath element
        Element classPath = new Element("classPath");
        ArrayList<String> classPaths = serviceComponentProject.getClassPathList();
        Element ClassPathItem = new Element("ClassPathItem");
        for(int i =0; i < classPaths.size(); i++) {
            Element c = (Element)ClassPathItem.clone();
            c.setAttribute("url",classPaths.get(i));
            classPath.addContent(c);
        }

        //ClassPathItem.setAttribute("url","/bin/"+jmenosveta+".jar");
        //ClassPathItem.setAttribute("url","/bin/demoworld.jar");        
        
        IveWorld.addContent(classPath);        
        
        // Get XML output from modul of processes and goals
        mpcElements = serviceModulMPG.getXML();
                
        // Add Process Templates
        for(int i = 0; i < mpcElements.size(); i++) {
            IveWorld.addContent(mpcElements.get(i));
        }
        mpcElements = null;        
                  
        // Get XML output from MOL
        molElements = serviceModulMOL.getXML();        
        
        for(int i = 0; i < molElements.size(); i++) {
            
            Element e = molElements.get(i);
            
            if (e.getName().equals("objectTemplates")) {
                if(e.getChildren().size()==0)
                    continue;
                
                // Add Object Templates
                // Complete XML subtrees. We must add some xml description for 
                // Ent genies and links for Ents and objects
                // System.out.println("doplneni podstromecku u geniu u enta a linku");
                IveWorld.addContent(createXMLobjectTemplSubtree(e));                               
            } 
            else if (e.getName().equals("locationTemplates")) {
                if(e.getChildren().size()==0)
                    continue;
                // Add Location Templates
                // Complete XML subtrees. We must add some xml description for 
                // genies that belong to some location
                // System.out.println("doplneni podstromecku u geniu u lokaci a linku");
                IveWorld.addContent(createXMLlocationTemplSubtree(e));

            }
            else if (e.getName().equals("rootLocation")) {
                if(e.getChildren().size()==0)
                    continue;
                // Add root Location
                //  System.out.println("doplnuji genies do root lokace");                
                IveWorld.addContent(createRootLocation(e));                            
            }
            else if(e.getName().equals("graphicTemplates")) {
                if(e.getChildren().size() == 0)
                    continue;
                IveWorld.addContent((Element)e.clone());
            }
            else IveWorld.addContent((Element)e.clone());
        }
        molElements = null;

        String path = serviceComponentProject.getProjectPath();
        if(file.getParentFile().getName().equals("Saved")) {
            File checkE = new File(path+File.separator+"Saved");
            if(!checkE.exists())
                checkE.mkdirs();        
        } else if(file.getParentFile().getName().equals("Complete")) {
            File checkE = new File(path+File.separator+"Complete");
            if(!checkE.exists())
                checkE.mkdirs();        
        }
        
        // Create complete file with xml world
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));        
            bw.write(outputter.outputString( IveWorld ));                    
            bw.close();
        } catch(IOException ex) {
            ex.printStackTrace(); 
        } 
        IveWorld.removeContent();
        IveWorld = null;                
    }
    
    /** 
     * Add genies xml destricption in all Ents 
     * @param objectTemplates Element desdribes objectTemplates
     * @return Element describes objectElements
     */
    private org.jdom.Element createXMLobjectTemplSubtree(
            org.jdom.Element objectTemplates) {
                         
        Element newobjectTemplates = new Element("objectTemplates");        
        List<org.jdom.Element> ch = objectTemplates.getChildren();
                
        for(int i = 0; i < ch.size(); i++) {
            
            Element template = ch.get(i);      
            Element newtemplate = (Element)template.clone();
                   
            if(template.getName().equals("EntTemplate")) {
                
                Element newtemplate2 = (Element)newtemplate.clone();
                newtemplate2.removeContent();                
                List<org.jdom.Element> children = template.getChildren();
                
                boolean nogenius = false;
                
                for(int j = 0; j < children.size(); j++) {
                    
                    Element child = children.get(j);
                    Element clonech = (Element)child.clone();
                    if(child.getName().equals("Genius")) {                        
                        String genId = child.getAttributeValue("geniusId");
                        if(genId.equals("none")) {
                            nogenius = true;
                        }
                        Element newchild = entGeniuses.get(genId);
                        Element newchildc = (Element)newchild;
                        if(newchildc!=null) {
                            newtemplate2.addContent((Element)newchild.clone());
                            nogenius = false; // genius Element was added
                        } 
                        else nogenius = true;                        
                    }
                    else
                        newtemplate2.addContent(clonech);
                }
                if(nogenius)
                    newtemplate2.addContent(new Element("Genius"));
                
                // add links
                String name = template.getAttributeValue("name");
                if(objectsToLinks.containsKey(name)) {
                    Element links = new Element("links");
                    ArrayList<Element> linksList = objectsToLinks.get(name);
                    for(int ii =0; ii < linksList.size(); ii++) {
                        links.addContent((Element)linksList.get(ii).clone());
                    }
                    newtemplate2.addContent(links);
                }                
                newobjectTemplates.addContent(newtemplate2);
            }
            else  {                             
                Element newtemplate2 = (Element)newtemplate.clone();
                String name2 = template.getAttributeValue("name");                
                                
                if(objectsToLinks.containsKey(name2)) {
                    newtemplate2.removeContent();                
                    List<org.jdom.Element> children = template.getChildren();                                                                    
                    
                    for(int j = 0; j < children.size(); j++) {                    
                        Element child = children.get(j);
                        Element clonech = (Element)child.clone();                                   
                        newtemplate2.addContent(clonech);                        
                    }
                    //addlinks
                    Element links = new Element("links");
                    ArrayList<Element> linksList = objectsToLinks.get(name2);
                    for(int ii =0; ii < linksList.size(); ii++) {
                        links.addContent((Element)linksList.get(ii).clone());                                                
                    }                    
                    newtemplate2.addContent(links);                                        
                    newobjectTemplates.addContent(newtemplate2);                    
                }
                else {                                                                           
                    // this object don't have a links
                    newobjectTemplates.addContent(newtemplate2);
                }
            }                        
        }
                            
        return newobjectTemplates;
    }
    
    /** 
     * Add XML description of genies in all locations 
     * @param locationTemplates Element describes locationTemplates
     * @return Element describes locationTemplates
     */
    private org.jdom.Element createXMLlocationTemplSubtree(
            org.jdom.Element locationTemplates) {                
        
        Element newlocationTemplates = new Element("locationTemplates");
        
        List<org.jdom.Element> ch = locationTemplates.getChildren();
        
        for(int i = 0; i < ch.size(); i++) {
            
            Element template = ch.get(i);      
            Element newtemplate = (Element)template.clone();
                   
            if(template.getName().equals("GraphLocation")) {
                
                Element newtemplate2 = (Element)newtemplate.clone();
                newtemplate2.removeContent();                
                List<org.jdom.Element> children = template.getChildren();
                
                for(int j = 0; j < children.size(); j++) {
                    
                    Element child = children.get(j);
                    Element clonech = (Element)child.clone();
                    if(child.getName().equals("subLocations")) {
                        
                        Element subLocations = new Element("subLocations");
                        
                        // Through all locations find all genies 
                        List subloc = child.getChildren();
                        
                        for(int k = 0; k < subloc.size(); k++) {
                            
                            Element location = (Element)subloc.get(k);
                            Element newlocation = (Element)location.clone();
                            newlocation.removeContent();
                            List locationChilds = location.getChildren();
                            
                            for(int l = 0; l < locationChilds.size(); l++) {
                                
                                Element locChild = (Element)locationChilds.get(l);
                                Element clonelCh = (Element)locChild.clone();
                                
                                if (locChild.getName().equals("genies")) {                                    
                                    Element genies = new Element("genies");
                                    List geniesChilds = locChild.getChildren();
                                    
                                    for(int m = 0; m < geniesChilds.size(); m++) {
                                        
                                        Element genius = (Element)geniesChilds.get(m);                                        
                                        
                                        String genId = genius.getAttributeValue("geniusId");        
                                        // Doplnuji genia " + genId;
                                        // Add genie
                                        Element locGen = (Element)locationGeniuses.get(genId);
                                                                                  
                                        if(locGen!= null) { // if genius exist
                                            Element clocGen = (Element)locGen.clone();                                        
                                            genies.addContent(clocGen);                                         
                                        }                                       
                                    }                                    
                                    // Add genius to location
                                    newlocation.addContent(genies);                                   
                                }
                                else
                                    newlocation.addContent(clonelCh);
                            }                            
                            // correct location added to sublocation 
                            // nove upravena lokace zasunuta do sublokace
                            subLocations.addContent(newlocation);                            
                        }
                         
                        // do GraphLokace pridan subLocations
                        // subLocation is added to GraphLocation
                        newtemplate2.addContent(subLocations);
                        
                    }
                    else // we add all to GraphLocation
                        newtemplate2.addContent(clonech);
                }
                
                // do locationTemplates pridam upravenou GraphLokaci
                // GraphLocation is added to locationTemplates
                newlocationTemplates.addContent(newtemplate2);
            }
            else // GridLocation is added to locationTemplates
                newlocationTemplates.addContent(newtemplate);                       
        }
                            
        return newlocationTemplates;
    }
    
    /** 
     * Add XML description of genies to rootLocation
     *
     * @param rootLocation Element describes rootLocation
     * @return Element describes rootLocation
     */
    private org.jdom.Element createRootLocation(
            org.jdom.Element rootLocation) {
        Element newrootLocation = (Element)rootLocation.clone();
        newrootLocation.removeContent();
        Element loc = (Element)rootLocation.getChild("Location").clone();
        List<Element> chld = rootLocation.getChild("Location").getChildren();
        loc.removeContent();
        
        for(int i = 0 ; i < chld.size(); i++) {            
            if( chld.get(i).getName().equals("genies") ) {
                Element genies = new Element("genies");
                List<Element> listgen = chld.get(i).getChildren();
                if(listgen!=null) {
                    for(int j = 0; j < listgen.size(); j++) {
                        String genId = listgen.get(j).getAttributeValue("geniusId");                                
                        Element locGen = (Element)locationGeniuses.get(genId);
                        if(locGen != null) { // if genius exists
                            Element clocGen = (Element)locGen.clone();                            
                            genies.addContent(clocGen); 
                        }                        
                    }
                    // Add genius to rootLocation
                    loc.addContent((Element)genies.clone());
                }                
            }
            else loc.addContent((Element)chld.get(i).clone());
        }
        // Add location to rootLocation
        newrootLocation.addContent((Element)loc.clone());
        return newrootLocation;
    }
    
    /** Set links for locations in MOL */
    private void setLinks() {
        serviceModulMPG.setLinks();
    }    
    
    /**
     * Set data structure that keeps entgenies 
     */
    public void getEntGeniuses() {
        entGeniuses = serviceModulMPG.getEntGeniuses();
    }
    
    /**
     *  Set data structure that keeps locationgenies 
     */    
    public void getLocationGeniuses() {
        locationGeniuses = serviceModulMPG.getLocationGeniuses();
    }    

    /** 
     * Set map describing which links belong to some object 
     */
    public void getObjectsToLinks() {
        objectsToLinks = serviceModulMPG.getObjectsToLinks();
    }        
    
    /** @return Services of this component */
    public XMLSaverService getService() {
        return new XMLSaverService(this);
    }
    
    /** Add listener to this component */
    public void addListener(XMLSaverListener listener)
    {
        _listeners.add( listener );
    }

    public void actionPerformed (ActionEvent e) {
       
    }
    
    /**
     * Saves xml file. Create new xml file. 
     * @param file XML file for new world
     **/
    public void saveXML(File file) {
        // fill genies and objectToLink structures first
        getEntGeniuses();                
        getLocationGeniuses();               
        getObjectsToLinks();                
        // set Links for locations in MOL
        setLinks();
        // create xml file
        createXML(file);                               
    }
       
}
 
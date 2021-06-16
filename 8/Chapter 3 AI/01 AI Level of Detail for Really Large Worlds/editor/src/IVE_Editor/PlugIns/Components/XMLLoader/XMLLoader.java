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
 
package IVE_Editor.PlugIns.Components.XMLLoader;

import IVE_Editor.Debug.XMLChecker;
import IVE_Editor.PlugIns.*; 
import IVE_Editor.PlugIns.Components.Project.ProjectService;
import IVE_Editor.PlugIns.Moduls.MOL.MOLService;
import IVE_Editor.PlugIns.Moduls.MPG.EntGenius.EntGenius;
import IVE_Editor.PlugIns.Moduls.MPG.EntGenius.EntGeniusDB;
import IVE_Editor.PlugIns.Moduls.MPG.EntGenius.EntTopLevelGoal;
import IVE_Editor.PlugIns.Moduls.MPG.MPGService;
import java.util.StringTokenizer;


import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
 
import org.jdom.output.XMLOutputter;
import org.jdom.*;

import org.jdom.input.*;
import org.jdom.output.*;

import java.io.*; 

/**
 *
 * Class is used for loading xml file
 *
 * @author  Juhász Martin*
 */
public class XMLLoader extends AbstractKomponent implements ActionListener {
    
    // menu and menu items
    private JMenu menu;
    private JMenuItem itemLoadXML;
       
    /** Used to hold reference for services of modul of processes ang goals */
    private MPGService serviceModulMPG;
    /** Used to hold reference to services of modul of objects and location */
    private MOLService serviceModulMOL;     
    /** Used to hold reference to services of project component */
    private ProjectService serviceComponentProjekt;    
    /** This Map is used for mapping ent to genius. It says which genius 
     * belong to Ent */
    private Map<String, String> entToGenius;
    /** List of new genies for ent */
    private ArrayList<EntGenius> newEntGenies;    
    /** List of all process templates */
    private List<Element> listPT;    
    /** List of all graphics templates */
    private List<Element> listGT;        
    /** List of all location templates */
    private List<Element> listLT;        
    /** List of all object templates */
    private List<Element> listOT;        
    /** List of all utterances */
    private List<Element> listUT;    
    /** List of all simulation settings elements */
    private List<Element> listSS;    
    /** List of all root locations */
    private List<Element> listRT;     
    /** List of all class paths */
    private List<Element> listCP;    
    /** File from witch is xml loading */
    private File loadingFile;    
    
    /* 
     * Element represents process templates, graphics teplates, loc templates
     * object templates, utterances, simulation settings, root location, classpath 
     */
    
    /** Represents processTemplates element */
    private Element pt;    
    /** Represents graphicTemplates element*/    
    private Element gt;  
    /** Represents locationTemplats element */    
    private Element lt;    
    /** Represents objectTemplates element*/    
    private Element ot;   
    /** Represents utterance element */    
    private Element ut;    
    /** Represents SimulationSettings element */    
    private Element ss;    
    /** Represents rootLocation element */    
    private Element rt;    
    /** Represents classPath element */    
    private Element cp;
    /** Reference to saxbuilder */
    private SAXBuilder builder; 
    /** Reference to document */
    private Document doc;
    /** Reference to xmlchecker */
    private XMLChecker xmlcheck;

    /**
     * This metod occures when this component is loading into aplication
     */
    public ComponentSettings load()
    {
        serviceModulMPG = (MPGService)pluginsServices().getMPGServices();   
        serviceModulMOL = (MOLService)pluginsServices().getMOLServices();   
        serviceComponentProjekt = (ProjectService)pluginsServices().getProjectServices();
                                
        entToGenius = new HashMap<String, String>(); 
        newEntGenies = new ArrayList();
        builder = new SAXBuilder();    
        doc = new Document();
        xmlcheck = new XMLChecker("");            
        
        listPT = new ArrayList<Element>();
        listGT = new ArrayList<Element>();        
        listOT = new ArrayList<Element>();        
        listLT = new ArrayList<Element>();        
        listUT = new ArrayList<Element>();      
        listSS = new ArrayList<Element>();
        listRT = new ArrayList<Element>();        
        listCP = new ArrayList<Element>();
        
        pt = new Element("processTemplates");
        gt = new Element("graphicTemplates");
        ot = new Element("objectTemplates");
        lt = new Element("locationTemplates");
        ut = new Element("utterances");
        ss = new Element("SimulationSettings");
        rt = new Element("rootLocation");
        cp = new Element("classPath");
                     
        ComponentSettings settings = new ComponentSettings();               
        
        return settings;
    }  
    
    public void actionPerformed (ActionEvent e) {                                                                     
    }
    
    /**
     * Load xml file, check validity, load include xml files and
     * send parts of xml tree to right moduls or components 
     * ( call their services )
     *
     * @param file is xml file which represents loading world
     */
    public boolean loadXML(File file) {
        loadingFile = file;
        // map Ent-Genius must be filled first
        setEntToGenius();
        
        builder = new SAXBuilder();
        doc = new Document();
        File schemaFile = new File("../default/XML/IveWorld.xsd");
        try {
            schemaFile = schemaFile.getCanonicalFile();
        } catch (IOException ex) {
            
        }
        if(!schemaFile.exists()) {
            JOptionPane.showMessageDialog(null,"Missing file "+schemaFile.getPath(),
                "Missing file", JOptionPane.WARNING_MESSAGE);  
         return false;
        }
        
        xmlcheck = new XMLChecker(schemaFile.toString());
            
        try {
             doc = builder.build(file);      
             if(!xmlcheck.checkValidity(doc)) {                
                JOptionPane.showMessageDialog(null,"XML file " + file.getPath() +
                        " is not valid against IveWorld.xsd",
                    "XML file is not valid",JOptionPane.WARNING_MESSAGE);
                return false;
             }
             if(doc.getRootElement() == null) {                 
                 return false;
             }
        }
        catch (IOException evt) {             
            return false;            
        }
        catch (JDOMException evt) { 
            JOptionPane.showMessageDialog(null,"XML file " + file.getPath() +
                        " is not valid!",
                    "XML file is not valid",JOptionPane.WARNING_MESSAGE);
                return false;
        }
        
        emptyAll();
        
        Element root = doc.getRootElement();               
        
        // load all included xml files
        if(!loadIncluded(root)) {           
                return false;            
        }

        for(int i =0; i < listPT.size(); i++) {                        
            pt.addContent((Element)listPT.get(i).clone());
        }        

        for(int i =0; i < listUT.size(); i++) {
          ut.addContent((Element)listUT.get(i).clone());
        }        
        
        for(int i =0; i < listSS.size(); i++) {
          ss.addContent((Element)listSS.get(i).clone());
        }                
        
        for(int i =0; i < listRT.size(); i++) {
          rt.addContent((Element)listRT.get(i).clone());
        }                
        
        for(int i =0; i < listGT.size(); i++) {
          gt.addContent((Element)listGT.get(i).clone());
        }                
        
        for(int i =0; i < listOT.size(); i++) {
          ot.addContent((Element)listOT.get(i).clone());
        }                
        
        for(int i =0; i < listLT.size(); i++) {
          lt.addContent((Element)listLT.get(i).clone());
        }                        
        
        for(int i =0; i < listCP.size(); i++) {
          cp.addContent((Element)listCP.get(i).clone());
        }                                                       
        
        if (pt != null) {
            List<Element> listpt = new ArrayList<Element>();
            listpt = pt.getChildren();
            serviceModulMPG.fillInProcesses( listpt );
            listpt = null;
        }        
        
        if (ut != null) {
            List<Element> listut = new ArrayList<Element>();
            listut = ut.getChildren();        
            serviceModulMPG.fillInUtterances(listut);
            listut = null;
        }
        
        if (ss != null) {
            List<Element> listss = new ArrayList<Element>();
            listss = ss.getChildren();        
            serviceModulMPG.fillInSS(listss);
            listss = null;
        }
        
        if (rt != null)    {    
            List<Element> listrl = new ArrayList<Element>();
            listrl = rt.getChildren();     
            if(listrl!= null && listrl.size() > 0) {
                serviceModulMPG.fillInLinks(listrl);
                serviceModulMPG.fillInRootLocationGenies(listrl);
                serviceModulMPG.fillInRootLocation(rt.getChild("Location"));
                serviceModulMOL.fillInRootLocation(listrl);
            }
            listrl = null;
        }                         
        
        if (gt != null)    {    
            List<Element> listgt = new ArrayList<Element>();
            listgt = gt.getChildren();        
            serviceModulMOL.fillInGraphics(listgt);
            listgt = null;
        }
                
        if (ot != null)    {    
            List<Element> listot = new ArrayList<Element>();
            listot = ot.getChildren();        
            // create new ent genies from loading xml file if they dont exist in tmp
            setEntGeniuses(listot);
            // MPG need create links object-process
            // MOL need list of object elements
            serviceModulMPG.fillInLinks(listot);
            serviceModulMOL.fillInObjects(listot);
            
            listot = null;
        }        
       
        if (lt != null)    {    
            List<Element> listlt = new ArrayList<Element>();
            listlt = lt.getChildren();
            serviceModulMPG.fillInLinksFromLocations(listlt);
            serviceModulMPG.fillInLocationGenies(lt.getChildren("GraphLocation"));
            serviceModulMOL.fillInLocations(listlt);
            //serviceModulMPG.fillInLocationGeniuses(lt.getChildren("GraphLocation"));
            //
            listlt = null;
        }        
        
        if (cp != null)    {    
            List<Element> listcp = new ArrayList<Element>();
            listcp = cp.getChildren();         
            serviceComponentProjekt.fillInClassPathItems(listcp);
            listcp = null;
        }    
        
        root = null;
        pt.removeContent();
        ut.removeContent();
        ot.removeContent();
        gt.removeContent();
        lt.removeContent();
        rt.removeContent();
        cp.removeContent();
        
        return true;                
    }
         
    /** @return XMLLoader service */
    public XMLLoaderService getService() {
        return new XMLLoaderService(this); 
    }  
    
    /**
     * Fill map representing what genies belong to some Ent
     */
    private void setEntToGenius() {
        
        File f = serviceComponentProjekt.getMpgEntToGeniusFile();
        StringBuffer buf = new StringBuffer();
        if(f==null || !f.exists()) {            
            return;
        }
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while((line = br.readLine()) != null) {
                buf.append(line);
            }            
                    
        } catch(IOException e) {
            //e.printStackTrace();
        }
        
        StringTokenizer st = new StringTokenizer(buf.toString(),";");
        while(st.hasMoreTokens()) {
            StringTokenizer st2 = new StringTokenizer((String)st.nextToken(),"=");
            String ent ="";
            String gen ="";
            if(st2.hasMoreTokens())
                ent = (String)st2.nextToken();
            if(st2.hasMoreTokens())            
                gen = (String)st2.nextToken();
            if(!ent.equals("") && !gen.equals(""))
                entToGenius.put(ent,gen);            
            
        }                
    }

    /** create new Ent genius if they don't exist in temp file in MPG
     * add them to Ent Genius DB
     */
    private void setEntGeniuses(List listot) {                        
        for(int i =0; i < listot.size(); i++) {            
            Element e = (Element)listot.get(i);            
            if (e.getName().equals("EntTemplate")) {
     
                String name = e.getAttributeValue("name");
  
                List<Element> children = e.getChildren();
                for(int j = 0; j < children.size(); j++) {
                    Element ch = (Element)children.get(j);
                    if (ch.getName().equals("Genius")) {
                        //Element g = new Element("Genius");
                        String genId = entToGenius.get(name);
                        if(genId ==null)                            
                        {
                            //new geniusId for MOL                            
                            //new EntGenius -> read his TLG                            
                            List<Element> tlg = ch.getChildren();
                            //EntGenius neweg = EntGenius();
                            EntGenius gen = new EntGenius();
                            gen.setEntGeniusID(name+"genius");                            
                            gen.addEnt(name);
                            if(tlg!=null && tlg.size()>0) { // if genius has some TLG
                                for(int k =0; k < tlg.size(); k ++) {
                                    EntTopLevelGoal etlg = new EntTopLevelGoal();
                                    etlg.setGoalId(tlg.get(k).
                                            getAttributeValue("goalId"));
                                    gen.addTLG(etlg);
                                }
                                EntGeniusDB.addEntGenius(gen);
                            }
                        }                        
                    }                    
                }                                                
            }            
        }                
    }  

    /**
     * Return Map represents which genius belong to ent 
     *
     * @return Map represents which genius belong to ent 
     */
    public Map<String, String> getEntToGenius() {
        return entToGenius;
    }
    
    /** Loads included XML files */
    private boolean loadIncluded(Element root) {
        List<Element> include = root.getChildren("include");
        for(int i =0; i < include.size(); i++) {
            Attribute urlAttr = include.get(i).getAttribute("url");
            if(urlAttr==null)
                continue;
            String includeFile = urlAttr.getValue();
            String file = loadingFile.getParent() + File.separator + includeFile;
            File newfile = new File(file);
            if(newfile.exists()) {    
                try {
                    doc = builder.build(newfile);      
                } catch (JDOMException ex) {
                    return false;
                } catch (IOException ex) {
                    return false;
                }      
                if(!xmlcheck.checkValidity(doc)) {                                   
                     JOptionPane.showMessageDialog(null,"Included XML file " 
                             + newfile.getPath() +
                        " is not valid against IveWorld.xsd",
                    "XML file is not valid",JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                loadIncludedFile(new File(file));
            }            
        }
        
        Element pt = root.getChild("processTemplates");
        if (pt != null) {
            List<Element> listpt = new ArrayList<Element>();
            listpt = pt.getChildren();
            listPT.addAll(listpt);            
        }
        Element ut = root.getChild("utterances");
        if (ut != null) {
            List<Element> listut = new ArrayList<Element>();
            listut = ut.getChildren();        
            listUT.addAll(listut);
        }
        Element ss = root.getChild("SimulationSettings");
        if (ss != null) {
            List<Element> listss = new ArrayList<Element>();
            listss = ss.getChildren();        
            listSS.addAll(listss);
        }
        Element rl = root.getChild("rootLocation");
        if (rl != null)    {    
            List<Element> listrl = new ArrayList<Element>();
            listrl = rl.getChildren();        
            listRT.addAll(listrl);
        }                         
        Element gt = root.getChild("graphicTemplates");
        if (gt != null)    {    
            List<Element> listgt = new ArrayList<Element>();
            listgt = gt.getChildren();        
            listGT.addAll(listgt);
        }
        
        Element ot = root.getChild("objectTemplates");
        if (ot != null)    {    
            List<Element> listot = new ArrayList<Element>();
            listot = ot.getChildren();        
            listOT.addAll(listot);
        }        
        Element lt = root.getChild("locationTemplates");
        if (lt != null)    {    
            List<Element> listlt = new ArrayList<Element>();
            listlt = lt.getChildren();
            listLT.addAll(listlt);
        }           
        Element cp = root.getChild("classPath");
        if (cp != null)    {    
            List<Element> listcp = new ArrayList<Element>();
            listcp = cp.getChildren();                 
            listCP.addAll(listcp);
        }  
        return true;
    }
    
    /** 
     * Loads included file 
     * @param file Included file 
    */
    private void loadIncludedFile(File file) {
        SAXBuilder builder = new SAXBuilder();
        Document doc = new Document();
        try {
             doc = builder.build(file);      
             if(doc.getRootElement() == null) {                 
                 return;
             }
        }
        catch (IOException evt) {             
            return;            
        }
        catch (JDOMException evt) { 
        }
        
        Element roote = doc.getRootElement();               
        
        //inlcude included
        
        List<Element> include = roote.getChildren("include");
        for(int i =0; i < include.size(); i++) {
            String includeFile = include.get(i).getAttributeValue("url");
            String loadfile = file.getParent() + File.separator + includeFile;
            if((new File(loadfile)).exists()) {           
                loadIncludedFile(new File(loadfile));
            }
            else {
               // System.out.println("Can't load " + loadfile);
            }
        }        
        
        Element pt = roote.getChild("processTemplates");
        if (pt != null) {
            List<Element> listpt = new ArrayList<Element>();
            listpt = pt.getChildren();
            listPT.addAll(listpt);            
        }
        Element ut = roote.getChild("utterances");
        if (ut != null) {
            List<Element> listut = new ArrayList<Element>();
            listut = ut.getChildren();        
            listUT.addAll(listut);
        }
        Element ss = roote.getChild("SimulationSettings");
        if (ss != null) {
            List<Element> listss = new ArrayList<Element>();
            listss = ss.getChildren();        
            listSS.addAll(listss);
        }        
        Element rl = roote.getChild("rootLocation");
        if (rl != null)    {    
            List<Element> listrl = new ArrayList<Element>();
            listrl = rl.getChildren();        
            listRT.addAll(listrl);
        }                         
        Element gt = roote.getChild("graphicTemplates");
        if (gt != null)    {    
            List<Element> listgt = new ArrayList<Element>();
            listgt = gt.getChildren();        
            listGT.addAll(listgt);
        }
        
        Element ot = roote.getChild("objectTemplates");
        if (ot != null)    {    
            List<Element> listot = new ArrayList<Element>();
            listot = ot.getChildren();        
            listOT.addAll(listot);
        }        
        Element lt = roote.getChild("locationTemplates");
        if (lt != null)    {    
            List<Element> listlt = new ArrayList<Element>();
            listlt = lt.getChildren();
            listLT.addAll(listlt);
        }        
        Element cp = roote.getChild("classPath");
        if (cp != null)    {    
            List listcp = new ArrayList<Element>();
            listcp = cp.getChildren();             
            listCP.addAll(listcp);
        }                                                                        
    }
    
    /**
     * frees All data structures
     */
    private void emptyAll() {
        entToGenius = new HashMap<String, String>(); 
        newEntGenies = new ArrayList();
        
        listPT = new ArrayList<Element>();
        listGT = new ArrayList<Element>();        
        listOT = new ArrayList<Element>();        
        listLT = new ArrayList<Element>();        
        listUT = new ArrayList<Element>();      
        listSS = new ArrayList<Element>();
        listRT = new ArrayList<Element>();        
        listCP = new ArrayList<Element>();
        
        pt = new Element("processTemplates");
        gt = new Element("graphicTemplates");
        ot = new Element("objectTemplates");
        lt = new Element("locationTemplates");
        ut = new Element("utterances");
        ss = new Element("SimulationSettings");
        rt = new Element("rootLocation");
        cp = new Element("classPath");
                      
    }    
}

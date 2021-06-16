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
 
package IVE_Editor.PlugIns.Moduls.MPG;

import IVE_Editor.Debug.XMLChecker;
import IVE_Editor.GuiServices;
import IVE_Editor.PlugIns.AbstractModul;
import IVE_Editor.PlugIns.Components.Project.Project;
import IVE_Editor.PlugIns.Components.Project.ProjectListener;
import IVE_Editor.PlugIns.Components.Project.ProjectService;
import IVE_Editor.PlugIns.Components.XMLLoader.XMLLoaderService;
import IVE_Editor.PlugIns.ModulSettings;
import IVE_Editor.PlugIns.Moduls.MOL.MOLService;
import IVE_Editor.PlugIns.Moduls.MPG.EntGenius.EntGenius;
import IVE_Editor.PlugIns.Moduls.MPG.EntGenius.EntGeniusDB;
import IVE_Editor.PlugIns.Moduls.MPG.EntGenius.EntTopLevelGoal;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Dialogs.WarningDialog;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Dialogs.ShowXMLDialog;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Genius.EntGeniusEditView;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Genius.LocationGeniusEditView;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.LinkEditorView;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Process.ExpressionView;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Process.InspectorView;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Process.PETreeView;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Process.ProcessViewerView;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Process.SourcesParamView;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.SimulationSettingsEditView;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Tables.MultiLineHeaderRenderer;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Tables.SortedColumnHeaderRenderer;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.UtteranceEditView;
import IVE_Editor.PlugIns.Moduls.MPG.LocationGenius.CleaningGoal;
import IVE_Editor.PlugIns.Moduls.MPG.LocationGenius.CleaningGoalDB;
import IVE_Editor.PlugIns.Moduls.MPG.LocationGenius.LocationGenius;
import IVE_Editor.PlugIns.Moduls.MPG.LocationGenius.LocationGeniusDB;
import IVE_Editor.PlugIns.Moduls.MPG.LocationGenius.LocationTopLevelGoal;
import IVE_Editor.PlugIns.Moduls.MPG.Models.CleanUpModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.EntGenius.EntGeniusModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.EntGenius.EntTLGModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.LocationGenius.CleaningGoalModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.LocationGenius.GeniusCleaningGoalModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.LocationGenius.LocationGeniusModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.LocationGenius.TLGGModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.LocationGenius.TLGPModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.ObjLocModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.Process.ExpressionModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.Process.GoalParameterModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.Process.GoalSourceModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.Process.InspectorModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.Process.ProcessSourceModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.Process.ProcessViewerModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.Process.SortedTableModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.LinkTableModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.SpeedModel;
import IVE_Editor.PlugIns.Moduls.MPG.Models.UtteranceModel;
import IVE_Editor.PlugIns.Moduls.MPG.Process.Expression;
import IVE_Editor.PlugIns.Moduls.MPG.Process.GContext;
import IVE_Editor.PlugIns.Moduls.MPG.Process.GParameter;
import IVE_Editor.PlugIns.Moduls.MPG.Process.GParameters;
import IVE_Editor.PlugIns.Moduls.MPG.Process.GSlot;
import IVE_Editor.PlugIns.Moduls.MPG.Process.GSources;
import IVE_Editor.PlugIns.Moduls.MPG.Process.GTrigger;
import IVE_Editor.PlugIns.Moduls.MPG.Process.Goal;
import IVE_Editor.PlugIns.Moduls.MPG.Process.PContext;
import IVE_Editor.PlugIns.Moduls.MPG.Process.PExpansion;
import IVE_Editor.PlugIns.Moduls.MPG.Process.PSlot;
import IVE_Editor.PlugIns.Moduls.MPG.Process.PSources;
import IVE_Editor.PlugIns.Moduls.MPG.Process.PSuitability;
import IVE_Editor.PlugIns.Moduls.MPG.Process.ProcessDB;
import IVE_Editor.PlugIns.Moduls.MPG.Process.ProcessTempl;
import IVE_Editor.PlugIns.Moduls.MPG.Repository.ExportRepositoryDB;
import IVE_Editor.PlugIns.Moduls.MPG.Repository.ImportRepositoryDB;
import IVE_Editor.PlugIns.Moduls.MPG.Links.Link;
import IVE_Editor.PlugIns.Moduls.MPG.Links.LinkDB;
import IVE_Editor.PlugIns.Moduls.MPG.SimulationSettings.CleanUp;
import IVE_Editor.PlugIns.Moduls.MPG.SimulationSettings.SimulationSettingsDB;
import IVE_Editor.PlugIns.Moduls.MPG.SimulationSettings.SimulationSettingsElement;
import IVE_Editor.PlugIns.Moduls.MPG.SimulationSettings.Speed;
import IVE_Editor.PlugIns.Moduls.MPG.Utterances.Utterance;
import IVE_Editor.PlugIns.Moduls.MPG.Utterances.UtteranceDB;

import java.awt.*; 
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.event.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


/**
 *
 *
 * Main class of plugin processes and goals
 *
 * @author Martin Juhasz
 */
public class MPG extends AbstractModul implements ActionListener, ProjectListener {
    
    private JFrame mainFrame;
        
    // Menu item definitions 
    private JMenu tmpMenu;
    private JMenu jMenuShowWindow;    
    private JCheckBoxMenuItem jCheckBoxMenuItemPETree;
    private JCheckBoxMenuItem jCheckBoxMenuItemImportRepozitory;    
    private JCheckBoxMenuItem jCheckBoxMenuItemExportRepozitory;        
    private JCheckBoxMenuItem jCheckBoxMenuItemHierarchyTree;        
    private JCheckBoxMenuItem jCheckBoxMenuItemRulesEditor;
    private JCheckBoxMenuItem jCheckBoxMenuItemProcessSource;
    private JCheckBoxMenuItem jCheckBoxMenuItemSubGoalSource;
    private JCheckBoxMenuItem jCheckBoxMenuItemSubGoalParameter;
    private JCheckBoxMenuItem jCheckBoxMenuItemProcessViewer;        
    private JCheckBoxMenuItem jCheckBoxMenuItemSubGoalInspector;        
    private JCheckBoxMenuItem jCheckBoxMenuItemProcessInspector;         
    private JCheckBoxMenuItem jCheckBoxMenuItemEntGenius;        
    private JCheckBoxMenuItem jCheckBoxMenuItemLocationGenius;   
    private JMenuItem jMenuItemLinkEdit;
    private JMenuItem jMenuItemUtterancesEdit;
    private JMenuItem jMenuItemSSEdit;
    private JMenuItem jMenuItemEntGeniusEdit;
    private JMenuItem jMenuItemLocationGeniusEdit;
    private JMenuItem jMenuItemShowWarnings;
    private JSeparator sep;
    // End Menu Item definitions
                            
    // Model definitions. We must define all models that are used in all views
    private ExpressionModel emodel;
    private ExpressionModel emodelC;    
    private ProcessViewerModel pvmodel;
    private ProcessSourceModel psmodel;
    private GoalSourceModel gsmodel;
    private GoalParameterModel gpmodel;
    private EntGeniusModel egmodel;
    private EntTLGModel etlgmodel;
    private LocationGeniusModel lgmodel;    
    private GeniusCleaningGoalModel gcgmodel;
    private TLGPModel tlgpmodel;
    private TLGGModel tlggmodel;
    private CleaningGoalModel cgmodel;
    private UtteranceModel umodel;
    private SpeedModel spmodel;
    private CleanUpModel cumodel;    
    private InspectorModel inmodel;
    private LinkTableModel rtm;
    private ObjLocModel olm;
    
    private SortedTableModel stm;
    private MultiLineHeaderRenderer mlhr;
    private SortedColumnHeaderRenderer renderer;        
    // End Model definitons
       
    // View definitions. We must create Views for all models
    private LocationGeniusEditView lgview;    
    private ExpressionView eview;
    private ProcessViewerView pvview;
    private PETreeView petview;
    private EntGeniusEditView egview;
    private LinkEditorView review;
    private UtteranceEditView ueview;
    private SimulationSettingsEditView ssview;
    private WarningDialog wview;    
    private ShowXMLDialog sxview;    
    private SourcesParamView spview;
    private InspectorView inview;
    // End View definitions
    
    /** Map that keeps xml representation for Ent genius */        
    private Map<String,org.jdom.Element> entGeniuses;
    /** Map that keeps xml representation for Location genius */
    private Map<String,org.jdom.Element> locationGeniuses;    
    /** Keeps which genius belong to which ent */
    private Map<String, String> entToGenius;
    /** Keeps which genius belong to which location */
    private Map<String, ArrayList<String> > locationToGenius;
       
    // GUI Variables declaration - do not modify
    private javax.swing.JPanel jPanelPETree;
    private javax.swing.JScrollPane jScrollPaneInspectorRuleNorth;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JSplitPane jSplitPaneOther2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel jPanelOtherSourceParameter;
    private javax.swing.JPanel jPanelOtherProcessView;
    private javax.swing.JScrollPane jScrollPaneInspectorRuleCenter;
    private javax.swing.JPanel jPanelInspectorRule;
    private javax.swing.JPanel jPanelOtherSplit;
    private javax.swing.JSplitPane jSplitPaneOther3;    
    private javax.swing.JSplitPane jSplitPaneOther4;    
    private javax.swing.JPanel jPanelStatusBar;
    // End of gui variables declaration
   
    /** Used to hold reference to services of modul of objects and location */
    private MOLService serviceModulMOL;     
    /** Used to hold reference to services of project component */
    private ProjectService serviceComponentProject;
    /** Used to hold reference to services of GUI in kernel */
    private GuiServices guiServices;
    /** Used to hold reference to services of xml loader component */        
    private XMLLoaderService serviceXMLLoader;            
    /** Keeps xml representation of links for each object */
    private Map<String, ArrayList<org.jdom.Element>> objectsToLinks;
    
    // Helper variables
    private Set objectsSet;
    private String rootLocation;
    
    /**
     * Constructor of MPG
     */
    public MPG( GuiServices guiServices ){
       this.guiServices = guiServices;
    }
    
     /** 
     * This metod occures when loading plugin to aplication
     */ 
    public ModulSettings load()
    {

        mainFrame = guiServices.getMainFrame();
       
        serviceModulMOL = (MOLService)pluginsServices().getMOLServices();  
        serviceComponentProject = (ProjectService)pluginsServices().getProjectServices();
        serviceXMLLoader = (XMLLoaderService)pluginsServices().getXMLLoaderServices();
        //Project projectref = serviceComponentProject.getProject();
            
        // Creating models
        inview = new InspectorView(mainFrame,serviceComponentProject);
        emodel = new ExpressionModel();
        emodelC = new ExpressionModel(); //expression model for cleaning goal in loc gen
        pvmodel = new ProcessViewerModel();
        psmodel = new ProcessSourceModel();
        gsmodel = new GoalSourceModel();                
        gpmodel = new GoalParameterModel();
        egmodel = new EntGeniusModel();
        etlgmodel = new EntTLGModel();
        //etlgmodel.setModel(egmodel); //?
        lgmodel = new LocationGeniusModel();        
        gcgmodel = new GeniusCleaningGoalModel();
        tlgpmodel = new TLGPModel();
        tlggmodel = new TLGGModel();
        cgmodel = new CleaningGoalModel();
        umodel = new UtteranceModel();        
        spmodel = new SpeedModel();
        cumodel = new CleanUpModel();
        rtm = new LinkTableModel();
        olm = new ObjLocModel();
        inmodel = new InspectorModel();
                        
        stm = new SortedTableModel(pvmodel);
        mlhr = new MultiLineHeaderRenderer();
        renderer = new SortedColumnHeaderRenderer(stm, mlhr);  
        // End model creating        
        
        // View creating with their models, setttings models
        eview = new ExpressionView(serviceComponentProject,serviceModulMOL);        
        pvview = new ProcessViewerView(stm,serviceComponentProject);
        pvview.setRenderer(renderer);        
        spview = new SourcesParamView(psmodel,gsmodel,gpmodel,mainFrame);
        petview = new PETreeView(this,serviceComponentProject,pvview,spview,mainFrame);    
        petview.setModel(inview);
        
        review = new LinkEditorView(mainFrame,null,null, rtm,olm, serviceModulMOL, spview);        
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        review.setSize(new java.awt.Dimension(1100, 500));
        review.setLocation((screenSize.width/2 - 550),(screenSize.height/2 - 250));        
        review.setVisible(false);
        
        egview = new EntGeniusEditView(mainFrame,null,null, egmodel, etlgmodel,serviceModulMOL);
        egview.setSize(new java.awt.Dimension(900, 500));
        egview.setLocation((screenSize.width/2 - 450),(screenSize.height/2 - 250));        
        egview.setVisible(false);
         
        lgview = new LocationGeniusEditView(mainFrame,null,null, lgmodel, gcgmodel,
                tlgpmodel, tlggmodel,serviceModulMOL, cgmodel,
                serviceComponentProject);
        lgview.setSize(new java.awt.Dimension(1110, 500));
        lgview.setLocation(screenSize.width/2 - 555,(screenSize.height/2 - 250));        
        lgview.setVisible(false);
        lgview.setModel(emodelC);
        
        ueview = new UtteranceEditView(mainFrame, umodel);
        ueview.setSize(new java.awt.Dimension(500, 500));
        ueview.setLocation((screenSize.width/2 - 250),(screenSize.height/2 - 250));        
        ueview.setVisible(false);  
        
        ssview = new SimulationSettingsEditView(mainFrame, spmodel, cumodel);
        ssview.setSize(new java.awt.Dimension(500, 300));
        ssview.setLocation((screenSize.width/2-250),(screenSize.height/2-150));        
        ssview.setVisible(false);  
        
        wview = new WarningDialog(mainFrame,true);
        wview.setSize(new java.awt.Dimension(800, 500));
        wview.setLocation((screenSize.width/2 - 400),(screenSize.height/2 - 250));        
        wview.setVisible(false);                                               
        
        sxview = new ShowXMLDialog(mainFrame,true);
        sxview.setSize(new java.awt.Dimension(750, 500));
        sxview.setLocation((screenSize.width/2 - 375),(screenSize.height/2 - 250));        
        sxview.setVisible(false);                                               
        
        // End creating views and settings their models
        
        // Settings models in other views, 
        // when one model changed other view has to change too
        eview.setModel(emodel);
        petview.setModel(emodel);
        petview.setModel(inmodel);        
        petview.setModel(psmodel);
        petview.setModel(gsmodel);
        petview.setModel(gpmodel);
        petview.setModel(pvmodel);
        petview.setModel(stm);            
        petview.setRenderer(renderer);
        spview.setModel(inmodel);
        inview.setModel(inmodel);
        inview.setModel(pvmodel);
        inview.setModel(petview);
        inview.setModel(emodel);
        inview.setModel(stm);
        inview.setRenderer(renderer);
                         
        // Helper maps and variables
        entGeniuses = new HashMap<String, org.jdom.Element>();
        locationGeniuses = new HashMap<String, org.jdom.Element>();     
        entToGenius= new HashMap<String, String>();
        locationToGenius = new HashMap<String, ArrayList<String> >();
        objectsSet = new HashSet();
        objectsToLinks = new HashMap();        
        rootLocation = new String("");
        // End helper maps and variables                 
        
        // Add Listener to Project events 
        // Open Project, Close Project, New Project..
        pluginsServices().addListenerToProject(this);                
      
        // GUI variables
        jPanelMain = new javax.swing.JPanel();
        jPanelMain.setEnabled(false);
        jPanelMain.setLayout(new java.awt.BorderLayout());
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane1.setDividerLocation(230);
        jSplitPane1.setResizeWeight(0.25);  
        jSplitPane1.setOneTouchExpandable(true);
        jPanelPETree = new javax.swing.JPanel();
        jPanelPETree.setLayout(new java.awt.BorderLayout());
        jPanelPETree.setMaximumSize(new java.awt.Dimension(100,10));       
        jPanelOtherSplit = new javax.swing.JPanel();
        jSplitPaneOther2 = new javax.swing.JSplitPane();
        jSplitPaneOther3 = new javax.swing.JSplitPane();
        jSplitPaneOther4 = new javax.swing.JSplitPane();
        jSplitPaneOther2.setOneTouchExpandable(true);
        jSplitPaneOther3.setOneTouchExpandable(true);
        jSplitPaneOther4.setOneTouchExpandable(true);
        jPanelOtherProcessView = new javax.swing.JPanel();
        jPanelOtherSourceParameter = new javax.swing.JPanel();
        jPanelInspectorRule = new javax.swing.JPanel();
        jScrollPaneInspectorRuleNorth = new javax.swing.JScrollPane();
        jScrollPaneInspectorRuleCenter = new javax.swing.JScrollPane(); 

        jSplitPane1.setLeftComponent(jPanelPETree);
        jPanelOtherSplit.setLayout(new java.awt.BorderLayout());
        jSplitPaneOther2.setDividerLocation(345);
        jSplitPaneOther2.setResizeWeight(0.2);        
        jSplitPaneOther3.setDividerLocation(350);
        jSplitPaneOther3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneOther3.setResizeWeight(0.5);
        jSplitPaneOther4.setDividerLocation(235);
        jSplitPaneOther4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneOther4.setResizeWeight(0.2);        
        jPanelOtherProcessView.setLayout(new java.awt.BorderLayout());
        jSplitPaneOther3.setLeftComponent(jPanelOtherProcessView);
        jPanelOtherSourceParameter.setLayout(new java.awt.BorderLayout());
        jSplitPaneOther3.setRightComponent(jPanelOtherSourceParameter);
        jSplitPaneOther2.setRightComponent(jSplitPaneOther3);
        jSplitPaneOther4.setLeftComponent(inview);
        jSplitPaneOther4.setRightComponent(eview);
        jPanelInspectorRule.setLayout(new java.awt.BorderLayout());        
        jSplitPaneOther2.setLeftComponent(jSplitPaneOther4);
        jPanelOtherSplit.add(jSplitPaneOther2, java.awt.BorderLayout.CENTER);
        jSplitPane1.setRightComponent(jPanelOtherSplit);
        jPanelMain.add(jSplitPane1, java.awt.BorderLayout.CENTER);                      
        jPanelPETree.add(petview);
        jPanelOtherProcessView.add(pvview);        
        jPanelOtherSourceParameter.add(spview);
        setEnabled(false);
        //End GUI variables
                                                
        tmpMenu = new JMenu("MPG");
        jMenuShowWindow = new JMenu("Show Window");                
        
        // Menu
        jMenuItemLinkEdit = new JMenuItem("Link Editor");
        jMenuItemLinkEdit.addActionListener(new linkEditActionListener());
        
        jMenuItemEntGeniusEdit = new JMenuItem("EntGenius Editor");
        jMenuItemEntGeniusEdit.addActionListener(new entGeniusEditActionListener());
        
        jMenuItemLocationGeniusEdit = new JMenuItem("LocationGenius Editor");
        jMenuItemLocationGeniusEdit.addActionListener(new locationGeniusEditActionListener());        
        
        jMenuItemShowWarnings = new JMenuItem("Show Warnings");
        jMenuItemShowWarnings.addActionListener(new showWarningsActionListener());                
        
        jMenuItemUtterancesEdit = new JMenuItem("Utterances");
        jMenuItemUtterancesEdit.addActionListener(new utterancesEditActionListener());                
        
        jMenuItemSSEdit = new JMenuItem("Simulation settings");
        jMenuItemSSEdit.addActionListener(new ssActionListener());                        
        
        //tmpMenu.add(jMenuShowWindow);
        
        tmpMenu.add(jMenuItemLinkEdit);
        
        tmpMenu.add(jMenuItemEntGeniusEdit);
        
        tmpMenu.add(jMenuItemLocationGeniusEdit);                        
        
        tmpMenu.add(jMenuItemUtterancesEdit);        
        
        tmpMenu.add(jMenuItemSSEdit);       
        
        tmpMenu.add(jMenuItemShowWarnings);        
        
        getMenu().addTemp(tmpMenu);                      
        // End Menu
        
        ModulSettings settings = new ModulSettings( jPanelMain, "MPG");
        
        return settings;
    }  
        
    
    public void actionPerformed( ActionEvent e )
    { 
    }
     
    /**
     * Occures when plugin is activated. Clicked on its tabbed pane
     */
    public void onActivate()
    {                
        getMenu().addTemp(tmpMenu);              
    }
    
    /**
     * Occures when plugin is activated. Clicked on its tabbed pane
     */
    public void onDeactivate()
    {                
        getMenu().removeTemp();                
    }    
    
    
    /** Provide its services to other moduls and components */
    public MPGService getService()
    {
        return new MPGService( this ); 
    }
     
    /**
     * Return xml representation of this plugin.
     * Return <processTemplates> <Utterances> <SimulationSettings>
     */
    public ArrayList<org.jdom.Element> getXML() {
        
        ArrayList<org.jdom.Element> elements = new ArrayList<Element>();
         
        // add <processTemplates>
        ArrayList<ProcessTempl> list = ProcessDB.getList();           
        elements.add(getProcessTemplates(list));                        
        // add <Utterances>
        elements.add(UtteranceDB.getUtterancesElement());
        // add <SimulationSettings>
        elements.add(SimulationSettingsDB.getSSElement());
        
        return elements;
    }
        
    /** 
     * Return element <processTemplates>
     */
    public org.jdom.Element getProcessTemplates(ArrayList<ProcessTempl> list )
    {        
        
        Iterator it = list.iterator(); 
        Element processTemplates = new Element( "processTemplates" ); 
        
        while(it.hasNext()) { 
                       
            Element obj = new Element( "ProcessTemplate" ); 
            ProcessTempl p0 = (ProcessTempl)it.next();        
            obj.setAttribute( "processId", p0.getProcessID() );
            obj.setAttribute( "goalId", p0.getGoalID() );
            obj.setAttribute( "minLod", ""+p0.getMinLod() );
            obj.setAttribute( "maxLod", ""+p0.getMaxLod() );                
            obj.setAttribute( "className", p0.getClassName() );
        
            // Expansion
            Element expansion = new Element("expansion");
            Vector<Goal> v = p0.getPExpansion().getGoalList();
            if(v.size() > 0) {
                Element ontology = new Element("OntologyToken");
                ontology.setAttribute("ontology",new String("jBRP.expansion"));                                
                Iterator iter = v.iterator();
                while(iter.hasNext()) {
                    Element goal = new Element("Goal");
                    Element trigger = new Element("gtrigger");
                    Element gcontext = new Element("gcontext");
                    Element gsources = new Element("sources");
                    Element gparameters = new Element("parameters");
                    Goal g = (Goal)iter.next();
                    goal.setAttribute("goalId",new String(""+g.getGoalID()));
                    // GTrigger
                    Element triggerontology = (g.getGTrigger().getEGTrigger());
                    if(triggerontology != null) {
                        if( !triggerontology.getName().equals("none") ) {
                            Element ctriggerontology = (Element)triggerontology.clone();
                            trigger.addContent(ctriggerontology);
                        }
                    }                                
                    goal.addContent(trigger);                    
                    // GContext
                    GContext gcon = g.getGContext();
                    if(gcon.getSameAsTrigger()) {
                        gcontext.setAttribute("sameAsTrigger",new String("true"));                    
                    } else {
                        Element egcontext = gcon.getEGContext();
                        if(egcontext != null) {
                            Element cegcontext = (Element)egcontext.clone();
                            gcontext.addContent(cegcontext);
                        }                        
                    }
                    goal.addContent(gcontext);                    
                    // GSources
                    List<GSlot> sloty = g.getGSources().getGSources();
                    if(sloty==null || sloty.size()==0) {
                        Element slot = new Element("Slot");
                        slot.setAttribute("name","defaultSlot");
                        gsources.addContent(slot);
                    }
                    Iterator slotiter = sloty.iterator();
                    while(slotiter.hasNext()) {
                        Element slot = new Element("Slot");
                        GSlot gslot = (GSlot)slotiter.next();
                        String jmenoSlotu = gslot.getName();
                        String actor = new String(""+gslot.getActor());                        
                        String valueFrom = new String(""+gslot.getValueFrom());                
                        slot.setAttribute("name",jmenoSlotu);
                        if(!actor.equals(""))
                            slot.setAttribute("actor",actor);                
                        slot.setAttribute("valueFrom",valueFrom);                                
                        gsources.addContent(slot);                        
                    }
                    goal.addContent(gsources);                    
                    // GParameters
                    List<GParameter> parametry = g.getGParameters().getGParametersList();
                    for(int k = 0;  k < parametry.size(); k++) {
                        GParameter gpr = parametry.get(k);
                        Element value = new Element("Value");
                        Element otoken = new Element("OntologyToken");
                        Element eparam = new Element("Parameter");
                        value.setAttribute("value",gpr.getValue());
                        otoken.setAttribute("ontology", gpr.getOntologyToken());
                        eparam.setAttribute("name",gpr.getName());
                        otoken.addContent(value);
                        eparam.addContent(otoken);
                        gparameters.addContent(eparam);
                        
                    }
                    goal.addContent(gparameters);                    
                    ontology.addContent(goal);
                }                
                expansion.addContent(ontology);                                
            }                        
            obj.addContent(expansion);            
            // PSources
            Element sources = new Element("sources");            
            ArrayList<PSlot> sloty = p0.getPSources().getPSourcesList();
            if(sloty==null || sloty.size()==0) {
                Element slot = new Element("Slot");
                slot.setAttribute("name","defaultSlot");
                sources.addContent(slot);
            }
            Iterator slotiter = sloty.iterator();
            while(slotiter.hasNext()) {
                Element slot = new Element("Slot");
                PSlot pslot = (PSlot)slotiter.next();
                String jmenoSlotu = pslot.getName();
                String actor = new String(""+pslot.getActor());
                String optional = new String(""+pslot.getOptional());
                String variable = new String(""+pslot.getVariable());       
                
                slot.setAttribute("name",jmenoSlotu);
                if(!actor.equals(""))
                    slot.setAttribute("actor",actor);                
                if(!optional.equals(""))
                    slot.setAttribute("optional",optional);                
                if(!variable.equals(""))
                    slot.setAttribute("variable",variable);                                
                
                sources.addContent(slot);
               
            }
            // Suitability
            Element suitability = new Element("suitability");
            Element sOntology = p0.getPSuitability().getEPSuitability();
            if(sOntology != null) {
                if( !sOntology .getName().equals("none") ) {                
                    Element csOntology = (Element)sOntology.clone();
                    suitability.addContent(csOntology);
                }
            }            
            // PContext
            Element pcontext = new Element("pcontext");        
            Element cOntology = p0.getPContext().getEPContext();
            if(cOntology != null && (!cOntology.getName().equals("none") )) {                
                    Element ccOntology = (Element)cOntology.clone();
                    pcontext.addContent(ccOntology);                
            }                    
            else
                pcontext.setAttribute("sameAsSuitability",new String("true"));
            
            obj.addContent(sources);        
            obj.addContent(suitability);        
            obj.addContent(pcontext);        
        
            // Add created template
            processTemplates.addContent(obj);
        }
                                      
        return processTemplates;
    }    
    
    /**
     * Return map that keeps ent genius and its xml representation of top level goals 
     */
    public Map<String, org.jdom.Element > getEntGeniuses() {
        
        //Map<String, org.jdom.Element > entToGenius = new HashMap();
        entGeniuses = new HashMap<String,org.jdom.Element>();
                
        ArrayList<EntGenius> genList = EntGeniusDB.getEntGeniusList();
        for(int i =0; i < genList.size(); i++) {
            ArrayList<org.jdom.Element> tlglist = genList.get(i).getTLGElement();
            if(tlglist!=null && tlglist.size() > 0) {
                Element genE = new Element("Genius");
                for(int j = 0; j < tlglist.size(); j++) {
                    genE.addContent((Element)tlglist.get(j).clone());
                }
                entGeniuses.put(genList.get(i).getEntGeniusID(), genE );
            }            
        }                        
        return entGeniuses;        
    }
    
    /** 
     * Return map that represents locationgenius and xml representations of its genies 
     */
    public Map<String, org.jdom.Element> getLocationGeniuses() {
          
        locationGeniuses.clear();
        ArrayList <LocationGenius> locGeniusList = LocationGeniusDB.getLocationGeniusList();
        
        for(int i = 0; i < locGeniusList.size(); i++) {
            LocationGenius lg = locGeniusList.get(i);            
            String id = lg.getLocationGeniusID();
            String className = lg.getClassName();
            ArrayList <LocationTopLevelGoal> locTLGList = lg.getLocationtlglist();
            ArrayList <CleaningGoal> locCleaningList = lg.getCleaningglist();
            if(locTLGList==null || locTLGList.size() == 0)
                continue; // we don't add genius with none TLG, it would create not valid xml'
            Element genius = new Element("Genius");
            
            genius.setAttribute("className", className==null ? "" : className);
            genius.setAttribute("geniusId", id==null?"":id);
            Element tlg = new Element("TopLevelGoal");               
            Element cg = new Element("CleaningGoal");               
            for(int j = 0; j < locTLGList.size(); j++) {                                
                Element tlgc = (Element)tlg.clone();
                tlgc.setAttribute("process",locTLGList.get(j).getProcess());                                
                tlgc.setAttribute("goal",locTLGList.get(j).getGoal());                                                
                genius.addContent(tlgc);                
            } 
            for(int j = 0; j < locCleaningList.size(); j++) {                                
                Element cgc = (Element)cg.clone();                
                cgc.setAttribute("goalId",locCleaningList.get(j).getGoalID());   
                Element expr = locCleaningList.get(j).getExpression().getEExpression();
                Element exprc = (Element)expr.clone();
                cgc.addContent(exprc);      
                //Element cgcc = (Element)cgc.clone();
                genius.addContent(cgc);                  
            }                        
            locationGeniuses.put(id,genius);
        }                       
       
        return locationGeniuses;
    }  
    
    /** 
     * Return map that keeps objects in string representation. For each
     * object exists its list of links in xml.
     */
    public Map<String, ArrayList<org.jdom.Element> > getObjectsToLinks() {
              
        // Create set of objects from LinkDB
        objectsToLinks = new HashMap();
        objectsSet = new HashSet();
        ArrayList<Link> linkList = LinkDB.getLinkList();        
        for(int i = 0; i < linkList.size(); i++) {
            ArrayList objectList = linkList.get(i).getObjects();
            if(objectList!=null)
                for(int j = 0; j < objectList.size(); j++) {
                    objectsSet.add(objectList.get(j));
                }
        }
        
        // Collect all links for object
        Iterator objectSetArray = objectsSet.iterator();
        while(objectSetArray.hasNext()) {
            ArrayList<org.jdom.Element> links = new ArrayList();
            ArrayList<Link> linkList2 = LinkDB.getLinkList();    
            String object = (String)objectSetArray.next();
            for(int j =0; j < linkList2.size(); j++) {
                ArrayList<String> objectList = linkList2.get(j).getObjects();
                if(objectList!=null)
                    for(int k = 0; k < objectList.size(); k++) {
                        if(objectList.get(k).equals(object)) {
                            links.add(linkList2.get(j).getLink());
                        }
                    }
            }
            objectsToLinks.put(object,links);
        }
        
         //XMLOutputter outputter = 
           // new XMLOutputter( org.jdom.output.Format.getPrettyFormat() ); 
         
       Iterator it = objectsToLinks.entrySet().iterator();
       while(it.hasNext()) {
           Map.Entry a = (Map.Entry)it.next();
           //System.out.println(a.getKey()+" :");
           ArrayList <Element> list = (ArrayList<Element>)a.getValue();
           org.jdom.Element links = new org.jdom.Element("links");
           for(int j =0; j < list.size();j++) {
               links.addContent(list.get(j));
           }
                     
            //System.out.println(outputter.outputString( links ));                                
           
       }
         
        // Return objects, ents a locations links
        return objectsToLinks;
    }
    
    /**
     * Create process templates from list of elements,
     * add them to process database and refresh all needed models
     */
   public void  fillInProcesses(List<org.jdom.Element> procesy) {
                                       
        for(int i = 0; i < procesy.size(); i++) {        
            ProcessTempl pt = new ProcessTempl();            
            String pId = procesy.get(i).getAttributeValue("processId").toString();
            pt.setProcessID(pId);
            pt.setGoalID(procesy.get(i).getAttributeValue("goalId").toString());
            pt.setMinLod(Integer.parseInt(procesy.get(i).getAttributeValue("minLod").toString()));
            pt.setMaxLod(Integer.parseInt(procesy.get(i).getAttributeValue("maxLod").toString()));        
            pt.setClassName(procesy.get(i).getAttributeValue("className").toString());  
            
            Element proces = procesy.get(i);
            // Expansion
            PExpansion pexpansion = new PExpansion();
            Element expansion = proces.getChild("expansion");
            if (expansion == null)
                System.out.println("expanze je null");
            
            Element ontology = expansion.getChild("OntologyToken");
            if(ontology != null) {                           
                List<Element> cile = ontology.getChildren();            
                if(cile.size() > 0) {
                    for(int j = 0 ; j < cile.size(); j++) {
                        Goal goal = new Goal();
                        goal.setGoalID(cile.get(j).getAttributeValue("goalId").toString());
                    
                        // Gtrigger
                        Element gtrigger = cile.get(j).getChild("gtrigger").getChild("OntologyToken");
                        if (gtrigger != null)
                            goal.setGTrigger(new GTrigger(gtrigger));                        
                        else goal.setGTrigger(new GTrigger());
                        // GContext
                        Element gcontext = cile.get(j).getChild("gcontext").getChild("OntologyToken");
                        if (gcontext == null) {
                            goal.setGContext(new GContext());
                        }
                        else {
                            goal.setGContext(new GContext(gcontext));
                        }                       
                        // GSources
                        List<Element> gsources = cile.get(j).getChild("sources").getChildren();
                        GSources gs = new GSources();
                        for(int k = 0; k < gsources.size(); k++) {
                            GSlot slot = new GSlot();
                            String name = gsources.get(k).getAttributeValue("name").toString();
                            if(name.equals("defaultSlot")) {
                                //System.out.println("Byl default");
                                break;
                            }
                            if(name != null) {
                                slot.setName(name);
                                //Add new slot name to LinkDB
                                LinkDB.addLink(new Link(name,"*","*"));
                            }
                            Attribute atr = gsources.get(k).getAttribute("valueFrom");
                            if(atr!=null) {
                                String valueFrom = atr.getValue();                            
                                if (valueFrom != null)
                                    slot.setValueFrom(valueFrom);                            
                            }
                            atr = gsources.get(k).getAttribute("actor");
                            if(atr!=null) {
                                String actor = atr.getValue();
                                if (actor != null) {
                                    slot.setActor(actor);
                                }
                            }
                            gs.add(slot);
                        }
                        goal.setGSources(gs);                         
                        // GParametres
                        List<Element> parameters = cile.get(j).getChild("parameters").getChildren();
                        GParameters gp = new GParameters();
                        for(int k = 0; k < parameters.size(); k++) {
                            GParameter param = new GParameter();
                            Element el= parameters.get(k);
                            String name = el.getAttributeValue("name");
                            String ontologyTok = el.getChild("OntologyToken").getAttributeValue("ontology");
                            String value = el.getChild("OntologyToken").getChild("Value").getAttributeValue("value");
                            param.setName(name);
                            param.setOntologyToken(ontologyTok);
                            param.setValue(value);
                            gp.addParameter(param);                            
                        }
                        goal.setGParameters(gp);
                        
                        pexpansion.addGoal(goal);
                    }
                }
            }
            
            pt.setPExpansion(pexpansion);            
            // Process Sources 
            Element sources = proces.getChild("sources");
            List <Element> sloty = sources.getChildren();
            PSources ps = new PSources();            
            if (sloty.size() > 0) {
                Iterator iter = sloty.iterator();
                while(iter.hasNext()) { 
                    PSlot slot = new PSlot();
                    Element eslot = (Element)iter.next();
                    String name = eslot.getAttributeValue("name").toString();
                    if(name.equals("defaultSlot")) {
                        //System.out.println("Byl default");
                        break;
                    }
                    slot.setName(name);
                    //System.out.println("Vytvarim Slot s jmenem: " +name);
                    //Add new slot name to ROleDB
                    LinkDB.addLink(new Link(name,"*","*"));
                    
                    String actor = eslot.getAttributeValue("actor");
                    if (actor != null) {
                        slot.setActor(actor);
                        //System.out.println("Vytvarim Slot s actorem: " +actor);  
                    }                    
                    
                    String optional = eslot.getAttributeValue("optional");
                    if (optional != null) {
                        slot.setOptional(optional);
                        //System.out.println("Vytvarim Slot s optional: " +optional);  
                    }                    
                    
                    String variable = eslot.getAttributeValue("variable");
                    if (variable!= null) {
                        slot.setVariable(variable);
                        //System.out.println("Vytvarim Slot s variable: " + variable);  
                    }                                                            
                    ps.addPSlot(slot);
                    //System.out.println("Slot zasunut.");
                }
            }
            pt.setPSources(ps);
            
            // Process Suitability            
            Element psuitability = proces.getChild("suitability");
            Element suitability = psuitability.getChild("OntologyToken");
            if( suitability != null ) {
                PSuitability psui = new PSuitability(suitability);
                pt.setPSuitability(psui);
            }

            // PContext
            Element pcontext = proces.getChild("pcontext");
            Element context = pcontext.getChild("OntologyToken");
            if( context != null ) {
                PContext pcon = new PContext(context);
                pt.setPContext(pcon);
            }                        
            
            // Add Process Template to Process Database
            ProcessDB.add(pt);                        
        }                          
    }
            
   /**
    * Frees all data structures and models
    */     
   public void freeAll() {       
        // Free databases
        ProcessDB.freeAll();
        EntGeniusDB.freeAll();        
        LocationGeniusDB.freeAll();        
        ImportRepositoryDB.freeAll();
        ExportRepositoryDB.freeAll();
        CleaningGoalDB.freeAll();       
        UtteranceDB.freeAll();
        LinkDB.freeAll();
       
        // Free views
        review.freeAll();       
        ueview.freeAll();
        ssview.freeAll();
        petview.freeAll();
        //petview.getHierarchyTree().removeAllChildren();
        //petview.createHierarchyModel();       
              
        // Free models       
        pvmodel.freeAll();
        stm.freeAll();
        emodel.freeAll();                                  
        inmodel.freeAll();
        psmodel.freeAll();
        gsmodel.freeAll();
        gpmodel.freeAll();
        egmodel.freeAll();
        etlgmodel.freeAll();
        lgmodel.freeAll();        
        gcgmodel.freeAll();
        tlgpmodel.freeAll();
        tlggmodel.freeAll();        
   }

    public void addListener(MPGListener listener) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Occures when new project is created
     **/
    public void newProjectEvent() {    
        
        File defaultMPG = new File( "../default/XML/DefaultMPG.xml" );
         try{             
             defaultMPG = defaultMPG.getCanonicalFile();
             if(defaultMPG.exists()) {                                          
                org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
                org.jdom.Document doc = builder.build( defaultMPG );
                 Element root = doc.getRootElement();
             
                 // Load defalut processes
                 Element procTemplEl = root.getChild( "processTemplates" );
                 if ( procTemplEl != null ) {
                     List<Element> processTemplates = procTemplEl.getChildren();
                    if(processTemplates!=null)
                        fillInProcesses(processTemplates);
                }             
             }
         } catch( java.io.IOException ex ){
             ex.printStackTrace();
         } catch (JDOMException ex) {
             ex.printStackTrace();
         } catch( NullPointerException ex ){ }

        
        
        // Fill in views
        spview.updateSlotNames();          
        petview.createNormalModel();
        //petview.sortIncActionNormalCurrentPopup();
        //petview.createHierarchyModel();
        //petview.updateGoalVector();
        petview.getTabbedPane().setSelectedIndex(0);        
        petview.setVisibleNormalTree();
        spview.getMainTabbedPane().setSelectedIndex(0); 
        spview.updateSlotNames();
        spview.updateGSlotNamesActorValueFrom(new ArrayList<String>());        
        // Fill in models
        pvmodel.freeAll();
        pvmodel.addProcesses(ProcessDB.getList());        
        stm.setModel(pvmodel);      
        
        setEnabled(true);
    }

    /**
     * Occures when project is closing 
     */
    public void closeProjectEvent() {        
        freeAll();
        jPanelMain.setVisible(false);
        setEnabled(false);        
    }
 
    /**
     * Occures when project is opening
     */
    public void openProjectEvent() {
        
        jPanelMain.setVisible(true);
        setEnabled(true);
        
        createEntGeniuses();
        createLocationGeniuses();
        
        setEntGeniusInMOL();
        setLocationGeniusInMOL();
        
        loadLinksFromFile();
                             
        // Fill in views
        spview.updateSlotNames();          
        petview.createNormalModel();
        petview.sortIncActionNormalCurrentPopup();
        //petview.createHierarchyModel();
        //petview.updateGoalVector();
        petview.getTabbedPane().setSelectedIndex(0);        
        petview.setVisibleNormalTree();
        spview.getMainTabbedPane().setSelectedIndex(0); 
        spview.updateSlotNames();
        spview.updateGSlotNamesActorValueFrom(new ArrayList<String>());        
        // Fill in models
        pvmodel.freeAll();
        pvmodel.addProcesses(ProcessDB.getList());        
        stm.setModel(pvmodel);        
        
    } 
    
    /**
     * Occures when saving project
     */
    public void saveProjectEvent() {
        
        // Save ent genius xml representation
        saveMpgEntGeniusXmlFile();
        // Save location genius xml representation
        saveMpgLocationGeniusXmlFile();
        // Save file that keeps ent and its genius, separated by ;
        saveMpgEntToGeniusFile();
        // Save file that keeps location and its list of genies, separated by ;
        saveMpgLocationToGeniusFile();
        // Save file that keeps all links
        // We need to keep links that were not used in edited world because
        // they will be missing after reopen project
        saveMpgLinksFile();
    }

    /**
     * Create utterances from xml and add them to database of all utterances
     */
    public void fillInUtterances(List listut) {                
        for(int i =0; i < listut.size(); i++) {
            Element e = (Element)listut.get(i);
            Utterance u = new Utterance();
            u.setText(e.getAttributeValue("text"));
            u.setIndex(e.getAttributeValue("index"));
            UtteranceDB.addUtterance(u);            
        }        
    }
    
   /**
    *  Create simulation settings elements from xml and add them to database of 
    *  all simulation settings elements
    */
    public void fillInSS(List sslist) {
                
        for(int i =0; i < sslist.size(); i++) {
            Element e = (Element)sslist.get(i);    
            SimulationSettingsElement sse = new SimulationSettingsElement();
            if(e.getName().equals("Speed")) {
                Speed s = new Speed();
                s.setSpeedRatio(Double.parseDouble(e.getAttributeValue("speedRatio")));
                sse.setSpeed(s);
                if(SimulationSettingsDB.getSSList().size()==0)
                    SimulationSettingsDB.addSS(sse);
                else SimulationSettingsDB.getSSList().get(0).setSpeed(s);
                    
            }
            if(e.getName().equals("CleanUp")) {
                CleanUp cu = new CleanUp();
                cu.setLoadTriggered(Boolean.parseBoolean(e.getAttributeValue("loadTriggered")));
                cu.setThreshold(Integer.parseInt(e.getAttributeValue("threshold")));
                cu.setMinimalLoop(Integer.parseInt(e.getAttributeValue("minimalLoop")));
                cu.setWindowSize(Integer.parseInt(e.getAttributeValue("windowSize")));
                sse.setCleanUp(cu);  
                if(SimulationSettingsDB.getSSList().size()==0)
                    SimulationSettingsDB.addSS(sse);
                else SimulationSettingsDB.getSSList().get(0).setCleanUp(cu);                
            }                        
            
        }        
    }    
    
    /**
     * Create links from xml. Add them to database of all links
     */
    public void fillInLinks(List<Element> listobjects) {
        
        Map<String,String> nametemplate = new HashMap<String,String>();
        
        for(int i =0; i < listobjects.size(); i++) {
            String name = listobjects.get(i).getAttributeValue("name");
            // because inheritance
            String template = listobjects.get(i).getAttributeValue("template");
            Element links = listobjects.get(i).getChild("links");
            if(links!=null) {
                List<Element> linkList = links.getChildren();
                if(linkList!=null) {
                    for(int j = 0; j < linkList.size(); j++) {
                        Element link = linkList.get(j);
                        Link li = new Link();
                        li.setName(link.getAttributeValue("slot"));
                        li.setProcess(link.getAttributeValue("process"));
                        li.setGoal(link.getAttributeValue("goal"));
                        LinkDB.addLink(li);
                        LinkDB.getLink(li).addObject(name);
                    }
                }
            }
            if(template !=null && !template.equals("")) {                
                // The template 'name' has an ancestor 'template'
                nametemplate.put(name,template);
            }
        }
        
        // projdi link a v kazdy zjisti zda obsahuje objekt template
        // pokud ano pridej do teto link objekt name
        // add object name to all links that have template added
        Iterator it = nametemplate.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry a = (Map.Entry)it.next();
            String name  = (String)a.getKey();
            String template = (String)a.getValue();
            LinkDB.addObject(name,template);
        }                
    }
    
    /**
    * Fill in LinksDB with links names that equals Slot name
    */    
    public void fillInLinksFromLocations(List<Element> listloc) {        
        Map<String,String> nametemplate = new HashMap<String,String>();        
        for(int i =0; i < listloc.size(); i++) {
            String name = listloc.get(i).getAttributeValue("name");
            // because inheritance
            //String template = listobjects.get(i).getAttributeValue("template");
            Element links = listloc.get(i).getChild("links");
            if(links!=null) {
                List<Element> linkList = links.getChildren();
                if(linkList!=null) {
                    for(int j = 0; j < linkList.size(); j++) {
                        Element link = linkList.get(j);
                        Link li = new Link();
                        li.setName(link.getAttributeValue("slot"));
                        li.setProcess(link.getAttributeValue("process"));
                        li.setGoal(link.getAttributeValue("goal"));
                        LinkDB.addLink(li);
                        ArrayList<String> upLocations = 
                            getUpLocations(name,listloc);
                        if (upLocations !=null) {
                            for(int l =0; l < upLocations.size(); l++) {
                                String loc = upLocations.get(l);// +"."+name;                    
                                upLocations.set(l,loc);                                                                
                            }                                                   
                            for(int k = 0; k < upLocations.size(); k++) {
                                // To link is added new location ( even location has links ) 
                                LinkDB.getLink(li).addObject(upLocations.get(k));                                                                  
                            }
                        }
                        else
                            LinkDB.getLink(li).addObject(name);                            
                    }
                }
            }            
        }               
    }
    
    /**
     * Set helper variable 'rootLocation' to name of the root location
     */
    public void fillInRootLocation(Element root) {
        if(root == null)
            return;
        String pom = root.getAttributeValue("name");
        if(pom!=null) {            
            rootLocation = pom;            
        }
    }       

    /**
     * Create ent genius from xml file, stored in temp directory
     */
    void createEntGeniuses() {
        
        File file = serviceComponentProject.getMpgEntGeniusXmlFile();
        
        if(file==null || !file.exists()) {            
            return;
        }        
                   
        SAXBuilder builder = new SAXBuilder();
        Document docum = new Document();
        
        try {
            
            docum = builder.build(file);
            if(docum.getRootElement() == null) {                
                return;
            }
        }
        catch (JDOMException e) {
            // xml file loading failed            
            return;
            //e.printStackTrace();
        }          
        catch (IOException e) {
            return;
            //e.printStackTrace();            
        }                        
        Element child = docum.getRootElement();        
        List<Element> children = child.getChildren("Genius");        
        // Genius creating
        if(children != null) {
            for(int i =0; i < children.size(); i++) {
                Element elem = (Element)children.get(i);
                Attribute atr = elem.getAttribute("geniusId");
                if(atr==null)   // invalid config file, there must be geniusId attribute
                    continue;
                String geniusId = atr.getValue();
                Element value = elem.getChild("Genius");             
                
                entGeniuses.put(geniusId,value);                                     
                
                EntGenius genius = new EntGenius();
                genius.setEntGeniusID(geniusId);
                if(value!=null) {
                    List<Element> chld = value.getChildren("TopLevelGoal");
                    for(int j =0; chld!=null && j < chld.size(); j++) {
                        EntTopLevelGoal tlg = new EntTopLevelGoal();   
                        Attribute goalIdAttr = ((Element)chld.get(j)).getAttribute("goalId");
                        if(goalIdAttr==null)
                            continue; // invalid config file, there must be goalId attribute
                        String id = goalIdAttr.getValue();
                        tlg.setGoalId(id);
                        genius.addTLG(tlg);
                    }
                }
                EntGeniusDB.addEntGenius(genius);
            }         
        }          
        //map genius - ent
        Map<String, String> entToGenius  = serviceXMLLoader.getEntToGenius();
        Iterator it = entToGenius.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry a = (Map.Entry)it.next();
            String ent = (String)a.getKey();
            String gen = (String)a.getValue();
            EntGenius eg = EntGeniusDB.getEntGenius(gen);
            if(eg!=null)
                eg.addEnt(ent);
        }        
        //notify ent genius model
        egmodel.setValues(EntGeniusDB.getEntGeniusList());
    }

    /**
     * Create location geniues from xml file stored in temp directory
     */
    void createLocationGeniuses() {
        
        File file = serviceComponentProject.getMpgLocationGeniusXmlFile();
        
        if(file==null || !file.exists()) {            
            return;
        }
        
        SAXBuilder builder = new SAXBuilder();
        Document docum = new Document();
        
        try {
            
            docum = builder.build(file);
            if(docum.getRootElement()==null) {            
                return;
            }
        }
        catch (IOException e) {
            //e.printStackTrace();
            return;
        }
        catch (JDOMException e) {
            //e.printStackTrace();
            return;
        }
        if(docum==null) {
            return;
        }
        Element child = docum.getRootElement();
        
        List<Element> children = child.getChildren("Genius");
        
        // Location Genius creating
        if(children != null) {
            for(int i =0; i < children.size(); i++) {
                LocationGenius genius = new LocationGenius();                
                Element elem = (Element)children.get(i);
                Attribute gId = elem.getAttribute("geniusId");
                if(gId==null)
                    continue;
                String geniusId = gId.getValue();
                Element value = elem.getChild("Genius");
                if(value==null) {
                    genius.setLocationGeniusID(geniusId);
                    genius.setClassName("cz.ive.genius."+geniusId);
                    LocationGeniusDB.addLocationGenius(genius);
                    //System.out.println("Added LocGen "+ genius.getLocationGeniusID());
                    locationGeniuses.put(geniusId,value);                                         
                    continue;    
                }
                Attribute cName = value.getAttribute("className");
                if(cName==null)
                    continue;                

                genius.setLocationGeniusID(geniusId);
                genius.setClassName(cName.getValue());
                List<Element> chld = value.getChildren("TopLevelGoal");
                for(int j =0; chld!=null && j < chld.size(); j++) {
                    Attribute pID = ((Element)chld.get(j)).getAttribute("process");
                    Attribute gID = ((Element)chld.get(j)).getAttribute("goal");
                    if(pID == null || gID == null)
                        continue;
                    LocationTopLevelGoal tlg = new LocationTopLevelGoal();     
                    String pid = pID.getValue();
                    String gid = gID.getValue();
                    tlg.setProcess(pid);
                    tlg.setGoal(gid);
                    genius.addLocationTLG(tlg);
                }
                chld = value.getChildren("CleaningGoal");
                for(int j =0; chld!=null && j < chld.size(); j++) {
                    Attribute gIdAttr = ((Element)chld.get(j)).getAttribute("goalId");
                    if(gIdAttr == null)
                        continue;
                    String cid = gIdAttr.getValue();
                    
                    Element expr = ((Element)chld.get(j)).getChild("OntologyToken");                    
                    if(expr==null || !isValidExpr(expr))
                        continue;
                    CleaningGoal cg = new CleaningGoal();
                    String name = "name"+cid;
                    int count = 0;
                    while(CleaningGoalDB.isIn(name)) {
                        name = "name"+cid+""+(++count);                        
                    }
                    cg.setName(name);
                    cg.setGoalID(cid);
                    cg.setExpression(new Expression(expr));                        
                    genius.addCleaningGoal(cg);
                    if(!LocationGeniusDB.isIn(genius))
                        CleaningGoalDB.addCleaningGoal(cg);                    
                }
                
                LocationGeniusDB.addLocationGenius(genius);
                //System.out.println("Added LocGen "+ genius.getLocationGeniusID());
                locationGeniuses.put(geniusId,value);                     
            }            
        }
        
         //loading from locationtogenius.dat
         File fileL = serviceComponentProject.getMpgLocationToGeniusFile();
        
        if(fileL==null || !fileL.exists()) {            
            return;
        }
                
        StringBuffer buf = new StringBuffer();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileL));                
            String line;
            while((line = br.readLine()) != null) {
                buf.append(line);
            }            
            StringTokenizer st = new StringTokenizer(buf.toString(),";");
            while(st.hasMoreTokens()) {
                String s = (String)st.nextToken();             
                StringTokenizer st2 = new StringTokenizer(s,"=");
                if(!st2.hasMoreTokens())
                    continue;
                String lgen = (String)st2.nextToken();
                if(!st2.hasMoreTokens())
                    continue;                
                String locs = (String)st2.nextToken();                
                StringTokenizer st3 = new StringTokenizer(locs,"*");
                while(st3.hasMoreTokens()) {
                    String lg = (String)st3.nextToken();                    
                    // Add location to genius
                    LocationGenius gen = LocationGeniusDB.getLocationGenius(lg);
                    if(gen!=null)
                            gen.addLocation(lgen);
                }   
            }
        }catch (FileNotFoundException ex) {
            
        } catch (IOException e) {
            
        }
    
        //notify location genius model
        lgmodel.setValues(LocationGeniusDB.getLocationGeniusList());              
    }
    
    /**
     * Load links from file. We must keep all links ( <Link name= process= goal=>)
     * because not each one is used in world. If we don't keep them we should lose
     * them after reopen project.
     */
    public void loadLinksFromFile() {
        
        File linksfile = serviceComponentProject.getMpgLinksFile();
        try {
            BufferedReader br = new BufferedReader(new FileReader(linksfile));
            String line;
            String slotName = new String("");
            String process = new String("");
            String goal = new String("");
            Link link = new Link(slotName,process,goal);
            while((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line,"@");
                    //slot token                
                    StringTokenizer slt = new StringTokenizer(
                            (String)st.nextToken()," ");
                    if(slt.hasMoreTokens()) {
                        slotName = (String)slt.nextToken();
                    } else continue;    // file is not correct
                    if(slt.hasMoreTokens()) {
                        process = (String)slt.nextToken();
                    } else continue;
                    if(slt.hasMoreTokens()) {
                        goal = (String)slt.nextToken();
                    } else continue;
                    if(slotName.equals("defaultSlot") && process.equals("*")
                    && goal.equals("*"))
                        continue;
                    // add new link
                    link = new Link(slotName,process,goal);
                    LinkDB.addLink(link);                                                        
                    // object token
                    if(st.hasMoreTokens()) {
                        StringTokenizer stobj = new StringTokenizer(
                            (String)st.nextToken(),";");
                        while(stobj.hasMoreTokens()) {
                            link.addObject((String)stobj.nextToken());
                        }                       
                    }
            }
        } catch(IOException e) {
            //e.printStackTrace();
        }
        
    }
    
    /**
     * Return list of Ent genius ID
     */
    public ArrayList<String> getEntGeniusesId() {
        ArrayList<String> geniusID = new ArrayList();
        ArrayList<EntGenius> list = EntGeniusDB.getEntGeniusList();
        for(int i = 0; i < list.size(); i++) {
            geniusID.add(list.get(i).getEntGeniusID());
        }
        return geniusID;
    }
    
    /**
     * Return list of location genies ID
     */
    public ArrayList<String> getLocationGeniusesId() {
        ArrayList<String> geniusID = new ArrayList();
        ArrayList<LocationGenius> list = LocationGeniusDB.getLocationGeniusList();
        for(int i = 0; i < list.size(); i++) {
            geniusID.add(list.get(i).getLocationGeniusID());
        }
        return geniusID;
    }            

    /**
     * Retuturn list of process IDs
     */
    public ArrayList<String> getProcessesID() {
        ArrayList<ProcessTempl> tplist = ProcessDB.getList();
        ArrayList<String> list = new ArrayList();
        for(int i = 0; i < tplist.size(); i++) 
            list.add(tplist.get(i).getProcessID());
        
        return list;
    }

    /**
     * Return list of goals ids. ( Each process template has goal id attribute )
     */
    public ArrayList<String> getGoalsID() {
        ArrayList<ProcessTempl> tplist = ProcessDB.getList();
        Set<String> listset = new HashSet();
        for(int i = 0; i < tplist.size(); i++) 
            listset.add(tplist.get(i).getGoalID());
        
        return new ArrayList<String>(listset);
    }
    
    public ArrayList<String> getSlotsName() {
        ArrayList<ProcessTempl> ptlist = ProcessDB.getList();
        Set<String> set = new HashSet();
        for(int i =0; i < ptlist.size(); i++) {
            List<PSlot> sources = ptlist.get(i).getPSources().getPSourcesList();
            if(sources != null && sources.size() != 0) {
                for(int j =0; j < sources.size(); j++) {
                    set.add(sources.get(j).getName());
                }
            }
        }
        return new ArrayList(set);
    }
    
    public ArrayList<String> getProcessSlotsName() {
        ArrayList<ProcessTempl> ptlist = ProcessDB.getList();
        Set<String> set = new HashSet();
        for(int i =0; i < ptlist.size(); i++) {
            
            List<PSlot> sources = ptlist.get(i).getPSources().getPSourcesList();
            if(sources != null && sources.size() != 0) {
                for(int j =0; j < sources.size(); j++) {
                    set.add(sources.get(j).getName());
                }
            }
            
            Vector<Goal> expansion = ptlist.get(i).getPExpansion().getGoalList();
            for(int j = 0; j < expansion.size(); j++) {
                List<GSlot> gsources = expansion.get(j).getGSources().getGSources();
                if(gsources != null && gsources.size() != 0) {
                    for(int k =0; k < gsources.size(); k++) 
                        set.add(gsources.get(k).getName());
                }
            }
            
        }
        return new ArrayList(set);
    }
    
    public ArrayList<String> getGoalExpansionSlotsName() {
        ArrayList<ProcessTempl> ptlist = ProcessDB.getList();
        Set<String> set = new HashSet();
        for(int i =0; i < ptlist.size(); i++) {
            Vector<Goal> expansion = ptlist.get(i).getPExpansion().getGoalList();
            for(int j = 0; j < expansion.size(); j++) {
                List<GSlot> sources = expansion.get(j).getGSources().getGSources();
                if(sources != null && sources.size() != 0) {
                    for(int k =0; k < sources.size(); k++) 
                        set.add(sources.get(k).getName());
                }
            }
        }
        return new ArrayList(set);                
    }

    /**
     * Save ent genius to xml file - to temp directory for this plugin
     */
    private void saveMpgEntGeniusXmlFile() {
        
        //create Ent-Genius map
        HashMap<String,org.jdom.Element> entToGeniusMap = new HashMap();
        Set<String> ents = new HashSet();
        // EntGenius without top level goal and ent, (or with TLG and without Ent)
        ArrayList<String> genWithout = new ArrayList<String>();
        ArrayList<EntGenius> eglist = EntGeniusDB.getEntGeniusList();
        for(int i =0; i < eglist.size(); i++) {
            ArrayList<String> entsList = eglist.get(i).getEntList();
            if(entsList!=null) {
                for(int j =0; j < entsList.size(); j++) {
                    ents.add(entsList.get(j));
                }
            }           
            if(eglist.get(i).getEntTLGoalsList().size()>=0
                    && entsList.size() == 0) {
                    genWithout.add(eglist.get(i).getEntGeniusID());
            }            
        }
        Iterator iter = ents.iterator();
        while (iter.hasNext()) {
            String ent = (String)iter.next();
            for(int i =0; i < eglist.size();i++) {
                // Asking whether genius has an ent                                    
                if(eglist.get(i).hasEnt(ent)) {
                    Element gen = new Element("Genius");
                    ArrayList<Element> elem = eglist.get(i).getTLGElement();                    
                    if(elem != null)
                        for(int j = 0; j < elem.size(); j++)
                            gen.addContent((Element)elem.get(j).clone());
                 
                    //<Ent> - <Genius> map                    
                    entToGeniusMap.put(ent,gen);
                }                       
            }            
        }

        if(entToGeniusMap.size()==0) {
            // We have none EntGeniuses            
            return;        
        }
        File f = serviceComponentProject.getMpgEntGeniusXmlFile();
        String path = serviceComponentProject.getMPGPath();
        File checkE = new File(path+File.separator+"EntGenius");
        if(!checkE.exists())
            checkE.mkdirs();        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));           
            Element root = new Element("genies");
            Iterator it = entToGeniusMap.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry a = (Map.Entry)it.next();
                Element e = (Element)a.getValue();                
                ArrayList<EntGenius> egList = EntGeniusDB.getEntGeniusList();
                String ent =(String)a.getKey();
                String gen = "";
                for(int i =0; i < egList.size(); i++) {
                    if(eglist.get(i).hasEnt(ent)) {
                        gen = egList.get(i).getEntGeniusID();
                        break;
                    }
                }
                //if(gen=="") //we don't add geniusId ' '
                  //  continue;
                Element genius = new Element("Genius");
                genius.setAttribute("geniusId", gen);
                if(e!=null)
                    genius.addContent((Element)e.clone());            
                root.addContent(genius);
                //System.out.println("Pridavam genia: "+gen+";");
            }
            // Add genies without TLG and Ents, or withTLG and without Ent
            for(int i = 0; i < genWithout.size(); i++) {
                Element genius = new Element("Genius");
                genius.setAttribute("geniusId", genWithout.get(i));
                ArrayList<Element> elem = EntGeniusDB.getEntGenius(
                        genWithout.get(i)).getTLGElement();
                Element genE = new Element("Genius");                
                    if(elem != null) {
                        for(int j = 0; j < elem.size(); j++)
                            genE.addContent((Element)elem.get(j).clone());                
                    }
                genius.addContent((Element)genE.clone());
                root.addContent(genius);
            }
           XMLOutputter outputter = 
                    new XMLOutputter( org.jdom.output.Format.getPrettyFormat() );                                                
            bw.write(outputter.outputString(root));            
            bw.close();            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
                        
    }
    
    /*
     * Save location genius to xml file - to temp directory of this plugin
     */
    private void saveMpgLocationGeniusXmlFile() {

        File f = serviceComponentProject.getMpgLocationGeniusXmlFile();
        String path = serviceComponentProject.getMPGPath();
        File checkE = new File(path+File.separator+"LocationGenius");
        if(!checkE.exists())
            checkE.mkdirs();                
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            //bw.write("<genies>");
            Element root = new Element("genies");
            //Iterator it = locationGeniuses.entrySet().iterator();
            ArrayList<LocationGenius> lglist = LocationGeniusDB.getLocationGeniusList();
            int i =0;
            while(i<lglist.size()) {
                //Map.Entry a = (Map.Entry)it.next();                
                Element genius = new Element("Genius");
                String lgid = lglist.get(i).getLocationGeniusID();
                genius.setAttribute("geniusId", lgid);
                Element e = (Element)locationGeniuses.get(lgid);
                if(e!=null)
                    genius.addContent((Element)e.clone());                
                
                root.addContent(genius);
                i++;
            }
            XMLOutputter outputter = 
                    new XMLOutputter( org.jdom.output.Format.getPrettyFormat() );                                                
            bw.write(outputter.outputString(root));            
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
                        
    }    

    /**
     * Save ent genius to file , where each ent has it genies. All is separated by 
     * ';' character
     */
    private void saveMpgEntToGeniusFile() {
        // Create map Ent-Genius        
        entToGenius = new HashMap();
        Set<String> ents = new HashSet();
        ArrayList<EntGenius> eglist = EntGeniusDB.getEntGeniusList();
        for(int i =0; i < eglist.size(); i++) {
            ArrayList<String> entsList = eglist.get(i).getEntList();
            if(entsList!=null)
                for(int j =0; j < entsList.size(); j++) {
                    ents.add(entsList.get(j));
                }
        }
        Iterator iter = ents.iterator();
        while (iter.hasNext()) {
            String ent = (String)iter.next();
            for(int i =0; i < eglist.size();i++) {                
                if(eglist.get(i).hasEnt(ent)) {
                    entToGenius.put(ent,eglist.get(i).getEntGeniusID());
                }                    
            }            
        }
                
        File f = serviceComponentProject.getMpgEntToGeniusFile();
        Iterator it = entToGenius.entrySet().iterator();
        StringBuffer buf = new StringBuffer();
        while(it.hasNext()) {
            Map.Entry a = (Map.Entry)it.next();
            String ent = a.getKey().toString();
            String genId = a.getValue().toString();
            String s = ent+"="+genId;
            if(it.hasNext())
                s+=";";
            buf.append(s);
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(buf.toString());
            bw.close();
        } catch(IOException e ){
            e.printStackTrace();
        }                                            
    }

    /**
     * Save location genius to file , where each ent has its list of genies. 
     * All is separated by ';' character
     */
    private void saveMpgLocationToGeniusFile() {        

        locationToGenius = new HashMap();
        Set<String> locations = new HashSet();
        ArrayList<LocationGenius> lglist = LocationGeniusDB.getLocationGeniusList();
        for(int i =0; i < lglist.size(); i++) {
            ArrayList<String> locList = lglist.get(i).getLocationList();
            if(locList!=null)
                for(int j =0; j < locList.size(); j++) {
                    locations.add(locList.get(j));
                }
        }
        Iterator iter = locations.iterator();
        ArrayList<String>genii = new ArrayList();
        while (iter.hasNext()) {
            genii = new ArrayList();
            String loc = (String)iter.next();
            for(int i =0; i < lglist.size();i++) {
                if(lglist.get(i).hasLocation(loc)) {
                    genii.add(lglist.get(i).getLocationGeniusID());
                }                    
            }
            if(genii.size() >0)
                locationToGenius.put(loc,genii);
        }
        
        
        File f = serviceComponentProject.getMpgLocationToGeniusFile();
        Iterator it = locationToGenius.entrySet().iterator();
        StringBuffer buf = new StringBuffer();
        while(it.hasNext()) {
            Map.Entry a = (Map.Entry)it.next();
            String loc = a.getKey().toString();
            String geniis = new String("");
            ArrayList<String> geniiL = (ArrayList<String>)a.getValue();            
            for(int i =0; i < geniiL.size(); i++) {
                if(i+1 <geniiL.size())
                    geniis+=geniiL.get(i)+"*";
                else geniis+=geniiL.get(i);
            }
            if(it.hasNext())
                loc+="="+geniis+";";
            else loc+="="+geniis;
            
            buf.append(loc);
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(buf.toString());
            bw.close();
        } catch(IOException e ){
            e.printStackTrace();
        }     
        
    }
    
    /**
     * Save Links to file <slotName process goal>
     */
    public void saveMpgLinksFile() {
        
        File linkfile = serviceComponentProject.getMpgLinksFile();
        String path = serviceComponentProject.getMPGPath();
        File checkE = new File(path+File.separator+"Links");
        if(!checkE.exists())
            checkE.mkdirs();        
        
        ArrayList<Link> linkList = LinkDB.getLinkList();
        try {
        BufferedWriter bw = new BufferedWriter(new FileWriter(linkfile));
            for(int i =0; i < linkList.size(); i++) {
                String slotName = linkList.get(i).getName();
                String process = linkList.get(i).getProcess();
                String goal = linkList.get(i).getGoal();            
                bw.write(slotName+" "+process+" "+goal+"@");
                ArrayList<String> objList = linkList.get(i).getObjects();
                for(int j =0; j < objList.size(); j++) {
                    if(j+1==objList.size())
                        bw.write(objList.get(j));
                    else
                        bw.write(objList.get(j)+";");
                }
                bw.write("\n");
            }
        bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        
    }

    
 /*   String getEntGeniusId(String entName) {
        ///throw new UnsupportedOperationException("Not yet implemented");
        return new String("");
        
    }

    ArrayList<String> getLocationGeniusesId(String locationId) {
        //throw new UnsupportedOperationException("Not yet implemented");
        return new ArrayList();
    }
  */
    
    /**
     * Used when some object in MOL is renamed
     */
    void renameItem(String oldName, String newName) {
        //throw new UnsupportedOperationException("Not yet implemented");
        //rename in Links
        for(int i = 0; i < LinkDB.getLinkList().size(); i++) {   
            ArrayList<String> objects = LinkDB.getLinkList().get(i).getObjects();
            ArrayList<String> newObjects = new ArrayList();
            for(int j = 0; j < objects.size(); j++ ) {
                if(objects.get(j).equals(oldName))              
                    newObjects.add(newName);
                else newObjects.add(objects.get(j));
            }
            LinkDB.getLinkList().get(i).setObjects(newObjects);
        }        
        
        for(int i = 0; i < LocationGeniusDB.getLocationGeniusList().size(); i++) {   
            ArrayList<String> loc = LocationGeniusDB.getLocationGeniusList().
                    get(i).getLocationList();
            ArrayList<String> newLocs = new ArrayList();
            for(int j = 0; j < loc.size(); j++ ) {
                if(loc.get(j).equals(oldName))              
                    newLocs.add(newName);
                else newLocs.add(loc.get(j));
            }
            LocationGeniusDB.getLocationGeniusList().get(i).setLocationList(newLocs);
        }                
        
        //rename in location geniuses
        
        egview.updateView();
        lgview.updateView();
        review.updateView();
    }
    
    /**
     *
     * Used when some object in MOL is removed
     */
    void removeItem(String itemName) {
    //throw new UnsupportedOperationException("Not yet implemented");
        for(int i = 0; i < LinkDB.getLinkList().size(); i++) {   
            ArrayList<String> objects = LinkDB.getLinkList().get(i).getObjects();
            ArrayList<String> newObjects = new ArrayList();
            for(int j = 0; j < objects.size(); j++ ) {
                if(objects.get(j).equals(itemName))              
                    continue;
                else newObjects.add(objects.get(j));
            }
            LinkDB.getLinkList().get(i).setObjects(newObjects);
        }           
        
       for(int i = 0; i < LocationGeniusDB.getLocationGeniusList().size(); i++) {   
            ArrayList<String> loc = LocationGeniusDB.getLocationGeniusList().
                    get(i).getLocationList();
            ArrayList<String> newLocs = new ArrayList();
            for(int j = 0; j < loc.size(); j++ ) {
                if(loc.get(j).equals(itemName))              
                    continue;
                else newLocs.add(loc.get(j));
            }
            LocationGeniusDB.getLocationGeniusList().get(i).setLocationList(newLocs);
        }               
        
        egview.updateView();
        lgview.updateView();
        review.updateView();
    }

     /**
     *
     * Used when some object in MOL is removed
     */
    void renameObject(String oldName, String newName) {
        renameItem(oldName, newName);
    }

     /**
     *
     * Used when some object in MOL is removed
     */
    void removeObject(String objectName) {
         removeItem(objectName);
    }
    
    /**
     * Used when Ent in MOL is removed
     */
    void removeEnt(String entName) {
        
        //remove in LinkDB
        ArrayList<Link> linkList = LinkDB.getLinkList();
        for(int i = 0; i < linkList.size(); i++) {
            ArrayList<String> objects = linkList.get(i).getObjects();
            if(objects != null)
                for (int j = 0; j < objects.size(); j++) 
                    if(objects.get(j).equals(entName)) {
                        objects.remove(j);
                        break;
                    }
        }
        
        //remove in EntGeniusDB
        ArrayList<EntGenius> geniusList = EntGeniusDB.getEntGeniusList();
        for(int i = 0; i < geniusList.size(); i++) {
            ArrayList<String> ents = geniusList.get(i).getEntList();
            if(geniusList != null)
                for (int j = 0; j < geniusList.size(); j++) 
                    if(geniusList.get(j).equals(entName)) {
                        geniusList.remove(j);
                        break;
                    }
        }
        
        //notify
        egview.updateView();
        review.updateView();
        
    }
    
    /**
     * Used when Ent in MOL is renamed
     */
    void renameEnt(String oldEntName, String newEntName) {
        //throw new UnsupportedOperationException("Not yet implemented");
        
        //rename in LinkDB
        ArrayList<Link> linkList = LinkDB.getLinkList();
        for(int i = 0; i < linkList.size(); i++) {
            ArrayList<String> objects = linkList.get(i).getObjects();
            if(objects != null)
                for (int j = 0; j < objects.size(); j++) 
                    if(objects.get(j).equals(oldEntName)) {
                        objects.set(j,newEntName);
                        break;
                    }
        }
        
        //rename in EntGeniusDB
        ArrayList<EntGenius> geniusList = EntGeniusDB.getEntGeniusList();
        for(int i = 0; i < geniusList.size(); i++) {
            ArrayList<String> ents = geniusList.get(i).getEntList();
            if(ents != null)
                for (int j = 0; j < ents.size(); j++) 
                    if(ents.get(j).equals(oldEntName)) {
                        ents.set(j,newEntName);
                        break;
                    }
        }
        
        //notify 
        egview.updateView();
        review.updateView();
        
        
    }    

     /**
     *
     * Used when some location in MOL is removed
     */
    void removeLocation(String locationId) {
        //throw new UnsupportedOperationException("Not yet implemented");
        removeItem(locationId);
    }

     /**
     *
     * Used when some location in MOL is removed
     */
    void renameLocation(String oldLocationId, String newLocationId) {
        //throw new UnsupportedOperationException("Not yet implemented");
        renameItem(oldLocationId, newLocationId);
    }
    
    
    // Inner Classes
    
    class linkEditActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            openLinkEdit();
        }
    }
    
    class entGeniusEditActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            openEntGeniusEdit();            
        }
    }    
    
    class locationGeniusEditActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            openLocationGeniusEdit();            
        }
    }        
    
    class showWarningsActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            openWarningsView();            
        }
    }            
    
    
    class utterancesEditActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            openUtterancesEdit();            
        }
    }    
    
    class ssActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            openSSEdit();            
        }
    }        
    
    // End of Inner Classes
    
    /** 
     * Occures when we want to edit links
     */
    public void openLinkEdit() {
        review.updateView();
        review.setVisible(true);
    }
    
    /**
     *  Occures when we want to edit ent genies
     */
    public void openEntGeniusEdit() {
        egview.updateView();
        egview.setVisible(true);
        
    }
    
    /**  
     * Occures when we want to edit ent genies 
     */
    public void openLocationGeniusEdit() {
        lgview.updateView();
        lgview.setVisible(true);
        
    }    
    /**  
     * Occures when we want to show warnings in MPG
     */    
    public void openWarningsView() {
        wview.setProcessText(getProcessWarnings());
        wview.setEntGeniusText(getEntGeniusWarnings());
        wview.setLocationGeniusText(getLocationGeniusWarnings());
        wview.setVisible(true);
    }
    
    public void openShowXMLView(ArrayList<String> procList) {
        ArrayList<ProcessTempl> ptlist = new ArrayList<ProcessTempl>();
        for(int i =0; i < procList.size();i++) {
            ptlist.add(ProcessDB.get(procList.get(i)));
        }
        Element iveworld = new Element("IveWorld");
        iveworld.addContent(getProcessTemplates(ptlist));
        XMLOutputter outputter = 
                    new XMLOutputter( org.jdom.output.Format.getPrettyFormat() );
        sxview.setProcessXMLText(outputter.outputString(iveworld));
        sxview.setVisible(true);
     
    }
    
    /** 
     * Occures when we want to edit utterances 
     */
    public void openUtterancesEdit() {
        ueview.updateView();
        ueview.setVisible(true);        
    }            
    /** 
     * Occures when we want to edit simulation settings
     */
    public void openSSEdit() {
        ssview.updateView();
        ssview.setVisible(true);        
    }            

    /**
     * Set links in MOL in locations
     */
    void setLinks() {
        List<String> paths = serviceModulMOL.getLocationPaths();
  
        for(int i = 0; i < paths.size(); i++) {
            org.jdom.Element linksE = new org.jdom.Element("links");
            ArrayList<org.jdom.Element> linksList = objectsToLinks.get(paths.get(i));
            if(linksList!=null)
                for(int j =0; j < linksList.size(); j++ ) {
                    linksE.addContent((Element)linksList.get(j).clone());
                }
            serviceModulMOL.setLinks(paths.get(i),(Element)linksE.clone() );
        }        
    }

    /**
     * Create location genies from xml. ( when loading or opening project )
     */
    void fillInLocationGenies(List <Element>listloc) {
        if(listloc == null)
            return;
        for(int i =0; i < listloc.size(); i++) {
            String locName = listloc.get(i).getAttributeValue("name");
            //System.out.println("Prohledavam Sablonu " + locName);
            Element sublocations = listloc.get(i).getChild("subLocations");
            if(sublocations!=null) {
                List<Element> locations = sublocations.getChildren("Location");
                if(locations!=null) {
                    for(int j =0; j < locations.size(); j++) {
                        String location = locations.get(j).getAttributeValue("name");
                        //System.out.println("Prohledavam lokaci: "+location);
                        Element genies = locations.get(j).getChild("genies");
                        if(genies!=null) {
                            List<Element> geniesL = genies.getChildren();
                            if(geniesL!=null) {
                                for(int k =0; k < geniesL.size(); k++) {
                                    String geniesId = geniesL.get(k).
                                            getAttributeValue("geniusId");
                                    ////System.out.println("Lokace ma genia : "+locName );
                                    LocationGenius locGen = new LocationGenius();
                                    locGen.setLocationGeniusID(geniesId);
                                    String className = geniesL.get(k).
                                            getAttributeValue("className");
                                    locGen.setClassName(className);
                                    List<Element> tlglist = geniesL.get(k).
                                            getChildren("TopLevelGoal");
                                    if(tlglist!=null)       
                                        for(int m = 0; m < tlglist.size(); m++) {
                                            String process = tlglist.get(m).
                                                    getAttributeValue("process");
                                            String goal = tlglist.get(m).
                                                    getAttributeValue("goal"); 
                                            LocationTopLevelGoal ltlg = new 
                                                    LocationTopLevelGoal(process,goal);
                                            locGen.addLocationTLG(ltlg);
                                            
                                        }
                                    List<Element> cglist = geniesL.get(k).
                                            getChildren("CleaningGoal");
                                    if(cglist!=null)
                                        for(int m =0; m < cglist.size(); m++) {
                                            String goalId = cglist.get(m).
                                                    getAttributeValue("goalId");
                                            // we named cleaningGoal like its ID
                                            String name = "name"+goalId;
                                            int count = 1;
                                            while(CleaningGoalDB.isIn(name)) {
                                                name = "name"+goalId+count;
                                                count++;
                                            }                                            
                                            Element ontology = cglist.get(m).
                                                    getChild("OntologyToken");
                                            CleaningGoal cg = new CleaningGoal();
                                            cg.setName(name);
                                            cg.setGoalID(goalId);
                                            cg.setExpression(new Expression(ontology));
                                            locGen.addCleaningGoal(cg);   
                                            // we add cleaning goal only
                                            // if loc gen not exist in DB
                                            if(!LocationGeniusDB.isIn(locGen))
                                                CleaningGoalDB.addCleaningGoal(cg);
                                        }
                                    LocationGeniusDB.addLocationGenius(locGen);
                                    
                                    ArrayList<String> upLocations = 
                                            getUpLocations(locName,listloc);
                                    if (upLocations !=null) {
                                        for(int l =0; l < upLocations.size(); l++) {
                                            String loc = upLocations.get(l) +"."+
                                                    location;
                                            //System.out.println("Vytvarim lokaci");
                                            upLocations.set(l,loc);                                            
                                            LocationGeniusDB.getLocationGenius(
                                                geniesId).addLocation(upLocations.get(l));                                            
                                        }                                                   
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //update GeniusCleaningGoalmodel
        cgmodel.setValues(CleaningGoalDB.getCleaningGoalList());
    }
    
    /**
     * Create root location genies from xml
     */
    void fillInRootLocationGenies(List <Element>listloc) {        
        if(listloc==null)
            return;
        for(int i =0; i < listloc.size(); i++) {
            String locName = listloc.get(i).getAttributeValue("name");
            Element ch = listloc.get(i).getChild("genies");
            if(ch==null)
                continue;
            List<Element> genList = ch.getChildren();
            for(int j =0; j < genList.size(); j++) {
                LocationGenius lg = new LocationGenius();
                lg.setLocationGeniusID(genList.get(j).getAttributeValue("geniusId"));
                lg.setClassName(genList.get(j).getAttributeValue("className"));
                List<Element> tlgList = genList.get(j).getChildren("TopLevelGoal");
                for(int k =0; k < tlgList.size(); k++) {
                    LocationTopLevelGoal ltlg = new LocationTopLevelGoal();
                    ltlg.setGoal(tlgList.get(k).getAttributeValue("goal"));
                    ltlg.setProcess(tlgList.get(k).getAttributeValue("process"));
                    lg.addLocationTLG(ltlg);
                }
                lg.addLocation(locName);
                LocationGeniusDB.addLocationGenius(lg);
            }
        }        
    }
    /**
     * Used while find uplocations in locations templates.
     */
    private ArrayList<String> getUpLocations(String template, List<Element> listloc) {
        ArrayList<String> locationsList = new ArrayList();
        for(int i =0; i < listloc.size(); i++) {
            Element graphL = listloc.get(i);
            String locName = graphL.getAttributeValue("name");
            Element subLocations = graphL.getChild("subLocations");
            if(subLocations != null) {
                List<Element> locList = subLocations.getChildren("Location");
                if(locList != null) {
                    for(int j =0; j < locList.size(); j++) {
                        String name = locList.get(j).getAttributeValue("name");
                        String templ = locList.get(j).getAttributeValue("template");
                        if(templ.equals(template)) {
                            ArrayList<String> subLocs = getUpLocations(locName,listloc);
                            if(subLocs != null) {
                                for(int k =0; k < subLocs.size(); k++) {
                                    locationsList.add(subLocs.get(k)+"."+name);
                                }                            
                            } //find root location !! not World
                            else locationsList.add(rootLocation+"."+name);
                        }
                    }                 
                }
            }                           
        }
        if(locationsList.size() > 0)
            return locationsList;
        return null;
    }
    
   /**
    * Set EntGenius in MOL
    */ 
   public void setEntGeniusInMOL() {

        entToGenius = new HashMap();
        Set<String> ents = new HashSet();
        ArrayList<EntGenius> eglist = EntGeniusDB.getEntGeniusList();
        for(int i =0; i < eglist.size(); i++) {
            ArrayList<String> entsList = eglist.get(i).getEntList();
            if(entsList!=null)
                for(int j =0; j < entsList.size(); j++) {
                    ents.add(entsList.get(j));
                }
        }
        Iterator iter = ents.iterator();
        while (iter.hasNext()) {
            String ent = (String)iter.next();
            for(int i =0; i < eglist.size();i++) {                
                if(eglist.get(i).hasEnt(ent)) {
                    entToGenius.put(ent,eglist.get(i).getEntGeniusID());
                }                    
            }            
        }
                        
        Iterator it = entToGenius.entrySet().iterator();
        StringBuffer buf = new StringBuffer();
        while(it.hasNext()) {
            Map.Entry a = (Map.Entry)it.next();
            String ent = a.getKey().toString();
            String genId = a.getValue().toString();
            serviceModulMOL.setEntGenius(ent, genId);
        }      
   }
   
   /**
    * Set LocationGenius in MOL (lokace, seznam geniu)
    */
   public void setLocationGeniusInMOL() {
       
      locationToGenius = new HashMap();
        Set<String> locations = new HashSet();
        ArrayList<LocationGenius> lglist = LocationGeniusDB.getLocationGeniusList();
        for(int i =0; i < lglist.size(); i++) {
            ArrayList<String> locList = lglist.get(i).getLocationList();
            if(locList!=null)
                for(int j =0; j < locList.size(); j++) {
                    locations.add(locList.get(j));
                }
        }
        Iterator iter = locations.iterator();
        ArrayList<String>genii = new ArrayList();
        while (iter.hasNext()) {
            genii = new ArrayList();
            String loc = (String)iter.next();
            for(int i =0; i < lglist.size();i++) {
                if(lglist.get(i).hasLocation(loc)) {
                    genii.add(lglist.get(i).getLocationGeniusID());
                }                    
            }
            if(genii.size() >0)
                locationToGenius.put(loc,genii);
        }
        
                
        Iterator it = locationToGenius.entrySet().iterator();
        StringBuffer buf = new StringBuffer();
        while(it.hasNext()) {
            Map.Entry a = (Map.Entry)it.next();
            String loc = a.getKey().toString();            
            ArrayList<String> geniiL = (ArrayList<String>)a.getValue();            
            serviceModulMOL.setLocationGenii(loc,geniiL);
        }              
   }
   
   
   /**
    * Add new location to genius
    */
    public void geniusSetToLocation( String geniusName, String locationPath ) {
        LocationGenius lg = LocationGeniusDB.getLocationGenius(geniusName);
        if(lg != null) {
            lg.addLocation(locationPath);
        }
        lgview.updateView();
    }   
    
    public boolean geniusSetToEnt( String geniusName, String entName) {
        ArrayList<EntGenius> glist = EntGeniusDB.getEntGeniusList();        
        for(int i =0; i < glist.size(); i++) {
            if(glist.get(i).getEntGeniusID().equals(geniusName)) {
                EntGeniusDB.getEntGenius(geniusName).addEnt(entName);
                return true;
            }            
        }
        return false;
    }

    /**
     * Return all warnings in Process Template
     */
    private String getProcessWarnings() {
        StringBuffer buf = new StringBuffer();
        ArrayList<ProcessTempl> list = ProcessDB.getList();
        for(int i =0; i < list.size(); i++) {
            String line = new String("");
            boolean pom = false;
            ProcessTempl pt = list.get(i);
            line+="processID="+pt.getProcessID();
            if(pt.getGoalID().equals("")) {
                line += "    goalID is empty";
                pom = true;
            }
            int minLod = pt.getMinLod();
            int maxLod = pt.getMaxLod();
            if(minLod<1) {
                line+= "    wrong minLOD";
                pom = true;
            }
            if(maxLod==-1) {
                line+= "    wrong maxLOD";
                pom = true;
            }
            if(minLod > maxLod) {
                line+= "    minLOD is higher than maxLOD";
                pom = true;
            }
            if(pt.getClassName().equals("")) {
                line+= "    className is empty";
                pom = true;
            }
            if(pt.getPSources().getPSourcesList().size() == 0) {
                line+= "    missing sources";
                pom = true;
            }
            String goals = new String("");
            Vector<Goal> glist = pt.getPExpansion().getGoalList();
            for(int j =0; j < glist.size(); j++) {
                if(glist.get(j).getGSources().getGSources().size()==0)
                    goals+=" "+glist.get(j).getGoalID();
            }
            if(goals.length() > 0) {
                line+="    missing sources in expansion: "+goals;
                pom = true;
            }
            
            if(pom)
                buf.append(line+"\n");
                
        }
        // Find goal that have no implemented process
        Set<String> notImplementedGoals = new TreeSet<String>();
        for(int i =0; i < list.size(); i++) {
            Vector<Goal> goals = list.get(i).getPExpansion().getGoalList();
            for(int j =0; j < goals.size(); j++) {
                if(ProcessDB.getImplementedProcesses(goals.get(j).getGoalID())
                    == null) {
                    notImplementedGoals.add(goals.get(j).getGoalID());
                }
            }            
        }
        if(!notImplementedGoals.isEmpty()) {
            buf.append("\n");
            Object[] obj = notImplementedGoals.toArray();
            for(int j = 0; j < obj.length; j++) {
                buf.append("Goal with goalID " + obj[j] +" has none implementing process.");
            }
        }
        // Check wheater java files exists
        String sourcep = serviceComponentProject.getProcessPath();
        TreeSet<String> missingf = new TreeSet<String>();
        for(int i =0; i < list.size(); i++) {
            String cname = list.get(i).getClassName();
            if(!cname.equals(""))
                cname = cname.substring(15);
            if(!cname.equals("")) {
                File f = new File(sourcep+File.separator+cname+".java");
                if(!f.exists()) {                                                        
                    missingf.add(cname+".java");
                }
            }
        }
        Object[] pom = missingf.toArray();
        for(int i =0; i < pom.length; i++) {
            if(
                    pom[i].equals("PermanentProcess.java") ||
                    pom[i].equals("CommonDelegatedProcessTemplate.java") ||
                    pom[i].equals("Wait.java") ||
                    pom[i].equals("Step.java") ||
                    pom[i].equals("HuntingStep.java") ||
                    pom[i].equals("FollowProcessTemplate.java") ||
                    pom[i].equals("Exchange.java") ||
                    pom[i].equals("Say.java") ||
                    pom[i].equals("Panic.java")) {
                buf.append("\n Missing file in sources: "+pom[i]+" " +
                        "( Not needed. It is build-in process. )");
            }
            else buf.append("\n Missing file in sources: "+pom[i]);
        }
        
        // Check java class for processes in sources
        // Wheater all java classes are used
        File procDir = new File(sourcep);
        String[] files = procDir.list();
        ArrayList<String> procJavaFile = ProcessDB.getProcessJavaClass();    
        
        for(int i =0; i < files.length; i++) {
            if(!procJavaFile.contains(files[i])) {
                buf.append("\n File "+files[i]+" in sources is not used by any process.");
            }
        }
        buf.append("\n");
        return buf.toString();
    }

    /**
     * Return all warning in EntGeniusDB
     */
    private String getEntGeniusWarnings() {
        StringBuffer buf = new StringBuffer();
        ArrayList<EntGenius> list = EntGeniusDB.getEntGeniusList();
        for(int i =0; i < list.size(); i++) {
            String line = new String("");
            EntGenius eg = list.get(i);
            boolean pom = false;
            line+="geniusID="+eg.getEntGeniusID();
            if(eg.getEntTLGoalsList().size()==0) {
                line+="    missing TopLevelGoal";
                pom = true;
            }
            if(eg.getEntTLGoalsList().size() > 0) {
                ArrayList<EntTopLevelGoal> tlglist = eg.getEntTLGoalsList();                    
                String goalswithoutprocess = new String("");
                for(int j =0; j < tlglist.size(); j++) {
                    ArrayList<String> p = ProcessDB.getImplementedProcesses(tlglist.get(j).getGoalId());
                    if(p==null)
                        goalswithoutprocess+=tlglist.get(j).getGoalId()+";";
                }
                if(goalswithoutprocess.length() > 0) {
                    line+="    TLG without implemented process: "+goalswithoutprocess;
                    pom = true;
                }
            }
            if(eg.getEntList().size()==0) {
                line+="    genius is unused in world";
                pom = true;
            }
            if(pom)
                buf.append(line+"\n");
        }
        return buf.toString();
    }

    /**
     * Return all warnings in LocationGeniusDB
     */
    private String getLocationGeniusWarnings() {
        StringBuffer buf = new StringBuffer();
        ArrayList<LocationGenius> list = LocationGeniusDB.getLocationGeniusList();
        for(int i =0; i < list.size(); i++) {
            String line = new String("");
            LocationGenius lg = list.get(i);
            boolean pom = false;
            line+="geniusID="+lg.getLocationGeniusID();
            if(lg.getLocationtlglist().size()==0) {
                line+="    missing TopLevelGoal";
                pom = true;
            }
            if(lg.getClassName().equals("")) {
                line+="    className is empty";
                pom = true;
            }
            if(lg.getLocationList().size()==0) {
                line+="    genius is unused in world";
                pom = true;
            }
            if(pom)
                buf.append(line+"\n");
        }
        // Check wheater java files exists
        String sourcep = serviceComponentProject.getGeniusPath();
        TreeSet<String> missingf = new TreeSet<String>();
        for(int i =0; i < list.size(); i++) {
            String cname = list.get(i).getClassName();
            if(!cname.equals(""))
                cname = cname.substring(14);
            if(!cname.equals("")) {
                File f = new File(sourcep+File.separator+cname+".java");
                if(!f.exists()) {                                                        
                    missingf.add(cname+".java");
                }
            }
        }
        Object[] pom = missingf.toArray();
        for(int i =0; i < pom.length; i++) {   
            if(
                    pom[i].equals("AreaGeniusImpl.java") ||
                    pom[i].equals("QueueGenius.java") ||
                    pom[i].equals("BasicGenius.java") ) {
                
                    buf.append("\n Missing file in sources: "+pom[i] +"" +
                     "( Not needed. It is build-in genius. )");    
            }
            else    buf.append("\n Missing file in sources: "+pom[i] );
            
        }
        // Check java class for loc gen in sources
        // Wheater all java classes are used
        File procDir = new File(sourcep);
        String[] files = procDir.list();
        ArrayList<String> procJavaFile = LocationGeniusDB.getProcessJavaClass();
        for(int i = 0; i < files.length; i++) {
            if(!procJavaFile.contains(files[i])) {
                buf.append("\n File "+files[i]+" in sources is not used by " +
                        "any location genius class.");
            }
        }
        buf.append("\n");        
        return buf.toString();
    }
   
    public void setEnabledMPG(boolean b) {
        setEnabled(b);
    }

    /** Check validity of given Element */
    private boolean isValidExpr(Element expr) {
        //
        XMLOutputter outputter = new XMLOutputter( org.jdom.output.Format.getPrettyFormat().setIndent("\t"));                                        
        File file = new File(serviceComponentProject.getMPGPath()+File.separator+"tempxmlfile.xml");        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));            
            bw.write(outputter.outputString(expr.getChildren()));
            bw.close();
        } catch (IOException e) {
            return false;
        }        
        
        SAXBuilder builder = new SAXBuilder();
        Document docum = new Document();                        
                        
        File iveworldxsd = new File("../default/XML/expressions.xsd");
        try {
            iveworldxsd = iveworldxsd.getCanonicalFile();            
        } catch (IOException ex) {
            return false;
        }
        if(!iveworldxsd.exists()) {           
            return false;
        }
        XMLChecker checker = new XMLChecker(iveworldxsd.toString());                
        try {    
            docum = builder.build(file);            
            if(!checker.checkValidity(docum)) {
                file.delete();
                return false;
            }
        }
        catch (IOException e) {                        
            file.delete();
            return false;
            
        }
        catch (JDOMException e) {            
            file.delete();
            return false;            
        }
        catch(Exception e){           
            file.delete();
            return false;            
        }        
        file.delete();
        return true;
    }
        
}

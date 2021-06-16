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
 
package IVE_Editor.PlugIns.Components.Project;

import IVE_Editor.GUI;
import IVE_Editor.GuiServices;
import IVE_Editor.PlugIns.Components.Project.Dialogs.CompilingOutputDialog;
import IVE_Editor.PlugIns.Components.Project.Dialogs.NewProjectDialog;
import IVE_Editor.PlugIns.Components.Project.Dialogs.ProjectBuildDialog;
import IVE_Editor.PlugIns.Components.Project.Dialogs.SaveProjectAsDialog;
import IVE_Editor.PlugIns.Components.Project.Dialogs.VerificationErrorsDialog;
import IVE_Editor.PlugIns.Components.Project.Dialogs.EditorSettingsDialog;
import IVE_Editor.PlugIns.Components.XMLLoader.XMLLoaderService;
import IVE_Editor.PlugIns.Components.XMLSaver.XMLSaverService;
import IVE_Editor.PlugIns.*;
import IVE_Editor.PlugIns.Moduls.MOL.MOLService;
import IVE_Editor.PlugIns.Moduls.MPG.Gui.Dialogs.WorkingDialog;
import IVE_Editor.PlugIns.Moduls.MPG.MPGService;

import java.util.StringTokenizer; 
import javax.swing.*;
import java.awt.event.*;
import java.io.*; 
import java.util.ArrayList;
import java.util.List;

/** 
 * Class represents Project component.  
 *
 * @author Juhasz Martin
 */
public class Project extends AbstractKomponent implements ActionListener {
    
    // MenuItems definition
    private JMenuItem menuItemNewProject;
    private JMenuItem menuItemOpenProject;
    private JMenu menuOpenRecentProject;
    private JMenuItem menuItemOpenRecentProjectRemove;    
    private JMenuItem menuItemSaveProject;
    private JMenuItem menuItemSaveProjectAs;
    private JMenuItem menuItemCloseProject; 
    private JMenuItem menuItemLoadXML;
    private JMenuItem menuItemSaveXML;
    private JMenuItem menuItemEditorSettings;    
    private JMenuItem menuItemBuildWorld;
    private JMenuItem menuItemRunInIve;
    private JMenuItem menuItemExit;
    // End of MenuItems definiton
    
    // Variable definition    
    /** Select directory of project */
    private String selectDir;    
    /** Name of the project e.g MyDemoworld */
    private String name;
    /** Name of the new project when save project as occures */
    private String newname;
    /** Place where project is created e.g. C:\IVEWorlds */
    private String place; 
    /** Path to MOL dir */
    private String mol;
    /** Path to MPG dir */
    private String mpg;
    /** Path to Saved dir */
    private String saved;
    /** Path to Sources dir */
    private String sources;
    /** Path to cz.ive.genius */
    private String genius;
    /** Path to cz.ive.iveobject */
    private String iveobject;
    /** Path to cz.ive.location */
    private String location;
    /** Path to cz.ive.process */
    private String process;
    /** Path to cz.ive.gui */
    private String gui;
    /** Path to cz.ive.sensors */
    private String sensors;
    /** Path to cz.ive.resources.images */
    private String images;
    /** Path to Complete dir */
    private String complete;   
    /** Path to Compile dir */
    private String compile;
    /** Path to java editor */
    private String javaEditor;
    /** Path to ive.jar */
    private String ivejar;
    /** Path to builded jar world e.g. C:\Worlds\MyProject\Complete\mydemoworld.jar */
   // private String jarworld;
    /** Path to java compiler */
    private String javac; 
    /** Pathto java archiver. Needed for creating jar file */
    private String jar;
    private String classPath;  //  world class path , /bin/demoword.jar
    private String worldSubdirectories;            
    /** Path to EntGenius dir */ 
    private String mpgEntGenius;
    /** Path to entGenius.xml file */
    private String mpgEntGeniusXml;
    /** Path to enttogenius.txt file */
    private String mpgEntToGenius;
    /** Path to LocationGenius dir */
    private String mpgLocationGenius;
    /** Path to locationGenius.xml */
    private String mpgLocationGeniusXml;
    /** Path to locationtogenius.txt file */
    private String mpgLocationToGenius;
    /** Path to Links dir */
    private String mpgLinks;   
    /** Path to links.dat */
    private String mpgLinksDat;
    /** File editorcfg.dat */
    private File editorFile;
    /** File recentprj.dat */
    private File recentPrjFile;
    /** File entgenius.xml */
    private File mpgEntGeniusXmlFile;
    /** File enttogenius.txt */
    private File mpgEntToGeniusFile;
    /** File locationgenius.xml */
    private File mpgLocationGeniusXmlFile;
    /** File locationtogenius.txt */
    private File mpgLocationToGeniusFile;
    /** File links.dat */
    private File mpgLinksFile;        
    /** Output string of compiling project */
    private String compilingOutput;    
    /** Output string of jar */
    private String jarOutput;        
    /** Helper variable if saveProjectAs is clicked */
    private boolean saveProjectAsBool;
    /** List of verification errors */
    private ArrayList<String> verificationErrors;
    /** Used to hold reference for services of modul of processes ang goals */
    private MPGService serviceModulMPG;
    /** Used to hold reference to services of modul of objects and location */
    private MOLService serviceModulMOL;     
    /** Used to hold reference to services of XMLSaver component */
    private XMLSaverService serviceXMLSaver;
    /** Used to hold reference to services of XMLLoader component */
    private XMLLoaderService serviceXMLLoader; 
    /** Used to hold reference to GUI Services of kernel */
    private GuiServices guiServices;
    /** Rererence to main frame of IVE editor */
    private JFrame mainFrame;             
    /** Reference to NewProjectDialog */
    private NewProjectDialog npDialog;
    /** Reference to EditorSettingsDialog */
    private EditorSettingsDialog esDialog; 
    /** Reference to SaveProjectAsDialog */
    private SaveProjectAsDialog spaDialog;
    /** Reference to CompilingOutputDialog */
    private CompilingOutputDialog coDialog;
    /** Reference to VerificationErrorsDialog */
    private VerificationErrorsDialog veDialog;                                           
    /** Reference to ProjectBuildDialog */
    private ProjectBuildDialog pbDialog;
    /** List of listeners to component of Project. */    
    private ArrayList<ProjectListener> projectListeners = new ArrayList< ProjectListener >();
    /** Whether project is open */
    boolean isProjectOpened;
    /** Implicit directory for new projects */
    private File implicitProjectDir;
    /** String variable for implicit project path */
    private String implicitProjectDirPath;    
    // End of variable definition
                    
    /** 
     * Constructor with guiServices. We need to obtain reference to main frame 
     * of IVE Editor 
     */
    public Project( GuiServices guiServices ){        
        this.guiServices = guiServices;
    }
    
    /** 
     * This method occures when loading component to application
     */ 
    public ComponentSettings load()    {

        mainFrame = guiServices.getMainFrame();
        
        serviceModulMPG = (MPGService)pluginsServices().getMPGServices();  
        serviceXMLSaver  = (XMLSaverService)pluginsServices().getXMLSaverServices(); 
        serviceModulMOL = (MOLService)pluginsServices().getMOLServices();          
        serviceXMLLoader = (XMLLoaderService)pluginsServices().getXMLLoaderServices();                  
               
        //-----  Menu -------
        JMenu menuPart = new JMenu( "File" );
        menuItemNewProject = new JMenuItem( "New Project" );
        menuItemOpenProject = new JMenuItem( "Open Project" );
        menuOpenRecentProject = new JMenu( "Open Recent Project" );                
        menuItemOpenRecentProjectRemove = new JMenuItem( "Remove All" );                        
        menuItemSaveProject = new JMenuItem( "Save Project" );        
        menuItemSaveProjectAs = new JMenuItem( "Save Project As");
        menuItemCloseProject = new JMenuItem( "Close Project" );  
        menuItemLoadXML = new JMenuItem( "Load XML" );          
        menuItemSaveXML = new JMenuItem( "Save XML" );   
        menuItemEditorSettings = new JMenuItem( "Editor Settings");
        menuItemBuildWorld = new JMenuItem( "Build World");
        menuItemRunInIve = new JMenuItem( "Run in Ive");
        menuItemExit = new JMenuItem("Exit");
        menuPart.add( menuItemNewProject );
        menuPart.add( menuItemOpenProject );      
        menuItemOpenRecentProjectRemove.setToolTipText("Remove All Items In Recent Opened Projects");
        menuOpenRecentProject.add(menuItemOpenRecentProjectRemove);
        menuPart.add( menuOpenRecentProject );              
        menuPart.addSeparator();
        menuPart.add( menuItemSaveProject );    
        menuPart.add( menuItemSaveProjectAs );    
        menuPart.add( menuItemCloseProject );         
        menuPart.addSeparator();
        menuPart.add( menuItemLoadXML );            
        menuPart.add( menuItemSaveXML );           
        menuPart.addSeparator();
        menuPart.add( menuItemEditorSettings );                        
        menuPart.addSeparator();
        menuPart.add( menuItemBuildWorld );                                
        menuPart.add( menuItemRunInIve );                
        menuPart.addSeparator(); 
        menuPart.add( menuItemExit );

        menuPart.setMnemonic( KeyEvent.VK_F );
                
        getMenu().addMenu( menuPart );
        
        menuItemNewProject.addActionListener(new NewProjectListener());
        menuItemOpenProject.addActionListener(new OpenProjectListener());  
        menuItemOpenRecentProjectRemove.addActionListener(new OpenRecentProjectRemoveListener());          
        menuItemSaveProject.addActionListener(new SaveProjectListener());     
        menuItemSaveProjectAs.addActionListener(new SaveProjectAsListener());     
        menuItemCloseProject.addActionListener(new CloseProjectListener());
        menuItemLoadXML.addActionListener(new LoadXMLListener());        
        menuItemSaveXML.addActionListener(new SaveXMLListener());   
        menuItemEditorSettings.addActionListener(new EditorSettingsListener());        
        menuItemBuildWorld.addActionListener(new BuildWorldListener());        
        menuItemRunInIve.addActionListener(new RunInIveListener());                
        menuItemExit.addActionListener(new ExitListener()); 
        
        menuItemSaveProject.setEnabled(false);
        menuItemSaveProjectAs.setEnabled(false);
        menuItemCloseProject.setEnabled(false);
        menuItemEditorSettings.setEnabled(true);
        menuItemBuildWorld.setEnabled(false);
        menuItemRunInIve.setEnabled(false);
        menuItemExit.setEnabled(true);
        menuItemLoadXML.setEnabled(false);
        menuItemSaveXML.setEnabled(false);    
        
        menuItemNewProject.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));        
        menuItemOpenProject.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));                
        menuItemSaveProject.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));        
        menuItemCloseProject.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));        
        menuItemEditorSettings.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        menuItemLoadXML.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));        
        menuItemBuildWorld.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        menuItemRunInIve.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        menuItemExit.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        
        verificationErrors = new ArrayList<String>();               
                
        npDialog = new NewProjectDialog(mainFrame, true, this);           
        esDialog = new EditorSettingsDialog(mainFrame, true, this );  
        spaDialog = new SaveProjectAsDialog(mainFrame, true, this);
        coDialog = new CompilingOutputDialog(mainFrame,true);
        veDialog = new VerificationErrorsDialog(mainFrame,true,this);
        
        javac = new String("");
        jar = new String("");
        ivejar = new String("");

        sources = new String("");
        complete = new String("");
        compile = new String("");
        classPath = new String("");
        javaEditor = new String("");
        worldSubdirectories = new String("");
        mpgEntGenius = new String("");
        mpgEntGeniusXml =  new String("");
        mpgEntToGenius  = new String("");
        mpgLocationGenius  = new String("");  
        mpgLocationGeniusXml = new String("");
        mpgLocationToGenius = new String("");
        mpgLinks = new String("");
        compilingOutput = new String("");
        jarOutput = new String("");
        
        selectDir = new String("");     
        name = new String("");
        place = new String("");
        saved = new String("");
        sources = new String("");
        genius = new String("");
        iveobject = new String("");
        location = new String("");
        process = new String("");
        sensors = new String("");
        images = new String("");
        gui = new String("");
        complete = new String("");
        compile = new String("");                            
        worldSubdirectories = new String("");                  
        isProjectOpened = false;
        editorFile = new File("../settings/editorcfg.dat");        
        File settingsDir = new File("../settings");        
        try {
            settingsDir = settingsDir.getCanonicalFile();                       
        } 
        catch(IOException e) {            
        }
        if(!settingsDir.exists())
            settingsDir.mkdirs();
        try {
            editorFile = editorFile.getCanonicalFile();          
            
        } catch(IOException e) {            
        }
        recentPrjFile = new File("../settings/recentprj.dat");
        try {
            recentPrjFile = recentPrjFile.getCanonicalFile();            
        } catch(IOException e) {            
        }               
        if(!recentPrjFile.exists()) {            
            try {
                recentPrjFile.createNewFile();
            } 
            catch (IOException ex) {         
            }
        }        
        if(!editorFile.exists()) {            
            try {
                editorFile.createNewFile();
            } catch (IOException ex) {         
            }
        }        
        implicitProjectDir = new File("../projects");
        try {
            implicitProjectDir = implicitProjectDir.getCanonicalFile(); 
            implicitProjectDirPath = implicitProjectDir.toString();
        } catch (IOException ex) {            
        }      
        if(!implicitProjectDir.exists()) {
            implicitProjectDir.mkdirs();
        }         
        
        pbDialog = new ProjectBuildDialog(mainFrame,
                "Compiling... Wait Please!!", false);
        pbDialog.setSize(250,70);
        java.awt.Dimension screenSize = 
                java.awt.Toolkit.getDefaultToolkit().getScreenSize();        
        pbDialog.setLocation((screenSize.width-350)/2,(screenSize.height-300)/2);
        pbDialog.setVisible(false);
          
        // load editor settings from config file - editorcfg.dat
        loadEditorSettings(editorFile);
        // load last recently opened projects from - recentprj.dat
        loadRecentProjects(recentPrjFile);
               
        return new ComponentSettings();
    }  
        
    /**
     * Occures when new project is creating
     */
    public class NewProjectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {                       
             npDialog.init(implicitProjectDirPath);
             npDialog.setVisible(true);  
        }
    }; 
    
    /**
     * Occures when project is saving
     */
    public class SaveProjectListener implements ActionListener {
        public void actionPerformed(ActionEvent e)  {                        
            saveProjectAction();
        }        
    };    
    
    /** Occures when save project as is invoked */
    public class SaveProjectAsListener implements ActionListener {
        public void actionPerformed(ActionEvent e)  {    
            saveProjectAsAction();
        }
    };
    
    /**
     * Occures when project is opening
     */
    public class OpenProjectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {  
            openProjectAction();                                         
        }
    };
    
    /**
     * Occures when recently project delete     
     */
    public class OpenRecentProjectRemoveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {  
            openRecentProjectRemoveAction();                                         
        }
    };    
    
    /**
     * Occures when recently project item is selected 
     */
    public class OpenRecentProjectItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {  
            openRecentProjectItemAction(e);                                         
        }
    };        
    
    /**
     * Occures when project is closing. Frees all datas and sets items in menu
     * for right state. Send message to other registred moduls and components
     */
    public class CloseProjectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {  
            closeProjectAction();
        }
    };     
        
    /** Occures when loading from xml file */
   public class LoadXMLListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {                    
            loadXMLAction();                                    
        } 
   };     
    
    /**
     * Occures when files is saving to xml file
     */
    public class SaveXMLListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            saveXMLAction();                
        }
    }
       
    /** 
     * Occures when Editor Setting dialog is shown 
     * We set all variables in Editor Setting
     */
    public class EditorSettingsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {                        
            esDialog.setJavaCompiler(javac);
            esDialog.setJavaEditor(javaEditor);
            esDialog.setJavaJar(jar);       
            esDialog.setIveJar(ivejar);
            esDialog.setVisible(true);
        }
    };         
    
    /**
     * Occures when compile button is clicked
     */
    public class BuildWorldListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {     
            buildAction();                                                                                      
        }
    };        
    
    /**
     * Occures when is clicked on RunInIve button
     */
    public class RunInIveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {            
            runInIveAction();
        }
    };            
    
    /**
     * Occures when is clicked on Exit button
     */
    public class ExitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) { 
            if(exitAction()) {
                System.exit(0);
            }
        }
    };                
                     
    public void onActivate()
    {
    }
    
    public void onDeactivate()
    {
    }            
    
    /**
     * Save project with new name. All needed directories and files from original
     * project are copied to this new project.
     */
    public void saveProjectAs(){
        final String namesaveas = spaDialog.getName();
        String newpath = spaDialog.getPlace();                                        
        final File destNew = new File(newpath+File.separator+namesaveas);
        boolean bnewdir = destNew.mkdir();
        if(!bnewdir) {
            // Creating new directory for 'save project as' failed
            return;
        }                  
        
        final File savedDir = new File(newpath+File.separator+namesaveas+File.separator+"Saved");
        bnewdir = savedDir.mkdirs();
        if(!bnewdir) {
            // Creating new Saved directory for 'save project as' failed.
            return;
        }                          
                
        //Copy defaults        
        Thread t = new Thread( new Runnable() {
            public void run() {
                final WorkingDialog wd = new WorkingDialog(mainFrame);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {                            
                        wd.setVisible(true);     
                        wd.setMessage("Work in progress ...");
                        wd.setProgressMessage("Saving Project As");
                    }
                });
                final File f = new File(savedDir+File.separator+namesaveas+".xml");
                saveProjectAsBool = true;
                newname = namesaveas;
                serviceXMLSaver.saveXML(f);                
                saveProjectAsBool = false;
                            
                File srcDir = new File(place+File.separator+name);
                final File dstDirComplete = destNew;
                
                String[] children = srcDir.list();        
                    for (int i=0; i<children.length; i++) {                    
                    try {                        
                     if(children[i].equals("Saved")) {                        
                        //File saved = new File(srcDir,children[i]);
                        //String[] chld = saved.list();                                                   
                            File savedDir = new File(destNew+""+File.separator+"Saved");
                            savedDir.mkdir();
                            //copy only verification.dat, but change it a litte
                            String veriFile = savedDir.toString()+
                                    File.separator+"verification.dat";
                            //false is mark. it means we created 
                            // verification file with save projet as
                            makeVerificationFile(veriFile.toString(),false);
                        }
                     else if(children[i].equals("Compile")) {
                        File compileDir = new File(destNew.toString()+
                                File.separator+"Compile");
                        boolean bnewCdir = compileDir.mkdirs();
                        if(!bnewCdir) {
                            // Creating new Compile directory 
                            // for save project as failed
                            return;
                        }  
                     }
                     else if(children[i].equals("Complete")) {
                        File completeDir = new File(destNew.toString()+File.separator+"Complete");
                        boolean bnewCdir = completeDir.mkdirs();
                        if(!bnewCdir) {
                           // Creating new Complete directory for save 
                           // project as failed
                            return;
                        }  
                     }
                     else {                                                                    
                        copyDirectory(new File(srcDir, children[i]),
                            new File(dstDirComplete, children[i]));                                                                              
                     }
                    } 
                    catch(IOException e) {                    
                    }
                }                            
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        wd.setVisible(false);
                        wd.dispose();
                    }
                });                    
            }   
        });
        t.start();                
    }
        
    public void setJavaCompiler(String path) {
        javac = path;
    }
    
    public void setJavaJar(String path) {
        jar = path;
    }
    
    public void setJavaEditor(String path) {
        javaEditor = path;
    }
    
    public void setIveJar(String path) {
        ivejar = path;
    }
       
    public void setClassPath(String path) {
        classPath = path;
    }
       
    public void setWorldSubdirectories(String path) {
        worldSubdirectories = path;
    }
       
    public String getJavaCompiler() {
        return javac;
    }
    
    public String getJavaJar() {
        return jar;
    }        
    
    public String getJavaEditorPath() {
        return javaEditor;
    }  
    
    
    public String getIveJar() {
        return ivejar;
    }        
       
    public String getClassPath() {
        return classPath;
    }
       
    public String getPathDirectoryIMAGE() {
        return images;
    }
    
    public String getWorldName() {
        return name;
    }    
    
    public String getProcessPath() {
        return process;
    }
    
    public String getWorldSubdirectories() {
        return worldSubdirectories;
    }
       
    public String getProjectPath() {
        return place+File.separator+name;
    }
    
    public String getProjectSouces() {
        return sources;
    }
    
    
    public String getGuiPath() {
        return gui;
    }
    
    public String getSensorsPath() {
        return sensors;
    }
        
    
    public void addListener( ProjectListener listener ){
        projectListeners.add( listener );
    }    
    
    public void setName(String n) {
        name = n;
    
    }
    public void setPlace(String m) {
        place = m;
    }

    public JFrame getMainFrame()
    {
        return mainFrame;
    }            
     
    /**
     * Return list of classpath items 
     *
     * @return list of classpath items e.g. SchoolDemoworld.jar */
    public  ArrayList<String> getClassPathList() {
      if(saveProjectAsBool) {
        ArrayList<String> list = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(classPath,";");
        while(tok.hasMoreTokens()) {
            list.add((String)tok.nextToken());
        }
        if (worldSubdirectories.length() == 0) {
            String classToJar = newname+".jar";
            list.add(classToJar);
        }
        else if(worldSubdirectories.length() > 0) {
            String classToJar = worldSubdirectories+"/"+newname+".jar";
            list.add(classToJar);
        }
        return list;          
      }
    
        ArrayList<String> list = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(classPath,";");
        while(tok.hasMoreTokens()) {
            list.add((String)tok.nextToken());
        }
        if (worldSubdirectories.length() == 0) {
            String classToJar = name+".jar";
            list.add(classToJar);
        }
        else if(worldSubdirectories.length() > 0) {
            String classToJar = worldSubdirectories+"/"+name+".jar";
            list.add(classToJar);
        }
        return list;
    }
      
    /**
     * return Service of this component
     *
     * @return Services of this component */
    public ProjectService getService() {
        return new ProjectService(this);
    }  
    
    /** 
     * Fill int classpathitem structure */
    public void fillInClassPathItems(List cplist) {
        // We don't class path items
    }        

    /**
     * Return path to location dir
     *
     * @return Path to cz.ive.location dir */
    public String getLocationPath() {
        return location;
    }

    /** 
     * Return path to genius dir
     *
     * @return Path to cz.ive.genius dir */
    public String getGeniusPath() {
        return genius;
    }

    /** 
     * Return path to object dir
     * @return Path to cz.ive.object dir */
    public String getIveObjectPath() {
        return iveobject;
    }
    
    /**
     * Return path to MPG dir
     * @return Path to MPG dir 
     */
    public String getMPGPath() {
        return mpg;
    }
        
    /**
     * Return file entgenius.xml
     *
     * @return File entgenius.xml 
     */    
    public File getMpgEntGeniusXmlFile() {
        return mpgEntGeniusXmlFile;
    }
    
    /** 
     * return File enttogenius.dat 
     * @return File enttogenius.dat 
     */
    public File getMpgEntToGeniusFile() {
        return mpgEntToGeniusFile;
    }    
    
    /**     
     * return file locationgenius.xml 
     * @return File locationgenius.xml 
     */    
    public File getMpgLocationGeniusXmlFile() {
        return mpgLocationGeniusXmlFile;
    }
    
    /** 
     * return file locationtogenius.dat
     * @return File locationtogenius.dat
     */    
    public File getMpgLocationToGeniusFile() {
        return mpgLocationToGeniusFile;
    }   
    
    /** 
     * return file links.dat
     * @return File links.dat
     */        
    public File getMpgLinksFile() {
        return mpgLinksFile;
    }
    
    /** 
     * Implicit project directory named 'projects' is located in instalation directory 
     * of IVE editor 
     *
     * @return Implicit project directory
     */        
    public String getImplicitProjectDirPath() {
        return implicitProjectDirPath;
    }
                
    /**
     * Used in compiling all java classes
     */
    private void compileAll() {
    	List<String> cmdLineList = new ArrayList<String>();
		cmdLineList.add(javac);
		cmdLineList.add("-cp");
		cmdLineList.add(ivejar);		
		compilingOutput = "";
		jarOutput = "";
        
        File helpexecutor = new File(sources+""+File.separator+"cz"+File.separator+"ive");
        int nhelpexecutor = 0;
        if(helpexecutor.listFiles()!=null)
            nhelpexecutor = helpexecutor.listFiles().length;
        if (nhelpexecutor > 0) {
        	String helperExecutor =sources+File.separator+"cz"+File.separator+"ive"+File.separator+"HelperExecutor.java"; 
        	cmdLineList.add(helperExecutor);
        }
        
        File geniusf = new File(genius);
        int ngenius = 0;
        File[] gfiles = geniusf.listFiles();
        if(gfiles !=null) 
            ngenius = gfiles.length;
                    
        if (ngenius > 0) {
            getJavaClasses(new File(sources+File.separator+"cz"+File.separator+"ive"+File.separator+"genius"), cmdLineList);                        
        }
        
        File object = new File(iveobject);            
        int nobject = 0;
        File[] ofiles = object.listFiles();
        if(ofiles !=null) 
            nobject = ofiles.length;  
        
        if (nobject > 0) {             
            getJavaClasses(new File(sources+File.separator+"cz"+File.separator+"ive"+File.separator+"iveobject"), cmdLineList);      
        }
        
        File locationf = new File(location);
        int nlocation = 0;
        File[] lfiles = locationf.listFiles();
        if(lfiles !=null) 
            nlocation = lfiles.length;   
        
        if (nlocation > 0) {
            //command += " " +sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"location"+File.separator+"*.java";
            getJavaClasses(new File(sources+File.separator+"cz"+File.separator+"ive"+File.separator+"location"), cmdLineList);                                                                                    
        }
                   
        File processf = new File(process);
        int nprocess = 0;
        File[] pfiles = processf.listFiles();
        if(pfiles !=null) 
            nprocess = pfiles.length;            
        if (nprocess > 0) {
            getJavaClasses(new File(sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"process"), cmdLineList);            
        }            
        
        File sensor = new File(sensors);
        int nsensor= 0;
        File[] sfiles = sensor.listFiles();
        if(sfiles !=null) 
            nsensor = sfiles.length;    
        
        if (nsensor > 0) {
            getJavaClasses(new File(sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"sensors"), cmdLineList);
        }
        
        File guif = new File(gui);
        int ngui = 0;
        File[] guifiles = guif.listFiles();
        if(guifiles !=null) 
            ngui = guifiles.length;            
        
        if (ngui > 0) {
            getJavaClasses(new File(sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"gui"), cmdLineList);            
        }
        
        cmdLineList.add("-d");
                
        File checkE = new File(compile);
        if(!checkE.exists())
            checkE.mkdirs();
        
        cmdLineList.add(compile);
                
        String commandArr[] = new String[cmdLineList.size()];
        int i = 0;
        for (String s: cmdLineList) {
        	commandArr[i] = s;
        	i++;
        }
        	
        try {            	            
        	        
            Process child = Runtime.getRuntime().exec(commandArr);                                                                            
            InputStream compileErrorInput = child.getErrorStream();
            InputStream compileInput = child.getInputStream();
            int ch;
            final StringBuffer buf = new StringBuffer();                
            while((ch = compileErrorInput.read()) != -1) {                                         
                buf.append((char)ch);                           
            }
            compileErrorInput.close();                                    
            String compilingErrOutput = buf.toString();            
            compilingOutput = compilingErrOutput+"\n";
            final StringBuffer buf2 = new StringBuffer();                
            while((ch = compileInput.read()) != -1) {                                         
                buf2.append((char)ch);                           
            }
            compileInput.close();                                    
            compilingOutput += buf2.toString();
                        
            try {
				child.waitFor();
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}                		
                //if compiling not succeded
            if(child.exitValue()>0) {
                	JOptionPane.showMessageDialog(mainFrame,
                			"COMPILING FAILED: \n"
                            +"\nCheck EditorSettings values",
                        "Compiling failed", JOptionPane.WARNING_MESSAGE);
                	compilingOutput = compilingErrOutput+"\n";
                	compilingOutput += buf2.toString();
                    return;              
            }
            } catch (IOException e) {                
                
                JOptionPane.showMessageDialog(mainFrame,"COMMAND FAILED: \n"+
                        "Check EditorSettings values",
                    "Compiling failed", JOptionPane.WARNING_MESSAGE);                
                return;                            
            }            
            try {                
                copyDirectory(new File(sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"resources"),
                    new File(compile+""+File.separator+"cz"+File.separator+"ive"+File.separator+"resources"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //End of Compiling;
            
            // Make complete jar file
            classToJar();                                            
    }        
    
   /**
     * Create jar file with all sources and compiled java classes
     */
     public void classToJar() {
    	 jarOutput = "";
         String command[] = new String[6];
        try {                   
            
            File jarDir = new File(complete+File.separator+new File(worldSubdirectories));                        
            jarDir.mkdirs(); 
            File jarname = new File(jarDir+File.separator+name+".jar");            
                                               
            command[0] = jar;
            command[1] = "-cfv";            
            command[2] = jarname.toString();
            command[3] = "-C";
            command[4] = compile;
            command[5] = ".";
                                  
            Process child2 = Runtime.getRuntime().exec(command);                        
            InputStream is = child2.getInputStream();
            StringBuffer buf = new StringBuffer();
            int ch;        
            while((ch = is.read()) != -1) {                                          
                buf.append((char)ch);                        
            }            
            InputStream es = child2.getErrorStream();                          
            while((ch = es.read()) != -1) {                                          
                buf.append((char)ch);                   
            }                        
            if(buf.length() > 0) {
                jarOutput=buf.toString();
            }            
        }                  
        catch (IOException e) {
            String com = new String("");
            for(int i =0; i < command.length; i++) {
                com+=command[i]+" ";
            }
            JOptionPane.showMessageDialog(mainFrame,"COMMAND FAILED: \n"+com,
                "Jar process failed", JOptionPane.WARNING_MESSAGE);                
                return;                           
        }                
    }
       
    public void actionPerformed(ActionEvent e) {
    }    
    
    /** 
     * Save project to xml file xmlfile 
     * @param xmlfile File where complete world is saved
     */
    public void saveProject(File xmlfile) {            
            serviceXMLSaver.saveXML(xmlfile);                        
            makeVerificationFile(place+"//"+name+
                    "//Saved//"+"verification.dat",true);
    }
    
    /** 
     * Loads editor settings from file
     *
     * @param file File from which is loaded editor settings
     */
    private void loadEditorSettings(File file) {
        if(file == null || !file.exists()) {            
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            StringBuffer buf = new StringBuffer();
            while((line = br.readLine()) != null) {
                buf.append(line);
            }
            
            StringTokenizer st = new StringTokenizer(buf.toString(),"*");
            while(st.hasMoreTokens()) {
                StringTokenizer st2 = new StringTokenizer((String)st.nextToken(),"=");
                if(st2==null)
                    continue;
                if(!st2.hasMoreTokens())
                    continue;                
                String key = (String)st2.nextToken();
                if(!st2.hasMoreTokens())
                    continue;
                String value = (String)st2.nextToken();
                if (key.equals("javacompiler")) {
                    if(value!=null && !value.equals("")) {
                        javac = value;
                    }
                }
                if (key.equals("javaarchive")) {
                    if(value!=null && !value.equals("")) {
                        jar = value;
                    }
                }                
                if (key.equals("javaeditor")) {
                    if(value!=null && !value.equals("")) {
                        javaEditor = value;
                    }
                }                  
                if (key.equals("ivejarfile")) {
                    if(value!=null && !value.equals("")) {
                        ivejar = value;
                    }
                }                               
                if (key.equals("classpathitems")) {
                    if(value!=null && !value.equals("")) {
                        classPath = value;
                    }
                }   
                if (key.equals("worldsubdirectories")) {
                    if(value!=null && !value.equals("")) {
                        worldSubdirectories = value;
                    }
                }                                
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    /** 
     * Load recentprj.dat a than build menu items in open recent project item      
     * @param file File reprezented recent opened projects
     */
    private void loadRecentProjects(File file) {
        if(!file.exists())
            return;
        
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            ArrayList<String> list = new ArrayList<String>();
            String line;
            while((line = br.readLine()) != null) {                
                list.add(line);
            }
            menuOpenRecentProject.removeAll();              
            JMenuItem menuItemOpenRecentProjectItem;        
            for(int i = 0; i <list.size(); i++) {
                StringTokenizer t = new StringTokenizer(list.get(i),";");
                String name = t.nextToken();
                String path = t.nextToken();
                if(name == null || path == null ||
                        name.equals("") || path.equals(""))
                    continue;
                else {
                    menuItemOpenRecentProjectItem = new JMenuItem(name);
                    menuItemOpenRecentProjectItem.setToolTipText(path);
                    menuItemOpenRecentProjectItem.addActionListener(
                            new OpenRecentProjectItemListener());
                    menuOpenRecentProject.add(menuItemOpenRecentProjectItem);                    
                }                
            }
            // At least 'remove' item is added             
            menuOpenRecentProject.addSeparator();            
            menuOpenRecentProject.add(menuItemOpenRecentProjectRemove);
        } 
        catch (IOException e) {            
        }        
    }
    
    /** 
     * Helper method. 
     * Create new recentprj.dat, refresh recent opened projects 
     */
    private void saveRecentProjects(File file) {
        if(!file.exists()) {
            File settingsDir = new File("../settings");        
            try {
                settingsDir = settingsDir.getCanonicalFile();                       
            } catch(IOException e) {            
            }
            if(!settingsDir.exists()) {
                settingsDir.mkdirs();
            }
            recentPrjFile = new File("../settings/recentprj.dat");
            try {
                recentPrjFile = recentPrjFile.getCanonicalFile();
            } catch(IOException e)  {                
            }               
            if(!recentPrjFile.exists()) {            
                try {
                    recentPrjFile.createNewFile();
                } catch (IOException ex) {         
                }
            }
        }
        
        BufferedReader br;
        ArrayList<String> list = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(file));
            
            String current = name+";"+place+File.separator+name;
            String line;
            while((line = br.readLine()) != null) {                
                if(line.equals(current))
                    continue;
                list.add(line);
            }
            list.add(current);            
            br.close();            
        } catch (IOException e) {            
        }   
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for(int i = list.size() -1; i>= 0; i--) {
                bw.write(list.get(i)+"\n");
            }
            bw.close();
        } catch(IOException e) {
            
        }        
    }    
    
    /**
     * Saves editor settings to config file - editorcfg.dat 
     */
    public void saveEditorSettings() {        
        File settingsDir = new File("../settings");        
        try {
            settingsDir = settingsDir.getCanonicalFile();                       
        } catch(IOException e) {            
        }
        if(!settingsDir.exists()) {
            settingsDir.mkdirs();
        }
        try {
        BufferedWriter bw = new BufferedWriter(new FileWriter(editorFile));
        bw.write("javacompiler="+javac+"*");
        bw.write("javaarchive="+jar+"*");
        bw.write("javaeditor="+javaEditor+"*");
        bw.write("ivejarfile="+ivejar+"*");
        //bw.write("pathtoive="+ive);
        bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
         
    /** Helper method for copying directories */
    private void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }    
            String[] children = srcDir.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(srcDir, children[i]),
                                     new File(dstDir, children[i]));
            }
        } else {
            copyFile(srcDir, dstDir);
        }
    }


    /** Helper method for copying files */
    private void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
    
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
 
 
    /**
    * Opens project named by dirPath
     * @param dirPath Path to project
    */
    public void openProjectByPath(String dirPath)  {
        if(dirPath==null)
            return;
        File selectDirP = new File(dirPath);               
        name = selectDirP.getName();        // set name of Project
        selectDir = dirPath;                // set directory of project
        place = selectDirP.getParent();     // set dir place of project
        
        openProject();      
    }
 
    /**
    * Opens project, set variables and load xml 
    */
    public void openProject() {
        if(selectDir != null) {
            saved = selectDir+ ""+File.separator+"Saved";
            process = selectDir +""+File.separator+"Sources"+File.separator+"cz"+File.separator+"ive"+File.separator+"process";
            images = selectDir +""+File.separator+"Sources"+File.separator+"cz"+File.separator+"ive"+File.separator+"resources"+File.separator+"images";
            genius = selectDir +""+File.separator+"Sources"+File.separator+"cz"+File.separator+"ive"+File.separator+"genius";
            location = selectDir + ""+File.separator+"Sources"+File.separator+"cz"+File.separator+"ive"+File.separator+"location";
            sensors = selectDir +""+File.separator+"Sources"+File.separator+"cz"+File.separator+"ive"+File.separator+"sensors";
            iveobject = selectDir +""+File.separator+"Sources"+File.separator+"cz"+File.separator+"ive"+File.separator+"iveobject";
            gui = selectDir +""+File.separator+"Sources"+File.separator+"cz"+File.separator+"ive"+File.separator+"gui";
            sources = selectDir + ""+File.separator+"Sources";
            mol = selectDir+ ""+File.separator+"MOL";
            mpg = selectDir+ ""+File.separator+"MPG";
            complete = selectDir + ""+File.separator+"Complete";
            compile = selectDir + ""+File.separator+"Compile";
            mpgEntGenius = mpg+""+File.separator+"EntGenius";
            mpgEntGeniusXml = mpgEntGenius + File.separator + "entgenius.xml";
            mpgEntToGenius = mpgEntGenius + File.separator + "enttogenius.dat";
            mpgLocationGenius = mpg + ""+File.separator+"LocationGenius";
            mpgLocationGeniusXml = mpgLocationGenius + File.separator +
                    "locationgenius.xml";
            mpgLocationToGenius = mpgLocationGenius + File.separator + 
                    "locationtogenius.dat";
            mpgLinks = mpg+""+File.separator+"Links";
            mpgLinksDat = mpgLinks+File.separator+"links.dat";        
            mpgEntGeniusXmlFile = new File(mpgEntGeniusXml);            
            mpgLocationGeniusXmlFile = new File(mpgLocationGeniusXml);            
            mpgEntToGeniusFile = new File(mpgEntToGenius);            
            mpgLocationToGeniusFile = new File(mpgLocationToGenius);    
            mpgLinksFile = new File(mpgLinksDat);                
            File savedF = new File(selectDir+File.separator+"Saved"+
                    File.separator+name+".xml");
            if(savedF.exists()) {
                final File saved = savedF;
                Thread t = new Thread( new Runnable() {
                    public void run() {
                        final WorkingDialog wd = new WorkingDialog(mainFrame);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {                            
                                wd.setVisible(true);                   
                                wd.setMessage("Work in progress ...");
                                wd.setProgressMessage("Project Opening");
                            }
                        });
                        boolean isValid = serviceXMLLoader.loadXML(saved);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                wd.setVisible(false);
                                wd.dispose();
                            }
                        });       
                        if(!isValid) {
                            return;
                        }
                        guiServices.getMainFrame().setTitle(name);
                        saveRecentProjects(recentPrjFile);
                        loadRecentProjects(recentPrjFile);
                    
                        menuItemNewProject.setEnabled(false);
                        menuItemOpenProject.setEnabled(false);
                        menuOpenRecentProject.setEnabled(false);
                        menuItemSaveProject.setEnabled(true);
                        menuItemSaveProjectAs.setEnabled(true);
                        menuItemCloseProject.setEnabled(true);      
                        menuItemEditorSettings.setEnabled(true);        
                        menuItemBuildWorld.setEnabled(true);
                        menuItemRunInIve.setEnabled(true);
                        menuItemLoadXML.setEnabled(true);
                        menuItemSaveXML.setEnabled(true);                            
                    
                        for(int i = 0; i < projectListeners.size(); i++) {
                            projectListeners.get(i).openProjectEvent();
                        }                                               
                    }   
                });
                t.start();
                isProjectOpened = true;                                
            }
            else {
                JOptionPane.showMessageDialog(mainFrame,"Recently saved xml world is missing!\n",
                    "Wrong project opening", JOptionPane.WARNING_MESSAGE);                
                return;
            }                                                                                  
        } 
    }
 
    /** Create verification file. */
    private void makeVerificationFile(String file,boolean tmp) {
        File verificateFile = new File(file);
        ArrayList<String> sourceList = new ArrayList<String>();
     
        goThroughDir(new File(place+"//"+name), sourceList);          
     
        try {            
            BufferedWriter bw = new BufferedWriter(new FileWriter(verificateFile));
            //modify sourceList
            int length = new File(place+"//"+name).toString().length();
            String savedxml = saved.replace('\\', '/')+'/'+name+".xml";
            for(int i =0; i < sourceList.size(); i++) {
                if(!tmp) {
                    if(sourceList.get(i).contains("Compile"))
                     continue;
                    if(sourceList.get(i).contains("Complete"))
                     continue;
                    if(sourceList.get(i).equals(savedxml)) {
                        String sub = savedxml.substring(0,saved.length());
                        sub+='/'+newname+".xml";
                        sourceList.set(i,sub);
                    }      
                }
                sourceList.set(i,sourceList.get(i).substring(length));
                bw.write(sourceList.get(i)+"\n");
            }
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }    
    }
 
 
    /** Check if all recently saved files are still in opening project */
    private boolean checkVerification() {
        File verifile = new File(place+""+File.separator+""+name+""+File.separator+"Saved"+File.separator+"verification.dat");
        if(!verifile.exists()) {
            verificationErrors.add(verifile.toString());  
            veDialog.setText(" " + verifile.toString());     
            return false;
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(verifile));
            String line;
            while((line = br.readLine()) != null) {
                File newfile = new File(place+""+File.separator+""+name+""+File.separator+""+line);
                if(!newfile.exists())
                    verificationErrors.add(newfile.toString());
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch( IOException ex) {
            ex.printStackTrace();
        }
        
        String errors = new String("");
        for(int i =0; i < verificationErrors.size(); i++) {
            errors += " "+ verificationErrors.get(i)+"\n";
        }
        veDialog.setText(errors);     
     
        return verificationErrors.size() == 0;
    } 
 
    /** 
     * Helper method.
     * Go throught src dir and collect all files to list 
     */
     private void goThroughDir(File src, ArrayList<String> sourceList) {
        if(src.isDirectory()) {
            String []list = src.list();
            for(int i =0; i < list.length; i++) {
                goThroughDir(new File(src, list[i]), sourceList);
            }
        }
        else          
            sourceList.add(src.toString().replace('\\', '/'));
    }
     /** 
      * Return java classes in dir as a string 
      *
      * @param dir Path to directory
      * @param cmdLineList List for command line files
      */             
     private static void getJavaClasses(File dir, List<String> cmdLineList) {
         File children[] = dir.listFiles(
                 new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".java");
             }});            
         
         for(int i =0; i < children.length; i++) {
             if(children[i].isFile()) {
             	String javafile = dir.toString()+File.separator+children[i].getName();
              //javafiles+=" \"" +dir.toString()+""+File.separator+""+children[i].getName()+"\"";
             	cmdLineList.add(javafile);
             }
             else {
             	List<String> tmpLineList = new ArrayList<String>();
                 getJavaClasses(new File(dir+""+File.separator+""+children[i].getName()), tmpLineList);
                 cmdLineList.addAll(tmpLineList);
             }
         }        
     }
 
   
    /** Set menuItems to correct enabled-disabled state */   
    private void setMenuItemsAfterCloseProject() {        
        menuItemNewProject.setEnabled(true);
        menuItemOpenProject.setEnabled(true);
        menuOpenRecentProject.setEnabled(true);
        menuItemSaveProject.setEnabled(false);
        menuItemSaveProjectAs.setEnabled(false);
        menuItemCloseProject.setEnabled(false);              
        menuItemBuildWorld.setEnabled(false);
        menuItemRunInIve.setEnabled(false);
        menuItemLoadXML.setEnabled(false);
        menuItemSaveXML.setEnabled(false);                    
    }
    
    /** 
    * Set variables to empty string. 
    * Needed when project is close and open again 
    */
    private void setVariablesToEmpty() {        
        name = "";
        place = "";
        selectDir ="";
        mol = "";
        mpg = "";
        saved = "";
        sources = "";
        genius ="";
        iveobject ="";
        location = "";
        process = "";
        sensors ="";
        gui ="";
        images = "";
        complete = "";
        compile = "";
        classPath = "";      
        isProjectOpened = false;
    }
    
    /** Occures when is clicked on new project item in menu File*/
    public void newProjectAction() {                
        File srcDir = new File("../default/");
        String[] children = srcDir.list(); 
        if(children==null) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Missing default directory with sources! Can't create new project.",
                "Missing default directory with sources",
                    JOptionPane.WARNING_MESSAGE);  
            return;
        }
        
        implicitProjectDir = new File("../projects");
        try {
            implicitProjectDir = implicitProjectDir.getCanonicalFile(); 
            implicitProjectDirPath = implicitProjectDir.toString();
        } catch (IOException ex) {
            //ex.printStackTrace();
        }      
        if(!implicitProjectDir.exists()) {
            implicitProjectDir.mkdirs();
        }                

        menuItemNewProject.setEnabled(false);
        menuItemOpenProject.setEnabled(false);            
        menuOpenRecentProject.setEnabled(false);        
        menuItemSaveProject.setEnabled(true); 
        menuItemSaveProjectAs.setEnabled(true);
        menuItemCloseProject.setEnabled(true);
        menuItemEditorSettings.setEnabled(true);            
        menuItemBuildWorld.setEnabled(true);          
        menuItemRunInIve.setEnabled(true);  
        menuItemLoadXML.setEnabled(true);
        menuItemSaveXML.setEnabled(true);
        
        if(name.equals("")) 
            name = npDialog.getName();
        if(place.equals("")) {
            place = npDialog.getPlace();
            selectDir = place+File.separator+name;
        }
        
        mol = place+File.separator+name+File.separator+"MOL";
        mpg = place+File.separator+name+File.separator+"MPG";
        mpgEntGenius = place+File.separator+name+File.separator+
                "MPG"+File.separator+"EntGenius";
        mpgEntGeniusXml = mpgEntGenius + File.separator + "entgenius.xml";
        mpgEntToGenius = mpgEntGenius + File.separator + "enttogenius.dat";               
        mpgLocationGenius = place+File.separator+name+File.separator+
                "MPG"+File.separator+"LocationGenius";
        mpgLocationGeniusXml = mpgLocationGenius + File.separator + "locationgenius.xml";
        mpgLocationToGenius = mpgLocationGenius + File.separator+ "locationtogenius.dat";               
        mpgLinks = place+File.separator+name+File.separator+
                "MPG"+File.separator+"Links";
        mpgLinksDat = mpgLinks + File.separator + "links.dat";                
        saved = place+File.separator+name+File.separator+"Saved";                
        sources = place+File.separator+name+File.separator+"Sources";
        complete = place+File.separator+name+File.separator+"Complete";
        compile = place+File.separator+name+File.separator+"Compile";        
        genius = sources + ""+File.separator+"cz"+File.separator+"ive"+File.separator+"genius";
        iveobject = sources + ""+File.separator+"cz"+File.separator+"ive"+File.separator+"iveobject";
        location = sources + ""+File.separator+"cz"+File.separator+"ive"+File.separator+"location";
        process = sources + ""+File.separator+"cz"+File.separator+"ive"+File.separator+"process";
        images = sources + ""+File.separator+"cz"+File.separator+"ive"+File.separator+"resources"+File.separator+"images";
        sensors = sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"sensors";
        gui = sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"gui";
        
        guiServices.getMainFrame().setTitle(name);
        verificationErrors = new ArrayList<String>();           
        
        boolean bdir = (new File(place+File.separator+name)).mkdirs();
        //if(!bdir)
            //System.out.println("Failed directory creating "+place+"//"+name);
        
        boolean bmol = (new File(mol)).mkdir();
        //if(!bmol)
            //System.out.println("Failed directory creating " + mol);
        
        boolean bmpg = (new File(mpg)).mkdir();
        //if(!bmpg)
            //System.out.println("Failed directory creating "+mpg);
        
        boolean bmpgEntG = (new File(mpgEntGenius)).mkdir();
        //if(!bmpgEntG)
            //System.out.println("Failed directory creating "+mpgEntGenius);
        
        boolean bmpgLocG = (new File(mpgLocationGenius)).mkdir();
        //if(!bmpgLocG)
            //System.out.println("Failed directory creating "+mpgLocationGenius);
        
        boolean bmpglink = (new File(mpgLinks)).mkdir();
        //if(!bmpglink)
            //System.out.println("Failed directory creating "+mpgLinks);
        
        boolean bsaved = (new File(saved)).mkdir();
        //if(!bsaved)
            //System.out.println("Failed directory creating "+saved);
             
        boolean bgenius = (new File(genius)).mkdirs();
        //if(!bgenius)
            //System.out.println("Failed directory creating "+genius);
        
        boolean biveobject = (new File(iveobject)).mkdir();
        //if(!biveobject)
            //System.out.println("Failed directory creating "+iveobject);
        
        boolean blocation = (new File(location)).mkdir();
        //if(!blocation)
            //System.out.println("Failed directory creating "+location);
        
        boolean bprocess = (new File(process)).mkdir();
        //if(!bprocess)
            //System.out.println("Failed directory creating "+process);
        
        File sensorF = new File(sensors);
        boolean bsensors = sensorF.mkdirs();
        //if(!bsensors)
            //System.out.println("Failed directory creating "+sensors);
        
        File guiF = new File(gui);
        boolean bgui = guiF.mkdirs();
        //if(!bgui)
            //System.out.println("Failed directory creating "+gui);
        
        File imagesF = (new File(images));
        boolean bimages = imagesF.mkdirs();
        //if(!bimages)
            //System.out.println("Failed directory creating "+images);

        File completeF = new File(complete);
        boolean bcomplete = completeF.mkdir();
        //if(!bcomplete)
            //System.out.println("Failed directory creating "+complete);
        
        boolean bcompile = (new File(compile)).mkdir();
        //if(!bcompile)
            //System.out.println("Failed directory creating "+compile);
        
        boolean bmpgEntGXmlFile = false;
        try {
            mpgEntGeniusXmlFile = new File(mpgEntGeniusXml);            
            bmpgEntGXmlFile = mpgEntGeniusXmlFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(mpgEntGeniusXmlFile));
            bw.write("<genies />");
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        boolean bmpgLocationGXmlFile = false;
        try {
            mpgLocationGeniusXmlFile = new File(mpgLocationGeniusXml);            
            bmpgLocationGXmlFile = mpgLocationGeniusXmlFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(mpgLocationGeniusXmlFile));
            bw.write("<genies />");
            bw.close();            
        } catch (IOException ex) {
            ex.printStackTrace();
        }        
        boolean bmpgEntGToObjectFile = false;
        try {
            mpgEntToGeniusFile = new File(mpgEntToGenius);            
            bmpgEntGXmlFile = mpgEntToGeniusFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        boolean bmpgLocationGToLocationFile = false;
        try {
            mpgLocationToGeniusFile = new File(mpgLocationToGenius);            
            bmpgLocationGToLocationFile = mpgLocationToGeniusFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();            
        }                
        boolean bmpgLinksFile = false;
        try {
            mpgLinksFile = new File(mpgLinksDat);            
            bmpgLinksFile = mpgLinksFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }                        
                  
        //Copy defaults                
        File dstDirComplete = completeF;
        File dstDirImages = imagesF;        
            for (int i=0; i<children.length; i++) {
                try {
                    if(children[i].equals("images"))
                        copyDirectory(new File(srcDir, children[i]),
                                     new File(sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"resources",
                                        children[i]));  
                    if(children[i].equals("classes")) {
                        File classes = new File(srcDir,children[i]);
                        String[] chld = classes.list();
                        for(int j =0; j < chld.length; j++) {
                            if(chld[j].equals("paths.xml"))
                                continue;
                            else if(chld[j].equals("SensorEye.java"))
                                copyFile(new File(srcDir+""+File.separator+"classes",chld[j]),
                                        new File(sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"sensors",chld[j]));
                            else if(chld[j].equals("AreaGeniusImpl.java"))
                                copyFile(new File(srcDir+""+File.separator+"classes",chld[j]),
                                        new File(sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"genius",chld[j]));
                            else if(chld[j].equals("HelperExecutor.java"))
                                copyFile(new File(srcDir+""+File.separator+"classes",chld[j]),
                                        new File(sources+""+File.separator+"cz"+File.separator+"ive",chld[j]));
                            else
                                copyFile(new File(srcDir+""+File.separator+"classes",chld[j]),
                                        new File(sources+""+File.separator+"cz"+File.separator+"ive"+File.separator+"process",chld[j]));
                        }                       
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        
            for(int i = 0; i < projectListeners.size(); i++) {
                projectListeners.get(i).newProjectEvent();  
            } 
        isProjectOpened=true;
    }
    
    /**
     * Create new project named projectName in implicit directory and
     * load do it xmlFile
     *
     */
    public void newProjectAction(String xmlFilePath, String projectName) {
        implicitProjectDir = new File("../projects");
        try {
            implicitProjectDir = implicitProjectDir.getCanonicalFile();      
        } catch (IOException ex) {
            //ex.printStackTrace();
        }      
        if(!implicitProjectDir.exists()) {
            implicitProjectDir.mkdirs();
        }        
        
        if(isInImplicitDir(projectName)) {
            JOptionPane.showMessageDialog(mainFrame,"Name of this project exists!\n",
                "Wrong project name", JOptionPane.WARNING_MESSAGE);  
            return;        
        }
        File implDir = new File("../projects");
        try {
            implDir = implDir.getCanonicalFile();
            name = projectName;
            place = implDir.toString();
            newProjectAction();
        } catch (IOException ex) {
           //ex.printStackTrace();
        }
        if(xmlFilePath == null || xmlFilePath.equals(""))
            return;
        File xmlFile = new File(xmlFilePath);        
        
        loadFileFromXML(xmlFile.toString());        
    }
    
    /**
     * check whether project exist in implicit directory
     * ../projects
     */
    private boolean isInImplicitDir(String projectName) {
        File impldir = new File("../projects");
        try {
            impldir = impldir.getCanonicalFile();
            File listf[] = impldir.listFiles();
            if(listf!=null) {
                for(int i=0; i < listf.length; i++) {
                    if(listf[i].getName().equals(projectName))
                        return true;
                }
            }
        } catch (IOException ex) {
            //ex.printStackTrace();
            return true;
        }
        return false;
    }
    
    /** Occures when open project item is selected */
    private void openProjectAction() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int state = fc.showOpenDialog(mainFrame);
        if (state == JFileChooser.APPROVE_OPTION) {
            selectDir = fc.getSelectedFile().toString();
            name = fc.getSelectedFile().getName();
            place = fc.getSelectedFile().getParent().toString();
        }
        else return;       
        
        // just first empty old errors
        verificationErrors = new ArrayList<String>();           
        if(!checkVerification()) {
            JOptionPane.showMessageDialog(mainFrame,
                        "Show missing files or directories", 
                        "Project corrupted",
                        JOptionPane.ERROR_MESSAGE);
            veDialog.setVisible(true);
        }
        else {
            openProject();            
        }
    }
    
    /** Remove all recent projects from recentPrj.dat */
    private void openRecentProjectRemoveAction()    {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(recentPrjFile));
            bw.close();
        } catch (IOException e) {
            
        }
        loadRecentProjects(recentPrjFile);
    }
    
    /** Occures when is clicked on item in openRecentProject */
    private void openRecentProjectItemAction(ActionEvent e) {
        String selectDirP = ((JMenuItem)e.getSource()).getToolTipText();        
        File selectDirPFile = new File(selectDirP);
        if(selectDirPFile.exists()) {
            name = selectDirPFile.getName();
            selectDir = selectDirP;
            place = selectDirPFile.getParent();            
            
            // just first empty old errors
            verificationErrors = new ArrayList<String>();           
            if(!checkVerification()) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Show missing files or directories", 
                        "Project corrupted",
                        JOptionPane.ERROR_MESSAGE);
                veDialog.setVisible(true);
            }
            else
                openProject();                 
        }else {
            JOptionPane.showMessageDialog(mainFrame,
                        "Project was removed!", 
                        "Wrong path to the project",
                        JOptionPane.ERROR_MESSAGE);
            
        }
            
    }
    
    /** Do save project */
    private void saveProjectAction() {        
        final File savedfile = new File(saved+File.separator+name+".xml");            
            Thread t = new Thread( new Runnable() {
                public void run() {
                    final WorkingDialog wd = new WorkingDialog(mainFrame);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {                            
                                    wd.setVisible(true);                   
                                    wd.setMessage("Work in progress ...");
                                    wd.setProgressMessage("Project Saving");
                                }
                            });   
                            saveProject(savedfile);
                            saveRecentProjects(recentPrjFile);
                            loadRecentProjects(recentPrjFile);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    wd.setVisible(false);
                                    wd.dispose();
                                }
                            });                    
                }
            });
            t.start();                 
            
            for(int i = 0; i < projectListeners.size(); i++) {
                projectListeners.get(i).saveProjectEvent();                
            }           
    }
    
    /** Save project as action */
    private void saveProjectAsAction() {
        spaDialog.setVisible(true);  
        spaDialog.setSize(new java.awt.Dimension(500, 400));        
    }
    
    /** Do close project */
    private void closeProjectAction() {
        final File savedfile = new File(saved+File.separator+name+".xml");
        final int what = JOptionPane.showConfirmDialog(mainFrame,"Save before close?",
                "Save project", JOptionPane.YES_NO_CANCEL_OPTION); 
        if( what == JOptionPane.CANCEL_OPTION)
            return;
            
        if( what == JOptionPane.NO_OPTION ) {
             if(!savedfile.exists()) {                                             
                 deleteProjectAction(new File(place+File.separator+name));
             }
        }
        if( what == JOptionPane.YES_OPTION ) {
        Thread t = new Thread( new Runnable() {
            public void run() {
                final WorkingDialog wd = new WorkingDialog(mainFrame);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {                            
                        wd.setVisible(true);                   
                        wd.setMessage("Work in progress ...");
                        wd.setProgressMessage("Project Closing");
                    }
                });
                if ( what==JOptionPane.YES_OPTION )
                    saveProject(savedfile);
                    
                makeVerificationFile(place+"//"+name+"//Saved//"+"verification.dat",true);
                saveRecentProjects(recentPrjFile);
                loadRecentProjects(recentPrjFile);
                for(int i = 0; i < projectListeners.size(); i++) {
                    projectListeners.get(i).closeProjectEvent();
                }
                       
                setMenuItemsAfterCloseProject();                     
                setVariablesToEmpty();
                guiServices.getMainFrame().setTitle("IVE Editor");
                                                                        
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        wd.setVisible(false);
                        wd.dispose();
                    }
                });                    
            }
        });
        t.start();       
        } else {
            Thread t = new Thread( new Runnable() {
            public void run() {
                final WorkingDialog wd = new WorkingDialog(mainFrame);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {                            
                        wd.setVisible(true);                   
                        wd.setMessage("Work in progress ...");
                        wd.setProgressMessage("Project Closing");
                    }
                });
                for(int i = 0; i < projectListeners.size(); i++) {                    
                    projectListeners.get(i).closeProjectEvent();
                }                       
                setMenuItemsAfterCloseProject();                     
                setVariablesToEmpty();
                guiServices.getMainFrame().setTitle("IVE Editor");                                                                                    
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        wd.setVisible(false);
                        wd.dispose();
                    }
                });                    
            }
            });
            t.start();                         
        }
        // empties models and views
    }
    
    /** Occures when load xml item is selected */
    private void loadXMLAction() {        
            String selectedFile = new String();        
            JFileChooser fc = new JFileChooser();        
            int state = fc.showOpenDialog(mainFrame);
            if(state == JFileChooser.CANCEL_OPTION)
                return;
            if (state == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile().toString();       
            }    
            loadFileFromXML(selectedFile);                              
    }
    
    /**
     * Loads xml file to project
     *
     * @param file Load this xml file
     */
    public void loadFileFromXML(String file) {
        if(file != null) {
                //System.out.println("Jedem loadxml");
                final String loadedFile = file;
                Thread t = new Thread( new Runnable() {                    
                    public void run() {
                        final WorkingDialog wd = new WorkingDialog(mainFrame);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {                            
                                wd.setVisible(true);                  
                                wd.setMessage("Work in progress ...");
                                wd.setProgressMessage("XML Loading");
                            }
                        });     
                        serviceModulMPG.setEnabledMPG(false);
                        
                        serviceXMLLoader.loadXML(new File(loadedFile));     
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                wd.setVisible(false);
                                wd.dispose();
                            }
                        });                           
                        menuItemNewProject.setEnabled(false);
                        menuItemOpenProject.setEnabled(false);
                        menuItemSaveProject.setEnabled(true);
                        menuItemSaveProjectAs.setEnabled(true);                        
                        menuItemCloseProject.setEnabled(true); 
                        
                        for(int i = 0; i < projectListeners.size(); i++) {
                            projectListeners.get(i).openProjectEvent();
                        }    
                        serviceModulMPG.setEnabledMPG(true);
                    }
                });
                t.start();
            }
            else return;         
    }
    
    /** Occures when save xml item is selected */
    private void saveXMLAction() {
            JFileChooser fc = new JFileChooser();
            int state = fc.showSaveDialog(mainFrame);
            String selectedFile = null;
            if (state == JFileChooser.APPROVE_OPTION) 
                selectedFile = fc.getSelectedFile().toString();    
            if(selectedFile!=null) {
                final String savedFile = selectedFile;
                Thread t = new Thread( new Runnable() {                    
                    public void run() {
                        final WorkingDialog wd = new WorkingDialog(mainFrame);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {                            
                                wd.setVisible(true);     
                                wd.setMessage("Work in progress ...");
                                wd.setProgressMessage("XML Saving");
                            }
                        });
                        serviceXMLSaver.saveXML(new File(savedFile));                    
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                wd.setVisible(false);
                                wd.dispose();
                            }
                        });                    
                    }   
                });
                t.start();    
            } else return;               
    }
    
    /** Build World - compile all sources, create output xml and jar file */
    private void buildAction() {
        if(ivejar==null || ivejar.equals("")) {
            JOptionPane.showMessageDialog(mainFrame,"Set path for ive.jar file!\n",
            "ive.jar file not set", JOptionPane.WARNING_MESSAGE);                
            return;
        }            
        if(javac==null || javac.equals("")) {
            JOptionPane.showMessageDialog(mainFrame,"Set path for java compiler!\n",
            "Java compiler not set", JOptionPane.WARNING_MESSAGE);                
            return;
        }                
        if(jar==null || jar.equals("")) {
            JOptionPane.showMessageDialog(mainFrame,"Set path for jar archive!\n",
            "Jar archive not set", JOptionPane.WARNING_MESSAGE);                
            return;
        }                        
        Thread t = new Thread( new Runnable() {
            public void run() {
                final WorkingDialog wd = new WorkingDialog(mainFrame);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {                            
                        wd.setVisible(true);                   
                        wd.setMessage("Work in progress");
                        wd.setProgressMessage("Compiling");
                    }
                });
                compileAll();
                File outputxml = new File(complete+File.separator+name+".xml");
                wd.setProgressMessage("XML File Building");
                serviceXMLSaver.saveXML(outputxml);                        
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {                       
                        wd.setVisible(false);
                        wd.dispose();
                    }
                });                    
                SwingUtilities.invokeLater(new Runnable() {                    
                    public void run() {
                        coDialog.setTextCompiler(compilingOutput);
                        coDialog.setTextJar(jarOutput);
                        coDialog.setVisible(true);
                    }
                });                                                
            }   
        });
        t.start();        
    }
    
    /** Runs world in IVE */    
    public void runInIveAction() {        
        if(selectDir == null || name == null)
            return;
        if(selectDir.equals("") || name.equals(""))
            return;
        if(!isProjectOpened) 
            return;
        
        String command[] = new String[5];
        command[0] = "java"; 
        command[1] = "-jar";
        command[2] = ivejar;
        command[3] = "-w";
        command[4] = complete+File.separator+name+".xml";
        try {
            Process child2 = Runtime.getRuntime().exec(command);    
            child2.getErrorStream();            
        } catch (IOException ex) {
            String com = new String("");
            for(int i =0; i < command.length; i++) {
                com+=command[i]+" ";
            }
            JOptionPane.showMessageDialog(mainFrame,"COMMAND FAILED: \n"+com,
                "Run in IVE failed", JOptionPane.WARNING_MESSAGE);                
                return;                  
        }             
    }
    
    /** Exits from IVE Editor */
    private boolean exitAction() {
       if(!menuItemSaveProject.isEnabled())
            return true;
        
         final File savedf = new File(saved+File.separator+name+".xml");
         
         int what = JOptionPane.showConfirmDialog(mainFrame,"Save before exit?",
                "Save project", JOptionPane.YES_NO_CANCEL_OPTION); 
         
         if(what==JOptionPane.CANCEL_OPTION)
             return false;
         if(what==JOptionPane.NO_OPTION) {
             if(!savedf.exists()) {                                             
                 deleteProjectAction(new File(place+File.separator+name));
             }
             return true;
         }
         
         if(what==JOptionPane.YES_OPTION) {                                                           
                            
            Thread t = new Thread( new Runnable() {
                        public void run() {
                            
                            final WorkingDialog md = new WorkingDialog(mainFrame);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {                            
                                    md.setVisible(true);                   
                                    md.setMessage("Work in progress ...");
                                    md.setProgressMessage("Project Saving");
                                }
                            });                            
                            saveProject(savedf);   
                            saveRecentProjects(recentPrjFile);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    md.setVisible(false);
                                    md.dispose();
                                    }
                                });                    
                            //System.out.println("Skoncil jsem savovani");
                }   
            });
            GUI.waitForThreadBeforeExit( t );
            t.start();             
           
         }
         return true;        
    }
    
    /** 
     * Helper method
     * Delete project
     * @param file File to delete
     */
    private void deleteProjectAction(File file) {
        if(file.isDirectory()) {
            File list[] = file.listFiles();
            if(list.length > 0) {                
                for(int i =0; i < list.length; i++ )
                    deleteProjectAction(list[i]);
            }
            file.delete();
        }
        if(file.isFile())
            file.delete();
    }    
    
    /**
     * Return true if project canExit
     * @return true if project canExit
     */
    public boolean canExit() {
        return exitAction();
    }

}
   
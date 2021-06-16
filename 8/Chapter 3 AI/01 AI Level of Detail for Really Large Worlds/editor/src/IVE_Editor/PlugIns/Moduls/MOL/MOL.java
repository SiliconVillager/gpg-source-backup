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
 
package IVE_Editor.PlugIns.Moduls.MOL;

import IVE_Editor.Debug.*;
import IVE_Editor.GuiServices;
import IVE_Editor.PlugIns.AbstractModul;
import IVE_Editor.PlugIns.Components.Project.ProjectListener;
import IVE_Editor.PlugIns.ModulSettings;
import IVE_Editor.PlugIns.Moduls.MOL.ClassManager.ClassManager;
import IVE_Editor.PlugIns.Moduls.MOL.ExternEditors.*;
import IVE_Editor.PlugIns.Moduls.MOL.ExternEditors.LinksEditor;
import IVE_Editor.PlugIns.Moduls.MOL.ExternEditors.AttributesEditor;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.Collector.CollectorViewPanel;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.Collector.ObjectCollectorController;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.Collector.ObjectCollectorListener;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.ControllPanel;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.GraphicManager.GraphicManager;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.GraphicManager.GraphicManagerListener;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.GraphicTemplates.GraphicTemplatesView;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.Icons.IconBag;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.ViewWorld.GridViewPane;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.ViewWorld.ViewWorld;
import IVE_Editor.PlugIns.Moduls.MOL.Inspector.Inspector;
import IVE_Editor.PlugIns.Moduls.MOL.Inspector.InspectorController;
import IVE_Editor.PlugIns.Moduls.MOL.GUI.Inspector.InspectorView;
import IVE_Editor.PlugIns.Moduls.MOL.Kinds.KindsManager;
import IVE_Editor.PlugIns.Moduls.MOL.Kinds.KindsManagerView;
import IVE_Editor.PlugIns.Moduls.MOL.Locations.*;
import IVE_Editor.PlugIns.Moduls.MOL.Log.*;
import IVE_Editor.PlugIns.Moduls.MOL.ObjectCollector.ObjectCollector;
import IVE_Editor.PlugIns.Moduls.MOL.ObjectCollector.WTObject;
import IVE_Editor.PlugIns.Moduls.MOL.Repository.*;
import IVE_Editor.PlugIns.PluginsServices;
import IVE_Editor.PlugIns.Service;
import java.io.*;
import java.net.URISyntaxException;
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.font.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.border.*;
import org.jdom.*;
import org.jdom.output.*;

/**
 *
 * @author Jirka
 */
public class MOL extends AbstractModul implements ActionListener, ProjectListener {
    
    /* services of GUI, which are passed by core while instanciating this modul */
    private GuiServices _guiServices;
    
    static final String LOG_FILE = "../log.txt";
    
    private JPanel _panel;    
    private JPanel _panelTools;
    private JPanel _panelControls;
    private JPanel _panelViewWorld;
    private JPanel _panelStatusBar;
    private JPanel _panelSplitPane;     
       
    /* MOL's components */
    
    /** Model of ObjectInspector component. */
    private Inspector _insp;
    /** View of ObjectInspector component. */
    private InspectorView _inspview;
    /** Controller of ObjectInspector component. */
    private InspectorController _inspcontroller;
    /** Connections view. */
    private ConnectionsView _connectionsView;
    
    /** Model of ObjectCollector component. */
    private ObjectCollector _objcol;
    /** View of ObjectCollector component. */
    private CollectorViewPanel _objcol_view;
    /** Controller of ObjectCollector component. */
    private ObjectCollectorController _objcol_ctrl;
    
    /** Manager of graphic. */
    private GraphicManager _graphic_manager;
    
    /** Manager of classes. */
    private ClassManager _class_manager;
    
    /** Manager of kinds. */
    private KindsManager _kindsManagerModel;
    
    /** Objects repository. */
    private Repository _repository;  
    
    /** Component managing everything concerning the locations ( templates, hierarchy etc. ). */
    private LocationManager _locationManager;
    
    /** Manager of kinds. */
    private KindsManagerView _kindsManagerView;
    
    private GraphicTemplatesView _graphicTemplatesView;    
    
    /* Contains View, which must be inicialized in load() method, because it 
     * realized some new graphic components and it needs to get reference on main frame of the application for creating the dialog;
     * the reference is obtained in LinksEditor from GUI via GuiServices, which are passed to the LinksEditor*/
    private LinksEditor _linksEditor;
    private AttributesEditor _attributesEditor;
    private KindsEditor _kindsEditor;
    private PlacementInLocationEditor _placementToLocationEditor;
    private SensorsEditor _sensorsEditor;
    
    //auxiliary fields for proper filling the LocationManager by the locations from XML
    private List<Element> _locationsXML = null;
    private Element _rootLocationXML = null;    
    
    private boolean sw = false;

    private JLabel _labelStatusBar;
    private JLabel _labelViewWorld;
    private JLabel _labelControls;
    private JLabel _labelObjectCollector;
    private JLabel _labelTools;
    private JSplitPane _splitPane1;
    private JSplitPane _splitPane2;    

    /** Temporary menu of this modul. */
    private JMenu _windowsMenu;
    
    private JMenuItem _locTreeMenuItem;
    
    private JMenuItem _connMenuItem;
    
    private JToolBar _toolBar;
    
    private JTabbedPane _tabbedPaneControls;
    
    private ControllPanel _controllPanel;
    
    
    //ladeni
//    private WTObject obj1;
//    private WTObject obj2;    

  
   
    
    //===================================================================================================
    /* Constructors */   
    
    public MOL( GuiServices guiServices ) {
        _guiServices = guiServices;
        _insp = new Inspector();        
    }

    //===================================================================================================
    /* AbstractModul interface */    
    
    public ModulSettings load()
    {       
//        System.out.println("MAX MEMORY = " + Runtime.getRuntime().maxMemory() / 1048576 + "MB");
//        System.out.println("TOTAL MEMORY = " + Runtime.getRuntime().totalMemory() / 1048576 + "MB");
//        System.out.println("FREE MEMORY = " + Runtime.getRuntime().freeMemory() / 1048576 + "MB");
        
        pluginsServices().addListenerToProject( this );

        //pluginsServices().getProjectServices().getPathDirectoryIMAGE();
        
        //pridam ladici menu polozku
//        JMenuItem item = new JMenuItem( "Moje" );
//        item.addActionListener( this );
//        
//        JMenu menu = new JMenu( "Dosla trpelivost" );
//        menu.add( item );
//        getMenu().add( menu );
        
        setEnabled( false );
        
        
        //priklad, jak ziskat sluzby nejakeho jineho modulu ci komponenty
        //PluginsServices p =  pluginsServices();
        //MPCServiceDummy s = p.getMPCServicesDummy();
        

        File logFile;
        logFile = new File( LOG_FILE );
        if ( !logFile.exists() )            
            try{
                logFile.createNewFile();
            } catch (IOException ex){
                ex.printStackTrace();
            }

        //initialize Log
        Log.instantiate();
        try{
            Log.setPrintingImmediately( new PrintStream( logFile ) );
        } catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        //Log.setPrintingImmediately( System.out );
        
//        File dir;                
//        dir = new File( "../images" );        
//        try
//        {
//            dir = dir.getCanonicalFile();
//        } catch( java.io.IOException ex ){
//            ex.printStackTrace();
//        }
        
//        try
//        {            
//            if ( dir == null || dir.exists() == false )
//                throw new java.lang.Throwable( "File \"" + dir.getPath() + "\" does not exists." );
//        } catch ( java.lang.Throwable e ){
//            e.printStackTrace();
//        }        
        
        _kindsManagerModel = new KindsManager();
        _kindsManagerView = new KindsManagerView( _guiServices.getMainFrame(), _kindsManagerModel );
        
        _graphic_manager = new GraphicManager( "" );//dir.getPath() );
                                                /*pluginsServices().getProjectServices().getPathDirectoryIMAGE()*/
        _class_manager = new ClassManager();  
        
        _objcol = new ObjectCollector( _insp, _graphic_manager,
                                       _kindsManagerModel, _class_manager,
                                       pluginsServices().getMPGServices() );
        
        _repository = new Repository( "../repositories", _graphic_manager, _objcol, pluginsServices().getProjectServices() );
        
        _locationManager = new LocationManager( _insp, _kindsManagerModel,
                                                _class_manager, _graphic_manager,
                                                pluginsServices().getMPGServices() );
        _locationManager.setObjectCollector( _objcol );
        
        //---------------Initialization of GUI components and building the GUI----------------------------//
        //------------------------------------------------------------------------------------------------//
        
        GridBagConstraints c = new GridBagConstraints();
        
        _panelTools = new JPanel( new BorderLayout() );
        _panelTools.setBorder( BorderFactory.createLineBorder(Color.black) );
        _panelTools.setPreferredSize( new Dimension( 0, 39 ) );
        _toolBar = new JToolBar();
        _toolBar.setPreferredSize( new Dimension( 0,39 ) );
        
        JButton but = new JButton( IconBag.MOL_GRAPHIC_TMPL.getIcon() );        
        but.addActionListener( this );
        but.setActionCommand( "GT" );
        but.setToolTipText( "Edit graphic templates" );
        but.setPreferredSize( new Dimension( 33, 33 ) );
        but.setMinimumSize( new Dimension( 33, 33 ) );
        but.setMaximumSize( new Dimension( 33, 33 ) );
        _toolBar.add( but );
        
        but = new JButton( IconBag.MOL_KINDS_MANAGER.getIcon() );        
        but.addActionListener( this );
        but.setToolTipText( "Edit kinds" );
        but.setActionCommand( "KM" );
        but.setPreferredSize( new Dimension( 33, 33 ) );
        but.setMinimumSize( new Dimension( 33, 33 ) );
        but.setMaximumSize( new Dimension( 33, 33 ) );        
        _toolBar.add( but );     
//        
//        JSeparator sep = new JSeparator();
//        toolBar.add( sep ); 
        _toolBar.addSeparator();
        
//        but = new JButton( IconBag.TN_GRAPH.getIcon() );        
//        but.addActionListener( this );
//        but.setActionCommand( "CreateGraph" );
//        but.setToolTipText( "Create new graph location" );
//        but.setPreferredSize( new Dimension( 33, 33 ) );
//        but.setMinimumSize( new Dimension( 33, 33 ) );
//        but.setMaximumSize( new Dimension( 33, 33 ) );        
//        toolBar.add( but );   
//        
//        but = new JButton( IconBag.TN_GRID.getIcon() );        
//        but.addActionListener( this );
//        but.setToolTipText( "Create new grid location" );
//        but.setActionCommand( "CreateGrid" );
//        but.setPreferredSize( new Dimension( 33, 33 ) );
//        but.setMinimumSize( new Dimension( 33, 33 ) );
//        but.setMaximumSize( new Dimension( 33, 33 ) );        
//        toolBar.add( but );         
        
//        toolBar.addSeparator();
        
        but = new JButton( IconBag.MOL_CREATE_OBJECT.getIcon() );        
        but.addActionListener( this );
        but.setToolTipText( "Create new object" );
        but.setActionCommand( "CreateObject" );
        but.setPreferredSize( new Dimension( 33, 33 ) );
        but.setMinimumSize( new Dimension( 33, 33 ) );
        but.setMaximumSize( new Dimension( 33, 33 ) );        
        _toolBar.add( but );   
        
        but = new JButton( IconBag.MOL_CREATE_ENT.getIcon() );        
        but.addActionListener( this );
        but.setActionCommand( "CreateEnt" );
        but.setToolTipText( "Create new ent" );
        but.setPreferredSize( new Dimension( 33, 33 ) );
        but.setMinimumSize( new Dimension( 33, 33 ) );
        but.setMaximumSize( new Dimension( 33, 33 ) );        
        _toolBar.add( but );        
        
        but = new JButton( IconBag.MOL_CREATE_SENSOR.getIcon() );        
        but.addActionListener( this );
        but.setActionCommand( "CreateSensor" );
        but.setToolTipText( "Create new sensor" );
        but.setPreferredSize( new Dimension( 33, 33 ) );
        but.setMinimumSize( new Dimension( 33, 33 ) );
        but.setMaximumSize( new Dimension( 33, 33 ) );        
        _toolBar.add( but );               
        
        _panelTools.add( _toolBar, BorderLayout.CENTER );     
       
        //----------------Control panel component-----------------//
        
        _panelControls = new JPanel( new BorderLayout() );
        _tabbedPaneControls = new JTabbedPane( JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT );
        //jen ladim ale stejne se nekde tady bude muset vytvorit inspector view
        
        _graphicTemplatesView = new GraphicTemplatesView( _graphic_manager, _guiServices.getMainFrame(), pluginsServices().getProjectServices() );     
        
        _inspview = new InspectorView( _insp );
        _inspcontroller = new InspectorController( _insp, _inspview,
                                                  _class_manager,
                                                  _graphic_manager,
                                                  _graphicTemplatesView,
                                                  pluginsServices().getMPGServices(),
                                                  pluginsServices().getProjectServices() );
        _inspview.makeController( _inspcontroller );
        /* it's necessary for the LinksEditor to have a reference on the main frame of
         * the application for creating the dialog in LinksView which is contained in LinksEditor. It's obtained
         * from the GUI via GuiServices. Make sure, that setGuiServices() is invoked before instantiating the 
         * singleton class LinksEditor throw invoking LinksEditorInstance() */        
        _linksEditor.setGuiServices( _guiServices );        
        _linksEditor = LinksEditor.LinksEditorInstance(); //make the new instance of LinksEditor - it's a singleton

        _attributesEditor.setGuiServices( _guiServices );        
        _attributesEditor = AttributesEditor.AttributesEditorInstance(); //make the new instance of AttributesEditor - it's a singleton
        
        _kindsEditor.setGuiServices( _guiServices );
        _kindsEditor.setKindsManagerView( _kindsManagerView );
        _kindsEditor = KindsEditor.KindsEditorInstance( _kindsManagerModel );
        
        _placementToLocationEditor.setGuiServices( _guiServices );
        PlacementInWpEditor.setGuiServices( _guiServices );
        _placementToLocationEditor = PlacementInLocationEditor.PlacementInLocationEditorInstance( _class_manager );
        
        _sensorsEditor.setGuiServices( _guiServices );
        _sensorsEditor = SensorsEditor.SensorsEditorInstance( _objcol );        
        
//        obj1 = new WTObject( _insp, _graphic_manager, _kindsManagerModel,
//                             _class_manager, _objcol );//pozn: ojekty vytvaret vzdy az po vytvoreni "externich" editoru kumul. vlastnosti
//        obj2 = new WTObject( _insp, _graphic_manager, _kindsManagerModel,
//                             _class_manager, _objcol );   

        LocationTreeView locationTree = new LocationTreeView( _locationManager, _graphic_manager,
                                                              _kindsManagerModel, pluginsServices().getMPGServices(), _class_manager );
        _connectionsView = new ConnectionsView( _locationManager );
        
//        tabbedPaneControls.addTab( "Object Inspector", inspview );
//        tabbedPaneControls.addTab( "Locations tree", locationTree );
//        tabbedPaneControls.addTab( "Conections", connectionsView );        
//        //panelControls.setBorder(BorderFactory.createLineBorder(Color.black));
//        panelControls.setPreferredSize( new Dimension( 60, 0 ) );
//       // panelControls.setMinimumSize( new Dimension( 100, 0 ) );        
//        labelControls = new JLabel( "Controls" );
//        panelControls.add( labelControls, BorderLayout.PAGE_START );
//        panelControls.add( tabbedPaneControls, BorderLayout.CENTER );
        
        _controllPanel = new ControllPanel( _inspview, locationTree, _connectionsView );        
        _panelControls.add( _controllPanel );
        

        //----------------Object collector component-----------------//        
        
        _objcol_ctrl = new ObjectCollectorController( _objcol, _graphic_manager, _insp );
        _locationManager.addLocationManagerListener( _objcol_ctrl );
        _objcol_ctrl.setLocationManager( _locationManager );
        
            //instanciate the ObjectCollectorView and binds it with its controller
        _objcol_view = new CollectorViewPanel( _objcol, _graphic_manager );//new JPanel(); 
        _objcol_view.setController( _objcol_ctrl );        
        _objcol_ctrl.setView( _objcol_view );
        _objcol_ctrl.setRepository( _repository );
            // view will get events from model
        _objcol.addListener( _objcol_view );
        _objcol.addListener( _insp );
        _objcol.setController( _objcol_ctrl );
        _repository.addListener( _objcol_view );        
        
        //----------------Veiw world component-----------------//
        
//        _panelViewWorld = new JPanel( new GridBagLayout() );
//        //_panelViewWorld.setBorder(BorderFactory.createLineBorder(Color.black));
//        //_panelViewWorld.setPreferredSize( new Dimension( 100, 0 ) );
//        //_panelViewWorld.setMinimumSize( new Dimension( 0, 100 ) );
//        labelViewWorld = new JLabel( "ViewWorld" );
//        _panelViewWorld.add( labelViewWorld );
//        
//        JButton buttonOutputTest = new JButton( "Output test" );
//        buttonOutputTest.addActionListener( this );
//        _panelViewWorld.add( buttonOutputTest );
//        
//        JButton b = new JButton( "Create new IveObject" );
//        b.addActionListener( this );
//        _panelViewWorld.add( b );     
//        
//        JButton bc = new JButton( "Connections" );
//        bc.addActionListener( this );
//        _panelViewWorld.add( bc );        
        
        _panelViewWorld = new ViewWorld( _locationManager, _graphic_manager, _insp,
                                         _kindsManagerModel, pluginsServices().getMPGServices(), _class_manager );
        _objcol.addListener( (ObjectCollectorListener) _panelViewWorld );
        _graphic_manager.addListener( (GraphicManagerListener) _panelViewWorld );
        _graphic_manager.addListener( _objcol );
        _objcol_ctrl.setMyDragAndDropListener( (ViewWorld) _panelViewWorld );
        _connectionsView.addConnectionViewListener( (ViewWorld) _panelViewWorld );

        _splitPane2 = new JSplitPane( JSplitPane.VERTICAL_SPLIT, _objcol_view, _panelViewWorld );
        _splitPane1 = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, _splitPane2, _panelControls );
        _splitPane1.setResizeWeight( 0.9 ); //to resize rather the left side of splitPane to cover the excess area
        _splitPane2.setResizeWeight( 0.1 ); //to resize rather the bottom side of splitPane to cover the excess area

        _panelStatusBar = new JPanel( new GridLayout( 0,1 ) );
        _panelStatusBar.setBorder(BorderFactory.createLineBorder( Color.lightGray, 1 ));
        //panelStatusBar.setMinimumSize( new Dimension( 0, 100 ) );
        _panelStatusBar.setPreferredSize( new Dimension( 0, 20 ) ); //to set the height of status bar       
        _labelStatusBar = new JLabel( "StatusBar" );
        //_panelStatusBar.add( _labelStatusBar );
    
      // buttonPress = new JButton( "Press" );
//        buttonAdd = new JButton( "Switch" );        
//        buttonAdd.addActionListener( this );  
   //   buttonPress.addActionListener( this );
        
        _panel = new JPanel( new BorderLayout() );
        
//panel.add( buttonPress, BorderLayout.LINE_START );         
        //panel.setBorder(BorderFactory.createLineBorder(Color.black));
        //panel.add( buttonPress );
        //panel.add( buttonAdd );

//        c.weightx = 1.0;
//        c.weighty = 1.0;
//        c.gridx = 0;
//        c.gridy = 0;
//        panel.add( panelTools, c );
//
//        c.fill = GridBagConstraints.BOTH;
//        c.gridx = 0;
//        c.gridy = 1;
//        panel.add( splitPane1, c );
//        
//        c.gridx = 0;
//        c.gridy = 2;
//        panel.add( panelStatusBar, c );             
        
        _panel.add( _panelTools, BorderLayout.PAGE_START ); //this component will cover the rest of space
        _panel.add( _splitPane1, BorderLayout.CENTER );   //any excess of space after resizing will get this component
        _panel.add( _panelStatusBar, BorderLayout.PAGE_END ); //this component will cover the rest of space                        
        
        settingTheMenu();
        
        //--------------------
        
//        debugFilling( "_all.xml" );  
            
        //---------------------        
                
        ModulSettings settings = new ModulSettings( _panel, "MOL"); //the name of this modul (it'll also become a name of tabbed pane for this modul )
                                                                   //and the component, which will be added on that tabbed pane
        return settings;
    }
    
    //---------------------------------------------------------------------------------------------------
    
    public void onActivate() {
        getMenu().addTemp( _windowsMenu );
    }

    //---------------------------------------------------------------------------------------------------
    
    public void onDeactivate() {
        getMenu().removeTemp();
    }

    //===================================================================================================
    /* Ovveriden methods from Plugin class */    
    
    /** Used while developing the application for testing anything without need to start the GUI. */
    public String loadWithoutGUI()
    {
        //Currently it generates the random number of objects with random content and try to validate it .
        //System.out.println( "LoadWithoutGUI ted obsahuje kraviny!!! Takze si sem dej, co potrebujes." );
        
//        ArrayList< WTObject > objs = new ArrayList< WTObject >();
//       
//        int i, j = 0;
//        Random gen = new Random();
//        j = gen.nextInt( 10 ) + 2;
//        for ( i = 0; i < j; i++ )
//            objs.add( new WTObject( _insp, _graphic_manager ) );
         
//        System.out.println( "Pocet vygenerovanych objektu: " + j );        
//        //In future should collect everything from MOL and it'll passe it upwards.
//        Outputter outputter = new Outputter();
//        
//        XMLChecker checker = new XMLChecker();
//        checker.checkObjects( outputter.getOutput( objs ) );
//       
//        for ( i = 0; i < j; i++ ){
//            System.out.println( "---------------------------------------------------------------------------------------" );            
//            System.out.println( "Object #" + i + ":" );
//            System.out.println( objs.get( i ) );    
//        }
        
        File logFile;
        logFile = new File( "log.txt" );
        if ( !logFile.exists() )            
            try{
                logFile.createNewFile();
            } catch (IOException ex){
                ex.printStackTrace();
            }

        //initialize Log
        Log.instantiate();
        try{
            Log.setPrintingImmediately( new PrintStream( logFile ) );
        } catch (FileNotFoundException ex){
            ex.printStackTrace();
        }        
        
        File dir;                
        dir = new File( "../images" );        
        try
        {
            dir = dir.getCanonicalFile();
        } catch( java.io.IOException ex ){
            ex.printStackTrace();
        }
        
        try
        {            
            if ( dir == null || dir.exists() == false )
                throw new java.lang.Throwable( "File \"" + dir.getPath() + "\" does not exists." );
        } catch ( java.lang.Throwable e ){
            e.printStackTrace();
        }        
        
        _kindsManagerModel = new KindsManager();
        
        _graphic_manager = new GraphicManager( dir.getPath() );

        _insp = new Inspector();
        
        _class_manager = new ClassManager();

        _objcol = new ObjectCollector( _insp, _graphic_manager,
                                       _kindsManagerModel, _class_manager,
                                       pluginsServices().getMPGServices() );    
         
        Repository rep = new Repository( "../repositories", _graphic_manager, _objcol, pluginsServices().getProjectServices() );
//         boolean result = rep.createNewRepository( null, "myRep" );
//         System.out.println( "Vtvoreni repositare: " + result );
//         rep.openRepository( rep.getLocation() + "/myRep" );   
                
        _locationManager = new LocationManager( _insp, _kindsManagerModel,
                                                _class_manager, _graphic_manager,
                                                pluginsServices().getMPGServices() );
         
        debugFilling( "tutorial7.xml" );
        //outputTest();

        _locationManager.printLocationTree();
        
       // Log.logMessage( "\n\nDeleting the template MujSvet.Palac.Pustina2:\n\n");
        //_locationManager.deleteLocation( "MujSvet.Palac.Pustina2" );
              
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
                                                public void run(){
                                                    JFrame frame;
                                                    frame = new JFrame( "Hlavní okno aplikace" );
                                                    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
                                                    frame.setLayout( new GridLayout( 0, 1 ) );  
                                                    
                                                    frame.getContentPane().add( new LocationTreeView( _locationManager,
                                                            _graphic_manager, _kindsManagerModel,
                                                            pluginsServices().getMPGServices(), _class_manager ) );                                                    
                                                                                                 
                                                    frame.pack();
                                                    frame.setVisible( true );                                                    
                                                }
        });
        
        //....v LinksEditoru nezapomen zapnout vypousteni vyjimky....//
        return "MOL";
    }
    
    //---------------------------------------------------------------------------------------------------
    
    public Service getService(){
        return new MOLService( this );
    }
    
    //===================================================================================================
    /* ActionListener interface */    
    
    public void actionPerformed(ActionEvent e)
    {
//        if( ( (JButton) e.getSource() ).getText().equals( "Press" ) )
//            _insp.LgetOut();
        if ( e.getSource() instanceof JButton ) {                        
            
            JButton b = (JButton) e.getSource();
            
            if ( b.getActionCommand().equals( "GT" ) ){
                
                if ( _graphicTemplatesView != null )
                    _graphicTemplatesView.setVisible( true, GraphicTemplatesView.function_enum.NORMAL, null );
                return;
            }  
            else if ( b.getActionCommand().equals( "KM" ) ){
                
                if ( _kindsManagerView != null )
                    _kindsManagerView.setVisible( true );
                return;
            } else if ( b.getActionCommand().equals( "CreateObject" ) ) {
                
                _objcol.newObjectTemplate();
                return;                
            } else if ( b.getActionCommand().equals( "CreateEnt" ) ) {
                
                _objcol.newEntTemplate();
                return;
            } else if ( b.getActionCommand().equals( "CreateSensor" ) ) {
                
                _objcol.newSensorTemplate();
                return;
            }
                
//            if ( b.getText().equals( "Objekt 1" ) )
//                obj1.editInInspector();
//
//            else if ( b.getText().equals( "Objekt 2" ) )
//                obj2.editInInspector();
//
//            else
            if ( b.getText().equals( "Output test" ) )
            {               
                outputTest();
            }
            else if ( b.getText().equals( "Create new IveObject" ) ){
                _objcol.newObjectTemplate();           
            }        
            else if ( b.getText().equals( "Connections" ) ){
                //_locationManager.connect( "MujSvet.Pustina.Pustina.wp_1_1", "MujSvet.Palac.Pustina2.wp_1_2" );
            }
        }

//        if( ( (JButton) e.getSource() ).getText().equals( "Switch" ) )
//        {            
//            sw = !sw;
//            if( sw ) Lobj1.editInInspector();
//            else Lobj2.editInInspector();
//            _insp.LgetOut();
//        }
            
    }

    //===================================================================================================
    /* MOL's services */ 
    
    public ArrayList<Element> getXML()
    {
        ArrayList<Element> list = new ArrayList<Element>();
    
        list.add( _graphic_manager.getInfosetForm() );
        list.add( _objcol.getInfosetForm() );                     
        list.add( _locationManager.getInfosetForm() );
        
        Element rootLocEl = _locationManager.getRootLocationTag();
        if ( rootLocEl != null )
            list.add( _locationManager.getRootLocationTag() );
        
        return list;
    }
    
    //---------------------------------------------------------------------------------------------------
    
    /** 
     *  Set <links> element to specified location.
     *
     *  @param locationId dots separated path to the location in location hierarchy uniquely
     *  identifying the location
     *  @param linksEl org.jdom.Element which should be assigned to the specified location
     */
    public void setLinksElement( String locationId, Element linksEl ){
        _locationManager.setLinksElement( locationId, linksEl );
    }    
    
    //---------------------------------------------------------------------------------------------------
    
    /** 
     *  Returns the paths to all locations currently presented in the MOL. These paths are
     *  unique identifiers of the locations in the projekt.
     */
    public List< String > getLocationPaths(){
        return _locationManager.getLocationPaths();
    }
    
    //---------------------------------------------------------------------------------------------------
    
    /** Returns the names of all ents currently present in the MOL. */
    public List< String > getEntNames(){
        return _objcol.getEntNames();
    }    

    //---------------------------------------------------------------------------------------------------
    
    /** Returns the names of all objects currently present in the MOL which are not ents. */
    public List<String> getObjectNames() {
        return _objcol.getObjectNames();
    }
    
    //---------------------------------------------------------------------------------------------------    
    
    /**
     * 
     * @param objName name of object which attributes should be returned
     * @param atrType Specify which attributes of specified object should be
     *        returned. If this parameter is null then all attributes of
     *        specified object are returned (types are not distinguished)
     *
     * @return ArrayList of "iveObject" attributes of specified type which has set the
     *         object with specified name
     * @return Empty ArrayList when objName is null or specified object has no attributes
     *         or specified object doesn't exist in the project
     *     
     */
    public ArrayList< String > getObjectAttributes( String objName, String atrType ) {
        return _objcol.getObjectAttributes( objName, atrType );
    }    
    
    //---------------------------------------------------------------------------------------------------
    
    /** 
     *  Create objects templates and fills in its data structures with data from
     *  world XML description.
     */    
    public void fillInObjects( List<Element> objects )
    {
        Log.logMessage( "Creating an Objects from XML", "MOL" );
        
        //get path to directory where the images are stored
        String path =  pluginsServices().getProjectServices().getPathDirectoryIMAGE();
        if ( path != null )
            _graphic_manager.setDirectoryPath( path );
        
        //list for temporary storing of templates which inherit from another
        //templates
        ArrayList< Element > listI = new ArrayList< Element >();
        
        //create objects
        for ( Element el : objects ){
            
            //cope with possible inherintence in templates                        
            String templateAttr = el.getAttributeValue( "template" );
            if ( templateAttr == null || templateAttr.equals( "" ) ){
                _objcol.newObjectTemplate( el, false, null );
            }
            else
                listI.add( el );
                        
        }
        
        loadObjectsFromInherentTemplates( listI );
        
        //transform some objects into sensors and set them to right objects (according to XML which is being read)
        _objcol.initiateSensors();
        
    }
    
    /** 
     *  Manage loading the <Objecttemplates> and <EntTemplates> tags in listI
     *  which inherit from some other objects. This ancestor objects must be already
     *  present in ObjectCollector before invoking this method. Otherwise it won't work
     *  properly. There is support only for one-level inheritance! ( It wouldn't be
     *  hard to implement more level inheritance by technique of repeated call of this
     *  method but there wasn't need to do that. The demo.xml is satisfied only with one-level
     *  inheritance. )
     * 
     *  Implementing of inheritance of XML tags is just
     *  for compatibility with hand-made worlds but worlds created by this editor
     *  doesn't use this inheritance.
     */
    private void loadObjectsFromInherentTemplates( List< Element > listI ){
        
        for ( Element el : listI ){
           
            WTObject obj = _objcol.get( el.getAttributeValue( "template" ) );
            if ( obj != null ){
                
                _objcol.newObjectTemplate( el, false, obj );
            } else {
                
            }
        }
    }

    //---------------------------------------------------------------------------------------------------
    
    /** 
    *  Create locations templates and fills in its data structures with data from
    *  world XML description.
    */    
    public void fillInLocations( List<Element> locations )
    {        
        
        if ( locations == null )
            return;
        
        _locationsXML = locations;
        if ( _rootLocationXML != null )
            _locationManager.buildHierarchy( _locationsXML, _rootLocationXML );
    }
    
    //---------------------------------------------------------------------------------------------------
    
    /** 
     *  This event occures when ent genius id changes in MPG.
     *  
     *  @param entName Name of the ent which belongs the genius to.
     *  @param geniusName Name of the genius - when it is empty it means
     *  removing the genius "from ent".
     */
    public void setEntGenius( String entName, String geniusName ){
        _objcol.setEntGenius( entName, geniusName );
    }
    
    //---------------------------------------------------------------------------------------------------
    
    /** 
     *  This event occures when location genii list changes in MPC.
     *  
     *  @param locPath path uniquely identifying the location in the MOL. Path
     *  is created from names of all locations on the path from root location to
     *  desired location included. The names are separated by dots.
     *  @param genii the list of names of all genii set to the location determined by
     *  the locPath
     */
    public void setLocationGenii( String locPath, List< String > genii ){
        _locationManager.setLocationGenii( locPath, genii );
    }

    //---------------------------------------------------------------------------------------------------
    
     /** 
     *  Create graphics templates and fills in its data structures with data from
     *  world XML description.
     */    
    public void fillInGraphics(List<Element> graphics)
    {
        Log.logMessage( "Creating graphicTemplates from XML", "MOL" );
        for ( Element el:graphics )
            _graphic_manager.newGraphicTemplate( el );
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    //---------------------------------------------------------------------------------------------------
    
    /** 
     *  Create root location template in modul MOL and fills in its data structures with data from
     *  world XML description.
     */    
    public void fillInRootLocation( List<Element> rootlocation )
    {
        if ( rootlocation == null )
            return;
        
        _rootLocationXML = rootlocation.get( 0 );
        if ( _locationsXML != null )
            _locationManager.buildHierarchy( _locationsXML, _rootLocationXML );
    }

    //===================================================================================================
    /* Project interface */     
    
    public void newProjectEvent()
    {
         Log.logMessage( "New projekt - MOL" );
                  
        //get path to directory where the images are stored
        String path =  pluginsServices().getProjectServices().getPathDirectoryIMAGE();
        if ( path != null )
            _graphic_manager.setDirectoryPath( path );
        
         File defF = new File( "../default/XML/DefaultMOL.xml" );
         try{
             
             defF = defF.getCanonicalFile();
             
             if ( defF.exists() ){                          
                 org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
                 org.jdom.Document doc = builder.build( defF );
                 Element root = doc.getRootElement();

                 //TODO vyresit defaultni sablony - aby empty_default nesla smazat ani menit
                 Element grEl = root.getChild( "graphicTemplates" );
                 if ( grEl != null )
                     for ( Element el : (List<Element>) grEl.getChildren() )
                         _graphic_manager.newGraphicTemplate( el );             

                 Element objsEl = root.getChild( "objectTemplates" );
                 if ( objsEl != null )
                     for ( Element el : (List<Element>) objsEl.getChildren() )
                         _objcol.newObjectTemplate( el, true, null );
             }
             
         } catch( java.io.IOException ex ){
             ex.printStackTrace();
         } catch (JDOMException ex) {
             ex.printStackTrace();
         } catch( NullPointerException ex ){ }
         
         setEnabled( true );         
    }

    public void closeProjectEvent()
    {
        setEnabled( false );  
        
        Log.logMessage( "Zavirame projekt - MOL" );
        _objcol_ctrl.closeRepository();
        _objcol.clear();
        _graphic_manager.clear();
        _locationManager.clear();
        
        _connectionsView.clear();

        _kindsManagerModel.clear();

        _class_manager.clear();
        
        ( (ViewWorld) _panelViewWorld ).reset();
        
        GridViewPane.clearGridLocationBuffer();
        
        _insp.clear();
        
//        _kindsManagerView.clear();
//        _attributesEditor.clear();
//        _kindsEditor.clear();
//        _placementToLocationEditor.clear();
//        _sensorsEditor.clear();        
        
        _rootLocationXML = null;
        _locationsXML = null;
        
//        _panel.removeAll();
//        
//        dropAllReferences();             
    }
    
    private void dropAllReferences() {
        _panel = null;    
        _panelTools = null;
        _panelControls = null;
        _panelViewWorld = null;
        _panelStatusBar = null;
        _panelSplitPane = null;     

        /* MOL's components */

        /** Model of ObjectInspector component. */
        _insp = null;
        /** View of ObjectInspector component. */
        _inspview = null;
        /** Controller of ObjectInspector component. */
        _inspcontroller = null;
        /** Connections view. */
        _connectionsView = null;

        /** Model of ObjectCollector component. */
        _objcol = null;
        /** View of ObjectCollector component. */
        _objcol_view = null;
        /** Controller of ObjectCollector component. */
        _objcol_ctrl = null;

        /** Manager of graphic. */
        _graphic_manager = null;

        /** Manager of classes. */
        _class_manager = null;

        /** Manager of kinds. */
        _kindsManagerModel = null;

        /** Objects repository. */
        _repository = null;  

        /** Component managing everything concerning the locations ( templates, hierarchy etc. ). */
        _locationManager = null;

        /** Manager of kinds. */
        _kindsManagerView = null;

        _graphicTemplatesView = null;    

        /* Contains View, which must be inicialized in load() method, because it 
         * realized some new graphic components and it needs to get reference on main frame of the application for creating the dialog;
         * the reference is obtained in LinksEditor from GUI via GuiServices, which are passed to the LinksEditor*/
        _linksEditor = null;
        _attributesEditor = null;
        _kindsEditor = null;
        _placementToLocationEditor = null;
        _sensorsEditor = null;

        //auxiliary fields for proper filling the LocationManager by the locations from XML
        _locationsXML = null;
        _rootLocationXML = null;           

        _labelStatusBar = null;
        _labelViewWorld = null;
        _labelControls = null;
        _labelObjectCollector = null;
        _labelTools = null;
        _splitPane1 = null;
        _splitPane2 = null;    

        /** Temporary menu of this modul. */
//        private JMenu _windowsMenu;
//
//        private JMenuItem _locTreeMenuItem;
//
//        private JMenuItem _connMenuItem;

        
        _toolBar = null;

        _tabbedPaneControls = null;

        _controllPanel = null;        
    }

    public void openProjectEvent()
    {
         Log.logMessage( "Otvirame projekt - MOL" );
         Log.logMessage( "Svazani objektu s grafickymi sablonami.", "public void openProjectEvent()" );
         
        _objcol.loadSensorsSpecial( pluginsServices().getProjectServices().getProjectPath() + "/MOL/SavedSensors" );
        
         _objcol.bindWithGraphicTemplates();
         
         setEnabled( true );
    }
    
    public void saveProjectEvent() {        
        if ( _objcol == null )
            return;
        
        _objcol.saveSensorsSpecial( pluginsServices().getProjectServices().getProjectPath() + "/MOL/SavedSensors" );
    }    
    
    //==================if=================================================================================
    /* MPCListenerDummy interface */ 
    
    public void pathChangedDirectoryIMAGE( String new_path ){
        setEnabled( true );
    }    
    
    //===================================================================================================
    /* Support */
    
    /** Just for debugging sake. Fills the objectCollector and GraphicManager with the content read 
     *  from given file.
     */
    private void debugFilling( String filename ){
            File inputw = new File( filename );
            org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
            try
            {

                _graphic_manager.setDirectoryPath( "c:"+File.separator+"Documents and Settings"+File.separator+"Jirka"+File.separator+"My Documents"+File.separator+"testingDemoIVED"+File.separator+"Sources"+File.separator+"cz"+File.separator+"ive"+File.separator+"resources"+File.separator+"images"+File.separator+"" );                

                org.jdom.Document doc = builder.build( inputw );
                fillInObjects( doc.getRootElement().getChild( "objectTemplates" ).getChildren() );
                fillInGraphics( doc.getRootElement().getChild( "graphicTemplates" ).getChildren() );
                fillInLocations( doc.getRootElement().getChild( "locationTemplates" ).getChildren() );
                fillInRootLocation( doc.getRootElement().getChild( "rootLocation" ).getChildren() );
                _objcol.bindWithGraphicTemplates();                

            } catch (IOException ex)
            {
                ex.printStackTrace();
            } catch (JDOMException ex)
            {
                ex.printStackTrace();
            }         
    }
    
    /** For debugging sake. Writes the XML content of ObjectCollector and GraphicManager into 
      * the Log and also it tries to validate this content against the world.xsd schema ( world's description ).*/
    private void outputTest(){
//                Log.logMessage( "Testovaci XML vystup z GraphicManageru a ObjectCollectoru " );        
//
//                Outputter outputter = new Outputter();
//                outputter.addToOutput( _graphic_manager.getInfosetForm() );
//                outputter.addToOutput( _objcol.getInfosetForm() );
//                
//                XMLChecker checker = new XMLChecker();
//                checker.checkObjects( outputter.getOutput() );
//
//                XMLOutputter xmloutputter = new XMLOutputter( 
//                                                    org.jdom.output.Format.getPrettyFormat() );
//                Log.logMessage( xmloutputter.outputString( outputter.getOutput() ) );     
        
            Log.logMessage( "-----------Testovaci XML vystup z LocationManageru---------\n" );
            //_locationManager.printLocationTree();
            Log.logMessage( _locationManager.toString(), log_status.DEBUG );
            _locationManager.printConnections();
            _locationManager.printDistances();
            //_class_manager.printClasses();
            //System.out.println( "instantiateSubloc v grid lokaci spusteno: " + TGridLocation.counter + "x" );
    }
    
    /** Creates and sets the main menu of the MOL. */
    private void settingTheMenu(){
        JMenu menu = new JMenu( "Windows" );
        menu.setMnemonic( KeyEvent.VK_W );
        _windowsMenu = menu;
        JCheckBoxMenuItem menuItem;
        
//        menuItem = new JCheckBoxMenuItem( "Inspector", true );
//        menuItem.addItemListener( new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
//                if ( item.getState() )
//                    _controllPanel.open( ControllPanel.INSP );
//                else
//                    _controllPanel.close( ControllPanel.INSP );
//            }
//        });
//        menu.add( menuItem );
        
        menuItem = new JCheckBoxMenuItem( "Location tree", true );
        menuItem.setMnemonic( KeyEvent.VK_T );
        menuItem.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                if ( item.getState() )
                    _controllPanel.open( ControllPanel.LOCTREE );
                else
                    _controllPanel.close( ControllPanel.LOCTREE );
            }
        });
        _locTreeMenuItem = menuItem;
        menu.add( menuItem );
        
        menuItem = new JCheckBoxMenuItem( "Connections", true );
        menuItem.setMnemonic( KeyEvent.VK_C );
        menuItem.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                if ( item.getState() )
                    _controllPanel.open( ControllPanel.CONN );
                else
                    _controllPanel.close( ControllPanel.CONN );
            }
        });
        _connMenuItem = menuItem;
        menu.add( menuItem );
        
        _controllPanel.setMenuItems( _locTreeMenuItem, _connMenuItem );
        
        getMenu().addTemp( menu );
        
        //------Debug section---//
        menu = new JMenu( "Debug" );
        JMenuItem menuIt = new JMenuItem( "Graphic manager debug output" );
        menuIt.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_1, ActionEvent.CTRL_MASK ) );
        menuIt.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                _graphic_manager.printToLog();
//                _class_manager.printClasses();
//                
//                printObjAtr( "WaiterEnt", "null", getObjectAttributes( "WaiterEnt", null ) );
//                printObjAtr( "Eye", "null", getObjectAttributes( "Eye", null ) );
//                printObjAtr( "Knowledge", "null", getObjectAttributes( "Knowledge", null ) );
//                printObjAtr( "WaiterEnt", "Object", getObjectAttributes( "WaiterEnt", "Object" ) );
//                printObjAtr( "WaiterEnt", "object", getObjectAttributes( "WaiterEnt", "object" ) );
//                printObjAtr( "WaiterEnt", "Integer", getObjectAttributes( "WaiterEnt", "Integer" ) );
//                printObjAtr( "WaiterEnt", "Fuzzy", getObjectAttributes( "WaiterEnt", "Fuzzy" ) );
//                printObjAtr( "MinerEnt", "Fuzzy", getObjectAttributes( "MinerEnt", "Fuzzy" ) );                
//                printObjAtr( "Jukebox", "null", getObjectAttributes( "Jukebox", null ) );
//                printObjAtr( "Pissoir2", "null", getObjectAttributes( "Pisoir2", null ) );
//                printObjAtr( "Jukebox", "Fuzzy", getObjectAttributes( "Jukebox", "Fuzzy" ) );
//                printObjAtr( "Jukebox", "Integer", getObjectAttributes( "Jukebox", "Integer" ) );
//                printObjAtr( "unknown", "null", getObjectAttributes(  "unknown", null ) );
//                printObjAtr( "unknown", "Integer", getObjectAttributes( "unknown", "Integer" ) );                
                
                _locationManager.printConnections();
                _locationManager.printDistances();
            }
        });
        menu.add( menuIt );
        //getMenu().addMenu( menu );
        
//        getMenu().addTemp( new JMenu( "na konec" ) );
//        getMenu().addMenu( new JMenu( "Settings" ) );
//        getMenu().addMenu( new JMenu( "Ja tam taky nejsem" ) );
    }
    
    //for debugging of getObjectAttributes( String, String ) method
    private void printObjAtr( String arg1, String arg2, ArrayList< String > atrs ) {
        if ( arg1 == null || arg2 == null ) {
            JOptionPane.showMessageDialog( null, "Missing testing arguments." );
            return;
        }
            
        if ( atrs == null ) {
            JOptionPane.showMessageDialog( null, "Fatal error occured." );
            return;
        }
        
        String message = "Object + " + arg1 + " has these atributes of type " + arg2 + ":\n";
        for ( String s : atrs ) {
            message += s + "\n";            
        }
        JOptionPane.showMessageDialog( null, message );
    }

}

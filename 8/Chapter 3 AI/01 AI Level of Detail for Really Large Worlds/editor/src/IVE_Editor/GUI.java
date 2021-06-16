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
 
package IVE_Editor;

import IVE_Editor.PlugIns.AbstractModul;
import IVE_Editor.PlugIns.PlugIn;
import IVE_Editor.PlugIns.PlugInSettings;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.*;

/**
 *
 * @author Jirka
 */
public class GUI implements MouseListener {
    
//    private AbstractModul[] Moduls = new AbstractModul[1];
//    private int count = 0; //ukazuje na prvni volne misto v poli Moduls
    private int selectedTabbedPane = -1; //index prave aktivni zalozky, -1 znamena zadna zalozka   
    
    private ArrayList< AbstractModul > Moduls = new ArrayList< AbstractModul >();
    private ArrayList<PlugIn> Parts = new ArrayList< PlugIn >();
    private ArrayList<PlugInSettings> _modul_settings = new ArrayList<PlugInSettings>();
    private int last_modul = -1;
    
    /* Components of MainFrame*/
    private JFrame _frame;
    private JTabbedPane zalozky;
    private JMenuItem menuItem;
    
    /** instance of main menu */
    private MainMenu mainMenu;
    /** Flag indicating whether the next loaded modul should have enabled/disabled tab at the start. */
    private boolean _disableNext = false;
    
    /** List of threads which should be finished before exiting the application. */
    private static ArrayList< Thread > _threads = new ArrayList< Thread >();
    
    /** Creates a new instance of GUI */
    public GUI() {       
    }
    
//    /** Registers a new Modul in GUI */
//    public void addModul( AbstractModul Modul ){
//        if ( selectedTabbedPane == -1 ) selectedTabbedPane = 0;        
//        if ( count < Moduls.length )
//            Moduls[count++] = Modul;
//        else
//        {
//            AbstractModul[] NewModuls = new AbstractModul[ 2*Moduls.length ];
//            for ( int i = 0; i < Moduls.length; i++ )
//            {
//                NewModuls[ i ] = Moduls[ i ];
//            }
//            NewModuls[ count++ ] = Modul;
//            Moduls = NewModuls;
//            //System.out.println(Moduls.length+" "+count);
//        }
//    }
    
    public void add( PlugIn plugin )
    {        
        Parts.add( plugin );
    }
    
    
    private void createGUI(){

        _frame = new JFrame("Main application window");
        _frame.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        _frame.addWindowListener( new WindowAdapter(){
            
            public void windowClosing(WindowEvent e) {
                
                new Thread( new Runnable() {
                    public void run() {
                        if ( Parts != null ) {
                            for ( PlugIn pl : Parts )
                                if ( !pl.canExit() ) {
                                    _threads.clear();
                                    return;           
                                }
                        }

                        joinThreads();

                        System.exit( 0 );                                            
                    }
                } ).start();
                
            }
        });
        
        _frame.setLayout( new GridLayout( 0, 1 ) );
        _frame.setTitle( "IVE editor" );
 
        mainMenu = new MainMenu();
        
        zalozky = new JTabbedPane( JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT );
        zalozky.setPreferredSize( new Dimension( 1200, 700 ) ); //tady by bylo dobry spis zjistit, jak maximalizovat okno
        zalozky.addMouseListener( this );
        for( int i = 0; i < Parts.size(); i++ )
        {
            Parts.get( i ).setMenu( mainMenu );  //umožním zásuvce využívat hlavní nabídku
            
            if ( Parts.get( i ) instanceof AbstractModul ) 
                ( (AbstractModul) Parts.get( i ) ).setGUI( this ); //important for invoking before load on moduls
            
            PlugInSettings settings = Parts.get( i ).load(); //nahraju GUI zásuvky
            //tohle je asi kvuli nasledujicimu radku zbytecny
            if ( settings.getComponent() != null )         //jestliže je zásuvka modul, vytvoøím pro nìj záložku a zobrazím na ní kontejner pøedaný v ModulSettings
            {
                if ( selectedTabbedPane == -1 ) selectedTabbedPane = 0;
                
                //Resolve disabling of modul if desired and also set the ability of desabling/enabling to modul itself and perhaps some other 
                //ability of working with GUI                
                if ( _disableNext == true ){                                    //enabling at the beginning
                    zalozky.addTab( settings.getName(), null );
                    zalozky.setEnabledAt( zalozky.getTabCount() - 1, false );
                    _disableNext = false;
                }
                else 
                    zalozky.addTab( settings.getName(), settings.getComponent() ); //normal loading
                
                //important to set tab index after assigning the tab to modul else there will be
                // an error in setEnabled( boolean ) method
                //if ( Parts.get( i ) instanceof AbstractModul ) 
                ( (AbstractModul) Parts.get( i ) ).setTabIndex( zalozky.getTabCount() - 1 ); 
                
                Moduls.add( ( AbstractModul )Parts.get( i ) );
                _modul_settings.add( settings );
                Parts.remove( i-- );
            }
        }

        _frame.getContentPane().add( zalozky );        
        _frame.pack();
        _frame.setVisible(true);
        _frame.setExtendedState( JFrame.MAXIMIZED_BOTH );        
        
        mainMenu.removeTemp();
        _frame.setJMenuBar( mainMenu.getBar() );
        
    }
    
    
    public void initGUI()
    {        
//         try {
//                    // Set cross-platform Java L&F (also called "Metal")
//                UIManager.setLookAndFeel(
//                    UIManager.getSystemLookAndFeelClassName());
//            } 
//            catch (UnsupportedLookAndFeelException e) {
//               // handle exception
//            }
//            catch (ClassNotFoundException e) {
//               // handle exception
//            }
//            catch (InstantiationException e) {
//               // handle exception
//            }
//            catch (IllegalAccessException e) {
//               // handle exception
//            }
        
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable(){
                                                    public void run(){
                                                        createGUI();
                                                    }
            });
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
    
    public void mouseClicked(MouseEvent e)
    {
        Component sender = e.getComponent();   
        //tady by asi mela byt nejaka bezpecna typova kontrola (resp. try/catch blok)
        JTabbedPane zalozka = (JTabbedPane) sender;
        int i = zalozka.getSelectedIndex();
        if ( i != selectedTabbedPane && i != -1 ) 
        {
            Moduls.get( selectedTabbedPane ).onDeactivate();
            Moduls.get( i ).onActivate();
            selectedTabbedPane = i;
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public GuiServices getServices()
    {
        return new GuiServices( this );
    }

    public JFrame getMainFrame()
    {
        return _frame;
    }
    
    /** 
     *  First and foremost it's used for developing the aplication and for its debuging.
     *  At the moment its loadnig only MOL Modul
     */
    void initWithoutGUI()
    {
        System.out.println( "Loading moduls: ");        
        for( int i = 0; i < Parts.size(); i++ )
        {
            PlugIn plg = Parts.get( i );
            String name = plg.loadWithoutGUI(); //nahraju GUI zásuvky
            if ( plg instanceof AbstractModul && name.equals( "MOL" ) ){
                System.out.println( name + "      O.K.");
                Moduls.add( ( AbstractModul ) plg );
                Parts.remove( i-- );
            }
        }        
    }
    
    //-----------------------------------------------------------------------------------------------------------
    // Services for Moduls
    //----------------------
        
    public void setEnabled( AbstractModul modul, boolean enabled ){    
        
        int index = modul.getTabIndex();
        
        if ( index < 0 ){
            _disableNext = !enabled;
            return;
        }
        
        if ( zalozky.isEnabledAt( index ) == enabled )
            return;
        
        if ( enabled == false ){
            zalozky.insertTab( _modul_settings.get( index ).getName(), null, null, null, index );
            zalozky.remove( index + 1 );
        }
        else
            zalozky.setComponentAt( index, _modul_settings.get( index ).getComponent() );
        
        zalozky.setEnabledAt( index, enabled );                               
            
        if ( selectedTabbedPane >= -1 )
            zalozky.setSelectedIndex( selectedTabbedPane );

        if ( enabled )
            Moduls.get( selectedTabbedPane ).onActivate();
        else
            Moduls.get( selectedTabbedPane ).onDeactivate();

    }
    
    /** Wait for finish of the other threads doing some job. */
    private void joinThreads() {
        for ( Thread t : _threads ) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /** GUI will wait for finish of the passed thread before exiting the application. */
    public static void waitForThreadBeforeExit( Thread t ) {        
        if ( t == null )
            return;
        _threads.add( t );
    }
    
}

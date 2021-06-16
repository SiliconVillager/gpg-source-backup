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

import IVE_Editor.PlugIns.Components.Project.Project;
import IVE_Editor.PlugIns.Components.XMLLoader.XMLLoader;
import IVE_Editor.PlugIns.Components.XMLSaver.XMLSaver;
import IVE_Editor.PlugIns.Moduls.MPG.MPG;
import IVE_Editor.PlugIns.Moduls.MOL.MOL;
import IVE_Editor.PlugIns.PlugIn;
import IVE_Editor.PlugIns.PluginsServices;

/**
 *
 * @author Jirka,Martin
 */
public class PlugInsLoader {
    
    private GUI gui;
    
    /** Array for storing of the loaded plug-ins. */
    private PlugIn [] _plugIns;
    
    /* PlugIn constants used for storing and retrieving of the loaded plugins
     * from field _plugIns.
     */
    public static final int MOL = 0;
    public static final int MPG = 1;
    public static final int XMLSaver = 2;
    public static final int Project = 3;
    public static final int XMLLoader = 4;
    
    /**
     * Creates a new instance of PlugInsLoader
     */
    public PlugInsLoader( GUI _gui ) {
        gui = _gui;
    }
    
    protected void add( PlugIn plugin )
    {
        gui.add( plugin );
    }  
    
    /** There are registered the particular Moduls used in Application*/
    public void loadModuls()
    {           
        _plugIns = new PlugIn[ 5 ];
        
        MOL mol = new MOL( gui.getServices() ); 
        _plugIns[ MOL ] = mol;
        
        
        
        MPG mpg = new MPG( gui.getServices() );
        _plugIns[ MPG ] = mpg;
        
        XMLSaver xmlsaver = new XMLSaver();
        _plugIns[ XMLSaver ] = xmlsaver;
        
        Project projekt = new Project( gui.getServices() );
        _plugIns[ Project ] = projekt;
    
        XMLLoader xmlloader = new XMLLoader();
        _plugIns[ XMLLoader ] = xmlloader;
        
        //instanciuji objekt zprostredkovavajici sluzby od vsech modulu vsem modulum -> bude problem s predanim referenci na prislusne
        // moduly viz. niz v cislovanych komentarich
        // Navrh reseni: PluginLoader by pro vsechny instanciovane moduly volal jednu metodu PluginsServices, ve ktere by 
        //               PluginsServices rozpoznal, o kterou tridu se jedna a ulozil si referenci v prislusnem miste
        //               ( jinymi slovy bude v ni jeden velky switch )
        PluginsServices serv = new PluginsServices();        
        serv.setProject( projekt );
        serv.setMPG( mpg );
        serv.setXMLSaver( xmlsaver );
        serv.setMOL( mol );
        serv.setXMLLoader( xmlloader );              
        
        /* dam modulum moznost vyuzivat sluzby ostanich modulu ( vstupni( tzn. musim se ptat, kdyz neco chci )
                                                                i vystupni( tzn. musim se zaregistrovat a implementovat spravny interface,
                                                                            kdyz chci poslouchat )
        */
        mol.setPluginsServices( serv );
        projekt.setPluginsServices( serv );
        mpg.setPluginsServices( serv );
        xmlsaver.setPluginsServices( serv );
        xmlloader.setPluginsServices( serv );
        
        //Nahraji do aplikace instanciovane moduly
        add( mol );        

        add( projekt );           
        add( mpg );
        add ( xmlsaver );        
        add ( xmlloader );        

        
        
        
        //---------
        //   Spravne poradi akci pri nahravani modulu
        //   ----------------------------------------
        //1. nahrat moduly
        //2. initializovat prislusne PlugIny v PluginsServices ( tezke misto - PS si bude muset postupne zadat, ktere tridy chce instanciovat
        //   a PlugInsLoader mu bud vyhovi nebo nevyhovi )
        //3. na vsech volat setPluginsServices( _pluginServices )
        //Pozn: vsem modulum predat nasilim sluzby jadra podobne, jako pluginsServices
               
    }
    
    /** 
     *  Returns the specified plugin. To prevent the errors of programers it is
     *  highly recomanded to test the returned reference to its particular type
     *  before typecasting the result according to the constant which was passed
     *  as the argument.
     *
     *  @param plugIn_const Identifier of the desired plugin. Particular constants
     *  can be found in PlugInsLoader class.
     * 
     *  @return PlugIn It is the desired loaded plugIn.
     *  @return null If the specified plug-in isn't loaded.
     */
    public PlugIn getPlugIn( int plugIn_const ) {
        if ( plugIn_const < 0 || plugIn_const >= _plugIns.length )
            return null;
        
        return _plugIns[ plugIn_const ];
    }
}

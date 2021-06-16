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
 
package IVE_Editor.PlugIns;

import IVE_Editor.PlugIns.Components.Project.Project;
import IVE_Editor.PlugIns.Components.Project.ProjectListener;
import IVE_Editor.PlugIns.Components.Project.ProjectService;
import IVE_Editor.PlugIns.Components.XMLLoader.XMLLoader;
import IVE_Editor.PlugIns.Components.XMLLoader.XMLLoaderService;
import IVE_Editor.PlugIns.Components.XMLSaver.XMLSaver;
import IVE_Editor.PlugIns.Components.XMLSaver.XMLSaverListener;
import IVE_Editor.PlugIns.Components.XMLSaver.XMLSaverService;
import IVE_Editor.PlugIns.Moduls.MOL.MOL;
import IVE_Editor.PlugIns.Moduls.MOL.MOLService;
import IVE_Editor.PlugIns.Moduls.MPG.MPG;
import IVE_Editor.PlugIns.Moduls.MPG.MPGListener;
import IVE_Editor.PlugIns.Moduls.MPG.MPGService;

/**
 * This class collect all services from all plug-ins and offers them to another plug-ing which can use them. This is
 * the interface point between all plug-ins so if someone has newly added some plug-in and wants it to offer some services to whole application
 * author of this plug-in should write some methods through which he or she offers this services to the rest of application.
 * That's the reason why this class is the only one which should be compiled with newly added plug-in and delivered with this plug-in
 * to user.
 *
 * @author Jirka
 */
public class PluginsServices
{
    /** Component of project. */
    private Project _kprojekt;
    /** Modul of proceses and goals. */
    private MPG _mpc2;
    /** Component XMLSaver */
    private XMLSaver _xmlsaver;
        /** Component XMLLoader */
    private XMLLoader _xmlloader; 
    /** Modul of objects and locations. */
    private MOL _mol;
    
    //===================================================================================================
    /* Constructors */           
    
    /**
     * Creates a new instance of PluginsServices
     */
    public PluginsServices()
    {
    }
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    // Set section
    //------------------------------------
    
    /** Pass the reference to modul of proceses and goals. */
    public void setMPG( MPG mpc ){
        if ( mpc == null )
            return;
        
        _mpc2 = mpc;
    }    
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** Pass the reference to modul of objects and locations. */
    public void setMOL( MOL mol ){
        if ( mol == null )
            return;
        
        _mol = mol;
    }  
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** Pass the reference to component of projects. */
    public void setProject( Project kprojekt ){
        if ( kprojekt == null )
            return;
        
        _kprojekt = kprojekt;
    }    
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** Pass the reference to component of XMLSaver */
    public void setXMLSaver( XMLSaver xmlsaver ){
        if ( xmlsaver == null )
            return;
        
        _xmlsaver = xmlsaver;
    }      
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** Pass the reference to component of XMLLoader */
    public void setXMLLoader( XMLLoader xmlloader ){
        if ( xmlloader == null )
            return;
        
        _xmlloader = xmlloader;
    }        
        
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    // Public section
    //------------------------------------
    
    
    /** <b>Source:</b> Component Project
     *  <b>Description:</b> Add listener to component Project. It will recieve the events from this modul. 
     *  Listener must implements interface ProjectListener.
     */
    public void addListenerToProject( ProjectListener listener ){
        if ( _kprojekt != null )
            _kprojekt.addListener( listener );
    }      
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /**
     * getMPCServicesDummy
     */
    public ProjectService getProjectServices(){
        if ( _kprojekt == null )
            return null;
        
        return (ProjectService)_kprojekt.getService();
    }   
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    
    /**
     * <b>Source:</b> Modul MVC
     *  <b>Description:</b> Add listener to modul MPG2. It will recieve the events from this modul. 
     *  Listener must implements interface MPGListener.
     */
    public void addListenerToMPG2( MPGListener listener ){
        if ( _mpc2 != null )
            _mpc2.addListener( listener );
    }
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** get MPG2Services */
    public MPGService getMPGServices(){
        if ( _mpc2 == null )
            return null;
        
        return (MPGService)_mpc2.getService();
    }       

    //-------------------------------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    
    /**
     * <b>Source:</b> Component XMLSaver
     *  <b>Description:</b> Add listener to modul XMLSaver. It will recieve the events from this modul. 
     *  Listener must implements interface XMLSaverListener.
     */
    public void addListenerToXMLSaver( XMLSaverListener listener ){
        if ( _xmlsaver != null )
            _xmlsaver.addListener( listener );
    }
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** get XMLSaverService */
    public XMLSaverService getXMLSaverServices(){
        if ( _xmlsaver == null )
            return null;
        
        return (XMLSaverService)_xmlsaver.getService();
    }       

    //-------------------------------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    
    /**
     * <b>Source:</b> Modul MOL
     *  <b>Description:</b> Add listener to modul XMLSaver. It will recieve the events from this modul. 
     *  Listener must implements interface XMLSaverListener.
     */
//    public void addListenerToMOL( XMLSaver listener ){
//        if ( _xmlsaver != null )
//            _xmlsaver.addListener( listener );
//    }
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** get MOLService */
    public MOLService getMOLServices(){
        if ( _mol == null )
            return null;
        
        return (MOLService)_mol.getService();
    }     
    
//-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** get XMLLoaderServices */
    public XMLLoaderService getXMLLoaderServices(){
        if ( _xmlloader == null )
            return null;
        
        return (XMLLoaderService)_xmlloader.getService();
    }       
    
}

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

import IVE_Editor.MainMenu;

/**
 *
 * @author Jirka
 */
public abstract class PlugIn {
    
    private int id;
    private MainMenu mainMenu;
    
    /** Class where are methods mediating services from all plugins presented in the application. */
    private PluginsServices _pluginsServices;
    
    /**
     * Creates a new instance of PlugIn
     */
    public PlugIn() {
    }
    public abstract PlugInSettings load();
    public void setMenu( MainMenu _mainMenu )
    {
        mainMenu = _mainMenu;
    }
    protected MainMenu getMenu(){
        return mainMenu;
    }
    public Service getService(){ //zrusit!!!
        return new Service();     //pokud se nedefinuje v konkrétní zásuvce jinak, modul neposkytuje navenek žádné služby
    }

    /** First and foremost used for debugging. Should be overriden in arbitrary modul where can be defined anything. */
    public String loadWithoutGUI()
    {        
        return "The loadWithoutGUI() method is not overriden properly. This is version from abstract PlugIn class.";
    }
        
    public boolean canExit() { 
        return true;
    }
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** Set object wraping services from all other plug-ins so that this plug-in could use them in case of need. */
    public void setPluginsServices( PluginsServices serv ){
        _pluginsServices = serv;
    }
    
    //-------------------------------------------------------------------------------------------------------------------------------------------
    
    /** Access object wraping services from all other plug-ins to descendants of PlugIn class. */
    protected PluginsServices pluginsServices(){
        return _pluginsServices;
    }    
    
}
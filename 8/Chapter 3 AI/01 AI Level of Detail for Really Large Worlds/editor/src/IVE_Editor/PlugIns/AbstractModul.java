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

import IVE_Editor.GUI;
import java.awt.Component;
import javax.swing.*;

/**
 *
 * @author Jirka, Martin
 */
public abstract class AbstractModul extends PlugIn{
    
    /** Reference on GUI so that the modul could use some of its API. */
    private GUI _gui = null;
    /** Number of tab where this particular modul is placed. */
    private int _my_tab_num = -1;
    /** Indicates wheter setTab method was already invoked at least one time on this Modul. */
    private boolean setGUIUsedFlag = false;
    /** 
     *  Indicates whether tab of this modul should be disabled at the start of the application or not.
     *  Its set on true if setEnabled() method is invoked in implementation of load method. Load method is invoked by core
     *  before assigning tab to modul. Core ask modul at the beginning wheter it should disable the modul at the beginning.
     */
    private boolean _should_start_disabled = false;
            
    /** Creates a new instance of AbstractModul */
    public AbstractModul(){
    }
    
    /** V potomkovi je v metodì Load() implementováno GUI Modulu. Vrací jeho nastavení. */    
    public abstract ModulSettings load();    

    /** Metoda implementována v potomkovi je volána jádrem, jesliže byl modul aktivován (uživatel klepnul na jeho záložku).*/
    public abstract void onActivate();
    
    /** Metoda implementována v potomkovi je volána jádrem, jesliže byl modul aktivován (uživatel klepnul na jeho záložku).*/
    public abstract void onDeactivate(); 
    
    //--------------------------------------------------------------------------------------------------------------------------------------------
    
    /** Disable or enable this modul in GUI. Particulary it enables the tabbedPane where the modul is placed. */
    protected void setEnabled( boolean enabled ){
        _gui.setEnabled( this, enabled );
    }    
    
    //-------------------------------------------------------------------------------------------------------------------------------------------

     /** 
     * Pass the reference on GUI class so that the modul could use some of its API. For example fo enabling/disabling the tab where 
     * the modul is placed.
     */
    
    public final void setGUI( GUI gui )
    {
        //it is posible to use effect of this method only once ( assumption is that this is called only by core in GUI class )
        if ( setGUIUsedFlag == true || gui == null )
            return;
        
        _gui = gui;
        
        //this method now can't be used again
        setGUIUsedFlag = true;
    }

    public int getTabIndex()
    {
        return _my_tab_num;
    }
    
    public void setTabIndex( int tabindex )
    {
        _my_tab_num = tabindex;
    }    
    
}    

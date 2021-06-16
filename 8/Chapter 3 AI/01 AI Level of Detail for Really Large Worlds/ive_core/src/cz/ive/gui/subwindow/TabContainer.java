/* 
 *
 * IVE - Inteligent Virtual Environment
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
 
package cz.ive.gui.subwindow;

import cz.ive.gui.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SubwindowContainer designed to store tabs.
 *
 * @author ondra
 */
public class TabContainer extends SubwindowContainer {
    
    /** Associated tabbed panel */
    JTabbedPane tabbedPanel;
    
    /** Focused Subwindow */
    Subwindow focused;
    
    /**
     * Creates a new instance of TabContainer
     *
     * @param tabbedPanel swing tabbed panel to be associated with this
     *      container and filled with Subwindows
     */
    public TabContainer(JTabbedPane tabbedPanel) {
        this.tabbedPanel = tabbedPanel;
        
        tabbedPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ev) {
                int idx = TabContainer.this.tabbedPanel.indexAtLocation(
                        ev.getX(), ev.getY());
                
                if (idx < 0)
                    return;
                
                Subwindow subwindow = subwindowList.get(idx);
                
                switch(ev.getButton()) {
                    case MouseEvent.BUTTON1:
                        break;
                    case MouseEvent.BUTTON2:
                        closeSubwindow(subwindow);
                        MainFrame.instance().getDefaultFrameContainer().
                                addSubwindow(subwindow);
                        break;
                    case MouseEvent.BUTTON3:
                        closeSubwindow(subwindow);
                        break;
                }
                
            }
        });
        
        tabbedPanel.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged( ChangeEvent ev){
                focusChanged();
            }
        });
    }
    
    /**
     * Add Subwindow to this container
     *
     * @param subwindow to be added
     */
    public void addSubwindow(Subwindow subwindow) {
        super.addSubwindow(subwindow);
        
        Info info = subwindow.getInfo();
        tabbedPanel.addTab(info.getCaption(), info.getIcon(),
                subwindow.getPanel(), info.getTooltip());
        tabbedPanel.setSelectedComponent(subwindow.getPanel());
        subwindow.setSubwindowContainer(this);
        subwindow.setInvisible(false);
        subwindow.opened();
    }
    
    /**
     * Closes given subwindow.
     *
     * @param subwindow to be closed
     */
    public void closeSubwindow(Subwindow subwindow) {
        super.closeSubwindow(subwindow);
        
        tabbedPanel.remove(subwindow.getPanel());
        subwindow.setInvisible(true);
        subwindow.closed();
        focusChanged();
    }
    
    /** 
     * Is the object viewed? If yes, focus it! 
     *
     * @param object to be queried.
     * @return <code>true</code> iff the object was found and the 
     *      subwindow focused.
     */
    public boolean findAndFocus(Object object) {
        for (Subwindow subwindow: subwindowList) {
            if (subwindow.contain(object)) {
                tabbedPanel.setSelectedComponent(subwindow.getPanel());
                focusChanged();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Info of a given Subwindow was update.
     *
     * @param subwindow Subwindow which Info was updated
     */
    public void updateSubwindow(Subwindow subwindow) {
        int idx = tabbedPanel.indexOfComponent(subwindow.getPanel());
        
        if (idx < 0)
            return;
        
        Info info = subwindow.getInfo();
        
        tabbedPanel.setTitleAt(idx, info.getCaption());
        tabbedPanel.setToolTipTextAt(idx, info.getTooltip());
        tabbedPanel.setIconAt(idx, info.getIcon());
    }
    
    /**
     * Called whenever the focus changes
     */
    protected void focusChanged() {
        if (focused != null) {
            focused.setInvisible(true);
        }
        int idx = TabContainer.this.tabbedPanel.getSelectedIndex();
        
        if (idx < 0) {
            focused = null;
            return;
        }
        focused = subwindowList.get(idx);
        focused.setInvisible(false);
    }
    
    /**
     * Switch to the next/previous tab.
     *
     * @param next <code>true</code> iff we should focus the next tab, 
     *      <code>false</code> for the previous.
     */
    public void focusAnotherTab(boolean next) {
        int count = tabbedPanel.getComponentCount();
        int idx = tabbedPanel.getSelectedIndex();
        
        if (next) {
            idx = (idx+1) % count;
        } else {
            idx = (idx+count-1) % count;
        }
        
        if (idx >= 0 && idx < count)
            tabbedPanel.setSelectedIndex(idx);
    }
}

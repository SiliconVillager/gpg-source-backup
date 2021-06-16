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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Simple frame able to contain Subwindow
 *
 * @author ondra
 */
public class SubwindowFrame extends JFrame {
    
    /** Contained subwindow */
    protected Subwindow subwindow;
    
    /** Responsible container */
    protected SubwindowContainer container;
    
    /**
     * Creates a new instance of SubwindowFrame
     *
     * @param container container responsible for this frame
     * @param subwindow control to be contained in this frame
     */
    public SubwindowFrame(SubwindowContainer container, Subwindow subwindow) {
        super();
        this.subwindow = subwindow;
        this.container = container;
        
        Container panel = getContentPane();
        
        panel.setLayout(new BorderLayout());
        panel.add(subwindow.getPanel(), BorderLayout.CENTER);
        
        updateFrame();
        
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent ev) {
                SubwindowFrame.this.container.closeSubwindow(
                        SubwindowFrame.this.subwindow);
            }
            
            /** Instead of iconifying, make a tab */
            public void windowIconified(WindowEvent ev) {
                SubwindowFrame.this.container.closeSubwindow(
                        SubwindowFrame.this.subwindow);
                
                MainFrame.instance().getDefaultTabContainer().addSubwindow(
                        SubwindowFrame.this.subwindow);
            }
        });
        
        
        pack();
    }
    
    /**
     * Contained Subwindow's info was updated. We should update look of the 
     * frame accordingly.
     */
    public void updateFrame() {
        Info info = subwindow.getInfo();
        Icon icon = info.getIcon();
        
        setTitle(info.getCaption());
        if (icon != null && icon instanceof ImageIcon)
            setIconImage(((ImageIcon)icon).getImage());
    }
}

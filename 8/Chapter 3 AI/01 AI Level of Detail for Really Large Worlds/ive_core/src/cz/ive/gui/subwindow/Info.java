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

import cz.ive.gui.icon.*;
import java.net.URL;
import javax.swing.*;

import cz.ive.*;

/**
 * Structure for holding UI specific information for Tabs or Subwindows.
 *
 * @author ondra
 */
public class Info {
    
    /** Caption to be seen by a user */
    protected String caption;
    /** Tooltip to be viewed as an immediate help */
    protected String tooltip;
    /** Icon to be seen by a user */
    protected Icon icon;
    
    /** 
     * Creates a new instance of Info 
     *
     * @param caption initial caption
     * @param tooltip initial title
     * @param icon initial icon
     */
    public Info(String caption, String tooltip, Icon icon) {
        this.caption = caption;
        this.tooltip = tooltip;
        this.icon = icon;
    }
    
    /** 
     * Creates a new instance of Info 
     *
     * @param caption initial caption
     * @param tooltip initial title
     * @param iconURL uri of the icon file
     */
    public Info(String caption, String tooltip, URL iconURL) {
        this(caption, tooltip, new ImageIcon(iconURL));
    }
    
    /** 
     * Creates a new instance of Info 
     *
     * @param caption initial caption
     * @param tooltip initial title
     * @param bagMember element of the IconBag enum identifying initial icon
     */
    public Info(String caption, String tooltip, IconBag bagMember) {
        this(caption, tooltip, bagMember.getIcon());
    }
    
    /**
     * Getter for the caption
     */
    public String getCaption() {
        return caption;
    }
    
    /**
     * Getter for the tooltip
     */
    public String getTooltip() {
        return tooltip;
    }
    
    /**
     * Getter for the icon
     */
    public Icon getIcon() {
        return icon;
    }
}

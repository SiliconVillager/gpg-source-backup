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
 
package cz.ive.gui;

import java.awt.BorderLayout;
import javax.swing.*;
import java.awt.event.*;

import cz.ive.logs.*;
import java.awt.Color;
import java.awt.Insets;
import javax.swing.border.EtchedBorder;

/**
 * Panel with group of checkboxes. Chechbox for each type of logmessage.
 * When filter configuration changes, models of trees and models of tables with
 * messages are modified.
 *
 * @author Zdenek
 */
public class LogFilter extends JPanel{
    
    /** Color of FINEST messages */
    public static Color FINEST = Color.WHITE;
    
    /** Color of FINER messages */
    public static Color FINER = new Color( 220, 220, 220);
    
    /** Color of FINE messages */
    public static Color FINE = new Color( 190, 190, 190);
    
    /** Color of CONFIG messages */
    public static Color CONFIG = new Color( 190, 190, 255);
    
    /** Color of INFO messages */
    public static Color INFO = new Color( 190, 255, 190);
    
    /** Color of WARNING messages */
    public static Color WARNING = new Color( 255, 255, 190);
    
    /** Color of SEVERE messages */
    public static Color SEVERE = new Color( 255, 190, 190);
    
    /** Model of table with content of directory */
    protected MessageModel model;
    
    /** Models of trees */
    protected LogModel treeModels[];
    
    /** Model of table with all messages */
    protected AllMessagesModel allModel;
    
    /** Severe checkbox */
    protected JCheckBox chBSevere;
    
    /** Warning checkbox */
    protected JCheckBox chBWarning;
    
    /** Info checkbox */
    protected JCheckBox chBInfo;
    
    /** Config checkbox */
    protected JCheckBox chBConfig;
    
    /** Fine checkbox */
    protected JCheckBox chBFine;
    
    /** Finer checkbox */
    protected JCheckBox chBFiner;
    
    /** Finest checkbox */
    protected JCheckBox chBFinest;
    
    /**
     * Listemer for checkboxes, changes filter.
     */
    ActionListener ClickListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            boolean filter[];
            filter = new boolean[7];
            // get configuration
            filter[0] = chBSevere.isSelected();
            filter[1] = chBWarning.isSelected();
            filter[2] = chBInfo.isSelected();
            filter[3] = chBConfig.isSelected();
            filter[4] = chBFine.isSelected();
            filter[5] = chBFiner.isSelected();
            filter[6] = chBFinest.isSelected();
            // change models
            model.changeFilter(filter);
            for (int i = 0; i < treeModels.length; i++) {
                if (treeModels[i] != null)
                    ((LogDir)treeModels[i].getRoot()).reload();
            }
            allModel.changeFilter(filter);
        }
    };
    
    /**
     * Private constructor. LogFilter can't be used without defining models.
     *
     */
    private LogFilter() {
        
    }
    
    /**
     * Creates LogFilter - group of checkboxes. Filter of logmessages.
     * @param model model of table with messages from one node
     * @param models array of tree model
     * @param allModel model with allmessages
     */
    public LogFilter(MessageModel model, LogModel models[],
            AllMessagesModel allModel) {
        super();
        this.model = model;
        this.treeModels = models;
        this.allModel = allModel;
        
        chBSevere = new JCheckBox("Error", true);
        chBWarning = new JCheckBox("Warning", true);
        chBInfo = new JCheckBox("Info", true);
        chBConfig = new JCheckBox("Config", true);
        chBFine = new JCheckBox("Fine", true);
        chBFiner = new JCheckBox("Finer", true);
        chBFinest = new JCheckBox("Finest", true);
        
        
        chBSevere.addActionListener(ClickListener);
        chBWarning.addActionListener(ClickListener);
        chBInfo.addActionListener(ClickListener);
        chBConfig.addActionListener(ClickListener);
        chBFine.addActionListener(ClickListener);
        chBFiner.addActionListener(ClickListener);
        chBFinest.addActionListener(ClickListener);
        
        
        chBSevere.setBackground(SEVERE);
        chBWarning.setBackground(WARNING);
        chBInfo.setBackground(INFO);
        chBConfig.setBackground(CONFIG);
        chBFine.setBackground(FINE);
        chBFiner.setBackground(FINER);
        chBFinest.setBackground(FINEST);
        
        add(prepareButton(chBSevere));
        add(prepareButton(chBWarning));
        add(prepareButton(chBInfo));
        add(prepareButton(chBConfig));
        add(prepareButton(chBFine));
        add(prepareButton(chBFiner));
        add(prepareButton(chBFinest));
    }
    
    /**
     * Helper method that prepares the check button.
     *
     * @param btn check box to be prepared.
     * @return component taht is or that contains the given button.
     */
    protected JComponent prepareButton(JCheckBox btn) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(btn, BorderLayout.CENTER);
        panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        btn.setMargin(new Insets(0, 0, 0, 0));
        
        return panel;
    }
    
}

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
 
package cz.ive.gui.dialog;

import cz.ive.gui.*;
import cz.ive.logs.Log;
import cz.ive.simulation.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Dialog for Scheduler configuration
 *
 * @author ondra
 */
public class SchedulerConfigDialog extends JDialog {
    
    /** Load threshold input field */
    protected JTextField thresholdTxt;
    
    /** Input field for the size of the monitoring window */
    protected JTextField frameSizeTxt;
    
    /** Input field for the minimal frame gap between two cleanups */
    protected JTextField minimalGapTxt;
    
    /** Check box for overall activation or deactivation of the cleanup */
    protected JCheckBox allowCleanUp;
    
    /** Cancel Button */
    protected JButton cancelBtn;
    
    /** OK Button */
    protected JButton okBtn;
    
    /** Creates a new instance of SchedulerConfigDialog */
    public SchedulerConfigDialog() {
        super(MainFrame.instance());
        setLayout(new GridBagLayout());
        setTitle("Cleanup configuration");
        setModal(true);
        
        SchedulingStatistics stats = SchedulerImpl.instance().getStatistics();
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = c.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        
        boolean allowed = 
                SchedulerImpl.instance().getStatistics().cleanupEnabled();
        add(new JLabel("Allow load-based cleanup: "), c);
        add(allowCleanUp = new JCheckBox("", allowed), c);
        allowCleanUp.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean allowed = allowCleanUp.isSelected();
                thresholdTxt.setEnabled(allowed);
                frameSizeTxt.setEnabled(allowed);
                minimalGapTxt.setEnabled(allowed);
            }
        });
        
        c.gridy++;
        
        add(new JLabel("Clean phase load threshold: "), c);
        c.gridwidth = 1;
        add(thresholdTxt = new JTextField(
                String.valueOf(stats.getThreshold()), 4), c);
        add(new JLabel("%"), c);
        thresholdTxt.setHorizontalAlignment(thresholdTxt.RIGHT);
        thresholdTxt.setEnabled(allowed);
        c.gridwidth = 2;
        
        c.gridy++;
        
        add(new JLabel("Monitoring window size: "), c);
        c.gridwidth = 1;
        add(frameSizeTxt = new JTextField(
                String.valueOf(stats.getWindowLen()), 4), c);
        add(new JLabel("frames"), c);
        frameSizeTxt.setHorizontalAlignment(thresholdTxt.RIGHT);
        frameSizeTxt.setEnabled(allowed);
        c.gridwidth = 2;
        
        c.gridy++;
        
        add(new JLabel("Clean phases minimal loop: "), c);
        c.gridwidth = 1;
        add(minimalGapTxt = new JTextField(
                String.valueOf(stats.getMinLoops()), 4), c);
        add(new JLabel("frames"), c);
        minimalGapTxt.setHorizontalAlignment(thresholdTxt.RIGHT);
        minimalGapTxt.setEnabled(allowed);
        c.gridwidth = 2;
        
        c.gridy++;
        c.gridwidth = 4;
        c.fill = c.NONE;
        c.anchor = c.EAST;
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        btnPanel.add(cancelBtn = new JButton("Cancel"));
        okBtn = new JButton("Ok");
        btnPanel.add(okBtn);
        getRootPane().setDefaultButton(okBtn);
        add(btnPanel, c);
        
        
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setVisible(false);
            }
        });
        
        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setUpScheduler();
            }
        });
        
        pack();
        setLocationRelativeTo(MainFrame.instance());
    }
    
    /**
     * Fire the dialog and if accepted, configure the scheduler.
     */
    static public void open() {
        new SchedulerConfigDialog().setVisible(true);
    }
    
    /**
     * Performs the configuration from the given parameters.
     */
    public void setUpScheduler() {
        String thresholdStr = thresholdTxt.getText();
        String windowStr = frameSizeTxt.getText();
        String minimalGapStr = minimalGapTxt.getText();
        int threshold = -1;
        int window = -1;
        int minimalGap = -1;
        
        boolean allowed = allowCleanUp.isSelected();
        SchedulingStatistics stats = SchedulerImpl.instance().getStatistics();
        
        if (allowed) {
            try {
                threshold = Integer.parseInt(thresholdStr);
                window = Integer.parseInt(windowStr);
                minimalGap = Integer.parseInt(minimalGapStr);
            } catch (NumberFormatException ex) {
                // Handled elsewhere
            }
            if (threshold < 0 || threshold > 100) {
                JOptionPane.showMessageDialog(this,
                        "Load threshold must be an integer value (0-100).",
                        "Error", 0);
                return;
            }
            if (window < 1) {
                JOptionPane.showMessageDialog(this,
                        "Monitoring window size must be a positive integer value.",
                        "Error", 0);
                return;
            }
            if (minimalGap < 0) {
                JOptionPane.showMessageDialog(this,
                        "Minimal loop size must be a positive integer value.",
                        "Error", 0);
                return;
            }
            stats.resetStatistics(threshold, window, minimalGap);
        }
        
        if (stats.cleanupEnabled() != allowed) {
            stats.enableCleanup(allowed);
        }
        
        setVisible(false);
    }
}

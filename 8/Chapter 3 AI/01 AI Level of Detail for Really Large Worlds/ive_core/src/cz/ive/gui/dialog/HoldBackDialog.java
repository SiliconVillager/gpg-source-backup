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
import cz.ive.iveobject.*;
import cz.ive.location.*;
import cz.ive.lod.Holdback;
import cz.ive.logs.Log;
import cz.ive.messaging.*;
import cz.ive.simulation.*;
import cz.ive.template.Holdbacks;
import cz.ive.template.TemplateMap;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Dialog for entering data about a holdback.
 *
 * @author ondra
 */
public class HoldBackDialog extends JDialog {
    
    /** Position Id input field */
    protected JTextField positionIdTxt;
    
    /** Assured LOD input field */
    protected JTextField lodTxt;
    
    /** Cancel Button */
    protected JButton cancelBtn;
    
    /** OK Button */
    protected JButton okBtn;;
    
    /** Holdback position */
    protected String positionId = "";
    
    /** Holdback lod requested */
    protected int lodRequested = 1;
    
    /** Was the dialog accepted */
    protected boolean accepted = false;
    
    /**
     * Creates a new instance of HoldBackDialog
     *
     * @param owner Dialog responsible for the opening
     * @param positionId Id of the location to be filled in the dialog
     * @param lodRequested Value of the LOD that should be filled in the 
     *      corresponding text field.
     */
    public HoldBackDialog(Dialog owner, String positionId, int lodRequested) {
        super(owner);
        this.positionId = positionId;
        if (lodRequested != -1) {
            this.lodRequested = lodRequested;
        }
        
        createComponents();
    }
    
    /**
     * Creates a new instance of HoldBackDialog
     *
     * @param owner Frame responsible for the opening
     * @param positionId Id of the location to be filled in the dialog
     * @param lodRequested Value of the LOD that should be filled in the 
     *      corresponding text field.
     */
    public HoldBackDialog(Frame owner, String positionId, int lodRequested) {
        super(owner);
        this.positionId = positionId;
        if (lodRequested != -1) {
            this.lodRequested = lodRequested;
        }
        
        createComponents();
    }
    
    /**
     * Creates all visual components of the dialog.
     */
    protected void createComponents() {
        setLayout(new GridBagLayout());
        setTitle("Holdback parameters");
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = c.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        
        add(new JLabel("Position (Id): "), c);
        add(positionIdTxt = new JTextField(positionId, 20), c);
        
        c.gridy = 1;
        
        add(new JLabel("Lod requested (1-5): "), c);
        c.gridwidth = 1;
        add(lodTxt = new JTextField(String.valueOf(lodRequested), 5), c);
        lodTxt.setHorizontalAlignment(lodTxt.RIGHT);
        
        c.gridy = 2;
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
                accept();
            }
        });
        
        pack();
        setLocationRelativeTo(MainFrame.instance());
    }
    
    public void open() {
        setModal(true);
        setVisible(true);
    }
    
    /**
     * Adds the holdback
     */
    protected void accept() {
        // Check the Lod
        try {
            lodRequested = Integer.parseInt(lodTxt.getText());
            if (lodRequested < 1)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "You should enter a number greater than 0",
                    "Error", 0);
            return;
        }
        // Check the position (as far as we can)
        positionId = positionIdTxt.getText();
        if (!Holdbacks.instance().prevalidateHoldback(positionId)) {
            JOptionPane.showMessageDialog(this,
                    "You did not enter valid location",
                    "Error", 0);
            return;
        }
        accepted = true;
        setVisible(false);
    }
    
    /**
     * Getter for the requested lod.
     *
     * @return Value entered to the requested lod field.
     */
    public int getRequestedLod() {
        return lodRequested;
    }
    
    /**
     * Getter for the holdback position.
     *
     * @return Value entered to the position field.
     */
    public String getPositionId() {
        return positionId;
    }
    
    /**
     * Setter for the requested lod.
     *
     * @param lodRequested value to be prefilled in the Requested lod field.
     */
    public void setRequestedLod(int lodRequested) {
        this.lodRequested = lodRequested;
        lodTxt.setText(String.valueOf(lodRequested));
    }
    
    /**
     * Was the dialog accepted?
     *
     * @return <code>true</code> iff the dialog was accepted by the ok button.
     */
    public boolean isAccepted() {
        return accepted;
    }
}

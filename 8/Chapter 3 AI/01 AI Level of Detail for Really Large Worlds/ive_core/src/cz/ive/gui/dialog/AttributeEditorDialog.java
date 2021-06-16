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
import cz.ive.iveobject.attributes.*;
import cz.ive.location.*;
import cz.ive.logs.Log;
import cz.ive.messaging.*;
import cz.ive.simulation.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A ancestor of all the attribute edit dialogs.
 *
 * @author ondra
 */
abstract public class AttributeEditorDialog extends JDialog {
    
    /**
     * Attribute type edited by this particular editor, to be set up by the
     * descendants of this class.
     */
    private String attrType = "UNKNOWN";
    
    /** Panel for the attribute editor component */
    protected JPanel attributePanel;
    
    /** Cancel Button */
    protected JButton cancelBtn;
    
    /** OK Button */
    protected JButton okBtn;
    
    /** Name of the attribute */
    protected String attrName;
    
    /** Id of the iveobject */
    protected String objectId;
    
    /** Was the dialog accepted */
    protected boolean accepted = false;
    
    /**
     * Creates a new instance of AttributeEditorDialog
     *
     * @param attrType type of the attribute to be viewed in the windoe caption.
     */
    public AttributeEditorDialog(String attrType) {
        super(MainFrame.instance());
        
        this.attrType = attrType;
        
        createComponents();
        createAttributeEditor();
        
        pack();
        setLocationRelativeTo(MainFrame.instance());
    }
    
    /**
     * This method is to be overiden by the descendants of this class in order
     * to create attribute type specific editor.
     */
    abstract protected void createAttributeEditor();
    
    /**
     * Creates all visual components of the dialog.
     */
    protected void createComponents() {
        setLayout(new GridBagLayout());
        setTitle("Attribute editor ("+attrType+")");
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = c.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        
        add(attributePanel = new JPanel(), c);
        
        c.gridy = 1;
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
                if (accept()) {
                    changeAttribute();
                    setVisible(false);
                }
            }
        });
    }
    
    public static void open(String attrName, String objectId) {
        AttributeEditorDialog dlg;
        
        SchedulerImpl.instance().lockSimulation();
        IveObject obj = ObjectMap.instance().getObject(objectId);
        if (obj == null) {
            JOptionPane.showMessageDialog(null,
                    "Object does not exist (any more).",
                    "Warning", JOptionPane.INFORMATION_MESSAGE);
            SchedulerImpl.instance().unlockSimulation();
            return;
        }
        AttributeValue value = obj.getAttribute(attrName);
        if (value == null) {
            JOptionPane.showMessageDialog(null,
                    "Attribute does not exist (any more).",
                    "Warning", JOptionPane.INFORMATION_MESSAGE);
            SchedulerImpl.instance().unlockSimulation();
            return;
        }
        
        if (value instanceof AttrInteger) {
            dlg = new IntegerEditorDialog(((AttrInteger)value).getValue());
        } else if (value instanceof AttrFuzzy) {
            dlg = new FuzzyEditorDialog(((AttrFuzzy)value).getValue());
        } else {
            JOptionPane.showMessageDialog(null,
                    "You can not edit this type of attributes.",
                    "Warning", JOptionPane.INFORMATION_MESSAGE);
            SchedulerImpl.instance().unlockSimulation();
            return;
        }
        SchedulerImpl.instance().unlockSimulation();
        
        dlg.attrName = attrName;
        dlg.objectId = objectId;
        
        dlg.setModal(true);
        dlg.setVisible(true);
    }
    
    /**
     * Can we edit a given attribute?
     *
     * @param attr Attribute that is querried.
     * @return <code>true</code> iff this type of attribyte can be edited.
     */
    static public boolean canEdit(AttributeValue attr) {
        if (attr.getName().equals("lod"))
            return false;
        if (attr instanceof AttrInteger) {
            return true;
        } else if (attr instanceof AttrFuzzy) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Changes the value of the attribute. It performs the proper locking 
     * and calls the overiden method changeValue.
     */
    protected void changeAttribute() {
        SchedulerImpl.instance().lockSimulation();
        IveObject obj = ObjectMap.instance().getObject(objectId);
        if (obj == null) {
            JOptionPane.showMessageDialog(null,
                    "Object does not exist (any more).",
                    "Warning", JOptionPane.INFORMATION_MESSAGE);
            SchedulerImpl.instance().unlockSimulation();
            return;
        }
        AttributeValue value = obj.getAttribute(attrName);
        if (value == null) {
            JOptionPane.showMessageDialog(null,
                    "Attribute does not exist (any more).",
                    "Warning", JOptionPane.INFORMATION_MESSAGE);
            SchedulerImpl.instance().unlockSimulation();
            return;
        }
        
        changeValue(value);
        
        SchedulerImpl.instance().updateWorld();
        
        SchedulerImpl.instance().unlockSimulation();
    }
    
    /**
     * Accepts the dialog, this method is to be overiden by the descendants
     * of this class.
     *
     * @return <code>true</code> iff the dialog was accepted.
     */
    abstract protected boolean accept();
    
    /**
     * Changes the value of the attribute (if necessary), this method is to be overiden
     * by the descendants of this class. It will be called with the simulation
     * lock locked.
     *
     * @param attribute Attribute to be changed.
     */
    abstract protected void changeValue(AttributeValue attribute);
}

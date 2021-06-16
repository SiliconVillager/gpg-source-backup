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
import cz.ive.iveobject.attributes.*;
import cz.ive.iveobject.attributes.AttributeValue;
import javax.swing.*;

/**
 * Attribute editor dialog specialized for editing AttrInteger values.
 *
 * @author ondra
 */
public class IntegerEditorDialog extends AttributeEditorDialog {
    
    /** Swing Text field for integer value editing */
    protected JTextField intTxt;
    
    /** Original value of the attribute */
    protected int oldvalue;
    
    /** New value of the attribute */
    protected int newvalue;
    
    /** 
     * Creates a new instance of IntegerEditorDialog .
     *
     * @param oldvalue original value of the attribute
     */
    public IntegerEditorDialog(int oldvalue) {
        super("INTEGER");
        setOldValue(oldvalue);
    }
    
    /**
     * This method is to be overiden by the descendants of this class in order
     * to create attribute type specific editor.
     */
    protected void createAttributeEditor() {
        attributePanel.add(
                intTxt = new JTextField("", 6));
    }
    
    /**
     * Setup the oldvalue.
     *
     * @param oldvalue old value of the attribute being edited.
     */
    protected void setOldValue(int oldvalue) {
        this.oldvalue = oldvalue;
        intTxt.setText(String.valueOf(oldvalue));
    }
    
    /**
     * Accepts the dialog. Checks first the validity of entries.
     */
    protected boolean accept() {
        String val = intTxt.getText();
        try {
            newvalue = Integer.parseInt(val);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null,
                    "The value you have entered, is not a valid integer value.",
                    "Warning", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Changes the value of the attribute (if necessary). It will be called 
     * with the simulation lock locked.
     */
    protected void changeValue(AttributeValue attribute) {
        oldvalue = ((AttrInteger)attribute).getValue();
        if (oldvalue != newvalue)
            ((AttrInteger)attribute).setValue(newvalue);
    }
}

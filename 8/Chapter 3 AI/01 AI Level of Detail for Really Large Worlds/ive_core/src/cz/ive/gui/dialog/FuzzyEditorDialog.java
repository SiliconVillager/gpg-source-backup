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
import cz.ive.valueholders.FuzzyValueHolder;
import javax.swing.*;

/**
 * Attribute editor dialog specialized for editing AttrFuzzy values.
 *
 * @author ondra
 */
public class FuzzyEditorDialog extends AttributeEditorDialog {
    
    /** Swing Text field for fuzzy value editing */
    protected JTextField fuzzyTxt;
    
    /** Original value of the attribute */
    protected short oldvalue;
    
    /** New value of the attribute */
    protected short newvalue;
    
    /** 
     * Creates a new instance of FuzzyEditorDialog 
     *
     * @param oldvalue original value of the attribute
     */
    public FuzzyEditorDialog(short oldvalue) {
        super("FUZZY");
        setOldValue(oldvalue);
    }
    
    /**
     * This method is to be overiden by the descendants of this class in order
     * to create attribute type specific editor.
     */
    protected void createAttributeEditor() {
        attributePanel.add(new JLabel("Fuzzy value: "));
        attributePanel.add(fuzzyTxt = new JTextField("", 6));
    }
    
    /**
     * Setup the oldvalue.
     *
     * @param oldvalue old value of the attribute being edited.
     */
    protected void setOldValue(short oldvalue) {
        String val;
        this.oldvalue = oldvalue;
        
        if (oldvalue == FuzzyValueHolder.True) {
            val = "true";
        } else if (oldvalue == FuzzyValueHolder.False) {
            val = "false";
        } else {
            val = String.valueOf(oldvalue);
        }
        fuzzyTxt.setText(val);
    }
    
    /**
     * Accepts the dialog. Checks first the validity of entries.
     */
    protected boolean accept() {
        String val = fuzzyTxt.getText();
        
        if (val != null && val.toLowerCase().equals("true")) {
            newvalue = FuzzyValueHolder.True;
            return true;
        } else if (val != null && val.toLowerCase().equals("false")) {
            newvalue = FuzzyValueHolder.False;
            return true;
        }
        
        try {
            newvalue = Short.parseShort(val);
            if (newvalue < 0)
                newvalue = 0;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null,
                    "The value you have entered, is not a valid " +
                    "fuzzy value (0-32767).", "Warning", 
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Changes the value of the attribute (if necessary). It will be called 
     * with the simulation lock locked.
     */
    protected void changeValue(AttributeValue attribute) {
        oldvalue = ((AttrFuzzy)attribute).getValue();
        if (oldvalue != newvalue)
            ((AttrFuzzy)attribute).setValue(newvalue);
    }
}

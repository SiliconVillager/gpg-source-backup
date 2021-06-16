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
import cz.ive.simulation.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for editing the Calendar breakpoints.
 *
 * @author ondra
 */
public class CalendarBreakpointDialog extends JDialog {
    
    /** Day field */
    protected JSpinner daySpin;
    
    /** Hour field */
    protected JSpinner hourSpin;
    
    /** Minute field */
    protected JSpinner minuteSpin;
    
    /** Second field */
    protected JSpinner secondSpin;
    
    /** Millis field */
    protected JSpinner millisSpin;
    
    /** Cancel Button */
    protected JButton cancelBtn;
    
    /** OK Button */
    protected JButton okBtn;
    
    /** Was the dialog accepted? */
    protected boolean accepted = false;
    
    /** Date of the breakpoint time. */
    protected Date sDate;
    
    /**
     * Creates a new instance of CalendarBreakpointDialog
     *
     * @param owner Frame of the owner.
     * @param sDate Date of the calendar breakpoint
     */
    public CalendarBreakpointDialog(Frame owner, Date sDate) {
        super(owner);
        if (sDate == null) {
            this.sDate = CalendarPlanner.instance().getSimulationDate();
        } else {
            this.sDate = sDate;
        }
        
        createComponents();
    }
    
    /**
     * Creates a new instance of CalendarBreakpointDialog
     *
     * @param owner Dialog of the owner.
     * @param sDate Date of the calendar breakpoint
     */
    public CalendarBreakpointDialog(Dialog owner, Date sDate) {
        super(owner);
        if (sDate == null) {
            this.sDate = CalendarPlanner.instance().getSimulationDate();
        } else {
            this.sDate = sDate;
        }
        
        createComponents();
    }
    
    /**
     * Creates all visual components.
     */
    protected void createComponents() {
        setLayout(new GridBagLayout());
        setTitle("Calendar breakpoint");
        setModal(true);
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = c.BOTH;
        c.insets = new Insets(5, 5, 0, 5);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(sDate);
        
        add(new JLabel(""), c);
        add(new JLabel("day"), c);
        add(new JLabel(""), c);
        add(new JLabel("hour"), c);
        add(new JLabel(""), c);
        add(new JLabel("min."), c);
        add(new JLabel(""), c);
        add(new JLabel("sec."), c);
        add(new JLabel(""), c);
        add(new JLabel("millis."), c);
        
        c.gridy++;
        
        add(new JLabel("Simulation time = "), c);
        
        add(daySpin = new JSpinner(new SpinnerNumberModel(
                1 + sDate.getTime() / (24*60*60*1000), 1 ,
                1000, 1)), c);
        add(new JLabel(""), c);
        add(hourSpin = new JSpinner(new SpinnerNumberModel(
                cal.get(cal.HOUR_OF_DAY), 0 , 23, 1)), c);
        add(new JLabel(":"), c);
        add(minuteSpin = new JSpinner(new SpinnerNumberModel(
                cal.get(cal.MINUTE), 0 , 59, 1)), c);
        add(new JLabel(":"), c);
        add(secondSpin = new JSpinner(new SpinnerNumberModel(
                cal.get(cal.SECOND), 0 , 59, 1)), c);
        add(new JLabel("."), c);
        add(millisSpin = new JSpinner(new SpinnerNumberModel(
                cal.get(cal.MILLISECOND), 0 , 999, 1)), c);
        
        c.gridy++;
        c.gridwidth = 10;
        c.fill = c.NONE;
        c.anchor = c.EAST;
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        btnPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
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
                if (check()) {
                    accepted = true;
                    setVisible(false);
                }
            }
        });
        
        pack();
        setLocationRelativeTo(MainFrame.instance());
    }
    
    /**
     * Fire the dialog.
     */
    public boolean open() {
        accepted = false;
        setVisible(true);
        return accepted;
    }
    
    /**
     * Are the values correct?
     *
     * @return <code>true</code> if the specified values are correct
     */
    protected boolean check() {
        int d = ((SpinnerNumberModel)daySpin.getModel()).getNumber().
                intValue();
        int h = ((SpinnerNumberModel)hourSpin.getModel()).getNumber().
                intValue();
        int m = ((SpinnerNumberModel)minuteSpin.getModel()).getNumber().
                intValue();
        int s = ((SpinnerNumberModel)secondSpin.getModel()).getNumber().
                intValue();
        int mm = ((SpinnerNumberModel)millisSpin.getModel()).getNumber().
                intValue();
        
        sDate = new Date(mm + 1000*(s + 60*(m + 60*(h + 24*(d-1)))));
        
        return true;
    }
    
    /**
     * Getter for the breakpoint simulation date.
     *
     * @return Breakpoint simulation time in the "GMT" date format.
     */
    public Date getDate() {
        return sDate;
    }
}
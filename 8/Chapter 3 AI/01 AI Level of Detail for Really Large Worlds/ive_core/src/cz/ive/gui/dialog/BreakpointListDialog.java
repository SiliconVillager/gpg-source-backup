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

import cz.ive.exception.BreakpointActivationFailedException;
import cz.ive.gui.*;
import cz.ive.gui.icon.IconBag;
import cz.ive.gui.table.IconTableCellRenderer;
import cz.ive.messaging.*;
import cz.ive.simulation.breakpoint.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

/**
 * Dialog viewing the list of all breakpoints. It allows for their
 * addition and deletition as well as editing their parameters.
 *
 * @author ondra
 */
public class BreakpointListDialog extends JDialog {
    
    /** Swing JTable of all breakpoints */
    protected JTable breakpointList;
    
    /** Actual List of breakpoints */
    protected List<Breakpoint> breakpoints;
    
    /** Addition Button */
    protected JButton addBtn;
    
    /** Activate/deactivate */
    protected JButton activateBtn;
    
    /** Edit Button */
    protected JButton editBtn;
    
    /** Deletition Button */
    protected JButton delBtn;
    
    /** Exit Button */
    protected JButton exitBtn;;
    
    /** Refresh listener */
    protected RefreshListener refreshListener = new RefreshListener();
    
    /** Creates a new instance of BreakpointListDialog */
    protected BreakpointListDialog() {
        super(MainFrame.instance());
        setLayout(new BorderLayout(5, 5));
        setTitle("Breakpoints");
        setModal(true);
        
        breakpoints = BreakpointList.instance().getBreakpoints();
        JLabel label = new JLabel("Breakpoints list: ");
        label.setBorder(new EmptyBorder(5, 5, 0, 5));
        add(label, BorderLayout.NORTH);
        add(new JScrollPane(breakpointList =
                new JTable(new BreakpointTableModel())), BorderLayout.CENTER);
        breakpointList.setDefaultRenderer(Object.class,
                new IconTableCellRenderer());
        setColumnsWidths(new float[]{80, 400});
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 5, 5, 5));
        btnPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
        btnPanel.add(addBtn = new JButton("Add", IconBag.ADD.getIcon()));
        btnPanel.add(activateBtn = new JButton("Switch",
                IconBag.ACTIVATE.getIcon()));
        btnPanel.add(editBtn = new JButton("Edit", IconBag.EDIT.getIcon()));
        btnPanel.add(delBtn = new JButton("Del", IconBag.DELETE.getIcon()));
        btnPanel.add(exitBtn = new JButton("Exit"));
        getRootPane().setDefaultButton(exitBtn);
        add(btnPanel, BorderLayout.SOUTH);
        
        
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                openAdd(BreakpointListDialog.this);
            }
        });
        activateBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                switchBreakpoint();
            }
        });
        editBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                editSelected();
            }
        });
        delBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                deleteSelected();
            }
        });
        
        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setVisible(false);
            }
        });
        
        pack();
        setLocationRelativeTo(MainFrame.instance());
    }
    
    /**
     * Fire the dialog.
     */
    static public void open() {
        BreakpointListDialog dlg = new BreakpointListDialog();
        dlg.setVisible(true);
        dlg.refreshListener.unregister();
    }
    
    /**
     * Fire the dialog for addition of a new Breakpoint
     *
     * @param owner Frame or Dialog resposible for this dialog.
     */
    static public void openAdd(Window owner) {
        CalendarBreakpointDialog dlg;
        if (owner  instanceof Dialog)
            dlg = new CalendarBreakpointDialog((Dialog)owner, null);
        else
            dlg = new CalendarBreakpointDialog((Frame)owner, null);
        
        if (dlg.open()) {
            Date sDate = dlg.getDate();
            
            try {
                BreakpointList.instance().addBreakpoint(
                        new CalendarBreakpoint(sDate));
            } catch (BreakpointActivationFailedException ex) {
                JOptionPane.showMessageDialog(owner,
                        "Breakpoint could not be added. " + ex.getMessage(),
                        "Warning", 0);
            }
        }
    }
    
    /**
     * Deletes the selected Breakpoints.
     */
    private void deleteSelected() {
        Breakpoint bps[] = null;
        
        // Prepare breakpoint list.
        synchronized (breakpoints) {
            int[] rows = breakpointList.getSelectedRows();
            boolean dirty = false;
            
            if (rows == null || rows.length == 0)
                return;
            
            bps = new Breakpoint[rows.length];
            int c = 0;
            
            for (int i : rows) {
                bps[c++] = breakpoints.get(i);
            }
        }
        
        // Delete its items
        if (bps != null) {
            for (Breakpoint bp : bps) {
                BreakpointList.instance().removeBreakpoint(bp);
            }
        }
    }
    
    /**
     * Open dialog for editing the holdback the selected Holdback.
     */
    private void editSelected() {
        Breakpoint bp;
        synchronized (breakpoints) {
            int row = breakpointList.getSelectedRow();
            
            if (row < 0)
                return;
            
            bp = breakpoints.get(row);
        }
        
        if (bp instanceof CalendarBreakpoint) {
            CalendarBreakpointDialog dlg;
            CalendarBreakpoint cbp = (CalendarBreakpoint)bp;
            dlg = new CalendarBreakpointDialog(this,
                    cbp.getBreakpointDate());
            
            if (dlg.open()) {
                Date sDate = dlg.getDate();
                
                try {
                    BreakpointList.instance().setActive(cbp, false);
                    cbp.setBreakpointDate(sDate);
                    BreakpointList.instance().setActive(cbp, true);
                } catch (BreakpointActivationFailedException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Breakpoint could not be activated. " +
                            ex.getMessage(), "Warning", 0);
                }
            }
        }
    }
    
    /**
     * Activates inactive and deactivate active selected breakpoints.
     */
    protected void switchBreakpoint() {
        Breakpoint bps[] = null;
        
        // Prepare breakpoint list.
        synchronized (breakpoints) {
            int[] rows = breakpointList.getSelectedRows();
            boolean dirty = false;
            
            if (rows == null || rows.length == 0)
                return;
            
            bps = new Breakpoint[rows.length];
            int c = 0;
            
            for (int i : rows) {
                bps[c++] = breakpoints.get(i);
            }
        }
        
        // Switch them.
        if (bps != null) {
            for (Breakpoint bp : bps) {
                try {
                    BreakpointList.instance().setActive(bp, !bp.isActive());
                } catch (BreakpointActivationFailedException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Breakpoint could not be activated. " +
                            ex.getMessage(), "Warning", 0);
                }
            }
        }
    }
    
    /**
     * Sets widths of table columns
     *
     * @param colSizes array of the proportional column widths.
     */
    protected void setColumnsWidths( float[] colSizes){
        TableColumnModel tcm = breakpointList.getColumnModel();
        int cols = tcm.getColumnCount();
        for( int i=0; i<cols; i++){
            TableColumn tc = tcm.getColumn( i);
            tc.setPreferredWidth( (int)colSizes[i]*3);
        }
    }
    
    /**
     * Helper refresh listener.
     */
    private class RefreshListener implements Listener {
        public RefreshListener() {
            BreakpointList.instance().getRefreshHook().registerListener(this);
        }
        
        public void changed(Hook caller) {
            synchronized (breakpoints) {
                int[] rows = breakpointList.getSelectedRows();
                
                breakpoints = BreakpointList.instance().getBreakpoints();
                ((BreakpointTableModel)breakpointList.getModel()).update();
                
                if (rows != null && rows.length > 0) {
                    boolean selected = false;
                    int len = breakpoints.size();
                    for (int idx : rows) {
                        if (idx < len) {
                            breakpointList.addRowSelectionInterval(idx, idx);
                            selected = true;
                        }
                    }
                    if (!selected && len > 0) {
                        breakpointList.addRowSelectionInterval(
                                len-1, len-1);
                    }
                }
            }
        }
        
        public void canceled(Hook caller) {
            // Not important
        }
        
        public void unregister() {
            BreakpointList.instance().getRefreshHook().unregisterListener(this);
        }
    }
    
    /**
     * Helper class for Breakpoint table visualisation.
     */
    private class BreakpointTableModel extends AbstractTableModel {
        
        public int getColumnCount() {
            return 2;
        }
        
        public int getRowCount() {
            return breakpoints.size();
        }
        
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "Active";
                case 1:
                    return "Description";
            }
            return "";
        }
        
        public Object getValueAt(int row, int col) {
            Breakpoint bp = breakpoints.get(row);
            
            switch (col) {
                case 0:
                    return bp.isActive() ?
                        IconBag.YES.getIcon() : IconBag.NO.getIcon();
                case 1:
                    return bp.toString();
            }
            return "";
        }
        
        public void update() {
            fireTableDataChanged();
        }
    };
}

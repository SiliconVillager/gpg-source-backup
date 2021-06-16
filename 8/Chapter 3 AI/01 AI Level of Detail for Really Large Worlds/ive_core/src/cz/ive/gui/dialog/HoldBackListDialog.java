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
import cz.ive.gui.icon.IconBag;
import cz.ive.gui.table.IconTableCellRenderer;
import cz.ive.iveobject.*;
import cz.ive.location.*;
import cz.ive.messaging.*;
import cz.ive.simulation.*;
import cz.ive.template.Holdbacks;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

/**
 * Dialog viewing the list of manually added holdbacks. It allows for their
 * addition and deletition as well as editing their parameters.
 *
 * @author ondra
 */
public class HoldBackListDialog extends JDialog {
    
    /** Swing JTable of all manually added holdbacks */
    protected JTable holdbackList;
    
    /** Actual List of Holdbacks */
    protected List<IveObject> holdbacks;
    
    /** Addition Button */
    protected JButton addBtn;
    
    /** Edit Button */
    protected JButton editBtn;
    
    /** Deletition Button */
    protected JButton delBtn;
    
    /** Exit Button */
    protected JButton exitBtn;;
    
    /** Refresh listener */
    protected RefreshListener refreshListener = new RefreshListener();
    
    /** Creates a new instance of HoldBackListDialog */
    protected HoldBackListDialog() {
        super(MainFrame.instance());
        setLayout(new BorderLayout(5, 5));
        setTitle("Holdbacks");
        setModal(true);
        
        holdbacks = Holdbacks.instance().getHoldbacks();
        
        JLabel label = new JLabel("Holdback list: ");
        label.setBorder(new EmptyBorder(5, 5, 0, 5));
        add(label, BorderLayout.NORTH);
        add(new JScrollPane(
                holdbackList = new JTable(new HoldbackTableModel())),
                BorderLayout.CENTER);
        holdbackList.setDefaultRenderer(Object.class,
                new IconTableCellRenderer());
        setColumnsWidths(new float[]{80, 150, 30});
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 5, 5, 5));
        btnPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
        btnPanel.add(new JLabel(""));
        btnPanel.add(new JLabel(""));
        btnPanel.add(addBtn = new JButton("Add", IconBag.ADD.getIcon()));
        btnPanel.add(delBtn = new JButton("Del", IconBag.DELETE.getIcon()));
        btnPanel.add(exitBtn = new JButton("Exit"));
        getRootPane().setDefaultButton(exitBtn);
        add(btnPanel, BorderLayout.SOUTH);
        
        
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                openAdd(HoldBackListDialog.this, null, -1);
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
        HoldBackListDialog dlg = new HoldBackListDialog();
        dlg.setVisible(true);
        dlg.refreshListener.unregister();
    }
    
    /**
     * Fire the dialog for addition of a new Holdback
     *
     * @param owner Frame or Dialog resposible for this dialog.
     * @param position Id of the location to be filled in the dialog
     * @param lodRequested Implicit lod value (-1 for none).
     */
    static public void openAdd(Window owner, String position,
            int lodRequested) {
        if (position == null) {
            SchedulerImpl.instance().lockSimulation();
            Area area = IveMapImpl.instance().getRoot();
            position = area == null ? "" : area.getId();
            SchedulerImpl.instance().unlockSimulation();
        }
        
        HoldBackDialog dlg;
        if (owner  instanceof Dialog)
            dlg = new HoldBackDialog((Dialog)owner, position, lodRequested);
        else
            dlg = new HoldBackDialog((Frame)owner, position, lodRequested);
        
        dlg.open();
        if (!dlg.isAccepted())
            return;
        
        if (!Holdbacks.instance().createHoldback(dlg.getPositionId(),
                dlg.getRequestedLod())) {
            JOptionPane.showMessageDialog(MainFrame.instance(),
                    "Holdback could not be placed. Location that you " +
                    "specified does not exist (but should according to " +
                    "LOD).", "Error", 0);
        }
    }
    
    /**
     * Deletes the selected Holdback.
     */
    private void deleteSelected() {
        IveObject objs[] = null;
        
        synchronized (holdbacks) {
            int[] rows = holdbackList.getSelectedRows();
            if (rows == null || rows.length == 0)
                return;
            
            objs = new IveObject[rows.length];
            int c = 0;
            
            for (int i : rows) {
                objs[c++] = holdbacks.get(i);
            }
        }
        
        if (objs != null) {
            for (IveObject holdback : objs) {
                Holdbacks.instance().removeHoldback(holdback);
            }
        }
    }
    
    /**
     * Sets widths of table columns
     * @param colSizes array of the proportional column widths.
     */
    public void setColumnsWidths( float[] colSizes){
        TableColumnModel tcm = holdbackList.getColumnModel();
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
            Holdbacks.instance().getRefreshHook().registerListener(this);
        }
        
        public void changed(Hook caller) {
            synchronized (holdbacks) {
                int[] rows = holdbackList.getSelectedRows();
                
                holdbacks = Holdbacks.instance().getHoldbacks();
                ((HoldbackTableModel)holdbackList.getModel()).update();
                
                if (rows != null && rows.length > 0) {
                    boolean selected = false;
                    int len = holdbacks.size();
                    for (int idx : rows) {
                        if (idx < len) {
                            holdbackList.addRowSelectionInterval(idx, idx);
                            selected = true;
                        }
                    }
                    if (!selected && len > 0) {
                        holdbackList.addRowSelectionInterval(
                                len-1, len-1);
                    }
                }
            }
        }
        
        public void canceled(Hook caller) {
            // Not important
        }
        
        public void unregister() {
            Holdbacks.instance().getRefreshHook().unregisterListener(this);
        }
    }
    
    /**
     * Helper class for Holdback table visualisation.
     */
    private class HoldbackTableModel extends AbstractTableModel {
        
        public int getColumnCount() {
            return 3;
        }
        
        public int getRowCount() {
            return holdbacks.size();
        }
        
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "Id";
                case 1:
                    return "Position";
                case 2:
                    return "LOD";
            }
            return "";
        }
        
        public Object getValueAt(int row, int col) {
            IveObject obj = holdbacks.get(row);
            
            switch (col) {
                case 0:
                    return obj.getId();
                case 1:
                    return obj.getPosition() == null ? "Not placed yet (run simulation)" :
                        obj.getPosition().getId();
                case 2:
                    return String.valueOf(obj.getHoldback().getView());
            }
            return "";
        }
        
        public void update() {
            fireTableDataChanged();
        }
    };
}

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
 
package cz.ive.logs;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Model for table with all messages
 *
 * @author Zdenek
 */
public class AllMessagesModel extends AbstractTableModel {
    
    /** list with all messages in table */
    protected ArrayList<LogNode> messages;
    
    /** list with messages in actual view */
    protected ArrayList<LogNode> view;
    
    /** actual filter */
    protected boolean filter[];
    
    /** Creates a new instance of AllMessagesModel */
    public AllMessagesModel() {
        filter = new boolean[7];
        for (int i = 0; i < 7; i++) {
            filter[i] = true;
        }
        this.messages = new ArrayList<LogNode>();
        fireTableDataChanged(); 
    }
    
    /** 
     * Method for get number of messages in actual view
     *
     * @return number of rows
     */
    public int getRowCount() {
        return view != null ? view.size() : 0;
    }
    
    /** Method for get number of columns */
    public int getColumnCount() {
        return 5;
    }
    
    /**
     * Method for get value in the table in cell specified by row and column.
     * Special column 4 returns type of message
     * 
     * @param row row of table
     * @param column column of table
     * @return value value in cell
     */
    public Object getValueAt(int row, int column){
        LogNode node = view.get(row);

        switch ( column ) {
            case 0:
                return node.sTimeStr;
            case 1:
                return node.message;
            case 2:
                return node.object;
            case 3:
                return node.process;
            case 4:
                return node.waypoint;
            default:
                return "";
        }
    }

    /**
     * Method for get type of message on row
     * 
     * @param row row of table which type we want to know
     * @return type of message on row
     */
    public int getTypeAt(int row){
        return view.get(row).type;
    }
    
    /**
     * Method which returns name of column specified with column
     *
     * @param column column of table which name we want to know
     * @return name of column
     */
    public String getColumnName( int column ) {
        switch ( column ) {
        case 0:
            return "S-time";
        case 1:
            return "Message";
        case 2:
            return "Object";
        case 3:
            return "Process";
        case 4:
            return "Waypoint";
        default:
            return "unknown";
        }
    }

    /**
     * Method which returns type of column specified with column
     *
     * @param column column of table which type we want to know
     * @return type of column
     */
    public Class getColumnClass( int column ) {
        return String.class;
    }
    
    /**
     * Changes filter of table and refresh data.
     * 
     * @param fil new filter
     */
    public void changeFilter(boolean fil[]) {
        for (int i = 0; i < 7; i++) {
            filter[i] = fil[i];
        }
        refresh();
    }
    
    /**
     * Adds message contained in node into table. 
     *
     * @param node node with message
     */
    public void addMessage(LogNode node) {
        messages.add(node);
        if (filter[node.type] == true) {
            if (view == null) {
                view = new ArrayList<LogNode>();
            }
            view.add(node);
            fireTableRowsInserted(view.size(), view.size());
        }
    }
    
    /**
     * Removes message contained in node from table.
     *
     * @param node node with message
     */
    public void recycleMessage(LogNode node) {
        messages.remove(node);
        if (filter[node.type] == true) {
            if (view == null) {
                view = new ArrayList<LogNode>();
            }
            int idx = view.indexOf(node);
            if (idx >= 0) {
                view.remove(idx);
                fireTableRowsDeleted(idx, idx);
            }
        }
    }
    
    /**
     * Refresges view using actual filter.
     */
    public void refresh() {
        if (view == null) {
            view = new ArrayList<LogNode>();
        } else {
            view.clear();
        }
        
        for (java.util.Iterator i = messages.iterator(); i.hasNext();) {
            LogNode node = (LogNode)i.next();
            if (filter[node.type] == true) {
                view.add(node);
            }
        }
        fireTableDataChanged(); 
    }
    
}

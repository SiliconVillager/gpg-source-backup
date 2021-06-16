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

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * Model for table with messages - content of LogDir
 *
 * @author Zdenek
 */
public class MessageModel extends AbstractTableModel {

    /** List with all messages in table */
    protected ArrayList<LogNode> messages;
    
    /** 
     * Type of LogDir which content is actually shown
     * 0 - object
     * 1 - process
     * 2 - waypoint 
     */
    protected int type;
    
    /** LogDir which content is actually shown */
    protected LogDir active;
    
    /** actual filter */
    protected boolean filter[];
            
    /** Creates a new instance of MessageModel */
    public MessageModel() {
        filter = new boolean[7];
        for (int i = 0; i < 7; i++) {
            filter[i] = true;
        }
        
    }
    
    /**
     * Creates MessageModel with specified type and dir
     * 
     * @param type type of dir
     * @param dir directory which content will be shown
     */
    public MessageModel(int type, LogDir dir) {
        this();
        active = dir;
        if (dir != null) {
            this.messages = dir.getView();
            dir.visible = true;
        } else {
            this.messages = null;
        }
        
        this.type = type;
        fireTableDataChanged(); 
    }
    
    /**
     * Sets data of table from dir.
     * 
     * @param type type of dir
     * @param dir directory which content will be shown
     */
    public void setData(int type, LogDir dir) {
        if (active != null)
            active.visible = false;
        active = dir;
        if (dir != null) {
            this.messages = dir.getView();
            dir.visible = true;
        } else {
            this.messages = null;
        }
        this.type = type;
        fireTableDataChanged(); 
    }
    
    /**
     * @return number of rows in table
     */
    public int getRowCount() {
        return messages != null ? this.messages.size() : 0;
    }

    /**
     * @return number of columns in table
     */
    public int getColumnCount() {
        return 4;
    }

    /** 
     * Gets value in cell of table specified by row and column.
     * Special column 3 returns type of message
     *
     * @param row row of table
     * @param column column of table
     * @return value in cell
     */
    public Object getValueAt(int row, int column){
        LogNode node = messages.get(row);

        switch ( column ) {
            case 0:
                return node.sTimeStr;
            case 1:
                return node.message;
            case 2:
                switch (this.type) {
                    case 0:
                        return node.process;
                    case 1:
                        return node.object;
                    case 2:
                        return node.object;
                }
            case 3:
                switch (this.type) {
                    case 0:
                        return node.waypoint;
                    case 1:
                        return node.waypoint;
                    case 2:
                        return node.process;
                }
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
        return messages.get(row).type;
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
            switch (this.type) {
                case 0: 
                    return "Process";
                case 1:
                    return "Object";
                case 2:
                    return "Object";
                default:
                    return "Unknown";
            }
        case 3:
            switch (this.type) {
                case 0: 
                    return "Waypoint";
                case 1:
                    return "Waypoint";
                case 2:
                    return "Process";
                default:
                    return "Unknown";
            }
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
     * Changes actual filter of table.
     * 
     * @param fil new filter
     */
    public void changeFilter(boolean fil[]) {
        for (int i = 0; i < 7; i++) {
            filter[i] = fil[i];
        }
        setData(type, active);
    }

    
}

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
import javax.swing.tree.DefaultTreeModel;

/**
 * Model of tree of LogDirs
 *
 * @author Zdenek
 */
public class LogModel extends DefaultTreeModel {
    
    /** Type of tree 0..object, 1..process, 2..waypoint */
    protected int type;
    
    /**
     * Creates new instance of LogNodel.
     *
     * @param type type of tree
     * @param tableModel model of table where content of dirs are shown
     */
    public LogModel(int type, AbstractTableModel tableModel) {
        super(null);
        this.root = new LogDir(this, tableModel, "empty");
        this.type = type;
    }
    
    /**
     * Add log from node to the tree.
     *
     * @param node node with message
     */
    public void addMessage(LogNode node) {
        String path = "";
        switch (type) {
            case 0: 
                path = node.object;
                break;
            case 1:
                path = node.process;
                break;
            case 2:
                path = node.waypoint;
                break;
        }
        ((LogDir)root).add(path, node);
    }
    
    /**
     * Removes message in node from tree
     * 
     * @param node node with message
     */
    public void recycleMessage(LogNode node) {
        String path = "";
        switch (type) {
            case 0: 
                path = node.object;
                break;
            case 1:
                path = node.process;
                break;
            case 2:
                path = node.waypoint;
                break;
        }
        ((LogDir)root).remove(path, node);
    }
}

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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
/**
 * Directory with log message.
 *
 * @author Zdenek
 */
public class LogDir extends DefaultMutableTreeNode {
    
    /** Name of directory */
    protected String name;
    
    /** List with messages in directory */
    protected ArrayList<LogNode> messages;
    
    /** List with messages in actual view in directory */
    protected ArrayList<LogNode> view;
    
    /** Model of tree where is directory */
    protected DefaultTreeModel treeModel;
    
    /** Model of table where will be messages shown */
    protected AbstractTableModel tableModel;
    
    /** true when content of directory is shown in table */
    protected boolean visible;
    
    /** true when directory contains some message in actual view */
    protected boolean containsMessages;
    
    /**
     * Private empty constructor. Disables creation of directory without seting
     * models.
     */
    private LogDir() {
        
    }
    
    /**
     * Creates new directory. 
     * @param treeModel model of tree which will contains directory
     * @param tableModel model of table where is content of directory shown
     */
    public LogDir(DefaultTreeModel treeModel, AbstractTableModel tableModel) {
        this(treeModel, tableModel, null);
    }
    
    /**
     * Creates new directory. 
     * @param treeModel model of tree which will contains directory
     * @param tableModel model of table where is content of directory shown
     * @param name name of directory
     */
    public LogDir(DefaultTreeModel treeModel, AbstractTableModel tableModel,
            String name) {
        this.containsMessages = false;
        this.treeModel = treeModel;
        this.tableModel = tableModel;
        this.messages = null;
        this.name = name;
        this.visible = false;
        this.view = null;
    }
    
    /**
     * Get name of directory. If directory contains messages then name starts
     * with dot.
     * 
     * @return name of directory
     */
    public String toString() {
        if (this.containsMessages == true) {
            return "." + name;
        } else {
            return name;
        }
        
    }
    
    /**
     * Reloads actual view of directory and all of its childs.
     */
    public void reload() {
        this.containsMessages = false;
        if (messages != null) {
            for (java.util.Iterator i = messages.iterator(); i.hasNext();) {
                if (((MessageModel)tableModel).
                        filter[((LogNode)i.next()).type] == true) {
                    this.containsMessages = true;
                    break;
                }
            }
        }
        treeModel.nodeChanged(this);
        if (!isLeaf()) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode)this.getFirstChild();
            while (node != null) {
                ((LogDir)node).reload();
                node = node.getNextLeaf();
            }
        }
    }
    
    /**
     * Gets child with name n. If child doesn't exists, we will create it.
     *
     * @param n name of child we want
     * @return child with name n
     */
    protected LogDir getDir(String n) {
        int index = 0;
        if (!this.isLeaf()) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode)this.getFirstChild();
            while (node != null && ((LogDir)node).name.
                    compareToIgnoreCase(n) <= 0) {
                if (((LogDir)node).name.compareToIgnoreCase(n) == 0) {
                    return (LogDir)node;
                }
                index++;
                node = node.getNextSibling();
            }
        }
        LogDir newNode = new LogDir(this.treeModel, this.tableModel, n);
        treeModel.insertNodeInto(newNode,  this, index);
        return newNode;
    }
    
    /**
     * Gets view of directory.
     * 
     * @return list of nodes in directory using filter of treemodel
     */
    public ArrayList<LogNode> getView() {
        if (messages == null) {
            return null;
        }
        if (view == null) {
            view = new ArrayList<LogNode>();
        } else {
            view.clear();
        }
        
        for (java.util.Iterator i = messages.iterator(); i.hasNext();) {
            LogNode node = (LogNode)i.next();
            if (((MessageModel)tableModel).filter[node.type] == true) {
                view.add(node);
            }
        }
        
        return view;
        
    }
    
    /**
     * Adds message in node into directory or into subdirectory (defined by 
     * path).
     * 
     * @param path subpath where message with node will be added. Directories 
     *  are delimited by dot
     * @param node message
     */
    public void add(String path, LogNode node) {
        if (path == null || path.length() == 0) {
            // add node to list
            if (messages == null) {
                messages = new ArrayList<LogNode>();
            }
            messages.add(node);
            if (visible &&
                    ((MessageModel)tableModel).filter[node.type] == true) {
                // actualize table
                if (view == null) {
                    view = getView();
                }
                view.add(node);
                tableModel.fireTableRowsInserted(view.size(), view.size());
            }
            if (this.containsMessages == false &&
                    ((MessageModel)tableModel).filter[node.type] == true) {
                this.containsMessages = true;
                treeModel.nodeChanged(this);
            }
            return;
        }
        
        String dirname;
        String rest;
        if (path.indexOf('.') == -1) {
            dirname = path;
            rest = "";
        } else {
            dirname = path.substring(0, path.indexOf('.'));
            rest = path.substring(path.indexOf('.')+1);
        }
        // find (or create) subdir and add there
        getDir(dirname).add(rest, node);
    }
    
    
    /**
     * Removes message in node from directory or from subdirectory (defined by 
     * path).
     * 
     * @param path subpath from where message with node will be removed. 
     *  Directories are delimited by dot
     * @param node message
     */
    public void remove(String path, LogNode node) {
        if (path == null || path.length() == 0) {
            // add node to list
            if (messages == null) {
                messages = new ArrayList<LogNode>();
            }
            messages.remove(node);
            if (visible &&
                    ((MessageModel)tableModel).filter[node.type] == true) {
                // actualize table
                if (view == null) {
                    view = getView();
                }
                int idx = view.indexOf(node);
                if (idx >= 0) {
                    view.remove(idx);
                    tableModel.fireTableRowsDeleted(idx, idx);
                }
            }
            
            // Remove node if empty and without childs
            if (messages.size() == 0 && getChildCount() == 0) {
                LogDir pnode = this;
                LogDir parent;
                
                while ((pnode.messages == null || pnode.messages.size() == 0) &&
                        pnode.getChildCount() == 0) {
                    parent = (LogDir)pnode.getParent();
                    if (parent == null)
                        break;
                    treeModel.removeNodeFromParent(pnode);
                    pnode = parent;
                }
                return;
            }
            
            // Mark as an empty node.
            if (this.containsMessages == true && 
                    ((view != null && view.size() == 0) ||
                    (messages.size() == 0))) {
                this.containsMessages = false;
                treeModel.nodeChanged(this);
            }
            return;
        }
        
        String dirname;
        String rest;
        if (path.indexOf('.') == -1) {
            dirname = path;
            rest = "";
        } else {
            dirname = path.substring(0, path.indexOf('.'));
            rest = path.substring(path.indexOf('.')+1);
        }
        // find (or create) subdir and add there
        getDir(dirname).remove(rest, node);
    }
}

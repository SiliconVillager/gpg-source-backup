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
 
package cz.ive.gui;

import cz.ive.logs.*;
import cz.ive.gui.icon.*;
import cz.ive.gui.subwindow.LogSubwindow;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.*;

/**
 * Extension of JPanel which displays graphic representation of hierarchical log
 *
 * @author Zdenek
 */
public class GuiLog extends JPanel implements LogReceiver {
    
    /** Graphic tree component used for all three trees */
    protected JTree tree;
    
    /** Graphic table component for content of directory with messages */
    protected JTable table;
    
    /** mode  for objecttree */
    protected LogModel objectModel;
    
    /** mode  for processtree */
    protected LogModel processModel;
    
    /** mode  for waypointtree */
    protected LogModel waypointModel;
    
    /** model for display log for one object/process/waypoint */
    protected MessageModel tableModel;
    
    /** model for display all messages */
    protected AllMessagesModel allMessageModel;
    
    /**  Scroll panel for tree component */
    protected JScrollPane treeScroller;
    
    /**  Scroll panel for table component */
    protected JScrollPane tableScroller;
    
    /**  Scroll panel for alltable component */
    protected JScrollPane allTableScroller;
    
    /** Split panel for tree & table components */
    protected JSplitPane topBoth;
    
    /** toolbar with buttons and checkboxes */
    protected JToolBar toolBar;
    
    /** button to show tree with messages by object */
    protected JButton objectButton;
    
    /** button to show tree with messages by process */
    protected JButton processButton;
    
    /** button to show tree with messages by waypoint */
    protected JButton waypointButton;
    
    /** button to show table with all messages */
    protected JButton allButton;
    
    /** Actually selected tree */
    protected int selectedTree;
    
    /** last path selected in object tree */
    protected TreePath objectLastPath;
    
    /** last path selected in process tree */
    protected TreePath processLastPath;
    
    /** last path selected in waypoint tree */
    protected TreePath waypointLastPath;
    
    /** Window outside GuiLog */
    protected LogSubwindow window;
    
    /** Queue with messages which will be added */
    private Queue<LogNode> addQueue;
    
    /** Queue with messages which will be removed */
    private Queue<LogNode> recycleQueue;
    
    /** Lock for adding/removing operations */
    private ReentrantLock lock;
    
    /**
     * Creates a new instance of GuiLog.
     *
     * @param window window outside GuiLog
     */
    public GuiLog(LogSubwindow window) {
        super();
        
        this.window = window;
        
        // initialize queues and lock
        addQueue = new LinkedList<LogNode>();
        recycleQueue = new LinkedList<LogNode>();
        lock = new ReentrantLock();
        
        // object tree is selected
        selectedTree = 0;
        
        // create table model
        tableModel = new MessageModel();
        
        // create alltable model
        allMessageModel = new AllMessagesModel();
        
        // create tree models
        objectModel = new LogModel(0, tableModel);
        processModel = new LogModel(1, tableModel);
        waypointModel = new LogModel(2, tableModel);
        
        // inicialize lastpath of models
        objectLastPath = null;
        processLastPath = null;
        waypointLastPath = null;
        
        // create tree
        tree = new JTree(objectModel);
        tree.setShowsRootHandles( true );
        tree.putClientProperty( "JTree.lineStyle", "Angled" );
        tree.addTreeSelectionListener(new TreeListener());
        tree.setCellRenderer(new TreeRenderer());
        
        // create table
        table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer renderer,
                    int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex,
                        vColIndex);
                int type = tableModel.getTypeAt(rowIndex);
                switch (type) {
                    case 0:
                        c.setBackground(LogFilter.SEVERE);
                        break;
                    case 1:
                        c.setBackground(LogFilter.WARNING);
                        break;
                    case 2:
                        c.setBackground(LogFilter.INFO);
                        break;
                    case 3:
                        c.setBackground(LogFilter.CONFIG);
                        break;
                    case 4:
                        c.setBackground(LogFilter.FINE);
                        break;
                    case 5:
                        c.setBackground(LogFilter.FINER);
                        break;
                    case 6:
                        c.setBackground(LogFilter.FINEST);
                        break;
                        
                }
                ((JLabel)c).setToolTipText(
                        (String)tableModel.getValueAt(rowIndex, 1));
                
                if (vColIndex == 0) {
                    ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                } else {
                    ((JLabel)c).setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        };
        table.setShowHorizontalLines( true );
        table.setShowVerticalLines( true );
        table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        
        // create table for allmessage log
        JTable allTable = new JTable(allMessageModel) {
            public Component prepareRenderer(TableCellRenderer renderer,
                    int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex,
                        vColIndex);
                int type = allMessageModel.getTypeAt(rowIndex);
                switch (type) {
                    case 0:
                        c.setBackground(LogFilter.SEVERE);
                        break;
                    case 1:
                        c.setBackground(LogFilter.WARNING);
                        break;
                    case 2:
                        c.setBackground(LogFilter.INFO);
                        break;
                    case 3:
                        c.setBackground(LogFilter.CONFIG);
                        break;
                    case 4:
                        c.setBackground(LogFilter.FINE);
                        break;
                    case 5:
                        c.setBackground(LogFilter.FINER);
                        break;
                    case 6:
                        c.setBackground(LogFilter.FINEST);
                        break;
                }
                ((JLabel)c).setToolTipText(
                        (String)allMessageModel.getValueAt(rowIndex, 1));
                
                if (vColIndex == 0) {
                    ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                } else {
                    ((JLabel)c).setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        };
        table.setShowHorizontalLines( true );
        table.setShowVerticalLines( true );
        table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        
        // create scrollers
        treeScroller = new JScrollPane(tree);
        tableScroller = JTable.createScrollPaneForTable(table);
        allTableScroller = JTable.createScrollPaneForTable(allTable);
        
        tableScroller.setBackground(Color.white);
        allTableScroller.setBackground(Color.white);
        
        treeScroller.setPreferredSize(new Dimension(200, 450));
        tableScroller.setPreferredSize(new Dimension(350, 450));
        allTableScroller.setPreferredSize(new Dimension(600, 450));
        
        topBoth = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                treeScroller, tableScroller);
        topBoth.setOneTouchExpandable(true);
        
        toolBar = new JToolBar("Log filter", JToolBar.HORIZONTAL);
        
        // create buttons
        objectButton = new JButton("Object");
        processButton = new JButton("Process");
        waypointButton = new JButton("Waypoint");
        allButton = new JButton("All messsages");
        
        objectButton.setForeground(Color.RED);
        
        // add listeners to buttons
        objectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switch (selectedTree) {
                    case 0:
                        return;
                    case 1:
                        // show last path
                        processLastPath = tree.getSelectionPath();
                        processButton.setForeground(Color.BLACK);
                        break;
                    case 2:
                        // show last path
                        waypointLastPath = tree.getSelectionPath();
                        waypointButton.setForeground(Color.BLACK);
                        break;
                    case 3:
                        // hide allTable
                        remove(allTableScroller);
                        // show tree & table
                        add(topBoth, BorderLayout.CENTER);
                        invalidate();
                        validate();
                        repaint();
                        allButton.setForeground(Color.BLACK);
                        break;
                }
                objectButton.setForeground(Color.RED);
                selectedTree = 0;
                // show tree with objects
                tree.setModel(objectModel);
                tree.setSelectionPath(objectLastPath);
                // set table with messages from last path
                if (objectLastPath != null) {
                    tableModel.setData(selectedTree, (LogDir)objectLastPath.
                            getLastPathComponent());
                } else {
                    tableModel.setData(selectedTree, null);
                }
                // change header
                table.getTableHeader().repaint();
                table.getTableHeader().getColumnModel().getColumn(2).
                        setHeaderValue("Process");
                table.getTableHeader().getColumnModel().getColumn(3).
                        setHeaderValue("Waypoint");
            }
        });
        
        processButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switch (selectedTree) {
                    case 0:
                        // show last path
                        objectLastPath = tree.getSelectionPath();
                        objectButton.setForeground(Color.BLACK);
                        break;
                    case 1:
                        return;
                    case 2:
                        // show last path
                        waypointLastPath = tree.getSelectionPath();
                        waypointButton.setForeground(Color.BLACK);
                        break;
                    case 3:
                        // hide allTable
                        remove(allTableScroller);
                        // show tree & table
                        add(topBoth, BorderLayout.CENTER);
                        invalidate();
                        validate();
                        repaint();
                        allButton.setForeground(Color.BLACK);
                        break;
                }
                processButton.setForeground(Color.RED);
                selectedTree = 1;
                // show tree with processes
                tree.setModel(processModel);
                // set table with messages from last path
                tree.setSelectionPath(processLastPath);
                if (processLastPath != null) {
                    tableModel.setData(selectedTree, (LogDir)processLastPath.
                            getLastPathComponent());
                } else {
                    tableModel.setData(selectedTree, null);
                }
                // change header
                table.getTableHeader().repaint();
                table.getTableHeader().getColumnModel().getColumn(2).
                        setHeaderValue("Object");
                table.getTableHeader().getColumnModel().getColumn(3).
                        setHeaderValue("Waypoint");
            }
        });
        
        waypointButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switch (selectedTree) {
                    case 0:
                        objectLastPath = tree.getSelectionPath();
                        objectButton.setForeground(Color.BLACK);
                        break;
                    case 1:
                        processLastPath = tree.getSelectionPath();
                        processButton.setForeground(Color.BLACK);
                        break;
                    case 2:
                        return;
                    case 3:
                        // hide allTable
                        remove(allTableScroller);
                        add(topBoth, BorderLayout.CENTER);
                        invalidate();
                        validate();
                        repaint();
                        allButton.setForeground(Color.BLACK);
                        break;
                }
                waypointButton.setForeground(Color.RED);
                selectedTree = 2;
                // show tree with waypoints
                tree.setModel(waypointModel);
                tree.setSelectionPath(waypointLastPath);
                if (waypointLastPath != null) {
                    tableModel.setData(selectedTree, (LogDir)waypointLastPath.
                            getLastPathComponent());
                } else {
                    tableModel.setData(selectedTree, null);
                }
                // change header
                table.getTableHeader().repaint();
                table.getTableHeader().getColumnModel().getColumn(2).
                        setHeaderValue("Object");
                table.getTableHeader().getColumnModel().getColumn(3).
                        setHeaderValue("Process");
            }
        });
        
        allButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switch (selectedTree) {
                    case 0:
                        objectLastPath = tree.getSelectionPath();
                        objectButton.setForeground(Color.BLACK);
                        break;
                    case 1:
                        processLastPath = tree.getSelectionPath();
                        processButton.setForeground(Color.BLACK);
                        break;
                    case 2:
                        waypointLastPath = tree.getSelectionPath();
                        waypointButton.setForeground(Color.BLACK);
                        break;
                    case 3:
                        return;
                }
                // hide tree & table
                remove(topBoth);
                // show alltable
                add(allTableScroller, BorderLayout.CENTER);
                
                allMessageModel.refresh();
                
                allButton.setForeground(Color.RED);
                
                selectedTree = 3;
                
                invalidate();
                validate();
                repaint();
            }
        });
        
        toolBar.add(objectButton);
        toolBar.add(processButton);
        toolBar.add(waypointButton);
        toolBar.add(allButton);
        
        LogModel models[] = new LogModel[3];
        models[0] = objectModel;
        models[1] = processModel;
        models[2] = waypointModel;
        
        JPanel tableSelection = new LogFilter(tableModel, models,
                allMessageModel);
        
        toolBar.add(tableSelection);
        
        this.setLayout(new BorderLayout());
        
        add(toolBar, BorderLayout.NORTH);
        add(topBoth, BorderLayout.CENTER);
        
        setColumnsWidths(table, new float[] {50, 400, 100, 100});
        setColumnsWidths(allTable, new float[] {50, 400, 80, 80, 80});
    }
    
    /**
     * Sets widths of table columns
     *
     * @param table A JTable to be altered.
     * @param colSizes array of the proportional column widths.
     */
    public void setColumnsWidths(JTable table, float[] colSizes){
        TableColumnModel tcm = table.getColumnModel();
        int cols = tcm.getColumnCount();
        for( int i=0; i<cols; i++){
            TableColumn tc = tcm.getColumn( i);
            tc.setPreferredWidth( (int)colSizes[i]*3);
        }
    }
    
    /**
     * Adds log message in trees and alltable
     * @param node LogNode which contains message
     *
     */
    private synchronized void addMessageNow(LogNode node) {
        objectModel.addMessage(node);
        processModel.addMessage(node);
        waypointModel.addMessage(node);
        allMessageModel.addMessage(node);
    }
    
    /**
     * Recycles log message from trees and alltable
     * @param node LogNode which contains message
     *
     */
    private synchronized void recycleMessageNow(LogNode node) {
        objectModel.recycleMessage(node);
        processModel.recycleMessage(node);
        waypointModel.recycleMessage(node);
        allMessageModel.recycleMessage(node);
    }
    
    /**
     * Adds log message. Message will be added imediatelly if window with GuiLog
     * is visible, otherwise message will be added into addQueue
     * @param node LogNode which contains message
     *
     */
    public void addMessage(LogMessage node) {
        if (node == null)
            return;
        if (!(node instanceof LogNode))
            return;
        final LogNode n = (LogNode)node;
        if (window.isInvisible()) {
            lock.lock();
            addQueue.add(n);
            lock.unlock();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    addMessageNow(n);
                }
            });
        }
        
    }
    
    /**
     * Removes log message. Message will be removed imediatelly if window with
     * GuiLog is visible, otherwise message will be added into recycleQueue
     * @param node LogNode which contains message
     *
     */
    public void recycleMessage(LogMessage node) {
        if (node == null)
            return;
        if (!(node instanceof LogNode))
            return;
        final LogNode n = (LogNode)node;
        if (window.isInvisible()) {
            lock.lock();
            if (addQueue.peek() == n) {
                addQueue.poll();
            } else {
                recycleQueue.add(n);
            }
            lock.unlock();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    recycleMessageNow(n);
                }
            });
        }
    }
    
    /**
     * Processes all messages to add into GuiLog and remove from GuiLog. This
     * method is called after window with GuiLog become visible.
     *
     */
    public void processMessages() {
        lock.lock();
        final Queue<LogNode> addQ = addQueue;
        addQueue = new LinkedList<LogNode>();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (LogNode node : addQ) {
                    addMessageNow(node);
                }
                
            }
        });
        final Queue<LogNode> recycleQ = recycleQueue;
        recycleQueue = new LinkedList<LogNode>();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (LogNode node : recycleQ) {
                    recycleMessageNow(node);
                }
                
            }
        });
        lock.unlock();
    }
    
    /**
     * Class with TreeSelectionListener which react for clicks on object /
     * process / waypoint tree and changes table with messages contained in
     * node user clicks on.
     *
     */
    protected class TreeListener implements TreeSelectionListener {
        public TreeListener() {
        }
        
        public void valueChanged( TreeSelectionEvent e ) {
            LogDir dir = (LogDir)e.getPath().getLastPathComponent();
            tableModel.setData(selectedTree, dir);
            
        }
    }
    
    /**
     * Class which changes default icons of tree. Icon depends if node / branch
     * contains messages or not.
     */
    class TreeRenderer extends DefaultTreeCellRenderer {
        
        Icon leafMessageIcon;
        Icon leafIcon;
        Icon branchMessageIcon;
        Icon branchIcon;
        
        /**
         * Initialize icons for tree
         */
        public TreeRenderer() {
            leafMessageIcon = IconBag.LOG_LEAF_MESSAGE.getIcon();
            leafIcon = IconBag.LOG_LEAF.getIcon();
            branchMessageIcon = IconBag.LOG_BRANCH_MESSAGE.getIcon();
            branchIcon = IconBag.LOG_BRANCH.getIcon();
        }
        
        /**
         * Override getTreeCellRendererComponent from DefaultTreeCellRenderer.
         * Changes icon of branch/leaf if contains message(s).
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                    row, hasFocus);
            if (leaf) {
                if (value != null && value.toString().length() > 0 &&
                        value.toString().charAt(0) == '.') {
                    setIcon(leafMessageIcon);
                    setText(value.toString().substring(1));
                } else {
                    setIcon(leafIcon);
                }
            } else {
                if (value != null && value.toString().length() > 0 &&
                        value.toString().charAt(0) == '.') {
                    setIcon(branchMessageIcon);
                    setText(value.toString().substring(1));
                } else {
                    setIcon(branchIcon);
                }
            }
            
            return this;
        }
        
    }
}


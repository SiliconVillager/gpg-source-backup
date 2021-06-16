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

import cz.ive.gui.dialog.HoldBackListDialog;
import cz.ive.gui.subwindow.*;
import cz.ive.gui.tree.*;
import cz.ive.iveobject.*;
import cz.ive.location.*;
import cz.ive.simulation.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Tree view for Loaction hierarchy visualisation.
 *
 * @author ondra
 */
public class LocationTree extends JTree implements Subwindow {
    
    /** Info for all Animated panel... it probably wont be used */
    protected static Info LOCATION_TREE_INFO = new Info("Location tree",
            "This panel views tree of locations",
            (Icon)null);
    
    /** String identification of the location tree subwindow. */
    public static String LOCATION_TREE = "Location tree";
    
    /** WayPoint treated as a root of location hierarchy */
    protected WayPoint root;
    
    /** Tree Model */
    DefaultTreeModel model;
    
    /** Responsible Subwindow container */
    SubwindowContainer container;
    
    /** Are we on the screen? Should we update? */
    protected boolean invisible = true;
    
    /** Are we listening right now? */
    protected boolean listening = false;
    
    /**
     * Creates a new instance of LocationTree
     *
     * @param root WayPoint to be treated as a root
     */
    public LocationTree(WayPoint root) {
        super();
        setModel(model = new DefaultTreeModel(null));
        setToggleClickCount(1);
        setShowsRootHandles(true);
        setCellRenderer(new LocationTreeCellRenderer());
        
        initialize(root);
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // Try to show the popup.
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                // Try to show the popup.
                maybeShowPopup(e);
            }
            
            public void mouseClicked(MouseEvent e) {
                // Perform the shortcut action
                if (e.getButton() == e.BUTTON2) {
                    int selRow = getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = getPathForLocation(e.getX(), e.getY());
                    if (selRow != -1) {
                        try {
                            performShortcutAction((LocationTreeNode)
                            ((DefaultMutableTreeNode)selPath.
                                    getLastPathComponent()).getUserObject());
                        } catch (ClassCastException ex) {
                            // No world is loaded.
                        }
                    }
                }
            }
            
            private boolean maybeShowPopup(MouseEvent e) {
                // Was this event a popup trigger at the current window system?
                if (e.isPopupTrigger()) {
                    int selRow = getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = getPathForLocation(e.getX(), e.getY());
                    if (selRow != -1) {
                        try {
                            JPopupMenu menu = getPopupMenu((LocationTreeNode)
                            ((DefaultMutableTreeNode)selPath.
                                    getLastPathComponent()).getUserObject());
                            
                            if (menu != null) {
                                menu.show(e.getComponent(),
                                        e.getX(), e.getY());
                                return true;
                            }
                        } catch (ClassCastException ex) {
                            // No world is loaded.
                        }
                    }
                }
                return false;
            }
        });
    }
    
    /**
     * Initialize the location tree by a given root location.
     *
     * @param root WayPoint to be treated as a root.
     */
    public void initialize(WayPoint root) {
        if (listening)
            stopListening();
        
        this.root = root;
        
        DefaultMutableTreeNode rootNode;
        if(root==null) {
            rootNode = new DefaultMutableTreeNode("<<< EMPTY >>>");
        } else {
            rootNode = createNode(root);
        }
        model.setRoot(rootNode);
        startListening();
    }
    
    /**
     * Creates JTree node representing given loaction.
     *
     * @param wp WayPoint to be transformed to the JTree node
     */
    protected DefaultMutableTreeNode createNode(WayPoint wp) {
        LocationTreeNode ltNode = new LocationTreeNode(this, wp);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                ltNode);
        ltNode.setTreeNode(node);
        
        if (wp instanceof Area) {
            Area area = (Area)wp;
            WayPoint[] wps = area.getWayPoints();
            
            if (wps != null) {
                int i=0;
                for (WayPoint child: wps) {
                    node.insert( createNode( child), i++);
                }
            }
        }
        
        return node;
    }
    
    /**
     * A given node was expanded .
     *
     * @param node DefaultMutableTreeNode representing Area being expanded.
     */
    public void expand(DefaultMutableTreeNode node) {
        ((LocationTreeNode)node.getUserObject()).stopListening();
        WayPoint wp = ((LocationTreeNode)node.getUserObject()).getWayPoint();
        wp = (WayPoint)ObjectMap.instance().getObject(wp.getId());
        
        DefaultMutableTreeNode parent =
                (DefaultMutableTreeNode)node.getParent();
        ((LocationTreeNode)node.getUserObject()).stopListening();
        
        if (parent == null) {
            node = createNode(wp);
            model.setRoot(node);
        } else {
            int idx = parent.getIndex(node);
            DefaultMutableTreeNode newNode = createNode(wp);
            model.insertNodeInto(newNode, parent, idx);
            removeSubtree(node);
            model.removeNodeFromParent(node);
            node = newNode;
        }
        setListening(node, listening);
    }
    
    /**
     * A given node was shrunk.
     *
     * @param node DefaultMutableTreeNode representing Area being shrunk.
     */
    public void shrink(DefaultMutableTreeNode node) {
        removeSubtree(node);
    }
    
    /**
     * Remove subtree of the given node.
     *
     * @param node root of the subtree to be removed
     */
    protected void removeSubtree(DefaultMutableTreeNode node) {
        while( node.getChildCount() > 0) {
            DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode)node.getFirstChild();
            setListening(child, false);
            removeSubtree(child);
            model.removeNodeFromParent(child);
        }
    }
    
    /**
     * Starts with the listening to locations expanding and shrinking.
     */
    protected void startListening() {
        listening = true;
        setListening((DefaultMutableTreeNode)model.getRoot(), true);
    }
    
    /**
     * Stops with the listening to locations expanding and shrinking.
     */
    protected void stopListening() {
        listening = false;
        setListening((DefaultMutableTreeNode)model.getRoot(), false);
    }
    
    /**
     * Recursive method for setting up or stopping the listening.
     *
     * @param node root of the subtree to be affected.
     */
    protected void setListening(DefaultMutableTreeNode node,
            boolean listening) {
        if (listening) {
            Object obj = node.getUserObject();
            if (obj instanceof LocationTreeNode) {
                ((LocationTreeNode)obj).startListening();
            }
        } else {
            Object obj = node.getUserObject();
            if (obj instanceof LocationTreeNode) {
                ((LocationTreeNode)obj).stopListening();
            }
        }
        for (int i=0; i<node.getChildCount(); i++) {
            setListening((DefaultMutableTreeNode)node.getChildAt(i), listening);
        }
    }
    
    /**
     * Performs shorcut action (a implicit action) on the selected tree node.
     * We only perform implicit action on Areas, where we show the area in the
     * new Tab.
     *
     * @param node LocationTreeNode for which the perform the action.
     */
    public void performShortcutAction(LocationTreeNode node) {
        if (
                node.getType().equals(LocationTreeNodeType.GRID_AREA) ||
                node.getType().equals(LocationTreeNodeType.GRAPH_AREA)) {
            new PopupActionListener(
                    node.getWayPoint().getId(), false).actionPerformed(null);
        }
    }
    
    /**
     * Prepares popup menu for the given tree node.
     *
     * @param node LocationTreeNode for which the popup menu should be prepared.
     * @return JPopupMenu created from info the given node.
     */
    public JPopupMenu getPopupMenu(final LocationTreeNode node) {
        JPopupMenu menu = new JPopupMenu();
        String type = "";
        
        if (
                node.getType().equals(LocationTreeNodeType.GRID_AREA) ||
                node.getType().equals(LocationTreeNodeType.GRAPH_AREA)) {
            type = "Area";
            
            String openStr = "Open "+type+" \""+node.toString()+"\"";
            menu.add(openStr+" in Window").addActionListener(
                    new PopupActionListener(node.getWayPoint().getId(), true));
            menu.add(openStr+" in Tab").addActionListener(
                    new PopupActionListener(node.getWayPoint().getId(), false));
            
            final int lodRequested = node.getWayPoint().getLod() + 1;
            
            menu.addSeparator();
            menu.add("Add holdback").addActionListener(
                    new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    HoldBackListDialog.openAdd(MainFrame.instance(),
                            node.getWayPoint().getId(), lodRequested);
                }
            });
            
        } else if (
                node.getType().equals(LocationTreeNodeType.WAYPOINT)
                ) {
            type = "WayPoint";
            
            final int lodRequested = node.getWayPoint().getLod();
            
            menu.add("Add holdback").addActionListener(
                    new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    HoldBackListDialog.openAdd(MainFrame.instance(),
                            node.getWayPoint().getId(), lodRequested);
                }
            });
        } else if (
                node.getType().equals(LocationTreeNodeType.OBJECT)
                ) {
            type = "Object";
            
            return null;
        }
        
        return menu;
    }
    
    /**
     * Extension of Action listener. This is used for opening a Tab or Window
     * with given location chosen by a user from the loaction tree.
     */
    public class PopupActionListener implements ActionListener {
        
        protected boolean asWindow;
        protected String wpId;
        
        public PopupActionListener(String wpId, boolean asWindow) {
            this.wpId = wpId;
            this.asWindow = asWindow;
        }
        
        public void actionPerformed(ActionEvent ev) {
            
            SchedulerImpl.instance().lockSimulation();
            
            // Validate the WayPoint. It may have disappeared.
            WayPoint wp = (WayPoint)ObjectMap.instance().
                    getObject(wpId);
            
            if (wp == null) {
                SchedulerImpl.instance().unlockSimulation();
                return;
            }
            
            if (MainFrame.instance().findAndFocus(wp)) {
                SchedulerImpl.instance().unlockSimulation();
                return;
            }
            
            Subwindow subwindow;
            if (wp instanceof GridArea) {
                subwindow = new HiLODPanel((GridArea)wp);
            } else if (wp instanceof GraphArea) {
                subwindow = new GraphAreaPanel((GraphArea)wp);
            } else
                return;
            
            if (asWindow) {
                MainFrame.instance().getDefaultFrameContainer().addSubwindow(
                        subwindow);
            } else {
                MainFrame.instance().getDefaultTabContainer().addSubwindow(
                        subwindow);
            }
            
            SchedulerImpl.instance().unlockSimulation();
            
        }
    }
    
    /**
     * Retrives Info for this Subwindow.
     *
     * @return Info filled with data about this Subwindow
     */
    public Info getInfo() {
        return LOCATION_TREE_INFO;
    }
    
    /**
     * Sets responsible SubwindowContainer.
     *
     * @param container SubwindowContainer newly responsible for this Subwindow
     */
    public void setSubwindowContainer(SubwindowContainer container) {
        this.container = container;
    }
    
    /**
     * Query wether this Subwindow accepts (can view) a given Object.
     *
     * @param object that is being offered.
     * @return Info representing action with the object if it can be accepted or
     *      <code>null</code> if not.
     */
    public Info canAccept(Object object) {
        return null;
    }
    
    /**
     * Does the subwindow already contain a given object?
     *
     * @param object that is querried.
     * @return <code>true</code> iff the object is already viewed by 
     *      this window.
     */
    public boolean contain(Object object) {
        if (object instanceof String) {
            return LOCATION_TREE.equals((String)object);
        }
        return false;
    }
    
    /**
     * Accept (view) the object. This can be called only after successfull
     * call to canAccept.
     *
     * @param object that is being offered.
     */
    public void accept(Object object) {
    }
    
    /**
     * Retrives root panel of this Subwindow. It is not necessarilly
     * this class, for example in case that we use some ToolBars and other
     * controls. So whenever the Subwindow component is to be added to some
     * container, the panel return by this call should be added instead
     * of instance of this class.
     *
     * @return root panel of this GUI component
     */
    public JComponent getPanel() {
        return this;
    }
    
    /**
     * Marks this Subwindow as invisible. That means it does not have to update
     * itsef.
     *
     * @param invisible <code>true</code> iff this Subwindow is not currently
     *      on the screen.
     */
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }
    
    /**
     * Is this Subwindow invisible?
     *
     * @return <code>true</code> iff this Subwindow is not currently
     *      on the screen.
     */
    public boolean isInvisible() {
        return invisible;
    }
    
    /**
     * Forces the Subwindow to revalidate its contents. This is called when
     * major parts of current simulation were changed (e.g. after a load).
     *
     * @return <code>true</code> iff the subwindow should be closed, since its
     *      contents are not valid any more.
     */
    public boolean revalidateSubwindow() {
        initialize(IveMapImpl.instance().getRoot());
        return false;
    }
    
    /**
     * Subwindow was just closed.
     */
    public void closed() {
        stopListening();
    }
    
    /**
     * Subwindow was just opened.
     */
    public void opened() {
        revalidateSubwindow();
        startListening();
    }
}
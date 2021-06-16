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

import cz.ive.IveApplication;
import cz.ive.evaltree.Expr;
import cz.ive.exception.OntologyNotSupportedException;
import cz.ive.genius.*;
import cz.ive.genius.goaltree.RuleTreeNode;
import cz.ive.gui.icon.IconBag;
import cz.ive.gui.subwindow.*;
import cz.ive.gui.tree.*;
import cz.ive.iveobject.*;
import cz.ive.ontology.OntologyToken;
import cz.ive.simulation.*;
import cz.ive.process.*;
import cz.ive.trigger.Trigger;
import cz.ive.valueholders.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Subwindow for tree of geniuses and their goal trees
 *
 * @author Ondra
 */
public class GeniusTree extends JTree implements Subwindow, Gui {
    
    /** Info for genius tree subwindow */
    protected static Info GENIUS_TREE_INFO = new Info("Geniuses",
            "This panel views tree of geniuses and their internals",
            IconBag.GENIUS_LIST);
    
    /** String identification of the genius tree subwindow. */
    public static String GENIUS_TREE = "Genius tree";
    
    /** Tree Model */
    DefaultTreeModel model;
    
    /** Responsible Subwindow container */
    SubwindowContainer container;
    
    /** Toolbar for command buttons */
    protected JToolBar toolbar;
    protected JPanel mainPanel;
    protected Action reload;
    
    /** Are we on the screen? Should we update? */
    protected boolean invisible = true;
    
    /**
     * Creates a new instance of GeniusTree
     */
    public GeniusTree() {
        super();
        setModel(model = new DefaultTreeModel(null));
        setToggleClickCount(1);
        setShowsRootHandles(true);
        setCellRenderer(new GeniusTreeCellRenderer());
        
        createActions();
        createComponents();
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int selRow = getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = getPathForLocation(e.getX(), e.getY());
                    if (selRow != -1) {
                        GeniusTreeNode node = (GeniusTreeNode)
                        ((DefaultMutableTreeNode)selPath.
                                getLastPathComponent()).getUserObject();
                        
                        JPopupMenu menu = node.getPopup();
                        
                        if (menu != null) {
                            menu.show(e.getComponent(),
                                    e.getX(), e.getY());
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Creates all necesary Swing components
     */
    protected void createComponents() {
        Insets zero = new Insets(0, 0, 0, 0);
        
        toolbar = new JToolBar( "Toolbar", JToolBar.HORIZONTAL);
        toolbar.setMargin( zero);
        
        prepareBtn(toolbar.add(reload), zero);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(this), BorderLayout.CENTER);
    }
    
    /**
     * Prepares toolbar button state
     */
    protected void prepareBtn(JButton btn, Insets ins) {
        btn.setFocusable(false);
        btn.setMargin(ins);
    }
    
    /**
     * Creates all necesary Swing actions to be associted with toolbar buttons
     * and whatever else controls.
     */
    protected void createActions() {
        reload = new AbstractAction("Reload",
                IconBag.RELOAD.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                reload();
            }
        };
        reload.putValue(Action.SHORT_DESCRIPTION, "Reloads geniuses tree with "+
                "actual information");
    }
    
    /**
     * Recreates the JTree from all geniuses. This locks the Simulation,
     * to prevent geniuses from simultaneous update.
     */
    public void reload() {
        SchedulerImpl.instance().lockSimulation();
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                new GeniusTreeNode("Geniuses",
                GeniusTreeNodeType.GENIUS_LIST, null));
        
        // Sort them ny the id firts
        Set<Genius> geniuses = GeniusList.instance().getGeniuses();
        Genius[] genSorted = new Genius[geniuses.size()];
        int i = 0;
        for (Genius genius : geniuses) {
            genSorted[i] = genius;
            i++;
        }
        java.util.Arrays.sort(genSorted, new Comparator<Genius>() {
            public int compare(Genius o1, Genius o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        
        // Generate the tree
        for (Genius genius : genSorted) {
            DefaultMutableTreeNode node;
            if (genius instanceof BasicGenius) {
                node = createNode((BasicGenius)genius);
            } else {
                node = new DefaultMutableTreeNode(new GeniusTreeNode(
                        "Unknown genius", GeniusTreeNodeType.GENIUS,
                        genius));
            }
            root.insert(node, root.getChildCount());
        }
        SchedulerImpl.instance().unlockSimulation();
        
        // Update the tree
        DefaultMutableTreeNode oldRoot =
                (DefaultMutableTreeNode)model.getRoot();
        if (oldRoot != null) {
            transformTree(oldRoot, root);
            root = oldRoot;
        } else {
            model.setRoot(root);
            
            Enumeration bfe = root.depthFirstEnumeration();
            
            for (;bfe.hasMoreElements();) {
                DefaultMutableTreeNode bfeNode =
                        (DefaultMutableTreeNode)bfe.nextElement();
                TreePath tp = new TreePath(bfeNode.getPath());
                if (((GeniusTreeNode)bfeNode.getUserObject()).
                        getType().isDefaultExpanded() !=
                        isExpanded(tp)) {
                    if (((GeniusTreeNode)bfeNode.getUserObject()).
                            getType().isDefaultExpanded()) {
                        expandPath(tp);
                    } else {
                        collapsePath(tp);
                    }
                }
            }
        }
    }
    
    /**
     * Creates JTree node representing GoalTreeNode
     *
     * @param goal GoalTreeNode to be transformed to the JTree subtree.
     */
    protected DefaultMutableTreeNode createNode(GoalTreeNode goal) {
        DefaultMutableTreeNode goalNode = createNode(goal.getGoal());
        DefaultMutableTreeNode processNode = createNode(goal.getProcess(),
                false);
        
        if (processNode != null) {
            goalNode.insert(processNode, 0);
            
            for (GoalTreeNode gtNode : goal.getSons()) {
                DefaultMutableTreeNode child = createNode(gtNode);
                
                processNode.insert(child, processNode.getChildCount());
            }
        }
        return goalNode;
    }
    
    
    /**
     * Creates JTree node representing given genius.
     *
     * @param genius Genius to be transformed into a JTree node.
     */
    protected DefaultMutableTreeNode createNode(BasicGenius genius) {
        String cap = genius.getId();
        if (cap == null) {
            cap = "<No ID>";
        }
        cap += " ("+genius.getClass().toString()+")";
        
        GeniusTreeNode gtNode = new GeniusTreeNode(cap,
                GeniusTreeNodeType.GENIUS, genius);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(gtNode);
        
        java.util.List<RuleTreeNode> goals = genius.getTopLevelRules();
        
        for (RuleTreeNode goal : goals) {
            DefaultMutableTreeNode child;
            
            child = createNode(goal);
            node.insert(child, node.getChildCount());
        }
        
        return node;
    }
    
    /**
     * Creates JTree node representing GoalTreeNode
     *
     * @param goal GoalTreeNode to be transformed to the JTree subtree.
     */
    protected DefaultMutableTreeNode createNode(RuleTreeNode goal) {
        DefaultMutableTreeNode goalNode = createNode(goal.getGoal());
        DefaultMutableTreeNode processNode =
                createNode(goal.getProcess(), goal.isAtomic());
        DefaultMutableTreeNode gTriggerNode =
                createNode(goal.getGTrigger(), "G-trigger");
        DefaultMutableTreeNode gContextNode =
                createNode(goal.getGContext(), "G-context");
        
        goalNode.insert(gTriggerNode, goalNode.getChildCount());
        goalNode.insert(gContextNode, goalNode.getChildCount());
        
        if (processNode != null) {
            DefaultMutableTreeNode pContextNode =
                    createNode(goal.getPContext(), "P-context");
            processNode.insert(pContextNode, processNode.getChildCount());
            
            goalNode.insert(processNode, goalNode.getChildCount());
            
            for (RuleTreeNode gtNode : goal.getSons()) {
                DefaultMutableTreeNode child = createNode(gtNode);
                
                processNode.insert(child, processNode.getChildCount());
            }
        }
        return goalNode;
    }
    
    /**
     * Creates JTree node representing a given trigger value.
     *
     * @param value value of the trigger.
     * @param defined is the value defined?
     * @param label base label to be viewed on the node.
     */
    protected DefaultMutableTreeNode createNode(short value, boolean defined,
            String label) {
        String cap = label + ": " + value + (defined ? "" : " - undefined");
        GeniusTreeNode gtNode = new GeniusTreeNode(cap,
                defined ? (
                value == FuzzyValueHolder.True ?
                    GeniusTreeNodeType.TRIGGER_TRUE :
                    GeniusTreeNodeType.TRIGGER_FALSE) :
                    GeniusTreeNodeType.TRIGGER_UNDEFINED, cap);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(gtNode);
        
        return node;
    }
    
    /**
     * Creates JTree node representing a given trigger.
     *
     * @param trigger itself.
     * @param label base label to be viewed on the node.
     */
    protected DefaultMutableTreeNode createNode(Trigger trigger, String label) {
        try {
            Expr root = (Expr)trigger.getData("jBRP.trigger");
            
            DefaultMutableTreeNode node = createNode(root, label);
            
            if (node != null)
                return createNode(root, label);
        } catch (OntologyNotSupportedException ex) {
            // It is ok.
        }
        try {
            return createNode(
                    ((Short)trigger.value().getData("java.Short")).shortValue(),
                    true, label);
        } catch (OntologyNotSupportedException exx) {
            IveApplication.printStackTrace(exx);
            return null;
        }
    }
    
    /**
     * Creates JTree node representing a given expr expresion.
     *
     * @param root root of the subtree to be viewed.
     * @param label base label to be viewed on the node.
     */
    protected DefaultMutableTreeNode createNode(Expr root, String label) {
        
        if (root == null) {
            String cap = label + ": " + "<< NULL >>";
            GeniusTreeNode gtNode = new GeniusTreeNode(cap,
                    GeniusTreeNodeType.TRIGGER_UNKNOWN, cap);
            return new DefaultMutableTreeNode(gtNode);
        }
        
        DefaultMutableTreeNode node;
        if (root.getType() != ValueType.FUZZY) {
            
            String cap = label + "(" +root.getInfo() +")";
            GeniusTreeNode gtNode = new GeniusTreeNode(cap,
                    root.getValue().isDefined() ?
                        GeniusTreeNodeType.TRIGGER_UNKNOWN :
                        GeniusTreeNodeType.TRIGGER_UNDEFINED, cap);
            node = new DefaultMutableTreeNode(gtNode);
        } else {
            node = createNode( ((FuzzyValueHolder)root.getValue()).getValue(),
                    root.getValue().isDefined(),
                    label + "(" + root.getInfo()+")");
        }
        
        int c=0;
        for (int i=0; i<root.getNumberOfChildren(); i++) {
            DefaultMutableTreeNode child = createNode( root.getChild(i),
                    String.valueOf(i));
            
            if (child != null)
                node.insert(child, c++);
        }
        return node;
    }
    
    /**
     * Creates JTree node representing given goal.
     *
     * @param goal goal to be transformed to the JTree subtree.
     */
    protected DefaultMutableTreeNode createNode(Goal goal) {
        String cap = goal.getGoalID();
        if (cap == null) {
            cap = "<No ID>";
        }
        
        if (goal instanceof TopLevelGoal) {
            boolean first = true;
            for (Slot slot : goal.getSubstitution().getActorSlots().values()) {
                IveObject obj = slot.getSource().getObject();
                
                cap += first ? " (" : ", ";
                cap += obj == null ? "null" : obj.getId();
                first = false;
            }
            if (!first) {
                cap += ")";
            }
        }
        
        GeniusTreeNode gtNode = new GeniusTreeNode(cap,
                GeniusTreeNodeType.GOAL, goal);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(gtNode);
        
        DefaultMutableTreeNode chNode;
        
        chNode = createNode(goal.getSubstitution());
        if (chNode != null)
            node.insert(chNode, 0);
        
        chNode = createNode(goal.getParameters());
        if (chNode != null)
            node.insert(chNode, 1);
        
        return node;
    }
    
    /**
     * Creates JTree node representing given sources.
     *
     * @param sources substitution with given sources to be represented as node.
     */
    protected DefaultMutableTreeNode createNode(Substitution sources) {
        Set<String> keys = sources.getSlotsKeys();
        
        if (keys == null || keys.isEmpty())
            return null;
        
        GeniusTreeNode paramsNode = new GeniusTreeNode("Sources",
                GeniusTreeNodeType.SOURCES, sources);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(paramsNode);
        
        int c = 0;
        
        for (String key : keys) {
            String cap = "\""+key+"\" = ";
            Source src = sources.getSource(key);
            IveObject obj = src == null ? null : src.getObject();
            cap = cap + (obj == null ? "null" : obj.getId());
            
            // Debug print
            if (IveApplication.debug) {
                cap += " [id="+((SourceImpl)src).idx+"]";
            }
            
            GeniusTreeNode paramNode = new GeniusTreeNode(cap,
                    GeniusTreeNodeType.SOURCE, cap);
            DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(paramNode);
            
            node.insert(pNode, c++);
        }
        
        return node;
    }
    
    /**
     * Creates JTree node representing given parameters.
     *
     * @param param Map of parameters
     */
    protected DefaultMutableTreeNode createNode(Map<String, Object> param) {
        if (param == null || param.isEmpty())
            return null;
        
        GeniusTreeNode paramsNode = new GeniusTreeNode("Parameters",
                GeniusTreeNodeType.PARAMETERS, param);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(paramsNode);
        
        int c = 0;
        
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String cap = "\""+entry.getKey()+"\" = ";
            Object obj = entry.getValue();
            
            if (obj instanceof OntologyToken) {
                OntologyToken token = (OntologyToken)obj;
                try {
                    cap = cap + (token.getData(
                            token.getOntologies()[0]).toString());
                } catch (OntologyNotSupportedException ex) {
                    // This should not happen
                    IveApplication.printStackTrace(ex);
                }
            } else if (obj instanceof IveId) {
                cap = cap + ((IveId)obj).getId();
            } else {
                cap = cap + obj.toString();
            }
            GeniusTreeNode paramNode = new GeniusTreeNode(cap,
                    GeniusTreeNodeType.PARAMETER, cap);
            DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(paramNode);
            
            node.insert(pNode, c++);
        }
        
        return node;
    }
    
    /**
     * Creates JTree node representing given process.
     *
     * @param process process to be transformed to the JTree subtree.
     * @param atomic is the process atomic.
     */
    protected DefaultMutableTreeNode createNode(IveProcess process,
            boolean atomic) {
        if (process == null)
            return null;
        
        String cap = process.getProcessId();
        if (cap == null) {
            cap = "<No ID>";
        }
        
        ProcessTemplate template =
                ProcessDBImpl.instance().getByProcessId(process.getProcessId());
        Map<String, Slot> slots = process.getSubstitution().getActorSlots();
        if (template instanceof CommonDelegatedProcessTemplate &&
                slots.size()==1) {
            IveObject obj =
                    slots.values().iterator().next().getSource().getObject();
            Genius gen =
                    ((CommonDelegatedProcessTemplate)template).findGenius(obj);
            if (gen != null) {
                cap += " (" + gen.getId() + ")";
            }
        }
        
        GeniusTreeNode gtNode = new GeniusTreeNode(cap,
                atomic ? GeniusTreeNodeType.PROCESS_ATOMIC :
                    GeniusTreeNodeType.PROCESS,  process);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(gtNode);
        
        DefaultMutableTreeNode chNode;
        
        chNode = createNode(process.getSubstitution());
        if (chNode != null)
            node.insert(chNode, 0);
        
        chNode = createNode(process.getParameters());
        if (chNode != null)
            node.insert(chNode, 1);
        
        return node;
    }
    
    
    /**
     * Transforms one tree to the another with as few changes as possible.
     * The root is supposed to be the same.
     *
     * @param oldNode root of the old tree to be transformed into the new one
     * @param newNode root of the new tree to be used as a template
     */
    protected void transformTree(DefaultMutableTreeNode oldNode,
            DefaultMutableTreeNode newNode) {
        Set<DefaultMutableTreeNode> toRemove =
                new HashSet<DefaultMutableTreeNode>();
        List<DefaultMutableTreeNode> oldSons =
                new LinkedList<DefaultMutableTreeNode>();
        List<DefaultMutableTreeNode> newSons =
                new LinkedList<DefaultMutableTreeNode>();
        
        GeniusTreeNode oldGTNode = (GeniusTreeNode)oldNode.getUserObject();
        GeniusTreeNode newGTNode = (GeniusTreeNode)newNode.getUserObject();
        boolean wasMod = oldGTNode.isModified();
        boolean isMod = false;
        
        
        if (oldGTNode.getType() != newGTNode.getType()) {
            oldGTNode.setType(newGTNode.getType());
            isMod = true;
            model.nodeChanged(oldNode);
        }
        
        if (wasMod != isMod) {
            oldGTNode.setModified(isMod);
            model.nodeChanged(oldNode);
        }
        
        for (Enumeration e = oldNode.children(); e.hasMoreElements();) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode)e.nextElement();
            toRemove.add(n);
            oldSons.add(n);
        }
        for (Enumeration e = newNode.children(); e.hasMoreElements();) {
            newSons.add((DefaultMutableTreeNode)e.nextElement());
        }
        
        TreePath parentTp = new TreePath(oldNode.getPath());
        boolean parentExpanded = isExpanded(parentTp);
        
        for (DefaultMutableTreeNode node : newSons) {
            boolean found = false;
            
            for (DefaultMutableTreeNode node2 : oldSons) {
                
                // Some old node is matching, so transform its subtree
                if (node2.getUserObject().equals(node.getUserObject())) {
                    toRemove.remove(node2);
                    
                    TreePath tp = new TreePath(node2.getPath());
                    boolean expanded = isExpanded(tp);
                    
                    transformTree(node2, node);
                    
                    if (expanded != isExpanded(tp)) {
                        if (parentExpanded) {
                            if (expanded) {
                                expandPath(tp);
                            } else {
                                collapsePath(tp);
                            }
                        } else {
                            GeniusTreeNode gtNode =
                                    (GeniusTreeNode)node2.getUserObject();
                            if (gtNode.getType().isDefaultExpanded()) {
                                expandPath(tp);
                            } else {
                                collapsePath(tp);
                            }
                        }
                    }
                    
                    found = true;
                    break;
                }
            }
            
            // No matching found, so add new subtree
            if (!found) {
                model.insertNodeInto(node, oldNode, oldNode.getChildCount());
                
                // Expand new nodes as is default for their types
                Enumeration<DefaultMutableTreeNode> desc =
                        node.depthFirstEnumeration();
                TreePath tp;
                
                for (; desc.hasMoreElements();) {
                    DefaultMutableTreeNode n = desc.nextElement();
                    
                    tp = new TreePath(n.getPath());
                    boolean defExp = ((GeniusTreeNode)n.getUserObject()).
                            getType().isDefaultExpanded();
                     
                    if (defExp != isExpanded(tp)) {
                        if (defExp) {
                            expandPath(tp);
                        } else {
                            collapsePath(tp);
                        }
                    }
                }
                tp = new TreePath(node.getPath());
                boolean defExp = ((GeniusTreeNode)node.getUserObject()).
                        getType().isDefaultExpanded();
                 
                if (defExp != isExpanded(tp)) {
                    if (defExp) {
                        expandPath(tp);
                    } else {
                        collapsePath(tp);
                    }
                }
            }
        }
        
        for (DefaultMutableTreeNode node : toRemove) {
            // Remove all old nodes not found in the new tree
            model.removeNodeFromParent(node);
        }
    }
    
    /**
     * Retrives Info for this Subwindow.
     *
     * @return Info filled with data about this Subwindow
     */
    public Info getInfo() {
        return GENIUS_TREE_INFO;
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
            return GENIUS_TREE.equals((String)object);
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
        return mainPanel;
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
        reload();
        return false;
    }
    
    /**
     * Subwindow was just closed.
     */
    public void closed() {
    }
    
    /**
     * Subwindow was just opened.
     */
    public void opened() {
        revalidateSubwindow();
    }
    
    /**
     * Paint the current state of the world. We use it only to draw reload
     * geniuses, once the simulation stops.
     */
    public void paint() {
        if (SchedulerImpl.instance().getSimulationState() ==
                SimulationState.STOPPED) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reload();
                }
            });
        }
    }
}
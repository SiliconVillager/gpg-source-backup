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
 
package cz.ive.gui.tree;

import cz.ive.IveApplication;
import cz.ive.genius.BasicGenius;
import cz.ive.genius.Genius;
import cz.ive.genius.GeniusList;
import cz.ive.gui.MainFrame;
import cz.ive.simulation.Scheduler;
import cz.ive.simulation.SchedulerImpl;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 * Nodes of the GeniusTree.
 *
 * @author Ondra
 */
public class GeniusTreeNode {
    
    /** NodeType of this node */
    protected GeniusTreeNodeType type;
    
    /** Caption for this JTree node */
    protected String caption;
    
    /**
     * Object represented by this node. It is used for counting the hashCode
     */
    protected Object object;
    
    /**
     * Was this tree node modified before last genius tree reload?
     */
    protected boolean modified = true;
    
    /**
     * Creates a new instance of GeniusTreeNode
     *
     * @param caption Text to be viewed in the JTree
     * @param type GeniusTreeNode type of this node
     * @param object object represented by this node. It will be used for
     *          counting the hashCode
     */
    public GeniusTreeNode(String caption, GeniusTreeNodeType type,
            Object object) {
        this.caption = caption;
        this.type = type;
        this.object = object;
    }
    
    /**
     * Retrives icon to be viewed in the JTree.
     *
     * @return swing Icon
     */
    public Icon getIcon() {
        return type.getIcon();
    }
    
    /**
     * Retrives type of this node.
     *
     * @return GeniusTreeNodeType of this Node
     */
    public GeniusTreeNodeType getType() {
        return type;
    }
    
    /**
     * Set new node type.
     *
     * @param type new type of this node.
     */
    public void setType(GeniusTreeNodeType type) {
        this.type = type;
    }
    
    public String toString() {
        return caption;
    }
    
    public boolean equals(Object object2) {
        if (object != null)
            return object.equals(((GeniusTreeNode)object2).object);
        return ((GeniusTreeNode)object2).object == null;
    }
    
    public int hashCode() {
        int code = 0;
        if (object != null)
            code += object.hashCode();
        
        return code;
    }
    
    /**
     * Was this node modified since last reload?
     *
     * @return <code>true</code> iff this node was modified since last genius
     *      tree reload
     */
    public boolean isModified() {
        return modified;
    }
    
    /**
     * Marks this node as modified (or not-modified) since the last reload.
     *
     * @param modified <code>true</code> to mark as modified.
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }
    
    /**
     * Retrieves popupmenu associated with this rule.
     *
     * @return PopupMenu or null, if no action can be taken on this node.
     */
    public JPopupMenu getPopup() {
        JPopupMenu menu = null;
        
        // Dump the DAG only if we use it.
        if (IveApplication.instance().useDAG) {
            if (type == GeniusTreeNodeType.GENIUS) {
                if (object instanceof BasicGenius) {
                    menu = new JPopupMenu("Genius");
                    
                    JMenuItem item = new JMenuItem(((Genius)object).getId());
                    item.setEnabled(false);
                    menu.add(item);
                    menu.addSeparator();
                    
                    item = new JMenuItem("Dump TriggerKeeper");
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            Scheduler sch = SchedulerImpl.instance();
                            
                            sch.lockSimulation();
                            
                            File file = null;
                            if (GeniusList.instance().getGeniuses().contains(
                                    (Genius)object)) {
                                file = new File("dump_"+
                                        ((Genius)object).getId()+".dt");
                                ((BasicGenius)object).dumpTriggerKeeper(file);
                            }
                            
                            sch.unlockSimulation();
                            
                            if (file != null) {
                                JOptionPane.showMessageDialog(
                                        MainFrame.instance(),
                                        "Dumped in the file \"" +
                                        file.getAbsolutePath() + "\".", "Info",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                            
                        }
                    });
                    
                    menu.add(item);
                }
            }
        }
        return menu;
    }
}

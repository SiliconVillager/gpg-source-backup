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

import cz.ive.gui.*;
import cz.ive.location.*;
import cz.ive.logs.Log;
import cz.ive.messaging.*;
import javax.swing.Icon;
import javax.swing.tree.*;

/**
 * LocationTree nodes copying location hierarchy.
 * This LocationNode automaticaly reacts to locations shrinking and expansions.
 *
 * @author ondra
 */
public class LocationTreeNode {
    
    /** Associated waypoint */
    protected WayPoint wayPoint;
    
    /** NodeType of this node */
    protected LocationTreeNodeType type;
    
    /** Caption for this JTree node */
    protected String caption;
    
    /** Associated MutableTreeNode */
    DefaultMutableTreeNode node;
    
    /** Responsible location tree */
    LocationTree tree;
    
    /** Are we listening now? */
    boolean listening = false;
    
    /** Shrink listener */
    Listener shrinkListener = new Listener() {
        public void changed(Hook initiator) {
            Log.addMessage("Location shrink signalled", Log.FINER, "", "", "");
            tree.shrink(node);
        }
        public void canceled(Hook initiator) {
            tree.revalidateSubwindow();
        }
    };
    
    /** Expand listener */
    Listener expandListener = new Listener() {
        public void changed(Hook initiator) {
            Log.addMessage("Location expand signalled", Log.FINER, "", "", "");
            tree.expand(node);
        }
        public void canceled(Hook initiator) {
            tree.revalidateSubwindow();
        }
    };
    
    /**
     * Creates a new instance of LocationTreeNode
     *
     * @param tree responsible LocationTree
     * @param wp Waypoint to be associated with this Node
     */
    public LocationTreeNode(LocationTree tree, WayPoint wp) {
        this.tree = tree;
        wayPoint = wp;
        type = LocationTreeNodeType.getNodeType( wp);
        caption = wp.getFlatId();
    }
    
    /**
     * Setter for associated MutableTreeNode
     */
    public void setTreeNode(DefaultMutableTreeNode node) {
        this.node = node;
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
     * @return LocationTreeNodeType of this Node
     */
    public LocationTreeNodeType getType() {
        return type;
    }
    
    /**
     * Retrives WayPoint represented by this node.
     *
     * @return WayPoint represented by this node
     */
    public WayPoint getWayPoint() {
        return wayPoint;
    }
    
    public String toString() {
        return caption;
    }
    
    /** Starts listening to Area expansion or shrink */
    public void startListening() {
        if (wayPoint instanceof Area && !listening) {
            ((Area)wayPoint).registerGuiExpandListener(expandListener);
            ((Area)wayPoint).registerGuiShrinkListener(shrinkListener);
            listening = true;
        }
    }
    
    /** Stops listening to Area expansion or shrink */
    public void stopListening() {
        if (wayPoint instanceof Area && listening) {
            ((Area)wayPoint).unregisterGuiExpandListener(expandListener);
            ((Area)wayPoint).unregisterGuiShrinkListener(shrinkListener);
            listening = false;
        }
    }
    
}

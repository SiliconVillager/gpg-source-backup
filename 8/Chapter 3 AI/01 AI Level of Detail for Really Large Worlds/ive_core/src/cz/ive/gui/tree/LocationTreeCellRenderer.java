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

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Cell renderer specialized for LocationTree.
 *
 * @author ondra
 */
public class LocationTreeCellRenderer extends DefaultTreeCellRenderer {
    
    /** Creates a new instance of LocationTreeCellRenderer */
    public LocationTreeCellRenderer() {
    }
    
    /**
     * Retrieves CellRenderer for viewing a LocationTree nodes.
     *
     * @param tree parent JTree
     * @param value TreeNode to be rendered.
     * @param sel is the node selected?
     * @param expanded is the node expanded?
     * @param leaf is the node a leaf?
     * @param row index of a row in the partialy expanded JTree
     * @param hasFocus is the node focused?
     * @return Component that can be used as a tree node renderer
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value,
                sel, expanded, leaf, row, hasFocus);
        Object node = ((DefaultMutableTreeNode)value).getUserObject();
        if (node instanceof LocationTreeNode) {
            Icon icon = ((LocationTreeNode)node).getIcon();
        
            if (icon != null)
                label.setIcon(icon);
        }
        
        return label;
    }
}

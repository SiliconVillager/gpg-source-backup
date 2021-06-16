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

import cz.ive.gui.icon.IconBag;
import cz.ive.iveobject.*;
import cz.ive.location.*;
import java.net.URL;
import javax.swing.*;

/**
 * Types of the GeniusTree nodes.
 *
 * @author Ondra
 */
public enum GeniusTreeNodeType {
    /** 
     * Represents a root of GeniusTree, the list of geniuses
     */
    GENIUS_LIST(IconBag.GENIUS_LIST, true),
    /** 
     * Represents single genius
     */
    GENIUS(IconBag.GENIUS, false),
    /** 
     * Represents a single goal
     */
    GOAL(IconBag.GOAL, true),
    /** 
     * Represent a single process
     */
    PROCESS(IconBag.PROCESS, false),
    /** 
     * Represent a single process
     */
    PROCESS_ATOMIC(IconBag.PROCESS_ATOMIC, false),
    /** 
     * Represent a source substitution
     */
    SOURCES(IconBag.SOURCES, false),
    /** 
     * Represent a single source
     */
    SOURCE(IconBag.SOURCE, false),
    /** 
     * Represent a parameters list
     */
    PARAMETERS(IconBag.PARAMETERS, false),
    /** 
     * Represent a parameters list
     */
    PARAMETER(IconBag.PARAMETER, false),
    /** 
     * Just a String item
     */
    STRING(null, false),
    /** 
     * Represents a trigger in the unknown state
     */
    TRIGGER_UNKNOWN(IconBag.UNKNOWN, true),
    /** 
     * Represents a trigger in the unknown state
     */
    TRIGGER_UNDEFINED(IconBag.UNDEFINED, true),
    /** 
     * Represents a firing trigger
     */
    TRIGGER_TRUE(IconBag.YES, false),
    /** 
     * Represents a firing trigger
     */
    TRIGGER_FALSE(IconBag.NO, false);
    
    /** Icon to be viewed in the Tree */
    private Icon icon;
    
    /** Are nodes of this type expanded by default? */
    private boolean defaultExpanded;
    
    /** 
     * Creates a new instance of GeniusTreeNodeType 
     *
     * @param bagMember element of the IconBag enum representing a Icon
     *      to be associted with this GeniusTreeNodeType
     */
    private GeniusTreeNodeType(IconBag bagMember, boolean de) {
        icon = bagMember == null ? null : bagMember.getIcon();
        defaultExpanded = de;
    }
    
    /**
     * Retrives icon to be viewed in the JTree
     *
     * @return swing Icon
     */
    public Icon getIcon() {
        return icon;
    }
    
    /**
     * Is the node expanded by default?
     * 
     * @return <code>true</code> iff nodes of this type should be 
     *      expanded by default
     */
    public boolean isDefaultExpanded() {
        return defaultExpanded;
    }
}

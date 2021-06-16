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
 
package cz.ive.gui.icon;

import cz.ive.IveApplication;
import java.net.URL;
import javax.swing.*;

/**
 * Storage for various icons used in gui.
 *
 * @author ondra
 */
public enum IconBag {
    /* Icons for location tree */
    GRID_AREA("resources/images/grid_area.gif"),
    GRAPH_AREA("resources/images/graph_area.gif"),
    WAYPOINT("resources/images/waypoint.gif"),
    OBJECT("resources/images/object.gif"),
    /* Icons for log tree */
    LOG_LEAF_MESSAGE("resources/images/leafM.gif"),
    LOG_LEAF("resources/images/leaf.gif"),
    LOG_BRANCH_MESSAGE("resources/images/branchM.gif"),
    LOG_BRANCH("resources/images/branch.gif"),
    /* Icons for Genius tree */
    GENIUS("resources/images/genius.gif"),
    GENIUS_LIST("resources/images/genius_list.gif"),
    CALENDAR("resources/images/calendar.gif"),
    GOAL("resources/images/goal.gif"),
    PROCESS("resources/images/process.gif"),
    PROCESS_ATOMIC("resources/images/process-atomic.gif"),
    SOURCES("resources/images/sources.gif"),
    SOURCE("resources/images/source.gif"),
    PARAMETERS("resources/images/params.gif"),
    PARAMETER("resources/images/param.gif"),
    /* Icons for panels and windows */
    LOG_PANEL("resources/images/log_panel.gif"),
    HI_LOD_PANEL("resources/images/hi_lod_panel.gif"),
    PLEASE_WAIT("resources/images/please_wait.gif"),
    /* Icons for actions */
    RELOAD("resources/images/reload.gif"),
    OPEN("resources/images/load.gif"),
    OPEN_WORLD("resources/images/load_xml.gif"),
    SAVE("resources/images/save.gif"),
    FAST("resources/images/FastPlay16.gif"),
    START("resources/images/Play16.gif"),
    STEP("resources/images/StepForward16.gif"),
    STOP("resources/images/Stop16.gif"),
    CLEANUP("resources/images/cleanup.gif"),
    ADD("resources/images/add.gif"),
    EDIT("resources/images/edit.gif"),
    DELETE("resources/images/delete.gif"),
    LIST("resources/images/list.gif"),
    CONFIG("resources/images/config.gif"),
    UNDEFINED("resources/images/undefined.gif"),
    UNKNOWN("resources/images/unknown.gif"),
    YES("resources/images/yes.gif"),
    HOLDBACK("resources/images/add-holdback.gif"),
    ZOOM_IN("resources/images/zoomin.gif"),
    ZOOM_OUT("resources/images/zoomout.gif"),
    LEFT("resources/images/left.gif"),
    RIGHT("resources/images/right.gif"),
    UP("resources/images/up.gif"),
    DOWN("resources/images/down.gif"),
    NO("resources/images/no.gif"),
    ACTIVATE("resources/images/activate.gif"),
    TIMER("resources/images/timer.gif"),
    ATOMIC("resources/images/atomic.gif");
    
    
    /** Particular Icon represented by this enum */
    private Icon icon;
    
    /** 
     * Creates a new instance of IconBag
     *
     * @param iconURL path to the icon file
     */
    private IconBag(URL iconURL) {
        this(iconURL==null ? null : new ImageIcon(iconURL));
    }
    
    /** 
     * Creates a new instance of IconBag
     *
     * @param icon the icon itself
     */
    private IconBag(Icon icon) {
        this.icon = icon;
    }
    
    /** 
     * Creates a new instance of IconBag
     *
     * @param pathToIcon path to the icon file
     */
    private IconBag(String pathToIcon) {
        this(IveApplication.class.getResource(
                pathToIcon));
    }
    
    /**
     * Retrives icon to be viewed in the JTree
     *
     * @return swing Icon
     */
    public Icon getIcon() {
        return icon;
    }
}

/* 
 *
 * IVE Editor 
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
 
package IVE_Editor.PlugIns.Moduls.MOL.GUI.Icons;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
/**
 *
 * @author Jirka
 */
public enum IconBag {
    //Icons for MOL...
    MOL_CLOSE("PlugIns/Moduls/MOL/GUI/Icons/small_undelete.gif"),
    MOL_DEL_REP("PlugIns/Moduls/MOL/GUI/Icons/flag.gif"),
    MOL_DEL_OBJ("PlugIns/Moduls/MOL/GUI/Icons/edit-trash.gif"),
    MOL_CON_REP("PlugIns/Moduls/MOL/GUI/Icons/HD-open.gif"),
    MOL_CREATE_REP("PlugIns/Moduls/MOL/GUI/Icons/treasure_chest.gif"),
    MOL_SORT("PlugIns/Moduls/MOL/GUI/Icons/package-edutainment.gif"),
    MOL_TO_PROJECT("PlugIns/Moduls/MOL/GUI/Icons/undo.gif"),
    MOL_TO_REP("PlugIns/Moduls/MOL/GUI/Icons/redo.gif"),
    MOL_GRAPHIC_TMPL("PlugIns/Moduls/MOL/GUI/Icons/Jpeg.gif"),
    MOL_KINDS_MANAGER("PlugIns/Moduls/MOL/GUI/Icons/Word.gif"),
    MOL_CREATE_ENT("PlugIns/Moduls/MOL/GUI/Icons/simpsons_homer.gif"),
    MOL_CREATE_OBJECT("PlugIns/Moduls/MOL/GUI/Icons/sextant.gif"),
    MOL_CREATE_SENSOR("PlugIns/Moduls/MOL/GUI/Icons/Mike_Wazowski.gif"),
    //Icons for GraphicManager
    GM_PICTURE_BORDER_NORMAL("PlugIns/Moduls/MOL/GUI/Icons/edge.png"),
    GM_PICTURE_BORDER_ALT("PlugIns/Moduls/MOL/GUI/Icons/edge_second.png"),
    GM_DOWN_BUTTON("PlugIns/Moduls/MOL/GUI/Icons/down.png"),
    GM_UP_BUTTON("PlugIns/Moduls/MOL/GUI/Icons/up.png"),
    GM_ANIMATED_VS_STATIC("PlugIns/Moduls/MOL/GUI/Icons/animatedVSstatic2.png"),
    //Icons of TLocation tree nodes
    TN_GRID("PlugIns/Moduls/MOL/GUI/Icons/grid_area.gif"),
    TN_GRAPH("PlugIns/Moduls/MOL/GUI/Icons/graph_area.gif"),
    TN_WP("PlugIns/Moduls/MOL/GUI/Icons/waypoint.gif"),
    TN_GRID_CONNECTED("PlugIns/Moduls/MOL/GUI/Icons/grid_area_connected.gif"),
    TN_GRAPH_CONNECTED("PlugIns/Moduls/MOL/GUI/Icons/graph_area_connected.gif"),
    TN_WP_CONNECTED("PlugIns/Moduls/MOL/GUI/Icons/waypoint_connected.gif"),
    //Icons of GridViewPane
    GVP_ZOOM_IN("PlugIns/Moduls/MOL/GUI/Icons/zoom_in.gif"),
    GVP_ZOOM_OUT("PlugIns/Moduls/MOL/GUI/Icons/zoom_out.gif"),
    GVP_MAP_SIZE("PlugIns/Moduls/MOL/GUI/Icons/change_map_size.png"),
    GVP_BRUSH("PlugIns/Moduls/MOL/GUI/Icons/brush.gif"),
    GVP_PICK_BRUSH("PlugIns/Moduls/MOL/GUI/Icons/small_dropper.png"),
    GVP_RECT_BRUSH("PlugIns/Moduls/MOL/GUI/Icons/small_paintbrush_rect.png"),
    GVP_POINT_BRUSH("PlugIns/Moduls/MOL/GUI/Icons/large_paintbrush.gif"),
    GVP_GRID("PlugIns/Moduls/MOL/GUI/Icons/change_grid_visibility.png"),
    GVP_SELECTION("PlugIns/Moduls/MOL/GUI/Icons/selection_cursor.png"),
    GVP_SHOW_JOINTS("PlugIns/Moduls/MOL/GUI/Icons/show_joints.png"),
    GVP_SHOW_PLACEMENTS("PlugIns/Moduls/MOL/GUI/Icons/show_placements.png"),
    GVP_LEFTWARDS("PlugIns/Moduls/MOL/GUI/Icons/leftwards.gif"),
    GVP_RIGHTWARDS("PlugIns/Moduls/MOL/GUI/Icons/rightwards.gif"),
    GVP_EDIT_JOINTS("PlugIns/Moduls/MOL/GUI/Icons/edit_joints.png"),
    GVP_BLUE_CHECK("PlugIns/Moduls/MOL/GUI/Icons/blue_check.png"),
    //Cursor Icons
    CI_MAGGLASS("PlugIns/Moduls/MOL/GUI/Icons/medium_magnifying_glass.gif"),
    CI_BRUSH("PlugIns/Moduls/MOL/GUI/Icons/medium_paintbrush.png"),
    CI_RECT_BRUSH("PlugIns/Moduls/MOL/GUI/Icons/medium_paintbrush_rect.png"),
    CI_DROPPER("PlugIns/Moduls/MOL/GUI/Icons/medium_dropper.png");
    /** Particular Icon represented by this enum */
    private Icon _icon;
    /**
     * Creates a new instance of IconBag
     * 
     * @param iconURL path to the icon file
     */
    private IconBag(URL iconURL) {
        this(iconURL == null ? null : new ImageIcon(iconURL));
    }
    /**
     * Creates a new instance of IconBag
     * 
     * @param icon the icon itself
     */
    private IconBag(Icon icon) {
        this._icon = icon;
    }
    /**
     * Creates a new instance of IconBag
     * 
     * @param pathToIcon path to the icon file
     */
    private IconBag(String pathToIcon) {
        this(IVE_Editor.Main.class.getResource(
                pathToIcon));
    }
    /**
     * Retrives icon to be viewed in the JTree
     *
     * @return swing Icon
     */
    public Icon getIcon() {
        return _icon;
    }
}

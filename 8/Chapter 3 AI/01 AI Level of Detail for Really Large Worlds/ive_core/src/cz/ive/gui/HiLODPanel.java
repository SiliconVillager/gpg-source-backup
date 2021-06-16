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

import cz.ive.iveobject.*;
import cz.ive.location.*;
import cz.ive.gui.subwindow.*;
import cz.ive.gui.icon.*;
import cz.ive.messaging.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Implementation of GUI panel, that can show hi-LOD locations.
 *
 * @author Ondra
 */
public class HiLODPanel extends ZoomScrollPanel {
    
    /** Info for all Animated panel... it probably wont be used */
    protected static Info HI_LOD_PANEL_INFO = new Info("Empty HiLODPanel",
            "This panel views animated contents using a backbuffer.",
            IconBag.HI_LOD_PANEL);
    
    /** Width of a single cell (one WayPoint) in screen coordinates */
    public static int CELL_WIDTH = 32;
    /** Height of a single cell (one WayPoint) in screen coordinates */
    public static int CELL_HEIGHT = 32;
    
    /** Font for the viewing texts */
    protected static Font font = new Font("Arial", Font.BOLD, 30);
    
    /** Map to be viewed */
    protected GridArea map;
    
    /** Is the area expanded? */
    protected boolean expanded = false;
    
    /** Parent of our location */
    protected Area parent;
    
    /** Parent and diplayed location shrinkHook Listener*/
    protected Listener shrinkListener = new Listener() {
        public void changed(Hook initiator) {
            if(revalidateSubwindow() && container != null)
                container.closeSubwindow(HiLODPanel.this);
        }
        public void canceled(Hook initiator) {
            if (container != null)
                container.closeSubwindow(HiLODPanel.this);
        }
    };
    
    /** Displayed location expandHook Listener*/
    protected Listener expandListener = new Listener() {
        public void changed(Hook initiator) {
            if(revalidateSubwindow() && container != null)
                container.closeSubwindow(HiLODPanel.this);
        }
        public void canceled(Hook initiator) {
            if (container != null)
                container.closeSubwindow(HiLODPanel.this);
        }
    };
    
    /** Are we listening? */
    protected boolean listening = false;
    
    /**
     * Creates a new instance of MapPanel
     *
     * @param map for this view
     */
    public HiLODPanel(GridArea map) {
        this();
        initialize(map);
    }
    
    /** Creates a new instance of MapPanel */
    protected HiLODPanel() {
        info = HI_LOD_PANEL_INFO;
        
        setScrollSize(new Point(CELL_WIDTH, CELL_HEIGHT));
        setMaxZoom(5);
        setZoom(2);
    }
    
    /**
     * This method is meant to be overidden by subclasses in order to render
     * the simulation state. This method is not threadsafe. It is called
     * from paint method.
     */
    protected void render() {
        
        int width = backBuffer.getWidth();
        int height = backBuffer.getHeight();
        Point zero = new Point(0, 0);
        
        Graphics2D g2 = backBuffer.createGraphics();
        
        updateViewpoint();
        g2.clearRect(0,0,width, height);
        prepareRender(g2);
        
        if (!isInitialized()) {
            g2.dispose();
            return;
        }
        
        WayPoint[][] wps = map.getGrid();
        
        if (!expanded || wps == null) {
            String str = "\""+map.getId()+"\" area is not expanded.";
            g2.setFont(font);
            Rectangle2D rect = font.getStringBounds(str, g2.getFontRenderContext());
            g2.setColor(Color.GRAY);
            g2.drawString(str,
                    width/2*zoom - (int)rect.getWidth()/2 + 2,
                    height/2*zoom + (int)rect.getHeight()/2 + 2);
            g2.setColor(Color.ORANGE);
            g2.drawString(str,
                    width/2*zoom - (int)rect.getWidth()/2,
                    height/2*zoom + (int)rect.getHeight()/2);
            g2.dispose();
            return;
        }
        
        
        int minx, miny;
        int maxx, maxy;
        
        minx = ((mapView.x-mapSize.x) / CELL_WIDTH) - 1;
        miny = ((mapView.y-mapSize.y) / CELL_HEIGHT) - 1;
        maxx = ((mapView.width+mapView.x-mapSize.x) / CELL_WIDTH) + 1;
        maxy = ((mapView.height+mapView.y-mapSize.y) / CELL_HEIGHT) + 1;
        
        minx = Math.max(minx,0);
        maxx = Math.min(maxx,wps.length-1);
        miny = Math.max(miny,0);
        maxy = Math.min(maxy,wps[0].length-1);

        // Draw the path waypoints (only as gray links)
        for (WayPoint oskliveKacatko : ((Area2DGrid)map).getPathWayPoints()) {
            java.util.List<Joint> joints = oskliveKacatko.getNeighbours();
            if (joints!=null){
                for (Joint joint : joints) {
                    float[] pos1 = oskliveKacatko.getRealPosition();
                    float[] pos2 = joint.target.getRealPosition();
                    
                    Point p1 = translate(pos1);
                    Point p2 = translate(pos2);
                    
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
        
        for (int y=miny; y<=maxy; y++) {
            for (int x=minx; x<=maxx; x++) {
                WayPoint wp = wps[x][y];
                GraphicInfo gr = wp.getGraphicInfo();
                
                if (gr != null) {
                    gr.draw(g2, this, zero);
                } else {
                    float[] pos = wp.getRealPosition();
                    Point p = translate(pos);
                    g2.drawOval(p.x-3,  p.y-3, 6, 6);
                }
            }
        }
        
        for (int y=miny; y<=maxy; y++) {
            for (int x=minx; x<=maxx; x++) {
                WayPoint wp = wps[x][y];
                GraphicInfo gr;
                java.util.Set<IveObject> objs = wp.getSlaves();
                
                if (objs != null) {
                    for (IveObject obj : objs) {
                        gr = obj.getGraphicInfo();
                        
                        if (gr != null)
                            gr.draw(g2, this, zero);
                    }
                }
            }
        }
        g2.dispose();
    }
    
    public IveObject getObjectAtPosition(
            Point point, boolean acceptWP) {
        if (!isInitialized()) {
            return null;
        }
        
        point = new Point(point.x*zoom, point.y*zoom);
        Point zero = new Point(0, 0);
        
        IveObject selected = null;
        
        WayPoint[][] wps = map.getGrid();
        
        if (!expanded || wps == null) {
            return null;
        }
        
        int minx, miny;
        int maxx, maxy;
        
        minx = ((mapView.x-mapSize.x) / CELL_WIDTH) - 1;
        miny = ((mapView.y-mapSize.y) / CELL_HEIGHT) - 1;
        maxx = ((mapView.width+mapView.x-mapSize.x) / CELL_WIDTH) + 1;
        maxy = ((mapView.height+mapView.y-mapSize.y) / CELL_HEIGHT) + 1;
        
        minx = Math.max(minx,0);
        maxx = Math.min(maxx,wps.length-1);
        miny = Math.max(miny,0);
        maxy = Math.min(maxy,wps[0].length-1);
        
        for (int y=miny; y<=maxy; y++) {
            for (int x=minx; x<=maxx; x++) {
                WayPoint wp = wps[x][y];
                GraphicInfo gr;
                java.util.Set<IveObject> objs = wp.getSlaves();
                
                if (objs != null) {
                    for (IveObject obj : objs) {
                        gr = obj.getGraphicInfo();
                        
                        IveObject o;
                        if (gr != null && null != (o =
                                gr.getObjectAtPosition(this, zero, point))) {
                            selected = o;
                        }
                    }
                }
            }
        }
        
        if (selected != null || !acceptWP)
            return selected;
        
        for (int y=miny; y<=maxy; y++) {
            for (int x=minx; x<=maxx; x++) {
                WayPoint wp = wps[x][y];
                GraphicInfo gr = wp.getGraphicInfo();
                
                IveObject o;
                if (gr != null && null != (o =
                        gr.getObjectAtPosition(this, zero, point))) {
                    selected = o;
                }
            }
        }
        return selected;
    }
    
    public void updateViewpoint() {
        super.updateViewpoint();
    }
    
    public Point translate(float[] coords) {
        return new Point((int)(coords[0]*CELL_WIDTH)-mapView.x,
                (int)(coords[1]*CELL_HEIGHT)-mapView.y);
    }
    
    /**
     * Initialization of the view. This highly depends on the given map.
     *
     * @param map Area2DGrid map to be viewed.
     */
    public void initialize(GridArea map) {
        if (listening) {
            if (parent != null) {
                parent.unregisterGuiShrinkListener(shrinkListener);
                parent = null;
            }
            if (this.map != null) {
                this.map.unregisterGuiExpandListener(expandListener);
                this.map.unregisterGuiShrinkListener(shrinkListener);
            }
            listening = false;
        }
        
        this.map = map;
        area = map;
        
        if (map == null) {
            info = HI_LOD_PANEL_INFO;
            if (container != null)
                container.updateSubwindow(this);
            return;
        }
        
        WayPoint[][] wps = map.getGrid();
        
        parent = (Area)map.getParent();
        
        if (parent != null) {
            parent.registerGuiShrinkListener(shrinkListener);
        }
        map.registerGuiExpandListener(expandListener);
        map.registerGuiShrinkListener(shrinkListener);
        listening = true;
        
        if (wps != null) {
            // Lets find out how big the location is.
            float minx = Float.MAX_VALUE;
            float miny = Float.MAX_VALUE;
            float maxx = -Float.MAX_VALUE;
            float maxy = -Float.MAX_VALUE;
            
            for (WayPoint[] wpsa : wps) {
                for (WayPoint wp : wpsa) {
                    float[] pos = wp.getRealPosition();
                    if (pos[0] > maxx)
                        maxx = pos[0];
                    if (pos[0] < minx)
                        minx = pos[0];
                    if (pos[1] > maxy)
                        maxy = pos[1];
                    if (pos[1] < miny)
                        miny = pos[1];
                }
            }
            minx *= CELL_WIDTH;
            maxx *= CELL_WIDTH;
            miny *= CELL_HEIGHT;
            maxy *= CELL_HEIGHT;
            
            mapSize.x = (int)minx - CELL_WIDTH / 2;
            mapSize.y = (int)miny - CELL_HEIGHT / 2;
            mapSize.width = (int)(maxx - minx) + CELL_WIDTH;
            mapSize.height = (int)(maxy - miny) + CELL_HEIGHT;
            position.x = mapSize.width / 2 + mapSize.x;
            position.y = mapSize.height / 2 + mapSize.y;
            
            expanded = true;
        } else {
            mapSize.x = 0;
            mapSize.y = 0;
            mapSize.width = 0;
            mapSize.height = 0;
            position.x = 0;
            position.y = 0;
            setZoom(1);
            
            expanded = false;
        }
        
        info = new Info("Area \""+map.getFlatId()+"\"",
                "View to the area \""+map.getId()+"\".",
                HI_LOD_PANEL_INFO.getIcon());
        if (container != null)
            container.updateSubwindow(this);
        repaint();
    }
    
    /**
     * Are we initialized?
     *
     * @return <code>TRUE</code> iff this view was previously initialized with
     *      existing Area2DGrid location and this location did not cease to
     *      exist.
     */
    public boolean isInitialized() {
        return map != null;
    }
    
    /**
     * Forces the Subwindow to revalidate its contents. This is called when
     * major parts of current simulation were changed (e.g. after a load).
     * This is supposed to be called with the Simulation (SchedulerImpl)
     * lock locked.
     *
     * @return <code>true</code> iff the subwindow should be closed, since its
     *      contents are not valid any more.
     */
    public boolean revalidateSubwindow() {
        if (!isInitialized())
            return true;
        
        IveObject obj = ObjectMap.instance().getObject(map.getId());
        
        if (obj == null || !(obj instanceof GridArea)) {
            return true;
        }
        
        initialize((GridArea)obj);
        return false;
    }
    
    /**
     * Subwindow was closed
     */
    public void closed() {
        if (listening) {
            if (parent != null) {
                parent.unregisterGuiShrinkListener(shrinkListener);
                parent = null;
            }
            if (this.map != null) {
                this.map.unregisterGuiExpandListener(expandListener);
                this.map.unregisterGuiShrinkListener(shrinkListener);
            }
            listening = false;
        }
    }
    
    /**
     * Subwindow was just opened.
     */
    public void opened() {
        if (revalidateSubwindow() && container != null)
            container.closeSubwindow(this);
    }
}

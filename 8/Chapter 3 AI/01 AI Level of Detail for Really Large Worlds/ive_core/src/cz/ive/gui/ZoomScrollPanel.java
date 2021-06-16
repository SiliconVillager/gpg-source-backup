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

import cz.ive.gui.dialog.*;
import cz.ive.gui.icon.IconBag;
import cz.ive.gui.subwindow.FrameContainer;
import cz.ive.gui.subwindow.Subwindow;
import cz.ive.iveobject.IveObject.ObjectState;
import cz.ive.iveobject.attributes.*;
import cz.ive.iveobject.*;
import cz.ive.location.*;
import cz.ive.lod.Holdback;
import cz.ive.logs.Log;
import cz.ive.simulation.SchedulerImpl;
import cz.ive.template.Holdbacks;
import cz.ive.template.TemplateMap;
import cz.ive.xmlload.ObjectTemplate;
import java.awt.event.*;
import java.awt.*;
import java.util.Comparator;
import java.util.Set;
import javax.swing.*;

/**
 * Skeleton implementation of GUI panel that can be zoomed and scrolled.
 *
 * @author Ondra
 */
abstract public class ZoomScrollPanel extends AnimatedPanel
        implements Viewpoint {
    
    /** Toolbar for command buttons */
    protected JToolBar toolbar;
    protected JPanel mainPanel;
    protected Action scrollLeft;
    protected Action scrollRight;
    protected Action scrollUp;
    protected Action scrollDown;
    protected Action zoomIn;
    protected Action zoomOut;
    protected Action addHoldback;
    
    /** Max zoom */
    protected int maxZoom = 1;
    /** Actual zoom. The map is viewed in scale 1:zoom */
    protected int zoom = 1;
    
    /** Position in the map */
    protected Point position = new Point(0,0);
    /** Position in the map */
    protected Rectangle mapSize = new Rectangle(0,0,0,0);
    /** Size of scroll step */
    protected Point scrollSize = new Point(32,32);
    
    /** Associated area */
    protected Area area;
    
    /** Rectangle of the viewpoint */
    protected Rectangle mapView;
    
    /** Creates a new instance of ZoomShiftPanel */
    public ZoomScrollPanel() {
        createActions();
        createComponents();
    }
    
    public JPanel getPanel() {
        return mainPanel;
    }
    
    /**
     * Creates all necesary Swing components
     */
    protected void createComponents() {
        Insets zero = new Insets(0, 0, 0, 0);
        
        toolbar = new JToolBar( "Position controls", JToolBar.HORIZONTAL);
        toolbar.setMargin( zero);
        
        prepareBtn(toolbar.add(zoomIn), zero);
        prepareBtn(toolbar.add(zoomOut), zero);
        toolbar.addSeparator();
        prepareBtn(toolbar.add(scrollLeft), zero);
        prepareBtn(toolbar.add(scrollDown), zero);
        prepareBtn(toolbar.add(scrollUp), zero);
        prepareBtn(toolbar.add(scrollRight), zero);
        toolbar.addSeparator();
        prepareBtn(toolbar.add(addHoldback), zero);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(this, BorderLayout.CENTER);
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
        scrollLeft = new AbstractAction("Scroll left",
                IconBag.LEFT.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                scrollLeft();
            }
        };
        scrollLeft.putValue(Action.SHORT_DESCRIPTION, "Scroll left");
        
        scrollRight = new AbstractAction("Scroll right",
                IconBag.RIGHT.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                scrollRight();
            }
        };
        scrollRight.putValue(Action.SHORT_DESCRIPTION, "Scroll right");
        
        scrollUp = new AbstractAction("Scroll up",
                IconBag.UP.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                scrollUp();
            }
        };
        scrollUp.putValue(Action.SHORT_DESCRIPTION, "Scroll up");
        
        scrollDown = new AbstractAction("Scroll down",
                IconBag.DOWN.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                scrollDown();
            }
        };
        scrollDown.putValue(Action.SHORT_DESCRIPTION, "Scroll down");
        
        zoomIn = new AbstractAction("Zoom in",
                IconBag.ZOOM_IN.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                zoomIn();
            }
        };
        zoomIn.putValue(Action.SHORT_DESCRIPTION, "Zoom in");
        
        zoomOut = new AbstractAction("Zoom out",
                IconBag.ZOOM_OUT.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                zoomOut();
            }
        };
        zoomOut.putValue(Action.SHORT_DESCRIPTION, "Zoom out");
        
        addHoldback = new AbstractAction("Add holdback (Expand)",
                IconBag.HOLDBACK.getIcon()) {
            public void actionPerformed(ActionEvent event) {
                if (area != null) {
                    HoldBackListDialog.openAdd(MainFrame.instance(),
                            area.getId(), area.getLod()+1);
                }
            }
        };
        addHoldback.putValue(Action.SHORT_DESCRIPTION, "Add holdback (Expand)");
        
        addMouseListener(new MouseAdapter() {
            protected Point last;
            
            public void mousePressed(MouseEvent ev) {
                int btn = ev.getButton();
                
                if (btn == ev.BUTTON1) {
                    Point p = ev.getPoint();
                    last = p;
                } else
                    last = null;
                
                maybeShowPopup(ev);
            }
            
            public void mouseReleased(MouseEvent ev) {
                int btn = ev.getButton();
                
                if (btn == ev.BUTTON1) {
                    Point p = ev.getPoint();
                    Dimension d = getSize();
                    
                    p.x = position.x - (p.x - last.x)*zoom;
                    p.y = position.y - (p.y - last.y)*zoom;
                    
                    setPosition(p);
                    repaint();
                }
                
                maybeShowPopup(ev);
            }
            
            public void mouseClicked(MouseEvent ev) {
                // Perform the shortcut action
                if (ev.getButton() == ev.BUTTON2) {
                    performShortcutAction(ev);
                }
            }
            
            private void maybeShowPopup(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    showSelectedObjectMenu(ev);
                }
            }
            
        });
        
        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent ev) {
                int rot = ev.getWheelRotation();
                for (int i=0; i<rot; i++)
                    zoomOut();
                for (int i=0; i>rot; i--)
                    zoomIn();
            }
        });
    }
    
    /**
     * Sets the maximal zoom level allowed
     *
     * @param maxZoom new zoom maximum to be set
     */
    protected void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
        if (zoom > maxZoom)
            zoom = maxZoom;
    }
    
    /**
     * Sets actual zoom level. Map is viewed in scale 1:zoom
     *
     * @param zoom new zoom level
     */
    protected void setZoom(int zoom) {
        this.zoom = zoom;
    }
    
    /**
     * Sets the maximal zoom level allowed
     *
     * @param mapSize new size of the map being viewed
     */
    protected void setMapSize(Rectangle mapSize) {
        this.mapSize = (Rectangle)mapSize.clone();
        
        if (position.x < mapSize.x)
            position.x = mapSize.x;
        else if (position.x > mapSize.x + mapSize.width)
            position.x = mapSize.x + mapSize.width;
        if (position.y < mapSize.y)
            position.y = mapSize.y;
        else if (position.y > mapSize.y + mapSize.height)
            position.y = mapSize.y + mapSize.height;
    }
    
    /**
     * Sets actual position
     *
     * @param position new position in the map
     */
    protected void setPosition(Point position) {
        this.position = (Point)position.clone();
        
        if (position.x < mapSize.x)
            this.position.x = mapSize.x;
        else if (position.x > mapSize.x + mapSize.width)
            this.position.x = mapSize.x + mapSize.width;
        if (position.y < mapSize.y)
            this.position.y = mapSize.y;
        else if (position.y > mapSize.y + mapSize.height)
            this.position.y = mapSize.y + mapSize.height;
    }
    
    /**
     * Sets actual scroll size. That is how much will we shift the view
     * over the map when scroll is performed
     *
     * @param scrollSize new scroll step size
     */
    protected void setScrollSize(Point scrollSize) {
        this.scrollSize = (Point)scrollSize.clone();
    }
    
    /**
     * Zooms the view in.
     */
    protected void zoomIn() {
        if (zoom > 1) {
            zoom--;
            repaint();
        }
    }
    
    /**
     * Zooms the view out.
     */
    protected void zoomOut() {
        if (zoom < maxZoom) {
            zoom++;
            repaint();
        }
    }
    
    /**
     * Scrolls the view right.
     */
    protected void scrollRight() {
        position.x += scrollSize.x;
        if (position.x > mapSize.x + mapSize.width) {
            position.x = mapSize.x + mapSize.width;
        }
        repaint();
    }
    
    /**
     * Scrolls the view left.
     */
    protected void scrollLeft() {
        position.x -= scrollSize.x;
        if (position.x < mapSize.x) {
            position.x = mapSize.x;
        }
        repaint();
    }
    
    /**
     * Scrolls the view up.
     */
    protected void scrollUp() {
        position.y -= scrollSize.y;
        if (position.y < mapSize.y) {
            position.y = mapSize.y;
        }
        repaint();
    }
    
    /**
     * Scrolls the view down.
     */
    protected void scrollDown() {
        position.y += scrollSize.y;
        if (position.y > mapSize.y + mapSize.height) {
            position.y = mapSize.y + mapSize.height;
        }
        repaint();
    }
    
    /**
     * This method should be called between JPanel size and position changes and
     * access to the viewpoint iface. This is here just to optimize
     * the performance
     */
    protected void updateViewpoint() {
        Dimension d = getSize();
        
        d.height *= zoom;
        d.width *= zoom;
        
        mapView = new Rectangle(
                position.x - d.width/2,
                position.y - d.height/2,
                d.width,
                d.height);
    }
    
    /**
     * Prepares given device context for rendering. This method should
     * be called before render itself to setup the device context properly.
     *
     * @param g2d Graphics2D to be prepared
     */
    protected void prepareRender(Graphics2D g2d) {
        g2d.scale(1/(double)zoom, 1/(double)zoom);
    }
    
    public boolean visible(Rectangle rect) {
        return
                rect.x >= mapView.x &&
                rect.y >= mapView.y &&
                rect.x + rect.width <= mapView.x + mapView.width &&
                rect.y + rect.height <= mapView.y + mapView.height;
    }
    
    public boolean visible(Point point) {
        return
                point.x >= mapView.x &&
                point.y >= mapView.y &&
                point.x <= mapView.x + mapView.width &&
                point.y <= mapView.y + mapView.height;
    }
    
    public int getZoom() {
        return zoom;
    }
    
    /**
     * Retrives the position in the map
     */
    public Point getPosition() {
        return (Point)position.clone();
    }
    
    /**
     * Retrieves object under the given point (ignoring transparency in GIFs)
     *
     * @param point Point to be searched.
     * @param acceptWP do we accept also the WayPoints?
     * @return IveObject (even a WayPoint, under the point)
     */
    abstract public IveObject getObjectAtPosition(
            Point point, boolean acceptWP);
    
    
    /**
     * Performs shorcut action (a implicit action) on the selected object.
     * We only perform implicit action on Areas, where we show the area in the
     * new Tab.
     *
     * @param ev Mouse event that provoked the ShortcutAction.
     */
    public void performShortcutAction(MouseEvent ev) {
        SchedulerImpl.instance().instance().lockSimulation();
        
        IveObject obj = getObjectAtPosition(ev.getPoint(), true);
        
        if (obj == null) {
            SchedulerImpl.instance().instance().unlockSimulation();
            return;
        }
        
        if (obj instanceof GridArea || obj instanceof GraphArea) {
            new PopupActionListener(obj.getId(), 
                    container instanceof FrameContainer).actionPerformed(null);
        }
        
        SchedulerImpl.instance().instance().unlockSimulation();
    }
    
    /**
     * Shows popup menu with some action appropriate for the selected object.
     * This method uses the simulation lock.
     */
    public void showSelectedObjectMenu(MouseEvent e) {
        SchedulerImpl.instance().instance().lockSimulation();
        
        IveObject obj = getObjectAtPosition(e.getPoint(), true);
        
        if (obj == null) {
            SchedulerImpl.instance().instance().unlockSimulation();
            return;
        }
        
        JPopupMenu menu = getPopupMenu(obj);
        
        SchedulerImpl.instance().instance().unlockSimulation();
        if (menu != null) {
            menu.show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }
    
    /**
     * Prepares popup menu for the given tree node.
     *
     * @param obj IveObject for which to create the PopupMenu.
     * @return JPopupMenu created from info the given object.
     */
    protected JPopupMenu getPopupMenu(final IveObject obj) {
        JPopupMenu menu = new JPopupMenu();
        String type = "";
        final String id = obj.getId();
        
        JMenuItem item;
        item = new JMenuItem(id);
        item.setEnabled(false);
        menu.add(item);
        menu.addSeparator();
        
        if (obj instanceof Area) {
            type = "Area";
            
            String openStr = "Open "+type+" \""+id+"\"";
            menu.add(openStr+" in Window").addActionListener(
                    new PopupActionListener(id, true));
            menu.add(openStr+" in Tab").addActionListener(
                    new PopupActionListener(id, false));
            
            final int lodRequested = obj.getLod() + 1;
            
            menu.addSeparator();
            menu.add("Add holdback").addActionListener(
                    new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    HoldBackListDialog.openAdd(MainFrame.instance(),
                            id, lodRequested);
                }
            });
            
        } else if (obj instanceof WayPoint) {
            type = "WayPoint";
            
            final int lodRequested = obj.getLod();
            
            menu.add("Add holdback").addActionListener(
                    new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    HoldBackListDialog.openAdd(MainFrame.instance(),
                            id, lodRequested);
                }
            });
            
            if (obj.getId().endsWith(".Ballroom.wp_2_13")) {
                menu.add("Add jukebox").addActionListener(
                        new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        SchedulerImpl.instance().lockSimulation();
                        WayPoint wp = (WayPoint)ObjectMap.instance().getObject(
                                obj.getId());
                        
                        if (wp != null) {
                            ObjectTemplate tmp =
                                    (ObjectTemplate)TemplateMap.instance().
                                    getTemplate("Jukebox");
                            IveObject jukebox = tmp == null ? null :
                                tmp.instantiate(wp.getId() + wp.SEP +
                                    "Jukebox");
                            
                            if (!wp.placeObject(jukebox, null, wp)) {
                                Log.severe("Unable to place the Jukebox.");
                            }
                        }
                        SchedulerImpl.instance().updateWorld();
                        SchedulerImpl.instance().unlockSimulation();
                    }
                });
            }
        } else {
            type = "Object";
            
            Set<AttributeValue> attrs = obj.getAllAtributes();
            AttributeValue[] labels = new AttributeValue[attrs.size()];
            int i = 0;
            for (AttributeValue value : attrs) {
                String attrName = value.getName();
                
                labels[i] = value;
                i++;
            }
            java.util.Arrays.sort(labels,new Comparator<AttributeValue>() {
                public int compare(AttributeValue o1, AttributeValue o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (AttributeValue value : labels) {
                String attrName = value.getName();
                
                item = menu.add("Attribute \"" + attrName + "\" = \"" +
                        value.toString() + "\" (" + value.getType() + ")");
                item.addActionListener(
                        new PopupAttributeListener(id, attrName));
                item.setEnabled(AttributeEditorDialog.canEdit(value));
            }
            
            menu.addSeparator();
            Holdback holdback = obj.getHoldback();
            item = new JMenuItem("LOD (" + holdback.getExistence() +
                    ", " + holdback.getView() + ")");
            item.setEnabled(false);
            menu.add(item);
            
            if (ObjectClassTree.instance().getObjectClass("/system/Holdback").
                    isInside(obj)) {
                item = new JMenuItem("Remove", IconBag.DELETE.getIcon());
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Holdbacks.instance().removeHoldback(obj);
                    }
                });
                menu.add(item);
            }
            
            if (ObjectClassTree.instance().getObjectClass("/object/Jukebox").
                    isInside(obj)) {
                item = new JMenuItem("Remove", IconBag.DELETE.getIcon());
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        SchedulerImpl.instance().lockSimulation();
                        IveObject jukebox = ObjectMap.instance().getObject(
                                obj.getId());
                        
                        if (jukebox != null) {
                            jukebox.getMaster().removeObject(jukebox);
                            jukebox.setObjectState(
                                    IveObject.ObjectState.NOT_EXIST);
                            ObjectMap.instance().unregister(jukebox.getId());
                            ObjectMap.instance().unregister(jukebox.getId() + 
                                    IveId.SEP + "Eye");
                        }
                        SchedulerImpl.instance().updateWorld();
                        SchedulerImpl.instance().unlockSimulation();
                    }
                });
                menu.add(item);
            }
        }
        
        return menu;
    }
    
    /**
     * Extension of Action listener. This is used for opening a Tab or Window
     * with given location chosen by a user from the ZoomScrollPanel.
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
     * Does the subwindow already contain a given object?
     *
     * @param object that is querried.
     * @return <code>true</code> iff the object is already viewed by 
     *      this window.
     */
    public boolean contain(Object object) {
        if (object instanceof Area) {
            return ((Area)object).getId().equals(area.getId());
        }
        return false;
    }
    
    /**
     * Extension of Action listener. This is used for opening the attribute
     * editors on the objects chosen from the ZoomScrollPanel.
     */
    public class PopupAttributeListener implements ActionListener {
        
        protected String objectId;
        protected String attributeId;
        
        public PopupAttributeListener(String objectId, String attributeId) {
            this.objectId = objectId;
            this.attributeId = attributeId;
        }
        
        public void actionPerformed(ActionEvent ev) {
            AttributeEditorDialog.open(attributeId, objectId);
        }
    }
}

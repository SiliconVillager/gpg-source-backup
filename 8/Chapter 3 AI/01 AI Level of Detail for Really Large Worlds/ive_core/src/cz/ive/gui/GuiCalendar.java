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

import cz.ive.evaltree.time.FuzzyTimeInterval;
import cz.ive.evaltree.time.FuzzyTimer;
import cz.ive.gui.icon.IconBag;
import cz.ive.gui.subwindow.*;
import cz.ive.gui.table.IconTableCellRenderer;
import cz.ive.iveobject.Ent;
import cz.ive.messaging.Listener;
import cz.ive.simulation.*;
import cz.ive.util.Pair;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Subwinodw with a gui representation of the calendar.
 *
 * @author ondra
 */
public class GuiCalendar extends JTable implements Subwindow, Gui {
    
    /** Info for gui calendar subwindow */
    protected static Info CALENDAR_INFO = new Info("Calendar",
            "This panel views contents of the simulation calendar",
            IconBag.CALENDAR);
    
    /** String identification of the calendar subwindow. */
    public static String CALENDAR = "Calendar";
    
    /** Responsible Subwindow container */
    SubwindowContainer container;
    
    /** Our table model */
    private CalendarTableModel ourModel;
    
    /** Toolbar for command buttons */
    protected JToolBar toolbar;
    protected JPanel mainPanel;
    protected Action reload;
    
    /** List of events in the calendar. */
    protected java.util.List<EventInfo> events;
    
    /** Set of old events for highlighting. */
    protected Set<EventInfo> old = new HashSet<EventInfo>();
    
    /** Are we on the screen? Should we update? */
    protected boolean invisible = true;
    
    /** Creates a new instance of GuiCalendar */
    public GuiCalendar() {
        super();
        setModel(ourModel = new CalendarTableModel());
        setDefaultRenderer(Object.class, new IconTableCellRenderer());
        setColumnsWidths(new float[]{20, 80, 800});
        
        createActions();
        createComponents();
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
        reload.putValue(Action.SHORT_DESCRIPTION, "Reloads calendar table " +
                "with actual information");
    }
    
    /**
     * Sets widths of table columns
     *
     * @param colSizes array of the proportional column widths.
     */
    public void setColumnsWidths( float[] colSizes){
        TableColumnModel tcm = getColumnModel();
        int cols = tcm.getColumnCount();
        for( int i=0; i<cols; i++){
            TableColumn tc = tcm.getColumn( i);
            tc.setPreferredWidth( (int)colSizes[i]*3);
        }
    }
    
    /**
     * Recreates the JTable from the calendar. This locks the Simulation,
     * to prevent calendar from simultaneous update.
     */
    public void reload() {
        SchedulerImpl.instance().lockSimulation();
        events = CalendarImpl.instance().getGuiInfo();
        SchedulerImpl.instance().unlockSimulation();
        
        for (EventInfo event : events) {
            if (old.contains(event)) {
                event.markOld();
            }
        }
        old.clear();
        old.addAll(events);
        ourModel.update();
    }

    /**
     * Retrives Info for this Subwindow.
     *
     * @return Info filled with data about this Subwindow
     */
    public Info getInfo() {
        return CALENDAR_INFO;
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
            return CALENDAR.equals((String)object);
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
    
    /**
     * Helper storage class holding the Calendar event Info.
     */
    static public class EventInfo {
        public Pair<Boolean, String> strPair = new Pair<Boolean, String>();
        public Pair<Boolean, String> timePair = new Pair<Boolean, String>();
        public long time;
        public Icon icon = null;
        
        public EventInfo(Listener listener, long time) {
            
            String str = listener.toString();
            
            if (listener instanceof FuzzyTimer || 
                    listener instanceof FuzzyTimeInterval) {
                icon = IconBag.TIMER.getIcon();
            } else if (listener instanceof Ent.ActivationListener) {
                icon = IconBag.GENIUS.getIcon();
            }
            
            strPair.first = Boolean.TRUE;
            strPair.second = str;
            timePair.first = Boolean.TRUE;
            timePair.second = CalendarPlanner.simulationTimeToString(time);
            this.time = time;
        }
        
        public void markOld() {
            strPair.first = Boolean.FALSE;
            timePair.first = Boolean.FALSE;
        }
        
        public int hashCode() {
            return (int)(strPair.second.hashCode() + 
                    time);
        }
        
        public boolean equals(Object obj) {
            if (obj instanceof EventInfo) {
                EventInfo ev = (EventInfo)obj;
                return strPair.second.equals(ev.strPair.second) && 
                        time == ev.time;
            }
            return false;
        }
    }
    
    /**
     * Helper class for calendar contents table visualisation.
     */
    private class CalendarTableModel extends AbstractTableModel {
        
        public int getColumnCount() {
            return 3;
        }
        
        public int getRowCount() {
            return events == null ? 0 : events.size();
        }
        
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "";
                case 1:
                    return "Time";
                case 2:
                    return "Description";
            }
            return "";
        }
        
        public Object getValueAt(int row, int col) {
            EventInfo info = events.get(row);
            
            switch (col) {
                case 0:
                    return info.icon != null ? info.icon : "";
                case 1:
                    return info.timePair;
                case 2:
                    return info.strPair;
            }
            return "";
        }
        
        public void update() {
            fireTableDataChanged();
        }
    };
}
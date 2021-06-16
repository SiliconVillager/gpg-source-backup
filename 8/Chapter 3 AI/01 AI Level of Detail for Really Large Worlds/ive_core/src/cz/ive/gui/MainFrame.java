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

import cz.ive.gui.dialog.BreakpointListDialog;
import cz.ive.gui.dialog.HoldBackListDialog;
import cz.ive.gui.dialog.SchedulerConfigDialog;
import cz.ive.gui.icon.IconBag;
import cz.ive.lod.LodManager;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import java.io.*;

import cz.ive.*;
import cz.ive.gui.subwindow.*;
import cz.ive.simulation.*;
import cz.ive.location.*;
import cz.ive.logs.*;
import javax.swing.filechooser.FileFilter;

/**
 * MainFrame of the application
 *
 * @author Ondra
 */
public class MainFrame extends JFrame implements Gui {
    
    /** Maximal time slider value */
    protected static final int MAX_TIME_RATIO = 50;
    
    /** Singleton for this class */
    protected static MainFrame instance;
    
    /** FrameContainer for child window like Subwindows */
    protected FrameContainer frameContainer;
    
    /** TabContainer for tab like Subwindows */
    protected TabContainer tabContainer;
    
    protected JPanel contentPane;
    
    protected JTabbedPane tabPanel;
    protected LocationTree locationTree;
    
    protected JSlider ratioSlider;
    protected JLabel ratioLabel;
    protected JScrollPane treeBox;
    protected JPanel rightBox;
    protected JTextField timeTxt;
    protected JTextField usageTxt;
    
    protected Action loadWorld;
    protected Action loadSimulation;
    protected Action saveSimulation;
    protected Action fastSimulation;
    protected Action startSimulation;
    protected Action stepSimulation;
    protected Action stopSimulation;
    protected Action cleanSimulation;
    protected Action configureScheduler;
    
    protected Action addHoldback;
    protected Action manageHoldbacks;
    
    protected Action openLog;
    protected Action openGeniusTree;
    protected Action manageBreakpoints;
    protected Action showCalendar;
    
    /** Standard dialog for saving/opening a file */
    protected JFileChooser fileChooser = new JFileChooser();
    
    /** File filter for the simulation save files. */
    protected FileFilter savFilter = new FileFilter(){
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            if (f.exists() && f.getName().toLowerCase().endsWith(".sav"))
                return true;
            return false;
        }
        public String getDescription() {
            return "*.sav IVE simulation save file.";
        }
    };
    
    /** File filter for the World descriptionXML files. */
    protected FileFilter xmlFilter = new FileFilter(){
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            if (f.exists() && f.getName().toLowerCase().endsWith(".xml"))
                return true;
            return false;
        }
        public String getDescription() {
            return "*.xml IVE world description XML file.";
        }
    };
    
    /** Slider motion listener */
    ChangeListener movedSlider = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            // Change the time ratio of the simulation
            resetTimeRatio(ratioSlider.getValue()-MAX_TIME_RATIO);
        }
    };
    
    /**
     * Creates a new instance of MainFrame
     */
    public MainFrame() {
        super();
        contentPane = (JPanel)getContentPane();
        
        createComponents();
        setLocationRelativeTo( null);
        
        if (Toolkit.getDefaultToolkit().isFrameStateSupported(
                JFrame.MAXIMIZED_BOTH)) {
            setExtendedState(MAXIMIZED_BOTH);
        }
    }
    
    /**
     * Prepares all visual components
     */
    protected void createComponents() {
        
        createActions();
        
        // Creation of the ToolBar
        JButton loadWorldBtn = new JButton(loadWorld);
        loadWorldBtn.setText(null);
        loadWorldBtn.setFocusable(false);
        JButton loadBtn = new JButton(loadSimulation);
        loadBtn.setText(null);
        loadBtn.setFocusable(false);
        JButton saveBtn = new JButton(saveSimulation);
        saveBtn.setText(null);
        saveBtn.setFocusable(false);
        
        JButton stopBtn = new JButton(stopSimulation);
        stopBtn.setText(null);
        stopBtn.setFocusable(false);
        JButton stepBtn = new JButton(stepSimulation);
        stepBtn.setText(null);
        stepBtn.setFocusable(false);
        JButton startBtn = new JButton(startSimulation);
        startBtn.setText(null);
        startBtn.setFocusable(false);
        JButton fastBtn = new JButton(fastSimulation);
        fastBtn.setText(null);
        fastBtn.setFocusable(false);
        JButton cleanBtn = new JButton(cleanSimulation);
        cleanBtn.setText(null);
        cleanBtn.setFocusable(false);
        
        ratioSlider = new JSlider();
        ratioSlider.setOrientation(JSlider.HORIZONTAL);
        ratioSlider.setSize(50, 200);
        ratioSlider.setMinimum(1);
        ratioSlider.setMaximum(MAX_TIME_RATIO * 2 - 1);
        ratioSlider.setValue(MAX_TIME_RATIO);
        ratioSlider.addChangeListener(movedSlider);
        ratioSlider.setMaximumSize( new Dimension(200,20));
        ratioSlider.setFocusable(false);
        
        ratioLabel = new JLabel();
        ratioLabel.setSize(80,20);
        ratioLabel.setText("Simulaton/Real time 1:1");
        
        timeTxt = new JTextField(8);
        timeTxt.setEditable(false);
        timeTxt.setMaximumSize(new Dimension(100,20));
        timeTxt.setHorizontalAlignment(timeTxt.RIGHT);
        timeTxt.setFocusable(false);
        
        usageTxt = new JTextField(6);
        usageTxt.setEditable(false);
        usageTxt.setMaximumSize(new Dimension(100,20));
        usageTxt.setHorizontalAlignment(usageTxt.RIGHT);
        usageTxt.setFocusable(false);
        
        Insets zero = new Insets(0, 0, 0, 0);
        JToolBar toolBar = new JToolBar( "Simulation ToolBar", JToolBar.HORIZONTAL);
        toolBar.setMargin( zero);
        
        toolBar.add(loadWorldBtn, zero);
        toolBar.add(loadBtn, zero);
        toolBar.add(saveBtn, zero);
        toolBar.addSeparator();
        toolBar.add(stopBtn, zero);
        toolBar.add(stepBtn, zero);
        toolBar.add(startBtn, zero);
        toolBar.add(fastBtn, zero);
        toolBar.addSeparator();
        toolBar.add(cleanBtn, zero);
        toolBar.addSeparator();
        toolBar.add(new JLabel("S-time: "), zero);
        toolBar.add(timeTxt, zero);
        toolBar.addSeparator();
        toolBar.add(new JLabel("Load: "), zero);
        toolBar.add(usageTxt, zero);
        toolBar.addSeparator();
        toolBar.add(new ProcessorLoadPanel(), zero);
        toolBar.addSeparator();
        toolBar.add(ratioSlider, zero);
        toolBar.add(ratioLabel, zero);
        toolBar.add(new JPanel(), zero);
        
        
        // Location TreeView
        locationTree = new LocationTree(IveMapImpl.instance().getRoot());
        locationTree.opened();
        treeBox = new JScrollPane(locationTree);
        
        rightBox = new JPanel();
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));
        rightBox.add(treeBox);
        rightBox.setMinimumSize(new Dimension(150, 400));
        
        // Tabbed Panel
        tabPanel = new JTabbedPane(JTabbedPane.TOP);
        tabPanel.setMinimumSize(new Dimension(450, 400));
        
        tabContainer = new TabContainer(tabPanel);
        frameContainer = new FrameContainer();
        
        // Split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                tabPanel, rightBox);
        splitPane.setResizeWeight( 0.8);
        
        
        // Place it all
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createMenu(), BorderLayout.NORTH);
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout());
        contentPane.add(innerPanel, BorderLayout.CENTER);
        
        innerPanel.add(toolBar, BorderLayout.NORTH);
        innerPanel.add(splitPane, BorderLayout.CENTER);
        
        
        setTitle("IVE - Intelligent Virtual Environment");
        setLocation(new Point(0, 0));
        setSize(new Dimension(1024, 740));
        setMinimumSize(new Dimension(800,600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
        // Log subwindow
        tabContainer.addSubwindow(new LogSubwindow());
    }
    
    /**
     * Creates main menu.
     *
     * @return prepared JMenuBar
     */
    public JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem item;
        
        menu = new JMenu("File");
        item = new JMenuItem(loadWorld);
        menu.add(item);
        item = new JMenuItem(loadSimulation);
        menu.add(item);
        item = new JMenuItem(saveSimulation);
        menu.add(item);
        menuBar.add(menu);
        
        menu = new JMenu("Simulation");
        item = new JMenuItem(stopSimulation);
        menu.add(item);
        item = new JMenuItem(stepSimulation);
        menu.add(item);
        item = new JMenuItem(startSimulation);
        menu.add(item);
        item = new JMenuItem(fastSimulation);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem(cleanSimulation);
        menu.add(item);
        item = new JMenuItem(configureScheduler);
        menu.add(item);
        menuBar.add(menu);
        
        menu = new JMenu("Lod");
        item = new JMenuItem(addHoldback);
        menu.add(item);
        item = new JMenuItem(manageHoldbacks);
        menu.add(item);
        menuBar.add(menu);
        
        menu = new JMenu("Debugging");
        item = new JMenuItem(openLog);
        menu.add(item);
        item = new JMenuItem(openGeniusTree);
        menu.add(item);
        item = new JMenuItem(manageBreakpoints);
        menu.add(item);
        item = new JMenuItem(showCalendar);
        menu.add(item);
        menuBar.add(menu);
        
        return menuBar;
    }
    
    /** Creates all actions */
    public void createActions() {
        loadWorld = new AbstractAction("Load World",
                IconBag.OPEN_WORLD.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                // Stop the simulation before loading
                SimulationState state =
                        SchedulerImpl.instance().getSimulationState();
                if (state == state.RUNNING) {
                    SchedulerImpl.instance().stopSimulation();
                    while (SchedulerImpl.instance().isInSimulationLoop()) {}
                }
                
                fileChooser.setDialogType(fileChooser.OPEN_DIALOG);
                fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);

                fileChooser.resetChoosableFileFilters();
                fileChooser.setAcceptAllFileFilterUsed(true);
                fileChooser.addChoosableFileFilter(xmlFilter);
                
                // open load file window
                if (fileChooser.showOpenDialog(MainFrame.this) ==
                        JFileChooser.APPROVE_OPTION) {
                    try {
                        IveApplication.instance().loadWorld(
                                fileChooser.getCurrentDirectory().toString()+
                                "/"+fileChooser.getSelectedFile().getName());
                    } catch (Exception ex) {
                        IveApplication.printStackTrace(ex);
                        JOptionPane.showMessageDialog(contentPane, new String(
                                "Error occured while loading!"), "Error", 0);
                    }
                    if (IveMapImpl.instance().getRoot() == null) {
                        JOptionPane.showMessageDialog(contentPane, new String(
                                "No root loaction was loaded!"), "Error", 0);
                    }
                    // Revalidate all subwindows
                    revalidateSubwindows();
                } else {
                    if (state == state.RUNNING)
                        SchedulerImpl.instance().runSimulation();
                }
            }
        };
        loadWorld.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_W,
                InputEvent.CTRL_DOWN_MASK));
        loadWorld.putValue(Action.SHORT_DESCRIPTION, "Load world description " +
                "from a XML file");
        
        loadSimulation = new AbstractAction("Load Simulation",
                IconBag.OPEN.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                // load simulation
                
                // Stop the simulation before loading
                SimulationState state =
                        SchedulerImpl.instance().getSimulationState();
                if (state == state.RUNNING) {
                    SchedulerImpl.instance().stopSimulation();
                    while (SchedulerImpl.instance().isInSimulationLoop()) {}
                }
                
                fileChooser.setDialogType(fileChooser.OPEN_DIALOG);
                fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);

                fileChooser.resetChoosableFileFilters();
                fileChooser.setAcceptAllFileFilterUsed(true);
                fileChooser.addChoosableFileFilter(savFilter);
                
                // open load file window
                if (fileChooser.showOpenDialog(MainFrame.this) ==
                        JFileChooser.APPROVE_OPTION) {
                    try {
                        IveApplication.instance().loadSimulation(
                                fileChooser.getCurrentDirectory().toString()+
                                "/"+fileChooser.getSelectedFile().getName());
                    } catch (ClassNotFoundException ex) {
                        JOptionPane.showMessageDialog(contentPane,
                                "Some of the saved classes were not found. " +
                                "URLs to the libraries may have changed.\n" +
                                "If it is so, you should manually change the " +
                                "CLASSPATH system variable, to contain those " +
                                "libraries.", "Error", 0);
                        Log.severe("Load failed: " + ex.getMessage());
                    } catch (IOException ex) {
                        IveApplication.printStackTrace(ex);
                        JOptionPane.showMessageDialog(contentPane,
                                "Problems when opening and reading the file.",
                                "Error", 0);
                        Log.severe("Load failed: " + ex.getMessage());
                    }
                    // Revalidate all subwindows
                    revalidateSubwindows();
                } else {
                    if (state == state.RUNNING)
                        SchedulerImpl.instance().runSimulation();
                }
            }
        };
        loadSimulation.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_O,
                InputEvent.CTRL_DOWN_MASK));
        loadSimulation.putValue(Action.SHORT_DESCRIPTION, "Load simulation " +
                "from a previously saved file");
        
        saveSimulation = new AbstractAction("Save Simulation",
                IconBag.SAVE.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                // Stop the simulation before saving
                
                SimulationState state =
                        SchedulerImpl.instance().getSimulationState();
                if (state == state.RUNNING) {
                    SchedulerImpl.instance().stopSimulation();
                    while (SchedulerImpl.instance().isInSimulationLoop()) {}
                }
                
                fileChooser.setDialogType(fileChooser.SAVE_DIALOG);
                fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);

                fileChooser.resetChoosableFileFilters();
                fileChooser.setAcceptAllFileFilterUsed(true);
                fileChooser.addChoosableFileFilter(savFilter);
                
                if (fileChooser.showSaveDialog(MainFrame.this) ==
                        JFileChooser.APPROVE_OPTION) {
                    
                    // save to file
                    try {
                        IveApplication.instance().saveSimulation(
                                fileChooser.getCurrentDirectory().toString()+
                                "/"+fileChooser.getSelectedFile().getName());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(contentPane,
                                "Problems when opening and writing the file.",
                                "Error", 0);
                        Log.severe("Save failed: " + ex.getMessage());
                    }
                }
                
                // Run the simulation again.
                if (state == state.RUNNING)
                    SchedulerImpl.instance().runSimulation();
            }
        };
        saveSimulation.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK));
        saveSimulation.putValue(Action.SHORT_DESCRIPTION, "Save simulation " +
                "to the file");
        
        fastSimulation = new AbstractAction("Full throttle",
                IconBag.FAST.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                // start simulation
                SchedulerImpl.instance().runSimulation();
                SchedulerImpl.instance().setFastSimulation(true);
            }
        };
        fastSimulation.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_M,
                InputEvent.CTRL_DOWN_MASK));
        fastSimulation.putValue(Action.SHORT_DESCRIPTION, "Simulate as fast " +
                "as possible");
        
        startSimulation = new AbstractAction("Start",
                IconBag.START.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                // start simulation
                SchedulerImpl.instance().setFastSimulation(false);
                SchedulerImpl.instance().runSimulation();
            }
        };
        startSimulation.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_N,
                InputEvent.CTRL_DOWN_MASK));
        startSimulation.putValue(Action.SHORT_DESCRIPTION, "Start the " +
                "simulation in the specified simulation/real time ratio");
        
        stepSimulation = new AbstractAction("Step",
                IconBag.STEP.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                // step simulation
                SchedulerImpl.instance().setFastSimulation(false);
                SchedulerImpl.instance().stepSimulation();
            }
            
        };
        stepSimulation.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_B,
                InputEvent.CTRL_DOWN_MASK));
        stepSimulation.putValue(Action.SHORT_DESCRIPTION, "Simulate to the " +
                "next event");
        
        stopSimulation = new AbstractAction("Stop",
                IconBag.STOP.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                // stop simulation
                SchedulerImpl.instance().setFastSimulation(false);
                SchedulerImpl.instance().stopSimulation();
            }
        };
        stopSimulation.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_V,
                InputEvent.CTRL_DOWN_MASK));
        stopSimulation.putValue(Action.SHORT_DESCRIPTION, "Stop the " +
                "simulation");
        
        cleanSimulation = new AbstractAction("CleanUp",
                IconBag.CLEANUP.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                SchedulerImpl.instance().lockSimulation();
                
                LodManager.instance().changed(null);
                
                SchedulerImpl.instance().updateWorld();
                
                SchedulerImpl.instance().unlockSimulation();
            }
        };
        cleanSimulation.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_C,
                InputEvent.CTRL_DOWN_MASK));
        cleanSimulation.putValue(Action.SHORT_DESCRIPTION, "Clean up " +
                "unnecessary locations");
        
        configureScheduler = new AbstractAction("Configure cleanup",
                IconBag.CONFIG.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                // Show the SchedulerConfigDialog
                SchedulerConfigDialog.open();
            }
        };
        configureScheduler.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_F,
                InputEvent.CTRL_DOWN_MASK));
        configureScheduler.putValue(Action.SHORT_DESCRIPTION, "Configure the " +
                "cleanup statistics gathering");
        
        openLog = new AbstractAction("Open Log",
                IconBag.LOG_PANEL.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                // Log subwindow
                if (!findAndFocus(LogSubwindow.LOG))
                    tabContainer.addSubwindow(new LogSubwindow());
            }
        };
        openLog.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_L,
                InputEvent.CTRL_DOWN_MASK));
        openLog.putValue(Action.SHORT_DESCRIPTION, "Open the log tab-window");
        
        openGeniusTree = new AbstractAction("Open Genius Tree",
                IconBag.GENIUS_LIST.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                if (!findAndFocus(GeniusTree.GENIUS_TREE))
                    tabContainer.addSubwindow(new GeniusTree());
            }
        };
        openGeniusTree.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_G,
                InputEvent.CTRL_DOWN_MASK));
        openGeniusTree.putValue(Action.SHORT_DESCRIPTION, "Open the geniuses " +
                "internals tab-window");
        
        // Holdbacks
        addHoldback = new AbstractAction("Add holdback",
                IconBag.HOLDBACK.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                HoldBackListDialog.openAdd(MainFrame.this, null, -1);
            }
        };
        addHoldback.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_A,
                InputEvent.CTRL_DOWN_MASK));
        addHoldback.putValue(Action.SHORT_DESCRIPTION, "Opens dialog for " +
                "holdback addition");
        
        manageHoldbacks = new AbstractAction("Manage holdbacks",
                IconBag.CONFIG.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                HoldBackListDialog.open();
            }
        };
        manageHoldbacks.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_H,
                InputEvent.CTRL_DOWN_MASK));
        manageHoldbacks.putValue(Action.SHORT_DESCRIPTION, "Opens dialog for " +
                "holdbacks management");
        
        manageBreakpoints = new AbstractAction("Manage breakpoints",
                IconBag.CONFIG.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                BreakpointListDialog.open();
            }
        };
        manageBreakpoints.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_X,
                InputEvent.CTRL_DOWN_MASK));
        manageBreakpoints.putValue(Action.SHORT_DESCRIPTION, "Opens dialog for " +
                "breakpoints management");
        
        showCalendar = new AbstractAction("Calendar",
                IconBag.CALENDAR.getIcon()) {
            public void actionPerformed(ActionEvent e) {
                if (!findAndFocus(GuiCalendar.CALENDAR))
                    tabContainer.addSubwindow(new GuiCalendar());
            }
        };
        showCalendar.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_K,
                InputEvent.CTRL_DOWN_MASK));
        showCalendar.putValue(Action.SHORT_DESCRIPTION, "Opens new tab with " +
                "calendar internals");
        
        // Tab switching
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK),
                "nextTab");
        this.getRootPane().getActionMap().put( "nextTab", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tabContainer.focusAnotherTab(true);
            }
        } );
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.SHIFT_MASK),
                "prevTab");
        this.getRootPane().getActionMap().put( "prevTab", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tabContainer.focusAnotherTab(false);
            }
        } );
        
        // Actualize the time and processor load indicators.
        new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                timeTxt.setText(
                        CalendarPlanner.instance().getSimulationTimeString());
                
                Scheduler sch = SchedulerImpl.instance();
                SimulationState state = sch.getSimulationState();
                
                // Update load
                if (sch.getSimulationState() ==
                        SimulationState.RUNNING) {
                    int load = sch.getStatistics().
                            getCurrentLoad();
                    String loadStr = load + " %";
                    usageTxt.setText(loadStr);
                    
                    if (sch.getFastSimulation()) {
                        fastSimulation.setEnabled(false);
                        startSimulation.setEnabled(true);
                    } else {
                        fastSimulation.setEnabled(true);
                        startSimulation.setEnabled(false);
                    }
                    stepSimulation.setEnabled(false);
                    stopSimulation.setEnabled(true);
                    cleanSimulation.setEnabled(true);
                    saveSimulation.setEnabled(true);
                } else if (IveMapImpl.instance().getRoot() != null) {
                    usageTxt.setText("N/A");
                    fastSimulation.setEnabled(true);
                    startSimulation.setEnabled(true);
                    stepSimulation.setEnabled(true);
                    stopSimulation.setEnabled(false);
                    cleanSimulation.setEnabled(true);
                    saveSimulation.setEnabled(true);
                } else {
                    usageTxt.setText("N/A");
                    fastSimulation.setEnabled(false);
                    startSimulation.setEnabled(false);
                    stepSimulation.setEnabled(false);
                    stopSimulation.setEnabled(false);
                    cleanSimulation.setEnabled(false);
                    saveSimulation.setEnabled(false);
                }
            }
        }).start();
    }
    
    /**
     * Updates the state of the slider from the values in the scheduler.
     */
    protected void updateSlider() {
        double ratio = SchedulerImpl.instance().getSchedulingState().timeRatio;
        int real = 1;
        int sim = 1;
        int value = MAX_TIME_RATIO;
        double r = 1;
        
        if (ratio > 1) {
            while (ratio > r) {
                real++;
                value--;
                r = (double)real/(double)sim;
            }
        } else if (ratio < 1) {
            while (ratio < r) {
                sim++;
                value++;
                r = (double)real/(double)sim;
            }
        }
        
        ratioLabel.setText("Simulaton/Real time " +
                new Integer(sim).toString() + ":" +
                new Integer(real).toString());
        
        ratioSlider.setValueIsAdjusting(true);
        ratioSlider.setValue(value);
        ratioSlider.setValueIsAdjusting(false);
    }
    
    /**
     * Resets the new time ratio for the simulation
     *
     * @param value New value to be set up.
     */
    protected void resetTimeRatio(int value) {
        int real = 1;
        int sim = 1;
        if (value > 0) {
            sim = value + 1;
        } else if (value < 0){
            real = -value + 1;
        }
        
        ratioLabel.setText("Simulaton/Real time " +
                new Integer(sim).toString() + ":" +
                new Integer(real).toString());
        
        SchedulerImpl.instance().setTimeRatio((double)real/(double)sim);
    }
    
    /**
     * Retrives a singleton of this class
     */
    public static MainFrame instance() {
        if (instance == null) {
            instance = new MainFrame();
            SchedulerImpl.instance().registerGui(instance, 100);
        }
        return instance;
    }
    
    /**
     * Paint alls Gui implementing subcomponents.
     */
    public void paint() {
        SchedulerImpl.instance().lockSimulation();
        tabContainer.paint();
        frameContainer.paint();
        SchedulerImpl.instance().unlockSimulation();
    }
    
    /**
     * Retrives MainFrame's TabContainer.
     *
     * @return TabContainer used for tab like subwindow management
     */
    public TabContainer getDefaultTabContainer() {
        return tabContainer;
    }
    
    /**
     * Retrives MainFrame's FrameContainer.
     *
     * @return FrameContainer used for frame like subwindow management
     */
    public FrameContainer getDefaultFrameContainer() {
        return frameContainer;
    }
    
    /** 
     * Is the object viewed? If yes, focus it! 
     *
     * @param object to be queried.
     * @return <code>true</code> iff the object was found and the 
     *      subwindow focused.
     */
    public boolean findAndFocus(Object object) {
        if (frameContainer.findAndFocus(object)) 
            return true;
        if (tabContainer.findAndFocus(object)) 
            return true;
        return false;
    }
    
    /**
     * Revalidates all subwindows. This is to be used after any load.
     */
    public void revalidateSubwindows() {
        // Revalidate all subwindows
        tabContainer.revalidateSubwindows();
        frameContainer.revalidateSubwindows();
        locationTree.revalidateSubwindow();
        
        updateSlider();
    }
}

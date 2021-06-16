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
 
package cz.ive;
import cz.ive.evaltree.EvalTreeCounters;
import cz.ive.exception.SimulationRunningException;
import cz.ive.genius.GeniusList;
import cz.ive.gui.dialog.WaitDialog;
import cz.ive.lod.LodManager;
import cz.ive.logs.Log;
import cz.ive.manager.ManagerOfSenses;
import cz.ive.simulation.*;
import cz.ive.process.*;
import cz.ive.iveobject.*;
import cz.ive.simulation.breakpoint.BreakpointList;
import cz.ive.template.*;
import cz.ive.location.*;
import cz.ive.util.IveObjectInputStream;
import cz.ive.xmlload.XMLDOMLoader;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.SwingUtilities;

/**
 * Container for maintaining all singletons. This is particularly usfull
 * during save and load.
 *
 * @author Zdenek
 */
public class IveApplication implements java.io.Serializable {
    
    /** Singleton for this class */
    protected static IveApplication instance;
    
    protected WorldInterpreter worldInterpreter;
    protected CalendarImpl calendarImpl;
    protected CalendarPlanner calendarPlanner;
    protected ProcessDBImpl processDBImpl;
    protected ObjectMap objectMap;
    protected SchedulingState schedulingState;
    protected ObjectClassTree objectClassTree;
    protected TemplateMap templateMap;
    protected Utterances utterances;
    protected Holdbacks holdbacks;
    protected IveMap iveMap;
    protected LodManager lMan;
    protected ManagerOfSenses sMan;
    protected PathFinderImpl pFinder;
    protected GeniusList geniusList;
    protected Updator updator;
    protected IveApplication application;
    protected EvalTreeCounters evalTreeCounters;
    protected BreakpointList breakpointList;
    
    
    
    transient protected List<URL> urls = new LinkedList<URL>();
    transient private URLClassLoader classLoader;
    
    /**
     * Should we stop the simulation when the calendar gets empty?
     */
    public boolean stopOnEmpty = false;
    
    /**
     * Ive uses DAG to manage expressions, if useDAG is true. Use of the DAG
     * in the trigger keeper is decided during the World load. This value will
     * be saved along with the simulation.
     */
    public boolean useDAG = true;
    
    /**
     * If this option is true the LOD related code is bypassed.
     * When root location the world is fully expanded
     */
    public boolean noLod = false;
    
    /**
     * GUI shows some Ive internals that are usefull for the framework
     * debugging. This is per execution (static) variable.
     */
    static public boolean debug = false;
    
    /** Log the cpu load to the log. */
    static public boolean logCpu = false;
    
    /**
     * Creates new instance of this class, this can be called only
     * from this class to ensure singleton property
     */
    private IveApplication() {
    }
    
    /**
     * Takes references to all Singletons. This must be done before saving
     * the simulation with simulation state STOPPED or NO_SIMULATION.
     */
    synchronized public void prepareToBackup() {
        worldInterpreter = WorldInterpreter.instance();
        calendarImpl = CalendarImpl.instance();
        calendarPlanner = CalendarPlanner.instance();
        processDBImpl = ProcessDBImpl.instance();
        objectMap = ObjectMap.instance();
        schedulingState = SchedulerImpl.instance().getSchedulingState();
        objectClassTree = ObjectClassTree.instance();
        templateMap = TemplateMap.instance();
        utterances = Utterances.instance();
        holdbacks = Holdbacks.instance();
        iveMap = IveMapImpl.instance();
        lMan = LodManager.instance();
        sMan = ManagerOfSenses.instance();
        pFinder = PathFinderImpl.instance();
        geniusList = GeniusList.instance();
        updator = Updator.getRootInstance();
        evalTreeCounters = EvalTreeCounters.instance();
        breakpointList = BreakpointList.instance();
        application = instance;
    }
    
    /**
     * Setups loaded instances of all Singletons. This must be done after load.
     */
    synchronized public void restoreFromBackup() {
        WorldInterpreter.setInstance(worldInterpreter);
        CalendarImpl.setInstance(calendarImpl);
        CalendarPlanner.setInstance(calendarPlanner);
        ProcessDBImpl.setInstance(processDBImpl);
        ObjectMap.setInstance(objectMap);
        ObjectClassTree.setInstance(objectClassTree);
        TemplateMap.setInstance(templateMap);
        Utterances.setInstance(utterances);
        Holdbacks.setInstance(holdbacks);
        IveMapImpl.setInstance(iveMap);
        LodManager.setInstance(lMan);
        ManagerOfSenses.setInstance(sMan);
        PathFinderImpl.setInstance(pFinder);
        GeniusList.setInstance(geniusList);
        Updator.setRootInstance(updator);
        EvalTreeCounters.setInstance(evalTreeCounters);
        breakpointList.setInstance(breakpointList);
        
        instance = application;
        try {
            SchedulerImpl.instance().setSchedulingState(schedulingState);
        } catch (Exception E) {
            
        }
    }
    
    /**
     * Empty all Singletons, to allow load of the whole World from the XML.
     * This must be called with simulation state STOPPED or NO_SIMULATION.
     */
    synchronized public void renewInstances() {
        urls.clear();
        classLoader = null;
        
        WorldInterpreter.emptyInstance();
        CalendarImpl.emptyInstance();
        CalendarPlanner.emptyInstance();
        ProcessDBImpl.emptyInstance();
        ObjectMap.emptyInstance();
        ObjectClassTree.emptyInstance();
        TemplateMap.emptyInstance();
        Utterances.emptyInstance();
        Holdbacks.emptyInstance();
        IveMapImpl.emptyInstance();
        LodManager.emptyInstance();
        ManagerOfSenses.emptyInstance();
        PathFinderImpl.emptyInstance();
        GeniusList.emptyInstance();
        Updator.emptyRootInstance();
        EvalTreeCounters.emptyInstance();
        BreakpointList.emptyInstance();
        try {
            schedulingState = SchedulerImpl.instance().getSchedulingState();
            schedulingState.timeRatio = 1;
            schedulingState.statistics.clearStatistics();
            SchedulerImpl.instance().setSchedulingState(schedulingState);
        } catch (SimulationRunningException ex) {
            IveApplication.printStackTrace(ex);
            Log.addMessage(
                    "Cannot renew instances, if the simulation is running.",
                    Log.SEVERE, "", "", "");
        }
    }
    
    /**
     * Loads whole world from the XML file.
     * The Simulation has to be in the STOPPED or the NO_SIMULATION state
     * when calling this method.
     *
     * @param path path to the XML file.
     * @throws IOException if there is some IO problem during the load.
     * @throws Exception on other problems during parsing the XML and class
     *      initialization.
     */
    synchronized public void loadWorld(String path) throws Exception {
        instance.renewInstances();
        
        if (!SwingUtilities.isEventDispatchThread()) {
            XMLDOMLoader loader = new XMLDOMLoader();
            
            loader.load(new File(path).toURL());
            return;
        }
        
        WaitDialog wdlg = WaitDialog.instance();
        XMLLoadThread thread = new XMLLoadThread( path);
        
        if (wdlg.startJob())
            thread.run();
        else {
            thread.start();
            wdlg.waitForCompletition(1000);
        }
        
        if (thread.exception != null) {
            instance.renewInstances();
            throw thread.exception;
        }
    }
    
    
    /**
     * Loads simulation from the specified file.
     * The Simulation has to be in the STOPPED or the NO_SIMULATION state
     * when calling this method.
     *
     * @param path path to the file with previously saved simulation.
     * @throws IOException if there is some IO problem during the load.
     * @throws ClassNotFoundException if there is some class that was not found
     *      and though can not be recreated.
     */
    synchronized public void loadSimulation(String path) throws IOException,
            ClassNotFoundException {
        
        if (!SwingUtilities.isEventDispatchThread()) {
            FileInputStream out = new FileInputStream(path);
            ObjectInputStream s = new ObjectInputStream(out);
            
            List<URL> urls = (List<URL>)s.readObject();
            URLClassLoader cl = new URLClassLoader(
                    urls.toArray(new URL[urls.size()]));
            
            this.classLoader = cl;
            
            // load
            ObjectInputStream is = new IveObjectInputStream(out, cl);
            IveApplication iveApp = (IveApplication)is.readObject();
            
            iveApp.urls = urls;
            iveApp.restoreFromBackup();
            
            is.close();
            return;
        }
        
        WaitDialog wdlg = WaitDialog.instance();
        LoadThread thread = new LoadThread( path);
        
        if (!SwingUtilities.isEventDispatchThread() || wdlg.startJob())
            thread.run();
        else {
            thread.start();
            wdlg.waitForCompletition(1000);
        }
        
        if (thread.exception != null) {
            instance.renewInstances();
            if (thread.exception instanceof ClassNotFoundException) {
                throw (ClassNotFoundException)thread.exception;
            }
            throw (IOException)thread.exception;
        }
    }
    
    /**
     * Saves simulation to the specified file.
     * The Simulation has to be in the STOPPED or the NO_SIMULATION state
     * when calling this method.
     *
     * @param path path to the file were the simulation should be stored.
     * @throws IOException if there is some IO problem during the save.
     */
    synchronized public void saveSimulation(String path) throws IOException {
        prepareToBackup();
        
        if (!SwingUtilities.isEventDispatchThread()) {
            FileOutputStream out = new FileOutputStream(path);
            ObjectOutputStream s = new ObjectOutputStream(out);
            
            // save
            s.writeObject(urls);
            s.writeObject(IveApplication.instance());
            s.close();
            return;
        }
        
        WaitDialog wdlg = WaitDialog.instance();
        SaveThread thread = new SaveThread( path);
        
        if (!SwingUtilities.isEventDispatchThread() || wdlg.startJob())
            thread.run();
        else {
            thread.start();
            wdlg.waitForCompletition(1000);
        }
        
        if (thread.exception != null) {
            IveApplication.printStackTrace(thread.exception);
            throw thread.exception;
        }
    }
    
    /**
     * Getter for IveApplication singleton
     *
     * @return singleton of this class
     */
    synchronized public static IveApplication instance() {
        return instance==null ? instance = new IveApplication() : instance;
    }
    
    /**
     * Loads class specified by the given name.
     *
     * @param className Name of the class to be loaded.
     * @return loaded class
     * @throws ClassNotFoundException when the class is not in the path.
     */
    public Class<?> loadIveClass(String className) throws
            ClassNotFoundException {
        ClassLoader cl = getIveClassLoader();
        return cl.loadClass(className);
    }
    
    /**
     * Appends new url, to the list of the classpath urls.
     *
     * @param url URL where to look for the classes.
     */
    public void addClassPathURL(URL url) {
        synchronized (urls) {
            urls.add(url);
            if (classLoader != null) {
                classLoader = new URLClassLoader(new URL[]{url},
                        classLoader);
            }
        }
    }
    
    /**
     * Locates the resource using the Ive class loader.
     *
     * @param path Path to the reource inside the package hierarchy.
     * @return Specified resource as an InputStream.
     */
    public InputStream getIveResourceAsStream(String path) {
        ClassLoader cl = getIveClassLoader();
        return cl.getResourceAsStream(path);
    }
    
    /**
     * Retrieves (and initializes if necessary) the classloader.
     *
     * @return Class loader with the extended classpath.
     */
    protected ClassLoader getIveClassLoader() {
        synchronized (urls) {
            if (classLoader == null && urls.size() > 0) {
                classLoader = new URLClassLoader(
                        urls.toArray(new URL[urls.size()]));
            } else if (classLoader == null) {
                return ClassLoader.getSystemClassLoader();
            }
            return classLoader;
        }
    }
    
    /**
     * Writes out the IveApplciation into the object stream.
     * We write the custom uri list first.
     *
     * @param s stream to be filled with description of this object
     */
    private void writeObject(java.io.ObjectOutputStream s) throws 
            java.io.IOException {
        
        // Write out any hidden serialization magic
        s.defaultWriteObject();
    }
    
    /**
     * Loads contents of this object from the object stream.
     * There is a hack, because we must load id before everything
     * else if we are instance of IveIdImpl.
     *
     * @param s stream to be used to load the description of this object.
     */
    private void readObject(java.io.ObjectInputStream s) throws 
            java.io.IOException, ClassNotFoundException {

        if (s instanceof IveObjectInputStream) {
            classLoader = ((IveObjectInputStream)s).getIveClassLoader();
        }
        
        // Read in any hidden serialization magic
        s.defaultReadObject();
    }
    
    /**
     * Print stack trace of the given exception. This method does nothing,
     * when the application is not run in the debug mode.
     *
     * @param exc Exception to be stackTraced
     */
    static public void printStackTrace(Throwable exc) {
        if (debug) {
            exc.printStackTrace();
        }
    }
    
    /**
     * Helper work thread for XML load
     */
    class XMLLoadThread extends Thread {
        public String path;
        public Exception exception;
        
        public XMLLoadThread(String path) {
            this.path = path;
        }
        
        public void run() {
            WaitDialog.instance().setProgressMessage("Loading XML...");
            try {
                XMLDOMLoader loader = new XMLDOMLoader();
                
                loader.load(new File(path).toURL());
                CalendarImpl.instance().updateWorld();
            } catch (Exception ex) {
                exception = ex;
            }
            WaitDialog.instance().finishJob();
        }
    }
    
    /**
     * Helper work thread for standard load.
     */
    class LoadThread extends Thread {
        public String path;
        public Exception exception;
        
        public LoadThread(String path) {
            this.path = path;
        }
        
        public void run() {
            WaitDialog.instance().setProgressMessage("Loading simulation...");
            try {
                FileInputStream out = new FileInputStream(path);
                ObjectInputStream s = new ObjectInputStream(out);
                
                List<URL> urls = (List<URL>)s.readObject();
                URLClassLoader cl = new URLClassLoader(
                        urls.toArray(new URL[urls.size()]));
                s.close();
                
                IveApplication.this.classLoader = cl;
                
                // load
                out = new FileInputStream(path);
                ObjectInputStream is = new IveObjectInputStream(out, cl);
                urls = (List<URL>)is.readObject();
                IveApplication iveApp = (IveApplication)is.readObject();
                
                iveApp.urls = urls;
                iveApp.restoreFromBackup();
                
                is.close();
            } catch (Exception ex) {
                exception = ex;
            }
            WaitDialog.instance().finishJob();
        }
    }
    
    /**
     * Helper work thread for standard load.
     */
    class SaveThread extends Thread {
        public String path;
        public IOException exception;
        
        public SaveThread(String path) {
            this.path = path;
        }
        
        public void run() {
            WaitDialog.instance().setProgressMessage("Saving simulation...");
            try {
                FileOutputStream out = new FileOutputStream(path);
                ObjectOutputStream s = new ObjectOutputStream(out);
                
                // save
                s.writeObject(urls);
                s.writeObject(IveApplication.instance());
                s.close();
            } catch (IOException ex) {
                exception = ex;
            }
            WaitDialog.instance().finishJob();
        }
    }
}


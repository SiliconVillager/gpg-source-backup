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

import cz.ive.gui.MainFrame;
import cz.ive.logs.Log;
import cz.ive.simulation.SchedulerImpl;
import java.io.*;
import javax.swing.*;

/**
 * Main of the whole application. It performs the command line parameters
 * parsing and fires the application.
 *
 * Command line parameters:
 *      --system-look       Use default system look and feel.
 *      --aqua-look         Use aqua look and feel.
 *      --noDAG             Do not use the DAG in the TriggerKeeper.
 *      --stop-on-empty     Stop the simulation when the calendar gets empty.
 *      -d --debug          Additional debug strings in the Gui.
 *      -w [XML filename]   Loads World from the specified XML file.
 *      -i [filename]       Loads previously saved Simulation from a file.
 *      -l --log            Log to the standard output.
 *      -m [count]          Size of the circular log (1000 by default).
 *      -h --help           View the help page (this page).
 *      --log-cpu           Log the cpu load to the log.
 *
 * @author ondra
 */
public class IveExecutor {
    
    /** View with gui? */
    protected boolean gui = true;
    
    /** Use default system look and feel? */
    protected boolean systemLF = false;
    
    /** Use aqua look? */
    protected boolean aquaLF = false;
    
    /** Log to the console. */
    protected boolean log = false;
    
    /** Path to the world XML file */
    protected String worldXMLFile;
    
    /** Path to the simulation file */
    protected String simulationFile;
    
    /**
     * Main method of the application
     *
     * @param args command line args
     */
    public static void main(String[] args) {
        IveExecutor executor;
        try {
            executor = new IveExecutor(args);
        } catch (Exception ex) {
            return;
        }
        executor.execute();
    }
    
    /**
     * Creates a new instance of IveExecutor
     *
     * @param args command line args to be parsed
     * @throws exception on unexpected params or explicit help request
     */
    public IveExecutor(String[] args) throws Exception {
        try {
            for (int i=0; i<args.length; i = parseOne(args,i));
        } catch (Exception ex) {
            if (ex.getMessage() != null)
                System.out.println(ex.getMessage());
            System.out.println("Inteligent Virtual Environment " +
                    "- IVE Simulator");
            System.out.println("");
            System.out.println("  Command line parameters:");
            System.out.println("        --system-look       " +
                    "Use default system look and feel.");
            System.out.println("        --aqua-look         " +
                    "Use aqua look and feel.");
            System.out.println("        --noDAG             " +
                    "Do not use the DAG in the TriggerKeeper.");
            System.out.println("        --noLOD             " +
                    "Do not use the Level Of Detail - simulate all details.");
            System.out.println("        --stop-on-empty     " +
                    "Stop the simulation when the calendar gets empty.");
            System.out.println("        -d --debug          " +
                    "Additional debug strings in the Gui.");
            System.out.println("        -w [XML filename]   " +
                    "Loads World from the specified XML file.");
            System.out.println("        -i [filename]       " +
                    "Loads previously saved Simulation from a file.");
            System.out.println("        -l --log            " +
                    "Log to the standard output.");
            System.out.println("        -m [count]          " +
                    "Size of the circular log (1000 by default).");
            System.out.println("        -h --help           " +
                    "View help page (this page).");
            System.out.println("        -log-cpu            " +
                    "Log the cpu load to the log.");
            
            System.out.println("");
        }
    }
    
    /**
     * Parses parameter at the given location
     *
     * @param args command line args
     * @param from index of the first unparsed parameter.
     * @return index of the first parameter that was left unparsed.
     * @throws exception on unexpected params or explicit help request
     */
    public int parseOne(String[] args, int from) throws Exception {
        /**
         * TODO: Future work
         *
         * This is a hidden parameter --nogui. It would be suitable if we wanted
         * to run a simulation in terminal. This would need some kind of command
         * line to allow manipulation with the simulation, which is out of the
         * scope of this project.
         *
         * if (args[from].equals("--nogui")) {
         * gui = false;
         * return from+1;
         * }
         */
        if (args[from].equals("--system-look")) {
            systemLF = true;
            aquaLF = false;
            return from+1;
        }
        if (args[from].equals("--aqua-look")) {
            aquaLF = true;
            systemLF = false;
            return from+1;
        }
        if (args[from].equals("--log-cpu")) {
            IveApplication.instance().logCpu = true;
            return from+1;
        }
        if (args[from].equals("-l") || args[from].equals("--log")) {
            log = true;
            return from+1;
        }
        if (args[from].equals("-m")) {
            if (args.length == from + 1) {
                throw new Exception("Size of the log was expected after the " +
                        "-m parameter.");
            }
            int size = 1000;
            try {
                size = Integer.parseInt(args[from+1]);
            } catch (NumberFormatException ex) {
                throw new Exception("A positive number was expected after the " +
                        "-m parameter.");
            }
            if (size < 10 || size > 100000) {
                throw new Exception("A positive number between 10 and " +
                        "100.000 included was expected after the " +
                        "-m parameter.");
            }
            Log.msgMax = size;
            return from+2;
        }
        if (args[from].equals("-d") || args[from].equals("--debug")) {
            IveApplication.debug = true;
            return from+1;
        }
        if (args[from].equals("--noDAG")) {
            IveApplication.instance().useDAG = false;
            return from+1;
        }
        if (args[from].equals("--noLOD")) {
            IveApplication.instance().noLod = true;
            return from+1;
        }
        if (args[from].equals("--stop-on-empty")) {
            IveApplication.instance().stopOnEmpty = true;
            return from+1;
        }
        if (args[from].equals("-h") || args[from].equals("--help")) {
            throw new Exception();
        }
        if (args[from].equals("-w")) {
            if (args.length == from + 1 ||
                    !new File(args[from+1]).canRead())
                throw new Exception("Filename of the existing readable file "+
                        "was expected after the -w parameter.");
            worldXMLFile = args[from+1];
            return from+2;
        }
        if (args[from].equals("-i")) {
            if (args.length == from + 1 ||
                    !new File(args[from+1]).canRead())
                throw new Exception("Filename of the existing readable file "+
                        "was expected after the -i parameter.");
            if (worldXMLFile != null)
                throw new Exception("You can use XML load or you can load " +
                        "previously saved simulation, not both.");
            simulationFile = args[from+1];
            return from+2;
        }
        throw new Exception("Unknown parameter "+args[from]+".");
    }
    
    /**
     * Executes the application according to the parsed parameters.
     */
    public void execute() {
        if (log)
            Log.consoleOn();
        else
            Log.consoleOff();
        
        if (gui) {
            
            // This is correct Swing initialisation invocation.
            try {
                // Set up the look and feel.
                /*
                try{
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                } catch( Exception ex){
                    IveApplication.printStackTrace(ex);
                }*/
                SwingUtilities.invokeAndWait(new Runnable(){
                    public void run() {
                        MainFrame.instance();
                        
                        if (simulationFile != null) {
                            try {
                                IveApplication.instance().loadSimulation(
                                        simulationFile);
                            } catch (ClassNotFoundException ex) {
                                System.out.println(
                                        "Some of the saved classes " +
                                        "were not found. Make sure that your " +
                                        "CLASSPATH system variable is well " +
                                        "configured.");
                                Log.severe("Load failed: " + ex.getMessage());
                                IveApplication.printStackTrace(ex);
                                JOptionPane.showMessageDialog(null,
                                        "Some of the saved classes were not " +
                                        "found. Make sure that your CLASSPATH" +
                                        " system variable is well configured.",
                                        "Error", 0);
                                return;
                            } catch (IOException ex) {
                                System.out.println(
                                        "Problems when opening and reading " +
                                        "the file.");
                                Log.severe("Load failed: " + ex.getMessage());
                                IveApplication.printStackTrace(ex);
                                JOptionPane.showMessageDialog(null,
                                        "Problems when opening and reading " +
                                        "the file.", "Error", 0);
                                return;
                            }
                        } else if (worldXMLFile != null) {
                            try {
                                IveApplication.instance().loadWorld(
                                        worldXMLFile);
                            } catch (IOException ex) {
                                System.out.println(
                                        "Problems when opening and reading " +
                                        "the file.");
                                Log.severe("XML Load failed: " + 
                                        ex.getMessage());
                                IveApplication.printStackTrace(ex);
                                JOptionPane.showMessageDialog(null,
                                        "Problems when opening and reading " +
                                        "the file.", "Error", 0);
                                
                                return;
                            } catch (Exception ex) {
                                System.out.println(
                                        "Problems when parsing the XML file.");
                                Log.severe("XML Load failed: " + 
                                        ex.getMessage());
                                IveApplication.printStackTrace(ex);
                                JOptionPane.showMessageDialog(null,
                                        "Problems when parsing the XML file.",
                                        "Error", 0);
                                
                                return;
                            }
                        }
                        
                        MainFrame.instance().revalidateSubwindows();
                    }
                });
            } catch (Exception ex) {
                // There should occur no exception here.
                IveApplication.printStackTrace(ex);
            }
            
            SchedulerImpl.instance().execute();
        } else {
            
            if (simulationFile != null) {
                try {
                    IveApplication.instance().loadSimulation(simulationFile);
                } catch (ClassNotFoundException ex) {
                    System.out.println(
                            "Some of the saved classes were not found. " +
                            "Make sure that your CLASSPATH system " +
                            "variable is well configured.");
                    Log.severe("Load failed: " + ex.getMessage());
                    IveApplication.printStackTrace(ex);
                    return;
                } catch (IOException ex) {
                    System.out.println(
                            "Problems when opening and reading the file.");
                    Log.severe("Load failed: " + ex.getMessage());
                    IveApplication.printStackTrace(ex);
                    return;
                }
            } else if (worldXMLFile != null) {
                try {
                    IveApplication.instance().loadWorld(worldXMLFile);
                } catch (IOException ex) {
                    System.out.println(
                            "Problems when opening and reading the file.");
                    Log.severe("XML Load failed: " + ex.getMessage());
                    IveApplication.printStackTrace(ex);
                    return;
                } catch (Exception ex) {
                    System.out.println(
                            "Problems when parsing the XML file.");
                    Log.severe("XML Load failed: " + ex.getMessage());
                    IveApplication.printStackTrace(ex);
                    return;
                }
            }
            
            long begin = 0;
            long end = 0;
            long runningTimeSeconds = 0;
            begin = System.currentTimeMillis();
            
            SchedulerImpl.instance().setTimeRatio(0.0001);
            SchedulerImpl.instance().execute(true);
            
            end = System.currentTimeMillis();
            runningTimeSeconds = (end - begin) / 1000;
            
            Log.info("Running time: " + runningTimeSeconds + " seconds.");
        }
    }
}

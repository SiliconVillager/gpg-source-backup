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
 
package cz.ive.logs;
import cz.ive.iveobject.IveObject;
import cz.ive.location.WayPoint;
import cz.ive.process.IveProcess;
import cz.ive.simulation.CalendarPlanner;
import java.util.logging.*;
import java.util.*;

/**
 * Class with static methods for logging.
 *
 * @author Zdenek
 */
public class Log {
    
    /** Logger used for logging on output */
    public static final Logger logger;
    
    /** SEVERE level of message */
    public static final int SEVERE = 0;
    
    /** WARNING level of message */
    public static final int WARNING = 1;
    
    /** INFO level of message */
    public static final int INFO = 2;
    
    /** CONFIG level of message */
    public static final int CONFIG = 3;
    
    /** FINE level of message */
    public static final int FINE = 4;
    
    /** FINER level of message */
    public static final int FINER = 5;
    
    /** FINEST level of message */
    public static final int FINEST = 6;
    
    /** true then logging to output too */
    private static boolean onOutput;
    
    /** List of receivers of messages */
    private static ArrayList<LogReceiver> receivers;
    
    /** Queue with all messages */
    private static Queue<LogMessage> messages = new LinkedList<LogMessage>();
    
    /** Actual count of messages in queue */
    private static int msgCount = 0;
    
    /** Maximum count of messages in queue */
    public static int msgMax = 1000;
    
    // static inicialization of log
    static {
        receivers = new ArrayList<LogReceiver>();
        onOutput = true;
        
        ConsoleHandler fh = new ConsoleHandler();
        fh.setFormatter(new ClearFilter());
        
        logger = Logger.getLogger("cz.ive");
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        fh.setLevel(Level.ALL);
        logger.addHandler(fh);
    }
    
    /**
     * Registers receiver of messages.
     *
     * @param receiver receiver to register
     */
    public final static void addReceiver(LogReceiver receiver) {
        synchronized (receivers) {
            receivers.add(receiver);
        }
    }
    
    /**
     * Unregisters receiver of messages.
     *
     * @param receiver registered receiver to unregister
     */
    public final static void removeReceiver(LogReceiver receiver) {
        synchronized (receivers) {
            receivers.remove(receiver);
        }
    }
    
    /**
     * Logs message.
     *
     * @param message text of message
     * @param level level of message
     * @param object objectId
     * @param process processId
     * @param waypoint waypointId
     */
    public final static void addMessage(String message, int level,
            String object, String process, String waypoint) {
        String sTime = CalendarPlanner.instance().getSimulationTimeString();
        LogNode node = new LogNode(message, level, object, process, waypoint,
                sTime);
        
        if (onOutput == true) {
            
            String sep = ", ";
            String out;
            StringBuffer bfr = new StringBuffer();
            
            bfr.append("Time:\"");
            bfr.append(sTime);
            bfr.append("\"");
            
            if (object != null && object.length() != 0) {
                bfr.append(sep);
                bfr.append("0bject:\"");
                bfr.append(object);
                bfr.append("\"");
            }
            
            if (process != null && process.length() != 0) {
                bfr.append(sep);
                bfr.append("Process:\"");
                bfr.append(process);
                bfr.append("\"");
            }
            
            if (waypoint != null && waypoint.length() != 0) {
                bfr.append(sep);
                bfr.append("Position:\"");
                bfr.append(waypoint);
                bfr.append("\"");
            }
            bfr.append(sep);
            bfr.append("Text:");
            bfr.append(message);
            
            switch (level) {
                case SEVERE:
                    out = "SEVERE "+bfr.toString();
                    logger.severe(out);
                    break;
                case WARNING:
                    out = "WARNING "+bfr.toString();
                    logger.warning(out);
                    break;
                case INFO:
                    out = "INFO "+bfr.toString();
                    logger.info(out);
                    break;
                case CONFIG:
                    out = "CONFIG "+bfr.toString();
                    logger.config(out);
                    break;
                case FINE:
                    out = "FINE "+bfr.toString();
                    logger.fine(out);
                    break;
                case FINER:
                    out = "FINER "+bfr.toString();
                    logger.finer(out);
                    break;
                case FINEST:
                    out = "FINEST "+bfr.toString();
                    logger.finest(out);
                    break;
            }
        }
        
        synchronized (receivers) {
            
        messages.offer(node);
        msgCount ++;
            for (LogReceiver receiver : receivers) {
                receiver.addMessage(node);
            }
            
            while (msgCount > msgMax) {
                LogMessage msg = messages.poll();
                for (LogReceiver receiver : receivers) {
                    receiver.recycleMessage(msg);
                }
                msgCount--;
            }
        }
    }
    
    /**
     * Logs message.
     *
     * @param message text of message
     * @param level level of message
     * @param object object
     * @param process process
     * @param wp waypoint
     */
    public static void addMessage(String message, int level,
            IveObject object, IveProcess process, WayPoint wp) {
        addMessage(message, level, object == null ? "" : object.getId(),
                process == null ? "" : process.getProcessId(),
                wp == null ? "" : wp.getId());
    }
    
    /**
     * Logs message to object.
     *
     * @param message text of message
     * @param level level of message
     * @param object objectId
     */
    public final static void addMessageToObject(String message, int level,
            String object) {
        addMessage(message, level, object, "", "");
    }
    
    /**
     * Logs message to object.
     *
     * @param message text of message
     * @param level level of message
     * @param object object
     */
    public final static void addMessageToObject(String message, int level,
            IveObject object) {
        addMessage(message, level, object == null ? "" : object.getId(), "",
                "");
    }
    
    /**
     * Logs message to process.
     *
     * @param message text of message
     * @param level level of message
     * @param process processId
     */
    public final static void addMessageToProcess(String message, int level,
            String process) {
        addMessage(message, level, "", process, "");
    }
    
    /**
     * Logs message to process.
     *
     * @param message text of message
     * @param level level of message
     * @param process process
     */
    public final static void addMessageToProcess(String message, int level,
            IveProcess process) {
        addMessage(message, level, "", process == null ? "" : process.
                getProcessId(), "");
    }
    
    /**
     * Logs message to waypoint.
     *
     * @param message text of message
     * @param level level of message
     * @param waypoint waypointId
     */
    public final static void addMessageToWaypoint(String message, int level,
            String waypoint) {
        addMessage(message, level, "", "", waypoint);
    }
    
    /**
     * Logs message to waypoint.
     *
     * @param message text of message
     * @param level level of message
     * @param waypoint waypoint
     */
    public final static void addMessageToWaypoint(String message, int level,
            WayPoint waypoint) {
        addMessage(message, level, "", "", waypoint == null ? "" : waypoint.
                getId());
    }
    
    /**
     * Turn logging on console to on
     */
    public final static void consoleOn() {
        onOutput = true;
    }
    
    /**
     * Turn logging on console to off
     */
    public final static void consoleOff() {
        onOutput = false;
    }
    
    /**
     * Logs message with CONFIG level
     *
     * @param msg message to log
     */
    public final static void config(String msg) {
        addMessage(msg, CONFIG, "", "", "");
    }
    
    /**
     * Logs message with FINE level
     *
     * @param msg message to log
     */
    public final static void fine(String msg) {
        addMessage(msg, FINE, "", "", "");
    }
    
    /**
     * Logs message with FINER level
     *
     * @param msg message to log
     */
    public final static void finer(String msg) {
        addMessage(msg, FINER, "", "", "");
    }
    
    /**
     * Logs message with FINEST level
     *
     * @param msg message to log
     */
    public final static void finest(String msg) {
        addMessage(msg, FINEST, "", "", "");
    }
    
    /**
     * Logs message with INFO level
     *
     * @param msg message to log
     */
    public final static void info(String msg) {
        addMessage(msg, INFO, "", "", "");
    }
    
    /**
     * Logs message with SEVERE level
     *
     * @param msg message to log
     */
    public final static void severe(String msg) {
        addMessage(msg, SEVERE, "", "", "");
    }
    
    /**
     * Logs message with WARNING level
     *
     * @param msg message to log
     */
    public final static void warning(String msg) {
        addMessage(msg, WARNING, "", "", "");
    }
}

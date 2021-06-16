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

import cz.ive.simulation.*;
import java.util.ArrayList;

/**
 * Basic implementation of LogMessage. 
 *
 * @author Zdenek
 */
public class LogNode implements LogMessage {
    
    /** Logged message */
    protected String message;
    
    /** objectId */
    protected String object;
    
    /** procesId */
    protected String process;
    
    /** waypointId */
    protected String waypoint;
    
    /** level of message */
    protected int type;
    
    /** String representation of the message creation s-time. */
    protected String sTimeStr;
    
    /**
     * Creates a new LogNode
     *
     * @param message text of message
     * @param type level of message
     * @param object objectId of object 
     * @param process processId of process 
     * @param waypoint waypointId of waypoint 
     * @param sTimeStr String representation of the creation s-time.
     */
    public LogNode(String message, int type, String object, String process, 
            String waypoint, String sTimeStr) {
        this.message = message;
        this.type = type;
        this.object = object;
        this.process = process;
        this.waypoint = waypoint;
        this.sTimeStr = sTimeStr + " ";
    }
}

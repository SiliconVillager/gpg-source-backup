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
 
package cz.ive.process;

import cz.ive.process.*;
import java.util.*;
/**
 *
 * @author Zdenek
 */
public class ProcessDBImpl implements ProcessDB, java.io.Serializable {
    
    /** Variable where is HashMap of processes stored by processId*/ 
    private HashMap<String,ProcessTemplate> MapByPID = null;
    
    /** Variable where is HashMap of processes stored by goalId*/
    private HashMap<String,Vector<ProcessTemplate>> MapByGID = null;
    
    /** Static member of implementation of ProcessDB*/
    private static ProcessDBImpl member;
    
    static public void setInstance(ProcessDBImpl processDBImpl) {
        member = processDBImpl;
    }
    
    /** Creates a new instance of ProcessDBimpl */       
    private ProcessDBImpl() {
        if (MapByPID == null) {
            MapByPID = new HashMap<String,ProcessTemplate>();
        }
        
        if (MapByGID == null) {
            MapByGID = new HashMap<String,Vector<ProcessTemplate>>();
        }
    }
    
    /**
     * Method which returns static instance of class
     */
    static public ProcessDBImpl instance() {
        if (member == null) {
            member = new ProcessDBImpl();            
        } 
        return member;
        
    }
    
    /** 
     * Empty whole the Process DB before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        member = new ProcessDBImpl();            
    }
    
    /**
     * Method which returns ProcessTemplate if stored in ProcessDB
     * @param processId processId of process which want to get from ProcessDB
     */
    public ProcessTemplate getByProcessId(String processId) {
       if (MapByPID == null || MapByPID.containsKey(processId) == false) {
           return null;
       } else {
           return MapByPID.get(processId);
       }
          
       
    }
    
    /**
     * Method which returns List of all ProcessTemplates stored in ProcessDB
     * which has requested goalId
     */
    public List<ProcessTemplate> getByGoalId(String goalId) {
        if (MapByGID == null || MapByGID.containsKey(goalId) == false) {
            return null;
        } else {
            return MapByGID.get(goalId);
        }
    }
    
    /**
     * Method which stores ProcessTemplate into ProcessDB by processId
     * @param processId ProcessId of ProcessTemplate
     * @param pt ProcessTemplate which we want to add into ProcessDB
     * @return true when ProcessTemplate successfully added into ProcessDB and
     *      false when not.
     */
    public boolean setByProcessId(String processId, ProcessTemplate pt) {
        if (MapByPID == null) {
            return false;
        }
        
        MapByPID.put(processId, pt);
        return true;                
    }
    
    /**
     * Method which stores ProcessTemplate into ProcessDB by goalId.
     * @param goalId goalId of ProcessTemplate
     * @param pt ProcessTemplate which we want to add into ProcessDB
     * @return true when ProcessTemplate successfully added into ProcessDB and
     *      false when not.
     */
    public boolean setByGoalId(String goalId, ProcessTemplate pt) {
        if (MapByGID == null) {
            return false;
        }
        
        Vector<ProcessTemplate> ProcessList;
        
        if (MapByGID.containsKey(goalId) == true) {
            ProcessList = MapByGID.get(goalId);
        } else {
            ProcessList = new Vector<ProcessTemplate>();                      
        }
        ProcessList.add(pt);
        MapByGID.put(goalId, ProcessList);
        
        return true;
    }
    
    /**
     * Method which stores ProcessTemplate into ProcessDB by goalId and by processId.
     * @param processId processId of ProcessTemplate
     * @param goalId goalId of ProcessTemplate
     * @param pt ProcessTemplate which we want to add into ProcessDB
     * @return true when ProcessTemplate successfully added into ProcessDB and
     *      false when not.
     */
    public boolean setProcess(String processId, String goalId, ProcessTemplate pt) {
        if (MapByPID == null || MapByGID == null) {
            return false;
        }
        
        MapByPID.put(processId, pt);
        Vector<ProcessTemplate> ProcessList;
        
        if (MapByGID.containsKey(goalId) == true) {
            ProcessList = MapByGID.get(goalId);
        } else {
            ProcessList = new Vector<ProcessTemplate>();                      
        }
        
        ProcessList.add(pt);
        MapByGID.put(goalId, ProcessList);
        
        return true;
    }
}


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

import java.io.Serializable;
import java.util.*;
import cz.ive.simulation.CalendarPlanner;
/**
 * Embodies process execution. ProcessTemplate can store information
 * needed per process execution here.
 * @author pavel
 */
public class ProcessExecutionImpl implements ProcessExecution, Serializable {
    
    /** Process template of this process */
    private ProcessTemplate template;
    
    /** Phantoms associated with this process */
    private Substitution phantoms;
    
    /** Objects associated with this process */
    private Substitution objects;
    
    /** Process parameters */
    private Map<String, Object> parameters;
    
    /** Start time of this process */
    protected long startTime;
    
    /** Last commit time  of this process */
    protected long lastCommitTime;
    
    /** Process that was used to create this execution */
    protected IveProcess process;
    
    /** 
     * Creates a new instance of ProcessExecutionImpl 
     *
     * @param template ProcessTemplate
     */
    public ProcessExecutionImpl(ProcessTemplate template, IveProcess process) {
        this.template = template;
        this.process = process;
        this.phantoms = process.getSubstitution();
        this.parameters = process.getParameters();
    }
    
    /**
     * Getter for associated ProcessTemplate
     * @return template of this ProcessExecution
     */
    public ProcessTemplate getTemplate() {
        return template;
    }
    
    /**
     * Retrieves phantoms substitution of sources.
     * That is substitution made by genius.
     * @return phantom substitution
     */
    public Substitution getPhantoms() {
        return phantoms;
    }
    
    /**
     * Called to set up phantom substitution of sources.
     * That is substitution made by genius.
     * @param phantoms phantom substitution
     */
    public void setPhantoms(Substitution phantoms) {
        this.phantoms = phantoms;
    }
    
    /**
     * Retrieves real-object substitution of sources.
     * That is substitution made by interpreter. Ale changes made by a process
     * during its commitment should be made on real objects.
     * @return real-object substitution
     */
    public Substitution getObjects() {
        return objects;
    }
    
    /**
     * Sets real-object substitution of sources.
     * That is substitution made by interpreter. Ale changes made by a process
     * during its commitment should be made on real objects.
     */
    public void setObjects(Substitution objects) {
        this.objects = objects;
    }
    
    /**
     * Retrieves parameters of this process.
     * Parameters are process specific data that elaborates meaning of the
     * process. It is used to distinguish between slightly different
     * processes from one process familly (for example speech)
     * @return parameters of this process execution
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    /**
     * Sets parameters for this process.
     * Parameters are process specific data that elaborates meaning of the
     * process. It is used to distinguish between slightly different
     * processes from one process familly (for example speech)
     * @param parameters Map of parameters for this process execution
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    /**
     * Setter for starting time of this process
     * @param time time when was this process executed
     */
    public void setStartTime(long time) {
        startTime = time;
        lastCommitTime = time;
    }
    
    /**
     * Getter for starting time of this process
     * @return time when was this process executed
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Getter for duration of this process execution
     * @return duration from start time till now
     */
    public long getDuration() {
        
        long currentTime;
        currentTime = CalendarPlanner.instance().getSimulationTime();
        
        return currentTime - startTime;
    }
    
    /**
     * Setter for starting time of this process
     * @param time time when was this process executed
     */
    public void setLastCommitTime(long time) {
        lastCommitTime = time;
    }
    
    /**
     * Getter for starting time of this process
     * @return time of last partial commit
     */
    public long getLastCommitTime() {
        return lastCommitTime;
    }
    
    /**
     * Getter for time elapsed since last partial commit has been performed
     * @return time since last partial commit
     */
    public long getTimeSinceLastCommit() {
        
        long currentTime;
        currentTime = CalendarPlanner.instance().getSimulationTime();
        
        return currentTime - lastCommitTime;
    }
    
    /**
     * Retrieves IveProcess associated with this execution.
     *
     * @return IveProcess which was used to create this ProcessExecution.
     */
    public IveProcess getProcess() {
        return process;
    }
    
    /**
     * Sets the process that was used to create this execution.
     *
     * @param process IveProcess associated with this execution.
     */
    public void setProcess(IveProcess process) {
        this.process = process;
    }
}

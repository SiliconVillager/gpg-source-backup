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

import java.util.*;

/**
 * Embodies process execution. ProcessTemplate can store information
 * needed per process execution here.
 *
 * @author Ondra
 */
public interface ProcessExecution {
    
    /**
     * Getter for associated ProcessTemplate
     * @return template of this ProcessExecution
     */
    ProcessTemplate getTemplate();
    
    /**
     * Retrieves phantoms substitution of sources.
     * That is substitution made by genius.
     * @return phantom substitution
     */
    Substitution getPhantoms();
    
    /**
     * Called to set up phantom substitution of sources.
     * That is substitution made by genius.
     * @param phantoms phantom substitution
     */
    void setPhantoms(Substitution phantoms);
    
    /**
     * Retrieves real-object substitution of sources.
     * That is substitution made by interpreter. All changes made by a process
     * during its commitment should be made on real objects.
     * @return real-object substitution
     */
    Substitution getObjects();
    
    /**
     * Sets real-object substitution of sources.
     * That is substitution made by interpreter. All changes made by a process
     * during its commitment should be made on real objects.
     */
    void setObjects(Substitution objects);
    
    /**
     * Retrieves parameters of this process.
     * Parameters are process specific data that elaborates meaning of the
     * process. It is used to distinguish between slightly different 
     * processes from one process familly (for example speech)
     * @return parameters of this process execution
     */
    Map<String, Object> getParameters();
    
    /**
     * Sets parameters for this process.
     * Parameters are process specific data that elaborates meaning of the
     * process. It is used to distinguish between slightly different 
     * processes from one process familly (for example speech)
     * @param parameters Map of parameters for this process execution
     */
    void setParameters(Map<String, Object> parameters);
    
    /**
     * Retrieves IveProcess associated with this execution.
     * 
     * @return IveProcess which was used to create this ProcessExecution.
     */
    IveProcess getProcess();
    
    /**
     * Sets the process that was used to create this execution.
     * 
     * @param process IveProcess associated with this execution.
     */
    void setProcess(IveProcess process);
    
    /**
     * Setter for starting time of this process
     * @param time time when was this process executed
     */
    void setStartTime(long time);
    
    /**
     * Getter for starting time of this process
     * @return time when was this process executed
     */
    long getStartTime();
    
    /**
     * Getter for duration of this process execution
     * @return duration from start time till now
     */
    long getDuration();

    /**
     * Setter for last partial commit time of this process
     * @param time time when was this process executed
     */
    void setLastCommitTime(long time);
    
    /**
     * Getter for last partial commit time of this process
     * @return time of last partial commit
     */
    long getLastCommitTime();

    /**
     * Getter for time elapsed since last partial commit has been performed
     * @return time since last partial commit
     */
    long getTimeSinceLastCommit();
}
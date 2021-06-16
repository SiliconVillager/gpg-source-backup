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

import cz.ive.ontology.*;
import cz.ive.sensors.*;
import cz.ive.trigger.*;
import cz.ive.lod.*;
import java.util.*;

/**
 * Interface for a process template. <code>ProcessTemplate</code> manages all
 * information about process for both Genius and Interpreter.
 * <code>ProcessTemplate</code> is one for a particular process available
 * in the system. Whenever Genius wants to run a process it has to get 
 * an instance of <code>Process</code> by chosing apropriate
 * <code>ProcessTemplate</code> and calling <code>instantiate</code> with
 * correctly filled <code>Substitution</code> as a parameter.
 *
 * @author  Ondra
 */
public interface ProcessTemplate extends OntologyToken {
    
    /**
     * Getter for LOD in which this process is atomic.
     * @param execution information needed to execute the process
     * @return LOD in which is this process treated as atomic
     */
    LOD getLOD(ProcessExecution execution);
    
    /**
     * Getter for p-suitability
     * @return p-suitability TriggerTemplate of this process
     */
    TriggerTemplate getSuitability();
    
    /**
     * Getter for p-context
     * @return p-context TriggerTemplate of this process
     */
    TriggerTemplate getContext();
    
    /**
     * Creates Process representing this template and given
     * Substitution.
     * @param sources Substitution of sources to be associated with newly 
     *          created process.
     * @param parameters Map of process parameters
     * @param sensors to be used to a Context queries
     * @return instantiated Process
     */
    IveProcess instantiate(Substitution sources, 
            Map<String, Object> parameters, List<Sensor> sensors);
    
    /**
     * Creates all necessary process specific data for its execution.
     * @param process Instantiated process to be executed
     * @return process specific implementation of ProcessExecution
     */
    ProcessExecution execute(IveProcess process);
    
    /**
     * Retrieve sources necessary for running the process.
     * All source entries are expected not to be assigned to any objects.
     * Only process's asumtions are filled in (source ID, type, obligatory...)
     * @return Substitution
     */
    Substitution getSources();
    
    /**
     * Retrives process specific expansion to subgoals, their g-triggers,
     * g-contexts, substitutions, priorities and maybe more...
     * @return process subgoal expansion as array of Goals
     */
    OntologyToken getExpansion(Substitution sources, Map<String, Object> parameters);

    /**
     * Evaluates partial results of running process. The atomicLength
     * should be called if the process is still running after an atomicCommit
     * call.
     * This method may be called at random time. No ProcessTemplate
     * implementation should expect any relation whith value returned
     * by atomicLength function. So the implementation is assumed to be
     * foolproof as much as possible.
     * @param execution information needed to execute the process
     * @return result of the process execution
     */
    ProcessResult atomicCommit(ProcessExecution execution);
    
    /**
     * Makes an estimate of the process remaining run time.
     * @param execution information needed to execute the process
     * @return process run time estimate
     */
    long atomicLength(ProcessExecution execution);
    
    /**
     * Called to stop the running process. This may not be an instant.
     * If the process is still running, than the atomicLength should be called
     * to retrieve updated run time estimate.
     * @param execution information needed to execute the process
     * @return result of the process execution
     */
    ProcessResult atomicStop(ProcessExecution execution);
    
    /**
     * LOD has been increased so that the process is no more atomic.
     * It should commit itself as necessary.
     * @param execution information needed to execute the process
     * @return result of the process execution
     */
    ProcessResult increaseLOD(ProcessExecution execution);

    /**
     * LOD has been decreased so that the process ceases to exist
     * It should commit itself as necessary.
     * @param execution information needed to execute the process     
     */
    void decreaseLOD(ProcessExecution execution);
    
    /**
     * Indicates whether this process is delegated or not.
     * @return True when the process is delegated; false in any other case.
     */
    boolean isDelegated();
    
    /**
     * Is this template for a rendezvous process? Interpreter acts as usual, but
     * also waits if the source "who" became an actor of the same process with 
     * the current actor substituted to the "who" source of the second process. 
     * If this happens, the iterpreter calls method rendezvous(two 
     * processes that have successfully met) on the iface 
     * RendezvousProcesTemplate.
     *
     * @return <code>true</code> if this is template for the rendezvous process.
     */
    boolean isRendezvous();
    
    String getId();
}

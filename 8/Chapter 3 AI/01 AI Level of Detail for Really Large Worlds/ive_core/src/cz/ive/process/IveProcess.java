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
import cz.ive.messaging.*;
import cz.ive.trigger.*;
import cz.ive.ontology.*;
import cz.ive.exception.ProcessNotRunningException;

/**
 * Instantiated process. Contains <code>Substitution</code> of sources.
 *
 * @author  Ondra
 */
public interface IveProcess {
    
    /**
     * Getter for represented process id
     * @return process id of this process
     */
    String getProcessId();
    
    /**
     * Getter for represented process goal id
     * @return goal id of this process
     */
    String getGoalId();
    
    /**
     * Getter for contained Substitution
     * @return Substitution associated withe this process
     */
    Substitution getSubstitution();
    
    /**
     * Getter for contained process parameters
     * @return Parameters of this process
     */
    Map<String, Object> getParameters();
    
    /**
     * Begin execution of this process
     */
    void execute();
    
    /**
     * Stops the process. This does not mean, that the process is stopped after
     * return from this function. The process is stopped when it signals
     * on the FinishHook. This may (but does not have to) occur during
     * execution of this method.
     */
    void stop() throws ProcessNotRunningException;
    
    /**
     * Retrieves Hook which is signaled when the process finishes.
     * @return process finish hook
     */
    FinishHook getFinishHook();
    
    /**
     * Retrieves Hook which is signaled when the process has to be expanded.
     * When the process becomes non-atomic due to a LOD change.
     * @return process expand hook
     */
    Hook getExpandHook();
    
    /**
     * Retrieves Hook which is signaled when the process has to be shrunk.
     * When the process becomes atomic due to a LOD change.
     * @return process shrink hook
     */
    Hook getShrinkHook();
    
    /**
     * Getter for p-context
     * @return instantiated p-context Trigger of this process
     */
    Trigger getContext();
    
    /**
     * Retrives process specific expansion to subgoals, their g-triggers,
     * g-contexts, substitutions, priorities and maybe more...
     * Order of goals in array is given by its priorities
     * @return process subgoal expansion as array of Goals
     */
    OntologyToken getExpansion();
    
    /**
     * Called by an Interpreter when the process finishes
     * @param result ProcessResult of this process execution
     */
    void finish(ProcessResult result);
    
    /**
     * Called by an Interpreter when the process should be expanded.
     * When the process becames non-atomic due to a LOD change.
     */
    void expand();
    
    /**
     * Called by an Interpreter when the process should be shrunk.
     * When the process becames atomic due to a LOD change.
     */
    void shrink();
}

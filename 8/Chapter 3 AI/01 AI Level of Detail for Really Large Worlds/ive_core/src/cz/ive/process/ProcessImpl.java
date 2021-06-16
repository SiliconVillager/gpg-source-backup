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
import cz.ive.messaging.Hook;
import cz.ive.messaging.FinishHook;
import cz.ive.messaging.SyncHook;
import cz.ive.trigger.Trigger;
import cz.ive.ontology.OntologyToken;
import cz.ive.simulation.Interpreter;
import cz.ive.simulation.WorldInterpreter;
import cz.ive.exception.ProcessNotRunningException;
import cz.ive.exception.DelegationFailedException;
import cz.ive.exception.StopDelegationFailedException;

/**
 * Implements an istantiated process
 * @author pavel
 */
public class ProcessImpl implements IveProcess, Serializable {
    
    /**
     * A class for the expand and shrink hooks of the process
     */
    protected class ProcessHook extends SyncHook {
        
        /** Creates a new instance of ProcessHook */
        public ProcessHook() {
        }
        
        /** Pulls legs of all registered listeners */
        protected void pullLegs() {
            notifyListeners();
        }
    }
    
    /**
     * A class for the finish hook of the process
     */
    protected class ProcessFinishHook extends ProcessHook
            implements FinishHook {
        
        /** A process result */
        private ProcessResult result;
        
        /** Creates a new instance of ProcessFinishHook */
        public ProcessFinishHook() {
        }
        
        /**
         * Sets the process result value
         * @param result The result of the process
         */
        public void setResult(ProcessResult result) {
            this.result = result;
        }
        
        /**
         * Retrieves the process result value
         * @return process result state
         */
        public ProcessResult getResult() {
            return result;
        }
        
    }
    
    /** ID of this process */
    private String processId;
    
    /** ID of the goal realised by this process */
    private String goalId;
    
    /** Finish hook of this process */
    protected ProcessFinishHook finishHook;
    
    /** Expand hook of this process */
    protected ProcessHook expandHook;
    
    /** Shrink hook of this process */
    protected ProcessHook shrinkHook;
    
    /** Substitution associated with this process */
    protected Substitution substitution;
    
    /** Map of parameters for this process */
    protected Map<String, Object> parameters;
    
    /** p-context of this process */
    protected Trigger pContext;
    
    /** Creates a new instance of ProcessImpl */
    public ProcessImpl(String processId, String goalId,
            Substitution substitution, Map<String, Object> parameters,
            Trigger pContext) {
        this.processId = processId;
        this.goalId = goalId;
        this.substitution = substitution;
        this.parameters = parameters;
        this.pContext = pContext;
        
        finishHook = new ProcessFinishHook();
        expandHook = new ProcessHook();
        shrinkHook = new ProcessHook();
    }
    
    /**
     * Getter for represented process id
     * @return process id of this process
     */
    public String getProcessId() {
        return processId;
    }
    
    /**
     * Getter for represented process goal id
     * @return goal id of this process
     */
    public String getGoalId() {
        return goalId;
    }
    
    /**
     * Getter for contained Substitution
     * @return Substitution associated withe this process
     */
    public Substitution getSubstitution() {
        return substitution;
    }
    
    /**
     * Getter for contained process parameters
     * @return Parameters of this process
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    /**
     * The process here is either delegated or executed on the interpeter.
     */
    public void execute() {
        Interpreter interpreter;
        ProcessDB processDB = ProcessDBImpl.instance();
        ProcessTemplate template = processDB.getByProcessId(processId);
        
        interpreter = WorldInterpreter.instance();
        interpreter.execute(this);
    }
    
    /**
     * Stops the process. This does not mean, that the process is stopped after
     * return from this function. The process is stopped when it signals
     * on the FinishHook. This may (but does not have to) occur during
     * execution of this method.
     */
    public void stop() throws ProcessNotRunningException {
        Interpreter interpreter;
        ProcessDB processDB = ProcessDBImpl.instance();
        ProcessTemplate template = processDB.getByProcessId(processId);
        
        interpreter = WorldInterpreter.instance();
        interpreter.stop(this);
    }
    
    /**
     * Retrieves Hook which is signaled when the process finishes.
     * @return process finish hook
     */
    public FinishHook getFinishHook() {
        return finishHook;
    }
    
    /**
     * Retrieves Hook which is signaled when the process has to be expanded.
     * When the process becomes non-atomic due to a LOD change.
     * @return process expand hook
     */
    public Hook getExpandHook() {
        return expandHook;
    }
    
    /**
     * Retrieves Hook which is signaled when the process has to be shrunk.
     * When the process becomes atomic due to a LOD change.
     * @return process shrink hook
     */
    public Hook getShrinkHook() {
        return shrinkHook;
    }
    
    /**
     * Getter for p-context
     * @return instantiated p-context Trigger of this process
     */
    public Trigger getContext() {
        return pContext;
    }
    
    /**
     * Retrives process specific expansion to subgoals, their g-triggers,
     * g-contexts, substitutions, priorities and maybe more...
     * @return process subgoal expansion as an ontology-driven object
     */
    public OntologyToken getExpansion() {
        
        ProcessDB processDB;
        ProcessTemplate template;
        
        processDB = ProcessDBImpl.instance();
        
        template = processDB.getByProcessId(processId);
        
        return template.getExpansion(substitution, parameters);
    }
    
    /**
     * Called by an Interpreter when the process finishes
     * @param result ProcessResult of this process execution
     */
    public void finish(ProcessResult result) {
        finishHook.setResult(result);
        finishHook.pullLegs();
    }
    
    /**
     * Called by an Interpreter when the process should be expanded.
     * When the process becames non-atomic due to a LOD change.
     */
    public void expand() {
        expandHook.pullLegs();
    }
    
    /**
     * Called by an Interpreter when the process should be shrunk.
     * When the process becames atomic due to a LOD change.
     */
    public void shrink() {
        shrinkHook.pullLegs();
    }
}

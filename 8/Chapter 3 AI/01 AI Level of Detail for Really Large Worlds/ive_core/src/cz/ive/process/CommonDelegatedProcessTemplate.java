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

import cz.ive.genius.*;
import cz.ive.iveobject.*;
import cz.ive.trigger.*;
import cz.ive.lod.LOD;
import cz.ive.logs.Log;
import cz.ive.ontology.OntologyToken;

/**
 * Common template for delegated processes. All delegated processes are supposed
 * to extend this template.
 *
 * @author honza
 */
public class CommonDelegatedProcessTemplate extends CommonProcessTemplate
        implements DelegatedProcessTemplate, java.io.Serializable {
    
    /**
     * Map between process executions and the area geniuses that are associted
     * to them.
     */
    protected Map<ProcessExecution, AreaGenius> executions =
            new HashMap<ProcessExecution, AreaGenius>();
    
    /** Map between area ids and the associated geniuses */
    protected Map<String, AreaGenius> registrations =
            new HashMap<String, AreaGenius>();
    
    /**
     * Suitability template. It returns true, whenever there is a genius
     * registered for the location in which the executing actor is situated
     */
    TriggerTemplate delegatedProcessSuitability;
    
    /** Creates a new instance of CommonDelegateProcessTemplate */
    public CommonDelegatedProcessTemplate()  {
    }
    
    public void initMembers(
            String goalId,
            String processId,
            Substitution sources,
            ExpansionProducer expansionCrt,
            TriggerTemplate suitability,
            TriggerTemplate pContext,
            LOD lod) {
        super.initMembers(goalId, processId, sources, expansionCrt, suitability,
                pContext, lod);
        delegatedProcessSuitability =
                new DelegationProcessSuitabilityTemplate(registrations,
                suitability);
    }
    
    public TriggerTemplate getSuitability() {
        return delegatedProcessSuitability;
    }
    
    public void register(AreaGenius genius, String areaId) {
        if (registrations.containsKey(areaId)) {
            Log.addMessage("More than one area geniuses tried to register on " +
                    "the same process in the same area.", Log.WARNING,
                    genius.getId(), processId, "");
            return;
        }
        registrations.put(areaId, genius);
    };
    
    public void unregister(AreaGenius genius, String areaId) {
        AreaGenius oldGenius = registrations.get(areaId);
        
        if (oldGenius == genius) {
            registrations.remove(areaId);
        } else {
            Log.addMessage("Tried to unregister genius that was not the one " +
                    "registered for this process in this area.", Log.WARNING,
                    genius.getId(), processId, "");
        }
    };
    
    /**
     * Finds a genius registered to the given process at the area of the actor.
     *
     * @param actor Actor to be delegated.
     */
    public AreaGenius findGenius(IveObject actor) {
        IveId position = actor.getPosition();
        
        while (position != null) {
            AreaGenius genius = registrations.get(position.getId());
            
            if (genius != null)
                return genius;
            
            position = position.getParent();
        }
        return null;
    }
    
    public long atomicLength(ProcessExecution execution) {
        if (!executions.containsKey(execution))
            return 1;
        return 60*60*1000;
    };
    
    public ProcessResult atomicCommit(ProcessExecution execution) {
        if (executions.containsKey(execution))
            return ProcessResult.RUNNING;
        
        // Start the delegation.
        HashMap<String, Slot> actors =
                execution.getObjects().getActorSlots();
        
        Source src = actors.size() != 1 ? null :
            actors.entrySet().iterator().next().getValue().getSource();
        IveObject actor = src == null ? null : src.getObject();
        
        if (actor == null){
            Log.addMessage("Delegated process fired with wrong number of " +
                    "actors. We expect exactly one.", Log.WARNING, "",
                    processId, "");
            return ProcessResult.FAILED;
        }
        
        AreaGenius genius = findGenius(actor);
        if (genius == null) {
            Log.addMessage("No genius is registered for the actor's location.",
                    Log.WARNING, "", processId, "");
            return ProcessResult.FAILED;
        }
        
        ProcessResult rs = genius.startDelegation(actor, execution);
        
        // We were succesfull.
        if (rs == ProcessResult.RUNNING) {
            executions.put(execution, genius);
        }
        return rs;
    };
    
    public ProcessResult atomicStop(ProcessExecution execution)  {
        // No notion of the delegation...that is no problem, we may try to stop
        // it before its initialization.
        if (!executions.containsKey(execution))
            return ProcessResult.OK;
        
        // Stop the delegation
        AreaGenius genius = executions.get(execution);
        ProcessResult rs = genius.stopDelegation(execution);
        
        // We were immediately succesfull.
        if (!rs.isRunning()) {
            executions.remove(execution);
        }
        return rs;
    };
    
    public ProcessResult increaseLOD(ProcessExecution execution) {
        AreaGenius genius = executions.get(execution);
        
        if (genius == null) {
            Log.addMessage("Delegated process was asked to increaseLod(), but " +
                    "the execution used is not registered.", Log.WARNING, "",
                    processId, "");
            return ProcessResult.FAILED;
        }
        
        genius.interruptDelegation(execution);
        executions.remove(execution);
        return ProcessResult.LOD_TOO_HIGH;
    }
    
    public void decreaseLOD(ProcessExecution execution) {
        increaseLOD(execution);
    }
    
    public OntologyToken getExpansion(Substitution sources,
            Map<String, Object> parameters){
        return null;
        
    }
    
    public boolean isDelegated() {
        return true;
    }
}

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
 
package cz.ive.genius;

import cz.ive.IveApplication;
import cz.ive.exception.OntologyNotSupportedException;
import cz.ive.exception.ProcessNotRunningException;
import cz.ive.genius.goaltree.*;
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.ObjectMap;
import cz.ive.iveobject.attributes.AttrObject;
import cz.ive.iveobject.attributes.AttributeValue;
import cz.ive.logs.Log;
import cz.ive.ontology.OntologyToken;
import java.util.*;

import cz.ive.process.*;
import cz.ive.sensors.Sensor;
import cz.ive.trigger.TriggerTemplate;
import cz.ive.util.Pair;
import cz.ive.valueholders.FuzzyValueHolder;

/**
 * Basic implementation of the area genius. It is still expected to be overidden
 * to support specific behavior.
 *
 * @author ondra
 */
public class AreaGeniusImpl extends BasicGenius implements AreaGenius {
    
    /** Id of the associated location */
    protected String areaId;
    
    /**
     * Delegation process table. It is a map from processId of the
     * DelegatedProcessTemplates to the goalId of the goals to be fullfilled
     * for the given process.
     */
    protected Map<String, String> processTable = new HashMap<String, String>();
    
    /**
     * Map between delegated processes executions and their info.
     */
    protected Map<ProcessExecution, AcceptedGoal> acceptedTableByProcess =
            new HashMap<ProcessExecution, AcceptedGoal>();
    
    /**
     * Map between top level goal rules and their info.
     */
    protected Map<RuleTreeNode, AcceptedGoal> acceptedTableByRule =
            new HashMap<RuleTreeNode, AcceptedGoal>();
    
    /**
     * Map between actors' ids and their info.
     */
    protected Map<String, AcceptedGoal> acceptedTableByActor =
            new HashMap<String, AcceptedGoal>();
    
    /**
     * Set of the goals (goalId) to be assigned manually, by the call to the
     * overiden method selectSpecialProcess().
     */
    protected Set<String> specialGoals = new HashSet<String>();
    
    /**
     * List of goals that can be executed before stoping delegated process.
     */
    protected List<Pair<TriggerTemplate,String>> cleaningGoals;
    
    
    /**
     * Creates a new instance of AreaGeniusImpl
     *
     * @param id Id to be assigned to the new genius.
     * @param areaId Id of the area assigned.
     */
    public AreaGeniusImpl(String id, String areaId) {
        super(id);
        this.areaId = areaId;
    }
    
    public void activate()  {
        if (activated)
            return;
        
        for (Map.Entry<String, String> entry : processTable.entrySet()) {
            ProcessTemplate template =
                    ProcessDBImpl.instance().getByProcessId(entry.getKey());
            
            if (template == null || !(template instanceof DelegatedProcessTemplate)) {
                Log.addMessage("No ProcessTemplate for the \""+entry.getKey()+
                        "\" processId to be delegated.", Log.WARNING, id,
                        entry.getKey(), "");
                continue;
            }
            ((DelegatedProcessTemplate)template).register(this, areaId);
        }
        super.activate();
    }
    
    /**
     * All goals and processes are deactivated. All hooks and all connection to
     * the world is disposed. Also all passive queries should be unregistered
     * here.
     */
    public void deactivate()  {
        if (!activated)
            return;
        
        for (Map.Entry<String, String> entry : processTable.entrySet()) {
            ProcessTemplate template =
                    ProcessDBImpl.instance().getByProcessId(entry.getKey());
            
            if (template == null || !(template instanceof DelegatedProcessTemplate)) {
                Log.addMessage("No ProcessTemplate for the \""+entry.getKey()+
                        "\" processId to be delegated.", Log.WARNING, id,
                        entry.getKey(), "");
                continue;
            }
            ((DelegatedProcessTemplate)template).unregister(this, areaId);
        }
        super.deactivate();
    }
    
    public void setDelegationTable(Map<String, String> table) {
        this.processTable = table;
    }
    
    public ProcessResult startDelegation(IveObject actor, ProcessExecution execution) {
        String goalId = processTable.get(execution.getTemplate().getId());
        
        if (goalId == null) {
            Log.addMessage("Delegated process not in the process table",
                    Log.WARNING, id, execution.getTemplate().getId(),
                    "");
            return ProcessResult.FAILED;
        }
        TopLevelGoal goal = new TopLevelGoal(goalId);
        addSource(new SourceImpl(actor));
        goal.setSubstitution(
                execution.getPhantoms().duplicateSubstitution(true));
        
        RuleTreeNode rule = addGoalInternal(goal, null);
        topLevelGoals.add(rule);
        AcceptedGoal info = new AcceptedGoal(actor, execution, rule);
        acceptedTableByProcess.put(execution, info);
        acceptedTableByRule.put(rule, info);
        acceptedTableByActor.put(actor.getId(), info);
        
        return ProcessResult.RUNNING;
    }
    
    public ProcessResult stopDelegation(ProcessExecution execution) {
        AcceptedGoal info = acceptedTableByProcess.get(execution);
        
        if (info == null) {
            Log.addMessage("Stopping proces that was not accepted.",
                    Log.WARNING, id, execution.getTemplate().getId(), "");
            return ProcessResult.FAILED;
        }
        
        RuleTreeNode rule = info.rule;
        
        // No process associted, we are free to finish the delegation.
        if (!rule.getRuleState().isProcessAssocited()) {
            rule.deactivateRule();
            topLevelGoals.remove(rule);
            dirtyNodes.remove(rule);
            removeSource(new SourceImpl(info.actor));
            acceptedTableByProcess.remove(execution);
            acceptedTableByRule.remove(rule);
            acceptedTableByActor.remove(info.actor.getId());
            
            return ProcessResult.OK;
        }
        
        // The stopDelegation was called before, but we are still running.
        if (info.shouldStop) {
            return ProcessResult.RUNNING;
        }
        
        // This is the first call of this method, try to stop the process.
        info.shouldStop = true;
        if (rule.getRuleState() == RuleState.PROCESS_RUNNING) {
            rule.stopProcess();
        }
        return ProcessResult.RUNNING;
    }
    
    public void interruptDelegation(ProcessExecution execution) {
        AcceptedGoal info = acceptedTableByProcess.get(execution);
        
        if (info == null) {
            Log.addMessage("Interrupting proces that was not accepted.",
                    Log.WARNING, id, execution.getTemplate().getId(), "");
            return;
        }
        
        RuleTreeNode rule = info.rule;
        
        rule.deactivateRule();
        dirtyNodes.remove(rule);
        topLevelGoals.remove(rule);
        removeSource(new SourceImpl(info.actor));
        acceptedTableByProcess.remove(execution);
        acceptedTableByRule.remove(rule);
        acceptedTableByActor.remove(info.actor.getId());
    }
    
    protected void evaluateTopLevelRule(RuleTreeNode rule) {
        RuleTreeNode running = null;
        RuleTreeNode stopping = null;
        RuleTreeNode shouldRun = null;
        RuleState state = rule.getRuleState();
        AcceptedGoal goal = acceptedTableByRule.get(rule);
        
        if (rule.getGTriggerValue() == FuzzyValueHolder.True) {
            if (state != RuleState.PROCESS_STOPPING)
                shouldRun = rule;
        }
        
        if (state == RuleState.PROCESS_RUNNING) {
            running = rule;
        } else if (state == RuleState.PROCESS_STOPPING) {
            stopping = rule;
        } else if (state == RuleState.PROCESS_FINISHED) {
            Log.addMessage("Process for goal "+rule.getGoal().getGoalID()+
                    " finished with result: "+rule.acceptProcessResult(),
                    Log.FINEST, id, rule.getGoal().getGoalID(), "");
            
            if (goal.shouldStop) {
                String cleanGoalId = shouldClean(goal);
                // Should we execute the cleaning proces?
                if (cleanGoalId != null &&
                        goal.cleaning_count < RuleTreeNode.PANIC_CYCLES) {
                    Log.addMessage("Running the cleaning process",
                            Log.CONFIG, id, "", "");
                    runCleaning(goal, cleanGoalId);
                    return;
                } else {
                    // Ask the interpreter to stop the process, now we know,
                    // that it will stop immediately.
                    try {
                        goal.execution.getProcess().stop();
                    } catch (ProcessNotRunningException ex) {
                        IveApplication.printStackTrace(ex);
                        Log.addMessage("We are stopping the main delegated " +
                                "process, but the interpreter does not know" +
                                "about it.", Log.SEVERE, id, "", "");
                    }
                    return;
                }
            }
        }
        
        // If there is no running or stopping process, check whether we do not
        // want to stop the delegation.
        if (goal.shouldStop && running == null && stopping == null &&
                shouldClean(goal) == null) {
            // Ask the interpreter to stop the process, now we know,
            // that it will stop immediately.
            try {
                goal.execution.getProcess().stop();
            } catch (ProcessNotRunningException ex) {
                IveApplication.printStackTrace(ex);
                Log.addMessage("We are stopping the main delegated " +
                        "process, but the interpreter does not know" +
                        "about it.", Log.SEVERE, id, "", "");
            }
            return;
        }
        
        if (!ruleJudgement(shouldRun, running, stopping)) {
            Log.addMessage("We should stop the top level goal, " +
                    "this look like a bad world design.",
                    Log.WARNING, id, rule.getGoal().getGoalID(), "");
        }
    }
    
    protected boolean runProcess(RuleTreeNode rule) {
        Log.addMessage("Running the process for goal: "+rule.getGoal().getGoalID(),
                Log.INFO, id, rule.getGoal().getGoalID(), "");
        if (rule.getGContextValue() != FuzzyValueHolder.True) {
            Log.addMessage("Goal context is not true. The process cannot be " +
                    "executed", Log.WARNING, id, rule.getGoal().getGoalID(),
                    "");
            return false;
        }
        
        Set<String> preferSet = null;
        if (specialGoals.contains(rule.getGoal().getGoalID())) {
            preferSet = selectSpecialProcess(rule);
        }
        
        Set<String> failedProcesses = rule.getFailedProcesses();
        Pair<ProcessTemplate, Substitution> chosenPair =
                p_decider.chooseProcess(rule.getGoal(), sources,
                failedProcesses, preferSet);
        
        if (chosenPair.first() == null) {
            failedProcesses.clear();
            chosenPair = p_decider.chooseProcess(rule.getGoal(),
                    sources, failedProcesses, null);
            
            if (chosenPair.first() == null) {
                Log.addMessage("There's no process (or no with \"true "
                        + "suitability\") for "
                        + rule.getGoal().getGoalID() + " goal.",
                        Log.WARNING, id, rule.getGoal().getGoalID(),"");
                return false;
            }
        }
        
        rule.executeProcess(chosenPair.first(), chosenPair.second());
        return true;
    }
    
    /**
     * Retrieves info that is associated with the toplevel rule that is
     * ancestor of the given rule.
     *
     * @param rule some rule in the rule tree hierarchy.
     * @return info about accepted delegated goal
     */
    protected AcceptedGoal getInfo(RuleTreeNode rule) {
        while (rule.getParent() != null) {
            rule = rule.getParent();
        }
        return acceptedTableByRule.get(rule);
    }
    
    public void addSource(Source source) {
        boolean changed = false;
        IveObject obj = source.getObject();
        sources.add(obj);
        Log.addMessage("Added ACTOR source: "+obj.getId(),
                Log.FINEST, id, "", "");
        
        IveObject area = ObjectMap.instance().getObject(areaId);
        int areaLod = area.getLod();
        
        Set<AttributeValue> attrs = obj.getAllAtributes();
        for (AttributeValue value : attrs) {
            
            if (value.getName().indexOf("sensors.") >= 0) {
                Sensor sensor = (Sensor)((AttrObject)value).getValue();
                
                if (sensor.getLOD().getMax() <= areaLod)
                    continue;
                
                Log.addMessage("Added sensor: "+sensor.getId(),
                        Log.FINEST, id, "", "");
                sensors.add(sensor);
                changed = true;
            }
        }
        
        if (changed) {
            phantomKeeper.changeSensors(sensors);
            triggerKeeper.changeSensors(sensors);
        }
        
        actorGained(obj);
    }
    
    public void removeSource(Source source) {
        boolean changed = false;
        IveObject obj = source.getObject();
        sources.remove(obj);
        Log.addMessage("Removed ACTOR source: "+obj.getId(),
                Log.FINEST, id, "", "");
        
        Set<AttributeValue> attrs = obj.getAllAtributes();
        for (AttributeValue value : attrs) {
            
            if (value.getName().indexOf("sensors.") >= 0) {
                Sensor sensor = (Sensor)((AttrObject)value).getValue();
                
                if (!sensors.contains(sensor))
                    continue;
                
                Log.addMessage("Removed sensor: "+sensor.getId(),
                        Log.FINEST, id, "", "");
                sensors.remove(sensor);
                changed = true;
            }
        }
        
        if (changed) {
            phantomKeeper.changeSensors(sensors);
            triggerKeeper.changeSensors(sensors);
        }
        
        actorLost(obj);
    }
    
    public void setCleaningGoals(List<Pair<TriggerTemplate,String>> cg){
        cleaningGoals = cg;
    }
    
    /**
     * Manually selects the best implementation for the given rule
     * (and thus a goal).
     *
     * @param rule Rule for which to find the best implementation.
     * @return Set of ids of processes to be preferred as an implementation
     *      of this goal.
     */
    protected Set<String> selectSpecialProcess(RuleTreeNode rule) {
        return null;
    }
    
    /**
     * Actor becomes delegated to this genius.
     * Descendants of this class can use this method to assign the actors
     * to different processes in the controlled area.
     *
     * @param actor The actor IveObject.
     */
    protected void actorGained(IveObject actor) {
    }
    
    /**
     * Actor is no more delegated to this genius.
     * Descendants of this class can use this method to assign the actors
     * to different processes in the controlled area.
     *
     * @param actor The actor IveObject.
     */
    protected void actorLost(IveObject actor) {
    }
    
    /**
     * Should we run the cleaning process?
     *
     * @param info Info about the accepted goal
     * @return Goal id of the cleaning goal if there is a cleaning goal that
     *      should be run or <code>null</code> if there is no need to run
     *      any cleaning goal.
     */
    protected String shouldClean(AcceptedGoal info) {
        
        if (cleaningGoals == null)
            return null;
        
        for (Pair<TriggerTemplate,String> pair : cleaningGoals) {
            OntologyToken token = pair.first.evaluate(
                    info.rule.getGoal().getSubstitution(), sensors);
            
            try {
                short value = ((Short)token.getData("java.Short")).shortValue();
                
                if (value == FuzzyValueHolder.True) {
                    Log.addMessage("Cleaning-suitability firing for the goal " +
                            pair.second(), Log.FINEST, id, pair.second(), "");
                    return pair.second();
                }
                Log.addMessage("Cleaning-suitability not firing for the goal " +
                        pair.second() + ": " + value, Log.FINEST, id,
                        pair.second(), "");
            } catch (OntologyNotSupportedException ex) {
                Log.addMessage("Unsupported ontology in the cleaning Trigger.",
                        Log.WARNING, id, pair.second(), "");
            }
        }
        return null;
    }
    
    /**
     * Run the cleaning process.
     *
     * @param info Info about the accepted goal
     * @param goalId Id of the cleaning to be run.
     */
    private void runCleaning(AcceptedGoal info, String goalId) {
        RuleTreeNode rule = info.rule;
        
        // First of all, clean the old top-level-goal
        rule.deactivateRule();
        dirtyNodes.remove(rule);
        topLevelGoals.remove(rule);
        acceptedTableByRule.remove(rule);
        
        // Create the new top-level-goal
        TopLevelGoal goal = new TopLevelGoal(goalId);
        goal.setSubstitution(
                info.execution.getPhantoms().duplicateSubstitution(true));
        
        rule = addGoalInternal(goal, null);
        topLevelGoals.add(rule);
        info.rule = rule;
        acceptedTableByRule.put(rule, info);
        
        info.cleaning_count++;
    }
    
    /**
     * Helper class for the accepted delegated process info.
     */
    protected class AcceptedGoal implements java.io.Serializable {
        public RuleTreeNode rule;
        public ProcessExecution execution;
        public IveObject actor;
        public boolean shouldStop = false;
        public int cleaning_count = 0;
        
        public AcceptedGoal(IveObject actor, ProcessExecution execution,
                RuleTreeNode rule) {
            this.actor = actor;
            this.execution = execution;
            this.rule = rule;
        }
    }
}


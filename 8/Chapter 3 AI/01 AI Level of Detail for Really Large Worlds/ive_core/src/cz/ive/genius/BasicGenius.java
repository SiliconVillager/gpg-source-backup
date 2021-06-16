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
import cz.ive.exception.*;
import cz.ive.genius.goaltree.*;
import cz.ive.logs.*;
import cz.ive.process.*;
import cz.ive.sensors.*;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.trigger.*;
import cz.ive.ontology.*;
import cz.ive.simulation.*;
import cz.ive.util.Pair;
import cz.ive.valueholders.FuzzyValueHolder;
import java.io.File;

import java.util.*;

/**
 * Basic implementation of a genius. This is just a simple genius as it is
 * driven only by the affordances. The main idea behind this implementation is:
 *
 * All changes in the triggers/contexts/process states are transfered to the
 * diry mark on an appropriate rule level. Then when all other changes to the
 * world are evaluated, genius simply goes through the dirty rules and makes
 * changes to the processes (stops the old, runs the new and so on).
 *
 * @author ondra
 */
public class BasicGenius implements Genius, java.io.Serializable {
    
    protected static final CalendarPlanner.RoundHookPosition GENIUS_ROUND_HOOK =
            CalendarPlanner.RoundHookPosition.END;
    
    /** P-decider used by this genius*/
    protected BasicPDecider p_decider;
    
    /** Storage for triggers */
    protected TriggerKeeper triggerKeeper = new TriggerKeeper();
    
    /** Phantom keeper for this genius, it assures that we update only these
     * phantoms that we realy need and each only once. */
    protected PhantomKeeper phantomKeeper;
    
    /** List of the Top-level goals */
    protected List<RuleTreeNode> topLevelGoals = new ArrayList<RuleTreeNode>(1);
    
    /** Actors assigned to this genius */
    protected List<IveObject> sources =  new ArrayList<IveObject>(1);
    
    /** Is this genius activated? */
    boolean activated = false;
    
    /** Id of this genius */
    protected String id;
    
    /** Sensors available for this genius */
    protected List<Sensor> sensors = new ArrayList<Sensor>(1);
    
    /**
     * Dirty nodes. These nodes should be run through and reevaluated
     * since some values of the trigger/contexts/process' state have changed.
     */
    protected List<RuleTreeNode> dirtyNodes = new LinkedList<RuleTreeNode>();
    
    /**
     * Creates a new instance of BasicGenius
     *
     * @param id Id of the new genius.
     */
    public BasicGenius(String id) {
        this.id = id;
        
        phantomKeeper = new PhantomKeeper(sensors, this);
        
        p_decider = new BasicPDecider(id);
        p_decider.setSensors(sensors);
        
        Updator.getRootInstance().insert(triggerKeeper);
    }
    
    /**
     * Value of the g_trigger/g_context/p_context/process state have changed.
     * We must reevaluate all siblings. So mark the parent as a dirty rule.
     *
     * @param rule Rule node which was changed
     */
    public void ruleChanged(RuleTreeNode rule) {
        RuleTreeNode dirtyRule = rule.getParent();
        
        if (dirtyRule == null)
            dirtyRule = rule;
        
        markDirty(dirtyRule);
    }
    
    /**
     * The given rule and whole its subtree do not exist any more, no need
     * to reevaluate it.
     *
     * @param rule RuleTreeNode to be unmarked.
     */
    public void ruleLost(RuleTreeNode rule) {
        dirtyNodes.remove(rule);
    }
    
    /**
     * The environment will not execute this process as atomic. We must
     * get the process expansion and create approprite rules from it.
     */
    public void expandProcess(RuleTreeNode rule) {
        IveProcess process = rule.getProcess();
        
        OntologyToken expansion= process.getExpansion();
        if (expansion != null){
            try {
                
                //  Ask for the expansion and add all the rules
                Goal[] subGoals = (Goal[])expansion.getData("jBRP.expansion");
                for (Goal goal : subGoals) {
                    if (goal == null) {
                        Log.addMessage("Subgoal is NULL. This looks like " +
                                "an error during the process expansion. " +
                                "Consult the Log for some previous messages.",
                                Log.WARNING, id, process.getProcessId(), "");
                        continue;
                    }
                    addGoalInternal((Goal)goal, rule);
                }
                
            } catch (OntologyNotSupportedException ex) {
                Log.addMessage("Process " + process.getProcessId() +
                        " does not contain expansion description in the " +
                        "\"jBRP.expansion\" ontology.", Log.SEVERE, id,
                        process.getProcessId(), "");
                return;
            }
        } else{
            Log.addMessage("Process " + process.getProcessId() +
                    " should be expanded, but it is atomic - " +
                    "incorrect world specification", Log.SEVERE, id,
                    process.getProcessId(), "");
        }
    }
    
    /**
     * Marks the given rule node as dirty for later reevaluating. This method
     * also registers the RoundHook if there was no dirty rule present.
     *
     * @param rule RuleTreeNode to be marked as dirty.
     */
    protected void markDirty(RuleTreeNode rule) {
        // Mark only the uppermost (nearest to the root) in each sub tree.
        List<RuleTreeNode> toDelete = new LinkedList<RuleTreeNode>();
        for (Iterator<RuleTreeNode> i=dirtyNodes.iterator(); i.hasNext();) {
            RuleTreeNode dirtyRule = i.next();
            
            if (dirtyRule == rule) {
                // This node is already marked as dirty
                return;
            } else if (dirtyRule.getDepth() > rule.getDepth()) {
                if (rule.isDescendant(dirtyRule)) {
                    // One of the marked nodes is descendant of the newly marked
                    // rule, unmark it.
                    i.remove();
                }
            } else {
                if (dirtyRule.isDescendant(rule)) {
                    // One of the ancestors is already marked, nothing to do.
                    return;
                }
            }
        }
        
        // Plan the round hook, if this is the first dirty rule.
        if (dirtyNodes.isEmpty()) {
            GeniusList.instance().markDirty(this);
        }
        
        dirtyNodes.add(rule);
    }
    
    /**
     * Perform posponded actions
     */
    public void update() {
        evaluateDirtyRules();
    }
    
    /**
     * Asks if there is an reason for invoking update()
     * @return true if there is any waiting action
     */
    public boolean needUpdate() {
        return !dirtyNodes.isEmpty();
    }
    
    /**
     * Evaluates all dirty rules. It is expected, that even during
     * the evaluation, some rules can be marked.
     */
    protected void evaluateDirtyRules() {
        while (!dirtyNodes.isEmpty()) {
            RuleTreeNode dirtyParent = dirtyNodes.remove(0);
            
            if (dirtyParent.getParent() == null) {
                evaluateTopLevelRule(dirtyParent);
            } else {
                evaluateChildRules(dirtyParent);
            }
        }
    }
    
    /**
     * Reevaluates children rules of the given rule node.
     *
     * @param parent RuleTreeNode parent of the rules to be evaluated.
     */
    protected void evaluateChildRules(RuleTreeNode parent) {
        RuleTreeNode running = null;
        RuleTreeNode stopping = null;
        RuleTreeNode shouldRun = null;
        
        // Extract important information from the child rules
        for (RuleTreeNode rule : parent.getSons()) {
            RuleState state = rule.getRuleState();
            
            if (shouldRun == null &&
                    rule.getGTriggerValue() == FuzzyValueHolder.True) {
                if (parent.getRuleState() != RuleState.PROCESS_STOPPING &&
                        running == null)
                    shouldRun = rule;
            }
            
            if (state == RuleState.PROCESS_STOPPING) {
                if (stopping != null)
                    Log.addMessage("More processes in the STOPPING state.",
                            Log.WARNING, id, rule.getProcess().getProcessId(),
                            "");
                stopping = rule;
            } else if (state == RuleState.PROCESS_RUNNING) {
                if (running != null)
                    Log.addMessage("More processes in the RUNNING state.",
                            Log.WARNING, id, rule.getProcess().getProcessId(),
                            "");
                running = rule;
                if (rule.getGContextValue() == FuzzyValueHolder.True &&
                        shouldRun == null) {
                    shouldRun = rule;
                }
            } else if (state == RuleState.PROCESS_FINISHED) {
                Log.addMessage("Process for goal "+rule.getGoal().getGoalID()+
                        " finished with result: "+rule.acceptProcessResult(),
                        Log.FINEST, id, rule.getGoal().getGoalID(), "");
            }
        }
        
        if (!ruleJudgement(shouldRun, running, stopping)) {
            // We should stop the parent process as failed
            Log.addMessage("Contradiction in p/g_contexts and g_trigger. " +
                    "We wanted to run goal: " + parent.getGoal().getGoalID() +
                    " but no of its children wants to run.",
                    Log.WARNING, id, parent.getGoal().getGoalID(), "");
            
            /**
             * TODO: Future work
             *
             * This is in fact a FAIL. Some future problem solver genius could
             * and should use this information.
             */
            
            parent.stopProcess();
        }
    }
    
    /**
     * Reevaluates the top level rule.
     *
     * @param rule The top level rule.
     */
    protected void evaluateTopLevelRule(RuleTreeNode rule) {
        RuleTreeNode running = null;
        RuleTreeNode stopping = null;
        RuleTreeNode shouldRun = null;
        RuleState state = rule.getRuleState();
        
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
        }
        
        if (!ruleJudgement(shouldRun, running, stopping)) {
            Log.addMessage("We should stop the top level goal, " +
                    "this look like a bad world design.",
                    Log.WARNING, id, rule.getGoal().getGoalID(), "");
        }
    }
    
    /**
     * Do the final rule judgement.
     *
     * @param shouldRun Rule which g_trigger is firing (and its priotity is
     *      the bigest).
     * @param running Rule that is currently runnning
     * @param stopping Rule that is currently stopping
     * @return <code>true</code> if no problem has occured, <code>false</code>
     *      if the parent process should fail.
     */
    protected boolean ruleJudgement(RuleTreeNode shouldRun,
            RuleTreeNode running, RuleTreeNode stopping) {
        
        // There should be only one running XOR one stopping process/rule
        if (running != null && stopping != null) {
            Log.addMessage("There should not be both stopping and running " +
                    "child processes at the same time.",
                    Log.WARNING, id, running.getProcess().getProcessId(), "");
            return true; // But it should be just a temporal problem
        }
        
        // It is OK for now, we must wait for all child to stop.
        if (stopping != null) {
            if (!stopping.isAtomic()) {
                evaluateChildRules(stopping);
            }
            return false;
        }
        
        // There is no process with its g_trigger firing
        if (shouldRun == null) {
            // There is some running process, stop it. If it is stopped
            // immediately, it will be marked as dirty during this call and
            // proceeded later.
            if (running != null) {
                Log.addMessage("No g-trigger is firing stop the process.",
                        Log.INFO, id, running.getProcess().getProcessId(), "");
                running.stopProcess();
            } else {
                // No process running or stopping. Then we have failed.
                return false;
            }
        } else {
            // The currently running process is the one that should be run
            if (running == shouldRun) {
                // Are the contexts firing? Then everything is OK.
                if (running.getGContextValue() == FuzzyValueHolder.True &&
                        running.getPContextValue() == FuzzyValueHolder.True) {
                    if (!running.isAtomic()) {
                        evaluateChildRules(running);
                    }
                } else {
                    // Contexts are not firing, stop the process
                    Log.addMessage("We must stop the process. G- or P-context is not firing.",
                            Log.INFO, id, running.getProcess().getProcessId(), "");
                    running.stopProcess();
                }
            } else if (running != null) {
                // We want to run another process, stop this one.
                Log.addMessage("We must stop the process to run another.",
                        Log.INFO, id, running.getProcess().getProcessId(), "");
                running.stopProcess();
            } else {
                // No running process, but there is one desired.
                // Choose one and run it!
                
                // First we test whether we are not inside the dead cycle
                // Panic if so.
                if (shouldRun.isDeadCycle()) {
                    Log.addMessage("We are deadly cycling. PANIC instead.",
                            Log.SEVERE, id, shouldRun.getGoal().getGoalID(),
                            "");
                    
                    // Run the PANNIC process.
                    runPanic(shouldRun);
                    return true;
                }
                
                if (!runProcess(shouldRun)) {
                    Log.addMessage("Could not run process for the firing rule.",
                            Log.WARNING, id, shouldRun.getGoal().getGoalID(),
                            "");
                    
                    // Run the PANNIC process.
                    runPanic(shouldRun);
                    return true;
                }
            }
        }
        return true;
    }
    
    /**
     * Choses one of the processes implementing the goal associated with
     * the given rule.
     *
     * @param rule Rule which process should be executed.
     * @return <code>true</code> iff the process was successfully chosen
     *      and executed, <code>false</code> otherwise.
     */
    protected boolean runProcess(RuleTreeNode rule) {
        Log.addMessage("Running the process for goal: "+rule.getGoal().getGoalID(),
                Log.INFO, id, rule.getGoal().getGoalID(), "");
        if (rule.getGContextValue() != FuzzyValueHolder.True) {
            Log.addMessage("Goal context is not true. The process cannot be " +
                    "executed", Log.WARNING, id, rule.getGoal().getGoalID(),
                    "");
            return false;
        }
        Set<String> failedProcesses = rule.getFailedProcesses();
        Pair<ProcessTemplate, Substitution> chosenPair =
                p_decider.chooseProcess(rule.getGoal(), sources,
                failedProcesses, null);
        
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
     * This method fires panic goal on the given rule tree node.
     *
     * @param rule Rule to be inhabited by a panic process.
     */
    protected void runPanic(RuleTreeNode rule) {
        Goal panicGoal = new Goal();
        panicGoal.setGoalID("PanicGoal");
        Pair<ProcessTemplate, Substitution> chosenPair =
                p_decider.chooseProcess(panicGoal, sources,
                new HashSet<String>(1), null);
        
        if (chosenPair == null || chosenPair.first() == null) {
            Log.addMessage("We failed to run the PanicGoal...something " +
                    "went realy wrong!", Log.SEVERE, id,
                    "PanicGoal","");
            return;
        }
        
        rule.executeProcess(chosenPair.first(), chosenPair.second());
    }
    
    public void addTopLevelGoal(Goal goal) {
        // We simply choose a random VIP source and try to assign it to the
        // goal as an actor (As many as needed). This behavior is tolerable
        // for the basic genius. It ahs no additional information it can use
        // and it is not approprite to be used with more than one actor.
        
        for (Map.Entry<String, Slot> entry :  goal.getSubstitution().
                getActorSlots().entrySet()) {
            
            int i = (int)(Math.random() * sources.size());
            
            Source src = entry.getValue().getSource();
            if (src != null && src.getObject() == null) {
                
                // Do we have at least one VIP source?
                if (sources.size() == 0) {
                    Log.addMessage("No VIP source to be set as actor slot \"" +
                            entry.getKey() + "\" into the top level goal.",
                            Log.WARNING, id, goal.getGoalID(), "");
                    return;
                }
                src.setObject(sources.get(i));
            }
        }
        
        topLevelGoals.add(addGoalInternal(goal, null));
    }
    
    public void removeTopLevelGoal(Goal goal) {
        RuleTreeNode oldRule = null;
        for (RuleTreeNode rule : topLevelGoals) {
            // Semantics of the top-level goal removal is not clear.
            // Now we just remove the goal that is first one with the reference
            // to the given goal.
            if (rule.getGoal() == goal) {
                oldRule = rule;
                break;
            }
        }
        
        if (oldRule != null) {
            // Now again, the process may still run for a while, but
            // it will not need any further assistance from the genius.
            // No callbacks about the real process finish will be issued.
            oldRule.deactivateRule();
            topLevelGoals.remove(oldRule);
            dirtyNodes.remove(oldRule);
        }
    }
    
    /**
     * Inserts the given goal to the rule tree hierarchy. If the genius is
     * actvate then the new rule is also activated.
     *
     * @param goal Goal representing new rule.
     * @param parent Parent of the enw rule in the rule tree hierarchy.
     * @return Newly created rule node (already inserted to the hierarchy).
     */
    protected RuleTreeNode addGoalInternal(Goal goal, RuleTreeNode parent) {
        RuleTreeNode newRule = new RuleTreeNode(this, phantomKeeper,
                triggerKeeper, sensors, goal, parent);
        
        if (activated) {
            newRule.activateRule();
        }
        
        return newRule;
    }
    
    public List<Goal> getTopLevelGoals() {
        List<Goal> goals = new ArrayList<Goal>(topLevelGoals.size());
        
        for (RuleTreeNode rule : topLevelGoals) {
            goals.add(rule.getGoal());
        }
        
        return goals;
    }
    
    /**
     * Getter for top level rules. To be used by a GUI only!
     *
     * @return List of the top-level rules as RuleTreeNodes. Whole rules
     *      trees is accessible via this roots.
     */
    public List<RuleTreeNode> getTopLevelRules() {
        return topLevelGoals;
    }
    
    public void addSource(Source source) {
        IveObject obj = source.getObject();
        sources.add(obj);
        Log.addMessage("Added ACTOR source: "+obj.getId(),
                Log.FINEST, id, "", "");
        
        Set<AttributeValue> attrs = obj.getAllAtributes();
        for (AttributeValue value : attrs) {
            
            if (value.getName().indexOf("sensors.") >= 0) {
                Sensor sensor = (Sensor)((AttrObject)value).getValue();
                sensors.add(sensor);
                phantomKeeper.changeSensors(sensors);
                triggerKeeper.changeSensors(sensors);
                Log.addMessage("Added sensor: "+sensor.getId(),
                        Log.FINEST, id, "", "");
            }
        }
    }
    
    public void removeSource(Source source) {
        IveObject obj = source.getObject();
        sources.remove(obj);
        Log.addMessage("Removed ACTOR source: "+obj.getId(),
                Log.FINEST, id, "", "");
        
        Set<AttributeValue> attrs = obj.getAllAtributes();
        for (AttributeValue value : attrs) {
            
            if (value.getName().indexOf("sensors.") >= 0) {
                Sensor sensor = (Sensor)((AttrObject)value).getValue();
                sensors.remove(sensor);
                phantomKeeper.changeSensors(sensors);
                triggerKeeper.changeSensors(sensors);
                Log.addMessage("Removed sensor: "+sensor.getId(),
                        Log.FINEST, id, "", "");
            }
        }
    }
    
    /** All given goals are activated (genius will start fulfilling them)
     * in this method.
     */
    public void activate()  {
        GeniusList.instance().registerGenius(this);
        try {
            // Activate the rules and start finding appropriate processes
            // for these of them that fire.
            for (RuleTreeNode rule : topLevelGoals) {
                rule.activateRule();
            }
            
            Log.addMessage("Genius activated", Log.FINE,
                    id,  "", "");
            activated = true;
        } catch (Exception e) {
            IveApplication.printStackTrace(e);
            Log.addMessage("Genius activation failed", Log.SEVERE,
                    id,  "", "");
        }
    }
    
    /**
     * All goals and processes are deactivated. All hooks and all connection to
     * the world is disposed. Also all passive queries should be unregistered
     * here.
     */
    public void deactivate()  {
        if (!activated)
            return;
        
        GeniusList.instance().unregisterGenius(this);
        
        activated = false;
        
        // Just deactivate all the rules and we are done.
        // Note that some of the processes may still run for a while, but this
        // does not need any further assistance from the genius.
        for (RuleTreeNode rule : topLevelGoals) {
            rule.deactivateRule();
        }
        
        dirtyNodes.clear();
        
        Log.addMessage("Genius deactivatated", Log.FINE,
                id,  "", "");
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * Dumps contents of the trigger keeper for the debuging purposes.
     *
     * @param file identification of the file were we should write the dump.
     */
    public void dumpTriggerKeeper(File file) {
        triggerKeeper.dump(file);
    }
}

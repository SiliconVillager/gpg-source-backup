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
 
package cz.ive.genius.goaltree;

import cz.ive.IveApplication;
import cz.ive.exception.DelegationFailedException;
import cz.ive.exception.OntologyNotSupportedException;
import cz.ive.exception.ProcessNotRunningException;
import cz.ive.exception.StopDelegationFailedException;
import cz.ive.genius.*;
import cz.ive.logs.Log;
import cz.ive.simulation.CalendarPlanner;
import cz.ive.simulation.WorldInterpreter;
import cz.ive.process.*;
import cz.ive.trigger.*;
import cz.ive.messaging.*;
import cz.ive.ontology.*;
import cz.ive.sensors.*;
import cz.ive.valueholders.FuzzyValueHolder;

import java.util.*;

/**
 * Node of the RuleTreeNode structure. It represents one rule in the If-do-then
 * expansion, trigger, kontext, goal and the probably running process.
 *
 * @author ondra
 */
public class RuleTreeNode implements java.io.Serializable, Listener {
    
    /**
     * Number of failed executions in the instant moment to be considered
     * as dead cycle. The genius should panic.
     */
    public static final int PANIC_CYCLES = 5;
    
    /** Number of the repeated execution in the instant moment */
    protected int cycles = 0;
    
    /** Last simulation time of the execution */
    protected long lastExecutionTime = -1;
    
    /** Depth of the node. Starting from 0 for the top-level rule */
    protected int depth = 0;
    
    /** Owner genius. */
    protected BasicGenius genius;
    
    /** PhantomKeeper to be used to keep phantoms updated. */
    protected PhantomKeeper phantomKeeper;
    
    /** TriggerKeeper to be used to store triggers. */
    protected TriggerKeeper triggerKeeper;
    
    /** Sensors available for triggers */
    protected List<Sensor> sensors;
    
    /** State of the rule. */
    protected RuleState ruleState = RuleState.NOT_ACTIVE;
    
    /** Rule in the Goal ontology. It is one element of the process expansion */
    protected Goal goal;
    
    /** Is this process atomic? */
    protected boolean atomic = true;
    
    /** G-trigger of the rule (the "If" part). */
    protected Trigger g_trigger;
    
    /** G-context of the rule. */
    protected Trigger g_context;
    
    /** P-context of the rule. */
    protected Trigger p_context;
    
    /** Associated process */
    protected IveProcess process;
    
    /** ShrinkHook shortcut */
    protected Hook shrinkHook;
    
    /** ExpandHook shortcut */
    protected Hook expandHook;
    
    /** FinishHook shortcut */
    protected FinishHook finishHook;
    
    /** Last result of the finished process */
    protected ProcessResult processResult;
    
    /** If the node is expanded, this contains sons of this node */
    List<RuleTreeNode> sons = new LinkedList<RuleTreeNode>();
    
    /**
     * Parent of this node or <code>null</code> if this is a top-level rule.
     * That is rule for the top-level goal.
     */
    RuleTreeNode parent;
    
    /*
     * Processes which failed, those shouldn't be chosen again in the p-decider
     */
    Set<String> failedProcesses = new HashSet<String>(5);
    
    /**
     *
     * Creates a new instance of RuleTreeNode.
     *
     * @param genius The owner genius to be called back.
     * @param phantomKeeper PhantomKeeper to be used to keep phantoms of the
     *      running processes updated
     * @param triggerKeeper TriggerKeeper to be used to store triggers.
     * @param sensors Sensors available for triggers.
     * @param goal one of the expansion goals (in fact also representing rules)
     * @param parent The parent RuleTreeNode
     */
    public RuleTreeNode(BasicGenius genius, PhantomKeeper phantomKeeper,
            TriggerKeeper triggerKeeper, List<Sensor> sensors,
            Goal goal, RuleTreeNode parent) {
        this.genius = genius;
        this.phantomKeeper = phantomKeeper;
        this.triggerKeeper = triggerKeeper;
        this.sensors = sensors;
        this.goal = goal;
        this.parent = parent;
        
        if (parent != null) {
            parent.sons.add(this);
            depth = parent.depth + 1;
        }
    }
    
    /**
     * Associates a given process with this rule and executes it.
     *
     * @param template ProcessTemplate of the process to be executed.
     * @param sources Substitution used by process to access sources
     */
    public void executeProcess(ProcessTemplate template, Substitution sources) {
        // Register phantom update
        phantomKeeper.registerSubstitution(sources);
        
        // Instantiate and run the process
        atomic = true; // Atomic, till it expands.
        process = template.instantiate(sources, goal.getParameters(), sensors);
        
        // Register on Hooks
        (shrinkHook = process.getShrinkHook()).registerListener(this);
        (expandHook = process.getExpandHook()).registerListener(this);
        (finishHook = process.getFinishHook()).registerListener(this);
        
        ruleState = RuleState.PROCESS_RUNNING;
        
        addCycle();
        
        // Execute it
        WorldInterpreter.instance().execute(process);
        
        // Instantiate the context if it has not failed immediatelly
        if (process != null) {
            p_context = triggerKeeper.insertTrigger(process.getContext());
            p_context.registerListener(this);
        }
    }
    
    /**
     * Ask the process to stop. This must be called in the ruleState
     * PROCESS_RUNNING and as a result, the process moves to the state
     * PROCESS_STOPPING or PROCESS_FINISHED.
     */
    public void stopProcess() {
        if (ruleState != RuleState.PROCESS_RUNNING) {
            Log.warning("Asked to stop process wich is not running");
            return;
        }
        ruleState = RuleState.PROCESS_STOPPING;
        
        if (atomic) {
            try {
                process.stop();
            } catch(ProcessNotRunningException ex) {
                IveApplication.printStackTrace(ex);
                Log.severe("Asked to stop process wich is not running (but " +
                        "we do not even know it in the RuleTreeNode)");
            }
        } else {
            boolean finished = true;
            ProcessResult result = null;
            RuleTreeNode running = null;
            
            for (RuleTreeNode son : sons) {
                if (son.ruleState == RuleState.PROCESS_RUNNING) {
                    running = son;
                    finished = false;
                } else if (son.ruleState == RuleState.PROCESS_STOPPING) {
                    finished = false;
                } else if (son.ruleState == RuleState.PROCESS_FINISHED) {
                    result = son.processResult;
                }
            }
            
            // Stop the running process
            if (running != null) {
                running.stopProcess();
            }
            
            // No child process is running, so we are finished.
            if (finished) {
                finish(result);
            }
        }
    }
    
    /**
     * Getter for sons of this node.
     */
    public List<RuleTreeNode> getSons() {
        return sons;
    }
    
    /** Getter for the Goal represented by this RuleNode. */
    public Goal getGoal() {
        return goal;
    }
    
    /** Getter for the associated process if any. */
    public IveProcess getProcess() {
        return process;
    }
    
    /** Getter for the state of this RuleNode. */
    public RuleState getRuleState() {
        return ruleState;
    }
    
    /**
     * Activates the rule. The rule instantiates the trigger and context and
     * registers itself to appropriate Hooks.
     */
    public void activateRule() {
        if (ruleState.isActive())
            return;
        
        // Registering the passive update on chosen sources
        phantomKeeper.registerSubstitution(goal.getSubstitution());
        
        // Initialize the triggers
        
        // Parent process substitution, if available.
        Substitution substitution = null;
        if (parent != null) {
            substitution = parent.getProcess().getSubstitution();
        } else {
            substitution = goal.getSubstitution();
        }
        
        Trigger triggerHook;
        g_trigger = ((TriggerTemplate)goal.getGTrigger()).instantiate(
                substitution, sensors, goal.getParameters());
        g_trigger = triggerKeeper.insertTrigger(g_trigger);
        
        g_context = ((TriggerTemplate)goal.getGContext()).instantiate(
                substitution, sensors, goal.getParameters());
        g_context = triggerKeeper.insertTrigger(g_context);
        
        // register on the trigger & context
        g_trigger.registerListener(this);
        g_context.registerListener(this);
        
        ruleState = RuleState.NO_PROCESS;
        
        genius.ruleChanged(this);
    }
    
    /**
     * Deactivates the rule. The rule frees trigger and context and unregisters
     * itself from the Hooks.
     */
    public void deactivateRule() {
        if (!ruleState.isActive())
            return;
        
        g_trigger.unregisterListener(this);
        g_context.unregisterListener(this);
        triggerKeeper.removeTrigger(g_trigger);
        triggerKeeper.removeTrigger(g_context);
        g_trigger = null;
        g_context = null;
        
        phantomKeeper.unregisterSubstitution(goal.getSubstitution());
        
        if (ruleState.isProcessAssocited()) {
            clearProcess();
        }
        
        ruleState = RuleState.NOT_ACTIVE;
        genius.ruleLost(this);
    }
    
    /**
     * Unregisters all listeners associated with the process and disposes its
     * context. It also deactivates all subrules. As a result, the ruleTreeNode
     * will be in the NO_PROCESS state with no sons.
     */
    protected void clearProcess() {
        if (!ruleState.isProcessAssocited())
            return;
        
        if (p_context != null) {
            p_context.unregisterListener(this);
            triggerKeeper.removeTrigger(p_context);
            p_context = null;
        }
        
        shrinkHook.unregisterListener(this);
        expandHook.unregisterListener(this);
        finishHook.unregisterListener(this);
        shrinkHook = expandHook = finishHook = null;
        
        if (atomic) {
            // If we are still running, stop it (with no callback).
            if (ruleState == RuleState.PROCESS_RUNNING) {
                stopProcess();
            }
        } else {
            if (ruleState == RuleState.PROCESS_RUNNING ||
                    ruleState == RuleState.PROCESS_STOPPING) {
                try {
                    process.stop();
                } catch (ProcessNotRunningException ex) {
                    Log.severe("Process is not running EXC.");
                }
            }
            for (RuleTreeNode son : sons) {
                son.deactivateRule();
                son.parent = null;
            }
            sons.clear();
        }
        
        phantomKeeper.unregisterSubstitution(process.getSubstitution());
        
        process = null;
        ruleState = RuleState.NO_PROCESS;
    }
    
    /**
     * Some of the hooks have been signalled. Change the state, and acknowledge
     * the genius.
     *
     * @param initiator Hook that originated this call.
     */
    public void changed(Hook initiator) {
        if (initiator == shrinkHook) {
            shrink();
        } else if (initiator == expandHook) {
            expand();
        } else if (initiator == finishHook) {
            finish(((FinishHook)initiator).getResult());
        } else if (initiator == g_trigger) {
            Log.addMessage("G-trigger of goal " + goal.getGoalID() +
                    " has changed to:" + getGTriggerValue(),
                    Log.FINE, genius.getId(), goal.getGoalID(), "");
            genius.ruleChanged(this);
        } else if (initiator == g_context) {
            Log.addMessage("G-context of goal " + goal.getGoalID() +
                    " has changed to:" + getGContextValue(),
                    Log.FINE, genius.getId(), goal.getGoalID(), "");
            genius.ruleChanged(this);
        } else if (initiator == p_context) {
            Log.addMessage("P-context of process " + process.getProcessId() +
                    " has changed to:" + getPContextValue(),
                    Log.FINE, genius.getId(), process.getProcessId(), "");
            genius.ruleChanged(this);
        }
    }
    
    /**
     * Hook was cancelled.
     *
     * @param initiator Hook that originated this call.
     */
    public void canceled(Hook initiator) {
        // This should not hurt us.
    }
    
    /**
     * Associted process should became atomic.
     */
    protected void shrink() {
        Log.addMessage("Process " + process.getProcessId() +
                " should be atomic.", Log.INFO,
                genius.getId(), process.getProcessId(), "");
        for (RuleTreeNode son : sons) {
            son.deactivateRule();
            son.parent = null;
        }
        sons.clear();
        atomic = true;
        
        if (ruleState == RuleState.PROCESS_STOPPING) {
            // We wanted this process to be stopped before,
            // issue the call again.
            Log.addMessage("Process " + process.getProcessId() +
                    " is STOPPING, but after shrink it becomes RUNNING. " +
                    "Genius should stop it again.", Log.INFO,
                    genius.getId(), process.getProcessId(), "");
            try {
                process.stop();
            } catch (ProcessNotRunningException ex) {
                IveApplication.printStackTrace(ex);
                Log.severe("Asked to stop process wich is not running (but " +
                        "we do not even know it in the RuleTreeNode)");
            }
            finish(ProcessResult.OK);
        }
        
        genius.ruleChanged(this);
    }
    
    /**
     * Associted process should be expanded.
     */
    protected void expand() {
        Log.addMessage("Process " + process.getProcessId() +
                " should be expanded.", Log.INFO, genius.getId(),
                process.getProcessId(), "");
        atomic = false;
        genius.expandProcess(this);
    }
    
    /**
     * Associted process have finished.
     *
     * @param result ProcessResult og the finished process.
     */
    public void finish(ProcessResult result) {
        Log.addMessage("Process " + process.getProcessId() + " finished " +
                "with result: "+result,
                Log.INFO, genius.getId(), process.getProcessId(), "");
        
        processResult = result;
        
        // If the parent is STOPPING, then let it do all the work.
        if (parent != null && parent.ruleState == RuleState.PROCESS_STOPPING) {
            parent.finish(result);
        } else {
            ruleState = RuleState.PROCESS_STOPPING;
            clearProcess();
            ruleState = RuleState.PROCESS_FINISHED;
            if (result == null) {
                // The result value may not be known, then we will treat is
                // as OK.
                processResult = ProcessResult.OK;
            }
            genius.ruleChanged(this);
        }
    }
    
    /**
     * Is the process, associted with this rule, atomic? This makes sense only
     * if the rule is in the state PROCESS_RUNNING or PROCESS_STOPPING.
     */
    public boolean isAtomic() {
        return atomic;
    }
    
    /**
     * Getter for the rule depth in the rule Hierarchy.
     *
     * @return depth of this node starting from 0.
     */
    public int getDepth() {
        return depth;
    }
    
    /**
     * Getter for the rule's parent in the rule hierarchy.
     *
     * @return parent of this rule node in the rule hierarchy.
     */
    public RuleTreeNode getParent() {
        return parent;
    }
    
    /**
     * Getter for the failed processes.
     *
     * @return Set of the failed processes.
     */
    public Set<String> getFailedProcesses() {
        return failedProcesses;
    }
    
    /**
     * Getter for the g_trigger.
     *
     * @return the trigger itself
     */
    public Trigger getGTrigger() {
        return g_trigger;
    }
    
    /**
     * Getter for the g_context.
     *
     * @return the trigger itself
     */
    public Trigger getGContext() {
        return g_context;
    }
    
    /**
     * Getter for the p_context.
     *
     * @return the trigger itself
     */
    public Trigger getPContext() {
        return p_context;
    }
    
    /**
     * Getter for the g_trigger value.
     *
     * @return the short value of the trigger
     */
    public short getGTriggerValue() {
        return getTriggerValue(g_trigger);
    }
    
    /**
     * Getter for the g_context value.
     *
     * @return the short value of the trigger
     */
    public short getGContextValue() {
        return getTriggerValue(g_context);
    }
    
    /**
     * Getter for the p_context value.
     *
     * @return the short value of the trigger
     */
    public short getPContextValue() {
        return getTriggerValue(p_context);
    }
    
    /**
     * Getter for the trigger value. This is just a helper for easier accessing
     * the Ontology token.
     *
     * @return the short value of the trigger
     */
    protected short getTriggerValue(Trigger trigger) {
        try {
            return ((Short)trigger.value().getData(
                    "java.Short")).shortValue();
        } catch (OntologyNotSupportedException ex) {
            IveApplication.printStackTrace(ex);
            Log.severe("Trigger does not support the \"java.Short\" " +
                    "ontology.");
            return FuzzyValueHolder.False;
        }
    }
    
    /**
     * Accepts the process result and transfers the rule
     * to the NO_PROCESS state.
     *
     * @return the last process result if present.
     */
    public ProcessResult acceptProcessResult() {
        if (ruleState != RuleState.PROCESS_FINISHED) {
            Log.warning("Cannot accept the process result. Rule is not in " +
                    "the PROCESS_FINISHED state.");
            return null;
        }
        ruleState = RuleState.NO_PROCESS;
        ProcessResult result = processResult;
        processResult = null;
        return result;
    }
    
    /**
     * Is the given node your descendant?
     *
     * @param descendant Potential descendant of this node.
     * @return <code>true</code> if the given node is descendant of this one,
     *      <code>false</code> otherwise (including equality).
     */
    public boolean isDescendant(RuleTreeNode descendant) {
        RuleTreeNode parentRule = descendant.parent;
        while (parentRule != null && parentRule.depth > depth)
            parentRule = parentRule.parent;
        
        return parentRule == this;
    }
    
    /**
     * Adds the execution to the panic cycle counting.
     */
    protected void addCycle() {
        long sTime = CalendarPlanner.instance().getSimulationTime();
        
        if (sTime == lastExecutionTime) {
            cycles++;
        } else {
            lastExecutionTime = sTime;
            cycles = 1;
        }
    }
    
    /**
     * Are we already in the dead cycle? Should we panic?
     *
     * @return <code>true</code> iff we are inside the dead cycle.
     */
    public boolean isDeadCycle() {
        long sTime = CalendarPlanner.instance().getSimulationTime();
        
        return lastExecutionTime == sTime &&
                cycles >= PANIC_CYCLES;
    }
}

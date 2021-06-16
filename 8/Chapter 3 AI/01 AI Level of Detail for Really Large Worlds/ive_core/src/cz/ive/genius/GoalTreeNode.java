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

import java.util.*;

import cz.ive.process.*;
import cz.ive.trigger.*;
import cz.ive.messaging.*;
import cz.ive.ontology.*;

/**
 * Holds all the information needed to work with a goal.
 * One node of the tree of goals.
 *
 * @author honza
 *
 */

public class GoalTreeNode extends SingleToken implements java.io.Serializable {
    Goal goal;
    Trigger trigger;
    Trigger context;
    Hook triggerHook;
    Hook contextHook;
    boolean triggerValue;
    boolean goalContextValue;
    // processes which failed - those shouldn't be chosen again in the p-decider
    Vector<String> failedProcesses;
    
    IveProcess process;
    ProcessTemplate processTemplate;
    Hook processContextHook;
    Hook finishHook;
    Hook expandHook;
    boolean processContextValue;
    Trigger processContext;
    boolean shouldRerunProcess = true;
    Vector<GoalTreeNode> sons;
    GoalTreeNode parent;
    boolean goalActivated;
    boolean processRuns = false;
    
    public GoalTreeNode(Goal givenGoal, Trigger givenTrigger, 
                        Trigger givenContext) {
        
        super("jBRP.goalTreeNode");  
        sons = new Vector<GoalTreeNode>();
        this.goal = givenGoal;
        setTriggers(givenTrigger, givenContext);
        goalActivated = false;
        failedProcesses = new Vector<String>();
        
    }
    
   
    /** Only fills class members */
    public void setTriggers(Trigger givenTrigger, Trigger givenContext) {
        this.trigger = givenTrigger;
        this.context = givenContext;
    }
    
    /**
     * Getter for sons of this node.
     */
    public Vector<GoalTreeNode> getSons() {
        return sons;
    }
    
    public Goal getGoal() {
        return goal;
    }
    
    public IveProcess getProcess() {
        return process;
    }
    
    public Trigger getTrigger() {
        return trigger;
    }
    
    public Trigger getContext() {
        return context;
    }
}

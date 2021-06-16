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

import cz.ive.genius.AreaGeniusImpl.AcceptedGoal;
import cz.ive.genius.goaltree.*;
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.IveObjectImpl;
import java.util.*;

/**
 * Area genius specialized for queueing the actors.
 *
 * @author ondra
 */
public class QueueGenius extends AreaGeniusImpl {
    
    /** Actors in the queue. */
    protected List<String> actors = new ArrayList<String>();
    
    /** Queue prefered process */
    static protected Set<String> queueProcess = new HashSet<String>(1);
    
    static {
        queueProcess.add("Queue");
    }
    
    /**
     * Creates a new instance of QueueGenius
     *
     * @param id Id to be assigned to the new genius.
     * @param areaId Id of the area assigned.
     */
    public QueueGenius(String id, String areaId) {
        super(id, areaId);
        specialGoals.add("QueueGoal");
    }
    
    protected Set<String> selectSpecialProcess(RuleTreeNode rule) {
        String goalId = rule.getGoal().getGoalID();
        
        if (goalId.equals("QueueGoal")) {
            AcceptedGoal info = getInfo(rule);
            String id = info.actor.getId();
            int i = actors.indexOf(id);
            
            if (i > 0) {
                rule.getGoal().getSubstitution().getSource("next").setObject(
                        new IveObjectImpl(actors.get(i-1)));
            } else {
                rule.getGoal().getSubstitution().getSource(
                        "next").setObject(null);
            }
            
            return queueProcess;
        }
        return null;
    }
    
    protected void actorGained(IveObject actor) {
        actors.add(actor.getId());
    }
    
    protected void actorLost(IveObject actor) {
        String id = actor.getId();
        int i = actors.indexOf(id);
        
        actors.remove(id);
        
        // Let the queue make a step.
        for (int j=i; j<actors.size(); j++) {
            AcceptedGoal info = acceptedTableByActor.get(actors.get(j));
            
            if (info.rule.getRuleState() == RuleState.PROCESS_RUNNING) {
                info.rule.stopProcess();
            }
        }
    }
}

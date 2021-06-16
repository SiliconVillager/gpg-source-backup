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

import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.attributes.*;
import cz.ive.logs.Log;
import cz.ive.trigger.TriggerTemplate;
import cz.ive.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * This class is used to produce Goal instances. Although goal is only a string
 * identifier in the specification we cannot use one Goal instance for all
 * occurences of particular goal.
 * Goal class keeps some data such as substitution that cannot be shared.
 *
 *
 * @author thorm
 */
public  class GoalFactory implements Serializable {
    
    TriggerTemplate gTrigger;
    TriggerTemplate gCtx;
    Substitution substitutionDraft;
    Map<String, Object> parameters;
    List<Pair<String,String>> copySlots;
    String goalId;
    
    /**
     * Constructor takes all information that is needed to create goal
     * @param gTrigger goal trigger template
     * @param gCtx goal context template
     * @param s substitution template
     * @param parameters goal parameters
     * @param copySlots list of slots of parent substitution that will be used 
     *        to fill goal substitution
     * @param goalId goal identifier
     */
    public GoalFactory(
            TriggerTemplate gTrigger,
            TriggerTemplate gCtx,
            Substitution s,
            Map<String, Object> parameters,
            List<Pair<String,String>> copySlots,
            String goalId
            ){
        this.gTrigger = gTrigger;
        this.gCtx = gCtx;
        this.substitutionDraft = s;
        this.parameters = parameters;
        this.copySlots = copySlots;
        this.goalId = goalId;
    }
    
    /**
     * Creates new goal instance
     * @param old Substitution that holds data used to fill some slots of this 
     *            goal. This substitution is typically substitution of parent 
     *            process
     * @return new goal instance
     */
    public Goal createGoal(Substitution old){
        Goal ret = new Goal();
        ret.setGcontext(gCtx);
        ret.setGtrigger(gTrigger);
        Substitution newOne = substitutionDraft.duplicateSubstitution(true);
        for(Pair<String,String> pair:copySlots){
            // Is it attribute?
            if (pair.first().indexOf("->")>=0) {
                String[] strs = pair.first().split("->");
                if (strs.length != 2) {
                    Log.addMessage("Unexpected source propagation: \"" +
                            pair.first() + "\"", Log.SEVERE, "", goalId, "");
                    return null;
                }
                Source src = old.getSource(strs[0]);
                if (src == null) {
                    Log.addMessage("Source not found: \"" +
                            strs[0] + "\"", Log.SEVERE, "", goalId, "");
                    return null;
                }
                IveObject io=src.getObject();
                if (io==null){
                    Log.addMessage("Empty source: \"" +
                            strs[0] + "\"", Log.SEVERE, "", goalId, "");
                    return null;
                }
                AttributeValue val = io.getAttribute(strs[1]);
                if (val == null || !(val instanceof AttrObject)) {
                    Log.addMessage("Attribute not found in the source " +
                            "object: \"" + strs[1] + "\"",
                            Log.SEVERE, "", goalId, "");
                    return null;
                }
                newOne.setSource(pair.second(),
                        new SourceImpl(((AttrObject)val).getValue()));
                Log.addMessage("Source propagated: \"" +
                        pair.first() + "\"", Log.INFO, "", goalId, "");
                continue;
                
            }
            
            
            // We share everything at this level. We break the sharing during
            // the Process to Goal associtaion in the Genius.
            Slot oldSlot = old.getSlots().get(pair.first());
            
            if (oldSlot == null) {
                Log.addMessage("Original slot \"" + pair.first() + 
                        "\" is missing. Unable to propagate to the slot \"" + 
                        pair.second() + "\".", Log.SEVERE, "", goalId, "");
                return null;
            }
            newOne.getSlots().put(pair.second(), oldSlot);
        }
        
        ret.setSubstitution(newOne);
        ret.setGoalID(goalId);
        
        Map<String, Object> params = new java.util.HashMap<String, Object>();
        params.putAll(parameters);
        ret.setParameters(params);
        return ret;
    }
}

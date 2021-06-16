/* 
 *
 * IVE Demo World
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
import cz.ive.genius.goaltree.RuleTreeNode;
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.attributes.AttrObject;
import java.util.*;

/**
 * Area genius specialized for the mine area.
 *
 * @author Ondra
 */
public class MineGenius extends AreaGeniusImpl {
    
    /** Mine down work process */
    static protected Set<String> downWork = new HashSet<String>(1);
    
    /** Mine up work process */
    static protected Set<String> upWork = new HashSet<String>(1);
    
    static {
        downWork.add("MineDownWorkTaxi");
        upWork.add("MineUpWorkTaxi");
    }
    
    /** Miners working in the MineDown area. */
    protected Set<String> mineDownMiners = new HashSet<String>();
    
    /** Miners working in the MineUp area. */
    protected Set<String> mineUpMiners = new HashSet<String>();
    
    /**
     * Creates a new instance of MineGenius
     *
     * @param id Id to be assigned to the new genius.
     * @param areaId Id of the area assigned.
     */
    public MineGenius(String id, String areaId) {
        super(id, areaId);
        specialGoals.add("MineWorkTaxiGoal");
    }
    
    protected Set<String> selectSpecialProcess(RuleTreeNode rule) {
        String goalId = rule.getGoal().getGoalID();
        
        if (goalId.equals("MineWorkTaxiGoal")) {
            AcceptedGoal info = getInfo(rule);
            String id = info.actor.getId();
            
            if (mineUpMiners.contains(id))
                return downWork;
            return upWork;
        }
        return null;
    }
    
    protected void actorGained(IveObject actor) {
        if (mineDownMiners.size() <= mineUpMiners.size()) {
            mineDownMiners.add(actor.getId());
        } else {
            mineUpMiners.add(actor.getId());
        }
    }
    
    protected void actorLost(IveObject actor) {
        String id = actor.getId();
        mineDownMiners.remove(id);
        mineUpMiners.remove(id);
    }
}

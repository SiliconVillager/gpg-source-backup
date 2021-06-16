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
import java.util.*;

/**
 * Area genius specialized for the pub area. It splits actors between the
 * BallroomJoy and BarJoy.
 *
 * @author ondra
 */
public class PubGenius extends AreaGeniusImpl {
    
    /** Guests in the ballroom. */
    protected Set<String> ballroomGuests = new HashSet<String>();
    
    /** Guests in the bar */
    protected Set<String> barGuests = new HashSet<String>();
    
    /** Bar joy process */
    static protected Set<String> barJoy = new HashSet<String>(1);
    
    /** Ballroom joy process */
    static protected Set<String> ballroomJoy = new HashSet<String>(1);
    
    static {
        barJoy.add("BarJoyTaxi");
        ballroomJoy.add("BallroomJoyTaxi");
    }
    
    /**
     * Creates a new instance of PubGenius
     *
     * @param id Id to be assigned to the new genius.
     * @param areaId Id of the area assigned.
     */
    public PubGenius(String id, String areaId) {
        super(id, areaId);
        specialGoals.add("PubJoyTaxiGoal");
    }
    
    protected Set<String> selectSpecialProcess(RuleTreeNode rule) {
        String goalId = rule.getGoal().getGoalID();
        
        if (goalId.equals("PubJoyTaxiGoal")) {
            AcceptedGoal info = getInfo(rule);
            String id = info.actor.getId();
            
            barGuests.remove(id);
            ballroomGuests.remove(id);
            
            if (barGuests.size() * 2 < ballroomGuests.size()) {
                barGuests.add(id);
                return barJoy;
            } else if (barGuests.size() > ballroomGuests.size() * 2 ||
                    Math.random() > 0.5) {
                ballroomGuests.add(id);
                return ballroomJoy;
            }
            
            barGuests.add(id);
            return barJoy;
        }
        return null;
    }
    
    protected void actorGained(IveObject actor) {
        if (barGuests.size() <= ballroomGuests.size()) {
            barGuests.add(actor.getId());
        } else {
            ballroomGuests.add(actor.getId());
        }
    }
    
    protected void actorLost(IveObject actor) {
        String id = actor.getId();
        barGuests.remove(id);
        ballroomGuests.remove(id);
    }
}

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
 
package cz.ive.trigger;

import cz.ive.iveobject.IveIdImpl;
import cz.ive.messaging.Listener;
import cz.ive.process.*;
import cz.ive.iveobject.IveObject;
import cz.ive.location.WayPoint;
import cz.ive.valueholders.FuzzyValueHolder;
import java.io.Serializable;
import java.util.Map;

/**
 * This class extends MoveGoalTrigger to hunt the given object.
 * @author pavel
 */
public class HuntGoalTrigger extends MoveGoalTrigger 
        implements Trigger, Listener, Serializable {

    /** The hunted object */
    protected IveObject huntedObject;
    
    /** Last position of the hunted object */
    protected WayPoint lastHuntedPos;
    
    /** 
     * Determines whether the target has moved so much, that it is not 
     * solvable on this lod level. 
     */
    protected boolean targetFatallyMoved;

    /** Creates a new instance of HuntGoalTrigger */
    public HuntGoalTrigger(Substitution givenSources,
            Map<String, Object> parameters) {
        super(givenSources, parameters);
        huntedObject = givenSources.getSource("targetObject").getObject();
        lastHuntedPos = huntedObject.getPosition();
        targetFatallyMoved = false;
    }
    
    /**
     * Computes trigger value from the instantiated trigger
     */
    public short getValue() {
        
        if (huntedObject == null) {
            /* this happens in constructor of the superclass. */
            return super.getValue();
        }
        
        WayPoint huntedPos = huntedObject.getPosition();

        if (targetFatallyMoved) {
            return FuzzyValueHolder.False;
        }
        
        if (huntedPos != lastHuntedPos) {
            /* target object has moved */

            if (huntedPos == actor.getPosition()) {
                return FuzzyValueHolder.False;
            }
            
            IveObject solvableArea = 
                    actor.getPosition().getLeastCommonParent(
                        new IveIdImpl(huntedPos.getLeastCommonParentId(
                            lastHuntedPos.getId())));
            
            while (huntedPos.isParent(solvableArea)) {

                if (actor.getPosition().getId().equals(solvableArea.getId())) {
                    break;
                }

                solvableArea = solvableArea.getChildPreceeding(
                        actor.getPosition().getId().toString());
            }
            int solvableLod = solvableArea.getLod();

            if (lod > solvableLod) {
                targetFatallyMoved = true;
                return FuzzyValueHolder.False;
            }
            
            if (lod == solvableLod) {
                state = MoveState.GOING_AHEAD;
                finalTarget = huntedPos;
                if ((nextStepTarget != actor.getPosition()) && 
                        actor.isSubstantial()) {
                    ((WayPoint) nextStepTarget).unreserveSpace(); 
                }

                prevStepTarget = actor.getPosition();
                recalculatePath();
                prepareNextStep();
            }
            
            lastHuntedPos = huntedPos;
        }

        return super.getValue();
    }
    
    /**
     * Computes trigger value without instantiating the trigger
     *
     * @param sources Sources to be used for evaluation
     */
    static public short getValue(Substitution sources) {
        Substitution testSources = sources;
        
        return MoveGoalTrigger.getValue(testSources);
    }
}

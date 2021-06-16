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

import cz.ive.exception.OntologyNotSupportedException;
import cz.ive.logs.Log;
import java.util.*;
import cz.ive.process.*;
import cz.ive.messaging.*;
import cz.ive.ontology.*;
import cz.ive.iveobject.*;
import cz.ive.location.*;
import cz.ive.sensors.Sensor;
import cz.ive.simulation.CalendarPlanner;
import cz.ive.valueholders.FuzzyValueHolder;

/**
 * This is a MoveGoalTrigger. It expects sources 'actor' and 'targetPosition'
 * to be ent and waypoint phantoms.<br>
 * The trigger finds a path and changes the sources as the actor is moving.
 * If it cannot do a step because the step target is occupied, at first 
 * it tries to wait for a random time if it won't get empty. If it is still
 * occupied, it tries to sidestep - performs a step somewhere to let the 
 * occupier move away. Then it wait whether the companion will step to the 
 * released WayPoint. If it does, it's pushing us and we make another sidestep. 
 * We try to find a new way all the time.<br>
 * Waiting for something is a little tricky. The actor makes steps on
 * it's WayPoint, not moving, steping in the place. These steps take a ten
 * times shorter time then regular steps, which enables them to differentiate
 * the waiting times and to react fastly on changes.<br>
 * Another trick is, that each WayPoint can say, if it is occupied or not, 
 * and moreover, if the occupier is going away or if some occupier is going
 * in. This allows trigger to prevent failing steps when two ents are stepping
 * on the same WayPoint. 
 *
 * @author honza
 */
public class MoveGoalTrigger  extends SyncHook
        implements Trigger, Listener,
        java.io.Serializable {
    
    /** Sources of the trigger */
    Substitution sources;
    /** Value of the trigger */
    short triggerValue = -1;
    /** Final target of the way */
    IveObject finalTarget;
    /** Target of our path */
    WayPoint currentTarget;
    /** Target of the next step */
    IveObject nextStepTarget;
    /** Actor of the movement */
    IveObject actor;
    /** The path */
    List<WayPoint> path;
    /** Hook notifying actor's position change */
    Hook positionHook;
    /** Hook notifying target source change */
    Hook targetHook;
    /** Roundhook notifying the end of the simulation round */
    Hook roundHook;
    /** Lod of this movement */
    int lod = -1;
    /** State of the movement */
    MoveState state;
    /** Previous position of the actor */
    WayPoint previousPosition;
    /** Previous position of the actor during sidestepping */
    WayPoint previousSidePos;
    /** Position from which the actor is sidestepping */
    WayPoint sideSourcePos;
    /** Determines whether the path is clear - no WayPoint on it is occupied */
    boolean clearPath;
    /** Number of steps to wait for something */
    int waitStepCount;
    /** Target of the previous step */
    IveObject prevStepTarget;
    /** 
     * Phantoms of WayPoints that seems to be occupied (the step on them
     * has failed. 
     */
    Set<WayPoint> occupiedPhantoms;
    /** 
     * Deremines whether we are sidestepping from the edge of location - we do
     * not see the space state of the WayPoint we are sidestepping from.
     */
    boolean sideStepFromPhantom;
    /** Remembers WayPoint with place reserved for our actor. */
    protected WayPoint reservedWP;
    /** Remembers WayPoint marked as leaving by our actor. */
    protected WayPoint leavingWP;
    
    
    /** States of the movement */
    enum MoveState {
        /** Moving regularily forward */
        GOING_AHEAD,
        /** Retrying a step */
        RETRYING_STEP,
        /** Retrying a step to phantom on substantial level */
        RETRYING_STEP_PHANTOM,
        /** Sidestepping */
        SIDE_STEPPING,
    }
    
    /**
     * Are we in the state of permanent failure? (no path found)
     */
    protected boolean failure = false;

    /**
     * Are we in the end fo the way?
     */
    protected boolean finish = false;
    
    /** Creates a new instance of MoveGoalTrigger */
    public MoveGoalTrigger(Substitution givenSources,
            Map<String, Object> parameters) {
        super();
        prevStepTarget = null;
        sideStepFromPhantom = false;
        reservedWP = null;
        leavingWP = null;
        occupiedPhantoms = new HashSet<WayPoint>();
        state = MoveState.GOING_AHEAD;
        sources = givenSources;
        actor = ObjectMap.instance().getObject(
                sources.getSource("actor").getObject().getId());
        if (actor == null) {
            Log.addMessage("Actor does not exist.", Log.SEVERE,
                    sources.getSource("actor").getObject().getId(),
                    getClass().getName(), "");
            return;
        }
        positionHook = actor.getAttribute("position");
        positionHook.registerListener(this);
        targetHook = sources.getSource("targetPosition");
        targetHook.registerListener(this);
        finalTarget = sources.getSource("targetPosition").getObject();
        if (finalTarget == null) {
            failure = true;
        }
        triggerValue = getValue();
        
        Integer lod = (Integer)parameters.get("lod");
        if (lod != null)
            this.lod = lod.intValue();
        
        if (triggerValue == FuzzyValueHolder.True) {
            recalculatePath();
        }
        
        Log.addMessageToObject("MGT: Creating moveGoalTrigger at lod "+this.lod+
                " with final target "+finalTarget.getId(), Log.FINE, actor);
        
        changed(null);
        roundHook = null;

        parameters.put("finalTarget", finalTarget);
        parameters.put("lod", new Integer(this.lod));
    }
    
    public void delete() {
        Log.addMessage("Trigger deleted.", Log.FINEST, "",
                getClass().getName(), "");
        positionHook.unregisterListener(this);
        targetHook.unregisterListener(this);
        if (roundHook != null) {
            roundHook.unregisterListener(this);
        }
        unprepareSpace();
    }
    
    /**
     * Sets the spaces to correspond to actor's intention to make a step.
     * @param from The actor's position
     * @param to The target of the step
     */
    protected void prepareSpace(WayPoint from, WayPoint to) {
        if (to != null) {
            to.reserveSpace();
        }
        if (from != null) {
            from.leavingSpace();
        }
        reservedWP = to;
        leavingWP = from;
    }
    
    /**
     * Takes back effects of prepareSpace().
     */
    protected void unprepareSpace() {
        if (reservedWP != null) {
            reservedWP.unreserveSpace();
        }
        if (leavingWP != null) {
            leavingWP.unleavingSpace();
        }
        reservedWP = null;
        leavingWP = null;
    }
    
    /**
     * Calculates the path at apropriate LOD level (first that needs it).
     */
    protected void recalculatePath() {
        WayPoint position = actor.getPosition();
        WayPoint comParent = (WayPoint)position.getLeastCommonParent(
                finalTarget);
        clearPath = true;
        
        // If the lod is not specified, find the first differenc in parents
        // and move on this lod level.
        if (lod == -1) {
            // Is it one of singular cases?
            if (comParent.getId().equals(position.getId()) ||
                    comParent.getId().equals(finalTarget.getId())) {
                // No path, we are already there.
                path = null;
                nextStepTarget = null;
                clearPath = false;
                return;
            }
            
            // No it is a regular case, lets count the path.
            currentTarget = (WayPoint)comParent.getChildPreceeding(
                    finalTarget.getId());
            position = (WayPoint)comParent.getChildPreceeding(
                    position.getId());
            
            List<WayPoint> targetList = new LinkedList<WayPoint>();
            targetList.add(currentTarget);
            path = PathFinderImpl.instance().findPath(position,
                    targetList, actor.getKind(), actor.isSubstantial());
            if (path == null) {
                clearPath = false;
                path = PathFinderImpl.instance().findPath(position,
                        targetList, actor.getKind(), false);
            }
            lod = position.getLod();
            return;
        }
        
        WayPoint area = null;
        
        // So the lod is specified, find the way on the given lod.
        while (comParent.getLod() < lod) {
            area = comParent;
            
            comParent = (WayPoint)comParent.getChildPreceeding(
                    position.getId());
        }
        
        if (area == null) {
            // We do not differ at this level.
            path = null;
            nextStepTarget = finalTarget;
            return;
        }
        
        WayPoint targetArea = null;
        
        if (finalTarget.isParent(area)) {
            WayPoint target = (WayPoint)area.getChildPreceeding(
                    finalTarget.getId());

            List<WayPoint> targetList = new LinkedList<WayPoint>();
            targetList.add(target);

            path = PathFinderImpl.instance().findPath(comParent,
                    targetList, actor.getKind(), actor.isSubstantial());
            if (path == null) {
                clearPath = false;
                path = PathFinderImpl.instance().findPath(comParent,
                        targetList, actor.getKind(), false);
            }
        } else {
            for (Joint j : area.getNeighbours()) {
                if (j.target.isParent(finalTarget) ||
                        finalTarget.isParent(j.target)) {
                    targetArea = j.target;
                    break;
                }

            }

            if (targetArea == null) {
                Log.addMessage("MGT: No neighbour on the way found.", 
                        Log.SEVERE, "", "", "");
                clearPath = false;
                return;
            }

            List<WayPoint> border = ((Area)area).getBorderWayPoints(targetArea);
            border.removeAll(occupiedPhantoms);

            path = PathFinderImpl.instance().findPath(comParent,
                    border, actor.getKind(), actor.isSubstantial());
            if (path == null) {
                clearPath = false;
                path = PathFinderImpl.instance().findPath(comParent,
                        border, actor.getKind(), false);
            }
            finalTarget = targetArea;
        }
    }
    
    /**
     * Tests the WayPoint if it is steppable.
     */
    protected boolean isOccupied(WayPoint wp) {
        if ((wp.getSpaceState() == WayPoint.SpaceState.OCCUPIED) || 
                (wp.getSpaceState() == WayPoint.SpaceState.GOING_OCCUPIED)) {
            return true;
        }
        if (wp.isPhantom() && occupiedPhantoms.contains(wp)) {
            return true;
        }
        return false;
    }
    
    /**
     * Prepares the next step WayPoint and sets it as a source.
     */
    protected void prepareNextStep() {
        WayPoint position = actor.getPosition();
        finish = false;
        
        if (previousPosition != position) {
            occupiedPhantoms.clear();
        } else {
            WayPoint prevTrg = 
                    (WayPoint) sources.getSource("targetPosition").getObject();
            if ((lod == position.getLod()) && 
                    (position.isSubstantial()) && 
                    (prevTrg.isPhantom()) && 
                    (!position.isParent(prevTrg))) {
                occupiedPhantoms.add(prevTrg);
            }
        }
        previousPosition = position;
        
        switch (state) {
            case GOING_AHEAD:
                
                boolean prevStepDone = position.isParent(prevStepTarget);

                if (position.isParent(finalTarget)) {
                    /* end of the way */
                    finish = true;
                    return;
                }
                
                if (path == null && nextStepTarget == null) {
                    // Nothing to do. Is that right?
                    Log.addMessage("MGT: We have no target WayPoint, is that " +
                            "right?", Log.WARNING, "", "MoveGoalTrigger", "");
                    triggerValue = FuzzyValueHolder.False;
                    unprepareSpace();
                    sources.getSource("targetPosition").setObject(null);
                    failure = true;
                    return;
                } else if (path != null) {
                    if (path.size() == 0) {
                        /* last step on the way (but can be on higher level) */
                        if (prevStepDone) {
                            nextStepTarget = finalTarget;
                            if (!finalTarget.isParent(position.getPosition())) {
                                return;
                            }
                            path = null;
                        }
                    } else {
                        
                        /* regular case */
                        if (nextStepTarget == null || prevStepDone) {
                            /* previous step done - 
                             * retrieving next step from path */
                            if (path.get(0).getSpaceState() != 
                                    WayPoint.SpaceState.GOING_EMPTY) {
                                
                                /* retrieve the next step */
                                nextStepTarget = path.remove(0);
                                Log.addMessageToObject("MGT: Retrieving next " +
                                        "step from path at lod "+this.lod+
                                        ": "+nextStepTarget.getId(), Log.FINE, 
                                        actor);
                            } else {
                                /* the step target is occupied, but going to be
                                 * empty - wait for it */
                                nextStepTarget = position;
                                Log.addMessageToObject("MGT: Waiting for " +
                                        "empty WayPoint " + path.get(0).getId(), 
                                        Log.FINE, actor);
                            }
                        } else if (actor.getLod() == lod) {
                            sources.getSource("targetPosition").setObject(null);
                            triggerValue = FuzzyValueHolder.False;
                            failure = true;
                            Log.addMessageToObject("MGT: The atomic step to " +
                                    nextStepTarget.getId()+" has failed.", 
                                    Log.FINE, actor);
                            unprepareSpace();
                            return;
                        } 
                    }
                }

                if ((nextStepTarget != null) &&
                        (nextStepTarget != position) &&
                        (isOccupied((WayPoint) nextStepTarget)) &&
                        ((nextStepTarget.isPhantom()) ||
                            ((nextStepTarget.getLod() <= lod) &&
                            (nextStepTarget.getObjectState() == 
                                IveObject.ObjectState.VALID)))) {

                    /* the step target is occupied - start a waiting for step
                     * retry */
                    waitStepCount = (int)(1+Math.random()*
                            (20 + (nextStepTarget.isPhantom() ? 40 : 0)));
                    state = MoveState.RETRYING_STEP;
                    Log.addMessageToObject("MGT: The step target " +
                            nextStepTarget.getId()+" is occupied. I'll try to "+
                            "wait for a while.", Log.FINE, actor);
                    sources.getSource("targetPosition").setObject(
                            actor.getPosition());
                    return;
                }
                
                if ((prevStepDone) && (nextStepTarget != prevStepTarget)) {
                    /* Set a step target (only in needed cases) */
                    if (actor.isSubstantial()) {
                        prepareSpace(position, (WayPoint) nextStepTarget);
                    }
                    sources.getSource("targetPosition").setObject(
                            nextStepTarget);
                    prevStepTarget = nextStepTarget;
                }
                return;
            
            case RETRYING_STEP:

                if ((nextStepTarget != null) &&
                        isOccupied((WayPoint) nextStepTarget)) {
                    
                    /* the target WayPoint is still occupied */
                    
                    if (waitStepCount == 0) {
                        /* our patience is at the end */
                        
                        if (nextStepTarget.isPhantom()) {
                            state = MoveState.RETRYING_STEP_PHANTOM;
                            sources.getSource("targetPosition").setObject(
                                nextStepTarget);
                            prevStepTarget = nextStepTarget;
                            return;
                        }
                        
                        if (nextStepTarget == finalTarget) {
                            /* this is the last step */
                            sources.getSource("targetPosition").setObject(null);
                            triggerValue = FuzzyValueHolder.False;
                            failure = true;
                            state = MoveState.GOING_AHEAD;
                            Log.addMessageToObject("MGT: The final step to " +
                                    nextStepTarget.getId()+" has failed.", 
                                    Log.FINE, actor);
                            unprepareSpace();
                            return;
                        }

                        recalculatePath();
                        if ((path == null) || isOccupied(path.get(0))) {
                            
                            /* We need to go through an occupied WayPoint -
                             * try to sidestep to let the occupier go from
                             * here */
                            sideSourcePos = (WayPoint)nextStepTarget;
                            Log.addMessageToObject("MGT: The step target " +
                                    nextStepTarget.getId()+" is still occupied"+
                                    ". I'm going to sidestep form here.", 
                                    Log.FINE, actor);
                            state = MoveState.SIDE_STEPPING;
                            prepareNextStep();
                            return;
                        }

                        /* another path found */
                        state = MoveState.GOING_AHEAD;
                        nextStepTarget = null;
                        prevStepTarget = null;
                        Log.addMessageToObject("MGT: I found a new path, " +
                                "let's go. ", Log.FINE, actor);
                        prepareNextStep();
                    } 
                } else {
                    /* The target WayPoint became empty */
                    Log.addMessageToObject("MGT: The step to " +
                            nextStepTarget.getId()+" successfully retried.",
                            Log.FINE, actor);
                    state = MoveState.GOING_AHEAD;
                    sources.getSource("targetPosition").setObject(
                            nextStepTarget);
                    if (actor.isSubstantial()) {
                        prepareSpace(position, (WayPoint) nextStepTarget);
                    }
                    prevStepTarget = nextStepTarget;
                }
                waitStepCount--;
                return;
                
            case RETRYING_STEP_PHANTOM: 
                if (position == prevStepTarget) {
                    Log.addMessageToObject("MGT: The step to phantom " +
                            nextStepTarget.getId()+" successfully retried.",
                            Log.FINE, actor);
                    state = MoveState.GOING_AHEAD;
                    prepareNextStep();
                } else {
                    sideSourcePos = (WayPoint)nextStepTarget;
                    Log.addMessageToObject("MGT: The step phantom target " +
                            nextStepTarget.getId()+" is still occupied"+
                            ". I'm going to sidestep form here.", 
                            Log.FINE, actor);
                    state = MoveState.SIDE_STEPPING;
                    prepareNextStep();
                    return;
                }
                return;
                
            case SIDE_STEPPING:

                recalculatePath();
                
                if ((path == null) || (!clearPath) || sideStepFromPhantom) {
                /* We have not a clear path to our target */
                    
                    if (isOccupied(sideSourcePos)) {
                        /* Somebody stepped to the WayPoint cleard by our
                         * sidestep - make another one */
                        
                        boolean goingEmpty = false;
                        
                        /* recognize possibilities */
                        List<WayPoint> candidates = new Vector<WayPoint>();
                        List<WayPoint> farCandidates = new Vector<WayPoint>();
                        for (Joint j : position.getNeighbours(
                                actor.getKind())) {
                            
                            if (j.target.isPhantom()) {
                                continue;
                            }
                            
                            if (j.target.getSpaceState() == 
                                    WayPoint.SpaceState.EMPTY) {
                                candidates.add(j.target);
                            } else if (j.target.getSpaceState() == 
                                    WayPoint.SpaceState.GOING_EMPTY) {
                                goingEmpty = true;
                            } 
                        }
                        
                        if (candidates.size() > 1) {
                            /* heuristic - try not to step to the WayPoint
                             * we came from */
                            candidates.remove(previousSidePos);
                        }

                        for (WayPoint wp : candidates) {
                            
                            /* heuristic - try to step far from the occupied
                             * WayPoint */
                            if (! wp.isNeighbour(sideSourcePos)) {
                                farCandidates.add(wp);
                            }
                        }
                        if (farCandidates.size() > 0) {
                            candidates = farCandidates;
                        }

                        if (candidates.size() > 0) {
                            /* prepare the sidestep */
                            WayPoint sideTarget = candidates.get(
                                   (int) (Math.random()*(candidates.size()-1)));

                            if (sideSourcePos.isPhantom() || 
                                    sideTarget.isPhantom() || 
                                    !position.isParent(
                                        sideTarget.getPosition())) {
                                sideStepFromPhantom = true;
                            } else {
                                sideStepFromPhantom = false;
                            }

                            previousSidePos = position;
                            sideSourcePos = position;
                            sources.getSource("targetPosition").
                                    setObject(sideTarget);
                            if (actor.isSubstantial()) {
                                prepareSpace(position, sideTarget);
                            }
                            waitStepCount = (int) (1+Math.random()*20);
                            if (sideStepFromPhantom) {
                                waitStepCount += 40;
                            }
                            return;
                        }
                        
                        if (!goingEmpty) {
                            /* No possibility, but some is going to be empty -
                             * wait for it */
                            state = MoveState.GOING_AHEAD;
                            sources.getSource("targetPosition").setObject(null);
                            triggerValue = FuzzyValueHolder.False;
                            failure = true;
                            unprepareSpace();
                            return;
                        }
                        
                    } else if (sideSourcePos.getSpaceState() ==
                                WayPoint.SpaceState.GOING_EMPTY) {

                        /* The one who stays on the WayPoint cleared by 
                         * our sidestep is going to leave, wait for it */
                        sources.getSource("targetPosition").setObject(position);
                        return;
                        
                    } else {
                        /* noone has entered the WayPoint cleared by our
                         * sidestep */
                        
                        if (waitStepCount == 0) {
                            /* our patience is at the end */
                            Log.addMessageToObject("MGT: Nobody has entered " +
                                    " the sidestepped wp, let's go.", 
                                    Log.FINE, actor);
                            state = MoveState.GOING_AHEAD;
                            nextStepTarget = path.remove(0);
                            prevStepTarget = nextStepTarget;
                            
                            if (actor.isSubstantial()) {
                                prepareSpace(position, 
                                        (WayPoint) nextStepTarget);
                            }
                            sources.getSource("targetPosition").setObject(
                                    nextStepTarget);
                            return;
                        }
                        waitStepCount--;
                        sources.getSource("targetPosition").setObject(position);
                    }
                    return;
                } else {
                    /* We have found another path */
                    state = MoveState.GOING_AHEAD;
                    sources.getSource("targetPosition").setObject(
                            path.get(0));
                    nextStepTarget = path.remove(0);
                    prevStepTarget = nextStepTarget;
                    if (actor.isSubstantial()) {
                        prepareSpace(position, (WayPoint) nextStepTarget);
                    }
                    Log.addMessageToObject("MGT: New path found, " +
                            "let's go.", Log.FINE, actor);
                    return;
                }
        }
        
    }
    
    
    /**
     * Gets first parent of the given WayPoint, that is neighbour of the
     * second given WayPoint (possibly a phantom)
     *
     * @param origin WayPoint from which to start the search upwards.
     * @param target WayPoint (possibly a phantom) which might by a neighbour.
     * @return the first parent that is neighbour of the target WayPoint or
     *      <code>null</code> if no such exists. The returned WayPoint may be
     *      even the given origin WayPoint.
     */
    static public WayPoint getFirstNeighbourParent(WayPoint origin, 
            IveId target) {
        String id = origin.getLeastCommonParentId(target.getId());
        WayPoint actual = origin;
        
        while (actual != null && !actual.getId().equals(id)) {
            if (isNeighbour(actual.getNeighbours(), target)) {
                return actual;
            }
            actual = (WayPoint)actual.getParent();
        }
        return null;
    }
    
    
    private static boolean isNeighbour(List<Joint> joints, IveId waypoint) {
        if (joints == null)
            return false;
        for (Joint joint : joints) {
            if (joint.target.isParent(waypoint) ||
                    waypoint.isParent(joint.target)) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * Added by pavel
     * During placing object somewhere, it's position can change several times.
     * The roundHook is used to wait for the end of the round, where we will
     * know the final position.
     */
    
    public void changed(Hook h) {
        if (h == targetHook) {
            if (sources.getSource("targetPosition").getObject() == null) {
                Log.addMessageToObject("Propagating MoveGoal failure to lod "
                        +lod, Log.FINE, actor);
                failure = true;
                triggerValue = getValue();
                notifyListeners();
            }
            return;
        }
        
        if ((h == roundHook) || (h == null)) {
            // the actor has moved
            short oldValue = triggerValue;
            triggerValue = getValue();
            
            if (triggerValue == FuzzyValueHolder.True) {
                if (oldValue == FuzzyValueHolder.False) {
                    recalculatePath();
                }
                // He's still on his way
                prepareNextStep();
                triggerValue = getValue();
            }
            
            if (oldValue != triggerValue) {
                notifyListeners();
            }
            
            if ((roundHook != null) && (h == roundHook)) {
                roundHook.unregisterListener(this);
                roundHook = null;
            }
        } else {
            if (roundHook == null) {
                roundHook = CalendarPlanner.instance().getRoundHook(
                        CalendarPlanner.RoundHookPosition.BEFORE_WORLD);
                roundHook.registerListener(this);
            }
        }
    }
    
    public void canceled(Hook h) {
        // Nothing to be done here.
    }
    
    
    /**
     * Computes trigger value from the instantiated trigger
     */
    public short getValue() {
        if (failure || finish) {
            return FuzzyValueHolder.False;
        }
                
        return (short)(
                (actor.getPosition().isParent(finalTarget) ||
                finalTarget.isParent(actor.getPosition())) ?
                    FuzzyValueHolder.False : FuzzyValueHolder.True
                );
    }
    
    /**
     * Computes trigger value without instantiating the trigger
     *
     * @param sources Sources to be used for evaluation
     */
    static public short getValue(Substitution sources) {
        IveObject actor = ObjectMap.instance().getObject(
                sources.getSource("actor").getObject().getId());
        if (actor == null) {
            Log.addMessage("Actor does not exist.", Log.SEVERE,
                    sources.getSource("actor").getObject().getId(),
                    MoveGoalTrigger.class.getName(), "");
            return FuzzyValueHolder.False;
        }
        IveObject finalTarget = sources.getSource("targetPosition").getObject();

        return (short)(
                (actor.getPosition().isParent(finalTarget) ||
                finalTarget.isParent(actor.getPosition())) ?
                    FuzzyValueHolder.False : FuzzyValueHolder.True
                );
    }
    
    static public OntologyToken getOntoValue(Substitution sources) {
        return new SingleToken("java.Short",
                new Short(getValue(sources)));
    }
    
    public OntologyToken value() {
        return new SingleToken("java.Short",
                new Short(triggerValue));
    }
    
    public Object getData(String ontology) 
            throws cz.ive.exception.OntologyNotSupportedException {
        throw new OntologyNotSupportedException("Ontology \""+ontology+
                "\" is not supported.");
    }
    
    public String[] getOntologies() {
        return null;
    }
    
    public boolean supports(String ontology) {
        return false;
    }
    
    public void changeSensors(List<Sensor> sensors){}
}

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
 
package cz.ive.process;

import cz.ive.IveApplication;
import cz.ive.exception.AtomicCommitException;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.location.Joint;
import cz.ive.location.WayPoint;
import cz.ive.logs.Log;
import cz.ive.valueholders.FuzzyValueHolder;

/**
 * Stand up process template. It makes the actor stand up from the chair,
 * he is sitting on.
 *
 * @author ondra
 */
public class Unmount extends CommonProcessTemplate {
    
    /** Length of the StandUp process, 2 s-seconds. */
    protected static final int LENGTH = 2000;
    
    protected String objectClass="/object/mount";
    
    /** Creates a new instance of StandUp */
    public Unmount() {
    }
    
    /**
     * Moves actor from the chair and sets its "empty" attribute to true while
     * changing actor's attribute "sitting" to false and "state" to 0 which
     * corresponds to the standing pose.
     *
     * @param execution ProcessExecution representing the running process
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution)
    throws AtomicCommitException{
        
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        IveObject actor = getCheckedSourceObject("actor", true, execution);
        
        IveObject mountedTo = actor.getMaster();
        if (!ObjectClassTree.instance().getObjectClass(
                objectClass).isInside(mountedTo) && mountedTo instanceof WayPoint) {
            mountedTo = null;
        } else if (!ObjectClassTree.instance().getObjectClass(
                objectClass).isInside(mountedTo)) {
            Log.addMessage("The actor is not placed on the chair.", Log.WARNING,
                    actor.getId(), "Unmount", actor.getPosition().getId());
            return ProcessResult.FAILED;
        }
        
        AttrFuzzy emptyAttr = null;
        AttrFuzzy standingAttr;
        AttrInteger stateAttr;
        boolean standing;
        boolean empty;
        try {
            standingAttr = (AttrFuzzy)actor.getAttribute("standing");
            if (standingAttr==null){
                //the process SitDown could not finished with result OK
                //this is here to enable Cart to Follow
                return ProcessResult.OK;
            }
            stateAttr = (AttrInteger)actor.getAttribute("state");
            standing = standingAttr.getValue() == FuzzyValueHolder.True;
            
            if (standing || stateAttr.getValue() == Ent.STANDING) {
                Log.addMessage("We are not sitting.", Log.WARNING,
                        actor.getId(), "Unmount", actor.getPosition().getId());
                return ProcessResult.FAILED;
            }
            
            if (mountedTo != null) {
                emptyAttr = (AttrFuzzy)mountedTo.getAttribute("empty");
                empty = emptyAttr.getValue() == FuzzyValueHolder.True;
                if (empty) {
                    Log.addMessage("The chair is not occupied. But we are " +
                            "sitting on it?", Log.WARNING, actor.getId(),
                            "SitDown", actor.getPosition().getId());
                    return ProcessResult.FAILED;
                }
            }
        } catch (Exception ex) {
            IveApplication.printStackTrace(ex);
            Log.addMessage("Unexpected or missing attributes.", Log.WARNING,
                    actor.getId(), "StandUp", actor.getPosition().getId());
            return ProcessResult.WRONG_SOURCES;
        }
        
        if (mountedTo != null) {
            mountedTo.removeObject(actor);
            emptyAttr.setValue(FuzzyValueHolder.True);
            
            
            boolean success=false;
            for (Joint j:mountedTo.getPosition().getNeighbours()){
                WayPoint neighbour = j.target;
                if (neighbour.placeObject(actor, null, neighbour)) {
                    success=true;
                    break;
                }
            }
            if (!success){
                Log.addMessage("Actor could not be placed near the position " +
                        "of the chair.", Log.WARNING,
                        actor.getId(), "StandUp", actor.getPosition().getId());
                return ProcessResult.FAILED;
            }
        }
        standingAttr.setValue(FuzzyValueHolder.True);
        stateAttr.setValue(0);
        
        return ProcessResult.OK;
    }
    
    public long atomicLength(ProcessExecution execution) {
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}

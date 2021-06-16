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
import cz.ive.exception.OntologyNotSupportedException;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.location.WayPoint;
import cz.ive.ontology.OntologyToken;
import cz.ive.valueholders.FuzzyValueHolder;
import java.util.Map;

/**
 * Sit down process template. It makes the actor sit on the given chair.
 *
 * @author Ondra
 */
public class Mount extends CommonProcessTemplate {
    
    /** Length of the SitDown process, 2 s-seconds. */
    protected static final int LENGTH = 2000;
    
    
    
    protected String objectClass="/object/mount";
    
    
    /**
     * Moves actor onto the chair and changes its "empty" attribute to false
     * while the actor's "sitting" attribute to true and "state" attribute
     * to 1 which corresponds to the sitting pose.
     *
     * @param execution ProcessExecution representing the running process
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws AtomicCommitException{
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        IveObject actor = getCheckedSourceObject("actor", true, execution);
        IveObject to = getCheckedSourceObject("to",objectClass, true, execution);
        
        
        
        AttrFuzzy emptyAttr = getFuzzyAttribute(to,"empty");
        AttrFuzzy standingAttr = getFuzzyAttribute(actor,"standing");
        AttrInteger stateAttr = getIntegerAttribute(actor,"state");
        boolean empty;
        boolean standing;
        
        empty = emptyAttr.getValue() == FuzzyValueHolder.True;
        standing = standingAttr.getValue() == FuzzyValueHolder.True;
        
        if (!(actor.getMaster() instanceof WayPoint) ||
                stateAttr.getValue() != 0) {
            LogWarning("The actor is not on his foot.", actor);
            return ProcessResult.FAILED;
        }
        if (!standing) {
            LogWarning("We are already sitting.", actor);
            return ProcessResult.FAILED;
        }
        if (!empty) {
            LogWarning("The chair is occupied.", actor);
            return ProcessResult.FAILED;
        }
        
        
        emptyAttr.setValue(FuzzyValueHolder.False);
        standingAttr.setValue(FuzzyValueHolder.False);
        stateAttr.setValue(getState(execution.getParameters()));
        actor.getPosition().removeObject(actor);
        to.addObject(actor);
        
        return ProcessResult.OK;
    }
    
    protected int getState(Map<String,Object> params){
        int state;
        OntologyToken ot = (OntologyToken)params.get("state");
        if (ot==null) return 0;
        try {
            state = (Integer)ot.getData("java.int");
        } catch (OntologyNotSupportedException ex) {
            state = 0;
            IveApplication.printStackTrace(ex);
            LogWarning("Unexpected parameter");
        }
        return state;
    }
    
    public long atomicLength(ProcessExecution execution) {
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}

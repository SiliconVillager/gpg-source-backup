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

import cz.ive.exception.AtomicCommitException;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.AttrFuzzy;
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.location.WayPoint;
import cz.ive.logs.Log;
import cz.ive.valueholders.FuzzyValueHolder;

/**
 * MinerDance process template.
 *
 * @author ondra
 */
public abstract class MinerDance extends CommonProcessTemplate {
    
    /** Period of the state change, 1 s-seconds. */
    protected static final int PERIOD = 1000;
    
    protected int pictureNo;
    
    /**
     * Marks the pissoir as not empty, waits a bit and
     * unmarks it again.
     *
     * @param execution ProcessExecution representing the running process
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws AtomicCommitException{
        
        if (execution.getDuration() == 1) {            
            if (setState(execution, pictureNo))
                return ProcessResult.RUNNING;
            else
                return ProcessResult.FAILED;
        }
        
        if (setState(execution, 0))
            return ProcessResult.OK;
        else
            return ProcessResult.FAILED;
    }
    
    /**
     * Sets the actors state.
     *
     * @param execution Current process execution
     * @param state State number o be set.
     * @return <code>true</code> if succeeded.
     */
    protected boolean setState(ProcessExecution execution, int state) throws AtomicCommitException{
        IveObject actor = getCheckedSourceObject("actor", true, execution);
        
        AttrInteger stateAttr = getIntegerAttribute(actor, "state");
                        
        if (stateAttr.getValue() != state) {
            stateAttr.setValue(state);
        }
        return true;
    }
    
    public ProcessResult atomicStop(ProcessExecution execution) {
        try{
        setState(execution, 0);
        }catch(AtomicCommitException ex){}
        return ProcessResult.OK;
    }
    
    public void decreaseLOD(ProcessExecution execution) {
        atomicStop(execution);
    }
    
    public long atomicLength(ProcessExecution execution) {
        if (execution.getDuration()==0)
            return 1;
        return PERIOD - execution.getDuration()%PERIOD;
    }
}

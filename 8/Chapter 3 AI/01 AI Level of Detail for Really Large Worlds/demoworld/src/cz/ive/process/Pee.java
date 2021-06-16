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
import cz.ive.valueholders.FuzzyValueHolder;

/**
 * Pee process template, marks the pissoir as not empty, waits a bit and
 * unmarks it again.
 *
 * @author ondra
 */
public class Pee extends CommonProcessTemplate {
    
    /** Length of the process. 10 s-seconds. */
    protected static final int LENGTH = 10000;
    
    /** Creates a new instance of Pee */
    public Pee() {
    }
    
    /**
     * Marks the pissoir as not empty, waits a bit and
     * unmarks it again.
     *
     * @param execution ProcessExecution representing the running process
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws
            AtomicCommitException {
        if (execution.getDuration() == 1) {
            if(setEmpty(execution, false)){
                return ProcessResult.RUNNING;
            } else return ProcessResult.FAILED;
        }
        
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        if (setEmpty(execution, true)) {
            
            IveObject actor = getCheckedSourceObject("actor","/person",
                    true, execution);
            
            AttrFuzzy peeAttr = getFuzzyAttribute(actor,"peeDrive");
            
            if ((peeAttr.getValue()) == FuzzyValueHolder.False) {
                LogWarning("Actor does not want to pee.", actor);
                return ProcessResult.FAILED;
            }
            peeAttr.setValue(FuzzyValueHolder.False);
            return ProcessResult.OK;
        } else
            return ProcessResult.FAILED;
    }
    
    /**
     * Sets the pissoir as empty or not-empty.
     *
     * @param execution Current process execution
     * @param empty value to be assigned to the "empty" possior's attribute.
     * @return <code>true</code> if succeeded.
     */
    protected boolean setEmpty(ProcessExecution execution, boolean empty) throws
            AtomicCommitException {
        
        IveObject pissoir = getCheckedSourceObject("pissoir",true, execution);
        AttrFuzzy emptyAttr = getFuzzyAttribute(pissoir,"empty");
        
        if (emptyAttr.getValue() == FuzzyValueHolder.True && empty) {
            LogWarning("Pissoir is not occupied, but should be.",pissoir);
            return false;
        } else if (emptyAttr.getValue() == FuzzyValueHolder.False && !empty) {
            LogWarning("Pissoir is occupied.",pissoir);
            return false;
        }
        
        if (empty) {
            emptyAttr.setValue(FuzzyValueHolder.True);
        } else {
            emptyAttr.setValue(FuzzyValueHolder.False);
        }
        return true;
    }
    
    public ProcessResult atomicStop(ProcessExecution execution) {
        try {
            setEmpty(execution, true);
        } catch (AtomicCommitException e) {}
        return ProcessResult.OK;
    }
    
    public void decreaseLOD(ProcessExecution execution) {
        atomicStop(execution);
    }
    
    public long atomicLength(ProcessExecution execution) {
        if (execution.getDuration()==0)
            return 1;
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}

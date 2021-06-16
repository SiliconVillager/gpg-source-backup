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
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.attributes.AttrFuzzy;
import cz.ive.valueholders.FuzzyValueHolder;


/**
 * Process template for sleeping on lower LODs
 *
 * @author thorm
 */
public class SleepAtomic extends CommonProcessTemplate {
    
    /**
     * How much time takes sleeping
     */
    protected static final int SLEEP_LENGTH = 7 * 60 * 60 * 1000;

    /**
     * How much is tiredness decreased after 15 minutes of sleep
     * 15 minutes is used just for compatibility of constants with Vital process
     */
    protected static final int TIREDNESS_DEC_15MINUTES = 1170;
    
    /**
     * 15 minutes in miliseconds
     */
    protected static final int LENGTH15_MINUTES = 900000;
    
    /**
     * Use SLEEP_LENGTH to compute remaining time
     */
    public long atomicLength(ProcessExecution execution) {
        return Math.max(SLEEP_LENGTH - execution.getDuration(), 0);
    }
    

    /**
     * Decrease the tiredness of the actor.
     * Amount depends on the execution time.
     */    
    protected ProcessResult atomicCommitWork(ProcessExecution execution) throws 
                AtomicCommitException {
        
        IveObject actor = getCheckedSourceObject("actor", true, execution);
        AttrFuzzy tirednessAttr = getFuzzyAttribute(actor, "tiredness");
        
        if (execution.getDuration() >= SLEEP_LENGTH){
            tirednessAttr.setValue(FuzzyValueHolder.False);
            return ProcessResult.OK;
        }
            
        int newAttrValue = (int)(tirednessAttr.getValue()-TIREDNESS_DEC_15MINUTES*
         execution.getTimeSinceLastCommit()/LENGTH15_MINUTES);
        
        if (newAttrValue <= FuzzyValueHolder.False) {
            tirednessAttr.setValue(FuzzyValueHolder.False);
            return ProcessResult.OK;
        } else {
            tirednessAttr.setValue((short) newAttrValue);
            return ProcessResult.RUNNING;
        }

    }
    
    
    /**
     * delegated to the atomicCommitWork
     */
    public ProcessResult atomicStop(ProcessExecution execution) {
        try {
            atomicCommitWork(execution);
        } catch (AtomicCommitException e) {
            return ProcessResult.FAILED;
        }
        return ProcessResult.OK;
    }

}


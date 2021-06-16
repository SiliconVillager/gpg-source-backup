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

import cz.ive.evaltree.valueholdersimpl.FuzzyValueHolderImpl;
import cz.ive.exception.AtomicCommitException;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.AttrFuzzy;
import cz.ive.iveobject.attributes.AttrObject;
import cz.ive.logs.Log;
import cz.ive.valueholders.FuzzyValueHolder;

/**
 * Process template for drinking the beer.
 *
 * @author ondra
 */
public class Drink extends CommonProcessTemplate {
    
    /** Length of the Drink process, 10 s-seconds. */
    protected static final int LENGTH = 60000;
    
    /** Increase in the peeDrive */
    protected static final int PEEDRIVE_INC = 3500;
    
    /** Diversity in the peeDrive increase */
    protected static final int PEEDRIVE_INC_DIVERSITY = 3000;
    
    /** Creates a new instance of Drink */
    public Drink() {
    }
    
    /**
     * The beer "full" attribute is set to false. PeeDrive of the actor 
     * is increased.
     *
     * @param execution ProcessExecution representing the running process
     * @throws AtomicCommitException on source-check failures.
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws 
            AtomicCommitException {
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        IveObject actor = getCheckedSourceObject("guest", true, execution);
        IveObject beer = getCheckedSourceObject("beer", "/object/Beer", 
                true, execution);
        AttrFuzzy fullAttr = getFuzzyAttribute(beer, "full");
        
        AttrObject armAttr = getObjectAttribute(actor,"arm");
        IveObject armObj = armAttr.getValue();
        
            
        if (fullAttr.getValue() == FuzzyValueHolder.False) {
            LogWarning("The beer is empty", actor);
            return ProcessResult.FAILED;
        }
        if (armObj == null) {
            LogWarning("Actor has empty hands.", actor);
            return ProcessResult.FAILED;
        }
        if (!armObj.getId().equals(beer.getId())) {
            LogWarning("Actor does not hold the beer in the source.", actor);                    
            return ProcessResult.FAILED;
        }

        fullAttr.setValue(FuzzyValueHolder.False);
        
        AttrFuzzy peeDrive = getFuzzyAttribute(actor,"peeDrive");
        short actualvalue = peeDrive.getValue();
        short newValue = (short)Math.min(FuzzyValueHolder.True, 
                actualvalue + PEEDRIVE_INC + 
                (int)(Math.random() * PEEDRIVE_INC_DIVERSITY));
        
        if (newValue != actualvalue) {
            peeDrive.setValue(newValue);
        }
        
        return ProcessResult.OK;
    }
    
    public long atomicLength(ProcessExecution execution) {
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}


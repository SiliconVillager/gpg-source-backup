/* 
 *
 * IVE Editor 
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

import java.util.*;

import cz.ive.ontology.*;
import cz.ive.exception.*;
import cz.ive.lod.*;
import cz.ive.trigger.*;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.evaltree.leaves.*;
import cz.ive.process.Exchange.RendezvousExecution;

/**
 * Processtemplate for the simple Say goal, that sets up the "mouth" attribute
 * of the actor to the sentence given as a parameter "sentence".
 *
 * @author ondra
 */
public class Say extends CommonProcessTemplate {
    
    /** Default exchange duration of 5 s-second */
    protected static final int LENGTH = 5000;
    
    /** Length of the prepare phase. */
    protected static final int PREPARE_LENGTH = 1000;
    
    /** Creates a new instance of Say */
    public Say() {
    }
    
    public ProcessExecution execute(IveProcess process) {
        return new SayExecution(this, process);
    };
    
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws
            AtomicCommitException {
        
        
        IveObject actor = getCheckedSourceObject("actor", true, execution);
        
        if (((SayExecution)execution).set) {
            if (LENGTH - execution.getDuration() > 0)
                return ProcessResult.RUNNING;
            
            AttrInteger mouthAttr = getIntegerAttribute(actor, "mouth");
            
            mouthAttr.setValue(0);
        } else {
            if (PREPARE_LENGTH - execution.getDuration() > 0)
                return ProcessResult.RUNNING;
            
            Integer sentence = (Integer) getCheckedParameter("sentence", "java.int",
                    execution);
            
            AttrInteger mouthAttr = getIntegerAttribute(actor, "mouth");
            
            mouthAttr.setValue(sentence.intValue());
            ((SayExecution)execution).set = true;
            return ProcessResult.RUNNING;
        }
        return ProcessResult.OK;
    };
    
    
    public long atomicLength(ProcessExecution execution) {
        if (((SayExecution)execution).set) {
            return Math.max(0, LENGTH - execution.getDuration());
        } else {
            return Math.max(0, PREPARE_LENGTH - execution.getDuration());
        }
    };
    
    public ProcessResult atomicStop(ProcessExecution execution) {
        try {
            IveObject actor = getCheckedSourceObject("actor", true, execution);
            
            AttrInteger mouthAttr = getIntegerAttribute(actor, "mouth");
            mouthAttr.setValue(0);
        } catch (AtomicCommitException ex) {
            LogSevere(ex.getMessage());
        }
        return ProcessResult.OK;
    };
    
    public void decreaseLOD(ProcessExecution execution) {
        atomicStop(execution);
    }
    
    /**
     * Execution specific for the say process.
     */
    protected class SayExecution extends ProcessExecutionImpl {
        /** Was the sentence setup? */
        protected boolean set = false;
        
        public SayExecution(ProcessTemplate template, IveProcess process) {
            super(template, process);
        }
    }
}

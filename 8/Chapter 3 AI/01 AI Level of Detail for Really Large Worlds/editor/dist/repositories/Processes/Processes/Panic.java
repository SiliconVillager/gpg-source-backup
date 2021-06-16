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
import cz.ive.IveApplication;

import cz.ive.ontology.*;
import cz.ive.exception.*;


/**
 * Process template for Pannic process implementing the PannicGoal.
 * This process is being executed when a genius does not know what to do next.
 * For example when the g-trigger and g-context (IF) fires but there is no 
 * implementation for the associated goal (DO)
 *
 * @author Ondra
 */
public class Panic extends CommonProcessTemplate {
    
    /** Default wait tim of 1 s-second */
    protected static final int MILLIS = 1000;
    
    /** Creates a new instance of Panic */
    public Panic() {
    }
    
    public ProcessResult atomicCommit(ProcessExecution execution) {
        int millis = MILLIS;
        Integer millisInt = null;
        OntologyToken timeOnt = (OntologyToken)execution.getParameters().get(
                "time");
        
        if (timeOnt == null) {
            millisInt = MILLIS;
        } else {
            try {
                millisInt = (Integer)timeOnt.getData("java.int");
            } catch (OntologyNotSupportedException ex) {
                IveApplication.printStackTrace(ex);
            }
        }
        if (millisInt != null)
            millis = millisInt.intValue();
        
        if (execution.getDuration() >= millis) {
            return ProcessResult.OK;
        }
        return ProcessResult.RUNNING;
    };
    
    
    public long atomicLength(ProcessExecution execution) {
        int millis = MILLIS;
        Integer millisInt = null;
        OntologyToken timeOnt = (OntologyToken)execution.getParameters().get(
                "time");
        
        if (timeOnt == null) {
            millisInt = MILLIS;
        } else {
            try {
                millisInt = (Integer)timeOnt.getData("java.int");
            } catch (OntologyNotSupportedException ex) {
                IveApplication.printStackTrace(ex);
            }
        }
        if (millisInt != null)
            millis = millisInt.intValue();
        
        return millis - execution.getDuration();
    };
}

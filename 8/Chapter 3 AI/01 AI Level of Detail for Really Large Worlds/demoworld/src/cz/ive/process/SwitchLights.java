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
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.logs.Log;
import cz.ive.ontology.SingleToken;


/**
 * Process template for switching the signal lights.
 *
 * @author ondra
 */
public class SwitchLights extends CommonProcessTemplate {
    
    /**
     * Length of the SwitchLights process
     */
    protected static final int LENGTH = 1;
    
    /** Creates a new instance of SwitchLights */
    public SwitchLights() {
    }
    
    /**
     * Switchs the lights on/off acording to the parameter "on".
     *
     * @param execution ProcessExecution representing the running process
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws AtomicCommitException{
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        IveObject light1 = getCheckedSourceObject("signalLight1", true, execution);
        IveObject light2 = getCheckedSourceObject("signalLight2", true, execution);
        
        
        AttrInteger l1_state = getIntegerAttribute(light1,"state");
        AttrInteger l2_state  = getIntegerAttribute(light2,"state");
        int state;
        
        
        try {
            state = ((Integer)((SingleToken)execution.getParameters().get(
                    "state")).getData("java.int")).intValue();
            
        } catch (Exception ex) {
            IveApplication.printStackTrace(ex);
            Log.addMessage("Unexpected or missing attributes.", Log.WARNING,
                    "", "SwitchLights", "");
            return ProcessResult.WRONG_SOURCES;
        }
        
        if ( l1_state.getValue() != l2_state.getValue()) {
            Log.addMessage("States of the lights do not correspond.", Log.WARNING,
                    "", "SwitchLights", "");
            return ProcessResult.FAILED;
        }
        l1_state.setValue(state);
        l2_state.setValue(state);
        
        return ProcessResult.OK;
    }
    
    public long atomicLength(ProcessExecution execution) {
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}


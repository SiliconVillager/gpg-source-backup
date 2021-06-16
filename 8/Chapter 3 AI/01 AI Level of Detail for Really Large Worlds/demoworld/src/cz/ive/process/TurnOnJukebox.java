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
import cz.ive.logs.Log;

/**
 * Proces template for turning the jukebox on.
 *
 * @author ondra
 */
public class TurnOnJukebox extends CommonProcessTemplate {
    
    /** Length of the process. 2 s-seconds. */
    protected static final int LENGTH = 2000;
    
    /** Creates a new instance of TurnOnJukebox */
    public TurnOnJukebox() {
    }
    
    /**
     * Turns on the jukebox. This means setting the "on" attribute to True.
     *
     * @param execution ProcessExecution representing the running process
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws AtomicCommitException{
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        IveObject actor = getCheckedSourceObject("guest", true, execution);
        IveObject jukebox = getCheckedSourceObject("jukebox","/object/Jukebox", true, execution);
                
        
        AttrFuzzy on = getFuzzyAttribute(jukebox, "on");
            
        if (on.getValue() == AttrFuzzy.False) {
            on.setValue(AttrFuzzy.True);
        } else { 
            return ProcessResult.FAILED;
        }
        
        return ProcessResult.OK;
    }

    public long atomicLength(ProcessExecution execution) {
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}

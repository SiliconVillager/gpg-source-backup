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
import cz.ive.iveobject.attributes.*;

/**
 * Process template for picking up a beer-glass.
 *
 * @author ondra
 */
public class PickUpGlass extends CommonProcessTemplate {
    
    /** Length of the PickUp process */
    protected static final int LENGTH = 2000;
    
    /** Creates a new instance of PickUpGlass */
    public PickUpGlass() {
    }
    
    /**
     * Creates new empty glass object placing it to the actors hand. It also
     * fills in the "beer" source in phantom substitutions.
     *
     * @param execution ProcessExecution representing the running process
     * @throws AtomicCommitException on source-check failures.
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws
            AtomicCommitException {
        
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        IveObject actor = getCheckedSourceObject("waiter", true, execution);
        IveObject from = getCheckedSourceObject("from", "/object/Glasses",
                true, execution);
        
        AttrInteger coalIntAtr;
        AttrObject armAttr = getObjectAttribute(actor,"arm");
        int coalInt;
        
        if (armAttr.getValue() != null) {
            LogWarning("Actor has full hands.", actor);
            return ProcessResult.FAILED;
        }
        
        IveObject beer = createObject(actor, "Beer", "Beer");
        
        actor.addObject(beer);
        armAttr.setValue(beer);
        
        Source src = execution.getPhantoms().getSource("beer");
        if (src == null){
            LogWarning("We expected source beer to be present.", actor);
            return ProcessResult.OK;
        }
        src.setObject(beer);
        
        return ProcessResult.OK;
    }
    
    public long atomicLength(ProcessExecution execution) {
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}

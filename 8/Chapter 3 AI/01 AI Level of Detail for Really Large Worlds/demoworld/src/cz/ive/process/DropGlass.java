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
import cz.ive.logs.Log;

/**
 * Process template for dropping the glass.
 *
 * @author ondra
 */
public class DropGlass extends CommonProcessTemplate {
    
    /** Length of the DropGlass process */
    protected static final int LENGTH = 2000;
    
    /** Creates a new instance of DropGlass */
    public DropGlass() {
    }
    
    /**
     * Removes beer from the actors hands (discarding the object).
     * It also clears the "beer" source of the phantom substitution.
     *
     * @param execution ProcessExecution representing the running process
     * @throws AtomicCommitException on source-check failures.
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws 
            AtomicCommitException {
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        IveObject actor = getCheckedSourceObject("actor", true, execution);
        IveObject beer = getCheckedSourceObject("beer", "/object/Beer", 
                true, execution);
        IveObject to = getCheckedSourceObject("to", true, execution);
        
        AttrObject armAttr = getObjectAttribute(actor,"arm");
        IveObject armObj = armAttr.getValue();
        
        if (armObj == null) {
            LogWarning("Actor has empty hands.", actor);
            return ProcessResult.FAILED;
        }
        if (!armObj.getId().equals(beer.getId())) {
            LogWarning("Actor does not hold the beer in the source.", actor);                    
            return ProcessResult.FAILED;
        }
        
        armAttr.setValue(null);
        actor.removeObject(beer);
        beer.setObjectState(IveObject.ObjectState.NOT_EXIST);
        ObjectMap.instance().unregister(beer.getId());
        
        cleanPhantomSource("beer", execution);
        
        return ProcessResult.OK;
    }
    
    public long atomicLength(ProcessExecution execution) {
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}


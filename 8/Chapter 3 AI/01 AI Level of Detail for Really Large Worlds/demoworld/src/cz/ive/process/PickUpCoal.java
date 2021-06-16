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
import cz.ive.location.WayPoint;

/**
 * Process template for Picking up a coal.
 *
 * @author ondra
 */
public class PickUpCoal extends CommonProcessTemplate {
    
    /**
     * Length of the PickUp process
     */
    protected static final int LENGTH = 2000;
    
    /** Creates a new instance of PickUpCoal */
    public PickUpCoal() {
    }
    
    /**
     * Decreases number of coals in the "from" source and creates new Coal
     * object placing it to the actors hand. It also fills in the
     * "coal" source in phantom substitutions. If this was the last coal-piece
     * taken from the coalPile, discard it also.
     *
     * @param execution ProcessExecution representing the running process
     * @throws AtomicCommitException on source-check failures.
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution) throws
            AtomicCommitException {
        
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        IveObject actor = getCheckedSourceObject("miner", true, execution);
        IveObject from = getCheckedSourceObject("from", true, execution);
        
        
        AttrInteger coalIntAtr;
        AttrObject armAttr = getObjectAttribute(actor,"arm");
        int coalInt;
        
        if (armAttr.getValue() != null) {
            LogWarning("Actor has full hands.", actor);
            return ProcessResult.FAILED;
        }
        
        coalIntAtr = getIntegerAttribute(from, "coal");
        coalInt = coalIntAtr.getValue();
        
        if (coalInt < 1) {
            LogWarning("No coal to pick up.", actor);
            return ProcessResult.FAILED;
        }
        
        IveObject coal = createObject(actor, "Coal","Coal" );
        
        actor.addObject(coal);
        armAttr.setValue(coal);
        
        
        Source src = execution.getPhantoms().getSource("coal");
        if (src == null){
            LogWarning("We expected source coal to be present.", actor);
            return ProcessResult.OK;
        }
        src.setObject(coal);
        
        if (ObjectClassTree.instance().getObjectClass(
                "/object/CoalPile").isInside(from) && coalInt == 1) {
            WayPoint pos = from.getPosition();
            pos.removeObject(from);
            from.setObjectState(IveObject.ObjectState.NOT_EXIST);
            src = execution.getPhantoms().getSource("from");
            src.setObject(null);
        } else {
            coalIntAtr.setValue(coalInt - 1);
        }
        
        return ProcessResult.OK;
    }
    
    public long atomicLength(ProcessExecution execution) {
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}

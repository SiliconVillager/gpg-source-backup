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
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.process.MineAtomicWork.MineWorkExecution;
import cz.ive.valueholders.FuzzyValueHolder;


/**
 * Process template for the MineWork in the down mine position.
 *
 * @author ondra
 */
public class MineDownWork extends MineAtomicWork {
    
    /** Creates a new instance of MineDownWork */
    public MineDownWork() {
    }
    
    /**
     * Clears the cart and pulls both the levers.
     *
     * @param execution ProcessExecution representing the running process
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution)
    throws AtomicCommitException{
        if (execution.getDuration() < PULL_PERIOD*((MineWorkExecution)execution).periodNum)
            return ProcessResult.RUNNING;
        
        ((MineWorkExecution)execution).periodNum++;
        
        IveObject cart = getSourceObject("cart", true, execution);
        IveObject lever = getCheckedSourceObject("leverDown", true, execution);
        IveObject actor = getCheckedSourceObject("miner", true, execution);
        
        if (cart == null) {
            // The cart is not waiting here. Then we are doing what we
            // are expected to do.
            return ProcessResult.OK;
        }
        
        
        if (!cart.getPosition().getId().equals(actor.getPosition().getId())) {
            return ProcessResult.RUNNING;
        }
        
        AttrInteger coalIntAtr = getIntegerAttribute(cart, "coal");
        AttrFuzzy lever_down = getFuzzyAttribute(lever,"pushed");
        int coalInt;
        
        if (lever_down.getValue() == FuzzyValueHolder.True) {
            return ProcessResult.RUNNING;
        }
        lever_down.setValue(FuzzyValueHolder.True);
        coalIntAtr.setValue(3);
        
        return ProcessResult.RUNNING;
    }
}

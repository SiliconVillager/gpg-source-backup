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
 * Process template for vital function. It periodicaly increases peeDrive
 * and tiredness of human actors in the world.
 *
 * @author Ondra
 */
public class Vital extends CommonProcessTemplate {
    
    /** Minimal period of the Vital process. */
    protected static final int LENGTH = 900000;
    
    /** Maximal atomic lenght diversity of the process */
    protected static final int DIVERSITY = 900000;
    
    /** How much do we increase peeDrive. */
    protected static final int PEE_INC = 750;
    
    /** Diversity in the peedrive increase. */
    protected static final int PEE_DIVERSITY = 1200;
    
    /** How much do we increase tiredness */
    protected static final int TIREDNESS_INC = 512; /* there was 750 */
    
    /** How much do we decrease tiredness if sleeping */
    protected static final int TIREDNESS_DEC = 2250;
    
    /** Diversity in the tiredness increase. */
    protected static final int TIREDNESS_INC_DIVERSITY = 512; /*there was 1200*/
    
    /** Diversity in the tiredness decrease. */
    protected static final int TIREDNESS_DEC_DIVERSITY = 1500;
    
    /** Index of the sleeping state. */
    protected static final int SLEEPING_STATE = Ent.SLEEPING;
    
    /** Creates a new instance of Vital */
    public Vital() {
    }
    
    /**
     * Creates CoalPile and places it to the same area. It also fills in the
     * "coalPile" source in phantom substitutions.
     *
     * @param execution ProcessExecution representing the running process
     */
    public ProcessResult atomicCommitWork(ProcessExecution execution)
    throws AtomicCommitException{
        if (execution.getDuration() < LENGTH)
            return ProcessResult.RUNNING;
        
        IveObject actor = getCheckedSourceObject("actor", true, execution);
        
        AttrFuzzy peeAttr = getFuzzyAttribute(actor, "peeDrive");
        AttrFuzzy tirednessAttr = getFuzzyAttribute(actor, "tiredness");
        AttrInteger stateAttr = getIntegerAttribute(actor, "state");
        
        int state = stateAttr.getValue();
        short pee =  peeAttr.getValue();
        int peeNew = Math.min((int)Short.MAX_VALUE,
                pee + PEE_INC + (int)(Math.random()*PEE_DIVERSITY));
        short tiredness =  tirednessAttr.getValue();
        int tirednessNew;
        
        // Are we sleeping?
        if (state == SLEEPING_STATE) {
            tirednessNew = Math.max((int)0,
                    tiredness - TIREDNESS_DEC -
                    (int)(Math.random()*TIREDNESS_DEC_DIVERSITY));
        } else {
            tirednessNew = Math.min((int)Short.MAX_VALUE,
                    tiredness + TIREDNESS_INC +
                    (int)(Math.random()*TIREDNESS_INC_DIVERSITY));
        }
        
        
        int actorLod = actor.getPosition().getLod();
        if (peeNew != pee && actorLod>3) {
            peeAttr.setValue((short)peeNew);
        }
        if (tirednessNew != tiredness) {
            tirednessAttr.setValue((short)tirednessNew);
        }
        
        return ProcessResult.OK;
    }
    
    public long atomicLength(ProcessExecution execution) {
        return Math.max(
                LENGTH - execution.getDuration() +
                (int)(DIVERSITY*Math.random()), 0);
    }
}


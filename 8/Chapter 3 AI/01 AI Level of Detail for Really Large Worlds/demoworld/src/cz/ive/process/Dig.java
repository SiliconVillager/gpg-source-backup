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
import cz.ive.location.WayPoint;
import cz.ive.logs.Log;


/**
 * Process template for digging in the mine.
 *
 * @author ondra
 */
public class Dig extends CommonProcessTemplate {
    
    /**
     * Length of the Dig process
     */
    protected static final int LENGTH = 10000;
    
    /** Creates a new instance of Dig */
    public Dig() {
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
        
        IveObject actor = getCheckedSourceObject("miner", true, execution);
        IveObject rock = getCheckedSourceObject("rock","/object/Rock", true, execution);
        
        
        IveObject coalPile = createObject(actor, "CoalPile", "CoalPile");
        
        WayPoint area = (WayPoint)rock.getPosition().getParent();
        area.placeObject(coalPile, null, null);
        
        Source src = execution.getPhantoms().getSource("coalPile");
        if (src == null){
            Log.addMessage("We expected source coalPile to be present.",
                    Log.WARNING, "", "Dig", "");
            return ProcessResult.OK;
        }
        src.setObject(coalPile);
        Log.warning("CoalPile was added.");
        return ProcessResult.OK;
    }

    public long atomicLength(ProcessExecution execution) {
        return Math.max(LENGTH - execution.getDuration(), 0);
    }
}


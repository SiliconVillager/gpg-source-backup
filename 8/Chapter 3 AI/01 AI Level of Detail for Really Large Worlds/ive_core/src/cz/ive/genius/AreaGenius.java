/* 
 *
 * IVE - Inteligent Virtual Environment
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
 
package cz.ive.genius;

import cz.ive.iveobject.IveObject;
import cz.ive.process.*;
import cz.ive.trigger.TriggerTemplate;
import cz.ive.util.Pair;
import java.util.List;
import java.util.Map;

/**
 * Area genius provides services for geniuses who doesn't want to operate
 * with their actors through accepting deleagation of processes.
 *
 * @author ondra
 */
public interface AreaGenius extends Genius {
    
    /**
     * Sets up the delegation table. It is a map from processId of the
     * DelegatedProcessTemplates to the goalId of the goals to be fullfilled
     * for the given process.
     *
     * @param table The new delegation table.
     */
    void setDelegationTable(Map<String, String> table);
    
    /**
     * Passes the given process to this area genius.
     *
     * @param actor Real object of the actor of the process.
     * @param execution Execution of a process to be delegated.
     */
    ProcessResult startDelegation(IveObject actor, ProcessExecution execution);
    
    /**
     * Asks the the area genius to stop the given process. It may, but does not
     * have to, do so immediately.
     *
     * @param execution Execution of the process which delegation to finish.
     * @return ProcessResult signaling current state of the delegation.
     *      Explicitely RUNNING state means that the delegation will continue
     *      for a while and then itself call stop() on the process.
     */
    ProcessResult stopDelegation(ProcessExecution execution);
    
    /**
     * This method instantly stops the delegation. It is called as a consequence
     * of the LOD changes.
     *
     * @param execution ProcessExecution of the process which delegation to 
     *      finish instantly.
     */
    void interruptDelegation(ProcessExecution execution);
    
    public void setCleaningGoals(List<Pair<TriggerTemplate,String>> cg);
}

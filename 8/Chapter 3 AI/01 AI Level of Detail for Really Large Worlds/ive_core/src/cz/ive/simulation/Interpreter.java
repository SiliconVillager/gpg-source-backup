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
 
package cz.ive.simulation;

import cz.ive.iveobject.IveObject;
import cz.ive.process.IveProcess;
import cz.ive.exception.ProcessNotRunningException;
import cz.ive.process.Substitution;
import cz.ive.process.Source;
import cz.ive.exception.ExchangeWrongSourcesException;
import java.util.ArrayList;

/**
 * Interpreter interface. There should be a public singleton implementing this
 * interface. Purpose of the Interpreter is to execute real processes on real
 * objects. It translates phantom substitutions made by geniuses 
 * into real-object substitutions and then calls ProcessTemplates to 
 * run/stop/commit the real process. This class is priviledged as it can
 * search real-objects by its id.
 *
 * @author Ondra
 */
public interface Interpreter {
    
    /**
     * Begin execution of given process
     * @param process Instance of a Process. Interpreter should get 
     *      the ProcesstTemplate by searching the ProcessDB not directly 
     *      from given the Process. This should prevent geniuses from accessing
     *      real objects by passing its own ProcessTemplate.
     */
    void execute(IveProcess process);
    
    /**
     * Stops the process. This does not mean, that the process is stopped after
     * return from this function. The process is stopped when it signals
     * on the FinishHook. This may (but does not have to) occur during
     * execution of this method.
     * If the process was not started by calling execute() method, the 
     * ProcessNotRunningException will be thrown.
     */
    void stop(IveProcess process) throws ProcessNotRunningException;

    /**
     * Tells to the interpreter that this object will increase it's lod.
     * All processes having this object as an actor will be prepared for this
     * change.
     * @param object changing it's lod
     */
    void preIncreaseLod(IveObject object);

    /**
     * Tells to the interpreter that this object will decrease it's lod.
     * All processes having this object as an actor will be prepared for this
     * change.
     * @param object changing it's lod
     */
    void preDecreaseLod(IveObject object);

    /**
     * Exchanges sources in the process and it's execution.
     * Tells to all involved participants about this change.
     * @param process The process with changing sources
     * @param slotId Slot whose source is to be changed
     * @param newSources New sources
     */
    void exchangeSources(IveProcess process, String slotId, 
            ArrayList<Source> newSources) 
            throws ExchangeWrongSourcesException;

    /**
     * Exchanges sources in the process and it's execution.
     * Tells to all involved participants about this change.
     * @param process The process with changing sources
     * @param slotId Slot whose source is to be changed
     * @param newSource New source
     */
    void exchangeSources(IveProcess process, String slotId, 
            Source newSource) 
            throws ExchangeWrongSourcesException;
}

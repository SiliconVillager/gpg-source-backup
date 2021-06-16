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
 
package cz.ive.process;

/**
 * Interface for the rendezvous process template.
 *
 * @author ondra
 */
public interface RendezvousProcessTemplate extends ProcessTemplate {
    
    /** 
     * Called by the interpreter to acknowledge the process template that
     * the rendezvou was successfull (the "who" also executed this process with
     * current "actor" as "who").
     *
     * @param execution1 one of the two ProcessExecutions representing 
     *      the running instance of this process.
     * @param execution2 one of the two ProcessExecutions representing 
     *      the running instance of this process.
     * @return ProcessResult representing processes' states after the rendezvous 
     *      acceptance. Whatever value is returned, it represents the state 
     *      of both the processes.Explicitely, if this value is RUNNING, 
     *      Interpreter should call the atomicLength method to get info about 
     *      new processes' expected length and plan their finish. If this value 
     *      equals to OK then both the processes have successfully finished.
     */
    ProcessResult rendezvous(ProcessExecution execution1,
            ProcessExecution execution2);
    
}

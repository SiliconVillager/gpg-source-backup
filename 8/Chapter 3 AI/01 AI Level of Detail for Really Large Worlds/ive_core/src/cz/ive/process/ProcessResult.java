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
 * Enum of possible process results
 *
 * @author Ondra
 */
public enum ProcessResult {
    
    /** 
     * Process is still running. Call to atomicLength should be performed 
     * in order to find out estimate of a process duration.
     */
    RUNNING,
    /** 
     * Process has already successfully finished. 
     */
    OK,
    /** 
     * Process has failed with unspecified reason
     */
    FAILED,
    /**
     * Process was stopped due to interruption
     */
    INTERRUPTED,
    /** 
     * Process has failed due to wrong sources substitution
     */
    WRONG_SOURCES,
    /**
     * Process has failed, because more actors demand different mode 
     * (atomic/expanded)
     */
    ACTOR_CONFUSION,
    /**
     * Process was destroyed, because the LOD value is below the level
     * of existence
     */
    LOD_TOO_LOW,
    /**
     * Process was destroyed, because the LOD value is forcing to expand
     * an always-atomic process.
     */
    LOD_TOO_HIGH;
    
    /**
     * Are we still running?
     * @return <code>true</code> iff the result means that the Process is
     *      still running
     *      <code>false</code> otherwise
     */
    public boolean isRunning() {
        return this == RUNNING;
    }
    
    /**
     * Has the process finished without error?
     * @return <code>true</code> iff the process has finished successfully
     *      <code>false</code> otherwise
     */
    public boolean isSuccessfull() {
        return this == OK;
    }
    
    /**
     * Has the process failed?
     * @return <code>true</code> iff an error has occured
     *      <code>false</code> otherwise
     */
    public boolean isFailure() {
        return this == FAILED ||
               this == WRONG_SOURCES ||
               this == INTERRUPTED ||
               this == ACTOR_CONFUSION ||
               this == LOD_TOO_LOW ||
               this == LOD_TOO_HIGH;
    }
}

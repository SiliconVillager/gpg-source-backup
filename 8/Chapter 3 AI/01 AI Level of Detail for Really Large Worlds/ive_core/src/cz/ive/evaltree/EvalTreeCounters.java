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
 
package cz.ive.evaltree;


/**
 * Singleton used to keep the value of instance counters needed by evaltree 
 * during serialization.
 * @author thorm
 */
public class EvalTreeCounters implements java.io.Serializable {
    
    /** Creates a new instance of EvalTreeCounters */
    protected EvalTreeCounters() {
        exprCounter = 0;
        dagCounter = 1;
    }
    
    /** Reference to current singleton instance */
    private static EvalTreeCounters instance;
    
    /**
     * Returns the current instance of the EvalTreeCounters singleton.
     * This singleton can change during load process.
     *
     * @return current instance of Utterances singleton
     */
    static public synchronized EvalTreeCounters instance() {
        if (instance == null) {
            instance = new EvalTreeCounters();
        }        
        return instance;
    }
    
    /**
     * Changes reference to current instance of EvalTreeCounters singleton
     * Used with serialization - after loading.
     *
     * @param instance reference to new EvalTreeCounters singleton
     */
    static public void setInstance(EvalTreeCounters instance) {
        EvalTreeCounters.instance = instance;
    }
    
    /**
     * Empty whole the EvalTreeCounters before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        instance = new EvalTreeCounters();
    }
    
    /**
     * Increment and return the counter of Expr constructor invocation
     * @return value of incremented counter of Expr instance creation
     */
    public static int incExprCounter() {
        return instance.exprCounter++;
    }
    
    /**
     * Increment and return the counter of ExprDAG constructor invocation
     * @return value of incremented counter of ExprDAG instance creation
     */
    public static int incDagCounter() {
        return instance.dagCounter++;
    }
    
    /**
     * keeps number of Expr instances created during the simulation
     */
    private int exprCounter;
    
    /**
     * keeps number of ExprDAG instances created during the simulation
     */
    private int dagCounter;
    
}

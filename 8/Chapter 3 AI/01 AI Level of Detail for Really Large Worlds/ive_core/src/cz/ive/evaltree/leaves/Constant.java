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
 
package cz.ive.evaltree.leaves;


import cz.ive.evaltree.Expr;
import java.util.List;
import java.util.Queue;
import cz.ive.messaging.QueueHook;
import cz.ive.sensors.Sensor;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;


/**
 * Succesor of constant expressions of all types
 * @author thorm
 * @param <T> ValueHolder descendant used to keep constant value
 */
public abstract class Constant<T extends ValueHolderImpl> extends Expr<T, ValueHolderImpl> {
    
    /**
     * @return <CODE>null</CODE>
     */
    public Expr<ValueHolderImpl, ? extends ValueHolderImpl> getChild(int i) {
        return  null;
    }
    
    /**
     * @return 0
     */
    public int getNumberOfChildren() {
        return 0;
    }
    
    /**
     * Needed to implement abstract method
     */
    public void uninstantiate() {}
    
    /**
     * No effect - constant value remains unchanged
     */
    public void DFSEval(cz.ive.process.Substitution s, List<Sensor> sensors) {}

    /**
     * @return false
     */
    public boolean remapChild(Expr<ValueHolderImpl, ? extends ValueHolderImpl> old, Expr<ValueHolderImpl, ? extends ValueHolderImpl> n) {
        return false;
    }
    
}

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


import cz.ive.messaging.QueueHook;
import java.util.Queue;
import cz.ive.valueholders.ValueType;
import cz.ive.valueholders.IntValueHolder;
import cz.ive.evaltree.valueholdersimpl.*;


/*
 * @author thorm
 */

/**
 * Int constant expression. Leaf of evaltree
 */
public class IntConstant extends Constant<IntValueHolderImpl> {

    /**
     * Create new IntConstant object
     * The initial value is defined and is 0
     */
    public IntConstant() {
        value = new IntValueHolderImpl();
        value.validate();
    }
    
    /**
     * Create new IntConstant object
     * Initial value is defined.
     *
     * @param v initial value
     */
    public IntConstant(int v) {
        value = new IntValueHolderImpl();
        setValue(v);
        value.validate();
    }

    /**
     * Copy value from v to internal IntValueHolder
     * Use this method to change value
     * @param v new value
     */
    public void setValue(IntValueHolder v) {
        setValue(v.getValue());
    }
    
    /**
     * Change value of internal IntValueHolder
     * Use this method to change value
     * @param v new value
     */
    public void setValue(int v) {
        value.setValue(v);
        signal();
    }
    
    
    public String getInfoArguments() {
        return "value=" + value.value;
    }
    
    /**
     * Two IntConstants are equal if their value is the same
     */
    public int contentHashCode() {
        return this.getClass().hashCode() + value.value;
    }

    /**
     * Two IntConstants are equal if their value is the same
     */
    public boolean contentEquals(Object obj) {
        return (obj instanceof IntConstant)
                && ((IntConstant) obj).value.value == value.value;
        
    }
}

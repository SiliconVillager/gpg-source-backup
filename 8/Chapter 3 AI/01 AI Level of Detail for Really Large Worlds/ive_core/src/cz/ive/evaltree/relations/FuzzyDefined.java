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
 
package cz.ive.evaltree.relations;


import cz.ive.evaltree.Expr;
import cz.ive.evaltree.dependentnodes.UnaryNode;
import cz.ive.evaltree.valueholdersimpl.FuzzyValueHolderImpl;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.valueholders.FuzzyValueHolder;


/**
 * Node whose value is true iff the operand has defined value
 * @author thorm
 */
public class FuzzyDefined<T extends ValueHolderImpl> 
        extends UnaryNode<FuzzyValueHolderImpl, T> {
    
    /** Creates a new instance of FuzzyDefined 
     *  
     *  @param p1 operand
     */
    public FuzzyDefined(Expr<T, ? extends ValueHolderImpl> p1) {
        super(p1);
        value = new FuzzyValueHolderImpl();
    }
    
    /**
     * Set the value to be true True if the operand value is defined and 
     * vice versa.
     * The value is always defined.
     */
    protected void updateValue() {
        value.value = (childValue.isDefined)
                ? FuzzyValueHolder.True
                : FuzzyValueHolder.False;
        value.isDefined = true;
    }
}

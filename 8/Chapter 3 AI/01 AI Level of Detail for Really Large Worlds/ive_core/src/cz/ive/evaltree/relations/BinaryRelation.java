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


import cz.ive.evaltree.*;
import cz.ive.evaltree.dependentnodes.*;

import java.util.Queue;
import cz.ive.messaging.QueueHook;
import cz.ive.valueholders.ValueType;
import cz.ive.evaltree.valueholdersimpl.FuzzyValueHolderImpl;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;


/**
 * Succesor of all binary relations - Fuzzy nodes having two child nodes 
 * of the same type)
 * @author thorm
 */
public abstract class BinaryRelation<T extends ValueHolderImpl> 
        extends BinaryNode<FuzzyValueHolderImpl, T> {
    
    /** 
     * Create a new instance of BinaryRelation 
     * 
     * @param p1 left operand 
     * @param p2 right operand
     */
    public BinaryRelation(Expr<T, ? extends ValueHolderImpl> p1,
                          Expr<T, ? extends ValueHolderImpl> p2) {
        super(p1, p2);
        value = new FuzzyValueHolderImpl();
        initialUpdateValue();
    }
    
}

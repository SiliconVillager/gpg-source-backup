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


import cz.ive.evaltree.dependentnodes.NaryNode;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;


/**
 * This class provides support for complex expressions written in java.
 * Let's say we want to create expression pot1.water<pot2.water - 1
 *
 * This expression is of type FuzzyBool, subexpressions are of type Integer.
 * So we derive new class:
 * <pre>
 * public class MyExpr extends JavaExpr<FuzzyValueHolderImpl,IntValueHolderImpl>{
 *      public MyExpr(){
 *          super(new IntLink[] {
 *                  new IntLink("pot1.water"),
 *                  new IntLink("pot2.water")
 *                  }
 *              );
 *          }
 *      public void updateValue(){
 *              value.value=(ch(0).value<ch(1).value - 1)?
 *                  FuzzyValueHolder.True:FuzzyValueHolder.False;
 *          }
 * }
 * </pre>
 *
 *
 * @author thorm
 */
public abstract class JavaExpr<TYPE extends ValueHolderImpl,
        CHTYPE extends ValueHolderImpl> extends NaryNode<TYPE, CHTYPE> {
    
    /**
     * Creates a new instance of JavaExpr
     * @param chlds array of child nodes
     */
    public JavaExpr(Expr<CHTYPE, ? extends ValueHolderImpl>[] chlds) {
        super(chlds);
        
    }

    /**
     * Get value type of the i-th expression
     * @param i index of the particular child
     * @return child's value type
     */
    protected CHTYPE ch(int i) {
        return childsValue[i];
    }
    
}

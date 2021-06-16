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
 
package cz.ive.evaltree.dependentnodes;



import java.util.List;
import cz.ive.evaltree.*;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.messaging.Hook;
import cz.ive.sensors.Sensor;


/**
 * Succesor of all binary operators ( nodes having two child nodes of the 
 * same type as they are)
 */

public abstract class BinaryNode<TYPE extends ValueHolderImpl,
        CHTYPE extends ValueHolderImpl> extends DependentNode<TYPE, CHTYPE> {    

    /**
     * right operand
     */
    protected Expr<CHTYPE, ? extends ValueHolderImpl> right;

    /**
     * left operand
     */
    protected Expr<CHTYPE, ? extends ValueHolderImpl> left;

    /**
     * right operands value holder
     */
    protected CHTYPE rightValue;

    /**
     * left operands value holder
     */
    protected CHTYPE leftValue;
    
    /**
     * Constructor
     * @param p1 left operand
     * @param p2 right operand
     */
    public BinaryNode(Expr<CHTYPE, ? extends ValueHolderImpl> p1,
            Expr<CHTYPE, ? extends ValueHolderImpl> p2) {
        left = p1;
        right = p2;
        refreshChildBindings();        
    }
    
    
    /**
     * @return left operand for i=0 and right operand for i=1
     */
    
    public Expr<CHTYPE, ? extends ValueHolderImpl> getChild(int i) {
        if (i == 0) {
            return left;
        }
        if (i == 1) {
            return right;
        }
        return null;
    }
    
    /**
     * @return 2
     */
    public int getNumberOfChildren() {
        return 2;
    }

    /**
     * Two binary expressions are consedered to be equall iff they are 
     * represented by the same class and their operands are same references.
     *
     * @return for two equivalent expression returns the same number.
     */
    public int contentHashCode() {
        return 
                this.getClass().hashCode() + leftValue.hashCode()
                + rightValue.hashCode();
    }
    
    /**
     * Two binary expressions are consedered to be equall iff they are 
     * represented by the same class and their operands are same references.
     * @param obj another expression
     * @return true if obj is equal to this
     */       
    public boolean contentEquals(Object obj) {
        return (obj.getClass() == this.getClass()
                && (left == ((BinaryNode) obj).left)
                && (right == ((BinaryNode) obj).right)
                );
    }

    public boolean remapChild(Expr<CHTYPE, ? extends ValueHolderImpl> old,
            Expr<CHTYPE, ? extends ValueHolderImpl> n) {
        if (old == left) {            
            leftValue = n.getValue();
            left = n;
        } else if (old == right) {
            right = n;
            rightValue = n.getValue();
        } else {
            return false;
        }
        
        old.unregisterListener(this);
        old.removeParent(this);
        n.addParent(this);
        
        initialUpdateValue();
        n.registerListener(this);
        
        int newD = n.getLevelsOfDescendants() + 1;

        levelsOfDescendants = ((newD > levelsOfDescendants)
                ? newD
                : levelsOfDescendants);
        
        return true;
    }
    
    /**
     * Forget the references of the both children
     */
    public void uninstantiate() {
        left.unregisterListener(this);
        left.removeParent(this);
        left = null;
        leftValue = null;        
        right.unregisterListener(this);
        right.removeParent(this);
        right = null;
        rightValue = null;
        levelsOfDescendants = 0;
    }

    /**
     * Evaluates the left expression first, right expression second and 
     * updates the value.
     */
    public void DFSEval(cz.ive.process.Substitution s, List<Sensor> sensors) {        
        left.DFSEval(s, sensors);
        right.DFSEval(s, sensors);        
        initialUpdateValue();
    }
    
    /**
     * Needed to implement abstract method from Expr
     */
    protected void updateInstantiation(Hook child) {}
    
    public void refreshChildBindings() {
        leftValue = left.getValue();
        rightValue = right.getValue();
        left.addParent(this);
        right.addParent(this);
        left.registerListener(this);
        right.registerListener(this);
        int ld = left.getLevelsOfDescendants();
        int rd = right.getLevelsOfDescendants();

        levelsOfDescendants = ((ld > rd) ? ld : rd) + 1;
    }
    
    protected BinaryNode<TYPE, CHTYPE> clone() {
        BinaryNode<TYPE, CHTYPE> ret = (BinaryNode<TYPE, CHTYPE>) super.clone();        

        ret.left = left.createCopy();
        ret.right = right.createCopy();
        ret.refreshChildBindings();
        return ret;
    }
    
}

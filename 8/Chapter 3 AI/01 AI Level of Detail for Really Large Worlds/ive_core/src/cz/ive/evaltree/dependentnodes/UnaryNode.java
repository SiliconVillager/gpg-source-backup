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


import cz.ive.evaltree.*;
import java.util.List;
import cz.ive.sensors.Sensor;


import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.messaging.Hook;

/**
 * Succesor of all unary operators 
 */
public abstract class UnaryNode<TYPE extends ValueHolderImpl,
        CHTYPE extends ValueHolderImpl> extends DependentNode<TYPE, CHTYPE> {

    /**
     * Child subexpression
     */
    protected Expr<CHTYPE, ? extends ValueHolderImpl> child;
    
    /**
     * Child subexpression's value holder
     */
    protected CHTYPE childValue;
    
    /**
     * Create UnaryNode instance
     * @param operand the only child of this node
     */
    public UnaryNode(Expr<CHTYPE, ? extends ValueHolderImpl> operand) {
        child = operand;
        refreshChildBindings();
    }
    
    /**
     * SourceDependentNodes are considered to be equal if they are represented
     * by the same class and child object is equal.
     */
    public int contentHashCode() {
        return (this.getClass().hashCode() + childValue.hashCode());
    }

    /**
     * SourceDependentNodes are considered to be equal if they are represented
     * by the same class and child object is equal.
     */
    public boolean contentEquals(Object obj) {
        if (obj.getClass() == this.getClass()) {
            return child == ((UnaryNode) obj).child;
        }
        return false;

    }
    
    /**
     * Needed to implement abstract method of DependentNode class
     */
    protected void updateInstantiation(Hook child) {}
    
    /**
     * @return 1
     */
    public int getNumberOfChildren() {
        return 1;
    }
    
    /**
     * @return child for i=1 or null otherwise
     */
    public Expr<CHTYPE, ? extends ValueHolderImpl> getChild(int i) {
        if (i == 0) {
            return child;
        }
        return null;
    }
    
    public boolean remapChild(Expr<CHTYPE, ? extends ValueHolderImpl> old,
            Expr<CHTYPE, ? extends ValueHolderImpl> n) {
        if (old == child) {            
            child = n;
            childValue = n.getValue();
            old.unregisterListener(this);
            old.removeParent(this);
            n.addParent(this);
            
            initialUpdateValue();
            n.registerListener(this);
            
            levelsOfDescendants = n.getLevelsOfDescendants() + 1;
            return true;
        }
        return false;
    }
    
    public void uninstantiate() {
        child.unregisterListener(this);
        child.removeParent(this);
        child = null;
        childValue = null;
        levelsOfDescendants = 0;
    }
    
    public void DFSEval(cz.ive.process.Substitution s, List<Sensor> sensors) {
        child.DFSEval(s, sensors);
        initialUpdateValue();
    }
    
    protected UnaryNode<TYPE, CHTYPE> clone() {
        UnaryNode<TYPE, CHTYPE> ret = (UnaryNode<TYPE, CHTYPE>) super.clone();

        ret.child = child.createCopy();
        ret.refreshChildBindings();
        return ret;
    }
    
    public void refreshChildBindings() {
        childValue = child.getValue();
        child.addParent(this);
        child.registerListener(this);
        levelsOfDescendants = child.getLevelsOfDescendants() + 1;
    }
}

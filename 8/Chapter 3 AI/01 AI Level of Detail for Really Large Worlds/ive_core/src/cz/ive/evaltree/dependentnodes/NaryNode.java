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
 * Succesor of all nary operators ( nodes having unknown number of child nodes 
 * of the same type as they are)
 */
public abstract class NaryNode<TYPE extends ValueHolderImpl,
        CHTYPE extends ValueHolderImpl> extends DependentNode<TYPE, CHTYPE> {

    /**
     * Array of child subexpressions
     */
    protected Expr<CHTYPE, ? extends ValueHolderImpl>[] childs;
    
    /**
     * Array of child subexpressions' values
     */
    protected CHTYPE[] childsValue;
    
    /**
     * Create NaryNode instance.
     * @param chlds array of children
     */
    public NaryNode(Expr<CHTYPE, ? extends ValueHolderImpl>[] chlds) {
        childs = chlds;
        refreshChildBindings();        
    }
    
    /**
     * Two NaryNodes are considered to be equal if they are represented by the
     * same class and all children are the same objects. ( references are equal)
     *
     * Two equal nodes has the same contentHashCode
     *
     * @return hash code that uses the real content
     */
    public int contentHashCode() {
        int hash = 0;

        for (int i = 0; i < childs.length; i++) {
            hash += childsValue[i].hashCode();
        }
        return (this.getClass().hashCode() + hash);
    }
        
    /**
     * Two NaryNodes are considered to be equal if they are represented by the
     * same class and all children are the same objects. ( references are equal)
     *
     * <br>This method provides comparison in sense of this definition.
     *
     * @param obj object to be compared with this
     * @return true if obj is equal to this
     */
    public boolean contentEquals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        if (childs.length != ((NaryNode) obj).childs.length) {
            return false;
        }
        for (int i = 0; i < childs.length; i++) {
            if (childs[i] != ((NaryNode) obj).childs[i]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @return i-th member of childs array
     */
    public Expr<CHTYPE, ? extends ValueHolderImpl> getChild(int i) {
        if (i < childs.length) {
            return childs[i];
        }
        return null;
    }
    
    /**
     * @return childs array length
     */
    public int getNumberOfChildren() {
        return childs.length;
    }
    
    public boolean remapChild(Expr<CHTYPE, ? extends ValueHolderImpl> old,
            Expr<CHTYPE, ? extends ValueHolderImpl> n) {
        for (int i = 0; i < childs.length; i++) {
            if (childs[i] == old) {
                old.unregisterListener(this);                
                old.removeParent(this);
                n.addParent(this);
                n.registerListener(this);
                childs[i] = n;
                childsValue[i] = n.getValue();
                initialUpdateValue();
                int childDepth = childs[i].getLevelsOfDescendants() + 1;

                levelsOfDescendants = (levelsOfDescendants > childDepth)
                        ? levelsOfDescendants
                        : childDepth;
                return true;
            }
        }
        return false;
    }
    
    
    protected void updateRegistrations() {
        for (int i = 0; i < childs.length; i++) {
            childs[i].registerListener(this);
        }
        
    }
    
    /**
     * Needed to implement abstract method from Expr
     */
    protected void updateInstantiation(Hook child) {}
    
    public void uninstantiate() {        
        for (int i = 0; i < childs.length; i++) {
            childs[i].unregisterListener(this);
            childs[i].removeParent(this);
            childs[i] = null;
            childsValue[i] = null;
        }
        levelsOfDescendants = 0;
    }

    public void DFSEval(cz.ive.process.Substitution s, List<Sensor> sensors) {
        int i;

        for (i = 0; i < childs.length; i++) {
            childs[i].DFSEval(s, sensors);

        }
            
        if (i == childs.length) {
            initialUpdateValue();
        }
        
    }
    
    protected NaryNode<TYPE, CHTYPE> clone() {
        NaryNode<TYPE, CHTYPE> ret = null;
                
        ret = (NaryNode<TYPE, CHTYPE>) super.clone();
        ret.childs = (Expr<CHTYPE, ? extends ValueHolderImpl>[])
                new Object[childs.length];

        for (int i = 0; i < childs.length; i++) {
            ret.childs[i] = childs[i].createCopy();
        }
        
        ret.refreshChildBindings();
        return ret;
    }
    
    public void refreshChildBindings() {
        childsValue = ((CHTYPE[]) new ValueHolderImpl[childs.length]);
        levelsOfDescendants = 0;
        for (int i = 0; i < childs.length; i++) {
            childsValue[i] = ((CHTYPE) childs[i].getValue());
            childs[i].addParent(this);
            int childDepth = childs[i].getLevelsOfDescendants() + 1;

            levelsOfDescendants = (levelsOfDescendants > childDepth)
                    ? levelsOfDescendants
                    : childDepth;
        }
        updateRegistrations();
    }
    
}

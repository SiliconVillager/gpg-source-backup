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


import cz.ive.IveApplication;
import cz.ive.sensors.Sensor;
import java.util.*;

import cz.ive.messaging.QueueHook;
import cz.ive.process.Substitution;
import cz.ive.evaltree.dependentnodes.DependentNode;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.valueholders.ValueType;


/**
 * Expression class<br/>
 * There are several types of expression trees in IVE used to evaluate fuzzy
 * value expressions, integer expressions and IveObject expressions.
 * This template and its descendant templates contains implementation of
 * properities common to all types of trees.
 * Value of expression is often changed. To prevent recomputation of whole tree
 * because of minor leaf change lazy approach is used.
 * Change spreads from the changed leaf to the root. Only parents that depend
 * on changed children s value are recomputed (similar to C style of
 * evaluating boolean expressions)
 * @param <TYPE> ValueHolder descendant used to keep expressions value
 * @param <CHTYPE> ValueHolder descendant used to keep values of childrens
 *
 */

public abstract class Expr<TYPE extends ValueHolderImpl,
                           CHTYPE extends ValueHolderImpl> 
        extends QueueHook 
        implements Cloneable, java.io.Serializable {
    
    /**
     * Container used to store parents - Expr descendant nodes, that has
     * reference to this node and thus can register as listenner to this node.
     */
    protected ArrayList<DependentNode<? extends ValueHolderImpl, TYPE>> parents;
    
    /**
     * Actual value of expression
     * This reference MUST be the same during whole Expr's life. (Parents has
     * their own references to it)
     * When you want to change value of Expr, don't create new ValueHolder
     * instance.
     * Call old ValueHolder instance's method setValue instead of it.
     */
    protected TYPE value;
    
    /**
     * Length of the longest path to some leaf
     * It is zero for leaves.
     */
    protected int levelsOfDescendants;
    
    /**
     * Each instance of ExprDAG has unique identifier. Value of this variable is
     * set to id of DAG Expr instance is inserted in. Zero means, that
     * Expr istn't in any DAG.
     */
    protected int dagIdentifier;
    
    /**
     * In that case no queue is used to store change notifications and changed
     * methods of registrted listenners are called immediately.
     */
    public Expr() {
        parents = new ArrayList<DependentNode<? extends ValueHolderImpl, TYPE>>();
        instanceNumber = EvalTreeCounters.incExprCounter();
    }
    
    /**
     * Get actual value of the node. It can be undetermined 
     * There are three ways how to make value actual
     *    -by construction of the tree
     *    -by calling DFSEval on the root node
     *    -by spreading messages from the leaves.
     *    
     * Spreading of messages depends on the state of the tree ( whether it is 
     * inserted into some DAG or not)
     *
     * @return actual value 
     */
    public TYPE getValue() {
        return value;
    }
    
    /**
     * Returns value type
     * @return value type
     */
    public ValueType getType() {
        return value.getType();
    }
    
    /**
     * Return the number of nodes that directly depends on the value of this node
     * @return number of parents
     */
    public int getNumberOfParents() {
        return parents.size();
    }
    
    /**
     * Add new parent. Parent is node whose value depends on the value of this 
     * node. This is used mainly during merging trees into DAG. This dependence 
     * is similar to the dependence described by node listenners but it is not 
     * influenced by current value
     *
     * @param p new parent
     */
    public void addParent(DependentNode<? extends ValueHolderImpl, TYPE> p) {
        parents.add(p);
    }
    
    /**
     * Removes parent. Parent is node whose value depends on the value of this 
     * node. This is used mainly during merging trees into DAG. This dependence 
     * is similar to the dependence described by node listenners but it is not 
     * influenced by current value.
     *
     * @param p Parent to be removed
     */
    public void removeParent(DependentNode<? extends ValueHolderImpl, TYPE> p) {
        parents.remove(p);
    }
    
    /**
     * Returns i-th parent of expression
     *
     * @param i index of desired parent (zero based)
     *
     * @return i-th parent ( or null if node has less than i parents)
     */
    public DependentNode<? extends ValueHolderImpl, TYPE> getParent(int i) {
        if (i < parents.size()) {
            return parents.get(i);
        }
        return null;
    }
    
    /**
     * Each DAG has unique identifier. If the node is member of some DAG this 
     * method returns identifier of this DAG.
     *
     * @return if node is member of  some DAG returns DAG identifier>0. 
     *         Otherwise returns 0
     */
    public int getDagIdentifier() {
        return dagIdentifier;
    }
    
    /**
     * Setter of DAG identifier
     * @param dagIdentifier DAG identifier
     * 0 stands for no DAG
     */
    public void setDagIdentifier(int dagIdentifier) {
        this.dagIdentifier = dagIdentifier;
    }
    
    /**
     * Return the size of the path from this node to the deepest leaf of its 
     * subtree
     *
     * @return size of the path
     */
    public int getLevelsOfDescendants() {
        return levelsOfDescendants;
    }
    
    /**
     * Set references from LinkNode subexpressions to objects found
     * in Substitution.
     * Creates new ManagerOfSenses queries and links them to ManagerQuery 
     * subexpressions.<br>
     * Only expression that isn't inserted in DAG yet can be instantiated.
     * 
     * Expr just calls this method on all of it's children.
     * If invoking of the method fails for some child, it returns fail
     * (FuzzyOr has different implementation)
     * 
     * If it fails for particular node its value is undetermined.
     * @param sensors sensors that are passed to the manager of senses query 
     *                that could appear somewhere in the tree
     * @param o Substitution where link nodes search their targets
     */
    public void instantiate(Substitution o, List<Sensor> sensors) {
        int i = 0;
        Expr<CHTYPE, ? extends ValueHolderImpl> chld;
        
        while ((chld = getChild(i)) != null) {
            chld.instantiate(o, sensors);
            i++;
        }

    }
    
    /**
     * Change the sensors of already instantiated expression.
     * It just passes the given list to all children.
     *
     * @param sensors sensors that are passed to the manager of senses query 
     *                that could appear somewhere in the tree
     */
    public void changeSensors(List<Sensor> sensors) {
        int i = 0;
        Expr<CHTYPE, ? extends ValueHolderImpl> chld;
        
        while ((chld = getChild(i)) != null) {
            chld.changeSensors(sensors);
            i++;
        }

    }
    
    /**
     * Unbinds references to other parts of ive, such as attribute values.
     * In contrary to instantiate this is not recursive and the invocation
     * must be done on each node of the expression. ( because of DAG ) 
     * It should be used whenever you want to forget some tree
     */
    public abstract void uninstantiate();
    
    /**
     * Call uninstantiate on the node and whole subtree.
     * This can not be used in case of DAG
     */
    public void uninstantiateTree() {
        int i = 0;
        Expr<CHTYPE, ? extends ValueHolderImpl> chld;

        while ((chld = getChild(i)) != null) {
            chld.uninstantiateTree();
            i++;
        }
        uninstantiate();
    }
    
    /**
     * Replaces old child by new one.
     * Used during inserting tree into DAG when equivalent subexpression is
     * found
     * @param old old child
     * @param n new child
     * @return true on succes
     */
    public abstract boolean remapChild(
            Expr<CHTYPE, ? extends ValueHolderImpl> old,
            Expr<CHTYPE, ? extends ValueHolderImpl> n);
    
    /**
     * Returns i-th child
     * @param i zero based index of child
     * @return child or null if expression has less than i children
     */
    public abstract Expr<CHTYPE, ? extends ValueHolderImpl> getChild(int i);
    
    /**
     * Return the number of nodes whose values directly influence the value 
     * of this node.
     *
     * @return number of children
     */
    public abstract int getNumberOfChildren();
    
    /**
     * Similar to equals method of Object.
     * Unfortunately we need to keep original functionality of equals method
     * (Hook uses Hash) 
     * <br>
     * The two evaltree nodes are considered to be equal if they are represented
     * by the same class and if all references to other objects (
     * evaltree, sources, queries ) are the same.
     * 
     * @param obj Object to compare
     *
     * @return true when obj is equal to this node in sense of this definition
     */
    public abstract boolean contentEquals(Object obj);
    
    /**
     * Similar to hashCode method of Object.
     * Unfortunately we need to keep original functionality of hashCode method
     * (Hook uses Hash)
     *
     * The two evaltree nodes are considered to be equal if they are represented
     * by the same class and if all references to other objects (
     * evaltree, sources, queries ) are the same.
     *
     * @return the same number for two nodes that are equal in the sense of this
     *         definition
     */
    public abstract int contentHashCode();
    
    /**
     * Evaluates expression using Depth First Traversal
     * (i.e evaluates subexpresions first)
     * Used to evaluate uninstantiated expressions. It just asks to the needed
     * values but it does not register to listen changes
     *
     * @param sensors sensors that are passed to the manager of senses query 
     *                that could appear somewhere in the tree
     * @param s substitution used to resolve uninstantiated links
     */
    public abstract void DFSEval(Substitution s, List<Sensor> sensors);
    
    /**
     * Create copy of the expression.
     * This can be invoked only on the expression that was never instantiated or
     * inserted into DAG
     *
     * @return new expression
     */
    public Expr<TYPE, CHTYPE> createCopy() {
        if (dagIdentifier != 0) {
            return null;
        }
        return (clone());
    }
    
    /**
     * Clonning is the first step of expression instantiation
     *
     * @return depth copy of expression.
     */
    protected Expr<TYPE, CHTYPE> clone() {
        Expr<TYPE, CHTYPE> ret = null;
        
        try {
            ret = (Expr<TYPE, CHTYPE>) super.clone();
            ret.value = (TYPE) this.value.clone();
        } catch (CloneNotSupportedException e) {}
        
        ret.parents = 
                new ArrayList<DependentNode<? extends ValueHolderImpl, TYPE>>();
        
        ret.listeners = new java.util.HashSet<cz.ive.messaging.Listener>();
        ret.instanceNumber = EvalTreeCounters.incExprCounter();
        return ret;
    }
    
    /**
     * Create new child bindings.
     * Before calling this only references given to node in constructor must be
     * set. All other bindings will be created (registerListenner, references to
     * children's value holders)
     */
    public void refreshChildBindings() {}
    
    /**
     * This is used by GUI to show the content of this node
     * @return description string
     */
    public String getInfo() {
        boolean debug = IveApplication.debug;
        String cn = this.getClass().getName();

        cn = cn.substring(cn.lastIndexOf('.') + 1);
        String args = getInfoArguments();

        if (args != "") {
            args = "(" + args + ")";
        }
        if (debug) {
            return cn + args + " : id=" + instanceNumber + " level="
                    + getLevelsOfDescendants();
        }
         
        return cn + args;
    }
    
    /**
     * This is used by getInfo to get arguments of this node
     * @return arguments description
     */
    public String getInfoArguments() {
        return "";
    }
    
    /**
     * unique Expr identifier
     */
    public int instanceNumber;
    
}

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


import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;
import cz.ive.messaging.QueueHook;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.logs.Log;
import cz.ive.sensors.Sensor;
import cz.ive.simulation.Updateable;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Used to store set of expressions. (DAG = directed acyclic graph)
 * If there are some  equivalent subexpressions they are evaluated only once.
 * Similar to RETE structure.
 * Changes are evaluated by calling update method.
 *
 */
public abstract class ExprDAG<ROOTTYPE extends ValueHolderImpl> 
        implements Updateable, java.io.Serializable {
    
    /**
     * Queue shared by all Expr nodes in DAG used to spread changes
     */
    protected Queue<QueueHook> queue;
    
    /**
     * Storage for candidates for equal subexpressions. 
     * Used during merging tree into DAG.
     * It is initially filled by the leaves. During the merge all parents of
     * nodes that are equal to some node from the new expression are added.
     */
    private ContentEqualSet equalCandidates;
    
    /**
     * DAG leaves
     */
    private Set<Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl>>
            leaves;
    
    /**
     * DAG roots
     */
    private HashMap<Expr<ROOTTYPE, ? extends ValueHolderImpl>, Integer> roots;

    /**
     * identifier of DAG instance
     */
    private final int dagId;
    
    /**
     * used by removeExpression to store childs to be removed (BFS of the tree)
     */
    private LinkedList<Expr> tmp;
    
    /**
     * Creates a new instance of ExprDAG.
     * This doesn't include creation of queue. That's why this class is
     * abstract.
     */
    public ExprDAG() {
        roots = 
              new HashMap<Expr<ROOTTYPE, ? extends ValueHolderImpl>, Integer>();
        leaves = 
              new HashSet<Expr<? extends ValueHolderImpl, 
                               ? extends ValueHolderImpl>>();
        
        equalCandidates = new ContentEqualSet();
        tmp = new LinkedList<Expr>();
        dagId = EvalTreeCounters.incDagCounter();
    }
    
    /**
     * Insert new expression into DAG.
     * You wont be able to get it back from DAG
     * 
     * The expression must be already instantiated.
     * 
     * It returns the given expression merged with the content of the DAG.
     * The returned reference can be the same as passed or reference to another
     * expression already inserted into the DAG.
     * Never keep the reference you passed into this function. Use only the 
     * returned version.
     *
     * @param e expression to be inserted
     *
     * @return the DAG version of given expression
     */
    public Expr<ROOTTYPE, ? extends ValueHolderImpl> insertExpression(
            Expr<ROOTTYPE, ? extends ValueHolderImpl> e
            ) {
        
        // we start with the comparison from the leaves
        for (Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> e1:leaves) {
            equalCandidates.insert(e1);
        }
 
        Expr<ROOTTYPE, ? extends ValueHolderImpl> dagVersion;

        dagVersion = (Expr<ROOTTYPE, ? extends ValueHolderImpl>) insertTree(e);
        Integer count;

        // put among the roots or increase counter if the dagVersion already is
        // root of another expression
        if ((count = roots.get(dagVersion)) != null) {
            roots.put(dagVersion, count + 1);
        } else {
            roots.put(dagVersion, 1);
        }
        equalCandidates.clear();
        
        return dagVersion;
    }
    
    /**
     * Postfix DFS pass of the tree to be inserted.
     * Node's queue used to store change notifications is changed.
     * Function is recursively called to all childs
     * If node equals to any member of equalCandidates, equal subexpression
     * was found.In that case node's parent's method remapChild is called
     * and parent of node from equalCandidates is inserted into equalCandidates.
     * equalCandidates is filled by DAG leaves (in insertExpression method)
     * at first
     * @param newExpr Expr to be inserted
     * @return the expression from the DAG that is equivalent to the newExpr.
     *          It may be the same expresion, but not necessary
     */
    private Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> 
            insertTree(Expr<? extends ValueHolderImpl, 
                            ? extends ValueHolderImpl> newExpr) {
        int i = 0;
        Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> child;
        Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> dagExpr;
        
                
        // process the children first ( postfix DFS ) 
        while ((child = newExpr.getChild(i)) != null) {
            insertTree(child);
            i++;            
        }
        
        // If the node is equal to some candidate replace it by the candidate.
        if ((dagExpr = equalCandidates.containsEqual(newExpr)) != null) {
            Expr dagParent;
            Expr newParent;

            i = 0;
            while ((dagParent = dagExpr.getParent(i)) != null) {
                i++;
                if (dagParent.getDagIdentifier() == dagId) {
                    equalCandidates.insert(
                            (Expr<? extends ValueHolderImpl,
                                ? extends ValueHolderImpl>) dagParent);
                }
            }
            
            i = 0;
            while ((newParent = newExpr.getParent(i)) != null) {
                if (newParent.getDagIdentifier() != dagId) {
                    // new Expr in newParent is replaced by oldExpr
                    newParent.remapChild(newExpr, dagExpr);
                } else {
                    // we need to increment the index only if the number of
                    // parents did not decrease
                    i++;
                }
            }
            
            newExpr.uninstantiate();
            return dagExpr;
            
        } else {
            // New node
            if (newExpr.getNumberOfChildren() == 0) {
                leaves.add(newExpr);
                equalCandidates.insert(newExpr);
            }
            newExpr.changeQueue(queue, true);
            newExpr.setDagIdentifier(dagId);
            return newExpr;
        }
        
    }
    
    /**
     * Remove expression  from DAG.
     * Destroys removed expression but there could be common subexpressions that
     * remains in the dag. Anyway do not keep the reference to the removed 
     * expression. 
     *
     * Breath first traversal of DAG from given root.
     * Removes only nodes, that hasn't parent any more
     *
     * @param expr expression to be removed
     */
    public void removeExpression(Expr expr) {
        Expr e;
        Expr child;
        
        tmp.clear();
        // expr is one of the roots. Get the number of usages of particular root.
        Integer count = roots.get(expr); 

        if (count == null) {
            Log.severe("ExprDAG :removing expression that was not inserted");
            return;
        }
        count--;
        if (count > 0) {
            // root was shared by more expressions - decrease counter and return
            roots.put(expr, count);            
            return;
        }
        // root is not used any more - remove it
        roots.remove(expr);
        
        tmp.offer(expr);
        // BFS
        while ((e = tmp.poll()) != null) {
            // if the node has some parent or is root of another expression
            if (e.getNumberOfParents() != 0 || (roots.get(e) != null)) {
                continue;
            }
            int i = 0;

            while ((child = e.getChild(i)) != null) {
                tmp.add(child);
                i++;
            }            
            e.uninstantiate();
            e.setDagIdentifier(0);
            leaves.remove(e);
        }
    }
    
    /**
     * Evaluate all changes in DAG.
     * Only changing values of leaves doesnt effect values of roots untill you
     * call this function
     */
    public void update() {
        QueueHook q;

        while ((q = queue.poll()) != null) {            
            q.processSignal();
        }
    }
    
    /**
     * Returns true if there is some parg of the DAG that needs to be 
     * reevaluated
     *
     * @return true if there is some waiting event in the queue of change 
     *         notifications.
     */
    public boolean needUpdate() {
        return !queue.isEmpty();
    }
    
    /**
     * Change the sensors of all expressions stored in the DAG.
     * It just passes the given list to all roots.
     *
     * @param sensors sensors that are passed to the manager of senses query 
     *                that could appear somewhere in the DAG
     */
    public void changeSensors(List<Sensor> sensors) {
        for (Expr e:roots.keySet()) {
            e.changeSensors(sensors);
        }
    }
    
    /**
     * Write current DAG into *.dot file. Dot file is used to represent graphs 
     * and can be processed by GraphViz tool (http://www.graphviz.org).
     * @param f file the DAG is written to
     */
    public void dump(File f) {
        
        PrintWriter out = null;

        try {
            f.createNewFile();
            out = new PrintWriter(f);
        } catch (Exception e) {
            assert(false);
        }
                
        out.println("digraph DAG_" + dagId + " {\n" );

        HashSet<Integer> visited = new HashSet<Integer>();
        LinkedList<Expr> queue = new LinkedList<Expr>();
        
        queue.addAll(roots.keySet());
        for (Expr exp:roots.keySet()) {
            visited.add(exp.instanceNumber);
        }
        while (!queue.isEmpty()) {
            Expr node = queue.poll();
            String nodeId = "id" + node.instanceNumber;
            Integer instances = roots.get(node);

            out.println(
                    nodeId + "[ label=\"" + node.getInfo()
                    + ((instances != null) ? " used:" + instances : "")
                    + /* " id:" + String.format("%h",node.hashCode()) + */ "\"];");
            int i = 0;
            Expr child;

            while ((child = node.getChild(i)) != null) {
                i++;
                out.println(nodeId + "-> id" + child.instanceNumber + ";");
                if (visited.add(child.instanceNumber)) {
                    queue.addLast(child);
                }
            }
        }
        
        out.println("}");
        out.close();
        
    }
}


/**
 * Class used to store candidates to equal expressions during inserting into DAG
 * Similar to java.utils.Set. The main difference is that it uses SetItem inner
 * class to translate items contentHashCode into hashCode.
 * , but it can return reference to stored value
 * (java.utils.Set returns only true/false)
 *
 * 
 */
class ContentEqualSet implements java.io.Serializable {

    /**
     * This class represents one item of the ContentEqualSet.
     * It holds the expression and delegates hashCode invocation to the expressions 
     * contentHashCode method.
     */
    static class SetItem implements java.io.Serializable {
        public SetItem() {}

        /**
         * Constructor
         * @param i the expression holded by this item
         */
        public SetItem(Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> i) {
            item = i;
        }
        
        /**
         * Delegate to the expression's method contentHashCode
         * If two expressions are equivalent this method returns the same number.
         * @return hashcode that reflects stored node content and meaning.
         */
        public int hashCode() {
            return item.contentHashCode();
        }

        /**
         * Delegate to the expression's method contentEquals
         * @return true if obj contains equivalent expression
         * @param obj another SetItem object
         */
        public boolean equals(Object obj) {
            return item.contentEquals(((SetItem) obj).item);
        }
        
        /**
         * Expression wraped by this SetItem
         */
        private Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> item;

        public Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> getItem() {
            return item;
        }

        public void setItem(Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> item) {
            this.item = item;
        }
    }

    /**
     * holds nodes that may be equivalent to the most deepest not yet inserted
     * nodes of new tree
     */

    private HashMap<SetItem, Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl>> cont;
    
    /**
     * Constructor
     */
    ContentEqualSet() {
        cont = new HashMap<SetItem, Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl>>();
    }
    
    /**
     * insert expression into structure
     * @param e expression to be inserted
     */
    public void insert(Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> e) {
        cont.put(new SetItem(e), e);
    }
    
    /**
     * remove expression from the structure
     * @param e expression to be removed
     */
    public void remove(Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> e) {
        toCompare.setItem(e);
        cont.remove(toCompare);
        toCompare.setItem(null);
    }
    
    /**
     * remove expression from the structure
     */
    public void clear() {
        cont.clear();
    }
    
    /**
     * Return the node from the structure that is equivalent to the e
     * @param e Expr node
     * @return Expr node that is equivalent to e
     */
    public Expr<? extends ValueHolderImpl, ? extends ValueHolderImpl> 
            containsEqual(Expr<? extends ValueHolderImpl, 
                               ? extends ValueHolderImpl> e) {
        toCompare.setItem(e);
        return cont.get(toCompare);
    }
    
    SetItem toCompare = new SetItem();
}

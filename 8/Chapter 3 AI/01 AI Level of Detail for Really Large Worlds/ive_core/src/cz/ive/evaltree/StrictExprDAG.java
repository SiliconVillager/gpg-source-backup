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


import java.util.*;
import cz.ive.messaging.QueueHook;


/**
 * update order is strictly given by depth of node
 * Value of each node is evaluated only once during update()
 * This type of DAG should be used for trees with evaluating
 * takes lot of time 
 * @author thorm
 */
public class StrictExprDAG extends ExprDAG {

    /** Creates a new instance of SetExprDAG */
    public StrictExprDAG() {
        super();
        queue = new PriorityQueue<QueueHook>(50, new ExprComparator());
    }
    
    /**
     * Helper class to allow java.io.Serializable marker iface implementation.
     */
    protected class ExprComparator implements Comparator<QueueHook>, 
                java.io.Serializable {
        
        /**
         * The a is considered to be smaller than b if the path from a to its
         * nearest leaf is smaller than path from b to its nearest leaf.
         * The smaller expressions will be evaluated first.
         * 
         * If a and b has the same level of descendants ( size of paths to
         * the leaf ) their instanceNumber is used for comparison to enforce
         * that no pair is considered equal. It would lead to the ommiting 
         * notifications.
         * @param a expression
         * @param b expression
         * @return negative number if a&lt;b and vice versa
         */
        public int compare(QueueHook a, QueueHook b) {
            Expr ae = (Expr) a;
            Expr be = (Expr) b;
            int adepth = ae.getLevelsOfDescendants();
            int bdepth = be.getLevelsOfDescendants();

            // lower number must be evaluated sooner
            if (adepth < bdepth) {
                return -1;
            }
            if (adepth > bdepth) {
                return 1;
            }
                
            return ae.instanceNumber - be.instanceNumber;
        }
            
    }
}

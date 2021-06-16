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
 
package cz.ive.evaltree.mos;


import cz.ive.process.Substitution;
import cz.ive.valueholders.FuzzyValueHolder;
import java.util.List;

import cz.ive.evaltree.Expr;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.iveobject.Link;
import cz.ive.manager.ManagerOfSenses.ReturnSet;
import cz.ive.sensors.Sensor;


/**
 * Ask the MOS whether the given sensors see any object of particular 
 * properities.
 * Result of this node is true if there is such object.
 *
 * @author thorm
 */
public class FuzzyManagerQuery extends ManagerQuery<FuzzyValueHolderImpl> {
    
    
    /** Creates a new instance of FuzzyManagerQuery
     *  @param role link that the result object must match
     *  @param condition evaltree tree that defines the constraint to the 
     *                  object from the query result
     */
    public FuzzyManagerQuery(
            Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> condition, 
            Link role) {
        super(condition, role);
        value = new FuzzyValueHolderImpl();
        value.validate();
        mode = ReturnSet.ANY_EMPTY;
    }
    
    /**
     * Set the result to be true if the query result set is not empty and vice
     * versa.
     * The value is allways defined
     */
    protected void updateValue() {
        value.value = (query.resultIsEmpty()
                ? FuzzyValueHolder.True
                : FuzzyValueHolder.False);
    }
    
    /**
     * Invoke the active query and set the result using updateValue
     */
    public void DFSEval(Substitution s, List<Sensor> sensors) {
        
        queryExpr.instantiate(s, null);
        boolean result = invokeQueryActive(sensors).isEmpty();

        value.value = (result ? FuzzyValueHolder.True : FuzzyValueHolder.False);
        value.changed();
        queryExpr.uninstantiate();
    }
}

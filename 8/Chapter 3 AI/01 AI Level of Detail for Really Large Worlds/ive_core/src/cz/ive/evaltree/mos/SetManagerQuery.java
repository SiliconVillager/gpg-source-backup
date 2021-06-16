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


import cz.ive.iveobject.IveObject;
import cz.ive.process.Substitution;

import java.util.*;

import cz.ive.evaltree.Expr;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.iveobject.Link;
import cz.ive.sensors.Sensor;


/**
 * Ask the MOS whether the given sensors see some objects of particular 
 * properities.
 * Result of this node is result of the query.
 * @author thorm
 */
public class SetManagerQuery extends ManagerQuery<SetValueHolderImpl<IveObject> > {
    
    /** Creates a new instance of FuzzyManagerQuery
     *  @param role link that the result object must match
     *  @param condition evaltree tree that defines the constraint to the 
     *                  object from the query result
     */
    public SetManagerQuery(
            Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> condition,
            Link role) {
        super(condition, role);
        value = new SetValueHolderImpl();
        value.validate();
    }
    
    /**
     * Copy the result of the query to this node value.
     * The value is always defined.
     */
    protected void updateValue() {
        Set<IveObject> result = new HashSet<IveObject>(query.getResultSet());
        value.changeValue(result);
    }
    
    /**
     * Invoke the active query and set the result using updateValue
     */
    public void DFSEval(Substitution s, List<Sensor> sensors) {        
        queryExpr.instantiate(s, null);        
        Set<IveObject> result = new HashSet<IveObject>(
                invokeQueryActive(sensors));

        value.changeValue(result);
        value.changed();
        
        queryExpr.uninstantiate();
    }
    
}

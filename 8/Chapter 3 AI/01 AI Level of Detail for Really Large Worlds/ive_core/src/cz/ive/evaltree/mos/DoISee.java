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


import cz.ive.evaltree.Expr;
import cz.ive.iveobject.IveObject;
import cz.ive.messaging.Hook;
import cz.ive.process.Substitution;
import cz.ive.valueholders.FuzzyValueHolder;

import java.util.*;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.logs.Log;
import cz.ive.process.Source;
import cz.ive.sensors.Sensor;
import cz.ive.evaltree.leaves.*;
import cz.ive.iveobject.Link;
import cz.ive.manager.ManagerOfSenses.ReturnSet;


/**
 * Ask the MOS whether the given sensors see any object of particular 
 * properities.
 * Assign this object to the role in the substitution
 *
 * @author thorm
 */
public class DoISee extends ManagerQuery<FuzzyValueHolderImpl> {
    
    /**
     * Source from the substitution where the found object is assigned to.
     */
    Source source;
    
    /** Creates a new instance of DoISee
     *  @param role link that the result object must match
     */
    public DoISee(Link role) {
        super(new FuzzyConstant(FuzzyValueHolder.True), role);
        value = new FuzzyValueHolderImpl();
        mode = ReturnSet.ANY_COPY;
    }

    /** Creates a new instance of DoISee
     *  @param role link that the result object must match
     *  @param condition evaltree tree that defines the constraint to the 
     *                  object from the query result
     */
    public DoISee(
            Link role, 
            Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> condition) {
        
        super(condition, role);
        value = new FuzzyValueHolderImpl();
        mode = ReturnSet.ANY_COPY;
    }
    
    /**
     * result of the query
     */
    protected List<IveObject> result;
    
    public void instantiate(Substitution o, List<Sensor> sensors) {

        source = o.getSource(link.getRole());
        if (source == null) {
            Log.warning("Evaltree/DoISee - Empty source:" + link.getRole());
        }
        super.instantiate(o, sensors);
        
    }
    
    /**
     * Set the node value to True if the result set is not empty. Assign
     * the one item of the result set to the role of substitution that is 
     * determined by the role part of the link passed in the constructor.
     *
     * Set the node value to False it the result set is empty.
     *
     * The value is undefined if the substitution does not contain the specified
     * role
     */
    
    protected void updateValue() {
        if (source == null) {
            Log.warning("Evaltree/DoISee - Empty source:" + link.getRole());
            value.isDefined = false;
            return;
        }
        
        if (query.resultIsEmpty()) {
            ((FuzzyValueHolderImpl) value).value = FuzzyValueHolder.False;
            source.setObject(null);
        } else {
            ((FuzzyValueHolderImpl) value).value = FuzzyValueHolder.True;            
            result = query.getResultSet();
            IveObject obj = result.get(0);
            source.setObject(obj);
        }
        value.isDefined = true;
    }
    
    public void uninstantiate() {
        super.uninstantiate();
        source = null;
    }
    
    protected void updateInstantiation(Hook child) {    
        if (child != query) {
            Log.warning("DoISee - unknown notification");
        }
    }
    
    /**
     * Invoke the active query and set the result using updateValue
     */
    public void DFSEval(Substitution s, List<Sensor> sensors) {
        result = invokeQueryActive(sensors);
        source = s.getSource(link.getRole());
        if (result.isEmpty()) {
            ((FuzzyValueHolderImpl) value).value = FuzzyValueHolder.False;
            source.setObject(null);
        } else {
            ((FuzzyValueHolderImpl) value).value = FuzzyValueHolder.True;            
            IveObject obj = result.get(0);

            source.setObject(obj);
        }
        
        source = null;
        result = null;
    }
    
}

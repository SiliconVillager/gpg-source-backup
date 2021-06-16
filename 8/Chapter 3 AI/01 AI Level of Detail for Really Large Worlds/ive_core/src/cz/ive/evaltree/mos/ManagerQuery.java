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


import cz.ive.evaltree.*;
import cz.ive.messaging.*;
import cz.ive.process.Substitution;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.manager.*;
import cz.ive.evaltree.dependentnodes.*;
import cz.ive.exception.*;
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.Link;
import cz.ive.sensors.*;
import cz.ive.lod.*;
import cz.ive.logs.Log;
import cz.ive.manager.ManagerOfSenses.ReturnSet;
import java.util.*;


/**
 * Succesor of all nodes that asks the Manager of senses
 * @author thorm
 */
public abstract class ManagerQuery<T extends ValueHolderImpl>
        extends GenericHookDependentNode<T> implements Listener {
    
    /**
     * Expression representing the query.
     * mow wil create QTQuery from it.
     */
    protected Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> queryExpr;

    /**
     * Query result must match this link
     */
    protected Link link;

    /**
     * mode of query passed to the MOS
     */
    protected ReturnSet mode = ReturnSet.ALL_EMPTY;
    
    /**
     * query handler returned by MOS
     */
    protected QTQuery query;

    /**
     * Create a new instance of ManagerQuery
     *
     * @param queryExpr evaltree tree that defines the constraint to the 
     *                  object from the query result
     * @param role another constraint - returned objects must match this link
     */
    public ManagerQuery(
            Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl> queryExpr, 
            Link role) {        
        this.queryExpr = queryExpr;
        link = role;
    }
    
    /**
     * Ask Manager of senses to create new query and register as listenner on 
     * it
     */
    public void instantiate(Substitution o, List<Sensor> sensors) {
        try {
            query = ManagerOfSenses.instance().queryPassive(sensors,
                    createQueryData(), null, mode);
            query.registerListener(this);

        } catch (OntologyNotSupportedException e) {
            Log.warning("ManagerQuery - OntologyNotSupported:");
        }
        initialUpdateValue();
    }
    
    /**
     * Change sensors of already used query
     */
    public void changeSensors(List<Sensor> sensors) {
        if (query != null) {
            query.changeSensors(sensors);
        }
    }
    
    /**
     * Needed to implement abstract method of DependentNode
     */
    protected void updateInstantiation(Hook child) {}
    
    /**
     * Destroy the query
     */
    public void uninstantiate() {
        ManagerOfSenses.instance().unregisterPassive(query);
        query = null;
        queryExpr = null;
    }
    
    protected ManagerQuery clone() {
        ManagerQuery ret = (ManagerQuery) super.clone();
        
        if (queryExpr != null) {
            ret.queryExpr = queryExpr.createCopy();
        }
        return ret;
    }
    
    /**
     * Two ManagerQuery objects are never equal
     */
    public int contentHashCode() {
        return (this.getClass().hashCode() + query.hashCode());
    }

    /**
     * Two ManagerQuery objects are never equal
     */
    public boolean contentEquals(Object obj) {
        return ((obj.getClass() != this.getClass())
                && (query == ((ManagerQuery) obj).query)
                );
    }
    
    public void canceled(Hook h) {
        if (h == query) {
            query.unregisterListener(this);
            query = null;
        }
    }
    
    /**
     * Create active query 
     *
     * @param sensors list of sensors passed to the MOS to evaluate the query
     */
    List<IveObject> invokeQueryActive(List<Sensor> sensors) {
        try {
            return ManagerOfSenses.instance().queryActive(sensors,
                    createQueryData(), null, mode);
        } catch (OntologyNotSupportedException e) {
            Log.warning("Manager query - Ontology of the query not supported");
        }
        return null;
    }
    
    /**
     * Prepare the data for the MOS
     * @return MOS structure to ask the question
     */
    QueryData createQueryData() {
        return  new QueryData(queryExpr, link, new LOD()); 
    }
    
    /**
     * @return "*" if the s is empty or the s
     */
    String wrapLinkString(String s) {
        if (s.length()==0) {
            return "*";
        }
        return s;
    }
    
    public String getInfoArguments() {
        return wrapLinkString(link.getGoal()) + ";"
                + wrapLinkString(link.getProcess()) + ";"
                + wrapLinkString(link.getRole());
    }
}

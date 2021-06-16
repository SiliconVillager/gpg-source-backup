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
 
package cz.ive.manager;

import cz.ive.iveobject.*;
import cz.ive.evaltree.Expr;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.messaging.Listener;
import cz.ive.sensors.*;
import cz.ive.process.*;
import cz.ive.valueholders.FuzzyValueHolder;
import java.util.*;

/**
 * Box of passive query.
 * Stores query expression, evaluate it continuously and notify listeners 
 * about changes in the set of query passing objects.
 *
 * @author Jirka
 */
public class QTQuery extends QueryTrigger implements Listener {
    
    /** IveObjects returned by query are at this root of set Expr */
    private Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl> expr;
    
    /** All data of query */
    private QueryData data;
    
    /** Substitution passed with the query */
    private Substitution sources;
    
    /** Set of objects passing the query */
    private Set<IveObject> resultSet;
    
    /** The last returned result set */
    private List<IveObject> lastResult;
    
    /** flags determining how will be objects returned */
    private ManagerOfSenses.ReturnSet flags;
    
    /** Objects staying on input of the query */
    private Set<IveObject> objects;

    /** Map from objects to expression instances */
    private HashMap<IveObject,
	    Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl>> objMapExpr;

    /** Map from expression instances to objects */
    private HashMap<Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl>, 
	    ObjectSatisfaction> exprMapObj;
    
    /** If set to true, listeners will not be notified about changes */
    private boolean forbidNotifying;
    
    /**
     * Creates new trigger to manage passive updates.
     *
     * @param man refernece to the Manager of Senses
     * @param sens list of sensors
     * @param input IveObject to be passed to procession of expression
     * @param d data of the query including expression
     * @param srcs substituted variables
     * @param f specifies how will be objects returned
     */
    public QTQuery(ManagerOfSenses man,
	    List<Sensor> sens,	    
	    Set<IveObject> input,
	    QueryData d, 
            Substitution srcs,
	    ManagerOfSenses.ReturnSet f) {

        objects = new HashSet<IveObject>();
        resultSet = new HashSet<IveObject>();
        objMapExpr = new HashMap<IveObject,
                Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl>>();
        exprMapObj = new HashMap<
                Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl>, 
                ObjectSatisfaction>();
	lastResult = null;
        
	manager = man;
	data = d;
        sensors = new Vector<Sensor>();
	sensors.addAll(sens);
	expr = (Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl>) 
                    d.getExpr();
	flags = f;
        sources = srcs;
        forbidNotifying = false;

        for (IveObject obj: input) {	    
	    addToInput( obj);
	}		
    }
    
    /**
     * Gets set of objects satisfactory to query according to result flag.
     *
     * @return set of IveObjects that realize the query
     */
    public List<IveObject> getResultSet() {
	
	List<IveObject> copySet = new ArrayList<IveObject>();
        lastResult = 
                manager.makeResultSet(lastResult, resultSet, sensors, flags);
	return lastResult;		
    }
    
    /**
     * Test whether is some object in result set
     *
     * @return true if result contains some object
     */
    public boolean resultIsEmpty() {
	return resultSet.isEmpty();
    }
    
    /**
     * Process query expression for added object.
     *
     * @param object object to be added
     */
    void addToInput(IveObject object) {		
	Link role = data.getRole();
	if (role != null && !object.getLinks(role.getGoal(),
                role.getProcess(),role.getRole()).isEmpty()) {
          
            objects.add(object);

            Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl> e 
                    = expr.createCopy();
	
            Substitution subst;
            if (sources != null) {
                subst = sources.duplicateSubstitution(true);
                subst.addSlot("setitem", new Slot(object), false);
            } else {
                subst = 
                    new SubstitutionImpl("setitem", new Slot(object), false);
            }

            boolean fine;
	    e.instantiate(subst, sensors);
            fine = e.getValue().isDefined();

            //evaluation of expression
            ObjectSatisfaction os = new ObjectSatisfaction(object);
            if ((e.getValue().value == FuzzyValueHolder.True) && fine) {
                os.satisfy = true;
                resultSet.add(object);
                changed(null);
            }
            
	    objMapExpr.put(object, e);
	    exprMapObj.put(e, os);
            e.registerListener(this);
        }
    }
    
    /**
     * Removes or adds objects in expression to reach the given state.
     * Makes intelligent diff. Consideres only object with appropriate role
     * @param input Objects that will be in the input after that call
     */
    void alterInput(Set<IveObject> input) {
        Set<IveObject> tmp = new HashSet<IveObject>();
        
        forbidNotifying = true;
        
        tmp.addAll(objects);

        for (IveObject object : tmp) {
            if (!input.contains(object)) {
                removeFromInput(object);
            }
        }
        
        input.removeAll(objects);
        
        for (IveObject object : input) {
            addToInput(object);
        }
        
        forbidNotifying = false;
        changed(null);
    }
    
    /**
     * Remove object from input of query expression.
     *
     * @param object object to be removed
     */
    void removeFromInput(IveObject object) {	
        objects.remove(object);

	Expr e = objMapExpr.remove(object);
        if (e != null) {
            e.uninstantiateTree();
            e.unregisterListener(this);
            exprMapObj.remove(e);
        }
        if (resultSet.contains(object)) {
            resultSet.remove(object);
            changed(null);
        }
    }
    
    /**
     * Emit signal to listener, registred to this QTQuery.
     * @param initiator root Expr which value set has changed.
     */
    public void changed(cz.ive.messaging.Hook initiator) {	
        if (initiator != null) {
            ObjectSatisfaction os;
            Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl> e = 
                    (Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl>) 
                        initiator;
            if ((os = exprMapObj.get(e)) != null) {
                if ((os.satisfy == true) &&
                        (e.getValue().value == FuzzyValueHolder.False)) {
                    resultSet.remove(os.object);
                    os.satisfy = false;
                    if (!forbidNotifying) {
                        notifyListeners();
                    }
                }
                else if ((os.satisfy == false) &&
                        (e.getValue().value == FuzzyValueHolder.True)) {
                    resultSet.add(os.object);
                    os.satisfy = true;
                    if (!forbidNotifying) {
                        notifyListeners();
                    }
                }
            }
        } else {
            if (!forbidNotifying) {
               notifyListeners();
            }
        }
    }
    
    /** 
     * This method is used when deleting this object to unregister from
     * all registered hooks.
     * @param initiator Initiator of the cancellation. Only if "this" is passed,
     *        there will be an effect.
     */
    public void canceled(cz.ive.messaging.Hook initiator) {	
        if (initiator == this) {
            forbidNotifying = true;
            for (IveObject o : objects) {
                Expr e = objMapExpr.remove(o);
                if (e != null) {
                    e.uninstantiateTree();
                    e.unregisterListener(this);
                }
            }
            objects.clear();
            resultSet.clear();
            exprMapObj.clear();
        }
    }
 
    /** 
     * class for remembering IveObject and whether it's satisfactory or not
     */
    private class ObjectSatisfaction implements java.io.Serializable {
	
        /** Object itself */
	public IveObject object;
        
        /** Determines whether the object satisfies the query */
	public boolean satisfy;
	
        /** 
         * Creates a new ObjectSatisfaction for the given object.
         * Sets satisfy member to false.
         * @param o The object to be contained
         */
	public ObjectSatisfaction(IveObject o) {
	    object = o;
	    satisfy = false;
	}
    }


    public void changeSensors(List<Sensor> sens) {	
        super.changeSensors(sens);
        
        resultSet.clear();
        
        Set<IveObject> objects = 
                            manager.getInputObjects(sensors, null, null);                    
        alterInput(objects);
        
        for (Map.Entry<Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl>, 
                ObjectSatisfaction> e : 
                exprMapObj.entrySet()) {
        
            e.getKey().changeSensors(sens);
            
            boolean fine = e.getKey().getValue().isDefined();
            if ((e.getKey().getValue().value == FuzzyValueHolder.True) 
                    && fine) {
                e.getValue().satisfy = true;
                resultSet.add(e.getValue().object);
            } else {
                e.getValue().satisfy = false;
            }

        }

        changed(null);
    }
}

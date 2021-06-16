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
 
package cz.ive.evaltree.topology;


import cz.ive.evaltree.Expr;
import cz.ive.evaltree.dependentnodes.GenericHookDependentNode;
import cz.ive.evaltree.valueholdersimpl.FuzzyValueHolderImpl;
import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import cz.ive.messaging.Hook;
import cz.ive.process.Substitution;
import cz.ive.sensors.Sensor;
import java.util.List;


/**
 * Common succesor of all nodes related to the relative position of two objects
 * from substutuion
 *
 * @author thorm
 */
public abstract class BinaryTopologyProposition 
        extends GenericHookDependentNode<FuzzyValueHolderImpl> {
    
    /**
     * Provides the position of the first object and keeps it up to date
     */
    SourceToPosition position1;
    
    /**
     * Provides the position of the second object and keeps it up to date
     */
    SourceToPosition position2;
    
    /**
     * Role of the first object
     */
    String role1;
    
    /**
     * Role of the second object
     */
    String role2;

    /** 
     * Creates a new instance of BinaryTopologyProposition 
     * 
     * @param firstRole role in the substitution of the first object 
     * @param secondRole role in the substitution of the first object 
     */
    public BinaryTopologyProposition(String firstRole, String secondRole) {        
        value = new FuzzyValueHolderImpl();
        role1 = firstRole;
        role2 = secondRole;
    }
    
    /**
     * @return <CODE>null</CODE>
     */
    public Expr<ValueHolderImpl, ? extends ValueHolderImpl> getChild(int i) {
        return  null;
    }
    
    /**
     * Bind the position{1,2} member values to the objects from the substitution
     * determined by the roles
     */
    public void instantiate(Substitution s, List<Sensor> sensors) {
        position1.instantiate(s);
        position2.instantiate(s);
        initialUpdateValue();
    }
    
    /**
     * Release all binds to the substitution
     */
    public void uninstantiate() {
        position1.uninstantiate();
        position2.uninstantiate();
        position1 = null;
        position2 = null;
        
    }
    
    /**
     * Needed to implement abstract method
     */
    protected void updateInstantiation(Hook child) {}
    
    /**
     * Two BinaryTopologyPropositions are equal if they are represented by the
     * same class and theis position member variables are equal
     */
    public int contentHashCode() {        
        return this.getClass().hashCode() + position1.contentHashCode()
                + position2.contentHashCode();
    }

    /**
     * Two BinaryTopologyPropositions are equal if they are represented by the
     * same class and theis position member variables are equal
     */
    public boolean contentEquals(Object obj) {
        if (obj.getClass() == getClass()) {
            BinaryTopologyProposition btp = (BinaryTopologyProposition) obj;

            return position1.contentEquals(btp.position1)
                    && position2.contentEquals(btp.position2);
        }
        return false;
    }
    
    public void DFSEval(cz.ive.process.Substitution s, List<Sensor> sensors) {
        position1.evaluate(s);
        position2.evaluate(s);
        updateValue();    
    }
    
    protected BinaryTopologyProposition clone() {
        assert ((position1.object == null) && (position1.source == null)
                && (position1.waypoint == null) && (position2.object == null)
                && (position2.source == null) && (position2.waypoint == null));
        BinaryTopologyProposition ret = (BinaryTopologyProposition) super.clone();
        return ret;
    }
    
    public String getInfoArguments() {
        return role1 + "," + role2;
    }
}

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
 
package cz.ive.evaltree.links;


import cz.ive.evaltree.valueholdersimpl.FuzzyValueHolderImpl;
import cz.ive.logs.Log;
import cz.ive.messaging.Hook;
import cz.ive.valueholders.FuzzyValueHolder;
import cz.ive.evaltree.dependentnodes.SourceDependentNode;
import cz.ive.process.Substitution;
import cz.ive.sensors.Sensor;
import java.util.List;


/**
 * This node is true if the particular role in given substitution is assigned.
 *
 * @author pocht1am
 */
public class FuzzyEmptySource extends SourceDependentNode<FuzzyValueHolderImpl> {
    
    /** Creates a new instance of FuzzyEmptySource 
     *
     *  @param role substitution role we are interested in
     */
    public FuzzyEmptySource(String role) {
        super(role);
        value = new FuzzyValueHolderImpl();
        value.validate();
    }

    public void DFSEval(Substitution s, List<Sensor> sensors) {
        source = s.getSource(role);
        if (source == null) {
            Log.warning(
                    "Evaltree/FuzzyEmptySource - No source for role: " + role);
        }
        initialUpdateValue();
        source = null;
    }

    /**
     * Set the value to True if the role in the substitution is assigned
     * Set the value to False otherwise
     */
    protected void updateValue() {
        value.value = (source.getObject() == null)
                ? FuzzyValueHolder.True
                : FuzzyValueHolder.False;
    }
    
    protected void updateInstantiation(Hook child) {
        if (child != source) {
            Log.warning("FuzzyEmptySource - unknown notification");
        }
    }

    public String getInfoArguments() {
        return role;
    }
}

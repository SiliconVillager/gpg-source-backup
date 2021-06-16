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


import cz.ive.iveobject.IveId;
import cz.ive.valueholders.FuzzyValueHolder;
import cz.ive.evaltree.leaves.*;


/**
 * Fuzzy expression leaf that takes two strings as parameter.
 * The first object is real object (that is typically able to move ) 
 * and second object is location. Location does not have a possition attribute.
 *
 * It is true when first object stands on the second object or its descendant.
 *
 * @author thorm
 */
public class FuzzyNotIn extends BinaryTopologyProposition {
    
    /**
     * Creates a new instance of NotIn 
     *
     * @param firstRole role of the object
     * @param secondRole role of the location
     */
    public FuzzyNotIn(String firstRole, String secondRole) {
        super(firstRole, secondRole);
        position1 = new SourceToObjectPosition(firstRole, this);
        position2 = new SourceToPosition(secondRole, this);
        
    }

    /**
     * Set the value to True if the object stays on the location or its 
     * descendant
     *
     * The value is defined if both roles are assigned in the substitution.
     */
    protected void updateValue() {
        IveId wp1id = position1.getPosition();
        IveId wp2id = position2.getPosition();
        
        if (wp1id == null || wp2id == null) {
            value.isDefined = false;
            return;
        }
        
        value.isDefined = true;
        
        value.value = (wp1id.getId().equals(wp2id.getId())
                || wp1id.isParent(wp2id) || wp2id.isParent(wp1id)
                )
                        ? FuzzyValueHolder.False
                        : FuzzyValueHolder.True;
    }
    
    protected FuzzyNotIn clone() {
        FuzzyNotIn ret = (FuzzyNotIn) super.clone();

        ret.position1 = new SourceToObjectPosition(role1, ret);
        ret.position2 = new SourceToPosition(role2, ret);
        return ret;
    }
}

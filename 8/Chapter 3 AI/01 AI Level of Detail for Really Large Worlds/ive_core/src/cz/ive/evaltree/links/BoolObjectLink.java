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
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.attributes.AttrObject;
import cz.ive.valueholders.FuzzyValueHolder;
import cz.ive.valueholders.ValueType;


/**
 * This node has true value if the particular item of substitution has 
 * filled particular object attribute.
 *
 * Replaced by 
 * <c> Defined(IveObjectAttr()) </c>
 * @deprecated
 * @author thorm
 */
public class BoolObjectLink extends LinkNode<FuzzyValueHolderImpl> {
    
    /** Creates a new instance of BoolObjectLink 
     *
     *  @param role role of the object in the substitution
     *  @param attribute attribute name
     */
    public BoolObjectLink(String role, String attribute) {
        super(role, attribute);
        value = new FuzzyValueHolderImpl();
    }

    /**
     *  @return the attribute value
     */
    public IveObject getObject() {
        return ((AttrObject) attr).getValue();
    }
    
    /**
     *  Set the fuzzy value of this node to True if the attribute contains
     *  some IveObject or to False otherwise
     */
    protected void updateValue() {
        if (attr != null) {
            value.value = (((AttrObject) attr).getValue() == null)
                    ? FuzzyValueHolder.False
                    : FuzzyValueHolder.True;
            value.isDefined = true;
        } else {
            value.isDefined = false;
        }
    }

    /**
     * @return true if the attribute is AttrObject.
     */
    boolean checkType() {
        return attr instanceof AttrObject;
    }
    
}

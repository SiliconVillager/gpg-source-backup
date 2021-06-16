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
 
package cz.ive.evaltree.valueholdersimpl;


import cz.ive.valueholders.ValueHolder;


/**
 * ValueHoldesr implementation used in evaltree. Adds changed() method
 * @author thorm
 */
public abstract class ValueHolderImpl implements ValueHolder, Cloneable {

    /**
     * True if this value holder is keeping defined value.
     */
    public boolean isDefined;
    
    /**
     * Old version of isDefined member variable.
     * It is used to detect change of state
     */
    boolean oldIsDefined;
    
    /**
     * has value changed from last call of this function?
     * @return true if value has changed from the last call of this fucntion
     */
    public boolean changed() {
        return true;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {}
        return null;
    }
    
    /**
     * Create a new instance of ValueHolderImpl
     * The value is initialy undefined.
     */
    public ValueHolderImpl() {
        isDefined = false;
    }
    
    /**
     * @return true if the value is defined and vice versa
     */
    public boolean isDefined() {
        return isDefined;
    }
    
    /**
     * Set the value to undefined state
     */
    public void invalidate() {
        isDefined = false;
    }
    
    /**
     * Set the value to the defined state
     */
    public void validate() {
        isDefined = true;
    }
    
    
}

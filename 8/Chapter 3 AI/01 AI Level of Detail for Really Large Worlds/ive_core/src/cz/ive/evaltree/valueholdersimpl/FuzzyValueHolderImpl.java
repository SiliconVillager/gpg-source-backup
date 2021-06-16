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


import cz.ive.valueholders.*;


/**
 * Used to hold Fuzzy values
 * @author thorm
 */
public class FuzzyValueHolderImpl 
        extends ValueHolderImpl 
        implements FuzzyValueHolder {

    /**
     * fuzzy value.
     * range 0-32768
     */
    public short value;

    /**
     * old value
     * used to detect change of state
     */
    private short oldvalue;
    
    
    public boolean changed() {
        boolean ret = ((oldIsDefined != isDefined) || (value != oldvalue));

        oldvalue = value;
        oldIsDefined = isDefined;
        return ret;
    }
    
    
    public void changeValue(ValueHolder val) {
        value = ((FuzzyValueHolder) val).getValue();
    }

    public short getValue() {
        return value;        
    }
    
    public void setValue(short val) {
        value = val;
    }

    /**
     * Try to assign to boolean value and assert error, if it's not possible.
     * @return boolean value
     */
    public boolean getBooleanValue() {
        if (value == True) {
            return true;
        }
        if (value == False) {
            return false;
        }
        assert(false);
        return false;
    }
    
    public ValueType getType() {
        return ValueType.FUZZY;
    }
    
    
    public Object clone() {
        FuzzyValueHolderImpl ret = (FuzzyValueHolderImpl) super.clone();

        ret.value = value;
        return ret;
    }
}

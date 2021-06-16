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
 
package cz.ive.iveobject.attributes;

import cz.ive.valueholders.*;
import org.w3c.dom.NodeList;

/**
 * Atributte which holds fuzzy value within the range of 'short' type.
 *
 * @author Jirka
 */
public class AttrFuzzy extends AttributeValue implements FuzzyValueHolder {
    
    private short value;    
    
    /**
     * Creates new instance, value is set to 0, that means false.
     */
    public AttrFuzzy(){	
	value = 0;
    }
    
    /**
     * @param val holded value
     */
    public AttrFuzzy(short val){
	value = val;
    }
    
    /**
     * @param val holded value
     * @param ui user info in xml 
     */
    public AttrFuzzy(short val, NodeList ui){	
	this(val);
	userInfo = ui;
    }    
        
    public void setValue(short val){	
	value = val;
	notifyFromAttr();
    }
    
    public short getValue(){
	return value;
    }
    
    public ValueType getType(){
	
	return ValueType.FUZZY;
    } 
    
    /**
     * Get value from given object. It should be AttrFuzzy.
     *
     * @param newValue object with copied value
     */
    public void changeValue( ValueHolder newValue) {	
	value = ((AttrFuzzy) newValue).getValue();
	userInfo = ((AttrFuzzy) newValue).getUserInfo();
	notifyFromAttr();
    }
    
    public boolean equals(Object other) {
	return (other instanceof AttrFuzzy) ?
	    value == ((AttrFuzzy) other).value : false;
    }
    
    /**
     * @return string representation of attribute value
     */
    public String toString() {
        if (FuzzyValueHolder.True == value)
            return "True";
        else if (FuzzyValueHolder.False == value)
            return "False";
	return Short.toString(value);
    }
    
    /**
     * If value isn't strictly boolean (min or max from fuzzy interval),
     * method fails.
     * 
     * @return true if object value is true
     */
    public boolean getBooleanValue() {
	if (value == True) return true;
	if (value == False) return false;
	assert(false);
	return false;
    }
}

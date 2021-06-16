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


import cz.ive.IveApplication;
import java.awt.Container;
import java.util.Set;
import java.util.HashSet;
import cz.ive.valueholders.*;


/**
 * Used to hold Set values
 * @author thorm
 */
public class SetValueHolderImpl<T> extends ValueHolderImpl implements SetValueHolder<T> {
    
    /**
     *Holded value. It contains actual value all the time.
     */
    public Set<T> value;
    
    /**
     * Contains items that has been added into value member during 
     * last evaluation
     */
    public Set<T> newItems;

    /**
     * Contains items that has been removed from value member during 
     * last evaluation
     */    
    public Set<T> removedItems;
    public boolean cleanDiferentialSets = false;
    
    public SetValueHolderImpl() {
        value = new HashSet<T>();
        newItems = new HashSet<T>();
        removedItems = new HashSet<T>();
    }
    
    public boolean changed() {
        return (oldIsDefined != isDefined)
                || (newItems.isEmpty() && removedItems.isEmpty());
    }
    
    public void changeValue(ValueHolder val) {
        changeValue(((SetValueHolder) val).getValue());
    }

    public void changeValue(Set<T> newValue) {
        for (T item:value) {
            if (!newValue.contains(item)) {
                removedItems.add(item);
                value.remove(item);
            }
        }
        
        for (T item:newValue) {
            if (!value.contains(item)) {
                value.add(item);
                newItems.add(item);
            }
        }
    }
    
    public Set<T> getValue() {
        return value;
    }

    public void setValue(Set<T> val) {
        value = val;
    }
    
    public ValueType getType() {
        return ValueType.SET;
    }
    
    public SetValueHolderImpl<T> clone() {
        SetValueHolderImpl<T> ret = (SetValueHolderImpl<T>) super.clone();

        try {
            ret.value = value.getClass().newInstance();
        } catch (Exception e) {
            IveApplication.printStackTrace(e);
        }
        ret.value.addAll(value);
        return ret;
    }
    
}

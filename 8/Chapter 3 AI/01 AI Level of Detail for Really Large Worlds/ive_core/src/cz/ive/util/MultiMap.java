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
 
package cz.ive.util;

import java.util.*;

/**
 * @author Jirka
 */

/**
 * Simple extension of java.util.HashMap - special behavior for managing
 * Sets of objects in value of HashMap.
 */
public class MultiMap<A,B> extends HashMap<A,Set<B>> {
    
    /**
     * add single value to set of values - create new set
     * or put to current one.
     *
     * @return true if new value was added or false if value set
     *  just contains this value
     */
    public boolean put(A key, B value) {
	Set<B> current = get(key);
	if (current == null) {
	    HashSet<B> set = new HashSet<B>();
	    set.add(value);
	    super.put(key, set);
	    return true;
	} else {
	    return current.add(value);
	}
    }
    
    /**
     * remove value exactly from value set according to given key
     *
     * @return true if value was sucessfuly removed
     *  or false, if value set doesn't contain this value
     */
    public boolean removeValue(A key, B value) {
	Set<B> keyset = get(key);
	boolean ret = false;
	if (keyset != null) {
	    ret = keyset.remove(value);
	    if (keyset.isEmpty()) {
		remove(key);
	    }
	}
	return ret;
    }
    
    /**
     * for each key remove value from set of values if it's present
     */
    public void removeValue(B value) {
        List<A> toDelete = new LinkedList<A>();
	for (A i: keySet()) {
            Set<B> set = get(i);
            if (set.remove(value) && set.isEmpty())
                toDelete.add(i);
	}
        
        for (A key : toDelete) {
            remove(key);
        }
    }
    
    /**
     * @return all keys which value set include given value
     */
    public HashSet<A> getKeysByValue(B value) {
	HashSet<A> ret = new HashSet<A>();
	for (A i: keySet()) {
	    if (get(i).contains(value)) {
		ret.add(i);
	    }
	}
	return ret;
    }
}


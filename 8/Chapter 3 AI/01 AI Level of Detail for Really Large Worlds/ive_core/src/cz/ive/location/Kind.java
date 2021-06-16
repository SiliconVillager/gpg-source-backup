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
 
package cz.ive.location;

/**
 * Kind of a location.
 * Particular objects can move over particular kinds of locations.
 * Example of Kind can be grass, water, rail or sand. However, they are
 * represented as numbers.
 * All numbers in the array are in OR relation.
 * @author pavel
 */
public class Kind implements java.io.Serializable {
    
    /**
     * Kind values, in OR relation.
     */
    protected int[] value;
    
    /** 
     * Creates a new instance of Kind 
     * @param value Kind values
     */
    public Kind(int[] value) {
        this.value = value;
    }
    
    public int[] getValue() {
        return value;
    }
    
    /**
     * Tests if the given (searched) kind matches this kind.
     * @param search Search kind
     * @return true if at least one number equals in both values.
     */
    public boolean match(Kind search) {

        if (search == null) {
            return false;
        }
        
        for (int i=0; i<value.length; i++) {
            for (int j=0; j<search.getValue().length; j++) {
                if (value[i] == search.getValue()[j]) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
}

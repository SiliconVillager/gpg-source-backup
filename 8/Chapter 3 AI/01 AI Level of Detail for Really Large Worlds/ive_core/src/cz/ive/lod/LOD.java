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
 
package cz.ive.lod;

import java.io.Serializable;

/**
 * Simple class that embodies LOD interval.
 *
 * @author Ondra
 */
public class LOD implements Serializable {
    
    /** Minimal LOD */
    protected int min;
    /** Maximal LOD */
    protected int max;
    
    
    /** 
     * Creates a new instance of nonrestrictive LOD - min value is set to 0
     * and max to maximal integer value.
     */
    public LOD() {
        this.min = 0;
        this.max = Integer.MAX_VALUE;
    }
    
    /** 
     * Creates a new instance of LOD 
     * @param min Minimal LOD initial value
     * @param max Maximal LOD initial value
     */
    public LOD(int min, int max) {
        this.min = min;
        this.max = max;
    }
    
    /**
     * Getter for minimall LOD
     * @return value od minimal LOD
     */
    public int getMin() {
        return min;
    }
    
    /**
     * Getter for maximal LOD
     * @return value od maximal LOD
     */
    public int getMax() {
        return max;
    }
    
    /**
     * Counts intersect of two LOD intervals.
     * @return new LOD which is intersection of given two LOD intervals
     */
    static public LOD lodIntersect(LOD a, LOD b) {
        int downBound =(a.getMin() >= b.getMin()) ? a.getMin() : b.getMin();
        int upBound = (a.getMax() <= b.getMax()) ? a.getMax() : b.getMax();
        if (downBound<=upBound){
            return new LOD(downBound, upBound);
        }
	return null;
    }
}

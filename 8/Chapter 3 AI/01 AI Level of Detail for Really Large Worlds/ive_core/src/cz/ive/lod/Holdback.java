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
 * Class that embodies LOD holdback. It defines existence and view level
 * of an IveObject they are soterd in. For more details see specification.
 *
 * @author ondra
 */
public class Holdback implements Serializable {
    
    /** 
     * View level of this holdback 
     */
    protected int view;
    
    /** 
     * Existence level of this holdback.
     * If an IveObject containing this holdback has LOD
     * value of at least this value and less than view level, 
     * than the LOD manager has to ensure that the LOD will
     * be either increased at least to the view level or decreased below
     * existence level.
     */
    protected int existence;
    
    /** 
     * Creates a new instance of Holdback 
     */
    public Holdback() {
        this(0, Integer.MAX_VALUE);
    }
    
    /** 
     * Creates a new instance of Holdback
     *
     * @param existence initial level of existence value
     * @param view initial level of view value
     */
    public Holdback(int existence, int view) {
        this.existence = existence;
        this.view = view;
    }
    
    /**
     * Getter for level of view.
     *
     * @return level of view value
     */
    public int getView() {
        return view;
    }
    
    /**
     * Getter for level of existence.
     *
     * @return level of existence value
     */
    public int getExistence() {
        return existence;
    }
}

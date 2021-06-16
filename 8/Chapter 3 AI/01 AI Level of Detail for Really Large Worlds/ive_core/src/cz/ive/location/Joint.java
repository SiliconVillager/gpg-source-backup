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

import cz.ive.iveobject.*;
import java.io.Serializable;

/**
 * Joint represents walkable connection
 * between two WayPoints.
 *
 * @author ondra
 */
public class Joint implements Serializable {
    
    /** 
     * Target of connection. It can be either empty WayPoint
     * or valid WayPoint, depending on the value of valid.
     * Valid WayPoint is one in the same Area. Invalid WayPoint is
     * in an other Area. In this case this is not reference to the WayPoint
     * itself but to its copy. It has the same id but its neighbourhood
     * is empty.
     */
    public WayPoint target;
    
    /** 
     * Is the target a valid WayPoint? This means that it is 
     * situated in the same Area.
     */
    public boolean valid;
    
    /** 
     * Weight asociated with this connection. This will be used during shortest
     * path search algorithm and is implementation specific.
     */
    public float weight;
    
    /** 
     * Creates a new instance of Joint 
     * @param target initial value
     * @param weight initial value
     */
    public Joint(WayPoint target, float weight) {
        this.target = target;
        this.valid = !target.isPhantom();
        this.weight = weight;
    }
}

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

/**
 * Interface for objects that are interested in being informed
 * about some object movement between locations or for
 * objects that themselves take care about this movement.
 *
 * @author ondra
 */
public interface MoveAware {
    
    /**
     * Method to be called when some object was or is to be added
     * to the given location.
     *
     * @param object IveObject that is moving
     * @param location destination WayPoint of the movement. This parameter has
     *      to have a valid Id but does not necessarily have to be 
     *      a valid WayPoint.
     */
    public void addObject(IveObject object, WayPoint location);
    
    /**
     * Method to be called when some object was or is to be moved
     * from one location to the other.
     *
     * @param object IveObject that is moving
     * @param src source WayPoint of the movement. This parameter has
     *      to have a valid Id but does not necessarily have to be 
     *      a valid WayPoint.
     * @param dest destination WayPoint of the movement. This parameter has
     *      to have a valid Id but does not necessarily have to be 
     *      a valid WayPoint.
     */
    public void moveObject(IveObject object, WayPoint src, WayPoint dest);
    
    /**
     * Method to be called when some object was or is to be removed
     * from the given location.
     *
     * @param object IveObject that is moving
     * @param location WayPoint from where the object was or is beeing removed.
     *      This parameter has to have a valid Id but does not necessarily 
     *      have to be a valid WayPoint.
     */
    public void removeObject(IveObject object, WayPoint location);
    
    /**
     * Method to be called when some WayPoint becomes non-atomic.
     *
     * @param location WayPoint beeing expanded
     */
    public void expand(WayPoint location);
    
    /**
     * Method to be called when some WayPoint becomes atomic beeing non-atomic
     * before.
     *
     * @param location WayPoint beeing shrunk
     */
    public void shrink(WayPoint location);
}

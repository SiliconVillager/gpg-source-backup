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
 
package cz.ive.gui;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Iface for definition of the graphical viewpoint.
 *
 * @author Ondra
 */
public interface Viewpoint {

    /**
     * Tranfser world coordinates to view coordinates
     *
     * @param coords world coordinates
     */
    public Point translate(float[] coords);
    
    /**
     * Is the given rectangle visible from this viewpoint?
     *
     * @param rect Rectangle to be queried for visibility
     * @return <code>TRUE</code> iff some part of the given rectangle
     *      might be inside the view.
     */
    public boolean visible(Rectangle rect);
    
    /**
     * Is the given point visible from this viewpoint?
     *
     * @param point Point to be queried for visibility
     * @return <code>TRUE</code> iff the point might be inside the view
     */
    public boolean visible(Point point);
    
    /**
     * Retrieves zoom in this view.
     *
     * @return zoom associted with this viewpoint. If the value is positive,
     *      scale is reduced by <code>1:value returned</code>
     */
    public int getZoom();
}
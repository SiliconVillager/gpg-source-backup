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

import cz.ive.iveobject.IveObject;
import java.awt.*;

/**
 * Iface representing object specific informations
 * for proper visualisation. This might be info about
 * actual object animation frame, actual process state
 * and so on.
 *
 * All successors should have constructor with the same parameters as in
 * the AbstractGraphic info class, to be compatible with 
 * the DelegatedGraphicInfo.
 *
 * @author Ondra
 */
public interface GraphicInfo extends java.io.Serializable {
    
    /**
     * Draws object on the given canvas using a viewpoint for correct
     * position and scale retrieval. The drawing should be offseted by
     * a given vector.
     * 
     * @param canvas graphical canvas where to draw the object
     * @param view graphical viewpoint associted with the calling GUI
     * @param offset drawing offset
     */
    public void draw(Graphics2D canvas, Viewpoint view, Point offset);
    
    /**
     * Retrieves bounding box of this object in given Viewpoint.
     * The bounding box should be offseted by a give vector.
     *
     * @param view Graphical viewpoint associated with the calling GUI
     * @param offset bounding box offset.
     * @return rectangular bounding box of this object
     */
    public Rectangle getBoundingBox(Viewpoint view, Point offset);
    
    /**
     * Retrieves object at the given position. This is either associated object
     * or one of its slaves.
     *
     * @param view Graphical viewpoint associated with the calling GUI
     * @param offset bounding box offset.
     * @param p the position we are interested in.
     * @return object at the given position.
     */
    public IveObject getObjectAtPosition(Viewpoint view, Point offset, Point p);
}

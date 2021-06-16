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
import cz.ive.iveobject.attributes.AttrFuzzy;
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.iveobject.attributes.AttributeValue;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

/**
 * Implementation of the GraphicInfo specialized for viewing objects depending
 * on the value of a particular attribute.
 *
 * @author ondra
 */
public class AnimatedGraphicInfo extends AbstractGraphicInfo {
    
    /** Name of the attribute that specifies the animation frame */
    protected String attrName;
    
    /** 
     * Creates a new instance of AnimatedGraphicInfo. This method affects
     * given IveObject by setting up its GraphicInfo.
     *
     * @param object IveObject we should be associted with.
     * @param template Graphic template that will be used for rendering.
     */
    public AnimatedGraphicInfo(IveObject object, GraphicTemplate template) {
        super(object, template);
        
        if (template instanceof AnimatedTemplate) {
            attrName = ((AnimatedTemplate)template).getFrameAttribute();
        }
    }
    
    public Rectangle getBoundingBox(Viewpoint view, Point offset) {
        setUpFrame();
        
        return super.getBoundingBox(view, offset);
    }
    
    public void draw(Graphics2D canvas, Viewpoint view, Point offset) {
        setUpFrame();
        
        super.draw(canvas, view, offset);
    }
    
    /**
     * Prepares index of a frame based on the value of associated attribute.
     */
    protected void setUpFrame() {
        AttributeValue attr = object.getAttribute(attrName);
        
        if (attr == null) {
            frame = 0;
        } else if (attr instanceof AttrInteger) {
            frame = ((AttrInteger)attr).getValue();
        } else if (attr instanceof AttrFuzzy) {
            frame = ((AttrFuzzy)attr).getBooleanValue() ? 1 : 0;
        } else {
            frame = 0;
        }
    }
}

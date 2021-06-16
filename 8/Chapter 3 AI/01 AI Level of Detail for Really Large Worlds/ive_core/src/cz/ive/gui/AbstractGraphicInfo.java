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
import java.util.Set;
/**
 * Abstract implementation of the graphic info iface.
 * This class is to be a common parent of real implementations.
 *
 * @author Ondra
 */
public class AbstractGraphicInfo implements GraphicInfo {
    
    /** Object that we are associated with */
    protected IveObject object;
    
    /** Graphic template to be used when rendering */
    protected GraphicTemplate template;
    
    /** Current frame of the animation */
    protected int frame;
    
    /** Font for the viewing */
    protected static Font font = new Font("Arial", Font.BOLD, 9);
    
    /**
     * Creates a new instance of AbstractGraphicInfo. This method affects
     * given IveObject by setting up its GraphicInfo.
     *
     * @param object IveObject we should be associted with.
     * @param template Graphic template that will be used for rendering.
     */
    public AbstractGraphicInfo(IveObject object, GraphicTemplate template) {
        this.object = object;
        this.template = template;
        frame = 0;
        object.setGraphicInfo(this);
    }
    
    public void draw(Graphics2D canvas, Viewpoint view, Point offset) {
        Point p = view.translate(object.getPosition().getRealPosition());
        p.x += offset.x;
        p.y += offset.y;
        template.draw(canvas, p, frame);
        
        // Draw all slaves
        Set<IveObject> objs = object.getSlaves();
        
        if (objs != null) {
            for (IveObject obj : objs) {
                GraphicInfo gr = obj.getGraphicInfo();
                if (gr != null)
                    gr.draw(canvas, view, offset);
            }
        }
    }
    
    public Rectangle getBoundingBox(Viewpoint view, Point offset) {
        Rectangle rect = template.getBoundingBox(view.translate(
                object.getPosition().getRealPosition()),
                frame);
        rect.x += offset.x;
        rect.y += offset.y;
        return rect;
    }
    
    public IveObject getObjectAtPosition(Viewpoint view, Point offset, Point p) {
        Rectangle rect = getBoundingBox(view, offset);
        
        IveObject selected = null;
        
        // Draw all slaves
        Set<IveObject> objs = object.getSlaves();
        
        if (objs != null) {
            for (IveObject obj : objs) {
                GraphicInfo gr = obj.getGraphicInfo();
                if (gr != null) {
                    if ( null != (selected = 
                            gr.getObjectAtPosition(view, offset, p))) {
                        return selected;
                    }
                }
            }
        }
        
        if (rect.contains(p))
            return object;
        return null;
    }
}

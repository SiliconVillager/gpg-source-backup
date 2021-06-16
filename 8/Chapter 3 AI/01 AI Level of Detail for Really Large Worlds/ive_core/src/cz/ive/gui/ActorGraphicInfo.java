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
import cz.ive.iveobject.ObjectClassTree;
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.template.Utterances;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Set;

/**
 * GraphicInfo specialized for drawing the actors.
 *
 * @author ondra
 */
public class ActorGraphicInfo extends AnimatedGraphicInfo {
    
    /** Helper variables for storing last arrangement. */
    /** Number of displayed slaves */
    protected int cnt = 0;
    
    /** Offset of the first one */
    protected Point slaveOffset = new Point(0, 0);
    
    /**
     * Creates a new instance of ActorGraphicInfo. This method affects
     * given IveObject by setting up its GraphicInfo.
     *
     * @param object IveObject we should be associted with.
     * @param template Graphic template that will be used for rendering.
     */
    public ActorGraphicInfo(IveObject object, GraphicTemplate template) {
        super(object, template);
    }
    
    public void draw(Graphics2D canvas, Viewpoint view, Point offset) {
        Point p = view.translate(object.getPosition().getRealPosition());
        int y = offset.y;
        int cx = offset.x;
        Rectangle bound = getBoundingBox(view, offset);
        template.draw(canvas, new Point(p.x + offset.x, p.y + offset.y), frame);
        
        String str = object.getFlatId();
        Rectangle2D rect = font.getStringBounds(
                str, canvas.getFontRenderContext());
        
        y = bound.y;
        
        canvas.setFont(font);
        canvas.setColor(Color.ORANGE);
        canvas.fillRect(p.x + cx - (int)rect.getWidth() / 2 - 3,
                y - 2 - (int)rect.getHeight(), (int)rect.getWidth()+4,
                (int)rect.getHeight()+2);
        canvas.setColor(Color.GRAY);
        canvas.drawRect(p.x +cx - (int)rect.getWidth() / 2 - 3,
                y - 2 - (int)rect.getHeight(), (int)rect.getWidth()+4,
                (int)rect.getHeight()+2);
        canvas.setColor(Color.BLACK);
        canvas.drawString(str, p.x +cx - (int)rect.getWidth() / 2,  y - 3);
        
        y -= (int)rect.getHeight() + 10;
        
        // Draw all slaves
        Set<IveObject> objs = object.getSlaves();
        int col = 0;
        
        if (objs != null) {
            
            cnt = 0;
            for (IveObject obj : objs) {
                
                if (ObjectClassTree.instance().getObjectClass("/sensor").
                        isInside(obj)) {
                    continue;
                }
                cnt ++;
            }
            
            if (cnt != 0) {
                cx -= cnt * 8; 
            
                canvas.setColor(Color.DARK_GRAY);
                canvas.fillRoundRect(p.x + cx - 4,
                        y - 20, cnt * 16 + 8,
                        24, 6, 6);
                canvas.setColor(Color.GRAY);
                canvas.drawRoundRect(p.x + cx - 4,
                        y - 20, cnt * 16 + 8,
                        24, 6, 6);
                
                y -= p.y;
                
                slaveOffset = new Point(cx + 8, y - 8);
                
                for (IveObject obj : objs) {
                    
                    if (ObjectClassTree.instance().getObjectClass("/sensor").
                            isInside(obj)) {
                        continue;
                    }
                    
                    GraphicInfo gr = obj.getGraphicInfo();
                    
                    Point off = new Point(cx + 8 + (col) * 16,
                            y - 8);
                    col++;
                    
                    if (gr != null)
                        gr.draw(canvas, view, off);
                }
                
                cx += cnt * 8; 
                y += p.y - 26;
            }
        }
        
        AttrInteger mouth = (AttrInteger)object.getAttribute("mouth");
        if (mouth != null) {
            int mouthInt = mouth.getValue();
            String utterance = null;
            
            utterance = Utterances.instance().getUtterance(mouthInt);
            
            if (utterance == null && mouthInt != 0) {
                utterance = "<<< Unknown Utterance! >>>";
            }
            if (utterance != null) {
                rect = font.getStringBounds(
                        utterance, canvas.getFontRenderContext());
                
                canvas.setColor(Color.WHITE);
                canvas.fillRoundRect(p.x +cx - (int)rect.getWidth() / 2 - 6,
                        y - 4 - (int)rect.getHeight(), (int)rect.getWidth()+10,
                        (int)rect.getHeight()+6, 5, 5);
                canvas.setColor(Color.GRAY);
                canvas.drawRoundRect(p.x + cx - (int)rect.getWidth() / 2 - 6,
                        y - 4 - (int)rect.getHeight(), (int)rect.getWidth()+10,
                        (int)rect.getHeight()+6, 5, 5);
                canvas.setColor(Color.BLACK);
                canvas.drawString(utterance, p.x + cx - (int)rect.getWidth() / 2,
                        y - 3);
                
                y -= (int)rect.getHeight() + 10;
            }
        }
    }
    
    public IveObject getObjectAtPosition(Viewpoint view, Point offset, Point p) {
        IveObject selected = null;
        
        // Draw all slaves
        Set<IveObject> objs = object.getSlaves();
        
        if (objs != null) {
            int cx = 0;
            for (IveObject obj : objs) {
                
                if (ObjectClassTree.instance().getObjectClass("/sensor").
                        isInside(obj)) {
                    continue;
                }
                
                Point off = new Point(cx*16 + slaveOffset.x, slaveOffset.y);
                GraphicInfo gr = obj.getGraphicInfo();
                if (gr != null) {
                    if ( null != (selected = 
                            gr.getObjectAtPosition(view, off, p))) {
                        return selected;
                    }
                }
                cx++;
            }
        }
        
        Rectangle rect = getBoundingBox(view, offset);
        
        if (rect.contains(p))
            return object;
        return null;
    }
}

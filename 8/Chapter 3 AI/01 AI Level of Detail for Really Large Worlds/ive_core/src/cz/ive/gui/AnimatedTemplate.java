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

import cz.ive.IveApplication;
import cz.ive.logs.Log;
import cz.ive.xmlload.XMLDOMLoader;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.w3c.dom.*;
import java.util.List;

/**
 * GraphicTemplate implementation for multistate images.
 *
 * @author ondra
 */
public class AnimatedTemplate implements GraphicTemplate {
    
    /** Images that are represented by this template */
    transient protected Image[] images;
    
    /**
     * Paths to the images to be able to load graphic again after
     * deserialization
     */
    protected String[] pathToImages;
    
    /**
     * Relative center positions of the images. This is relative displacement
     * of the object when drawn.
     */
    protected Point[] centers;
    
    /**
     * Name of the attribute that specifies an animation frame
     */
    protected String attrName;
    
    /**
     * Creates a new instance of AnimatedTemplate
     */
    public AnimatedTemplate() {
    }
    
    public void draw(Graphics2D canvas, Point position, int frame) {
        if (frame >= centers.length)
            frame = centers.length-1;
        else if (frame < 0)
            frame = 0;
        
        if (images[frame] != null) {
            canvas.drawImage(
                    images[frame], position.x - centers[frame].x,
                    position.y - centers[frame].y, null);
        }
    }
    
    public Rectangle getBoundingBox(Point position, int frame) {
        if (frame >= centers.length)
            frame = centers.length-1;
        else if (frame < 0)
            frame = 0;
        
        if (images[frame] != null) {
            return new Rectangle(
                    position.x - centers[frame].x, position.y - centers[frame].y,
                    images[frame].getWidth(null), images[frame].getHeight(null));
        }
        return new Rectangle(0, 0, 0, 0);
    }
    
    public String load(org.w3c.dom.Element e) {
        List<Element> imgTags = XMLDOMLoader.getChildElements(e, "image");
        
        pathToImages = new String[imgTags.size()];
        centers = new Point[imgTags.size()];
        images = new Image[imgTags.size()];
        
        int i = 0;
        for (Element imgEl : imgTags) {
            pathToImages[i] = imgEl.getAttribute("src");
            try {
                InputStream is = 
                        IveApplication.instance().getIveResourceAsStream(
                        pathToImages[i]);
                images[i] = ImageIO.read(is);
            } catch (Exception ex) {
                Log.severe("Could not load image \""+pathToImages[i]+"\" : "+
                        ex.getMessage());
            }
            centers[i] = new Point(Integer.parseInt(imgEl.getAttribute("center_x")),
                    Integer.parseInt(imgEl.getAttribute("center_y")));
            i++;
        }
        attrName = e.getAttribute("frameAttribute");
        return e.getAttribute("name");
    }
    
    /**
     * Getter fro the frame attribute.
     *
     * @return name of the attribute that specifies the animation frame.
     */
    public String getFrameAttribute() {
        return attrName;
    }
    
    /**
     * Writes out the object into the object stream.
     *
     * @param s stream to be filled with description of this object
     */
    private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException {
        
        // Write out any hidden serialization magic
        s.defaultWriteObject();
    }
    
    /**
     * Loads contents of this object from the object stream.
     * We must also recreate the image this template represents.
     *
     * @param s stream to be used to load the description of this object.
     */
    private void readObject(java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {
        
        // Read in any hidden serialization magic
        s.defaultReadObject();
        
        images = new Image[centers.length];
        
        int i=0;
        for (String path : pathToImages) {
            try {
                InputStream is = 
                        IveApplication.instance().getIveResourceAsStream(
                        pathToImages[i]);
                images[i] = ImageIO.read(is);
            } catch (Exception ex) {
                Log.severe("Could not load image \""+path+"\" : "+
                        ex.getMessage());
            } finally {
                i++;
            }
        }
    }
}

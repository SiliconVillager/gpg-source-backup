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
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import javax.imageio.ImageIO;


/**
 * GraphicsTemplate implementation for still images
 *
 * @author Ondra
 */
public class StaticTemplate implements GraphicTemplate {
    
    /** Image that is represented by this template */
    transient protected Image image;
    
    /** Path to the image to be able to load graphic again
     * after deserialization */
    protected String pathToImage;
    
    /**
     * Relative center position of the image. This is relative displacement
     * of the object when drawn.
     */
    protected Point center;
    
    /**
     * Creates a new instance of StaticTemplate.
     *
     * @param pathToImage path to the image to be represented by this template
     * @param center Center of the image
     */
    public StaticTemplate(String pathToImage, Point center) {
        this.pathToImage = pathToImage;
        try {
            InputStream is = IveApplication.instance().getIveResourceAsStream(
                    pathToImage);
            this.image = ImageIO.read(is);
        } catch (Exception ex) {
            Log.severe("Could not load image \""+pathToImage+"\" : "+
                    ex.getMessage());
        }
        
        this.center = (Point)center.clone();
    }
    
    /**
     * Creates a new instance of StaticTemplate.
     */
    public StaticTemplate(){}
    
    public void draw(Graphics2D canvas, Point position, int frame) {
        if (image != null) {
            canvas.drawImage(
                    image, position.x - center.x,
                    position.y - center.y, null);
        }
    }
    
    public Rectangle getBoundingBox(Point position, int frame) {
        if (image != null) {
            return new Rectangle(
                    position.x - center.x, position.y - center.y,
                    image.getWidth(null), image.getHeight(null));
        }
        return new Rectangle(0, 0, 0, 0);
    }
    
    public String load(org.w3c.dom.Element e) {
        pathToImage = e.getAttribute("src");
        try {
            InputStream is = IveApplication.instance().getIveResourceAsStream(
                    pathToImage);
            this.image = ImageIO.read(is);
        } catch (Exception ex) {
            Log.severe("Could not load image \""+pathToImage+"\" : "+
                    ex.getMessage());
        }
        center = new Point(Integer.parseInt(e.getAttribute("center_x")),
                Integer.parseInt(e.getAttribute("center_y")));
        return e.getAttribute("name");
    }
    
    
    /**
     * Writes out the object into the object stream.
     *
     * @param s stream to be filled with description of this object
     */
    private void writeObject(java.io.ObjectOutputStream s) throws 
            java.io.IOException {
        
        // Write out any hidden serialization magic
        s.defaultWriteObject();
    }
    
    /**
     * Loads contents of this object from the object stream.
     * We must also recreate the image this template represents.
     *
     * @param s stream to be used to load the description of this object.
     */
    private void readObject(java.io.ObjectInputStream s) throws 
            java.io.IOException, ClassNotFoundException {
        
        // Read in any hidden serialization magic
        s.defaultReadObject();
        
        try {
            InputStream is = IveApplication.instance().getIveResourceAsStream(
                    pathToImage);
            this.image = ImageIO.read(is);
        } catch (Exception ex) {
            Log.severe("Could not load image \""+pathToImage+"\" : "+
                    ex.getMessage());
        }
    }
}

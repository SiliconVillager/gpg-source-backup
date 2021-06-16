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
import java.awt.*;
import cz.ive.template.*;
import cz.ive.iveobject.*;
import cz.ive.logs.Log;
import java.lang.reflect.Constructor;

/**
 * Implementation of GraphicInfo used only for templates IveObjects.
 * This is only a storage for reference to real GraphicInfo class and to
 * a GraphicTemplate.
 *
 * @author ondra
 */
public class DelegatedGraphicInfo implements GraphicInfo, Cloneable {
    
    /**
     * Real GraphicInfo that was (or was not, if null) created from
     * references stored in this class
     */
    protected GraphicInfo grInfo;
    
    /** Object that we are associated with */
    protected IveObject object;
    
    /** Name of the class of the represented GraphicInfo */
    protected String graphicInfoClassName;
    
    /** Id of the represented GraphicTemplate in the TemplateMap */
    protected String templateId;
    
    /**
     * Creates a new instance of DelegatedGraphicInfo. This method affects
     * given IveObject by setting up its GraphicInfo.
     * The first time this instance is used it will transparently create the
     * real Graphic info to do the job.
     *
     * @param object IveObject we should be associted with.
     * @param graphicInfoClassName class of GraphicInfo to be represented
     * @param templateId id of the GraphicTemplate to be represented
     */
    public DelegatedGraphicInfo(IveObject object, String graphicInfoClassName,
            String templateId) {
        this.object = object;
        this.graphicInfoClassName = graphicInfoClassName;
        this.templateId = templateId;
        object.setGraphicInfo(this);
    }
    
    public void draw(Graphics2D canvas, Viewpoint view, Point offset) {
        GraphicInfo gi = instantiate();
        if (gi != null)
            gi.draw(canvas, view, offset);
    }
    
    public Rectangle getBoundingBox(Viewpoint view, Point offset) {
        GraphicInfo gi = instantiate();
        if (gi != null)
            return gi.getBoundingBox(view, offset);
        return new Rectangle(0,0);
    }
    
    public IveObject getObjectAtPosition(Viewpoint view, Point offset, Point p) {
        GraphicInfo gi = instantiate();
        if (gi != null)
            return gi.getObjectAtPosition(view, offset, p);
        return null;
    }
    
    /**
     * Create the real GraphicInfo.
     * To do the job. This call also affects IveObject for which this instance
     * was created by setting its GraphicInfo to the newly created one.
     * This method creates new instance of Graphic info only for the first time.
     * It returns the same instance, if called again (which should not occur).
     *
     * @return instance of GraphicInfo that was represented by this
     *      storage only object.
     */
    public GraphicInfo instantiate() {
        
        if (grInfo != null)
            return grInfo;
        
        try {
            GraphicTemplate template = (GraphicTemplate)TemplateMap.instance().
                    getTemplate(templateId);
            
            if (template == null) {
                Log.severe("Could not find \""+templateId+"\" GraphicTemplate");
                object.setGraphicInfo(null);
                return null;
            }
            
            Class<GraphicInfo> cl = 
                    (Class<GraphicInfo>)IveApplication.instance().loadIveClass(
                    graphicInfoClassName);
            
            Constructor<GraphicInfo> constr = cl.getConstructor(IveObject.class,
                    GraphicTemplate.class);
            
            grInfo = constr.newInstance(object, template);
            
            return grInfo;
            
        } catch (Exception ex) {
            Log.severe("Error instantiating GraphicInfo: " +
                    ex.getMessage());
            object.setGraphicInfo(null);
        }
        return null;
    }
    
    /**
     * Clones this instance and assocites it with given IveObject.
     * Given IveObject IS NOT altered in any way.
     * Setting of GraphicInfo for given IveObject is expected in
     * clone/instantiate/whatevercalled method in IveObject class.
     *
     * @param obj copy of IveObject to be associated with
     */
    public DelegatedGraphicInfo clone(IveObject obj) {
        DelegatedGraphicInfo info = clone();
        info.object = obj;
        info.grInfo = null;
        
        return info;
    }
    
    protected DelegatedGraphicInfo clone() {
        try {
            return (DelegatedGraphicInfo)super.clone();
        } catch (Exception ex) {
            // Will not occur
            return null;
        }
    }
}

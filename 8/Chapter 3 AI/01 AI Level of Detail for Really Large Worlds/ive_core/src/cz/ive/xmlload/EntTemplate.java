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
 
package cz.ive.xmlload;


import cz.ive.IveApplication;
import cz.ive.genius.Genius;
import cz.ive.iveobject.*;
import cz.ive.process.*;
import java.lang.reflect.InvocationTargetException;
import org.w3c.dom.Element;

import cz.ive.iveobject.IveObject;
import cz.ive.logs.Log;

import static cz.ive.xmlload.XMLDOMLoader.*;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;


/**
 * Template used to create new Ents
 * Ent is pair of IveObject and Genius
 * @author thorm
 */
public class EntTemplate extends ObjectTemplate {
    /**
     * Name of java class that is used as genius for this Ent
     */
    String geniusClassName;
    
    /**
     * List of goals that forms ents meaning of life
     */
    List<String> topLevelGoals;
    
    
    // documented in Template
    public String load(Element e) {
        String name = super.load(e);
        
        if (name == null) {
            return null;
        }
        
        topLevelGoals = new LinkedList<String>();
        
        Element geniusElement = getOneChildElement(e, "Genius");

        geniusClassName = geniusElement.getAttribute("className");
        for (Element tlgElement:getChildElements(geniusElement, "TopLevelGoal")) {
            topLevelGoals.add(tlgElement.getAttribute("goalId"));
        }
        
        return name;
    }
    
    /**
     * Create new instance of the Ent described by this template
     * It instantiates new actor genius, fills it with top level goals and 
     * binds it to the Ent.
     * The new genius identifier is objectId_genius.
     *
     * @param objectId structured identifier of newly created Ent
     * @return new IveObject or null if fails
     */
    
    public IveObject instantiate(String objectId) {
        Ent ret = (Ent) super.instantiate(objectId);
        
        try {
            Class geniusClass = IveApplication.instance().loadIveClass(
                    geniusClassName);
            Constructor geniusCtor = geniusClass.getConstructor(String.class);
            
            Genius g = (Genius) geniusCtor.newInstance(ret.getId() + "_genius");

            ret.setGenius(g);
            
            SourceImpl src = new SourceImpl();

            src.setObject(ret);
            g.addSource(src);
            for (String tlgStr:topLevelGoals) {
                g.addTopLevelGoal(new TopLevelGoal(tlgStr));
            }
            
            return ret;
        } catch (ClassNotFoundException exc) {
            Log.severe(
                    "Genius class " + geniusClassName + " for ent "
                    + ret.getId() + " not found");
            return null;
        } catch (NoSuchMethodException exc) {
            Log.severe(
                    "Genius class " + geniusClassName + " for ent "
                    + ret.getId()
                    + " doesn't have constructor with proper signature");
            return null;
        } catch (InstantiationException exc) {
            Log.severe("Failed instantiation of genius class for " + ret.getId());
        } catch (IllegalAccessException exc) {
            Log.severe("Failed instantiation of genius class for " + ret.getId());
        } catch (InvocationTargetException exc) {
            Log.severe("Failed instantiation of genius class for " + ret.getId());
        }
        return null;
    }
}

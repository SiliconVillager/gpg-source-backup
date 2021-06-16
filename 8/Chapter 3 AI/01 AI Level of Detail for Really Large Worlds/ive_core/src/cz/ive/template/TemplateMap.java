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
 
package cz.ive.template;

import java.io.Serializable;
import java.util.*;

/**
 * TemplateMap is a serializable singleton used for storing and mapping various
 * templates by their string names.
 * Reason for just another singleton map is in the option to exclude it from
 * saving and loading process since its contents are only loaded during BigLoad
 * and then remains the same. But This is now only an option, TemplateMap
 * is implemented to undergo normal serialization process now.
 *
 * @author ondra
 */
public class TemplateMap implements Serializable {

    /** Reference to current singleton instance */
    private static TemplateMap templateMap;
    
    /** Map of templates itself */
    private HashMap<String, Template> templates;
    
    
    
    /** Creates a new instance of TemplateMap */
    protected TemplateMap() {
        templates = new HashMap<String, Template>();
    }
    
    /** 
     * Returns the current instance of the TemplateMap singleton.
     * This singleton can change during load process.
     *
     * @return current instance of TemplateMap singleton
     */
    static public synchronized TemplateMap instance() {
        if (templateMap == null) {
            templateMap = new TemplateMap();
        }        
        return templateMap;
    }
    
    /**
     * Changes reference to current instance of TemplateMap singleton
     * Used with serialization - after loading.
     *
     * @param map reference to new TemplateMap singleton
     */
    static public void setInstance(TemplateMap map) {
	templateMap = map;
    }
    
    /** 
     * Empty whole the TemplateMap before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        templateMap = new TemplateMap();
    }
    
    /**
     * Register new Template under given Id. Id must be unique.
     *
     * @param template Template to be registered
     * @param id to be associated with given template
     * @return <code>true</code> on success, <code>false</code> otherwise 
     *      (that means the given id was not unique).
     */
    public boolean register(String id, Template template) {
	if (templates.containsKey(id)) {
	    return false;
	}
	templates.put(id, template);
	return true;
    }
    
    /**
     * Unregister Template from the map.
     *
     * @param id Id of the Template to be unregistered.
     */
    public void unregister(String id) {
	templates.remove(id);
    }
    
    
    /**
     * Unregister all templates.
     * This method could be usefull if a different loading schema for templates
     * was used.
     *
     * @param id Id of the Template to be unregistered.
     */
    public void unregisterAll(String id) {
	templates.clear();
    }
    
    /**
     * Finds mapping of given id.
     *
     * @return Template associated with given id or <code>null</code> if no
     *      such template exists
     */
    public Template getTemplate(String id) {
	return templates.get(id);
    }
}

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


import java.io.Serializable;
import cz.ive.iveobject.IveObject;
import cz.ive.logs.Log;
import cz.ive.template.TemplateMap;
import org.w3c.dom.Element;


/**
 * Contains information needed to instantiate IveObject.
 * This is at least name of the template used to create the instance and 
 * new instance name
 * @author thorm
 */
public class ObjectInstanceInfo implements Serializable {

    /**
     * name of the template that is used to create new IveObject instance
     */
    public String template;

    /**
     * name of the object ( last part )
     */
    public String name;
    
    /**
     * Create new instance of IveObject using informations from this class<br>
     * Typical usage is to read public member name, add it to the parent 
     * identifier, in some cases add some next information and result string 
     * pass to this method.
     * 
     * @param objectId fully qualified name of the new IveObject.
     * @return new IveObject instance                
     */
    public IveObject instantiate(String objectId) {
        ObjectTemplate objTemp = (ObjectTemplate) TemplateMap.instance().getTemplate(
                template);

        if (objTemp != null) {
            return  objTemp.instantiate(objectId);
        }
        Log.severe("Instantiation failed : template " + template + " not found");
        return null;
    }
    
    /**
     * Load the information from the given element
     * @param e <CODE>Object</CODE> xml DOM element
     */
    public String load(Element e) {
        name = e.getAttribute("name");
        template = e.getAttribute("template");
        return ((name != null) && (template != null)) ? name : null;
    }
    
}


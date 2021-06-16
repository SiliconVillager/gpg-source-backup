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
 
package cz.ive.iveobject;

import java.util.*;
import java.io.Serializable;

/**
 * Singlton containing Tree and Map of ObjectClasses for easy
 * saving and loading.
 *
 * Object classes are identified by their String id which is set of tokens 
 * separated by ObjectClass.SEP ("/") and starting by this separator.
 * One exception is a root ObjectClass which is represented by empty id and
 * which (in sense of inclusion) contains all other object classes.
 *
 * @author ondra
 */
public class ObjectClassTree implements Serializable {
    
    /** Static reference to current singleton */
    private static ObjectClassTree objectClassTree;
    
    /** Map of object classes */
    protected Map<String, ObjectClass> classes =
            new HashMap<String, ObjectClass>();
    
    /** Creates a new instance of ObjectClassTree */
    public ObjectClassTree() {
        getObjectClass("/");
    }

    /**
     * Retrieves root of ObjectClass hierarchy which (in sense of inclusion) 
     * contains all object classes.
     *
     * @return ObjectClass representing root of ObjectClass hierarchy.
     */
    static public ObjectClass getRoot() {
        return instance().getObjectClass("/");
    }
    
    /** 
     * Returns current instance of the ObjectClassTree singleton 
     */
    static public synchronized ObjectClassTree instance() {
        if (objectClassTree == null) {
            objectClassTree = new ObjectClassTree();
        }        
        return objectClassTree;
    }
    
    /** 
     * Empty whole the ObjectClassTree before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        objectClassTree = new ObjectClassTree();
    }
    
    /**
     * Changes internal refence to ObjectClassTree singleton.
     * Used with serialization - after loading.
     *
     * @param tree reference to new instance of ObjectClassTree singleton
     */
    static public void setInstance(ObjectClassTree tree) {
	objectClassTree = tree;
    }
    
    /**
     * Retrives ObjectClass representing given hierarchical object class id
     *
     * @param cid hierarchical String id
     * @return ObjectClass representing given class id
     */
    public ObjectClass getObjectClass(String cid) {
        ObjectClass oc = classes.get(cid);
        
        if (oc != null)
            return oc;
        
        oc = new ObjectClass(cid);
        classes.put(cid, oc);
        
        if( cid.equals("/") || cid.indexOf(ObjectClass.SEP) == -1)
            return oc;
        
        String scid = cid.substring(0, cid.lastIndexOf(ObjectClass.SEP));
        ObjectClass soc;
        if (scid.equals("")) {
            soc = getRoot();
        } else {
            soc = getObjectClass(scid);
        }
        oc.superclass = soc;
        soc.addSubclass(oc);
        
        return oc;
    }
}

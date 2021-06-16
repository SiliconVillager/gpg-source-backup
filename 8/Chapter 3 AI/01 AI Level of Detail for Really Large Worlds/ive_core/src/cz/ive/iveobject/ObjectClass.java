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
 * Iface representing object-space partitioning into some classes.
 * This is rather symbolic approach, but this is not seen by a genius
 * itself, so it should not be a big problem. Object classes will be used
 * for example during object placement or for the  graphical log filtering.
 *
 * Object classes are identified by their String id which is set of tokens 
 * separated by ObjectClass.SEP ("/") and starting by this separator.
 * One exception is a root ObjectClass which is represented by empty id and
 * which (in sense of inclusion) contains all other object classes.
 *
 * @author Ondra
 */
public class ObjectClass implements Serializable {
    
    /** Separator to be used in hierarchical IveObject class id */
    public static char SEP = '/';
    
    /** Our subclasses*/
    protected List<ObjectClass> subclasses = new LinkedList<ObjectClass>();
    
    /** Our superclass */
    protected ObjectClass superclass;
    
    /** Hierarchical Id of this IveObject class */
    protected String classId;
    
    /**
     * Creation of new instance of ObjectClass.
     * This class will not be constructed from the oustside.
     *
     * @param classId hierarchical String id of this new class
     */
    protected ObjectClass(String classId) {
        this.classId = classId;
    }
    
    /**
     * Is a given IveObject inside this class?
     *
     * @param object IveObject to be querried
     * @return <code>TRUE</code> iff the given object is inside this
     *      IveObject class
     */
    public boolean isInside(IveObject object) {
        return isInside(object.getObjectClass());
    }
    
    /**
     * Is a given IveObject class subclass of this class?
     *
     * @param oc IveObject class to be querried
     * @return <code>TRUE</code> iff the given IveObject class is whole inside
     *      this one.
     */
    public boolean isInside(ObjectClass oc) {
        String cid = oc.classId;
        if (cid.indexOf(classId) == 0) {
            if (classId.length() < cid.length()) {
                return cid.charAt(classId.length()) == SEP ||
                        cid.charAt(classId.length()-1) == SEP;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Retrieves superclass of this IveObject class
     *
     * @return superclass of this class. This is <code>null</code> iff
     *      this class is root in the IveObject class hierarchy
     */
    public ObjectClass getSuperclass() {
        return superclass;
    }
    
    /**
     * Retrieves subclasses of this IveObject class.
     *
     * @return list of subclasses of this class.
     */
    public List<ObjectClass> getSubclasses() {
        return subclasses;
    }
    
    /**
     * Addition of single subclass.
     *
     * @param oc ObjectClass to be added to the subclass list of this
     *      ObjectClass.
     */
    protected void addSubclass(ObjectClass oc) {
        subclasses.add(oc);
    }
    
    public String toString() {
        return classId;
    }
}
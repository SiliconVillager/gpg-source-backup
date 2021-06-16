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
 
package cz.ive.location;

import cz.ive.iveobject.ObjectMap;
import cz.ive.manager.ManagerOfSenses;

/** World root holder implementation
 *
 * @author honza
 */
public class IveMapImpl implements IveMap, java.io.Serializable {
    
    private Area worldRoot;
    static IveMap instance = null;
    
    /** Creates a new instance of Map */
    public IveMapImpl() {
    }
    
    public static synchronized IveMap instance() {
        if (instance == null) {
            instance = new IveMapImpl();  
        }
        return instance;
    }
    
    public static void setInstance(IveMap map) {
        instance = map;
    }
    
    /** 
     * Empty whole the IveMap before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        instance = new IveMapImpl();  
    }
    
    public void setRoot(Area root) {
        if (worldRoot != null) {
            ManagerOfSenses.instance().removeObject(worldRoot, worldRoot);
        }
        worldRoot = root;
        if (root != null) {
            ObjectMap.instance().register(root);
            ManagerOfSenses.instance().addObject(root, root);
        }
    }
    
    public Area getRoot() {
        return worldRoot;
    }
}

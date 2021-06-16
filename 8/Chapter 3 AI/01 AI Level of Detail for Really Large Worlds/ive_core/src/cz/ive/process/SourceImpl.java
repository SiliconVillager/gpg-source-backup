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
 
package cz.ive.process;

import cz.ive.messaging.*;
import cz.ive.iveobject.*;
import java.util.*;

/**
 * Straight forward implementation of the Source interface.
 *
 * @author Zdenek
 */
public class SourceImpl implements Source, Cloneable, java.io.Serializable {
    
    /** Static source instance counter for the debuging puposes. */
    protected static int cnt = 1;
    
    /** 
     * Unique index of this source instance. This is to be used when debuging
     * the source sharing.
     */
    public int idx = cnt++;
    
    /**
     * Pointer to IveObject stored in Source
     */
    private IveObject storedObject;
    
    /**
     * Set of listeners who waits on change of Source
     */
    private java.util.HashSet<Listener> listeners; 
    
    /** 
     * Creates a new instance of SourceImpl 
     */
    public SourceImpl() {
        listeners = new HashSet<Listener>();
        storedObject = null;
    }
   
    /**
     * Creates a new instance of SourceImpl and stores object
     */
    public SourceImpl(IveObject object) {
        listeners = new HashSet<Listener>();
        this.setObject(object);
    }
   
    /**
     * Clones Source
     * @return cloned Source
     */
    public Source clone() {
        try {
            return (Source)super.clone();
        } catch (CloneNotSupportedException e) { 
	    throw new InternalError();
	}
    }
    
    /**
     * @return returs IveObject stored inside
     */
    public IveObject getObject() {
        return storedObject;     
    }
    
    /**
     * Stores IveObject into Source and notify all listeners
     * @param object object which will be registeres as a source
     */
    public void setObject(IveObject object) {
        storedObject = object;
        for (java.util.Iterator<Listener> i = listeners.iterator(); 
                i.hasNext(); ) {
            i.next().changed(this);
        }

        
    }
    
    /**
     * Registers listener who will be informed when Source changed
     * @param listener registered listener 
     */
    public void registerListener(Listener listener) {
        listeners.add(listener);
    }
    
    /**
     * Unregisters listener from this Source
     * @param listener unregistered listener
     */
    public void unregisterListener(Listener listener) {
        listeners.remove(listener);                
    }
    
}

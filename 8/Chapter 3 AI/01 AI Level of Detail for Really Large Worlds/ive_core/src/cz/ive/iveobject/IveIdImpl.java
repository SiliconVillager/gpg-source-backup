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

import cz.ive.messaging.*;

/**
 * Simple implementation of IveId interface.
 *
 * @author ondra
 */
public class IveIdImpl extends SyncHook implements IveId, java.io.Serializable {
    
    /** unique name of IveObject */
    protected String id;
    
    /**
     * Creates a new instance of IveIdImpl
     * @param id unique id to be associated with this instance
     */
    public IveIdImpl(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * Writes id to the object stream.
     * To prevent dark serialization magic, we must load/save id before
     * anything else when Serializing and Deserializing respectively.
     *
     * @param s object stream where to push the id
     */
    public void writeId(java.io.ObjectOutputStream s) throws
            java.io.IOException {
        s.writeObject(id);
    }
    
    /**
     * Loads id from the object stream.
     * To prevent dark serialization magic, we must load/save id before
     * anything else when Serializing and Deserializing respectively.
     *
     * @param s object stream from which to load the id
     */
    public void readId(java.io.ObjectInputStream s) throws
            java.io.IOException, ClassNotFoundException {
        id = (String)s.readObject();
    }
    
    public String getFlatId() {
        int lSep = id.lastIndexOf(SEP);
        
        if (lSep == -1)
            lSep = 0;
        else
            lSep ++;
        
        return id.substring(lSep);
    }
    
    public IveObject getLeastCommonParent(IveId kin) {
        return ObjectMap.instance().getObject(
                getLeastCommonParentId(kin.getId()));
    }
    
    public String getLeastCommonParentId(String kinId) {
        
        String[] splittedkinId = kinId.split("\\"+IveId.SEP);
        String[] splittedId = id.split("\\"+IveId.SEP);
        
        
        int i=0;
        int minlength = Math.min(splittedId.length,splittedkinId.length);
        while(i<minlength && splittedId[i].equals(splittedkinId[i])){
            i++;
        }
                
        if (i == 0)
            return null;
               
        String ret = splittedId[0];
        for(int j=1 ; j<i;j++){
            ret = ret+IveId.SEP+splittedId[j];
        }
        return ret;
    }
    
    public IveObject getParent() {
        return ObjectMap.instance().getObject(getParentId());
    }
    
    public String getParentId() {
        int lSep = id.lastIndexOf(SEP);
        
        if (lSep == -1)
            return null;
        
        return id.substring(0, lSep);
    }
    
    protected String getParentId(String id) {
        int lSep = id.lastIndexOf(SEP);
        
        if (lSep == -1)
            return null;
        
        return id.substring(0, lSep);
    }
    
    public IveObject getLeastActiveParent() {
        String parId = getParentId();
        
        while (parId != null) {
            IveObject parent = ObjectMap.instance().getObject(parId);
            if (parent!=null)
                return parent;
            parId = getParentId(parId);
        }
        return null;
    }
    
    public IveObject getLeastActiveParentWithLODLEThan(int i){
        IveObject o = getLeastActiveParent();
        if (o==null ) return null;
        while (o!=null &&o.getLod()>i){
            o=o.getParent();
        }
        return o;
    }
    
    public boolean isSibling(String siblingId) {
        int sSep = siblingId.lastIndexOf(SEP);
        int oSep = id.lastIndexOf(SEP);
        
        if (sSep != oSep || sSep == -1)
            return false;
        
        return id.regionMatches(0, siblingId, 0, oSep);
    }
    
    public boolean isSibling(IveId sibling) {
        return isSibling(sibling.getId());
    }
    
    public boolean isParent(IveId parent) {
        if (parent == null) {
            return true;
        }
        
        String parentId = parent.getId();

        if (id.equals(parentId)) {
            return true;
        }
        
        if (id.startsWith(parentId) &&
             (parentId.length() < id.length()) &&
             (id.charAt(parentId.length()) == IveId.SEP)) {
            return true;
        }
        
        return false;
    }
    
    public String getChildPreceedingId(String descendantId) {
        String str = id + SEP;
        
        if (!str.regionMatches(0, descendantId, 0, str.length()))
            return null;
        
        int sep = descendantId.indexOf(SEP,  str.length());
        
        if (sep == -1)
            return descendantId;
        
        return descendantId.substring(0, sep);
    }
    
    public IveObject getChildPreceeding(String descendantId) {
        String str = getChildPreceedingId(descendantId);
        return str == null ? null : ObjectMap.instance().getObject(str);
    }
    
    public int hashCode() {
        return id.hashCode();
    }
    
    public boolean equals(Object other) {
        return (other instanceof IveId) ?
            id.equals(((IveId) other).getId()) : false;
    }
}

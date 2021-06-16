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


import java.util.HashMap;


/**
 * Just maps the diferent Strings used to identify waypoint Kind in the XML 
 * to the distinct integers.
 *
 * This is needed only during the XMLLoad. After it only numbers are used
 * @author thorm
 */
public class KindMap implements java.io.Serializable {
    
    /** Creates a new instance of KindMap */
    protected KindMap() {
        kinds = new HashMap<String, Integer>();
    }
    
    /**
     * The singleton instance
     */
    private static KindMap kindMap;
    
    /**
     * Counter of the different kind types
     */
    private int count = 1;
    
    /**
     * Used to map the strings to the integers
     */
    private HashMap<String, Integer> kinds;
    
    /**
     * Returns the instance of the KindMap singleton 
     */
    static public KindMap instance() {
        if (kindMap == null) {
            kindMap = new KindMap();
        }        
        return kindMap;
    }
    
    /**
     * Changes internal refence to KindMap singleton.
     * Used with serialization - after loading.
     * This isn't necessary while it is used only by xmlload
     *
     * @param map reference to new map
     */
    static public void setInstance(KindMap map) {
        kindMap = map;
    }
    
    /**
     * Get the integer associated with goven String s. If the s is not in the
     * map yet it is inserted and unique number is assigned to it.
     *
     * @param s String representation of Kind type
     * @return integer representation of Kind type
     */
    public Integer getKind(String s) {
        Integer ret = kinds.get(s);

        if (ret == null) {
            ret = new Integer(count++);
            kinds.put(s, ret);            
        }
        return ret;
    }
}

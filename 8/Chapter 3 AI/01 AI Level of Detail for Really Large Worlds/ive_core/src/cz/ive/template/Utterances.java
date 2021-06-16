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
 * Database of utterances.
 *
 * @author ondra
 */
public class Utterances implements Serializable {

    /** Reference to current singleton instance */
    private static Utterances instance;
    
    /** Map of templates itself */
    private HashMap<Integer, String> utterances;
    
    /** Creates a new instance of Utterances */
    protected Utterances() {
        utterances = new HashMap<Integer, String>();
    }
    
    /** 
     * Returns the current instance of the Utterances singleton.
     * This singleton can change during load process.
     *
     * @return current instance of Utterances singleton
     */
    static public synchronized Utterances instance() {
        if (instance == null) {
            instance = new Utterances();
        }        
        return instance;
    }
    
    /**
     * Changes reference to current instance of Utterances singleton
     * Used with serialization - after loading.
     *
     * @param instance reference to new Utterances singleton
     */
    static public void setInstance(Utterances instance) {
	Utterances.instance = instance;
    }
    
    /** 
     * Empty whole the Utterances before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        instance = new Utterances();
    }
    
    /**
     * Finds an utterance from the given index.
     *
     * @param index Index of the utterance
     * @return String utterance associated with the given index.
     */
    public String getUtterance(Integer index) {
        return utterances.get(index);
    }
    
    /**
     * Adds new utterance with the given index.
     *
     * @param index Index of the utterance
     * @param utterance String utterance     
     */
    public void addUtterance(Integer index, String utterance) {
        utterances.put(index, utterance);
    }
}

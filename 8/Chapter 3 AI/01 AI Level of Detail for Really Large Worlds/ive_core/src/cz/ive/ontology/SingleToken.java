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
 
package cz.ive.ontology;

import cz.ive.exception.*;

/**
 * Simple implementation of OntologyToken containing data in only one ontology
 * specified during construction.
 *
 * @author ondra
 */
public class SingleToken implements OntologyToken, java.io.Serializable {
    
    /** Ontology identifier of contained data */
    protected String ontology;
    
    /** Contained data itself */
    protected Object data;
    
    /**
     * Creates a new instance of SingleToken
     * @param ontology identifier of supplied data
     * @param data ontology specific data
     */
    public SingleToken(String ontology, Object data) {
        assert (ontology != null);        
        this.ontology = ontology;
        this.data = data;
    }
    
    /**
     * Creates a new instance of SingleToken
     * This ctor should be called only from ctors of derived classes.
     * Sets ontology specific class to this.
     * @param ontology identifier of supplied data     
     */
    public SingleToken(String ontology) {
        assert (ontology != null);
        this.ontology = ontology;
        this.data = this;
    }
    
    /**
     * Queries whether this token contains data of given ontology
     * @param ontology identifier of ontology being questioned
     * @return <code>true</code> iff this token contains data in this ontology.
     *          Successive call to <code>getData(ontology)</code> will not fail.
     */
    public boolean supports(String ontology) {
        return this.ontology.equals(ontology);
    }
    
    /**
     * Retrieval of data in given ontology
     * @param ontology identifier of ontology
     * @return ontology specific data
     * @throws OntologyNotSupportedException if data in given
     *          ontology are not available
     */
    public Object getData(String ontology) throws OntologyNotSupportedException {
        if (this.ontology.equals(ontology))
            return data;
        throw new OntologyNotSupportedException(ontology);
    }
    
    /**
     * Retrieval of all ontologies supported
     * @return array of ontology identifiers. Successive calls to
     *          <code>getData( ontology)</code>, provided that ontology is one
     *          of the returned array, will not fail.
     */
    public String[] getOntologies(){
        return new String[]{ontology};
    }
    
}

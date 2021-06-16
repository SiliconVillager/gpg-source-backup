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
 
package cz.ive.manager;

import cz.ive.ontology.OntologyToken;
import cz.ive.exception.OntologyNotSupportedException;
import cz.ive.evaltree.Expr;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.iveobject.Link;
import cz.ive.lod.LOD;

/**
 * Holder of query data - stores tree of expression, role identifier
 *
 * @author Jirka
 */
public class QueryData implements OntologyToken, java.io.Serializable {
    
    private static final String ontoStringQuery = "jBRP.queryExpr";
    private static final String ontoStringRole = "jBRP.queryRole";
    private static final String ontoStringLod = "jBRP.queryLod";
    
    private Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl> expr;

    private Link role;
    
    private LOD lod;
    
    /**
     * Creates a new instance of QueryData
     * @param e only one fuzzy expression which will be instanciate 
     *	for for each input object, no other set operation are perfomed
     * @param link only objects containing compatible 
     *  {@link cz.ive.iveobject.Link} will be procceded. 
     *  If null is passed, there is no restriction. 
     * @param l main lod area for the query
     */
    public QueryData(Expr<FuzzyValueHolderImpl,? extends ValueHolderImpl> e,
	    Link link, LOD l) {
	
	expr = e;
	role = link;
	lod = l;
    }
    
    /**
     * @return expression of query
     */
    public Expr getExpr() {
	return expr;
    }
    
    /**
     * @return name of the role or null
     */
    public Link getRole() {
	return role;
    }
    
    /**
     * 
     * @return area LOD
     */
    public LOD getLod() {
	return lod;
    }

    public Object getData(String ontology) 
	    throws cz.ive.exception.OntologyNotSupportedException {
	
	if (ontoStringQuery.equals(ontology))
            return expr;
	else if (ontoStringRole.equals(ontology))
            return role;
	else if (ontoStringLod.equals(ontology))
            return lod;
	else
	    throw new OntologyNotSupportedException(ontology);
    }
    
    public String[] getOntologies() {
	return new String[]{ontoStringQuery, ontoStringRole};
    }
    
    public boolean supports(String ontology) {
	return (ontoStringQuery.equals(ontology) || ontoStringRole.equals(ontology));
    }
}

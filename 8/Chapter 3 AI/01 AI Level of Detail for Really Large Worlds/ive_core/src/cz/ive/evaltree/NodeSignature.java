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
 
package cz.ive.evaltree;


import cz.ive.valueholders.ValueType;

/**
 *  Class holding node signature.
 *  Node signature is node name and types of it's parameters
 *  Distinguishes normal node types (number of childs is known) and
 *  mutlinode types (number of chillds varies -multiand). Only first
 *  child type is significant in case of multinode
 *
 *  @author thorm
 */
class NodeSignature {
    
    /**
     * node name
     */
    protected String element;
    
    /**
     * whether node has unknown number of childs
     */
    protected boolean multi;
    
    /**
     * first child type
     */
    protected ValueType parama;
    
    /**
     * Second child type
     */
    protected ValueType paramb;
    
    /**
     *  Create empty signature
     */
    public NodeSignature() {
        parama = paramb = ValueType.NULL;
    }
    
    /**
     *  Create signature of leaf node (no childrens)
     * @param s node name (and,or..)
     */
    public NodeSignature(String s) {
        element = s;
        this.parama = ValueType.NULL;
        this.paramb = ValueType.NULL;
        this.multi = false;
    }
    
    /**
     *  Create signature of unary node
     * @param s node name (and,or..)
     * @param param child type
     */
    public NodeSignature(String s, ValueType param) {
        element = s;
        this.parama = param;
        this.paramb = ValueType.NULL;
        this.multi = false;
    }
    
    /**
     *  Create signature of unary node or multi-child node
     * @param s node name (and,or..)
     * @param param child or first child (in case of multinode) type
     * @param multi true means multinode
     */
    public NodeSignature(String s, ValueType param, boolean multi) {
        element = s;
        this.parama = param;
        this.paramb = ValueType.NULL;
        this.multi = multi;
    }
    
    /**
     *  Create signature of binary node
     * @param s node name (and,or..)
     * @param parama first child type
     * @param paramb second child type
     */
    public NodeSignature(String s, ValueType parama, ValueType paramb) {
        element = s;
        this.parama = parama;
        this.paramb = paramb;
        this.multi = false;
    }
    
    /**
     *  Set signature of leaf node (no childrens)
     * @param s node name (and,or..)
     */
    public void setSignature(String s) {
        element = s;
        this.parama = ValueType.NULL;
        this.paramb = ValueType.NULL;
        this.multi = false;
    }
    
    /**
     *  Set signature of unary node
     * @param s node name (and,or..)
     * @param param child type
     */
    public void setSignature(String s, ValueType param) {
        element = s;
        this.parama = param;
        this.paramb = ValueType.NULL;
        this.multi = false;
    }
    
    /**
     *  Set signature of unary node or multi-child node
     * @param s node name (and,or..)
     * @param param child type of first child type
     * @param multi true means that node has unknown number of childs
     */
    public void setSignature(String s, ValueType param, boolean multi) {
        element = s;
        this.parama = param;
        this.paramb = ValueType.NULL;
        this.multi = multi;
    }
    
    /**
     *  Set signature of binary node
     * @param s node name (and,or..)
     * @param parama first child type
     * @param paramb second child type
     */
    public void setSignature(String s, ValueType parama, ValueType paramb) {
        element = s;
        this.parama = parama;
        this.paramb = paramb;
        this.multi = false;
    }
    
    /**
     * demanded by HashMap
     * Two signatures equals iff
     *    their names are equal
     *    number of children is same (i.e one, two or unknown)
     *    types of children are same
     * @param obj object to compare
     * @return true if obj equals to this
     */
    public boolean equals(Object obj) {
        NodeSignature e = (NodeSignature) obj;
        
        return (element.equals(e.element) && multi == e.multi
                && parama == e.parama && paramb == e.paramb);
    }
    
    /**
     * demanded by hashMap
     * Each two equal signatures must has the same hashcode
     * @return hash code
     */
    public int hashCode() {
        return element.hashCode() + parama.hashCode() + paramb.hashCode();
    }
    
    /**
     * Return string containing human readable signature.
     * @return Human readable signature
     */
    public String toString() {
        if (multi) {
            return new String(element + "(" + parama + ", ... )");
        } else {
            return new String(element + "(" + parama + "," + paramb + ")");
        }
    }
}


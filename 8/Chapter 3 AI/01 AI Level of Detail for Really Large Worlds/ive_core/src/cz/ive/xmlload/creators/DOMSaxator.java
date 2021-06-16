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
 
package cz.ive.xmlload.creators;


import cz.ive.IveApplication;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Performs the DFS traversal on the DOM tree.
 * @author thorm
 */
public abstract class DOMSaxator {
    
    /** Creates a new instance of DOMSaxator */
    public DOMSaxator() {
        temporary = new AttributesImpl();
    }
    
    /**
     * Perform DFS traversal on the given DOM element.
     * @param e the element to traverse
     */
    public void traverseDOM(Element e) throws Exception {
        startDocument();
        recursive(e);
        endDocument();
        temporary.clear();
    }
    
    /**
     * Structure to keep attributes of processed element
     */
    AttributesImpl temporary;
    
    /**
     * DFS implementation
     */
    protected void recursive(Element e) throws Exception {
        copyAttrs(e.getAttributes());
        if (elementBegin(e)) {
            NodeList nl = e.getChildNodes();
            
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                
                n.getNodeName();
                short type = n.getNodeType();
                
                if (n.getNodeType() == n.ELEMENT_NODE) {
                    recursive((Element) n);
                }
            }
        }
        elementEnd(e);
    }
    
    /**
     * Copy attributes to the temporary member variable
     * @param nnm all child nodes of the processed element. Only some of them
     *            are attributes
     */
    private void copyAttrs(NamedNodeMap nnm) {
        temporary.clear();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node item = nnm.item(i);
            
            try {
                temporary.addAttribute("", item.getLocalName(),
                        item.getNodeName(), "", item.getNodeValue());
            } catch (Exception e) {
                IveApplication.printStackTrace(e);
                assert(false);
            }
        }
    }
    
    /**
     * Called on the begin of element processing
     * @param e processed element
     * @return true if subelements should be processed
     */
    abstract boolean elementBegin(Element e) throws Exception;
    
    /**
     * Called on the end of element processing ( when all children are processed)
     * @param e processed element
     */
    abstract void elementEnd(Element e)throws Exception;
    
    /**
     * Called before DFS
     * @return true
     */
    abstract boolean startDocument() throws Exception;
    
    /**
     * Called after DFS
     */
    abstract void endDocument() throws Exception;
}

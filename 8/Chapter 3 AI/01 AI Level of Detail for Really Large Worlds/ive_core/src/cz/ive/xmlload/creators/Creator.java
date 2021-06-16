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
import cz.ive.logs.Log;
import cz.ive.template.Template;
import java.util.*;
import org.w3c.dom.*;


/**
 * Crator for class T is able to load information about T from XML element 
 * and instantly create instance of T
 *
 * Template descendants performs the same task in two steps - load and instantiate.
 *
 * @param T class that the Creator can create.
 * @author thorm
 */
public abstract class Creator<T> {
    
    /**
     * Create the instance of <CODE>T</CODE> class using information from the
     * given element.
     *
     * @param t XML DOM element
     * @return new instance of <CODE>T</CODE>
     */
    public abstract T load(Element t);
    
    /**
     * Use the content of <CODE>t</CODE> to create Creator used to load the
     * element subtree
     * Attribute <CODE>creatorClassName</CODE> of <CODE>t</CODE> should contain 
     * name of the java class derived from Creator
     *
     * @param t element what we want to load
     * @param defCreator is used if <CODE>creatorClassName</CODE> attibute is 
     *                   not used
     *
     * @return suitable Creator
     */
    public static Creator getCreator(Element t, Creator defCreator) {
        String s = t.getAttribute("creatorClassName");
        Creator byClassLoader = (Creator) loadObject(s);

        return (byClassLoader == null) ? defCreator : byClassLoader;
    }
    
    
    /**
     * Use the content of <CODE>t</CODE> to create Template to use to load the
     * element subtree
     * Attribute <CODE>templateClassName</CODE> of <CODE>t</CODE> should contain 
     * name of the java class derived from Template class
     *
     * @param t element what we want to load
     * @param defTemplate is used if <CODE>templateClassName</CODE> attibute is 
     *                   not used
     *
     * @return suitable Creator
     */
    public static Template getTemplate(Element t, Template defTemplate) {
        String s = t.getAttribute("templateClassName");
        Template byClassLoader = (Template) loadObject(s);

        return (byClassLoader == null) ? defTemplate : byClassLoader;
    }
    
    /**
     * Uses te IveApplication to load some class.
     * IveApplication uses classpath modified by the loaded XML content
     *
     * @param s java class name
     * @return the new instance or null on fail.
     */
    static Object loadObject(String s) {
        if (s != null && !s.equals("")) {
            try {
                Class c = IveApplication.instance().loadIveClass(s);

                return c.newInstance();
            } catch (Exception e) {
                Log.severe("Class " + s + " not in classpath");
            }
        }
        return null;
    }
    
    /**
     * Get one subelement of given name
     * @param e	XML element whose subelement we want to obtain
     * @param s	name of desired subelement
     * @return first subelement of given name
     */
    public static Element getOneSubElement(Element e, String s) {
        NodeList nl = e.getElementsByTagName(s);

        return (Element) nl.item(0);
    }
    
    /**
     * Get one childelement of given name
     * @param e	XML element whose childelement we want to obtain
     * @param s	name of desired childelement
     * @return first childelement of given name
     */
    public static Element getOneChildElement(Element e, String s) {
        NodeList nl = e.getChildNodes();
        int c = nl.getLength();

        for (int i = 0; i < c; i++) {
            Node n = nl.item(i);
            
            if (n.getNodeType() == n.ELEMENT_NODE) {
                Element e2 = (Element) nl.item(i);
                
                if (s.equals("*") || e2.getTagName().equals(s)) {
                    return e2;
                }
            }
        }
        return null;
    }
    
    /**
     * Get all subelements of given name
     * @param e	XML element whose subelements we want to obtain
     * @param s	name of desired subelements
     * @return list of subelements of given name
     */
    public static List<Element> getSubElements(Element e, String s) {
        NodeList nl = e.getElementsByTagName(s);
        
        List<Element> elements = new LinkedList<Element>();

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);

            if (n.getNodeType() == n.ELEMENT_NODE) {
                elements.add((Element) n);
            }
            
        }
        return elements;
    }
    
    /**
     * Get all child elements of given name
     * @param e	XML element whose childelements we want to obtain
     * @param s	name of desired childelements
     * @return list of childelements of given name
     */
    public static List<Element> getChildElements(Element e, String s) {
        NodeList nl = e.getChildNodes();
        
        List<Element> elements = new LinkedList<Element>();

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);

            if (n.getNodeType() == n.ELEMENT_NODE
                    && (s.equals("*") || ((Element) n).getTagName().equals(s))
                    ) {
                elements.add((Element) n);
            }
            
        }
        return elements;
    }
    
    /**
     * Read the attribute <CODE>s</CODE> from the element <CODE>e</CODE> and 
     * parse it as boolean value.
     * 
     * @param e XML element
     * @param s name of the attribute
     * @param defValue default value
     * @return value of the attribute or the default value if such attribute 
     *         does not exist or has wrong format
     */
    public static boolean readBoolean(Element e, String s, boolean defValue) {
        String val = e.getAttribute(s);

        if (val == null || val.length() == 0) {
            return defValue;
        }
        return Boolean.parseBoolean(val);
    }
    
    /**
     * Read the attribute <CODE>s</CODE> from the element <CODE>e</CODE> and 
     * parse it as short value.
     * 
     * @param e XML element
     * @param s name of the attribute
     * @param defValue default value
     * @return value of the attribute or the default value if such attribute 
     *         does not exist or has wrong format
     */    
    public static short readShort(Element e, String s, short defValue) {
        String val = e.getAttribute(s);

        if (val == null || val.length() == 0 || val.equals("")) {            
            return defValue;
        }
        return Short.parseShort(val);
    }
    
    
}

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


import cz.ive.IveApplication;
import java.io.*;
import java.util.Stack;
import java.util.HashMap;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import cz.ive.evaltree.operators.fuzzy.*;
import cz.ive.evaltree.operators.integer.*;
import cz.ive.evaltree.operators.set.*;
import cz.ive.evaltree.relations.fuzzy.*;
import cz.ive.evaltree.relations.integer.*;
import cz.ive.valueholders.FuzzyValueHolder;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.valueholders.ValueType;
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.Link;


/**
 * Class used to load xml expression
 *
 * File format:
 * <PRE>
 * &lt;EVALTREE&gt;
 * expression1
 * expression2
 * &lt;/EVALTREE&gt;
 * </PRE>
 *
 * There are several types of epressions
 * <ul>
 *  <li> fuzzy </li>
 *      Fuzzy expressions consists from operators (and, or) and atomic values.<br>
 *      In case of fuzzy expressions atomic value can be some relation over
 *      numeric expressions ( described later), query of manager of sense, link
 *      to IveObject attribute of type FuzzyBool or constant value.
 *
 *  <li> integer </li>
 *      Integer expression can contain numeric operators ( add, mult,...). Atomic
 *      expression can be either link to IveObject attribute of type Int or
 *      constant value.
 *
 *  <li> set </li>
 *      Sets are used to describe queries, so they can appear only as subelements of
 *      queries.
 *      There is only one atomic expression when we speak about Set expressions - QueryLeaf.
 *      QueryLeaf describes set that contains all objects that hold given condition.
 *  </ul>
 *
 *
 * <h3> examples </h3>
 * <h4> link </h4>
 * <pre>
 * &lt;intlink target="pot1" attrib="water"/&gt;
 * </pre>
 * This is expression of type int. You can for example compare it to another one
 * <pre>
 * &lt;lt&gt;
 *	&lt;intlink target="pot1" attrib="water"/&gt;
 *	&lt;intlink target="pot1" attrib="maximum"/&gt;
 * &lt;/lt&gt;
 *
 * </pre>
 * and obtain expression of type Fuzzy.
 *
 * <h4> Manager query </h4>
 * mqnotepmty returns true when result of query is empty
 * Considered object is referenced as "setitem" in the description of the query. <br/>
 * Query can be described by fuzzy expression. In such case all objects that
 * holds given condition are in result set.
 * Condition can be specified by fuzzy expression.
 * <pre>
 * &lt;mqnotempty role="can"&gt;
 *      &lt;and&gt;
 *          &lt;gt&gt;
 *              &lt;intlink target="setitem" attrib="maximum"/&gt;
 *              &lt;int value="5"&gt;
 *          &lt;gt/&gt;
 *          &lt;fuzzylink target="setitem" attrib="empty"/&gt;
 *      &lt;and/&gt;
 * &lt;mqnotempty/&gt;
 * </pre>
 * Returns true if there is some empty can big enaugh to put 5 items of liquid into it.
 *
 * You can describe this query another way using set expressions:
 ** <pre>
 * &lt;mqcontains role="can" src="CanInMyHand"&gt;
 *      &lt;and&gt;
 *          &lt;queryleaf&gt;
 *          &lt;gt&gt;
 *              &lt;intlink target="setitem" attrib="maximum"/&gt;
 *              &lt;int value="5"&gt;
 *          &lt;gt/&gt;
 *          &lt;queryleaf/&gt;
 *          &lt;queryleaf&gt;
 *          &lt;fuzzylink target="setitem" attrib="empty"/&gt;
 *          &lt;queryleaf&gt;
 *      &lt;and/&gt;
 * &lt;mqnotempty/&gt;
 * </pre>
 * In this case, topmost operator and is not boolean operator, but intersection of
 * two sets described by queryleaf. This approach seems to be equivalent to the
 * first one so it might become unsupported in future.
 *
 * <h3> Table of elements </h3>
 * In expressions (that are XML trees) can be used this elements:<br/>
 * <table border="1">
 * <tbody>
 * <tr><td>element</td><td># children</td><td>child type</td><td>type </td><td>attributes</td><td>result</td></tr>
 * <tr><td> Fuzzy operators </td></tr>
 * <tr><td>and    </td><td>2         </td><td>Fuzzy     </td><td>Fuzzy  </td><td>          </td><td>fuzzy and </td></tr>
 * <tr><td>or     </td><td>2         </td><td>Fuzzy     </td><td>Fuzzy  </td><td>          </td><td>fuzzy or </td></tr>
 * <tr><td>not    </td><td>2         </td><td>Fuzzy     </td><td>Fuzzy  </td><td>          </td><td>fuzzy not </td></tr>
 * <tr><td>multiand</td><td>n        </td><td>Fuzzy     </td><td>Fuzzy  </td><td>          </td><td>fuzzy and </td></tr>
 * <tr><td>multior</td><td>n         </td><td>Fuzzy     </td><td>Fuzzy  </td><td>          </td><td>fuzzy or of children</td></tr>
 * <tr><td> Fuzzy relations </td></tr>
 * <tr><td>eq     </td><td>2         </td><td>Fuzzy     </td><td>Fuzzy  </td><td>          </td><td>equality of children<td></tr>
 * <tr><td>noteq  </td><td>2         </td><td>Fuzzy     </td><td>Fuzzy  </td><td>          </td><td>inequality of children<td></tr>
 * <tr><td>lt     </td><td>2         </td><td>Fuzzy     </td><td>Fuzzy  </td><td>          </td><td>less then<td></tr>
 * <tr><td>gt     </td><td>2         </td><td>Fuzzy     </td><td>Fuzzy  </td><td>          </td><td>greater then<td></tr>
 * <tr><td> Fuzzy leaves </td></tr>
 * <tr><td>fuzzy  </td><td>0         </td><td>-         </td><td>Fuzzy  </td><td>value     </td><td>constant<td></tr>
 * <tr><td>near   </td><td>0         </td><td>-         </td><td>Fuzzy  </td><td>obj1,obj2 </td><td>>returns true whenever obj1 and obj2 are on neighbour waypoints<td></tr>
 * <tr><td>fuzzylink</td><td>0       </td><td>-         </td><td>Fuzzy  </td><td>target,attrib</td><td>fuzzy value of <i>attrib</i> property of object <i>target</i> obtained from actual substitution<td></tr>
 * <tr><td>timer  </td><td>0         </td><td>-         </td><td>Fuzzy  </td><td>time,unit </td><td><td></tr>
 * <tr><td>timeinterval</td><td>0         </td><td>-         </td><td>Fuzzy  </td><td>start,end,unit </td><td><td></tr>
 * <tr><td> Manager queries </td></tr>
 * <tr><td>queryleaf</td><td>1    </td><td>Fuzzy     </td><td>Set  </td><td>          </td><td>Set of objects that complies condition expressed by Fuzzy expression. This is the only leaf that can be used in set expressions given to manager queries</td></tr>
 * <tr><td>managerquery</td><td>1    </td><td>Set or Fuzzy       </td><td>Fuzzy  </td><td>role          </td><td>Subtree is query for manager of senses. Result is set returned by manager.</td></tr>
 * <tr><td> Set Relations </td></tr>
 * <tr><td>empty</td><td>1    </td><td>Set       </td><td>Fuzzy  </td><td>role          </td><td>Subtree is query for manager of senses. True when result is empty set</td></tr>
 * <tr><td>notempty</td><td>1    </td><td>Set       </td><td>Fuzzy  </td><td>role          </td><td>Opposite to empty node</td></tr>
 * <tr><td>contains</td><td>1    </td><td>Set       </td><td>Fuzzy  </td><td>target, attrib </td><td>Is true if set contains the Object that is stored in attribute <i>attrib</i> of Object that is stored in source <i>target</i></td></tr>
 *
 * <tr><td> Integer operators </td></tr>
 * <tr><td>add    </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>addition<td></tr>
 * <tr><td>sub    </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>subtraction<td></tr>
 * <tr><td>mult   </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>multiplication<td></tr>
 * <tr><td>div    </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>division<td></tr>
 * <tr><td>mod    </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>modulo<td></tr>
 * <tr><td>neg    </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>negation<td></tr>
 * <tr><td>abs    </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>absolute value<td></tr>
 * <tr><td> Integer relations </td></tr>
 * <tr><td>eq     </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>equals<td></tr>
 * <tr><td>noteq  </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>not equal<td></tr>
 * <tr><td>lt     </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>less than<td></tr>
 * <tr><td>gt     </td><td>2         </td><td>Integer   </td><td>Integer</td><td>          </td><td>greater than<td></tr>
 * <tr><td> Integer leaves </td></tr>
 * <tr><td>int    </td><td>0         </td><td>-         </td><td>Integer</td><td>value     </td><td>constant<td></tr>
 * <tr><td>intlink</td><td>0         </td><td>-         </td><td>Integer</td><td>role,attrib</td><td>integer value of <i>attrib</i> property of object <i>target</i> obtained from actual substitution<td></tr>
 * <tr><td> Set operators </td></tr>
 * <tr><td>intersection    </td><td>2         </td><td>Set       </td><td>Set    </td><td>          </td><td>intersection</td></tr>
 * <tr><td>union     </td><td>2         </td><td>Set       </td><td>Set    </td><td>          </td><td>union</td></tr>
 * <tr><td>sub    </td><td>2         </td><td>Set       </td><td>Set    </td><td>          </td><td>substraction</td></tr>
 * </tbody>
 * </table>
 */

public class SAX2Creator {
    
    /**
     * Sax handler
     */
    EvalTreeSAXHandler handler;
    
    /**
     * Load expressions from xml file
     *
     * This version is recomended to correct functionality of Xinclude
     * @param uri URI of file to load
     * @return array of expressions from file
     */
    public Expr[] xml2expr(java.net.URI uri) {
        
        handler = new EvalTreeSAXHandler();
        try {
            prepareParser().parse(uri.toString(), handler);
            
        } catch (Throwable t) {
            IveApplication.printStackTrace(t);
        }
        Expr[] retarray = new Expr[1];
        
        return handler.finishedNodes.toArray(retarray);
    }
    
    /**
     * Load expressions from xml file
     *
     * This version is recomended to correct functionality of Xinclude
     * @param f file to load
     * @return array of expressions from file
     */
    public Expr[] xml2expr(String f) {
        return xml2expr(new File(f));
    }
    
    /**
     * Load expressions from xml file
     *
     *
     * @param f file to load
     * @return array of expressions from file
     */
    public Expr[] xml2expr(File f) {
        // Use an instance of ourselves as the SAX event handler
        handler = new EvalTreeSAXHandler();
        
        try {
            prepareParser().parse(f, handler);
            
        } catch (Throwable t) {
            IveApplication.printStackTrace(t);
        }
        Expr[] retarray = new Expr[1];
        
        return handler.finishedNodes.toArray(retarray);
    }
    
    /**
     * Load expressions from xml file
     * @param s input stream
     * @return array of expressions from file
     */
    public Expr[] xml2expr(InputStream s) {
        // Use an instance of ourselves as the SAX event handler
        handler = new EvalTreeSAXHandler();
        
        try {
            prepareParser().parse(s, handler);
        } catch (Throwable t) {
            IveApplication.printStackTrace(t);
        }
        Expr[] retarray = new Expr[1];
        
        return handler.finishedNodes.toArray(retarray);
    }
    
    /**
     * Create new parser and sets all needed features (validation)
     * @return new parser
     */
    private SAXParser prepareParser() {
        // Use the default (non-validating) parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        
        SAXParser saxParser = null;
        
        try {
            // Set up output stream
            saxParser = factory.newSAXParser();
        } catch (Throwable t) {
            IveApplication.printStackTrace(t);
        }
        return saxParser;
        
    }
    
    /**
     * Returned HashMap contains mapping from the expression name to the expression
     * @return all named expressions in the last parsed file
     */
    public HashMap<String, Expr> getNames() {
        return handler.getNames();
    }
    
    // ===========================================================
    // SAX DocumentHandler methods
    // ===========================================================
    /**
     * SAX2DefaultHandler descendant
     */
    public static class EvalTreeSAXHandler extends DefaultHandler {
        
        /**
         * Constructor
         * Initializes all members
         */
        public EvalTreeSAXHandler() {
            finishedNodes = new Stack<Expr>();
            parentsAttribs = new Stack<HashMap<String, String> >();
            names = new HashMap<String, Expr>();
            
        }
        
        void printlevels(String s) {
            System.out.print(s + "\t:");
            for (int i = 0; i < levelchilds.length; i++) {
                System.out.print("[" + i + ":" + levelchilds[i] + "]");
            }
            System.out.println();
            
        }
        
        /**
         * @throws org.xml.sax.SAXException
         */
        public void startDocument()
        throws SAXException {
            nodeSignature = new NodeSignature();
        }
        
        /**
         * @throws org.xml.sax.SAXException
         */
        public void endDocument()
        throws SAXException {}
        
        /**
         * @param namespaceURI unused
         * @param lName unused
         * @param qName node name
         * @param attrs node attributes
         * @throws org.xml.sax.SAXException except for SAX reasons thrown
         *          when syntax error ocured in input file (unknown node, wrong number
         *          of children)
         */
        public void startElement(String namespaceURI,
                String lName, // local name
                String qName, // qualified name
                Attributes attrs)
                throws SAXException {
            
            HashMap<String, String> attribcopy = new HashMap<String, String>();
            
            for (int i = 0; i < attrs.getLength(); i++) {
                attribcopy.put(attrs.getQName(i), attrs.getValue(i));
            }
            parentsAttribs.push(attribcopy);
            
            if (level >= lchildsize) {
                lchildsize *= 2;
                int[] tmplevelchilds = new int[lchildsize];
                
                for (int i = 0; i < levelchilds.length; i++) {
                    tmplevelchilds[i] = levelchilds[i];
                }
                levelchilds = tmplevelchilds;
            }
            
            (levelchilds[level++])++;
            levelchilds[level] = 0;
            // printlevels("Start "+qName);
        }
        
        
        
        /**
         * @param namespaceURI unused
         * @param sName unused
         * @param qName node name
         * @throws org.xml.sax.SAXException except for SAX reasons thrown when
         *         syntax error ocured in input file (unknown node, wrong
         *         number of children)
         */
        public void endElement(String namespaceURI,
                String sName, // simple name
                String qName// qualified name
                )
                throws SAXException {
            level--;
            HashMap<String, String> a = parentsAttribs.pop();
            
            if (qName.compareTo("link") == 0) {
                finishedNodes.push(names.get(a.get("name")));
                return;
            }
            
            if (qName.equals("EVALTREE")) {
                return;
            }
            
            int childs = levelchilds[level + 1];
            Expr[] chlds = new Expr[childs];
            
            for (int i = chlds.length - 1; i >= 0; i--) {
                chlds[i] = finishedNodes.pop();
            }
            
            boolean multi = qName.startsWith("multi");
            
            switch (childs) {
                case 0:
                    nodeSignature.setSignature(qName);
                    break;
                    
                case 1:
                    nodeSignature.setSignature(qName, chlds[0].getType(), multi);
                    break;
                    
                case 2:
                    nodeSignature.setSignature(qName, chlds[0].getType(),
                            chlds[1].getType());
                    break;
                    
                default:
                    if (multi) {
                        nodeSignature.setSignature(qName, chlds[0].getType(), multi);
                    } else {
                        throw new SAXException(
                                "Wrong number of childs:" + qName + ":" + childs);
                    }
            }
            
            NodeCreator c = elements.get(nodeSignature);
            
            if (c != null) {
                String name;
                Expr newNode = c.create(chlds, a);
                
                if (newNode == null) {
                    throw new SAXException(
                            "Couldnt create node:" + nodeSignature);
                }
                finishedNodes.push(newNode);
                
                if ((name = a.get("name")) != null) {
                    names.put(name, newNode);
                }
            } else {
                throw new SAXException("Signature not found:" + nodeSignature);
            }
            
            // printlevels("End   "+qName);
        }
        
        /**
         * Get names of expressions from file
         * If element has optional attribute "name", it is stored into map
         * returned by this function
         *
         * @return Map String->Expression
         */
        public HashMap<String, Expr> getNames() {
            return names;
        }
        
        /**
         * actual size of levelchilds array
         */
        private int lchildsize = 128;
        
        /**
         * actual depth
         */
        private int level = 0;
        
        /**
         * stack of finished nodes (to be used during creation of their parents)
         * xml2expr returns content of this stack (that contains top level nodes
         * when parsing finished)
         */
        public Stack<Expr> finishedNodes;
        
        /**
         * Used to store xml attributes of unfinished nodes (ie nodes on the path
         * from current position to the root)
         * We get this arguments when startElement method is called, but we need
         * it in endElement method
         */
        private Stack<HashMap<String, String> > parentsAttribs;
        
        /**
         * For each unfinished member remembers number of it' children
         */
        private int[] levelchilds = new int[lchildsize];
        
        /**
         * Remembers expression names
         */
        private HashMap<String, Expr> names;
        
        /**
         * Table of known node signatures. CreateNode succesor instance is
         * asociated to each entry
         */
        private static HashMap<NodeSignature, NodeCreator> elements;
        
        /**
         * Succesor of all CreatorClasses
         * Instances of CreatorClasses are used at elements map.
         */
        abstract static class NodeCreator<TYPE extends ValueHolderImpl,
                CHTYPE extends ValueHolderImpl> {
            
            /**
             * create new node
             * @param childs array of children
             * @param a attributes
             * @return new node
             */
            abstract Expr<TYPE, CHTYPE> create(
                    Expr<CHTYPE, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a
                    );
        }
        
        
        /**
         * FuzzyAnd creator class
         */
        static class FAndCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            /**
             * create FuzzyAnd new node
             * @param childs array of children
             * @param a attributes
             * @return new node
             */
            Expr<FuzzyValueHolderImpl, FuzzyValueHolderImpl> create(
                    Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new FuzzyAnd(childs[0], childs[1]);
            }
        }
        
        
        /**
         * FuzzyOr creator class
         */
        static class FOrCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            /**
             * create new FuzzyOr node
             * @param childs array of children
             * @param a attributes
             * @return new node
             */
            Expr<FuzzyValueHolderImpl, FuzzyValueHolderImpl> create(
                    Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new FuzzyOr(childs[0], childs[1]);
            }
        }
        
        
        /**
         * FuzzyNot creator class
         */
        static class FNotCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            /**
             * create new FuzzyNot node
             * @param childs array of children
             * @param a attributes
             * @return new node
             */
            Expr<FuzzyValueHolderImpl, FuzzyValueHolderImpl> create(
                    Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new FuzzyNot(childs[0]);
            }
        }
        
        
        /**
         * FuzzyMultiOr creator class
         */
        static class FMultiOrCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            /**
             * create new FuzzyMultiOr node
             * @param childs array of children
             * @param a attributes
             * @return new node
             */
            Expr<FuzzyValueHolderImpl, FuzzyValueHolderImpl> create(
                    Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new FuzzyMultiOr(childs);
            }
        }
        
        
        /**
         * FuzzyMultiAnd creator class
         */
        static class FMultiAndCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            /**
             * create new FuzzyMultiAnd node
             * @param childs array of children
             * @param a attributes
             * @return new node
             */
            Expr<FuzzyValueHolderImpl, FuzzyValueHolderImpl> create(
                    Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new FuzzyMultiAnd(childs);
            }
        }
        
        
        /**
         * FuzzyLeaf creator class
         */
        static class FuzzyLeafCreator extends
                NodeCreator<FuzzyValueHolderImpl, ValueHolderImpl> {
            
            /**
             * create new FuzzyLeaf node
             * @param childs array of children
             * @param a attributes
             * @return new node
             */
            Expr<FuzzyValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                String s = a.get("value");
                
                if (s.equals("true")) {
                    return new cz.ive.evaltree.leaves.FuzzyConstant(
                            FuzzyValueHolder.True);
                }
                if (s.equals("false")) {
                    return new cz.ive.evaltree.leaves.FuzzyConstant(
                            FuzzyValueHolder.False);
                }
                return new cz.ive.evaltree.leaves.FuzzyConstant(
                        Short.valueOf(a.get("value")));
            }
            
        }
        
        
        static class FuzzyTimerCreator extends
                NodeCreator<FuzzyValueHolderImpl, ValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                long time = Long.valueOf(a.get("time"));
                long inaccuracy = Long.valueOf(a.get("inaccuracy"));
                String units = a.get("units");
                int multiply = 1;
                
                if (units != null) {
                    if (units.equalsIgnoreCase("s")) {
                        multiply = 1000;
                    }
                    if (units.equalsIgnoreCase("min")) {
                        multiply = 1000 * 60;
                    }
                    if (units.equalsIgnoreCase("hour")) {
                        multiply = 1000 * 60 * 60;
                    }
                    if (units.equalsIgnoreCase("day")) {
                        multiply = 1000 * 60 * 60 * 24;
                    }
                }
                time *= multiply;
                inaccuracy *= multiply;
                return new cz.ive.evaltree.time.FuzzyTimer(time, inaccuracy);
            }
            
        }
        
        
        static class FuzzyTimeIntervalCreator extends
                NodeCreator<FuzzyValueHolderImpl, IntValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                int field = java.util.Calendar.HOUR_OF_DAY;
                String units = a.get("units");
                
                if (units != null) {
                    if (units.equalsIgnoreCase("day_of_week")) {
                        field = java.util.Calendar.DAY_OF_WEEK;
                    }
                }
                return new cz.ive.evaltree.time.FuzzyTimeInterval(childs[0],
                        childs[1], field);
            }
            
        }
        
        
        /**
         * FuzzyNear creator
         */
        static class FuzzyNearCreator extends
                NodeCreator<FuzzyValueHolderImpl, ValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.topology.FuzzyNear(a.get("obj1"),
                        a.get("obj2"));
            }
            
        }
        
        
        static class FuzzyNotNearCreator extends
                NodeCreator<FuzzyValueHolderImpl, ValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.topology.FuzzyNotNear(a.get("obj1"),
                        a.get("obj2"));
            }
            
        }
        
        
        static class FuzzyNotInCreator extends
                NodeCreator<FuzzyValueHolderImpl, ValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.topology.FuzzyNotIn(a.get("obj"),
                        a.get("waypoint"));
                
            }
        }
        
        
        static class FuzzyEmptySourceCreator extends
                NodeCreator<FuzzyValueHolderImpl, ValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.links.FuzzyEmptySource(
                        a.get("source"));
            }
            
        }
        
        
        static class DoISeeCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            Expr create(Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                String role = a.get("role");
                String process = a.get("process");
                String goal = a.get("goal");
                
                return new cz.ive.evaltree.mos.DoISee(
                        new Link(goal, process, role, null));
            }
            
        }
        
        
        static class DoISeeConditionCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            Expr create(Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                String role = a.get("role");
                String process = a.get("process");
                String goal = a.get("goal");
                
                return new cz.ive.evaltree.mos.DoISee(
                        new Link(goal, process, role, null), childs[0]);
            }
            
        }
        
        
        static class SetManagerQueryLeafCreator extends
                NodeCreator<SetValueHolderImpl, FuzzyValueHolderImpl> {
            
            Expr create(Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                String role = a.get("role");
                String process = a.get("process");
                String goal = a.get("goal");
                
                return new cz.ive.evaltree.mos.SetManagerQuery(childs[0],
                        new Link(goal, process, role, null));
            }
            
        }
        
        
        static class FuzzyManagerQueryLeafCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            Expr create(Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                
                String role = a.get("role");
                String process = a.get("process");
                String goal = a.get("goal");
                
                return new cz.ive.evaltree.mos.FuzzyManagerQuery(childs[0],
                        new Link(goal, process, role, null));
            }
            
        }
        
        
        static class EmptyCreator extends
                NodeCreator<FuzzyValueHolderImpl, SetValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, SetValueHolderImpl> create(
                    Expr<SetValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.relations.set.SetEmpty(childs[0]);
            }
            
        }
        
        
        static class NotEmptyCreator extends
                NodeCreator<FuzzyValueHolderImpl, SetValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, SetValueHolderImpl> create(
                    Expr<SetValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.relations.set.SetNotEmpty(childs[0]);
            }
            
        }
        
        
        static class ContainsCreator extends
                NodeCreator<FuzzyValueHolderImpl, SetValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, SetValueHolderImpl> create(
                    Expr<SetValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.relations.set.SetContains(childs[0],
                        a.get("target"), a.get("attrib"));
            }
            
        }
        
        
        /**
         * FuzzyLink creator class
         */
        static class FuzzyLinkCreator extends
                NodeCreator<FuzzyValueHolderImpl, ValueHolderImpl> {
            
            /**
             * create new FuzzyLeaf node
             * @param childs array of children
             * @param a attributes
             * @return new node
             */
            Expr<FuzzyValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.links.FuzzyLink(a.get("role"),
                        a.get("attrib"));
            }
            
        }
        
        
        static class BoolObjectLinkCreator extends
                NodeCreator<FuzzyValueHolderImpl, ValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.links.BoolObjectLink(a.get("role"),
                        a.get("attrib"));
            }
            
        }
        
        
        static class ObjectLinkCreator extends
                NodeCreator<IveObjectValueHolderImpl, ValueHolderImpl> {
            
            Expr<IveObjectValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.links.ObjectLink(a.get("role"),
                        a.get("attrib"));
            }
            
        }
        
        
        static class SourceCreator extends
                NodeCreator<IveObjectValueHolderImpl, ValueHolderImpl> {
            
            Expr<IveObjectValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.links.IveObjectSource(a.get("role"));
            }
            
        }
        
        
        // Integer operators
        static class IntAddCreator extends
                NodeCreator<IntValueHolderImpl, IntValueHolderImpl> {
            
            Expr<IntValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntAdd(childs[0], childs[1]);
            }
            
        }
        
        
        static class IntSubCreator extends
                NodeCreator<IntValueHolderImpl, IntValueHolderImpl> {
            
            Expr<IntValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntSub(childs[0], childs[1]);
            }
            
        }
        
        
        static class IntMultCreator extends
                NodeCreator<IntValueHolderImpl, IntValueHolderImpl> {
            
            Expr<IntValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntMult(childs[0], childs[1]);
            }
            
        }
        
        
        static class IntDivCreator extends
                NodeCreator<IntValueHolderImpl, IntValueHolderImpl> {
            
            Expr<IntValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntDiv(childs[0], childs[1]);
            }
            
        }
        
        
        static class IntModCreator extends
                NodeCreator<IntValueHolderImpl, IntValueHolderImpl> {
            
            Expr<IntValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntMod(childs[0], childs[1]);
            }
            
        }
        
        
        static class IntNegCreator extends
                NodeCreator<IntValueHolderImpl, IntValueHolderImpl> {
            
            Expr<IntValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntNeg(childs[0]);
            }
            
        }
        
        
        static class IntAbsCreator extends
                NodeCreator<IntValueHolderImpl, IntValueHolderImpl> {
            
            Expr<IntValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntAbs(childs[0]);
            }
            
        }
        
        
        // Integer relations
        static class IntEqCreator extends
                NodeCreator<FuzzyValueHolderImpl, IntValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntEq(childs[0], childs[1]);
            }
            
        }
        
        
        static class IntNotEqCreator extends
                NodeCreator<FuzzyValueHolderImpl, IntValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntNotEq(childs[0], childs[1]);
            }
            
        }
        
        
        static class IntGtCreator extends
                NodeCreator<FuzzyValueHolderImpl, IntValueHolderImpl> {
            Expr<FuzzyValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntGt(childs[0], childs[1]);
            }
            
        }
        
        
        static class IntLtCreator extends
                NodeCreator<FuzzyValueHolderImpl, IntValueHolderImpl> {
            Expr<FuzzyValueHolderImpl, IntValueHolderImpl> create(
                    Expr<IntValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new IntLt(childs[0], childs[1]);
            }
            
        }
        
        
        // FuzzyBool relations
        static class FuzzyEqCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            Expr<FuzzyValueHolderImpl, FuzzyValueHolderImpl> create(
                    Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new FuzzyEq(childs[0], childs[1]);
            }
            
        }
        
        
        static class FuzzyGtCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            Expr<FuzzyValueHolderImpl, FuzzyValueHolderImpl> create(
                    Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new FuzzyGt(childs[0], childs[1]);
            }
            
        }
        
        
        static class FuzzyLtCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, FuzzyValueHolderImpl> create(
                    Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new FuzzyLt(childs[0], childs[1]);
            }
            
        }
        
        
        static class IntLeafCreator extends
                NodeCreator<IntValueHolderImpl, ValueHolderImpl> {
            
            Expr<IntValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.leaves.IntConstant(
                        Integer.valueOf(a.get("value")));
            }
            
        }
        
        
        static class IntLinkCreator extends
                NodeCreator<IntValueHolderImpl, ValueHolderImpl> {
            
            Expr<IntValueHolderImpl, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.links.IntLink(a.get("role"),
                        a.get("attrib"));
            }
            
        }
        
        
        static class SetIntersectionCreator<T> extends
                NodeCreator<SetValueHolderImpl<T>, SetValueHolderImpl<T>> {
            
            Expr<SetValueHolderImpl<T>, SetValueHolderImpl<T>> create(
                    Expr<SetValueHolderImpl<T>, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new SetIntersection<T>(childs[0], childs[1]);
            }
            
        }
        
        
        static class SetUnionCreator<T> extends
                NodeCreator<SetValueHolderImpl<T>, SetValueHolderImpl<T>> {
            
            Expr<SetValueHolderImpl<T>, SetValueHolderImpl<T>> create(
                    Expr<SetValueHolderImpl<T>, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new SetUnion<T>(childs[0], childs[1]);
            }
            
        }
        
        
        static class SetSubCreator<T> extends
                NodeCreator<SetValueHolderImpl<T>, SetValueHolderImpl<T>> {
            
            Expr<SetValueHolderImpl<T>, SetValueHolderImpl<T>> create(
                    Expr<SetValueHolderImpl<T>, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new SetSub<T>(childs[0], childs[1]);
            }
            
        }
        
        
        static class FuzzyNotEqCreator extends
                NodeCreator<FuzzyValueHolderImpl, FuzzyValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, FuzzyValueHolderImpl> create(
                    Expr<FuzzyValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new FuzzyNotEq(childs[0], childs[1]);
            }
            
        }
        
        
        static class SetLinkCreator extends
                NodeCreator<SetValueHolderImpl<IveObject>, ValueHolderImpl> {
            
            Expr<SetValueHolderImpl<IveObject>, ValueHolderImpl> create(
                    Expr<ValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.links.SetLink(a.get("role"),
                        a.get("attrib"));
            }
            
        }
        
        
        static class DefinedCreator<T extends ValueHolderImpl> extends
                NodeCreator<FuzzyValueHolderImpl, T> {
            
            Expr<FuzzyValueHolderImpl, T> create(Expr<T, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.relations.FuzzyDefined<T>(childs[0]);
            }
            
        }
        
        
        static class IveObjectEqCreator extends
                NodeCreator<FuzzyValueHolderImpl, IveObjectValueHolderImpl> {
            
            Expr<FuzzyValueHolderImpl, IveObjectValueHolderImpl> create(
                    Expr<IveObjectValueHolderImpl, ? extends ValueHolderImpl>[] childs,
                    HashMap<String, String> a) {
                return new cz.ive.evaltree.relations.IveObjectEq(childs[0],
                        childs[1]);
            }
            
        }
        
        /**
         * used to store current node signature
         */
        private NodeSignature nodeSignature;
        
        static {
            elements = new HashMap<NodeSignature, NodeCreator>();
            // Fuzzy operators
            elements.put(
                    new NodeSignature("and", ValueType.FUZZY, ValueType.FUZZY),
                    new FAndCreator());
            elements.put(
                    new NodeSignature("or", ValueType.FUZZY, ValueType.FUZZY),
                    new FOrCreator());
            elements.put(new NodeSignature("not", ValueType.FUZZY),
                    new FNotCreator());
            elements.put(new NodeSignature("multiand", ValueType.FUZZY, true),
                    new FMultiAndCreator());
            elements.put(new NodeSignature("multior", ValueType.FUZZY, true),
                    new FMultiOrCreator());
            // Fuzzy relations
            elements.put(
                    new NodeSignature("eq", ValueType.FUZZY, ValueType.FUZZY),
                    new FuzzyEqCreator());
            elements.put(
                    new NodeSignature("noteq", ValueType.FUZZY, ValueType.FUZZY),
                    new FuzzyNotEqCreator());
            elements.put(
                    new NodeSignature("lt", ValueType.FUZZY, ValueType.FUZZY),
                    new FuzzyLtCreator());
            elements.put(
                    new NodeSignature("gt", ValueType.FUZZY, ValueType.FUZZY),
                    new FuzzyGtCreator());
            // Fuzzy leaves
            elements.put(new NodeSignature("fuzzy"), new FuzzyLeafCreator());
            elements.put(new NodeSignature("fuzzylink"), new FuzzyLinkCreator());
            elements.put(new NodeSignature("boolobjectlink"),
                    new BoolObjectLinkCreator());
            elements.put(new NodeSignature("timer"), new FuzzyTimerCreator());
            elements.put(
                    new NodeSignature("timeinterval", ValueType.INTEGER,
                    ValueType.INTEGER),
                    new FuzzyTimeIntervalCreator());
            
            elements.put(new NodeSignature("notin"), new FuzzyNotInCreator());
            elements.put(new NodeSignature("emptysource"),
                    new FuzzyEmptySourceCreator());
            elements.put(new NodeSignature("doisee"), new DoISeeCreator());
            elements.put(new NodeSignature("doisee", ValueType.FUZZY),
                    new DoISeeConditionCreator());
            
            // Manager queries
            
            
            elements.put(new NodeSignature("managerquery", ValueType.FUZZY),
                    new SetManagerQueryLeafCreator());
            elements.put(new NodeSignature("empty", ValueType.SET),
                    new EmptyCreator());
            elements.put(new NodeSignature("notempty", ValueType.SET),
                    new NotEmptyCreator());
            elements.put(new NodeSignature("contains", ValueType.SET),
                    new ContainsCreator());
            
            elements.put(
                    new NodeSignature("managerquerynotempty", ValueType.FUZZY),
                    new FuzzyManagerQueryLeafCreator());
            
            elements.put(new NodeSignature("defined", ValueType.FUZZY),
                    new DefinedCreator());
            elements.put(new NodeSignature("defined", ValueType.INTEGER),
                    new DefinedCreator());
            elements.put(new NodeSignature("defined", ValueType.OBJECT),
                    new DefinedCreator());
            
            elements.put(new NodeSignature("objectlink"),
                    new ObjectLinkCreator());
            elements.put(new NodeSignature("source"), new SourceCreator());
            
            elements.put(
                    new NodeSignature("eq", ValueType.OBJECT, ValueType.OBJECT),
                    new IveObjectEqCreator());
            
            // Integer operators
            elements.put(
                    new NodeSignature("add", ValueType.INTEGER,
                    ValueType.INTEGER),
                    new IntAddCreator());
            elements.put(
                    new NodeSignature("sub", ValueType.INTEGER,
                    ValueType.INTEGER),
                    new IntSubCreator());
            elements.put(
                    new NodeSignature("mult", ValueType.INTEGER,
                    ValueType.INTEGER),
                    new IntMultCreator());
            elements.put(
                    new NodeSignature("div", ValueType.INTEGER,
                    ValueType.INTEGER),
                    new IntDivCreator());
            elements.put(
                    new NodeSignature("mod", ValueType.INTEGER,
                    ValueType.INTEGER),
                    new IntModCreator());
            elements.put(new NodeSignature("neg", ValueType.INTEGER),
                    new IntNegCreator());
            elements.put(new NodeSignature("abs", ValueType.INTEGER),
                    new IntAbsCreator());
            // Integer relations
            elements.put(
                    new NodeSignature("eq", ValueType.INTEGER, ValueType.INTEGER),
                    new IntEqCreator());
            elements.put(
                    new NodeSignature("noteq", ValueType.INTEGER,
                    ValueType.INTEGER),
                    new IntNotEqCreator());
            elements.put(
                    new NodeSignature("lt", ValueType.INTEGER, ValueType.INTEGER),
                    new IntLtCreator());
            elements.put(
                    new NodeSignature("gt", ValueType.INTEGER, ValueType.INTEGER),
                    new IntGtCreator());
            // Integer leaves
            elements.put(new NodeSignature("int"), new IntLeafCreator());
            elements.put(new NodeSignature("near"), new FuzzyNearCreator());
            elements.put(new NodeSignature("notnear"), new FuzzyNotNearCreator());
            elements.put(new NodeSignature("intlink"), new IntLinkCreator());
            
            // Set operators
            elements.put(
                    new NodeSignature("intersection", ValueType.SET,
                    ValueType.SET),
                    new SetIntersectionCreator());
            elements.put(
                    new NodeSignature("union", ValueType.SET, ValueType.SET),
                    new SetUnionCreator());
            elements.put(new NodeSignature("sub", ValueType.SET, ValueType.SET),
                    new SetSubCreator());
            
            // Set leaves
            elements.put(new NodeSignature("setlink"), new SetLinkCreator());
            
        }
        
    }
    
}

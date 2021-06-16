/* 
 *
 * IVE Editor 
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
 
package IVE_Editor.Debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import javax.swing.JOptionPane;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.DOMException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Jirka, uprava 10.12.2007 martin
 */
public class XMLChecker implements ErrorHandler
{
    
    private File schemaFile;
    private boolean _error_flag = false;
    
    /** Creates a new instance of XMLChecker */
    public XMLChecker()
    {         
    }
    
    public XMLChecker(String file) {
        schemaFile = new File(file);
        try {
            schemaFile = schemaFile.getCanonicalFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void checkObjects( Document input_doc )
    {        
            //this object is capable of different outputs from JDOM document
            XMLOutputter outputter = new XMLOutputter( org.jdom.output.Format.getPrettyFormat() );
            
            //SAXBuilder builder = new SAXBuilder( true ); //building document with JDOM - but it supports only DTD validation
            
            
            org.w3c.dom.Document document = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();            
            
            SchemaFactory schemaFactory = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI);         
            
        try
        {
            Schema schema = schemaFactory.newSchema( new File( "IveWorld.xsd" ) );
            //Schema schema = schemaFactory.newSchema( schemaFile );
            assert(schema != null);
            factory.setSchema(schema);
            DocumentBuilder builder = factory.newDocumentBuilder();

            document = builder.parse( 
                    new StringBufferInputStream( 
                        outputter.outputString( input_doc ) ) );
            System.out.println( "The document is in valid XML form.        O.K." );
        } catch (ParserConfigurationException ex)
        {
            
            ex.printStackTrace();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        } catch (SAXException ex)
        {
            System.out.println( "The objects are in valid XML form.        FAILED:" );
            ex.printStackTrace();            
        }                        
            
    }
    
    public boolean checkValidity( Document input_doc)
    {                 
            XMLOutputter outputter = new XMLOutputter( org.jdom.output.Format.getPrettyFormat() );
            
            org.w3c.dom.Document document = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();            
            
//            System.out.println("input Dokument:");
//            System.out.println(input_doc);
        try
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI);      
            
            Schema schema = schemaFactory.newSchema( schemaFile );

            assert(schema != null);
            factory.setSchema(schema);
            DocumentBuilder builder = factory.newDocumentBuilder();                           
            builder.setErrorHandler( this );
            
            document = builder.parse( 
                    new StringBufferInputStream( 
                        outputter.outputString( input_doc ) ) );                           
                      
//            System.out.println("Helper Document:");
//            System.out.println(document);
//            System.out.println("a ma " + document.getChildNodes().getLength() + " elementu");
//            System.out.println("a jmenuje se " + document.getChildNodes().item(0).getLocalName() ); 
//            System.out.println( document.getChildNodes().item(0).getLocalName() == null );
            
//            return !( document == null ||
//                      document.getChildNodes().getLength() == 0 ||
//                      document.getChildNodes().item(0).getLocalName() == null );
            
            if ( _error_flag ) {
                _error_flag = false;
                return false;
            }
            
            return true;
        }
        catch (SAXParseException spe) {
            // Error generated by the parser
            spe.printStackTrace();
            _error_flag = false;
            return false;                     
        }  
            catch (SAXException ex)
        {
            ex.printStackTrace();
            _error_flag = false;
            return false;
        }catch (ParserConfigurationException ex)
        {            
            ex.printStackTrace();
            _error_flag = false;
            return false;
        } catch (IOException ex)
        {
           ex.printStackTrace();
           _error_flag = false;
           return false;
        } catch (Exception e) {
            e.printStackTrace();
            _error_flag = false;
            return false;
        }
                                 
            
    }
    
    /**
     * check  validity of checkFile against actual xsd file
     */
    public boolean checkValidity( File checkFile )
    {                 
        // parse an XML document into a DOM tree  
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document document = parser.parse(checkFile);

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory factory = SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // load a WXS schema, represented by a Schema instance        
            Source schemaF = new StreamSource(schemaFile);
//            System.out.println( checkFile );
//            System.out.println( schemaFile );
//            System.out.println( document );
            Schema schema = factory.newSchema(schemaF);

            // create a Validator instance, which can be used to validate an instance document
            Validator validator = schema.newValidator();

            // validate the DOM tree        
            validator.validate(new DOMSource(document));
        } catch (SAXException e) {
        // instance document is invalid!            
            System.out.println( e.getMessage() );
            return false;
        } catch(ParserConfigurationException e) {
            System.out.println( e.getMessage() );
            return false;
        }
        catch (IOException e ) {
            System.out.println( e.getMessage() );
            return false;
        }                                  
         return true;   
    }    

    public void warning(SAXParseException exception) throws SAXException {
        System.out.println("Warning: " + exception.getMessage() );
    }

    public void error(SAXParseException exception) throws SAXException {
        System.out.println("Error: " + exception.getMessage() );
        _error_flag = true;
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        System.out.println("fatalError: " + exception.getMessage() );
        _error_flag = true;
    }
    
}

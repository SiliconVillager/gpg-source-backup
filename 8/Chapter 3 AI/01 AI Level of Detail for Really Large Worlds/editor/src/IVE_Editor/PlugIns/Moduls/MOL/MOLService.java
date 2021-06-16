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
 
package IVE_Editor.PlugIns.Moduls.MOL;

import IVE_Editor.PlugIns.Service;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author Jirka
 */
public class MOLService extends Service
{
    
    private MOL _mol;
    
    /**
     * Creates a new instance of KProjektService
     */
    public MOLService( MOL mol )
    {
          _mol = mol;
    }
    
    /**
     * @return Returns XML code from MPC ( objectsTemplates node, locationsTemplates node,
     *                                     graphicTemplates node )
     */
    public ArrayList<org.jdom.Element> getXML() {
        return _mol.getXML();
    }  
    
    /** 
     *  Create objects templates in modul MPC and fills in its data structures with data from
     *  world XML description.
     */
    public void fillInObjects( List <org.jdom.Element> objects ) {
        _mol.fillInObjects( objects );
    }        
   
    /** 
     *  Create locations templates in modul MPC and fills in its data structures with data from
     *  world XML description.
     */
    public void fillInLocations( List <org.jdom.Element> locations ) {
        _mol.fillInLocations( locations );
    }        
     
    /** 
     *  Create graphics templates in modul MPC and fills in its data structures with data from
     *  world XML description.
     */
    public void fillInGraphics( List <org.jdom.Element> graphics ) {
        _mol.fillInGraphics( graphics );
    }
    
    /** 
     *  Create root location template in modul MPC and fills in its data structures with data from
     *  world XML description.
     */
    public void fillInRootLocation( List <org.jdom.Element> rootlocation ) {
        _mol.fillInRootLocation( rootlocation );
    }
    
    /** 
     *  This event occures when ent genius id changes in MPG.
     *  
     *  @param entName Name of the ent which belongs the genius to.
     *  @param geniusName Name of the genius - when it is empty it means
     *  removing the genius "from ent".
     */
    public void setEntGenius( String entName, String geniusName ){
        _mol.setEntGenius( entName, geniusName );
    }
    
    /** 
     *  This event occures when location genii list changes in MPC.
     *  
     *  @param locPath path uniquely identifying the location in the MOL. Path
     *  is created from names of all locations on the path from root location to
     *  desired location included. The names are separated by dots.
     *  @param genii the list of names of all genii set to the location determined by
     *  the locPath
     */
    public void setLocationGenii( String locPath, List< String > genii ){
        _mol.setLocationGenii( locPath, genii );
    }        
    
    /** 
     *  Returns the paths to all locations currently presented in the MOL. These paths are
     *  unique identifiers of the locations in the projekt.
     */
    public List< String > getLocationPaths(){
        return _mol.getLocationPaths();
    }
    
    /** Returns the names of all ents currently present in the MOL. */
    public List< String > getEntNames(){
        return _mol.getEntNames();
    }    
    
    /** Returns the names of all objects currently present in the MOL which are not ents. */
    public List< String > getObjectNames(){
        return _mol.getObjectNames();
    }       
    
    /** 
     *  Set <links> element to specified location.
     *
     *  @param locationId dots separated path to the location in location hierarchy uniquely
     *  identifying the location
     *  @param linksEl org.jdom.Element which should be assigned to the specified location
     */
    public void setLinks( String locationId, Element linksEl ){
        _mol.setLinksElement( locationId, linksEl );
    }
    
    /**    
     * Supported attribute types and form in which they should be referred to can 
     * be found in class IVE_Editor.PlugIns.Moduls.MOL.Attribute in 
     * public static final array type_captions.    
     *
     * @param objName name of object which attributes should be returned
     * @param atrType Specify which attributes of specified object should be
     *        returned. If this parameter is null then all attributes of
     *        specified object are returned (types are not distinguished)
     *
     * @return ArrayList of "iveObject" attributes of specified type which has set the
     *         object with specified name
     * @return Empty ArrayList when objName is null or specified object has no attributes
     *         or specified object doesn't exist in the project
     * @return null if some fatal error has occured
     *     
     */
    public ArrayList< String > getObjectAttributes( String objName, String atrType ) {
        return _mol.getObjectAttributes( objName, atrType );
    }
    
}

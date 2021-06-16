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
 
package IVE_Editor.PlugIns.Moduls.MPG;
 
import IVE_Editor.PlugIns.Service;
import java.util.*;

/**
 * 
 *
 * Class represents all services that can be used by other plugins
 *
 * @author Juhasz Martin
 */
public class MPGService extends Service
{
    
    private MPG _mpg;
    
    /**
     * Creates a new instance of MPGService
     */
    public MPGService( MPG mpg )
    {
          _mpg = mpg;
    }
    
    /**
     * 
     * 
     * @return Returns XML code fromMPG 
     * ( processTemplates node, utterances node, SimulationSettings node)
     */
    public ArrayList<org.jdom.Element> getXML() {
        return _mpg.getXML();
    }            
    
    /**
     * @return Returns map of Geniuses ( pairs of <GeniusId,its XML code> )
     * used by XMLSaver
     */    
    public Map<String,org.jdom.Element > getEntGeniuses() {
        return _mpg.getEntGeniuses();
    }
 
    /**
     * @return Returns map of Geniuses 
     * ( pairs of <id of genius in some location,its XML code> )
     * used by XMLSaver
     */        
    public Map<String,org.jdom.Element> getLocationGeniuses() {
        return _mpg.getLocationGeniuses();
    }
    
     /**
     * @return Returns map of Object-Links( pairs of <id of object (location) 
     * its Links like ArrayList<org.jdom.Element> )
     * used by XMLSaver
     */        
    public Map<String,ArrayList<org.jdom.Element >> getObjectsToLinks() {
        return _mpg.getObjectsToLinks();
    }    
    
    /**
     * 
     *  Create processes templates in modul MPG and fills in its data structures with data from
     *  XML description.
     */
    public void fillInProcesses( List <org.jdom.Element> procesy ) {
        _mpg.fillInProcesses(procesy);
    }        

     /**
     * 
     *  Create utterances in modul MPG and fills in its data structures with data from
     *  XML description.
     */
    public void fillInUtterances(List listut) {        
        _mpg.fillInUtterances(listut);
    }
        
     /**
     * 
     *  Create simulation settings elements in MPG and fills in its data structures 
     * with datas from XML description.
     */
    public void fillInSS(List listss) {     
        _mpg.fillInSS(listss);
    }
    
     /**
     * 
     *  Create links in modul MPG and fills in its data structures with datas from
     *  XML description.
     */
    public void fillInLinks(List listobj) {
        _mpg.fillInLinks(listobj);
    }
    
     /**
     * 
     *  Create links in modul MPG and fills in its data structures with datas from
     *  XML description.
     */
    public void fillInLinksFromLocations(List listloc) {
        _mpg.fillInLinksFromLocations(listloc);
    }    
    
    /**     
     *  MPG need name of root location during creating location genies
     */
    public void fillInRootLocation(org.jdom.Element root) {
        _mpg.fillInRootLocation(root);
    }
       
    
    /** 
     *  Create genies from root location
     *  XML description.
     */
    public void fillInRootLocationGenies(List<org.jdom.Element> root) {
        _mpg.fillInRootLocationGenies(root);
    }    
    
    /**
     * @return Returns ArrayList of EntGeniuses 
     */        
    public ArrayList<String> getEntGeniusesId() {
        return _mpg.getEntGeniusesId();
    }    
        
    /** 
     * @return Returns ArrayList of LocationGeniuses 
     */        
    public ArrayList<String> getLocationGeniusesId() {
        return _mpg.getLocationGeniusesId();
    }        
    
  
    /**
     * 
     * Event occures when Ent is removed from MOL
     * 
     * removes EntName from MPG
     * 
     * ( used in genius editing )
     */        
    public void removeEnt(String entName) {
        _mpg.removeEnt(entName);
    }
    
    /**
     * 
     *  Event occures when Ent is renamed in MOL
     * 
     * renames oldEntName to newEntName in MPG
     * 
     * ( used in genius editing )
     */        
    public void renameEnt(String oldEntName, String newEntName) {
        _mpg.renameEnt(oldEntName, newEntName);
    }
    
    /**
     * 
     * Event occures when objectName is removed from MOL
     * 
     * removes objectName from MPG
     * 
     * ( used in Link Editing )
     */            
    public void removeObject(String objectName) {
        _mpg.removeObject(objectName);
    }   
    
    /**
     * 
     * Event occures when objectName is renamed in MOL
     * 
     * renames oldObject to newObject in MPG
     * 
     * ( used in Link Editing )
     */                
    public void renameObject(String oldName, String newName) {
        _mpg.renameObject(oldName, newName);
    }
    
    /**
     * 
     * Event occures when locationId is removed from MOL
     * 
     * removes locationId in MPG 
     * 
     * ( used in Link Editing )
     */        
    public void removeLocation(String locationId) {
        _mpg.removeLocation(locationId);
    }
    
    /**
     * 
     * Event occures when locatinId is renamed in MOL
     * 
     * remames oldLocationId to newLocationId in MPG 
     * 
     * ( used in Link Editing )
     */        
    public void renameLocation(String oldLocationId, String newLocationId) {
        _mpg.renameLocation(oldLocationId, newLocationId);
    }
    
    
    /** 
     * Event occures during xml saving
     *
     * notify MPG to send created links to MOl (only links for locations)
     * 
     * 
     */        
    public void setLinks() {
        _mpg.setLinks();
    }
    
    /**
     * Create location genies from xml. ( when loading or opening project )
     */
    public void fillInLocationGenies(List listloc) {
        _mpg.fillInLocationGenies(listloc);
    }
    
    /**
     * Event occures when location in MOL is copyied.
     * 
     * Add new locationPath to geniusName in MPG
     */
    public void geniusSetToLocation( String geniusName, String locationPath ) {
        _mpg.geniusSetToLocation(geniusName,locationPath);
    }
    
    public boolean geniusSetToEnt( String entName, String geniusName ) {
        return _mpg.geniusSetToEnt( entName, geniusName);
    }
    /**
     * Enables MPG
     */
    public void setEnabledMPG(boolean b) {
       _mpg.setEnabledMPG(b); 
    }
    
                  
}

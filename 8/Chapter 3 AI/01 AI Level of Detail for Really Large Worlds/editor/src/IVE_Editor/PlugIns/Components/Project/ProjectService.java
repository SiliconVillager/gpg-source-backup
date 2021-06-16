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
 
package IVE_Editor.PlugIns.Components.Project;

import IVE_Editor.PlugIns.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *  
 * Class represents all services that can be used by other plugins
 *
 * @author Juhasz Martin
 */
public class ProjectService extends Service
{
    
    private Project _project;
    
    /**
     * Creates a new instance of ProjectService
     */
    public ProjectService( Project project)
    {
          _project = project;
    }
    
    /** 
     * Return path on the local disc to directory with images for the project.
     * @return  Path on the local disc to directory with images for the project.
     */    
    public String getPathDirectoryIMAGE(){
        return _project.getPathDirectoryIMAGE();
    }
    
    /** 
     * Return world name e.g. SchoolDemoworld
     * @return World name e.g. SchoolDemoworld
     */        
    public String getWorldName(){
        return _project.getWorldName();
    }   
    
    /** 
     * return path to java editor
     * @return path to java editor
     */        
    public String getJavaEditorPath(){
        return _project.getJavaEditorPath();
    }       
       
    /** 
     * Return list of class paths
     * @return List of class paths
     */            
    public ArrayList<String> getClassPathList() {
        return _project.getClassPathList();
    }

    /** 
    * Fill in class path atribut
    */        
    public void fillInClassPathItems(List listcp) {
        _project.fillInClassPathItems(listcp);
    }
             
    /** 
     * Returns path to cz.ive.process directory
     * @return Path to cz.ive.process directory
     */        
    public String getProcessPath() {
        return _project.getProcessPath();
    }
    
    /** 
     * Returns path to cz.ive.object directory
     * @return Path to cz.ive.object directory
     */            
    public String getIveObjectPath() {
        return _project.getIveObjectPath();
    }
    
    /** 
     * Returns path to cz.ive.location directory
     * @return Path to cz.ive.location directory
     */            
    public String getLocationPath() {
        return _project.getLocationPath();
    }    
    
    /** 
     * Returns path to cz.ive.genius directory
     * @return Path to cz.ive.genius directory
     */            
    public String getGeniusPath() {
        return _project.getGeniusPath();
    }
    
    /** 
     * Returns path to cz.ive.process directory
     * @return Path to cz.ive.process directory
     */            
    public String getGuiPath() {
        return _project.getGuiPath();
    }    
    
    /** 
     * Returns file entgenius.xml
     * @return File entgenius.xml
     */            
    public File getMpgEntGeniusXmlFile() {
        return _project.getMpgEntGeniusXmlFile();
    }
    
    /** 
     * Returns file enttogenius.dat
     * @return File enttogenius.dat
     */                
    public File getMpgEntToGeniusFile() {
        return _project.getMpgEntToGeniusFile();
    }    
    
    /** 
     * Returns file locationgenius.xml
     * @return File locationgenius.xml
     */                
    public File getMpgLocationGeniusXmlFile() {
        return _project.getMpgLocationGeniusXmlFile();
    } 
    
    /** 
     * Returns file locationgenius.dat
     * @return File locationgenius.dat
     */                
    public File getMpgLocationToGeniusFile() {
        return _project.getMpgLocationToGeniusFile();
    }        
    
    /** 
     * Returns file links.dat
     * @return File links.dat
     */            
    public File getMpgLinksFile() {
        return _project.getMpgLinksFile();
    }
    
    /** 
     * Returns path to current project e.g. D:/MyIVEworlds/TestWorld
     * @return Path to current project e.g. D:/MyIVEworlds/TestWorld
     */            
    public String getProjectPath() {
        return _project.getProjectPath();
    }
    
    /** 
     * Returns path to MPG directory
     * @return Path to MPG directory
     */                
    public String getMPGPath() {
        return _project.getMPGPath();
    }
    
    /** 
     * Returns path to project sources directory 
     * @return Path to project sources directory 
     */                    
    public String getProjectSources() {
        return _project.getProjectSouces();
    }
    
    /** 
     * Returns path to cz.ive.sensors directory
     * @return Path to cz.ive.sensors directory
     */                    
    public String getSensorsPath() {
        return _project.getSensorsPath();
    }       
    
}

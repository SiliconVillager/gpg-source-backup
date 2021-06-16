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

import java.util.ArrayList;
import org.jdom.*;

/**
 * Collects everything from MOL which should appear in the output World
 *
 * @author Jirka
 */
public class Outputter
{
    /** Current output document. */
    private Document _output;
    
    
    //--------------------------------------------------------------------------------------------------------------------------------------------
    // Constructors section
    //------------------------------    
    
    /** Creates a new instance of Outputter */
    public Outputter()
    {
        reset();
    }
        
    //--------------------------------------------------------------------------------------------------------------------------------------------
    // Public section
    //------------------------------
    /** For now it's used for debuging but in future work it'll collect everything from MOL what should occure in the World and
     *  let it go upwards to some saving objects on demand.
     */
    public org.jdom.Document getOutput()
    {                
        return _output;
    }
    
    //--------------------------------------------------------------------------------------------------------------------------------------------
    
    /** 
     * Adds an Element e to the output document. Current Document can be obtained be invoking mothod getOutput.
     */
    public void addToOutput( org.jdom.Element e )
    {                
        _output.getRootElement().addContent( e );        
    }    
    
    //--------------------------------------------------------------------------------------------------------------------------------------------
    /** Reset the content of outtputer for adding some new content. */
    public void reset(){
        Element iveWorld = new Element( "IveWorld" );
        _output = new Document( iveWorld );
    }
    
    
    
}

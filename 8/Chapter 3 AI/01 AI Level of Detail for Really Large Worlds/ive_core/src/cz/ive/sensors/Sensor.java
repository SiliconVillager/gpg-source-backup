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
 
package cz.ive.sensors;


import cz.ive.iveobject.IveObject;
import cz.ive.location.WayPoint;
import cz.ive.lod.LOD;

/**
 * Simple interface of sensors - specification of it's identifications.
 * Sensors reduce set of input IveObject and filter data (attributes) of objets.
 * IveObjects and attributes have mask from long, which defines 
 * applicable sensor types. 
 * Sensor have information about it's position and about LOD interval. 
 * These data enables reduction of applicable IveObject.
 *
 * @author Jirka
 */
public interface Sensor extends IveObject {

    public long EYE = 1;
    //the other types will be added as square of 2 (so follows 2, 4, 8, 16...)
  
    /**
     * To get identifier of sensor type
     *
     * @return id of sensor type
     */
    public long getType();
    
    /**
     * @return interval (LOD holds min and max values) of interesting LOD
     */
    public LOD getLOD();   
    
    /**
     * Sets the LOD interval of this sensor
     * @param lod new Lod value.
     */
    public void setLOD(LOD lod);
        
    /**
     * Determine wheter mask suits to this sensor.
     *
     * @return true if mask suits.
     */
    public boolean compareWithMask(long mask);
}


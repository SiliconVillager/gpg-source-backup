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
 
package cz.ive.manager;

import cz.ive.messaging.SyncHook;
import cz.ive.sensors.*;
import java.util.*;

/**
 * @author Jirka
 */
public abstract class QueryTrigger extends SyncHook {
    
    /** sensors to restrict objects */
    public List<Sensor> sensors;
    
    /** reference to manager - to enable of calling some his methods */
    protected ManagerOfSenses manager;
    
    /**
     * Unregistration. Listener will not be called back anymore
     * If it's last listener, unregister trigger from manager.
     *
     * @param listener who want to be unregistred
     */
    public void unregisterListener(cz.ive.messaging.Listener listener) {
	
	super.unregisterListener(listener);
	if (listeners.isEmpty()) {
	    manager.unregisterPassive(this);
	}
    }
    
    /**
     * Unregisters all registered listenners 
     * and unregister trigger from manager
     */
    public void unregisterAll() {
	
	super.unregisterAll();
    }        

    /**
     * Exchanges the old sensor by the new one.
     * @param oldSensor Sensor to be replaced
     * @param newSensor Sensor to add
     * @return true if the sensor list contained the oldSensor
     */
    public boolean exchangeSensor(Sensor oldSensor, Sensor newSensor) {
        int idx = sensors.indexOf(oldSensor);
        if (idx >= 0) {
            sensors.set(idx, newSensor);
            return true;
        }
        
        return false;
    }
    
    /**
     * Replaces set of sensors for new one and process actualization - allways
     * calls notification.
     * 
     * @param sens new list of sensors
     */ 
    public void changeSensors(List<Sensor> sens) {	
        manager.sensorsExchanged(this, sensors, sens);
        
        sensors.clear();
        sensors.addAll(sens);
    }
    
}


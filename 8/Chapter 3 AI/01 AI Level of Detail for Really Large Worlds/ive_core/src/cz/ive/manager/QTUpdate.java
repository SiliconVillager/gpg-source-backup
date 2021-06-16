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

import cz.ive.iveobject.IveObject;
import cz.ive.sensors.*;
import cz.ive.messaging.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.iveobject.*;
import cz.ive.location.WayPoint;
import cz.ive.logs.*;
import java.util.*;


/**
 * Box of passive update.
 * Stores copy of the IveObject and notify listeners about its changes.
 *
 * @author Jirka
 */
public class QTUpdate extends QueryTrigger implements Listener {
    
    /** copy of updated object is stored here */
    private IveObject copy;
    
    /** valid object according to is object actualized */
    private IveObject original;
    
    /** 
     * Signify whether object is visible by given sensors. 
     * If not, no changes in object are visible and actualization continues
     * after it become again visible.
     */
    private boolean valid;
    
    /** sensors aplicable regarding to actual object position */
    private List<Sensor> aplicableSensors;
    
    /**
     * Creates new trigger to manage passive updates.
     *
     * @param man refernece to manager of wits
     * @param sens list of sensors
     * @param obj copy of object to update
     * @param orig valid object according to is actualized
     */
    public QTUpdate(ManagerOfSenses man, List<Sensor> sens, 
	    IveObject obj, IveObject orig) {
	manager = man;
        sensors = new Vector<Sensor>();
	sensors.addAll(sens);
	copy = obj;
	original = null;
        valid = false;
        if (orig != null) {
            setOriginalObject(orig);
        }
    }
    
    /**
     * Sets the original object.
     * Can be called only once (with non-null object) on one instance. 
     * @param object Source of the updated copy
     * @return true if the original was set, false if this is not a first call
     */
    public boolean setOriginalObject(IveObject object) {
        
        if ((original != null) || (object == null)) {
            return false;
        }
        
        original = object;

        aplicableSensors = new LinkedList<Sensor>();
        if (object.getObjectState() == IveObject.ObjectState.VALID) {
            for (Sensor s: sensors) {
                WayPoint pos = original.getPosition();
                if (pos == null || 
                        manager.isApplicableSensor(s, original)) {
                    aplicableSensors.add(s);
                }
            }
        }
        
	if (aplicableSensors.isEmpty()) {
	    valid = false; //object is not visible by any sensor
	} else {
	    valid = true;	
	}
	manager.updateCopy(aplicableSensors, original, copy, null);	
	for (AttributeValue attr : original.getAllAtributes()) {
	    attr.registerListener(this);
	}
        
        return true;
    }
    
    /** 
     * Signify whether object is visible by given sensors. 
     * If not, no changes in object are visible and actualization continues 
     * after it become again visible.
     *
     * @return true if object is valid
     */
    public boolean isValid() {
	return valid;
    }
    
    /**
     * @return actualized copy
     */
    public IveObject getCopy() {
	return copy;
    }
    
    /**
     * Called by Manager of senses when sensor relevant to this query change 
     * its position so the set of covered waypoitns has changed.
     *
     * @param sens sensor which has moved
     */
    void sensorMove(Sensor sens) {
        
        if (original == null) {
            return;
        }
        
	if (aplicableSensors.contains(sens)) {	   
	    if ((original.getObjectState() != IveObject.ObjectState.VALID) || 
                    (!manager.isApplicableSensor(sens, original))) {
		aplicableSensors.remove(sens);
		if (aplicableSensors.isEmpty()) {
		    valid = false;
		    notifyListeners();
		} else {
		    if (manager.updateCopy(aplicableSensors, 
			    original, copy, this)) {
			notifyListeners();
		    }
		}	    
	    }
	} else {
	    if ((original.getObjectState() == IveObject.ObjectState.VALID) &&
                    (!manager.isApplicableSensor(sens, original))) {
		boolean becomeValid = false;
		if (valid == false) {
		    valid = true;
		    becomeValid = true;
		}
		aplicableSensors.add(sens);
		if (manager.updateCopy(aplicableSensors, 
			    original, copy, this) || becomeValid) {
		    notifyListeners();
		}	    
	    }
	}
    }
    
    public void changeSensors(List<Sensor> sens) {	

        super.changeSensors(sens);
        
        if (original == null) {
            return;
        }
        
        aplicableSensors.clear();
        
	for (Sensor s: sensors) {
	    if (manager.isApplicableSensor(s, original)) {
		aplicableSensors.add(s);
	    }
	}
	if (aplicableSensors.isEmpty()) {
	    valid = false; //object is not visible by any sensor
	} else {
	    valid = true;	
	}
	manager.updateCopy(aplicableSensors, 
                original, copy, this);
	
	notifyListeners();
    }     
    
    /**
     * Signalization that original object has changed.
     * Update copy (change in one attribute, if possible, 
     * or update whole object) and notify listeners.
     *
     * @param initiator object that was changed - could be AttributeValue or
     *	whole IveObject - algorithm depends on it
     */
    public void changed(Hook initiator) {
	if (initiator instanceof IveObject) {
	    IveObject original = (IveObject) initiator;
	    if (manager.updateCopy(aplicableSensors, original, copy, this)) {
		notifyListeners();	    		    	
	    }
	} else if (initiator instanceof AttributeValue) {
	    AttributeValue original = (AttributeValue) initiator;
	    copy.changeAttribute(original.getName(), original);	    
	    notifyListeners();
	} else {
	    Log.warning("Unknown initiator of change in QTUpdate.");
	}
    }
    
    /** 
     * Called when object moves to new position. Re-count the set of applicable
     * sensors and updates copy according to possible changes or makes object 
     * ivalid if it's not visible by any sensor.
     * 
     * @param newPos new position where object moves
     */
    void moveObject(WayPoint newPos) {
	boolean change = false;
	for (Sensor s: sensors) { //add new one
	    if (!aplicableSensors.contains(s) &&
		    manager.isApplicableSensor(s, original)) {
		if (aplicableSensors.isEmpty()) {
		    valid = true;
		}
		aplicableSensors.add(s);
		change = true;		
	    }
	}
        // We have something that is called ConcurrentModificationException
        // and it is evil. It hunts down newbie programmers!     Ondra
        for (Iterator i = aplicableSensors.iterator(); i.hasNext();) {
            // Remove old one
            Sensor s = (Sensor) i.next();
            
	    if (!manager.isApplicableSensor(s, original)) {
		i.remove();
		change = true;
	    }
	    if (aplicableSensors.isEmpty() && change) {
		makeInvalid();		
		notifyListeners();
		return;
	    }
        }
	if (change) {
	    changed(ObjectMap.instance().getObject(copy.getId()));
	}
    }
    
    /**
     * Called when object is removed from Manager of Senses or 
     * when it moves outside from the range of all applicable sensors.
     */
    void makeInvalid() {
	valid = false;
    }
    
    /** 
     * This method is used when deleting this object to unregister from
     * all registered hooks.
     * @param initiator Initiator of the cancellation. Only if "this" is passed,
     *        there will be an effect.
     */
    public void canceled(Hook initiator) {
        if (initiator == this) {
            if (original != null) {
                original.unregisterListener(this);
                for (AttributeValue attr : original.getAllAtributes()) {
                    attr.unregisterListener(this);
                }
            }
        }
    }
    
}
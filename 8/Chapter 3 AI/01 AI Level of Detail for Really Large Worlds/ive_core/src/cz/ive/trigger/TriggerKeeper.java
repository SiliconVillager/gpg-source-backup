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
 
package cz.ive.trigger;


import cz.ive.IveApplication;
import cz.ive.evaltree.ExprDAG;
import cz.ive.evaltree.StrictExprDAG;
import cz.ive.logs.Log;
import cz.ive.sensors.Sensor;
import cz.ive.simulation.Updateable;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This class is used to keep triggers. All events of inserted triggers are
 * delayed until update() method is called
 * @author thorm
 */
public class TriggerKeeper implements Updateable, java.io.Serializable {

    /**
     * Used to store EvalTreeTriggers
     */
    ExprDAG evaltreeStorage;
    
    /**
     * Used to store other types of triggers
     */
    Set<Trigger> genericTriggers;
    
    /** Creates a new instance of TriggerKeeper */
    public TriggerKeeper() {
        if (IveApplication.instance().useDAG) {
            evaltreeStorage = new StrictExprDAG();
            // evaltreeStorage=new RelaxedExprDAG();
        }
        genericTriggers = new HashSet<Trigger>();
        
    }
    
    /**
     * Insert instantiated trigger t.
     * If t is instance of EvalTreeTrigger, DAG is used. Otherwise DelayedTrigger
     * wrapping t is created and stored.
     * @param t When you pass t into this function you can not use it. Use
     *          returned trigger instead of it
     * @return Reference to the new trigger wrapping old one
     */
    public Trigger insertTrigger(Trigger t) {
        
        if ((evaltreeStorage != null) && (t instanceof EvalTreeTrigger)) {
            ((EvalTreeTrigger) t).insertIntoDAG(evaltreeStorage);
            return t;
        } else {
            genericTriggers.add(t);
        }
        return t;
    }
    
    /**
     * Removes and deletes given trigger
     * @param t Trigger obtained by calling insertTrigger method
     * @return true on succes
     */
    public boolean removeTrigger(Trigger t) {
        t.delete();
        genericTriggers.remove(t);
        return true;
    }
    
    /**
     * @return true if invocation of the update method can change value of some 
     * trigger
     */
    public boolean needUpdate() {
        if (evaltreeStorage != null && evaltreeStorage.needUpdate()) {
            return true;
        }
        return false;
    }

    /**
     * Update values of all stored triggers
     */
    public void update() {
        if (evaltreeStorage != null) {
            evaltreeStorage.update();
        }
    }
    
    /**
     * Change sensors of all stored triggers.
     */
    public void changeSensors(List<Sensor> sensors) {
        if (evaltreeStorage != null) {
            evaltreeStorage.changeSensors(sensors);
        }
        for (Trigger t:genericTriggers) {
            t.changeSensors(sensors);
        }
    }
    
    /**
     *  for debugging purposes
     */
    public void dump(File f) {
        if (evaltreeStorage != null) {
            evaltreeStorage.dump(f);
        } else {
            Log.warning("DAG is not used - DAG dump is empty");
        }
    }
    
}

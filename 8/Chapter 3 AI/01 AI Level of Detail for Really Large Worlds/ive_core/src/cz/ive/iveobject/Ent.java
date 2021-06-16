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
 
package cz.ive.iveobject;


import cz.ive.exception.HookNotPlannedException;
import cz.ive.genius.Genius;
import cz.ive.logs.Log;
import cz.ive.messaging.*;
import cz.ive.simulation.*;
import java.io.Serializable;


/**
 * This IveObject represents a pair actor,genius. The genius drives the actor.
 *
 * @author thorm
 */
public class Ent extends IveObjectImpl {
    
    /**
     * Exp. time to the genius activation.
     */
    protected static final long ACTIVATION_TIME = 1;
    
    /**
     * Diversiti of the activation time.
     */
    protected static final long ACTIVATION_TIME_DIVERSITY = 999;
    
    /**
     * Content of the state attribute if the actor is standing
     */
    public static final int STANDING = 0;
    
    /**
     * Content of the state attribute if the actor is sleeping
     */
    public static final int SLEEPING = 1;
    
    /**
     * Content of the state attribute if the actor is sitting
     */
    public static final int SITTING = 2;
    
    /** The genius associated with this Ent */
    protected Genius genius;
    
    /** Activation calendar hook. */
    protected Hook activationHook;
    
    /** Activation Listener */
    protected Listener activationListener;
    
    /** Is the genius activated? */
    protected boolean isGeniusActivated = false;
    
    /**
     * Creates a new instance of Ent
     *
     * @param objectId Id of the actor.
     */
    public Ent(String objectId) {
        super(objectId);
    }
    
    /**
     * Creates a new instance of Ent
     *
     * @param objectId Id of the actor.
     * @param objCls object class of the actor.
     */
    public Ent(String objectId, ObjectClass objCls) {
        super(objectId, objCls);
    }
    
    /**
     * Sets new genius of Ent.
     * @param genius new genius
     */
    public void setGenius(Genius genius) {
        this.genius = genius;
    }
    
    /**
     * Change state of the object and associated genius.
     * The genius is activated when the state of the object changes to VALID 
     * and deactivated if the state changes to NOT_EXIST
     */
    public void setObjectState(ObjectState state) {
        super.setObjectState(state);
        switch (state) {
        case VALID:
            if (!isGeniusActivated) {
                Log.addMessage("Planned activation of the genius.", Log.FINE,
                        Ent.this, null, getPosition());
                activationHook = CalendarPlanner.instance().plan(
                        ACTIVATION_TIME + 
                        (long)(Math.random()*ACTIVATION_TIME_DIVERSITY));
                if (activationListener == null) {
                    activationListener = new ActivationListener();
                }
                activationHook.registerListener(activationListener);
                isGeniusActivated = true;
            }
            break;

        case NOT_EXIST:
            if (isGeniusActivated) {
                Log.addMessage("Deactivation of the genius", Log.FINE, Ent.this,
                        null, getPosition());
                genius.deactivate();
                isGeniusActivated = false;
                if (activationHook != null) {
                    activationHook.unregisterListener(activationListener);
                    try {
                        CalendarPlanner.instance().cancelHook(activationHook);
                    } catch (HookNotPlannedException ex) {// Then it is no problem.
                    }
                    activationHook = null;
                }
            }
            break;
        }
    }
    
    /** Helper listener class */
    public class ActivationListener implements Listener, Serializable {
        public void changed(Hook initiator) {
            Log.addMessage("Activation of the genius", Log.FINE, Ent.this, null,
                    getPosition());
            genius.activate();
            activationHook = null;
        }
        
        public void canceled(Hook initiator) {// Calendar hook never cancels
        }
        
        public String toString() {
            return "Genius activation: " + genius.getId();
        }
    }
}

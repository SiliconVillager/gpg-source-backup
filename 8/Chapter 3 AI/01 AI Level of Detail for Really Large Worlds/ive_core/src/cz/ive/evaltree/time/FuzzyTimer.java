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
 
package cz.ive.evaltree.time;


import cz.ive.messaging.Hook;
import cz.ive.messaging.Listener;
import cz.ive.process.Substitution;
import cz.ive.simulation.CalendarPlanner;
import java.util.List;
import cz.ive.valueholders.FuzzyValueHolder;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.sensors.*;
import cz.ive.evaltree.leaves.*;


/*
 * This node starts to have true value after the certain amount of time from the
 * instantiation.
 *
 * @author thorm
 */
public class FuzzyTimer extends Constant<FuzzyValueHolderImpl>
    implements Listener {
    
    /**
     * Time in miliseconds
     */
    private long time;
    
    /**
     * The Timer can be inaccurate
     */
    private long inaccuracy;
    
    /**
     * Hook returned by calendar
     */
    private Hook hook;
    
    /**
     * Create a new FuzzyTimer instance
     *
     * @param time in miliseconds
     * @param inaccuracy in miliseconds
     */
    public FuzzyTimer(long time, long inaccuracy) {
        value = new FuzzyValueHolderImpl();
        value.value = FuzzyValueHolder.False;
        value.validate();
        this.time = time;
        this.inaccuracy = inaccuracy;
        
    }
    
    /**
     * Plan at the calendar. The changed method will be invoked in the interval
     * [time-inaccuracy, time+inaccuracy].
     */
    public void instantiate(Substitution s, List<Sensor> a) {
        hook = CalendarPlanner.instance().plan(
                time + (long) (2 * Math.random() * inaccuracy) - inaccuracy);
        hook.registerListener(this);
    }
    
    public void canceled(cz.ive.messaging.Hook initiator) {
        hook.unregisterListener(this);
    }

    public void uninstantiate() {
        hook.unregisterListener(this);
        hook = null;
    }

    /**
     * Set the value to be True.
     */
    public void changed(cz.ive.messaging.Hook initiator) {
        value.value = FuzzyValueHolder.True;
        hook.unregisterListener(this);
        notifyListeners();
    }
    
    public int contentHashCode() {
        return this.hashCode();
    }
    
    public boolean contentEquals(Object obj) {
        return obj == this;
    }
    
    public String toString() {
        return "Fuzzy timer: DAG id = " + String.valueOf(getDagIdentifier());
    }
}

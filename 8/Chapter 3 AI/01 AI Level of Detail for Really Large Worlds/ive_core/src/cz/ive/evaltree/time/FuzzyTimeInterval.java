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


import cz.ive.evaltree.Expr;
import cz.ive.evaltree.dependentnodes.BinaryNode;
import cz.ive.process.Substitution;
import cz.ive.simulation.CalendarPlanner;
import java.util.List;
import cz.ive.valueholders.FuzzyValueHolder;
import cz.ive.evaltree.valueholdersimpl.*;
import cz.ive.messaging.Hook;
import cz.ive.sensors.*;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * This node value is true in the certain time interval
 * 
 * @author thorm
 */


public class FuzzyTimeInterval 
        extends BinaryNode<FuzzyValueHolderImpl, IntValueHolderImpl> {
    
    /**
     * Hook obtained from calendar
     */
    private Hook hook;
    
    /**
     * Calendar objects for usage in methods
     */
    private Calendar actualTime, nextTime;
    
    /**
     * Time units used to express interval
     */
    private int field;
    
    /**
     * Number of units that changes the closest bigger unit
     * This is 60 for field = minutes
     */
    private int fieldPeriod;
    
    /**
     * List of units that must be cleaned to get time of the next change.
     */
    private int[] toClean;
    
    /**
     * If the interval is in Hours we must clean minutes, seconds and 
     * miliseconds to get simulation time of the next change
     */
    private static int[] toCleanForHour = new int[] {
        Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND
    };
    
    /**
     * If the interval is in days of week we must clean hours, minutes, seconds 
     * and miliseconds to get simulation time of the next change
     */    
    private static int[] toCleanForDay = new int[] {
        Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND
    };
    
    /**
     * Create a new FuzzyTimeInterval instance
     *
     * @param p1 begin of the interval
     * @param p2 end of the interval
     * @param field java.util.Calendar constant that determines the units used
     *              to express begin and end.
     */
    public FuzzyTimeInterval(Expr<IntValueHolderImpl, ?> p1, 
                             Expr<IntValueHolderImpl, ?> p2,
                             int field) {
        super(p1, p2);
        value = new FuzzyValueHolderImpl();
        this.field = field;
        
        switch (field) {
        case java.util.Calendar.DAY_OF_WEEK:
            toClean = toCleanForDay;
            break;

        case java.util.Calendar.HOUR_OF_DAY:
            toClean = toCleanForHour;
            break;

        default:
            toClean = toCleanForHour;
        }

        actualTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        nextTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        fieldPeriod = actualTime.getMaximum(field) + 1;
    }
    
    /**
     * Needed to implement abstract method
     */
    protected void updateInstantiation(Hook child) {}
    
    /**
     * Set the value to be true if actual simulation time is inside of the 
     * specified interval. The value is false otherwise.
     * Compute the time remaining to the next change and plan the calendar.
     * 
     *
     * The value is defined iff both operand values are defined
     */
    protected void updateValue() {
        long updateMilis = updateValueAndGetNextChange();

        if (hook != null) {
            CalendarPlanner.instance().replan(hook, updateMilis, false);
        }
    }
    
    /**
     * Set the value to be true if actual simulation time is inside of the 
     * specified interval. The value is false otherwise.
     * 
     * @return The time remaining to the next change in milis
     *
     */
    protected long updateValueAndGetNextChange() {
        long updateMilis;

        actualTime.setTimeInMillis(
                CalendarPlanner.getInstance().getSimulationTime());
        if (rightValue.isDefined && leftValue.isDefined) {
            int begin = leftValue.value;
            int end = rightValue.value;
            
            int actualTimeField = actualTime.get(field);
            
            if (isInInterval(actualTimeField, begin, end)) {
                value.value = FuzzyValueHolder.True;
                updateMilis = getNextChangeDistance(end);
            } else {
                value.value = FuzzyValueHolder.False;
                updateMilis = getNextChangeDistance(begin);
                
            }
            value.isDefined = true;
            return updateMilis;
            
        } else {
            value.isDefined = false;
            return 0;
        }
    }
    
    /**
     * Instantiate operands, set the value and plan event to emerge when the
     * value should be changed
     */
    public void instantiate(Substitution s, List<Sensor> a) {
        super.instantiate(s, a);
        long updateMilis = updateValueAndGetNextChange();

        hook = CalendarPlanner.instance().plan(updateMilis);
        hook.registerListener(this);
    }
    
    public void canceled(cz.ive.messaging.Hook initiator) {
        hook.unregisterListener(this);
    }
    
    /**
     * Two time intervals are never equal
     */
    public boolean contentEquals(Object obj) {
        return false;
    }
    
    public void uninstantiate() {
        if (hook != null) {
            hook.unregisterListener(this);
        }
        super.uninstantiate();
    }
    
    /**
     * Decide whether the value is within the specified interval.
     * This enables situation when begin>end. ( intervals containing midnight )
     *
     * @param begin begin of the interval
     * @param end end of the interval
     * @param value we want to know if this is within the interval
     * @return true if the value is in the interval specified by begin and end
     */
    public static boolean isInInterval(int value, int begin, int end) {
        boolean overMidnight = begin > end;

        if (overMidnight) {
            return (begin <= value || value < end);
        } else {
            return (begin <= value && value < end);
        }
    }
    
    public String toString() {
        return "Fuzzy interval timer: DAG id = "
                + String.valueOf(getDagIdentifier());
    }
    
    /**
     * Compute time remaining to the next change of state
     * 
     * @param fieldValue time of the next change in field units.
     * @return time remaining to the next change in milis
     */
    protected long getNextChangeDistance(int fieldValue) {
        long simulTime = CalendarPlanner.getInstance().getSimulationTime();

        nextTime.setTimeInMillis(simulTime);
        int actualFieldValue = nextTime.get(field);
        int fieldDelta = fieldValue - actualFieldValue;

        fieldDelta = (fieldDelta < 0) ? fieldDelta + fieldPeriod : fieldDelta;
        nextTime.add(field, fieldDelta);
        for (int i = 0; i < toClean.length; i++) {
            nextTime.set(toClean[i], 0);
        }
        long ret = nextTime.getTimeInMillis() - simulTime;

        assert ret > 0;
        return ret;
    }
}

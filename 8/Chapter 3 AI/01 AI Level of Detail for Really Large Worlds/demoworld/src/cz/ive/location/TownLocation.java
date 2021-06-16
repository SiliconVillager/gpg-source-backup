/* 
 *
 * IVE Demo World
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
 
package cz.ive.location;


import cz.ive.evaltree.time.FuzzyTimeInterval;
import cz.ive.exception.ObjectRegistrationFailedException;
import cz.ive.iveobject.IveId;
import cz.ive.iveobject.IveObject;
import cz.ive.iveobject.IveObjectImpl;
import cz.ive.iveobject.ObjectClassTree;
import cz.ive.iveobject.ObjectMap;
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.iveobject.attributes.AttrObject;
import cz.ive.simulation.CalendarPlanner;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Java implementation of the location House from Estate.xml
 * HouseLocation modifies attributes of inherent miners that determines time
 * plan and addreses. It enables to miners created from the same template 
 * to have diferent dayplan , house address and work addres.
 *
 * HouseLocation gives advice to the standard object placement mechanism during 
 * expansion so that actors apear on the right places in the right time 
 * according to their time plan
 * @author thorm
 */
public class TownLocation extends SimpleArea {
    
    String[] estates = { "Estate1", "Estate2", "Estate3", "Estate4"};
    String[] houses = { "House1", "House2", "House3"};
    String[] mines = { "Mine1", "Mine2", "Mine3", "Mine4", "Mine5"};
    int minerCounter;
    int waiterCounter;
    
    /** Creates a new instance of TownLocation */
    public TownLocation(String objectId, WayPoint parent, float[] realPosition,
            Kind kind, ExpansionInfo info)
        throws ObjectRegistrationFailedException {
        super(objectId, parent, realPosition, kind, info);
    }
  
    /**
     * Add special attributes to the given object if the object is member of
     * Object class /person/Miner,/person/Waiter or /person/Singer.
     *
     * @param o Object to be modified 
     */
    protected void addSpecialLocationAttributes(IveObject o) {
        if (ObjectClassTree.instance().getObjectClass("/person/Miner").isInside(
                o)) {
            addMinerAttrs(o);
        }
        if (ObjectClassTree.instance().getObjectClass("/person/Waiter").isInside(
                o)) {
            addWaiterAttrs(o);
        }
        if (ObjectClassTree.instance().getObjectClass("/person/Singer").isInside(
                o)) {
            addSingerAttrs(o);
        }
    }
    
    /**
     * Modify attributes of waiter
     * Waiter sleeps at Estate4, House2 and works from 10:00 to 2:00
     *
     * @param o Object to be modified 
     */
    protected void addWaiterAttrs(IveObject o) {
        String estate = getId() + IveId.SEP + "Estate4";

        o.changeAttribute("address.estate",
                new AttrObject(new IveObjectImpl(estate)));
        o.changeAttribute("address.house",
                new AttrObject(new IveObjectImpl(estate + IveId.SEP + "House2")));
        setPersonelWorkAttributes(o);
    }

    /**
     * Modify attributes of waiter
     * Waiter sleeps at Estate4, House3 and works from 10:00 to 2:00
     *
     * @param o Object to be modified 
     */
    protected void addSingerAttrs(IveObject o) {
        String estate = getId() + IveId.SEP + "Estate4";

        o.changeAttribute("address.estate",
                new AttrObject(new IveObjectImpl(estate)));
        o.changeAttribute("address.house",
                new AttrObject(new IveObjectImpl(estate + IveId.SEP + "House3")));
        setPersonelWorkAttributes(o);
    }
    
    /**
     * Set the dayplan attributes of pub personel.
     * Works from 10:00 to 2:00
     *
     * @param o Object to be modified 
     */
    protected void setPersonelWorkAttributes(IveObject o) {
        setWorkAttributes(o, 10, 2);
        setSleepAttributes(o, 2, 10);
    }
    
    /**
     * Set work attributes of given object
     *
     * @param o Object to be changed
     * @param begin value of throws <CODE>workBegin</CODE> attribute
     * @param end value of throws <CODE>workEnd</CODE> attribute
     *
     */
    protected void setWorkAttributes(IveObject o, int begin, int end) {
        o.changeAttribute("workBegin", new AttrInteger(begin));
        o.changeAttribute("workEnd", new AttrInteger(end));
    }
    
    /**
     * Set sleep attributes of given object
     *
     * @param o Object to be changed
     * @param begin value of throws <CODE>sleepBegin</CODE> attribute
     * @param end value of throws <CODE>sleepEnd</CODE> attribute
     *
     */    
    protected void setSleepAttributes(IveObject o, int begin, int end) {
        o.changeAttribute("sleepBegin", new AttrInteger(begin));
        o.changeAttribute("sleepEnd", new AttrInteger(end));
    }
    
    /**
     * Regulary split the given miners into two shifts and assign them locations
     * to sleep and to work
     * 
     * @param o Object to be changed
     */
    protected void addMinerAttrs(IveObject o) {
        String estate = getId() + IveId.SEP
                + estates[((minerCounter / 2) / houses.length) % estates.length];

        o.changeAttribute("address.estate",
                new AttrObject(new IveObjectImpl(estate)));
        o.changeAttribute("address.house",
                new AttrObject(
                new IveObjectImpl(
                        estate + IveId.SEP
                        + houses[((minerCounter / 2) % houses.length)])));
        o.changeAttribute("address.work",
                new AttrObject(
                new IveObjectImpl(
                        getId() + IveId.SEP
                        + mines[(minerCounter) % mines.length])));
        if (minerCounter < 10) {
            setWorkAttributes(o, 6, 14);
            setSleepAttributes(o, 22, 6);
        } else {
            setWorkAttributes(o, 14, 22);
            setSleepAttributes(o, 2, 10);
        }
        minerCounter = (minerCounter+1)%20;
        
    }
    
    /**
     * Return the special place for members of ObjectClass <CODE>/person</CODE>
     *
     * @param object object that should be placed to the location
     * @return place obtained from the day plan of the person and address attribute
     *         or <CODE>null</CODE> for objects that are not member of /person class
     */
    protected IveObject findObjectPlaceSpecial(IveObject object) {
        if (ObjectClassTree.instance().getObjectClass("/person").isInside(object)) {
            return findPlaceForPerson(object);
        }
        return null;
    }
    
    protected WayPoint findPlaceForPerson(IveObject o) {
        AttrObject addressEstate = (AttrObject) o.getAttribute("address.estate");
        AttrObject addressWork = (AttrObject) o.getAttribute("address.work");
        
        int workBegin = ((AttrInteger) o.getAttribute("workBegin")).getValue();
        int workEnd = ((AttrInteger) o.getAttribute("workEnd")).getValue();
        
        int sleepBegin = ((AttrInteger) o.getAttribute("sleepBegin")).getValue();
        int sleepEnd = ((AttrInteger) o.getAttribute("sleepEnd")).getValue();
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        c.setTimeInMillis(CalendarPlanner.getInstance().getSimulationTime());
        int hour = c.get(Calendar.HOUR_OF_DAY);

        if (FuzzyTimeInterval.isInInterval(hour, workBegin, workEnd)) {
            if (addressWork == null) {
                return (WayPoint) ObjectMap.instance().getObject(
                        getId() + IveId.SEP + "Pub");
            }
            IveObject id = addressWork.getValue();

            if (id == null) {
                return null;
            }
            return (WayPoint) ObjectMap.instance().getObject(id.getId());
        } else if (FuzzyTimeInterval.isInInterval(hour, sleepBegin, sleepEnd)) {
            IveObject id = addressEstate.getValue();

            if (id == null) {
                return null;
            }
            return (WayPoint) ObjectMap.instance().getObject(id.getId());
        }
        return (WayPoint) ObjectMap.instance().getObject(
                getId() + IveId.SEP + "Pub");
                
    }
    
}

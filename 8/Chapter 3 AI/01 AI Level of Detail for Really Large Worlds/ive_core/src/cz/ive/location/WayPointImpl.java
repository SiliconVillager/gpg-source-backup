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
 
package cz.ive.location;

import java.util.List;
import cz.ive.iveobject.*;
import cz.ive.exception.*;
import java.util.Vector;
import cz.ive.lod.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * An implementation of WayPoit.
 * WayPoint can be either valid or invalid.
 * Valid WayPoint is a real IveObject registered in the ObjectMap.
 * Invalid WayPoint is a copy of valid WayPoint used just to describe a position
 * of a valid (may be not currently existing) WayPoint.
 *
 * @author pavel
 */
public class WayPointImpl extends IveObjectImpl implements WayPoint {
    
    /** lod related state of this location. */
    protected LocationState locationState;
    
    /** The real position of the waypoint */
    protected float[] coordinates;
    
    /** Joints to the neighbour waypoints */
    protected List<Joint> neighbours;
    
    /** Objects located on this waypoint */
    protected List<IveObject> objects;
    
    /** LOD influences */
    protected Influence[] influences;
    
    /** State of the WayPoint's space */
    protected SpaceState spaceState;
    
    /** 
     * Sets some well-known attributes 
     * @param parent The parent WayPoint
     */
    protected void setAttributes(WayPoint parent) {
        int lod = 0;
        
        if (parent != null) {
            setLod(parent.getLod()+1);
            setPosition(parent);
        } else {
            setLod(0);
        }
    }

    /** 
     * Creates a new instance of empty (invalid) WayPointImpl. 
     * Does not take care of the kind, any path can go to it (the kind
     * is set to null).
     * @param objectId id of the WayPoint
     * @param realPosition coordinates of the new WayPoint
     */
    public WayPointImpl(String objectId, float[] realPosition) {
        super(objectId);
        spaceState = SpaceState.EMPTY;
        coordinates = realPosition.clone();
    }

    /** 
     * Creates a new instance of empty (invalid) WayPointImpl. 
     * @param objectId id of the WayPoint
     * @param realPosition coordinates of the new WayPoint
     * @param kind kind of the WayPoint
     */
    public WayPointImpl(String objectId, float[] realPosition, Kind kind) {
        super(objectId);
        coordinates = realPosition.clone();
        setKind(kind);
        spaceState = SpaceState.EMPTY;
    }
 
    /**
     * Creates a new instance of filled (valid) WayPointImpl.
     * @param objectId id of the WayPoint
     * @param parent Parent area
     * @param realPosition coordinates of the new WayPoint
     * @param kind kind of the WayPoint
     */
    public WayPointImpl(String objectId, WayPoint parent, float[] realPosition, 
            Kind kind) throws ObjectRegistrationFailedException {
                super(objectId);
                initialize();
                coordinates = realPosition.clone();
                this.neighbours = neighbours;
                this.influences = null;
                this.kind = kind;
                setAttributes(parent);
                locationState = LocationState.NOT_EXIST;
                objects = new Vector<IveObject>();
                spaceState = SpaceState.EMPTY;
    }

    /** 
     * Creates a new instance of filled (valid) WayPointImpl 
     * Does not take care of the kind, any path can go through it (the kind
     * is set to null).
     * This constructor is for backward compatibility, should not be used any 
     * more.
     * @param objectId id of the WayPoint
     * @param parent Parent area
     * @param realPosition coordinates of the new WayPoint
     * @param influences Lod influences
     */
    public WayPointImpl(String objectId, WayPoint parent, float[] realPosition, 
            Influence[] influences) 
            throws ObjectRegistrationFailedException {
                super(objectId);
                initialize();
                coordinates = realPosition.clone();
                this.neighbours = neighbours;
                this.influences = influences;
                this.kind = null;
                setAttributes(parent);
                locationState = LocationState.NOT_EXIST;
                objects = new Vector<IveObject>();
                spaceState = SpaceState.EMPTY;
    }
    
    /** 
     * Creates a new instance of filled (valid) WayPointImpl 
     * This constructor is for backward compatibility, should not be used any 
     * more.
     * @param objectId id of the WayPoint
     * @param parent Parent area
     * @param realPosition coordinates of the new WayPoint
     * @param influences Lod influences
     * @param kind kind of the WayPoint
     */
    public WayPointImpl(String objectId, WayPoint parent, float[] realPosition, 
            Influence[] influences, Kind kind) 
            throws ObjectRegistrationFailedException {
                super(objectId);
                initialize();
                coordinates = realPosition.clone();
                this.neighbours = neighbours;
                this.influences = influences;
                this.kind = kind;
                setAttributes(parent);
                locationState = LocationState.NOT_EXIST;
                objects = new Vector<IveObject>();
                spaceState = SpaceState.EMPTY;
    }
    
    public float[] getRealPosition() {
        return coordinates;
    }
    
    public List<Joint> getNeighbours() {
        return neighbours;
    }
    
    public boolean matchKinds(Kind wpkind, Kind skind) {
        if ((wpkind == null) || (skind == null)) {
            return true;
        }

        return wpkind.match(skind);
    }
    
    public boolean matchKind(Kind kind) {
        return matchKinds(getKind(), kind);
    }
    
    public List<Joint> getNeighbours(Kind kind) {
        Vector<Joint> found;
        Joint neighbour;
        
        if (kind == null) {
            return getNeighbours();
        }
        
        found = new Vector<Joint>();
        
        for (int i=0; i<neighbours.size(); i++) {
            neighbour = neighbours.get(i);
            if (matchKinds(neighbour.target.getKind(), kind)) {
                found.add(neighbour);
            }
        }
        
        return found;
    }

    
    /**
     * Objects located on this WayPoint.
     * If this is not a valid WayPoint, this will be empty.
     * @return list of real objects
     */
    public List<IveObject> getObjects() {
        return objects;
    }

    public void addObject(IveObject slave) {
        super.addObject(slave);

        if ((isSubstantial()) && (spaceState != spaceState.GOING_EMPTY)) {
            for (IveObject o : getObjects()) {
                if (o.isSubstantial()) {
                    spaceState = SpaceState.OCCUPIED;
                    break;
                }
            }
        }
    }
    
    public void removeObject(IveObject slave) {
        super.removeObject(slave);

        if ((isSubstantial()) && (spaceState != spaceState.GOING_OCCUPIED)) {
            for (IveObject o : getObjects()) {
                if (o.isSubstantial()) {
                    return;
                }
            }
            spaceState = SpaceState.EMPTY;
        }
    }
    
    public boolean reserveSpace() {
        if (spaceState != SpaceState.EMPTY) {
            return false;
        }
        spaceState = SpaceState.GOING_OCCUPIED;
        return true;
    }
    
    public void unreserveSpace() {
        if (spaceState == SpaceState.GOING_OCCUPIED) {
            spaceState = SpaceState.EMPTY;
        }
    }

    public void leavingSpace() {
        if (spaceState != SpaceState.EMPTY) {
            spaceState = SpaceState.GOING_EMPTY;
        }
    }
    
    public void unleavingSpace() {
        if (spaceState == SpaceState.GOING_EMPTY) {
            spaceState = SpaceState.OCCUPIED;
        }
    }

    /**
     * Finds the WayPoint suitable to place the Object on.
     * The WayPoint will be the most detailed existing child of the 'where'
     * WayPoint, will have matching kind with the object and will be connected
     * to the 'from' WayPoint (if 'from'is null, it won't be considered).
     *
     * @param where WayPoint in which we are searching
     * @param object Object we want to place
     * @param from WayPoint we are comming from, can be null
     * @param to WayPoint we are comming to, can be null
     * @return WayPoint to place the object on, null if no suitable WayPoint
     * was found.
     */
    protected WayPoint findPlace(WayPoint where, IveObject object, 
            WayPoint from, WayPoint to) {
        
        if (where == null) {
            return null;
        }
        
        if ((where instanceof Area) 
                && (where.getLocationState() == LocationState.EXPANDED)) {
            
            WayPoint place;
            List<WayPoint> children;
            
            if ((to != null) && (to.isParent(where)) && 
                    (!to.getId().toString().equals(where.getId().toString()))) {
                /* get the child on the way to "to" */
                children = new Vector<WayPoint>();
                children.add((WayPoint) where.getChildPreceeding(to.getId()));
            } else {
                if (from != null) {
                    /* "to" cannot help, "from" will help. Get all children */
                    children = ((Area) where).getWayPoints(object.getKind());
                } else {
                    /* neither "to" nor "from" can help. get the object place */
                    place = ((Area) where).findObjectPlace(object);
                    if (place != null) {
                        children = new Vector<WayPoint>();
                        children.add(place);
                    } else {
                        /* no place found, place it anywhere */
                        children = ((Area) where).getWayPoints(
                                object.getKind());
                    }
                }
            }
            
            while (!children.isEmpty()) {
                int index = (int) (Math.random() * children.size());
                place = findPlace(children.remove(index), object, from, to);
                if (place != null) {
                    return place;
                }
            }
            return null;

        } else {
            if (where.matchKind(object.getKind())) {
                if ((object.isSubstantial()) && 
                        ((where.getSpaceState() == SpaceState.OCCUPIED) ||
                         (where.getSpaceState() == SpaceState.GOING_EMPTY))) {
                    return null;
                }
                if ((from == null) || (from.isParent(where)) || 
                        (where.isParent(from))) {
                    return where;
                }
                List<Joint> neighbours = where.getNeighbours();
                WayPoint wp;
                for (Iterator<Joint> i = neighbours.iterator(); i.hasNext(); ) {
                    wp = i.next().target;
                    if (from.isParent(wp) || wp.isParent(from)) {
                        return where;
                    }
                }
                
            }
        }
        
        return null;
    }
    
    public boolean placeObject(IveObject object, WayPoint from, WayPoint to) {

        WayPoint place = findPlace(this, object, from, to);

        if (place == null) {
            return false;
        }
        
        object.setPosition((to != null) ? to : place);

        place.addObject(object);

        return true;
    }
    
    /**
     * Getter for LOD influences. This affects LOD level of target loactions
     * if there is a holdback on this WayPoint.
     * @return list influences
     */
    public Influence[] getInfluences() {
        return influences;
    }

    public void setNeighbours(List<Joint> neighbours) {
        this.neighbours = neighbours;
    }

    public void setInfluences(Influence[] influences) {
        this.influences = influences;
    }
    
    
    public void addNeighbour(Joint neighbour) {

        if (this.neighbours == null) {
            this.neighbours = new Vector<Joint>();
        }
        
        this.neighbours.add(neighbour);
    }

    /**
     * Recursively calling method to realize the getSubtree method.
     * Recursively backtrakcs the tree of WayPoints to the depth
     * and builds the set of their names.
     * @param wp Root of the tree to backtrack
     * @param lod Lod interval of the result
     * @param set Incrementally builded result set
     */
    protected void buildSubtree(WayPoint wp, LOD lod, Set<String> set) {
        if ((wp.isPhantom()) || (wp.getLod() > lod.getMax())) {
            return;
        }
        
        if (wp.getLod() >= lod.getMin()) {
            set.add(wp.getId());
        }
        
        if (wp.getLod() < lod.getMax()) {
            if (wp instanceof Area) {
                
                WayPoint[] children;
                int child;
                Area a = (Area) wp;
                
                children = a.getWayPoints();
                
                if (children == null) {
                    return;
                }
                
                for (child=0; child<children.length; child++) {
                    buildSubtree(children[child], lod, set);
                }
            }
        }
    }

    /**
     * Implements method from WayPoint interface.
     * Runs up through the tree while it reaches the minimum of the given lod
     * interval. From that point backtracks the tree to the maximum of the given
     * lod interval and builds the set.
     * @param lod The lod interval of the result
     * @return Set of all names of accessible WayPoints
     */
    public Set<String> getSubtree(LOD lod) {
        HashSet<String> set;
        WayPoint wp;
        Area a;
        
        set = new HashSet<String>();
        if  ( lod==null ) return set;
        
        /* run up through the tree */
        wp = this;
        while ((wp.getPosition() != null) && (wp.getLod() > lod.getMin())) {
            wp = wp.getPosition();
        }
        
        
        
        /* build the list */
        buildSubtree(wp, lod, set);
        
        return set;
    }


    public LocationState getLocationState() {
        return locationState;
    }
    
    public void setLocationState(LocationState state) {
        if ((locationState != LocationState.NOT_EXIST) &&
            (state == LocationState.NOT_EXIST)) {
                ObjectMap.instance().unregister(getId());
        } else if ((locationState == LocationState.NOT_EXIST) && 
            (state != LocationState.NOT_EXIST)) {
                ObjectMap.instance().register(this);
        }
        locationState = state;
    }

    public void expand() throws NoObjectPlaceException {
        if (this instanceof Area) {
            ((Area) this).expand();
        }
    }
    
    public void shrink() {
        if (this instanceof Area) {
            ((Area) this).shrink();
        }
    }
    
    public WayPoint getPhantom() {
       
        return new WayPointImpl(getId(), getRealPosition(), getKind());
    }
 
    public float getDistance(WayPoint target) {

        float c1[], c2[];
        double distance = 0;
        
        c1 = getRealPosition();
        c2 = target.getRealPosition();
        for (int i=0; i < c1.length; i++) {
            distance += (c2[i] - c1[i]) * (c2[i] - c1[i]);
        }
        distance = java.lang.Math.sqrt(distance);
        
        return (float)distance;
    }

    public WayPoint getRootWP(LOD lod) {
	WayPoint root = this;
	while ((root.getPosition() != null) && (root.getLod() > lod.getMin())) {
	    root = root.getPosition();
	}
	return root;
    }

    public SpaceState getSpaceState() {
        if (! isSubstantial()) {
            return SpaceState.EMPTY;
        }

        return spaceState;
    }

    public boolean isNeighbour(WayPoint wayPoint) {
        if (wayPoint.getNeighbours() == null) {
            return false;
        }
        
        for (Joint j : wayPoint.getNeighbours()) { 
            if (j.target == this) {
                return true;
            }
        }
        
        return false;
    }
    
}
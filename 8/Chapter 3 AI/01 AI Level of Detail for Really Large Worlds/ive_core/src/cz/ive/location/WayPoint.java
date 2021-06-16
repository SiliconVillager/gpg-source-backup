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

import java.util.*;
import cz.ive.iveobject.*;
import cz.ive.lod.LOD;
import cz.ive.exception.NoObjectPlaceException;

/**
 * Interface representing single WayPoint in the net
 * of WayPoints which corresponds to single Area.
 * WayPoint can be either valid or invalid.
 * Valid WayPoint is a real IveObject registered in the ObjectMap.
 * Invalid WayPoint is a copy of valid WayPoint used just to describe a position
 * of a valid (may be not currently existing) WayPoint.
 * In the case of invalid WayPoint, the Neighbourhood, Influence and stored
 * Objects are empty. Id and RealPosition should be filled correctly.
 * Validity of WayPoint can be querried by calling isCopy from IveObject 
 * interface
 *
 * @author ondra
 */
public interface WayPoint extends IveObject {
    
    /** States of the space on the WayPoint */
    enum SpaceState {
        /** 
         * The WayPoint is empty - no substantial object is present 
         */
        EMPTY,
        
        /** 
         * The WayPoint is occupied by some substantial object 
         */
        OCCUPIED,
        
        /** 
         * The WayPoint is occupied, but the substantial object is going to 
         * move away 
         */
        GOING_EMPTY,
        
        /**
         * The WayPoint is empty, but some substantial object is going to move
         * here.
         */
        GOING_OCCUPIED,
    }
    
    /**
     * Getter for 2D/3D position in simulated world.
     * @return 2D/3D coordinates
     */
    float[] getRealPosition();
    
    /**
     * Getter for neighbours of this WayPoints. These are instances of class
     * WayPoint if they belongs to the same area and may be phantoms
     * otherwise.
     * @return joints to neighbours of this WayPoint
     */
    List<Joint> getNeighbours();
    
    /**
     * Getter for neighbours of this WayPoints with the given kind. 
     * These are instances of class WayPoint if they belongs to the same area 
     * and may be phantoms otherwise.
     * There are two arrays of kinds, the given one and the neighbour's one.
     * The neighbour is returned if one of the two arrays is null, or at least
     * one number equals in both arrays.
     * @return joints to neighbours of the given kind of this WayPoint
     */
    List<Joint> getNeighbours(Kind kind);

    /**
     * Objects located on this WayPoint.
     * If this is not a valid WayPoint, this will be empty.
     * @return list of real objects
     */
    List<IveObject> getObjects();
    
    /**
     * Places an object to a WayPoint or it's child.
     * The object will be placed to a most detailed existing waypoint, which is
     * a child of this WayPoint, with respect to the WayPoint's kinds,
     * the incoming direction and object places defined for expand.
     * @param object Object to be placed
     * @param from WayPoint from which the object is comming. If it is null,
     *             the direction will not be considered. If it is not null,
     *             the object will be placed to some WayPoint which adjoins the
     *             'from' WayPoint or any it's direct or indirect child.
     *             This means, if no joint exists between the childrens of
     *             this WayPoint and subtree of the 'from' WayPoint, the object
     *             cannot be placed.
     * @param to WayPoint (maybe phantom) to which the object is comming
     * @return true if the object was placed on some WayPoint;
     * false if the object was not placed. The reason can be one of
     * these: object cannot be placed to any WayPoint because of their kinds,
     * no connection to the 'from' WayPoint was found, or their combination.
     */
    boolean placeObject(IveObject object, WayPoint from, WayPoint to);
    
    /**
     * Getter for LOD influences. This affects LOD level of target loactions
     * if there is a holdback on this WayPoint.
     * @return list influences
     */
    Influence[] getInfluences();

    /** 
     * Setter of joints to neighbours
     * @param neighbours Joints to the neighbours
     */
    void setNeighbours(List<Joint> neighbours);

    /**
     * Setter for LOD influences. This affects LOD level of target loactions
     * if there is a holdback on this WayPoint.
     * param influences list of influences
     */
    void setInfluences(Influence[] influences);

    /**
     * Add a joint to another neighbour
     * @param neighbour The new neighbour
     */
    void addNeighbour(Joint neighbour);
    
    /**
     * Getter for all names of WayPoints accessible in subtree, which is 
     * determined by this WayPoint and by the interval of LOD.
     * @param lod The lod interval of the result
     * @return Set of all names of accessible WayPoints
     */
    Set<String> getSubtree(LOD lod);
    
    /**
     * Determines whether the given kind matches the searched kind.
     * The kinds match, if one of the two arrays is null, or at least
     * one number equals in both arrays.
     * @param wpkind Kind of the WayPoint
     * @param skind Searched kind
     * @return true if the kinds match;
     *  false if the kinds do not match
     */
    boolean matchKinds(Kind wpkind, Kind skind);

    /**
     * Determines whether this WayPoints kind matches the searched kind.
     * The kinds match, if one of the two arrays is null, or at least
     * one number equals in both arrays.
     * @param kind Searched kind
     * @return true if the kinds match;
     *  false if the kinds do not match
     */
    boolean matchKind(Kind kind);

    /**
     * Called when LOD of this WayPoint is being increased. An object may want
     * to create some subobjects. Throws NoObjectPlaceException if cannot
     * find a WayPoint to place some object.
     */
    void expand() throws NoObjectPlaceException;
    
    /**
     * Called when LOD of this object is being decreased. An object may want
     * to destroy some subobjects.
     */
    void shrink();

    /**
     * Getter for the lod related state of the location.
     * @return current state of the location
     */
    LocationState getLocationState();
    
    /**
     * Setter for the lod related state of the location.
     * @param state New state of the location
     */
    void setLocationState(LocationState state);
    
    /**
     * Creates a phantom of this WayPoint
     * @return the phantom
     */
    WayPoint getPhantom();
    
    /**
     * Computes the direct distance to the given WayPoint using coordinates.
     * @param target The WayPoint whose distance from this WayPoint will
     *               be coumputed.
     * @return The distance of RealPosition
     */
    float getDistance(WayPoint target);
    
    /**
     * Get root of tree of waypoints according to given LOD view.     
     *      
     * @param lod specify part of waypoints tree
     * @return root waypoint
     */
    WayPoint getRootWP(LOD lod);
    
    /**
     * Checks the WayPoint's space state.
     * @return the current state of the WayPoint's space
     */
    SpaceState getSpaceState();
    
    /** 
     * Reserves the WayPoint's space for object moving to it
     * @return true if the space was successfully reserved
     */
    boolean reserveSpace();
    
    /** 
     * Unreserves the WayPoint's space for object moving to it (it has changed
     * it's mind).
     * 
     */
    void unreserveSpace();

    /**
     * Tells to the WayPoint that the substantial object occupying this WayPoint
     * is going to leave it.
     */
    void leavingSpace();
    
    /**
     * Tells to the WayPoint that the substantial object occupying this WayPoint
     * is no more going to leave it.
     */
    void unleavingSpace();

    /**
     * Checks whether the WayPoint is a neighbour of the given WayPoint.
     * @param wayPoint WayPoint to test on neighbourhood
     * @return true if the WayPoints are neighbours
     */
    boolean isNeighbour(WayPoint wayPoint);
    
}
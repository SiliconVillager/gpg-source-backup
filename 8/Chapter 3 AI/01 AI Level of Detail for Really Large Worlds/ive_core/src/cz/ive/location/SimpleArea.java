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
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import cz.ive.exception.*;
import cz.ive.util.Pair;
import cz.ive.xmlload.*;
import cz.ive.logs.Log;
import cz.ive.iveobject.IveId;
import cz.ive.iveobject.ObjectClass;
import cz.ive.location.CommonArea.InfluenceInfo;
import cz.ive.location.CommonArea.NeighbourInfo;
import cz.ive.location.CommonArea.PhantomGenerationInfo;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * Implements an area interface - location containing a net of WayPoints.
 * This implementation is independent on the number of dimensions and
 * positions of particular WayPoints, but offers no more intelligence
 * than remembering the contained WayPoints.
 *
 * @author pavel
 */
public class SimpleArea extends CommonArea implements GraphArea {
    
    /** Contained waypoints */
    protected List<WayPoint> wayPoints;

    /**
     * Information needed to expand the location.
     */
    private ExpansionInfo expansionInfo;
    
    /**
     * Creates a new instance of Area2DGrid 
     * @param objectId id of the WayPoint
     * @param parent Parent area
     * @param realPosition coordinates of the new WayPoint
     * @param kind kind of the area
     * @param info Expansion info
     */
    public SimpleArea(String objectId, WayPoint parent, float[] realPosition, 
            Kind kind, ExpansionInfo info)
            throws ObjectRegistrationFailedException {
                super (objectId, parent, realPosition, kind, info);
                wayPoints = null;
                expansionInfo = info;
                if (expansionInfo == null) {
                    setSubstantial(true);
                }
    }
    
    public void setWayPoints(List<WayPoint> wayPoints) {
        this.wayPoints = wayPoints;
        
        borderWayPoints.clear();
            
        for (int i=0; i<wayPoints.size(); i++) {
            for (Joint joint : wayPoints.get(i).getNeighbours())
            {
                if (joint.target.isPhantom()) {
                    borderWayPoints.add(joint);
                }
            }
        }
    }
    
    public void addWayPoint(WayPoint wayPoint) {
        this.wayPoints.add(wayPoint);

        for (Joint joint : wayPoint.getNeighbours())
        {
            if (joint.target.isPhantom()) {
                borderWayPoints.add(joint);
            }
        }
    }
    
    public void forgetWayPoints() {
        wayPoints = null;
        borderWayPoints.clear();
        clearObjectPlaces();
    }
    
    /**
     * Getter for WayPoints that this area consists of.
     * @return WayPoint expansion on higher LOD
     */
    public WayPoint[] getWayPoints() {
        if (wayPoints == null) {
            return null;
        }
        
        WayPoint[] arrayedWPs= new WayPoint[0];
        return (WayPoint[]) wayPoints.toArray(arrayedWPs);
    }
    
    public List<WayPoint> getWayPoints(Kind kind) {
        Vector<WayPoint> wps;
        
        if (wayPoints == null) {
            return null;
        }

        wps = new Vector<WayPoint>();
        for (WayPoint wp : wayPoints) {
            if (wp.matchKind(kind)) {
                wps.add(wp);
            }
        }
        
        if (wps.isEmpty()) {
            return null;
        } else {
            return wps;
        }
    }
    
    /**
     * Applies all information stored in expansionInfo on expanding location.
     */
    protected void applyExpansionInfo() throws NoObjectPlaceException {
        HashMap<String, Area> map = new HashMap<String, Area>();
        HashMap<String, List<Influence>> influences = 
                new HashMap<String, List<Influence>>();
        HashMap<String, PhantomGenerationInfo> phantoms = 
                new HashMap<String, PhantomGenerationInfo>();
        HashMap<String, WayPoint> phantomMap =
                new HashMap<String, WayPoint>();
        
        LocationInstanceInfo iinfo;
        PhantomGenerationInfo pinfo;
        Area area;
        WayPoint wp1, wp2, phantom;
        NeighbourInfo neighbours;
        InfluenceInfo influence;
        List<Influence> wpInfluence;
        String id;
        int sep;
        
        wayPoints = new Vector<WayPoint>();
        clearObjectPlaces();
        
        /* generate sublocations */
        for (Iterator<LocationInstanceInfo> i = 
                expansionInfo.getLocationInstances().iterator(); 
                i.hasNext(); ) {
            iinfo = i.next();
            area = iinfo.instantiate(this);
            map.put(iinfo.name, area);
            wayPoints.add(area);
            influences.put(iinfo.name, new Vector<Influence>());
            phantoms.put(iinfo.name, new PhantomGenerationInfo());
            for (ObjectClass c : iinfo.objectPlacement) {
                defineObjectPlace(c, area);
            }
            
        }
        
        /* connect neighbours */
        for (Iterator<NeighbourInfo> i = 
                expansionInfo.getNeighbours().iterator(); i.hasNext(); ) {

            neighbours = i.next();
            
            sep = -1;
            if (neighbours.first().indexOf(IveId.SEP) != -1) 
            {
                /* phantom information for descendants */
                sep = neighbours.first().indexOf(IveId.SEP);
                pinfo = phantoms.get(neighbours.first().substring(0, sep));
                if (pinfo != null) {
                    String first=neighbours.first().substring(sep+1);
                    String second=neighbours.second();
                    second=getId()+IveId.SEP+neighbours.second();
                    
                    pinfo.neighbours.add(new NeighbourInfo(first,second, 
                        neighbours.dx, neighbours.dy, neighbours.kinds));
                } else {
                    Log.warning("World definition problem: joint defined from "
                            + neighbours.first() + " to " + neighbours.second() 
                            + ", but " + neighbours.first() + " was not found " 
                            + "among " + getId() + " children.");
                }
                
            }
            if (neighbours.second().indexOf(IveId.SEP) != -1) 
            {
                /* phantom information for descendants */
                sep = neighbours.second().indexOf(IveId.SEP);
                pinfo = phantoms.get(neighbours.second().substring(0, sep));
                if (pinfo != null)
                {

                    String first=neighbours.second().substring(sep+1);
                    String second = neighbours.first();
                    second = getId()+IveId.SEP+neighbours.first();


                    Pair<Kind, Kind> swappedKinds=null;
                    if (neighbours.kinds!=null){
                        swappedKinds = new Pair<Kind, Kind>(
                                neighbours.kinds.second(), 
                                neighbours.kinds.first());
                    }
                    pinfo.neighbours.add(new NeighbourInfo(
                        first,second,
                        -neighbours.dx, -neighbours.dy, swappedKinds
                        ));
                } else {
                    Log.warning("World definition problem: joint defined from "
                            + neighbours.first() + " to " + neighbours.second() 
                            + ", but " + neighbours.first() + " was not found " 
                            + "among " + getId() + " children.");
                }
            }
            if (sep == -1)
            {
                /* valid neighbours */
                wp1 = map.get(neighbours.first());
                wp2 = map.get(neighbours.second());
                if ((wp1 != null) && (wp2 != null))
                {
                    wp1.addNeighbour(new Joint(wp2, wp1.getDistance(wp2)));
                    wp2.addNeighbour(new Joint(wp1, wp2.getDistance(wp1)));
                } else {
                    if (wp1 == null) {
                        Log.warning("World definition problem: joint defined " 
                                + "from "  + neighbours.first() + " to " 
                                + neighbours.second() + ", but " 
                                + neighbours.first() + " was not found " 
                                + "among " + getId() + " children.");
                    } else {
                        Log.warning("World definition problem: joint defined " 
                                + "from "  + neighbours.first() + " to " 
                                + neighbours.second() + ", but " 
                                + neighbours.second() + " was not found " 
                                + "among " + getId() + " children.");
                    }
                }
            }
        }
        
        /* generate phantoms of neighbour locations */
        if (phantomGenerationInfo != null) {
            for (Iterator<NeighbourInfo> i = 
                    phantomGenerationInfo.neighbours.iterator(); i.hasNext(); ){
                
                neighbours = i.next();

                if (neighbours.first().indexOf(IveId.SEP) != -1) {
                    /* phantom information for descendants */
                    sep = neighbours.first().lastIndexOf(IveId.SEP);
                    pinfo = phantoms.get(neighbours.first().substring(0, sep));
                    if (pinfo != null) {
                        pinfo.neighbours.add(new NeighbourInfo(
                            neighbours.first().substring(sep+1),
                            neighbours.second(),
                            neighbours.dx, neighbours.dy, neighbours.kinds));
                    } else {
                        Log.warning("World definition problem: joint defined " 
                                + "from "  + neighbours.first() + " to " 
                                + neighbours.second() + ", but " 
                                + neighbours.first() + " was not found " 
                                + "among " + getId() + " children.");
                    }
                } else {
                    /* phantom info for this location */
                    area = map.get(neighbours.first());
                    if (area != null) {
                        Joint phantomJoint;
                        float[] position = area.getRealPosition().clone();
                        position[0] += neighbours.dx;
                        position[1] += neighbours.dy;
                        Kind targetKind = area.getKind();
                        if (neighbours.kinds != null) {
                            targetKind = neighbours.kinds.second();
                        }
                        phantom = new WayPointImpl(neighbours.second(), 
                                position, targetKind);
                        phantomMap.put(neighbours.second(), phantom);
                        phantomJoint = new Joint(phantom, 
                                area.getDistance(phantom));
                        area.addNeighbour(phantomJoint);
                        borderWayPoints.add(phantomJoint);
                    } else {
                        Log.warning("World definition problem: joint defined " 
                                + "from "  + neighbours.first() + " to " 
                                + neighbours.second() + ", but " 
                                + neighbours.first() + " was not found " 
                                + "among " + getId() + " children.");
                    }
                }
            }
        }
        
        /* build influences */
        for (Iterator<InfluenceInfo> i = 
                expansionInfo.getInfluences().iterator(); i.hasNext(); ) {
            influence = i.next();

            sep = -1;
            if (influence.first().indexOf(IveId.SEP) != -1) 
            {
                /* phantom information for descendants */
                sep = influence.first().lastIndexOf(IveId.SEP);
                pinfo = phantoms.get(influence.first().substring(0, sep));
                pinfo.influences.add(new InfluenceInfo(
                        influence.first().substring(sep+1),
                        getId()+IveId.SEP+influence.second(), 
                        influence.influence));
            }
            if (sep == -1)
            {
                /* influence info for this location */
                wpInfluence = influences.get(influence.first());
                if (wpInfluence == null) {
                    Log.warning("World definition problem: influence defined " 
                            + "from "  + influence.first() + " to " 
                            + influence.second() + ", but " 
                            + influence.first() + " was not found " 
                            + "among " + getId() + " children.");
                } else {
                    wpInfluence.add(
                            new Influence(map.get(influence.second()), 
                            influence.influence));
                }
            }
        }
        
        /* build influences to phantom neighbours */
        if (phantomGenerationInfo != null) {
            for (Iterator<InfluenceInfo> i = 
                    phantomGenerationInfo.influences.iterator(); i.hasNext(); ){

                influence = i.next();

                if (influence.first().indexOf(IveId.SEP) != -1) {
                    /* phantom information for descendants */
                    sep = influence.first().lastIndexOf(IveId.SEP);
                    pinfo = phantoms.get(influence.first().substring(0, sep));
                    if (pinfo != null) {
                        pinfo.influences.add(new InfluenceInfo(
                            influence.first().substring(sep+1),
                            influence.second(),
                            influence.influence));
                    } else {
                        Log.warning("World definition problem: influence " 
                                + "defined from "  + influence.first() + " to " 
                                + influence.second() + ", but " 
                                + influence.first() + " was not found " 
                                + "among " + getId() + " children.");
                    }
                } else {
                    /* phantom info for this location */
                    wpInfluence = influences.get(influence.first());
                    if (wpInfluence == null) {
                        Log.warning("World definition problem: influence " 
                                + "defined from "  + influence.first() + " to " 
                                + influence.second() + ", but " 
                                + influence.first() + " was not found " 
                                + "among " + getId() + " children.");
                    } else {
                        WayPoint target = phantomMap.get(influence.second());
                        if (target == null) {
                            Log.warning("World definition problem: influence " 
                                + "defined from "  + influence.first() + " to " 
                                + influence.second() + ", but " 
                                + influence.second() + " was not found " 
                                + "among phantom neighbours.");
                        } else {
                        
                            wpInfluence.add(
                                new Influence(
                                        phantomMap.get(influence.second()),
                                        influence.influence));
                        }
                    }
                }
            }
        }
        
        /* set influences */
        for (Iterator<String> i = influences.keySet().iterator(); 
                i.hasNext(); ) {
            id = i.next();

            Influence[] arrayedInfluences=new Influence[0];
            map.get(id).setInfluences(
                    (Influence[]) influences.get(id).toArray(
                        arrayedInfluences));
        }
                
        /* set phantom generation infos */
        for (Iterator<String> i = phantoms.keySet().iterator(); 
                i.hasNext(); ) {
            id = i.next();
            map.get(id).setPhantomGenerationInfo(phantoms.get(id));
        }

    }

    /**
     * All informations needed to instantiate sublocations
     */
    public static class ExpansionInfo extends CommonArea.ObjectExpansionInfo
            implements Serializable {
        
        /**
         * Instantiation information about all sublocations.
         * It defines order of locations used by Pair and InfluenceInfo to 
         * reference them.
         */
        private List<LocationInstanceInfo> locationInstances;
        
        /**
         * Information about neighbouring WayPoints 
         */
        private List<NeighbourInfo> neighbours;
        
        /**
         * Information about influences among WayPoints.
         */
        private List<InfluenceInfo> influences;
                
        
        /**
         * Creates a new instance of ExpansionInfo.
         * @param objInstancesInfo Information about inherent objects. The Pair
         *                         contains template needed to create the object
         *                         and count of the objects
         * @param locInstances Information needed to generate the sublocation
         * @param neigh Information about neighbourhood among sublocations
         * @param infs Information about influences among sublocations
         */
        public ExpansionInfo(List<Pair<ObjectInstanceInfo,Integer>> 
                                objInstancesInfo,
                             List<LocationInstanceInfo> locInstances,
                             List<NeighbourInfo> neigh,
                             List<InfluenceInfo> infs
                ){
            super(objInstancesInfo);
            locationInstances=locInstances;
            neighbours= neigh;
            influences= infs;
            
            if (locationInstances==null) locationInstances = 
                    new LinkedList<LocationInstanceInfo>();
            
            if (neighbours==null) neighbours = 
                    new LinkedList<NeighbourInfo>();
            
            if (influences==null) influences = 
                    new LinkedList<InfluenceInfo>();
            
        }

        public List<LocationInstanceInfo> getLocationInstances() {
            return locationInstances;
        }

        public List<NeighbourInfo> getNeighbours() {
            return neighbours;
        }

        public List<InfluenceInfo> getInfluences() {
            return influences;
        }


    }
}

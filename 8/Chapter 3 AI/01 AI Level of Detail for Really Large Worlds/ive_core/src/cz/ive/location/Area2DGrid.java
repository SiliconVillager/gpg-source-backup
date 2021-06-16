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

import cz.ive.iveobject.ObjectClass;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;
import cz.ive.exception.*;
import cz.ive.util.Pair;
import cz.ive.xmlload.*;
import cz.ive.logs.*;
import cz.ive.iveobject.IveId;
import cz.ive.gui.DelegatedGraphicInfo;
import cz.ive.iveobject.IveObjectImpl;

/**
 * Implements an area as a 2d grid - location containing a net of WayPoints in
 * a two-dimensional full grid.
 *
 * @author pavel
 */
public class Area2DGrid extends CommonArea implements GridArea {
    
    /** Contained waypoints */
    protected WayPoint[][] wayPoints;
    
    /** Array of contained WayPoints - only a cache for getWayPoints calls */
    protected WayPoint[] wpArray;
    
    /** Array of WayPoints represented long paths */
    protected List<WayPoint> paths;
    
    /**
     * Information needed to expand the location.
     */
    private ExpansionInfo expansionInfo;

    /**
     * Creates a new instance of Area2DGrid 
     * @param objectId id of the WayPoint
     * @param parent Parent area
     * @param realPosition coordinates of the new WayPoint
     * @param kind Kind of the area
     * @param info Expansion info
     */
    public Area2DGrid(String objectId, WayPoint parent, float[] realPosition, 
            Kind kind, ExpansionInfo info) 
            throws ObjectRegistrationFailedException {
                super(objectId, parent, realPosition, kind, info);
                this.wayPoints = null;
                this.expansionInfo = info;
                wpArray = null;
                paths = new Vector<WayPoint>();
    } 

    public void forgetWayPoints() {
        wayPoints = null;
        wpArray = null;
        borderWayPoints.clear();
        paths.clear();
        clearObjectPlaces();
    }

    /**
     * Parses the WayPoint's id to the indexes in the grid.
     * @param wp The WayPoint to parse
     * @return x and y indexes in the grid
     */
    private Pair<Integer, Integer> posFromName(WayPoint wp) {
        String id = wp.getFlatId().substring(3);
        String num;
        int i = id.indexOf("_");
        num = id.substring(0, i);
        int x = Integer.parseInt(num);
        num = id.substring(i+1);
        int y = Integer.parseInt(num);
        
        return new Pair<Integer, Integer>(x, y);
    }
    
    /**
     * Takes a joint to a phantom and generates the path of two unsubstantial 
     * WayPoints.
     * The join to phantom will be changed to following structure: join to 
     * a new valid WayPoint representing the path, from this WayPoint join
     * to the phantom of similar generated WayPoint on the other side. This 
     * avoids crowds.
     * @param wp WayPoint from which the joint starts
     * @param j The joint itself
     */
    private void generatePath(WayPoint wp, Joint j) {
        if (! j.target.isPhantom()) {
            return;
        }
        
        if (j.weight >= 1) { 
            
            boolean isShort = (j.weight < 2);
            
            String id = wp.getId().toString()+"_path";
            Pair<Integer, Integer> idxs = posFromName(wp);
            int x = idxs.first();
            int y = idxs.second();

            float dx = 0;
            if (x == 1) {
                dx = -1;
            }
            if (x == expansionInfo.getWidth()) {
                dx = 1;
            }
            float dy = 0;
            if (y == 1) {
                dy = -1;
            }
            if (y == expansionInfo.getHeight()) {
                dy = 1;
            }
            
            float len = j.weight-2;
            if (isShort) {
                len = 0.01f;
            }
            float pos[] = new float[2];
            pos[0] = (float) (wp.getRealPosition()[0] + dx);
            pos[1] = (float) (wp.getRealPosition()[1] + dy);

            WayPoint path = null;
            path = null;
            for (WayPoint p : paths) {
                if (p.getId().toString().equals(id)) {
                    path = p;
                    break;
                }
            }

            if (path == null) {
                try {
                    path = new WayPointImpl(id, this, pos, wp.getKind());
                    path.setSubstantial(false);
                    Joint newJoint = new Joint(
                            new WayPointImpl(
                                j.target.getId()+"_path", 
                                j.target.getRealPosition().clone()), 
                            len);
                    Joint backJoint = new Joint(wp, 1);
                    backJoint.valid = true;
                    path.addNeighbour(backJoint);
                    path.addNeighbour(newJoint);

                    borderWayPoints.add(newJoint);
                    j.target = path;
                    j.weight = 1;
                    j.valid = true;
                    paths.add(path);
                } catch (ObjectRegistrationFailedException e) {
                    borderWayPoints.add(j);
                }
            } else {
                Joint newJoint = new Joint(
                        new WayPointImpl(
                            j.target.getId()+"_path", 
                            j.target.getRealPosition().clone()), 
                        len);
                path.addNeighbour(newJoint);

                j.target = path;
                j.weight = 1;
                j.valid = true;
            }
        } else {
            borderWayPoints.add(j);
        }
    }
    
    public void setGrid(WayPoint[][] grid) {
        wpArray = null;
        wayPoints = grid;
        int i, j;
        
        borderWayPoints.clear();

        for (i=0; i<wayPoints.length; i++) {
            for (j=0; j<wayPoints[i].length; j++) {
                for (Joint joint : wayPoints[i][j].getNeighbours())
                {
                    if (joint.target.isPhantom()) {
                        generatePath(wayPoints[i][j], joint);
                    }
                }
            }
        }
    }
    
    public WayPoint[][] getGrid() {
        return wayPoints;
    }
    
    public WayPoint getAt(int x, int y) {
        if ((x < 0) || 
            (y < 0) ||
            (x >= wayPoints.length) ||
            (y >= wayPoints[x].length)) {
                return null;
        }
        return wayPoints[x][y];
    }

    /**
     * Getter for WayPoints that this area consists of.
     * @return WayPoint expansion on higher LOD
     */
    public WayPoint[] getWayPoints() {
        
        int i,j,k;
        
        if (wayPoints == null) {
            return null;
        }
        
        if (wpArray != null) {
            return wpArray;
        }
        
        k = 0;
        for (i=0; i<wayPoints.length; i++) {
            k += wayPoints[i].length;
        }
        k += paths.size();
        
        wpArray = new WayPoint[k];

        k = 0;
        for (i=0; i<wayPoints.length; i++) {
            for (j=0; j<wayPoints[i].length; j++) {
                wpArray[k] = wayPoints[i][j];   
                k++;
            }
        }
        for (WayPoint wp : paths) {
            wpArray[k] = wp;
            k++;
        }
        
        return wpArray;
    }

    public List<WayPoint> getWayPoints(Kind kind) {
        Vector<WayPoint> wps;
        
        if (wayPoints == null) {
            return null;
        }

        wps = new Vector<WayPoint>();

        int k = 0;
        for (int i=0; i<wayPoints.length; i++) {
            for (int j=0; j<wayPoints[i].length; j++) {
                if (wayPoints[i][j].matchKind(kind)) {
                    wps.add(wayPoints[i][j]);   
                }
                k++;
            }
        }

        for (WayPoint wp : paths) {
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
    
    public List<WayPoint> getPathWayPoints() {
        return paths;
    }
    
    /**
     * Applies all information stored in expansionInfo on expanding location.
     */
    protected void applyExpansionInfo() throws NoObjectPlaceException {

        WayPoint wp;
        WayPoint phantom;
        float[] coordinates, position;
        int i, j;
        String id;
        HashMap<String, WayPoint> map;
        HashMap<String, WayPoint> phantomMap;
        HashMap<WayPoint, List<Influence>> influences = 
                new HashMap<WayPoint, List<Influence>>();
        WayPoint wp1, wp2;
        Set<WayPoint> exceptionSet = new HashSet<WayPoint>();
        
        wayPoints = new WayPoint[expansionInfo.getWidth()]
                                [expansionInfo.getHeight()];
        wpArray = null;
        
        map = new HashMap<String, WayPoint>();
        coordinates = new float[2];
        position = new float[2];
        
        for (i=0; i<2; i++) {
            coordinates[i] = getRealPosition()[i];
        }
        
        /* generate sublocations */
        for (i=0; i<expansionInfo.getWidth(); i++) {
            for (j=0; j<expansionInfo.getHeight(); j++) {
                position[0] = coordinates[0] + i;
                position[1] = coordinates[1] + j;
                id = "wp_"+(i+1)+"_"+(j+1);
                try {
                    wp = new WayPointImpl(getId()+IveId.SEP+id, 
                            this, position, expansionInfo.getDefaultWpKind());
                    wp.setSubstantial(true);
                    map.put(id, wp);
                    influences.put(wp, new Vector<Influence>());
                    wayPoints[i][j] = wp;
                    new DelegatedGraphicInfo(wp, 
                            expansionInfo.getDefaultTemplateStrings().className, 
                            expansionInfo.getDefaultTemplateStrings().name);
                } catch (Exception e) {
                    Log.warning("Cannot create waypoint "+id+".");
                }
            }
        }
        
        /* set kinds */
        exceptionSet.clear();
        if (expansionInfo.getKindExceptions() != null) {
            for (ExpansionInfo.GridException<Kind> kind : 
                        expansionInfo.getKindExceptions()) {
                for (i=kind.x1; i<=kind.x2; i++) {
                    for (j=kind.y1; j<=kind.y2; j++) {
                        id = "wp_"+i+"_"+j;
                        wp = map.get(id);
                        if (wp != null) {
                            if (! exceptionSet.contains(wp))
                            {
                                wp.setKind(kind.value);
                                exceptionSet.add(wp);
                            }
                        } else {
                            Log.warning("Kind defined for not existing "+
                                    "WayPoint at ["+i+" "+j+"]");
                        }
                    }
                }
            }
        }
        
        /* set objectPlaces */
        exceptionSet.clear();
        clearObjectPlaces();
        if (expansionInfo.getObjPlacement() != null) {
            for (ExpansionInfo.GridException<ObjectClass> place : 
                        expansionInfo.getObjPlacement()) {
                for (i=place.x1; i<=place.x2; i++) {
                    for (j=place.y1; j<=place.y2; j++) {
                        id = "wp_"+i+"_"+j;
                        wp = map.get(id);
                        if (wp != null) {
                            if (! exceptionSet.contains(wp))
                            {
                                defineObjectPlace(place.value, wp);
                                exceptionSet.add(wp);
                            }
                        } else {
                            Log.warning("Object place defined for not "+
                                "existing WayPoint at ["+i+" "+j+"]");
                        }
                    }
                }
            }
        }
        
        /* set graphic infos */
        exceptionSet.clear();
        if (expansionInfo.getGraphicTemplates() != null) {  
            for (ExpansionInfo.GridException<GraphicTemplateStrings> template: 
                        expansionInfo.getGraphicTemplates()) {
                for (i=template.x1; i<=template.x2; i++) {
                    for (j=template.y1; j<=template.y2; j++) {
                        id = "wp_"+i+"_"+j;
                        wp = map.get(id);
                        if (wp != null) {
                            if (! exceptionSet.contains(wp))
                            {
                                new DelegatedGraphicInfo(wp, 
                                        template.value.className, 
                                        template.value.name);
                                exceptionSet.add(wp);
                            }
                        } else {
                            Log.warning("Graphic info defined for not existing"+
                                    "WayPoint at ["+i+" "+j+"]");
                        }
                    }
                }
            }
        }
                    
        /* connect neighbours */
        HashMap<String, HashSet<String>> jointExceptions = 
                expansionInfo.getJointExceptions();
        boolean jointExcsPresent = ((jointExceptions != null) && 
                            !jointExceptions.isEmpty());
        boolean ommit;
        HashSet<String> targets;
        
        for (i=0; i<expansionInfo.getWidth(); i++) {
            for (j=0; j<expansionInfo.getHeight(); j++) {
                id = "wp_"+(i+1)+"_"+(j+1);
                for (int k=-1; k<2; k++) {
                    for (int l=-1; l<2; l++) {
                        if ((i+k>=0) && (i+k<expansionInfo.getWidth()) &&
                            (j+l>=0) && (j+l<expansionInfo.getHeight()))
                        {
                            if ((k==0) && (l==0)) {
                                continue;
                            }
                            String targetId = "wp_"+(i+k+1)+"_"+(j+l+1);
                      
                            ommit = false;
                            if (jointExcsPresent) {
                                targets = jointExceptions.get(id);
                                if ((targets != null) && 
                                        (!targets.contains(targetId))) {
                                    ommit = true;
                                } 
                                targets = jointExceptions.get(targetId);
                                if ((targets != null) && 
                                        (!targets.contains(id))) {
                                    ommit = true;
                                }
                            }
                            
                            if (! ommit) {
                                wp1 = map.get(id);
                                wp2 = map.get(targetId);
                                wp1.addNeighbour(
                                    new Joint(wp2, wp1.getDistance(wp2)));
                            }
                        }
                    }
                }
            }
        }
 
        if (phantomGenerationInfo != null) {
            
            /* create phantoms of neighbour locations */
            phantomMap = new HashMap<String, WayPoint>();
            for (NeighbourInfo pn : phantomGenerationInfo.neighbours) {
                wp = map.get(pn.first());
                if (wp != null) {
                    Joint phantomJoint;
                    float[] pos = wp.getRealPosition().clone();
                    pos[0] += pn.dx;
                    pos[1] += pn.dy;
                    Kind targetKind = wp.getKind();
                    if (pn.kinds != null) {
                        targetKind = pn.kinds.second();
                    }
                    phantom = new WayPointImpl(pn.second(), pos, targetKind);
                    phantomMap.put(pn.second(), phantom);
                    phantomJoint = new Joint(phantom, wp.getDistance(phantom));
                    wp.addNeighbour(phantomJoint);
                    generatePath(wp, phantomJoint);
                    //borderWayPoints.add(phantomJoint);
                } else {
                    Log.warning("World definition problem: joint defined from "
                            + pn.first() + " to " + pn.second() 
                            + ", but " + pn.first() + " was not found " 
                            + "in " + getId() + " children.");
                }
            }
        
            /* build influences to phantom neighbours */
            for (InfluenceInfo pi : phantomGenerationInfo.influences) {
                wp = map.get(pi.first());
                if (wp != null) {
                    phantom = phantomMap.get(pi.second());
                    influences.get(wp).add(new Influence(
                            phantom, pi.influence));
                } else {
                    Log.warning("World definition problem: influence defined " 
                            + "from " + pi.first() + " to " + pi.second() 
                            + ", but " + pi.first() + " was not found " 
                            + "in " + getId() + " children.");
                }
            }
        }
                    
        for (WayPoint wpi : influences.keySet()) {
            Influence[] tmp = new Influence[0];
            wpi.setInfluences( influences.get(wpi).toArray(tmp));
        }

    }

    /**
     * Encapsulates all information needed to expand grid area.
     */
    public static class ExpansionInfo extends CommonArea.ObjectExpansionInfo 
            implements Serializable {
        
        /**
         * Defines exceptions from default values of any member of the 
         * WayPoints in the grid.
         */
        public static class GridException<T> implements Serializable {
            /** Coordinates of the top-left WayPoint */
            public int x1,y1;
            /** Coordinates of the right-bottom WayPoint */
            public int x2, y2;
            
            /** Value of the member */
            public T value;
            
            /**
             * Creates a new instance of GridException
             * @param _x1 x1 coordinate
             * @param _y1 y1 coordinate
	     * @param _x2 x2 coordinate
             * @param _y2 y2 coordinate
             * @param _value Value of the member
             */
            public GridException(int _x1,int _y1, int _x2, int _y2, T _value){
                x1=_x1;
                y1=_y1;
                x2=_x2;
                y2=_y2;
                value=_value;
            }
        }
        
        /**
         * Creates a new instance of ExpansionInfo.
         * Exception lists should be ordered in reverse order, only the first
         * occurence for single WayPoint is used.
         * @param objInstancesInfo Information about inherent objects. The Pair
         *                         contains template needed to create the object
         *                         and count of the objects
         * @param _kindExceptions Exceptions of the "kind" member. As the 
         *                        default kind is used the parent's.
         * @param _objPlacement Places for objects passed to sublocations
         * @param graphicTemplates Exceptions of grpahic templates. The default
         *                         value is in the next argument.
         * @param defaultTemplateStrings Default graphic template.
         * @param width Width of the grid
         * @param height Height of the grid
         */               
        public ExpansionInfo(
                List<Pair<ObjectInstanceInfo,Integer>> objInstancesInfo,
                List<GridException<Kind>> _kindExceptions,
                List<GridException<ObjectClass>> _objPlacement,
                List<GridException<GraphicTemplateStrings>> graphicTemplates,
                GraphicTemplateStrings defaultTemplateStrings,
                Kind defaultWpKind,
                HashMap<String,HashSet<String>> jointExceptions,
                int width, int height) {
            
            super(objInstancesInfo);
            this.width = width;
            this.height = height;
            this.graphicTemplates=graphicTemplates;
            this.defaultTemplateStrings=defaultTemplateStrings;
            kindExceptions = _kindExceptions;
            objPlacement = _objPlacement;
            this.defaultWpKind=defaultWpKind;
            this.jointExceptions=jointExceptions;
        }
        
        /** Width of the grid */
        private int width;
        
        /** Height of the grid */
        private int height;
        
        private Kind defaultWpKind;
        
        /** Exceptions of the "kind" member */
        private List<GridException<Kind>> kindExceptions;
        /** Places for objects passed to sublocations during expand */
        private List<GridException<ObjectClass>> objPlacement;
        /** Exceptions of graphic templates */
        private List<GridException<GraphicTemplateStrings> > graphicTemplates;
        /** Default graphic template */
        private GraphicTemplateStrings defaultTemplateStrings;
        /** Exceptions of joints among child WayPoints */
        private HashMap<String,HashSet<String>> jointExceptions;
        
        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        
        public List<GridException<Kind>> getKindExceptions() {
            return kindExceptions;
        }

        
        public List<GridException<ObjectClass>> getObjPlacement() {
            return objPlacement;
        }

        
        public List<GridException<GraphicTemplateStrings>>
                getGraphicTemplates() {
            return graphicTemplates;
        }


        public GraphicTemplateStrings getDefaultTemplateStrings() {
            return defaultTemplateStrings;
        }

        public Kind getDefaultWpKind() {
            return defaultWpKind;
        }

        public HashMap<String, HashSet<String>> getJointExceptions() {
            return jointExceptions;
        }
        
    }
}

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

import java.lang.Math;
import java.util.PriorityQueue;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;

/**
 * General implementation of PathFinder. This class finds the path with no 
 * restriction to the topology and to the world's dimense. This class is
 * implemented as a singleton and does not need to be serializable, because
 * when it does not computing, it contains no state data.
 *
 * @author pavel
 */
public class PathFinderImpl implements PathFinder, java.io.Serializable {

    /**
     * The main structure of the A* algorithm.
     */
    protected PriorityQueue<PathPoint> bag;
    
    /**
     * The set of target waypoints.
     * This information is needed when comparing two PathPoints, by this
     * variable we avoid storing it in each PathPoint.
     */
    protected List<WayPoint> targetSet;
    
    /**
     * The map from WayPoints to PathPoints.
     */
    protected HashMap<WayPoint, PathPoint> pointMap;
    
    /** The only instance of the path finder */
    static private PathFinderImpl pathFinder;

    /**
     * Enhances the WayPoint for usability in A* algorithm.
     */
    protected class PathPoint implements Comparable, java.io.Serializable {
        
        /** Creates a new instance of PathPoint */
        PathPoint(WayPoint point) {
            wayPoint = point;
            distance = Double.MAX_VALUE;
            guessed = -1;
            pred = null;
        }
        
        /** The represented WayPoint */
        public WayPoint wayPoint;
        
        /** Distance from the start point found so far. */
        public Double distance;
        
        /** The predecessor on the shortest way so far. */
        public PathPoint pred;
        
        /** The guessed distance to the target, -1 if not guessed yet. */
        public double guessed;
        
        /** Compares A* vertexes of two PathPoints */
        public int compareTo(Object o) {
            PathPoint point;
            double diff;
            
            point = (PathPoint) o;
            
            if (point.guessed < 0) {
                point.guessed = guessDistanceFromSet(point.wayPoint, targetSet);
            }

            if (guessed < 0) {
                guessed = guessDistanceFromSet(wayPoint, targetSet);
            }
            
            diff = (point.distance + point.guessed) -
                    (this.distance + guessed);
            
            if (diff < 0.0) {
                return 1;
            }
            
            if (diff > 0.0) {
                return -1;
            }
            
            return 0;
        }
        
    }
    
    /** Creates a new instance of PathFinderImpl */
    protected PathFinderImpl() {
        bag = new PriorityQueue<PathPoint>();
        pointMap = new HashMap<WayPoint, PathPoint>();
    }
    
    /** Returns the single instance of the path finder */
    static public synchronized PathFinderImpl instance() {
        if (pathFinder == null) {
            pathFinder = new PathFinderImpl();
        }
        
        return pathFinder;
    }
    
    /** Returns the single instance of the path finder */
    static public void setInstance(PathFinderImpl newInstance) {
        pathFinder = newInstance;
    }
    
    /** 
     * Empty whole the PathFinder before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        pathFinder = new PathFinderImpl();
    }
    
    /** Cleans data structures to be ready for another search */
    protected void cleanStructures() {
        bag.clear();
        pointMap.clear();
        targetSet = null;
    }
    
    /** 
     * Guesses the distance of two points. It means, computes the shortest
     * distance between their real coordinates.
     * @param from First WayPoint
     * @param to Second Waypoint
     * @return the lower guess of their distance
     */
    protected double guessDistance(WayPoint from, WayPoint to) {
        float c1[], c2[];
        double distance;
        
        c1 = from.getRealPosition();
        c2 = to.getRealPosition();
        
        distance = 0;
        for (int i=0; i < c1.length; i++) {
            distance += (c2[i] - c1[i]) * (c2[i] - c1[i]);
        }
        if (c1.length == 2) {
            distance = java.lang.Math.sqrt(distance);
        } else {
            distance = java.lang.Math.pow(distance, 1.0/(double) c1.length);
        }
        
        return distance;
    }

    /** 
     * Guesses the distance of a point and a set of points. It computes distance
     * tu each point from the set as described at guessDistance() method, and
     * takes the shortest one.
     * @param from Starting WayPoint
     * @param to Set of target Waypoints
     * @return the lower guess of their distance
     */
    protected double guessDistanceFromSet(WayPoint from, List<WayPoint> to) {
        double distance;
        double min;
        
        min = Double.MAX_VALUE;
        for (java.util.Iterator<WayPoint> i = to.iterator(); i.hasNext(); ) {
            distance = guessDistance(from, i.next());
            if (distance < min) {
                min = distance;
            }
        }
        
        return min;
    }
    
    /**
     * Relaxes all joints from the given pathpoint.
     * @param point PathPoint which neighbours' distances will be recomputed
     * @param kinds Kinds of WayPoints that can be used
     * @param substantial avoid occupied WayPoints?
     */
    protected void decreaseKeys(PathPoint point, Kind kinds, 
            boolean substantial) {
        
        List<Joint> neighbours;
        PathPoint next;
        WayPoint wayPoint;
        double newDistance;
        
        if (point.wayPoint.isPhantom()) {
            return;
        }
            
        neighbours = point.wayPoint.getNeighbours();
        
        if (neighbours == null) {
            return;
        }
        
        for (int i=0; i<neighbours.size(); i++) {
            wayPoint = neighbours.get(i).target;
            
            if (!targetSet.contains(wayPoint))
            {
                if (! wayPoint.matchKind(kinds)) {
                    continue;
                }

                if (substantial && 
                            ((wayPoint.getSpaceState() == 
                                WayPoint.SpaceState.OCCUPIED) ||
                            (wayPoint.getSpaceState() == 
                                WayPoint.SpaceState.GOING_EMPTY))) {
                    continue;
                }
            }
            
            next = pointMap.get(wayPoint);
            if (next == null) {
                next = new PathPoint(wayPoint);
                pointMap.put(wayPoint, next);
            }

            newDistance = point.distance + neighbours.get(i).weight;
            if (next.distance > newDistance) {
                next.distance = newDistance;
                next.pred = point;
                if (! bag.contains(next)) {
                    bag.add(next);
                }
            }
        }
    }
    
    /**
     * Builds the path from the predecessor-list created by the algorithm.
     * The path will start by the first WayPoint after the 'from' WayPoint
     * and finish by the 'to' WayPoint.
     * @param from The starting point of the path.
     * @param to The target point of the path.
     */
    protected List<WayPoint> constructPath(PathPoint from, PathPoint to) {
        
        PathPoint point;
        double minDistance;
        Vector<WayPoint> path;
        
        point = to;
        path = new Vector<WayPoint>();
        while (point != from) {
            path.add(0,point.wayPoint);
            point = point.pred;
        }
        
        return path;
    }
    
    /**
     * Finds path within one Area on the same LOD level.
     * The A* algorithm is used to find the path. However, it is generalized
     * to search for the path to the set of points, not only one. As a heuristic 
     * function is used the direct distance between the real coordinates of the 
     * points.
     * @param from WayPoint from where we should start the search
     * @param to list of target WayPoints
     * @return list of WayPoints on found path or <code>null</code> if there
     *      is no possible path within this Area
     */
    public List<WayPoint> findPath(WayPoint from, List<WayPoint> to, 
            Kind kinds, boolean substantial) {
                
        PathPoint point;
        PathPoint start;

        /* fill globals */
        cleanStructures();
        targetSet = to;

        /* init the bag */
        start = new PathPoint(from);
        start.distance = 0.0;
        bag.add(start);
        pointMap.put(start.wayPoint, start);

        /* run the search algorithm */
        while (! bag.isEmpty()) {

            point = bag.poll();

            if (to.contains(point.wayPoint)) {
                List<WayPoint> path = constructPath(start, point);
                cleanStructures();
                return path;
            }

            decreaseKeys(point, kinds, substantial);                    
        }

        cleanStructures();

        /* no path found */
        return null;
    }
   
    /**
     * Finds path within one Area to one of its neighbour Areas.
     * See findPath() method for algorithm implementation details.
     * @param area Area where is the starting WayPoint situated
     * @param from WayPoint from where we should start the search
     * @param to WayPoint representing adjacent Area
     * @return list of WayPoints on found path or <code>null</code> if there
     *      is no possible path within this Area
     */
    public List<WayPoint> findPathToNeighbour(Area area, WayPoint from, 
            WayPoint to, Kind kinds, boolean substantial) {
                return findPath(from, area.getBorderWayPoints(to), kinds, 
                        substantial);
    }
}

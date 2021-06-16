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

/**
 * Interface for path finding logic.
 *
 * @author ondra
 */
public interface PathFinder {
    
    /**
     * Finds path within one Area on the same LOD level.
     * If kind is null, all WayPoints can be used. If not, only that WayPoints
     * can be used, whose kind contains at least one of the numbers given here.
     * The exception are WayPoints with null kind, which can be used always.
     * @param from WayPoint from where we should start the search
     * @param to list of target WayPoints
     * @param kinds kinds of the WayPoints used in the Path
     * @param substantial avoid occupied WayPoints?
     * @return list of WayPoints on found path or <code>null</code> if there
     *      is no possible path within this Area
     */
    List<WayPoint> findPath(WayPoint from, List<WayPoint> to, Kind kinds, 
            boolean substantial);
    
    /**
     * Finds path within one Area to one of its neighbour Areas.
     * If kind is null, all WayPoints can be used. If not, only that WayPoints
     * can be used, whose kind contains at least one of the numbers given here.
     * The exception are WayPoints with null kind, which can be used always.
     * @param area Area where is the starting WayPoint situated
     * @param from WayPoint from where we should start the search
     * @param to WayPoint representing adjacent Area
     * @param kinds kinds of the WayPoints used in the Path
     * @param substantial avoid occupied WayPoints?
     * @return list of WayPoints on found path or <code>null</code> if there
     *      is no possible path within this Area
     */
    List<WayPoint> findPathToNeighbour(Area area, WayPoint from, WayPoint to, 
            Kind kinds, boolean substantial);
    
}

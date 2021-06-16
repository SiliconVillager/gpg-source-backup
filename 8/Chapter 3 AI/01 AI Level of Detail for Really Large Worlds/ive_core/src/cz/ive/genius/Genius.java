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
 
package cz.ive.genius;
import cz.ive.simulation.Updateable;
import java.io.Serializable;
import java.util.*;
import cz.ive.process.Goal;
import cz.ive.process.*;

/**
 * General genius interface. The genius tries to fulfill goals which are
 * assigned to him. He (she..) can use sources which he "owns" to do this. 
 *
 * @author Honza
 */
public interface Genius extends Serializable, Updateable {
    
    /** 
     * Adddition of the Top-level goal to be fullfilled by this genius.
     *
     * @param goal Goal to be added and fullfilled.
     */
    public void addTopLevelGoal(Goal goal);
    
    /** 
     * Removes a specified top level goal.
     *
     * @param goal Goal to be removed (stopped if running).
     */
    public void removeTopLevelGoal(Goal goal);
    
    /** 
     * Retrieves all top-level goals.
     *
     * @return List of top-level goals assigned to this genius.
     */
    public List<Goal> getTopLevelGoals();
    
    /** 
     * The given source will be held by the genius to do more extended
     * operations than with other objects.
     * ummm u know what i'm saying.. imagine actor
     * @param source Source to be added to the genius
     */
    public void addSource(Source source);
    
    /** 
     * The given source will no longer be genius's source.
     * hate comments..;).
     * @param source Source to be removed.
     */
    public void removeSource(Source source);
     
    /** 
     * Genius activates itself in this method, mainly registers on Hooks
     */
    public void activate();
    
    /**
     * Deactivates all the geniuses' goals
     */
    public void deactivate();
    
    /**
     * Getter for the genius' Id.
     */
    public String getId();
}

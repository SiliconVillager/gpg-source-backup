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

import cz.ive.IveApplication;
import cz.ive.exception.OntologyNotSupportedException;
import cz.ive.util.Pair;
import java.util.*;

import cz.ive.ontology.OntologyToken;
import cz.ive.process.*;
import cz.ive.trigger.*;
import cz.ive.iveobject.*;
import cz.ive.manager.*;
import cz.ive.evaltree.leaves.*;
import cz.ive.valueholders.*;
import cz.ive.evaltree.*;
import cz.ive.sensors.*;
import cz.ive.logs.*;

/**
 * Simple PDecider implementation. Compatible with the BasicGenius.
 *
 * @author ondra
 */
public class BasicPDecider implements java.io.Serializable {
    
    /** Id of the associated genius */
    protected String id;
    
    /** List of sensors given by owner genius */
    protected List<Sensor> sensors;
    
    /** Last chosen sources. This is just temporal helper. */
    protected Substitution chosenSources;
    
    /**
     * Creates a new instance of BasicPDecider
     *
     * @param id Id of the associated genius
     */
    public BasicPDecider(String id) {
        this.id = id;
    }
    
    /** Tries to find a source from the give list which has a link to
     *  the given role, goal and process. If one is find, it's returned.
     *  @param sources List of sources to search in
     *  @param role Role of the object which is looked for in the link
     *  @param goalId Id of the goal in the wanted link
     *  @param processId Id of the process in the wanted link
     *  @return found Source or null if there's no object with a link of given
     *          properties
     */
    private IveObject findSource(List<IveObject> sources, String role,
            String goalId, String processId) {
        Set<Link> links;
        for (IveObject source: sources) {
            links = source.getLinks(goalId, processId, role);
            if (!links.isEmpty()) {
                return source;
            }
        }
        return null;
    }
    
    /**
     * Tries to fill each slot of the given substitution with
     * 1. goal source
     * 2. genius source
     * 3. an object found via the manager of senses
     *
     * @param substitution substitution of the process to fill.
     * @param goalId Id of the goal for which we are filling the subst.
     * @param processId Id of the process for which we are filling the subst.
     * @param sources VIP sources (usually actors)
     * @param goalSources substitution of the goal (rule).
     * @return did we fill all the necessary sources?
     */
    public boolean fillSubstitution(Substitution substitution, String goalId,
            String processId, List<IveObject> sources,
            Substitution goalSources) {
        Set<String> roles = substitution.getSlotsKeys();
        Source source;
        
        for (String sourceId: roles) {
            IveObject obj = null;
            
            // Is the source already assigned to the goal?
            if (goalSources != null &&
                    (source = goalSources.getSource(sourceId)) != null) {
                
                Slot oldSlot = goalSources.getSlots().get(sourceId);
                Slot newSlot = substitution.getSlots().get(sourceId);
                
                obj = source.getObject();
                
                // Share the source between both variable or both nonvariable
                // slots.
                if ((oldSlot.isVariable && newSlot.isVariable) ||
                        (!oldSlot.isVariable && !newSlot.isVariable)) {
                    // Share
                    newSlot.setSource(source);
                } else {
                    // Duplicate and thus break the sharing
                    newSlot.setSource(new SourceImpl(obj));
                }
                
            } else if ( (obj = findSource(sources, sourceId,
                    goalId, processId)) != null) {
                // We have some VIP source, that can be used in this process
                Source src = substitution.getSource(sourceId);
                if (src == null) {
                    src = new SourceImpl(obj);
                    substitution.setSource(sourceId, src);
                } else {
                    src.setObject(obj);
                }
            } else {
                // No source... look for it via the Manager of senses.
                
                // We look for the sources only if they are mandatory.
                if (substitution.isMandatory(sourceId)) {
                    OntologyToken queryData =
                            new QueryData(
                            (Expr) new FuzzyConstant(FuzzyValueHolder.True),
                            new Link("","",sourceId,null),
                            substitution.getSlotLod(sourceId));
                    List<IveObject> result = null;
                    try {
                        result = ManagerOfSenses.instance().queryActive(
                                sensors, queryData, null,
                                ManagerOfSenses.ReturnSet.ANY_COPY);
                    } catch (Exception e) {
                        IveApplication.printStackTrace(e);
                        Log.addMessage("MOS did not accept an active query.",
                                Log.SEVERE, id, processId, "");
                    }
                    if (result != null && !result.isEmpty()) {
                        for (IveObject object : result) {
                            source = new SourceImpl(object);
                            obj = object;
                            substitution.setSource(sourceId, source);
                            break;
                        }
                    } else {
                        Log.addMessage("PDecider could not find appropriate " +
                                "object to the slot: "+sourceId,
                                Log.FINE, id, processId, "");
                        return false;
                    }
                }
            }
            // Check the affordances.
            if (obj != null) {
                if (obj.getLinks(goalId, processId, sourceId).isEmpty()) {
                    if (!processId.equals("Step") &&
                            !goalId.equals("TransportGoal") &&
                            !processId.equals("HuntingStep") &&
                            !goalId.equals("FollowGoal")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Tests the process suitability (first fills its substitution of course).
     * If the suitability value is more than the current maximum, the process
     * is a new candidate to be chosen to fulfill the goal.
     *
     * @param templateCandidate ProcessTemplate of the process to be tested
     */
    public short testProcess(Goal goal, List<IveObject> sources,
            ProcessTemplate templateCandidate) {
        TriggerTemplate suitability = templateCandidate.getSuitability();
        chosenSources = templateCandidate.getSources();
        if (!fillSubstitution(chosenSources, goal.getGoalID(),
                templateCandidate.getId(), sources, goal.getSubstitution())) {
            
            return FuzzyValueHolder.False;
        }
        
        try {
            short value = (Short)suitability.evaluate(
                    chosenSources, sensors).getData("java.Short");
            
            Log.addMessage("Testing process: "+templateCandidate.getId() +
                    " for goal " + goal.getGoalID() +
                    " suitability "+value, Log.FINER, id,
                    templateCandidate.getId(), "");
            
            return value;
        } catch (OntologyNotSupportedException ex) {
            IveApplication.printStackTrace(ex);
            Log.addMessage("Process suitability does not support ontology " +
                    "\"java.Short\".", Log.WARNING, id,
                    templateCandidate.getId(), "");
            return FuzzyValueHolder.False;
        }
    }
    
    /**
     * This method chooses approprite process for throws given goal.
     * This implementation calls p-suitability of all the processes which
     * implement this goal. The one with the best result is chosen
     *
     * @param goal For which to seek the implementation.
     * @param sources VIP sources that can be assigned as needed.
     * @param skipSet set of processes that should not be used.
     * @param preferSet set of processes that should be prefered.
     * @return The chosen process template and substitution or <code>null</code>
     *      if no appropriate process (not in skipList) exists.
     */
    public Pair<ProcessTemplate, Substitution> chooseProcess(Goal goal,
            List<IveObject> sources, Set<String> skipSet,
            Set<String> preferSet) {
        ProcessTemplate chosenTemplate = null;
        Substitution chSources = null;
        short max = 1;
        int maxRand = -1;
        boolean delegated = false;
        
        List<ProcessTemplate> processes =
                ProcessDBImpl.instance().getByGoalId(goal.getGoalID());
        if (processes != null) {
            for (ProcessTemplate processTemplate: processes) {
                if (!skipSet.contains(processTemplate.getId()) &&
                        (preferSet == null ||
                        preferSet.contains(processTemplate.getId()))) {
                    short value = testProcess(goal, sources,
                            processTemplate);
                    int rnd = (int)(Math.random() * Integer.MAX_VALUE);
                    if (value > max || (value == max && rnd > maxRand)) {
                        if (value > max || !delegated ||
                                processTemplate.isDelegated()) {
                            max = value;
                            maxRand = rnd;
                            chosenTemplate = processTemplate;
                            chSources = chosenSources;
                            delegated = processTemplate.isDelegated();
                        }
                    }
                }
            }
        }
        if (chosenTemplate != null) {
            Log.addMessage("PDecider: chooosed process is "
                    + chosenTemplate.getId() + " with a suitability "
                    + "value: " + max, Log.INFO, id, chosenTemplate.getId(),
                    "");
        }
        return new Pair<ProcessTemplate, Substitution>(chosenTemplate,
                chSources);
    }
    
    /**
     * Sets sensors available for making (P)decision.
     * @param sensors
     */
    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }
}

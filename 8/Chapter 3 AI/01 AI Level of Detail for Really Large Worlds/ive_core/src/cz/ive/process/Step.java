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
 
package cz.ive.process;

import java.util.*;

import cz.ive.ontology.*;
import cz.ive.exception.*;
import cz.ive.lod.*;
import cz.ive.trigger.*;
import cz.ive.iveobject.*;
import cz.ive.location.*;
import cz.ive.evaltree.*;
import cz.ive.evaltree.leaves.*;
import cz.ive.valueholders.*;
import cz.ive.logs.*;

/**
 *
 * @author honza
 */
public class Step extends CommonProcessTemplate {
    
    IveObject actor;
    WayPoint target;
    
    /** Creates a new instance of Step */
    public Step() {
        super();
        lod = new LOD(2, 2);
        
        SAX2Creator loader = new SAX2Creator();
        java.io.InputStream xml =
                ClassLoader.getSystemResourceAsStream("cz/ive/resources/xml/StepProcess.xml");
        Expr[] roots = loader.xml2expr(xml);
        java.util.HashMap<String,Expr>names = loader.getNames();
        
        //suitability = new EvalTreeTriggerTemplate((Expr) new QueryLeaf(new FuzzyLeaf(FuzzyValueHolder.True)));
        suitability = new EvalTreeTriggerTemplate(names.get("stepSuitability"));
        context = new EvalTreeTriggerTemplate(names.get("stepContext"));
        
        // slots creation
        sources = new SubstitutionImpl();
        sources.addSlot("actor", (Source) null,
                true, false, true);
        sources.addSlot("targetPosition", (Source) null,
                true, false, false);
    }
    
    private void updateSources(ProcessExecution execution) {
        Substitution sources = execution.getObjects();
        actor = sources.getSource("actor").getObject();
        target = (WayPoint)execution.getPhantoms().getSource(
                "localTarget").getObject();
        
        /* needed to correct the shared sources when we shrink a little and
         * we are committing some process, whose sources were changed by some
         * higher-lod stopped process. */
        if (!actor.getPosition().getId().toString().equals(
                target.getId().toString())) {
            execution.getPhantoms().getSource("targetPosition").setObject(
                    target);
        }
    }
    
    public ProcessResult atomicCommit(ProcessExecution execution) {
        updateSources(execution);

        Log.addMessage("Step process atomic commit entered at lod " +
                execution.getParameters().get("lod") +
                " to "+target.getId(), Log.FINE, actor.getId(), "Step", "");
        
        if (target.getFlatId().endsWith("_path")) {
            int k = 4;
        }
        
        boolean tooFar = true;
        WayPoint previousPosition = actor.getPosition();
        
        List<Joint> neighborous = previousPosition.getNeighbours();
        
        if (previousPosition.isParent(target) || 
                target.isParent(previousPosition)) {
            tooFar = false;
        } else if (neighborous != null) {
            for (Joint joint: neighborous) {
                if ((joint.target.isParent(target)) || 
                        (target.isParent(joint.target))) {
                    tooFar = false;
                    break;
                }
            }
        }
        
        if (tooFar) {
            Log.addMessage("Tried to move from " + actor.getPosition().getId() +
                    " to " + target.getId(), 1, actor.getId(),
                    Step.class.getName(), actor.getPosition().getId());
            return ProcessResult.FAILED;
        }
         
       
        if (target.getId().equals(previousPosition.getId())) {
            actor.setPosition(actor.getPosition());
            return ProcessResult.OK;
        }

        // Remove the object from its master not its position.
        IveObject master = actor.getMaster();
        master.removeObject(actor);

        WayPoint targetWP;
        if (null == (
                targetWP = (WayPoint)ObjectMap.instance().getObject(
                target.getId()))) {
            targetWP = (WayPoint)target.getLeastActiveParent();
        }

        if ((actor.isSubstantial()) && 
                (targetWP.getSpaceState() == WayPoint.SpaceState.OCCUPIED)) {
            Log.addMessageToObject("Step target "+targetWP.getId().toString()+
                    " is occupied, cannot move.", Log.FINE, actor);
            
            previousPosition.addObject(actor);
            return ProcessResult.OK;
        }
        
        
        if (!targetWP.placeObject(actor, previousPosition, target))
            if (!targetWP.placeObject(actor, previousPosition, null)) {
                Log.addMessage("WayPoint " + targetWP.getId() + " refused " +
                        "placement to the WayPoint " + target.getId() + 
                        " from " + previousPosition.getId(), 
                        Log.WARNING, actor.getId(), "Step", targetWP.getId());
                previousPosition.addObject(actor);
            }
        return ProcessResult.OK;
    };
    
    
    public long atomicLength(ProcessExecution execution) {
        int lodPar = ((Integer)execution.getParameters().get("lod")).intValue();
        int length;
        
        length = 1000;

        WayPoint pos = execution.getObjects().getSource(
                "actor").getObject().getPosition();
        Source src = execution.getPhantoms().getSource("localTarget");
        IveObject targetPos = null;
        if (src != null) {
            targetPos = src.getObject();
        } else {
            src = execution.getObjects().getSource("targetPostion");
            if (src != null) {
                targetPos = src.getObject();
            }
        }
        if (targetPos != null) {
            for (Joint j : pos.getNeighbours()) {
                if (j.target.getId().equals(targetPos.getId().toString())) {
                    length = (int) (j.weight * 500);
                }
            }
        }
        
        if ((execution.getObjects().getSource("targetPosition") != null) && 
                (execution.getObjects().getSource("actor").getObject().
                    getPosition().getId().equals(
                        execution.getObjects().getSource("targetPosition").
                        getObject().getId()))) {
            length = length / 10;
        }
        
        return length;
    };
    
    public LOD getLOD(ProcessExecution execution) {
        int lodPar = ((Integer)execution.getParameters().get("lod")).intValue();
        return new LOD(lodPar, lodPar);
    };
    
    public ProcessExecution execute(IveProcess process) {
        ProcessExecution processExecution = new ProcessExecutionImpl(this,
                process);
        processExecution.setParameters( process.getParameters() == null ? 
            null : new HashMap<String, Object>(process.getParameters()));
        if (processExecution.getParameters().get("lod") == null) {
            initLod(processExecution.getPhantoms(),
                    processExecution.getParameters());
        }

        IveObject trg = process.getSubstitution().getSource("targetPosition").
                getObject();
        
        if (trg != null) {
            /* remember target of this step in non-shared source (the shared
             * one is changing */
            process.getSubstitution().addSlot("localTarget", 
                    new SourceImpl(trg), false, false, false);
        }
        
        return processExecution;
    };
    
    /**
     * Initializes "lod" parameter to a value on witch is this process atomic.
     *
     * @param phantoms Phantom substitution to be used as input.
     * @param params Parameter map to be altered.
     */
    protected void initLod(Substitution phantoms, Map<String, Object> params) {
        IveObject actor = phantoms.getSource("actor").getObject();
        IveId actorLoc = ObjectMap.instance().getObject(
                actor.getId()).getPosition();
        IveId targetLoc = phantoms.getSource("targetPosition").getObject();
        IveObject comParent = actorLoc.getLeastCommonParent(targetLoc);
        IveObject actualLocLod = comParent.getChildPreceeding(actorLoc.getId());
        
        params.put("lod", new Integer(actualLocLod.getLod()));
    }
    
    /** Fills the goal substitution with the objects of the give substitution.
     *  The key - same slot name is the same and the slot hasn't been filled
     *  already -> put it there.
     * @param goal The goal which substitution is to be filled
     * @param sources Sources to look for the objects
     */
    public void fillGoalWithSources(Goal goal, Substitution sources) {
        Substitution goalSources = goal.getSubstitution();
        Set<String> goalSlots = goalSources.getSlotsKeys();
        Set<String> sourcesSlots = sources.getSlotsKeys();
        for (String goalSourceId : goalSlots) {
            for (String sourceId : sourcesSlots) {
                if (goalSourceId.equals(sourceId)
                && goalSources.getSource(sourceId) == null) {
                    goalSources.setSource(goalSourceId,
                            sources.getSource(sourceId));
                }
            }
        }
    }
    
    public OntologyToken getExpansion(Substitution sources,
            Map<String, Object> parameters){
        Goal goal = new MoveGoal();
        Map<String, Object> map = new HashMap<String, Object>(1);
        Integer lodPar = (Integer)parameters.get("lod");
        
        if (lodPar == null) {
            initLod(sources, map);
            lodPar = (Integer)map.get("lod");
        }
        lodPar = new Integer(lodPar.intValue() + 1);
        map.put("lod", lodPar);
        
        goal.setParameters(map);
        goal.getSubstitution().setSource("targetPosition", 
                sources.getSource("targetPosition"));
        goal.getSubstitution().setSource("actor", 
                sources.getSource("actor"));
        
        if (sources.getSource("localTarget").getObject() != null) { 
            goal.getSubstitution().getSource("targetPosition").setObject(  
                sources.getSource("localTarget").getObject());
        }
        
        Expansion exp = new Expansion(new Goal[] {goal});
        return exp;
    }
}

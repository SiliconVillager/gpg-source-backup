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
 
package cz.ive.simulation;

import java.util.HashMap;
import java.util.Set;
import java.io.Serializable;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import cz.ive.process.*;
import cz.ive.exception.ProcessNotRunningException;
import cz.ive.exception.HookNotPlannedException;
import cz.ive.exception.ExchangeWrongSourcesException;
import cz.ive.messaging.*;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.lod.*;
import cz.ive.logs.*;
import cz.ive.util.Pair;
import java.util.Map;


/**
 * The interpreter of the world. This class is a public singleton.
 * @author pavel
 */
public class WorldInterpreter implements Interpreter, Serializable, Updateable {

    /**
     * The LOD mode of the running process.
     */
    private enum RunningProcessMode {
        /** The lod was not yet identified */
        UNKNOWN, 
        
        /** The LOD value is too low, the process does not run at all. */
        NOT_EXIST,
        
        /** The process is running atomicaly */
        ATOMIC,
                
        /** The process is expanded to subgoals */
        EXPANDED,
                
        /** States of process requested by particular actors conflict */
        CONFLICT;
    }
        
    /**
     * Structure for remembering all parts of a running process.
     */
    private class RunningProcess implements Listener, Serializable {
        
        /** 
         * Process increaseLod was called before the actor's lod change, it's 
         * validity should be tested.
         **/
        private boolean LodIncreased;
        /** 
         * Process decreaseLod was called before the actor's lod change, it's 
         * validity should be tested.
         **/
        private boolean LodDecreased;
        
        /** Process itself */
        public IveProcess process;
        /** Process' execution */
        public ProcessExecution execution;
        /** Process' template */
        public ProcessTemplate template;
        /** Hook of the process' finish planned in Calendar */
        public Hook doneHook;
        /** Current mode of the process */
        public RunningProcessMode mode;

        /* lodHooks[i] is a hook of actor actors[i] */
        /** Actors participating on that process */
        public IveObject[] actors;
        /** Lod-change hooks of particular actors */
        public AttributeHook[] lodHooks;
        
        /** 
         * Hook of the process' finish, which is no more planned. This 
         * remembers expired or canceled hooks for possible reuse.
         */
        private Hook dummyHook;

        /** Creates a new instance of RunningProcess */
        public RunningProcess(IveProcess process) {
            this.process = process;
            this.execution = null;
            this.template = null;
            doneHook = null;
            actors = null;
            lodHooks = null;
            dummyHook = null;
            LodIncreased = false;
            LodDecreased = false;
        }

        /** The calendar hook cancel callback */
        public void canceled(cz.ive.messaging.Hook initiator) {
            /* Process stopped, nothing to do here (cleaning implemented 
             * in the destroy() method which cause this call) */
        }

        /** 
         * Finishes the process, cleans all registered callbacks.
         * @param result Result of the finishing process
         */
        public void destroy(ProcessResult result) {
            
            for (int i=0; i<lodHooks.length; i++) {
                lodHooks[i].unregisterListener(this);
            }
            
            removeProcessFromSources(process, execution.getObjects());
            running.remove(process);  
            if (template.isRendezvous()) {
                rendezvousProcesses.remove(this);
            }
            unplanFinish();
            
            if (dirtyLod.contains(this)) {
                dirtyLod.remove(this);
            }
            
            if (mode != RunningProcessMode.EXPANDED) {
                finished.add(
                        new Pair<IveProcess, ProcessResult>(process, result));
            }
        }
        
        /**
         * Plans the finish of an atomic process to the calendar. 
         * The remaining time is queried inside.
         * The process should have ATOMIC mode.
         */
        public void planFinish() {
            long length;
            
            length = template.atomicLength(execution);
            
            if (doneHook != null) {
                CalendarPlanner.instance().replan(doneHook, length, false);
                return;
            }
            
            if (dummyHook == null) {
                doneHook = CalendarPlanner.instance().plan(length);
            } else {
                doneHook = dummyHook;
                dummyHook = null;
                CalendarPlanner.instance().replan(doneHook, length, true);
            }
            doneHook.registerListener(this);                
        }
        
        /**
         * Unplans the finish of an atomic process from the calendar. 
         */
        public void unplanFinish() {

            if (doneHook != null) {
                try {
                    CalendarPlanner.instance().cancelHook(doneHook);
                    doneHook.unregisterListener(this);
                }
                catch (HookNotPlannedException e) {
                    /* This is strange but not fatal - we are stopping process, 
                     * which has not planned it's finish, but holds some hook */
                    Log.warning("unplanned process has doneHook, "+
                            "which is not planned");
                }
            }
            dummyHook = doneHook;
            doneHook = null;
        }
        
        /** Check all actors and update process' lod */
        public void updateLod() {
                RunningProcessMode newMode;
                
                newMode = requestedProcessMode(template, actors, execution);

                switch (newMode) {
                    case UNKNOWN:
                        Log.severe("No actor substituted to process");
                        destroy(ProcessResult.WRONG_SOURCES);
                        break;

                    case NOT_EXIST:
                        destroy(ProcessResult.LOD_TOO_LOW);
                        break;

                    case ATOMIC:
                        if (mode == RunningProcessMode.EXPANDED) {
                            
                            if (LodIncreased) {
                                destroy(ProcessResult.ACTOR_CONFUSION);
                            } else {
                                if (! LodDecreased) {
                                    template.decreaseLOD(execution);
                                }
                                LodDecreased = false;
                                process.shrink();
                                if (running.containsKey(process)) {
                                    /* the process could be stopped as a 
                                     * reaction on shrink */
                                    planFinish();
                                }
                            }
                        } else {
                            if (LodIncreased || LodDecreased) {
                                destroy(ProcessResult.ACTOR_CONFUSION);
                            }
                        }
                        break;
                    case EXPANDED:
                        if (template.isRendezvous()) {
                            destroy(ProcessResult.LOD_TOO_HIGH);
                            return;
                        }
                        if (mode == RunningProcessMode.ATOMIC) {
                            if (LodDecreased) {
                                destroy(ProcessResult.ACTOR_CONFUSION);
                            } else {
                                unplanFinish();
                                if (! LodIncreased) {
                                    template.increaseLOD(execution);
                                }
                                LodIncreased = false;
                                process.expand();
                            }
                        } else {
                            if (LodIncreased || LodDecreased) {
                                destroy(ProcessResult.ACTOR_CONFUSION);
                            }
                        }
                        break;
                    case CONFLICT:
                        destroy(ProcessResult.ACTOR_CONFUSION);
                        break;
                }
                
                mode = newMode;
        }
        
        /**
         * Calls increaseLod before the actor's lod and process' mode actually
         * change. This action is remembered to check it's validity later
         * (in the time of physical lod change)
         */
        public void preIncreaseLod() {
            if (LodIncreased) {
                return;
            }
            if (LodDecreased) {
                destroy(ProcessResult.ACTOR_CONFUSION);
            }
            template.increaseLOD(execution);
            LodIncreased = true;

            /* make sure the change will be checked */
            if (! dirtyLod.contains(this)) {
                dirtyLod.add(this);
            }
        }
        
        /**
         * Calls decreaseLod before the actor's lod and process' mode actually
         * change. This action is remembered to check it's validity later
         * (in the time of physical lod change)
         */
        public void preDecreaseLod() {
            if (LodDecreased) {
                return;
            }
            if (LodIncreased) {
                destroy(ProcessResult.ACTOR_CONFUSION);
            }
            template.decreaseLOD(execution);
            LodDecreased = true;

            /* make sure the change will be checked */
            if (! dirtyLod.contains(this)) {
                dirtyLod.add(this);
            }
        }
        
        /** The calendar hook callback */
        public void changed(cz.ive.messaging.Hook initiator) {

            ProcessResult result;
            long length;

            if (initiator == doneHook) {
                /* finishing the process */
                
                doneHook = null;
                if (mode != RunningProcessMode.ATOMIC) {
                    /* This is strange, no doneHook should be present */
                    Log.warning("Finish of non-atomic procss signaled");
                    return;
                }
                
                result = template.atomicCommit(execution);
                execution.setLastCommitTime (
                        CalendarPlanner.instance().getSimulationTime());

                if (result == ProcessResult.RUNNING) {
                    planFinish();
                } else {
                    destroy(result);
                }
            } else {
                /* some actor signals change of it's lod */
                int source;
                for (source=0; source<lodHooks.length; source++) {
                    if (initiator == lodHooks[source]) {
                        break;
                    }
                }
                if (source == lodHooks.length) {
                    Log.warning(
                            "Running process signaled by non-remembered hook");
                }
                
                if (! dirtyLod.contains(this)) {
                    dirtyLod.add(this);
                }
            }
        }
        
        /** Stringify contained info. */
        public String toString() {
            String actors = "";
            String sep = "";
            
            for (Map.Entry<String, Slot> entry : 
                execution.getPhantoms().getActorSlots().entrySet()) {
                    
                Source src = entry.getValue().getSource();
                
                if (src != null) {
                    IveObject obj = src.getObject();
                    actors += sep + "\"" + entry.getKey() + "\" = " + 
                            (obj == null ? "NULL" : obj.getId());
                    sep = ", ";
                }
            }
            
            return "Atomic commit: " + template.getId() + 
                    " (" + actors + ")";
        }
    }
    
    /** The static reference to the interpreter singleton */
    static private WorldInterpreter interpreter;
    
    /** The set of the running processes. */
    private HashMap<IveProcess, RunningProcess> running;
    
    /** 
     * Remembers all processes, whose actor signalled lod change, but the
     * process didn't process it yet.
     */
    private Vector<RunningProcess> dirtyLod;

    /** 
     * Remembers all processes, which are to be finished at the end of this 
     * round.
     */
    private Vector<Pair<IveProcess, ProcessResult>> finished;

    /**
     * Running rendez-vous processes.
     */
    private Vector<RunningProcess> rendezvousProcesses;
    
    /** Creates a new instance of WorldInterpreter */
    protected WorldInterpreter() {
        running = new HashMap<IveProcess, RunningProcess>();
        dirtyLod = new Vector<RunningProcess>();
        finished = new Vector<Pair<IveProcess, ProcessResult>>();
        rendezvousProcesses = new Vector<RunningProcess>();
    }
    
    /** Returns the single instance of the interpreter */
    static public WorldInterpreter instance() {
        if (interpreter == null) {
            interpreter = new WorldInterpreter();
        }
        
        return interpreter;
    }
    
    /** 
     * Empty whole the Interpreter before the XML load. We just drop 
     * the singleton and create a new one.
     */
    static public synchronized void emptyInstance() {
        interpreter = new WorldInterpreter();
    }
    
    /** 
     * Returns the single instance of the WorldInterpreter, or null if it does 
     * not exist yet 
     */
    static public synchronized WorldInterpreter getInstance() {
        return interpreter;
    }

    /** 
     * Sets the static reference to the singleton. This method should be called
     * only in case of loading the singleton object from the serialized stream
     * @param instance Reference to the new instance
     */
    static public synchronized void setInstance(WorldInterpreter instance) {
       interpreter = instance;
    }

    /**
     * Implements method from interface Updateable.
     * Checks if some processes were signaled to update their lod (by their
     * actors).
     * @return true if some processes need to be updated;
     *  false if lod value of all processes is up to date
     */
    public boolean needUpdate() {
        return (!dirtyLod.isEmpty()) || (!finished.isEmpty());
    }
    
    /**
     * Implements method from interface Updateable.
     * Updates lod of all processes that were signaled by their actors.
     */
    public void update() {
        RunningProcess p;
        Pair<IveProcess, ProcessResult> fp;

        while ((!dirtyLod.isEmpty()) || (!finished.isEmpty())) {
         
            while (! finished.isEmpty()) {
                try {
                    fp = finished.remove(0);
                } catch (ArrayIndexOutOfBoundsException e) {
                    /* This will never happen */
                    return;
                }
                fp.first().finish(fp.second());
            }

            while (! dirtyLod.isEmpty()) {
                try {
                    p = dirtyLod.remove(0);
                } catch (ArrayIndexOutOfBoundsException e) {
                    /* This will never happen */
                    return;
                }
                p.updateLod();
            }
        }
    }
    
    /**
     * Tells to all sources that they are participating on the given process.
     * @param process Executing process
     * @param sources Process' sources
     */
    protected void addProcessToSources(IveProcess process, 
            Substitution sources) {
        
        Set<String> keys;
        ArrayList<Source> srcs;
        
        keys = sources.getSlotsKeys();       
        for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            srcs = sources.getSourceArray(i.next());
            if (srcs == null)
                continue;
            for (Iterator<Source> j = srcs.iterator(); j.hasNext(); ) {
                IveObject object = j.next().getObject();
                if (object != null)
                    object.addProcess(process);
            }
        }

        keys = sources.getActorSlotsKeys();       
        for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            srcs = sources.getSourceArray(i.next());
            for (Iterator<Source> j = srcs.iterator(); j.hasNext(); ) {
                j.next().getObject().addActorProcess(process);
            }
        }
    }

    /**
     * Tells to all sources that they are no longer participating on the given 
     * process.
     * @param process Finishing process
     * @param sources Process' sources
     */
    protected void removeProcessFromSources(IveProcess process, 
            Substitution sources) {
        
        Set<String> keys;
        ArrayList<Source> srcs;
        
        keys = sources.getSlotsKeys();       
        for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            srcs = sources.getSourceArray(i.next());
            if (srcs == null)
                continue;
            for (Iterator<Source> j = srcs.iterator(); j.hasNext(); ) {
                IveObject object = j.next().getObject();
                if (object != null)
                    object.removeProcess(process);
            }
        }

        keys = sources.getActorSlotsKeys();       
        for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            srcs = sources.getSourceArray(i.next());
            for (Iterator<Source> j = srcs.iterator(); j.hasNext(); ) {
                j.next().getObject().removeActorProcess(process);
            }
        }
    }
    
    /**
     * Gets mode of the process required by the given actors.
     * @param process Template of the process
     * @param actors Actors participating on that process
     * @return Mode of the process required by the actor's current LOD value.
     */
    protected RunningProcessMode requestedProcessMode(ProcessTemplate process, 
                    IveObject[] actors, ProcessExecution execution) {
        
        int i;
        RunningProcessMode result = RunningProcessMode.UNKNOWN;
        int actorLOD;
        LOD processLOD;
        
        for (i=0; i<actors.length; i++) {
            
            actorLOD = ((AttrInteger) actors[i].getAttribute("lod")).getValue();
            processLOD = process.getLOD(execution);

            if (actorLOD < processLOD.getMin()) {
                if (result == RunningProcessMode.UNKNOWN) {
                    result = RunningProcessMode.NOT_EXIST;
                }
                if (result != RunningProcessMode.NOT_EXIST) {
                    result = RunningProcessMode.CONFLICT;
                }
            } else if (actorLOD <= processLOD.getMax()) {
                if (result == RunningProcessMode.UNKNOWN) {
                    result = RunningProcessMode.ATOMIC;
                }
                if (result != RunningProcessMode.ATOMIC) {
                    result = RunningProcessMode.CONFLICT;
                }
            } else {
                if (result == RunningProcessMode.UNKNOWN) {
                    result = RunningProcessMode.EXPANDED;
                }
                if (result != RunningProcessMode.EXPANDED) {
                    result = RunningProcessMode.CONFLICT;
                }
            }
            
            if (result == RunningProcessMode.CONFLICT) {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Tests whether the process will change it's state to the given one when 
     * one of it's actors change lod.
     * This function is to be used when preIncreasing or preDecreasing.
     * @param process The tested process
     * @param lod The assumed lod value
     * @return true if the mode will change and the result mode will be 
     * the given one
     */
    protected boolean wouldChangeModeTo(IveProcess process, int lod, 
            RunningProcessMode mode) {
        RunningProcess p;
        RunningProcessMode currMode;
        RunningProcessMode reqMode;
        LOD processLod;
        
        p = running.get(process);
        if (p == null) {
            return false;
        }
        currMode = p.mode;
        processLod = p.template.getLOD(p.execution);
        
        reqMode = RunningProcessMode.UNKNOWN;
        if (lod < processLod.getMin()) {
            reqMode = RunningProcessMode.NOT_EXIST;
        } else if (lod <= processLod.getMax()) {
            reqMode = RunningProcessMode.ATOMIC;
        } else {
            reqMode = RunningProcessMode.EXPANDED;
        }
        
        if ((reqMode != currMode) && (reqMode == mode)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Each process which will expand due to increasing of objet's lod will
     * be asked to prepare for this action (to call increaseLod).
     * Implements the Interpreter interface.
     * @param object changing it's lod
     */
    public void preIncreaseLod(IveObject object) {
       Object[] processes;
       IveProcess process;
       
       processes = object.getActorProcesses().toArray();
       for (Object p : processes) {
           process = (IveProcess) p;
           if (wouldChangeModeTo(process, object.getLod()+1, 
                   RunningProcessMode.EXPANDED)) {
               running.get(process).preIncreaseLod();
           }
       }
    }
    
    /**
     * Each process which will become atomic due to increasing of objet's lod 
     * will be asked to prepare for this action (to call decreaseLod).
     * Implements the Interpreter interface.
     * @param object changing it's lod
     */
    public void preDecreaseLod(IveObject object) {
       Object[] processes;
       IveProcess process;
       
       processes = object.getActorProcesses().toArray();
       for (Object p : processes) {
           process = (IveProcess) p;
           if (wouldChangeModeTo(process, object.getLod()+1, 
                   RunningProcessMode.ATOMIC)) {
               running.get(process).preDecreaseLod();
           }
       }
    }

    /**
     * Tests whether the given processes are rendezvous companions.
     * The test is positive, if both processes are RendezVous, have the same
     * id and have switched "actor" and "who" sources.
     * @param process1 The first process
     * @param process2 The second process
     * @return true iff they are a rendezvous companions
     */
    boolean areRendezvousCompanions(RunningProcess process1, 
            RunningProcess process2) {
       if (!process1.template.isRendezvous() || 
                !process2.template.isRendezvous()) {
           return false;
       }
       
       if (!process1.process.getProcessId().equals(
               process2.process.getProcessId())) {
           return false;
       }
       
       if (!process1.execution.getPhantoms().getSource("actor").
               getObject().getId().equals(
                    process2.execution.getPhantoms().getSource("who").
                        getObject().getId())) {
           return false;
       }

       if (!process1.execution.getPhantoms().getSource("who").
               getObject().getId().equals(
                    process2.execution.getPhantoms().getSource("actor").
                        getObject().getId())) {
           return false;
       }
       
       return true;
    }
    
    /**
     * Begin execution of given process
     * @param process Instance of a Process. Interpreter should get 
     *      the ProcesstTemplate by searching the ProcessDB not directly 
     *      from given the Process. This should prevent geniuses from accessing
     *      real objects by passing its own ProcessTemplate.
     */
    public void execute(IveProcess process) {
        
        RunningProcess newProcess;
        ProcessDB processDB;
        CalendarPlanner calendarPlanner;
        Substitution translatedSubst, oldSubst;
        Set<String> keys;
        long length;
        int actorNum;
        IveObject actor;
        
        newProcess = new RunningProcess(process);
        newProcess.template = 
               ProcessDBImpl.instance().getByProcessId(process.getProcessId());
        newProcess.execution = newProcess.template.execute(process);
        

        oldSubst = newProcess.process.getSubstitution();
        translatedSubst = ObjectMap.instance().translate(oldSubst);
        
        if ((translatedSubst == null) || 
                    (!translatedSubst.checkSubstitution(oldSubst))) {

            newProcess.process.finish(ProcessResult.WRONG_SOURCES);
            return;    
        }
        
        newProcess.execution.setObjects(translatedSubst);
        
        keys = newProcess.execution.getObjects().getActorSlotsKeys();
        newProcess.actors = new IveObject[keys.size()];
        newProcess.lodHooks = new AttributeHook[keys.size()];

        actorNum = 0;
        for (java.util.Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            
            actor = newProcess.execution.getObjects().getSource(i.next()).
                        getObject();
            
            newProcess.actors[actorNum] = actor;
            newProcess.lodHooks[actorNum] = actor.getAttribute("lod");
            actorNum++;
        }
        
        newProcess.mode = requestedProcessMode(newProcess.template, 
                    newProcess.actors, newProcess.execution);
        newProcess.execution.setStartTime(
                CalendarPlanner.instance().getSimulationTime());

        switch (newProcess.mode) {
            case UNKNOWN:
                Log.severe("No actor substituted to process");
                newProcess.destroy(ProcessResult.WRONG_SOURCES);
                break;

            case NOT_EXIST:
                newProcess.destroy(ProcessResult.LOD_TOO_LOW);
                break;
                
            case ATOMIC:
                if (newProcess.template.isRendezvous()) {
                    for(RunningProcess companion : rendezvousProcesses) {
                        if (areRendezvousCompanions(newProcess, companion)) {
                            ProcessResult res = ((RendezvousProcessTemplate) 
                                newProcess.template).rendezvous(
                                    newProcess.execution, companion.execution); 
                            if (res == ProcessResult.RUNNING) {
                                newProcess.planFinish();
                                companion.planFinish();
                                break;
                            }
                            newProcess.destroy(res);
                            companion.destroy(res);
                            return;
                        }
                    }
                    rendezvousProcesses.add(newProcess);
                }

                for (int i=0; i<newProcess.lodHooks.length; i++) {
                    newProcess.lodHooks[i].registerListener(newProcess);
                }
                newProcess.planFinish();
                running.put(newProcess.process, newProcess);
                addProcessToSources(newProcess.process, translatedSubst);
                break;
            case EXPANDED:
                if (newProcess.template.isRendezvous()) {
                    newProcess.destroy(ProcessResult.LOD_TOO_HIGH);
                    break;
                }
                for (int i=0; i<newProcess.lodHooks.length; i++) {
                    newProcess.lodHooks[i].registerListener(newProcess);
                }
                newProcess.template.increaseLOD(newProcess.execution);
                newProcess.process.expand();
                running.put(newProcess.process, newProcess);
                addProcessToSources(newProcess.process, translatedSubst);
                break;
            case CONFLICT:
                newProcess.destroy(ProcessResult.ACTOR_CONFUSION);
                break;
        }
        
    }
    
    /**
     * Stops the process. This does not mean, that the process is stopped after
     * return from this function. The process is stopped when it signals
     * on the FinishHook. This may (but does not have to) occur during
     * execution of this method.
     * If the process was not started by calling execute() method, the 
     * ProcessNotRunningException will be thrown.
     * @param process The process, which will be stopped.
     */
    public void stop(IveProcess process) throws ProcessNotRunningException {

        RunningProcess p;
        CalendarPlanner calendarPlanner;
        ProcessResult result;
        long length;

        for (Pair<IveProcess, ProcessResult> fp : finished) {
            if (fp.first() == process) {
                return;
            }
        }
        
        p = running.get(process);

        if (p == null) {
            throw new ProcessNotRunningException();
        }

        if (p.mode == RunningProcessMode.EXPANDED) {
            p.destroy(null);
            return;
        }
        
        p.unplanFinish();
        
        result = p.template.atomicStop(p.execution);
            
        if (result == ProcessResult.RUNNING) {
            p.planFinish();
        } else {
            p.destroy(result);
        }
            
    }

    /**
     * Exchanges sources in the process and it's execution, inform all
     * involved participants about this change.
     * @param process Process whose sources are exchanged
     * @param slotId Id of the changed slot 
     * @param newSources New sources to substitute to the slot
     * @param array Determines whether the new value is a single source (false)
     *              or a source array (true). In case of single source, the 
     *              first member of the newSources array will be used as the 
     *              source.
     */
    protected void exchangeSourcesInternal(IveProcess process, String slotId, 
            ArrayList<Source> newSources, boolean array) 
            throws ExchangeWrongSourcesException {
        RunningProcess p = running.get(process);
        Substitution subst;
        Substitution trSubst;
        boolean actor;
        
        if (p == null) {
            return;
        }
        
        subst = process.getSubstitution();
        actor = subst.getActorSlotsKeys().contains(slotId);
        if (actor) {
            /* this would cause lod problems */
            Log.severe("Trying to exchange actors!");
            throw new ExchangeWrongSourcesException();
        }
        
        /* tell to old process sources they are no more participating on this 
           process */
        ArrayList<Source> processSources = subst.getSourceArray(slotId);
        if (processSources != null) {
            for (Source s : processSources) {
                s.getObject().removeProcess(process);
            }
        }
        
        /* exchange sources in process and inform the new sources */
        if (array) {
            subst.setSource(slotId, newSources);
            for (Source s : newSources) {
                s.getObject().addProcess(process);
            }
        } else {
            Source s = newSources.get(0);
            subst.setSource(slotId, s);
            s.getObject().addProcess(process);
        }
        
        /* exchange source in execution */
        trSubst = ObjectMap.instance().translate(subst);

        if ((trSubst == null) || 
                    (!trSubst.checkSubstitution(subst))) {
            Log.severe("Exchanging untranslatable sources.");
            throw new ExchangeWrongSourcesException();
        }

        subst = p.execution.getObjects();
        subst.setSource(slotId, trSubst.getSource(slotId));        
    }
    
    public void exchangeSources(IveProcess process, String slotId, 
            ArrayList<Source> newSources) 
            throws ExchangeWrongSourcesException {
        exchangeSourcesInternal(process, slotId, newSources, true);
    }

    public void exchangeSources(IveProcess process, String slotId, 
            Source newSource) 
            throws ExchangeWrongSourcesException {
        ArrayList<Source> list = new ArrayList<Source>();
        list.add(newSource);
        exchangeSourcesInternal(process, slotId, list, false);
    }
    
}

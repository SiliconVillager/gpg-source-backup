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
import cz.ive.lod.*;
import cz.ive.exception.*;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.*;
import cz.ive.logs.Log;
import cz.ive.trigger.*;
import cz.ive.sensors.*;
import cz.ive.template.TemplateMap;
import cz.ive.xmlload.ObjectTemplate;

/**
 * Common template for processes. In all subclasses, you should override
 * at least atomicCommit, atomicStop and atomicLength methods, to provide
 * functionality no metter when they are called. For example, the atomicCommit
 * should not expect that it is called exactly after the atomicLength returned
 * time.
 *
 * @author honza
 */
public abstract class CommonProcessTemplate implements ProcessTemplate,
        OntologyToken,
        java.io.Serializable {
    
    protected LOD lod;
    protected String goalId;
    protected String processId;
    protected ExpansionProducer expansionCrt;
    protected TriggerTemplate context;
    protected TriggerTemplate suitability;
    protected Substitution sources;
    
    protected Expansion expansion;
    
    /** Created objects counter for prettier naming. */
    private int tmp = 1;
    
    /** Creates a new instance of CommonProcessTemplate */
    public CommonProcessTemplate()  {
    }
    
    /**
     * Fills class fields by given values.
     */
    public void initMembers(
            String goalId, String processId, Substitution sources,
            ExpansionProducer expansionCrt, TriggerTemplate suitability,
            TriggerTemplate pContext, LOD lod) {
        this.goalId = goalId;
        this.processId = processId;
        this.expansionCrt = expansionCrt;
        this.sources = sources;
        this.suitability = suitability;
        this.context = pContext;
        this.lod = lod;
    };
    
    public String getGoalId() {
        return goalId;
    }
    
    public String getId() {
        return processId;
    }
    
    /**
     * Registers the process by the goal id as well as by the process id.
     */
    protected void registerProcessGoal() {
        ProcessDBImpl.instance().setByProcessId(getId(), this);
        ProcessDBImpl.instance().setByGoalId(getGoalId(), this);
    };
    
    public boolean supports(String ontology) {
        if (ontology.equals("jBRP.processTemplate")) {
            return true;
        } else {
            return false;
        }
    };
    
    public Object getData(String ontology) throws 
            OntologyNotSupportedException {
        
        if (ontology.equals("jBRP.processTemplate")) {
            return this;
        } else {
            throw new OntologyNotSupportedException();
        }
    };
    
    public String[] getOntologies() {
        String [] ontos = {"jBRP.processTemplate"} ;
        return ontos;
    };
    
    
    public LOD getLOD(ProcessExecution execution) {
        return lod;
    };
    
    public TriggerTemplate getSuitability() {
        return suitability;
    };
    
    public TriggerTemplate getContext() {
        return context;
    };
    
    
    public IveProcess instantiate(Substitution sources,
            Map<String, Object> parameters, List<Sensor> sensors) {
        return new ProcessImpl(getId(), getGoalId(),
                sources, parameters, context.instantiate(sources, sensors, 
                parameters));
    };
    
    public ProcessExecution execute(IveProcess process) {
        return new ProcessExecutionImpl(this, process);
    };
    
    public Substitution getSources() {
        return sources.duplicateSubstitution(true);
    };
    
    
    public long atomicLength(ProcessExecution execution) {
        return 0;
    };
    
    
    protected ProcessResult atomicCommitWork(ProcessExecution execution) throws
            AtomicCommitException {
        return null;
    }
    
    
    public ProcessResult atomicCommit(ProcessExecution execution) {
        try{
            return atomicCommitWork(execution);
        } catch (AtomicCommitException e){
            return e.getResult();
        }
    };
    
    public ProcessResult increaseLOD(ProcessExecution execution) {
        // atomicCommit(execution);
        return atomicStop(execution);
    }
    
    public void decreaseLOD(ProcessExecution execution) {
        return;
    }
    
    public ProcessResult atomicStop(ProcessExecution execution)  {
        return ProcessResult.OK;
    };
    
    public OntologyToken getExpansion(Substitution sources,
            Map<String, Object> parameters){
        if (expansionCrt!=null)
            return expansionCrt.newExpansion(sources);
        return null;
        
    }
    
    public boolean isDelegated() {
        return false;
    }
    
    public boolean isRendezvous() {
        return false;
    }
    
    /**
     * Helper method for easier getting of IveObjects from sources.
     *
     * @param role Role of the sources to be retrieved.
     * @param real <CODE>true</CODE> iff we wnat the real object, otherwise
     *      it is false.
     * @param pe ProcessExecution with info about sources substitution.
     * @return requested source object or <CODE>null</CODE> if not present
     *      in the substitution on any level of indirection.
     */
    protected IveObject getSourceObject(String role, boolean real,
            ProcessExecution pe) {
        Substitution subst = real ?
            pe.getObjects() : pe.getPhantoms();
        Slot slot = subst.getSlots().get(role);
        if (slot == null)
            return null;
        Source src = slot.getSource();
        if (src == null)
            return null;
        return src.getObject();
    }
    
    /**
     * Helper method for easier getting of IveObjects from sources.
     * If the returned Object would be null it throws an exception
     * This exception is typically caught by atomicCommit and causes
     * process failure.
     *
     * @param role Role of the sources to be retrieved.
     * @param real <CODE>true</CODE> iff we wnat the real object, otherwise
     *      it is false.
     * @param pe ProcessExecution with info about sources substitution.
     * @return requested source object or <CODE>null</CODE> if not present
     *      in the substitution on any level of indirection.
     */
    protected IveObject getCheckedSourceObject(String role, boolean real,
            ProcessExecution pe) throws AtomicCommitException {
        
        IveObject ret = getSourceObject(role, real, pe);
        
        if (ret==null) {
            Log.addMessage("Empty source "+role, Log.WARNING,"", processId, "");
            throw new AtomicCommitException(ProcessResult.WRONG_SOURCES);
        }
        return ret;
    }
    
    
    /**
     * Helper method for easier getting of IveObjects from sources.
     * If the returned Object would be null it throws an exception.
     * Moreover it checks the class of the object and throws an exception if
     * this check fails
     * This exception is typically caught by atomicCommit and causes
     * process failure.
     *
     * @param role Role of the sources to be retrieved.
     * @param real <CODE>true</CODE> iff we wnat the real object, otherwise
     *      it is false.
     * @param pe ProcessExecution with info about sources substitution.
     * @return requested source object or <CODE>null</CODE> if not present
     *      in the substitution on any level of indirection.
     */
    protected IveObject getCheckedSourceObject(String role, String objClass,
            boolean real, ProcessExecution pe) throws AtomicCommitException {
        
        IveObject ret = getCheckedSourceObject(role, real, pe);
        
        if (!ObjectClassTree.instance().getObjectClass(
                objClass).isInside(ret.getObjectClass())) {
            LogWarning("Unexpected object class in source "+role);
            throw new AtomicCommitException(ProcessResult.WRONG_SOURCES);
        }
        return ret;
    }
    
    /**
     * Helper method for easier getting the parameter in particular ontology
     * from process execution.
     * If there is no such parameter or it does not support given ontology
     * an exception is thrown.
     * This exception is typically caught by atomicCommit and causes
     * process failure.
     *
     * @param name parameter name
     * @param paramOntology parameter ontology
     * @param pe ProcessExecution with info about sources substitution.
     * @return object returned by the ontology token that holds wanted parameter
     */
    protected Object getCheckedParameter(String name,
            String paramOntology, ProcessExecution pe) throws
            AtomicCommitException {
        
        OntologyToken ot = (OntologyToken) pe.getParameters().get(name);
        if (ot==null) {
            LogWarning("Missing process parameter "+name);
            throw new AtomicCommitException(ProcessResult.FAILED);
        }
        try {
            return ot.getData("java.int");
        } catch(OntologyNotSupportedException ex){
            LogWarning("Wrong ontology of process parameter "+name);
            throw new AtomicCommitException(ProcessResult.FAILED);
        }
    }
    
    
    /**
     * Clean the given source ( set it to be null ) from the phantom substitution
     * @param role Role of the sources to be retrieved.
     * @param pe ProcessExecution with info about sources substitution.
     */
    protected void cleanPhantomSource(String role,ProcessExecution pe) {
        Source src = pe.getPhantoms().getSource(role);
        if (src == null) {
            LogWarning("We expected source "+role+" to be present.");
        }
        src.setObject(null);
    }
    
    /**
     * Get the object"s attribute. It throws an exception if there is not
     * such attribute in the object.
     * @param obj object whose attribute we want to get
     * @param attr attribute path
     */
    protected AttributeValue getAttribute(IveObject obj,String attr) throws 
            AtomicCommitException {
        
        AttributeValue val = obj.getAttribute(attr);
        if (val == null) {
            LogWarning("Missing attribute "+attr,obj);
            throw new AtomicCommitException(ProcessResult.FAILED);
        }
        return val;
    }
    
    /**
     * Get the object"s fuzzy attribute. It throws an exception if there is not
     * such attribute in the object or if the attribute is not fuzzy
     * @param obj object whose attribute we want to get
     * @param attr attribute path
     */
    protected AttrFuzzy getFuzzyAttribute(IveObject obj,String attr) throws 
            AtomicCommitException {
        
        try {
            AttrFuzzy val = (AttrFuzzy)getAttribute(obj,attr);
            return val;
        } catch (ClassCastException ex){
            LogWarning("Wrong attribute "+attr+" type",obj);
            throw new AtomicCommitException(ProcessResult.FAILED);
        }
    }
    
    /**
     * Get the object"s object attribute. It throws an exception if there is not
     * such attribute in the object or if the attribute is not fuzzy.
     *
     * @param obj object whose attribute we want to get
     * @param attr attribute path
     */
    protected AttrObject getObjectAttribute(IveObject obj,String attr) throws 
            AtomicCommitException {
        
        try {
            AttrObject val = (AttrObject)getAttribute(obj,attr);
            return val;
        } catch (ClassCastException ex){
            LogWarning("Wrong attribute "+attr+" type",obj);
            throw new AtomicCommitException(ProcessResult.FAILED);
        }
    }
    
    /**
     * Checks whether the object is member of given class.
     * It throws an exception if it is not a member.
     *
     * @param o object whose class we want to check.
     * @param objClass the given object should be a member.
     */
    protected void checkObjectClass(IveObject o ,String objClass) throws 
            AtomicCommitException {
        
        if (!ObjectClassTree.instance().getObjectClass(
                objClass).isInside(o.getObjectClass())) {
            LogWarning("Unexpected object class", o);
            throw new AtomicCommitException(ProcessResult.WRONG_SOURCES);
        }
    }
    
    
    /**
     * Get the object"s integer attribute. It throws an exception if there 
     * is not such attribute in the object or if the attribute is not fuzzy
     *
     * @param obj object whose attribute we want to get
     * @param attr attribute path
     */
    protected AttrInteger getIntegerAttribute(IveObject obj,String attr) throws 
            AtomicCommitException {
        
        try {
            AttrInteger val = (AttrInteger)getAttribute(obj,attr);
            return val;
        } catch (ClassCastException ex){
            LogWarning("Wrong attribute \""+attr+"\" type",obj);
            throw new AtomicCommitException(ProcessResult.FAILED);
        }
    }
    
    
    /**
     * Create a new object.
     *
     * @param obj object that wants to create another object; typically actor.
     *            It is used as parent of new object and for logging
     * @param solidName part of new object name. It is concated by _tmp and
     *        random value.
     * @param templateName name of template used to create new object
     */
    protected IveObject createObject(IveObject obj, String solidName,
            String templateName) throws AtomicCommitException {
        
        ObjectTemplate temp = (ObjectTemplate)TemplateMap.instance().
                getTemplate(templateName);
        
        if (temp == null) {
            LogSevere(templateName+" template not found.",obj);
            throw new AtomicCommitException(ProcessResult.FAILED);
        }
        
        String idbase = obj.getPosition().getId() + IveId.SEP + solidName;
        String id = null;
        for (;;) {
            id = idbase + String.valueOf(tmp++);
            if (ObjectMap.instance().canRegister(id)) {
                break;
            }
        }
        IveObject ret = temp.instantiate(id);
        
        if (ret == null) {
            LogSevere("Unable to create the "+solidName+" object.",obj);
            throw new AtomicCommitException(ProcessResult.FAILED);
        }
        return ret;
    }
    
    /**
     * Logs the warning message. Uses information that CommonProcessTemplate 
     * has got in its members to simplify logging in user defined actions. 
     *
     * @param text text of log message
     * @param obj that the message is related to.
     */
    protected void LogWarning(String text, IveObject obj) {
        Log.addMessage(text, Log.WARNING,obj.getId(), processId,
                obj.getPosition().getId());
    }
    
    
    /**
     * Logs the warning message. Uses information that CommonProcessTemplate 
     * has got in its members to simplify logging in user defined actions. 
     *
     * @param text text of log message
     */
    protected void LogWarning(String text) {
        Log.addMessage(text, Log.WARNING,"", processId, "");
    }
    
    /**
     * Logs the severe message. Uses information that CommonProcessTemplate 
     * has got in its members to simplify logging in user defined actions. 
     *
     * @param text text of log message
     * @param obj that the message is related to.
     */
    protected void LogSevere(String text, IveObject obj) {
        Log.addMessage(text, Log.SEVERE,obj.getId(), processId,
                obj.getPosition().getId());
    }    
    
    /**
     * Logs the severe message. Uses information that CommonProcessTemplate 
     * has got in its members to simplify logging in user defined actions. 
     *
     * @param text text of log message
     */
    protected void LogSevere(String text) {
        Log.addMessage(text, Log.SEVERE,"", processId, "");
    }
}

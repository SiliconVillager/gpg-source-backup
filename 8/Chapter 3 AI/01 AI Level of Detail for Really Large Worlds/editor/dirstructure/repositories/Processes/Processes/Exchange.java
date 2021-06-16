/* 
 *
 * IVE Editor 
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
import cz.ive.IveApplication;
import cz.ive.exception.OntologyNotSupportedException;
import cz.ive.iveobject.*;
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.iveobject.attributes.AttrObject;
import cz.ive.logs.Log;
import cz.ive.ontology.OntologyToken;

/**
 * Basic process template for the rendezvous process exchanging objects.
 *
 * @author Ondra
 */
public class Exchange extends CommonProcessTemplate
        implements RendezvousProcessTemplate {
    
    /** Default timeout time of 5 s-second */
    protected static final int TIMEOUT = 60000;
    
    /** Default exchange duration of 1 s-second */
    protected static final int LENGTH = 1000;
    
    /** Creates a new instance of Exchange */
    public Exchange() {
    }
    
    public ProcessExecution execute(IveProcess process) {
        return new RendezvousExecution(this, process);
    };
    
    public ProcessResult rendezvous(ProcessExecution execution1,
            ProcessExecution execution2) {
        
        Log.addMessage("Rendezvous accepted.", Log.INFO, "", "Exchange", "");
        
        RendezvousExecution ex1 = (RendezvousExecution)execution1;
        RendezvousExecution ex2 = (RendezvousExecution)execution2;
        
        ex1.accepted = ex2.accepted = true;
        ex1.master = true;
        ex1.acceptedTime = ex1.getDuration();
        ex2.acceptedTime = ex2.getDuration();
        ex1.second = ex2;
        ex2.second = ex1;
        
        return ProcessResult.RUNNING;
    }
    
    public long atomicLength(ProcessExecution execution) {
        RendezvousExecution ex = (RendezvousExecution)execution;
        
        if (!ex.sentenceSet)
            return 1;
        
        if (ex.accepted) {
            return Math.max(0, LENGTH - ex.getDuration() + ex.acceptedTime);
        }
        
        return Math.max(0, ex.timeout - ex.getDuration());
    };
    
    
    public ProcessResult atomicCommit(ProcessExecution execution) {
        RendezvousExecution ex = (RendezvousExecution)execution;
        
        if (!ex.sentenceSet) {
            setSentence(ex, true);
            ex.sentenceSet = true;
            return ProcessResult.RUNNING;
        }
        
        if (!ex.accepted) {
            if(ex.timeout <= ex.getDuration()) {
                Log.addMessage("Exchange process timeouted without rendezvous.",
                        Log.FINE, "", "Exchange", "");
                setSentence(ex, false);
                return ProcessResult.FAILED;
            }
            return ProcessResult.RUNNING;
        }
        
        if (LENGTH > (ex.getDuration() - ex.acceptedTime))
            return ProcessResult.RUNNING;
        
        // Should this execution do the exchange?
        if (ex.master) {
            Source offer1 = ex.getObjects().getSource("offer");
            Source offer2 = ex.second.getObjects().getSource("offer");
            IveObject o1 = offer1 == null ? null : offer1.getObject();
            IveObject o2 = offer2 == null ? null : offer2.getObject();
            Source actor1 = ex.getObjects().getSource("actor");
            Source actor2 = ex.getObjects().getSource("who");
            IveObject a1 = actor1 == null ? null : actor1.getObject();
            IveObject a2 = actor2 == null ? null : actor2.getObject();
            
            // Check actors
            if (a1 == null || a2 == null) {
                Log.addMessage("Exchange process does not have valid actor " +
                        "and who sources.", Log.WARNING, "", "Exchange", "");
                setSentence(ex, false);
                return ProcessResult.FAILED;
            }
            
            // Check their arms
            AttrObject arm1Attr = (AttrObject)a1.getAttribute("arm");
            AttrObject arm2Attr = (AttrObject)a2.getAttribute("arm");
            
            if (arm1Attr == null || arm1Attr == null) {
                Log.addMessage("At least one of actors does not have an arm.",
                        Log.WARNING, a1.getId(), "Exchange",
                        a1.getPosition().getId());
                setSentence(ex, false);
                return ProcessResult.FAILED;
            }
            
            // Check what they hold
            IveObject inArm1 = arm1Attr.getValue();
            IveObject inArm2 = arm2Attr.getValue();
            if ((inArm1 != null &&
                    (o1 == null || !o1.getId().equals(inArm1.getId()))) ||
                    (inArm1 == null && o1 != null)) {
                Log.addMessage("Actor does not hold what he should.", Log.WARNING,
                        a1.getId(), "Exchange", a1.getPosition().getId());
                setSentence(ex, false);
                return ProcessResult.FAILED;
            }
            if ((inArm2 != null &&
                    (o2 == null || !o2.getId().equals(inArm2.getId()))) ||
                    (inArm2 == null && o2 != null)) {
                Log.addMessage("Target does not hold what he should.", Log.WARNING,
                        a1.getId(), "Exchange", a1.getPosition().getId());
                setSentence(ex, false);
                return ProcessResult.FAILED;
            }
            
            // Do the exchange
            if (o1 != null) {
                arm1Attr.setValue(null);
                a1.removeObject(o1);
            }
            if (o2 != null) {
                arm2Attr.setValue(null);
                a2.removeObject(o2);
                a1.addObject(o2);
                arm1Attr.setValue(o2);
            }
            if (o1 != null) {
                a2.addObject(o1);
                arm2Attr.setValue(o1);
            }
        }
        
        // Alter sources substitution
        Source acceptSrc = ex.getPhantoms().getSource("accept");
        Source accepted = ex.second.getObjects().getSource("offer");
        
        if (accepted != null && acceptSrc != null) {
            IveObject acceptedObj = accepted.getObject();
            acceptSrc.setObject(acceptedObj == null ? null :
                new IveObjectImpl(acceptedObj.getId()));
        }
        
        Source offerSrc = ex.getPhantoms().getSource("offer");
        if (offerSrc != null) {
            offerSrc.setObject(null);
        }
        
        setSentence(ex, false);
        
        return ProcessResult.OK;
    };
    
    public ProcessResult atomicStop(ProcessExecution execution) {
        RendezvousExecution ex = (RendezvousExecution)execution;
        setSentence(ex, false);
        if (ex.accepted) {
            ex.accepted = false;
            ex.second.accepted = false;
            ex.second.master = false;
        }
        return ProcessResult.OK;
    }
    
    public void decreaseLOD(ProcessExecution execution) {
        atomicStop(execution);
    }
    
    public boolean isRendezvous() {
        return true;
    }
    
    /**
     * Sets the sentence if present.
     *
     * @param execution Rendezvous process execution
     * @param setup <code>true</code> iff the sentence should be set up
     *          <code>false</code> if it shloud be cleared.
     */
    protected void setSentence(RendezvousExecution execution, boolean setup) {
        IveObject actor = getSourceObject("actor", true, execution);
        AttrInteger mouth = actor == null ? null :
            (AttrInteger)actor.getAttribute("mouth");
        
        if (mouth == null)
            return;
        
        if (!setup) {
            mouth.setValue(0);
            return;
        }
        
        OntologyToken sentenceOnt = (OntologyToken)execution.getParameters().
                get("sentence");
        Integer sentenceInt = null;
        try {
            sentenceInt = (Integer)sentenceOnt.getData("java.int");
        } catch (OntologyNotSupportedException ex) {
            IveApplication.printStackTrace(ex);
        }
        
        if (mouth == null || sentenceInt == null)
            return;
        
        mouth.setValue(sentenceInt.intValue());
    }
    
    
    
    /**
     * Execution specific for the rendezvous process. It contains additional
     * information about process timeout or rendezvous acceptance.
     */
    protected class RendezvousExecution extends ProcessExecutionImpl {
        protected int timeout;
        protected long acceptedTime;
        protected boolean accepted = false;
        protected boolean sentenceSet = false;
        protected boolean master = false;
        protected RendezvousExecution second;
        
        public RendezvousExecution(ProcessTemplate template, IveProcess process) {
            super(template, process);
            
            int timeout = TIMEOUT;
            Integer timeoutInt = null;
            OntologyToken timeOnt = (OntologyToken)process.getParameters().get(
                    "timeout");
            
            if (timeOnt != null) {
                try {
                    timeoutInt = (Integer)timeOnt.getData("java.int");
                    if (timeoutInt != null)
                        timeout = timeoutInt.intValue();
                } catch (OntologyNotSupportedException ex) {
                    IveApplication.printStackTrace(ex);
                }
            }
            this.timeout = timeout;
        }
    }
}

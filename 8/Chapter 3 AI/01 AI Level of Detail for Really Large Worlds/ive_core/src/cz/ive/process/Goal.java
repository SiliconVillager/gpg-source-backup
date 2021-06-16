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

import cz.ive.trigger.*;
import cz.ive.ontology.*;
import java.util.*;

/**
 * This SimpleToken derivate contains information about Goal.
 * It contains four goal properities (the trigger ones are ontology tokens)
 * Contains : g-context,g-trigger,goalid,substitution and parameters
 * @author thorm
 */
public class Goal extends SingleToken implements java.io.Serializable {
    
    /**
     * goal-trigger description
     */
    protected OntologyToken gTrigger;
    /**
     * goal-context description
     */
    protected OntologyToken gContext;
    /**
     * empty substitution
     */
    protected Substitution substitution;
    /**
     * id of the goal
     */
    protected String goalID;
    /**
     * parameters of the goal
     */
    protected Map<String, Object>   parameters;
          
    /** Creates a new instance of Goal */
    public Goal() {
        super("jBRP.goal");        
    }
    
    public OntologyToken getGTrigger() {
        return gTrigger;
    }

    public void setGtrigger(OntologyToken gTrig) {
        this.gTrigger = gTrig;
    }


    public OntologyToken getGContext() {
        return gContext;
    }

    public void setGcontext(OntologyToken gCtx) {
        this.gContext = gCtx;
    }

    public Substitution getSubstitution() {
        return substitution;
    }

    public void setSubstitution(Substitution substitution) {
        this.substitution = substitution;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getGoalID() {
        return goalID;
    }

    public void setGoalID(String goalID) {
        this.goalID = goalID;
    }
}

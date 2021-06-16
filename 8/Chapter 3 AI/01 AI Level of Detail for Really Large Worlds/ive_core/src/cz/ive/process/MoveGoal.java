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

import cz.ive.evaltree.leaves.FuzzyConstant;
import cz.ive.trigger.*;
import cz.ive.process.*;
import cz.ive.messaging.*;
import cz.ive.ontology.*;
import cz.ive.exception.*;
import cz.ive.evaltree.*;
import cz.ive.valueholders.FuzzyValueHolder;

import java.util.*;

/**
 *
 * @author honza
 */
public class MoveGoal extends Goal {
    
    /** Creates a new instance of MoveGoal */
    public MoveGoal() {
        gTrigger = (TriggerTemplate)new MoveGoalTriggerTemplate();
        
        substitution = new SubstitutionImpl();
        substitution.addSlot("actor", new SourceImpl(null),
                true, false, true);
        /*
        substitution.addSlot("targetPosition", (Source) null,
                true, true, false);
         */
        substitution.addSlot("targetPosition", new SourceImpl(null),
                true, true, false);
        parameters = new HashMap<String, Object>();
        goalID = "MoveGoal";
        
        gContext = new EvalTreeTriggerTemplate(new FuzzyConstant(
                FuzzyValueHolder.True));
    }
    
}
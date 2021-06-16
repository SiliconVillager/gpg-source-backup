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

import cz.ive.iveobject.ObjectMap;
import cz.ive.ontology.OntologyToken;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pavel
 */
public class HuntingStep extends Step {
    
    /** Creates a new instance of HuntingStep */
    public HuntingStep() {

        sources.addSlot("targetObject", (Source) null,
                true, false, false);
    }
    
    public OntologyToken getExpansion(Substitution sources,
            Map<String, Object> parameters){
        Goal goal = new HuntGoal();
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
        goal.getSubstitution().setSource("targetObject", 
                sources.getSource("targetObject"));
        goal.getSubstitution().setSource("actor", 
                sources.getSource("actor"));

        if (sources.getSource("localTarget").getObject() != null) { 
            goal.getSubstitution().getSource("targetPosition").setObject(  
                sources.getSource("localTarget").getObject());
        }
        
        Expansion exp = new Expansion(new Goal[] {goal});
        return exp;
    }
    
    public ProcessExecution execute(IveProcess process) {
    
        /*if (process.getSubstitution().getSource("targetPosition") == null) {
            Source src = new SourceImpl(); 
            src.setObject(sources.getSource("targetObject").
                    getObject().getPosition());
            process.getSubstitution().addSource("targetPosition", src);
        }*/
        
        return super.execute(process);
    }
}

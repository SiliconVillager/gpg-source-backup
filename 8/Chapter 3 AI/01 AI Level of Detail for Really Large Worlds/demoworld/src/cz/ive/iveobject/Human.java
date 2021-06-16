/* 
 *
 * IVE Demo World
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
 
package cz.ive.iveobject;

import cz.ive.iveobject.attributes.AttrFuzzy;
import cz.ive.iveobject.attributes.AttrInteger;
import cz.ive.iveobject.attributes.AttrObject;
import cz.ive.valueholders.FuzzyValueHolder;

/**
 * Human ent object. It takes care of attributes "arm", "standing" and "state"
 * that can change during shrink phase.
 * 
 * @author ondra
 */
public class Human extends Ent {
    
    /**
     * Creates a new instance of Human
     * 
     * @param objectId Id of the actor.
     */
    public Human(String objectId){
        super(objectId);
    }
    
    /**
     * Creates a new instance of Human
     * 
     * @param objectId Id of the actor.
     * @param objCls object class of the actor.
     */
    public Human(String objectId,ObjectClass objCls){
        super(objectId, objCls);
    }
    
    /**
     * Called when master becames non-existing during shrink.
     * Can be defined in descendants to perform cleaning actions.
     */
    public void loosingMaster() {
        AttrFuzzy standingAttr = (AttrFuzzy)getAttribute("standing");
        AttrInteger stateAttr = (AttrInteger)getAttribute("state");
        if (standingAttr.getValue() != FuzzyValueHolder.True) {
            standingAttr.setValue(FuzzyValueHolder.True);
            stateAttr.setValue(STANDING);
        }
    }
    
    /**
     * Called when some slave becames non-existing during shrink.
     * Can be defined in descendants to perform cleaning actions.
     * @param slave The disappearing slave
     */
    public void loosingSlave(IveObject slave) {
        AttrObject armAttr = (AttrObject)getAttribute("arm");
        IveObject armObj = armAttr.getValue();
        if (armObj != null && armObj.getId().equals(slave.getId())) {
            armAttr.setValue(null);
        }
    }
}

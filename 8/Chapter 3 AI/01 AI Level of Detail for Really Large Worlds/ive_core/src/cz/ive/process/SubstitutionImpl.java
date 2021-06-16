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
import cz.ive.process.*;
import cz.ive.messaging.*;
import cz.ive.lod.*;
import java.util.*;

/**
 *
 * @author Zdenek
 */
public class SubstitutionImpl implements Substitution, Cloneable, 
        java.io.Serializable {

    /**
     * Map of all Slots stored by SourceId
     */
    public HashMap<String, Slot> slots;
    
    /**
     * Map of all Actorslots stored by SourceId
     */    
    public HashMap<String, Slot> actorSlots;
          
    /** 
     * Creates a new instance of SubstitutionImpl
     */    
    public SubstitutionImpl() {
        slots = new HashMap<String, Slot>();
        actorSlots = new HashMap<String, Slot>();
    }
    
    /**
     * Creates a new instance of SubstitutionImpl and use Map of Slots as slots
     *  contained in substitution and map of actorslots as actorslots 
     * @param data Map of Slots
     * @param data2 Map of Slots with actors
     */
    public SubstitutionImpl(HashMap<String, Slot> data, HashMap<String, Slot> data2) {
        this();
        slots.putAll(data);
        actorSlots.putAll(data2);
    }

    /**
     * Creates a new instance of SubstitutionImpl and add one slot
     * @param sourceId Id of source
     * @param slot added slot]
     * @param actor true if added slot is a slot with actor
     */    
    public SubstitutionImpl(String sourceId, Slot slot, boolean actor) {
        this();
        if (actor) {
            actorSlots.put(sourceId, slot);
            slots.put(sourceId, slot);
        } else {
            slots.put(sourceId, slot);
        }
                
    }
    
    public HashMap<String, Slot> getSlots() {
        return slots;
    }
    
    public HashMap<String, Slot> getActorSlots() {
        return actorSlots;
    }

    public boolean addSlot(String sourceId, Source source, boolean mandatory, 
            boolean variable, boolean actor, LOD setLod) {
        
        if (slots.containsKey(sourceId) == true) {
            return false;
        }
        
        Slot addedSlot = new Slot();
        addedSlot.setSource(source);
        addedSlot.isMandatory = mandatory;
        addedSlot.isVariable = variable;
        addedSlot.lod = setLod;

        if (actor) {
            actorSlots.put(sourceId, addedSlot);
            slots.put(sourceId, addedSlot);
        } else {
            slots.put(sourceId, addedSlot);
        }
        return true;
        
    }
    
    public boolean addSlot(String sourceId, Source source, boolean mandatory, 
            boolean variable, boolean actor) {
        return this.addSlot(sourceId, source, mandatory, variable, actor, new LOD());
    }

    public boolean addSlot(String sourceId, Source source, boolean mandatory,
            boolean variable, boolean actor, int minLod, int maxLod) {
        return this.addSlot(sourceId, source, mandatory, variable, actor, 
                new LOD(minLod, maxLod));
    }

    public boolean addSlot(String sourceId, ArrayList<Source> source, 
            boolean mandatory, boolean variable, boolean actor, LOD setLod) {
        if (slots.containsKey(sourceId) == true) {
            return false;
        } 
        
        Slot addedSlot = new Slot();
        addedSlot.slotArray = source;
        addedSlot.isMandatory = mandatory;
        addedSlot.isVariable = variable;
        addedSlot.lod = setLod;
        
        if (actor) {
            actorSlots.put(sourceId, addedSlot);
            slots.put(sourceId, addedSlot);
        } else {
            slots.put(sourceId, addedSlot);
        }
                
        return true;
    }
    
    public boolean addSlot(String sourceId, Slot slot, boolean actor) {
        if (slots.containsKey(sourceId) == true) {
            return false;
        } 
        
        if (actor) {
            actorSlots.put(sourceId, slot);
            slots.put(sourceId, slot);
        } else {
            slots.put(sourceId, slot);
        }
                
        return true;
    }
    
    public boolean addSlot(String sourceId, ArrayList<Source> source, 
            boolean mandatory, boolean variable, boolean actor) {
        return this.addSlot(sourceId, source, mandatory, variable, actor, 
                new LOD());
    }

    public boolean addSlot(String sourceId, ArrayList<Source> source, 
            boolean mandatory, boolean variable, boolean actor, int minLod, 
            int maxLod) {
        return this.addSlot(sourceId, source, mandatory, variable, actor, 
                new LOD(minLod, maxLod));
    }
   
    public boolean deleteSlot(String sourceId) {
        if (slots.containsKey(sourceId) == false) {           
            return false;
        }
        
        slots.remove(sourceId);
        actorSlots.remove(sourceId);
        return true;
    }
    
    public boolean slotIsList(String sourceId) {
        Slot slot;
        if (slots.containsKey(sourceId) == true) {
            slot = slots.get(sourceId);
        } else {
            return false;            
        }
        if (slot.slotArray == null || slot.slotArray.size() == 0) {
            return false;
        } else {
            return true;
        }        
    }

    public boolean slotIsAlone(String sourceId) {
        Slot slot;
        if (slots.containsKey(sourceId) == true) {
            slot = slots.get(sourceId);
        } else {
            return false;
        }

        if (slot.slotArray == null || slot.slotArray.size() != 1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean slotIsEmpty(String sourceId) {
        Slot slot;
        if (slots.containsKey(sourceId) == true) {
            slot = slots.get(sourceId);
        } else {            
            return false;
        }
        if (slot.slotArray == null || slot.slotArray.size() == 0) {
            return true;
        } else {
            return false;
        }
        
    }

    public boolean isMandatory(String sourceId) {
        if (slots.containsKey(sourceId) == true) {
            return slots.get(sourceId).isMandatory;
        } 
        return false;
    }

    public boolean isOptional(String sourceId) {
        if (slots.containsKey(sourceId) == true) {
            return !slots.get(sourceId).isMandatory;
        }
        return false;        
    }

    public boolean isVariable(String sourceId) {
        if (slots.containsKey(sourceId) == true) {
            return slots.get(sourceId).isVariable;
        }
        return false;                
    }
    
    public Source getSource(String sourceId) {
        if (slots.containsKey(sourceId) == false) {
            return null;
        }      
	Slot s = slots.get(sourceId);
        if (s.slotArray == null || s.slotArray.size() == 0) {
            return null;
        }
        return s.slotArray.get(0);
    }

    public ArrayList<Source> getSourceArray(String sourceId) {
        if (slots.containsKey(sourceId) == false) {
            return null;           
        }
                
        Slot getSlot;
        getSlot = slots.get(sourceId);
        if (getSlot.slotArray == null) {
            return null;
        }
        return getSlot.slotArray;
    }

    public void setSource(String sourceId, Source source) {
        Slot slot = slots.get(sourceId);
        Slot actorSlot = actorSlots.get(sourceId);
        
        if (slot != null) {
            slot.setSource(source);
        }
        if (actorSlot != null && actorSlot != slot) {
            slot.setSource(source);
        }
    }
  
    public void setSource(String sourceId, Source[] source) {
        Slot slot = slots.get(sourceId);                 
        Slot actorSlot = actorSlots.get(sourceId);                 
        
        if (slot != null) {
            slot.setSourceArray(source);
        }
        if (actorSlot != null && actorSlot != slot) {
            actorSlot.setSourceArray(source);
        }
    }

    public void setSource(String sourceId, ArrayList<Source> source) {
        Slot slot = slots.get(sourceId);                 
        Slot actorSlot = actorSlots.get(sourceId);                 
        
        if (slot != null) {
            slot.slotArray = source;
        }
        if (actorSlot != null && actorSlot != slot) {
            actorSlot.slotArray = source;
        }
    }
           
    public void addSource(String sourceId, Source source) {
        Slot slot = slots.get(sourceId);                 
        Slot actorSlot = actorSlots.get(sourceId);                 
        
        if (slot != null) {
            slot.addSource(source);
        }
        if (actorSlot != null && actorSlot != slot) {
            actorSlot.addSource(source);
        }
    }
          
    public void addSource(String sourceId, Source[] source) {
        Slot slot = slots.get(sourceId);                 
        Slot actorSlot = actorSlots.get(sourceId);                 
        
        if (slot != null) {
            slot.addSource(source);
        }
        if (actorSlot != null && actorSlot != slot) {
            actorSlot.addSource(source);
        }
    }
         
    public void addSource(String sourceId, ArrayList<Source> source) {
        Slot slot = slots.get(sourceId);                 
        Slot actorSlot = actorSlots.get(sourceId);                 
        
        Source[] arr = source.toArray(new Source[source.size()]);
        
        if (slot != null) {
            slot.addSource(arr);
        }
        if (actorSlot != null && actorSlot != slot) {
            actorSlot.addSource(arr);
        }
    }
          
    public void deleteSource(String sourceId, Source source) {
        Slot slot = slots.get(sourceId);                 
        Slot actorSlot = actorSlots.get(sourceId);                 
        
        if (slot != null) {
            slot.slotArray.remove(source);
        }
        if (actorSlot != null && actorSlot != slot) {
            actorSlot.slotArray.remove(source);
        }
    }
          
    public void clearSource(String sourceId) {
        if (slots.containsKey(sourceId) == false) {
            return;            
        }
        boolean actor = actorSlots.containsKey(sourceId);
        Slot getSlot = slots.get(sourceId);                 
        getSlot.slotArray = null;
        
        slots.put(sourceId, getSlot);            
        if (actor) {
            actorSlots.put(sourceId, getSlot);            
        }        
    } 

    public Substitution duplicateSubstitution(boolean deepCopy) {
        if (deepCopy) {
            SubstitutionImpl copySubst;
            try {
                copySubst = (SubstitutionImpl)super.clone();
            } catch (CloneNotSupportedException e) { 
	        throw new InternalError();
            }
            copySubst.slots = new HashMap<String, Slot>();
            copySubst.actorSlots = new HashMap<String, Slot>();
            
            Set<String> keys = slots.keySet();
            for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {                               
                String str = i.next();                
                Slot clonedSlot = slots.get(str).clone(true);
                copySubst.getSlots().put(str, clonedSlot);
                if (actorSlots.containsKey(str)) {
                    copySubst.getActorSlots().put(str, clonedSlot);
                }
            }                        
            return copySubst;
                        
        } else {
            try {
                return (Substitution)super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError();
            }
            
            
        }
    }

    public void changeSubstitution(Substitution subst) {
        if (subst == null) {
            return;
        }
        HashMap<String, Slot> substSlots = subst.getSlots();
        HashMap<String, Slot> substActorSlots = subst.getActorSlots();
        Set<String> substSet = substSlots.keySet();        
        for (Iterator<String> i = substSet.iterator(); i.hasNext(); ) {
            String str = i.next();
            if (slots.containsKey(str) == true) {
                Slot getSlot = slots.get(str);
                Slot substSlot = substSlots.get(str);
                getSlot.slotArray = substSlot.slotArray;
                slots.put(str, getSlot);
            }
        }
    }

    public boolean checkSubstitution(Substitution subst) {        
        if (subst == null || subst.getSlots() == null) {
            return false;
        }
                 
        Set<String> substSet = subst.getSlots().keySet();
        for (Iterator<String> i = substSet.iterator(); i.hasNext(); ) {
            String str = i.next();
            if (slots.containsKey(str) == false) {
                return false;
            }            
            if (subst.getSlots().get(str).isMandatory == true && 
                    slots.get(str).isEmpty() == true) {
                return false;
            }
        }
        substSet = subst.getActorSlots().keySet();
        for (Iterator<String> i = substSet.iterator(); i.hasNext(); ) {
            String str = i.next();
            if (actorSlots.containsKey(str) == false) {
                return false;
            }            
            if (subst.getActorSlots().get(str).isMandatory == true && 
                    actorSlots.get(str).isEmpty() == true) {
                return false;
            }
        }
        return true;
    }
 
    public Set<String> getSlotsKeys() {
        return slots.keySet();
    }
   
    public Set<String> getActorSlotsKeys() {
        return actorSlots.keySet();
    }

    public LOD getSlotLod(String sourceId) {
        if (slots.containsKey(sourceId) == false) {
            return null;            
        }
        
        return slots.get(sourceId).lod;
    }
    
}

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
import cz.ive.iveobject.*;
import java.util.*;
import cz.ive.lod.*;

/**
 *
 * @author Zdenek
 */
public class Slot implements Cloneable, java.io.Serializable {
    
    /** Mandatority of Slot */                    
    public enum slotStatus {
	/** slot must be filled */
	mandatory, 
	/** slot needn't to be filled */
	optional, 
	/** slot is a variable */
	variable};

    public boolean isMandatory;
    
    public boolean isVariable;
    
    /**
     * Pointer to ArrayList when array of Sources is stored in Slot
     */
    public ArrayList<Source> slotArray;
    
    /**
     * Status of Slot instance
     */
    //public slotStatus status;
    
    /**
     * Allowed Lod of objects in slot
     */
    public LOD lod;
    
    /** Creates a new instance of Slot */
    public Slot() {
        slotArray = null;
        isMandatory = true;
        isVariable = false;
        lod = null;
    }

    /**
     * Creates a new instance of Slot. Into slot will be added Source with 
     * object. SlotStatus will be set as mandatory.
     * @param object IveObject which will be added into slot
     */    
    public Slot(IveObject object) {
        this();
        slotArray = new ArrayList<Source>();
        slotArray.add(new SourceImpl(object));
        lod = new LOD();
    }

    /**
     * Creates a new instance of Slot. Into slot will be added Source with 
     * object. SlotStatus will be set as mandatory.
     * @param object IveObject which will be added into slot
     * @param setLod LOD object with constrains of LOD
     */    
    public Slot(IveObject object, LOD setLod) {
        this();
        slotArray = new ArrayList<Source>();
        slotArray.add(new SourceImpl(object));
        lod = setLod;
    }
    
    /**
     * Creates a new instance of Slot. Into slot will be added Source with 
     * object. SlotStatus will be set as mandatory.
     * @param object IveObject which will be added into slot
     * @param min minimal LOD level
     * @param max maximal LOD level
     */    
    public Slot(IveObject object, int min, int max) {
        this();
        slotArray = new ArrayList<Source>();
        slotArray.add(new SourceImpl(object));
        lod = new LOD(min, max);
    }
    
    /**
     * Creates a new instance of Slot. Into slot will be added Source with 
     * object. SlotStatus will be set from stat.
     * @param object IveObject which will be added into slot
     * @param mandatory true when slot is mandatory
     * @param variable true whem slot is variable
     */
    public Slot(IveObject object, boolean mandatory, boolean variable) {
        isMandatory = mandatory;
        isVariable = variable;
        slotArray = new ArrayList<Source>();
        slotArray.add(new SourceImpl(object));
        lod = new LOD();
        
    }

    /**
     * Creates a new instance of Slot. Into slot will be added Source with 
     * object. SlotStatus will be set from stat.
     * @param object IveObject which will be added into slot
     * @param mandatory true when slot is mandatory
     * @param variable true whem slot is variable
     * @param setLod LOD object with constrains of LOD
     */
    public Slot(IveObject object, boolean mandatory, boolean variable, 
            LOD setLod) {
        isMandatory = mandatory;
        isVariable = variable;
        slotArray = new ArrayList<Source>();
        slotArray.add(new SourceImpl(object));
        lod = setLod;
    }
    
    /**
     * Creates a new instance of Slot. Into slot will be added Source with 
     * object. SlotStatus will be set from stat.
     * @param object IveObject which will be added into slot
     * @param mandatory true when slot is mandatory
     * @param variable true whem slot is variable
     * @param min minimal LOD level
     * @param max maximal LOD level
     */
    public Slot(IveObject object, boolean mandatory, boolean variable, int min, 
            int max) {
        isMandatory = mandatory;
        isVariable = variable;
        slotArray = new ArrayList<Source>();
        slotArray.add(new SourceImpl(object));
        lod = new LOD(min, max);
    }
    
    /**
     * Creates a new instance of Slot. Into slot will be added Sources with 
     * objects. SlotStatus will be set as mandatory.
     * @param objects array of IveObjects which will be added into slot
     */
    public Slot(IveObject[] objects) {
        this();
        slotArray = new ArrayList<Source>();
        for (int i=0; i < objects.length; i++) {
            slotArray.add(new SourceImpl(objects[i]));
        }                
        lod = new LOD();
    }
    
    /**
     * Creates a new instance of Slot. Into slot will be added Sources with 
     * objects. SlotStatus will be set as mandatory.
     * @param objects array of IveObjects which will be added into slot
     * @param setLod LOD object with constrains of LOD
     */
    public Slot(IveObject[] objects, LOD setLod) {
        this();
        slotArray = new ArrayList<Source>();
        for (int i=0; i < objects.length; i++) {
            slotArray.add(new SourceImpl(objects[i]));
        }                
        lod = setLod;
    }
    
    /**
     * Creates a new instance of Slot. Into slot will be added Sources with 
     * objects. SlotStatus will be set as mandatory.
     * @param objects array of IveObjects which will be added into slot
     * @param min minimal LOD level
     * @param max maximal LOD level
     */
    public Slot(IveObject[] objects, int min, int max) {
        this();
        slotArray = new ArrayList<Source>();
        for (int i=0; i < objects.length; i++) {
            slotArray.add(new SourceImpl(objects[i]));
        }                
        lod = new LOD(min, max);
    }
        
    
    /**
     * Creates a new instance of Slot. Into slot will be added Sources with 
     * objects. SlotStatus will be set from stat.
     * @param objects array of IveObjects which will be added into slot
     * @param mandatory true when slot is mandatory
     * @param variable true whem slot is variable
     */
    public Slot(IveObject[] objects, boolean mandatory, boolean variable) {
        isMandatory = mandatory;
        isVariable = variable;
        slotArray = new ArrayList<Source>();
        for (int i=0; i < objects.length; i++) {
            slotArray.add(new SourceImpl(objects[i]));
        }                
        lod = new LOD();
    }
    
    /**
     * Creates a new instance of Slot. Into slot will be added Sources with 
     * objects. SlotStatus will be set from stat.
     * @param objects array of IveObjects which will be added into slot
     * @param mandatory true when slot is mandatory
     * @param variable true whem slot is variable
     * @param setLod LOD object with constrains of LOD
     */
    public Slot(IveObject[] objects, boolean mandatory, boolean variable, 
            LOD setLod) {
        slotArray = new ArrayList<Source>();
        for (int i=0; i < objects.length; i++) {
            slotArray.add(new SourceImpl(objects[i]));
        }                
        isMandatory = mandatory;
        isVariable = variable;
        lod = setLod;
    }
    
    /**
     * Creates a new instance of Slot. Into slot will be added Sources with 
     * objects. SlotStatus will be set from stat.
     * @param objects array of IveObjects which will be added into slot
     * @param mandatory true when slot is mandatory
     * @param variable true whem slot is variable
     * @param min minimal LOD level
     * @param max maximal LOD level
     */
    public Slot(IveObject[] objects, boolean mandatory, boolean variable, 
            int min, int max) {
        slotArray = new ArrayList<Source>();
        for (int i=0; i < objects.length; i++) {
            slotArray.add(new SourceImpl(objects[i]));
        }                
        isVariable = variable;
        isMandatory = mandatory;
        lod = new LOD(min, max);
    }
    
    
    /**
     * Sets source into slot;
     * @param source source which will be set into slot
     */
    public void setSource(Source source) {
        if (slotArray == null) {
            slotArray = new ArrayList<Source>();
        } else {
            slotArray.clear();
        }
        slotArray.add(source);
    }
    
    /**
     * Sets array of sources into slot
     * @param sources sources which will be set into slot
     */
    public void setSourceArray(Source[] sources) {
        if (slotArray == null) {
            slotArray = new ArrayList<Source>();
        } else {
            slotArray.clear();
        }
        for (int i=0; i<sources.length; i++) {
            slotArray.add(sources[i]);
        }
    }
    
    /**
     * Sets one source into slot
     * @param source source which will be set into slot
     */
    public void setSourceArray(Source source) {
        this.setSource(source);
    }
        
    /**
     * Adds Source into slot
     * @param source source which will be added into slot
     */
    public void addSource(Source source) {
        if (slotArray == null) {
            slotArray = new ArrayList<Source>();
        }
        slotArray.add(source);
    }
    
    /**
     * Adds Sources into slot
     * @param source source which will be added into slot
     */
    public void addSource(Source[] source) {
        if (slotArray == null) {
            slotArray = new ArrayList<Source>();
        }
        for (int i = 0; i < source.length; i++) {
            slotArray.add(source[i]);
        }
    }

    
    /**
     * Deletes Source in slot if contained
     * @param source source which will be removed from slot
     */
    public void deleteSource(Source source) {
        if (slotArray != null && slotArray.contains(source)) {
            slotArray.remove(source);
        }
    }
    
    /**
     * Returns Source in slot
     */ 
    public Source getSource() {
        if (slotArray == null || slotArray.size() == 0) {
            return null;
        }
        return slotArray.get(0);
        
    }   
    
    /**
     * Returns array of all sources inside slot
     */
    public Source[] getSourceArray() {
        if (slotArray == null) {
            return null;
        }
        return (Source[])slotArray.toArray();
    }
    
    /**
     * Returns ArrayList of all sources inside slot
     */
    public ArrayList<Source> getSourceArrayList() {
        return slotArray;
    }
    
    /**
     * Clones actual instance of slot.
     * @return duplicated Slot
     */
    protected Slot clone() {
        try {
            return (Slot)super.clone();
        } catch (CloneNotSupportedException e) { 
            throw new InternalError();
	}
    }
    
    /**
     * Clones actual instance of slot.
     * 
     * @param deep indicates wether the copy should be deep or not.
     * @return duplicated Slot
     */
    public Slot clone(boolean deep) {
        if (!deep)
            return clone();
        
        Slot slot = new Slot();
        slot.isMandatory = isMandatory;
        slot.isVariable = isVariable;
        slot.lod = lod;
        slot.slotArray = new ArrayList<Source>(slotArray.size());
        for (Source src : slotArray) {
            slot.slotArray.add(new SourceImpl(src.getObject()));
        }
        //slot.slotArray = (ArrayList)slotArray.clone();
        return slot;
    }
       
    /**
     * Function which returns fulfilment of Slot.
     * @return false when some Source is stored in slot and true when not.
     */
    public boolean isEmpty() {
        if (slotArray == null || slotArray.size() == 0) {
            return true;
        } else {
            return false;
        }
                
    }
    
    public void setLod(LOD setlod) {
        lod = setlod;
    }
    
    public void setLod(int min, int max) {
        lod = new LOD(min, max);
    }
    
    public LOD getLOD() {
        return lod;
    }
    
}

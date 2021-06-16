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

import cz.ive.messaging.*;
import cz.ive.lod.*;
import java.util.*;

/**
 * Source substitution. Map between SourceId and other Substitution information:
 * <ul>
 *  <li>Substituted - single IveObject/ set of IveObjects / not yet assigned
 *  </li>
 *  <li>Obligation - obligatory single / obligatory set / optional single / 
 *          optional set / variable
 *  </li>
 *  <li>Type - actor / object
 *  </li>
 * </ul>
 *
 * It allows insertion and deletion of substituted objects, easy cloning.
 * Substitution also provides Hook for changes in object assignment.
 *
 * @author  Ondra
 */
public interface Substitution extends Cloneable {
    
    public HashMap<String, Slot> getSlots();
    public HashMap<String, Slot> getActorSlots();
    
    
    /**
     * Method adds Slot into Substitution - version with single source
     * addition.
     *
     * @param sourceId Id of source
     * @param source source which will be stored in slot
     * @param mandatory true when source is mandatory
     * @param variable true when source is variable
     * @param actor true if added slot is slot with actor
     * @param setLod LOD object with constrains of LOD
     * @return true when new slot successfully added, false when Substitution 
     *      contains slot with same sourceId
     */
    public boolean addSlot(String sourceId, Source source, boolean mandatory, 
            boolean variable, boolean actor, LOD setLod);


    /**
     * Method adds Slot into Substitution - version with single source
     * addition.
     *
     * @param sourceId Id of source
     * @param source source which will be stored in slot
     * @param mandatory true when source is mandatory
     * @param variable true when source is variable
     * @param actor true if added slot is slot with actor
     * @return true when new slot successfully added, false when Substitution 
     *      contains slot with same sourceId
     */
    public boolean addSlot(String sourceId, Source source, boolean mandatory, 
            boolean variable, boolean actor);

    /**
     * Method adds Slot into Substitution - version with single source
     * addition.
     *
     * @param sourceId Id of source
     * @param source source which will be stored in slot
     * @param mandatory true when source is mandatory
     * @param variable true when source is variable
     * @param actor true if added slot is slot with actor
     * @param minLod minimal Lod
     * @param maxLod maximal Lod
     * @return true when new slot successfully added, false when Substitution 
     *      contains slot with same sourceId
     */
    public boolean addSlot(String sourceId, Source source, boolean mandatory,
            boolean variable, boolean actor, int minLod, int maxLod);
    
    /**
     * Method adds Slot into Substitution - version with multi source
     * addition.
     *
     * @param sourceId Id of source
     * @param source List of sources which will be stored in slot
     * @param mandatory true when source is mandatory
     * @param variable true when source is variable
     * @param actor true if added slot is slot with actor
     * @param setLod LOD object with constrains of LOD
     * @return true when new slot successfully added, false when Substitution 
     *      contains slot with same sourceId
     */
    public boolean addSlot(String sourceId, ArrayList<Source> source, 
            boolean mandatory, boolean variable, boolean actor, LOD setLod);
    
    /**
     * Method adds Slot into Substitution - version with multi source
     * addition.
     *
     * @param sourceId Id of source
     * @param source List of sources which will be stored in slot
     * @param mandatory true when source is mandatory
     * @param variable true when source is variable
     * @param actor true if added slot is slot with actor
     * @return true when new slot successfully added, false when Substitution 
     *      contains slot with same sourceId
     */
    public boolean addSlot(String sourceId, ArrayList<Source> source, 
            boolean mandatory, boolean variable, boolean actor);
    
    /**
     * Method adds Slot into Substitution - version with multi source
     * addition.
     *
     * @param sourceId Id of source
     * @param source List of sources which will be stored in slot
     * @param mandatory true when source is mandatory
     * @param variable true when source is variable
     * @param actor true if added slot is slot with actor
     * @param minLod minimal Lod
     * @param maxLod maximal Lod
     * @return true when new slot successfully added, false when Substitution 
     *      contains slot with same sourceId
     */
    public boolean addSlot(String sourceId, ArrayList<Source> source, 
            boolean mandatory, boolean variable, boolean actor, int minLod, 
            int maxLod);

    /**
     * Method adds Slot into Substitution. This slot is added as is, so
     * it can be shared with multiple Substitution.
     *
     * @param sourceId Id of source
     * @param slot already created Slot
     * @param actor true if added slot is slot with actor
     * @return true when new slot successfully added, false when Substitution 
     *      contains slot with same sourceId
     */
    public boolean addSlot(String sourceId, Slot slot, boolean actor);
    
    /**
     * Method which deletes slot from substitution.
     *
     * @param sourceId sourceId of slot we want to delete
     * @return true if slot was deleted from substitution, false when slot with
     *      sourceId is not in substitution
     */
    public boolean deleteSlot(String sourceId);

    /**
     * Method which returs if slot contains list of Sources.
     *
     * @param sourceId sourceId of Slot we ask for
     * @return true when Slot contains array of Sources, false when not
     */
    public boolean slotIsList(String sourceId);

    
    /**
     * Method which returns if slot contains single Source.
     *
     * @param sourceId sourceId of Slot we ask for
     * @return true when Slot contains single Source, false when not
     */
    public boolean slotIsAlone(String sourceId);
    
    /**
     * Method which returns if slot contains no Sources.
     *
     * @param sourceId sourceId of Slot we ask for
     * @return true when Slot contains no Sources, false when not
     */
    public boolean slotIsEmpty(String sourceId);
    
    /**
     * Getter for the mandatory flag.
     *
     * @param sourceId sourceId of the Slot we ask for.
     * @return <code>true</code> when the Slot is mandatory.
     */
    public boolean isMandatory(String sourceId);
    
    /**
     * Getter for the optional flag (negation of the mandatory flag).
     *
     * @param sourceId sourceId of the Slot we ask for.
     * @return <code>true</code> when the Slot is optional.
     */
    public boolean isOptional(String sourceId);
    
    /**
     * Getter for the variable flag (the source can be reassigned during some
     * processes).
     *
     * @param sourceId sourceId of the Slot we ask for.
     * @return <code>true</code> when the Slot is variable.
     */
    public boolean isVariable(String sourceId);
    
    /**
     * Method which returns single Source from slot.
     *
     * @param sourceId sourceId of Slot from which we want to get Source
     * @return Source when slot with sourceId is in substitution and slot
     *      contains single Source, otherwise null
     */
    public Source getSource(String sourceId);
    
    /**
     * Method which returns array of Sources from slot.
     *
     * @param sourceId sourceId of Slot from which we want to get Source
     * @return Source when slot with sourceId is in substitution and slot
     *      contains array of Sources, otherwise null
     */
    public ArrayList<Source> getSourceArray(String sourceId);
    
    /**
     * Method which sets single Source into Slot in substitution.
     * Slot must exist before setting source.
     *
     * @param sourceId sourceId of slot where we want to set source
     * @param source Source we want to set
     */
    public void setSource(String sourceId, Source source);
    
    /**
     * Assigns sources to given name (source ID).
     * Slot must exist before setting source.
     *
     * @param sourceId string name of the source
     * @param source sources to be assigned with given name
     */    
    public void setSource(String sourceId, Source[] source);
    
    /**
     * Method which sets array of Sources into Slot in substitution.
     * Slot must exist before setting source.
     *
     * @param sourceId sourceId of slot where we want to set source
     * @param source Array of Sources we want to set
     */
    public void setSource(String sourceId, ArrayList<Source> source);
    
    /**
     * Add source to actual sources by given name (source ID).
     * Slot must exist before adding source.
     *
     * @param sourceId string name of the source
     * @param source source to be added
     */            
    public void addSource(String sourceId, Source source);
    
    /**
     * Add sources to actual sources by given name (source ID).
     * Slot must exist before adding source.
     *
     * @param sourceId string name of the source
     * @param source sources to be added
     */            
    public void addSource(String sourceId, Source[] source);
    
    /**
     * Add sources to actual sources by given name (source ID).
     * Slot must exist before adding source.
     *
     * @param sourceId string name of the source
     * @param source sources to be added
     */            
    public void addSource(String sourceId, ArrayList<Source> source);

    /**
     * Remove source in slot by given name (source ID).
     * Slot must exist before deleting source.
     *
     * @param sourceId string name of the source
     * @param source which will be removed
     */            
    public void deleteSource(String sourceId, Source source);

    /**
     * Remove all sources in slot by given name (source ID).
     * Slot must exist before clearing source.
     *
     * @param sourceId string name of the source
     */            
    public void clearSource(String sourceId);
    
    /**
     * Method which duplicates instance of substitution.
     *
     * @param deepCopy If true then we duplicate slots stored in substitution too,
     *      otherwise only instance of substitution is duplicated
     * @return duplicated Substitution
     */
    public Substitution duplicateSubstitution(boolean deepCopy);
    
    /**
     * Changes actual substitution along substitution added in parameter. 
     * All Sources in Slots which are contained in subst too will be changed  
     * with sources containted in subst.
     *
     * @param subst Substitution along which will be instance substitution 
     *      changed.
     */
    public void changeSubstitution(Substitution subst);

    /**
     * Method checks instance of substitution along substitution in parameter.
     * If some slot is not contained in instance substitution and contained in
     * added substitution, or if some slot is mandatory and is empty in 
     * instance substitution, then check doesn't pass.
     *
     * @param subst Given template substitution.
     * @return true when check passed, false otherwise.
     */
    public boolean checkSubstitution(Substitution subst);
    
    /**
     * Returns all containted names of slots.
     */    
    public Set<String> getSlotsKeys();

    /**
     * Returns all containted names of slots with actors.
     */    
    public Set<String> getActorSlotsKeys();

    /**
     * Method which returns LOD object of Slot identified by sourceId.
     *
     * @param sourceId string identification of slot
     * @return LOD LOD of slot, null when slot is not contained in substitution
     */
    public LOD getSlotLod(String sourceId);
    
}
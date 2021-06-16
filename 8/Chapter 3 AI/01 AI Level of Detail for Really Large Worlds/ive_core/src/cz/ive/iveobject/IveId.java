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
 
package cz.ive.iveobject;

/**
 *
 * IveId labeled objects' interface
 * IveId is represented as String value.
 * In hierarchies of IveObjects, this Id is generated as 
 * concatenation of IveId of a parent, separator "." and
 * the name of object beeing labeled.
 *
 * @author ondra
 */
public interface IveId {
    
    /** Separator to be used in hierarchical id */
    public static char SEP = '.';
    
    /**
     * Getter for Id
     *
     * @return unique string id identifying this object
     */
    public String getId();
    
    /**
     * Retrieves last part of the Id, which identify this object
     * amongs its siblings.
     *
     * @return last part of the Id
     */
    public String getFlatId();
    
    /**
     * Finds the least common parent of actual and given IveId. Which means
     * the first object in the parental hierarchy that is on both ways 
     * from objects to the root. This is just string comparison and ObjectMap 
     * lookup.
     *
     * @param kin IveId related with actual one
     * @return lowest common parent or null if it is currently not present
     *      in the ObjectMap
     */
    public IveObject getLeastCommonParent(IveId kin);
    
    /**
     * Finds the least common parent id of our id and given id. Which means id
     * of the first object in the parental hierarchy that is on both ways 
     * from objects to the root. This is just string comparison.
     *
     * @param kinId String id of possible relative
     * @return id of lowest common parent
     */
    public String getLeastCommonParentId(String kinId);
    
    /**
     * Lookups the parent in the ObjectMap
     *
     * @return parent or null if it is currently not present in the ObjectMap
     */
    public IveObject getParent();
    
    /**
     * Finds id of our parent by just searching our String id.
     *
     * @return Strign id of our parent
     */
    public String getParentId();

    /**
     * Looks for the least active parent. That is the first object on the way
     * through the parental hierarchy to the root, found in the ObjectMap.
     *
     * @return the least active parent or null if none was found, which means
     *      that our Id is sort of broken, because at least the root
     *      of the parental hierarchy should be active all the time.
     */
    public IveObject getLeastActiveParent();

    /**
     * Looks for the least active parent with lod smaller than i. 
     * 
     * @param i must be bigger than 1
     * @return the least active parent with lod lower than i or null if none 
     *      was found, which means that our Id is sort of broken, because at 
     *      least the root of the parental hierarchy should be active all the 
     *      time.
     */
    public IveObject getLeastActiveParentWithLODLEThan(int i);
    
    
    
    /**
     * Queries whether our and the given id has the same parent
     *
     * @param siblingId String id of possible sibling
     * @return true iff the given id really is our sibling
     */
    public boolean isSibling(String siblingId);
    
    /**
     * Queries whether our and the given object has the same parent
     *
     * @param sibling possible sibling
     * @return true iff the given object really is our sibling
     */
    public boolean isSibling(IveId sibling);
    
    /**
     * Tests whether the given id is (not necessarily direct) parent of this 
     * object. It is realized only by string comparison of ids.
     *
     * @param parent Id to test on parentship
     * @return true if the given id is predecessor of this object, false 
     *         otherwise
     */
    public boolean isParent(IveId parent);
    
    /**
     * Finds first descendant from this Id to the given childId.
     * This is being done only on the level of String ids, no ObjectMap
     * querry is needed. Note that if the descendant is direct, than its id
     * will be returned. On the other hand our id is not correct answer even
     * if the given id was ours one.
     *
     * @param descendantId id of our (usually not direct) descendant
     * @return String id of our child on the way to given descendant 
     *      or <code>null</code> if we are not a parent of given id.
     */
    public String getChildPreceedingId(String descendantId);
    
    /**
     * Finds first descendant IveObject from this Id to the given child.
     * The real IveObject is retrieved from the ObjectMap.
     * Note that if the descendant is direct, than it will be returned
     * as being the first child on the way to itself.
     * On the other hand we shall never return IveObject representing this Id.
     *
     * @param descendantId id of our (usually not direct) descendant
     * @return our child IveObject on the way to given descendant 
     *      or <code>null</code> if we are not a parent of given id
     *      or if the child is currently not in the ObjectMap.
     */
    public IveObject getChildPreceeding(String descendantId);
    
}
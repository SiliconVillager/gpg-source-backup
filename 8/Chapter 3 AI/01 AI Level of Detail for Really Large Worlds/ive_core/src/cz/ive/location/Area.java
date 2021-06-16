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
 
package cz.ive.location;
import cz.ive.messaging.*;
import java.util.List;
import cz.ive.exception.NoObjectPlaceException;
import cz.ive.genius.AreaGenius;
import cz.ive.iveobject.ObjectClass;
import cz.ive.iveobject.IveObject;

/**
 * Interface describing an Area. This is basicly a WayPoint that can expand
 * into net of WayPoints with bigger LOD.
 *
 * @author ondra
 */
public interface Area extends WayPoint {
    
    /**
     * Getter for WayPoints that this area consists of.
     * @return WayPoint expansion on higher LOD
     */
    WayPoint[] getWayPoints();
    
    /**
     * Getter for WayPoints contained in this area and matching the given kind.
     * @param kind Kind to search for
     * @return WayPoints with the kind match
     */
    List<WayPoint> getWayPoints(Kind kind);
    
    /**
     * Getter for WayPoints that are <b>behind</b> the border in the Area 
     * specified by a possibly empty WayPoint (with at least valid id).
     * @param neighbour WayPoint representing an adjacent Area
     * @return array of possibly empty WayPoint that are situated in the Area
     *      represented by neighbour parameter and that are accessible in 
     *      a single step.
     */
    List<WayPoint> getBorderWayPoints(WayPoint neighbour);
    
    /**
     * Defines a WayPoint, on which will be placed objects of the given class id 
     * when expanding.
     * On that place will be placed each object, whose class id is equal to
     * the given id, or is a child and no more specific place was found.
     * In case of more places for one object, a random one will be chosen.
     * @param objClass Class of objects, which should be placed there
     * @param place WayPoint to place the objects
     */
    void defineObjectPlace(ObjectClass objClass, WayPoint place);
    
    /**
     * Undefines object place for the given class id.
     * Reverse action of defineObjectPlace().
     * @param objClass Class to remove the place from
     * @param place Place to undefine
     */
    void undefineObjectPlace(ObjectClass objClass, WayPoint place);

    /**
     * Undefines all object places for the given class id.
     * Reverse action of defineObjectPlace().
     * @param objClass Class to undefine
     */
    void undefineObjectPlace(ObjectClass objClass);

    /**
     * Clears all defined object places.
     */
    void clearObjectPlaces();
    
    /**
     * Finds object place for the given object among childs.
     * Considers object's class, kind and substantiality.
     * @param object Object to find place for
     * @return WayPoint Place for the object, null if no WayPoint found
     */
    WayPoint findObjectPlace(IveObject object);
    
    /**
     * Expands the area.
     * Places all objects placed in the area (existing or not, valid or not)
     * to some contained WayPoint when expanding.
     * The most specific class id on the way from each object's class id to the
     * class root will be found in the object places set defined by 
     * defineObjectPlace and the object will be placed there. If no entry 
     * is found for the object, the NoObjectPlaceException will be thrown.
     * Informs the interpreter.
     */
    void expand() throws NoObjectPlaceException;
    
    /**
     * Shrinks the location.
     * Places all objects from all childs (existing or not, valid or not)
     * to this WayPoint.
     * Informs the interpreter.
     */
    void shrink();
    
    /**
     * Sets the info needed to generate phantoms during expand.
     * @param info The phantom generation info
     */
    void setPhantomGenerationInfo(CommonArea.PhantomGenerationInfo info);
    
    /**
     * Forgets all children - to be used during shrink
     */
    void forgetWayPoints();
    
    /** 
     * Registers new expand listener. This should be used only by u Gui,
     * since the callback is performed in the Swing thread with the Simulation
     * lock locked.
     *
     * @param listener callback to be called when expanding.
     */
    public void registerGuiExpandListener(Listener listener);
    
    /** 
     * Registers new shrink listener. This should be used only by u Gui,
     * since the callback is performed in the Swing thread with the Simulation
     * lock locked.
     *
     * @param listener callback to be called when shrinking.
     */
    public void registerGuiShrinkListener(Listener listener);
    
    
    /** 
     * Unregisters expand listener. This should be used only by u Gui,
     * since the callback is performed in the Swing thread with the Simulation
     * lock locked.
     *
     * @param listener callback not to be called anymore.
     */
    public void unregisterGuiExpandListener(Listener listener);
    
    /** 
     * Unregisters shrink listener. This should be used only by u Gui,
     * since the callback is performed in the Swing thread with the Simulation
     * lock locked.
     *
     * @param listener callback not to be called anymore.
     */
    public void unregisterGuiShrinkListener(Listener listener);
    
    /**
     * Sets and activates the area genies.
     * 
     * @param genies Array of all area genies
     */
    public void setGenies(AreaGenius[] genies);
 
    /**
     * Deactivates area genies of this area
     */
    public void deactivateGenies();
}

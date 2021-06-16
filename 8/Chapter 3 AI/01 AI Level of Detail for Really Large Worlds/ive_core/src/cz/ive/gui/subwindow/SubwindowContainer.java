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
 
package cz.ive.gui.subwindow;

import java.util.*;

import cz.ive.gui.*;

/**
 * Abstract superclass of all Subwindow containers.
 * It defines methods for Subwindow adding/closing and for object sending
 *
 * @author ondra
 */
abstract public class SubwindowContainer implements Gui {
    
    /** All Gui implementing Subwindows */
    protected List<Gui> guiList;

    /** All Subwindows */
    protected List<Subwindow> subwindowList;
    
    /** Creates new instance of SubwindowContainer */
    public SubwindowContainer() {
        guiList = new LinkedList<Gui>();
        subwindowList = new LinkedList<Subwindow>();
    }

    /**
     * Add Subwindow to this container
     *
     * @param subwindow to be added
     */
    synchronized public void addSubwindow(Subwindow subwindow) {
        if (subwindow instanceof Gui)
            guiList.add((Gui)subwindow);
        subwindowList.add(subwindow);
    }
    
    /**
     * Closes given subwindow.
     *
     * @param subwindow to be closed
     */
    synchronized public void closeSubwindow(Subwindow subwindow) {
        if (subwindow instanceof Gui)
            guiList.remove((Gui)subwindow);
        subwindowList.remove(subwindow);
    }
    
    /**
     * Info of a given Subwindow was update.
     *
     * @param subwindow Subwindow which Info was updated
     */
    abstract public void updateSubwindow(Subwindow subwindow);
    
    /**
     * Paint alls Gui implementing subwindows.
     */
    synchronized public void paint() {
        for (Gui gui : guiList) {
            gui.paint();
        }
    }
    
    /** 
     * Is the object viewed? If yes, focus it! 
     *
     * @param object to be queried.
     * @return <code>true</code> iff the object was found and the 
     *      subwindow focused.
     */
    public boolean findAndFocus(Object object) {
        for (Subwindow subwindow: subwindowList) {
            if (subwindow.contain(object)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Forces the Subwindows to revalidate their contents. This is called when
     * major parts of current simulation were changed (e.g. after a load).
     */
    public void revalidateSubwindows() {
        List<Subwindow> deleteList = new LinkedList<Subwindow>();
        for (Subwindow subwindow : subwindowList) {
            if (subwindow.revalidateSubwindow())
                deleteList.add(subwindow);
        }
        for (Subwindow subwindow : deleteList) {
            closeSubwindow(subwindow);
        }
    }
}

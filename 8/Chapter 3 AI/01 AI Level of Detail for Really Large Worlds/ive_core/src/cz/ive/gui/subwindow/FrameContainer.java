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

/**
 * SubwindowContainer designed to store subwindows as child windows.
 *
 * @author ondra
 */
public class FrameContainer extends SubwindowContainer {
    
    /** Map from subwindows to the frames they are contained in */
    protected Map<Subwindow, SubwindowFrame> frames;
    
    /** Creates a new instance of FrameContainer */
    public FrameContainer() {
        frames = new HashMap<Subwindow, SubwindowFrame>();
    }

    /**
     * Add Subwindow to this container
     *
     * @param subwindow to be added
     */
    public void addSubwindow(Subwindow subwindow) {
        super.addSubwindow(subwindow);
        
        SubwindowFrame frame = new SubwindowFrame(this, subwindow);
        frame.setVisible(true);
        frames.put(subwindow, frame);
        subwindow.setSubwindowContainer(this);
        subwindow.setInvisible(false);
        subwindow.opened();
    }
    
    /**
     * Closes given subwindow.
     *
     * @param subwindow to be closed
     */
    public void closeSubwindow(Subwindow subwindow) {
        super.closeSubwindow(subwindow);

        SubwindowFrame frame = frames.remove(subwindow);
        if (frame != null) {
            frame.dispose();
        }
        subwindow.setInvisible(true);
        subwindow.closed();
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
                SubwindowFrame frame = frames.get(subwindow);
                frame.requestFocus();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Info of a given Subwindow was update.
     *
     * @param subwindow Subwindow which Info was updated
     */
    public void updateSubwindow(Subwindow subwindow) {
        SubwindowFrame frame = frames.get(subwindow);
        frame.updateFrame();
    }
    
}

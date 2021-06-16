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

import javax.swing.*;

/**
 * Interface for Subwindow viewed in the MainFrame.
 * This Subwindow can be asked to view (accept) a particular
 * object. This feature will be used when constructing 
 * popup menus (e.g. in the LocationTree)
 *
 * @author ondra
 */
public interface Subwindow {
    
    /**
     * Retrives Info for this Subwindow.
     *
     * @return Info filled with data about this Subwindow
     */
    public Info getInfo();
    
    /**
     * Sets responsible SubwindowContainer.
     *
     * @param container SubwindowContainer newly responsible for this Subwindow
     */
    public void setSubwindowContainer(SubwindowContainer container);
    
    /**
     * Query wether this Subwindow accepts (can view) a given Object.
     *
     * @param object that is being offered.
     * @return Info representing action with the object if it can be accepted or
     *      <code>null</code> if not.
     */
    public Info canAccept(Object object);
    
    /**
     * Does the subwindow already contain a given object?
     *
     * @param object that is querried.
     * @return <code>true</code> iff the object is already viewed by 
     *      this window.
     */
    public boolean contain(Object object);
    
    /**
     * Accept (view) the object. This can be called only after successfull 
     * call to canAccept.
     *
     * @param object that is being offered.
     */
    public void accept(Object object);
    
    /**
     * Retrives root panel of this Subwindow. It is not necessarilly 
     * this class, for example in case that we use some ToolBars and other 
     * controls. So whenever the Subwindow component is to be added to some 
     * container, the panel return by this call should be added instead 
     * of instance of this class.
     *
     * @return root panel of this GUI component
     */
    public JComponent getPanel();
    
    /**
     * Marks this Subwindow as invisible. That means it does not have to update 
     * itsef.
     *
     * @param invisible <code>true</code> iff this Subwindow is not currently
     *      on the screen.
     */
    public void setInvisible(boolean invisible);
    
    /**
     * Is this Subwindow invisible?
     *
     * @return <code>true</code> iff this Subwindow is not currently
     *      on the screen.
     */
    public boolean isInvisible();
    
    /**
     * Forces the Subwindow to revalidate its contents. This is called when
     * major parts of current simulation were changed (e.g. after a load).
     *
     * @return <code>true</code> iff the subwindow should be closed, since its
     *      contents are not valid any more.
     */
    public boolean revalidateSubwindow();
    
    /**
     * Subwindow was just closed.
     */
    public void closed();
    
    /**
     * Subwindow was just opened.
     */
    public void opened();
}

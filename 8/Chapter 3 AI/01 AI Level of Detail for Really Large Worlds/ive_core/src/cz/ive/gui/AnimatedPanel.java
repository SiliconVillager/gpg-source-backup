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
 
package cz.ive.gui;

import cz.ive.gui.subwindow.*;
import cz.ive.simulation.Scheduler;
import cz.ive.simulation.SchedulerImpl;
import cz.ive.simulation.SimulationState;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Abstract implementation of GUI panel, that supports
 * backbuffer and its filling from another thread
 *
 * @author Ondra
 */
abstract public class AnimatedPanel extends JPanel implements Gui, Subwindow {
    
    /** Info for all Animated panel... it probably wont be used */
    protected static Info ANIMATED_PANEL_INFO = new Info("Animated panel",
            "This panel views animated contents using a backbuffer",
            (Icon)null);
    
    /** Informations about this subwindow */
    protected Info info = ANIMATED_PANEL_INFO;
    
    /** Offscreen buffer to be filled from the thread of the simulation */
    public BufferedImage backBuffer;
    
    /** Window system toolkit */
    protected Toolkit toolkit;
    
    /** Responsible SubwindowContainer */
    protected SubwindowContainer container;
    
    /** Are we on the screen? Should we update? */
    protected boolean invisible = true;
    
    /**
     * Creates a new AnimatedPanel
     */
    public AnimatedPanel() {
        super(false);
        toolkit = getToolkit();
        setPreferredSize(new Dimension(300, 300));
    }
    
    /**
     * Fills the offscreen buffer. This method is supposed to be called
     * from the simulation thread, when the world is in a consistent state.
     * This method is threadsafe.
     */
    synchronized public void paint() {
        if (backBuffer == null || invisible) {
            return;
        }
        
        render();
        
        repaint();
    }
    
    /**
     * This method is meant to be overidden by subclasses in order to render
     * the simulation state. This method is not threadsafe. It is called
     * from paint method.
     */
    abstract protected void render();
    
    /**
     * Main Swing painting method. This method just copies contents of
     * the offscreen buffer to the screen (onto this component).
     *
     * @param g Graphics representing this component's canvas
     */
    public void paint(Graphics g) {
        
        synchronized (this) {
            
            Dimension d = getSize();
            
            if (backBuffer == null || backBuffer.getWidth() != d.width ||
                    backBuffer.getHeight() != d.height) {
                backBuffer = getGraphicsConfiguration().
                        createCompatibleImage(d.width, d.height);
            }
            
            Scheduler sch = SchedulerImpl.instance();
            
            // If we are not currently running, render the still scene anyway
            if (sch.getSimulationState() == SimulationState.STOPPED ||
                    sch.getSimulationState() == SimulationState.NO_SIMULATION) {
                sch.lockSimulation();
                render();
                sch.unlockSimulation();
            }
            
            if (backBuffer != null)  {
                g.drawImage(backBuffer, 0, 0, null);
            } else
                return;
        }
        toolkit.sync();
    }
    
    /**
     * Retrives Info for this Subwindow.
     *
     * @return Info filled with data about this Subwindow
     */
    public Info getInfo() {
        return info;
    }
    
    /**
     * Sets responsible SubwindowContainer.
     *
     * @param container SubwindowContainer newly responsible for this Subwindow
     */
    public void setSubwindowContainer(SubwindowContainer container) {
        this.container = container;
    }
    
    /**
     * Query wether this Subwindow accepts (can view) a given Object.
     *
     * @param object that is being offered.
     * @return Info representing action with the object if it can be accepted or
     *      <code>null</code> if not.
     */
    public Info canAccept(Object object) {
        return null;
    }
    
    /**
     * Does the subwindow already contain a given object?
     *
     * @param object that is querried.
     * @return <code>true</code> iff the object is already viewed by 
     *      this window.
     */
    public boolean contain(Object object) {
        return false;
    }
    
    /**
     * Accept (view) the object. This can be called only after successfull
     * call to canAccept.
     *
     * @param object that is being offered.
     */
    public void accept(Object object) {
    }
    
    /**
     * Retrives root panel of this Subwindow. It is not necessarilly
     * this class, for example in case that we use some ToolBars and other
     * controls. So whenever the Subwindow component is to be added to some
     * container, the panel return by this call should be added instead
     * of instance of this class.
     *
     * @return root panel of this GUI component
     */
    public JPanel getPanel() {
        return this;
    }
    
    /**
     * Marks this Subwindow as invisible. That means it does not have to update
     * itsef.
     *
     * @param invisible <code>true</code> iff this Subwindow is not currently
     *      on the screen.
     */
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }
    
    /**
     * Is this Subwindow invisible?
     *
     * @return <code>true</code> iff this Subwindow is not currently
     *      on the screen.
     */
    public boolean isInvisible() {
        return invisible;
    }
    
    /**
     * Forces the Subwindow to revalidate its contents. This is called when
     * major parts of current simulation were changed (e.g. after a load).
     *
     * @return <code>true</code> iff the subwindow should be closed, since its
     *      contents are not valid any more.
     */
    public boolean revalidateSubwindow() {
        return false;
    }
}

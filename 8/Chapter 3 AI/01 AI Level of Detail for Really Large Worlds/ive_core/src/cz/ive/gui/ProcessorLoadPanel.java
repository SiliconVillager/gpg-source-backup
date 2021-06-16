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

import cz.ive.IveApplication;
import cz.ive.logs.Log;
import cz.ive.simulation.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;

/**
 * Simple processor load JPanel
 *
 * @author ondra
 */
public class ProcessorLoadPanel extends JPanel {
    
    /** History length */
    protected final static int HISTORY = 32;
    
    /** Height of this component */
    protected final static int HEIGHT = 20;
    
    /** Processor load history */
    protected int[] history = new int[HISTORY];
    
    /** Actual history position */
    protected int position = 0;
    
    /** Creates a new instance of ProcessorLoadPanel */
    public ProcessorLoadPanel() {
        Dimension dim = new Dimension(HISTORY,  HEIGHT);
        setMaximumSize(dim);
        setMinimumSize(dim);
        setPreferredSize(dim);
        
        new Timer(250, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int load = 0;
                if (SchedulerImpl.instance().getSimulationState() ==
                        SimulationState.RUNNING) {
                    
                    load = SchedulerImpl.instance().getStatistics().
                            getCurrentLoad();
                    
                    if (IveApplication.logCpu) {
                        Log.config("PROCESSOR_LOAD <"+new Date().toString()+","+load+">");
                    }
                }
                setLoad(load);
            }
        }).start();
    }
    
    /**
     * Adds new value to the load history.
     *
     * @param load actual load of the system.
     */
    synchronized public void setLoad(int load) {
        history[position] = load;
        position = (position+1)%HISTORY;
        
        repaint();
    }
    
    /**
     * Paints the load history.
     *
     * @param g where to paint.
     */
    synchronized public void paint(Graphics g) {
        int i;
        int x = 0;
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, HISTORY, HEIGHT);
        
        
        for (x=0;x<HISTORY;x++) {
            i = (x+position)%HISTORY;
            if (history[i] < 33)
                g.setColor(Color.GREEN);
            else if (history[i] < 66)
                g.setColor(Color.YELLOW);
            else
                g.setColor(Color.RED);
            g.drawLine(x, HEIGHT - (int)(HEIGHT*(history[i]*0.01)), x, HEIGHT);
        }
    }
}

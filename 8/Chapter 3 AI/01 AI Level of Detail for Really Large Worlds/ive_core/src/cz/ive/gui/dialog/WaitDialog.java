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
 
package cz.ive.gui.dialog;

import cz.ive.gui.*;
import cz.ive.gui.icon.IconBag;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * Progress dialog window.
 *
 * @author ondra
 */
public class WaitDialog extends JDialog {
    
    /** Is the job running? */
    protected int running = 0;
    
    /** Is the window opened */
    protected boolean waiting = false;
    
    /** Instance of this class */
    protected static WaitDialog instance;
    
    /** Icon JLabel */
    protected JLabel iconLabel;
    
    /** Message JLabel */
    protected JLabel messageLabel;
    
    /** Progress */
    protected JProgressBar progress;
    
    /**
     * Creates a new instance of WaitDialog
     * @param owner parent frame
     */
    public WaitDialog( Frame owner) {
        super( owner, true);
        
        setDefaultCloseOperation(this.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setCursor(Cursor.getPredefinedCursor(
                Cursor.WAIT_CURSOR));
        
        createComponents();
        
        setTitle( "Wait please");
        setLocationRelativeTo( owner);
    }
    
    /**
     * Creates components of this dialog
     */
    protected void createComponents(){
        Container panel = getContentPane();
        panel.setLayout( new BorderLayout(10,10));
        
        
        JPanel controlPanel = new JPanel();
        
        iconLabel = new JLabel( IconBag.PLEASE_WAIT.getIcon());
        messageLabel = new JLabel( "Work in progress");
        messageLabel.setHorizontalAlignment(messageLabel.CENTER);
        progress = new JProgressBar(0, 100);
        progress.setValue(0);
        progress.setStringPainted(true);
        
        iconLabel.setBorder( new EtchedBorder( EtchedBorder.LOWERED));
        controlPanel.setLayout( new GridLayout( 2, 1, 5, 5));
        controlPanel.add(messageLabel);
        controlPanel.add(progress);
        
        panel.add( iconLabel, BorderLayout.NORTH);
        panel.add( controlPanel, BorderLayout.CENTER);
        
        pack();
    }
    
    /**
     * Getter for the only instance
     */
    public static synchronized WaitDialog instance() {
        return instance == null ?
            instance = new WaitDialog(MainFrame.instance()) :
            instance;
    }
    
    /**
     * Current (swing) thread waits for completition of all running tasks.
     */
    public void waitForCompletition(int millisToShow){
        long millisStart = System.currentTimeMillis();
        
        synchronized (this) {
            if (running == 0 || waiting) {
                return;
            }
            waiting = true;
        }
        
        synchronized (this) {
            while (running != 0 &&
                    System.currentTimeMillis() - millisStart < millisToShow
                    ) {
                try {
                    wait( 50);
                } catch (InterruptedException ex) {
                    // Thats ok.
                }
            }
            if (running == 0) {
                waiting = false;
                notifyAll();
                return;
            }
        }
        
        setVisible( true);
        
        synchronized (this) {
            waiting = false;
            notifyAll();
        }
    }
    
    /**
     * Job was started
     * @return <code>true</code> iff the job should be run synchronously
     */
    synchronized public boolean startJob() {
        running += 1;
        
        if (running == 1) {
            messageLabel.setText( "Work in progress");
            progress.setValue( 0);
            progress.setString( "");
            progress.setIndeterminate(true);
            MainFrame.instance().setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
            
            return false;
        }
        return true;
    }
    
    /**
     * Job was finished
     */
    synchronized public void finishJob() {
        running -= 1;
        notifyAll();
        if (running == 0) {
            while (waiting) {
                if (isVisible()) {
                    SwingUtilities.invokeLater( new Runnable(){
                        public void run() {
                            setVisible(false);
                            dispose();
                        }
                    });
                }
                try {
                    wait();
                } catch (InterruptedException ex) {
                    // That is ok
                }
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MainFrame.instance().setCursor(
                            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                            );
                }
            });
        }
    }
    
    /**
     * Sets up the message
     */
    synchronized public void setMessage( final String message) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                messageLabel.setText( message);
            }
        });
    }
    
    /**
     * Sets up the progress value
     */
    synchronized public void setProgress( final int progressInt) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                progress.setIndeterminate(false);
                progress.setValue( progressInt);
            }
        });
    }
    
    /**
     * Sets up the progress message
     */
    synchronized public void setProgressMessage( final String message) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                progress.setString(message);
            }
        });
    }
}


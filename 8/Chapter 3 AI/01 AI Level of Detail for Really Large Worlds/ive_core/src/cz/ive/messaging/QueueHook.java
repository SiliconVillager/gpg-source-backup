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
 
package cz.ive.messaging;

import java.util.Queue;

/**
 * Queue Hook implementation
 *
 * @author pavel
 */
public abstract class QueueHook extends SyncHook implements Hook {
    
    /** 
     * The queue of QueueHooks, in which this Hook will be added 
     * by calling signal() 
     */
    private java.util.Queue<QueueHook> queue;
    
    /** 
     * Remembers the mode of the Hook 
     */
    private boolean addOnce;
    
    /** 
     * Remembers the call of signal. Used only for addOnce Hooks 
     * to filter multiple signals 
     */
    private boolean signalled;

    /** 
     * Creates a new instance of QueueHook 
     *
     * @param queue the queue of hooks
     * @param addOnce mode of the Hook - add to the queue only by first 
     *      signal or by each
     */
    public QueueHook(java.util.Queue<QueueHook> queue, boolean addOnce) {
        this.queue = queue;
        this.addOnce = addOnce;
        this.signalled = false;
    }
    
    /** 
     * Creates a new instance of QueueHook without set properties
     * Until you assign a queue using changeQueue(), it will behave 
     * as a SyncHook
     */
    public QueueHook() {
        queue = null;
        addOnce = false;
        signalled = false;
    }

    /**
     * Adds a reference to this Hook to the queue
     * To remove it from the queue use only unsignal() or processSignal()
     * If the queue is null, immediately pulls legs of all registered listeners.
     */
    protected void signal() {
        if (queue == null) {
            notifyListeners();
            return;
        }
        
        if (!addOnce || !signalled) {
            queue.add(this);
        }
        
        if (addOnce) {
            signalled = true;
        }
    }
    
    /**
     * Removes one reference to this Hook from the queue
     */
    protected void unsignal() {
        queue.remove(this);
        signalled = false;
    }

    /**
     * Removes one reference to this Hook from the queue and pulls legs of 
     * all registered listeners
     */
    public void processSignal() {
        queue.remove(this);
        notifyListeners();
        signalled = false;
    }

    /** 
     * Changes the queue of hooks, removes all references to this hook 
     * from the previous queue
     *
     * @param queue the queue of hooks
     * @param addOnce mode of the Hook - add to the queue only by first 
     *      signal or by each
     */
    public void changeQueue( java.util.Queue<QueueHook> queue, 
        boolean addOnce) {
            if (this.queue != null) {
                while (this.queue.remove(this)) ;
            }
            this.queue = queue;
            this.addOnce = addOnce;
            this.signalled = false;
    }
}

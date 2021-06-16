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
 
package cz.ive.simulation;

import cz.ive.messaging.SyncHook;

/**
 * Class providing scheduling statistics
 * @author pavel
 */
public class SchedulingStatisticsImpl extends SyncHook 
        implements SchedulingStatistics {

    /** Determines whether the statistics measuring is in progress */
    private boolean running;

    /** The procentual bussiness to start cleaning */
    private int threshold;

    /** Minimum number of loops between two cleanings */
    private int minLoops;

    /** 
     * Counter of overloaded loops from the last cleaning.
     * This counter is used only in case of nonzero minLoops.
     * If we are not currently counting overloaded loops, it is set to
     * minLoop, not to delay the cleaning.
     */
    private int loopCounter;

    /** 
     * The window measuring time not spent by computing of the simulation
     * in each iteration, used for cleanup.
     * The window is used as a cyclic buffer.
     */
    private long[] cWindow;
    
    /** The first member of the cWindow cyclic buffer */
    private int cWindowFirst;

    /** Length of the measured cWindow */
    private int cWindowLen;

    /** Time spent by sleeping in the last cWindow */
    private long cSleepingTime;

    /** Scheduling time for one cWindow turnaround */
    private long cWindowTime;

    /** 
     * The window measuring time not spent by computing of the simulation
     * in each iteration, used for viewing.
     * The window is used as a cyclic buffer.
     */
    private long[] vWindow;
    
    /** The first member of the vWindow cyclic buffer */
    private int vWindowFirst;

    /** Length of the measured vWindow */
    private int vWindowLen;

    /** Time spent by sleeping in the last vWindow */
    private long vSleepingTime;

    /** Scheduling time for one vWindow turnaround */
    private long vWindowTime;

    /** Scheduling time for one scheduling loop */
    private long loopTime;

    /** Current load */
    private int currentLoad;
    
    /** The time overload above loopTime in previous steps */
    private long timePenalty;
    
    /** Determines whether the cleaning is enabled */
    private boolean cleaningEnabled;
    
    /** Determines whether we are int initializing phase. */
    private boolean init;
    
    /** Counter of frames of the init phase. */
    private int initCounter;
    
    /** 
     * Create a new instance of Statistics 
     * @param threshold Percentage of bussiness to start cleaning
     * @param windowLen Number of scheduling loops to consider.
     *        Longer windowLen means slower reaction, but better stability
     *        in local boosts
     * @param minLoops Minimum loops between two cleanings if the threshold
     *        is permanently overshooted
     * @param loopTime Time of one scheduling loop
     */
    public SchedulingStatisticsImpl(int threshold, int windowLen, int minLoops, 
                long loopTime) {

        resetStatistics(threshold, windowLen, minLoops, loopTime);

        running = false;
        cleaningEnabled = true;
        currentLoad = 0;
        timePenalty = 0;
    }

    public synchronized void resetStatistics(int threshold, int windowLen, 
            int minLoops, long loopTime) {

        if (threshold > 100) {
            threshold = 100;
        }
        if (threshold < 0) {
            threshold = 0;
        }
        if (windowLen < 1) {
            windowLen = 1;
        }
        if (loopTime < 1) {
            loopTime = 1;
        }
        this.threshold = threshold;
        this.cWindowLen = windowLen;
        this.vWindowLen = 10;
        this.minLoops = minLoops;
        this.cWindowTime = 0;
        this.vWindowTime = 0;
        this.loopTime = loopTime;
        cWindow = new long[cWindowLen];
        vWindow = new long[vWindowLen];
        timePenalty = 0;
        init = true;
        initCounter = 0;

        clearStatistics();
    }

    public void resetStatistics(int threshold, int windowLen, int minLoops) {
        resetStatistics(threshold, windowLen, minLoops, loopTime);
    }
    
    public void clearPenalty() {
        timePenalty = 0;

        init = true;
        initCounter = 0;
        vWindowTime = 0;
        cWindowTime = 0;
        vSleepingTime = 0;
        cSleepingTime = 0;
    }
    
    public synchronized void setLoopTime(long loopTime) {
        resetStatistics(threshold, cWindowLen, minLoops, loopTime);
    }

    public synchronized void clearStatistics() {

        /* set all values as we were sleeping whole the time before..
         * this will help to get over the initializing load boost */

        cWindowFirst = 0;
        for (int i=0; i<cWindowLen; i++) {
            cWindow[i] = loopTime;
        }
        
        vWindowFirst = 0;
        for (int i=0; i<vWindowLen; i++) {
            vWindow[i] = loopTime;
        }

        this.cSleepingTime = 0;
        this.vSleepingTime = 0;
        this.loopCounter = minLoops;

        clearPenalty();
    }

    public synchronized void startStatistics() {
        clearPenalty();
        running = true;
    }
    
    public synchronized void stopStatistics() {
        running = false;
    }

    public synchronized boolean isRunning() {
        return running;
    }
    
    public synchronized void shift(long time) {
        long vLoad, cLoad;

        if (! running) {
            return;
        }

        time = loopTime - time - timePenalty;
        timePenalty = 0;
        
        if (time > loopTime) {
            time = loopTime;
        }
        if (time < 0) {
            timePenalty = -time;
            time = 0;
        }

            
        if (initCounter>=cWindowLen) {
            cSleepingTime -= cWindow[cWindowFirst];
        } else {
            cWindowTime += loopTime;
        }
        cSleepingTime += time;
        cWindow[cWindowFirst++] = time;
        if (cWindowFirst == cWindowLen) {
            cWindowFirst = 0;
        }

        if (initCounter>=vWindowLen) {
            vSleepingTime -= vWindow[vWindowFirst];
        } else {
            vWindowTime += loopTime;
        }
        vSleepingTime += time;
        vWindow[vWindowFirst++] = time;
        if (vWindowFirst == vWindowLen) {
            vWindowFirst = 0;
        }

        if (init) {
            initCounter++;
        }
        if ((initCounter>vWindowLen) && (initCounter>cWindowLen)) {
            init = false;
        }
            
        cLoad = 100 - ((100*cSleepingTime) / cWindowTime);
        vLoad = 100 - ((100*vSleepingTime) / vWindowTime);
        currentLoad = (int)vLoad;

        if (cleaningEnabled) {
            if (cLoad >= threshold) {
                if (loopCounter < minLoops) {
                    loopCounter++;
                } else {
                    /* clean */
                    loopCounter = 0;
                    notifyListeners();
                }
            } else {
                loopCounter = minLoops;
            }
        }
    }

    public int getCurrentLoad() {
       return currentLoad;
    }
    
    public synchronized int getThreshold() {
        return threshold;
    }
    
    public synchronized int getWindowLen() {
        return cWindowLen;
    }
    
    public synchronized int getMinLoops() {
        return minLoops;
    }

    public void enableCleanup(boolean enable) {
         cleaningEnabled = enable;
    }
    
    public boolean cleanupEnabled() {
        return cleaningEnabled;
    }
    
}

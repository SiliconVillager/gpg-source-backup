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

import cz.ive.messaging.Hook;

/**
 * Interface for controlling scheduling statistics.
 * For a usage of the statistics for cleaning, the Hook interface is provided
 * @author pavel
 */
public interface SchedulingStatistics extends Hook {

    /** 
     * Reset the statistics parameters and clears the state
     * @param threshold Percentage of bussiness to start cleaning
     * @param windowLen Number of scheduling loops to consider.
     *        Longer windowLen means slower reaction, but better stability
     *        in local boosts
     * @param minLoops Minimum loops between two cleanings if the threshold
     *        is permanently overshooted
     * @param loopTime Time of one scheduling loop
     */
    void resetStatistics(int threshold, int windowLen, int minLoops, 
            long loopTime);

    /** 
     * Reset the statistics parameters and clears the state
     * @param threshold Percentage of bussiness to start cleaning
     * @param windowLen Number of scheduling loops to consider.
     *        Longer windowLen means slower reaction, but better stability
     *        in local boosts
     * @param minLoops Minimum loops between two cleanings if the threshold
     *        is permanently overshooted
     */
    void resetStatistics(int threshold, int windowLen, int minLoops);

    /** 
     * setter for the loopTime parameter. This method clears the statistics
     * state 
     */
    void setLoopTime(long loopTime);

    /** clear all measured statistics */
    void clearStatistics();

    /** start statistics measuring if they are not running yet */
    void startStatistics();
    
    /** stop statistics measuring */
    void stopStatistics();
    
    /** 
     * Clears the time penalty, when we are no longer going to hurry because
     * of the previous slowness.
     */
    void clearPenalty();

    /** 
     * Checks whether the statistics are measured.
     * @return true iff the statistics are measured and used.
     */
    boolean isRunning();
    
    /** gets current business percentage */
    int getCurrentLoad();
    
    /** 
     * Shifts the window and recomputes and apply all statistics 
     * @param time Time spent by computing in this iteration
     */
    void shift(long time);
    
    /**
     * Getter for the current treshnold for cleaning.
     * @return percentage of bussiness to start cleaning
     */
    int getThreshold();
    
    /**
     * Getter for the current window lenght
     * @return Number of scheduling loops considered
     */
    int getWindowLen();
    
    /**
     * Getter for the current minimal number of loops between cleanups.
     * @return minimum loops between two cleanings
     */
    int getMinLoops();
    
    /**
     * Enables or disables load-based cleanup.
     * @param enable The requested mode
     */
    void enableCleanup(boolean enable);
    
    /**
     * Checks whether the load-based cleanup is enabled.
     * @return true iff the cleanup is enabled
     */
    boolean cleanupEnabled();

}

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
 
package cz.ive.exception;

import cz.ive.location.WayPoint;
import cz.ive.logs.Log;

/**
 * Exception raised when no WayPoint to place object when expanding an area 
 * is found for the object's class id.
 *
 * @author pavel
 */
public class NoObjectPlaceException extends Exception {
    
    /** WayPoint throwing this exception */
    private WayPoint wp;
    
    /** Creates a new instance of NoObjectPlaceException */
    public NoObjectPlaceException() {
        wp = null;
    }
    
    /** Creates a new instance of NoObjectPlaceException */
    public NoObjectPlaceException(String message) {
        super(message);
        wp = null;
    }
    
    /** Creates a new instance of NoObjectPlaceException */
    public NoObjectPlaceException(String message, WayPoint wp) {
        super(message);
        this.wp = wp;
    }

    /** Creates a new instance of NoObjectPlaceException */
    public NoObjectPlaceException(String message, Throwable cause) {
        super(message, cause);
        wp = null;
    }
    
    /** Creates a new instance of NoObjectPlaceException */
    public NoObjectPlaceException(Throwable cause) {
        super(cause);
        wp = null;
    }
    
    /** Prints this exception to the log. */
    public void log() {
        if (wp == null) {
            Log.severe(toString());
        } else {
            Log.addMessageToWaypoint(toString(), Log.SEVERE, wp);
        }
    }
}


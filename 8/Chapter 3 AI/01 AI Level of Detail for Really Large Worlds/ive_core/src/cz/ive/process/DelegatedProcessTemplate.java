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
 
package cz.ive.process;

import cz.ive.genius.*;
import cz.ive.location.*;
import cz.ive.process.*;
import cz.ive.exception.*;

/**
 * An extension to ProcessTemplate for delegated processes.
 *
 * @author honza
 */

public interface DelegatedProcessTemplate extends ProcessTemplate {
    
    /**
     * This method is called by the area genius to indicate it is able to
     * serve as a target of a delegation for the process which is represented
     * by this template.
     *
     * @param areaGenius Genius which is able to be the target of the 
     *          delegation.
     * @param areaId Area in which is this genius able to server as a target 
     *      of the delegation.
     */
    void register(AreaGenius areaGenius, String areaId);
    
    /**
     * This method is called by the area genius to indicate it is not able any 
     * more to serve as a target of a delegation for the process which is 
     * represented by this template.
     *
     * @param areaGenius Genius which is able to be the target of the 
     *          delegation.
     * @param areaId Area in which is this genius able to server as a target 
     *      of the delegation.
     */
    void unregister(AreaGenius areaGenius, String areaId);
}

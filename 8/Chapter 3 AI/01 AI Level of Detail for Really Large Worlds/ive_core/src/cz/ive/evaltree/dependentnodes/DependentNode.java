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
 
package cz.ive.evaltree.dependentnodes;



import cz.ive.evaltree.valueholdersimpl.ValueHolderImpl;
import java.util.List;
import cz.ive.messaging.*;
import cz.ive.process.Substitution;
import cz.ive.evaltree.*;
import cz.ive.sensors.Sensor;


/**
 * Succesor of all nodes that depends on anothers evaltree nodes
 */
public abstract class DependentNode<TYPE extends ValueHolderImpl,
                                    CHTYPE extends ValueHolderImpl> 
        extends Expr<TYPE, CHTYPE> implements Listener {
    
    
    /**
     * Compute value using actual child's values
     */
    protected abstract void updateValue();

    /**
     * Change the listening if needed.
     * This is caused typically by changes in the substitution.
     */
    protected abstract void updateInstantiation(Hook child);
    
    /**
     * To be used during construction instead of updateValue.
     * It initializes oldvalue member variable of value holder.
     */
    protected void initialUpdateValue() {
        updateValue();
        value.changed();
    }
    
    /**
     * Called when child has changed
     * @param child child that has changed
     */
    
    final public void changed(Hook child) {
        updateInstantiation(child);
        updateValue();
        if (value.changed()) { // value has changed
            signal();
        }
    }
    
    /**
     * We don't care about canceling inside of evaltree.
     * The cleanup is performed during uninstantiation
     */
    public void canceled(Hook initiator) {}
    
    /**
     * Instantiate all children and then compute the actual value.
     */
    public void instantiate(Substitution o, List<Sensor> sensors) {
        super.instantiate(o, sensors);
        initialUpdateValue();
    }

}

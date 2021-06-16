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
 
package cz.ive.location;

import cz.ive.evaltree.*;
import cz.ive.iveobject.*;

import java.io.Serializable;
import java.util.*;

/**
 * Class storing informations about movement of objects, 
 * to delay evaluation of reactions on this movement. 
 * It should also join movement messages
 * as mentioned in the specification. So remove and add messages
 * should be combined into one move message and so on.
 *
 * @author ondra
 */
public class JamImpl implements Jam, Serializable {
    
    /**
     * Receiver of stored movement messages.
     * It will be called during update.
     */
    protected MoveAware receiver;
    
    /**
     * Map from object ids to movement messages
     */
    protected Map<String,MovementMessage> map;
    
    /**
     * Queue of ids of objects that have moved
     */
    protected java.util.Queue<String> queue;
    
    /**
     * Creates new instance of JamImpl class
     *
     * @param receiver implementation of MoveAware iface that should be
     *      called during update method
     */
    public JamImpl(MoveAware receiver) {
        this.receiver = receiver;
        map = new HashMap<String,MovementMessage>();
        queue = new LinkedList<String>();
    }
    
    public void addObject(IveObject object, WayPoint location) {
        String id = object.getId();
        MovementMessage msg = map.get(id);
        
        if (msg != null) {
            msg = msg.update(MovementType.ADD, null, location, object);
            if (msg == null) {
                map.remove(id);
                queue.remove(id);
            }
        } else {
            msg = new MovementMessage(MovementType.ADD, null, location, object);
            map.put(id, msg);
            queue.offer(id);
        }
    }
    
    public void moveObject(IveObject object, WayPoint src, WayPoint dest) {
        String id = object.getId();
        MovementMessage msg = map.get(id);
        
        if (msg != null) {
            msg = msg.update(MovementType.MOVE, src, dest, object);
            if (msg == null) {
                map.remove(id);
                queue.remove(id);
            }
        } else {
            msg = new MovementMessage(MovementType.MOVE, src, dest, object);
            map.put(id, msg);
            queue.offer(id);
        }
    }
    
    public void removeObject(IveObject object, WayPoint location) {
        String id = object.getId();
        MovementMessage msg = map.get(id);
        
        if (msg != null) {
            msg = msg.update(MovementType.REMOVE, location, null, object);
            if (msg == null) {
                map.remove(id);
                queue.remove(id);
            }
        } else {
            msg = new MovementMessage(MovementType.REMOVE, location, null,
                    object);
            map.put(id, msg);
            queue.offer(id);
        }
    }
    
    
    public void expand(WayPoint location) {
        String id = location.getId();
        MovementMessage msg = map.get(id);
        
        if (msg != null) {
            msg = msg.update(MovementType.EXPAND, location, null, null);
            if (msg == null) {
                map.remove(id);
                queue.remove(id);
            }
        } else {
            msg = new MovementMessage(MovementType.EXPAND, location, null,
                    null);
            map.put(id, msg);
            queue.offer(id);
        }
    }

    public void shrink(WayPoint location) {
        String id = location.getId();
        MovementMessage msg = map.get(id);
        
        if (msg != null) {
            msg = msg.update(MovementType.SHRINK, location, null, null);
            if (msg == null) {
                map.remove(id);
                queue.remove(id);
            }
        } else {
            msg = new MovementMessage(MovementType.SHRINK, location, null,
                    null);
            map.put(id, msg);
            queue.offer(id);
        }
    }
    
    public void update() {
        while (!queue.isEmpty()) {
            String id = queue.remove();
            MovementMessage msg = map.remove(id);
            
            if (msg.type == MovementType.ADD) {
                receiver.addObject(msg.object, msg.destination);
            } else if (msg.type == MovementType.REMOVE) {
                receiver.removeObject(msg.object, msg.source);
            } else if (msg.type == MovementType.MOVE) {
                receiver.moveObject(msg.object, msg.source, msg.destination);
            } else if (msg.type == MovementType.EXPAND) {
                receiver.expand(msg.source);
            } else if (msg.type == MovementType.SHRINK) {
                receiver.shrink(msg.source);
            } else {
                // Should not occure
                assert false;
            }
        }
    }

    public boolean needUpdate() {
        return !queue.isEmpty();
    }
    
    /** Types of movement messages */
    protected enum MovementType { MOVE, ADD, REMOVE, EXPAND, SHRINK};
    
    /** Structure with movement message types */
    protected class MovementMessage {
        
        /** IveObject this message is associated with */
        public IveObject object;
        
        /** Type of movement */
        public MovementType type;
        
        /** Source WayPoint of the movement */
        public WayPoint source;
        
        /** Destination WayPoint of the movement */
        public WayPoint destination;
        
        /** 
         * Construct new instance of MovementMessage 
         *
         * @param type initial type of this message
         * @param source initial source of movement
         * @param destination initial destination of movement
         * @param object IveObject this message is associated with
         */
        public MovementMessage(MovementType type, WayPoint source, 
                WayPoint destination, IveObject object) {
            this.type = type;
            this.object = object;
            this.source = source;
            this.destination = destination;
        }
        
        /**
         * Updates values and type of this message
         *
         * @param type MovementType of new event
         * @param source source of movement event
         * @param destination destination of movement event
         * @param object IveObject this message is associated with
         * @return updated MovementMessage or null if changes cancelled out
         *      each other.
         */
        public MovementMessage update(MovementType type, WayPoint source, 
                WayPoint destination, IveObject object) {
            
            this.object = object;
            
            if (MovementType.ADD == this.type) {
                
                if (MovementType.ADD == type) {
                    // Should not occure
                    assert false;
                    
                    this.destination = destination;
                } else if (MovementType.REMOVE == type) {
                    return null;
                } else if (MovementType.MOVE == type) {
                    this.destination = destination;
                } else {
                    // Should not occure
                    assert false;
                }
            
            } else if (MovementType.REMOVE == this.type) {
                
                if (MovementType.ADD == type) {
                    if (this.source.getId().equals(destination.getId()))
                        return null;
                    this.destination = destination;
                    this.type = MovementType.MOVE;
                } else if (MovementType.REMOVE == type) {
                    // Should not occure
                    assert false;
                } else if (MovementType.MOVE == type) {
                    // Should not occure
                    assert false;
                    
                    this.destination = destination;
                    this.type = type;
                } else {
                    // Should not occure
                    assert false;
                }
            
            } else if (MovementType.MOVE == this.type){
                
                if (MovementType.ADD == type) {
                    // Should not occure
                    assert false;
                    
                    this.destination = destination;
                } else if (MovementType.REMOVE == type) {
                    this.type = type;
                    this.destination = null;
                } else if (MovementType.MOVE == type) {
                    if (this.source.getId().equals(destination.getId()))
                        return null;
                    this.destination = destination;
                } else {
                    // Should not occure
                    assert false;
                }
            
            } else if (MovementType.EXPAND == this.type) {
                if (MovementType.SHRINK == type) {
                    return null;
                } else {
                    // Should not occure
                    assert false;
                }
            } else if (MovementType.SHRINK == this.type) {
                if (MovementType.EXPAND == type) {
                    return null;
                } else {
                    // Should not occure
                    assert false;
                }
            } else {
                // Should not occure
                assert false;
            }
            
            return this;
        }
    }
}

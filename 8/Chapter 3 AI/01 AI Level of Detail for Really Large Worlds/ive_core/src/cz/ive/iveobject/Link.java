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
 
package cz.ive.iveobject;

import org.w3c.dom.NodeList;

/**
 * Simple holder of link information which identified goal, role and process.
 *
 * @author Jirka
 */
public class Link implements Cloneable, java.io.Serializable {
    
    private String goal;
    private String process;
    private String role;
    private NodeList userInfo;
    
    /**
     * Creates an empty link, all strings are set to "".
     */
    public Link(){
	
	goal = "";
	process = "";
	role = "";
	userInfo = null;
    }
    
    /**
     * Creates new link.
     *
     * @param g goal id
     * @param p process id
     * @param r role id
     * @param ui user info - xml root node
     */
    public Link(String g, String p, String r, NodeList ui){	
	goal = g;
	process = p;
	role = r;
	userInfo = ui;
    }
    
    /**
     * Makes a copy of the link.
     */
    public Object clone() {	
	Object o = null;
	try {
	    o = super.clone();
	} catch (CloneNotSupportedException e) {}
	
	return o;
    }
    
    /**
     * @return goal id
     */
    public String getGoal(){
	return goal;
    }
    
    /**
     * @return process id
     */
    public String getProcess(){	
	return process;
    }
    
    /**
     * @return role id
     */
    public String getRole(){	
	return role;
    }
    
    /**
     * @param newGoal new goal id
     */
    public void setGoal(String newGoal){	
	goal = newGoal;
    }
    
    /**
     * @param newProcess new process id
     */
    public void setProcess(String newProcess){	
	process = newProcess;
    }
    
    /**
     * @param newRole new role id
     */
    public void setRole(String newRole){	
	role = newRole;
    }
    
    public int hashCode() {
	int ret = ((goal == null) ? 0 : goal.hashCode()) +
		((process == null) ? 0 : process.hashCode()) + 
		((role == null) ? 0 : role.hashCode());
	return (ret == 0) ? super.hashCode() : ret;
    }
    
    /**
     * Compares Link to another according to contained values.
     * @return true, if goal, process and role values are the same 
     * in both objects.
     */
    public boolean equals(Object other) {		
	return (other instanceof Link) ? 
	    (goal.equals(((Link) other).getGoal()) && 
		process.equals(((Link) other).getProcess()) && 
		role.equals(((Link) other).getRole())) :
	    false;
    }
}
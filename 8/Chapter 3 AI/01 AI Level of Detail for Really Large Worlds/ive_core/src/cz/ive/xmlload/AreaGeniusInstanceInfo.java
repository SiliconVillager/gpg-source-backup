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
 
package cz.ive.xmlload;


import cz.ive.IveApplication;
import cz.ive.genius.AreaGenius;
import cz.ive.iveobject.IveId;
import cz.ive.logs.Log;
import cz.ive.ontology.OntologyToken;
import cz.ive.trigger.TriggerTemplate;
import cz.ive.util.Pair;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import org.w3c.dom.Element;
import static cz.ive.xmlload.XMLDOMLoader.*;
import cz.ive.xmlload.creators.Creator;
import cz.ive.xmlload.creators.OntologyTokenCreator;
import java.util.LinkedList;
import java.util.List;


/**
 * Contains information needed to create instance of AreaGenius
 * and creates the new instances of AreaGenius.
 *
 * @author thorm
 */
public class AreaGeniusInstanceInfo implements java.io.Serializable {

    /**
     *  Constructor obtained by java reflection that is used to create
     *  instance of the java class specified in xml.
     *  This is transient because of serialization.
     *  After load of the saved simulation it is assigned again using className
     *  member
     */
    public transient Constructor ctor;
    
    /**
     *  Unique identifier of genius
     */
    public String geniusId;
    
    /**
     * Name of the class that implements this genius.
     */
    private String className;

    List<Pair<TriggerTemplate, String> > cleaningGoals;

    /**
     *  Maps the delegated process to the top level goal
     */
    HashMap<String, String> processToGoal;    
    
    /**
     *  Load the information about genius stored in DOM element e
     *  It fills the member variables that are used during instantiation to 
     *  create new genius.
     *  @param e Element that contains genius description
     */
    public void load(Element e) {
        geniusId = e.getAttribute("geniusId");
        className = e.getAttribute("className");
        fillGeniusCtor();
        processToGoal = new HashMap<String, String>();
        cleaningGoals = new LinkedList<Pair<TriggerTemplate, String>>();
        for (Element tableItem : getChildElements(e, "TopLevelGoal")) {
            processToGoal.put(tableItem.getAttribute("process"),
                    tableItem.getAttribute("goal"));
        }
        for (Element cleaningGoalElem : getChildElements(e, "CleaningGoal")) {
            Creator<OntologyToken> oCreator = new OntologyTokenCreator();
            Element gtriggerElement = getOneChildElement(cleaningGoalElem,
                    "OntologyToken");
            TriggerTemplate gtrigger = (TriggerTemplate) oCreator.load(
                    gtriggerElement);
            String goalId = cleaningGoalElem.getAttribute("goalId");

            cleaningGoals.add(
                    new Pair<TriggerTemplate, String>(gtrigger, goalId));
        }
    }
    
    /**
     * Create the new genius instance and fill it by the data from member 
     * variables
     *
     * @param areaId location where the genius will rule.
     * @return new AreaGenius instance
     */
    public AreaGenius instantiate(String areaId) {
        AreaGenius ret = null;

        try {
            ret = (AreaGenius) ctor.newInstance(areaId + IveId.SEP + geniusId,
                    areaId);
            ret.setCleaningGoals(cleaningGoals);
        } catch (InstantiationException ex) {
            Log.severe("Instantiation exception " + geniusId);
        } catch (IllegalAccessException ex) {
            Log.severe("Illegal acces on constructor of " + geniusId);
        } catch (InvocationTargetException ex) {
            Log.severe("Illegal acces on constructor of " + geniusId);
        }
        
        ret.setDelegationTable(processToGoal);
        return ret;
    }
    
    /**
     * Loads contents of this object from the object stream.
     * We must also recreate the image this template represents.
     *
     * @param s stream to be used to load the description of this object.
     */
    private void readObject(java.io.ObjectInputStream s) throws
                java.io.IOException, ClassNotFoundException {
        
        // Read in any hidden serialization magic
        s.defaultReadObject();
        fillGeniusCtor();
        
    }

    /**
     * Fill the ctor member variable using className member variable
     */
    private void fillGeniusCtor() {
        try {
            ctor = IveApplication.instance().loadIveClass(className).getConstructor(
                    String.class, String.class);
        } catch (ClassNotFoundException ex) {
            Log.severe("Class not found exception " + className);
        } catch (NoSuchMethodException ex) {
            Log.severe(
                    "Class " + className + " does not have suitable constructor");
        }
    }
}


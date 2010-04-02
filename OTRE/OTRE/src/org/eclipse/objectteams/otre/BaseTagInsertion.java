/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BaseTagInsertion.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import de.fub.bytecode.generic.*;
import de.fub.bytecode.*;


import java.util.*;

import org.eclipse.objectteams.otre.util.*;

/**
 * This transformer inserts tag fields for all (topmost) bound base classes.
 * Tag fields look like this:
 * <pre>
 *   public short <i>TeamName</i>_OT$Tag;
 * </pre>
 * where <i>TeamName</i> is any team which has a callin binding for this base class.
 * It also inserts initialization of this tag into every constructor.
 * 
 * @version $Id: BaseTagInsertion.java 23408 2010-02-03 18:07:35Z stephan $
 * @author  Christine Hundt
 * @author  Stephan Herrmann
 */
public class BaseTagInsertion 
{
    private static boolean logging   = false;
    final   static String  tagSuffix = "_OT$Tag";

	static String getTagFieldName (String teamName) {
        return teamName.replace('.', '$') + tagSuffix;
    }

	static {
		if (System.getProperty("ot.log") != null)
			logging = true;
	}

	public static class SharedState extends ObjectTeamsTransformation.SharedState {
	    // Record base tags of interfaces here (String->HashMap). 
	    // Need to be considered in all implementing classes.
	    // baseName -> HashMap  (teamName -> tag (Integer)):
	    private HashMap<String, HashMap<String, Integer>> baseTagsOfInterfaces = new HashMap<String, HashMap<String, Integer>>();
	}

	SharedState state;
	
    public BaseTagInsertion(SharedState state) {
		this.state = state;
	}

	/**
     * @param ce
     * @param cg
     */
    public void doTransformInterface(ClassEnhancer ce, ClassGen cg) {
       
        String class_name = cg.getClassName();
        ConstantPoolGen cpg = cg.getConstantPool();
/*
 		if (interfaceTransformedClasses.contains(class_name))
 			continue; // already transformed!
*/

        HashMap<String, Integer> baseTags = getAllBaseTags(cg);

        if (baseTags == null) return; // not a bound base

        if (cg.isInterface())
        	state.baseTagsOfInterfaces.put(class_name, baseTags);
			
        state.interfaceTransformedClasses.add(class_name);
        if (logging)
			printLogMessage("Found bound base: " + class_name);
            
        Iterator<String> teams = baseTags.keySet().iterator();
        while (teams.hasNext()) {
        	String teamName = teams.next();
       	
        	String fieldName = getTagFieldName(teamName);//team + tagSuffix;

        	if (cg.containsMethod("get"+fieldName, "()S") == null)
        	{
        		if(logging)
        			printLogMessage("create base tag access get" + fieldName);
        		Integer tag = baseTags.get(teamName);
        		MethodGen mg = genBaseTagGetter(cpg, class_name, fieldName, tag.intValue(), cg.isInterface());
        		ce.addMethod(mg.getMethod(), cg);
        	}
        }
        // addedBaseTags.addAll(baseTags);
    }

	/**
	 * Collect base tags from this class and its super interfaces.
	 * @param cg
	 * @return
	 */
	private HashMap<String, Integer> getAllBaseTags(ClassGen cg) {
		// accumulate here base tags from bound super interfaces and this class:
		HashMap<String, Integer> baseTags = null;
		
		// TODO (SH): consider all transformed super interfaces.
		String superIfc = getTransformedSuperIfc(cg);
		if (superIfc != null) 
			baseTags = state.baseTagsOfInterfaces.get(superIfc);
		
		String class_name = cg.getClassName();
		HashMap<String, Integer> classBaseTags = CallinBindingManager.getBaseTags(class_name);
		if (CallinBindingManager.isRole(class_name)) {
				// search for base tags inherited from the implicit super (role) class:
				HashMap<String, Integer> inheritedBaseTags = CallinBindingManager.getInheritedBaseTags(class_name);
				classBaseTags.putAll(inheritedBaseTags);
				// search for base tags of the implementing role class:
				String implementingRoleName = ObjectTeamsTransformation.genImplementingRoleName(class_name);
				HashMap<String, Integer> implementingRolebaseTags = CallinBindingManager.getBaseTags(implementingRoleName);
				classBaseTags.putAll(implementingRolebaseTags);
				// TODO: check, if the we also need the implicitly inherited tags of the implementing role class!
		}
		
		if (baseTags == null)
			baseTags = classBaseTags;
		else
			baseTags.putAll(classBaseTags);
		
		return baseTags;
	}

    /**
     * Search the interfaces implemented by the class `cg' for an interface
     * that is a bound base. 
     * TODO (SH): should return all such interfaces!
	 * @param cg
	 * @return
	 */
	private String getTransformedSuperIfc(ClassGen cg) {
		String[] ifcs = cg.getInterfaceNames();
		for (int i=0; i<ifcs.length; i++){
			if (state.interfaceTransformedClasses.contains(ifcs[i]))
				return ifcs[i];
		}
		return null;
	}

	/**
	 * Generate a getter method for the base tag field.
	 * @param cpg
	 * @param class_name this (base-) class shall carry the new method
	 * @param fieldName name of the tag field.
	 * @param abstractFlg should the method be generated as abstract (ie., without a body)?
	 * @return
	 */
	static MethodGen genBaseTagGetter(
			ConstantPoolGen cpg, String class_name, 
			String fieldName, int tagValue, boolean abstractFlg) 
	{
		int accessFlags = Constants.ACC_PUBLIC;
		if (abstractFlg)
			accessFlags |= Constants.ACC_ABSTRACT;
			
		InstructionList il = new InstructionList();
		MethodGen mg = new MethodGen(
				accessFlags, 
				Type.SHORT, Type.NO_ARGS, new String[]{}, 
				"get"+fieldName, class_name, 
				il, cpg);
		if (!abstractFlg) {
			// gen: "return <constant tagValue>"
			il.append(new PUSH(cpg, tagValue));
			il.append(InstructionFactory.createReturn(Type.INT));
		}
		mg.setMaxStack();
		mg.setMaxLocals();
		return mg;
	}
	
    /**
     *  Insert tag initializations into each constructor.
     */	
	public void doTransformCode(ClassGen cg) {
    	// FIXME(SH): do not declare as CodeTransformer.
    }
  
    /**
     * @param message
     */
    private static void printLogMessage(String message) {
    	System.out.println(message);
    }
}

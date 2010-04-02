/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2009 Stephan Herrmann
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: LiftingParticipantTransformation.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 	Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;


import org.objectteams.Team;

import de.fub.bytecode.Constants;
import de.fub.bytecode.classfile.Method;
import de.fub.bytecode.generic.ALOAD;
import de.fub.bytecode.generic.BranchInstruction;
import de.fub.bytecode.generic.ClassGen;
import de.fub.bytecode.generic.ConstantPoolGen;
import de.fub.bytecode.generic.DUP;
import de.fub.bytecode.generic.GOTO;
import de.fub.bytecode.generic.IFNULL;
import de.fub.bytecode.generic.INVOKESPECIAL;
import de.fub.bytecode.generic.InstructionFactory;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.InstructionList;
import de.fub.bytecode.generic.LDC;
import de.fub.bytecode.generic.MethodGen;
import de.fub.bytecode.generic.NEW;
import de.fub.bytecode.generic.NOP;
import de.fub.bytecode.generic.ObjectType;
import de.fub.bytecode.generic.POP;
import de.fub.bytecode.generic.Type;

/**
 * If the property ot.lifting.participant is set transform all lift methods and insert 
 * static calls to createRole(Team,Object,String)Object; to the registered lifting participant
 * before creating a new role. If createRole returns non-null then that value is taken
 * as the new role, otherwise lifting proceeds as normal, i.e., normally creates a new role.
 *  
 * @author stephan
 * @since 1.3.1
 */
public class LiftingParticipantTransformation extends ObjectTeamsTransformation {
	

	private static String PARTICIPANT_NAME = System.getProperty("ot.lifting.participant");
	private static boolean checked = false;
	
	final private static String LIFT_PREFIX = "_OT$liftTo"; 

	private static final String WRONG_ROLE_EXCEPTION = "org.objectteams.WrongRoleException";
	private static final String LIFTING_FAILED_EXCEPTION = "org.objectteams.LiftingFailedException";
	private static final String LIFTING_VETO_EXCEPTION = "org.objectteams.LiftingVetoException";
	
	private static final ObjectType iLiftingParticipant = new ObjectType("org.objectteams.ILiftingParticipant"); 

	private static final String CREATE_ROLE_METHOD = "createRole";
	private static final String LIFTING_PARTICIPANT_FIELD = "_OT$liftingParticipant";
	
	public LiftingParticipantTransformation(SharedState state) { this(null, state); }

	public LiftingParticipantTransformation(ClassLoader loader, SharedState state) { super(loader, state); }

	public void doTransformCode(ClassGen cg) 
	{
		if (PARTICIPANT_NAME == null) return;
	
		if (!classNeedsTeamExtensions(cg)) return;
		
		synchronized (LiftingParticipantTransformation.class) {
			if (!checked) {
				try {
					// install a shared instance into class Team:
					Class<?> participantClass = loader.loadClass(PARTICIPANT_NAME);
					Team.class.getField(LIFTING_PARTICIPANT_FIELD).set(null, participantClass.newInstance());
				} catch (Exception e) {
					new IllegalArgumentException("Lifting participant "+PARTICIPANT_NAME+" is invalid.", e).printStackTrace();
					PARTICIPANT_NAME = null;
				}
				checked = true;
			}
		}
		
    	factory = new InstructionFactory(cg);
    	ConstantPoolGen cpg        = cg.getConstantPool();
    	String          class_name = cg.getClassName();
    	
    	// FIXME(SH): evaluate inclusion/exclusion filter per className
			
    	Method[] methods = cg.getMethods();
    	for (int i=0; i<methods.length; i++) {
    		Method m           = methods[i];
    		if (!m.getName().startsWith(LIFT_PREFIX))
    			continue;
    		
			cg.replaceMethod(m, m = weaveLiftingParticipant(m, class_name, cpg));
    	}
	}

	private Method weaveLiftingParticipant(Method m, String className, ConstantPoolGen cpg) {
		MethodGen mg = new MethodGen(m, className, cpg);
		InstructionList il = mg.getInstructionList();
		InstructionHandle[] ihs = il.getInstructionHandles();
		for (int i=0; i<ihs.length; i++) {
			InstructionHandle ih = ihs[i];
			if (ih.getInstruction() instanceof NEW) 
			{
				NEW newInstr = (NEW) ih.getInstruction();
				Type newType = newInstr.getType(cpg);
				String newTypeName = newType.toString();
				
				// don't transform creation of these exceptions:
				if (newTypeName.equals(LIFTING_FAILED_EXCEPTION)) continue;
				if (newTypeName.equals(LIFTING_VETO_EXCEPTION)) continue;
				if (newTypeName.equals(WRONG_ROLE_EXCEPTION)) continue;
				
				ih.setInstruction(new NOP()); // keep this handle for the enclosing switch
				
				InstructionList inset = new InstructionList();
				// fetch instance of lifting participant from Team._OT$liftingParticipant
				inset.append(factory.createFieldAccess(teamClassType.getClassName(),  
													   LIFTING_PARTICIPANT_FIELD, 
													   iLiftingParticipant, 
													   Constants.GETSTATIC));
				inset.append(new ALOAD(0)); 						// load the team
				inset.append(new ALOAD(1));							// load the base
				inset.append(new LDC(cpg.addString(newTypeName)));	// load the role class name
				inset.append(factory.createInvoke(iLiftingParticipant.getClassName(),	// receiver type 
												  CREATE_ROLE_METHOD, 					// method
												  object, 								// return type
												  new Type[] {teamType, object, string},// arg types
												  Constants.INVOKEINTERFACE));
				inset.append(new DUP());	// keep value after null-check
				BranchInstruction isNull = new IFNULL(null);
				inset.append(isNull);
				inset.append(factory.createCast(object, newType));
				// let goto skip: 0: new R, 1: dup, 2: aload_0, 3: aload_1, (4: cast if needed), 4: o. 5: invokespecial<init>
				int invokeOffset = 4;
				if (!(ihs[i+invokeOffset].getInstruction() instanceof INVOKESPECIAL))
					invokeOffset++;
				inset.append(new GOTO(ihs[i+invokeOffset+1])); // one past above sequence
				
				// continue here if null, i.e., perform the original new-instruction
				InstructionHandle goOn = inset.append(new POP()); // discard dup'ed value from above
				isNull.setTarget(goOn);
				inset.append(newInstr); // re-insert deleted first instruction
				
				il.append(ih, inset);
			}
		}
		return mg.getMethod();
	}
}

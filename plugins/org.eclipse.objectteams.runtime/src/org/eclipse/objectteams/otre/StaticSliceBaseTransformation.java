/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: StaticSliceBaseTransformation.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.*;

import org.eclipse.objectteams.otre.util.*;
import org.eclipse.objectteams.otre.util.CallinBindingManager.BoundSuperKind;


/**
 * Adds to base classes with callin bindings what can be literally pasted in:
 * 
 * Fields:
 * 		protected static Team[] _OT$activeTeams;
 * 		protected static int[] _OT$activeTeamIDs;
 * Methods:
 * 		public static void _OT$addTeam(Team team, in team_id)
 * 		public static void _OT$removeTeam(Team team)
 * Static initialization (adds clinit method, if not yet presented):
 * 		_OT$activeTeams = new Team[0];
 * 		_OT$activeTeamIDs = new int[0];
 * 
 *   
 * @version $Id: StaticSliceBaseTransformation.java 23408 2010-02-03 18:07:35Z stephan $
 * @author  Christine Hundt
 * @author  Stephan Herrmann
 */
public class StaticSliceBaseTransformation 
	extends ObjectTeamsTransformation 
{
	static final String _OT_ACTIVE_TEAMS=    "_OT$activeTeams";   //$NON-NLS-1$
	static final String _OT_ACTIVE_TEAM_IDS= "_OT$activeTeamIDs"; //$NON-NLS-1$

	public StaticSliceBaseTransformation(ClassLoader loader, SharedState state) { super(loader, state); }
	
	public void doTransformCode(ClassGen cg) {
		if (cg.isInterface())
			return; // can't add implementation
		// IMPLICIT_INHERITANCE
        
        String class_name = cg.getClassName();
		if (!CallinBindingManager.isBoundBaseClass(class_name) 
		/*&& !CallinBindingManager.containsBoundBaseInterface(cg.getInterfaceNames())*/)
			return; //only (base-)classes with callin bindings need this addition
		/*// this only works if teams are only added at the root bound base class
		if (CallinBindingManager.isBoundBaseClass(cg.getSuperclassName()))
			continue; // team infrastructur already added by super class
		*/
		if (CallinBindingManager.hasBoundBaseParent(class_name) == BoundSuperKind.CLASS)
			return; // team infrastructure already has been added to a super class

        ConstantPoolGen cpg = cg.getConstantPool();
		factory = new InstructionFactory(cpg);
		addStaticInitializations(cg, cpg); 
     }

	/**
	 * @param ce
	 * @param cg
	 */
	public void doTransformInterface(ClassEnhancer ce, ClassGen cg) {
		String class_name = cg.getClassName();
		ConstantPoolGen cpg = cg.getConstantPool();
		
		checkReadClassAttributes(ce, cg, class_name, cpg);
		
		if (cg.isInterface())
			return; // can't add implementation
		if (state.interfaceTransformedClasses.contains(class_name))
			return; // class has already been transformed by this transformer
		// IMPLICIT_INHERITANCE
		if (!CallinBindingManager.isBoundBaseClass(class_name)
		/*&& !CallinBindingManager.containsBoundBaseInterface(cg.getInterfaceNames())*/)
			return; //only (base-)classes with callin bindings need this addition
		/*// this only works if teams are only added at the root bound base class
		if (CallinBindingManager.isBoundBaseClass(cg.getSuperclassName()))
		continue; // team infrastructur already added by super class
		*/
		if (CallinBindingManager.hasBoundBaseParent(class_name) == BoundSuperKind.CLASS)
			return; // team infrastructure already has been added to a super class
		if(logging) printLogMessage("StaticSliceBaseTransformer transforms "+ class_name);
		
		if(CallinBindingManager.isRole(class_name)) {
			addImplicitSubclassNotificationInfrastructure(ce, cg);
		}

		// addition of the fields '_OT$activeTeams' and '_OT$activeTeamIDs':
		int accessFlags = Constants.ACC_PROTECTED | Constants.ACC_STATIC;

		FieldGen activeTeamsField = new FieldGen(accessFlags, teamArray, _OT_ACTIVE_TEAMS, cpg);
		ce.addField(activeTeamsField.getField(), cg);
		// generated global variable: protected static Team[] _OT$activeTeams;

		FieldGen activeTeamIDsField = new FieldGen(accessFlags, intArray, _OT_ACTIVE_TEAM_IDS, cpg);
		ce.addField(activeTeamIDsField.getField(), cg);
		// generated global variable: protected static int[] _OT$activeTeamIDs;

		factory = new InstructionFactory(cpg);
		
		// implementation of method '_OT$addTeam'
		ce.addMethod(genAddTeam(class_name, cg.getMajor(), cpg).getMethod(), cg);
            
		// implementation of method '_OT$removeTeam':
		ce.addMethod(genRemoveTeam(class_name, cg.getMajor(), cpg).getMethod(), cg);
            
		// implementation of the static class initialization method '<clinit>':
		/* Adding static initializations requires the addition of the clinit method, if not yet presented.
		 * This requires synchronization with other transformers (TeamInterfaceImplementer) 
		 * which may do the same this. This is done via 'TeamIdDispenser.clinitAdded(class_name)'.
		 */
		Method clinit = cg.containsMethod(Constants.STATIC_INITIALIZER_NAME, "()V");
         
		if (clinit != null || TeamIdDispenser.clinitAdded(class_name, loader)) {
			// the clinit-Method only has to be extended by the code transformation of this transformer
			state.interfaceTransformedClasses.add(class_name);
			return;
		}

		InstructionList il = new InstructionList();
		MethodGen clinitMethod = new MethodGen(Constants.ACC_STATIC,
                                                    Type.VOID,
                                                    Type.NO_ARGS,
                                                    new String[] { },
                                                    Constants.STATIC_INITIALIZER_NAME, class_name,
                                                    il, cpg);

		il.append(InstructionFactory.createReturn(Type.VOID));

		clinitMethod.setMaxStack();
		clinitMethod.setMaxLocals();
		
		ce.addMethod(clinitMethod.getMethod(), cg);

/***********************************************************************************************************/
		il.dispose();
		state.interfaceTransformedClasses.add(class_name);
    }

	/* Code to be generated:
	    public static void _OT$addTeam(Team team, int teamID)
	    {
	        int l;
	        // Second part of the "if" below: Avoid duplicate entry of the same team.
	        // Assumption (see r17473): this strategy assumes that a team instance
	        // can only be duplicated directly after the first registration.
	        // Reason: registerAtBases() calling addTeam() for multiple sub-bases
	        // which share the same _OT$activateTeams et al fields from a common super-base.
	        if((l = _OT$activeTeams.length) != 0 && _OT$activeTeams[0] == team)
	        {
	            return;
	        } else
	        {
	            Team newTeams[] = new Team[l + 1];
	            int newTeamIDs[] = new int[l + 1];
	            System.arraycopy(_OT$activeTeams, 0, newTeams, 1, l);
	            System.arraycopy(_OT$activeTeamIDs, 0, newTeamIDs, 1, l);
	            _OT$activeTeams = newTeams;
	            _OT$activeTeamIDs = newTeamIDs;
	            _OT$activeTeams[0] = team;
	            _OT$activeTeamIDs[0] = teamID;
	            return;
	        }
	    }
	 */
	private MethodGen genAddTeam(String class_name, int major, ConstantPoolGen cpg) {
		LocalVariableGen lg;
		InstructionList il;
		il = new InstructionList();
		MethodGen addTeamMethod = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC | Constants.ACC_SYNCHRONIZED,
                                                    Type.VOID,
                                                    new Type[] { teamType, Type.INT },
                                                    new String[] { "team", "teamID" },
                                                    "_OT$addTeam", class_name,
                                                    il, cpg);
	    // synchronized (BaseClass.class) {
	  int monitor = addClassMonitorEnter(addTeamMethod, il, class_name, major, cpg).first;

		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		il.append(new ARRAYLENGTH());
		lg = addTeamMethod.addLocalVariable("l", Type.INT, null, null);
		int l = lg.getIndex();
		il.append(new DUP());
		lg.setStart(il.append(InstructionFactory.createStore(Type.INT, l)));
		// generated: int l = _OT$activeTeams.length;
		
		// add duplication check:
	  BranchHandle emptyArray =
		il.append(new IFEQ(null));
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		il.append(new ICONST(0));
		il.append(InstructionFactory.createArrayLoad(teamType));
		il.append(new ALOAD(0));
	  BranchHandle noDuplication =
		il.append(new IF_ACMPNE(null));
	  GOTO earlyExit = new GOTO(null);
		il.append(earlyExit);
		InstructionHandle skipReturn = il.append(new NOP());
		emptyArray.setTarget(skipReturn);
		noDuplication.setTarget(skipReturn);
		// generated: if (l > 0 && _OT$activeTeams[0] == team ) return; 
		
		lg = addTeamMethod.addLocalVariable("newTeams", teamArray, null, null);
		int newTeams = lg.getIndex();
		il.append(InstructionFactory.createLoad(Type.INT, l));
		il.append(new ICONST(1));
		il.append(new IADD());
		il.append((Instruction)factory.createNewArray(teamType, (short)1));
		//this are very strange (but necessary!?) casts...
		lg.setStart(il.append(InstructionFactory.createStore(teamArray, newTeams)));
		// generated: Team[] newTeams = new Team[l+1];
		
		lg = addTeamMethod.addLocalVariable("newTeamIDs", intArray, null, null);
		int newTeamIDs = lg.getIndex();
		il.append(InstructionFactory.createLoad(Type.INT, l));
		il.append(new ICONST(1));
		il.append(new IADD());
		il.append((Instruction)factory.createNewArray(Type.INT, (short)1));
		//this are very strange (but necessary!?) casts...
		lg.setStart(il.append(InstructionFactory.createStore(intArray, newTeamIDs)));
		// generated: int[] newTeamIDs = new int[l+1];
		
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		il.append(new ICONST(0));
		il.append(InstructionFactory.createLoad(teamArray, newTeams));
		il.append(new ICONST(1));
		il.append(InstructionFactory.createLoad(Type.INT, l));
		ObjectType object = new ObjectType("java.lang.Object");
		il.append(factory.createInvoke("java.lang.System", "arraycopy",
                                            Type.VOID,
                                            new Type[] {object, Type.INT, object, Type.INT, Type.INT },
                                            Constants.INVOKESTATIC));
            // generated: System.arraycopy(_OT$activeTeams, 0, newTeams, 1, l);

		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAM_IDS, intArray, Constants.GETSTATIC));
		il.append(new ICONST(0));
		il.append(InstructionFactory.createLoad(intArray, newTeamIDs));
		il.append(new ICONST(1));
		il.append(InstructionFactory.createLoad(Type.INT, l));
		il.append(factory.createInvoke("java.lang.System", "arraycopy",
                                            Type.VOID,
                                            new Type[] {object, Type.INT, object, Type.INT, Type.INT },
                                            Constants.INVOKESTATIC));
            // generated: System.arraycopy(_OT$activeTeamIDs, 0, newTeamIDs, 1, l);

		il.append(InstructionFactory.createLoad(teamArray, newTeams));
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.PUTSTATIC));
		// generated: _OT$activeTeams   = newTeams;

		il.append(InstructionFactory.createLoad(intArray, newTeamIDs));
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAM_IDS, intArray, Constants.PUTSTATIC));
		// generated: _OT$activeTeamIDs = newTeamIDs;
		
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		il.append(new ICONST(0));
		il.append(new ALOAD(0));
		il.append(InstructionFactory.createArrayStore(teamType));
		// generated: _OT$activeTeams[0]   = team;
		
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAM_IDS, intArray, Constants.GETSTATIC));
		il.append(new ICONST(0));
		il.append(new ILOAD(1));
		il.append(InstructionFactory.createArrayStore(Type.INT));
		// generated: _OT$activeTeamIDs[0] = teamID;
		
		if (CallinBindingManager.isRole(class_name)) {
			il.append(new ALOAD(0));
			il.append(new ILOAD(1));
			il.append(factory.createInvoke(class_name, "_OT$activateNotify", Type.VOID, new Type[] { teamType, Type.INT }, Constants.INVOKESTATIC));
			// generated: _OT$activateNotify(team, teamID);
		}
		
	    // No more access to array fields, release monitor:
	  InstructionHandle exitSequence =
		il.append(InstructionFactory.createLoad(Type.OBJECT, monitor));
	    il.append(new MONITOREXIT());
	    earlyExit.setTarget(exitSequence);

		il.append(InstructionFactory.createReturn(Type.VOID));

		addTeamMethod.setMaxStack();
		addTeamMethod.setMaxLocals();
		addTeamMethod.removeNOPs();
		return addTeamMethod;
	}

	/* Code to be generated:
   		public static void _OT$removeTeam(Team team)
    	{
	        int l;
	        if((l = _OT$activeTeams.length) == 0)
	            return;
	        boolean found = false;
	        int newLen= l-1;
	        Team newTeams[] = new Team[newLen];
	        int newTeamIDs[] = new int[newLen];
	        for(int i = 0; i < l; i++)
	            if(!found)
	            {
	                if(_OT$activeTeams[i] == team)
	                {
	                    found = true;
	                } else if (i<newLen) { // coded as: jump if (newLen<=i)
	                {
	                    newTeams[i] = _OT$activeTeams[i];
	                    newTeamIDs[i] = _OT$activeTeamIDs[i];
	                }
	            } else
	            {
	                newTeams[i - 1] = _OT$activeTeams[i];
	                newTeamIDs[i - 1] = _OT$activeTeamIDs[i];
	            }
	
	        if(found)
	        {
	            _OT$activeTeams = newTeams;
	            _OT$activeTeamIDs = newTeamIDs;
	        }
	    }
	 */
	private MethodGen genRemoveTeam(String class_name, int major, ConstantPoolGen cpg) 
	{
		LocalVariableGen lg;
		InstructionList il;
		int l;
		BranchHandle emptyArray;
		int newTeams;
		int newTeamIDs;
		il = new InstructionList();
		MethodGen removeTeamMethod = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC | Constants.ACC_SYNCHRONIZED,
                                                    Type.VOID,
                                                    new Type[] { teamType },
                                                    new String[] { "team" },
                                                    "_OT$removeTeam", class_name,
                                                    il, cpg);

	    // synchronized (BaseClass.class) {
	    int monitor = addClassMonitorEnter(removeTeamMethod, il, class_name, major, cpg).first;

		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		il.append(new ARRAYLENGTH());
		lg = removeTeamMethod.addLocalVariable("l", Type.INT, null, null);
		//int l = lg.getIndex();
		l = lg.getIndex();
		il.append(new DUP());
		lg.setStart(il.append(InstructionFactory.createStore(Type.INT, l)));
		// generated: int l = _OT$activeTeams.length;

		emptyArray = il.append(new IFNE(null));
	  GOTO earlyExit = new GOTO(null);
		il.append(earlyExit);
		emptyArray.setTarget(il.append(new NOP()));
		// generated: if (l == 0) return;
		
		lg = removeTeamMethod.addLocalVariable("found", Type.BOOLEAN, null, null);
		int found = lg.getIndex();
		il.append(new ICONST(0));
		lg.setStart(il.append(InstructionFactory.createStore(Type.BOOLEAN, found)));
		// generated: boolean found = false;

		lg = removeTeamMethod.addLocalVariable("newTeams", teamArray, null, null);
		newTeams = lg.getIndex();
		il.append(InstructionFactory.createLoad(Type.INT, l));
		// [SH]: variable newLen
		LocalVariableGen lgNewLen= removeTeamMethod.addLocalVariable("newLen", Type.INT, il.getEnd(), null);
		il.append(new ICONST(1));
		il.append(new ISUB());
		// [SH] store for later use:
		il.append(new DUP());
		il.append(InstructionFactory.createStore(Type.INT, lgNewLen.getIndex()));
		// [HS] generated: "newLen= l-1;"
		il.append((Instruction)factory.createNewArray(teamType, (short)1));
		lg.setStart(il.append(InstructionFactory.createStore(teamArray, newTeams)));
		// generated: Team[] newTeams = new Team[newLen];
		
		lg = removeTeamMethod.addLocalVariable("newTeamIDs", intArray, null, null);
		//int newTeamIDs = lg.getIndex();
		newTeamIDs = lg.getIndex();
		//[SH]:
		il.append(InstructionFactory.createLoad(Type.INT, lgNewLen.getIndex()));
		il.append((Instruction)factory.createNewArray(Type.INT, (short)1));
		lg.setStart(il.append(InstructionFactory.createStore(intArray, newTeamIDs)));
		// generated: int[] newTeamIDs = new int[newLen];

		// start for-loop
		lg = removeTeamMethod.addLocalVariable("i", Type.INT, null, null);
		int loopCounter = lg.getIndex();
		il.append(new ICONST(0));
		lg.setStart(il.append(InstructionFactory.createStore(Type.INT, loopCounter)));
	  GOTO try_leave_loop = new GOTO(null);
		il.append(try_leave_loop);
		InstructionHandle i_lower_l = il.append(new NOP());
		// loop body:
		il.append(InstructionFactory.createLoad(Type.BOOLEAN, found));
	  IFNE already_found = new IFNE(null);
		il.append(already_found);

		// outer if part:
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		il.append(new ILOAD(loopCounter));
		il.append(InstructionFactory.createArrayLoad(teamType));
		il.append(new ALOAD(0)); //first parameter
	  IF_ACMPNE teams_not_equal = new IF_ACMPNE(null);
		il.append(teams_not_equal);
		
		// inner if part:
		il.append(new ICONST(1));
		il.append(InstructionFactory.createStore(Type.BOOLEAN, found));
	  GOTO skip_outer_else_part = new GOTO(null);
		il.append(skip_outer_else_part);
		
		// inner else part:
		teams_not_equal.setTarget(il.append(new NOP()));
		
		// [SH] sanity check:
		il.append(InstructionFactory.createLoad(Type.INT, lgNewLen.getIndex()));
		il.append(new ILOAD(loopCounter));
	  IF_ICMPLE if_len_le_i= new IF_ICMPLE(null);
		il.append(if_len_le_i);
		// [HS] generated: else if (!(newLen <= i)) { 
		il.append(new ALOAD(newTeams));
		il.append(new ILOAD(loopCounter));
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		il.append(new ILOAD(loopCounter));
		il.append(InstructionFactory.createArrayLoad(teamType));
		il.append(new AASTORE());
		// generated: newTeams[i] = _OT$activeTeams[i];
		
		il.append(new ALOAD(newTeamIDs));
		il.append(new ILOAD(loopCounter));
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAM_IDS, intArray, Constants.GETSTATIC));
		il.append(new ILOAD(loopCounter));
		il.append(InstructionFactory.createArrayLoad(Type.INT));
		il.append(new IASTORE());
		// generated: newTeamIDs[i] = _OT$activeTeamIDs[i];
		
	  GOTO end_of_loop = new GOTO(null);
		il.append(end_of_loop);
		
		// outer else part:
		already_found.setTarget(il.append(new NOP()));
		il.append(new ALOAD(newTeams));
		il.append(new ILOAD(loopCounter));
		il.append(new ICONST(1));
		il.append(new ISUB());
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		il.append(new ILOAD(loopCounter));
		il.append(InstructionFactory.createArrayLoad(teamType));
		il.append(new AASTORE());
		// generated: newTeams[i-1] = _OT$activeTeams[i];
		
		il.append(new ALOAD(newTeamIDs));
		il.append(new ILOAD(loopCounter));
		il.append(new ICONST(1));
		il.append(new ISUB());
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAM_IDS, intArray, Constants.GETSTATIC));
		il.append(new ILOAD(loopCounter));
		il.append(InstructionFactory.createArrayLoad(Type.INT));
		il.append(new IASTORE());
		// generated: newTeamIDs[i-1] = _OT$activeTeamIDs[i];
		
		skip_outer_else_part.setTarget(il.append(new NOP()));
		// [SH] connect "else if" from above:
		if_len_le_i.setTarget(il.getEnd());
		end_of_loop.setTarget(il.append(new IINC(loopCounter, 1)));
		try_leave_loop.setTarget(il.append(InstructionFactory.createLoad(Type.INT, loopCounter)));
		il.append(new ILOAD(l));
		il.append(new IF_ICMPLT(i_lower_l));
		// end for-loop
		
		il.append(InstructionFactory.createLoad(Type.BOOLEAN, found));
		BranchHandle notFound = il.append(new IFEQ(null));
		// generated: if (found) {
		
		il.append(InstructionFactory.createLoad(teamArray, newTeams));
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.PUTSTATIC));
		// generated: _OT$activeTeams = newTeams;

		il.append(InstructionFactory.createLoad(intArray, newTeamIDs));
		il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAM_IDS, intArray, Constants.PUTSTATIC));
		// generated: _OT$activeTeamIDs = newTeamIDs;

		if (CallinBindingManager.isRole(class_name)) {
			il.append(new ALOAD(0));
			il.append(factory.createInvoke(class_name, "_OT$deactivateNotify", Type.VOID, new Type[] { teamType }, Constants.INVOKESTATIC));
			// generated: _OT$deactivateNotify(team);
		}

	    // No more access to array fields, release monitor:
	  InstructionHandle exitSequence =
	    il.append(InstructionFactory.createLoad(Type.OBJECT, monitor));
	    il.append(new MONITOREXIT());
	    earlyExit.setTarget(exitSequence);
	    notFound.setTarget(exitSequence);

		il.append(InstructionFactory.createReturn(Type.VOID));

		removeTeamMethod.setMaxStack();
		removeTeamMethod.setMaxLocals();
		removeTeamMethod.removeNOPs();
		return removeTeamMethod;
	}

    /**
     * Add infrastructure for implicit subclasses to register and be notified. This 'observer' mechanism 
     * is necessary, because an implicit subclass does not inherit the addition/removal 
     * of a team at the implicit superclass. 
	 * @param ce	The ClassEnhancer object to add methods and fields.
	 * @param cg	The ClassGen object representing the current class. 
	 */
	private void addImplicitSubclassNotificationInfrastructure(ClassEnhancer ce, ClassGen cg) {

		ConstantPoolGen cpg = cg.getConstantPool();
		factory = new InstructionFactory(cpg);
		String class_name = cg.getClassName();
		ObjectType linkedListType = new ObjectType("java.util.LinkedList");
		int accessFlags = Constants.ACC_PROTECTED | Constants.ACC_STATIC;
		FieldGen teamRegistrationObserversField = new FieldGen(accessFlags, linkedListType, "_OT$teamRegistrationObservers", cpg);
		ce.addField(teamRegistrationObserversField.getField(), cg);
		
/***********************************************************************************************************/
		
		InstructionList il = new InstructionList();
		
		MethodGen observerRegistrationMethod = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                                                    Type.VOID,
                                                    new Type[] { classType },
                                                    new String[] { "implicitSubClass" },
                                                    "_OT$registerObserver", class_name,
                                                    il, cpg);

		il.append(factory.createFieldAccess(class_name, "_OT$teamRegistrationObservers", linkedListType, Constants.GETSTATIC));
		il.append(InstructionFactory.createLoad(classType, 0));
		il.append(factory.createInvoke("java.util.LinkedList", "add", Type.BOOLEAN, new Type[] { Type.OBJECT }, Constants.INVOKEVIRTUAL));
		il.append(new POP());
		il.append(new RETURN());
		
		observerRegistrationMethod.setMaxStack(2);
		observerRegistrationMethod.setMaxLocals();
		
		ce.addMethod(observerRegistrationMethod.getMethod(), cg);
/***********************************************************************************************************/
		il = new InstructionList();
		
		MethodGen activateNotifyMethod = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                                                    												  Type.VOID,
																									  new Type[] { teamType, Type.INT },
																									  new String[] { "team", "teamID" },
																									  "_OT$activateNotify", class_name,
																									  il, cpg);
		
		createNotifyMethodImplementation(activateNotifyMethod, cpg, class_name);
		
		ce.addMethod(activateNotifyMethod.getMethod(), cg);
/***********************************************************************************************************/
		il = new InstructionList();
		
		MethodGen deactivateNotifyMethod = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC,
                                                    												  Type.VOID,
																									  new Type[] { teamType },
																									  new String[] { "team" },
																									  "_OT$deactivateNotify", class_name,
																									  il, cpg);
		createNotifyMethodImplementation(deactivateNotifyMethod, cpg, class_name);

		ce.addMethod(deactivateNotifyMethod.getMethod(), cg);
	}


	/**
	 * Create implementation of methods '_OT$activateNotify' and _OT$deactivateNotify'.
	 * @param notifyMethod	Method which has to be implemented.
	 * @param cpg					Corresponding constant pool.
	 * @param class_name	Name of the class for wich to implement the method.
	 */
	private void createNotifyMethodImplementation(MethodGen notifyMethod, ConstantPoolGen cpg, String class_name) {
		boolean isActivateMethod = notifyMethod.getName().equals("_OT$activateNotify");
//		int methodArgs = isActivateMethod ? 2 : 1;

		int iteratorIdx = isActivateMethod ? 2 : 1;
		int curClassIdx = isActivateMethod ? 3 : 2;
		int anotherIdx1 = isActivateMethod ? 4 : 3;
		int anotherIdx2 = isActivateMethod ? 5 : 4;
		String methodToInvoke = isActivateMethod ? "_OT$addTeam" : "_OT$removeTeam";
		
		InstructionList il = notifyMethod.getInstructionList();
		il.append(factory.createFieldAccess(class_name,  "_OT$teamRegistrationObservers", new ObjectType("java.util.LinkedList"), Constants.GETSTATIC));
		BranchInstruction ifnonnull_3 = InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
		il.append(ifnonnull_3);
		il.append(InstructionFactory.createReturn(Type.VOID));
		InstructionHandle ih_7 = il.append(factory.createFieldAccess(class_name,  "_OT$teamRegistrationObservers", new ObjectType("java.util.LinkedList"), Constants.GETSTATIC));
		il.append(factory.createInvoke("java.util.LinkedList", "iterator", new ObjectType("java.util.Iterator"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		il.append(InstructionFactory.createStore(Type.OBJECT,iteratorIdx));
		InstructionHandle ih_14 = il.append(InstructionFactory.createLoad(Type.OBJECT,iteratorIdx));
		il.append(factory.createInvoke("java.util.Iterator", "hasNext", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEINTERFACE));
		BranchInstruction ifeq_20 = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
		il.append(ifeq_20);
		il.append(InstructionFactory.createLoad(Type.OBJECT,iteratorIdx));
		il.append(factory.createInvoke("java.util.Iterator", "next", Type.OBJECT, Type.NO_ARGS, Constants.INVOKEINTERFACE));
		il.append(factory.createCheckCast(classType));
		il.append(InstructionFactory.createStore(Type.OBJECT,curClassIdx));
		il.append(InstructionConstants.ACONST_NULL);
		il.append(InstructionFactory.createStore(Type.OBJECT,anotherIdx1));
		InstructionHandle ih_36 = il.append(InstructionFactory.createLoad(Type.OBJECT,curClassIdx));
		il.append(new PUSH(cpg, methodToInvoke));
		il.append(new PUSH(cpg,iteratorIdx));
		il.append((Instruction)factory.createNewArray(classType, (short) 1));
		il.append(InstructionConstants.DUP);
		
		il.append(new PUSH(cpg, 0));
		il.append(new PUSH(cpg, OTConstants.teamName));
		il.append(factory.createInvoke("java.lang.Class", "forName", 
															classType, new Type[] { Type.STRING },
															 Constants.INVOKESTATIC));

		il.append(InstructionConstants.AASTORE);
		if (isActivateMethod){ // add the integer argument:
			il.append(InstructionConstants.DUP);
			il.append(new PUSH(cpg, 1));
			il.append(factory.createFieldAccess("java.lang.Integer", "TYPE", classType, Constants.GETSTATIC));
			il.append(InstructionConstants.AASTORE);
		}
		il.append(factory.createInvoke("java.lang.Class", "getMethod", new ObjectType("java.lang.reflect.Method"), new Type[] { Type.STRING, new ArrayType(classType, 1) }, Constants.INVOKEVIRTUAL));
		InstructionHandle ih_76 = il.append(InstructionFactory.createStore(Type.OBJECT,anotherIdx1));
		BranchInstruction goto_78 = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
		il.append(goto_78);
		InstructionHandle ih_81 = il.append(InstructionFactory.createStore(Type.OBJECT,anotherIdx2));
		il.append(factory.createFieldAccess("java.lang.System", "err", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
		il.append(new PUSH(cpg, "activateNotifyMethod not found!"));
		il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
		InstructionHandle ih_91 = il.append(InstructionFactory.createLoad(Type.OBJECT,anotherIdx1));
		BranchInstruction ifnull_93 = InstructionFactory.createBranchInstruction(Constants.IFNULL, null);
		il.append(ifnull_93);
		il.append(InstructionFactory.createLoad(Type.OBJECT,anotherIdx1));
		il.append(InstructionConstants.ACONST_NULL);
		il.append(new PUSH(cpg,iteratorIdx));
		il.append((Instruction)factory.createNewArray(Type.OBJECT, (short) 1));
		il.append(InstructionConstants.DUP);
		il.append(new PUSH(cpg, 0));
		il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
		il.append(InstructionConstants.AASTORE);

		if (isActivateMethod) {	// load the integer argument:
			il.append(InstructionConstants.DUP);
			il.append(new PUSH(cpg, 1));
			il.append(factory.createNew("java.lang.Integer"));
			il.append(InstructionConstants.DUP);
			il.append(InstructionFactory.createLoad(Type.INT, 1));
			il.append(factory.createInvoke("java.lang.Integer", Constants.CONSTRUCTOR_NAME, Type.VOID, new Type[] { Type.INT }, Constants.INVOKESPECIAL));
			il.append(InstructionConstants.AASTORE);
		}
		il.append(factory.createInvoke("java.lang.reflect.Method", "invoke", Type.OBJECT, new Type[] { Type.OBJECT, new ArrayType(Type.OBJECT, 1) }, Constants.INVOKEVIRTUAL));
		InstructionHandle ih_121 = il.append(InstructionConstants.POP);
		InstructionHandle ih_122;
		BranchInstruction goto_122 = InstructionFactory.createBranchInstruction(Constants.GOTO, ih_14);
		ih_122 = il.append(goto_122);
		InstructionHandle ih_125 = il.append(InstructionFactory.createStore(Type.OBJECT,anotherIdx2));
		il.append(factory.createFieldAccess("java.lang.System", "err", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
		il.append(new PUSH(cpg, "Can not call activateNotifyMethod!"));
		il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
		BranchInstruction goto_135 = InstructionFactory.createBranchInstruction(Constants.GOTO, ih_14);
		il.append(goto_135);
		InstructionHandle ih_138 = il.append(InstructionFactory.createStore(Type.OBJECT,anotherIdx2));
		il.append(factory.createFieldAccess("java.lang.System", "err", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
		il.append(new PUSH(cpg, "InvocationTargetException"));
		il.append(factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
		BranchInstruction goto_148 = InstructionFactory.createBranchInstruction(Constants.GOTO, ih_14);
		il.append(goto_148);
		InstructionHandle ih_151 = il.append(InstructionFactory.createReturn(Type.VOID));
		ifnonnull_3.setTarget(ih_7);
		ifeq_20.setTarget(ih_151);
		goto_78.setTarget(ih_91);
		ifnull_93.setTarget(ih_122);
		notifyMethod.addExceptionHandler(ih_36, ih_76, ih_81, new ObjectType("java.lang.NoSuchMethodException"));
		notifyMethod.addExceptionHandler(ih_91, ih_121, ih_125, new ObjectType("java.lang.IllegalAccessException"));
		notifyMethod.addExceptionHandler(ih_91, ih_121, ih_138, new ObjectType("java.lang.reflect.InvocationTargetException"));
		
		notifyMethod.setMaxStack();
		notifyMethod.setMaxLocals();
	}

	/**
     * Adds initialization of the team array and the team index array to the static initializer of this class.
     * If this base class is a role at the same time, then also add initialization of the observer list.
     * @param cg	The representation of the class.
     * @param cpg	The constant pool of the class.
	 */
	private void addStaticInitializations(ClassGen cg, ConstantPoolGen cpg) {
		Method clinitMethod = cg.containsMethod(Constants.STATIC_INITIALIZER_NAME, "()V");
		 /* The clinit method always exists at this moment, because it has been added 
         * by the interface transformer part of this transformer if necessary.
         */
		MethodGen mg = newMethodGen(clinitMethod, cg.getClassName(), cpg);
		InstructionList il = mg.getInstructionList();
		// add static initialization for added static fields at start of the <clinit> method:
		il.insert(inizializeStaticFields(cg.getClassName()));
		mg.setMaxStack();
		mg.setMaxLocals();
		Method newClinit = mg.getMethod();
		cg.replaceMethod(clinitMethod, newClinit);
		il.dispose();
		// Reuse instruction handles
	}
	
    /**
     * @param class_name
     * @return
     */
    private InstructionList inizializeStaticFields(String class_name) {
    	// STATIC_PARTS_TODO : in base: static initialization of team fields
        InstructionList il = new InstructionList();
        il.append(new ICONST(0));
        il.append((Instruction)factory.createNewArray(teamType, (short)1));
        //this are very strange (but necessary!?) casts...
        il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.PUTSTATIC));
        //generated: _OT$activeTeams = new Team[0];

        il.append(new ICONST(0));
        il.append((Instruction)factory.createNewArray(Type.INT, (short)1));
        //this are very strange (but necessary!?) casts...
        il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAM_IDS, intArray, Constants.PUTSTATIC));
        //generated: _OT$activeTeamIDs = new int[0];
        
        if (CallinBindingManager.isRole(class_name)) {
        	ObjectType linkedListType = new ObjectType("java.util.LinkedList");
        	il.append(factory.createNew(linkedListType));
        	il.append(new DUP());
        	il.append(factory.createInvoke("java.util.LinkedList", 
        								   Constants.CONSTRUCTOR_NAME, 
        								   Type.VOID, Type.NO_ARGS, 
        								   Constants.INVOKESPECIAL));
        								   
			il.append(factory.createFieldAccess(class_name, 
												"_OT$teamRegistrationObservers", 
												linkedListType, Constants.PUTSTATIC ));
        	
        	//generated: _OT$teamRegistrationObservers = new LinkedList();
        }
        return il;
    }
}

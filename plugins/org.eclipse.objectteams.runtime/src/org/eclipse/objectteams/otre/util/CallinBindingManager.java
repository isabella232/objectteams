/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CallinBindingManager.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;

//import org.apache.bcel.generic.Type; // just for javadoc.
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ObjectType;

import org.eclipse.objectteams.otre.OTREInternalError;
import org.eclipse.objectteams.otre.ObjectTeamsTransformation;
import org.eclipse.objectteams.otre.RepositoryAccess;
import org.eclipse.objectteams.otre.ObjectTeamsTransformation.BaseMethodInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @version $Id: CallinBindingManager.java 23408 2010-02-03 18:07:35Z stephan $
 * @author Christine Hundt
 */
@SuppressWarnings("nls")
public class CallinBindingManager {
	/** When asking for a bound super type, we may find a super class, a super interface or nothing. */
	public static enum BoundSuperKind {
		NONE, CLASS, INTERFACE;
	}

	// map of bindings for role classes: roleClassName -> RoleBaseBinding
	private static HashMap<String, RoleBaseBinding> roleBindings = new HashMap<String, RoleBaseBinding>(); 
	
	// map of bindings for base classes: baseClassName -> List[RoleBaseBinding]
	private static ListValueHashMap<RoleBaseBinding> baseBindings = new ListValueHashMap<RoleBaseBinding>();
	
	// maps a team to its handled bases: teamClassName -> List[BaseClassName]
	private static ListValueHashMap<String> basesPerTeam = new ListValueHashMap<String>();
	
	//	maps a team to its contained roles: teamClassName -> List[RoleClassName]
	private static ListValueHashMap<String> rolesPerTeam = new ListValueHashMap<String>();
	
	/**
	 * Add super-link of the BoundRole object for the RoleBaseBinding of 'className' to 'superClassName'.
	 * @param className				the name of the class for which to add the super-link
	 * @param superClassName	the name of the super class to be linked
	 */
	public static void addSuperRoleLink(String className, String superClassName) {
		if (!roleBindings.containsKey(superClassName))
			return; // no super role class stored!
		RoleBaseBinding rbbSuper = roleBindings.get(superClassName);
		RoleBaseBinding rbb = roleBindings.get(className);
		// establish link from the role to its super role class:
		rbb.getRoleClass().setSuper(rbbSuper.getRoleClass());
	}
	
	/**
	 * @param baseClassName
     */
	private static void addSuperBaseLink(String teamClassName, String baseClassName, RoleBaseBinding rbb) 
	{
		BoundClass baseClass = rbb.getBaseClass();
		ObjectType baseClassType = new ObjectType(baseClassName);
		// clone the set for re-entrance:
		Iterator<Entry<String, LinkedList<RoleBaseBinding>>> it = getBaseBindingsCloneIterator();
		while (it.hasNext()) {
			Entry<String, LinkedList<RoleBaseBinding>> entry = it.next();
			String currentBaseClassName = entry.getKey();
			if (currentBaseClassName.equals(baseClassName)) continue;
			ObjectType currentType = new ObjectType(currentBaseClassName);
			// BoundClass objects are unique per base object, so just take the first binding: 
			BoundClass currentBaseClass = entry.getValue().getFirst().getBaseClass();
			if(RepositoryAccess.safeSubclassOf(baseClassType, currentType)) {
				BoundClass rbbSuper = baseClass.getSuper();
				if (rbbSuper != null)  {
					if (rbbSuper.getName().equals(currentBaseClassName))
						continue; // no need for action: already set.
					baseClass.updateSuper(currentBaseClass, currentType);
				}
				baseClass.setSuper(currentBaseClass);
			}
			else if (RepositoryAccess.safeSubclassOf(currentType, baseClassType)) {
				BoundClass currentSuper = currentBaseClass.getSuper();
				// if sub base classes may be registered before super base class, 
				// this case has to be considered too:
				if (currentSuper != null) {
					if (currentSuper.getName().equals(baseClassName))
						continue; // no need for action: already set.
					currentBaseClass.updateSuper(baseClass, baseClassType);
				}
				currentBaseClass.setSuper(baseClass);
			}
		}
	}

    /**
     * Declare role playedBy base.
	 *
     * @param roleClassName		the name of the played role class
     * @param baseClassName		the name of the playing base class
     * @param baseIsInterface   whether the given base is an interface
     */
    public static void addRoleBaseBinding(String roleClassName, String baseClassName, boolean baseIsInterface, String teamClassName) {
		RoleBaseBinding rbb = roleBindings.get(roleClassName);
		if (rbb == null) {
			rbb = new RoleBaseBinding(roleClassName, baseClassName, baseIsInterface, teamClassName);
			roleBindings.put(roleClassName, rbb);
		}
		addSuperBaseLink(teamClassName, baseClassName, rbb);
		synchronized (baseBindings) {
			baseBindings.put(baseClassName, rbb);
		}
		addTeamRoleRelation(teamClassName, roleClassName);
  	}

    /**
     * @param teamClassName
     * @param baseClassName
     */
    public static void addTeamBaseRelation(String teamClassName, String baseClassName) {
    	JavaClass baseClass = null;
		try {
			baseClass = RepositoryAccess.lookupClass(baseClassName);
		} catch (ClassNotFoundException e) {
			// FIXME(SH): where to log?
			e.printStackTrace();
		}
    	if (baseClass != null && baseClass.isInterface()) { 
    		// TODO (SH): need to register with all implementing classes!
            if (logging) {
                ObjectTeamsTransformation.printLogMessage("*** Skipping base " + baseClassName + ": is an interface");
    			ObjectTeamsTransformation.printLogMessage("Classses implementing the interface " + baseClassName + " have to be transformed!");
    		}
    		addBoundBaseInterface(baseClassName);
    	} else {
    		List<String> bases = basesPerTeam.get(teamClassName);
    		if (bases==null || !bases.contains(baseClassName))
    			basesPerTeam.put(teamClassName, baseClassName);
    	}
    }
    
    /**
     * @param teamClassName
     * @return
     */
    public static List<String> getBasesPerTeam(String teamClassName) {
        // (PH): return empty list instead of null when team has no bases?
    	return basesPerTeam.get(teamClassName);
    }
    
    /**
     * @param teamClassName
     * @param roleClassName
     */
    public static void addTeamRoleRelation(String teamClassName, String roleClassName) {
        	List<String> roles = rolesPerTeam.get(teamClassName);
    		if (roles == null || !roles.contains(roleClassName))
    			rolesPerTeam.put(teamClassName, roleClassName);
    }
    
    /**
     * @param teamClassName
     * @return
     */
    public static List<String> getRolePerTeam(String teamClassName) {
    	return rolesPerTeam.get(teamClassName);
    }

    private static LinkedList<String> allRoles = new LinkedList<String>();
    
    /**
     * @param roleName
     */
    public static void addRole(String roleName) {
    	allRoles.add(roleName);
    }
    
    /**
     * @param roleName
     * @return
     */
    public static boolean isRole(String roleName) {
    	return allRoles.contains(roleName);
    }
    
    
	/**
     * Declare binding of a pair of methods.
	 * @param roleClassName
	 * @param bindingFileName TODO
	 * @param bindingLineNumber TODO
	 * @param bindingLineOffset TODO
	 * @param roleMethodName
	 * @param roleMethodSignature signature ready for interpretation by
     *                      {@link Type org.apache.bcel.generic.Type}
	 * @param isStaticRoleMethod TODO
	 * @param modifier "before", "after" or "replace"
	 * @param baseMethodName
	 * @param baseMethodSignature
	 * @param isStaticBaseMethod is the base method static?
	 * @param baseIsCallin is the base method a callin method?
	 * @param translationFlags one bit for the result and for each argument indicating whether lifting/lowering is needed
	 * @param liftMethodName
	 * @param liftMethodSignature
     */
    public static void addMethodBinding(
	        String roleClassName, String baseClassName, 
	        String bindingFileName, int bindingLineNumber, int bindingLineOffset,
            String bindingLabel, String roleMethodName, String roleMethodSignature, boolean isStaticRoleMethod, 
			String wrapperName, String wrapperSignature, String modifier, 
			String baseMethodName, String baseMethodSignature, 
			boolean isStaticBaseMethod,	boolean baseIsCallin, boolean covariantBaseReturn, 
			int translationFlags, String liftMethodName, String liftMethodSignature)
	{
		RoleBaseBinding rbb = roleBindings.get(roleClassName);
		if (rbb == null) {
			// no binding found, create it now:
			if (baseClassName == null)
				throw new OTREInternalError("PlayedBy attribute must be read before method bindings.");
			int lastDollar = roleClassName.lastIndexOf('$');
			String teamClassName = roleClassName.substring(0, lastDollar);
			rbb = new RoleBaseBinding(roleClassName, baseClassName, false, teamClassName);
			roleBindings.put(roleClassName, rbb);
		}
		//System.err.println(rbb.getRoleClass().getName()+"<-*->"+rbb.getBaseClass().getName());
		rbb.addMethodBinding(bindingFileName, bindingLineNumber, bindingLineOffset, 
											 bindingLabel, roleMethodName, roleMethodSignature, isStaticRoleMethod,
			                                 wrapperName, wrapperSignature, modifier,
			                                 baseMethodName, baseMethodSignature, 
			                                 isStaticBaseMethod, baseIsCallin, covariantBaseReturn,
			                                 translationFlags,
			                                 liftMethodName, liftMethodSignature);
  	
		if (modifier.equals("replace"))
			assignBaseCallTag(rbb.getBaseClassName(), baseMethodName, baseMethodSignature);
	}

    /**
     * Get all callin bindings for a given base method.
     * @param className the base class
     * @param methodName the base method
     * @return Collection of <pre>MethodBinding</pre>
     */
    public static Collection<MethodBinding> getBindingForBaseMethod(String baseClassName, String baseMethodName, String baseMethodSignature)
	{
		LinkedList<RoleBaseBinding> rbbList = baseBindings.get(baseClassName);
		LinkedList<MethodBinding> resultList = new LinkedList<MethodBinding>();
		if (rbbList != null) {
			Iterator<RoleBaseBinding> it = rbbList.iterator();
			while (it.hasNext()) {
				RoleBaseBinding rbb = it.next();
				List<MethodBinding> mbs = rbb.getBaseMethodBindings(baseMethodName, baseMethodSignature);
				if (mbs != null)
					addFiltered(resultList, mbs, baseMethodName, baseMethodSignature);
			}
		}
		// IMPLICIT_INHERITANCE
		addFiltered(resultList, 
					getImplicitlyInheritedBaseMethodBindings(baseClassName, baseMethodName, baseMethodSignature),
					baseMethodName,
					baseMethodSignature);
		if (resultList.isEmpty())
			return null;
		return resultList;
    }
    /** variant of the above, optimized for computing only a boolean result instead of a complete list. */
    public static boolean isBoundBaseMethod(String baseClassName, String baseMethodName, String baseMethodSignature)
	{
		LinkedList<RoleBaseBinding> rbbList = baseBindings.get(baseClassName);
		if (rbbList != null) {
			for (RoleBaseBinding rbb : rbbList) {
				List<MethodBinding> mbs = rbb.getBaseMethodBindings(baseMethodName, baseMethodSignature);
				if (mbs != null)
					for (MethodBinding mb : mbs)
						if (mb.matchesMethod(baseMethodName, baseMethodSignature, /* strict */ true))
								return true;
			}
		}
		// IMPLICIT_INHERITANCE
		for (MethodBinding mb : getImplicitlyInheritedBaseMethodBindings(baseClassName, baseMethodName, baseMethodSignature))
			if (mb.matchesMethod(baseMethodName, baseMethodSignature, /* strict */ true))
					return true;
		return false;
    }
    // add bindings from candidates to resultList, perhaps checking
    // full signatures (i.e., if not covariantBaseReturn).
    private static void addFiltered(Collection<MethodBinding> resultList, 
    								Collection<MethodBinding> candidates,
    								String name,
    							    String fullSignature) 
    {
   		for (MethodBinding methodBinding : candidates) 
			if (methodBinding.matchesMethod(name, fullSignature, /*strict*/false)) 
				resultList.add(methodBinding);			
    }
    
    /**
     * Gets all implicitly inherited method bindings for the given base method.
     * Note: the result can only contain elements for base classes which are
     * roles at the same time! 
     * @param baseClassName			the name of the base class
     * @param baseMethodName		the name of the base mehtod
     * @param baseMethodSignature	the descriptor or the base method signature
     * @return a Collection with all implicitly inherited method bindings
     */
    private static Collection<MethodBinding> getImplicitlyInheritedBaseMethodBindings(String baseClassName, String baseMethodName, String baseMethodSignature) { 
    	List<MethodBinding> resultList = new LinkedList<MethodBinding>();
    	
    	if (!isRole(baseClassName))
    		return resultList; // only roles can have implicit super types
		
		Iterator<Entry<String, LinkedList<RoleBaseBinding>>> it = getBaseBindingsCloneIterator();

		while (it.hasNext()) {
			Entry<String, LinkedList<RoleBaseBinding>> entry = it.next();
			String have = entry.getKey();
			// look for true superClass (not same):
			if (have.equals(baseClassName)) 
				continue;
			if (isImplicitSubtype(baseClassName, have)) {
				// now we have an implicit superClass:
				if (logging)
                    ObjectTeamsTransformation.printLogMessage(baseClassName
                            + " implicitly inherits callin bindings from " + have);
                // collect the signatures of all bound base methods:
   				List<RoleBaseBinding> rbbList = entry.getValue();
				if (rbbList != null) {
					Iterator<RoleBaseBinding> rbb_it = rbbList.iterator();
					while (rbb_it.hasNext()) {
						RoleBaseBinding rbb = rbb_it.next();
						List<MethodBinding> mbs = rbb.getBaseMethodBindings(baseMethodName, baseMethodSignature);
						if (mbs != null)
							resultList.addAll(mbs);
					}
				}
			}
		}
		//if (!resultList.isEmpty())
		//	System.err.println(resultList);
		return resultList;
    }

	/**
	 * Get all callin bindings that a given base class may inherit from its superclass.
	 * @param className
	 * @result list of MethodBinding
	 */
	public static Collection<MethodBinding> getInheritedCallinBindings (String className) {
/*
		List classBindings = baseBindings.get(className);
	if (classBindings!=null) {
		// any RoleBaseBinding contains the corresponding BoundClass object for the super base:
		RoleBaseBinding anyRBB = (RoleBaseBinding)classBindings.get(0);
		// problem: if a sub base class is not explicitly bound to a role class no corresponding 
		//                BoundClass object exists. How can we access the super bindings via the 
		//                super-link??
		BoundClass bc = anyRBB.getBaseClass();
		//System.out.println("-----------");
		while (bc.getSuper()!=null) {
			System.out.println(bc.getName());
			bc = bc.getSuper();
	}
		//System.out.println("-----------");
		}
*/		

		List<MethodBinding> result = new LinkedList<MethodBinding>();
		ObjectType current = new ObjectType(className);
		// clone for re-entrance:
		Iterator<Entry<String, LinkedList<RoleBaseBinding>>> it= getBaseBindingsCloneIterator();
		while (it.hasNext()) {
			Entry<String, LinkedList<RoleBaseBinding>> entry = it.next();
			String have = entry.getKey();
			// look for true superClass (not same):
			if (have.equals(className)) continue;
			ObjectType haveType = new ObjectType(have);
			if (RepositoryAccess.safeSubclassOf(current, haveType)) {
				// now we have a true superClass:
				if (logging)
                    ObjectTeamsTransformation.printLogMessage(className
                            + " inherits callin bindings from " + have);
                // collect the signatures of all bound base methods:
   				List<RoleBaseBinding> rbbList = entry.getValue();
				if (rbbList != null) {
					Iterator<RoleBaseBinding> rbb_it = rbbList.iterator();
					while (rbb_it.hasNext()) {
						RoleBaseBinding rbb = rbb_it.next();
						result.addAll(rbb.getBaseMethodBindings());
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Get all super classes defining callin bindings that a given static base
	 * method may inherit.
	 * 
	 * @author juerwid
	 * @param className
	 * @param methodName
	 * @param methodSignaturte
	 * @result list of <String[] {class_name}>
	 */
	public static Collection<String> getInheritedCallinBindingsForStaticMethods(
			String className, String methodName, String methodSignature)
	{
		List<String> result = new LinkedList<String>();
		ObjectType current = new ObjectType(className);
		// clone for re-entrance:
		Iterator<Entry<String, LinkedList<RoleBaseBinding>>> it = getBaseBindingsCloneIterator();
		boolean getNextClass = false;
		while (it.hasNext()) {
			Entry<String,LinkedList<RoleBaseBinding>> entry = it.next();			
			String have = entry.getKey();
			// look for true superClass (not same):
			if (have.equals(className)) continue;
			ObjectType haveType = new ObjectType(have);
			if (RepositoryAccess.safeSubclassOf(current, haveType)) {
				// now we have a true superClass:
				if (logging)
					ObjectTeamsTransformation.printLogMessage(className
							+ " inherits callin bindings from " + have);
                // collect the signatures of all bound base methods:
   				LinkedList<RoleBaseBinding> rbbList = entry.getValue();
				if (rbbList != null) {
					Iterator<RoleBaseBinding> rbb_it = rbbList.iterator();
					while (rbb_it.hasNext() && !getNextClass) {
						RoleBaseBinding rbb = rbb_it.next();
						List<String[]> methods = rbb.getBaseSignatures();
						Iterator<String[]> methodsIter = methods.iterator();
						while(methodsIter.hasNext() && !getNextClass){
							String[] aMethod = methodsIter.next();
							if(methodName.equals(aMethod[0]) && methodSignature.equals(aMethod[1])){
								result.add(have);
								getNextClass = true;
							}
						}
					}
					if(getNextClass){
						getNextClass = false;
						continue;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Checks if 'subType' is an implicit subtype of 'superType'.
	 * To be true the role name parts have to be equal and the 
	 * team name parts have to be in a 'real' subtype relationship.
	 *
	 * @param subType	the name of the potential subtype.
	 * @param superType	the name of the potential supertype.
	 * @return	true, if subType implicitly inherits from superType.
	 */
	private static boolean isImplicitSubtype(String subType, String superType) {
		//System.err.println(subType +" -?-> " + superType);
		int dollarIdxSub = subType.lastIndexOf('$');
		int dollarIdxSuper = superType.lastIndexOf('$');
		if (dollarIdxSub==-1 || dollarIdxSuper==-1)
			return false; // no roles
		String pureSubType = subType.substring(dollarIdxSub+1, subType.length());
		String pureSuperType = superType.substring(dollarIdxSuper+1, superType.length());
		if (!pureSubType.equals(pureSuperType))
			return false;// no identical role names
		
		String subTeamName = subType.substring(0, dollarIdxSub);
		String superTeamName = superType.substring(0,dollarIdxSuper);

		ObjectType subTeamType = new ObjectType(subTeamName);
		ObjectType superTeamType = new ObjectType(superTeamName);
		if (RepositoryAccess.safeSubclassOf(subTeamType, superTeamType)) {
            if (logging)
                ObjectTeamsTransformation.printLogMessage(subType
                        + " implicitly inherits method bindings from " + superType);
			//System.err.println(subType + " implicitly inherits method bindings from " + superType);
			return true;
		}
		return false; // no inheritance relation between teams
	}
	
	/**
	 * Get all inherited method bindings for a given base method, which are 
	 * bindings declared for a super base class.
	 *
	 * the actual base class has additional bindings for this method!  
	 * @param baseClassName			the name of the role class
	 * @param baseMethodName		the name of the role method
	 * @param baseMethodSignature	the signature of the role method
	 * @result a List containing all inherited bindings for the role method
	 */
	public static List<MethodBinding> getInheritedBaseMethodBindings(String baseClassName, String baseMethodName, String baseMethodSignature) {

		List<MethodBinding> inheritedMethodBindings = new LinkedList<MethodBinding>();
		if (!isBoundBaseClass(baseClassName)) {
			return inheritedMethodBindings;
		}
		LinkedList<RoleBaseBinding> baseBindingsForBase = baseBindings.get(baseClassName);
		if (baseBindingsForBase == null) 
			return inheritedMethodBindings;
		
		Iterator<RoleBaseBinding> it = baseBindingsForBase.iterator();
		while (it.hasNext()) {
			RoleBaseBinding rbb = it.next(); 
			BoundClass bc = rbb.getBaseClass();
			while (bc.getSuper() != null) {
				bc = bc.getSuper();
				String superRoleName = bc.getName();
				Collection<MethodBinding> superRoleMethodBindings = CallinBindingManager.getBindingForBaseMethod(
																				   		 superRoleName, 
																						 baseMethodName, 
																						 baseMethodSignature);
				if (superRoleMethodBindings!=null)
					inheritedMethodBindings.addAll(superRoleMethodBindings);
			}
		}
		return inheritedMethodBindings;
	}

	/**
	 * Get all inherited method bindings for a given role method, which are
	 * bindings declared in a super role class. The actual role class has
	 * additional bindings for this method!
	 * 
	 * @param roleClassName			the name of the role class
	 * @param roleMethodName		the name of the role method
	 * @param roleMethodSignature	the signature of the role method
	 * @result a List containing all inherited bindings for the role method
	 */
	public static List<MethodBinding> getInheritedRoleMethodBindings(String roleClassName, String roleMethodName, String roleMethodSignature) {
		List<MethodBinding> inheritedMethodBindings = new LinkedList<MethodBinding>();
		if (!isBoundRoleClass(roleClassName)) {
			return inheritedMethodBindings;
		}
		RoleBaseBinding rbb = roleBindings.get(roleClassName);
		BoundClass bc = rbb.getRoleClass();
		while (bc.getSuper() != null) {
			bc = bc.getSuper();
			String superRoleName = bc.getName();
			List<MethodBinding> superRoleMethodBindings = CallinBindingManager.getBindingsForRoleMethod(
																		superRoleName, 
																		roleMethodName, 
																		roleMethodSignature);
			inheritedMethodBindings.addAll(superRoleMethodBindings);
		}
		return inheritedMethodBindings;
	}

	/**
	 * All bindings for a given role method concerning its base class.
	 * 
	 * @return List of MethodBinding
	 */
	public static List<MethodBinding> getBindingsForRoleMethod(String roleClassName,
			String roleMethodName, String roleMethodSignature)
	{
		RoleBaseBinding rbb = roleBindings.get(roleClassName);
		if (rbb==null) {
			return new LinkedList<MethodBinding>();
		}
		List<MethodBinding> roleMethodBindings = rbb.getRoleMethodBindings(roleMethodName, roleMethodSignature);
		if (roleMethodBindings == null) {
			return new LinkedList<MethodBinding>();
		}
		return roleMethodBindings;
	}

	/**
	 * @param className
	 * @return
	 */
	public static boolean isBoundBaseClass(String className) {
		// and has at least one method binding?
		boolean result = baseBindings.containsKey(className);
		if (result || !isRole(className)) {
			return result;
		}
		else {// bound implicit super class?
			Iterator<String> it = getBaseBindingsKeyIterator();
			while (it.hasNext()) {
				String have = it.next();
				// look for true superClass (not same):
				if (have.equals(className)) 
					continue;
				if (isImplicitSubtype(className, have))
					return true;
			}
			return result;
		}
	}
	
	/**
	 * @param className
	 * @return
	 */
	public static boolean isBoundBaseAndRoleClass(String className) {
		// and has at least one method binding?
		if (baseBindings.containsKey(className)) 
			return true;
		// bound implicit super class?
		Iterator<String> it = getBaseBindingsKeyIterator();
		while (it.hasNext()) {
			String have = it.next();
			// look for true superClass (not same):
			if (have.equals(className)) 
				continue;
			if (isImplicitSubtype(className, have))
				return true;
		}
		return false;
	}
	
	/**
	 * @param baseClassName
	 * @return
	 */
	public static BoundSuperKind hasBoundBaseParent(String baseClassName) {
		LinkedList<RoleBaseBinding> rbbList = baseBindings.get(baseClassName);
		if (rbbList != null) {
			Iterator<RoleBaseBinding> it = rbbList.iterator();
			while (it.hasNext()) {
				RoleBaseBinding rbb = it.next();
				BoundClass baseSuper = rbb.getBaseClass().getSuper();
				if (baseSuper!=null)
					return baseSuper.isInterface ? BoundSuperKind.INTERFACE : BoundSuperKind.CLASS;
			}
		}
		// IMPLICIT_INHERITANCE
		//if (isUnboundSubBase(baseClassName))
		//	return true;
		return BoundSuperKind.NONE;
	}
	
	/**
	 * Returns the name of the topmost bound base class.
	 * 
	 * @param baseClassName
	 * @return
	 */
	public static String getTopmostBoundBaseClass(String baseClassName) {
		LinkedList<RoleBaseBinding> rbbList = baseBindings.get(baseClassName);
		if (rbbList != null) {
			// assumption: EVERY BoundClass object has been connected to its bound super classes.
			RoleBaseBinding anyRBB = rbbList.get(0);
			BoundClass bc = anyRBB.getBaseClass();
			while (bc.getSuper() != null) {
				if (bc.getSuper().isInterface)
					break; // use bc instead of travelling up to super interfaces
	            bc = bc.getSuper();
	        }
			return bc.getName();
		}
		return baseClassName;
	}

	/**
	 * @param teamClassName
	 * @param baseClassName
	 * @return
	 */
	public static boolean teamAdaptsSuperBaseClass(String teamClassName, String baseClassName) {
		LinkedList<RoleBaseBinding> rbbList = baseBindings.get(baseClassName);
		if (rbbList != null && !rbbList.isEmpty()) {
			RoleBaseBinding rbb = rbbList.getFirst(); // all entries refer to the same base class.
			BoundClass bc = rbb.getBaseClass().getSuper();
			while (bc!=null && !bc.isInterface) {
				if (bc.isAdaptedByTeam(teamClassName))
					return true;
				bc = bc.getSuper();
			}
		}
		return false;
	}
	
	/**
	 * @param baseClassName
	 * @return
	 */
//	private static boolean isUnboundSubBase(String baseClassName) {
//		ObjectType current = new ObjectType(baseClassName);
//		if (!checkLookup(current.getClassName())) // TODO: workaround for classes loaded from special locations
//			return false; 
//		Iterator /* String */ it = baseBindings.keySet().iterator();
//		while (it.hasNext()) {
//			String have = (String)it.next();
//			// look for true superClass (not same):
//			if (have.equals(baseClassName)) continue;
//			ObjectType haveType = new ObjectType(have);
//			//System.err.println(current + " : "+haveType);
//			//System.err.println(org.apache.bcel.Repository.lookupClass(have));
//			if (!checkLookup(haveType.getClassName())) // TODO: workaround for classes loaded from special locations
//					continue;
//			if (current.subclassOf(haveType)) {
//				return true;
//			}
//		}
//		return false;
//	}

	/**
	 * @param className
	 * @return
	 */
	public static boolean isBoundRoleClass(String className) {
		return roleBindings.containsKey(className);
	}

	/**
	 * @param className
	 * @return
	 */
	public static RoleBaseBinding getRoleBaseBinding(String roleClassName) {
		return roleBindings.get(roleClassName);
	}
	
	/**
	 * @param roleClassName
	 * @return
	 */
	public static List<MethodBinding> getMethodBindingsForRoleClass(String roleClassName)
	{
		RoleBaseBinding rbb = roleBindings.get(roleClassName);
		return rbb.getRoleMethodBindings();
	}
	
	/**
	 * @param roleClassName
	 * @return
	 */
	public static Set<String> getBoundRoleMethods(String roleClassName) {
		Set<String> roleMethodKeySet = new HashSet<String>();
		if (roleBindings.get(roleClassName) != null) {
			RoleBaseBinding rbb = roleBindings.get(roleClassName);
			roleMethodKeySet = rbb.getRoleMethodSignatures();
		}
		return roleMethodKeySet;
	}
	
	/**
	 * @param baseClassName
	 * @return
	 */
	public static List<MethodBinding> getMethodBindingsForBaseClass(String baseClassName) 
	{
		LinkedList<RoleBaseBinding> rbbList = baseBindings.get(baseClassName);
		LinkedList<MethodBinding> resultList = new LinkedList<MethodBinding>();
		if (rbbList != null) {
			Iterator<RoleBaseBinding> it = rbbList.iterator();
			while (it.hasNext()) {
				RoleBaseBinding rbb = it.next();
				resultList.addAll(rbb.getRoleMethodBindings());
			}
		 }
		 return resultList;
	}
	
    // --------------------------------------------------
    //     discriminate base methods by tag per callin
    //  --------------------------------------------------
    // baseClass.baseMeth.baseMethodSignature -> tag
    private static HashMap<String, Integer> baseCallTags = new HashMap<String, Integer>();
    // -- follow access methods for baseCallTags:
	
    /**
     * @param baseClass
     * @param baseMeth
     * @param baseMethodSignature
     */
    public static void assignBaseCallTag (String baseClass, String baseMeth, 
    														   String baseMethodSignature) 
    {
		// every base method bound by a replace callin gets an unique tag:	
		String key = baseClass+"."+baseMeth+"."+baseMethodSignature;
		int tag = baseCallTags.size();
		if (!baseCallTags.containsKey(key))
			baseCallTags.put(key, Integer.valueOf(tag));
	}

    public static int getBaseCallTag (String base_class_name, 
    								  String base_method_name, 
    								  String base_method_signature) {
        String key = base_class_name + "." + base_method_name
		+ "." + base_method_signature;
		return baseCallTags.get(key).intValue();
    }

    // ----------------------------------------------
    //      Map argument positions:
    // ----------------------------------------------
    // teamName -> callinWrapperName -> int[]
	private static HashMap<String, HashMap<String, int[]>> paramMappings = new HashMap<String, HashMap<String, int[]>>();
    // -- follow access methods for paramMappings:

	/**
	 * @param teamName
	 * @param methodWrapper
	 * @param positions
	 */
	public static void addParameterBinding (String teamName,
											String methodWrapper,
											int[] positions) {
		HashMap<String, int[]> teamMap = paramMappings.get(teamName);
		if (teamMap == null) {
			teamMap = new HashMap<String, int[]>();
			paramMappings.put(teamName, teamMap);
		}
		teamMap.put(methodWrapper, positions);
	}

	public static boolean hasParamMappings (String teamName, String methodWrapper)
	{
		HashMap<String,int[]> teamMap = paramMappings.get(teamName);
		if (teamMap == null) return false;
		return teamMap.containsKey(methodWrapper);
	}

	/** Get parameter positions for the given team or any super team. */
	public static int[] getParamPositions (String teamName, String methodWrapper)
	{
		int[] positions = getExactParamPositions(teamName, methodWrapper);
		if (positions != null)
			return positions;
		// inherit parameter mappings from super teams: -- >> 
		Iterator<String> superTeams = getSuperTeamsWithParamMappigs(teamName).iterator();
		while (superTeams.hasNext()) {
			positions = getExactParamPositions(superTeams.next(), methodWrapper);
			if (positions != null) // found (the!) parameter mapping
				return positions;
		}
		return null;
	}
	/** Get parameter positions for exactly the given team (no super teams). */
	private static int[] getExactParamPositions(String teamName, String methodWrapper) {
		HashMap<String,int[]>teamMap = paramMappings.get(teamName);
		if (teamMap == null)
			return null;
		return teamMap.get(methodWrapper);
	}
	
	private static List<String> getSuperTeamsWithParamMappigs(String teamName) {
		LinkedList<String> result = new LinkedList<String>();
		try {
			for (JavaClass superTeam : RepositoryAccess.getSuperClasses(teamName))
				if (paramMappings.get(superTeam.getClassName()) != null)
					result.add(superTeam.getClassName());
		} catch (ClassNotFoundException e) {
			// FIXME(SH): where to log to?
			e.printStackTrace();
			// continue, just nothing added to result
		}
		return result;
	}

	// -----------------------------------------------
	//   callout bindings that require adjustment:
	// -----------------------------------------------
	// baseClass -> HashSet (method_name+signature)
	private static HashMap<String, HashSet<String>> calloutBindings = new HashMap<String, HashSet<String>>();

	/**
	 * @param clazz
	 * @param meth
	 * @param sign
	 */
	public static void addCalloutBinding(String clazz, String meth, String sign) {
		HashSet<String> bindings = calloutBindings.get(clazz);
		if (bindings == null) {
			bindings = new HashSet<String>();
			calloutBindings.put(clazz, bindings);
		}
		bindings.add(meth + sign);
	}

	/**
	 * @param clazz
	 * @return
	 */
	public static HashSet<String> getCalloutBindings (String clazz) {
		return calloutBindings.get(clazz);
	}
	
	/**
	 * @param bindings
	 * @param method_name
	 * @param signature
	 * @return
	 */
	public static boolean requiresCalloutAdjustment(HashSet<String> bindings,
											 		String method_name,
											 		String signature)
	{
		return bindings.contains(method_name+signature);
	}
	
	//	 -----------------------------------------------
	//   callouts to base class fields are stored here for furher reading and get/set method generation:
	// -----------------------------------------------
	private static ListValueHashMap<FieldDescriptor> calloutSetFields = new ListValueHashMap<FieldDescriptor>();
	private static ListValueHashMap<FieldDescriptor> calloutGetFields = new ListValueHashMap<FieldDescriptor>();

 
	/**
	 * @param roleClassName
	 * @param fieldName
	 * @param fieldSignature
	 * @param accessMode
	 * @param isStaticField
	 */
	public static void addCalloutBoundFileds(String baseClassName, String fieldName, 
			String fieldSignature, String accessMode, boolean isStaticField) 
	{
		FieldDescriptor fd = new FieldDescriptor(fieldName, fieldSignature, isStaticField);

		if (accessMode.equals("get")) {
			calloutGetFields.put(baseClassName, fd);
		} else if (accessMode.equals("set")) {
			calloutSetFields.put(baseClassName, fd);
		} else {
			throw new OTREInternalError("CalloutFieldAccess attribute contains wrong access mode: "+accessMode);
		}
	}
	
	/**
	 * @param baseClassName
	 * @return
	 */
	public static List<FieldDescriptor> getCalloutGetFields(String baseClassName) {
		return calloutGetFields.get(baseClassName);
	}
	
	/**
	 * @param baseClassName
	 * @return
	 */
	public static List<FieldDescriptor> getCalloutSetFields(String baseClassName) {
		return calloutSetFields.get(baseClassName);
	}

	// -----------------------------------------------
	// base calls to super methods via special accessor
	// -----------------------------------------------
	private static ListValueHashMap<SuperMethodDescriptor> superMethods = new ListValueHashMap<SuperMethodDescriptor>();

	public static void addSuperAccess(String baseClassName, String superClassName, String methodName, String signature) {
		SuperMethodDescriptor superMethod = new SuperMethodDescriptor(methodName, baseClassName, superClassName, signature);
		superMethods.put(baseClassName, superMethod);
	}
	public static List<SuperMethodDescriptor> getSuperAccesses(String class_name) {
		return superMethods.get(class_name);
	}

	//	 -----------------------------------------------
	//   bound interfaces have to be registered, for implementing classes to inherit callin bindings:
	// -----------------------------------------------
	private static List<String> boundBaseInterfaces = new LinkedList<String>();

	/**
	 * @param baseInterfaceName	The name of the bound base interface.
	 */
	public static void addBoundBaseInterface(String baseInterfaceName) {
		if (!boundBaseInterfaces.contains(baseInterfaceName))
			boundBaseInterfaces.add(baseInterfaceName);
	}

	/**
	 * @param interfaceName
	 * @return
	 */
	public static Collection<String[]> getInterfaceInheritedCallinBindings(String interfaceName) {
		List<String[]> result = new LinkedList<String[]>();
		//System.err.println(interfaceName);
		//System.err.println(boundBaseInterfaces);
		if (boundBaseInterfaces.contains(interfaceName)) {
            if (logging)
                ObjectTeamsTransformation.printLogMessage(interfaceName
                        + " bequests callin bindings to implementing class");
              // collect the signatures of all bound base methods:
 				LinkedList<RoleBaseBinding> rbbList = baseBindings.get(interfaceName);
				if (rbbList != null) {
					Iterator<RoleBaseBinding> rbb_it = rbbList.iterator();
					while (rbb_it.hasNext()) {
						RoleBaseBinding rbb = rbb_it.next();
						result.addAll(rbb.getBaseSignatures());

						// System.err.println("inheriting binding: "+rbb);
				}
			}
		}
		return result;
	}

	/**
	 * @param methodName
	 * @param methodSignature
	 * @param interfaceName
	 * @return
	 */
	public static Collection<MethodBinding> getInterfaceInheritedMethodBindings(
			String methodName, String methodSignature, String interfaceName) {
		List<MethodBinding> result = new LinkedList<MethodBinding>();
		if (boundBaseInterfaces.contains(interfaceName)) {
			LinkedList<RoleBaseBinding> rbbList = baseBindings.get(interfaceName);
			if (rbbList != null) {
				Iterator<RoleBaseBinding> rbb_it = rbbList.iterator();
				while (rbb_it.hasNext()) {
					RoleBaseBinding rbb = rbb_it.next();
					List<MethodBinding> mbs = rbb.getBaseMethodBindings(methodName, methodSignature);
					if (mbs != null)
						result.addAll(mbs);
				}
			}
		}
		return result;
	}
	
	/**
	 * @param interfaceNames
	 * @return
	 */
	public static boolean containsBoundBaseInterface(String[] interfaceNames) {
		for (int i = 0; i < interfaceNames.length; i++) {
			if (boundBaseInterfaces.contains(interfaceNames[i]))
				return true;
		}
		return false;
	}
	
	//	 -----------------------------------------------
	//   precedence can be set for a list of method bindings:
	// -----------------------------------------------
	
	private static ListValueHashMap<List<String>> precedencePerTeam = new ListValueHashMap<List<String>>();
	
	/**
	 * Add a precedence list for the given team.
	 * @param precedenceList	the list of binding labels defining their precedence
	 * @param teamName 		the name of the team for which the precedence list was defined
	 */
	public static void addPrecedenceList(List<String> precedenceList, String teamName) {
		precedencePerTeam.put(teamName, precedenceList);
	}
	
	/**
	 * Sorts the method binding list according to the given precedence for the team
	 * @param mbList			the list of 'MethodBinding's to be sorted
	 * @param teamName	the name of the corresponding team
	 * @return	the sorted 'MethodBindig' list
	 */
	public static List<MethodBinding> sortMethodBindings(List<MethodBinding> mbList, String teamName) {
		if (mbList.size() < 2)
			return mbList; // nothing to sort
		String outermostTeamName;
		int dollarIdx = teamName.indexOf('$');
		if (dollarIdx > 0)
			outermostTeamName = teamName.substring(0, dollarIdx);
		else outermostTeamName = teamName;
		
		List<List<String>> precedenceList = precedencePerTeam.get(outermostTeamName);
		if (precedenceList==null) {
			// mbList has to be reduced by removing overridden bindings:
			return removeOverridden(mbList);
		}
		
		LinkedList<MethodBinding> sortedMethodBindings = new LinkedList<MethodBinding>();
		Iterator<List<String>> predIt = precedenceList.iterator();
		
		while (predIt.hasNext()) {
			List<String> plainList = predIt.next();
			Iterator<String> plainIt = plainList.iterator();
			
			while (plainIt.hasNext()) {
				boolean foundOne = false;
				String label = plainIt.next();
				Iterator<MethodBinding> mbIter = mbList.iterator();
				List<MethodBinding> alreadySorted = new LinkedList<MethodBinding>();
				
				while (mbIter.hasNext()) {
					MethodBinding mb = mbIter.next();
					
					if (mb.getQualifiedBindingLabel().equals(label)) { // mb exactly fits binding label:
						alreadySorted.add(mb);
						if (!foundOne) {
							sortedMethodBindings.add(mb);
							foundOne = true;
						} else checkInheritance(sortedMethodBindings.getLast(), mb); 
					
					} else if (mb.inheritsBindingLabel(label, teamName)) { // mb inherits  binding label:
						alreadySorted.add(mb);
						if (!foundOne) {
							sortedMethodBindings.add(mb);
							foundOne = true;
						} else {// maybe it is a subtype of the already added?
							MethodBinding lastAdded = sortedMethodBindings.getLast();
							if (mb.overridesMethodBinding(lastAdded)) {
								sortedMethodBindings.set(sortedMethodBindings.size()-1, mb);
								foundOne = true;
							} else checkInheritance(sortedMethodBindings.getLast(), mb);
						}
					}
				}
				if (foundOne) {
					Iterator<MethodBinding> sortedIter = alreadySorted.iterator();
					while (sortedIter.hasNext()) {
						MethodBinding mb = sortedIter.next();
						mbList.remove(mb);
					}
				}
			}
		}
//		if (mbList.size() > 0) {
//			System.err.println("ERROR: Unsortable method bindings: " + mbList +"!");
//			 // assumption: all remaining method bindings are overridden! TODO: check assumption!
//		}
		return sortedMethodBindings;
	}

	/**
	 * Removes overridden method bindings from the given list. Assumes that 
	 * all bindings in the list are in a sub/super type relationship.
	 * @param mbList		the method binding list
	 * @return		the most specific method binding overriding all others
	 */
	private static List<MethodBinding> removeOverridden(List<MethodBinding> mbList) {
		MethodBinding mostSpecificMB = mbList.get(0);
		Iterator<MethodBinding> mbIter = mbList.iterator();
		while (mbIter.hasNext()) {
			MethodBinding mb = mbIter.next();
			if (mb.overridesMethodBinding(mostSpecificMB)) {
				mostSpecificMB = mb;
			} else checkInheritance(mostSpecificMB, mb);
		}
		List<MethodBinding> resultList = new LinkedList<MethodBinding>();
		resultList.add(mostSpecificMB);
		return resultList;
	}
		
	/**
	 * Checks if 'subMB' 'inherits' from 'superMB'. Used to check if assumptions 
	 * about the precedence lists are fulfilled.
	 *
	 * @param subMB		the overriding method binding
	 * @param superMB	the overridden method binding
	 */
	private static void checkInheritance(MethodBinding subMB, MethodBinding superMB) {
		if (! (subMB.equals(superMB) || subMB.overridesMethodBinding(superMB))) {
			//System.err.println("sub: " + subMB + "\n super: " + superMB);
			throw new OTREInternalError("Wrong assumption! Broken precedence list possible.");
		}
	}
    
    // ------------------------------------------
    // ---------- Logging: ----------------------
    // ------------------------------------------
    /** Initialized from property <tt>ot.log</tt>. */
    static boolean logging = false;
    /** Initialized from property <tt>otequinox.debug</tt> */
    static boolean OTEQUINOX_WARN = false;
    static {
        if(System.getProperty("ot.log") != null)
            logging = true;
        String warnlevel = System.getProperty("otequinox.debug");
        if (warnlevel != null) {
        	warnlevel = warnlevel.toUpperCase();
        	if (warnlevel.startsWith("INFO") || warnlevel.equals("OK"))
        		OTEQUINOX_WARN = true;
        }
    }
    
	//	 -----------------------------------------------
	//   static replace callin bindings have to be stored in the team, special treatment:
	// -----------------------------------------------
    	
	// maps an implemented role method to its base methods: roleMethod -> List[baseMethod] // added by JU
	private static ListValueHashMap<BaseMethodInfo> staticReplaceBindings = new ListValueHashMap<BaseMethodInfo>();
	
	/**
	 * Adds a base method to its implemented role method (static replace bindings).
	 * 
	 * @param roleMethodKey a string structured according team_class_ name.role_class_name.role_method_name.role_method_signature
	 * @param baseMethodInfo an Object contained base class name, base method name and base method signature
	 */
	public static void addStaticReplaceBindingForRoleMethod(String roleMethodKey, BaseMethodInfo baseMethodInfo) {
		staticReplaceBindings.put(roleMethodKey, baseMethodInfo);
	}
	
	/**
	 * Returns static method bindings for the given role method
	 * @param roleMethodKey
	 * @return
	 */
	public static LinkedList<BaseMethodInfo> getStaticReplaceBindingsForRoleMethod(String roleMethodKey) {
		return staticReplaceBindings.get(roleMethodKey);
	}

	/**
	 * @param roleClassName
	 * @param roleMethodName
	 * @param roleMethodSignature
	 * @return
	 */
	public static boolean roleMethodHasBinding(String roleClassName, String roleMethodName, String roleMethodSignature) {
		RoleBaseBinding rbb = roleBindings.get(roleClassName);
		if (rbb == null) {
			return false;
		}
		return rbb.hasRoleMethodBinding(roleMethodName, roleMethodSignature);
	}
	
	// -----------------------------------------------
	//  access modifiers of some base classes have to be changed to 'public' (decapsulation)
	//  the names of these classes are stored in baseClassesForModifierChange 
	// -----------------------------------------------
	private static LinkedList<String> baseClassesForModifierChange = new LinkedList<String>();
	
	/**
	 * Adds the given class name to the list of class names intended for decapsulation 
	 * @param className
	 */
	public static void addBaseClassForModifierChange(String className){
		baseClassesForModifierChange.add(className);
	}
	
	/**
	 * Checks whether a class name is contained in the list of classes intended for decapsulation
	 * @param className
	 * @return
	 */
	public static boolean checkBaseClassModifierChange(String className) {
		Iterator<String> iter = baseClassesForModifierChange.iterator();
		while(iter.hasNext()) {
			if(className.equals(iter.next())){
				return true;
			}
		}
		return false;
	}

	public static void addBoundSuperclassLink(String sub_name, String super_name) {
		synchronized (baseBindings) {
			bases:
			for (RoleBaseBinding subBinding : baseBindings.getFlattenValues()) {
				if (subBinding.getBaseClassName().equals(sub_name)) {
					for (RoleBaseBinding superBinding : baseBindings.getFlattenValues()) {
						if (subBinding == superBinding) continue;
						if (superBinding.getBaseClassName().equals(super_name)) {
							subBinding.getBaseClass().setSuper(superBinding.getBaseClass());
							break bases;
						}
					}
				}
			}
		}
	}

	/** Either retrieve an existing BoundClass for `className' are create a new one. 
     *  If an existing one is used it is updated for the new adapting team.
     */
	public static BoundClass getBoundBaseClass(String className, String teamClassName) {
		for (RoleBaseBinding subBinding : baseBindings.getFlattenValues()) {
			if (subBinding.getBaseClassName().equals(className)) { 
				BoundClass baseClass = subBinding.getBaseClass();
				baseClass.addAdaptingTeam(teamClassName);
				return baseClass;
			}
		}
		return new BoundClass(className, teamClassName);
	}

	// ==== HELPERS FOR RE-ENTRANCE SAFETY: ====

	private static Iterator<String> getBaseBindingsKeyIterator() {
		synchronized (baseBindings) {
			ArrayList<String> list = new ArrayList<String>();
	    	list.addAll(baseBindings.keySet());
	    	Iterator<String> it = list.iterator();
			return it;
		}
	}

	private static Iterator<Entry<String, LinkedList<RoleBaseBinding>>> getBaseBindingsCloneIterator() {
		synchronized (baseBindings) {
			ArrayList<Entry<String, LinkedList<RoleBaseBinding>>> list = new ArrayList<Entry<String,LinkedList<RoleBaseBinding>>>();
			list.addAll(baseBindings.entrySet());
			Iterator<Entry<String, LinkedList<RoleBaseBinding>>> it = list.iterator();
			return it;
		}
	}
}

/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2003, 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Decapsulation.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.objectteams.otre.util.CallinBindingManager;
import org.eclipse.objectteams.otre.util.FieldDescriptor;
import org.eclipse.objectteams.otre.util.SuperMethodDescriptor;

import de.fub.bytecode.Constants;
import de.fub.bytecode.classfile.Method;
import de.fub.bytecode.generic.ClassGen;
import de.fub.bytecode.generic.ConstantPoolGen;
import de.fub.bytecode.generic.InstructionFactory;
import de.fub.bytecode.generic.InstructionList;
import de.fub.bytecode.generic.MethodGen;
import de.fub.bytecode.generic.ObjectType;
import de.fub.bytecode.generic.Type;

/**
 *  For each base method that is bound by callout and has
 *  insufficient visibility, the visibility is set to public.
 *  If the corresponding JMangler-patch is installed, check
 *  whether the affected base class resides in a sealed package.
 *  In that case dissallow decapsulation by throwing an IllegalAccessError.
 *  
 *  @version $Id: Decapsulation.java 23408 2010-02-03 18:07:35Z stephan $
 *  @author Stephan Herrmann
 */
public class Decapsulation 
	extends ObjectTeamsTransformation
	implements Constants 
{
	
//	HashSet modifiedPackages = new HashSet();

	public static class SharedState extends ObjectTeamsTransformation.SharedState {
		private HashMap /* class_name -> HashSet(callout accessed fields) */<String, HashSet<String>> generatedFieldCalloutAccessors
			= new HashMap<String, HashSet<String>>();
		private HashMap /* class_name -> HashSet(super-accessed methods (sign))*/<String, HashSet<String>> generatedSuperAccessors
    		= new HashMap<String, HashSet<String>>();
	}
	@Override
	SharedState state() {
		return (SharedState)this.state;
	}
   
    public Decapsulation(SharedState state) {
    	this(null, state);
    }
    public Decapsulation(ClassLoader loader, SharedState state) {
    	super(loader, state);
    	// FIXME(SH): can we ever release this transformer and its state?
    	synchronized(ObjectTeamsTransformation.reentrentTransformations) {
    		ObjectTeamsTransformation.reentrentTransformations.add(this);
    	}
    }

	/**
	 * Main entry for this transformer.
	 */
//	@SuppressWarnings("unchecked")
	public void doTransformInterface(ClassEnhancer ce, ClassGen cg) {		
		String          class_name = cg.getClassName();
		ConstantPoolGen cpg        = cg.getConstantPool();
		
		// if class is already transformed by this transformer
		if (state.interfaceTransformedClasses.contains(class_name))
			return;

		checkReadClassAttributes(ce, cg, class_name, cpg);
            
        // next step starts to transform, so record this class now.
		state.interfaceTransformedClasses.add(class_name);

		generateFieldAccessForCallout(ce, cg, class_name, cpg);
		
		generateSuperAccessors(ce, cg, class_name, cpg);

		HashSet<String> calloutBindings = CallinBindingManager.getCalloutBindings(class_name);

		if (calloutBindings == null) {
            if(logging) printLogMessage("\nClass " + class_name 
					+ " requires no callout adjustment.");
			return; 
		}
			
        if(logging) printLogMessage("\nCallout bindings might be changing class " 
					+ class_name + ":");

		HashSet<String> oldStyleBinding = new HashSet<String>();
		
		// try new style decapsulation first (since 1.2.8):
		for (String calloutBinding : calloutBindings) {
			DecapsulationDescriptor desc = new DecapsulationDescriptor();
			if (!desc.decode(calloutBinding, cg))
				oldStyleBinding.add(calloutBinding); // old style attribute
			else if (!desc.existsAlready) 
				ce.addMethod(desc.generate(class_name, cpg), cg);
		}
		
		if (oldStyleBinding.isEmpty()) return;
		
		// --> follows: old style decapsulation for remaining bindings:
		int pos = class_name.lastIndexOf('.');
		String package_name = "NO_PACKAGE";
		if (pos != -1)
			package_name = class_name.substring(0,pos);
			
		Method[] methods = cg.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m           = methods[i];
			String method_name = m.getName();
			
			boolean requiresAdjustment = CallinBindingManager.
			requiresCalloutAdjustment(oldStyleBinding, 
										method_name,
										m.getSignature());
				
			if (requiresAdjustment) {
				ce.decapsulateMethod(m, cg, package_name, cpg);
			}
		}
	}
	class DecapsulationDescriptor {
		short invokeKind;
		String targetClass;
		String methodName;
		String methodSign;
		Type returnType; 
		Type[] args;
		String accessorName;
		
		boolean existsAlready;
		
		/**
		 * new style encoding is
		 * targetClassName ('!' | '?') methodName '.' methodSign 
		 */
		boolean decode(String encodedBinding, ClassGen cg) {
			int sepPos = encodedBinding.indexOf('!');
			if (sepPos != -1) { // static method:
				invokeKind = INVOKESTATIC;
			} else {
				sepPos = encodedBinding.indexOf('?');
				if (sepPos != -1) {
					invokeKind = INVOKEVIRTUAL;
				} else {
					return false; // old style
				}
			}
			targetClass = encodedBinding.substring(0, sepPos);
			int sigPos = encodedBinding.indexOf('(', sepPos);
			methodName = encodedBinding.substring(sepPos+1, sigPos);
			methodSign = encodedBinding.substring(sigPos);
			
			returnType = Type.getReturnType(methodSign); 
			args = Type.getArgumentTypes(methodSign);

			accessorName = "_OT$decaps$"+methodName;
			existsAlready = cg.containsMethod(accessorName, methodSign) != null;
			if (invokeKind == INVOKEVIRTUAL) {
				Method existing = cg.containsMethod(methodName, methodSign);
				if (existing != null && existing.isPrivate())
					invokeKind = INVOKESPECIAL; // accessing private
			}
			
			return true;
		}
		Method generate(String currentClass, ConstantPoolGen cpg) {
			InstructionList il = new InstructionList();
			int instanceOffset = 0;
			short flags = Constants.ACC_PUBLIC;
			if (invokeKind != INVOKESTATIC) {
				instanceOffset=1;
				il.append(InstructionFactory.createThis());
			} else {
				flags |= Constants.ACC_STATIC;
			}
			int pos= 0;
			for (int i = 0; i < args.length; i++) {
				il.append(InstructionFactory.createLoad(args[i], pos+instanceOffset));
				pos += args[i].getSize();
			}
			il.append(new InstructionFactory(cpg).createInvoke(targetClass, methodName, returnType, args, invokeKind));
			il.append(InstructionFactory.createReturn(returnType));
			MethodGen newMethod = new MethodGen(flags, returnType, args, /*argNames*/null, accessorName, currentClass, il, cpg);
			newMethod.setMaxLocals();
			newMethod.setMaxStack();
			return newMethod.getMethod();
		}
	}

	/**
	 * Generates getter and setter methods for all fields of the class 'class_name' which are accessed via callout. 
	 * Informations are received via attributs (CallinBindingManager). 
	 * @param cg					the ClassGen of the appropriate class
	 * @param class_name		the name of the class
	 * @param cpg					the ConstantPoolGen of the class
	 * @param es					the ExtensionSet to add the new access methods
	 */
	private void generateFieldAccessForCallout(ClassEnhancer ce, ClassGen cg, String class_name, ConstantPoolGen cpg) {
		InstructionFactory factory = null;
		
		HashSet<String> addedAccessMethods = state().generatedFieldCalloutAccessors.get(class_name);

		List<FieldDescriptor> getter = CallinBindingManager.getCalloutGetFields(class_name);
		if (getter != null) {
	    	factory = new InstructionFactory(cg);
			Iterator<FieldDescriptor> it = getter.iterator();
			while (it.hasNext()) {
				FieldDescriptor fd = it.next();
				String key = "get_" + fd.getFieldName() + fd.getFieldSignature();
				if (logging)
					printLogMessage("Generating getter method "+key);
				if (addedAccessMethods == null)
					addedAccessMethods = new HashSet<String>();
				if (addedAccessMethods.contains(key))
					continue; // this getter has already been created
				ce.addMethod(generateGetter(cpg, class_name, fd, factory), cg);
				addedAccessMethods.add(key);
				state().generatedFieldCalloutAccessors.put(class_name, addedAccessMethods);
			}
		}

		List<FieldDescriptor> setter = CallinBindingManager.getCalloutSetFields(class_name);
		if (setter != null) {
			if (factory == null)
		    	factory = new InstructionFactory(cg);
			Iterator<FieldDescriptor> it = setter.iterator();
			while (it.hasNext()) {
				FieldDescriptor fd = it.next();
				String key = "set_"+fd.getFieldName()+fd.getFieldSignature();
				if (logging)
					printLogMessage("Generating setter method "+key);
				if (addedAccessMethods == null)
					addedAccessMethods = new HashSet<String>();
				if (addedAccessMethods.contains(key))
					continue; // this setter has already been created
				ce.addMethod(generateSetter(cpg, class_name, fd, factory), cg);
				addedAccessMethods.add(key);
				state().generatedFieldCalloutAccessors.put(class_name, addedAccessMethods);
			}
		}
	}


	/**
	 * Generates a getter method for the field described by 'fd' in the class 'class_name'. 
	 * @param cpg					the ConstantPoolGen of the class
	 * @param class_name		the name of the class
	 * @param fd						the FieldDescriptor describing the affected field
	 * @param factory				an InstructionFactory for this class
	 * @return							the generated getter method
	 */
	private Method generateGetter(ConstantPoolGen cpg, String class_name, FieldDescriptor fd, InstructionFactory factory) {
		String fieldName = fd.getFieldName();
		Type fieldType =  Type.getType(fd.getFieldSignature());
		Type baseType = new ObjectType(class_name);
		
		InstructionList il = new InstructionList();
		String[] argumentNames;
		Type[]   argumentTypes;
		if (fd.isStaticField()) {
			argumentNames = new String[0];
			argumentTypes = new Type[0];
		} else {
			argumentNames = new String[] {"base_obj"};
			argumentTypes = new Type[]   {baseType};
		}
		MethodGen mg = new MethodGen((Constants.ACC_PUBLIC|Constants.ACC_STATIC),
	 			  												  fieldType,
																  argumentTypes,
																  argumentNames,
																  OT_PREFIX+"get$"+fieldName,
																  class_name,
																  il, cpg);
		if (!fd.isStaticField())
			il.append(InstructionFactory.createLoad(baseType, 0)); // first argument is at slot 0 in static methods
		short fieldKind = fd.isStaticField()?Constants.GETSTATIC:Constants.GETFIELD;
		il.append(factory.createFieldAccess(class_name, fieldName, fieldType, fieldKind));
		il.append(InstructionFactory.createReturn(fieldType));

		mg.removeNOPs();
		mg.setMaxStack();
		mg.setMaxLocals();
		return mg.getMethod();
	}
	

	/**
	 * Generates a setter method for the field described by 'fd' in the class 'class_name'. 
	 * @param cpg					the ConstantPoolGen of the class
	 * @param class_name		the name of the class
	 * @param fd						the FieldDescriptor describing the affected field
	 * @param factory				an InstructionFactory for this class
	 * @return							the generated getter method
	 */
	private Method generateSetter(ConstantPoolGen cpg, String class_name, FieldDescriptor fd, InstructionFactory factory ) {
		String fieldName = fd.getFieldName();
		Type fieldType =  Type.getType(fd.getFieldSignature());
		Type baseType = new ObjectType(class_name);

		Type[]   argumentTypes;
		String[] argumentNames;
		if (fd.isStaticField()) {
			argumentTypes = new Type[]   { fieldType};
			argumentNames = new String[] {"new_value"};			
		} else {
			argumentTypes = new Type[]   {baseType,   fieldType};
			argumentNames = new String[] {"base_obj", "new_value"};
		}
		
		InstructionList il = new InstructionList();
		MethodGen mg = new MethodGen((Constants.ACC_PUBLIC|Constants.ACC_STATIC),
	 			  												  Type.VOID,
																  argumentTypes,
																  argumentNames,
																  OT_PREFIX+"set$"+fieldName,
																  class_name,
																  il, cpg);

		int argumentPosition; // position for the argument holding the new field value.
		if (!fd.isStaticField()) {
			il.append(InstructionFactory.createLoad(baseType, 0)); // first argument is at slot 0 in static methods
			argumentPosition = 1;
		} else {
			argumentPosition = 0;
		}
		il.append(InstructionFactory.createLoad(fieldType, argumentPosition));
		short fieldKind = fd.isStaticField()?Constants.PUTSTATIC:Constants.PUTFIELD;
		il.append(factory.createFieldAccess(class_name, fieldName, fieldType, fieldKind));
		il.append(InstructionFactory.createReturn(Type.VOID));

		mg.removeNOPs();
		mg.setMaxStack();
		mg.setMaxLocals();
		return mg.getMethod();
	}
	
	private void generateSuperAccessors(ClassEnhancer ce, ClassGen cg, String class_name, ConstantPoolGen cpg) {
		InstructionFactory factory = null;
		
		HashSet<String> addedAccessMethods = state().generatedSuperAccessors.get(class_name);

		List<SuperMethodDescriptor> methods = CallinBindingManager.getSuperAccesses(class_name);
		if (methods != null) {
	    	factory = new InstructionFactory(cg);
	    	for (SuperMethodDescriptor superMethod : methods) {
				String key = superMethod.methodName+'.'+superMethod.signature;
				if (logging)
					printLogMessage("Generating super access method "+key);
				if (addedAccessMethods == null)
					addedAccessMethods = new HashSet<String>();
				if (addedAccessMethods.contains(key))
					continue; // this accessor has already been created
				ce.addMethod(generateSuperAccessor(cpg, class_name, superMethod, factory), cg);
				addedAccessMethods.add(key);
				state().generatedSuperAccessors.put(class_name, addedAccessMethods);
			}
		}
	}
	
	private Method generateSuperAccessor(ConstantPoolGen cpg, String className, SuperMethodDescriptor superMethod, InstructionFactory factory) 
	{ 
		int endPos = superMethod.signature.indexOf(')');
		String segment = superMethod.signature.substring(1, endPos);
		String[] typeNames = (segment.length() > 0) ? segment.split(",") : new String[0];
		Type[] argTypes = new Type[typeNames.length];
		for (int i = 0; i < argTypes.length; i++) 
			argTypes[i] = Type.getType(typeNames[i]);
		
		int index = superMethod.signature.lastIndexOf(')') + 1;
		Type returnType = Type.getType(superMethod.signature.substring(index));
		
		Type baseType = new ObjectType(className);
		Type[] wrapperTypes = new Type[argTypes.length+1];
		System.arraycopy(argTypes, 0, wrapperTypes, 1, argTypes.length);
		wrapperTypes[0] = baseType;
		String[] argNames = new String[wrapperTypes.length];
		for (int i = 0; i < argNames.length; i++) {
			argNames[i] = "arg"+i;
		}
		InstructionList il = new InstructionList();
		MethodGen mg = new MethodGen((Constants.ACC_PUBLIC|Constants.ACC_STATIC),
									 returnType,
									 wrapperTypes,
									 argNames,
									 OT_PREFIX+superMethod.methodName+"$super",
									 className,
									 il, cpg);
		il.append(InstructionFactory.createLoad(baseType, 0)); // first argument is base instance
		for (int i = 0; i < argTypes.length; i++) 
			il.append(InstructionFactory.createLoad(argTypes[i], i+1));
		
		// if super method is also callin bound directly invoke the orig-version 
		// (to avoid that BaseMethodTransformation.checkReplaceWickedSuper() has to rewrite this code again): 
		String methodName = (CallinBindingManager.isBoundBaseMethod(superMethod.superClass, superMethod.methodName, superMethod.signature))
								? genOrigMethName(superMethod.methodName)
								: superMethod.methodName;
		
		il.append(factory.createInvoke(superMethod.superClass, methodName, returnType, argTypes, INVOKESPECIAL));
		il.append(InstructionFactory.createReturn(returnType));
		mg.setMaxStack();
		mg.setMaxLocals();
		return mg.getMethod();
	}
	
}

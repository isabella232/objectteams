/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2005-2009 Berlin Institute of Technology, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ObjectTeamsTransformer.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.jplis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.objectteams.otre.BaseCallRedirection;
import org.eclipse.objectteams.otre.BaseMethodTransformation;
import org.eclipse.objectteams.otre.BaseTagInsertion;
import org.eclipse.objectteams.otre.Decapsulation;
import org.eclipse.objectteams.otre.LiftingParticipantTransformation;
import org.eclipse.objectteams.otre.LowerableTransformation;
import org.eclipse.objectteams.otre.OTConstants;
import org.eclipse.objectteams.otre.ObjectTeamsTransformation;
import org.eclipse.objectteams.otre.StaticSliceBaseTransformation;
import org.eclipse.objectteams.otre.SubBoundBaseMethodRedefinition;
import org.eclipse.objectteams.otre.TeamInterfaceImplementation;
import org.eclipse.objectteams.otre.ThreadActivation;
import org.eclipse.objectteams.otre.util.AttributeReadingGuard;
import org.eclipse.objectteams.otre.util.CallinBindingManager;

import de.fub.bytecode.Repository;
import de.fub.bytecode.classfile.ClassParser;
import de.fub.bytecode.classfile.JavaClass;
import de.fub.bytecode.generic.ClassGen;


/**
 * Main entry into the OTRE when using JPLIS
 * 
 * @author Christine Hundt
 * @author Stephan Herrmann
 */
public class ObjectTeamsTransformer implements ClassFileTransformer {
	
	// force loading all transformer classes to reduce risk of deadlock in class loading.
	static Class<?>[] transformerClasses = new Class<?>[] {	
		BaseCallRedirection.class, 
		BaseMethodTransformation.class,
		BaseTagInsertion.class,
		Decapsulation.class,
		LiftingParticipantTransformation.class,
		LowerableTransformation.class,
		StaticSliceBaseTransformation.class,
		SubBoundBaseMethodRedefinition.class,
		TeamInterfaceImplementation.class,
		ThreadActivation.class
	};
	
	/**
	 * One instance of this class is used per class loader to ensure disjoint scopes.
	 */
	static class StateGroup {
		ObjectTeamsTransformation.SharedState bcrState = new ObjectTeamsTransformation.SharedState();
		ObjectTeamsTransformation.SharedState bmtState = new ObjectTeamsTransformation.SharedState();
		BaseTagInsertion.SharedState 		  btiState = new BaseTagInsertion.SharedState();
		Decapsulation.SharedState 			  decState = new Decapsulation.SharedState();
		ObjectTeamsTransformation.SharedState lptState = new ObjectTeamsTransformation.SharedState();
		ObjectTeamsTransformation.SharedState lowState = new ObjectTeamsTransformation.SharedState();
		ObjectTeamsTransformation.SharedState ssbtState = new ObjectTeamsTransformation.SharedState();
		ObjectTeamsTransformation.SharedState sbbmrState = new ObjectTeamsTransformation.SharedState();
		ObjectTeamsTransformation.SharedState tiiState = new ObjectTeamsTransformation.SharedState();
	}
	static Map<ClassLoader, StateGroup> states = new HashMap<ClassLoader, StateGroup>();

	static boolean warmedUp = false;
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader,
	 *      java.lang.String, java.lang.Class, java.security.ProtectionDomain,
	 *      byte[])
	 */
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer)
			throws IllegalClassFormatException
	{
		if (warmedUp)
			return internalTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		synchronized (loader) {
			try {
				return internalTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
			} finally {
				warmedUp = true;
			}		
		}
	}
	public byte[] internalTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer)
			throws IllegalClassFormatException
	{
		if (className.startsWith("org/objectteams/transformer")
				|| className.startsWith("org/cs3/jmangler")
				|| className.startsWith("de/fub/bytecode"))
		{
			// skip OTRE, BCEL and JMangler classes
			return null;
		}
//		if (!(className.startsWith("java") || className.startsWith("sun")))
			// System.err.println("ObjectTeamsTransformer transforming: " + className);
		if (classBeingRedefined != null) {
			System.out.println("Redefinition!");
			return null;
		}
		
		// state sharing among transformers:
		StateGroup states = ObjectTeamsTransformer.states.get(loader);
		if (states == null)
			ObjectTeamsTransformer.states.put(loader, states = new StateGroup());
		//
		// One fresh instance of each transformer for a given class:
		//
		BaseCallRedirection baseCallRedirection 
			= new BaseCallRedirection(				loader,	states.bcrState);
		BaseMethodTransformation baseMethodTransformation 
			= new BaseMethodTransformation(			loader,	states.bmtState);
		BaseTagInsertion baseTagInsertion 
			= new BaseTagInsertion(							states.btiState);
		Decapsulation decapsulation 
			= new Decapsulation(					loader,	states.decState);
		LiftingParticipantTransformation liftingParticipantTransformation 
			= new LiftingParticipantTransformation(	loader, states.lptState);
		LowerableTransformation lowerableTransformation 
			= new LowerableTransformation(			loader, states.lowState);
		StaticSliceBaseTransformation staticSliceBaseTransformation
			= new StaticSliceBaseTransformation(	loader, states.ssbtState);
		SubBoundBaseMethodRedefinition subBoundBaseMethodRedefinition 
			= new SubBoundBaseMethodRedefinition(	loader,	states.sbbmrState);
		TeamInterfaceImplementation teamInterfaceImplementation 
			= new TeamInterfaceImplementation(true, loader, states.tiiState);
		ThreadActivation threadActivation 
			= new ThreadActivation();
				
		// tell Repository about the class loader for improved lookupClass()
		ClassLoader prevLoader= Repository.classLoaders.get();
		Repository.classLoaders.set(loader);
		
		InputStream is  = new ByteArrayInputStream(classfileBuffer);
		try {
			JavaClass java_class = new ClassParser(is, className).parse();
			//Repository.addClass(java_class);
			ClassGen cg = new ClassGen(java_class);
			
			JPLISEnhancer jpe = new JPLISEnhancer(cg, loader);
			
			Collection<String> adaptedBases; // [OT/Equinox]
			// [OT/Equinox] remember the first transformation which holds adaptedBases
			adaptedBases= setFirstTransformation(subBoundBaseMethodRedefinition);
			// [OT/Equinox] if class has previously been transformed fetch the list of
			// adapted bases from the CallinBindingManager instead of reading it now.
			if (   (cg.getAccessFlags() & OTConstants.TEAM) != 0
				&& !AttributeReadingGuard.getInstanceForLoader(loader).iAmTheFirst(cg.getClassName())) 
			{
				List<String> basesOfTeam = CallinBindingManager.getBasesPerTeam(cg.getClassName());
				if (basesOfTeam != null)
					adaptedBases.addAll(basesOfTeam);				
			}
			subBoundBaseMethodRedefinition.doTransformInterface(jpe, cg);
			baseCallRedirection.doTransformInterface(jpe, cg);
			decapsulation.doTransformInterface(jpe, cg);
		try {
			baseMethodTransformation.useReflection = (loader == null); // bootstrap classes cannot be called directly
			baseMethodTransformation.doTransformInterface(jpe, cg);
		} catch (Throwable t) {
			System.err.println("Error transforming class: "+cg.getClassName());
			t.printStackTrace();
		}
			baseTagInsertion.doTransformInterface(jpe, cg);
			lowerableTransformation.doTransformInterface(jpe, cg);
			staticSliceBaseTransformation.doTransformInterface(jpe, cg);
			teamInterfaceImplementation.doTransformInterface(jpe, cg);
			
//			subBoundBaseMethodRedefinition.doTransformInterface(jpe, cg);
//			baseCallRedirection.doTransformInterface(jpe, cg);
//			decapsulation.doTransformInterface(jpe, cg);
//			baseMethodTransformation.doTransformInterface(jpe, cg);
//			baseTagInsertion.doTransformInterface(jpe, cg);
//			staticSliceBaseTransformation.doTransformInterface(jpe, cg);
//			teamInterfaceImplementation.doTransformInterface(jpe, cg);
			threadActivation.doTransformInterface(jpe, cg);

			
//			baseCallRedirection.doTransformCode(cg); // empty method
			baseMethodTransformation.doTransformCode(cg);
			baseTagInsertion.doTransformCode(cg);
			liftingParticipantTransformation.doTransformCode(cg);
			staticSliceBaseTransformation.doTransformCode(cg);
			teamInterfaceImplementation.doTransformCode(cg);
			threadActivation.doTransformCode(cg);
			
			JavaClass new_java_class = cg.getJavaClass(); 
			if (dumping) {
				new_java_class.dump("jplis_dump/" + className + ".class");
			}
			return new_java_class.getBytes();
		} catch (IOException e) {
			System.err.println("ClassFileTransformer could not parse class file buffer to JavaClass");
			e.printStackTrace();
		} finally {
			// uninstall class loader:
			Repository.classLoaders.set(prevLoader);
		}
		return null;
	}
	
	/**
	 * External API (for OT/Equinox):
	 * Destructively fetch the set of adapted base classes 
	 * recorded since the last call to this method.
	 * 
	 * @return
	 */
	public Collection<String> fetchAdaptedBases() {
		if (this.firstTransformation == null)
			return null;
		Collection<String>result= this.firstTransformation.fetchAdaptedBases();
		this.firstTransformation= null;
		return result;
	}
	
	/**
	 * External API (for OT/Equinox):
	 * Read the OT-Attributes of a class without loading the class.
	 * @throws IOException 
	 * @throws ClassFormatError 
	 */
	public void readOTAttributes(InputStream file, String fileName, ClassLoader loader) 
			throws ClassFormatError, IOException 
	{
		ClassParser   cp  = new ClassParser(file, fileName);
		ClassGen      cg  = new ClassGen(cp.parse());
		JPLISEnhancer jpe = new JPLISEnhancer(cg, /*loader (unused)*/null);
		ClassLoader prevLoader= Repository.classLoaders.get();
		Repository.classLoaders.set(loader);
		try {
			setFirstTransformation(new ObjectTeamsTransformation(loader, null) {});
			firstTransformation.checkReadClassAttributes(jpe, cg, cg.getClassName(), cg.getConstantPool());
		} finally {
			Repository.classLoaders.set(prevLoader);
		}
	}
	
	// helper structure for above:
	/* The first transformation performed holds the list of adapted bases. */
	private ObjectTeamsTransformation firstTransformation;

	/* @return the collection of adapted bases currently in use. */
	private Collection<String> setFirstTransformation(ObjectTeamsTransformation t) {
		if (this.firstTransformation != null)
			t.adaptedBases= this.firstTransformation.adaptedBases; // collect into existing
		this.firstTransformation= t;
		return t.adaptedBases;
	}

	//	 ------------------------------------------
	// ---------- Class file dumping: ----------------------
	// ------------------------------------------
	/** Initialized from property <tt>ot.dump</tt>. */
    static boolean dumping = false;
    static {
        if(System.getProperty("ot.dump")!=null)
            dumping = true;
    }
}

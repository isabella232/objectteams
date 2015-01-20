/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2005-2015 Berlin Institute of Technology, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
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
import java.util.List;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.eclipse.objectteams.otre.BaseCallRedirection;
import org.eclipse.objectteams.otre.BaseMethodTransformation;
import org.eclipse.objectteams.otre.Decapsulation;
import org.eclipse.objectteams.otre.LiftingParticipantTransformation;
import org.eclipse.objectteams.otre.OTConstants;
import org.eclipse.objectteams.otre.ObjectTeamsTransformation;
import org.eclipse.objectteams.otre.RepositoryAccess;
import org.eclipse.objectteams.otre.StaticSliceBaseTransformation;
import org.eclipse.objectteams.otre.SubBoundBaseMethodRedefinition;
import org.eclipse.objectteams.otre.TeamInterfaceImplementation;
import org.eclipse.objectteams.otre.ThreadActivation;
import org.eclipse.objectteams.otre.bcel.DietClassLoaderRepository;
import org.eclipse.objectteams.otre.util.AttributeReadingGuard;
import org.eclipse.objectteams.otre.util.CallinBindingManager;


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
		Decapsulation.class,
		LiftingParticipantTransformation.class,
		StaticSliceBaseTransformation.class,
		SubBoundBaseMethodRedefinition.class,
		TeamInterfaceImplementation.class,
		ThreadActivation.class
	};

	static boolean warmedUp = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader,
	 *      java.lang.String, java.lang.Class, java.security.ProtectionDomain,
	 *      byte[])
	 */
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		return transform((Object)loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
	}
	
	public byte[] transform(Object loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer)
			throws IllegalClassFormatException
	{
		if (warmedUp || loader == null)
			return internalTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		synchronized (loader) {
			try {
				return internalTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
			} finally {
				warmedUp = true;
			}		
		}
	}
	public byte[] internalTransform(Object loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer)
			throws IllegalClassFormatException
	{
		switch(className.charAt(0)) {
		case 'o':
			if (   className.startsWith("org/eclipse/objectteams/otre")
				|| className.startsWith("org/apache/bcel"))
				// skip OTRE and BCEL classes
				return null;
			break;
		case 's':
			if (className.startsWith("sun/misc"))
				// skip, I saw a mysterious deadlock involving sun.misc.Cleaner
				return null;
			break;
		case 'j':
			if (className.equals("java/util/LinkedHashMap$KeyIterator")) 
				// skip, I saw class loading circularity caused by accessing this class
				return null;
			if (className.equals("java/util/function/Function")) 
				// skip, contains constant pool with tag 18, which is unknown to BCEL
				return null;
			break;
		}
		if (classBeingRedefined != null) {
			if (!ObjectTeamsTransformation.debugging) {
				System.out.println("Redefinition!");
				return null;
			}
		}
		
		//
		// One fresh instance of each transformer for a given class:
		//
		BaseCallRedirection 				baseCallRedirection 				= new BaseCallRedirection(loader);
		BaseMethodTransformation 			baseMethodTransformation 			= new BaseMethodTransformation(loader);
		Decapsulation 						decapsulation 						= new Decapsulation(loader);
		LiftingParticipantTransformation 	liftingParticipantTransformation	= new LiftingParticipantTransformation(loader);
		StaticSliceBaseTransformation 		staticSliceBaseTransformation 		= new StaticSliceBaseTransformation(loader);
		SubBoundBaseMethodRedefinition 		subBoundBaseMethodRedefinition 		= new SubBoundBaseMethodRedefinition(loader);
		TeamInterfaceImplementation 		teamInterfaceImplementation 		= new TeamInterfaceImplementation(loader);
		ThreadActivation 					threadActivation					= new ThreadActivation();
				
		// tell Repository about the class loader for improved lookupClass()
		DietClassLoaderRepository prevRepository = RepositoryAccess.setClassLoader(loader);
		
		try {
			InputStream is = new ByteArrayInputStream(classfileBuffer);
			JavaClass java_class;
			try {
				java_class = new ClassParser(is, className).parse();
			} catch (ClassFormatException e) {
				// CFE doesn't show the class name, so at least print it to console:
				System.err.println(e.getMessage()+", offending className: "+className);
				throw e;
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
						// nothing we can do
					}
			}
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
			staticSliceBaseTransformation.doTransformInterface(jpe, cg);
			teamInterfaceImplementation.doTransformInterface(jpe, cg);
			
//			subBoundBaseMethodRedefinition.doTransformInterface(jpe, cg);
//			baseCallRedirection.doTransformInterface(jpe, cg);
//			decapsulation.doTransformInterface(jpe, cg);
//			baseMethodTransformation.doTransformInterface(jpe, cg);
//			staticSliceBaseTransformation.doTransformInterface(jpe, cg);
//			teamInterfaceImplementation.doTransformInterface(jpe, cg);
			threadActivation.doTransformInterface(jpe, cg);

			
//			baseCallRedirection.doTransformCode(cg); // empty method
			baseMethodTransformation.doTransformCode(cg);
			liftingParticipantTransformation.doTransformCode(cg);
			staticSliceBaseTransformation.doTransformCode(cg);
			teamInterfaceImplementation.doTransformCode(cg);
			threadActivation.doTransformCode(cg);
			
			JavaClass new_java_class = cg.getJavaClass(); 
			if (dumping) {
				String binaryName = className.replace('.','/');
				new_java_class.dump("jplis_dump/" + binaryName + ".class");
			}
			return new_java_class.getBytes();
		} catch (IOException e) {
			System.err.println("ClassFileTransformer could not parse class file buffer to JavaClass");
			e.printStackTrace();
		} catch (RuntimeException re) {
			re.printStackTrace();
			throw re;
		} finally {
			// restore previous repository:
			RepositoryAccess.resetRepository(prevRepository);
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
	public void readOTAttributes(InputStream file, String fileName, Object loader) 
			throws ClassFormatError, IOException 
	{
		ClassParser   cp  = new ClassParser(file, fileName);
		ClassGen      cg  = new ClassGen(cp.parse());
		JPLISEnhancer jpe = new JPLISEnhancer(cg, /*loader (unused)*/null);
		DietClassLoaderRepository prevRepository = RepositoryAccess.setClassLoader(loader);
		try {
			setFirstTransformation(new ObjectTeamsTransformation(loader) {});
			firstTransformation.checkReadClassAttributes(jpe, cg, cg.getClassName(), cg.getConstantPool());
		} finally {
			RepositoryAccess.resetRepository(prevRepository);
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

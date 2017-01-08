/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2002, 2015 Berlin Institute of Technology, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.transformer.jplis;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.objectteams.otredyn.bytecode.AbstractBoundClass;
import org.eclipse.objectteams.otredyn.bytecode.ClassRepository;
import org.eclipse.objectteams.otredyn.bytecode.asm.WeavableRegionReader;
import org.eclipse.objectteams.otredyn.transformer.IWeavingContext;
import org.eclipse.objectteams.runtime.IReweavingTask;


/**
 * This class does all needed transformations at load time.
 * @author  Christine Hundt
 */
public class ObjectTeamsTransformer implements ClassFileTransformer {

	private IWeavingContext weavingContext;
	
	private Set<String> boundBaseClassNames = new HashSet<String>();

	public ObjectTeamsTransformer() {
		this.weavingContext = new IWeavingContext() {
			@Override public boolean isWeavable(String className) {
				return ObjectTeamsTransformer.isWeavable(className.replace('.', '/'))
						&& WeavableRegionReader.isWeavable(className);
			}
			@Override public boolean scheduleReweaving(String className, IReweavingTask task) {
				return false; // default is to let the transformer work immediately
			}
		};
	}

	public ObjectTeamsTransformer(IWeavingContext weavingContext) {
		this.weavingContext = weavingContext;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader, java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
	 */
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
	{
		try {
			return transform(loader, className, className, classBeingRedefined, classfileBuffer);
		} catch (IllegalClassFormatException e) {
			e.printStackTrace();
			return classfileBuffer;
		}
	}
	
	public byte[] transform(ClassLoader loader, String className, String classId, Class<?> classBeingRedefined,
            byte[] classfileBuffer) throws IllegalClassFormatException {
		if (loader == null)
			loader = ClassLoader.getSystemClassLoader();

		ClassRepository classRepo = ClassRepository.getInstance();
		AbstractBoundClass clazz = classRepo.peekBoundClass(classId);
		String sourceClassName = className.replace('/','.');

		if (!weavingContext.isWeavable(sourceClassName) || loader == null) {
			if (clazz != null) {
				if (isWeavable(className) && clazz.needsWeaving()) {
					// only print out for now, exceptions thrown by us are silently caught by TransformerManager.
					new LinkageError("Classs "+className+" requires weaving, but is not weavable!").printStackTrace();
				} else {
					clazz.markAsUnweavable(); // mark in case we'll try to weave later
				}
			}
			return null;
		}

		if (clazz == null)
			clazz = classRepo.getBoundClass(sourceClassName, classId, loader);

		synchronized(clazz) { // all modifications done in this critical section
			if (classBeingRedefined == null && !clazz.isFirstTransformation()) {
				return clazz.getBytecode();
			}
			try {
				if (clazz.isTransformationActive()) {
					return null;
				}
				clazz = classRepo.getBoundClass(
						className, classId, classfileBuffer, loader);
				clazz.setWeavingContext(this.weavingContext);
				if (!clazz.isInterface())
					classRepo.linkClassWithSuperclass(clazz);
				if (!clazz.isInterface() || clazz.isRole())
					clazz.transformAtLoadTime();
				
				classfileBuffer = clazz.getBytecode();
			} catch (IllegalClassFormatException e) {
				throw e; // expected, propagate to caller (OT/Equinox?)
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		clazz.dump(classfileBuffer, "initial");
		
		Collection<String> boundBaseClasses = clazz.getBoundBaseClasses();
		if (boundBaseClasses != null)
			this.boundBaseClassNames.addAll(boundBaseClasses);

		return classfileBuffer;
	}

	public static boolean isWeavable(String className) {
		switch(className.charAt(0)) {
		case 'o':
			if (   className.startsWith("org/eclipse/objectteams/otre") // incl. otredyn
				|| className.startsWith("org/objectteams/") 			// Team etc.
				|| className.startsWith("org/objectweb/asm"))
				// skip OTRE and ASM classes
				return false;
			break;
		case 's':
			// skip, I saw a mysterious deadlock involving sun.misc.Cleaner
			if (className.startsWith("sun/misc"))
				return false;
			break;
		case 'j':
			// skip, I saw class loading circularity caused by accessing this class:
			if (   className.equals("java/util/LinkedHashMap$KeyIterator")
				|| className.startsWith("java/lang")
				|| className.startsWith("java/util")
				|| className.startsWith("java/io")
				) 
				return false;
			break;
		case '$':
			// funny case, motivated by the following stack trace:
			// 		java.lang.NoClassDefFoundError: org/eclipse/objectteams/otredyn/runtime/TeamManager
			//	    at $Proxy0.<clinit>(Unknown Source)
			//	    at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
			//	    at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:57)
			//	    at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
			//	    at java.lang.reflect.Constructor.newInstance(Constructor.java:532)
			//	    at java.lang.reflect.Proxy.newProxyInstance(Proxy.java:605)
			return false;
		}
		return true;
	}
	
	/** Parse the bytecode of the given class, so we are able to answer {@link #fetchAdaptedBases()} afterwards. */
	public void readOTAttributes(String className, String classId, InputStream inputStream, ClassLoader loader) throws ClassFormatError, IOException {		
		AbstractBoundClass clazz = ClassRepository.getInstance().getBoundClass(
				className.replace('/','.'), classId, loader);
		if (!clazz.isFirstTransformation()) {
			return; // FIXME: re-loading existing class?? Investigate classloader, check classId strategy etc.pp.
		}
		try {
			if (clazz.isTransformationActive()) {
				return;
			}
			int available = inputStream.available();
			byte[] bytes = new byte[available];
			new DataInputStream(inputStream).readFully(bytes);
			clazz = ClassRepository.getInstance().getBoundClass(
					className, classId, bytes, loader);
			if (!clazz.isInterface())
				ClassRepository.getInstance().linkClassWithSuperclass(clazz);
			if (!clazz.isInterface() || clazz.isRole())
				clazz.parseBytecode();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		Collection<String> boundBaseClasses = clazz.getBoundBaseClasses();
		if (boundBaseClasses != null)
			this.boundBaseClassNames.addAll(boundBaseClasses);
	}
	
	/**
	 * After {@link #transform(ClassLoader, String, Class, ProtectionDomain, byte[])} or {@link #readOTAttributes(String, String, InputStream, ClassLoader)}
	 * this method will answer the qualified names (dot-separated) of all base classes adapated by the current team and its roles.
	 */
	public Collection<String> fetchAdaptedBases() {
		try {
			return this.boundBaseClassNames;
		} finally {
			this.boundBaseClassNames = new HashSet<String>();
		}
	}
}

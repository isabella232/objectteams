/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ASMByteCodeAnalyzer.java 23468 2010-02-04 22:34:27Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.objectteams.otequinox.hook.IByteCodeAnalyzer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * This class performs some fast readClass analyses
 * to determine further processing.
 * 
 * @author Oliver Frank
 * @since 1.2.3
 */
public class ASMByteCodeAnalyzer implements IByteCodeAnalyzer {
	private static final int ACC_TEAM = 0x8000;

	private static class ClassInformation {
		private boolean isTeam;
		private String superClassName;

		public ClassInformation(boolean isTeam, String superClassName) {
			super();
			this.isTeam = isTeam;
			this.superClassName = superClassName;
		}

		public boolean isTeam() {
			return isTeam;
		}

		public String getSuperClassName() {
			if (superClassName != null)
				return superClassName.replace('/', '.');
			return null;
		}
	}

	private static class MyVisitor extends EmptyVisitor {
		MyVisitor() { super();}
		private boolean isTeam;
		private String superClassName;

		public boolean isTeam() {
			return isTeam;
		}

		public String getSuperClassName() {
			return superClassName;
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			superClassName = superName;
			isTeam = (access & ACC_TEAM) != 0;
		}
	}

	private Map<String, ClassInformation> classInformationMap = new HashMap<String, ClassInformation>();

	/** 
	 * Fetch the superclass name of a given class.
	 * @param classStream stream providing the class bytes
	 * @param className fully qualified name of the class
	 * @return fully qualified ('.'-separated) name of the class' superclass or null if an exception occurred.
	 */
	public String getSuperclass(InputStream classStream, String className) {
		try {
			return getClassInformation(null, classStream, className)
					.getSuperClassName();
		} catch (IOException e) {
			return null;
		}
	}
	
	/** 
	 * Fetch the superclass name of a given class.
	 * @param classStream stream providing the class bytes
	 * @param className fully qualified name of the class
	 * @return fully qualified ('.'-separated) name of the class' superclass or null if an exception occurred.
	 */
	public String getSuperclass(byte[] classBytes, String className) {
		try {
			return getClassInformation(classBytes, null, className)
					.getSuperClassName();
		} catch (IOException e) {
			return null;
		}
	}

	public boolean isTeam(byte[] classBytes, String className)
			throws IOException {
		return getClassInformation(classBytes, null, className).isTeam();
	}

	private ClassInformation getClassInformation(byte[] classBytes,
			InputStream classStream, String className) throws IOException 
	{
		synchronized (this.classInformationMap) {
			ClassInformation classInformation = classInformationMap.get(className);
			if (classInformation != null) {
				return classInformation;
			}
			if (classBytes != null) {
				classInformation = this.getClassInformationPrivate(classBytes);
			} else {
				classInformation = this.getClassInformationPrivate(classStream);
			}
			classInformationMap.put(className, classInformation);
			return classInformation;
		}
	}


	private ClassInformation getClassInformationPrivate(InputStream classStream)
			throws IOException {
		return getClassInformationPrivate(new ClassReader(classStream));
	}

	private ClassInformation getClassInformationPrivate(byte[] classBytes) {
		return getClassInformationPrivate(new ClassReader(classBytes));
	}

	private ClassInformation getClassInformationPrivate(ClassReader reader) {
		MyVisitor visitor = new MyVisitor();

		reader.accept(visitor, 0);
		return new ClassInformation(visitor.isTeam(), visitor
				.getSuperClassName());
	}
}

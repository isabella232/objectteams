/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008, 2014 Oliver Frank and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 		Oliver Frank - Initial API and implementation
 * 		Stephan Herrmann - adjusted for ASM 5.0
 **********************************************************************/
package org.eclipse.objectteams.internal.osgi.weaving;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.objectteams.internal.osgi.weaving.OTWeavingHook.WeavingScheme;
import org.eclipse.objectteams.otredyn.bytecode.asm.Attributes;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

/**
 * This class performs some fast readClass analyses
 * to determine further processing.
 * 
 * @author Oliver Frank
 * @since 1.2.3
 */
public class ASMByteCodeAnalyzer {
	private static final int ACC_TEAM = 0x8000;

	public static class ClassInformation {
		private int modifiers;
		private String superClassName;
		private String[] superInterfaceNames;

		ClassInformation(ClassReader classReader) {
			this.modifiers = classReader.getAccess();
			this.superClassName = classReader.getSuperName();
			this.superInterfaceNames = classReader.getInterfaces();
		}

		public boolean isTeam() {
			return (modifiers & ACC_TEAM) != 0;
		}

		public boolean isInterface() {
			return (modifiers & Opcodes.ACC_INTERFACE) != 0;
		}

		public String getSuperClassName() {
			if (superClassName != null)
				return superClassName.replace('/', '.');
			return null;
		}
		
		public String[] getSuperInterfaceNames() {
			if (superInterfaceNames != null)
				for (int i = 0; i < superInterfaceNames.length; i++) {
					superInterfaceNames[i] = superInterfaceNames[i].replace('/', '.');
				}
			return superInterfaceNames;
		}
	}

	private Map<String, ClassInformation> classInformationMap =
			new ConcurrentHashMap<String, ClassInformation>(512, 0.75f, 4);

	public ClassInformation getClassInformation(InputStream classStream, String className) {
		try {
			return getClassInformation(null, classStream, className);
		} catch (IOException e) {
			return null;
		}
	}
	
	public ClassInformation getClassInformation(byte[] classBytes, String className) {
		try {
			return getClassInformation(classBytes, null, className);
		} catch (IOException e) {
			return null;
		}
	}

	private ClassInformation getClassInformation(byte[] classBytes,
			InputStream classStream, String className) throws IOException 
	{
		ClassInformation classInformation = classInformationMap.get(className);
		if (classInformation != null) {
			return classInformation;
		}
		ClassReader classReader = classBytes != null ? new ClassReader(classBytes) : new ClassReader(classStream);
		classInformation = new ClassInformation(classReader);
		classInformationMap.put(className, classInformation);
		return classInformation;
	}

	public static WeavingScheme determineWeavingScheme(byte[] classBytes, String className) {
		return determineWeavingScheme(classBytes, null, className);
	}
	public static @NonNull WeavingScheme determineWeavingScheme(InputStream classStream, String className) {
		return determineWeavingScheme(null, classStream, className);
	}
	static @NonNull WeavingScheme determineWeavingScheme(byte[] classBytes, InputStream classStream, String className) {

		class OTCompilerVersion extends Attribute {
			WeavingScheme weavingScheme;
			public OTCompilerVersion() {
				super(Attributes.ATTRIBUTE_OT_COMPILER_VERSION);
			}
			@Override
			protected Attribute read(ClassReader cr, int off, int len, char[] buf, int codeOff, Label[] labels) {
				int encodedVersion  = cr.readUnsignedShort(off);
				weavingScheme = ((encodedVersion & Attributes.OTDRE_FLAG) != 0) ? WeavingScheme.OTDRE : WeavingScheme.OTRE;
				return this;
			}
		}
		class MyClassVisitor extends ClassVisitor {
			OTCompilerVersion compilerVersion;

			private MyClassVisitor() {
				super(org.eclipse.objectteams.otredyn.bytecode.asm.AsmBoundClass.ASM_API);
			}

			@Override
			public void visitAttribute(Attribute attr) {
				if (attr instanceof OTCompilerVersion)
					compilerVersion = (OTCompilerVersion) attr;
			}
		}

		try {
			ClassReader classReader = classBytes != null ? new ClassReader(classBytes) : new ClassReader(classStream);
			// TODO: consider optimizing by copying reduced internals
			MyClassVisitor classVisitor = new MyClassVisitor();
			classReader.accept(classVisitor, new Attribute[] { new OTCompilerVersion() }, ClassReader.SKIP_CODE);
			OTCompilerVersion version = classVisitor.compilerVersion;
			if (version != null) {
				WeavingScheme scheme = version.weavingScheme;
				if (scheme != null)
					return scheme;
			}
		} catch (IOException e) {
			// ignore
		}
		return WeavingScheme.Unknown;
	}
}

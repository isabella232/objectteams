/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2014 GK Software AG
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 	Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.bytecode.asm;

import static org.eclipse.objectteams.otredyn.transformer.names.ClassNames.OBJECT_SLASH;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.objectteams.otredyn.bytecode.asm.ASMByteCodeAnalyzer.ClassInformation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Variant of its superclass which strictly avoids the use of Class.forName(),
 * because that would by-pass our transformer, when invoked from within the transformer!
 */
public class LoaderAwareClassWriter extends ClassWriter {

	// Only use as a resource loader!
	private ClassLoader loader;
	private ASMByteCodeAnalyzer analyzer; // hopefully caching loaded classes per class-being-written is sufficient for performance
	private Map<String, ClassInformation> knownClasses = new HashMap<String, ASMByteCodeAnalyzer.ClassInformation>();
	
	public LoaderAwareClassWriter(ClassReader reader, int computeFrames, ClassLoader loader) {
		super(reader, computeFrames);
		this.loader = loader;
		this.analyzer = new ASMByteCodeAnalyzer(false);
	}
	
	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		// simple cases first:
		if (type1.equals(type2))
			return type1;
		if (type1.equals(OBJECT_SLASH) || type2.equals(OBJECT_SLASH))
			return OBJECT_SLASH;
		
		ClassInformation ci1;
		ClassInformation ci2;
		// need to load class bytes:
		try {
			InputStream s1 = this.loader.getResourceAsStream(type1+".class");
			ci1 = this.analyzer.getClassInformation(s1, type1);
			if (ci1 == null)
				return OBJECT_SLASH;
			InputStream s2 = this.loader.getResourceAsStream(type2+".class");
			ci2 = this.analyzer.getClassInformation(s2, type2);
			if (ci2 == null)
				return OBJECT_SLASH;
		} catch (Exception e) {
		    throw new RuntimeException(e.toString());
		}

        // do a breadth-first search: each iteration adds just one more level of super types,
		// but strictly prefer common super class over super interface.
        Set<String> allTypes1 = new HashSet<String>();
        Set<String> allTypes2 = new HashSet<String>();
        allTypes1.add(type1);
        allTypes2.add(type2);
        // Phase 1: classes:
        List<String> newTypes1 = new ArrayList<String>(); 
        addSuperClass(newTypes1, ci1);
        List<String> newTypes2 = new ArrayList<String>(); 
        addSuperClass(newTypes2, ci2);
        while (!newTypes1.isEmpty() || !newTypes2.isEmpty()) {
        	for (String newType1 : newTypes1)
        		if (allTypes2.contains(newType1))
        			return newType1;
        	allTypes1.addAll(newTypes1);
        	for (String newType2 : newTypes2)
        		if (allTypes1.contains(newType2))
        			return newType2;
        	allTypes2.addAll(newTypes2);
        	newTypes1 = getDirectSupersLayer(newTypes1, true);
        	newTypes2 = getDirectSupersLayer(newTypes2, true);
        }
        // Phase 2: interfaces:
        addSuperInterfaces(newTypes1, ci1);
        addSuperInterfaces(newTypes2, ci2);
        while (true) {
        	if (newTypes1.isEmpty() && newTypes2.isEmpty())
        		return OBJECT_SLASH;
        	for (String newType1 : newTypes1)
        		if (allTypes2.contains(newType1))
        			return newType1;
        	allTypes1.addAll(newTypes1);
        	for (String newType2 : newTypes2)
        		if (allTypes1.contains(newType2))
        			return newType2;
        	allTypes2.addAll(newTypes2);
        	newTypes1 = getDirectSupersLayer(newTypes1, false);
        	newTypes2 = getDirectSupersLayer(newTypes2, false);
        }
	}

	private List<String> getDirectSupersLayer(List<String> types, boolean classes) {
		List<String> result = new ArrayList<String>();
		for (String type : types) {
			ClassInformation ci = this.knownClasses.get(type);
			if (ci == null) {
				InputStream s;
				try {
					s = this.loader.getResourceAsStream(type+".class");
		        } catch (Exception e) {
		            throw new RuntimeException(e.toString());
				}
				ci = this.analyzer.getClassInformation(s, type);
				this.knownClasses.put(type, ci);
			}
			if (ci != null) {
				if (classes)
					addSuperClass(result, ci);
				else
					addSuperInterfaces(result, ci);
			}
		}
		return result;
	}

	private void addSuperClass(List<String> result, ClassInformation ci) {
		String superClass = ci.getSuperClassName();
		if (superClass != null && !superClass.equals(OBJECT_SLASH)) // avoid prematurely answering j.l.Object
			result.add(superClass);
	}

	private void addSuperInterfaces(List<String> result, ClassInformation ci) {
		for (String ifc : ci.getSuperInterfaceNames())
			result.add(ifc);
	}
}

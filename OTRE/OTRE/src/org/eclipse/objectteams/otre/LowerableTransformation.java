/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2009 Stephan Herrmann
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: LowerableTransformation.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 	Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import java.util.HashSet;
import java.util.Set;

import de.fub.bytecode.Constants;
import de.fub.bytecode.generic.ClassGen;
import de.fub.bytecode.generic.MethodGen;
import de.fub.bytecode.generic.Type;

/**
 * This transformer helps legacy class files pre 1.3.2 to cope with changes re ILowerable.lower().
 * 
 * @author stephan
 * @since 1.3.2
 */
public class LowerableTransformation extends ObjectTeamsTransformation {
	
	// static because checking is performed without instance context (from scanClassOTAttribrutes)
	// using ClassGen rather than names should, however, avoid conflicts between different class loaders etc.
	static Set<ClassGen> transformationRequests = new HashSet<ClassGen>();
	
	public LowerableTransformation(SharedState state) { this(null, state); }

	public LowerableTransformation(ClassLoader loader, SharedState state) {
		super(loader, state);
	}
	
	public void doTransformInterface(ClassEnhancer ce, ClassGen cg) {
		synchronized (transformationRequests) {
			if (!transformationRequests.remove(cg))
				return;
		}
		// yes, a change was requested, add method "public abstract Object lower();"
		MethodGen lower = new MethodGen(Constants.ACC_PUBLIC|Constants.ACC_ABSTRACT, object, new Type[0], new String[0], "lower", cg.getClassName(), null, cg.getConstantPool());
		ce.addMethod(lower.getMethod(), cg);
	}

	/** After reading the compiler version of a class file, check if this class is affected by the change. */
	public static void checkRequiresAdaptation(int major, int minor, int revision, ClassGen cg) {
		// only 1.3.1 and below:
		if (major > 1) return;
		if (major == 1 && minor > 3) return;
		if (major == 1 && minor == 3 && revision > 1) return;
		// only interfaces ...
		if (!cg.isInterface()) return;
		// ... implementing ILowerabel:
		for (String superInterface : cg.getInterfaceNames()) {
			if ("org.objectteams.Team$ILowerable".equals(superInterface))
				synchronized(transformationRequests) {
					transformationRequests.add(cg);
					return;
				}
		}
	}
}

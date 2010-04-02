/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2005-2008 Berlin Institute of Technology, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JPLISEnhancer.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.jplis;


import java.io.IOException;
import java.io.InputStream;

import org.eclipse.objectteams.otre.ClassEnhancer;
import org.eclipse.objectteams.otre.OTREInternalError;
import org.eclipse.objectteams.otre.ObjectTeamsTransformation;

import de.fub.bytecode.Constants;
import de.fub.bytecode.classfile.ClassParser;
import de.fub.bytecode.classfile.Field;
import de.fub.bytecode.classfile.Method;
import de.fub.bytecode.classfile.Utility;
import de.fub.bytecode.generic.ClassGen;
import de.fub.bytecode.generic.ConstantPoolGen;
import de.fub.bytecode.generic.MethodGen;


/**
* This class implements the ClassEnhancer interface with the JPLIS (Java5) specific behavior.
*  
* @author Christine Hundt
* @author Juergen Widiker
* @author Stephan Herrmann
*/

public class JPLISEnhancer implements ClassEnhancer {

	private ClassLoader loader;
	
	public JPLISEnhancer(ClassGen cg, ClassLoader loader) {
		this.loader = loader;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otre.ClassEnhancer#addImplements(java.lang.String, de.fub.bytecode.generic.ClassGen)
	 */
	public void addImplements(String interfaceName, ClassGen cg) {
		cg.addInterface(interfaceName);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otre.common.ClassEnhancer#addMethod(de.fub.bytecode.classfile.Method, de.fub.bytecode.generic.ClassGen)
	 */
	public void addMethod(Method m, ClassGen cg) {
		if (cg.containsMethod(m.getName(), m.getSignature()) != null)
			new OTREInternalError("Warning: repetive adding of method "
					           + m.getName() + m.getSignature() 
							   + " to class " + cg.getClassName())
						.printStackTrace();
		cg.addMethod(m);
	}

	public void addOrReplaceMethod(Method method, ClassGen cg) {
		Method existingMethod = cg.containsMethod(method.getName(), method.getSignature());
		if (existingMethod == null)
			addMethod(method, cg);
		else
			cg.replaceMethod(existingMethod, method);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otre.common.ClassEnhancer#addField(de.fub.bytecode.classfile.Field, de.fub.bytecode.generic.ClassGen)
	 */
	public void addField(Field f, ClassGen cg) {
		if (cg.containsField(f.getName()) != null)
			new OTREInternalError("Warning: repetitive adding of field "
					           + f.getName() + f.getSignature() 
							   + " to class " + cg.getClassName())
						.printStackTrace();
		cg.addField(f);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otre.common.ClassEnhancer#loadClass(java.lang.String)
	 */
	public void loadClass(String className, ObjectTeamsTransformation client) {
		// SH: no forced class loading within OT/Equinox
		if(System.getProperty("ot.equinox") != null)
			return;
		try {
			String binaryName = className.replace('.', '/');
			InputStream is = loader.getResourceAsStream(binaryName+".class");
			if (is != null) {
				ClassGen cg = new ClassGen(new ClassParser(is, className).parse());
				client.checkReadClassAttributes(this, cg, className, cg.getConstantPool());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otre.common.ClassEnhancer#decapsulateMethod(de.fub.bytecode.classfile.Method, java.lang.String, de.fub.bytecode.generic.ConstantPoolGen)
	 */
	public void decapsulateMethod(Method m, ClassGen cg, String packageName, ConstantPoolGen cpg) {
		String className = cg.getClassName();
		int flags = m.getAccessFlags();
		MethodGen mg = new MethodGen(m, className, cpg);

		if ((flags & Constants.ACC_PUBLIC) == 0) {
			int newFlags = flags;
			newFlags &= ~(Constants.ACC_PROTECTED|Constants.ACC_PRIVATE); // clear old visibility
			newFlags |= Constants.ACC_PUBLIC;                             // set new visibility
			mg.setAccessFlags(newFlags);
            if(System.getProperty("ot.log") != null)
                ObjectTeamsTransformation.printLogMessage("Adjusting from "
										+ Utility.accessToString(flags)
										+ " to public:\n\t"
										+ className
                        				+ "." + m);
			if (!packageName.equals("NO_PACKAGE")) 
				checkSeal(packageName, className);
		}
		cg.replaceMethod(m, mg.getMethod());
	}
	
	private static void checkSeal(String package_name, String class_name) {
		Package pckg = Package.getPackage(package_name);
		if ( (pckg != null) && pckg.isSealed()) 
			throw new IllegalAccessError(
					"OT/J callout binding:\n"
					+"Trying to break sealed "+pckg);
	}
}

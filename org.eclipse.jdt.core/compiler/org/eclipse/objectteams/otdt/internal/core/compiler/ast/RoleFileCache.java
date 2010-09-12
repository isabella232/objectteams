/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleFileCache.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.RoleFilesAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * NEW for OTDT.
 *
 * This class allows to persistently cache which role files have been
 * seen for a given team.
 *
 * This class is only used during batch compilation (see {@link TeamModel#TeamModel(TypeDeclaration)}).
 *
 * During incremental compilation in workbench mode this class is useless,
 * since the output folder is not searched for types!
 * See NameLookup.packageFragmentRoots.
 *
 * @author stephan
 * @version $Id: RoleFileCache.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class RoleFileCache {

	private TypeDeclaration teamDecl;

	// synthetic declaration of a local type holding the RoleFiles attribute
	private TypeDeclaration _ast = null;
	private List<char[]> knownRoleFiles = new LinkedList<char[]>();
	private ReferenceBinding binaryCache = null;
	public boolean isValid = true;

	/**
	 * @param teamDecl the type for which to read and store the list of known role files.
	 */
	public RoleFileCache(TypeDeclaration teamDecl) {
		this.teamDecl = teamDecl;
	}

	/**
	 * Setup this structure after the enclosing type's binding is initialized.
	 *
	 * @param environment use this to look up the binary role file cache.
	 */
	public void createTypeAndBinding(Scope scope, LookupEnvironment environment) {
		this._ast = new TypeDeclaration(this.teamDecl.compilationResult);
		this._ast.modifiers = ExtraCompilerModifiers.AccRole;
		this._ast.isGenerated = true;
		this._ast.bits |= ASTNode.IsLocalType; // no regular member, not reachable on regular traversals.
		this._ast.name = IOTConstants.ROFI_CACHE;
		this.teamDecl.scope.addGeneratedType(this._ast);
		// set manually because connectTypeHierarchy will not be called for this type.
		this._ast.binding.superInterfaces = Binding.NO_SUPERINTERFACES;
		this._ast.binding.superclass = scope.getJavaLangObject();
		readBinary(environment);
	}

	/**
	 * Record that the team of this RoleFileCache contains a role file
	 * by name `name'.
	 *
	 * @param name
	 */
	public void addRoleFile(char[] name) {
		if (!contains(name))
			this.knownRoleFiles.add(name);
	}

	/**
	 * Generate the code to make this role file cache persistent.
	 * @param enclosingClassFile
	 */
	public void generateCode(ClassFile enclosingClassFile) {
		((LocalTypeBinding)this._ast.binding).setConstantPoolName(
				CharOperation.concat(this.teamDecl.binding.constantPoolName(), this._ast.name, '$'));
		int len = this.knownRoleFiles.size();
		char[][] names = new char[len][];
		System.arraycopy(this.knownRoleFiles.toArray(), 0, names, 0, len);
		this._ast.getModel().addAttribute(new RoleFilesAttribute(names));
		this._ast.generateCode(enclosingClassFile);
	}

	public char[][] getNames() {
		int len = this.knownRoleFiles.size();
		char[][] names = new char[len][];
		System.arraycopy(this.knownRoleFiles.toArray(), 0, names, 0, len);
		return names;
	}

	private boolean contains (char[] name) {
		for (char[] aName : this.knownRoleFiles) {
			if (CharOperation.equals(name, aName))
				return true;
		}
		return false;
	}

	private void readBinary(LookupEnvironment environment) {
		int len = this.teamDecl.binding.compoundName.length;
		char[][] compoundName = new char[len][];
		System.arraycopy(this.teamDecl.binding.compoundName, 0, compoundName, 0, len);
		compoundName[len-1] = CharOperation.concat(compoundName[len-1], IOTConstants.ROFI_CACHE, '$');
		try {
			this.binaryCache = environment.askForType(compoundName);
			if (this.binaryCache == null)
				this.isValid = false;
		} catch (AbortCompilation e) {
			this.isValid = false;
		}
	}

	/**
	 * Ensure all role types listed in this cache are loaded.
	 */
	public void readKnownRoles() {
		if (this.binaryCache != null) {
			RoleFilesAttribute attr = this.binaryCache.model._roleFilesAttribute;
			if (attr != null) {
				char[][] names = attr.getNames();
				if (names != null) {
					for (int i = 0; i < names.length; i++) {
						// force loading:
						this.teamDecl.binding.getMemberType(names[i]);
						//successful retrieval also invokes addRoleFile.
					}
				}
			}
		}
	}

	public static boolean isRoFiCache(ReferenceBinding referenceBinding) {
		return CharOperation.equals(referenceBinding.sourceName, IOTConstants.ROFI_CACHE);
	}
}

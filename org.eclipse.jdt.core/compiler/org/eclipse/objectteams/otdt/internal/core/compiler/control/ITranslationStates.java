/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ITranslationStates.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.control;

/**
 * Constants for the translation states of classes.
 * @author stephan
 * @version $Id: ITranslationStates.java 23416 2010-02-03 19:59:31Z stephan $
 */
public interface ITranslationStates {
	public static final int STATE_NONE					  =  0;
	/**
	 * IMPLEMENTOR: RoleSplitter
	 * PRE:         parsed (maybe diet)
	 * BEFORE: 		LookupEnvironment.buildTypeBindings()
	 * 			    	This way, buildTypeBindings can faithfully share role models
	 * 					of class- and interface parts.
	 */
	public static final int STATE_ROLE_FILES_LINKED		  =  1;//LookupEnv.
	public static final int STATE_ROLES_SPLIT			  =  2;//RoleSplitter & LookupEnv.
	public static final int STATE_BINDINGS_BUILT          =  3;//LookupEnvironment
	// the next for states correspond to LookupEnvironment.{BUILD_TYPE_HIERARCHY .. BUILD_FIELDS_AND_METHODS}:
	public static final int STATE_LENV_BUILD_TYPE_HIERARCHY=    4;//LookupEnvironment & Deps.
	public static final int STATE_LENV_CHECK_AND_SET_IMPORTS=   5;//LookupEnvironment & Deps.
	public static final int STATE_LENV_CONNECT_TYPE_HIERARCHY=  6;//LookupEnvironment & Deps. & CopyInheritance
	public static final int STATE_LENV_DONE_FIELDS_AND_METHODS= 7;//LookupEnvironment & Deps.
	public static final int STATE_ROLES_LINKED            =  8;//CopyInheritance & Deps.
	public static final int STATE_METHODS_PARSED          =  9;//Compiler
	public static final int STATE_ROLE_INIT_METHODS       = 10;//RoleInitializationMethod
    public static final int STATE_ROLE_FEATURES_COPIED    = 11;//Copy inheritance
	public static final int STATE_ROLE_HIERARCHY_ANALYZED = 12;//Lifting
	public static final int STATE_FULL_LIFTING            = 13;//Lifting
    public static final int STATE_FAULT_IN_TYPES          = 14;//Scope
    public static final int STATE_METHODS_CREATED         = 15;//RoleTypeBinding, CopyInheritance
    public static final int STATE_TYPES_ADJUSTED          = 16;//RoleTypeBinding, CopyInheritance
    public static final int STATE_STATEMENTS_TRANSFORMED  = 17;//TransformStatementsVisitor
    public static final int STATE_CALLINS_TRANSFORMED     = 18;//CallinImplementor[Dyn]
    public static final int STATE_LATE_ATTRIBUTES_EVALUATED= 19; // ModelElement, TypeModel
    public static final int STATE_METHODS_VERIFIED        = 20;//Scope
    public static final int STATE_RESOLVED                = 21;//AST
    public static final int STATE_LATE_ELEMENTS_COPIED    = 22;//CopyInheritance
    public static final int STATE_CODE_ANALYZED           = 23;//AST
    public static final int STATE_BYTE_CODE_PREPARED      = 24;//Dependencies
    public static final int STATE_BYTE_CODE_GENERATED     = 25;//AST
    /**
     * Note, that this state does not trigger intermediate steps, in fact never use it
     * with ensureState() but directly invoke Dependencies.cleanup!
     */
    public static final int STATE_FINAL                   = 26;

	@SuppressWarnings("nls")
	public static final String[] STATE_NAMES =
	{
		"none",
		"role files linked",
		"roles split",
		"bindings built",
		"bindings: hierarchy built",
		"bindings: imports built",
		"bindings: hierarchy connected",
		"bindings: fields and methods [completed]",
		"roles linked",
		"method bodies parsed",
		"role initialization method",
        "role features copied",
        "role hierarchy analyzed",
        "full lifting",
        "fault in types",
        "methods created",
        "types adjusted",
        "statements transformed",
        "callins transformed",
        "late attributes evaluated",
        "methods verified",
        "resolved",
		"late elements copied",
        "code analyzed",
        "byte code prepared",
        "byte code generated",
        "translation completed"
	};
}

/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2014 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions.WeavingScheme;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.CPTypeAnchorAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.OTSpecialAccessAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ReferencedTeamsAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.RoleFilesAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.WordValueAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateMemento;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.LineNumberProvider;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * Generalizes TeamModel and RoleModel
 * @author stephan
 */
public class TypeModel extends ModelElement {

    /* Constructor for regular instances. */
    public TypeModel (TypeDeclaration ast) {
        this._ast = ast;
    	int[] lineSeparatorPositions = ast.compilationResult.lineSeparatorPositions;
    	if (lineSeparatorPositions != null) {
    		this._maxLineNumber = Util.getLineNumber(ast.declarationSourceEnd, lineSeparatorPositions, 0, lineSeparatorPositions.length-1);
    	} else if (   !ast.isConverted
    		 	   && !ast.isGenerated
				   && Dependencies.needMethodBodies(ast))
    	{
    		throw new InternalCompilerError("Unexpectedly missing line number information");  //$NON-NLS-1$
    	}
    }
    /* Constructor for regular instances. */
    public TypeModel (ReferenceBinding bind) {
        this._binding = bind;
    }

    /** Store the byte code attribute which maps constant pool entries (Class_info) to type anchors. */
    private CPTypeAnchorAttribute _typeAnchors;

    /** Mediate RoleFilesAttribute to RoleFileCache. */
    public RoleFilesAttribute _roleFilesAttribute = null;

    /** Backward reference for the Ast being wrapped. */
	protected TypeDeclaration _ast;

	protected ReferenceBinding _binding;

    private HashSet<ReferenceBinding> _referencedTeams = null;

    /** translation state of this model element */
    final public StateMemento _state= new StateMemento();

    /** Record here all types which are defined local to a method (TypeDeclaration). */
    private LinkedList<TypeDeclaration> _localPlainTypes = new LinkedList<TypeDeclaration>();

    public int _maxLineNumber = 0;

    /**
     * Needed for JSR-045 support.
	 * Can be asked for new line numbers(above linenumberrange) and stores mapping.
	 */
	protected LineNumberProvider _lineNumberProvider;

	/**
	 * Version number of the compiler that created a (binary) type;
	 */
	public int _compilerVersion;
	
	WeavingScheme weavingScheme;


	public void setBinding (ReferenceBinding binding) {
        this._binding = binding;
        binding.model = this;
    }
	/**
	 * @return The class-part of the AST being wrapped. May be null!!
	 */
	public TypeDeclaration getAst() {
		return this._ast;
	}
	public ReferenceBinding getBinding() {
		if (this._binding == null && this._ast != null && this._ast.binding != null)
			setBinding(this._ast.binding);
		return this._binding;
	}

    /**
     * Get all members which are not roles represented by their TypeModel.
     * Considers Ast or Bindings, whatever is more appropriate.
     * Note, that members could again be teams.
     *
     * @returns all non-role members for this Team.
     */
    public TypeModel[] getMembers()
    {
        List<TypeModel> list = new LinkedList<TypeModel>();

        if(this._ast == null)
        {
        	if (   this._binding.kind() == Binding.TYPE_PARAMETER
        		|| this._binding.kind() == Binding.WILDCARD_TYPE
        		|| this._binding.kind() == Binding.INTERSECTION_TYPE
        		|| this._binding.kind() == Binding.INTERSECTION_TYPE18)
        		return new TypeModel[0]; // has no members
            //binary type
        	assert this._binding.isBinaryBinding();
        	// don't used memberTypes() as not to trigger resolving unneeded members.
        	// see tests.compiler.regegression.LookupTest.test044
            ReferenceBinding[] memberBindings = ((BinaryTypeBinding)this._binding).unresolvedMemberTypes();
            for (int i = 0; i < memberBindings.length; i++)
            {
                ReferenceBinding binding = memberBindings[i];
                if (binding instanceof UnresolvedReferenceBinding)
                	continue; // has no model yet cannot handle yet.
                if (binding.isTeam())
                    list.add(binding.getTeamModel());
                else
                    list.add(binding.model);
            }
        }
        else
        {
            TypeDeclaration[] members = this._ast.memberTypes;
            if (members != null)
            {
                for (int idx = 0; idx<members.length; idx++)
                {
                    TypeDeclaration decl = members[idx];
                    if (decl.isTeam())
                        list.add(decl.getTeamModel());
                    else if (decl.isRole())
                    	list.add(decl.getRoleModel());
                    else
                        list.add(decl.getModel());
                }
            }

            AbstractMethodDeclaration[] methods = this._ast.methods;
            if (methods != null) {
                for (int i=0; i<methods.length; i++) {
                    if (methods[i].scope != null) {
                        ClassScope[] scopes = methods[i].scope.getAllLocalTypes();
                        for (int j=0; j<scopes.length; j++) {
                            TypeDeclaration type = scopes[j].referenceContext;
                            if (type.isTeam()) // very unlikely ;-)
                                list.add(type.getTeamModel());
                            else if (type.isRole())
                            	list.add(type.getRoleModel());
                            else
                                list.add(type.getModel());
                        }
                    }
                    // no scope could mean:
                    // 1. it's a <clinit>
                    // 2. we are before scope creation
                    //    (which happens in completeTypeBindings(), last step of beginToCompile).
                    // This is ok, since then we only miss step RoleSplitter, but local types
                    // cannot be teams with roles anyway.
                }
            }
        }
        return list.toArray(new TypeModel[list.size()]);
    }

    public ReferenceBinding findType(LookupEnvironment environment, char[][] compoundName) {
		char[][] myName = CharOperation.splitOn('/', this._binding.constantPoolName());
		if (   this._binding.isLocalType()
			&& CharOperation.equals(compoundName, myName))
			return this._binding;
		if (   this._binding.enclosingType() != null
			&& this._binding.enclosingType().isRole())
			return this._binding.enclosingType().roleModel.findType(environment, compoundName);
		return environment.getType(compoundName);
    }

    public char[] getInternalName() {
        if (this._ast != null)
            return this._ast.name;
        else
            return this._binding.internalName();
    }


    public void cleanup() {
        this._ast = null;
        this._lineNumberProvider = null;
    }

    public void addLocalType(TypeDeclaration localType) {
    	this._localPlainTypes.add(localType);
    }

    // === state handling: ===

	public int getState() {
		return this._state.getState();
	}

    /**
     * Include local types in set state.
     */
    public int setState(int state) {
    	int oldState= this._state.setState(state);
    	for (TypeDeclaration localType : this._localPlainTypes)
    		if (localType.getModel().getState() >= ITranslationStates.STATE_RESOLVED)
    			localType.getModel().setState(state);
    	this._state.runPendingJobs(state);
    	return oldState;
    }

	public void setMemberState(int state) {
        TypeModel[] members = getMembers();
        if (members != null)
        {
            for (int i=0; i<members.length; i++)
            {
                TypeModel member = members[i];
                member.setMemberState(state);
                member.setState(state);
            }
        }
    }

	public boolean isReadyToProcess(int state) {
		return this._state.isReadyToProcess(state);
	}

    public boolean isTeam() {
        return false;
    }
	public boolean isRole() {
		if (this._binding != null)
			return this._binding.isRole();
		return this._ast.isRole();
	}

	protected String getKindString() {
        return "Class "; //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	public String toString() {
		String name = (this._ast != null) ?
				new String(this._ast.name) :
				new String(this._binding.sourceName());
		return   getKindString()+" "+name
		       + " (state: " + this._state +")";
	}

    /**
     * Check whether a binding found by getTypeOrPackage is a team,
     * and if so, record it as a referenced team of this type.
     *
     * @param binding
     * @param scope site where reference was found
     */
    public static void checkReferencedTeam(Binding binding, Scope scope) {
        if (!(binding instanceof ReferenceBinding))
            return;
        ReferenceBinding type = (ReferenceBinding)binding;
        if (!type.isTeam())
            return;
        // is binding an enclosing type of scope?
        ReferenceBinding currentType = scope.enclosingSourceType();
    	while (currentType != null) {
			if (currentType == binding)
				return; // no need to record self-reference
			currentType = currentType.enclosingType();
    	}
    	while (scope != null) {
			TypeDeclaration site = scope.referenceType();
			if (site == null)
				return;
			site.getModel().registerReferencedTeam(type);
			scope = scope.parent;
    	}
    }

    /**
     * Mark that the current class references the given team type.
     * @param teamType
     */
    private void registerReferencedTeam(ReferenceBinding teamType) {
    	if (TypeAnalyzer.isOrgObjectteamsTeam(teamType))
    		return; // optimize since Team has no bindings.
        if (this._referencedTeams == null) {
            this._referencedTeams = new HashSet<ReferenceBinding>();
            addAttribute(new ReferencedTeamsAttribute(this));
        }
        this._referencedTeams.add(teamType);
    }

    /**
     * Retrieve the gathered data about referenced teams.
     * @return Object[] containing ReferenceBindings
     */
    public ReferenceBinding[] getReferencedTeams() {
        return this._referencedTeams.toArray(new ReferenceBinding[this._referencedTeams.size()]);
    }

    public boolean isIgnoreFurtherInvestigation() {
    	if (this._ast == null)
    		return false;
    	return TypeModel.isIgnoreFurtherInvestigation(this._ast);
    }
    public static boolean isIgnoreFurtherInvestigation(TypeDeclaration type)
    {
    	if (type.ignoreFurtherInvestigation)
    		return true;
    	if (   type.compilationUnit != null
    		&& type.compilationUnit.ignoreFurtherInvestigation)
    		return true;
    	if (   type.scope != null
    		&& type.scope.cuIgnoreFurtherInvestigation())
        	return true;
    	if (   type.enclosingType != null
    		&& type.enclosingType.ignoreFurtherInvestigation)
    		return true;
    	if((type.bits & ASTNode.IsLocalType)!=0 && type.scope != null) {
    		MethodScope outerMostMethodScope = type.scope.outerMostMethodScope();
    		if (outerMostMethodScope != null) {
    			if (outerMostMethodScope.referenceContext instanceof AbstractMethodDeclaration)
    			{ // weired: MethodScope.referenceContext can be a TypeDeclaration!?!
    				AbstractMethodDeclaration method = (AbstractMethodDeclaration)outerMostMethodScope.referenceContext;
    				if (   method != null
    				    && method.ignoreFurtherInvestigation)
    					return true;
    			}
    		}
    	}
    	return false;
    }

    /**
     * Has this type been compiled with an old compiler,
     * meaning byte code is not compatible to current commpiler?
     *
     * Should only be used when a problem has already been detected,
     * because we might be over-cautious here which means a <code>true</code>
     * answer from this method does not necessarily imply actual incompatibility.
     */
    public boolean isIncompatibleCompilerVersion() {
    	// require major and minor to match the current compiler, for now.
		int minVersion =    (IOTConstants.OTVersion.getMajor() << 9)
						  + (IOTConstants.OTVersion.getMinor() << 5);
		return this._compilerVersion < minVersion;
	}

	public CPTypeAnchorAttribute getTypeAnchors() {
    	return this._typeAnchors;
    }
	/**
	 * Note: this setter is overridden in RoleModel
	 */
	public void setTypeAnchors(CPTypeAnchorAttribute attribute) {
		this._typeAnchors = attribute;
	}
	public void addTypeAnchor(ITeamAnchor anchor, int cpIndex) {
		if (this._typeAnchors == null) {
			this._typeAnchors = new CPTypeAnchorAttribute();
			addAttribute(this._typeAnchors);
		}
		this._typeAnchors.addTypeAnchor(anchor, cpIndex);
	}

	/**
	 * Some attributes can only be evaluated after translation reaches a specific state
	 *	at end of FAULT_IN_TYPES:
	 *	    CallinMethodMappingAttribute, CalloutMethodMappingAttribute
	 * 	task of LATE_ATTRIBUTES_EVALUATED:
	 *	    CopyInheritanceSrc (roles)
	 *	    CallinPrecedence (teams)
	 */
	public void evaluateLateAttributes(int state) {
		try {
			try {
				if (this._binding != null && this._binding.isBinaryBinding()) {
					if (this._attributes != null) {
						for (int i = 0; i < this._attributes.length; i++) {
							this._attributes[i].evaluateLateAttribute(this._binding, state);
						}
					}
				}
			} catch (AbortCompilation ac) {
				// abort may e.g. happen if a binary tsuper role is inconsistent.
				TypeDeclaration type = this._ast;
				if (type == null) {
					ReferenceBinding enclosing = this._binding.enclosingType();
					if (enclosing != null && !enclosing.isBinaryBinding())
						type = enclosing.model.getAst();
				}
				if (type != null)
					ac.updateContext(type, type.compilationResult);
				throw ac;
			}
			if (this._binding != null) {
				ReferenceBinding[] memberTypes = this._binding.memberTypes();
				if (memberTypes != null) {
					for (int i = 0; i < memberTypes.length; i++) {
						ModelElement.evaluateLateAttributes(memberTypes[i], state);
					}
				}
			}
		} finally {
			// don't advance the state when working on behalf of another state
			if (state == ITranslationStates.STATE_LATE_ATTRIBUTES_EVALUATED)
				setState(ITranslationStates.STATE_LATE_ATTRIBUTES_EVALUATED);
		}
	}
	// ====  allow to re-read bytes from the written class file ====
	// See comment in ClassFile (near field "generatingModel").

	protected String _classFilePath = null;

	/** Store different elements accessed by this class, which require special treatment:
	 *  methods and base-classes requiring decapsulation.
	 *  fields requiring a getter and/or setter.
	 */
	protected OTSpecialAccessAttribute _specialAccess = null;

	/**
	 * @param classFilePath
	 */
	public void setClassFilePath(String classFilePath) {
		this._classFilePath = classFilePath;
	}
	public ClassFileReader read () throws IOException, ClassFormatException
	{
		if (this._classFilePath != null) {
			FileNotFoundException fileNotFoundException = null;
			for (int i=0; i<5; i++) { // make <= 5 attempts thus waiting <= 500 ms
				try {
					return ClassFileReader.read(this._classFilePath); // not recording attributes
				} catch (FileNotFoundException ex) {
					fileNotFoundException = ex;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (fileNotFoundException != null)
				throw fileNotFoundException;
		}
		return null;
	}
	/**
	 * supports CALLIN_METHOD_MAPPINGS_ATTRIBUTE and STATE_REPLACE_BINDINGS_ATTRIBUTE
	 * @param superDeclaringType represents the type from which the attribute is being copied
	 * @param attributeName
	 */
	public void copyAttributeFrom(TypeModel superDeclaringType, char[] attributeName) {
		if (superDeclaringType._attributes == null)
			return;
		for (int i = 0; i < superDeclaringType._attributes.length; i++) {
			if (superDeclaringType._attributes[i].nameEquals(attributeName)) {
				addOrMergeAttribute(superDeclaringType._attributes[i]);
				return;
			}
		}
	}

	public void setSpecialAccess(OTSpecialAccessAttribute attribute) {
		assert this._specialAccess == null;
		this._specialAccess = attribute;
	}
	public OTSpecialAccessAttribute getSpecialAccessAttribute() {
		if (this._specialAccess == null) {
			this._specialAccess = new OTSpecialAccessAttribute(this._binding, getWeavingScheme());
			addAttribute(this._specialAccess);
		}
		return this._specialAccess;
	}

	public LineNumberProvider getLineNumberProvider() {
	    if (this._lineNumberProvider == null)
	        this._lineNumberProvider = new LineNumberProvider(this._binding, this._maxLineNumber);
	    return this._lineNumberProvider;
	}
	
	public static LineNumberProvider getLineNumberProvider(TypeDeclaration type) {
		if (type.isRole())
			return type.getRoleModel().getLineNumberProvider();
		if (type.isTeam())
			return type.getTeamModel().getLineNumberProvider();
		return type.getModel().getLineNumberProvider();
	}

	public static boolean isConverted(ReferenceBinding declaringClass) {
		TypeDeclaration result;
		if (declaringClass instanceof SourceTypeBinding) {
			Scope scope = ((SourceTypeBinding) declaringClass).scope;
			if (scope != null && scope.referenceType() != null)
				return scope.referenceType().isConverted;
		}
		// TODO(SH): the rest of this method is probably obsoleted the above
		if (declaringClass.isRole()) {
			RoleModel roleModel = declaringClass.roleModel;
			if (roleModel != null) {
				if (  (result = roleModel.getClassPartAst()) != null
					&& result.isConverted)
					return true;
				if (  (result = roleModel.getInterfaceAst()) != null
					&& result.isConverted)
					return true;
			}
		}
		if (declaringClass.isTeam()) {
			TeamModel teamModel = declaringClass.getTeamModel();
			if (teamModel != null) {
				if (  (result = teamModel.getAst()) != null
					&& result.isConverted)
					return true;
			}
		}
		return false;
	}
	public WeavingScheme getWeavingScheme() {
		if (this.weavingScheme == null && this._ast != null && this._ast.scope != null) {
			this.weavingScheme = this._ast.scope.compilerOptions().weavingScheme;
			if (this.weavingScheme == WeavingScheme.OTDRE) {
				if (this._attributes != null)
					for (int i = 0; i < this._attributes.length; i++)
						if (this._attributes[i].nameEquals(IOTConstants.OT_COMPILER_VERSION)) {
							((WordValueAttribute)this._attributes[i]).setWeavingScheme(this.weavingScheme);
							break;
						}
			}
		}
		return this.weavingScheme;
	}
}

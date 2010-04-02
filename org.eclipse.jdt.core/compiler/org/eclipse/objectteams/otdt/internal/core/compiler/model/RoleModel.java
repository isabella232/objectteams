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
 * $Id: RoleModel.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.model;

import static org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers.AccOverriding;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccInterface;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccSynthetic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.CPTypeAnchorAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.CallinMethodMappingsAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.CalloutMappingsAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.OTSpecialAccessAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.RoleLocalTypesAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.SingleValueAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.WordValueAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticRoleFieldAccess;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CalloutImplementor;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.TypeLevel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.RoleSplitter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * This class augments and generalizes ReferenceBinding and MemberTypeDeclaration
 * to take some additional information for role classes.
 *
 * These are the main tasks:
 * <ul>
 *   <li>Store byte code from tsuper role before adjustment
 *   <li>Link to team, link ifc/class parts, link to tsuper role
 *   <li>record the need to create a concrete _OT$getBase() method.
 *   <li>Iterate through role hierarchies.
 * </ul>
 *
 * @author stephan
 * @version $Id: RoleModel.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class RoleModel extends TypeModel
{


	// ========== Byte code related fields: ======================
	// name to use for ClassFileReader when re-interpreting existing constant pool
    public static final char[] NO_SOURCE_FILE = "<intermediate>".toCharArray(); //$NON-NLS-1$

	/** Temporarily store the ClassFile to initialize offsets later. */
    private ClassFile _classFile = null;

    /** The full byte code of this class. */
    private byte[] _classByteCode = null;

    private int    _headerOffset = 0;

    /** Offsets into the constant pool */
    private int[]  _constantPoolOffsets = null;

    /** A table of all offsets to methods within the byte code */
    private HashMap <MethodBinding, Integer> _methodByteCodeOffsets = new HashMap<MethodBinding, Integer>();

    // ========= Structure related fields: ============
    /** The Team containing this role. */
	private TeamModel   _teamModel;

    /**
     * The corresponding roles from super-Teams.
     * This is a list, because for each level of nesting a new super team arises:
     * super, tsuper, tsuper-of-tsuper etc.
     * Examples:
     *   a role T2M2R1 may have these tsupers: T2M1R1 and T1M2R1
     *   a role O2T2M2R1 has these: O2T2M1R1, O2T1M2R1 and O1T2M2R1
     * All remaining roles R1 are indirect tsupers as they can be reached via any
     * of the above.
     */
    private ReferenceBinding[] _tsuperRoleBindings = new ReferenceBinding[2]; // 2 should suffice for human understandable programs ;-}
    private int numTSuperRoles = 0;

    /** this is set while role-splitting */
    public TypeDeclaration _interfacePart = null;

    /** this is set while role-splitting */
    public TypeDeclaration _classPart = null;

    private ReferenceBinding _interfaceBinding = null; // only internal cache
    private ReferenceBinding _classBinding = null;

    /** Record here all types which are defined local to a method (RoleModel). */
    private LinkedList<RoleModel> _localTypes = new LinkedList<RoleModel>();

	/** The known sub roles (including this) as determined by RoleHierarchyAnalyzer.analyze(). */
	private RoleModel[] _subRoles = null;

    /** bound roles store here the topmost bound (super)role, which determines the cache to use: */
    public RoleModel _boundRootRole = null;

    /** will this role declare an abstract lower method? */
    public boolean _abstractLower = false;

    /** is this role refining the extends clause of its tsuper role? */
    public boolean _refinesExtends = false;

    /** does this bound role have a binding ambiguity prohibiting lifting? */
    public boolean _hasBindingAmbiguity = false;

    /** map synthetic methods/fields of tsuper role to this role's */
    private HashMap<Binding, Binding> _syntheticMap = new HashMap<Binding, Binding>();

    /** is this role a local type? */
    public boolean _isLocalType;

	/** Remember which tsuper-features (methods & fields) have already been copied,
	 *  so that we can come back and copy more features (copyGeneratedFeatures)
	 *  without the risk of duplication.
	 */
	private HashSet<Binding> _copiedFeatureBindings = new HashSet<Binding>();


	/** Signal if a more specific role exists with unchanged playedBy. */
	public ReferenceBinding _supercededBy;


    /**
     * Constructor for use by BinaryTypeBinding only.
     */
    public RoleModel (ReferenceBinding roleBinding)
    {
        super(roleBinding);
        if (roleBinding.isLocalType())
        	this._isLocalType = true;
    }

    /**
     * Constructor for use by MemberTypeDeclaration only.
     */
    public RoleModel (TypeDeclaration roleAst)
    {
        super(roleAst);
        addAttribute(WordValueAttribute.compilerVersionAttribute());
        if (roleAst.scope != null && roleAst.scope.parent instanceof MethodScope)
        	this._isLocalType = true;
    }

    public boolean hasState(int state) {
    	return this._state.getState() >= state;
	}

	/**  @return old state */
	public int setState(int state) {
// FIXME(SH): should perhaps also set state of role-CUDs??
//            their state seems to skip many stages...
		int oldState = super.setState(state); // may also run pending jobs
		if (   oldState >= ITranslationStates.STATE_ROLE_FEATURES_COPIED
			&& state    <  ITranslationStates.STATE_BYTE_CODE_GENERATED)
		{
			// we are past STATE_ROLE_FEATURES_COPIED, take care to add generated features.
			// (This assumes, super team already has 'state').
			if (this._ast != null)
				CopyInheritance.copyGeneratedFeatures(this);
		}
// TODO(SH): enable only if needed, currently such behavior triggered only in setStateRecursive(CUD,state)
//    	if (   this._currentlyProcessingState == ITranslationStates.STATE_NONE
//        	&& this._requestedState > state)
//    	{
//    		this._currentlyProcessingState= this._requestedState;
//        	if (Dependencies.ensureRoleState(this, this._requestedState))
//        		this._requestedState= ITranslationStates.STATE_NONE;
//    	}
		return oldState;
	}

    /**
     * Override method from TypeModel:
     * Also recurse into role local types.
     */
	public void setMemberState(int state) {
		super.setMemberState(state);
		if (   state <= ITranslationStates.STATE_ROLES_SPLIT
			|| state >= ITranslationStates.STATE_RESOLVED)
		{
	        Iterator<RoleModel> localTypes = localTypes();
	        while (localTypes.hasNext()) {
	        	RoleModel type = localTypes.next();
        		type.setState(state);
        		type.setMemberState(state);
	        }
		}
	}

	@Override
	public boolean isReadyToProcess(int state) {
		if (!this._state.isReadyToProcess(state))
			return false;
		// check the other model, too:
		RoleModel otherModel= null;
		if (this._interfaceBinding != null)
			otherModel= this._interfaceBinding.roleModel;
		if (otherModel == null || otherModel == this) {
			if (this._classBinding != null)
				otherModel= this._classBinding.roleModel;
		}
		if (otherModel != null && otherModel != this)
			return otherModel._state.isReadyToProcess(state);
		return true;
	}

	public boolean isTeam() {
		if (this._ast != null)
			return this._ast.isTeam();
		return this._binding.isTeam();
	}

	public TeamModel getTeamModelOfThis() {
		// the class part has all the structure:
		getClassPartAst(); // initialize
		if (this._classPart != null && this._classPart.isTeam())
			return this._classPart.getTeamModel();
		getClassPartBinding(); // initialze
		if (this._classBinding != null && this._classBinding.isTeam())
			return this._classBinding.getTeamModel();
		return null;
	}


	public static boolean isClass(ReferenceBinding memberType) {
		if (!memberType.isRole())
			return memberType.isClass();
		ReferenceBinding realClass = memberType.getRealClass();
		return realClass != null && realClass.isClass();
	}


	public static boolean isInterface(ReferenceBinding memberType) {
		if (!memberType.isRole())
			return memberType.isInterface();
		// role can only be class or interface; test !class:
		ReferenceBinding realClass = memberType.getRealClass();
		return realClass == null || !realClass.isClass();
	}

	/** Is ifc the synthetic interface part of clazz? */
	public static boolean isSynthIfcOfClass(ReferenceBinding ifc, ReferenceBinding clazz) {
		if (ifc.isSynthInterface() && ifc.roleModel != null)
			if (ifc.roleModel.getClassPartBinding() == clazz)
				return true;
		return false;
	}

    // ================= Byte code related methods =========================

    /** Store byte code information of a method */
    public void recordByteCode (
            MethodBinding method,
            byte[] code,
            int offset,
            int[] constantPoolOffsets)
    {
        this._methodByteCodeOffsets.put(method, new Integer(offset));
        if (this._classByteCode == null) {
            this._classByteCode = code;
            this._constantPoolOffsets = constantPoolOffsets;
        } else {
            assert(this._classByteCode == code);
            // when registering from IBinaryType, the code will not be modified any more.
        }
    }

    /** Store byte code information of a method */
    public static void maybeRecordByteCode(
            AbstractMethodDeclaration method,
            ClassFile file,
            int codeAttributeOffset)
    {
        TypeDeclaration clazz = method.scope.referenceType();
        if (clazz.isRole())
        {
            TypeDeclaration memberType = clazz;
            RoleModel role = memberType.getRoleModel();
            role.recordClassFile(method.binding, file, codeAttributeOffset);
        }
    }

    /**
     * Is this role expected to have byte code?
     * Reasons for not having byte code originate in ignoreFurtherInvestigation
     * and may be propagated via MethodBinding.bytecodeMissing.
     * If no method with bytecode exists, no bytes have been recorded here.
     */
    public boolean hasByteCode() {
    	if (this._classByteCode != null || this._classFile != null || this._classFilePath != null)
    		return true;
    	TypeDeclaration ast = this._ast;
    	if (ast == null)
    		ast = this._binding.isInterface() ? this._interfacePart : this._classPart;
    	if (ast == null) return false;
    	if (TypeModel.isIgnoreFurtherInvestigation(ast)) return false;
    	MethodBinding[] methods= ast.binding.methods();
    	for (MethodBinding methodBinding : methods) {
			if (!methodBinding.bytecodeMissing)
				return true;
		}
    	return false;
    }
    /** Get the byte code of this role class.
     *  Must be registered with (maybe)recordByteCode
     */
    public byte[] getByteCode ()
    {
        if (this._classByteCode == null) {
            if (this._classFile != null) // nullified once a class file is re-used for a different type
            {
            	this._classByteCode = this._classFile.getBytes();
            	this._headerOffset = this._classFile.headerOffset;
            } else {
            	// restore bytes from class file on disk:
            	try {
            		ClassFileReader reader = read();
	            	if (reader == null) {
	        			if (Config.getConfig().ignoreMissingBytecode)
	        				return null;
	            		throw new InternalCompilerError("Class file was not yet written to disk"); //$NON-NLS-1$
	            	}
                	this._classByteCode = reader.getBytes();
                	this._headerOffset = reader.getHeaderOffset();
                	this._constantPoolOffsets = reader.getConstantPoolOffsets();
				} catch (Exception e) {
					throw new InternalCompilerError("cannot retrieve generated class file: "+e); //$NON-NLS-1$
            	}
            }
        }
        assert(this._classByteCode != null);
        return this._classByteCode;
    }

    /** Get the byte code offsets of this role's constant pool.
     *  Must be registered with (maybe)recordByteCode
     */
    public int[] getConstantPoolOffsets()
    {
        if (this._constantPoolOffsets == null)
            restoreCPOffsets();
        return this._constantPoolOffsets;
    }

    /** Get the offset where in the byte code 'method' start.
     *  Must be registered with (maybe)recordByteCode
     *  @return method offset or -1 if source method has no byte code due to errors.
     */
    public int getByteCodeOffset (MethodBinding method)
    {
    	if (getByteCode() == null)
    		return -1;
        Integer offset = this._methodByteCodeOffsets.get(method);
        if (offset == null)
        {
            if(!method.bytecodeMissing)
                throw new InternalCompilerError("Method has no byte code: "+method); // unexpectedly! //$NON-NLS-1$
            return -1;
        }
        return offset.intValue() + this._headerOffset;
    }

    @Override
    public void setClassFilePath(String classFilePath) {
       	super.setClassFilePath(classFilePath);
       	this._classFile = null; // don't use any more but retrieve using the class file path
       	if (isTeam())
       		getTeamModelOfThis().setClassFilePath(classFilePath);
    }
    /**
     *  Currently unusable, since bytecode may be needed any time again!
     */
    public void forgetByteCode() {
        this._methodByteCodeOffsets = new HashMap<MethodBinding, Integer>();
        this._classFile           = null;
        this._classByteCode       = null;
        this._constantPoolOffsets = null;
    }

    private void recordClassFile(MethodBinding method, ClassFile file, int offset)
    {
        this._methodByteCodeOffsets.put(method, new Integer(offset));
        if (   (this._classFile != null)
            && (this._classFile != file))
        {
            throw new InternalCompilerError("wrong ClassFile instance."); //$NON-NLS-1$
        }
        this._classFile = file;
    }

    private void restoreCPOffsets() {
        try {
            byte[] code = getByteCode();
            if (code != null) {
                ClassFileReader reader
                    = new ClassFileReader(code, NO_SOURCE_FILE); // not recording OT-attributes
                this._constantPoolOffsets = reader.getConstantPoolOffsets();
            }
        } catch (ClassFormatException ex) {
            throw new InternalCompilerError(ex.toString());
        }
    }

    // =================== structure related methods: =================
	/**
	 * Role name: for normal roles (with ifc part) strip of the __OT__.
	 * @return the name
	 */
	public char[] getName() {
		return this._binding != null ?
			this._binding.sourceName() :
		    this._ast.name;
	}

	/**
	 * Is this a real role from source code (not role nested)?
	 */
	public boolean isSourceRole() {
		return this._ast != null ? this._ast.isSourceRole() : this._binding.isSourceRole();
	}

	// can be role file and/or purely copied
	private int _extraRoleFlags = 0;
	public boolean isRoleFile() {
		if (this._ast == null)
			return (this._extraRoleFlags & WordValueAttribute.OT_CLASS_ROLE_FILE) != 0;
		return this._ast.isRoleFile();
	}

	public void setExtraRoleFlags(int flags) {
		this._extraRoleFlags = flags;
		// FIXME(SH): for role files also mark enclosing team (-> role files attribute)
	}

	public int getExtraRoleFlags() {
		return this._extraRoleFlags;
	}

	public boolean isLocalType() {
		if (this._binding != null)
			return this._binding.isLocalType();
		if (this._classBinding != null)
			return this._classBinding.isLocalType();
		if (this._ast != null && this._ast.scope != null)
			return (this._ast.scope.parent.kind == Scope.METHOD_SCOPE);
		return false;
	}

    public TypeDeclaration getInterfaceAst()
    {
    	if (this._interfacePart != null)
    		return this._interfacePart;
    	if (this._ast == null)
    		return null; // neither class nor ifc ast present, must be binary.
    	if (   this._interfaceBinding != null
    		&& !this._ast.isInterface())
    	{
    		RoleModel interfaceModel = this._interfaceBinding.roleModel;
    		if (interfaceModel != null && interfaceModel != this) // model sharing didn't work
    			return interfaceModel.getInterfaceAst();
    	}
    	if ((this._ast.bits & TypeDeclaration.IsLocalType) != 0)
    		return null;
    	assert this._ast.isInterface();
    	return this._ast;
    }

    public TypeDeclaration getClassPartAst()
    {
    	if (this._classPart != null)
        	return this._classPart;
    	if (this._ast != null && !this._ast.isInterface())
    		return this._classPart = this._ast;
    	if (this._classBinding != null) {
    		RoleModel classRole = this._classBinding.roleModel;
    		if (classRole != null)
    			return this._classPart = classRole._ast;
    	}
    	return null;
    }

    public ReferenceBinding getInterfacePartBinding()
    {
        if (this._interfaceBinding == null) {
            if (this._interfacePart != null)
                this._interfaceBinding = this._interfacePart.binding;
            else if (this._binding != null && this._binding.enclosingType() != null)
                this._interfaceBinding = this._binding.enclosingType().getMemberType(
                    this._binding.sourceName()); // chop off OT_DELIM

            // Sanity check:
            if (   this._interfaceBinding  != null && this._binding != null
            	&& this._interfaceBinding.isBinaryBinding() != this._binding.isBinaryBinding()) {
            	Scope scope = null;
            	if (!this._binding.isBinaryBinding())
            		scope = ((SourceTypeBinding)this._binding).scope;
            	else if (!this._interfaceBinding.isBinaryBinding())
            		scope = ((SourceTypeBinding)this._interfaceBinding).scope;
            	String message = "Mismatching binary/source class/interface parts: "+String.valueOf(this._binding.readableName())+" / "+String.valueOf(this._interfaceBinding.readableName()); //$NON-NLS-1$ //$NON-NLS-2$
            	if (scope != null) {
					scope.problemReporter().mismatchingRoleParts(this._interfaceBinding, scope.referenceType());
            	} else
            		throw new InternalCompilerError(message);
            }

        }
        return this._interfaceBinding;
    }

	public ReferenceBinding getClassPartBinding()
	{
		if (this._classBinding == null) {
			if (this._binding == null) return null; // assuming error
	        if (!this._binding.isInterface())
			    this._classBinding = this._binding;
	        else if (this._binding.isSynthInterface()) {
	        	if (this._classPart != null)
	        		this._classBinding = this._classPart.binding;
	        	if (this._classBinding == null)
	        		this._classBinding = this._binding.enclosingType().getMemberType(
	        				CharOperation.concat(IOTConstants.OT_DELIM_NAME, this._binding.internalName()));
	        }
		}
        return this._classBinding;
	}

    public void setTeamModel(TeamModel teamModel) {
        this._teamModel = teamModel;
    }

    public TeamModel getTeamModel() {
        if (this._teamModel == null) {
            if (this._binding != null)
            	this._teamModel = TeamModel.getEnclosingTeam(this._binding).getTeamModel();
            else // ROFI: fallback if team model is required before a binding is created:
            	this._teamModel = this._ast.enclosingType.getTeamModel();
        }
        return this._teamModel;
    }

    /**
     * Retrieve the (first) tsuper role.
     *
     * Note, that more tsuper roles may exist due to team nesting.
     * TODO (SH): need to check all callers, whether reading just the first
     * tsuper role is OK!
     *
     * @return first tsuper role or null
     */
    public ReferenceBinding getTSuperRoleBinding() {
		return this._tsuperRoleBindings[0];
	}

    public ReferenceBinding[] getTSuperRoleBindings() {
    	ReferenceBinding[] tsupers = new ReferenceBinding[this.numTSuperRoles];
    	System.arraycopy(this._tsuperRoleBindings, 0, tsupers, 0, this.numTSuperRoles);
    	return tsupers;
    }

    /** Does this role have any tsuper role, other than the predefined roles from Team? */
    public boolean hasRelevantTSuperRole() {
    	return    this.numTSuperRoles > 0
    	       && !TypeAnalyzer.isPredefinedRole(getBinding());
    }

	/**
	 * Is role some kind of tsuper role of current?
	 * It could be that any enclosing teams are role-and-tsuper-role.
	 * Then from there on inwards only role names are compared.
	 */
	public boolean hasTSuperRole(ReferenceBinding role) {
		ReferenceBinding otherClass = null, otherIfc = null;
		if(role.isInterface()) {
			otherIfc = role;
			otherClass = role.getRealClass();
		} else {
			otherIfc = role.getRealType();
			otherClass = role;
		}
		Dependencies.ensureRoleState(this, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY);
		for (int i = 0; i < this.numTSuperRoles; i++) {
			ReferenceBinding other = this._tsuperRoleBindings[i].isInterface() ? otherIfc : otherClass;
			if (this._tsuperRoleBindings[i] == other)
				return true;
			if (this._tsuperRoleBindings[i].roleModel.hasTSuperRole(other))
				return true;
		}
		ReferenceBinding outerRole = this._binding.enclosingType();
		if (outerRole.isRole()) {
			ReferenceBinding roleOuter = role.enclosingType();
			if (!roleOuter.isRole())
				return false;
			if (!outerRole.roleModel.hasTSuperRole(roleOuter))
				return false;
			return CharOperation.equals(role.internalName(), this._binding.internalName());
		}
		return false;
	}

	public void setBinding (ReferenceBinding binding) {
        this._binding = binding;
        binding.roleModel = this;
    }

    // -------------- local types -----------------

    /**
     * An binary local type was requested during resolve.
     * Maybe fix enclosingType and enter to _localTypes.
     *
     * @param local
     */
    public void addBinaryLocalType (ReferenceBinding local) {
		if (local instanceof BinaryTypeBinding) {
			((BinaryTypeBinding)local).setEnclosingOfRoleLocal(this._binding);
		}
		this._localTypes.add(local.roleModel);
    }

    /**
     * A local type was read from source code.
     * Record it as part (though not member) of this role.
     * @param local
     */
    public void addLocalType (char[] constantPoolName, RoleModel local) {
    	this._localTypes.add(local);
    	local._isLocalType = true;
    	if (constantPoolName != null) {
    		if (this._ast != null) {
    			Scope scope = this._ast.scope;
    			if (scope != null) {
    				CompilationUnitScope cuScope = scope.compilationUnitScope();
    				cuScope.registerLocalType(constantPoolName, local.getAst());
    			}
    		}
    	}
    	if (this._attributes != null)
    		for (AbstractAttribute attribute : this._attributes)
    			if (attribute.nameEquals(IOTConstants.ROLE_LOCAL_TYPES))
    				return;
    	addAttribute(new RoleLocalTypesAttribute(this));
    }

	public void maybeAddLocalToEnclosing() {
		ReferenceBinding enclosing= this._binding.enclosingType();
		if (enclosing != null && enclosing.isRole()) {
			enclosing.roleModel.addBinaryLocalType(this._binding);
		}
	}
    /**
     * Get source type bindings for all local types ready to
     * write the RoleLocalTypesAttribute.
     *
     * @return a new non-null array
     */
    public ReferenceBinding[] getLocalTypes() {
    	ReferenceBinding[] types = new ReferenceBinding[this._localTypes.size()];
    	int j = 0;
    	for (int i=0; i<types.length; i++) {
    		types[j] = this._localTypes.get(i).getBinding();
    		if (types[j] != null)
    			j++;
    	}
    	if (j != types.length)
        	System.arraycopy(types, 0,
        					 types = new ReferenceBinding[j], 0,
							 j);
   		return types;
    }

    /**
     * Get role models of all recorded local types of this role.
     * Resolve these types if needed.
     */
	public Iterator<RoleModel> localTypes() {
		return this._localTypes.iterator();
	}

	/**
	 * Find a type by  its compound name.
	 * First look in local types (including enclosing roles).
	 * Second use the environment for lookup.
	 * This method is needed since local types are not stored in the environment.
	 *
	 * @param environment
	 * @param compoundName
	 * @return type specified by compoundName
	 */
	public ReferenceBinding findType(LookupEnvironment environment, char[][] compoundName)
	{
		char[][] myName = CharOperation.splitOn('/', this._binding.constantPoolName());
		if (   this._binding.isLocalType()
			&& CharOperation.equals(compoundName, myName))
			return this._binding;
		Iterator<RoleModel> locals = localTypes();
		while (locals.hasNext()) {
			RoleModel role = locals.next();
			char[][] roleName = CharOperation.splitOn('/', role.getBinding().constantPoolName());
			if (CharOperation.equals(compoundName, roleName))
				return role.getBinding();
		}
		if (this._binding.enclosingType().isRole())
			return this._binding.enclosingType().roleModel.findType(environment, compoundName);
		return environment.getType(compoundName);
	}

	/**
	 * Similar to the above, but comparison is made be relative names, ie., team names
	 * as prefix are chopped of. Instead of an environment the scope of this role is used.
	 *
	 * @param compoundName
	 * @return type as specified by compoundName
	 */
	public ReferenceBinding findTypeRelative(char[] compoundName)
	{
		ReferenceBinding teamBinding = TeamModel.getEnclosingTeam(this._binding);
		if (this._binding.isLocalType())
		{
			if (TypeAnalyzer.equalRoleLocal(teamBinding, this._binding, compoundName))
				return this._binding;
		} else {
			if (CharOperation.equals(this._binding.internalName(), compoundName))
				return this._binding;
		}
		Iterator<RoleModel> locals = localTypes();
		while (locals.hasNext())
		{
			RoleModel role = locals.next();
			if (TypeAnalyzer.equalRoleLocal(teamBinding, role.getBinding(), compoundName))
				return role.getBinding();
		}
		if (this._binding.enclosingType().isRole())
			return this._binding.enclosingType().roleModel.findTypeRelative(compoundName);
		if (this._ast != null)
			return (ReferenceBinding)this._ast.scope.getType(compoundName);
		return null;
	}

	/**
     * @return is this role bound to a base type?
     */
	private boolean hasCheckedBaseclass= false; // caching the result the below method
	private boolean isBound; // --"--
    public boolean isBound() {
    	if (this.hasCheckedBaseclass)
    		return this.isBound;
    	this.hasCheckedBaseclass= true;
    	if (   this._ast != null
    		&& this._ast.baseclass != null)
    		return this.isBound= true;
        if (this._binding != null) {
            if (this._binding.rawBaseclass() != null)
            	return this.isBound= true;
            ReferenceBinding superRole = this._binding.superclass();
			if (   superRole != null
            	&& superRole.isRole()
            	&& superRole.roleModel.isBound())
				return this.isBound= true;
        }
        for (int i=0; i<this.numTSuperRoles; i++) {
        	if (this._tsuperRoleBindings[i].roleModel.isBound())
        		return this.isBound= true;
        }
        return this.isBound= false;
    }

    public boolean hasBaseclassProblem() {
    	ReferenceBinding binding = (this._classBinding != null) ? this._classBinding : this._binding;
    	if ((binding.tagBits & TagBits.BaseclassHasProblems) != 0)
    		return true;
    	if (binding.superclass() != null) {
    		ReferenceBinding superclass = binding.superclass();
    		if (   (   superclass.isRole()
    				&& superclass.isHierarchyInconsistent())
    			|| (   (superclass.roleModel != null)
    		        && (superclass.roleModel.hasBaseclassProblem())))
    		{
    			binding.tagBits |= TagBits.BaseclassHasProblems;
    			return true;
    		}
    	}
    	return false;
    }

    public static boolean isRoleWithBaseProblem(TypeDeclaration declaration) {
        if (!declaration.isSourceRole())
            return false;
        return declaration.getRoleModel().hasBaseclassProblem();
    }

    public ReferenceBinding getBaseTypeBinding() {
        return this._binding.baseclass();
    }

    public int getBaseTag() {
        return getTeamModel().getBaseTag(getBaseTypeBinding());
    }
    /**
     * Is current a direct supertype of model?
     * Considers superClass and superInterfaces
     */
    public boolean isSuperTypeOf(RoleModel model) {
        if (this._binding.isSuperclassOf(model.getBinding()))
            return true;
        ReferenceBinding[] superInterfaces =
        	(model.getInterfacePartBinding() != null) ?
                model.getInterfacePartBinding().superInterfaces():
        		model.getClassPartBinding().superInterfaces();
        if (superInterfaces != null)
        {
            for (int i=0; i<superInterfaces.length; i++) {
                if (superInterfaces[i] == this._binding)
                    return true;
            }
        }
        return false;
    }

	public RoleModel getExplicitSuperRole()
	{
		ReferenceBinding superClass = this._binding.superclass();
		if(superClass.superclass() == null)
		{
			//superClass is java.lang.Object
			return null;
		}
		return this._binding.superclass().roleModel;
	}

	public RoleModel getImplicitSuperRole()
	{
		if(getTSuperRoleBinding() == null)
		{
			return null;
		}
		return getTSuperRoleBinding().roleModel;
	}


	/**
	 * Give the most general role type on the path between this role
	 * and it's tsub version which is visible in 'scope'.
	 * @pre startRole.baseclass() != null
	 * @return non-null
	 */
	public static ReferenceBinding getTopmostBoundRole(BlockScope scope, ReferenceBinding startRole) {
		ReferenceBinding current = startRole;
		ReferenceBinding candidate = null;
		while (current != null) {
			if (!current.isRole())
				return candidate;
			if (current.baseclass() != null) // true at least in first iteration.
				candidate = current;
			else
				return candidate;
			current = current.roleModel.getTSuperRoleBinding();
		}
		return candidate;
	}

	/**
	 * Store the known sub roles (including this) as determined by RoleHierarchyAnalyzer.analyze().
	 *
	 * @param subRoles
	 */
	public void setSubRoles(RoleModel[] subRoles) {
		this._subRoles = subRoles;
	}

	/**
	 * Retrieve the known sub roles (including this) as determined by RoleHierarchyAnalyzer.analyze().
	 *
	 * @param subRoles
	 */
	public RoleModel[] getSubRoles() {
		return this._subRoles;
	}


    RoleModel getSuperIfcRole(int idx) {
        if (getInterfacePartBinding().superInterfaces() == null)
            return null;
        if (getInterfacePartBinding().superInterfaces().length <= idx)
            return null;
        ReferenceBinding ifc = getInterfacePartBinding().superInterfaces()[idx];
        if (ifc.isSourceRole())
            return ifc.roleModel;
        return null;
    }

    int getNumSuperIfc () {
        if (this._binding.superInterfaces() == null)
            return 0;
        return this._binding.superInterfaces().length;
    }

    public boolean isSynthInterface() {
        int AccSynthIfc = AccInterface|AccSynthetic;
        return (this._binding.modifiers & AccSynthIfc) == AccSynthIfc;
    }


    public boolean isRegularInterface() {
        return this._binding.isRegularInterface();
    }

    public boolean equals(RoleModel other) {
    	if (other == null) return false;
    	if (this._interfaceBinding != null && other._interfaceBinding != null)
    		return this._interfaceBinding == other._interfaceBinding;
    	if (this._classBinding != null && other._classBinding != null)
    		return this._classBinding == other._classBinding;
    	return this._ast == other._ast;
    }
    /**
     * Record the interface part of a copied role.
     * @param roleIfcs map of interface indexed by name (String)
     */
    public void recordIfcPart(HashMap<String,TypeDeclaration> roleIfcs) {
        if (this._interfacePart == null) {
        	// defensively search the interface part's name:
            String ifcName = null;
            if (this._binding != null)
            	ifcName = new String(this._binding.sourceName());
            else if (this._ast != null) {
            	if (RoleSplitter.isClassPartName(this._ast.name))
            		ifcName = new String(RoleSplitter.getInterfacePartName(this._ast.name));
            }
            // on incredibly broken code, ifcName may still be null (see TPX-423).
            if (ifcName != null) {
            	this._interfacePart = roleIfcs.get(ifcName);
            	checkClassAndIfcParts();
            } else {
            	assert this._ast == null || this._ast.ignoreFurtherInvestigation : "should only ever happen one erroneous code"; //$NON-NLS-1$
            }
        }

    }

    public void checkClassAndIfcParts() {
    	boolean hasError = false;
    	Scope scope = null;
    	char[] ifcName = null;
    	char[] className = null;
		if (this._interfacePart != null && this._classPart != null) {
			scope = this._classPart.scope;
			ifcName = this._interfacePart.name;
			className = this._classPart.name;
			if (this._interfacePart.enclosingType != this._classPart.enclosingType)
				hasError = true;
		}
		if (this._interfaceBinding != null && this._classBinding != null) {
			// binding names contain more information:
			ifcName = this._interfaceBinding.readableName();
			className = this._classBinding.readableName();
			if (this._interfaceBinding.enclosingType() != this._classBinding.enclosingType())
				hasError = true;
		}
		if (hasError) {
			if (scope == null)
				if (this._ast != null)
					scope = this._ast.scope;
			if (scope == null)
				throw new InternalCompilerError("Multiple errors processing role "+toString()); //$NON-NLS-1$
			scope.problemReporter().inconsistentlyResolvedRole(this._ast, ifcName, className);
		}
	}

	/**
     * Once team and tsuper role are known, record this information.
     */
    public void connect(TeamModel teamModel, ReferenceBinding tsuperRole) {
        setTeamModel(teamModel);
        boolean tsuperAlreadyPresent = false;
        for (int i = 0; i < this.numTSuperRoles; i++) {
			if (this._tsuperRoleBindings[i] == tsuperRole) {
				tsuperAlreadyPresent = true;
				break;
			}
		}
        if (!tsuperAlreadyPresent) {
	        if (this.numTSuperRoles == this._tsuperRoleBindings.length)
	        	System.arraycopy(
	        			this._tsuperRoleBindings, 0,
	        			this._tsuperRoleBindings = new ReferenceBinding[2*this.numTSuperRoles], 0,
						this.numTSuperRoles);
	        this._tsuperRoleBindings[this.numTSuperRoles++] = tsuperRole;
	        if (getAst() != null && getAst().isInterface())
	        	TypeLevel.addImplicitInheritance(getAst(), tsuperRole);
	        WordValueAttribute.addClassFlags(this, IOTConstants.OT_CLASS_FLAG_HAS_TSUPER);
	        if (this._binding != null)
	        	this._binding.modifiers |= AccOverriding;
        }
    	// invoked from copy role we have at least this state:
        this._state.inititalize(ITranslationStates.STATE_ROLES_SPLIT);
    }

    protected String getKindString() {
        return "Role"; //$NON-NLS-1$
    }

	/**
	 * @param method
	 * @param dstMethod
	 */
	public void addSyntheticMethodMapping(MethodBinding srcMethod, MethodBinding dstMethod) {
		this._syntheticMap.put(srcMethod, dstMethod);
	}

	public MethodBinding mapSyntheticMethod (MethodBinding srcMethod) {
		if (srcMethod instanceof SyntheticRoleFieldAccess) {
			// matching by name
			ReferenceBinding dstType = getBinding();
			if (dstType.isBinaryBinding())
				return null; // will be found within methods
			SyntheticMethodBinding[] synthetics = ((SourceTypeBinding)dstType).syntheticMethods();
			if (synthetics == null)
				return null;
			for (SyntheticMethodBinding methodBinding : synthetics) {
				if (CharOperation.equals(methodBinding.selector, srcMethod.selector))
					return methodBinding;
			}
			return null;
		} else {
			// matching by map
			return (MethodBinding)this._syntheticMap.get(srcMethod);
		}
	}

	/**
	 * @param field
	 * @param newField
	 */
	public void addSyntheticFieldMapping(FieldBinding srcField, FieldBinding newField) {
		this._syntheticMap.put(srcField, newField);
	}

	public FieldBinding mapSyntheticField(FieldBinding srcField) {
		return (FieldBinding)this._syntheticMap.get(srcField);
	}

	/**
     * Record that the given method is only accessible using decapsulation.
	 * @param binding
	 */
	public void addInaccessibleBaseMethod(MethodBinding binding) {
		// push out to the team to ensure early evaluation by the OTRE:
		OTSpecialAccessAttribute specialAccess = getTeamModel().getSpecialAccessAttribute();
		specialAccess.addDecapsulatedMethodAccess(this._binding.baseclass(), binding);
		specialAccess.addAdaptedBaseClass(binding.declaringClass);
	}

	/**
	 * Record that a given field is accessed using callout.
	 * @param field
	 * @param calloutModifier either TokenNameget or TokenNameset (from TerminalTokens).
	 * @return the target class into which the OTRE will insert the accessor method
	 */
	public ReferenceBinding addAccessedBaseField(FieldBinding field, int calloutModifier) {
		// find appropriate target class
		ReferenceBinding targetClass = field.declaringClass; // default: the class declaring the field (could be super of bound base)
		if (!field.isStatic() && (field.isProtected() || field.isPublic()))
			targetClass = getBaseTypeBinding();	// use the specific declared bound class (avoids weaving into possibly inaccessible super base)

		// push out to the team to ensure early evaluation by the OTRE:
		OTSpecialAccessAttribute specialAccess = getTeamModel().getSpecialAccessAttribute();
		specialAccess.addCalloutFieldAccess(field, targetClass, calloutModifier);
		specialAccess.addAdaptedBaseClass(field.declaringClass);
		return targetClass;
	}

	/**
	 * Record that a base call needs access to the base.super-method.
	 * @param baseMethod
	 */
	public void addMethodSuperAccess(MethodBinding baseMethod) {
		OTSpecialAccessAttribute specialAccess = getTeamModel().getSpecialAccessAttribute();
		specialAccess.addSuperMethodAccess(baseMethod);
	}

	/**
	 * Record that an inaccessible base is bound.
	 * @param baseclass
	 */
	public void markBaseClassDecapsulation(ReferenceBinding baseclass) {
		// push out to the team to ensure early evaluation by the OTRE:
		OTSpecialAccessAttribute specialAccess = getTeamModel().getSpecialAccessAttribute();
		specialAccess.addBaseClassDecapsulation(baseclass);
	}

	/** Hook into attribute writing in order to insert callout mappings attribute. */
    public int writeAttributes(ClassFile file) {
    	if (this._binding.callinCallouts != null) {
    		for (int i = 0; i < this._binding.callinCallouts.length; i++) {
				if (this._binding.callinCallouts[i].type != CallinCalloutBinding.CALLIN) // callout or -override
				{
					addAttribute(new CalloutMappingsAttribute(this));
					break;
				}
			}
    	}
    	return super.writeAttributes(file);
    }

	public CPTypeAnchorAttribute getTypeAnchors() {
		return this._binding.model.getTypeAnchors();
	}

	public void setErrorFlag(boolean flag) {
		if (this._ast != null)
			this._ast.ignoreFurtherInvestigation = flag;
		if (this._classPart != null)
			this._classPart.ignoreFurtherInvestigation = flag;
		if (this._interfacePart != null)
			this._interfacePart.ignoreFurtherInvestigation = flag;
	}

	public void recordCopiedFeature(Binding feature) {
		this._copiedFeatureBindings.add(feature);
	}
	public boolean hasAlreadyBeenCopied(Binding feature) {
		return this._copiedFeatureBindings.contains(feature);
	}

	/** The Attributename of this role's baseclass as it is used in bytecode attributes.
	 *  PRE: this is a bound role.
	 *  @param includeAnchor should anchored types be encoded in full?
	 */
	public char[] getBaseclassAttributename(boolean includeAnchor) {
		ReferenceBinding baseclass = getBinding().baseclass();
		char[] baseName = baseclass.getRealClass().attributeName();
		if (includeAnchor && RoleTypeBinding.isRoleWithExplicitAnchor(baseclass))
			return CharOperation.concat(baseName,
				   CharOperation.concat(SingleValueAttribute.ANCHOR_DELIM,
				   CharOperation.append(((RoleTypeBinding)baseclass)._teamAnchor.getBestName(), '>')));
		return baseName;
	}

	/* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel#cleanup()
     */
    public void cleanup()
    {
        super.cleanup();
        this._copiedFeatureBindings = null;
    }

	public RoleModel getBoundRootRole() {
		if (this._boundRootRole != null)
			return this._boundRootRole;
		if (this._binding.isInterface() && this._classBinding != null)
		{
			RoleModel classPart = this._classBinding.roleModel;
			return classPart.getBoundRootRole();
		}
		return null;
	}

	public static int getDeclaredModifiers(ReferenceBinding typeBinding) {
		if (!typeBinding.isRole() || typeBinding.roleModel == null)
			return typeBinding.modifiers;
		ReferenceBinding classPartBinding = typeBinding.roleModel.getClassPartBinding();
		if (classPartBinding != null)
			return classPartBinding.modifiers;
		return typeBinding.modifiers;
	}

	public static boolean isRoleFromOuterEnclosing(SourceTypeBinding sourceType,
												   ReferenceBinding baseclass)
	{
		ReferenceBinding enclosingTeam = sourceType.enclosingType();
		if (enclosingTeam == null || !enclosingTeam.isTeam())
			return false;
		if (enclosingTeam == baseclass.enclosingType())
			return false;
		while (true) {
			enclosingTeam = enclosingTeam.enclosingType();
			if (enclosingTeam == null || !enclosingTeam.isTeam())
				return false;
			if (enclosingTeam == baseclass.enclosingType())
				return true;
		}
	}

	public boolean hasCallins() {
		if (this._attributes == null)
			return false;
		for (AbstractAttribute attribute : this._attributes)
			if (attribute.nameEquals(IOTConstants.CALLIN_METHOD_MAPPINGS)) {
				// don't report if all callins are inherited:
				if (!((CallinMethodMappingsAttribute)attribute).isInherited())
					return true;
			}

		return false;
	}

	/**
	 * After attributes are evaluated, each role class must check, whether it is
	 * supposed to implement method mappings inherited from a super interface.
	 */
	public void implementMethodBindingsFromSuperinterfaces() {
		if (this._ast == null || this._ast.isInterface() || this._binding == null || this._binding.isLocalType())
			return;
		ReferenceBinding interfacePartBinding = getInterfacePartBinding();
		if (interfacePartBinding == null) // paranoia
			return;
		ReferenceBinding[] superInterfaces = interfacePartBinding.superInterfaces();
		if (superInterfaces == null) // paranoia (null seen in real life)
			return;
		for (ReferenceBinding superInterface : superInterfaces)
		{
			if (!superInterface.isRole())
				continue;
			if (superInterface.roleModel.getState() < ITranslationStates.STATE_LATE_ATTRIBUTES_EVALUATED-1)
				continue; // not yet prepared
			Dependencies.ensureBindingState(superInterface, ITranslationStates.STATE_LATE_ATTRIBUTES_EVALUATED);
			CallinCalloutBinding[] methodMappings = superInterface.callinCallouts;
			if (methodMappings != null)
				for (CallinCalloutBinding mapping : methodMappings)
					if (mapping.isValidBinding() && mapping.isCallout())
						new CalloutImplementor(this).generateFromBinding(mapping);
		}
	}

	public void releaseClassFile() {
		this._classFile= null;
	}

	/**
	 * Starting with this role look for subtypes that are bound, returning all top-bound roles.
	 * @return non-null
	 */
	public ReferenceBinding[] getBoundDescendants() {
		if (this._binding == null)
			return new ReferenceBinding[0];
		if (this._binding.baseclass() != null)
			return new ReferenceBinding[]{this._binding};

		ArrayList<ReferenceBinding> roles = new ArrayList<ReferenceBinding>();
		for (ReferenceBinding knownRole: getTeamModel().getKnownRoles()) {
			if (knownRole.baseclass() == null) 				continue; // not bound
			if (!knownRole.isCompatibleWith(this._binding)) continue; // not compatible
			if (knownRole.superclass().baseclass() != null) continue; // not top
			if (knownRole.isClass()) 						continue; // only record ifc to avoid dupes
			roles.add(knownRole);
		}
		return roles.toArray(new ReferenceBinding[roles.size()]);
	}

	/** recored here any unimplemented getBase method:
	 *    <_OT$AnyBase base R> _OT$getBase() { throw new AbstractMethodError(); }
	 */
	public MethodBinding unimplementedGetBase;
	/**
	 * If an unbound super role exists return its unimplemented getBase method.
	 */
	public MethodBinding getInheritedUnimplementedGetBase() {
		ReferenceBinding classPartBinding = getClassPartBinding();
		if (classPartBinding != null) {
			ReferenceBinding superclass = classPartBinding.superclass();
			while (superclass != null && superclass.isRole()) {
				RoleModel superRole = superclass.roleModel;
				if (superRole.unimplementedGetBase != null)
					return superRole.unimplementedGetBase;
				superclass = superclass.superclass();
			}
		}
		return null;
	}

}

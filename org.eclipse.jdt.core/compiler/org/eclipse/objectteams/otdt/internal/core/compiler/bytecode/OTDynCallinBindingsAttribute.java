/** 
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors: Stephan Herrmann - Initial API and implementation
 **********************************************************************/
 package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * Encodes all callin bindings of a team class.
 * Structure:
 * <pre> 
 * CallinBindings_attribute {
 *   u2 attribute_name_index;
 *   u4 attribute_length;
 *   u2 bindings_count;
 *   {
 *   	u2 role_class_name_index_table;		// sipmle name of the role type (without __OT__ prefix)
 *      u2 callin_name_index_table;
 *      u2 role_selector_name_index_table;
 *      u2 role_signature_name_index_table;
 *      u1 callin_flags;
 *      u2 callin_modifier_name_index_table;
 *      u2 base_class_index_table;			// internal name: java/util/Map$Entry
 *      u2 file_name_index_table; 	// for SMAP
 *      u2 line_number;				// for SMAP
 *      u2 line_offset; 			// for SMAP 
 *      u2 base_methods_count;
 *      {
 *         u2 base_method_name_index_table;
 *         u2 base_method_signature_index_table;
 *         u4 callin_id;
 *         u1 base_flags;
 *         u2 translation_flags;
 *      } base_methods[base_methods_count];
 *   } bindings[bindings_count];
 * }
 * </pre>
 * 
 * @author stephan
 * @since 1.3.0M3
 */
public class OTDynCallinBindingsAttribute extends ListValueAttribute {

	public static final char[] ATTRIBUTE_NAME = "OTDynCallinBindings".toCharArray(); //$NON-NLS-1$

	// bits in 'flags'
	public static final short STATIC_ROLE_METHOD = 1;
	public static final short INHERITED = 4;
	public static final short COVARIANT_BASE_RETURN = 8;
	
	private class Mapping {
		char[] roleClassName, callinName, roleSelector, roleSignature, callinModifier, baseClassName, fileName;
		int flags; // STATIC_ROLE_METHOD, INHERITED, COVARIANT_BASE_RETURN
		int lineNumber, lineOffset;
		BaseMethod[] baseMethods;
		public CallinCalloutBinding binding;
		Mapping(char[] roleClassName, char[] callinName, char[] roleSelector, char[] roleSignature, char[] callinModifer, int flags, char[] baseClassName, int baseMethodCount) 
		{
			this.roleClassName	= roleClassName;
			this.callinName		= callinName;
			this.roleSelector	= roleSelector;
			this.roleSignature 	= roleSignature;
			this.callinModifier = callinModifer;
			this.flags			= flags;
			this.baseClassName 	= baseClassName;
			this.baseMethods	= new BaseMethod[baseMethodCount];
		}
		void addBaseMethod(int i, char[] baseMethodName, char[] baseMethodSignature, int callinID, int baseFlags, int translationFlags) {
			this.baseMethods[i] = new BaseMethod(baseMethodName, baseMethodSignature, callinID, baseFlags, translationFlags);
		}
		public BaseMethod[] getBaseMethods() {
			return this.baseMethods;
		}
		public int getAttributeSize() {
			int s = 21; // 7 names (roleClassName callinName roleSelector roleSignature callinModifier baseClassName fileName)
						// + 1 byte (flags) 3 shorts (lineNumber lineOffset baseMethodCount)
			for (int i = 0; i < this.baseMethods.length; i++)
				s += 11; // 2 names, 1 int (callinID) 1 byte (baseFlags) 1 short (translationFlags)
			return s;
		}
		/**
		 * Compute from 'decl' and store info as needed for SMAP (JSR-045).
		 */
		void setSMAPinfo(CallinMappingDeclaration decl) {
			this.fileName = getFileName(decl);
			int[] lineEnds = decl.scope.referenceCompilationUnit().compilationResult().getLineSeparatorPositions();
			this.lineNumber =
				(short)Util.getLineNumber(decl.sourceStart, lineEnds, 0, lineEnds.length-1);
			short lineEnd =
				(short)Util.getLineNumber(decl.declarationSourceEnd, lineEnds, 0, lineEnds.length-1);
			this.lineOffset = (short)(lineEnd - this.lineNumber);
		}
		

	    /** Compute the name of the file containing the given callin mapping.
	     *  Do consider packages but no projects or source folders.
	     * @param decl
	     * @return
	     */
	    private char[] getFileName(CallinMappingDeclaration decl) {
			CompilationUnitDeclaration compilationUnit = decl.scope.referenceCompilationUnit();
			char[] fullName = compilationUnit.getFileName();
			char[][] packageName = null;
			if (   compilationUnit.currentPackage == null
				|| compilationUnit.currentPackage.tokens.length == 0)
			{ // default package, use last path component only
				int pos = CharOperation.lastIndexOf('/', fullName);
				if (pos == -1) // no '/'
					return fullName;
				return CharOperation.subarray(fullName, pos+1, -1);
			}
			packageName = compilationUnit.currentPackage.tokens;
			char[][] components = CharOperation.splitOn('/', fullName);

			// sometimes fullname (ie., compilationUnit.getFileName()) does not contain any path, just sourceunitname,
			// which is due to the many different contexts calling new CompilationResult(fileName..)
			int pos = CharOperation.lastIndexOf('/', fullName);
			if (pos == -1)
			{
			    return CharOperation.concatWith (packageName, fullName, '/');
			}

			//check whether components contains packageName:
			if (components.length <= packageName.length)
				throw new InternalCompilerError("too few path elements"); //$NON-NLS-1$
			int start = components.length - (packageName.length + 1);
			int end = components.length;
			if (!CharOperation.equals(packageName,
					CharOperation.subarray(components, start, end - 1)))
				decl.scope.problemReporter().packageIsNotExpectedPackage(compilationUnit);
			return CharOperation.concatWith(CharOperation.subarray(components, start, end), '/');
		}
		public void setSMAPInfo(char[] fileName, int lineNumber, int lineOffset) {
			this.fileName = fileName;
			this.lineNumber = lineNumber;
			this.lineOffset = lineOffset;
		}
	}
	
	/* Encodes the binding to one individual base method. */
	private class BaseMethod {
    	static final int CALLIN = 1;
    	static final int STATIC = 2;
		char[] baseMethodName, baseMethodSignature;
		int callinID, baseFlags, translationFlags;
		BaseMethod(char[] baseMethodName, char[] baseMethodSignature, int callinID, int baseFlags, int translationFlags) {
			this.baseMethodName 		= baseMethodName;
			this.baseMethodSignature 	= baseMethodSignature;
			this.callinID 				= callinID;
			this.baseFlags 				= baseFlags;
			this.translationFlags 		= translationFlags;
		}
	}
	
	private List<Mapping> mappings;
	private TeamModel theTeam;
	
	OTDynCallinBindingsAttribute(TeamModel theTeam) {
		super(ATTRIBUTE_NAME, -1/*size pending*/, -1/*variable entry size*/);
		this.theTeam = theTeam;
		this.theTeam.addAttribute(this);
		this.mappings = new ArrayList<Mapping>();
	}
	
    /**
     * Read the attribute from byte code.
     *
	 * @param info
	 * @param readOffset
	 * @param structOffset
	 * @param constantPoolOffsets
	 */
	public OTDynCallinBindingsAttribute(ClassFileStruct reader, int readOffset, int[] constantPoolOffsets) {
		super(ATTRIBUTE_NAME, -1/*size pending*/, -1/*variable entry size*/);
		this._reader = reader;
		this._readOffset = readOffset;
		this._constantPoolOffsets = constantPoolOffsets;
		this._count = consumeShort();
		this.mappings = new ArrayList<Mapping>();
		for (int i=0; i<this._count; i++)
			this.mappings.add(readMapping());
	}
	
	@Override
	protected int getAttributeSize() {
		int s = 2; // entry count
		for (Mapping mapping : this.mappings)
			s += mapping.getAttributeSize();
		return s;
	}
	void addMappings(char[] baseClassName, CallinMappingDeclaration callinDecl) {
		int flags = 0; 			
		if (callinDecl.roleMethodSpec.resolvedMethod.isStatic())
			flags |= STATIC_ROLE_METHOD;
		if (callinDecl.hasCovariantReturn())
			flags |= COVARIANT_BASE_RETURN;
		MethodSpec roleSpec = callinDecl.roleMethodSpec;
		MethodSpec[] baseMethodSpecs = callinDecl.getBaseMethodSpecs();
		Mapping mapping = new Mapping(callinDecl.scope.enclosingSourceType().sourceName(), // indeed: simple name
									  callinDecl.name, roleSpec.selector, roleSpec.signature(),
									  callinDecl.getCallinModifier(), flags, 
									  baseClassName, baseMethodSpecs.length);
		for (int i=0; i<baseMethodSpecs.length; i++) {
			MethodSpec baseSpec = baseMethodSpecs[i];
			int baseFlags = 0;
			if (baseSpec.isCallin())
				baseFlags |= BaseMethod.STATIC;
			if (baseSpec.isStatic())
				baseFlags |= BaseMethod.CALLIN;
			mapping.addBaseMethod(i, baseSpec.selector, baseSpec.signature(), baseSpec.getCallinId(theTeam), baseFlags, baseSpec.getTranslationFlags());
		}
		mapping.setSMAPinfo(callinDecl);
		this.mappings.add(mapping);
		this._count = this.mappings.size();
	}
	
	public static void createOrMerge(TeamModel theTeam, char[] baseClassName, CallinMappingDeclaration[] mappingDecls) {
		AbstractAttribute existingAttr = theTeam.getAttribute(ATTRIBUTE_NAME);
		OTDynCallinBindingsAttribute theAttr = existingAttr != null 
					? (OTDynCallinBindingsAttribute)existingAttr 
					: new OTDynCallinBindingsAttribute(theTeam);
		for (CallinMappingDeclaration callinDecl : mappingDecls)
			theAttr.addMappings(baseClassName, callinDecl);
	}

	String toString(int i) {
		Mapping mapping = this.mappings.get(i);
		StringBuffer buf = new StringBuffer();
		buf.append('\t');
		buf.append(String.valueOf(mapping.callinName));
		buf.append(": ");
		buf.append(String.valueOf(mapping.roleSelector));
		buf.append(String.valueOf(mapping.roleSignature));
		buf.append(" <- ");
		buf.append(String.valueOf(mapping.callinModifier));
		buf.append(' ');
		buf.append(String.valueOf(mapping.baseClassName));
		BaseMethod[] baseMethods = mapping.getBaseMethods();
		for (int j = 0; j < baseMethods.length; j++) {
			buf.append("\n\t\t");
			buf.append(String.valueOf(baseMethods[j].baseMethodName));
			buf.append(String.valueOf(baseMethods[j].baseMethodSignature));
			buf.append('{');
			buf.append(baseMethods[j].callinID);
			buf.append('}');
		}
		buf.append('\n');
		return buf.toString();
	}

	@Override
	public void write(ClassFile classFile) {
		super.write(classFile);
		// DEBUGGING:
		System.out.println("Wrote "+this);
	}
	
	void writeElementValue(int i) {
		Mapping mapping = this.mappings.get(i);
		writeName(mapping.roleClassName);
		writeName(mapping.callinName);
		writeName(mapping.roleSelector);
		writeName(mapping.roleSignature);
		writeName(mapping.callinModifier);
		writeByte((byte) mapping.flags);
		writeName(mapping.baseClassName);
		writeName(mapping.fileName);
		writeUnsignedShort(mapping.lineNumber);
		writeUnsignedShort(mapping.lineOffset);
		BaseMethod[] baseMethods = mapping.getBaseMethods();
		writeUnsignedShort(baseMethods.length);
		for (int j = 0; j < baseMethods.length; j++) {
			writeName(baseMethods[j].baseMethodName);
			writeName(baseMethods[j].baseMethodSignature);
			writeInt(baseMethods[j].callinID);
			writeByte((byte)baseMethods[j].baseFlags);
			writeUnsignedShort(baseMethods[j].translationFlags);
		}
	}
	
	Mapping readMapping() {
		char[] roleClassName	= consumeName();
		char[] callinName 		= consumeName();
		char[] roleSelector 	= consumeName();
		char[] roleSignature	= consumeName();
		char[] callinModifer	= consumeName();
		int flags				= consumeByte();
		char[] baseClassName	= consumeName();
		char[] fileName 		= consumeName();
		int	lineNumber			= consumeShort();
		int lineOffset			= consumeShort();
		int baseMethodCount 	= consumeShort();
		Mapping result = new Mapping(roleClassName, callinName, roleSelector, roleSignature, callinModifer, flags, baseClassName, baseMethodCount); 
		result.setSMAPInfo(fileName, lineNumber, lineOffset);
		for (int i=0; i<baseMethodCount; i++) {
			char[] baseMethodName 		= consumeName();
			char[] baseMethodSignature 	= consumeName();
			int callinID				= consumeInt();
			int baseFlags				= consumeByte();
			int translationFlags		= consumeShort();
			result.addBaseMethod(i, baseMethodName, baseMethodSignature, callinID, baseFlags, translationFlags);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
	 */
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		checkBindingMismatch(binding, ClassFileConstants.AccTeam);
		if (((ReferenceBinding)binding).isTeam())
			((ReferenceBinding)binding).getTeamModel().addAttribute(this);
	}
	
	// Evaluate CallinMethodMappingAttribute late, because we need our methods to be in place.
    public void evaluateLateAttribute(ReferenceBinding teamBinding, int state)
    {
    	if (state != ITranslationStates.STATE_FAULT_IN_TYPES)
    		return;
    	for (int i = 0; i < this.mappings.size(); i++)
			createBinding(teamBinding, this.mappings.get(i));
    }

    /**
	 * @param roleBinding
	 * @param mapping
	 * @throws InternalCompilerError
	 */
	private void createBinding(ReferenceBinding teamBinding, Mapping mapping)
	{
		ReferenceBinding roleBinding = teamBinding.getMemberType(this.mappings.get(0).roleClassName).getRealClass();
		CallinCalloutBinding result = null;
		CallinCalloutBinding[] callinCallouts = roleBinding.callinCallouts;
		if (callinCallouts != null) {
			for (int i = 0; i < callinCallouts.length; i++) {
				if (CharOperation.equals(mapping.callinName, callinCallouts[i].name))
				{
					// fill in details to existing binding:
					result = callinCallouts[i];
					result.callinModifier = encodeCallinModifier(mapping.callinModifier);
					break;
				}
			}
		}
		if (result == null)
			result = new CallinCalloutBinding(roleBinding,
											  mapping.callinName,
											  encodeCallinModifier(mapping.callinModifier));
		BaseMethod[] mappingBaseMethods = mapping.baseMethods;
		MethodBinding[] baseMethods = new MethodBinding[mappingBaseMethods.length];

		ReferenceBinding currentType = roleBinding;
		char[] roleSignature = mapping.roleSignature;
		if (result.callinModifier == TerminalTokens.TokenNamereplace) {
			// ignore generalized return by truncating the signature:
			int closePos = CharOperation.indexOf(')', roleSignature);
			if (closePos > -1)
				roleSignature = CharOperation.subarray(roleSignature, 0, closePos+1);
		}

		roleMethod:
		while (currentType != null) {
			MethodBinding[] methods = currentType.getMethods(mapping.roleSelector);
			for (int j = 0; j < methods.length; j++) {
				if (CharOperation.prefixEquals(roleSignature, methods[j].signature(true/*retrench*/)))
				{
					result._roleMethodBinding = methods[j];
					break roleMethod;
				}
			}
			currentType = currentType.superclass();
		}
		if (result._roleMethodBinding == null)
			throw new InternalCompilerError("role method specified in callin mapping does not exist "+mapping); //$NON-NLS-1$


		mappingBaseMethods:
		for (int i = 0; i < mappingBaseMethods.length; i++) {
			BaseMethod bm = mappingBaseMethods[i];
			currentType = roleBinding.baseclass();
			while (currentType != null) {
				MethodBinding[] methods = currentType.getMethods(bm.baseMethodName);
				for (int j = 0; j < methods.length; j++) {
					if (CharOperation.equals(bm.baseMethodSignature, methods[j].signature())) // TODO(SH): enhancing? / _isCallin?
					{
						baseMethods[i] = methods[j];
						continue mappingBaseMethods;
					}
				}
				currentType = currentType.superclass();
			}
			baseMethods[i]= new ProblemMethodBinding(bm.baseMethodName, null, roleBinding.baseclass(), ProblemReasons.NotFound);
		}
		result._baseMethods = baseMethods;
		mapping.binding = result;

		result.copyInheritanceSrc = findTSuperBinding(mapping.callinName, roleBinding);
		roleBinding.addCallinCallouts(new CallinCalloutBinding[]{result});
	}

	private CallinCalloutBinding findTSuperBinding(char[] name, ReferenceBinding roleType) {
		ReferenceBinding[] tsuperRoles = roleType.roleModel.getTSuperRoleBindings();
		for (ReferenceBinding tsuperRole : tsuperRoles) {
			if (tsuperRole.callinCallouts != null)
				for (CallinCalloutBinding mapping : tsuperRole.callinCallouts)
					if (CharOperation.equals(mapping.name, name))
						return mapping.copyInheritanceSrc != null ?
								mapping.copyInheritanceSrc :
									mapping;
		}
		return null;
	}

	private int encodeCallinModifier(char[] modifierName) {
    	if (CharOperation.equals(modifierName, IOTConstants.NAME_REPLACE))
    		return TerminalTokens.TokenNamereplace;
    	if (CharOperation.equals(modifierName, IOTConstants.NAME_AFTER))
    		return TerminalTokens.TokenNameafter;
    	if (CharOperation.equals(modifierName, IOTConstants.NAME_BEFORE))
    		return TerminalTokens.TokenNamebefore;
        throw new InternalCompilerError("invalid callin modifier in byte code"); //$NON-NLS-1$
    }

}

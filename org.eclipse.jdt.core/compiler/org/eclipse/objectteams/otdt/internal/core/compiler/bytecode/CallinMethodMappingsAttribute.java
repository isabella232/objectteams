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
 * $Id: CallinMethodMappingsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
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
import org.eclipse.objectteams.otdt.internal.core.compiler.model.ModelElement;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;

/**
 * Represents the "CallinMethodMappings" attribute
 *
 * Location:
 * A bound role class with a callin binding.
 *
 * Content:
 * A list of method bindings, for structure see inner classes Mapping and BaseMethod.
 *
 * Purpose:
 * The OTRE uses this attribute to determine
 * 		a) which callins (callin wrapper calls) have to be woven into which base methods
 *		b) which base method has to be called while generating a base-call in a callin role method
 *
 *
 * The value of this attribute is a nested list, therefor everything
 * is handcoded here.
 *
 * @author stephan
 */
public class CallinMethodMappingsAttribute extends AbstractAttribute {

	// bits in 'flags'
	public static final short STATIC_ROLE_METHOD = 1;
	public static final short INHERITED = 4;
	public static final short COVARIANT_BASE_RETURN = 8;

	public static final int CONSTANT_PART_LENGTH = 20;   // 3 short (linepos) + 7 names, : 1 filename, 1 name, 2 selectors, 2 signatures; 1 modifier
	public static final int BASE_METHOD_PART_LENGTH = 13; // 4 names (2 selectors, 2 signatures) + 1 + 4 bytes (flags)
    /**
     * Local structure to store values when read from byte code.
     */
    public class Mapping {
    	public CallinCalloutBinding _binding;
		// for JSR-045:
    	short _lineNumber, _lineOffset, _flags;
    	char[]
			 _fileName,
	    // generally used fields:
			 _mappingName, _roleSelector, _roleSignature, _liftMethodName, _liftMethodSignature, _callinModifier;

    	BaseMethod[] _baseMethods;
    	Mapping(char[] name, char[] selector, char[] signature, short flags, char[] liftMethodName, char[] liftMethodSignature, char[] modifier)
    	{
    		this._mappingName = name;
    		this._roleSelector = selector;
    		this._roleSignature = signature;
    		this._flags = flags;
    		this._liftMethodName = liftMethodName;
    		this._liftMethodSignature = liftMethodSignature;
    		this._callinModifier = modifier;
    	}

    	Mapping(char[] fileName, short lineNumber, short lineOffset,
    			char[] name, char[] selector, char[] signature, short flags, char[] liftMethodName, char[] liftMethodSignature, char[] modifier)
    	{
    		this(name, selector, signature, flags, liftMethodName, liftMethodSignature, modifier);
    		this._fileName = fileName;
			this._lineNumber = lineNumber;
			this._lineOffset = lineOffset;
    	}

    	int getSize() {
    		return
    			   CONSTANT_PART_LENGTH
			     + 2      // count
    		     +(this._baseMethods.length
    		       * BASE_METHOD_PART_LENGTH);
    	}
    	@SuppressWarnings("nls")
		public String toString() {
    		String result =
    			new String(this._mappingName)+":\n"+
    			new String(this._roleSelector) + new String(this._roleSignature)+
				" <- " + new String(this._callinModifier) + " ";
    		for (int i = 0; i < this._baseMethods.length; i++) {
				result += new String(this._baseMethods[i]._selector)+new String(this._baseMethods[i]._signature);
				if ((this._flags & COVARIANT_BASE_RETURN) != 0)
					result += "+";
			}
    		if (this._liftMethodName.length > 0)
    			result += "\nlift by: "+new String(this._liftMethodName)+new String(this._liftMethodSignature);
    		return result;
    	}

		/**
		 * Compute from 'decl' and store info as needed for SMAP (JSR-045).
		 */
		void setSMAPinfo(CallinMappingDeclaration decl) {
			this._fileName = getFileName(decl);
			int[] lineEnds = decl.scope.referenceCompilationUnit().compilationResult().getLineSeparatorPositions();
			this._lineNumber =
				(short)Util.getLineNumber(decl.sourceStart, lineEnds, 0, lineEnds.length-1);
			short lineEnd =
				(short)Util.getLineNumber(decl.declarationSourceEnd, lineEnds, 0, lineEnds.length-1);
			this._lineOffset = (short)(lineEnd - this._lineNumber);
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

    	// == below: support for copying wrappers for callin-to-private

	    public boolean roleMethodIsPrivate() {
	    	if (this._binding == null)
	    		return false;
	    	return this._binding._roleMethodBinding.isPrivate();
	    }

		public char[][] getWrapperNames() {
			if (this._baseMethods == null)
				return null;
			char[][] names = new char[this._baseMethods.length][];
			for (int i = 0; i < this._baseMethods.length; i++)
				names[i] = this._baseMethods[i]._wrapperName;
			return names;
		}

		public char[][] getWrapperSignatures() {
			if (this._baseMethods == null)
				return null;
			char[][] signatures = new char[this._baseMethods.length][];
			for (int i = 0; i < this._baseMethods.length; i++)
				signatures[i] = this._baseMethods[i]._wrapperSign;
			return signatures;
		}

		public Mapping cloneForSubrole() {
			Mapping other = new Mapping(this._fileName, this._lineNumber, this._lineOffset,
										this._mappingName, this._roleSelector, this._roleSignature,
										this._flags, this._liftMethodName, this._liftMethodSignature,
										this._callinModifier);
			other._flags |= INHERITED;
			other._baseMethods = this._baseMethods; // shared, not modified
			return other;
		}
    }
    /**
     * Local structure to store values when read from byte code.
     */
    private class BaseMethod {
    	static final int CALLIN = 1;
    	static final int STATIC = 2;
    	char[] _selector, _signature, _wrapperName, _wrapperSign;
    	boolean _isCallin, _isStatic;
    	int _translationFlags;
    	MethodSpec _methodSpec = null;
    	BaseMethod (char[] sel, char[] sig, char[] wrapName, char[] wrapSign, int flags, int translationFlags)
    	{
    		this(sel, sig, wrapName, wrapSign, (flags & CALLIN)!=0, (flags & STATIC)!=0, translationFlags);
    	}
    	BaseMethod (char[] sel, char[] sig, char[] wrapName, char[] wrapSign,
    				boolean isCallin, boolean isStatic, int translationFlags)
		{
    		this._selector = sel;
    		this._signature = sig;
    		this._wrapperName = wrapName;
    		this._wrapperSign = wrapSign;
    		this._isCallin = isCallin;
    		this._isStatic = isStatic;
    		this._translationFlags = translationFlags;
		}
		@SuppressWarnings("nls")
		public String toString() {
			return (this._isCallin ? "callin " : "") + (this._isStatic ? "static " : "") + new String(this._selector)+new String(this._signature);
		}
		public void fetchTranslationFlags() {
			this._translationFlags = this._methodSpec.getTranslationFlags();
			this._methodSpec = null;
		}

    }
    public Mapping[] _mappings; // this holds the data read from byte code
    private int _size;           // attribute size in bytes

    /**
     * @param decls the mapping declarations to encode and store in this attribute.
     */
    public CallinMethodMappingsAttribute(CallinMappingDeclaration[] decls)
    {
        super(IOTConstants.CALLIN_METHOD_MAPPINGS);

        // lengths/counts:
        this._size = 0;
        int len = decls.length;
        // subtract errors:
        for (int i = 0; i < decls.length; i++) {
			if (decls[i].ignoreFurtherInvestigation)
				len--;
		}

        this._mappings = new Mapping[len];
        int idx = 0;
        for (int i = 0; i < decls.length; i++) {
			CallinMappingDeclaration decl = decls[i];
			if (decl.ignoreFurtherInvestigation)
				continue;

			this._size += CONSTANT_PART_LENGTH;

			short flags = 0;
			if (decl.roleMethodSpec.resolvedMethod.isStatic())
				flags |= STATIC_ROLE_METHOD;
			if (decl.hasCovariantReturn())
				flags |= COVARIANT_BASE_RETURN;


			Mapping currentMapping =
				  this._mappings[idx++] = new Mapping(
										decl.name,
										decl.roleMethodSpec.selector,
										decl.roleMethodSpec.signature(),
										flags,
							            decl.liftMethod != null ?
								            	decl.liftMethod.selector : new char[0],
										decl.liftMethod != null ?
								            	decl.liftMethod.signature() : new char[0],
							            decl.getCallinModifier());
			currentMapping.setSMAPinfo(decl);

			MethodSpec[] baseMethods = decl.baseMethodSpecs;
			currentMapping._baseMethods = new BaseMethod[baseMethods.length];
			this._size += 2; // 1 count
			for (int j = 0; j < baseMethods.length; j++) {
				MethodSpec bm = baseMethods[j];
				MethodDeclaration wrapper = decl.getWrapper(bm);

				this._size += BASE_METHOD_PART_LENGTH;
				currentMapping._baseMethods[j] = new BaseMethod(
						bm.selector,
						bm.resolvedMethod.signature(),
						wrapper.selector,
						wrapper.binding.signature(),
						bm.isCallin(),
						bm.resolvedMethod.isStatic(),
						bm.getTranslationFlags());
				// callins with param mappings compute the need for translation only during resolve()
				// (see PotentialLiftExpression.rememberMethodSpec().)
				if (decl.mappings != null)
					currentMapping._baseMethods[j]._methodSpec = bm;
			}
		}
    }

    /**
     * Read the attribute from byte code.
     *
	 * @param info
	 * @param readOffset
	 * @param structOffset
	 * @param constantPoolOffsets
	 */
	public CallinMethodMappingsAttribute(ClassFileStruct reader, int readOffset, int[] constantPoolOffsets) {
		super(IOTConstants.CALLIN_METHOD_MAPPINGS);
		this._reader = reader;
		this._readOffset = readOffset;
		this._constantPoolOffsets = constantPoolOffsets;

		this._size = 0;
		int numMappings = consumeShort();
		this._mappings = new Mapping[numMappings];
		for (int i=0; i<numMappings; i++)
			this._mappings[i] = readMapping();
	}

	public void merge(ModelElement model, AbstractAttribute other)
	{
		assert other instanceof CallinMethodMappingsAttribute;
		assert model instanceof TypeModel;
		ReferenceBinding typeBinding = ((TypeModel)model).getBinding();

		CallinMethodMappingsAttribute otherCMMA = (CallinMethodMappingsAttribute)other;
		HashMap<String,Mapping> set = new HashMap<String,Mapping>();
		LinkedList<CallinCalloutBinding> newBindings = new LinkedList<CallinCalloutBinding>();
		for (int i = 0; i < this._mappings.length; i++) {
			set.put(new String(this._mappings[i]._mappingName), this._mappings[i]);
		}
		for (int i = 0; i < otherCMMA._mappings.length; i++) {
			Mapping mapping = otherCMMA._mappings[i];
			if (set.containsKey(new String(mapping._mappingName))) {
				continue;
			}
			set.put(new String(mapping._mappingName), mapping.cloneForSubrole());
			this._size += mapping.getSize();

			newBindings.add(createBinding(typeBinding, mapping));
		}

		// store combined array:
		Collection<Mapping> values = set.values();
		this._mappings = new Mapping[values.size()];
		values.toArray(this._mappings);

		// add new mappings to the binding:
		if (newBindings.size() > 0) {
			CallinCalloutBinding[] callins = new CallinCalloutBinding[newBindings.size()];
			newBindings.toArray(callins);
			typeBinding.addCallinCallouts(callins);
		}
	}
	/**
	 * Read one method mapping
	 */
	private Mapping readMapping() {
		this._size += CONSTANT_PART_LENGTH;
		Mapping map = new Mapping(
				consumeName(), (short)consumeShort(), (short)consumeShort(),
				consumeName(), consumeName(), consumeName(), (short)consumeShort(),
				consumeName(), consumeName(), consumeName());
		this._size += 2;
		int numBaseMethods = consumeShort();
		map._baseMethods = new BaseMethod[numBaseMethods];
		for (int i=0; i<numBaseMethods; i++) {
			map._baseMethods[i] = readBaseMethod();
		}
		return map;
	}

	/**
	 * Read data for one bound base method.
	 */
	private BaseMethod readBaseMethod() {
		this._size += BASE_METHOD_PART_LENGTH;
		return new BaseMethod(consumeName(),
							  consumeName(),
							  consumeName(),
							  consumeName(),
							  consumeByte(),
							  consumeInt());
	}

	/* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#write(org.eclipse.jdt.internal.compiler.ClassFile)
     */
    public void write(ClassFile classFile) {
        super.write(classFile);

       	writeValues(classFile);

       	writeBack(classFile);
    }

    /**
     * Write the contents from values stored in _mappings
	 * @param classFile
	 */
	private void writeValues(ClassFile classFile) {
        if (this._contentsOffset + 8 + this._size >= this._contents.length) { // 8: name(2) + size(4) + elementCount(2)
        	this._contents = classFile.getResizedContents(8 + this._size);
        }
        writeName         (this._name);
        writeInt          (this._size + 2);  // + elementCount
        writeUnsignedShort(this._mappings.length);
        for (int i=0; i<this._mappings.length; i++) {
            Mapping map = this._mappings[i];
            writeName(map._fileName);
            writeUnsignedShort(map._lineNumber);
            writeUnsignedShort(map._lineOffset);
            writeName(map._mappingName);
            writeName(map._roleSelector);
            writeName(map._roleSignature);
            writeUnsignedShort(map._flags);
            writeName(map._liftMethodName);
            writeName(map._liftMethodSignature);
            writeName(map._callinModifier);
            writeBaseMethods(this._mappings[i]._baseMethods);
        }
	}


    private void writeBaseMethods(BaseMethod[] methods) {
    	writeUnsignedShort(methods.length);
    	for (int i = 0; i < methods.length; i++) {
			writeName(methods[i]._selector);
			writeName(methods[i]._signature);
			writeName(methods[i]._wrapperName);
			writeName(methods[i]._wrapperSign);
			int flags = 0;
			if (methods[i]._isCallin) flags |= BaseMethod.CALLIN;
			if (methods[i]._isStatic) flags |= BaseMethod.STATIC;
			writeByte((byte)flags);
			if (methods[i]._methodSpec != null)
				methods[i].fetchTranslationFlags();
			writeInt(methods[i]._translationFlags);
		}
    }
	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
	 */
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		checkBindingMismatch(binding, ExtraCompilerModifiers.AccRole);
		if (((ReferenceBinding)binding).isRole())
			((ReferenceBinding)binding).roleModel.addAttribute(this);
	}
	// Evaluate CallinMethodMappingAttribute late, because we need our methods to be in place.
    public void evaluateLateAttribute(ReferenceBinding roleBinding, int state)
    {
    	if (state != ITranslationStates.STATE_FAULT_IN_TYPES)
    		return;
    	CallinCalloutBinding[] callins = new CallinCalloutBinding[this._mappings.length];
    	for (int i = 0; i < this._mappings.length; i++) {
			callins[i] = createBinding(roleBinding, this._mappings[i]);
		}
    	if (callins.length > 0)
    		roleBinding.addCallinCallouts(callins);
    }

    /**
	 * @param roleBinding
	 * @param mapping
	 * @throws InternalCompilerError
	 */
	private CallinCalloutBinding createBinding(ReferenceBinding roleBinding, Mapping mapping)
	{
		CallinCalloutBinding result = null;
		CallinCalloutBinding[] callinCallouts = roleBinding.callinCallouts;
		if (callinCallouts != null) {
			for (int i = 0; i < callinCallouts.length; i++) {
				if (CharOperation.equals(mapping._mappingName, callinCallouts[i].name))
				{
					// fill in details to existing binding:
					result = callinCallouts[i];
					result.callinModifier = encodeCallinModifier(mapping._callinModifier);
					break;
				}
			}
		}
		if (result == null)
			result = new CallinCalloutBinding(roleBinding,
											  mapping._mappingName,
											  encodeCallinModifier(mapping._callinModifier));
		BaseMethod[] mappingBaseMethods = mapping._baseMethods;
		MethodBinding[] baseMethods = new MethodBinding[mappingBaseMethods.length];

		ReferenceBinding currentType = roleBinding;
		char[] roleSignature = mapping._roleSignature;
		if (result.callinModifier == TerminalTokens.TokenNamereplace) {
			// ignore generalized return by truncating the signature:
			int closePos = CharOperation.indexOf(')', roleSignature);
			if (closePos > -1)
				roleSignature = CharOperation.subarray(roleSignature, 0, closePos+1);
		}

		roleMethod:
		while (currentType != null) {
			ReferenceBinding currentType2 = currentType;
			while (currentType2 != null) {
				MethodBinding[] methods = currentType2.getMethods(mapping._roleSelector);
				for (int j = 0; j < methods.length; j++) {
					if (CharOperation.prefixEquals(roleSignature, methods[j].signature(true/*retrench*/)))
					{
						result._roleMethodBinding = methods[j];
						break roleMethod;
					}
				}
				currentType2 = currentType2.superclass();
			}
			currentType = currentType.enclosingType();
		}
		if (result._roleMethodBinding == null)
			throw new InternalCompilerError("role method specified in callin mapping does not exist "+mapping); //$NON-NLS-1$


		mappingBaseMethods:
		for (int i = 0; i < mappingBaseMethods.length; i++) {
			BaseMethod bm = mappingBaseMethods[i];
			currentType = roleBinding.baseclass();
			while (currentType != null) {
				MethodBinding[] methods = currentType.getMethods(bm._selector);
				for (int j = 0; j < methods.length; j++) {
					if (CharOperation.equals(bm._signature, methods[j].signature())) // TODO(SH): enhancing? / _isCallin?
					{
						baseMethods[i] = methods[j];
						continue mappingBaseMethods;
					}
				}
				currentType = currentType.superclass();
			}
			baseMethods[i]= new ProblemMethodBinding(bm._selector, null, roleBinding.baseclass(), ProblemReasons.NotFound);
		}
		result._baseMethods = baseMethods;
		mapping._binding = result;

		result.copyInheritanceSrc = findTSuperBinding(mapping._mappingName, roleBinding);
		return result;
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


	// ==== public accessors for use by org.eclipse.jdt.internal.core.ClassFileInfo ====
    public int getLength() {
    	return this._mappings.length;
    }
    public char[] getCallinNameAt(int i) {
    	return this._mappings[i]._mappingName;
    }
    public String getRoleMethodNameAt(int i) {
    	return new String(this._mappings[i]._roleSelector);
    }
    public String getRoleMethodSignatureAt(int i) {
    	return new String(this._mappings[i]._roleSignature);
    }
    public String[] getBaseMethodNamesAt(int i) {
    	BaseMethod[] baseMethods = this._mappings[i]._baseMethods;
		String[] names = new String[baseMethods.length];
    	for (int j = 0; j < baseMethods.length; j++) {
			names[j] = new String(baseMethods[j]._selector);
		}
    	return names;
    }
    public String[] getBaseMethodSignaturesAt(int i) {
    	BaseMethod[] baseMethods = this._mappings[i]._baseMethods;
		String[] signatures = new String[baseMethods.length];
    	for (int j = 0; j < baseMethods.length; j++) {
			signatures[j] = new String(baseMethods[j]._signature);
		}
    	return signatures;
    }

	/**
	 * translate modifier name of the ith mapping to token (TerminalTokens.TokenNameXXX).
	 */
	public int getCallinModifierAt(int i) {
		char[] modifierName = this._mappings[i]._callinModifier;
		if (CharOperation.equals(modifierName, IOTConstants.NAME_BEFORE))
			return TerminalTokens.TokenNamebefore;
		if (CharOperation.equals(modifierName, IOTConstants.NAME_REPLACE))
			return TerminalTokens.TokenNamereplace;
		if (CharOperation.equals(modifierName, IOTConstants.NAME_AFTER))
			return TerminalTokens.TokenNameafter;
		return -1;
	}

	/** Does mapping `i' declare covariant return types ('+')? */
	public boolean getCovariantReturnAt(int i) {
		return (this._mappings[i]._flags & COVARIANT_BASE_RETURN) != 0;
	}

	public String toString() {
		String result = new String(this._name);
		for (int i = 0; i < this._mappings.length; i++) {
			result += "\n"+this._mappings[i]; //$NON-NLS-1$
		}
		return result;
	}

	public boolean isInherited() {
		for (Mapping mapping : this._mappings) {
			if ((mapping._flags & INHERITED) == 0)
				return false;
		}
		return true; // only if all mappings are inherited.
	}
}

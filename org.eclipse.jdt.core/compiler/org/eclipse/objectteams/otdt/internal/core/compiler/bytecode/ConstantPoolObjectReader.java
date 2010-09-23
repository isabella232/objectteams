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
 * $Id: ConstantPoolObjectReader.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
/**
 * ObjectTeams Eclipse source extensions
 * More information available at www.ObjectTeams.org
 *
 * @author Markus Witte
 *
 * @date 27.09.2003
 */
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import java.util.Iterator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SignatureWrapper;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticBaseCallSurrogate;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticRoleFieldAccess;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;

/**
 * Reads BinaryTypeBinding ConstantPool Entrys
 * and returns the Binding/Object at the specific ConstantPoolOffset
 *
 * @author Markus Witte
 * @version $Id: ConstantPoolObjectReader.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ConstantPoolObjectReader extends ClassFileStruct implements ClassFileConstants
{

	class IncompatibleBytecodeException extends RuntimeException {
		// make the compiler happy:
		private static final long serialVersionUID = -7100113603999284971L;
		char[] _offendingName;
		int _problemId;
		IncompatibleBytecodeException(char[]offendingName, int problemId) {
			this._offendingName = offendingName;
			this._problemId = problemId;
		}
	}

	private LookupEnvironment _environment;
	private TypeModel _srcModel;

	/**
	 * @param srcRole the source role being copied from
	 * @param reference the ConstantPool Bytecode which should be read
	 * @param environment
	 */
	public ConstantPoolObjectReader(RoleModel srcRole, byte[] reference, LookupEnvironment environment)
	{
		super(reference, srcRole.getConstantPoolOffsets(), 0);
		this._srcModel = srcRole;
        this._environment = environment;
	}

	public ConstantPoolObjectReader(MethodModel model, TypeModel srcModel, LookupEnvironment environment)
	{
		super(model.getBytes(), model.getConstantPoolOffsets(), 0);
		this._srcModel = srcModel;
		this._environment = environment;
	}
	public ConstantPoolObjectReader(byte[] bytes, int[] constantPoolOffsets, TypeModel srcModel, LookupEnvironment environment) {
		super(bytes, constantPoolOffsets, 0);
		this._srcModel = srcModel;
		this._environment = environment;
	}

	/**
	 * The Type of an ConstantPool entry e.g. ClassTag or StringTag ...
	 * @param index the index to an entry of constantPoolOffsets[]
	 * @return Type
	 */
	private int getConstantPoolEntryType(int index){
		int offset = this.constantPoolOffsets[index];
		return u1At(offset);
	}

	/**
	 * @param index the index to an entry of constantPoolOffsets[]
	 * @return the start position of the constantPool entry
	 */
	private int getConstantPoolStartPosition(int index){
		int start = this.constantPoolOffsets[index];
		return start;
	}

	public ConstantPoolObject readConstantPoolObject(int ref, int length){
		// TODO (SH): check reference length (usually 1).
		int type = getConstantPoolEntryType(ref);
		switch(type){
			case StringTag : 				return new ConstantPoolObject(type, getString(ref), ref);
			case IntegerTag : 				return new ConstantPoolObject(type, getInteger(ref));
			case FloatTag : 				return new ConstantPoolObject(type, getFloat(ref));
			case LongTag : 					return new ConstantPoolObject(type, getLong(ref));
			case DoubleTag : 				return new ConstantPoolObject(type, getDouble(ref));
			case ClassTag : 				return new ConstantPoolObject(type, decodeClassEntry(ref));
			case FieldRefTag : 				return new ConstantPoolObject(type, getFieldRef(ref));
			case MethodRefTag :	 			return new ConstantPoolObject(type, getMethodRef(ref));
			case InterfaceMethodRefTag : 	return new ConstantPoolObject(type, getInterfaceMethodRef(ref));
			case Utf8Tag : 				  	return new ConstantPoolObject(type, getUtf8(ref));
			//case NameAndTypeTag : 		//...
			default:
				throw new RuntimeException();
		}
	}

    public TypeBinding getSignatureBinding(int ref, boolean useGenerics)
    {
        char[] typeName = getUtf8(ref);
        if (useGenerics)
        	return this._environment.getTypeFromTypeSignature(
        				new SignatureWrapper(typeName), Binding.NO_TYPE_VARIABLES, this._srcModel.getBinding(), null); // no missing type info available
        else
        	return this._environment.getTypeFromSignature(
        				typeName, 0, typeName.length-1, false/*GENERIC*/, this._srcModel.getBinding(), null); // no missing type info available
    }

	private int getInteger(int index){
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==IntegerTag);
		return i4At(start+1);
	}

	private long getLong(int index){
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==LongTag);
		return i8At(start+1);
	}

	private float getFloat(int index){
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==FloatTag);
		return floatAt(start+1);
	}

	private double getDouble(int index){
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==DoubleTag);
		return doubleAt(start+1);
	}

	char[] getUtf8(int index){
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==Utf8Tag);
		return utf8At(start + 3, u2At(start + 1));
	}

	private String getString(int index){
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==StringTag);
		return new String(getUtf8(u2At(start+1)));
	}

	/** retrieve a class or array binding from the constant pool. */
	private TypeBinding decodeClassEntry(int index) {
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==ClassTag);
		int name_index = u2At(start+1);
		char[] name_str = getUtf8(name_index);

		// first check scalar class type:
		int dims=0;
		while (name_str[dims] == '[') dims++;
		if (dims == 0) {
			// no '[' prefix -> plain class name, must not have 'L' prefix nor ';' suffix:
			if (name_str[0] == 'L' || name_str[name_str.length-1] == ';')
				throw new IncompatibleBytecodeException(name_str, 0);
			return getClassBinding(index);
		}

		// follows: decoding of array, leaf component type needs 'L' for reference bindings:
		TypeBinding typeBinding = this._environment.getTypeFromSignature(name_str, 0, -1, false, this._srcModel.getBinding(), null);
		if (!typeBinding.leafComponentType().isBaseType()) {
			ReferenceBinding referenceBinding = (ReferenceBinding)typeBinding.leafComponentType();
			if (referenceBinding instanceof UnresolvedReferenceBinding) {
				ReferenceBinding localType = findLocalType(CharOperation.subarray(name_str, dims, -1));
				if (localType != null)
					referenceBinding = localType;
				else
					referenceBinding = (ReferenceBinding)BinaryTypeBinding.resolveType(referenceBinding, this._environment, false);
			}
			// check whether this type is actually an anchored type:
			CPTypeAnchorAttribute typeAnchors = this._srcModel.getTypeAnchors();
			if (typeAnchors != null) {
				char[] anchorPath = typeAnchors.getPath(index);
				if (anchorPath != null) {
					// create a RTB using a dummy field:
					ReferenceBinding teamType = referenceBinding.enclosingType();
					FieldBinding anchor = new FieldBinding(anchorPath, teamType, AccFinal, this._srcModel.getBinding(), Constant.NotAConstant);
					return anchor.getRoleTypeBinding(referenceBinding, dims);
				}
			}
		}
		return typeBinding;
	}

	private MethodBinding getMethodRef(int index){
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==MethodRefTag);
		int class_index = u2At(start+1);
		int name_index = u2At(start+3);
		ReferenceBinding class_rb = getClassBinding(class_index);
		// deactivated, see below. ReferenceBinding actualReceiver = class_rb;
		if(class_rb==null)
			return null;

		char[][] nameandtype = getNameAndType(name_index);
		char[] name = nameandtype[0];
		char[] type = nameandtype[1];
		MethodBinding mb = findMethodBinding(class_rb,name,type);

		// Note: donot revert to actual receiver, because the linkage of
		//       copyInheritanceSrc will otherwise be broken!
		if (mb == null && CharOperation.endsWith(name, IOTConstants._OT_TAG)) {
			// This method is faked within the compiler, will be added by the OTRE.
			return new MethodBinding(ClassFileConstants.AccPublic, name, TypeBinding.SHORT, TypeBinding.NO_PARAMETERS, TypeBinding.NO_EXCEPTIONS, class_rb);
		}
		assert(mb != null);
		return mb;

	}

	private FieldBinding getFieldRef(int index){
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==FieldRefTag);
		int class_index = u2At(start+1);
		int name_index = u2At(start+3);
		ReferenceBinding class_rb = getClassBinding(class_index);
		ReferenceBinding actualReceiver = class_rb;
		if(class_rb==null)
			return null;

		char[][] nameandtype = getNameAndType(name_index);
		char[] name = nameandtype[0];
		char[] type = nameandtype[1];
		FieldBinding fb = null;
		if (!class_rb.isBinaryBinding()) {
			SourceTypeBinding sourceType = (SourceTypeBinding)class_rb.erasure();
			// can't find synthetics in 'fields'.
			if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX, name))
				return sourceType.getSyntheticOuterLocal(name);
			if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_CLASS, name))
				return sourceType.getSyntheticClassLiteral(name);
		} // FIXME(SH): else read from RoleModel??

		do {
			fb = findFieldBinding(class_rb, name, type);
			if (fb != null) {
 				if (actualReceiver != class_rb)
					// return sourceType.getUpdatedFieldBinding(fb, actualReceiver);
 					// no sourceType available so directly create the updated binding:
 					return new FieldBinding(fb, actualReceiver);
				return fb;
			}
			class_rb = class_rb.superclass();
		} while (!CharOperation.equals(class_rb.constantPoolName(), ConstantPool.JavaLangObjectConstantPoolName));
		return fb;
	}

	private MethodBinding getInterfaceMethodRef(int index)
	{
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==InterfaceMethodRefTag);
		int class_index = u2At(start+1);
		int name_index = u2At(start+3);
		ReferenceBinding class_rb = getClassBinding(class_index);
		if(class_rb==null)
			return null;

		char[][] nameandtype = getNameAndType(name_index);
		char[] name = nameandtype[0];
		char[] type = nameandtype[1];
		MethodBinding mb = findMethodBinding(class_rb, name, type);
		assert(mb != null);
		if (mb.declaringClass != class_rb) {
			mb = new MethodBinding(mb, class_rb);
		}
		return mb;
	}

	//char[0][] name
	//char[1][] descriptor
	private char[][] getNameAndType(int index){
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==NameAndTypeTag);
		int name_index 			=  u2At(start+1);
		int descriptor_index 	=  u2At(start+3);
		char[] name 		= getUtf8(name_index);
		char[] descriptor = getUtf8(descriptor_index);
		char[][] result = {name, descriptor};
		return result;
	}

//*************************************************
	// as used to retrieve the declaring class of various entries
	// and for class entries without 'L' and ';'
	private ReferenceBinding getClassBinding(int index) {
		int start = getConstantPoolStartPosition(index);
		assert(u1At(start)==ClassTag);
		int name_index = u2At(start+1);
		char[] name_str = getUtf8(name_index);
		ReferenceBinding referenceBinding = this._environment.getTypeFromConstantPoolName(name_str, 0, -1, false, null);
		if (referenceBinding instanceof UnresolvedReferenceBinding) {
			ReferenceBinding localType = findLocalType(name_str);
			if (localType != null)
				return localType;
			referenceBinding = (ReferenceBinding)BinaryTypeBinding.resolveType(referenceBinding, this._environment, false);
		}
		// check whether this type is actually an anchored type:
		CPTypeAnchorAttribute typeAnchors = this._srcModel.getTypeAnchors();
		if (typeAnchors != null) {
			char[] anchorPath = typeAnchors.getPath(index);
			if (anchorPath != null) {
				// create a RTB using a dummy field:
				ReferenceBinding teamType = referenceBinding.enclosingType();
				FieldBinding anchor = new FieldBinding(anchorPath, teamType, AccFinal, this._srcModel.getBinding(), Constant.NotAConstant);
				referenceBinding = (ReferenceBinding) anchor.getRoleTypeBinding(referenceBinding, 0);
			}
		}
		return referenceBinding;
	}

	private ReferenceBinding findLocalType(char[] name_str) {
		if (this._srcModel.getBinding().isLocalType()) {
			if (CharOperation.equals(this._srcModel.getBinding().constantPoolName(), name_str))
				return this._srcModel.getBinding();
		}
		if (this._srcModel instanceof RoleModel) {
			Iterator<RoleModel> localTypes = ((RoleModel)this._srcModel).localTypes();
			while (localTypes.hasNext()) {
				RoleModel local = localTypes.next();
				if (CharOperation.equals(local.getBinding().constantPoolName(), name_str)) {
					return local.getBinding();
				}
			}
		}
		return null;
  	}

	public MethodBinding findMethodBinding(ReferenceBinding class_rb, char[] name, char[] descriptor){
		boolean isDecapsWrapper = false;
		if (CharOperation.prefixEquals(IOTConstants.OT_DECAPS, name)) {
			// accessors for decapsulation: strip prefix and prepend it after we have found the method:
			isDecapsWrapper = true;
			name = CharOperation.subarray(name, IOTConstants.OT_DECAPS.length, -1);
		}
		MethodBinding foundMethod = doFindMethodBinding(class_rb, name, descriptor);
		if (foundMethod != null && isDecapsWrapper) {
			foundMethod = new MethodBinding(foundMethod, class_rb); // OTRE adds the accessor to the exact base class, even if method is inherited
			foundMethod.selector = CharOperation.concat(IOTConstants.OT_DECAPS, name);
		}
		return foundMethod;
	}
	private MethodBinding doFindMethodBinding(ReferenceBinding class_rb, char[] name, char[] descriptor){
		MethodBinding[] mbs = class_rb.getMethods(name);

		if(mbs != Binding.NO_METHODS) {
			for (int i = 0; i < mbs.length; i++) {
				MethodBinding binding = mbs[i];
				if(binding!=null){
					if(isEqual(binding, name,descriptor))
						return binding;
				}
			}
			// TODO(SH): currently this may happen, if a role file is not recompiled which
			// required e.g., a getter access$n, while a setter access$n is being generated.
			if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, name))
				throw new InternalCompilerError("synthetic method "+new String(name)+" has unexpected signature"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (   CharOperation.prefixEquals(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, name)
			|| SyntheticBaseCallSurrogate.isBaseCallSurrogateName(name)) 
		{
			// for normal access methods class_rb should not be a BinaryTypeBinding, 
			// because in that case the above loop should have found the method
			// (access$n are stored like normal methods).
			if (class_rb.isBinaryBinding()) {
				if (SyntheticBaseCallSurrogate.isBaseCallSurrogateName(name)) { // surrogate might be inherited
					while ((class_rb = class_rb.superclass()) != null) {
						MethodBinding candidate = doFindMethodBinding(class_rb, name, descriptor);
						if (candidate != null)
							return candidate;
					}
				}
				// TODO(SH): but when T has been compiled only with T.R1 while T.R2
				//           requires a synth.method, than this method will be missing!
				return null;
			}
			SourceTypeBinding stb = (SourceTypeBinding)class_rb.erasure();
			SyntheticMethodBinding[] accessMethods = stb.syntheticMethods();
			if (accessMethods != null) {
				for (int i=0; i<accessMethods.length; i++) {
					if (CharOperation.equals(accessMethods[i].selector, name))
						return accessMethods[i];
				}
			}
		}
		if (SyntheticRoleFieldAccess.isRoleFieldAccess(AccSynthetic, name)) {
			if (class_rb.isBinaryBinding())
				return null; // should have been found within methods
			SourceTypeBinding sourceType = (SourceTypeBinding)class_rb.erasure();
			SyntheticMethodBinding[] synthetics = sourceType.syntheticMethods();
			if (synthetics == null)
				return null;
			for (SyntheticMethodBinding methodBinding : synthetics) {
				if (CharOperation.equals(methodBinding.selector, name))
					return methodBinding;
			}
		}
		if (isFakedOTREMethod(name))
		{
			// These methods will be generated by the OTRE,
			// may safely be faked during compilation:
			MethodBinding fakedMethod = createMethodFromSignature(
											class_rb,
											AccPublic|AccStatic,
											name,
											descriptor);
			class_rb.addMethod(fakedMethod);
			return fakedMethod;
		}
		// since Eclipse 3.0 and for JDK >= 1.2 the declaring class is changed to the
		// declared receiver (see SourceTypeBinding.getUpdatedMethodBinding()).
		// need to search super class/interfaces to really find the method.
		ReferenceBinding currentType = class_rb.superclass();
		if (currentType != null) {
			MethodBinding mb = findMethodBinding(currentType, name, descriptor);
			if (mb != null)
				return mb;
		}
		ReferenceBinding[] superIfcs = class_rb.superInterfaces();
		if (superIfcs != null) {
			for (int i = 0; i < class_rb.superInterfaces().length; i++) {
				MethodBinding mb = findMethodBinding(superIfcs[i], name, descriptor);
				if (mb != null) {
					if (!class_rb.isInterface())
						return new MethodBinding(mb, class_rb); // need a class method!
					return mb;
				}
			}
		}
		return null;
	}

	/** Detect methods that will be generated by the OTRE. */
	private boolean isFakedOTREMethod(char[] name) {
		if (!CharOperation.prefixEquals(IOTConstants.OT_DOLLAR_NAME, name))
			return false;
		if (CharOperation.prefixEquals(IOTConstants.OT_GETFIELD, name))
			return true;
		if (CharOperation.prefixEquals(IOTConstants.OT_SETFIELD, name))
			return true;
		if (CharOperation.prefixEquals(IOTConstants.ADD_ROLE, name))
			return true;
		return false;
	}

	/** Stolen mainly from BinaryTypeBinding.createMethod. */
	private MethodBinding createMethodFromSignature(
								ReferenceBinding declaringClass,
								int methodModifiers,
								char[] methodSelector,
								char[] methodSignature)
	{
		int numOfParams = 0;
		char nextChar;
		int index = 0;   // first character is always '(' so skip it
		while ((nextChar = methodSignature[++index]) != ')') {
			if (nextChar != '[') {
				numOfParams++;
				if (nextChar == 'L')
					while ((nextChar = methodSignature[++index]) != ';'){/*empty*/}
			}
		}

		// Ignore synthetic argument for member types.
		int startIndex = 0;
		TypeBinding[] parameters = Binding.NO_PARAMETERS;
		int size = numOfParams - startIndex;
		if (size > 0) {
			parameters = new TypeBinding[size];
			index = 1;
			int end = 0;   // first character is always '(' so skip it
			for (int i = 0; i < numOfParams; i++) {
				while ((nextChar = methodSignature[++end]) == '['){/*empty*/}
				if (nextChar == 'L')
					while ((nextChar = methodSignature[++end]) != ';'){/*empty*/}

				if (i >= startIndex)   // skip the synthetic arg if necessary
					parameters[i - startIndex] = this._environment.getTypeFromSignature(methodSignature, index, end, false/*GENERIC*/, this._srcModel.getBinding(), null); // no missing type info available
				index = end + 1;
			}
		}
		return new MethodBinding(
				methodModifiers,
				methodSelector,
				this._environment.getTypeFromSignature(methodSignature, index + 1, -1, false/*GENERIC*/, this._srcModel.getBinding(), null),   // index is currently pointing at the ')'
				parameters,
				Binding.NO_EXCEPTIONS,
				declaringClass);
	}

	private FieldBinding findFieldBinding(TypeBinding class_tb, char[] name, char[] descriptor)
	{
		if(class_tb instanceof ReferenceBinding)
		{
			ReferenceBinding class_rb = (ReferenceBinding) class_tb;
	        return findFieldByName(class_rb, name);
		}
		else if(class_tb instanceof ArrayBinding)
		{
			if(!CharOperation.equals(name, TypeConstants.LENGTH))
			{
				assert(false);
			}
			return ArrayBinding.ArrayLength;
		}
		else
		{
			//no fields on basetypes
			assert(false);
			return null;
		}
	}

	public FieldBinding findFieldByName(ReferenceBinding clazz, char[] name)
	{
		char[] prefix = TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX;
		if (CharOperation.prefixEquals(prefix, name))
		{
			if (clazz instanceof NestedTypeBinding)
			{
		        if(   clazz.isMemberType()
		           || clazz.isLocalType())
		        {
                    NestedTypeBinding ntb = (NestedTypeBinding)clazz;
                    SyntheticArgumentBinding[] sab = ntb.syntheticEnclosingInstances();
                    for (int i=0; i<sab.length; i++) {
                        if (CharOperation.equals(name, sab[i].name))
                                return sab[i].matchingField;
                    }
		        }
			}
			// no name adjustment or synthetics needed at the reading (source) side.
		}
		// either regular field or synthetic in a BinaryTypeBinding:
		return clazz.getField(name, true);
	}

	public static FieldBinding findFieldByBinding(ReferenceBinding clazz, FieldBinding refField)
	{
		char[] thisPrefix = TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX;
		char[] classPrefix = TypeConstants.SYNTHETIC_CLASS;
		char[] name = refField.name;
		if (CharOperation.prefixEquals(thisPrefix, name))
		{
            int distance = refField.declaringClass.depth()
							- ((ReferenceBinding)refField.type).depth();
			if (clazz instanceof NestedTypeBinding)
			{
		        {
		            NestedTypeBinding mtb = (NestedTypeBinding)clazz;
		            ReferenceBinding dstFieldType = clazz;
		            while (distance-- > 0) {
		            	dstFieldType = dstFieldType.enclosingType();
		            }
		            return mtb.addSyntheticFieldForInnerclass(dstFieldType);
		        }
			} else { // BinaryTypeBinding
				int depth = clazz.depth() - distance;
				name = CharOperation.concat(
							thisPrefix,
							String.valueOf(depth).toCharArray());
			}
		} else if (CharOperation.prefixEquals(classPrefix, name))
		{
			// assume the synthetic field is present, just create a new binding.
			return new FieldBinding(refField.name, refField.type, 0, clazz, null);
		}
		// directly find field in clazz or superclass(es)
		ReferenceBinding current = clazz;
		while (current != null) {
			FieldBinding f = current.getField(name, true);
			if (f != null) return f;
			current = current.superclass();
		}
		return null;
	}

	/**
	 * compare the MethodBinding with an MethodRef entry from ConstantPool
	 * @param binding the binding wich is to be comared with the ConstantPool-Entry
	 * @param name the ConstantPool name of a Method
	 * @param descriptor the ConstantPool signature of a Method
	 * @return true if binding and signature means the same Method
	 */
	private boolean isEqual(MethodBinding binding, char[] name, char[] descriptor){
		if(new String(binding.selector).compareTo(new String(name))!=0)
					return false;

		char[] signature = binding.signature(); // erasure
		if (CharOperation.equals(signature, descriptor))
			return true;

		if (binding.isConstructor()) {

			// chop off synthetic enclosing instance argument:

			// 1. try to chop off enclosing team only:
			int separatorPosMethod     = CharOperation.indexOf('$', signature);
			int separatorPosDescriptor = CharOperation.indexOf('$', descriptor);

			// 2. chop off full arg until ';'
			if (separatorPosMethod == -1 && separatorPosDescriptor == -1) {
				separatorPosMethod     = CharOperation.indexOf(';', signature);
				separatorPosDescriptor = CharOperation.indexOf(';', descriptor);
			}
			if (separatorPosMethod > 0 && separatorPosDescriptor > 0) {
				TypeBinding type1 = this._environment.getTypeFromSignature(signature, 1, separatorPosMethod, false/*GENERIC*/, this._srcModel.getBinding(), null);
				TypeBinding type2 = this._environment.getTypeFromSignature(descriptor, 1, separatorPosDescriptor, false/*GENERIC*/, this._srcModel.getBinding(), null);
				if (!type1.isTeam() || !type2.isTeam())
					return false;
				if (!type1.isCompatibleWith(type2))
					return false;

				signature  = CharOperation.subarray(signature, separatorPosMethod, -1);
				descriptor = CharOperation.subarray(descriptor, separatorPosDescriptor, -1);
			}
		}
		return false;
	}

	/** Answer an iterator over all strings and integers in the constant pool,
	 *  which are addressed with a single byte offset
	 *  (non-wide indices, requiring ldc, not ldcw).
	 * @param nConstants number of constants in the src constant pool.
	 */
	public Iterator<ConstantPoolObject> getNonWideConstantIterator(final int nConstants) {
		return new Iterator<ConstantPoolObject> () {
			int cur = Math.min(256, nConstants);
			public boolean hasNext() {
				while (--this.cur >= 0) {
					int entryType = getConstantPoolEntryType(this.cur);
					switch (entryType) {
					case StringTag:
					case IntegerTag:
					case FloatTag:
					case ClassTag:
						return true;
					// note: No need to handle long,
					//       which is stored using ldc2_w of which no non-wide version exists
					//       No need to handle char which is inlined using bipush or sipush
					}
				}
				return false;
			}
			public ConstantPoolObject next() {
				return readConstantPoolObject(this.cur, 2);
			}
			public void remove() {
				throw new InternalCompilerError("method not meant to be invoked."); //$NON-NLS-1$
			}
		};
	}
}
//Markus Witte}



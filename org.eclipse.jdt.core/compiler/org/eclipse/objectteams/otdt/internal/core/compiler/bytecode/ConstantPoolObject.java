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
 * $Id: ConstantPoolObject.java 23416 2010-02-03 19:59:31Z stephan $
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
 * @date 28.09.2003
 */
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel.ProblemDetail;

/**
 * MIGRATION_STATE: complete
 *
 * @author Markus Witte
 * @version $Id: ConstantPoolObject.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ConstantPoolObject implements ClassFileConstants {

	private int _type;

	private TypeBinding _typeBinding;
	private MethodBinding _methodBinding;
	private FieldBinding _fieldBinding;

	private String _string;
	private char[] _char;
	private int 	_int;
	private long 	_long;
	private float 	_float;
	private double 	_double;

	// needed to determine whether a string is referenced by a byte or wide index:
	private int     _index;

	public int getType(){
		return this._type;
	}

	public boolean isMethod() {
		return this._type == MethodRefTag || this._type == InterfaceMethodRefTag;
	}
	public boolean isField() {
		return this._type == FieldRefTag;
	}
	public boolean isClass() {
		return this._type == ClassTag;
	}
	public boolean isSpecificType(char[][] compoundName) {
		if (this._type != ClassTag)
			return false;
		ReferenceBinding rb = (ReferenceBinding) this._typeBinding;
		return CharOperation.equals(compoundName, rb.compoundName);
	}
	/** Is this a string with a low index (suitable for LDC as opposed to LDCW)? */
	public boolean isNonWideString () {
		return this._type == StringTag && this._index < 256;
	}

	public ConstantPoolObject(int type, String string, int index) {
		this._type=type;
		this._string=string;
		this._index=index;
	}

	public ConstantPoolObject(int type, char[] _char){
		this._type=type;
		this._char=_char;
	}

	public ConstantPoolObject(int type, int _int){
		this._type=type;
		this._int=_int;
	}

	public ConstantPoolObject(int type, float _float){
		this._type=type;
		this._float=_float;
	}

	public ConstantPoolObject(int type, long _long){
		this._type=type;
		this._long=_long;
	}

	public ConstantPoolObject(int type, double _double){
		this._type=type;
		this._double=_double;
	}

	public ConstantPoolObject(int type, TypeBinding _binding){
		this._type=type;
		this._typeBinding=_binding;
		assert(this._typeBinding!=null);
	}

	public ConstantPoolObject(int type, MethodBinding _binding){
		this._type=type;
		this._methodBinding=_binding;
		assert(this._methodBinding!=null);
	}


	public ConstantPoolObject(int type, FieldBinding _binding){
		this._type=type;
		this._fieldBinding=_binding;
		assert(this._fieldBinding!=null);
	}

	public int getInteger(){
		assert(this._type==IntegerTag);
		return this._int;
	}

	public long getLong(){
		assert(this._type==LongTag);
		return this._long;
	}

	public float getFloat(){
		assert(this._type==FloatTag);
		return this._float;
	}

	public double getDouble(){
		assert(this._type==DoubleTag);
		return this._double;
	}

	public char[] getUtf8(){
		assert(this._type==Utf8Tag);
		return this._char;
	}

	public String getString(){
		assert(this._type==StringTag);
		return this._string;
	}

	public TypeBinding getClassObject(){
		assert(this._type==ClassTag);
		return this._typeBinding;
	}

	public MethodBinding getMethodRef(){
		assert(this._type==MethodRefTag || this._type==InterfaceMethodRefTag);
		return this._methodBinding;
	}

	public FieldBinding getFieldRef(){
		assert(this._type==FieldRefTag);
		return this._fieldBinding;
	}

	public MethodBinding getInterfaceMethodRef(){
		assert(this._type==InterfaceMethodRefTag);
		return this._methodBinding;
	}

	public void setMethod (MethodBinding newMeth) {
		assert (this._type==MethodRefTag);
		this._methodBinding = newMeth;
	}

	public boolean isIllegallyCopiedCtor() {
		assert (this._type==MethodRefTag || this._type==InterfaceMethodRefTag);
		if (!this._methodBinding.isConstructor())
			return false;
		if (this._methodBinding.model == null)
			return false;
		return this._methodBinding.model.problemDetail == ProblemDetail.IllegalDefaultCtor;
	}
	@SuppressWarnings("nls")
	public String toString(){
		String str="";
		switch(getType()){
			case	IntegerTag: str = new String("Integer:")+new Integer(this._int).toString() +"\n"; break;
			case	LongTag: str = new String("Long:")+new Long(this._long).toString() +"\n"; break;
			case	FloatTag: str = new String("Float:")+new Float(this._float).toString() +"\n"; break;
			case	DoubleTag: str = new String("Double:")+new Double(this._double).toString() +"\n"; break;
			case	Utf8Tag: str = new String("Utf8:")+new String(this._char) +"\n"; break;
			case	StringTag: str = new String("String:")+this._string +"\n"; break;
			case	ClassTag: str = new String("TypeBinding:")+this._typeBinding.toString() +"\n"; break;
			case	MethodRefTag: str = new String("MethodBinding:")+this._methodBinding.toString() +" \n=>"+((this._methodBinding.declaringClass!=null)?this._methodBinding.declaringClass.toString():"")+"\n"; break;
			case	FieldRefTag: str = new String("FieldBinding:")+this._fieldBinding.toString()+" \n=>"+((this._fieldBinding.declaringClass!=null)?this._fieldBinding.declaringClass.toString():"")+"\n"; break;
			case	InterfaceMethodRefTag: str = new String("Interface:MethodBinding:")+" \n=>"+((this._methodBinding.declaringClass!=null)?this._methodBinding.declaringClass.toString():"")+"\n"; break;
		}
		return str;
	}
}
//Markus Witte}
/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: WeakenedTypeBinding.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;


/**
 * NEW for OTDT.
 *
 * This class models a type binding for which declaration and byte-code view differ.
 * Weakening is used to make the JVM happy when overriding methods with role-signatures.
 * Type-checking should mostly use the declared type.
 * Byte code will be generated using the weakened type.
 *
 * @author stephan
 * @version $Id: WeakenedTypeBinding.java 23417 2010-02-03 20:13:55Z stephan $
 * @since OTDT 0.9.2
 */
public class WeakenedTypeBinding extends DependentTypeBinding {

	/** The version that goes into the byte-code. */
	public final ReferenceBinding weakenedType;

	public WeakenedTypeBinding(DependentTypeBinding declaredType, ReferenceBinding weakenedType, LookupEnvironment environment)
	{
		super(declaredType, declaredType.arguments, declaredType.enclosingType(), environment);
		this.weakenedType = weakenedType;
		this.roleModel = declaredType.roleModel;
		if (this.type instanceof WeakenedTypeBinding)
			this.type = ((WeakenedTypeBinding)this.type).type;
		initializeDependentType(declaredType._teamAnchor, declaredType._valueParamPosition);
	}

	/** Factory method. */
	public static TypeBinding makeWeakenedTypeBinding(DependentTypeBinding declaredType, ReferenceBinding weakenedType, int dimensions)
	{
		if (declaredType instanceof WeakenedTypeBinding) {
			if (((WeakenedTypeBinding)declaredType).contains(weakenedType))
				return declaredType;
			declaredType = ((WeakenedTypeBinding)declaredType).getStrongType();
		}
		if (weakenedType instanceof WeakenedTypeBinding) {
			WeakenedTypeBinding weakenedTypeBinding = (WeakenedTypeBinding)weakenedType;
			weakenedType = weakenedTypeBinding.weakenedType;
		}
		WeakenedTypeBinding leafType = new WeakenedTypeBinding(declaredType, weakenedType, declaredType.environment);
		if (dimensions == 0)
			return leafType;
		return declaredType.environment.createArrayType(leafType, dimensions);
	}

	boolean contains(TypeBinding other) {
		if (!(other instanceof ReferenceBinding))
			return false;
		TypeBinding otherStrong = other;
		TypeBinding otherWeak   = other;
		if (other instanceof WeakenedTypeBinding) {
			otherWeak   = ((WeakenedTypeBinding) other).weakenedType;
			otherStrong = ((WeakenedTypeBinding) other).getStrongType();
		}
		return   this.type.isCompatibleWith(otherStrong)
			  && otherWeak.isCompatibleWith(this.weakenedType);
	}
	
	// ----- BYTE CODE VIEW -----
	@Override
	public char[] constantPoolName() {
		return this.weakenedType.constantPoolName();
	}

	@Override
	public TypeBinding erasure() {
		return this.weakenedType.erasure();
	}

	// ----- SOURCE CODE / TYPE CHECKING VIEW.
	@Override
	protected void registerAnchor() {
		// don't registered lightweight weakened types.
	}

	@Override
	public TypeBinding maybeInstantiate(ITeamAnchor anchor, int dimensions) {
		return ((DependentTypeBinding)this.type).maybeInstantiate(anchor, dimensions);
	}

	@Override
	public boolean isCompatibleWith(TypeBinding otherType) {
		return this.type.isCompatibleWith(otherType);
	}

    /** Forward to either part: */
	@Override
    public boolean isProvablyDistinct(TypeBinding otherType) {
    	if (super.isProvablyDistinct(otherType))
    		return true;
    	return this.weakenedType.isProvablyDistinct(otherType);
    }

	public DependentTypeBinding getStrongType () {
		return (DependentTypeBinding) this.type; // cast is safe by construction in constructor
	}

	public static ReferenceBinding getBytecodeType(TypeBinding returnType) {
		if (returnType instanceof WeakenedTypeBinding)
			// recursivly unpack all contained WTB, too:
			return getBytecodeType(((WeakenedTypeBinding)returnType).weakenedType);
		return (ReferenceBinding)returnType;
	}

	public boolean isSignificantlyWeakened() {
		return this.weakenedType != this.type.getRealType();
	}
	
	/**
     * Weakening implies that the interfaces are used to ensure compatibility,
     * so never use the signature of a class part.
	 */
	@Override
	public char[] signature() {
		if (this.signature != null)
			return this.signature;

		return this.signature = this.weakenedType.signature();
	}

	public static boolean requireWeakening(DependentTypeBinding strongType, ReferenceBinding weakType) {
		if (strongType == weakType)
			return false;
		if (strongType instanceof WeakenedTypeBinding)
			return ((WeakenedTypeBinding)strongType).weakenedType == weakType;
		return false;
	}

}

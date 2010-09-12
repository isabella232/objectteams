/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: FieldModel.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.model;

import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.OT_GETFIELD;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.OT_SETFIELD;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.AccSynthIfc;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.ANCHOR_USAGE_RANKS;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AnchorUsageRanksAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.WordValueAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel.FakeKind;


/**
 * Store additional information for a field.
 * + Modifiers (in the case of static final in a synthetic role interface).
 *
 * @author stephan
 * @version $Id: FieldModel.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class FieldModel extends ModelElement {
    public static FieldModel getModel(FieldDeclaration decl) {
        FieldModel model = decl.model;
        if (model == null) {
        	if (decl.binding != null && decl.binding.model != null)
        		model = decl.binding.model;
        	else
        		model = new FieldModel(decl);
        }
        return model;
    }
    public static FieldModel getModel(FieldBinding binding) {
    	FieldModel model = binding.model;
    	if (model == null)
    		model = new FieldModel(binding);
    	return model;
    }

    private FieldDeclaration _decl = null;
    private FieldBinding _binding = null;

    /** A flat encoding of a type anchor for this fields declared type .*/
    public char[] typeAnchor = null;
    public boolean hasResolveAnchorStarted = false;

    /** Flag for transfering a bit from generated AST to binding. */
    public boolean _clearPrivateModifier = false;

    /** Faked _OT$base fields store here, in which super-role a real base field can be found. */
	public ReferenceBinding actualDeclaringClass;

	// store inferred callouts to avoid duplicate generation: // FIXME(SH): should be per adapting role
	public CalloutMappingDeclaration _setterCallout= null;
	public CalloutMappingDeclaration _getterCallout= null;

	private MethodBinding _decapsulatingGetter = null;
	private MethodBinding _decapsulatingSetter = null;

    private FieldModel(FieldDeclaration decl) {
        this._decl    = decl;
        decl.model = this;
    }
    private FieldModel(FieldBinding binding) {
    	this._binding = binding;
    	binding.model = this;
    }
	public void setBinding(FieldBinding fieldBinding) {
		if (this._binding != null)
			assert this._binding == fieldBinding;
		this._binding = fieldBinding;
		fieldBinding.model = this;

		if (this._clearPrivateModifier)
			fieldBinding.tagBits |= TagBits.ClearPrivateModifier;

	}
	public FieldDeclaration getAST() {
		return this._decl;
	}
	/**
	 * Return the class that really (in the Java-view) declares the given field,
	 * considering whether f is a faked covariant _OT$base field.
	 */
	public static ReferenceBinding getActualDeclaringClass(FieldBinding f) {
		if ((f.tagBits & TagBits.IsFakedField) != 0)
			if (f.model != null)
				return f.model.actualDeclaringClass;
		return f.declaringClass;
	}

	/** Given that this is the model of a fake strong base field, return the original field from the super role. */
	public FieldBinding getOriginalFromFake() {
		ReferenceBinding superRole = this.actualDeclaringClass;
		FieldBinding fieldBinding = superRole.getField(this._binding.name, true);
		if (fieldBinding == null)
			throw new InternalCompilerError("Expected base field not found in super Role "+new String(superRole.readableName())); //$NON-NLS-1$
		return fieldBinding;
	}

	/** After inserting a field into a role interface create an attribute to store its source modifiers. */
	public static boolean checkCreateModifiersAttribute(TypeDeclaration type, FieldDeclaration field)
	{
		if ((type.modifiers & AccSynthIfc) != 0) {
			if ((field.modifiers & ClassFileConstants.AccPublic) == 0) {
				FieldModel model = getModel(field);
				model.addAttribute(WordValueAttribute.modifiersAttribute(field.modifiers));
				// if no binding is present yet remember this bit for transfer in FieldBinding().
				model._clearPrivateModifier = true;
				return true;
			}
		}
		return false;
	}
	/** Record that the rank'th type parameter is anchored to this field. */
	public void addUsageRank(int rank) {
		AnchorUsageRanksAttribute attr = (AnchorUsageRanksAttribute) getAttribute(ANCHOR_USAGE_RANKS);
		attr.addUsageRank(rank);
	}
	
	/** Create a faked method binding for a getAccessor to a given base field. 
	 * @param isGetter select getter or setter
	 */
	public static MethodBinding getDecapsulatingFieldAccessor(ReferenceBinding baseType,
												         		  FieldBinding     resolvedField,
												         		  boolean 		   isGetter)
	{
		FieldModel model = FieldModel.getModel(resolvedField);
		MethodBinding accessor = isGetter ? model._decapsulatingGetter : model._decapsulatingSetter;
		if (accessor != null)
			return accessor;
		
		TypeBinding[] argTypes = resolvedField.isStatic() 
									? (isGetter 
											? new TypeBinding[0] 
											: new TypeBinding[]{resolvedField.type})
									: (isGetter
											? new TypeBinding[]{baseType}
											: new TypeBinding[]{baseType, resolvedField.type});
		accessor = new MethodBinding(
					ClassFileConstants.AccPublic|ClassFileConstants.AccStatic,
					CharOperation.concat(isGetter ? OT_GETFIELD : OT_SETFIELD, resolvedField.name),
					isGetter ? resolvedField.type : TypeBinding.VOID,
					argTypes,
					Binding.NO_EXCEPTIONS,
					baseType);
		MethodModel.getModel(accessor)._fakeKind = FakeKind.BASE_FIELD_ACCESSOR;
		if (isGetter) 
			model._decapsulatingGetter = accessor;
		else
			model._decapsulatingSetter = accessor;
		return accessor;
	}
	/** Retrieve the array of ranks as read from bytecode. Returns null if no such attribute found. */
	public static int[] getAnchorUsageRanks(FieldBinding field) {
		if (field.model == null)
			return null;
		AnchorUsageRanksAttribute attr = (AnchorUsageRanksAttribute) field.model.getAttribute(ANCHOR_USAGE_RANKS);
		return attr.getRanks();
	}
	/** Is the given field accessed via an inferred callout-to-field? */
	public static boolean isCalloutAccessed(FieldBinding fieldBinding) {
		FieldModel model = fieldBinding.model;
		if (model == null)
			return false;
		return model._setterCallout != null || model._getterCallout != null;
	}

}

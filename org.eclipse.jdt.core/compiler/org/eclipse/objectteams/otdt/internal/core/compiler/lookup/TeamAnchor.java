/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamAnchor.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;


import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config.NotConfiguredException;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.FieldModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * NEW for OTDT.
 *
 * This class implements what is needed to use a variable as a team anchor
 * for an externalized role.
 *
 * Note: team anchors should only be accessed via the interface ITeamAnchor!
 * There are only two exceptions:
 *  + implementers of "TeamAnchor getClone()"
 *  + access to the static method maybeImproveAnchor(..).
 *
 * Rationale: There is a potential conflict if a team is also a role:
 * + For type checking the interface part should be used throughout
 * + For accessing roles of the nested team, the class part is needed.
 * For reasons of this conflict, accessing the type of a team anchor is
 * fully encapsulated in this class. The methods of this class will do
 * the selection between ifc and class part if needed.
 *
 * Secondly, this class implements a registry for role types that are known
 * to be anchored to the given variable.
 *
 * @author stephan
 */
public abstract class TeamAnchor extends Binding implements ITeamAnchor {

	// pulled up from VariableBinding:
	public TypeBinding type;

	public boolean isTeam() {
		return this.type.isTeam();
	}
	/** Is this binding possibly a team anchor? */
	public boolean couldBeTeamAnchor() {
		return isValidBinding() && isFinal() && this.type.isTeam();
	}
	// ====== managing bestname paths ======
	/**
	 * Each variable has a reference to it's bestNamePath denoting a path
	 * by which its value can be reached. The first element may be any
	 * VariableBinging, the others are FieldBindings.
	 *
	 * This path is either the variable itself,
	 * or the head of a chain of paths initialized from one another.
	 * All variables in a path must be final, the last one must have a team-type,
	 * in order to be used as a type anchor.
	 *
	 * Synthetic variabe bindings can be used as anchors for multi-element paths
	 * evaluated from QualifiedNameReferences.
	 *
	 * Note: typing the whole array to ITeamAnchor[] is over-cautious, but when
	 * working with bestname paths, no other properties than those available
	 * via ITeamAnchor are needed.
	 */
	protected ITeamAnchor[] bestNamePath = new ITeamAnchor[]{this};

	public ITeamAnchor[] getBestNamePath() {
		return getBestNamePath(true);
	}

	public ITeamAnchor[] getBestNamePath(boolean needResolve) {
		if (needResolve)
			resolveInitIfNeeded();  // may set bestNamePath
		return flattenBestNamePath(this.bestNamePath, 0);
	}

	/**
	 * Does this anchor's path start with a field binding (rather than a local/arg)?
	 */
	public boolean pathIsAbsolute() {
		return ((TeamAnchor)this.bestNamePath[0]).kind() == Binding.FIELD;
	}

	public boolean isValidAnchor() {
		if (!isValidBinding())
			return false;
		if (!this.type.isValidBinding())
			return false;
		if (this.type.leafComponentType().isBaseType())
			return false;
		return true;
	}

	private ITeamAnchor[] flattenBestNamePath(ITeamAnchor[] tree, int start) {
		if (tree == null || start >= tree.length)
			return new ITeamAnchor[0];
		TypeBinding firstType = ((TeamAnchor)this.bestNamePath[start]).leafReferenceType();
		ITeamAnchor[] prefix;
		if (RoleTypeBinding.isRoleWithExplicitAnchor(firstType)) {
			prefix = ((IRoleTypeBinding)firstType).getAnchorBestName();
		} else {
			prefix = new ITeamAnchor[] { this.bestNamePath[start] };
		}
		if (start == tree.length-1)
			return prefix;
		ITeamAnchor[] tail   = flattenBestNamePath(tree, start+1);
		if (tail.length == 0)
			return prefix;
		int len1 = prefix.length;
		int len2 = tail.length;
		ITeamAnchor[] result = new ITeamAnchor[len1+len2];
			System.arraycopy(prefix, 0, result, 0,    len1);
		System.arraycopy(tail,   0, result, len1, len2);
		return result;
	}

	public char[][] tokens()
	{
		char[][] tokens = new char[this.bestNamePath.length][];
		for (int i = 0; i < this.bestNamePath.length; i++) {
			tokens[i] = this.bestNamePath[i].internalName();
		}
		return tokens;
	}
	/**
	 * Check whether the given expression determines a proper bestName for this binding.
	 * @param rhs expression being assigned to this variable (Expression or Argument)
	 */
	public void setBestNameFromStat(Statement rhs)
	{
		if (   isFinal()
			&& (this.type instanceof ReferenceBinding))
		{
			ITeamAnchor[] path =  getBestNameFromStat(rhs);
			if (path != null) {
				ITeamAnchor lastBinding = path[path.length-1];
				if (   this instanceof FieldBinding) {
					if (lastBinding instanceof LocalVariableBinding) {
						LocalVariableBinding localVar = ((LocalVariableBinding)lastBinding);
						Scope scope  = localVar.declaringScope;
						if  (scope.referenceContext() instanceof ConstructorDeclaration) {
							// don't record the initial field assignment within a constructor,
							// but reverse this: pretend the local was initialized from the field,
							// to make both equivalent within this ctor.
							if (lastBinding.isFinal()) {
								if (   (localVar.tagBits & TagBits.IsArgument) != 0
									&& !localVar.pathIsAbsolute())
								{
									// localVar is really an argument not anchored to a field
									lastBinding.shareBestName(this);
								}
							}
						}
						return; // all others: ignore
					} else if (lastBinding instanceof TThisBinding) {
						return; // field may already have better anchor
					}
				}
				this.bestNamePath = path;
			}
		}
	}

	public void shareBestName(ITeamAnchor other) {
		this.bestNamePath = other.getBestNamePath();
	}

	public static ITeamAnchor[] getBestNameFromStat(Statement stat) {
		if (stat instanceof Argument) {
			return ((Argument)stat).binding.bestNamePath;
		}
		Expression expr = (Expression)stat;
		if (expr instanceof QualifiedNameReference) {
			QualifiedNameReference qRef = (QualifiedNameReference)expr;
			if (qRef.binding instanceof ITeamAnchor) {
				// collect all bindings from this qualified reference:
				ITeamAnchor first = (ITeamAnchor)qRef.binding;
				if (!first.isFinal())
					return null;
				int resultLength = qRef.otherBindings == null ? 0 : qRef.otherBindings.length;
				ITeamAnchor[] result = new ITeamAnchor[resultLength + 1];
				result[0] = first;
				for (int i = 0; i < resultLength; i++) {
					result[i+1] = qRef.otherBindings[i];
					if (!result[i+1].isFinal())
						return null;
				}
				return result;
			}
		} else if (expr instanceof NameReference) {
			//  a name representing a variable? => already has a bestNamePath:
			Binding bind = ((NameReference)expr).binding;
			if (   bind instanceof VariableBinding
				&& ((VariableBinding)bind).isFinal()) // see 1.6.5-otjld-*-2
				return ((VariableBinding)bind).bestNamePath;

		} else if (expr instanceof FieldReference) {
			// a field reference => append one element to the receiver's bestNamePath:
			FieldReference fieldRef = (FieldReference)expr;
			if (fieldRef.binding != null && fieldRef.binding.isFinal())
			{
				ITeamAnchor[] prefix = getBestNameFromStat(fieldRef.receiver);
				if (prefix == null)
					prefix = new VariableBinding[0];
				ITeamAnchor[] tail = fieldRef.binding.bestNamePath;
				ITeamAnchor[] path = new ITeamAnchor[prefix.length+tail.length];
				System.arraycopy(prefix, 0, path, 0, prefix.length);
				System.arraycopy(tail, 0,  path, prefix.length, tail.length);
				return path;
			}
		} /*else if (expr instanceof MessageSend) {
			MessageSend send = (MessageSend)expr;
			ITeamAnchor[] prefix = getBestNameFromStat(send.receiver);
			return prefix;
		} */
		// Note(SH): getting best name from message send with non-wrapped type
		// would require flow analysis to compute unique best name of the methods
		// return value. Simply using the receiver as attempted above is incorrect.
		if (expr.resolvedType instanceof IRoleTypeBinding) {
			return ((IRoleTypeBinding)expr.resolvedType).getAnchorBestName();
		}
		return null;
	}

	/**
	 * Assuming that reference is resolved construct the team anchor deduced from it.
	 * @param expression
	 * @return an existing or newly constructed anchor or null
	 */
	public static ITeamAnchor getTeamAnchor(Expression expression) {
		Binding bind = null;
		if (expression instanceof NameReference) {
			bind = ((NameReference)expression).binding;
	        if (expression instanceof QualifiedNameReference) {
	        	FieldBinding[] otherFields = ((QualifiedNameReference)expression).otherBindings;
	        	if (otherFields != null && otherFields.length > 0) {
	        		int len = otherFields.length;
	        		ITeamAnchor anchor = otherFields[len-1];
	        		for (int i = len-2; i >= 0; i--)
        				anchor = anchor.setPathPrefix(otherFields[i]);
	        		if (bind instanceof ITeamAnchor)
	        			return anchor.setPathPrefix((ITeamAnchor)bind);
	        	}
	        }
		} else if (expression instanceof TypeAnchorReference) {
			bind = (Binding) ((TypeAnchorReference)expression).getResolvedAnchor();
		}		
		if (bind != null && (bind instanceof TeamAnchor))
			return (TeamAnchor)bind;
		TypeBinding type = expression.resolvedType;
		if (type != null && type.isRoleType())
			return ((IRoleTypeBinding)type).getAnchor();
		else
			return null;
	}


	/**
	 * Do two variables provably denote the same instance?
	 * Shown by shallow-equal bestNamePaths.
	 */
	public boolean hasSameBestNameAs(ITeamAnchor other) {
		if (other == this) return true;
		ITeamAnchor[] otherBestName = other.getBestNamePath();
		return hasSameBestNameAs(otherBestName, other);
	}
	/** Variant if path of other is directly known. */
	public boolean hasSameBestNameAs(ITeamAnchor[] otherBestName, ITeamAnchor other) {
		ITeamAnchor[] thisBestName  = getBestNamePath();

		// perhaps one anchor directly refers to the other:
		if (thisBestName.length == 1 && thisBestName[0] == other)
			return true;
		if (otherBestName.length == 1 && otherBestName[0] == this)
			return true;

		// general case, compare paths:
		if (thisBestName.length == otherBestName.length)
		{
			for (int i = 0; i < thisBestName.length; i++) {
				if (!isSameVariable(thisBestName[i], otherBestName[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private boolean isSameVariable(ITeamAnchor one, ITeamAnchor two) {
		if (one == two) return true;
		if (one instanceof TThisBinding && two instanceof TThisBinding)
			return true;
			// thanks to 1.4(c) tthis bindings from the same context
			// can never refer to different roles of the same name.

		// check if one is a fake replica of the other:
		if (one instanceof FieldBinding && two instanceof FieldBinding)
		{
			FieldBinding f1= (FieldBinding)one;
			FieldBinding f2= (FieldBinding)two;
			if (!CharOperation.equals(f1.name, f2.name))
				return false;

			return TypeBinding.equalsEquals(FieldModel.getActualDeclaringClass(f1), FieldModel.getActualDeclaringClass(f2));
		}
		return false;
	}
	/** Hook method to be overridden in FieldBinding. */
	protected void resolveInitIfNeeded() {/*nothing*/}

	/**
     * API for generating team anchors into byte-code attributes.
     *
	 * Get this anchor's "best name", ie., the 'minimal' path.
	 * If first component is a field prepend the fully qualified name of its declaring class.
	 * (This FQN uses '$' to separate inner classes, because this way LookupEnvironment.askForType()
	 *  can directly find the type).
	 * @return a flat ('.'-seperated) representation of this variable's best name.
	 */
	public char[] getBestName() {
		int len = this.bestNamePath.length;
		int prefixLen = 0;
		char[][] tokens;
		if (this.bestNamePath[0] instanceof FieldBinding) {
			tokens = new char[len+1][];
			tokens[0] = CharOperation.concatWith(
				((FieldBinding)this.bestNamePath[0]).declaringClass.compoundName,
				'.');
			prefixLen++;
		} else {
			tokens = new char[len][];
		}
		for(int i = 0; i < len; i++) {
			tokens[i+prefixLen] = this.bestNamePath[i].internalName();
		}
		return CharOperation.concatWith(tokens, '.');
	}

	/**
	 * Answer an anchor that is suitable for externalizing a given role type.
	 * The anchor can either be this anchor itself, or, if the anchor
	 * has a role type it might be that role type's anchor, etc.
	 * If roleType already has a significant anchor, and this anchor is TThis
	 * use the more specific anchor from roleType.
	 */
	public ITeamAnchor asAnchorFor (ReferenceBinding roleType) {
		if (! (this.type instanceof ReferenceBinding))
			return null;
		ReferenceBinding leafType = leafReferenceType();
		// TODO(SH): is it correct to compare leafType and roleType?
		// shouldn't we look at roleType._teamAnchor[0] instead??
		if (TeamModel.isTeamContainingRole(leafType, roleType)) {
			if (   this instanceof TThisBinding
				&& RoleTypeBinding.isRoleWithExplicitAnchor(roleType))
			{
				return ((IRoleTypeBinding)roleType).getAnchor();
			}
			return this;
		}
		if (leafType instanceof IRoleTypeBinding)
			return ((IRoleTypeBinding)leafType).getAnchor().asAnchorFor(roleType);
		return null;
	}

	public ITeamAnchor retrieveAnchorFromAnchorRoleTypeFor(ReferenceBinding roleType) {
		// TODO (SH) copied and not yet validated!
		// in contrast to the above, this method will never return 'this'
	    ReferenceBinding leafType = leafReferenceType();
		if (this.type != null && leafType != null && leafType.isRoleType())
	    {
	        ITeamAnchor anchor = ((IRoleTypeBinding)leafType).getAnchor();
	        if (anchor.isTeamContainingRole(roleType))
	        {
	            return anchor;
	        } else {
	        	return anchor.asAnchorFor(roleType);
	        }
	    }
	    return null;
	}

	/**
	 * Cloning is needed to create synthetic bindings used as type anchor
	 * evaluated from QualifiedNameReference.
	 */
	protected abstract TeamAnchor getClone();

	/**
	 * Create a VariableBinding with a bestNamePath constructed from
	 * the bestNamePath of `prefix' plus this as last element.
	 */
	public ITeamAnchor setPathPrefix (ITeamAnchor prefix) {
		TeamAnchor result = getClone();
		ITeamAnchor[] prefixBestName = prefix.getBestNamePath();
		ITeamAnchor[] thisBestName = getBestNamePath();
		int len1 = prefixBestName.length;
		int len2 = thisBestName.length;
		result.bestNamePath = new VariableBinding[len1 + len2];
		System.arraycopy(prefixBestName, 0, result.bestNamePath, 0, len1);
		System.arraycopy(thisBestName, 0, result.bestNamePath, len1, len2);
		return result;
	}


	public ITeamAnchor replaceFirst(ITeamAnchor anchor) {
		TeamAnchor result = getClone();
		result.bestNamePath[0] = anchor;
		return result;
	}

	public boolean isPrefixLegal(ReferenceBinding site, ITeamAnchor prefix) {
	   	ITeamAnchor firstField = this.bestNamePath[0];
	   	if (firstField instanceof FieldBinding) {
	   		ReferenceBinding declaringClass = ((FieldBinding)firstField).declaringClass;
	   		if (maySkipAnchor(site, prefix, declaringClass))
	   			return false;
	   		ReferenceBinding currentType = (ReferenceBinding)((TeamAnchor)prefix).type.leafComponentType();
	   		while (currentType != null) {
	   			if (currentType.isCompatibleWith(declaringClass))
	   				return true;
	   			currentType = currentType.enclosingType();
	   		}
	   	}
	   	return false;
	}
	/**
	 *  If an anchor simply leads us to a field which is also directly accessible in the current context,
	 *  the anchor may be skipped in constructing a path.
	 * @param site
	 * @param prefix
	 * @param declaringClass
	 * @return the result ;-)
	 */
	private boolean maySkipAnchor(ReferenceBinding site, ITeamAnchor prefix, ReferenceBinding declaringClass) {
		if (((TeamAnchor)prefix).hasRoleTypeWithRelevantAnchor())
			return false;
   		ReferenceBinding currentType = site;
   		while (currentType != null) {
   			if (currentType.isCompatibleWith(declaringClass))
   				return true;
   			currentType = currentType.enclosingType();
   		}
   		return false;
	}
	/**
	 * If an anchor has in its best name a role type with explicit anchor,
	 * this component is needed and will be found in flattenBestNamePath().
	 */
	private boolean hasRoleTypeWithRelevantAnchor() {
		for (int i = 0; i < this.bestNamePath.length; i++) {
			ReferenceBinding currentLeafType = ((TeamAnchor)this.bestNamePath[i]).leafReferenceType();
			if (RoleTypeBinding.isRoleWithExplicitAnchor(currentLeafType))
				return true;
		}
		return false;
	}
	// ====== encapsulate all access to type:

	public boolean hasValidReferenceType() {
		return this.type != null && (this.type instanceof ReferenceBinding) && this.type.isValidBinding();
	}
	public boolean hasSameTypeAs(ITeamAnchor other) {
		return TypeBinding.equalsEquals(leafReferenceType(), ((TeamAnchor)other).leafReferenceType());
	}
	public TeamModel getTeamModelOfType() {
		return leafReferenceType().getTeamModel();
	}
	public boolean isTypeCompatibleWith(ReferenceBinding other) {
		if (! (this.type instanceof ReferenceBinding))
			return false;
		if (this.type.isRole())
			return (((ReferenceBinding)this.type).getRealClass().isCompatibleWith(other));
		return this.type.isCompatibleWith(other);
	}
	public boolean isTypeCompatibleWithTypeOf(ITeamAnchor other) {
        ReferenceBinding otherAnchorType = (ReferenceBinding)((TeamAnchor)other).type;
        if (    otherAnchorType.isRole()
        	&& !otherAnchorType.isInterface())
        {
        	// for compatibility check don't use a role-class-part
        	otherAnchorType = otherAnchorType.roleModel.getInterfacePartBinding();
        }
		return this.type.isCompatibleWith(otherAnchorType);
	}

	public void setStaticallyKnownTeam(RoleTypeBinding rtb) {
		ReferenceBinding teamBinding = leafReferenceType();

		if (teamBinding.isSynthInterface()) {
			// never use a synth interface as the team of a RTB
			ReferenceBinding outerTeam = teamBinding.enclosingType();
			char[] teamName = CharOperation.concat(
					IOTConstants.OT_DELIM_NAME,
					teamBinding.internalName());
			teamBinding = outerTeam.getMemberType(teamName);
		}
		rtb._staticallyKnownTeam = teamBinding;
	}

	public boolean isTeamContainingRole(ReferenceBinding roleType) {
		return TeamModel.isTeamContainingRole(leafReferenceType(), roleType);
	}

	/**
	 * Get a field from this anchor's type.
	 */
	public FieldBinding getFieldOfType(char[] token, boolean isStatic, boolean allowOuter) {
		return TypeAnalyzer.findField(
				leafReferenceType(),
				token,
				isStatic,
				allowOuter);
	}

	public ReferenceBinding getMemberTypeOfType(char[] name) {
		// safe if type is a class-part
		ReferenceBinding roleType = leafReferenceType().getMemberType(name);
		if (roleType == null)
			return null;
		return (RoleTypeBinding)getRoleTypeBinding(roleType, 0);
	}
	public RoleModel getStrengthenedRole (ReferenceBinding role) {
        if (TypeBinding.notEquals(role.roleModel.getTeamModel().getBinding(), leafReferenceType()))
        	return leafReferenceType().getMemberType(role.internalName()).roleModel;
        return role.roleModel;
	}

	/**
	 * Try to interpret existingAnchor relative to receiver,
	 * composing a new anchor path from receiver and existingAnchor.
	 * @param site 			 type where resolving takes places,
	 * 						 an implicit this of this type can always be assumed as an implicit anchor.
     *                       FIXME(SH): make sure we always have that instance (static scopes??)
	 * @param existingAnchor
	 * @param receiver
	 *
	 * @return a team anchor or null
	 */
	public static ITeamAnchor maybeImproveAnchor(ReferenceBinding site, ITeamAnchor existingAnchor, Expression receiver)
	{
		TeamAnchor receiverVar = null;
		ITeamAnchor fallback = null;
		if (receiver instanceof CastExpression)
			receiver = ((CastExpression)receiver).expression;
		if (receiver instanceof NameReference) {
			Binding bind = ((NameReference)receiver).binding;
			if (bind instanceof VariableBinding)
				receiverVar = (VariableBinding)bind;
		} else if (receiver instanceof AllocationExpression) {
			// need to record that this anchor is unique (not compatible to any other anchor).
			fallback = receiverVar = new FieldBinding(("'non-final anchor "+receiver.toString()+'\'').toCharArray(),  //$NON-NLS-1$
								    receiver.resolvedType,  		// type
								    ClassFileConstants.AccFinal,
								    site, 							// declaring class
								    Constant.NotAConstant)
			{
				@Override public int problemId() { return IProblem.AnchorNotFinal; }
			};
		}	else {
			TypeBinding receiverType = receiver.resolvedType;
			if (   receiverType != null
				&& RoleTypeBinding.isRoleWithExplicitAnchor(receiverType))
			{
				receiverVar = (TeamAnchor)((IRoleTypeBinding)receiverType).getAnchor();
			}
		}
		if (receiverVar != null) {
			ITeamAnchor[] prefix = existingAnchor.getBestNamePath();
			ITeamAnchor previous = null;
			if (prefix != null && prefix.length > 0)
				previous = prefix[prefix.length-1];
			if (previous instanceof FieldBinding) {
				if (DependentTypeBinding.isDependentTypeOf(receiverVar.type, previous))
				{
					DependentTypeBinding depReceiver = (DependentTypeBinding)receiverVar.type;
					return existingAnchor.replaceFirst(depReceiver._teamAnchor);
				}
				FieldBinding previousField = (FieldBinding)previous;
				ReferenceBinding currentType = null;
				boolean firstImplicitlyReachable = false;
				if (receiverVar instanceof FieldBinding)
					currentType = ((FieldBinding)receiverVar).declaringClass;
				else if (receiverVar instanceof LocalVariableBinding)
					currentType = ((LocalVariableBinding)receiverVar).declaringScope.enclosingSourceType();
				while(currentType != null) {
					if (currentType.isCompatibleWith(previousField.declaringClass)) {
						firstImplicitlyReachable = true;
						break;
						// reachable without navigating receiverVar
					}
					currentType = currentType.enclosingType();
				}
				currentType = receiverVar.leafReferenceType();
				while(currentType != null) {
					if (currentType.isCompatibleWith(previousField.declaringClass)) {
						if (firstImplicitlyReachable) {
							if (fallback != null)
								return fallback; // if we detected non-finalness above, now is the time to report this back
						} else {
							return existingAnchor.setPathPrefix(receiverVar);
						}
					}
					currentType = currentType.enclosingType();
					fallback = null; // don't use any more after we have traveled out
				}
				if (firstImplicitlyReachable)
					return existingAnchor; // didn't improve but improvement wasn't necessary
			}
			return null;
		}
		// did not improve
		return null;
	}

	public TypeBinding getResolvedType() {
		if (leafReferenceType() != null && leafReferenceType().isRole()) {
			ReferenceBinding roleIfc = leafReferenceType().roleModel.getInterfacePartBinding();
			if (this.bestNamePath != null && this.bestNamePath.length > 1) {
				int len = this.bestNamePath.length;
				ITeamAnchor previousAnchor = this.bestNamePath[len-2];
				if (previousAnchor.isTeam()) {
					for (int i=len-3; i>=0; i--)
						previousAnchor = previousAnchor.setPathPrefix(this.bestNamePath[i]);
					TypeBinding[] typeArguments = this.type.isParameterizedType() ? ((ParameterizedTypeBinding)this.type).arguments : null;
					return previousAnchor.getDependentTypeBinding(roleIfc, -1, typeArguments, this.type.dimensions());
				}
			}
			return roleIfc;
		}
		return this.type;
	}

	// internal helper: when used as an anchor an arraybinding is stripped to its leaf:
	private ReferenceBinding leafReferenceType() {
		if (this.type != null) {
			TypeBinding leafComponentType = this.type.leafComponentType();
			if (leafComponentType instanceof ReferenceBinding)
				return (ReferenceBinding)leafComponentType;
		}
		return null;
	}

	/** Answer the declaring class of the first path element, if it
      * is either a field or tthis.
      */
	public ReferenceBinding getFirstDeclaringClass() {
		if (this.bestNamePath[0] instanceof FieldBinding)
			return ((FieldBinding)this.bestNamePath[0]).declaringClass;
		if (this.bestNamePath[0] instanceof TThisBinding)
			return ((TThisBinding)this.bestNamePath[0]).declaringClass;
		return null;
	}
	// ====== manage role types anchored to this variable ======


	/**
	 * Retrieve or create and record a role type depending on this variable as its anchor.
	 * Use this factory method instead of new RoleTypeBinding()!
	 *
	 * PRE: type checking done.
	 *
	 * @param roleBinding the pure ReferenceBinding of the role
	 * @param dimensions  if > 0 we request an array of roles.
	 * @return either RoleTypeBinding or ArrayBinding.
	 */
	public TypeBinding getRoleTypeBinding(
			ReferenceBinding 		roleBinding,
			int              		dimensions)
	{
		TypeBinding[] typeArguments = roleBinding.isParameterizedType() ? ((ParameterizedTypeBinding)roleBinding).arguments : null;
		return getRoleTypeBinding(roleBinding, typeArguments, dimensions);
	}
	public TypeBinding getRoleTypeBinding(
			ReferenceBinding	roleBinding,
			TypeBinding[] 		arguments,
			int             	dimensions)
	{
		if (kind() == Binding.LOCAL)
			((LocalVariableBinding)this).useFlag = LocalVariableBinding.USED;

		if (RoleTypeBinding.isRoleWithExplicitAnchor(roleBinding)) {
//			ITeamAnchor combinedAnchor = combineAnchors(this, ((RoleTypeBinding)roleBinding)._teamAnchor);
//			if (combinedAnchor == null) {
//				ITeamAnchor anchor = ((RoleTypeBinding)roleBinding)._teamAnchor;
//				if (anchor.isPrefixLegal(roleBinding, this)) {
//					//combinedAnchor = anchor.setPathPrefix(this);
//					throw new InternalCompilerError("HERE!");
//				} else {
			// FIXME(SH): who calls us with an RTB ?? is this strategy OK?
			// saw a call from FieldAccessSpec.resolveFeature() (improving anchor)
					return getRoleTypeBinding(roleBinding.getRealType(), arguments, dimensions);
//				}
//			}
//			return combinedAnchor.getRoleTypeBinding(roleBinding.getRealType(), dimensions);
		}
    	ReferenceBinding roleEnclosing = roleBinding.enclosingType();
    	if (   roleEnclosing != null
    		&& TypeBinding.notEquals(roleEnclosing.erasure(), leafReferenceType().getRealClass())) // i.e.: teams differ
    	{
    		//assert TeamModel.areCompatibleEnclosings(this.leafType(), roleEnclosing);
    		// team of roleBinding is less specific than this anchor => requesting a weakened type
    		ReferenceBinding strengthenedRole =
    				(ReferenceBinding)TeamModel.strengthenRoleType(leafReferenceType(), roleBinding);
    		if (TypeBinding.notEquals(strengthenedRole, roleBinding)) {// avoid infinite recursion if strengthening made no difference
    			DependentTypeBinding strongRoleType = (DependentTypeBinding)getRoleTypeBinding(strengthenedRole, arguments, 0);
    			if (strongRoleType != null)
    				return WeakenedTypeBinding.makeWeakenedTypeBinding(strongRoleType, roleBinding, dimensions);
    		}
    		if (dimensions == roleBinding.dimensions() && roleBinding instanceof DependentTypeBinding)
    			return roleBinding;
    	}

    	int paramPosition = -1;
    	if (roleBinding instanceof DependentTypeBinding)
    		paramPosition = ((DependentTypeBinding)roleBinding)._valueParamPosition;
    	
    	if (roleBinding instanceof RoleTypeBinding) 
    		if (!this.isTeamContainingRole(roleBinding)) // need to switch to outer anchor?
    			return this.asAnchorFor(roleBinding).getDependentTypeBinding(roleBinding, paramPosition, arguments, dimensions, ((RoleTypeBinding)roleBinding).environment);
    	
    	if (roleBinding instanceof WeakenedTypeBinding) {
    		if (((WeakenedTypeBinding)roleBinding)._teamAnchor == this)
    			return roleBinding;
    	}
    	
    	if (roleBinding instanceof ParameterizedTypeBinding)
    		return getDependentTypeBinding(roleBinding, paramPosition, arguments, dimensions, ((ParameterizedTypeBinding)roleBinding).environment);
    	return getDependentTypeBinding(roleBinding, paramPosition, arguments, dimensions);
	}

	/**
	 * Retrieve or create and record a role type depending on this variable as its anchor.
	 * Use this factory method instead of new RoleTypeBinding()!
	 *
	 * PRE: type checking done.
	 *
	 * @param typeBinding the pure ReferenceBinding of the role
	 * @param dimensions  if > 0 we request an array of roles.
	 * @return either RoleTypeBinding or ArrayBinding.
	 */
	public TypeBinding getDependentTypeBinding(
			ReferenceBinding 	typeBinding,
			int	            	paramPosition,
			TypeBinding[] 		arguments,
			int             	dimensions)
	{
		try {
			return getDependentTypeBinding(typeBinding, paramPosition, arguments, dimensions, Config.getLookupEnvironment());
		} catch (NotConfiguredException e) {
			throw new AbortCompilation(false, e);
		}
	}
	public TypeBinding getDependentTypeBinding(
			ReferenceBinding 	typeBinding,
			int	            	paramPosition,
			TypeBinding[] 		arguments,
			int             	dimensions,
			LookupEnvironment   env) 
	{
	    DependentTypeBinding dependentTypeBinding =
	    	(DependentTypeBinding)env.createParameterizedType(typeBinding, arguments, this, paramPosition, typeBinding.enclosingType(), typeBinding.getTypeAnnotations());
	    return (dimensions > 0)
	    	? dependentTypeBinding.getArrayType(dimensions)
	    	: dependentTypeBinding;
	}

	public TypeBinding resolveRoleType(char[] roleName, int dims) {
		if (!this.type.isTeam())
			return null;
		ReferenceBinding roleType = ((ReferenceBinding)this.type).getMemberType(roleName);
		if (roleType == null || !roleType.isValidBinding())
			return roleType; // FIXME(SH): is it correct to return a ProblemBinding? Check clients!
		return getRoleTypeBinding(roleType, dims);
	}
}

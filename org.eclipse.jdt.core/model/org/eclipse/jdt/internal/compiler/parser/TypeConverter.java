/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TypeConverter.java 23404 2010-02-03 14:10:22Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.util.FieldData;
import org.eclipse.objectteams.otdt.core.util.MethodData;
import org.eclipse.objectteams.otdt.internal.core.CallinMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.FieldAccessSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.QualifiedBaseReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeValueParameter;

public abstract class TypeConverter {

	int namePos;

	protected ProblemReporter problemReporter;
	protected boolean has1_5Compliance;
	private char memberTypeSeparator;

	protected TypeConverter(ProblemReporter problemReporter, char memberTypeSeparator) {
		this.problemReporter = problemReporter;
		this.has1_5Compliance = problemReporter.options.complianceLevel >= ClassFileConstants.JDK1_5;
		this.memberTypeSeparator = memberTypeSeparator;
	}

	private void addIdentifiers(String typeSignature, int start, int endExclusive, int identCount, ArrayList fragments) {
		if (identCount == 1) {
			char[] identifier;
			typeSignature.getChars(start, endExclusive, identifier = new char[endExclusive-start], 0);
			fragments.add(identifier);
		} else
			fragments.add(extractIdentifiers(typeSignature, start, endExclusive-1, identCount));
	}

	/*
	 * Build an import reference from an import name, e.g. java.lang.*
	 */
	protected ImportReference createImportReference(
		String[] importName,
		int start,
		int end,
		boolean onDemand,
		int modifiers) {

		int length = importName.length;
		long[] positions = new long[length];
		long position = ((long) start << 32) + end;
		char[][] qImportName = new char[length][];
		for (int i = 0; i < length; i++) {
			qImportName[i] = importName[i].toCharArray();
			positions[i] = position; // dummy positions
		}
		return new ImportReference(
			qImportName,
			positions,
			onDemand,
			modifiers);
	}

//{ObjectTeams: argument baseBounds added:
/* orig:
	protected TypeParameter createTypeParameter(char[] typeParameterName, char[][] typeParameterBounds, int start, int end) {
  :giro */
	protected TypeParameter createTypeParameter(char[] typeParameterName, char[][] typeParameterBounds, boolean hasBaseBound, int start, int end) {
// SH}
		TypeParameter parameter = new TypeParameter();
		parameter.name = typeParameterName;
		parameter.sourceStart = start;
		parameter.sourceEnd = end;
		if (typeParameterBounds != null) {
			int length = typeParameterBounds.length;
			if (length > 0) {
				parameter.type = createTypeReference(typeParameterBounds[0], start, end);
//{ObjectTeams: base bound (only first bound)?
				if (hasBaseBound)
					parameter.type.bits |= ASTNode.IsRoleType;
// SH}
				if (length > 1) {
					parameter.bounds = new TypeReference[length-1];
					for (int i = 1; i < length; i++) {
						TypeReference bound = createTypeReference(typeParameterBounds[i], start, end);
						bound.bits |= ASTNode.IsSuperType;
						parameter.bounds[i-1] = bound;
					}
				}
			}
		}
		return parameter;
	}

//{ObjectTeams: value parameters:
	protected TypeParameter createValueParameter(char[] parameterName, char[] parameterType, int start, int end)
	{
		TypeParameter parameter = new TypeValueParameter(parameterName, 0L); // set pos below
		parameter.sourceStart = start;
		parameter.sourceEnd = end;
		parameter.declarationSourceStart = start;
		parameter.declarationSourceEnd = end;
		parameter.declarationEnd = end;
		parameter.type = createTypeReference(parameterType, start, end);
		return parameter;
	}
// SH}

	/*
	 * Build a type reference from a readable name, e.g. java.lang.Object[][]
	 */
	protected TypeReference createTypeReference(
		char[] typeName,
		int start,
		int end) {

		int length = typeName.length;
		this.namePos = 0;
		return decodeType(typeName, length, start, end);
	}

	/*
	 * Build a type reference from a type signature, e.g. Ljava.lang.Object;
	 */
	protected TypeReference createTypeReference(
			String typeSignature,
			int start,
			int end) {

		int length = typeSignature.length();
		this.namePos = 0;
		return decodeType(typeSignature, length, start, end);
	}

	private TypeReference decodeType(String typeSignature, int length, int start, int end) {
		int identCount = 1;
		int dim = 0;
		int nameFragmentStart = this.namePos, nameFragmentEnd = -1;
		boolean nameStarted = false;
		ArrayList fragments = null;
//{ObjectTeams: consider @anchor:
		boolean isAnchor = false;
// SH}
		typeLoop: while (this.namePos < length) {
			char currentChar = typeSignature.charAt(this.namePos);
			switch (currentChar) {
				case Signature.C_BOOLEAN :
					if (!nameStarted) {
						this.namePos++;
						if (dim == 0)
							return new SingleTypeReference(TypeBinding.BOOLEAN.simpleName, ((long) start << 32) + end);
						else
							return new ArrayTypeReference(TypeBinding.BOOLEAN.simpleName, dim, ((long) start << 32) + end);
					}
					break;
				case Signature.C_BYTE :
					if (!nameStarted) {
						this.namePos++;
						if (dim == 0)
							return new SingleTypeReference(TypeBinding.BYTE.simpleName, ((long) start << 32) + end);
						else
							return new ArrayTypeReference(TypeBinding.BYTE.simpleName, dim, ((long) start << 32) + end);
					}
					break;
				case Signature.C_CHAR :
					if (!nameStarted) {
						this.namePos++;
						if (dim == 0)
							return new SingleTypeReference(TypeBinding.CHAR.simpleName, ((long) start << 32) + end);
						else
							return new ArrayTypeReference(TypeBinding.CHAR.simpleName, dim, ((long) start << 32) + end);
					}
					break;
				case Signature.C_DOUBLE :
					if (!nameStarted) {
						this.namePos++;
						if (dim == 0)
							return new SingleTypeReference(TypeBinding.DOUBLE.simpleName, ((long) start << 32) + end);
						else
							return new ArrayTypeReference(TypeBinding.DOUBLE.simpleName, dim, ((long) start << 32) + end);
					}
					break;
				case Signature.C_FLOAT :
					if (!nameStarted) {
						this.namePos++;
						if (dim == 0)
							return new SingleTypeReference(TypeBinding.FLOAT.simpleName, ((long) start << 32) + end);
						else
							return new ArrayTypeReference(TypeBinding.FLOAT.simpleName, dim, ((long) start << 32) + end);
					}
					break;
				case Signature.C_INT :
					if (!nameStarted) {
						this.namePos++;
						if (dim == 0)
							return new SingleTypeReference(TypeBinding.INT.simpleName, ((long) start << 32) + end);
						else
							return new ArrayTypeReference(TypeBinding.INT.simpleName, dim, ((long) start << 32) + end);
					}
					break;
				case Signature.C_LONG :
					if (!nameStarted) {
						this.namePos++;
						if (dim == 0)
							return new SingleTypeReference(TypeBinding.LONG.simpleName, ((long) start << 32) + end);
						else
							return new ArrayTypeReference(TypeBinding.LONG.simpleName, dim, ((long) start << 32) + end);
					}
					break;
				case Signature.C_SHORT :
					if (!nameStarted) {
						this.namePos++;
						if (dim == 0)
							return new SingleTypeReference(TypeBinding.SHORT.simpleName, ((long) start << 32) + end);
						else
							return new ArrayTypeReference(TypeBinding.SHORT.simpleName, dim, ((long) start << 32) + end);
					}
					break;
				case Signature.C_VOID :
					if (!nameStarted) {
						this.namePos++;
						return new SingleTypeReference(TypeBinding.VOID.simpleName, ((long) start << 32) + end);
					}
					break;
				case Signature.C_RESOLVED :
				case Signature.C_UNRESOLVED :
				case Signature.C_TYPE_VARIABLE :
					if (!nameStarted) {
						nameFragmentStart = this.namePos+1;
						nameStarted = true;
					}
					break;
				case Signature.C_STAR:
					this.namePos++;
					Wildcard result = new Wildcard(Wildcard.UNBOUND);
					result.sourceStart = start;
					result.sourceEnd = end;
					return result;
				case Signature.C_EXTENDS:
					this.namePos++;
					result = new Wildcard(Wildcard.EXTENDS);
					result.bound = decodeType(typeSignature, length, start, end);
					result.sourceStart = start;
					result.sourceEnd = end;
					return result;
				case Signature.C_SUPER:
					this.namePos++;
					result = new Wildcard(Wildcard.SUPER);
					result.bound = decodeType(typeSignature, length, start, end);
					result.sourceStart = start;
					result.sourceEnd = end;
					return result;
				case Signature.C_ARRAY :
					dim++;
					break;
				case Signature.C_GENERIC_END :
				case Signature.C_SEMICOLON :
					nameFragmentEnd = this.namePos-1;
//{ObjectTeams: don't consume stop char of anchor:
				  if (!isAnchor)
// SH}
					this.namePos++;
					break typeLoop;
				case Signature.C_DOLLAR:
//{ObjectTeams: recover _OT$ prefix (revert separator-ness of '$'):
					if (   nameStarted
						&& this.namePos >= 3
						&& typeSignature.substring(this.namePos-3, this.namePos+1)
							   		.equals(IOTConstants.OT_DOLLAR))
					{
						nameFragmentStart = this.namePos-3;
						break;
					}
// SH}
					if (this.memberTypeSeparator != Signature.C_DOLLAR)
						break;
					// $FALL-THROUGH$
				case Signature.C_DOT :
					if (!nameStarted) {
						nameFragmentStart = this.namePos+1;
						nameStarted = true;
					} else if (this.namePos > nameFragmentStart) // handle name starting with a $ (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=91709)
						identCount ++;
					break;
				case Signature.C_GENERIC_START :
					nameFragmentEnd = this.namePos-1;
					// convert 1.5 specific constructs only if compliance is 1.5 or above
					if (!this.has1_5Compliance)
						break typeLoop;
					if (fragments == null) fragments = new ArrayList(2);
					addIdentifiers(typeSignature, nameFragmentStart, nameFragmentEnd + 1, identCount, fragments);
					this.namePos++; // skip '<'
					TypeReference[] arguments = decodeTypeArguments(typeSignature, length, start, end); // positionned on '>' at end
					fragments.add(arguments);
					identCount = 1;
					nameStarted = false;
					// next increment will skip '>'
					break;
//{ObjectTeams: detecting the @anchor
				case '@':
					isAnchor = true;
					nameFragmentStart++;
					break;
// SH}
			}
			this.namePos++;
		}
		if (fragments == null) { // non parameterized
			/* rebuild identifiers and dimensions */
			if (identCount == 1) { // simple type reference
				if (dim == 0) {
					char[] nameFragment = new char[nameFragmentEnd - nameFragmentStart + 1];
					typeSignature.getChars(nameFragmentStart, nameFragmentEnd +1, nameFragment, 0);
//{ObjectTeams: anchor reference detected?
					if (isAnchor) {
						// _OT$base may have been beautified to base, recover original:
						if (CharOperation.equals(nameFragment, IOTConstants.BASE))
							nameFragment = IOTConstants._OT_BASE;
						NameReference name = new SingleNameReference(nameFragment, ((long) start << 32) + end);
						return new TypeAnchorReference(name, start);
					}
// SH}
					return new SingleTypeReference(nameFragment, ((long) start << 32) + end);
				} else {
					char[] nameFragment = new char[nameFragmentEnd - nameFragmentStart + 1];
					typeSignature.getChars(nameFragmentStart, nameFragmentEnd +1, nameFragment, 0);
					return new ArrayTypeReference(nameFragment, dim, ((long) start << 32) + end);
				}
			} else { // qualified type reference
				long[] positions = new long[identCount];
				long pos = ((long) start << 32) + end;
				for (int i = 0; i < identCount; i++) {
					positions[i] = pos;
				}
				char[][] identifiers = extractIdentifiers(typeSignature, nameFragmentStart, nameFragmentEnd, identCount);
				if (dim == 0) {
//{ObjectTeams: anchor reference detected?
					if (isAnchor) {
						Reference anchor;
						// anchors including a 'base' token are encoded differently:
						if (CharOperation.equals(identifiers[identCount-1], IOTConstants.BASE)) {
							TypeReference qualification;
							if (identCount == 2) {  
								qualification = new SingleTypeReference(identifiers[0], positions[0]);
							} else {
								System.arraycopy(positions, 0, positions=new long[positions.length-1], 0, positions.length-1);
								qualification = new QualifiedTypeReference(CharOperation.subarray(identifiers, 0, identCount-1), positions);
							}
							anchor = new QualifiedBaseReference(qualification, end-3, end);
						} else {
							anchor = new QualifiedNameReference(identifiers, positions, start, end);
						}
						return new TypeAnchorReference(anchor, start);
					}
// SH}
					return new QualifiedTypeReference(identifiers, positions);
				} else {
					return new ArrayQualifiedTypeReference(identifiers, dim, positions);
				}
			}
		} else { // parameterized
			// rebuild type reference from available fragments: char[][], arguments, char[][], arguments...
			// check trailing qualified name
			if (nameStarted) {
				addIdentifiers(typeSignature, nameFragmentStart, nameFragmentEnd + 1, identCount, fragments);
			}
			int fragmentLength = fragments.size();
			if (fragmentLength == 2) {
				Object firstFragment = fragments.get(0);
				if (firstFragment instanceof char[]) {
					// parameterized single type
					return new ParameterizedSingleTypeReference((char[]) firstFragment, (TypeReference[]) fragments.get(1), dim, ((long) start << 32) + end);
				}
			}
			// parameterized qualified type
			identCount = 0;
			for (int i = 0; i < fragmentLength; i ++) {
				Object element = fragments.get(i);
				if (element instanceof char[][]) {
					identCount += ((char[][])element).length;
				} else if (element instanceof char[])
					identCount++;
			}
			char[][] tokens = new char[identCount][];
			TypeReference[][] arguments = new TypeReference[identCount][];
			int index = 0;
			for (int i = 0; i < fragmentLength; i ++) {
				Object element = fragments.get(i);
				if (element instanceof char[][]) {
					char[][] fragmentTokens = (char[][]) element;
					int fragmentTokenLength = fragmentTokens.length;
					System.arraycopy(fragmentTokens, 0, tokens, index, fragmentTokenLength);
					index += fragmentTokenLength;
				} else if (element instanceof char[]) {
					tokens[index++] = (char[]) element;
				} else {
					arguments[index-1] = (TypeReference[]) element;
				}
			}
			long[] positions = new long[identCount];
			long pos = ((long) start << 32) + end;
			for (int i = 0; i < identCount; i++) {
				positions[i] = pos;
			}
			return new ParameterizedQualifiedTypeReference(tokens, arguments, dim, positions);
		}
	}

	private TypeReference decodeType(char[] typeName, int length, int start, int end) {
		int identCount = 1;
		int dim = 0;
		int nameFragmentStart = this.namePos, nameFragmentEnd = -1;
		ArrayList fragments = null;
//{ObjectTeams: consider @anchor:
		boolean isAnchor = false;
// SH}
		typeLoop: while (this.namePos < length) {
			char currentChar = typeName[this.namePos];
			switch (currentChar) {
				case '?' :
					this.namePos++; // skip '?'
					while (typeName[this.namePos] == ' ') this.namePos++;
					switch(typeName[this.namePos]) {
						case 's' :
							checkSuper: {
								int max = TypeConstants.WILDCARD_SUPER.length-1;
								for (int ahead = 1; ahead < max; ahead++) {
									if (typeName[this.namePos+ahead] != TypeConstants.WILDCARD_SUPER[ahead+1]) {
										break checkSuper;
									}
								}
								this.namePos += max;
								Wildcard result = new Wildcard(Wildcard.SUPER);
								result.bound = decodeType(typeName, length, start, end);
								result.sourceStart = start;
								result.sourceEnd = end;
								return result;
							}
							break;
						case 'e' :
							checkExtends: {
								int max = TypeConstants.WILDCARD_EXTENDS.length-1;
								for (int ahead = 1; ahead < max; ahead++) {
									if (typeName[this.namePos+ahead] != TypeConstants.WILDCARD_EXTENDS[ahead+1]) {
										break checkExtends;
									}
								}
								this.namePos += max;
								Wildcard result = new Wildcard(Wildcard.EXTENDS);
								result.bound = decodeType(typeName, length, start, end);
								result.sourceStart = start;
								result.sourceEnd = end;
								return result;
							}
							break;
					}
					Wildcard result = new Wildcard(Wildcard.UNBOUND);
					result.sourceStart = start;
					result.sourceEnd = end;
					return result;
				case '[' :
					if (dim == 0) nameFragmentEnd = this.namePos-1;
					dim++;
					break;
				case ']' :
					break;
				case '>' :
				case ',' :
					break typeLoop;
				case '.' :
					if (nameFragmentStart < 0) nameFragmentStart = this.namePos+1; // member type name
					identCount ++;
					break;
				case '<' :
					// convert 1.5 specific constructs only if compliance is 1.5 or above
					if (!this.has1_5Compliance)
						break typeLoop;
					if (fragments == null) fragments = new ArrayList(2);
					nameFragmentEnd = this.namePos-1;
					char[][] identifiers = CharOperation.splitOn('.', typeName, nameFragmentStart, this.namePos);
					fragments.add(identifiers);
					this.namePos++; // skip '<'
					TypeReference[] arguments = decodeTypeArguments(typeName, length, start, end); // positionned on '>' at end
					fragments.add(arguments);
					identCount = 0;
					nameFragmentStart = -1;
					nameFragmentEnd = -1;
					// next increment will skip '>'
					break;
//{ObjectTeams: detecting the @anchor
				case '@':
					isAnchor = true;
					nameFragmentStart++;
					break;
// SH}
			}
			this.namePos++;
		}
		if (nameFragmentEnd < 0) nameFragmentEnd = this.namePos-1;
		if (fragments == null) { // non parameterized
			/* rebuild identifiers and dimensions */
			if (identCount == 1) { // simple type reference
				if (dim == 0) {
					char[] nameFragment;
					if (nameFragmentStart != 0 || nameFragmentEnd >= 0) {
						int nameFragmentLength = nameFragmentEnd - nameFragmentStart + 1;
						System.arraycopy(typeName, nameFragmentStart, nameFragment = new char[nameFragmentLength], 0, nameFragmentLength);
					} else {
						nameFragment = typeName;
					}
//{ObjectTeams: anchor reference detected?
					if (isAnchor) {
						// _OT$base may have been beautified to base, recover original:
						if (CharOperation.equals(nameFragment, IOTConstants.BASE))
							nameFragment = IOTConstants._OT_BASE;
						NameReference name = new SingleNameReference(nameFragment, ((long) start << 32) + end);
						return new TypeAnchorReference(name, start);
					}
// SH}
					return new SingleTypeReference(nameFragment, ((long) start << 32) + end);
				} else {
					int nameFragmentLength = nameFragmentEnd - nameFragmentStart + 1;
					char[] nameFragment = new char[nameFragmentLength];
					System.arraycopy(typeName, nameFragmentStart, nameFragment, 0, nameFragmentLength);
					return new ArrayTypeReference(nameFragment, dim, ((long) start << 32) + end);
				}
			} else { // qualified type reference
				long[] positions = new long[identCount];
				long pos = ((long) start << 32) + end;
				for (int i = 0; i < identCount; i++) {
					positions[i] = pos;
				}
				char[][] identifiers = CharOperation.splitOn('.', typeName, nameFragmentStart, nameFragmentEnd+1);
				if (dim == 0) {
//{ObjectTeams: anchor reference detected?
					if (isAnchor) {
						Reference anchor;
						// anchors including a 'base' token are encoded differently:
						if (CharOperation.equals(identifiers[identCount-1], IOTConstants.BASE)) {
							TypeReference qualification;
							if (identCount == 2) { // Single.base
								qualification = new SingleTypeReference(identifiers[0], positions[0]);
							} else {
								System.arraycopy(positions, 0, positions=new long[positions.length-1], 0, positions.length-1);
								qualification = new QualifiedTypeReference(CharOperation.subarray(identifiers, 0, identCount-1), positions);
							}
							anchor = new QualifiedBaseReference(qualification, end-3, end);
						} else {
							anchor = new QualifiedNameReference(identifiers, positions, start, end);
						}
						return new TypeAnchorReference(anchor, start);
					}
// SH}
					return new QualifiedTypeReference(identifiers, positions);
				} else {
					return new ArrayQualifiedTypeReference(identifiers, dim, positions);
				}
			}
		} else { // parameterized
			// rebuild type reference from available fragments: char[][], arguments, char[][], arguments...
			// check trailing qualified name
			if (nameFragmentStart > 0 && nameFragmentStart < length) {
				char[][] identifiers = CharOperation.splitOn('.', typeName, nameFragmentStart, nameFragmentEnd+1);
				fragments.add(identifiers);
			}
			int fragmentLength = fragments.size();
			if (fragmentLength == 2) {
				char[][] firstFragment = (char[][]) fragments.get(0);
				if (firstFragment.length == 1) {
					// parameterized single type
					return new ParameterizedSingleTypeReference(firstFragment[0], (TypeReference[]) fragments.get(1), dim, ((long) start << 32) + end);
				}
			}
			// parameterized qualified type
			identCount = 0;
			for (int i = 0; i < fragmentLength; i ++) {
				Object element = fragments.get(i);
				if (element instanceof char[][]) {
					identCount += ((char[][])element).length;
				}
			}
			char[][] tokens = new char[identCount][];
			TypeReference[][] arguments = new TypeReference[identCount][];
			int index = 0;
			for (int i = 0; i < fragmentLength; i ++) {
				Object element = fragments.get(i);
				if (element instanceof char[][]) {
					char[][] fragmentTokens = (char[][]) element;
					int fragmentTokenLength = fragmentTokens.length;
					System.arraycopy(fragmentTokens, 0, tokens, index, fragmentTokenLength);
					index += fragmentTokenLength;
				} else {
					arguments[index-1] = (TypeReference[]) element;
				}
			}
			long[] positions = new long[identCount];
			long pos = ((long) start << 32) + end;
			for (int i = 0; i < identCount; i++) {
				positions[i] = pos;
			}
			return new ParameterizedQualifiedTypeReference(tokens, arguments, dim, positions);
		}
	}

	private TypeReference[] decodeTypeArguments(char[] typeName, int length, int start, int end) {
		ArrayList argumentList = new ArrayList(1);
		int count = 0;
		argumentsLoop: while (this.namePos < length) {
			TypeReference argument = decodeType(typeName, length, start, end);
			count++;
			argumentList.add(argument);
			if (this.namePos >= length) break argumentsLoop;
			if (typeName[this.namePos] == '>') {
				break argumentsLoop;
			}
			this.namePos++; // skip ','
		}
		TypeReference[] typeArguments = new TypeReference[count];
		argumentList.toArray(typeArguments);
		return typeArguments;
	}

	private TypeReference[] decodeTypeArguments(String typeSignature, int length, int start, int end) {
		ArrayList argumentList = new ArrayList(1);
		int count = 0;
		argumentsLoop: while (this.namePos < length) {
			TypeReference argument = decodeType(typeSignature, length, start, end);
			count++;
			argumentList.add(argument);
			if (this.namePos >= length) break argumentsLoop;
			if (typeSignature.charAt(this.namePos) == Signature.C_GENERIC_END) {
				break argumentsLoop;
			}
		}
		TypeReference[] typeArguments = new TypeReference[count];
		argumentList.toArray(typeArguments);
		return typeArguments;
	}

	private char[][] extractIdentifiers(String typeSignature, int start, int endInclusive, int identCount) {
		char[][] result = new char[identCount][];
		int charIndex = start;
		int i = 0;
		while (charIndex < endInclusive) {
			char currentChar;
			if ((currentChar = typeSignature.charAt(charIndex)) == this.memberTypeSeparator || currentChar == Signature.C_DOT) {
				typeSignature.getChars(start, charIndex, result[i++] = new char[charIndex - start], 0);
				start = ++charIndex;
			} else
				charIndex++;
		}
		typeSignature.getChars(start, charIndex + 1, result[i++] = new char[charIndex - start + 1], 0);
		return result;
	}

//{ObjectTeams: OT Elements are handled uniformely in source/binary types:
	/**
	 * @param methodMapping
	 * @param compilationResult
	 * @return
	 */
	protected AbstractMethodMappingDeclaration convertCallout(
			IMethodMapping methodMapping, CompilationResult compilationResult)
	{
        if (methodMapping instanceof ICalloutToFieldMapping)
        {
            return convertCalloutToField(methodMapping, compilationResult);
        }
        else
        {
    		ICalloutMapping callout = (ICalloutMapping)methodMapping;
    		boolean hasSignature = callout.hasSignature();

    		CalloutMappingDeclaration result =
    			new CalloutMappingDeclaration(compilationResult);
    		result.declarationSourceStart = callout.getDeclarationSourceStart();
    		result.sourceStart            = callout.getSourceStart();
    		result.sourceEnd              = callout.getSourceEnd();
    		result.declarationSourceEnd   = callout.getDeclarationSourceEnd();
    		result.declaredModifiers	  = callout.getDeclaredModifiers();
    		result.hasSignature           = hasSignature;
    		result.setCalloutKind(callout.isOverride());
    		result.compilationResult = compilationResult;
    		result.roleMethodSpec = convert(callout.getRoleMethodHandle(), hasSignature);

    		MethodData baseHandle = callout.getBaseMethodHandle();
    		if (baseHandle != null)
    			result.baseMethodSpec = convert(baseHandle, hasSignature);

    		return result;
        }
	}

    protected AbstractMethodMappingDeclaration convertCalloutToField(
            IMethodMapping methodMapping,
            CompilationResult compilationResult)
    {
        ICalloutToFieldMapping callout = (ICalloutToFieldMapping)methodMapping;
        boolean hasSignature = callout.hasSignature();

        CalloutMappingDeclaration result =
            new CalloutMappingDeclaration(compilationResult);
		result.declarationSourceStart = callout.getDeclarationSourceStart();
		result.sourceStart            = callout.getSourceStart();
        result.sourceEnd              = callout.getSourceEnd();
        result.declarationSourceEnd   = callout.getDeclarationSourceEnd();
        result.hasSignature           = hasSignature;
    	result.setCalloutKind(callout.isOverride());
        result.compilationResult = compilationResult;
        result.roleMethodSpec = convert(callout.getRoleMethodHandle(), hasSignature);

        FieldData baseFieldHandle = callout.getBaseFieldHandle();
        result.baseMethodSpec = convert(baseFieldHandle, hasSignature);

        return result;
    }

	/**
	 * @param methodMapping
	 * @param compilationResult
	 */
	protected CallinMappingDeclaration convertCallin(
			IMethodMapping methodMapping, CompilationResult compilationResult)
	{
		ICallinMapping callinMapping = (ICallinMapping)methodMapping;
		boolean hasSignature = callinMapping.hasSignature();

		CallinMappingDeclaration result =
			new CallinMappingDeclaration(compilationResult);
		result.name = callinMapping.getName().toCharArray();
		result.declarationSourceStart = callinMapping.getDeclarationSourceStart();
		result.sourceStart            = callinMapping.getSourceStart();
		result.sourceEnd              = callinMapping.getSourceEnd();
		result.declarationSourceEnd   = callinMapping.getDeclarationSourceEnd();
		result.hasSignature = hasSignature;
		result.callinModifier = CallinMapping.convertModelToTerminalToken(callinMapping.getCallinKind());
		result.compilationResult = compilationResult;
		result.roleMethodSpec = convert(callinMapping.getRoleMethodHandle(), hasSignature);

		MethodData[] baseHandles = callinMapping.getBaseMethodHandles();
		int baseCount = baseHandles.length;
		result.baseMethodSpecs = new MethodSpec[baseCount];
		for(int idx = 0; idx < baseCount; idx++)
		{
			result.baseMethodSpecs[idx] =
				convert(baseHandles[idx], hasSignature);
		}
		return result;
	}

	protected MethodSpec convert(MethodData handle, boolean hasSignature)
	{
		//FIXME (haebor) : sourcepositions are not set!! try to find them elsewhere

		//initialize a dummy and then fill the values,
		//since there's no constructor that fits the needs of this initialization
		MethodSpec result = new MethodSpec(handle.getSelector().toCharArray(), 0);
        int start = result.sourceStart = handle.getSourceStart();
		int end   = result.sourceEnd = handle.getSourceEnd();
        result.hasSignature = hasSignature;
        result.isDeclaration = handle.isDeclaration();
        result.covariantReturn= handle.hasCovariantReturn();

		if(hasSignature)
		{
			String[] argTypes = handle.getArgumentTypes();
			String[] argNames = handle.getArgumentNames();
			result.arguments = new Argument[argTypes.length];
			for(int idx = 0; idx < argTypes.length; idx++)
			{
				//TODO (haebor) : again no sourceposition
				//(put them to the methodspec pos like convert(ISourceMethod,..))

				//don't care about access modifiers (see conversion of methods)
				result.arguments[idx] = new Argument(
						((argNames.length > idx) ? argNames[idx].toCharArray() : ("arg"+idx).toCharArray()), //$NON-NLS-1$
						0,
						createTypeReference(argTypes[idx]),
						ClassFileConstants.AccDefault);
			}
	        if (result.arguments != null) {
	        	result.argNeedsTranslation = new boolean[result.arguments.length];
	        	Arrays.fill(result.argNeedsTranslation, false);
	        }
	        String returnType = handle.getReturnType();
	        if (returnType != null && returnType.length() > 0) // empty string caused IllegalArgumentException below
				result.returnType = createTypeReference(returnType);

			/* convert type parameters (see convert(SourceMethod,..)) */
			char[][] typeParameterNames = handle.getTypeParameterNames();
			if (typeParameterNames != null) {
				int parameterCount = typeParameterNames.length;
				if (parameterCount > 0) { // method's type parameters must be null if no type parameter
					char[][][] typeParameterBounds = handle.getTypeParameterBounds();
					TypeParameter[] typeParams = new TypeParameter[parameterCount];
					for (int i = 0; i < parameterCount; i++) {
						typeParams[i] = createTypeParameter(typeParameterNames[i], typeParameterBounds[i], false/*baseBound*/, start, end);
					}
					result.typeParameters= typeParams;
				}
			}
		}
		return result;
	}

    protected MethodSpec convert(FieldData handle, boolean hasSignature)
    {
        //FIXME(gbr) source positions are not set!
        MethodSpec result = null;
        if (hasSignature)
        {
            TypeReference fieldType = createTypeReference(handle.getFieldType());
            result = new FieldAccessSpec(
                    handle.getSelector().toCharArray(), fieldType, 0, handle.isSetter());
        }
        else
        {
	        result = new FieldAccessSpec(handle.getSelector().toCharArray(), 0, handle.isSetter());
        }

        result.sourceStart = 0;
        result.sourceEnd = 0;
        result.hasSignature = hasSignature;

        return result;
    }

	protected TypeReference createTypeReference(String typeSignature) {
		return createTypeReference(typeSignature, 0, 0);
	}

// SH,haebor,gbr}
}

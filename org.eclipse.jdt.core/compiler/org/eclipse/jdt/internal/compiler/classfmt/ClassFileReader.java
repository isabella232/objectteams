/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 365992 - [builder] [null] Change of nullness for a parameter doesn't trigger a build for the files that call the method
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *								Bug 440687 - [compiler][batch][null] improve command line option for external annotations
 *     Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *         						bug 407191 - [1.8] Binary access support for type annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Predicate;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryModule;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IRecordComponent;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.*;

/**
 * OTDT changes:
 *
 * What: Support reading of OT-specific byte code attributes
 * How:  call AbstractAttribute.clearReadAttributes() and create individual attributes
 *       from constructor.
 *
 * What: Flag-handling: support AccSynthetic (as doc already claimed) and AccTeam/AccRole.
 *
 * @version $Id: ClassFileReader.java 23404 2010-02-03 14:10:22Z stephan $
 */
public class ClassFileReader extends ClassFileStruct implements IBinaryType {

	private int accessFlags;
	private char[] classFileName;
	private char[] className;
	private int classNameIndex;
	private int constantPoolCount;
	private AnnotationInfo[] annotations;
	private TypeAnnotationInfo[] typeAnnotations;
	private FieldInfo[] fields;
	private ModuleInfo moduleDeclaration;
	public char[] moduleName;
	private int fieldsCount;

	// initialized in case the .class file is a nested type
	private InnerClassInfo innerInfo;
	private InnerClassInfo[] innerInfos;
	private char[][] interfaceNames;
	private int interfacesCount;
	private char[][] permittedSubtypesNames;
	private int permittedSubtypesCount;
	private MethodInfo[] methods;
	private int methodsCount;
	private char[] signature;
	private char[] sourceName;
	private char[] sourceFileName;
	private char[] superclassName;
//{ObjectTeams: additional fields
	// one more kind of 'super':
	private char[] baseclassName;
    // After reading a class file its class-level attributes are stored here.
    private LinkedList<AbstractAttribute> classAttributes = new LinkedList<AbstractAttribute>();
    // for additional state see below: #roleBaseBindings
// SH}
	private long tagBits;
	private long version;
	private char[] enclosingTypeName;
	private char[][][] missingTypeNames;
	private int enclosingNameAndTypeIndex;
	private char[] enclosingMethod;
	private char[] nestHost;
	private int nestMembersCount;
	private char[][] nestMembers;
	private boolean isRecord;
	private int recordComponentsCount;
	private RecordComponentInfo[] recordComponents;

private static String printTypeModifiers(int modifiers) {
	java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
	java.io.PrintWriter print = new java.io.PrintWriter(out);

	if ((modifiers & ClassFileConstants.AccPublic) != 0) print.print("public "); //$NON-NLS-1$
	if ((modifiers & ClassFileConstants.AccPrivate) != 0) print.print("private "); //$NON-NLS-1$
	if ((modifiers & ClassFileConstants.AccFinal) != 0) print.print("final "); //$NON-NLS-1$
	if ((modifiers & ClassFileConstants.AccSuper) != 0) print.print("super "); //$NON-NLS-1$
	if ((modifiers & ClassFileConstants.AccInterface) != 0) print.print("interface "); //$NON-NLS-1$
	if ((modifiers & ClassFileConstants.AccAbstract) != 0) print.print("abstract "); //$NON-NLS-1$
	if ((modifiers & ExtraCompilerModifiers.AccSealed) != 0) print.print("sealed "); //$NON-NLS-1$
	print.flush();
	return out.toString();
}

public static ClassFileReader read(File file) throws ClassFormatException, IOException {
	return read(file, false);
}

public static ClassFileReader read(File file, boolean fullyInitialize) throws ClassFormatException, IOException {
	byte classFileBytes[] = Util.getFileByteContent(file);
	ClassFileReader classFileReader = new ClassFileReader(classFileBytes, file.getAbsolutePath().toCharArray());
	if (fullyInitialize) {
		classFileReader.initialize();
	}
	return classFileReader;
}

public static ClassFileReader read(InputStream stream, String fileName) throws ClassFormatException, IOException {
	return read(stream, fileName, false);
}

public static ClassFileReader read(InputStream stream, String fileName, boolean fullyInitialize) throws ClassFormatException, IOException {
	byte classFileBytes[] = Util.getInputStreamAsByteArray(stream, -1);
	ClassFileReader classFileReader = new ClassFileReader(classFileBytes, fileName.toCharArray());
	if (fullyInitialize) {
		classFileReader.initialize();
	}
	return classFileReader;
}

public static ClassFileReader read(
	java.util.zip.ZipFile zip,
	String filename)
	throws ClassFormatException, java.io.IOException {
		return read(zip, filename, false);
}

public static ClassFileReader readFromJrt(
		File jrt,
		IModule module,
		String filename)

		throws ClassFormatException, java.io.IOException {
		return JRTUtil.getClassfile(jrt, filename, module);
	}
public static ClassFileReader readFromModule(
		File jrt,
		String moduleName,
		String filename,
		Predicate<String> moduleNameFilter)

		throws ClassFormatException, java.io.IOException {
		return JRTUtil.getClassfile(jrt, filename, moduleName, moduleNameFilter);
}
public static ClassFileReader read(
	java.util.zip.ZipFile zip,
	String filename,
	boolean fullyInitialize)
	throws ClassFormatException, java.io.IOException {
	java.util.zip.ZipEntry ze = zip.getEntry(filename);
	if (ze == null)
		return null;
	byte classFileBytes[] = Util.getZipEntryByteContent(ze, zip);
	ClassFileReader classFileReader = new ClassFileReader(classFileBytes, filename.toCharArray());
	if (fullyInitialize) {
		classFileReader.initialize();
	}
	return classFileReader;
}

public static ClassFileReader read(String fileName) throws ClassFormatException, java.io.IOException {
	return read(fileName, false);
}

public static ClassFileReader read(String fileName, boolean fullyInitialize) throws ClassFormatException, java.io.IOException {
	return read(new File(fileName), fullyInitialize);
}

//{ObjectTeams: access to byte code and an offset.
	private int headerOffset;
	public int getHeaderOffset() {
		return this.headerOffset;
	}

	public byte[] getBytes() {
		return this.reference;
	}
// SH}

/**
 * @param classFileBytes Actual bytes of a .class file
 * @param fileName	Actual name of the file that contains the bytes, can be null
 *
 * @exception ClassFormatException
 */
public ClassFileReader(byte classFileBytes[], char[] fileName) throws ClassFormatException {
	this(classFileBytes, fileName, false);
}

/**
 * @param classFileBytes byte[]
 * 		Actual bytes of a .class file
 *
 * @param fileName char[]
 * 		Actual name of the file that contains the bytes, can be null
 *
 * @param fullyInitialize boolean
 * 		Flag to fully initialize the new object
 * @exception ClassFormatException
 */
public ClassFileReader(byte[] classFileBytes, char[] fileName, boolean fullyInitialize) throws ClassFormatException {
	// This method looks ugly but is actually quite simple, the constantPool is constructed
	// in 3 passes.  All non-primitive constant pool members that usually refer to other members
	// by index are tweaked to have their value in inst vars, this minor cost at read-time makes
	// all subsequent uses of the constant pool element faster.
	super(classFileBytes, null, 0);
	this.classFileName = fileName;
	int readOffset = 10;
	try {
		this.version = ((long)u2At(6) << 16) + u2At(4); // major<<16 + minor
		this.constantPoolCount = u2At(8);
		// Pass #1 - Fill in all primitive constants
		this.constantPoolOffsets = new int[this.constantPoolCount];
		for (int i = 1; i < this.constantPoolCount; i++) {
			int tag = u1At(readOffset);
			switch (tag) {
				case ClassFileConstants.Utf8Tag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += u2At(readOffset + 1);
					readOffset += ClassFileConstants.ConstantUtf8FixedSize;
					break;
				case ClassFileConstants.IntegerTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantIntegerFixedSize;
					break;
				case ClassFileConstants.FloatTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantFloatFixedSize;
					break;
				case ClassFileConstants.LongTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantLongFixedSize;
					i++;
					break;
				case ClassFileConstants.DoubleTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantDoubleFixedSize;
					i++;
					break;
				case ClassFileConstants.ClassTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantClassFixedSize;
					break;
				case ClassFileConstants.StringTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantStringFixedSize;
					break;
				case ClassFileConstants.FieldRefTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantFieldRefFixedSize;
					break;
				case ClassFileConstants.MethodRefTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantMethodRefFixedSize;
					break;
				case ClassFileConstants.InterfaceMethodRefTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantInterfaceMethodRefFixedSize;
					break;
				case ClassFileConstants.NameAndTypeTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantNameAndTypeFixedSize;
					break;
				case ClassFileConstants.MethodHandleTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantMethodHandleFixedSize;
					break;
				case ClassFileConstants.MethodTypeTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantMethodTypeFixedSize;
					break;
				case ClassFileConstants.DynamicTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantDynamicFixedSize;
					break;
				case ClassFileConstants.InvokeDynamicTag :
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantInvokeDynamicFixedSize;
					break;
				case ClassFileConstants.ModuleTag:
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantModuleFixedSize;
					break;
				case ClassFileConstants.PackageTag:
					this.constantPoolOffsets[i] = readOffset;
					readOffset += ClassFileConstants.ConstantPackageFixedSize;
					break;
			}
		}
//{ObjectTeams: store offset
		this.headerOffset = readOffset;
// SH}
		// Read and validate access flags
		this.accessFlags = u2At(readOffset);
		readOffset += 2;

		// Read the classname, use exception handlers to catch bad format
		this.classNameIndex = u2At(readOffset);
		if (this.classNameIndex != 0) {
			this.className = getConstantClassNameAt(this.classNameIndex);
		}
		readOffset += 2;
//{ObjectTeams: org.objectteams.Team requires a phantom flag:
		if (CharOperation.equals(this.className, "org/objectteams/Team".toCharArray())) //$NON-NLS-1$
			this.accessFlags |= ExtraCompilerModifiers.AccTeam;
// SH}

		// Read the superclass name, can be null for java.lang.Object
		int superclassNameIndex = u2At(readOffset);
		readOffset += 2;
		// if superclassNameIndex is equals to 0 there is no need to set a value for the
		// field this.superclassName. null is fine.
		if (superclassNameIndex != 0) {
			this.superclassName = getConstantClassNameAt(superclassNameIndex);
			if (CharOperation.equals(this.superclassName, TypeConstants.CharArray_JAVA_LANG_RECORD_SLASH)) {
				this.accessFlags |= ExtraCompilerModifiers.AccRecord;
			}
		}

		// Read the interfaces, use exception handlers to catch bad format
		this.interfacesCount = u2At(readOffset);
		readOffset += 2;
		if (this.interfacesCount != 0) {
			this.interfaceNames = new char[this.interfacesCount][];
			for (int i = 0; i < this.interfacesCount; i++) {
				this.interfaceNames[i] = getConstantClassNameAt(u2At(readOffset));
				readOffset += 2;
			}
		}
		// Read the fields, use exception handlers to catch bad format
		this.fieldsCount = u2At(readOffset);
		readOffset += 2;
		if (this.fieldsCount != 0) {
			FieldInfo field;
			this.fields = new FieldInfo[this.fieldsCount];
			for (int i = 0; i < this.fieldsCount; i++) {
				field = FieldInfo.createField(this.reference, this.constantPoolOffsets, readOffset, this.version);
				this.fields[i] = field;
				readOffset += field.sizeInBytes();
			}
		}
		// Read the methods
		this.methodsCount = u2At(readOffset);
		readOffset += 2;
		if (this.methodsCount != 0) {
			this.methods = new MethodInfo[this.methodsCount];
			boolean isAnnotationType = (this.accessFlags & ClassFileConstants.AccAnnotation) != 0;
			for (int i = 0; i < this.methodsCount; i++) {
				this.methods[i] = isAnnotationType
					? AnnotationMethodInfo.createAnnotationMethod(this.reference, this.constantPoolOffsets, readOffset, this.version)
					: MethodInfo.createMethod(this.reference, this.constantPoolOffsets, readOffset, this.version);
				readOffset += this.methods[i].sizeInBytes();
			}
		}

		// Read the attributes
		int attributesCount = u2At(readOffset);
		readOffset += 2;

		for (int i = 0; i < attributesCount; i++) {
			int utf8Offset = this.constantPoolOffsets[u2At(readOffset)];
			char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
			if (attributeName.length == 0) {
				readOffset += (6 + u4At(readOffset + 2));
				continue;
			}
//{ObjectTeams: integrate intepretation of OT-bytecode attributes (most is orig):
			switch(attributeName[0] ) {
		// OT:
				case 'C' :
					if (attributeName.length > 6)
						switch (attributeName[6]) {
						// Callin...
						case 'M':
							if (CharOperation.equals(attributeName, IOTConstants.CALLIN_METHOD_MAPPINGS))
								this.classAttributes.add(new CallinMethodMappingsAttribute(this, readOffset+6, this.constantPoolOffsets));
							break;
						case 'P':
							if (CharOperation.equals(attributeName, IOTConstants.CALLIN_PRECEDENCE))
								this.classAttributes.add(new CallinPrecedenceAttribute(this, readOffset+6, this.constantPoolOffsets));
							break;
						case 'R':
							if (CharOperation.equals(attributeName, IOTConstants.ROLE_BASE_BINDINGS))
								evaluateRoleBaseBindings(readOffset+6);
							break;
						// Callou...
						case 't':
							if (CharOperation.equals(attributeName, IOTConstants.CALLOUT_MAPPINGS))
								this.classAttributes.add(new CalloutMappingsAttribute(this, readOffset+6, this.constantPoolOffsets));
							break;
						// ClassI...
						case 'n':
							if (CharOperation.equals(attributeName, IOTConstants.CLASS_INFO_ANCHORS))
				               	this.classAttributes.add(new CPTypeAnchorAttribute(this, readOffset+6, this.constantPoolOffsets));
							break;
						}
					break;
		// :TO
				case 'E' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.EnclosingMethodName)) {
						utf8Offset =
							this.constantPoolOffsets[u2At(this.constantPoolOffsets[u2At(readOffset + 6)] + 1)];
 						this.enclosingTypeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
						this.enclosingNameAndTypeIndex = u2At(readOffset + 8);
					}
					break;
				case 'D' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.DeprecatedName)) {
						this.accessFlags |= ClassFileConstants.AccDeprecated;
					}
					break;
				case 'I' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.InnerClassName)) {
						int innerOffset = readOffset + 6;
						int number_of_classes = u2At(innerOffset);
						if (number_of_classes != 0) {
							innerOffset+= 2;
							this.innerInfos = new InnerClassInfo[number_of_classes];
							for (int j = 0; j < number_of_classes; j++) {
								this.innerInfos[j] =
									new InnerClassInfo(this.reference, this.constantPoolOffsets, innerOffset);
								if (this.classNameIndex == this.innerInfos[j].innerClassNameIndex) {
									this.innerInfo = this.innerInfos[j];
								}
								innerOffset += 8;
							}
							if (this.innerInfo != null) {
								char[] enclosingType = this.innerInfo.getEnclosingTypeName();
								if (enclosingType != null) {
									this.enclosingTypeName = enclosingType;
								}
							}
						}
					} else if (CharOperation.equals(attributeName, AttributeNamesConstants.InconsistentHierarchy)) {
						this.tagBits |= TagBits.HierarchyHasProblems;
					}
					break;
		// OT:
				case 'O' :
					if (CharOperation.equals(attributeName, IOTConstants.OT_CLASS_FLAGS))
			            this.classAttributes.add(WordValueAttribute.readClassFlags(this, readOffset+6, this.constantPoolOffsets));
					else if (CharOperation.equals(attributeName, IOTConstants.OTSPECIAL_ACCESS))
			            this.classAttributes.add(new OTSpecialAccessAttribute(this, readOffset+6, this.constantPoolOffsets));
					else if (CharOperation.equals(attributeName, IOTConstants.OT_COMPILER_VERSION))
			            this.classAttributes.add(WordValueAttribute.readCompilerVersion(this, readOffset+6, this.constantPoolOffsets));
					else if (CharOperation.equals(attributeName, OTDynCallinBindingsAttribute.ATTRIBUTE_NAME))
			            this.classAttributes.add(new OTDynCallinBindingsAttribute(this, readOffset+6, this.constantPoolOffsets));
			        break;
		// :TO
				case 'S' :
					if (attributeName.length > 2) {
						switch(attributeName[1]) {
							case 'o' :
								if (CharOperation.equals(attributeName, AttributeNamesConstants.SourceName)) {
									utf8Offset = this.constantPoolOffsets[u2At(readOffset + 6)];
									this.sourceFileName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
								}
								break;
							case 'y' :
								if (CharOperation.equals(attributeName, AttributeNamesConstants.SyntheticName)) {
									this.accessFlags |= ClassFileConstants.AccSynthetic;
								}
								break;
							case 'i' :
								if (CharOperation.equals(attributeName, AttributeNamesConstants.SignatureName)) {
									utf8Offset = this.constantPoolOffsets[u2At(readOffset + 6)];
									this.signature = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
								}
		// OT:
								break;
							case 't' :
								if (CharOperation.equals(attributeName, IOTConstants.STATIC_REPLACE_BINDINGS))
						        	this.classAttributes.add(new StaticReplaceBindingsAttribute(this, readOffset+6, this.constantPoolOffsets));
		// :TO
						}
					}
					break;
				case 'R' :
		// OT:
				 if (attributeName.length > 2) {
				  switch(attributeName[1]) {
				  case 'u':
					// orig:
					if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleAnnotationsName)) {
						decodeAnnotations(readOffset, true);
					} else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleAnnotationsName)) {
						decodeAnnotations(readOffset, false);
					} else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleTypeAnnotationsName)) {
						decodeTypeAnnotations(readOffset, true);
					} else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleTypeAnnotationsName)) {
						decodeTypeAnnotations(readOffset, false);
					} 	else if (CharOperation.equals(attributeName, AttributeNamesConstants.RecordClass)) {
						decodeRecords(readOffset, attributeName);
					}

					break;
					// :giro
				  case 'o':
					if (CharOperation.equals(attributeName, IOTConstants.ROLE_LOCAL_TYPES))
						this.classAttributes.add(new RoleLocalTypesAttribute(this, readOffset+6, this.constantPoolOffsets));
					if (CharOperation.equals(attributeName, IOTConstants.ROLE_FILES))
						this.classAttributes.add(new RoleFilesAttribute(this, readOffset+6, this.constantPoolOffsets));

					break;
				  }
				 }
				 break;
		// :TO
				case 'M' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.MissingTypesName)) {
						// decode the missing types
						int missingTypeOffset = readOffset + 6;
						int numberOfMissingTypes = u2At(missingTypeOffset);
						if (numberOfMissingTypes != 0) {
							this.missingTypeNames = new char[numberOfMissingTypes][][];
							missingTypeOffset += 2;
							for (int j = 0; j < numberOfMissingTypes; j++) {
								utf8Offset = this.constantPoolOffsets[u2At(this.constantPoolOffsets[u2At(missingTypeOffset)] + 1)];
								char[] missingTypeConstantPoolName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
								this.missingTypeNames[j] = CharOperation.splitOn('/', missingTypeConstantPoolName);
								missingTypeOffset += 2;
							}
						}
					} else if (CharOperation.equals(attributeName, AttributeNamesConstants.ModuleName)) {
						this.moduleDeclaration = ModuleInfo.createModule(this.reference, this.constantPoolOffsets, readOffset);
						this.moduleName = this.moduleDeclaration.name();
					}
					break;
				case 'N' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.NestHost)) {
						utf8Offset =
							this.constantPoolOffsets[u2At(this.constantPoolOffsets[u2At(readOffset + 6)] + 1)];
 						this.nestHost = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
					} else if (CharOperation.equals(attributeName, AttributeNamesConstants.NestMembers)) {
						int offset = readOffset + 6;
						this.nestMembersCount = u2At(offset);
						if (this.nestMembersCount != 0) {
							offset += 2;
							this.nestMembers = new char[this.nestMembersCount][];
							for (int j = 0; j < this.nestMembersCount; j++) {
								utf8Offset =
									this.constantPoolOffsets[u2At(this.constantPoolOffsets[u2At(offset)] + 1)];
		 						this.nestMembers[j] = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		 						offset += 2;
							}
						}
					}
					break;
// orig:
				case 'P' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.PermittedSubclasses)) {
						int offset = readOffset + 6;
						this.permittedSubtypesCount = u2At(offset);
						if (this.permittedSubtypesCount != 0) {
							this.accessFlags |= ExtraCompilerModifiers.AccSealed;
							offset += 2;
							this.permittedSubtypesNames = new char[this.permittedSubtypesCount][];
							for (int j = 0; j < this.permittedSubtypesCount; j++) {
								utf8Offset =
									this.constantPoolOffsets[u2At(this.constantPoolOffsets[u2At(offset)] + 1)];
		 						this.permittedSubtypesNames[j] = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		 						offset += 2;
							}
						}
					}
// OT:
			        if (CharOperation.equals(attributeName, IOTConstants.PLAYEDBY_NAME)) {
						SingleValueAttribute playedByAttr = SingleValueAttribute.readPlayedBy(this, readOffset+6, this.constantPoolOffsets);
						this.classAttributes.add(playedByAttr);
						this.baseclassName = playedByAttr.getValue();
					}
// :TO
			        break;
			}
// SH}
			readOffset += (6 + u4At(readOffset + 2));
		}
		if (this.moduleDeclaration != null && this.annotations != null) {
			this.moduleDeclaration.setAnnotations(this.annotations, this.tagBits, fullyInitialize);
			this.annotations = null;
		}
		if (fullyInitialize) {
			initialize();
		}
	} catch(ClassFormatException e) {
		throw e;
	} catch (Exception e) {
		throw new ClassFormatException(e,
			this.classFileName,
			ClassFormatException.ErrTruncatedInput,
			readOffset);
	}
}

private void decodeRecords(int readOffset, char[] attributeName) {
	if (CharOperation.equals(attributeName, AttributeNamesConstants.RecordClass)) {
		this.isRecord = true;
		int offset = readOffset + 6;
		this.recordComponentsCount = u2At(offset);
		if (this.recordComponentsCount != 0) {
			offset += 2;
			this.recordComponents = new RecordComponentInfo[this.recordComponentsCount];
			for (int j = 0; j < this.recordComponentsCount; j++) {
				RecordComponentInfo component = RecordComponentInfo.createComponent(this.reference, this.constantPoolOffsets, offset, this.version);
				this.recordComponents[j] = component;
				offset += component.sizeInBytes();
			}
		}
	}
}

public char[] getNestHost() {
	return this.nestHost;
}

@Override
public ExternalAnnotationStatus getExternalAnnotationStatus() {
	return ExternalAnnotationStatus.NOT_EEA_CONFIGURED;
}
/**
 * Conditionally add external annotations to the mix.
 * If 'member' is given it must be either of IBinaryField or IBinaryMethod, in which case we're seeking annotations for that member.
 * Otherwise we're seeking annotations for top-level elements of a type (type parameters & super types).
 */
@Override
public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker walker, Object member, LookupEnvironment environment) {
	return walker;
}

//{ObjectTeams: OT attributes:

// role-base-bindings per team:
HashMap<String, String> roleBaseBindings;
// build the role-base map:
void evaluateRoleBaseBindings(int offset) {
	int n = u2At(offset);
	if (n == 0) return;
	this.roleBaseBindings = new HashMap<String, String>();
	offset+=2;
	for (int i=0; i<n; i++) {
		// role name:
		int utf8Offset = this.constantPoolOffsets[u2At(offset)];
		offset += 2;
		char[] roleName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		// use the simple name as key
		int otpos = CharOperation.indexOf(IOTConstants.OT_DELIM_NAME, roleName, true);
		if (otpos != -1)
			roleName = CharOperation.subarray(roleName, otpos+IOTConstants.OT_DELIM_LEN, -1);
		// base name:
		utf8Offset = this.constantPoolOffsets[u2At(offset)];
		offset += 2;
		char[] baseName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		// store mapping:
		this.roleBaseBindings.put(String.valueOf(roleName), String.valueOf(baseName));
	}
}
/**
 * Answer the name of the base class bound to the given role or null.
 * @param roleName simple source type name (no qualification, no __OT__ prefix)
 * @return qualified base class name, encoded like "java.lang.Object"
 */
public String getBaseclassName(String roleName) {
	if (this.roleBaseBindings == null)
		return null;
	return this.roleBaseBindings.get(roleName);
}
public void evaluateOTClassFlagsAttribute(BinaryTypeBinding binaryTypeBinding, LookupEnvironment environment, char[][][] missingTypeNames) {
	for (AbstractAttribute attr : this.classAttributes)
		if (attr.nameEquals(IOTConstants.OT_CLASS_FLAGS))
			attr.evaluate(binaryTypeBinding, environment, missingTypeNames);
}

public void evaluateOtherOTAttributes(BinaryTypeBinding binaryTypeBinding, LookupEnvironment environment, char[][][] missingTypeNames) {
	for (AbstractAttribute attr : this.classAttributes)
		if (!attr.nameEquals(IOTConstants.OT_CLASS_FLAGS))
			attr.evaluate(binaryTypeBinding, environment, missingTypeNames);
}
public Collection<AbstractAttribute> getOTAttributes() {
	return this.classAttributes;
}
// SH}

/**
 * Answer the receiver's access flags.  The value of the access_flags
 *	item is a mask of modifiers used with class and interface declarations.
 *  @return int
 */
public int accessFlags() {
	return this.accessFlags;
}

private void decodeAnnotations(int offset, boolean runtimeVisible) {
	int numberOfAnnotations = u2At(offset + 6);
	if (numberOfAnnotations > 0) {
		int readOffset = offset + 8;
		AnnotationInfo[] newInfos = null;
		int newInfoCount = 0;
		for (int i = 0; i < numberOfAnnotations; i++) {
			// With the last parameter being 'false', the data structure will not be flushed out
			AnnotationInfo newInfo = new AnnotationInfo(this.reference, this.constantPoolOffsets, readOffset, runtimeVisible, false);
			readOffset += newInfo.readOffset;
			long standardTagBits = newInfo.standardAnnotationTagBits;
			if (standardTagBits != 0) {
				this.tagBits |= standardTagBits;
				if (this.version < ClassFileConstants.JDK9 || (standardTagBits & TagBits.AnnotationDeprecated) == 0)
					continue;
			}
			if (newInfos == null)
				newInfos = new AnnotationInfo[numberOfAnnotations - i];
			newInfos[newInfoCount++] = newInfo;
		}
		if (newInfos == null)
			return; // nothing to record in this.annotations

		if (this.annotations == null) {
			if (newInfoCount != newInfos.length)
				System.arraycopy(newInfos, 0, newInfos = new AnnotationInfo[newInfoCount], 0, newInfoCount);
			this.annotations = newInfos;
		} else {
			int length = this.annotations.length;
			AnnotationInfo[] temp = new AnnotationInfo[length + newInfoCount];
			System.arraycopy(this.annotations, 0, temp, 0, length);
			System.arraycopy(newInfos, 0, temp, length, newInfoCount);
			this.annotations = temp;
		}
	}
}

private void decodeTypeAnnotations(int offset, boolean runtimeVisible) {
	int numberOfAnnotations = u2At(offset + 6);
	if (numberOfAnnotations > 0) {
		int readOffset = offset + 8;
		TypeAnnotationInfo[] newInfos = null;
		newInfos = new TypeAnnotationInfo[numberOfAnnotations];
		for (int i = 0; i < numberOfAnnotations; i++) {
			// With the last parameter being 'false', the data structure will not be flushed out
			TypeAnnotationInfo newInfo = new TypeAnnotationInfo(this.reference, this.constantPoolOffsets, readOffset, runtimeVisible, false);
			readOffset += newInfo.readOffset;
			newInfos[i] = newInfo;
		}
		if (this.typeAnnotations == null) {
			this.typeAnnotations = newInfos;
		} else {
			int length = this.typeAnnotations.length;
			TypeAnnotationInfo[] temp = new TypeAnnotationInfo[length + numberOfAnnotations];
			System.arraycopy(this.typeAnnotations, 0, temp, 0, length);
			System.arraycopy(newInfos, 0, temp, length, numberOfAnnotations);
			this.typeAnnotations = temp;
		}
	}
}

/**
 * @return the annotations or null if there is none.
 */
@Override
public IBinaryAnnotation[] getAnnotations() {
	return this.annotations;
}

/**
 * @return the type annotations or null if there is none.
 */
@Override
public IBinaryTypeAnnotation[] getTypeAnnotations() {
	return this.typeAnnotations;
}

/**
 * Answer the char array that corresponds to the class name of the constant class.
 * constantPoolIndex is the index in the constant pool that is a constant class entry.
 *
 * @param constantPoolIndex int
 * @return char[]
 */
private char[] getConstantClassNameAt(int constantPoolIndex) {
	int utf8Offset = this.constantPoolOffsets[u2At(this.constantPoolOffsets[constantPoolIndex] + 1)];
	return utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
}

/**
 * Answer the int array that corresponds to all the offsets of each entry in the constant pool
 *
 * @return int[]
 */
public int[] getConstantPoolOffsets() {
	return this.constantPoolOffsets;
}

@Override
public char[] getEnclosingMethod() {
	if (this.enclosingNameAndTypeIndex <= 0) {
		return null;
	}
	if (this.enclosingMethod == null) {
		// read the name
		StringBuffer buffer = new StringBuffer();

		int nameAndTypeOffset = this.constantPoolOffsets[this.enclosingNameAndTypeIndex];
		int utf8Offset = this.constantPoolOffsets[u2At(nameAndTypeOffset + 1)];
		buffer.append(utf8At(utf8Offset + 3, u2At(utf8Offset + 1)));

		utf8Offset = this.constantPoolOffsets[u2At(nameAndTypeOffset + 3)];
		buffer.append(utf8At(utf8Offset + 3, u2At(utf8Offset + 1)));

		this.enclosingMethod = String.valueOf(buffer).toCharArray();
	}
	return this.enclosingMethod;
}

/*
 * Answer the resolved compoundName of the enclosing type
 * or null if the receiver is a top level type.
 */
@Override
public char[] getEnclosingTypeName() {
	return this.enclosingTypeName;
}

/**
 * Answer the receiver's this.fields or null if the array is empty.
 * @return org.eclipse.jdt.internal.compiler.api.IBinaryField[]
 */
@Override
public IBinaryField[] getFields() {
	return this.fields;
}
/**
 * @see IBinaryType#getModule()
 */
@Override
public char[] getModule() {
	return this.moduleName;
}
/**
 * Returns the module declaration that this class file represents. This will be
 * null for non module-info class files.
 *
 * @return the module declaration this represents
 */
public IBinaryModule getModuleDeclaration() {
	return this.moduleDeclaration;
}

/**
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
@Override
public char[] getFileName() {
	return this.classFileName;
}

@Override
public char[] getGenericSignature() {
	return this.signature;
}

/**
 * Answer the source name if the receiver is a inner type. Return null if it is an anonymous class or if the receiver is a top-level class.
 * e.g.
 * public class A {
 *	public class B {
 *	}
 *	public void foo() {
 *		class C {}
 *	}
 *	public Runnable bar() {
 *		return new Runnable() {
 *			public void run() {}
 *		};
 *	}
 * }
 * It returns {'B'} for the member A$B
 * It returns null for A
 * It returns {'C'} for the local class A$1$C
 * It returns null for the anonymous A$1
 * @return char[]
 */
public char[] getInnerSourceName() {
	if (this.innerInfo != null)
		return this.innerInfo.getSourceName();
	return null;
}

@Override
public char[][] getInterfaceNames() {
	return this.interfaceNames;
}

@Override
public char[][] getPermittedSubtypeNames() {
	return this.permittedSubtypesNames;
}

@Override
public IBinaryNestedType[] getMemberTypes() {
	// we might have some member types of the current type
	if (this.innerInfos == null) return null;

	int length = this.innerInfos.length - (this.innerInfo != null ? 1 : 0);
	if (length != 0) {
		IBinaryNestedType[] memberTypes =
				new IBinaryNestedType[length];
		int memberTypeIndex = 0;
		for (InnerClassInfo currentInnerInfo : this.innerInfos) {
			int outerClassNameIdx = currentInnerInfo.outerClassNameIndex;
			int innerNameIndex = currentInnerInfo.innerNameIndex;
			/*
			 * Checking that outerClassNameIDx is different from 0 should be enough to determine if an inner class
			 * attribute entry is a member class, but due to the bug:
			 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14592
			 * we needed to add an extra check. So we check that innerNameIndex is different from 0 as well.
			 *
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49879
			 * From JavaMail 1.2, the class javax.mail.Folder contains an anonymous class in the
			 * terminateQueue() method for which the inner attribute is boggus.
			 * outerClassNameIdx is not 0, innerNameIndex is not 0, but the sourceName length is 0.
			 * So I added this extra check to filter out this anonymous class from the
			 * member types.
			 */
			if (outerClassNameIdx != 0
				&& innerNameIndex != 0
				&& outerClassNameIdx == this.classNameIndex
				&& currentInnerInfo.getSourceName().length != 0) {
				memberTypes[memberTypeIndex++] = currentInnerInfo;
			}
		}
		if (memberTypeIndex == 0) return null;
		if (memberTypeIndex != memberTypes.length) {
			// we need to resize the memberTypes array. Some local or anonymous classes
			// are present in the current class.
			System.arraycopy(
				memberTypes,
				0,
				(memberTypes = new IBinaryNestedType[memberTypeIndex]),
				0,
				memberTypeIndex);
		}
		return memberTypes;
	}
	return null;
}

/**
 * Answer the receiver's this.methods or null if the array is empty.
 * @return org.eclipse.jdt.internal.compiler.api.env.IBinaryMethod[]
 */
@Override
public IBinaryMethod[] getMethods() {
	return this.methods;
}

/*
public static void main(String[] args) throws ClassFormatException, IOException {
	if (args == null || args.length != 1) {
		System.err.println("ClassFileReader <filename>"); //$NON-NLS-1$
		System.exit(1);
	}
	File file = new File(args[0]);
	ClassFileReader reader = read(file, true);
	if (reader.annotations != null) {
		System.err.println();
		for (int i = 0; i < reader.annotations.length; i++)
			System.err.println(reader.annotations[i]);
	}
	System.err.print("class "); //$NON-NLS-1$
	System.err.print(reader.getName());
	char[] superclass = reader.getSuperclassName();
	if (superclass != null) {
		System.err.print(" extends "); //$NON-NLS-1$
		System.err.print(superclass);
	}
	System.err.println();
	char[][] interfaces = reader.getInterfaceNames();
	if (interfaces != null && interfaces.length > 0) {
		System.err.print(" implements "); //$NON-NLS-1$
		for (int i = 0; i < interfaces.length; i++) {
			if (i != 0) System.err.print(", "); //$NON-NLS-1$
			System.err.println(interfaces[i]);
		}
	}
	System.err.println();
	System.err.println('{');
	if (reader.fields != null) {
		for (int i = 0; i < reader.fields.length; i++) {
			System.err.println(reader.fields[i]);
			System.err.println();
		}
	}
	if (reader.methods != null) {
		for (int i = 0; i < reader.methods.length; i++) {
			System.err.println(reader.methods[i]);
			System.err.println();
		}
	}
	System.err.println();
	System.err.println('}');
}
*/
@Override
public char[][][] getMissingTypeNames() {
	return this.missingTypeNames;
}

/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 * Set the AccDeprecated and AccSynthetic bits if necessary
 * @return int
 */
@Override
public int getModifiers() {
	int modifiers;
	if (this.innerInfo != null) {
		modifiers = this.innerInfo.getModifiers()
			| (this.accessFlags & ClassFileConstants.AccDeprecated)
			| (this.accessFlags & ClassFileConstants.AccSynthetic);
	} else {
		modifiers = this.accessFlags;
	}
//{ObjectTeams:
	for (AbstractAttribute attribute : getOTAttributes()) {
		if (attribute.nameEquals(IOTConstants.OT_CLASS_FLAGS)) {
			modifiers |= ((WordValueAttribute) attribute).classFlagsToModifiers();
			break;
		}
	}
// SH}
	if (this.permittedSubtypesCount > 0)
		modifiers |= ExtraCompilerModifiers.AccSealed;
	return modifiers;
}

@Override
public char[] getName() {
	return this.className;
}

@Override
public char[] getSourceName() {
	if (this.sourceName != null)
		return this.sourceName;

	char[] name = getInnerSourceName(); // member or local scenario
	if (name == null) {
		name = getName(); // extract from full name
		int start;
		if (isAnonymous()) {
			start = CharOperation.indexOf('$', name, CharOperation.lastIndexOf('/', name) + 1) + 1;
		} else {
			start = CharOperation.lastIndexOf('/', name) + 1;
		}
		if (start > 0) {
			char[] newName = new char[name.length - start];
			System.arraycopy(name, start, newName, 0, newName.length);
			name = newName;
		}
	}
	return this.sourceName = name;
}

@Override
public char[] getSuperclassName() {
	return this.superclassName;
}

//{ObjectTeams: one more kind of 'super':
/**
 * Answer the resolved name of the receiver's baseclass as
 * given by a PlayedBy attribute, or null if not given.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[]
 */
public char[] getBaseclassName() {
	return this.baseclassName;
}
// SH}
@Override
public long getTagBits() {
	return this.tagBits;
}

/**
 * Answer the major/minor version defined in this class file according to the VM spec.
 * as a long: (major<<16)+minor
 * @return the major/minor version found
 */
public long getVersion() {
	return this.version;
}

private boolean hasNonSyntheticFieldChanges(FieldInfo[] currentFieldInfos, FieldInfo[] otherFieldInfos) {
	int length1 = currentFieldInfos == null ? 0 : currentFieldInfos.length;
	int length2 = otherFieldInfos == null ? 0 : otherFieldInfos.length;
	int index1 = 0;
	int index2 = 0;

	end : while (index1 < length1 && index2 < length2) {
		while (currentFieldInfos[index1].isSynthetic()) {
			if (++index1 >= length1) break end;
		}
		while (otherFieldInfos[index2].isSynthetic()) {
			if (++index2 >= length2) break end;
		}
		if (hasStructuralFieldChanges(currentFieldInfos[index1++], otherFieldInfos[index2++]))
			return true;
	}

	while (index1 < length1) {
		if (!currentFieldInfos[index1++].isSynthetic()) return true;
	}
	while (index2 < length2) {
		if (!otherFieldInfos[index2++].isSynthetic()) return true;
	}
	return false;
}

private boolean hasNonSyntheticMethodChanges(MethodInfo[] currentMethodInfos, MethodInfo[] otherMethodInfos) {
	int length1 = currentMethodInfos == null ? 0 : currentMethodInfos.length;
	int length2 = otherMethodInfos == null ? 0 : otherMethodInfos.length;
	int index1 = 0;
	int index2 = 0;

	MethodInfo m;
	end : while (index1 < length1 && index2 < length2) {
		while ((m = currentMethodInfos[index1]).isSynthetic() || m.isClinit()) {
			if (++index1 >= length1) break end;
		}
		while ((m = otherMethodInfos[index2]).isSynthetic() || m.isClinit()) {
			if (++index2 >= length2) break end;
		}
		if (hasStructuralMethodChanges(currentMethodInfos[index1++], otherMethodInfos[index2++]))
			return true;
	}

	while (index1 < length1) {
		if (!((m = currentMethodInfos[index1++]).isSynthetic() || m.isClinit())) return true;
	}
	while (index2 < length2) {
		if (!((m = otherMethodInfos[index2++]).isSynthetic() || m.isClinit())) return true;
	}
	return false;
}

/**
 * Check if the receiver has structural changes compare to the byte array in argument.
 * Structural changes are:
 * - modifiers changes for the class, the this.fields or the this.methods
 * - signature changes for this.fields or this.methods.
 * - changes in the number of this.fields or this.methods
 * - changes for field constants
 * - changes for thrown exceptions
 * - change for the super class or any super interfaces.
 * - changes for member types name or modifiers
 * If any of these changes occurs, the method returns true. false otherwise.
 * The synthetic fields are included and the members are not required to be sorted.
 * @param newBytes the bytes of the .class file we want to compare the receiver to
 * @return boolean Returns true is there is a structural change between the two .class files, false otherwise
 */
public boolean hasStructuralChanges(byte[] newBytes) {
	return hasStructuralChanges(newBytes, true, true);
}

/**
 * Check if the receiver has structural changes compare to the byte array in argument.
 * Structural changes are:
 * - modifiers changes for the class, the this.fields or the this.methods
 * - signature changes for this.fields or this.methods.
 * - changes in the number of this.fields or this.methods
 * - changes for field constants
 * - changes for thrown exceptions
 * - change for the super class or any super interfaces.
 * - changes for member types name or modifiers
 * If any of these changes occurs, the method returns true. false otherwise.
 * @param newBytes the bytes of the .class file we want to compare the receiver to
 * @param orderRequired a boolean indicating whether the members should be sorted or not
 * @param excludesSynthetic a boolean indicating whether the synthetic members should be used in the comparison
 * @return boolean Returns true is there is a structural change between the two .class files, false otherwise
 */
public boolean hasStructuralChanges(byte[] newBytes, boolean orderRequired, boolean excludesSynthetic) {
	try {
		ClassFileReader newClassFile =
			new ClassFileReader(newBytes, this.classFileName);
		// type level comparison
		// modifiers
		if (getModifiers() != newClassFile.getModifiers())
			return true;

		// only consider a portion of the tagbits which indicate a structural change for dependents
		// e.g. @Override change has no influence outside
		long OnlyStructuralTagBits = TagBits.AnnotationTargetMASK // different @Target status ?
			| TagBits.AnnotationDeprecated // different @Deprecated status ?
			| TagBits.AnnotationRetentionMASK // different @Retention status ?
			| TagBits.HierarchyHasProblems; // different hierarchy status ?

		// meta-annotations
		if ((getTagBits() & OnlyStructuralTagBits) != (newClassFile.getTagBits() & OnlyStructuralTagBits))
			return true;
		// annotations
		if (hasStructuralAnnotationChanges(getAnnotations(), newClassFile.getAnnotations()))
			return true;
		if (this.version >= ClassFileConstants.JDK1_8
				&& hasStructuralTypeAnnotationChanges(getTypeAnnotations(), newClassFile.getTypeAnnotations()))
			return true;

		// generic signature
		if (!CharOperation.equals(getGenericSignature(), newClassFile.getGenericSignature()))
			return true;
		// superclass
		if (!CharOperation.equals(getSuperclassName(), newClassFile.getSuperclassName()))
			return true;
		// interfaces
		char[][] newInterfacesNames = newClassFile.getInterfaceNames();
		if (this.interfaceNames != newInterfacesNames) { // TypeConstants.NoSuperInterfaces
			int newInterfacesLength = newInterfacesNames == null ? 0 : newInterfacesNames.length;
			if (newInterfacesLength != this.interfacesCount)
				return true;
			for (int i = 0, max = this.interfacesCount; i < max; i++)
				if (!CharOperation.equals(this.interfaceNames[i], newInterfacesNames[i]))
					return true;
		}

		// permitted sub-types
		char[][] newPermittedSubtypeNames = newClassFile.getPermittedSubtypeNames();
		if (this.permittedSubtypesNames != newPermittedSubtypeNames) {
			int newPermittedSubtypesLength = newPermittedSubtypeNames == null ? 0 : newPermittedSubtypeNames.length;
			if (newPermittedSubtypesLength != this.permittedSubtypesCount)
				return true;
			for (int i = 0, max = this.permittedSubtypesCount; i < max; i++)
				if (!CharOperation.equals(this.permittedSubtypesNames[i], newPermittedSubtypeNames[i]))
					return true;
		}

		// member types
		IBinaryNestedType[] currentMemberTypes = getMemberTypes();
		IBinaryNestedType[] otherMemberTypes = newClassFile.getMemberTypes();
		if (currentMemberTypes != otherMemberTypes) { // TypeConstants.NoMemberTypes
			int currentMemberTypeLength = currentMemberTypes == null ? 0 : currentMemberTypes.length;
			int otherMemberTypeLength = otherMemberTypes == null ? 0 : otherMemberTypes.length;
			if (currentMemberTypeLength != otherMemberTypeLength)
				return true;
			for (int i = 0; i < currentMemberTypeLength; i++)
				if (!CharOperation.equals(currentMemberTypes[i].getName(), otherMemberTypes[i].getName())
					|| currentMemberTypes[i].getModifiers() != otherMemberTypes[i].getModifiers())
						return true;
		}

		// fields
		FieldInfo[] otherFieldInfos = (FieldInfo[]) newClassFile.getFields();
		int otherFieldInfosLength = otherFieldInfos == null ? 0 : otherFieldInfos.length;
		boolean compareFields = true;
		if (this.fieldsCount == otherFieldInfosLength) {
			int i = 0;
			for (; i < this.fieldsCount; i++)
				if (hasStructuralFieldChanges(this.fields[i], otherFieldInfos[i])) break;
			if ((compareFields = i != this.fieldsCount) && !orderRequired && !excludesSynthetic)
				return true;
		}
		if (compareFields) {
			if (this.fieldsCount != otherFieldInfosLength && !excludesSynthetic)
				return true;
			if (orderRequired) {
				if (this.fieldsCount != 0)
					Arrays.sort(this.fields);
				if (otherFieldInfosLength != 0)
					Arrays.sort(otherFieldInfos);
			}
			if (excludesSynthetic) {
				if (hasNonSyntheticFieldChanges(this.fields, otherFieldInfos))
					return true;
			} else {
				for (int i = 0; i < this.fieldsCount; i++)
					if (hasStructuralFieldChanges(this.fields[i], otherFieldInfos[i]))
						return true;
			}
		}

		// methods
		MethodInfo[] otherMethodInfos = (MethodInfo[]) newClassFile.getMethods();
		int otherMethodInfosLength = otherMethodInfos == null ? 0 : otherMethodInfos.length;
		boolean compareMethods = true;
		if (this.methodsCount == otherMethodInfosLength) {
			int i = 0;
			for (; i < this.methodsCount; i++)
				if (hasStructuralMethodChanges(this.methods[i], otherMethodInfos[i])) break;
			if ((compareMethods = i != this.methodsCount) && !orderRequired && !excludesSynthetic)
				return true;
		}
		if (compareMethods) {
			if (this.methodsCount != otherMethodInfosLength && !excludesSynthetic)
				return true;
			if (orderRequired) {
				if (this.methodsCount != 0)
					Arrays.sort(this.methods);
				if (otherMethodInfosLength != 0)
					Arrays.sort(otherMethodInfos);
			}
			if (excludesSynthetic) {
				if (hasNonSyntheticMethodChanges(this.methods, otherMethodInfos))
					return true;
			} else {
				for (int i = 0; i < this.methodsCount; i++)
					if (hasStructuralMethodChanges(this.methods[i], otherMethodInfos[i]))
						return true;
			}
		}

		// missing types
		char[][][] missingTypes = getMissingTypeNames();
		char[][][] newMissingTypes = newClassFile.getMissingTypeNames();
		if (missingTypes != null) {
			if (newMissingTypes == null) {
				return true;
			}
			int length = missingTypes.length;
			if (length != newMissingTypes.length) {
				return true;
			}
			for (int i = 0; i < length; i++) {
				if (!CharOperation.equals(missingTypes[i], newMissingTypes[i])) {
					return true;
				}
			}
		} else if (newMissingTypes != null) {
			return true;
		}
		return false;
	} catch (ClassFormatException e) {
		return true;
	}
}

private boolean hasStructuralAnnotationChanges(IBinaryAnnotation[] currentAnnotations, IBinaryAnnotation[] otherAnnotations) {
	if (currentAnnotations == otherAnnotations)
		return false;

	int currentAnnotationsLength = currentAnnotations == null ? 0 : currentAnnotations.length;
	int otherAnnotationsLength = otherAnnotations == null ? 0 : otherAnnotations.length;
	if (currentAnnotationsLength != otherAnnotationsLength)
		return true;
	for (int i = 0; i < currentAnnotationsLength; i++) {
		Boolean match = matchAnnotations(currentAnnotations[i], otherAnnotations[i]);
		if (match != null)
			return match.booleanValue();
	}
	return false;
}
private Boolean matchAnnotations(IBinaryAnnotation currentAnnotation, IBinaryAnnotation otherAnnotation) {
	if (!CharOperation.equals(currentAnnotation.getTypeName(), otherAnnotation.getTypeName()))
		return true;
	IBinaryElementValuePair[] currentPairs = currentAnnotation.getElementValuePairs();
	IBinaryElementValuePair[] otherPairs = otherAnnotation.getElementValuePairs();
	int currentPairsLength = currentPairs == null ? 0 : currentPairs.length;
	int otherPairsLength = otherPairs == null ? 0 : otherPairs.length;
	if (currentPairsLength != otherPairsLength)
		return Boolean.TRUE;
	for (int j = 0; j < currentPairsLength; j++) {
		if (!CharOperation.equals(currentPairs[j].getName(), otherPairs[j].getName()))
			return Boolean.TRUE;
		final Object value = currentPairs[j].getValue();
		final Object value2 = otherPairs[j].getValue();
		if (value instanceof Object[]) {
			Object[] currentValues = (Object[]) value;
			if (value2 instanceof Object[]) {
				Object[] currentValues2 = (Object[]) value2;
				final int length = currentValues.length;
				if (length != currentValues2.length) {
					return Boolean.TRUE;
				}
				for (int n = 0; n < length; n++) {
					if (!currentValues[n].equals(currentValues2[n])) {
						return Boolean.TRUE;
					}
				}
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		} else if (!value.equals(value2)) {
			return Boolean.TRUE;
		}
	}
	return null;
}
private boolean hasStructuralFieldChanges(FieldInfo currentFieldInfo, FieldInfo otherFieldInfo) {
	// generic signature
	if (!CharOperation.equals(currentFieldInfo.getGenericSignature(), otherFieldInfo.getGenericSignature()))
		return true;
	if (currentFieldInfo.getModifiers() != otherFieldInfo.getModifiers())
		return true;
	if ((currentFieldInfo.getTagBits() & TagBits.AnnotationDeprecated) != (otherFieldInfo.getTagBits() & TagBits.AnnotationDeprecated))
		return true;
	if (hasStructuralAnnotationChanges(currentFieldInfo.getAnnotations(), otherFieldInfo.getAnnotations()))
		return true;
	if (this.version >= ClassFileConstants.JDK1_8
			&& hasStructuralTypeAnnotationChanges(currentFieldInfo.getTypeAnnotations(), otherFieldInfo.getTypeAnnotations()))
		return true;
	if (!CharOperation.equals(currentFieldInfo.getName(), otherFieldInfo.getName()))
		return true;
	if (!CharOperation.equals(currentFieldInfo.getTypeName(), otherFieldInfo.getTypeName()))
		return true;
	if (currentFieldInfo.hasConstant() != otherFieldInfo.hasConstant())
		return true;
	if (currentFieldInfo.hasConstant()) {
		Constant currentConstant = currentFieldInfo.getConstant();
		Constant otherConstant = otherFieldInfo.getConstant();
		if (currentConstant.typeID() != otherConstant.typeID())
			return true;
		if (!currentConstant.getClass().equals(otherConstant.getClass()))
			return true;
		switch (currentConstant.typeID()) {
			case TypeIds.T_int :
				return currentConstant.intValue() != otherConstant.intValue();
			case TypeIds.T_byte :
				return currentConstant.byteValue() != otherConstant.byteValue();
			case TypeIds.T_short :
				return currentConstant.shortValue() != otherConstant.shortValue();
			case TypeIds.T_char :
				return currentConstant.charValue() != otherConstant.charValue();
			case TypeIds.T_long :
				return currentConstant.longValue() != otherConstant.longValue();
			case TypeIds.T_float :
				return currentConstant.floatValue() != otherConstant.floatValue();
			case TypeIds.T_double :
				return currentConstant.doubleValue() != otherConstant.doubleValue();
			case TypeIds.T_boolean :
				return currentConstant.booleanValue() != otherConstant.booleanValue();
			case TypeIds.T_JavaLangString :
				return !currentConstant.stringValue().equals(otherConstant.stringValue());
		}
	}
	return false;
}

private boolean hasStructuralMethodChanges(MethodInfo currentMethodInfo, MethodInfo otherMethodInfo) {
	// generic signature
	if (!CharOperation.equals(currentMethodInfo.getGenericSignature(), otherMethodInfo.getGenericSignature()))
		return true;
	if (currentMethodInfo.getModifiers() != otherMethodInfo.getModifiers())
		return true;
	if ((currentMethodInfo.getTagBits() & TagBits.AnnotationDeprecated) != (otherMethodInfo.getTagBits() & TagBits.AnnotationDeprecated))
		return true;
	if (hasStructuralAnnotationChanges(currentMethodInfo.getAnnotations(), otherMethodInfo.getAnnotations()))
		return true;
	// parameter annotations:
	int currentAnnotatedParamsCount = currentMethodInfo.getAnnotatedParametersCount();
	int otherAnnotatedParamsCount = otherMethodInfo.getAnnotatedParametersCount();
	if (currentAnnotatedParamsCount != otherAnnotatedParamsCount)
		return true;
	for (int i=0; i<currentAnnotatedParamsCount; i++) {
		if (hasStructuralAnnotationChanges(currentMethodInfo.getParameterAnnotations(i, this.classFileName), otherMethodInfo.getParameterAnnotations(i, this.classFileName)))
			return true;
	}
	if (this.version >= ClassFileConstants.JDK1_8
			&& hasStructuralTypeAnnotationChanges(currentMethodInfo.getTypeAnnotations(), otherMethodInfo.getTypeAnnotations()))
		return true;

	if (!CharOperation.equals(currentMethodInfo.getSelector(), otherMethodInfo.getSelector()))
		return true;
	if (!CharOperation.equals(currentMethodInfo.getMethodDescriptor(), otherMethodInfo.getMethodDescriptor()))
		return true;
	if (!CharOperation.equals(currentMethodInfo.getGenericSignature(), otherMethodInfo.getGenericSignature()))
		return true;

	char[][] currentThrownExceptions = currentMethodInfo.getExceptionTypeNames();
	char[][] otherThrownExceptions = otherMethodInfo.getExceptionTypeNames();
	if (currentThrownExceptions != otherThrownExceptions) { // TypeConstants.NoExceptions
		int currentThrownExceptionsLength = currentThrownExceptions == null ? 0 : currentThrownExceptions.length;
		int otherThrownExceptionsLength = otherThrownExceptions == null ? 0 : otherThrownExceptions.length;
		if (currentThrownExceptionsLength != otherThrownExceptionsLength)
			return true;
		for (int k = 0; k < currentThrownExceptionsLength; k++)
			if (!CharOperation.equals(currentThrownExceptions[k], otherThrownExceptions[k]))
				return true;
	}
	return false;
}

private boolean hasStructuralTypeAnnotationChanges(IBinaryTypeAnnotation[] currentTypeAnnotations, IBinaryTypeAnnotation[] otherTypeAnnotations) {
	if (otherTypeAnnotations != null) {
		// copy so we can delete matched annotations:
		int len = otherTypeAnnotations.length;
		System.arraycopy(otherTypeAnnotations, 0, otherTypeAnnotations = new IBinaryTypeAnnotation[len], 0, len);
	}
	if (currentTypeAnnotations != null) {
		loopCurrent:
		for (IBinaryTypeAnnotation currentAnnotation : currentTypeAnnotations) {
			if (!affectsSignature(currentAnnotation)) continue;
			if (otherTypeAnnotations == null)
				return true;
			for (int i = 0; i < otherTypeAnnotations.length; i++) {
				IBinaryTypeAnnotation otherAnnotation = otherTypeAnnotations[i];
				if (otherAnnotation != null && matchAnnotations(currentAnnotation.getAnnotation(), otherAnnotation.getAnnotation()) == Boolean.TRUE) {
					otherTypeAnnotations[i] = null; // matched
					continue loopCurrent;
				}
			}
			return true; // not matched
		}
	}
	if (otherTypeAnnotations != null) {
		for (IBinaryTypeAnnotation otherAnnotation : otherTypeAnnotations) {
			if (affectsSignature(otherAnnotation))
				return true;
		}
	}
	return false;
}

private boolean affectsSignature(IBinaryTypeAnnotation typeAnnotation) {
	if (typeAnnotation == null) return false;
	int targetType = typeAnnotation.getTargetType();
	if (targetType >= AnnotationTargetTypeConstants.LOCAL_VARIABLE && targetType <= AnnotationTargetTypeConstants.METHOD_REFERENCE_TYPE_ARGUMENT)
		return false; // affects detail within a block
	return true;
}

/**
 * This method is used to fully initialize the contents of the receiver. All methodinfos, fields infos
 * will be therefore fully initialized and we can get rid of the bytes.
 */
private void initialize() throws ClassFormatException {
	try {
		for (int i = 0, max = this.fieldsCount; i < max; i++) {
			this.fields[i].initialize();
		}
		for (int i = 0, max = this.methodsCount; i < max; i++) {
			this.methods[i].initialize();
		}
		if (this.innerInfos != null) {
			for (int i = 0, max = this.innerInfos.length; i < max; i++) {
				this.innerInfos[i].initialize();
			}
		}
		if (this.annotations != null) {
			for (int i = 0, max = this.annotations.length; i < max; i++) {
				this.annotations[i].initialize();
			}
		}
		this.getEnclosingMethod();
		reset();
	} catch(RuntimeException e) {
		ClassFormatException exception = new ClassFormatException(e, this.classFileName);
		throw exception;
	}
}
@Override
public boolean isAnonymous() {
	if (this.innerInfo == null) return false;
	char[] innerSourceName = this.innerInfo.getSourceName();
	return (innerSourceName == null || innerSourceName.length == 0);
}

@Override
public boolean isBinaryType() {
	return true;
}

@Override
public boolean isLocal() {
	if (this.innerInfo == null) return false;
	if (this.innerInfo.getEnclosingTypeName() != null) return false;
	char[] innerSourceName = this.innerInfo.getSourceName();
	return (innerSourceName != null && innerSourceName.length > 0);
}

@Override
public boolean isMember() {
	if (this.innerInfo == null) return false;
	if (this.innerInfo.getEnclosingTypeName() == null) return false;
	char[] innerSourceName = this.innerInfo.getSourceName();
	return (innerSourceName != null && innerSourceName.length > 0);	 // protection against ill-formed attributes (67600)
}

/**
 * Answer true if the receiver is a nested type, false otherwise
 *
 * @return <CODE>boolean</CODE>
 */
public boolean isNestedType() {
	return this.innerInfo != null;
}
//{ObjectTeams
public boolean isTeam()
{
	return (this.accessFlags & ExtraCompilerModifiers.AccTeam) != 0;
}

public boolean isRole()
{
	return (this.accessFlags & ExtraCompilerModifiers.AccRole) != 0;
}
// SH}
/**
 * Answer the source file name attribute. Return null if there is no source file attribute for the receiver.
 *
 * @return char[]
 */
@Override
public char[] sourceFileName() {
	return this.sourceFileName;
}

@Override
public String toString() {
	java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
	java.io.PrintWriter print = new java.io.PrintWriter(out);
	print.println(getClass().getName() + "{"); //$NON-NLS-1$
	print.println(" this.className: " + new String(getName())); //$NON-NLS-1$
	print.println(" this.superclassName: " + (getSuperclassName() == null ? "null" : new String(getSuperclassName()))); //$NON-NLS-2$ //$NON-NLS-1$
	if (this.moduleName != null)
		print.println(" this.moduleName: " + (new String(this.moduleName))); //$NON-NLS-1$
	print.println(" access_flags: " + printTypeModifiers(accessFlags()) + "(" + accessFlags() + ")"); //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-2$
	print.flush();
	return out.toString();
}

@Override
public boolean isRecord() {
	return this.isRecord;
}

@Override
public IRecordComponent[] getRecordComponents() {
	return this.recordComponents;
}
}

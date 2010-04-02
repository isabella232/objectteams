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
 * $Id: BytecodeTransformer.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import java.util.Iterator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.IVerificationTypeInfo;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObjectReader.IncompatibleBytecodeException;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.LineInfo;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.LineNumberProvider;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.TeamMethodGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * MIGRATION_STATE: complete.
 *
 * Documentation is in the methods themselves.
 *
 * @author Markus Witte and Stephan Herrmann
 * @version $Id: BytecodeTransformer.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class BytecodeTransformer
        implements AttributeNamesConstants, ClassFileConstants
{

    private ConstantPoolObjectReader _reader;
    private ConstantPoolObjectWriter _writer;
    private ConstantPoolObjectMapper _mapper;

    private static final int METHOD_PREFIX_LEN = 8;
    private static final int CODE_ATTR_PREFIX_LEN = 14;

    // length of field before actual byte code starts within the code attribute:
    // u2 attribute_name_index;
    // u4 attribute_length;
    // u2 max_stack;
    // u2 max_locals;
    // u4 code_length;

    /**
     * When generating code for a role class, copy all non-wide string/integer constants
     * from all tsuper roles in order to reserve constant pool positions below 256.
     * Note, that this strategy is not safe, since multiple tsupers may introduce any
     * number of constants below 256 :(
     */
    public void checkCopyNonWideConstants(Scope scope, ClassFile classFile)
    {
        SourceTypeBinding dstType = classFile.referenceBinding;
        this._writer = new ConstantPoolObjectWriter(classFile);
        if (dstType.isRole() && !dstType.isInterface()) // for all role classes
        {

            ReferenceBinding[] tsuperRoles = dstType.roleModel.getTSuperRoleBindings();
        	for (int i = 0; i < tsuperRoles.length; i++) // for all tsuper roles
        	{
                RoleModel srcRole = tsuperRoles[i].roleModel;
                if (srcRole == null || !srcRole.hasByteCode()) continue;
                byte[] srcConstantPool = srcRole.getByteCode();
                if (srcConstantPool == null) continue; // be shy, no idea how it could happen

                this._reader = new ConstantPoolObjectReader(srcRole, srcConstantPool, scope.environment());

                copyAllNonWideConstants(srcRole.getConstantPoolOffsets().length);
			}
        }
        if (dstType.isTeam()) {
        	ReferenceBinding orgObjectteamsTeam = scope.getOrgObjectteamsTeam();
        	if (   !TypeAnalyzer.isOrgObjectteamsTeam(dstType)
        		&& !dstType.superclass.isTeam()) 
        	{
        		this._reader = new ConstantPoolObjectReader(TeamMethodGenerator.classBytes, TeamMethodGenerator.constantPoolOffsets, orgObjectteamsTeam.getTeamModel(), scope.environment());
        		copyAllNonWideConstants(TeamMethodGenerator.constantPoolOffsets.length);
        	}

        	TeamModel srcModel= dstType.superclass.getTeamModel();
        	if (srcModel == null) return;
        	// if the team has a copied ctor (w/ arg-lifting), bytecodes
        	// for the team need to be copied from the super-team, too:
        	for (MethodBinding method : dstType.methods()) {
				if (method.model != null && method.model.liftedParams != null) {
					method= method.copyInheritanceSrc;
					if (method == null || method.model == null) continue; // shouldn't happen anyway
					this._reader= new ConstantPoolObjectReader(method.model, srcModel, scope.environment());
					copyAllNonWideConstants(method.model.getConstantPoolOffsets().length);
					return; // triggered by any method, this team class is fully handled.
				}
			}
        }
    }

    /**
     * Creates a Copy of the source-Method-Bytecode and adjusts all references from superrole
     * to local role. Operates directly on the ClassFile.
     *
     * @param classFile class file structure for the destination role
     * @param dstMethod Declaration of the method to be copied.
     */
    public void checkCopyMethodCode(ClassFile classFile, AbstractMethodDeclaration dstMethod)
    {
        assert(dstMethod.isRelevantCopied());
        assert(dstMethod.sourceMethodBinding!=null);


        SourceTypeBinding dstType	   = classFile.referenceBinding;
        MethodBinding srcMethodBinding = dstMethod.sourceMethodBinding;
        ReferenceBinding srcType       = srcMethodBinding.declaringClass;
        RoleModel        srcRole       = srcType.roleModel;

        if (TypeAnalyzer.isSourceTypeWithErrors(srcType)) {
       		// broken tsuper role has probably no byte code.
        	dstMethod.binding.bytecodeMissing= true;
       		return;
        }

        byte[] srcConstantPool  = null;
        int    offset           = -1;
        ConstantPoolObjectReader reader = null;
        if (!srcMethodBinding.bytecodeMissing) {
	        if (srcRole != null) {
        		srcConstantPool = srcRole.getByteCode();
        		offset          = srcRole.getByteCodeOffset(srcMethodBinding);
        		reader 			= new ConstantPoolObjectReader(srcRole, srcConstantPool, dstMethod.scope.environment());
	        } else if (srcMethodBinding.model != null) {
	            // if not copying from a role, it must be a team constructor, or a callin wrapper
	        	assert srcType.isTeam();
	        	MethodModel mModel = srcMethodBinding.model;
	        	srcConstantPool = mModel.getBytes();
	        	offset          = mModel.getStructOffset();
	        	reader		 	= new ConstantPoolObjectReader(mModel, srcType.getTeamModel(), dstMethod.scope.environment());
    		}
        }

        if (offset == -1) {
        	dstMethod.binding.bytecodeMissing = true; // propagate error.
            return;
        }
        doCopyMethodCode(  srcRole,
    					   srcMethodBinding,
    					   dstType,
    					   dstMethod,
    					   srcConstantPool,
    					   null, // cp-offsets not needed here
    					   offset,
    					   reader,
    					   new ConstantPoolObjectMapper(srcMethodBinding, dstMethod.binding),
    					   classFile);
    }
    public void doCopyMethodCode(RoleModel 					srcRole,
    						    MethodBinding 				srcMethodBinding,
    							SourceTypeBinding 			dstType,
    							AbstractMethodDeclaration 	dstMethod,
    							byte[] 						srcConstantPool,
     						    int[] 						constantPoolOffsets,
    							int 						offset,
    							ConstantPoolObjectReader 	reader,
    							ConstantPoolObjectMapper 	mapper,
    							ClassFile 					classFile)
    {
        this._reader = reader;
        this._mapper = mapper;
        this._writer = new ConstantPoolObjectWriter(classFile);

        int codeAttributeOffset = findCodeAttribute(srcConstantPool, offset);
        int codeLength          = OTByteCodes.getInt (srcConstantPool, codeAttributeOffset+10);
        int tailOffset          = codeAttributeOffset+CODE_ATTR_PREFIX_LEN+codeLength;

        // calculate size of attributes:
        int attributesLen       = computeAttributesLen(srcConstantPool, offset);

        // copy method code:
        int totalLen = METHOD_PREFIX_LEN+attributesLen;
		byte[] methodCode = new byte[totalLen];
    	System.arraycopy (srcConstantPool, offset, methodCode, 0, totalLen);

    	// keep this offset for updating the stored value after adjustTail():
    	int copyInhSrcLineOffsetOffset;
    	// first phase of adjustment (method prefix and attributes except code):
		copyInhSrcLineOffsetOffset = copyAdjustStructure(
				constantPoolOffsets == null 
				? ConstantPoolSimpleConverter.create(srcRole, srcMethodBinding, methodCode, classFile)
				: new ConstantPoolSimpleConverter(srcConstantPool, constantPoolOffsets, offset, methodCode, classFile),
				srcConstantPool, offset, methodCode,
				dstMethod.binding,
				totalLen);

        codeAttributeOffset -= offset; // now relative to methodCode
        tailOffset          -= offset;

        if (copyInhSrcLineOffsetOffset == -1) {
	        // CopyInheritanceSrc added when copying a method for the first time
        	byte[] extraAttr = generateCpInhSrc(dstMethod.model, classFile);

        	if (extraAttr != null) {
        		System.arraycopy(methodCode, 0,
        				         methodCode = new byte[totalLen+extraAttr.length], 0,
								 totalLen);
        		System.arraycopy (extraAttr, 0, methodCode, totalLen, extraAttr.length);
        		incrementWord(methodCode, 0, 6, 1); // attributes_count++

        		copyInhSrcLineOffsetOffset = totalLen+8; // offset of lineOffset within CopyInheritanceSrc attr.
        	}
        }
        if (dstMethod.isTSuper)
        	incrementWord(methodCode, codeAttributeOffset, 8, 1); // max_locals++

        CodeStream codeStream = classFile.codeStream;
        codeStream.init(classFile); // a weaker reset (we have no AbstractMethodDeclaration)
        int newMethodOffset = classFile.contentsOffset;

        boolean isCtorAddingMarkArg =
        			   srcMethodBinding.isConstructor()
					&& !TSuperHelper.isTSuper(srcMethodBinding)
					&& dstMethod.isTSuper;
		try {
			adjustCode(classFile, dstMethod.getModel(), methodCode, codeAttributeOffset, codeLength, isCtorAddingMarkArg);
		} catch (IncompatibleBytecodeException ibe) {
			ProblemReporter pr = dstMethod.scope.problemReporter();
			pr.incompatibleBytecode(ibe._offendingName, ibe._problemId);
		} catch (Throwable t) {
			ProblemReporter pr = dstMethod.scope.problemReporter();
			pr.corruptBytecode(dstMethod.binding);
		}

        int lineOffset = adjustTail(dstType, methodCode, tailOffset, dstMethod.binding, srcMethodBinding.model);
        if (copyInhSrcLineOffsetOffset > -1 && lineOffset != 0) {
        	// adjustTail has computed a new lineOffset, insert it into the existing attribute now:
        	OTByteCodes.setInt(methodCode, copyInhSrcLineOffsetOffset, lineOffset);
        	MethodModel.getModel(dstMethod)._lineOffset = lineOffset;
        }
        codeStream.writeBytes(methodCode);
        classFile.contents = codeStream.bCodeStream; // might have grown during adjust
        classFile.contentsOffset += methodCode.length;
        classFile.methodCount++;

        dstMethod.maybeRecordByteCode(classFile, newMethodOffset);
    }
    
    /**
	 * @param srcConstantPool
	 * @param offset          start of this method (within srcConstantPool)
	 * @return length of all attributes of this method
	 */
	private int computeAttributesLen(byte[] srcConstantPool, int offset) {
		int attributesCount = OTByteCodes.getWord(srcConstantPool, offset+6);
		int attributesStart = offset+METHOD_PREFIX_LEN;
		int attrOffset = attributesStart;
        for (int i=0; i<attributesCount; i++)
        {
            attrOffset += 2; // name
            attrOffset += 4 + OTByteCodes.getInt(srcConstantPool, attrOffset); // len
        }
        return attrOffset - attributesStart;
	}


	/** Increment a word value by 'increment', which is 'offset' bytes into the method code. */
	private void incrementWord(byte[] code, int codeAttributeOffset, int offset, int increment) {
        int value = OTByteCodes.getWord(code, codeAttributeOffset+offset);
        OTByteCodes.setWord(code, codeAttributeOffset+offset, value+increment);
	}

	/**
     * Find the start of the code attribute. Don't yet map attribute names, since
     * the result of this function is needed to compute the target byte-array size,
     * i.e., we can't create the new byte array before calling this method.
     * @param code
     * @param start first position of the method.
     * @return start position of the code attribute.
     */
    private int findCodeAttribute(byte[] code, int start)
    {
        int attributesCount = OTByteCodes.getWord(code, start+6);
        start += METHOD_PREFIX_LEN;
        for (int i=0; i<attributesCount; i++)
        {
            char[] attribute_name    = this._reader.getUtf8(OTByteCodes.getWord(code, start+ 0));
            if (CharOperation.equals(attribute_name, CodeName))
                return start;
            int attribute_length     = OTByteCodes.getInt (code, start+ 2);
            start += 6+attribute_length;
        }
        throw new InternalCompilerError("Binary method has no code attribute"); //$NON-NLS-1$
    }

    /**
     * Copy the method structure, ie., the header comprising name and signature
     * and _all_ attributes (including Code).
     * Everything _but_ Code is adjusted (only strings).
     *
     * @param conv            converter for mapping strings to the new constantpool
     * @param src             bytes to copy from
     * @param srcOffset       offset into src
     * @param dest            bytes to copy into
     * @param dstMethod
     * @param expectedLen     number of bytes to be adjusted (for sanity checking only)
     * @return if a CopyInheritanceSrc Attribute is already present return the offset where the lineOffset is stored, -1 otherwise
     */
    private int copyAdjustStructure(
    		ConstantPoolSimpleConverter conv,
			byte[] src, int srcOffset, byte[] dest,
			MethodBinding dstMethod,
			int expectedLen)
    {
    	conv.updateName(2); // method name
    	conv.writeName(4, dstMethod.signature()); // method signature
    	int attributesCount = OTByteCodes.getWord(dest, 6);

    	int offset = METHOD_PREFIX_LEN;

    	int copyInhSrcLineOffsetOffset = -1;

    	for (int i = 0; i<attributesCount; i++) {
            char[] attrName = conv.updateName(offset); // attribute name
            offset += 2;

            int attrLen = OTByteCodes.getInt(src, srcOffset+offset); // attribute length
            offset += 4;

            if (CharOperation.equals(attrName, ExceptionsName))
            {
            	int numExceptions = OTByteCodes.getWord(src, srcOffset+offset);
            	offset += 2;
            	for (int j=0; j<numExceptions; j++) {
					int ref = OTByteCodes.getWord(src, srcOffset+offset);
            		ConstantPoolObject cpo = this._reader.readConstantPoolObject(ref, 2);
            		cpo = this._mapper.mapConstantPoolObject(cpo);
            		this._writer.writeConstantPoolObject(dest, offset, 2, cpo);
            		offset+=2;
            	}
            }
            else if (CharOperation.equals(attrName, SignatureName))
            {
            	conv.updateName(offset);
            	offset += 2;
            }
            else if (CharOperation.equals(attrName, IOTConstants.COPY_INHERITANCE_SOURCE_NAME))
            {
            	conv.updateName(offset);
            	if (attrLen > 2) {
					copyInhSrcLineOffsetOffset = offset+2;
					offset += 6;
				} else {
					// legacy attribute without lineOffset
					copyInhSrcLineOffsetOffset = 0;
					offset += 2;
				}
            }
            else if (CharOperation.equals(attrName, IOTConstants.TYPE_ANCHOR_LIST))
            {
            	int num = OTByteCodes.getWord(src, srcOffset+offset);
            	offset += 2;
            	for (int j = 0; j<num; j++) {
	            	conv.updateName(offset);
	            	offset += 2;
            	}
            } else if (   CharOperation.equals(attrName, RuntimeVisibleAnnotationsName)
         		   || CharOperation.equals(attrName, RuntimeInvisibleAnnotationsName))
            {
            	int annotCount = OTByteCodes.getWord(src, srcOffset+offset);
            	offset+=2;
            	for (int j=0; j<annotCount; j++)
            		offset = adjustAnnotation(conv, src, srcOffset, offset);	             	
            } else {
            	offset += attrLen;
            }

    	}
        assert offset == expectedLen;
        return copyInhSrcLineOffsetOffset;
    }

    /** Scan for all names in a possibly structured/nested annotation to adjust constants to the new pool. */
	private int adjustAnnotation(ConstantPoolSimpleConverter conv, byte[] src, int srcOffset, int offset) {
		conv.updateName(offset); // annotation name
		offset += 2;
		int numMembers = OTByteCodes.getWord(src, srcOffset+offset);
		offset += 2; 
		if (numMembers > 0)
			for (int k=0; k<numMembers; k++) {
				conv.updateName(offset);
				offset += 2;
				offset = adjustAnnotationElementValue(conv, src, srcOffset, offset);
			}
		return offset;
	}
    /**
     * Adjust all string values within an annotation with members.
     * SH: inspired by AnnotationInfo.scanElementValue(int);
     * @return the next offset to read.
     */
    private int adjustAnnotationElementValue(ConstantPoolSimpleConverter conv, byte[] src, int srcOffset, int offset) 
    		throws IllegalArgumentException 
    {
    	int currentOffset = offset;
    	int tag = OTByteCodes.getUnsignedByte(src, srcOffset+currentOffset);
    	currentOffset++;
    	switch (tag) {
    		case 'B':
    		case 'C':
    		case 'D':
    		case 'F':
    		case 'I':
    		case 'J':
    		case 'S':
    		case 'Z':
    			currentOffset += 2;
    			break;
    		case 's':
    		case 'c':
    			conv.updateName(currentOffset);
    			currentOffset += 2;
    			break;
    		case 'e':
    			conv.updateName(currentOffset); // enum type
    			currentOffset += 2;
    			conv.updateName(currentOffset); // constant
    			currentOffset += 2;
    			break;
    		case '@':
    			currentOffset = adjustAnnotation(conv, src, srcOffset, currentOffset);
    			break;
    		case '[':
    			int numberOfValues = OTByteCodes.getWord(src, srcOffset+currentOffset);
    			currentOffset += 2;
    			for (int i = 0; i < numberOfValues; i++)
    				currentOffset = adjustAnnotationElementValue(conv, src, srcOffset, currentOffset);
    			break;
    		default:
    			throw new IllegalStateException();
    	}
    	return currentOffset;
    }


    /* generate the CopyInheritanceSrc Attribute. */
    byte[] generateCpInhSrc(MethodModel model, ClassFile classFile)
    {
    	if (model == null || model._attributes == null)
    		return null;
    	for (int i = 0; i < model._attributes.length; i++) {
    		if (CharOperation.equals(model._attributes[i]._name, IOTConstants.COPY_INHERITANCE_SOURCE_NAME))
    		{
    	    	byte[] result = new byte[model._attributes[i].size()];
	            model._attributes[i].generate(result, 0, classFile.constantPool);
	            return result;
    		}
		}
    	return null;
    }

    /**
     * Ths method replaces all references to constantpool of the superclass with references
     * to current subclass constantpool
     *  - all References in codeToAdjust will be exchanged
     *  - unknown references must be added to the ConstantPool of classFile
     *
     * @param srcMethodBinding the method from which will be copied
     *                   (just a stored reference to orignal BinaryMethodBinding)
     * @param dstClassFile This is the destination Classfile wich contains the later flushed ConstantPool
     * @param codeToAdjust this is the duplicated code-part of the code_attribute (see getCodeByteCode)
     */
	private void adjustCode(
            ClassFile   dstClassFile,
			MethodModel mModel,
            byte[]      codeToAdjust,
            int         codeAttributeOffset,
            int         codeLength,
			boolean     isCtorAddingMarkArg)
	{
		/*
	     * States concerning marker-arg chaining sequences:
	     * (See class comment of class ExlicitConstructorCall for background information).
	     *
		 * 0 : do nothing
		 * 1 : this is a ctor
		 * 2 : saw aconst_null
		 * 3 : saw astore_<n> : volatile state: immediately leave this state.
		 * When in state 3 we immediately see a self call invokespecial
		 *   replace the chaining sequence and replace the method to call;
		 * This heuristic is safe, since the sequence
		 *               "aconst_null; astore<n>; invokespecial"
		 * could not be created from source-code.
		 */
		int state = 1;
		boolean initCachesAdded = false;


		int actualBytecodeStart = codeAttributeOffset+CODE_ATTR_PREFIX_LEN;

		for (int i = actualBytecodeStart; i<actualBytecodeStart+codeLength; i++) {
				//get byte from bytecode array
				byte b_int = codeToAdjust[i];

				//get number of operands from Table
				int length = OTByteCodes.cpReferenceLength(b_int);

				// if byte code is followed by a reference into the cp
				if (length != 0) {
					//get reference from current code position -> a reference consist of one or more bytes
					int ref = OTByteCodes.getRef(length, codeToAdjust, i+1);

					ConstantPoolObject src_cpo = this._reader.readConstantPoolObject(ref, length);
					ConstantPoolObject dst_cpo = this._mapper.mapConstantPoolObject(src_cpo, state==3/*addMarkerArgAllowed*/);
					if (dst_cpo.isMethod()) {
						if (dst_cpo.isIllegallyCopiedCtor()) {
							// illegal ctor is actually used: report as error!
							AbstractMethodDeclaration methodDecl = mModel.getDecl();
							methodDecl.scope.problemReporter().illegallyCopiedDefaultCtor(methodDecl, methodDecl.scope.referenceType());
							return;
						}
						MethodBinding method = dst_cpo.getMethodRef();
						if (state == 3) {
							if (   isCtorAddingMarkArg
							    && method.overriddenTSupers != null
							    && method.isConstructor())
							{
								// statemachine recognized full pattern for chaining marker arg. Patch now:
								MethodBinding tsuperVersion = findTSuperVersion(method);
								if (tsuperVersion != null) {
									replaceChainArg(codeToAdjust, i-3);
									dst_cpo.setMethod(tsuperVersion);
								}
							} else if (method.parameters.length > src_cpo.getMethodRef().parameters.length) {
								if (method.copyInheritanceSrc != null) {
									// overwrite 2-byte astore_n:
									// as to leave preceding aconst_null on stack to match the tsuper-marker
									codeToAdjust[i-2] = Opcodes.OPC_nop;
									codeToAdjust[i-1] = Opcodes.OPC_nop;
								}
							}
						}
					}
					this._writer.writeConstantPoolObject(codeToAdjust, i+1, length, dst_cpo);
				} else if (   b_int == Opcodes.OPC_nop
						   && mModel != null
						   && mModel.liftedParams != null
						   && mModel.liftedParams.length > 0)
				{
					int arg = checkPatchLiftingNops(codeToAdjust, i);
					if (arg >= 0) {
						// found a nop sequence to be patched (constant parts are already patched)
						ConstantPoolObject dst_cpo = null;
						int loadPos = arg & 0x1FFF; // bit 0x2000 flags (storePos > 3)

						if (!initCachesAdded) { // only insert once
							// find the _OT$initCaches method:
							dst_cpo = getInitCachesMethod(dstClassFile);
							if (dst_cpo != null) {
								// write the method reference into the nop space:
								this._writer.writeConstantPoolObject(codeToAdjust, i+2, 2, dst_cpo);
							}
							initCachesAdded = true;
						}
						if (dst_cpo == null) {
							codeToAdjust[i+1] = Opcodes.OPC_nop; // delete invokevirtual
							// Note: aload_0 and pop are already balanced
						}

						int offset = (loadPos > 3) ? 1 : 0; // longer byte code used for aload?
						// find the lift method:
						dst_cpo = getLiftMethod(dstClassFile, mModel, loadPos);
						if (dst_cpo == null)
							return; // cannot perform required lifting
						// write the method reference into the nop space:
						this._writer.writeConstantPoolObject(codeToAdjust, i+offset+8, 2, dst_cpo);


						// call sequence had two additional loads, reserve space on the stack:
			        	incrementWord(codeToAdjust, codeAttributeOffset, 6, 2); // max_stack++

						length = 10+offset;
						if ((arg & 0x2000) != 0) // longer byte code used for astore?
							length++;
					}
				}
				// state machine:
				if (state == 1 && b_int == Opcodes.OPC_aconst_null)
					state = 2;
				else if (state == 2 && b_int == Opcodes.OPC_astore)
					state = 3;
				else if (state == 3)
					state = 0; // patch sequence not followed by self call: quit state machine.

			i += length;
 			// also skip parameters, which are not CP-index
			i += OTByteCodes.getParamLength(b_int, codeToAdjust, i, actualBytecodeStart);
		}

	}

	/**
	 * Watch out: String/Integer constants require a LDC or a LDCW opcode depending on their index
	 * in the constant pool. This method tries to prioritize constants with low index,
	 * to avoid that they get a high index in the target class, which will lead to invalid
	 * copied LDC operations.
	 * @param nConstants number of constants in the src constant pool
	 */
	private void copyAllNonWideConstants(int nConstants) {
		Iterator<ConstantPoolObject> it = this._reader.getNonWideConstantIterator(nConstants);
		while (it.hasNext()) {
			ConstantPoolObject src_cpo = it.next();
			this._writer.writeConstantPoolObject(src_cpo);
		}
	}

	private ConstantPoolObject getInitCachesMethod(ClassFile dstClassFile) {
		MethodBinding[] inits = dstClassFile.referenceBinding.getMethods(IOTConstants.OT_INIT_CACHES);
		if (inits.length == 0)
			return null; // not all teams have initCaches (super ctor already calls the super version)
		assert inits.length == 1; // TODO (SH): watchout: tsuper versions in nested teams!!
		// wrap it:
		ConstantPoolObject dst_cpo = new ConstantPoolObject(MethodRefTag, inits[0]);
		return dst_cpo;
	}

	/**
	 * Get the liftmethod for lifting the arg-th argument of mModel.
	 *
	 * @param dstClassFile
	 * @param mModel
	 * @param arg
	 * @return a wrapper for the liftmethod.
	 */
	private ConstantPoolObject getLiftMethod(ClassFile dstClassFile, MethodModel mModel, int arg) {
		// find the role type:
		TypeBinding[] adjustedArgs = mModel.liftedParams;
		ReferenceBinding role = (ReferenceBinding)adjustedArgs[arg-1];
		AbstractMethodDeclaration mDecl = mModel.getDecl();
		role = (ReferenceBinding)TeamModel.getRoleToLiftTo(
												mDecl.scope,
												mDecl.binding.parameters[arg-1],
												role,
												true,
												mDecl.arguments[arg-1]);
		// find the method:
		char[] liftName = Lifting.getLiftMethodName(role);
		MethodBinding[] lifters = dstClassFile.referenceBinding.getMethods(liftName);
		if (lifters.length != 1) {
			assert (role.getRealClass().tagBits & TagBits.HasLiftingProblem) != 0 : "must have lift method unless lifting problem was detected"; //$NON-NLS-1$
			return null;
		}
		// wrap it:
		ConstantPoolObject dst_cpo = new ConstantPoolObject(MethodRefTag, lifters[0]);
		return dst_cpo;
	}

	/** Look for the nop pattern of a local var that is used to
     *  prepare for lifting. If the pattern is found, patch all those
     *  opcodes, that are constant (see class comment in Lifting).
	 */
	private int checkPatchLiftingNops(byte[] code, int idx) {
		int i;

		// Expecting 6 nops:
		for(i=idx+1;i<idx+6;i++)
			if (code[i] != Opcodes.OPC_nop)
				return -1;

		// Expecting aload_<loadPos>:
		int loadPos = OTByteCodes.getAloadPos(code[idx+6], code[idx+7]);
		if (loadPos == -1)
			return -1;
		int storeStart = (loadPos > 3) ? 8 : 7; // depends on space for pos operand

		// Expecting 3 nops:
		for(i=idx+storeStart;i<idx+storeStart+3;i++)
			if (code[i] != Opcodes.OPC_nop)
				return -1;

		// Expecting astore_<storePos>:
		int storePos = OTByteCodes.getAstorePos(code[idx+10], code[idx+11]);
		if (storePos == -1)
			return -1;

		// Full match, start patching:

		// patching 6 nops:
		code[idx+0] = Opcodes.OPC_aload_0;
		code[idx+1] = Opcodes.OPC_invokevirtual;
		//  [idx+2/3]: space for "_OT$initCaches"
		code[idx+4] = Opcodes.OPC_pop;

		code[idx+5] = Opcodes.OPC_aload_0;

		// leave aload<loadPos>
		if (loadPos > 3)
			idx++;

		// patching 3 nops:
		code[idx+7] = Opcodes.OPC_invokevirtual;
		//  [idx+8/9]: space for _OT$liftTo<Role>

		// leave astore<storePos>
		if (storePos > 3)
			loadPos += 0x2000; // flag to client that longer byte code is used
		return loadPos;
	}

	/**
	 * Add a byte code sequence that is a placeholder for chaining the
	 * marker arg if the current method is copied lateron.
	 * (See class comment in class ExplicitConstructorCall).
	 *
	 * @param scope
	 * @param codeStream
	 * @param chainTSuperMarkArgPos position that a marker arg will get when added.
	 */
	public static void addChainingPlaceholder(
			BlockScope scope,
			CodeStream codeStream,
			int        chainTSuperMarkArgPos)
	{
		// create local variable "Object _OT$chainArg"
		// at the very position that will be taken by an added
		// marker argument:
		LocalVariableBinding nullVar = new LocalVariableBinding(
				"_OT$chainArg".toCharArray(), //$NON-NLS-1$
				scope.getJavaLangObject(),
				0, false);
		nullVar.resolvedPosition = chainTSuperMarkArgPos;
		nullVar.useFlag = LocalVariableBinding.USED;
		nullVar.declaringScope = scope.methodScope();
		codeStream.record(nullVar);
		codeStream.addVisibleLocalVariable(nullVar);
		// add dummy code sequence "aconst_null; astore <i>"
		// which will be changed by BytecodeTransformer.replaceChainArg
		// to "nop; aload <i>" with the same <i>.
		codeStream.aconst_null();
		codeStream.astore(chainTSuperMarkArgPos); // optimize small indices?
		// record positions for local varaible table.
		nullVar.recordInitializationStartPC(0);
		if (nullVar.initializationPCs != null)
			nullVar.recordInitializationEndPC(codeStream.position);
	}

	/** Perform changes to code sequence produced by addChainingPlaceholder(). */
	private void replaceChainArg(byte[] codeToAdjust, int idx) {
		codeToAdjust[idx] = Opcodes.OPC_nop;
		codeToAdjust[idx+1] = Opcodes.OPC_aload;
		// argument to aload remains unchanged from astore
	}

	/** Answer the tsuper version which is overridden by 'orig',
	 *  or null if orig itself is copied without adding the marker arg.
	 * @param orig
	 * @return tsuper method or null
	 */
	private MethodBinding findTSuperVersion (MethodBinding orig) {
		ReferenceBinding roleType = orig.declaringClass;
		MethodBinding[] methods = roleType.methods();
		for (int i = 0; i < methods.length; i++) {
			if (orig.overridesTSuper(methods[i].copyInheritanceSrc))
				return methods[i];
		}
		assert orig.copyInheritanceSrc != null; // must be a copied method, don't replace
		return null;
	}

	private int[][] getLineNumberTable(byte[] classFileBytes, int offset) {
		final int length = OTByteCodes.getWord(classFileBytes, offset);
		int[][] result = null;
		if (length != 0) {
			result = new int[length][2];
			int readOffset = 2;
			for (int i = 0; i < length; i++) {
				result[i][0] = OTByteCodes.getWord(classFileBytes, offset+readOffset);
				result[i][1] = OTByteCodes.getWord(classFileBytes, offset+readOffset+2);
				readOffset += 4;
			}
		}
    	return result;
    }

	/**
	 * Adjust further information outside the Code attribute.
	 *
	 * @param dstType
	 * @param codeToAdjust
	 * @param tailOffset
	 * @param dstMethodBinding
	 * @param srcMethodModel
	 * @return				   the new offset between line-numbers in the copied byte code and original source lines
	 */
	private int adjustTail(SourceTypeBinding dstType, byte[] codeToAdjust, int tailOffset, MethodBinding dstMethodBinding, MethodModel srcMethodModel)
    {
		int newLineOffset = 0;
        int exceptionCount = OTByteCodes.getWord(codeToAdjust, tailOffset);
        int offset = tailOffset+2;
        for (int i = 0; i<exceptionCount; i++) {
            if (OTByteCodes.getWord(codeToAdjust, offset+6)!=0) // 0 means any exception
                updateCPO(dstType, codeToAdjust, offset+6, 2); // catch type
            offset += 8;
        }

        // remap arguments and locals:
        //   non-static methods: [0] is "this", shift others by 1
        //   static methods: [0] is dummy int, [1] is synthetic team arg,
        //                   these two have no entry in LVT and LVTT, so just ignore them.
        //                   however, shift all real parameters to positions 2 ..
        int implicitSlots = dstMethodBinding.isStatic() ? 2 : 1;
        TypeBinding[] arguments = new TypeBinding[dstMethodBinding.parameters.length+implicitSlots];
        System.arraycopy(dstMethodBinding.parameters,0, arguments, implicitSlots, dstMethodBinding.parameters.length);
        if (!dstMethodBinding.isStatic()) {
        	arguments[0] = dstMethodBinding.declaringClass;
        } else {
        	arguments[0] = TypeBinding.INT;
        	arguments[1] = dstType.enclosingType();
        }

        // Attributes within the Code attribute:
        int attribCount = OTByteCodes.getWord(codeToAdjust, offset);
        offset += 2;
        for (int i=0; i<attribCount; i++)
        {
            ConstantPoolObject newCPO = updateCPO(dstType, codeToAdjust, offset, 2); // attribute name
            offset += 2;
            int attrLen = OTByteCodes.getInt(codeToAdjust, offset);
            offset += 4;

            char[] attrName = newCPO.getUtf8();
            if (   CharOperation.equals(attrName, LineNumberTableName)
            	&& dstType.roleModel != null)
            {
            	int [][] lineNumberTable = getLineNumberTable(codeToAdjust, offset);
            	if (lineNumberTable != null)
            	{
            		// this offset is used to reconstruct original source lines:
	                int oldLineOffset = srcMethodModel != null ? srcMethodModel._lineOffset : 0;
	                // while iterating all lines and creating an appropriate LineInfo
	                // compute the offset between new byte code lines and src:
					newLineOffset = mapLines(dstMethodBinding.copyInheritanceSrc.declaringClass,
	                					  lineNumberTable,
	                					  dstType.roleModel.getLineNumberProvider(),
	                					  oldLineOffset);
	                // patch the new byte code lines into the byte code attribute
	                for (int j=0; j<lineNumberTable.length; j++)
	                {
           				if (lineNumberTable[j][1] < ISMAPConstants.STEP_INTO_LINENUMBER) {
		                	int numberOffset = offset+2   /*skip numbersCount*/
							                         +j*4 /*previous entries*/
													 +2   /*skip start_pc*/;
		                	this._writer.write2(codeToAdjust, numberOffset, lineNumberTable[j][1] - oldLineOffset + newLineOffset);
           				}
	                }
                }
            } else if (CharOperation.equals(attrName, LocalVariableTableName))
            {
                int localsCount = OTByteCodes.getWord(codeToAdjust, offset);
                for (int j=0; j<localsCount; j++)
                {
                    int localBase  = offset+2/*localsCount*/+j*10;

                    int nameOffset = localBase+4;
                    updateCPO(dstType, codeToAdjust, nameOffset, 2); // local variable name

                    int descOffset = localBase+6;           // type descriptor

                    int slotOffset = localBase+8;           // local variable slot
                    int slotNumber = OTByteCodes.getWord(codeToAdjust, slotOffset);

                    if (slotNumber < arguments.length)
                    {
                        // map from parameters of the MethodBinding
                        this._writer.writeUtf8(
                                codeToAdjust,
                                descOffset,
                                arguments[slotNumber].signature());
                    } else {
                        // lookup pure local variable from environment
                        int descRef    = OTByteCodes.getRef(2, codeToAdjust, descOffset);
                        TypeBinding typeBinding    = this._reader.getSignatureBinding(descRef, false/*generic*/);
                        ConstantPoolObject typeCPO = this._mapper.mapTypeUtf8(typeBinding, false/*generic*/);
                        this._writer.writeConstantPoolObject(codeToAdjust, descOffset, 2, typeCPO);
                    }
                }
            } else if (CharOperation.equals(attrName, LocalVariableTypeTableName))
            {
                int localsCount = OTByteCodes.getWord(codeToAdjust, offset);
                for (int j=0; j<localsCount; j++)
                {
                    int localBase  = offset+2/*localsCount*/+j*10;

                    int nameOffset = localBase+4;
                    updateCPO(dstType, codeToAdjust, nameOffset, 2); // local variable name

                    int descOffset = localBase+6;           // type descriptor

                    int slotOffset = localBase+8;           // local variable slot
                    int slotNumber = OTByteCodes.getWord(codeToAdjust, slotOffset);

                    if (slotNumber < arguments.length)
                    {
                        // map from parameters of the MethodBinding
                        this._writer.writeUtf8(
                                codeToAdjust,
                                descOffset,
                                arguments[slotNumber].genericTypeSignature());
                    } else {
                        // lookup pure local variable from environment
                        int descRef    = OTByteCodes.getRef(2, codeToAdjust, descOffset);
                        TypeBinding typeBinding    = this._reader.getSignatureBinding(descRef, true/*generic*/);
                        ConstantPoolObject typeCPO = this._mapper.mapTypeUtf8(typeBinding, true/*generic*/);
                        this._writer.writeConstantPoolObject(codeToAdjust, descOffset, 2, typeCPO);
                    }
                }
            } else if (CharOperation.equals(attrName, StackMapTableName))
            {
            	rewriteStackMapTable(codeToAdjust, offset);
            }
            offset += attrLen;
        }
        return newLineOffset;
    }
	/**
	 * Maps all lines in lineNumberTable to possibly new locations adding the necessary LineInfo to the
	 * line number provider.
	 *
	 * @param copySrc         class the defined the original method
	 * @param lineNumberTable line number table of method to copy
	 * @param provider        line number provider
	 * @param oldLineOffset   offset lines within lineNumberTable wrt original source lines
	 * @return                the new offset to use in the destination method.
	 */
    private int mapLines(ReferenceBinding copySrc, int[][] lineNumberTable, LineNumberProvider provider, int oldLineOffset)
    {
    	if (lineNumberTable.length == 0) return 0; // no lines -> no offset
		int firstLine = Integer.MAX_VALUE;
		int lastLine = 0;
		for (int[] line : lineNumberTable) {
			if (line[1] < ISMAPConstants.STEP_INTO_LINENUMBER) {
				firstLine = Math.min(firstLine, line[1]);
				lastLine = Math.max(lastLine, line[1]);
			}
		}
		if (firstLine == Integer.MAX_VALUE)
			return 0; // apparently no relevant line number found
		// unapply old offset to yield real source lines:
		firstLine -= oldLineOffset;
		lastLine -= oldLineOffset;
		LineInfo info = provider.addLineInfo(copySrc, firstLine, lastLine - firstLine + 1);
		return info.getOutputStartLine() - firstLine;
	}

	private void rewriteStackMapTable(byte[] codeToAdjust, int offset) {
    	// this code is a stripped down version of StackMapFrame.<init> (from model):
        int numberOfEntries = OTByteCodes.getWord(codeToAdjust, offset);
        offset += 2;
        for (int j=0; j<numberOfEntries; j++)
        {
        	int type = (codeToAdjust[offset] & 0xFF);
        	int readOffset = 0;
        	switch(type) {
			case 247 : // SAME_LOCALS_1_STACK_ITEM_EXTENDED
				readOffset = 3;
				readOffset += rewriteVerificationInfo(codeToAdjust, offset + readOffset);
				break;
			case 248 :
			case 249 :
			case 250:
				// CHOP
				readOffset = 3;
				break;
			case 251 :
				// SAME_FRAME_EXTENDED
				readOffset = 3;
				break;
			case 252 :
			case 253 :
			case 254 :
				// APPEND
				readOffset = 3;
				int diffLocals = type - 251;
				for (int i = 0; i < diffLocals; i++) {
					readOffset += rewriteVerificationInfo(codeToAdjust, offset + readOffset);
				}
				break;
			case 255 :
				// FULL_FRAME
				int tempLocals = OTByteCodes.getWord(codeToAdjust, offset + 3);
				readOffset = 5;
				if (tempLocals != 0) {
					for (int i = 0; i < tempLocals; i++) {
						readOffset += rewriteVerificationInfo(codeToAdjust, offset + readOffset);
					}
				}
				int tempStackItems = OTByteCodes.getWord(codeToAdjust, offset + readOffset);
				readOffset += 2;
				if (tempStackItems != 0) {
					for (int i = 0; i < tempStackItems; i++) {
						readOffset += rewriteVerificationInfo(codeToAdjust, offset + readOffset);
					}
				}
				break;
			default:
				if (type <= 63) {
					// SAME_FRAME
					readOffset = 1;
				} else if (type <= 127) {
					// SAME_LOCALS_1_STACK_ITEM
					readOffset = 1;
					readOffset += rewriteVerificationInfo(codeToAdjust, offset + readOffset);
				}
        	}
			offset += readOffset;
        }
	}

	private int rewriteVerificationInfo(byte[] codeToAdjust, int offset) {
		// this code is a stripped down version of VerificationInfo.<init> (from model):
		final int t = (codeToAdjust[offset] & 0xFF);
		int readOffset = 1;
		switch(t) {
			case IVerificationTypeInfo.ITEM_OBJECT :
				updateCPO(null, codeToAdjust, offset+1, 2);
				readOffset += 2;
				break;
			case IVerificationTypeInfo.ITEM_UNINITIALIZED :
				readOffset += 2;
		}
		return readOffset;
	}

	// parameter dstType is in preparation of JDK1.4 compatibility: adjust declaringClass to actual receiver.
    private ConstantPoolObject updateCPO(SourceTypeBinding dstType, byte [] code, int offset, int length)
    {
        int ref =  OTByteCodes.getRef(2, code, offset);
        if (ref != 0)
        {
            ConstantPoolObject srcCPO = this._reader.readConstantPoolObject(ref, length);
            ConstantPoolObject dstCPO = this._mapper.mapConstantPoolObject(srcCPO);
            this._writer.writeConstantPoolObject(code, offset, length, dstCPO);
            return dstCPO;
        }
        return null;
    }

	/**
	 * When copying a team constructor which might require lifting,
	 * peek into the byte code to find the self call.
	 * @param model
	 * @return called constructor or null
	 */
	public MethodBinding peekConstructorCall(
			TeamModel teamModel, MethodModel model, LookupEnvironment environment)
	{
		byte[] bytes = model.getBytes();
		int offset = model.getStructOffset();
		this._reader =
			new ConstantPoolObjectReader(model, teamModel, environment);
        int codeAttributeOffset = findCodeAttribute(bytes, offset);
        int start = codeAttributeOffset+CODE_ATTR_PREFIX_LEN;
        int i = 0;
        while(true) {
			byte b_int = bytes[start+i];
			int length = OTByteCodes.cpReferenceLength(b_int);
			if (length != 0) {
				int ref = OTByteCodes.getRef(length, bytes, start+i+1);
				ConstantPoolObject src_cpo = this._reader.readConstantPoolObject(ref, length);
				if (!src_cpo.isMethod() || !src_cpo.getMethodRef().isConstructor()) {
					if (src_cpo.isSpecificType(TypeConstants.JAVA_LANG_ERROR)) {
						try {
							// search subsequent string
							b_int = bytes[start+i+4];
							length = OTByteCodes.cpReferenceLength(b_int);
							src_cpo = this._reader.readConstantPoolObject(OTByteCodes.getRef(length, bytes, start+i+5), length);
							if (src_cpo.getType() == ClassFileConstants.StringTag)
								if (src_cpo.getString().startsWith("Unresolved compilation problem:")) //$NON-NLS-1$
									model.getBinding().bytecodeMissing = true; // signal that byte code is not usable due to compile error
						} catch (Throwable t) {
							// nop, string not found
						}
					}
					return null;
				}
				return src_cpo.getMethodRef();
			} else if (   OTByteCodes.getAloadPos(b_int, bytes[start+i+1]) != i
					   && b_int != Opcodes.OPC_aconst_null)
				return null;
			i++;
        }
	}
}

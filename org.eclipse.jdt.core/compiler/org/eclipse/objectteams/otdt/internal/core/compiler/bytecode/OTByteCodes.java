/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Fraunhofer FIRST - Initial API and implementation
 * 		Technical University Berlin - Initial API and implementation
 * 
 * This file is a modified version of class org.apache.bcel.Constants
 * originating from the Apache BCEL project which was provided under the 
 * Apache 2.0 license. Original Copyright from BCEL:
 * 
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.internal.compiler.codegen.Opcodes;

/**
 * @author stephan
 * @version $Id$
 */
public class OTByteCodes implements Opcodes {

	/**
	 * Checks if a reference into the constant pool must follow
	 * the given byte code. If so the length of the reference
	 * is returned, otherwise 0. (Not complete yet)
	 * @param byteCode
	 * @return int
	 **/
	public static int cpReferenceLength(byte byteCode) {
		switch (byteCode) {
			case OPC_invokeinterface: // additional parameters covered by getParamLength.
			case OPC_invokevirtual:
			case OPC_invokestatic:
			case OPC_invokespecial:
			case OPC_new:
			case OPC_anewarray:
			case OPC_getfield:
			case OPC_getstatic:
			case OPC_putfield:
			case OPC_putstatic:
			case OPC_instanceof:
			case OPC_checkcast:
			case OPC_ldc_w:
			case OPC_ldc2_w:
				return 2;
			case OPC_ldc:
				return 1;
			case OPC_multianewarray:
                return 2;
				//throw new RuntimeException("bytecode multianewarray not supported in role methods.");
			default:
				return 0;
		}
	}

	/**
	 * Compute the length of parameters which are not references into the constant pool.
	 *
	 * @param op
	 * @param code
	 * @param idx     where op is found in code
	 * @param bytecodeOffset position into code, where the actual bytecode starts.
	 * @return length
	 */
	public static short getParamLength(byte op, byte[] code, int idx, int bytecodeOffset)
	{
        int padding, n_match;
		switch (op) {
			case OPC_goto: case OPC_ifeq: case OPC_ifge:
			case OPC_ifle:      case OPC_iflt:   case OPC_jsr:
			case OPC_ifne:
		    case OPC_ifnonnull: case OPC_ifnull:
			case OPC_if_acmpeq:  case OPC_if_acmpne:
			case OPC_if_icmpeq: case OPC_if_icmpge:
			case OPC_if_icmpgt:  case OPC_if_icmple:
			case OPC_if_icmplt:   case OPC_if_icmpne:

			case OPC_invokeinterface: // this has two non-cp parameters.
			case OPC_iinc:
				return 2;
			case OPC_goto_w:    case OPC_jsr_w:
				return 4;
			case OPC_aload:  case OPC_astore: case OPC_dload:
			case OPC_dstore:  case OPC_fload:   case OPC_fstore:
			case OPC_iload:   case OPC_istore:  case OPC_lload:
			case OPC_lstore:  case OPC_ret: case OPC_multianewarray:
				return 1;
			case OPC_wide:
			    return 3; // swallow opcode and wide parameter
			case OPC_getfield: case OPC_putfield:
			case OPC_getstatic: case OPC_putstatic:
			case OPC_new:  case OPC_anewarray:
			case OPC_checkcast: case OPC_instanceof:
			case OPC_invokespecial: case OPC_invokestatic:
			case OPC_invokevirtual:
			case OPC_ldc_w: case OPC_ldc2_w: case OPC_ldc:
				return 0; // cp index already swallowed by cpReferenceLength.
		    case OPC_lookupswitch:
                idx++; // goto start of arguments
				padding = (4 - ((idx-bytecodeOffset) % 4)) % 4; // Compute number of pad bytes
                // ignore first argument "default"
				n_match = getInt(code, idx+padding+4);
   				return (short)(8 + padding + n_match * 8); // report arguments length without opcode
            case OPC_tableswitch:
                idx++;
                padding = (4 - ((idx-bytecodeOffset) % 4)) % 4 ;
                // ignore first argument "default" at idx+padding+0
                int low = getInt(code, idx+padding+4);
                int high = getInt(code, idx+padding+8);
                n_match = high - low + 1;
                return (short)(12+padding + n_match * 4);
			default:
			int ib;

			if(op<0) ib = op & 0x0FF;
			else ib = op;

				if(NO_OF_OPERANDS[ib] > 0) {
					short n = 0;
       				for(int i=0; i < LENGTH_OF_OPERANDS[ib].length; i++)
       					n+=LENGTH_OF_OPERANDS[ib][i];
					return n;
				} else if (NO_OF_OPERANDS[ib] < 0)
					throw new RuntimeException("Opcode "+op+" not supported for role methods"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return 0;
	}

    public static boolean isPush(int op) {
        return (op >= OPC_aconst_null) &&
            (op <= OPC_saload);
    }

    public static int getAloadPos(int op, int arg) {
    	switch(op) {
    	case OPC_aload_0:
    	case OPC_aload_1:
    	case OPC_aload_2:
    	case OPC_aload_3:
    		return op-OPC_aload_0;
    	case OPC_aload:
    		return arg;
    	}
    	return -1;
    }

    public static int getAstorePos(int op, int arg) {
    	switch(op) {
    	case OPC_astore_0:
    	case OPC_astore_1:
    	case OPC_astore_2:
    	case OPC_astore_3:
    		return op-OPC_astore_0;
    	case OPC_astore:
    		return arg;
    	}
    	return -1;
    }

	public static int getWord(byte[] code, int idx) {
		int bp = idx;
		return ((code[bp++] & 255)<< 8) + (code[bp++] & 255);
	}

	public static int getInt (byte[] code, int idx) {
		int bp = idx;
		return ((code[bp++] & 255)<< 24) + ((code[bp++] & 255)<< 16) +
                ((code[bp++] & 255)<< 8) + (code[bp++] & 255);
	}

	public static int getUnsignedByte (byte[] code, int idx) {
        if (idx >= code.length) return -1;
		byte b = code[idx];
		// bytes are signed, unsigned value needed -> convert to int when negative
		if (b < 0)
			return b & 0x0ff;
		else
			return b;
	}

    public static void setWord(byte[] code, int offset, int value) {
        code[offset]   = (byte)(value >> 8);
        code[offset+1] = (byte)value;
    }

    public static void setInt(byte[] code, int offset, int value) {
        code[offset++] = (byte)(value >> 24);
        code[offset++] = (byte)(value >> 16);
        code[offset++] = (byte)(value >> 8);
        code[offset++] = (byte)value;
    }

    /**
     * Reference in big endian order.
     **/
    public  static int getRef(int length, byte[] code, int offset) {
    	int ref = -1;
    	switch (length) {
    		case 1:	ref = getUnsignedByte(code, offset); break;
    		case 2:
    		case 4: // [SH] also invokeinterface is followed by a two byte reference!
    					ref = getUnsignedByte(code, offset);
    					ref <<= 8;
    					ref |= getUnsignedByte(code, offset+1); break;
    	}
    	return ref;
    }

    /**
     * Reference in big endian order.
     **/
    public static void setRef(int length, byte[] code, int cp, short newRef) {
    	switch (length) {
    		case 1:	code[cp] = (byte)newRef; break;
    		case 2:	code[cp+1] = (byte)newRef;
    					newRef >>= 8;
    					code[cp] = (byte)newRef; break;
    	}
    }

	// SH: stolen from org.apache.bcel.Constants
	// (used to calculated length of byte code parameters):
	static final short UNDEFINED=-1;
	static final short UNPREDICTABLE=-2;
	static final short RESERVED=-3;
	static final short[] NO_OF_OPERANDS = {
    0/*nop*/, 0/*aconst_null*/, 0/*iconst_m1*/, 0/*iconst_0*/,
    0/*iconst_1*/, 0/*iconst_2*/, 0/*iconst_3*/, 0/*iconst_4*/,
    0/*iconst_5*/, 0/*lconst_0*/, 0/*lconst_1*/, 0/*fconst_0*/,
    0/*fconst_1*/, 0/*fconst_2*/, 0/*dconst_0*/, 0/*dconst_1*/,
    1/*bipush*/, 2/*sipush*/, 1/*ldc*/, 2/*ldc_w*/, 2/*ldc2_w*/,
    1/*iload*/, 1/*lload*/, 1/*fload*/, 1/*dload*/, 1/*aload*/,
    0/*iload_0*/, 0/*iload_1*/, 0/*iload_2*/, 0/*iload_3*/,
    0/*lload_0*/, 0/*lload_1*/, 0/*lload_2*/, 0/*lload_3*/,
    0/*fload_0*/, 0/*fload_1*/, 0/*fload_2*/, 0/*fload_3*/,
    0/*dload_0*/, 0/*dload_1*/, 0/*dload_2*/, 0/*dload_3*/,
    0/*aload_0*/, 0/*aload_1*/, 0/*aload_2*/, 0/*aload_3*/,
    0/*iaload*/, 0/*laload*/, 0/*faload*/, 0/*daload*/,
    0/*aaload*/, 0/*baload*/, 0/*caload*/, 0/*saload*/,
    1/*istore*/, 1/*lstore*/, 1/*fstore*/, 1/*dstore*/,
    1/*astore*/, 0/*istore_0*/, 0/*istore_1*/, 0/*istore_2*/,
    0/*istore_3*/, 0/*lstore_0*/, 0/*lstore_1*/, 0/*lstore_2*/,
    0/*lstore_3*/, 0/*fstore_0*/, 0/*fstore_1*/, 0/*fstore_2*/,
    0/*fstore_3*/, 0/*dstore_0*/, 0/*dstore_1*/, 0/*dstore_2*/,
    0/*dstore_3*/, 0/*astore_0*/, 0/*astore_1*/, 0/*astore_2*/,
    0/*astore_3*/, 0/*iastore*/, 0/*lastore*/, 0/*fastore*/,
    0/*dastore*/, 0/*aastore*/, 0/*bastore*/, 0/*castore*/,
    0/*sastore*/, 0/*pop*/, 0/*pop2*/, 0/*dup*/, 0/*dup_x1*/,
    0/*dup_x2*/, 0/*dup2*/, 0/*dup2_x1*/, 0/*dup2_x2*/, 0/*swap*/,
    0/*iadd*/, 0/*ladd*/, 0/*fadd*/, 0/*dadd*/, 0/*isub*/,
    0/*lsub*/, 0/*fsub*/, 0/*dsub*/, 0/*imul*/, 0/*lmul*/,
    0/*fmul*/, 0/*dmul*/, 0/*idiv*/, 0/*ldiv*/, 0/*fdiv*/,
    0/*ddiv*/, 0/*irem*/, 0/*lrem*/, 0/*frem*/, 0/*drem*/,
    0/*ineg*/, 0/*lneg*/, 0/*fneg*/, 0/*dneg*/, 0/*ishl*/,
    0/*lshl*/, 0/*ishr*/, 0/*lshr*/, 0/*iushr*/, 0/*lushr*/,
    0/*iand*/, 0/*land*/, 0/*ior*/, 0/*lor*/, 0/*ixor*/, 0/*lxor*/,
    2/*iinc*/, 0/*i2l*/, 0/*i2f*/, 0/*i2d*/, 0/*l2i*/, 0/*l2f*/,
    0/*l2d*/, 0/*f2i*/, 0/*f2l*/, 0/*f2d*/, 0/*d2i*/, 0/*d2l*/,
    0/*d2f*/, 0/*i2b*/, 0/*i2c*/, 0/*i2s*/, 0/*lcmp*/, 0/*fcmpl*/,
    0/*fcmpg*/, 0/*dcmpl*/, 0/*dcmpg*/, 2/*ifeq*/, 2/*ifne*/,
    2/*iflt*/, 2/*ifge*/, 2/*ifgt*/, 2/*ifle*/, 2/*if_icmpeq*/,
    2/*if_icmpne*/, 2/*if_icmplt*/, 2/*if_icmpge*/, 2/*if_icmpgt*/,
    2/*if_icmple*/, 2/*if_acmpeq*/, 2/*if_acmpne*/, 2/*goto*/,
    2/*jsr*/, 1/*ret*/, UNPREDICTABLE/*tableswitch*/, UNPREDICTABLE/*lookupswitch*/,
    0/*ireturn*/, 0/*lreturn*/, 0/*freturn*/,
    0/*dreturn*/, 0/*areturn*/, 0/*return*/,
    2/*getstatic*/, 2/*putstatic*/, 2/*getfield*/,
    2/*putfield*/, 2/*invokevirtual*/, 2/*invokespecial*/, 2/*invokestatic*/,
    4/*invokeinterface*/, UNDEFINED, 2/*new*/,
    1/*newarray*/, 2/*anewarray*/,
    0/*arraylength*/, 0/*athrow*/, 2/*checkcast*/,
    2/*instanceof*/, 0/*monitorenter*/,
    0/*monitorexit*/, UNPREDICTABLE/*wide*/, 3/*multianewarray*/,
    2/*ifnull*/, 2/*ifnonnull*/, 4/*goto_w*/,
    4/*jsr_w*/, 0/*breakpoint*/, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, RESERVED/*impdep1*/, RESERVED/*impdep2*/
  };


	static final int T_BYTE=1;
	static final int T_SHORT=2;
	static final int T_INT=4;
	  public static final short[][] LENGTH_OF_OPERANDS = {
    {}/*nop*/, {}/*aconst_null*/, {}/*iconst_m1*/, {}/*iconst_0*/,
    {}/*iconst_1*/, {}/*iconst_2*/, {}/*iconst_3*/, {}/*iconst_4*/,
    {}/*iconst_5*/, {}/*lconst_0*/, {}/*lconst_1*/, {}/*fconst_0*/,
    {}/*fconst_1*/, {}/*fconst_2*/, {}/*dconst_0*/, {}/*dconst_1*/,
    {T_BYTE}/*bipush*/, {T_SHORT}/*sipush*/, {T_BYTE}/*ldc*/,
    {T_SHORT}/*ldc_w*/, {T_SHORT}/*ldc2_w*/,
    {T_BYTE}/*iload*/, {T_BYTE}/*lload*/, {T_BYTE}/*fload*/,
    {T_BYTE}/*dload*/, {T_BYTE}/*aload*/, {}/*iload_0*/,
    {}/*iload_1*/, {}/*iload_2*/, {}/*iload_3*/, {}/*lload_0*/,
    {}/*lload_1*/, {}/*lload_2*/, {}/*lload_3*/, {}/*fload_0*/,
    {}/*fload_1*/, {}/*fload_2*/, {}/*fload_3*/, {}/*dload_0*/,
    {}/*dload_1*/, {}/*dload_2*/, {}/*dload_3*/, {}/*aload_0*/,
    {}/*aload_1*/, {}/*aload_2*/, {}/*aload_3*/, {}/*iaload*/,
    {}/*laload*/, {}/*faload*/, {}/*daload*/, {}/*aaload*/,
    {}/*baload*/, {}/*caload*/, {}/*saload*/, {T_BYTE}/*istore*/,
    {T_BYTE}/*lstore*/, {T_BYTE}/*fstore*/, {T_BYTE}/*dstore*/,
    {T_BYTE}/*astore*/, {}/*istore_0*/, {}/*istore_1*/,
    {}/*istore_2*/, {}/*istore_3*/, {}/*lstore_0*/, {}/*lstore_1*/,
    {}/*lstore_2*/, {}/*lstore_3*/, {}/*fstore_0*/, {}/*fstore_1*/,
    {}/*fstore_2*/, {}/*fstore_3*/, {}/*dstore_0*/, {}/*dstore_1*/,
    {}/*dstore_2*/, {}/*dstore_3*/, {}/*astore_0*/, {}/*astore_1*/,
    {}/*astore_2*/, {}/*astore_3*/, {}/*iastore*/, {}/*lastore*/,
    {}/*fastore*/, {}/*dastore*/, {}/*aastore*/, {}/*bastore*/,
    {}/*castore*/, {}/*sastore*/, {}/*pop*/, {}/*pop2*/, {}/*dup*/,
    {}/*dup_x1*/, {}/*dup_x2*/, {}/*dup2*/, {}/*dup2_x1*/,
    {}/*dup2_x2*/, {}/*swap*/, {}/*iadd*/, {}/*ladd*/, {}/*fadd*/,
    {}/*dadd*/, {}/*isub*/, {}/*lsub*/, {}/*fsub*/, {}/*dsub*/,
    {}/*imul*/, {}/*lmul*/, {}/*fmul*/, {}/*dmul*/, {}/*idiv*/,
    {}/*ldiv*/, {}/*fdiv*/, {}/*ddiv*/, {}/*irem*/, {}/*lrem*/,
    {}/*frem*/, {}/*drem*/, {}/*ineg*/, {}/*lneg*/, {}/*fneg*/,
    {}/*dneg*/, {}/*ishl*/, {}/*lshl*/, {}/*ishr*/, {}/*lshr*/,
    {}/*iushr*/, {}/*lushr*/, {}/*iand*/, {}/*land*/, {}/*ior*/,
    {}/*lor*/, {}/*ixor*/, {}/*lxor*/, {T_BYTE, T_BYTE}/*iinc*/,
    {}/*i2l*/, {}/*i2f*/, {}/*i2d*/, {}/*l2i*/, {}/*l2f*/, {}/*l2d*/,
    {}/*f2i*/, {}/*f2l*/, {}/*f2d*/, {}/*d2i*/, {}/*d2l*/, {}/*d2f*/,
    {}/*i2b*/, {}/*i2c*/,{}/*i2s*/, {}/*lcmp*/, {}/*fcmpl*/,
    {}/*fcmpg*/, {}/*dcmpl*/, {}/*dcmpg*/, {T_SHORT}/*ifeq*/,
    {T_SHORT}/*ifne*/, {T_SHORT}/*iflt*/, {T_SHORT}/*ifge*/,
    {T_SHORT}/*ifgt*/, {T_SHORT}/*ifle*/, {T_SHORT}/*if_icmpeq*/,
    {T_SHORT}/*if_icmpne*/, {T_SHORT}/*if_icmplt*/,
    {T_SHORT}/*if_icmpge*/, {T_SHORT}/*if_icmpgt*/,
    {T_SHORT}/*if_icmple*/, {T_SHORT}/*if_acmpeq*/,
    {T_SHORT}/*if_acmpne*/, {T_SHORT}/*goto*/, {T_SHORT}/*jsr*/,
    {T_BYTE}/*ret*/, {}/*tableswitch*/, {}/*lookupswitch*/,
    {}/*ireturn*/, {}/*lreturn*/, {}/*freturn*/, {}/*dreturn*/,
    {}/*areturn*/, {}/*return*/, {T_SHORT}/*getstatic*/,
    {T_SHORT}/*putstatic*/, {T_SHORT}/*getfield*/,
    {T_SHORT}/*putfield*/, {T_SHORT}/*invokevirtual*/,
    {T_SHORT}/*invokespecial*/, {T_SHORT}/*invokestatic*/,
    {T_SHORT, T_BYTE, T_BYTE}/*invokeinterface*/, {},
    {T_SHORT}/*new*/, {T_BYTE}/*newarray*/,
    {T_SHORT}/*anewarray*/, {}/*arraylength*/, {}/*athrow*/,
    {T_SHORT}/*checkcast*/, {T_SHORT}/*instanceof*/,
    {}/*monitorenter*/, {}/*monitorexit*/, {T_BYTE}/*wide*/,
    {T_SHORT, T_BYTE}/*multianewarray*/, {T_SHORT}/*ifnull*/,
    {T_SHORT}/*ifnonnull*/, {T_INT}/*goto_w*/, {T_INT}/*jsr_w*/,
    {}/*breakpoint*/, {}, {}, {}, {}, {}, {}, {},
    {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
    {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
    {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
    {}/*impdep1*/, {}/*impdep2*/
	};

}

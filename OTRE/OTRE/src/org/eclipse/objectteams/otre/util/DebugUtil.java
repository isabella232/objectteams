/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2003-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DebugUtil.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;

import de.fub.bytecode.generic.*;
import de.fub.bytecode.Constants;
import java.util.Enumeration;

import org.eclipse.objectteams.otre.OTConstants;

/**
 * @author Stephan Herrmann
 */
@SuppressWarnings("nls")
public class DebugUtil {

	// -----------------------------------------
	// -------- Development utilities  ---------
	// -----------------------------------------

    /**
    *  Creates an the instructions necessary to "System.out.println" 
	*  the given string.
    *
    *  @param cpg     the constant pool of the class where this instructions 
	*                 will be inserted
    *  @param string  the String to be printed
    *  @return        an InstructionList containing the necessary instructions
    */
	public static InstructionList createPrintln(ConstantPoolGen cpg, 
												InstructionFactory factory,
												String string) 
	{
        InstructionList il  = new InstructionList();
        ObjectType p_stream = new ObjectType("java.io.PrintStream");

        il.append(factory.createFieldAccess("java.lang.System", "out", 
											p_stream,
											Constants.GETSTATIC));
        il.append(new PUSH(cpg, string + "\n"));
        il.append(factory.createInvoke("java.io.PrintStream", "print", 
									   Type.VOID, 
									   new Type[] { Type.STRING }, 
									   Constants.INVOKEVIRTUAL));
        return il;
    }

	/**
	 * Create a <tt>System.out.println</tt> call for an <tt>Object</tt>
	 * argument. The argument is assumed to be on the stack and will not
	 * be consumed.
	 */
    public static InstructionList createPrintlnObj(InstructionFactory factory) {
        InstructionList il  = new InstructionList();
        ObjectType p_stream = new ObjectType("java.io.PrintStream");

		il.append(new DUP());
        il.append(factory.createFieldAccess("java.lang.System", "out", 
											p_stream,
											Constants.GETSTATIC));
		il.append(new SWAP());
        il.append(factory.createInvoke("java.io.PrintStream", "print", 
									   Type.VOID, 
									   new Type[] { OTConstants.object }, 
									   Constants.INVOKEVIRTUAL));
        return il;
    }

	/**
	 * Create a <tt>System.out.println</tt> call for an <tt>Exception</tt>
	 * argument. The argument is assumed to be on the stack and _will_
	 * be consumed. This effect is however only performed, if 
	 * the property ot.log.lift is set.
	 */
    public static InstructionList createReportExc(InstructionFactory factory) {
        InstructionList il  = new InstructionList();
		if (System.getProperty("ot.log.lift") == null) {
			il.append(new POP());
		} else {
			ObjectType p_stream = new ObjectType("java.io.PrintStream");
			
			il.append(factory.createFieldAccess("java.lang.System", "out", 
												p_stream,
												Constants.GETSTATIC));
			il.append(new SWAP());
			il.append(factory.createInvoke("java.io.PrintStream", "println", 
										   Type.VOID, 
										   new Type[] { OTConstants.object }, 
										   Constants.INVOKEVIRTUAL));
		}
        return il;
    }

	/**
	 * Create a <tt>System.out.println</tt> call for an <tt>int</tt>
	 * argument. The argument is assumed to be on the stack and will not
	 * be consumed.
	 */
    public static InstructionList createPrintlnInt(InstructionFactory factory) {
        InstructionList il  = new InstructionList();
        ObjectType p_stream = new ObjectType("java.io.PrintStream");

		il.append(new DUP());
        il.append(factory.createFieldAccess("java.lang.System", "out", 
											p_stream,
											Constants.GETSTATIC));
		il.append(new SWAP());
        il.append(factory.createInvoke("java.io.PrintStream", "print", 
									   Type.VOID, 
									   new Type[] { Type.INT }, 
									   Constants.INVOKEVIRTUAL));
        return il;
    }
    
    public static InstructionList createPrintlnBool(ConstantPoolGen cp) {
    	InstructionList il= new InstructionList();
    	int             out     = cp.addFieldref("java.lang.System", "out",
    											 "Ljava/io/PrintStream;");
    	int             println = cp.addMethodref("java.io.PrintStream", "println",
    											  "(Z)V");
    	il.append(new DUP());
    	il.append(new GETSTATIC(out));
    	il.append(new SWAP());
    	il.append(new INVOKEVIRTUAL(println));
    	return il;
    }

    @SuppressWarnings("unchecked")
	public static void printIL (InstructionList il, ConstantPoolGen cpg) {
        int off = 0;
        Enumeration en =  il.elements();
        while(en.hasMoreElements()) {
            Instruction i = ((InstructionHandle)en.nextElement()).getInstruction();
            off += i.produceStack(cpg);
            off -= i.consumeStack(cpg);
            System.out.print(off);
            System.out.println("  = "+i);
        }
    }

}

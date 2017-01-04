/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2011, 2015 GK Software AG.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.bytecode.asm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.objectteams.otredyn.transformer.names.ClassNames;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import static org.eclipse.objectteams.otredyn.bytecode.asm.AsmBoundClass.ASM_API;

/**
 * This adapter adds some instructions to the front of the main-method.
 * These instructions will instantiate and globally activate all teams 
 * that are listed in the team config file.
 * The team config file is specified via the <code>ot.teamconfig</code> property.
 */
public class AddGlobalTeamActivationAdapter extends ClassVisitor {
	
	/** Initialized from property <tt>ot.teamconfig</tt>. */
	private final static String TEAM_CONFIG_FILE = System.getProperty("ot.teamconfig");
	/**	Marker for comment lines in the team config file. */    
	private final static String COMMENT_MARKER = "#";
	 
	private static boolean done = false;

	private AddGlobalTeamActivationAdapter(ClassVisitor cv) {
		super(ASM_API, cv);
	}

	/**
	 * Adds the one and only {@link AddGlobalTeamActivationAdapter} to the given multiAdaptor,
	 * if needed and if not already done.
	 * @param multiAdapter
	 * @param writer
	 */
	synchronized public static void checkAddVisitor(MultiClassAdapter multiAdapter, ClassWriter writer) {
		if (done || TEAM_CONFIG_FILE == null)
			return;
		multiAdapter.addVisitor(new AddGlobalTeamActivationAdapter(writer));		
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		synchronized (AddGlobalTeamActivationAdapter.class) {			
			if (!done && isMainMethod(name, desc, access)) {
				done = true;
				final MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, null, null);
				return new AdviceAdapter(this.api, methodVisitor, access, name, desc) {
					@Override
					protected void onMethodEnter() {
						List<String> teams = getTeamsFromConfigFile();
						for (String aTeam : teams) {
							Label start, end, typeHandler, ctorHandler, after;
							
							String aTeamSlash = aTeam.replace('.', '/');
							
							// new SomeTeam():
							methodVisitor.visitLabel(start=new Label());
							methodVisitor.visitTypeInsn(Opcodes.NEW, aTeamSlash);
							// 		.activate(Team.ALL_THREADS):
							methodVisitor.visitInsn(Opcodes.DUP);
							methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, aTeamSlash, "<init>", "()V", false);
							methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, ClassNames.TEAM_SLASH, "ALL_THREADS", "Ljava/lang/Thread;");
							methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, aTeamSlash, "activate", "(Ljava/lang/Thread;)V", false);
							
							methodVisitor.visitLabel(end=new Label());
							methodVisitor.visitJumpInsn(Opcodes.GOTO, after=new Label());
							
							// catch (ClassNotFoundException, NoClassDefFoundError):
							//   System.err.println(...)
							methodVisitor.visitLabel(typeHandler=new Label());
							methodVisitor.visitInsn(Opcodes.POP); // discard the exception
							methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
							methodVisitor.visitLdcInsn("Config error: Team class '"+aTeam+ "' in config file '"+ TEAM_CONFIG_FILE+"' can not be found!");
							methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
							methodVisitor.visitJumpInsn(Opcodes.GOTO, after);
							methodVisitor.visitTryCatchBlock(start, end, typeHandler, "java/lang/ClassNotFoundException");
// dup to avoid stackmap errors (ASM bug at 1.8)
							methodVisitor.visitLabel(typeHandler=new Label());
							methodVisitor.visitInsn(Opcodes.POP); // discard the exception
							methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
							methodVisitor.visitLdcInsn("Config error: Team class '"+aTeam+ "' in config file '"+ TEAM_CONFIG_FILE+"' can not be found!");
							methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
							methodVisitor.visitJumpInsn(Opcodes.GOTO, after);
//
							methodVisitor.visitTryCatchBlock(start, end, typeHandler, "java/lang/NoClassDefFoundError");

							// catch (NoSuchMethodError):
							//   System.err.println(...)
							methodVisitor.visitLabel(ctorHandler=new Label());
							methodVisitor.visitInsn(Opcodes.POP); // discard the exception
							methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
							methodVisitor.visitLdcInsn("Activation failed: Team class '"+aTeam+ "' has no default constuctor!");
							methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
							methodVisitor.visitTryCatchBlock(start, end, ctorHandler, "java/lang/NoSuchMethodError");

							methodVisitor.visitLabel(after);
						}
					}
					@Override
					public void visitMaxs(int maxStack, int maxLocals) {
						super.visitMaxs(Math.max(maxStack,3), maxLocals);
					}
				};
			}
			return null;	
		}
    }

	private boolean isMainMethod(String name, String desc, int access) {
		if (access != (Opcodes.ACC_STATIC|Opcodes.ACC_PUBLIC))
			return false;
		if (!"main".equals(name))
			return false;
		if (!"([Ljava/lang/String;)V".equals(desc))
			return false;
		return true;
	}
	
	/**
	 * @return a list of teams in the team initialization config file
	 */
	private static List<String> getTeamsFromConfigFile() {
		List<String> result = new ArrayList<String>();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(TEAM_CONFIG_FILE)))) {
 			while (in.ready()) {
				String nextLine = in.readLine();
				String nextTeam = nextLine.trim();
				if (nextTeam.startsWith(COMMENT_MARKER))
					continue; // this is a comment line
				if (!nextTeam.equals("")) {
					result.add(nextTeam.trim());
				}
			}
		} catch (Exception e) {
			System.err.println("File input error: config file '" + TEAM_CONFIG_FILE + "' can not be found!");
		}
		return result;
	}
}

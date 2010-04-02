/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AstFactory.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.SourcePosition;

/**
 * MIGRATION_STATE: complete.
 *
 * Super class of different AST factories:
 * @see AstGenerator
 *
 * Static methods in this class and subclasses must calculate valid source locations.
 * Member methods may use the default locations from the factory's fields 'sourceStart', 'sourceEnd'.
 *
 * @author stephan
 * @version $Id: AstFactory.java 23416 2010-02-03 19:59:31Z stephan $
 */
public abstract class AstFactory implements ClassFileConstants, ExtraCompilerModifiers, IOTConstants {

	public int sourceStart;
	public int sourceEnd;
	public long pos;
	private SourcePosition _sourcePosition;

	/**
	 * @param start default source start location for newly created elements
	 * @param end default source end location for newly created elements
	 */
	public AstFactory(int start, int end) {
		this.sourceStart = start;
		this.sourceEnd = end;
		this.pos = (((long)start)<<32) + end;
		this._sourcePosition = new SourcePosition(this.sourceStart, this.sourceEnd, this.pos);
	}

	/** 
	 * This method shifts the generators position off by one.
	 * If adding a @SuppressWarnings annotation with this offset,
	 * non-generated code will not be affected by that annotation.
	 */
	public void shiftPosition() {
		this.sourceEnd--;
		if (this.sourceEnd<this.sourceStart)
			this.sourceStart--;
		this.pos = (((long)this.sourceStart)<<32) + this.sourceEnd;
		this._sourcePosition = new SourcePosition(this.sourceStart, this.sourceEnd, this.pos);
	}

	public void retargetFrom(ASTNode node) {
		this.sourceStart = node.sourceStart;
		this.sourceEnd = node.sourceEnd;
		this.pos = (((long)this.sourceStart)<<32) + this.sourceEnd;
		this._sourcePosition = new SourcePosition(this.sourceStart, this.sourceEnd, this.pos);
	}

    public void setMethodPositions(AbstractMethodDeclaration method) {
		method.sourceStart = this.sourceStart;
		method.sourceEnd   = this.sourceEnd;
		method.declarationSourceStart = this.sourceStart;
		method.declarationSourceEnd   = this.sourceEnd;
		method.bodyStart = this.sourceStart;
		method.bodyEnd   = this.sourceEnd;
		method.modifiersSourceStart = this.sourceStart;
	}


	public void setSourcePosition(long position) {
		this.pos = position;
		this.sourceStart = (int)position >> 32;
		this.sourceEnd   = (int)(position & 0x00000000FFFFFFFFL);
	}

	public void setSourcePosition(SourcePosition sourcePosition)
	{
		this.sourceStart = sourcePosition.sourceStart;
		this.sourceEnd   = sourcePosition.sourceEnd;
		this.pos = sourcePosition.position;
		this._sourcePosition = sourcePosition;
	}

	public SourcePosition getSourcePosition()
	{
		if(this._sourcePosition == null ||
 		   (this.sourceStart != this._sourcePosition.sourceStart) ||
		   (this.sourceEnd != this._sourcePosition.sourceEnd) ||
		   (this.pos != this._sourcePosition.position))
		{
			this._sourcePosition = new SourcePosition(this.sourceStart, this.sourceEnd, this.pos);
		}

		return this._sourcePosition;
	}
}

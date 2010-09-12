/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.smap;

public class SourcePosition
{
	public int sourceStart;
	public int sourceEnd;
	public long position;

	public SourcePosition(int sourceStart, int sourceEnd, long position)
	{
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.position = position;
	}

	public SourcePosition(int sourceStart, int sourceEnd)
	{
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.position = (((long)sourceStart)<<32) + sourceEnd;
	}
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "Position ("+this.sourceStart+","+this.sourceEnd+")";
	}
}
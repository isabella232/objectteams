/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: WorkQueue.java 19914 2009-04-18 23:34:13Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.jdt.internal.compiler.util.SimpleSet;

public class WorkQueue {

//{ObjectTeams: the mems are needed in AbstractImageBuilder - original was private;
protected SimpleSet needsCompileList;
protected SimpleSet compiledList;
//km}

public WorkQueue() {
	this.needsCompileList = new SimpleSet();
	this.compiledList = new SimpleSet();
}

public void add(SourceFile element) {
	this.needsCompileList.add(element);
}

public void addAll(SourceFile[] elements) {
	for (int i = 0, l = elements.length; i < l; i++)
		add(elements[i]);
}

public void clear() {
	this.needsCompileList.clear();
	this.compiledList.clear();
}

public void finished(SourceFile element) {
	this.needsCompileList.remove(element);
	this.compiledList.add(element);
}

public boolean isCompiled(SourceFile element) {
	return this.compiledList.includes(element);
}

public boolean isWaiting(SourceFile element) {
	return this.needsCompileList.includes(element);
}

public String toString() {
	return "WorkQueue: " + this.needsCompileList; //$NON-NLS-1$
}
}

/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
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
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.builder;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.builder.IncrementalImageBuilder;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.core.builder.SourceFile;

/**
 * Specialize the IncrementalImageBuilder to include teams of role files in 
 * the incremental compilation process.
 * 
 * @author stephan
 * @version $Id: OTJIncrementalImageBuilder.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class OTJIncrementalImageBuilder extends IncrementalImageBuilder {


	public OTJIncrementalImageBuilder(JavaBuilder javaBuilder) {
		super(javaBuilder);
	}
	
	/**
	 * For each affected source file, guess whether it could be a role
	 * file and include its team in the compilation process.
	 */
	@SuppressWarnings("unchecked") // accessing super field with raw type
	protected void addAffectedSourceFiles() {
		super.addAffectedSourceFiles();
		if (sourceFiles != null) {
			SourceFile[] oldSources = new SourceFile[sourceFiles.size()];
			sourceFiles.toArray(oldSources); // copy, because list might be extended.
			for (int i=0; i<oldSources.length; i++) {
				checkAddTeamFile(oldSources[i]);
			}
		}
	}
		
	private void checkAddTeamFile(SourceFile file) {
		IPath path = file.getPath();
		// TODO (SH): should we try more than one level?
		IPath teamPath = path.removeLastSegments(1).addFileExtension("java"); //$NON-NLS-1$
		if (!teamPath.isEmpty()) {
			SourceFile teamFile = javaBuilder.findSourceByPath(teamPath);
			if (teamFile != null) {
				if (JavaBuilder.DEBUG)
					System.out.println("Adding team file: "+teamPath); //$NON-NLS-1$
				checkAddFile(teamFile);
			}
		}
	}
	
	/** Add a file to sourceFiles if it's not already contained. */
	@SuppressWarnings("unchecked") // accessing super field with raw type
	void checkAddFile (SourceFile teamFile) {
		// could use a hashtable, but probably this list won't grow too long.
		if (!sourceFiles.contains(teamFile))
			sourceFiles.add(0, teamFile); // put to front: compile teams first.
	}
}

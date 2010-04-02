/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ReferenceToTeamLocator.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.search.matching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.PatternLocator;

/**
 * NEW for OTDT
 * <br>
 * This locator performs the final matching for {@link ReferenceToTeamPackagePattern}s.
 * 
 * @author haebor
 *
 * 14.06.2005
 */
public class ReferenceToTeamLocator extends PatternLocator
{
    ReferenceToTeamPackagePattern _pattern;
    
    /**
     * @param pattern
     */
    public ReferenceToTeamLocator(ReferenceToTeamPackagePattern pattern)
    {
        super(pattern);
        _pattern = pattern;
    }

    /** 
     * An import reference was found, is it a team package declaration
     * that matches the pattern?
     */
    @Override
    protected int matchLevel(ImportReference importRef) {
    	if (!importRef.isTeam())
    		return IMPOSSIBLE_MATCH;
    	return this._pattern.matches(importRef);
    }
    
    /**
     * A match was found, perform final check:
     * if a role was specified check the current compilation unit's first type against the role name.
     * If successful report as a type reference match regarding this first type.
     */
    @Override
    protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) 
    		throws CoreException 
    {
    	if (locator.encloses(element)) {
    		ICompilationUnit cu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
    		if (cu != null) {
    			IType[] types = cu.getTypes();
    			if (types != null && types.length > 0) {
    				// only now we have the info to check the role name:
    				if (   this._pattern._roleName != null
    					&& !new String(this._pattern._roleName).equals(types[0].getElementName()))
    					return;
    				
    				int offset = importRef.sourceStart;
					int length = importRef.sourceEnd-offset+1;
					this.match = locator.newTypeReferenceMatch(types[0], null/*binding*/, accuracy, offset, length, importRef);
    				if (this.match != null)
    					locator.report(this.match);
		    	}
    		}
    	}
    }
    
    protected int matchContainer() 
    {
    	return COMPILATION_UNIT_CONTAINER;
    }
}

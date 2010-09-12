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
 * $Id: OTSearchEngine.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.internal.core.search.matching.RoleTypePattern;
import org.eclipse.objectteams.otdt.internal.core.search.matching.TeamTypePattern;

/**
 * This class allows for searching all kinds of ObjectTeams specific elements.
 * Note that you can combine the patterns with other patterns, e.g. via 
 * SearchPattern.createAndPattern(SearchPattern left, SearchPattern right).
 *
 * @author gis
 */
public class OTSearchEngine
{
    private SearchEngine m_searchEngine = new SearchEngine();
    
    //TODO: check whether this will be synchronous or asynchronous and whether we will keep a cache like AllTypesCache
    // Note that the pattern's limitTo rule does not work in Eclipse 3.0 (see #73696)
    // Work around this by using an appropriate SearchRequestor.
    public void search(SearchPattern pattern, IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException
    {
        m_searchEngine.search(
                pattern,
                new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
                scope,
                requestor,
                monitor);
    }
    
    /**
     * Optimized search scope for OT elements (not including system libraries, which can't contain OT elements)
     * @param elements pure IJavaElements or IOTJavaElements
     */
    public static IJavaSearchScope createOTSearchScope(IJavaElement[] elements, boolean includeReferencedProjects)
    {
    	IJavaElement[] javaElements = convertOTToJavaElements(elements);
    	
		int includeMask = IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES;
		if (includeReferencedProjects) {
			includeMask |= IJavaSearchScope.REFERENCED_PROJECTS;
		}
		return SearchEngine.createJavaSearchScope(javaElements, includeMask);
    }
    
	private static IJavaElement[] convertOTToJavaElements(IJavaElement[] elements) 
	{
		IJavaElement[] javaElements = new IJavaElement[elements.length];
		System.arraycopy(elements, 0, javaElements, 0, elements.length);
		
		for (int i = 0; i < javaElements.length; i++) {
			IJavaElement element = javaElements[i];
			if (element instanceof IOTJavaElement)
				javaElements[i] = ((IOTJavaElement)element).getCorrespondingJavaElement();
		}
		return javaElements;
	}

	/**
     * Overloaded for convenience. Searches for all team type names in any package, independent of enclosing types.
     * @param limitTo IJavaSearchConstants.CLASS, INTERFACE or TYPE (searching for classes and interfaces)
     * @param matchRule SearchPattern.R_EXACT_MATCH et al.
     */
    public static SearchPattern createTeamTypePattern(
            int limitTo,
            int matchRule)
    {
        return new TeamTypePattern(null, null, null, getClassOrInterface(limitTo), matchRule);
    }

    public static SearchPattern createTeamTypePattern(
            char[] pkg, 
            char[][] enclosingTypeNames,
            char[] simpleName,
            int limitTo,
            int matchRule)
    {
        return new TeamTypePattern(pkg, enclosingTypeNames, simpleName, getClassOrInterface(limitTo), matchRule);
    }

    /**
     * Overloaded for convenience. Searches for all role type names in any package, independent of enclosing types.
     * @param limitTo IJavaSearchConstants.CLASS, INTERFACE or TYPE (searching for classes and interfaces)
     * @param matchRule SearchPattern.R_EXACT_MATCH et al.
     */
    public static SearchPattern createRoleTypePattern(
            int limitTo,
            int matchRule)
    {
        return new RoleTypePattern(null, null, null, getClassOrInterface(limitTo), matchRule);
    }

    public static SearchPattern createRoleTypePattern(
            char[] pkg, 
            char[][] enclosingTypeNames,
            char[] simpleName,
            int limitTo,
            int matchRule)
    {
        return new RoleTypePattern(pkg, enclosingTypeNames, simpleName, getClassOrInterface(limitTo), matchRule);
    }

    /**
     * Transforms the public IJavaSearchConstants CLASS, INTERFACE and TYPE into internal IIndexConstants.
     */
    protected static final char getClassOrInterface(int limitTo)
    {
        switch(limitTo)
        {
        	case IJavaSearchConstants.CLASS:
        	    return IIndexConstants.CLASS_SUFFIX;
        	case IJavaSearchConstants.INTERFACE:
        	    return IIndexConstants.INTERFACE_SUFFIX;
        	case IJavaSearchConstants.TYPE:
        		return IIndexConstants.TYPE_SUFFIX;
        }
        throw new IllegalArgumentException("Bad argument 'limitTo': " + limitTo); //$NON-NLS-1$
    }
}

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
 * $Id: ReferenceToTeamPackagePattern.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.search.matching;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchPattern;
import org.eclipse.jdt.internal.core.search.matching.PatternLocator;

/**
 * NEW for OTDT.
 * <br>
 * This pattern is used to find role files of a given team.
 * 
 * Searching uses the team package declaration of the role files.
 * These declarations are indexed as {@link IIndexConstants#REF_TO_TEAMPACKAGE}.
 * The match locator calls {@link ReferenceToTeamPackagePattern#matches(ImportReference)} for stage 1 matching, 
 * conclusive matching is performed by the {@link ReferenceToTeamLocator}.
 * <br>
 * This pattern can be used to either search all role files of a given team or only
 * one specific role given by it's name.
 * 
 * @author haebor
 *
 * 14.06.2005
 */
public class ReferenceToTeamPackagePattern extends JavaSearchPattern
{
    protected static char[][] CATEGORIES = { IIndexConstants.REF_TO_TEAMPACKAGE };
    protected char[] _teamQualifiedName;
    protected char[] _roleName;
    protected char[] _indexKey;
     
    /**
     * qualifiedTeamName/roleSimpleName
     */
    public static char[] createIndexKey(char[] teamName, char[] roleName)
    {
        char[] result = CharOperation.concat(teamName, roleName, '/');
        assert(result != null);
        return result;
    }    
    
    /**
     * Create a pattern for finding all role files of a given team.
     * @param teamName	the name of the team to start at
     * @param matchRule bitset of constants defined in {@link SearchPattern}
     */
    public ReferenceToTeamPackagePattern(char[] teamName, int matchRule)
    {
    	this(teamName, null, matchRule);
     }
    
    /**
     * Create a pattern for finding a specific role file of a given team.
     * @param teamName	the name of the team to start at
     * @param roleName  name of the role in the role file to search, 
     * 					if null is passed all role files of the given team will be found.
     * @param matchRule bitset of constants defined in {@link SearchPattern}
     */
    public ReferenceToTeamPackagePattern(char[] teamQualifiedName, char[] roleName, int matchRule)
    {
        super(IIndexConstants.REF_TO_TEAMPACKAGE_PATTERN, matchRule | R_EXACT_MATCH | R_CASE_SENSITIVE);
        this.mustResolve = false;
        
        if (teamQualifiedName == null)
            throw new NullPointerException("teamQualifiedName must not be null"); //$NON-NLS-1$

        _teamQualifiedName = teamQualifiedName; 
    	_roleName = roleName;
    	
    	createIndexKey();
     }
    
    /**
     * @param patternKind
     * @param matchRule
     */
    protected ReferenceToTeamPackagePattern(int patternKind, int matchRule)
    {
        super(IIndexConstants.REF_TO_TEAMPACKAGE_PATTERN, matchRule & R_EXACT_MATCH | R_CASE_SENSITIVE);
        this.mustResolve = false;
    }

    public SearchPattern getBlankPattern() 
    {
    	return new ReferenceToTeamPackagePattern(IIndexConstants.REF_TO_TEAMPACKAGE_PATTERN, R_EXACT_MATCH | R_CASE_SENSITIVE);
    }

    public char[][] getIndexCategories()
    {
    	return CATEGORIES;
    }
    
	public boolean matchesDecodedKey(SearchPattern decodedPattern)
	{
		ReferenceToTeamPackagePattern pattern = (ReferenceToTeamPackagePattern) decodedPattern;
		boolean teamMatches = matchesName(_teamQualifiedName, pattern._teamQualifiedName);
		if (!teamMatches)
		    return false;
		
	    return matchesName(_roleName, pattern._roleName);
	}
	    
    public int matches(ImportReference importRef) {
		// note: _roleName is not compared at this stage, deferred to handling by the ReferenceToTeamLocator
		if (matchesName(this._teamQualifiedName, CharOperation.concatWith(importRef.tokens, '.')))
			return PatternLocator.ACCURATE_MATCH;
		return PatternLocator.IMPOSSIBLE_MATCH;
	}

	private void createIndexKey()
    {
	    _indexKey = createIndexKey(_teamQualifiedName, _roleName);
    }

    // contrary to what our SearchPattern.getIndexKey() says, we can't return null here (leads to NPE).
    // so we need to return either the team index or the full team+role index, depending on the search
    // criteria. To make this work, we need to have both index entries, as well! (carp)
    public char[] getIndexKey()
    {
        // If this assertion ever fails, then decodeIndexKey() probably needs to call createIndexKey().
        assert(_indexKey != null);
        
        return _indexKey;
    }
    
    public void decodeIndexKey(char[] key)
    {
        int slash = CharOperation.indexOf('/', key);
        _teamQualifiedName = CharOperation.subarray(key, 0, slash);
        if (slash > 0 && slash < key.length - 2)
        {
            _roleName = CharOperation.subarray(key, slash + 1, -1);
        }
//        createIndexKey(); // should not be necessary
    }
}

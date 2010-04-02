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
 * $Id: RoleTypePattern.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.search.matching;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;

/**
 * NEW for OTDT
 * 
 * A SearchPattern for locating Role types.
 * @author gis
 */
public class RoleTypePattern extends TypeDeclarationPattern
{
    protected static char[][] ROLE_CATEGORIES = new char[][] { IIndexConstants.ROLE_DECL };
    
    protected int _modifiers = 0;
    
    /**
     * @param classOrInterface TYPE_SUFFIX, CLASS_SUFFIX or INTERFACE_SUFFIX
     * @param matchRule SearchPattern.R_EXACT_MATCH et al.
     */
    public RoleTypePattern(
            char[] pkg, 
            char[][] enclosingTypeNames, 
            char[] simpleName, 
            char classOrInterface, 
            int matchRule)
    {
        super(pkg, enclosingTypeNames, simpleName, classOrInterface, matchRule);
        this.kind = ROLE_DECL_PATTERN;
    }
    
    public SearchPattern getBlankPattern() 
    {
    	return new RoleTypePattern(null, null, null, TYPE_SUFFIX, R_EXACT_MATCH | R_CASE_SENSITIVE);
    }
    
// Reimplement those when we need special index handling apart from the category
    public void decodeIndexKey(char[] key)
    {
        _modifiers = 0;

    	int slash = CharOperation.lastIndexOf(SEPARATOR, key, 0);
    	if (slash > 0)
    	{
        	String mods = String.valueOf(CharOperation.subarray(key, slash +1, key.length));
        	try {
        	    _modifiers = Integer.parseInt(mods); 
        	}
        	catch(NumberFormatException ex)
        	{
        	    // ignore
        	    //ex.printStackTrace(); // just for testing
        	}
    	}

        super.decodeIndexKey(CharOperation.subarray(key, 0, slash));
    }
    
//    public char[] getIndexKey()
//    {
//        return super.getIndexKey();
//    }
    
    public boolean matchesDecodedKey(SearchPattern decodedPattern)
    {
        RoleTypePattern pattern = (RoleTypePattern) decodedPattern;
        return Flags.isRole(pattern._modifiers) && super.matchesDecodedKey(decodedPattern);
    }
    
    public char[][] getIndexCategories()
    {
        return ROLE_CATEGORIES;
        //return CATEGORIES; 
    }
}

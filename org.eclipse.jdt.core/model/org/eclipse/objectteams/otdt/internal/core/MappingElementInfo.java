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
 * $Id: MappingElementInfo.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.objectteams.otdt.core.util.FieldData;
import org.eclipse.objectteams.otdt.core.util.MethodData;


/**
 * Data structure holding mapping related informations provided by the
 * SourceElementParser and CompilationUnitStructureRequestor.
 * 
 * @author kaiser
 * @version $Id: MappingElementInfo.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class MappingElementInfo
{
	private MethodData   _roleMethod;
	private MethodData[] _baseMethods;
    private FieldData    _baseField;
    private int          _sourceStart;
    private int          _sourceEnd;
	private int          _declarationSourceStart;
	private int          _declarationSourceEnd;
	private char[]       _callinName;
	private int          _callinKind;
	private boolean      _hasSignature;
	private boolean      _isOverride; // callout override.
	private int			 _declaredModifiers;
	char[][] 			 _categories;
	public Annotation[] annotations;
	
    
    public MethodData getRoleMethod()
    {
        return _roleMethod;
    }
    
    public MethodData[] getBaseMethods()
    {
        return _baseMethods;
    }
    
    public FieldData getBaseField()
    {
    	return _baseField;
    }

    public int getDeclarationSourceStart()
    {
        return _declarationSourceStart;
    }

    public int getSourceStart() 
    {
		return _sourceStart;
	}

    public int getSourceEnd() 
    {
		return _sourceEnd;
	}
    
    public int getDeclarationSourceEnd()
    {
    	return _declarationSourceEnd;
    }
    
    public char[] getCallinName() 
    {
    	return _callinName;
    }

    public int getCallinKind()
    {
        return _callinKind;
    }

    public char[][] getCategories()
    {
    	return _categories;
    }

    public void setRoleMethod(MethodData data)
    {
        _roleMethod = data;
    }
    
    public void setBaseMethods(MethodData[] data)
    {
        _baseMethods = data;
    }

    public void setBaseField(FieldData data)
    {
    	_baseField = data;
    }
    
    public void setDeclarationStart(int start)
    {
        _declarationSourceStart = start;
    }
    
	public void setSourceStart(int start) 
	{
		_sourceStart = start;
	}
    
	public void setSourceEnd(int end) 
	{
		_sourceEnd = end;
	}
    
    public void setDeclarationSourceEnd(int end)
    {
        _declarationSourceEnd = end;
    }

    public void setCallinName(char[] callinName) 
    {
    	_callinName = callinName;
    }
    
    public void setCallinKind(int terminalTokenCallinKind)
    {
    	_callinKind = CallinMapping.convertTerminalTokenToModel(terminalTokenCallinKind);
    }

    public boolean hasSignature()
    {
        return _hasSignature;
    }
    
    public void setHasSignature(boolean signature)
    {
        _hasSignature = signature;
    }
    
    public void setCategories(char[][] categories)
    {
    	_categories = categories;
    }

	public boolean isOverride() {
		return _isOverride;
	}
	
	public void setOverride(boolean flag) {
		this._isOverride = flag;
	}

	public int getDeclaredModifiers() {
		return this._declaredModifiers;
	}

	public void setDeclaredModifiers(int declaredModifiers) {
		this._declaredModifiers = declaredModifiers;
	}
}

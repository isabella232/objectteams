/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TypeDeclarationFinder.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * TODO(mkr) see JavaModelDOMASTTranslator.DOMASTTypeFinder
 * @author Michael Krueger
 * @version $Id: TypeDeclarationFinder.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class TypeDeclarationFinder extends ASTVisitor
{
    private TypeDeclaration _typeDecl;
    private String[] _path;
    private int _pathIdx;
    
    public TypeDeclarationFinder()
    {
        _typeDecl = null;
        _path = null;
        _pathIdx = 0;
    }
     
    private boolean checkTypeDeclaration(TypeDeclaration node)
    {
        if ( (_path == null) || (_pathIdx >= _path.length))
        {
            return false;
        }
        
        if (!node.getName().getIdentifier().equals(_path[_pathIdx]))
        {
            return false; // wrong path
        }
        
        _pathIdx++;
        if (_pathIdx == _path.length)
        {
            _typeDecl = node;
            return false;
        }
        
        return true;
    }
    
    
    public boolean visit(RoleTypeDeclaration node) 
    {
        return checkTypeDeclaration(node);
    }
        
    public boolean visit(TypeDeclaration node)
    {
        return checkTypeDeclaration(node);
    }
        
    public TypeDeclaration getTypeDeclaration()
    {
        return _typeDecl;
    }
        
    public void setName(String name)
    {
        if (name != null)
        {
            _path = name.split("\\.");
            _pathIdx = 0;
            _typeDecl = null;
        }
    }
}

/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BaseCallFinder.java 23473 2010-02-05 19:46:08Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.refactoring.corext.rename;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BaseCallMessageSend;

/**
 * @author svacina
 * @version $Id: BaseCallFinder.java 23473 2010-02-05 19:46:08Z stephan $
 * DOM AST Visitor for searching base calls in AST nodes
 */
public class BaseCallFinder extends ASTVisitor
{
    private ArrayList<BaseCallMessageSend> _result = null;
    public BaseCallFinder()
    {
        _result = new ArrayList<BaseCallMessageSend>();
    }
    public boolean visit(BaseCallMessageSend node)
    {
        _result.add(node);
        return false;
    }
    
    public BaseCallMessageSend[] getResult()
    {
        return _result.toArray(new BaseCallMessageSend[_result.size()]);
    }
}

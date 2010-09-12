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
 * $Id: IOTTypeHierarchy.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author gis
 * Public interface for OTTypeHierarchy.
 * TODO: Add all those methods that need to be public API
 */
public interface IOTTypeHierarchy extends ITypeHierarchy
{
    /**
     * Switches phantom mode on or off.
     * The default mode is off.
     * In phantom mode PhantomTypes may be returned by getXXX() methods.
     * In non phantom mode all PhantomTypes are filtered or replaced.
     * @param flag true - sets phantom mode, false - sets non phantom mode 
     */
    public void setPhantomMode(boolean flag);
    
    /**
     * Returns wether or not this hierarchy is in phantom mode.
     * @return phantom mode
     */
    public boolean isPhantomMode();
    
    /**
     * Returns the focus type for which the hierarchy was created.
     * @return focus type
     */
    public IType getFocusType();
    
    public IOTTypeHierarchy getOTSuperTypeHierarchy(IType type) throws JavaModelException;

    /**
     * Returns the direct resolved subtypes of the given type,
     * in no particular order, limited to the types in this
     * type hierarchy's graph.
     * If the type is a class, this returns the resolved subclasses.
     * If the type is an interface, this returns both the classes which implement 
     * the interface and the interfaces which extend it.
     * If the type is a role, implicit sub roles are included.
     * In phantom mode, sub roles may be PhantomTypes.
     * 
     * @param type the given type
     * @return the direct resolved subtypes of the given type limited to the
     * types in this type hierarchy's graph
     */
    public IType[] getSubtypes(IType type);

    /**
     * @see ITypeHierarchy
     */
    public IType[] getTSuperTypes(IType type);

    /**
     * Returns all implicit super types (tsupers) of the given type.
     * This recursively collects all implicit super roles of the given role,
     * but ignores the explicit (extends/implements) ones.
     */
    public IType[] getAllTSuperTypes(IType type);

    
    /**
     * Returns all implicit subtypes (direct and indirect)
     * of the given type, in no particular order, limited to the
     * types in this type hierarchy's graph. An empty array
     * is returned if there are no resolved subtypes for the
     * given type.
     * 
     * @param type the given type
     * @return all tsubtypes (direct and indirect) of the given type
     */
    public IType[] getAllTSubtypes(IType type);
    
    /**
     * Returns the type's explicit superclass.
     * For roles the explicit superclass relationship
     * is either declared by the role itself,
     * or by one of it's implicit superroles.
     * 
     * In the latter case, only the 'nearest'
     * implicit supertype may contributes it's
     * explicit superclass.
     * 
     * @return explicit superclass or null
     */
    public IType getExplicitSuperclass(IType type);

    public IType[] getExtendingInterfaces(IType type);

    public IType[] getImplementingClasses(IType type);
}
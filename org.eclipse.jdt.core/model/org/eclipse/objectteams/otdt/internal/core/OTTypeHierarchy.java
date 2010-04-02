/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTTypeHierarchy.java 23401 2010-02-02 23:56:05Z stephan $
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.TypeVector;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IOTTypeHierarchy;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.PhantomType;
import org.eclipse.objectteams.otdt.core.TypeHelper;


/**
 * OT_COPY_PASTE: copied private methods:
 *                TypeHierarchy.isInterface(IType)
 *                TypeHierarchy.addAllCheckingDuplicates(ArrayList, IType[])
 *                
 * This is the OT/J type hierarchy.
 * It extends the JDT TypeHierarchy with the implicit inheritance of roles.
 * In addition to explicit inheritance (extends, implements), a role may have
 * implicit supertypes (tsuper types) and hence implicit subtypes.
 * Roles don't have to be redeclared in subteams, because of OT/J copy
 * inheritance. In this case, they are missing in the JavaModel/OTModel,
 * but are represented in the hierarchy as PhantomTypes.
 * PhantomTypes are placeholders of copied but not redeclared models.
 * After creation, the hierarchy may operates in two modes: Default mode and
 * phantom mode. In default mode all returned PhantomTypes are either filtered
 * or replaced with their real type. In phantom mode, PhantomTypes may be
 * returned, e.g. for type hierarchy view.
 * 
 * - All public methods and constructors accept OTModel types as arguments,
 *   though only their corresponding java model type is used.
 * - No OTModel types are returned, ever.
 * 
 * @author Michael Krueger (mkr)
 * @version $Id: OTTypeHierarchy.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class OTTypeHierarchy extends TypeHierarchy
                             implements IOTTypeHierarchy
{    
    //kinds
    private static int EXPLICIT = 0x0001;
    private static int IMPLICIT = 0x0002;
   
    /**
     * Roles and their implicit super/subtypes (IType->TypeVector of ITypes).
     */
    protected Map<IType, TypeVector> _typeToTSuperTypes;    
    protected Map<IType, TypeVector> _typeToTSubTypes;    
    
    protected Map<IType, CopyInheritanceInfo> _copyInheritanceInfos;
    protected OTTypeHierarchyCache _hierarchyCache;
    protected boolean _phantomMode = false;
    
    protected IType JAVA_LANG_OBJECT;
    protected IType ORG_OBJECTTEAMS_TEAM;    
    
    /**
     * Creates an OTTypeHierarchy for a given focus type.
     * @param type focus type
     * @param project java project
     * @param computeSubtypes boolean flag for subtype hierarchy
     */
    public OTTypeHierarchy(IType type,
                           IJavaProject project,
                           boolean computeSubtypes)
    {
        this(OTTypeHierarchyHelper.getJavaModelIType(type),
             project,
             computeSubtypes,
             new OTTypeHierarchyCache());
    }
    
    protected OTTypeHierarchy(IType type,
                           IJavaProject project,
                           boolean computeSubtypes,
                           OTTypeHierarchyCache cache)
    {        
        super(OTTypeHierarchyHelper.getJavaModelIType(type),
              new ICompilationUnit[0],
              project,
              computeSubtypes);
        
        init(cache);
    }
    
    public OTTypeHierarchy(IType type, boolean computeSubtypes) {
    	super(type, 
    		  JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true/*addPrimary*/), 
    		  SearchEngine.createWorkspaceScope(), 
    		  computeSubtypes);
    	init(new OTTypeHierarchyCache());
    }
    
	private void init(OTTypeHierarchyCache cache) {
		this._hierarchyCache = cache;
        
        try {
            this.JAVA_LANG_OBJECT = this.focusType.getJavaProject().findType(TypeHelper.JAVA_LANG_OBJECT);
            this.ORG_OBJECTTEAMS_TEAM = this.focusType.getJavaProject().findType(TypeHelper.ORG_OBJECTTEAMS_TEAM);
        } 
        catch (JavaModelException ex) {
            assert(false);
        }
	}

    // -----------------------------------------------------------------------
    // getAllClasses/Interfaces/Types
    // -----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getAllClasses()
     */
    public IType[] getAllClasses()
    {        
        // Ignore role classes in the _typeToTSuperTypes keyset,
        // because each role is in the classToSuperClass keyset already.
        return filterPhantomTypes(super.getAllClasses());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getAllInterfaces()
     */
    public IType[] getAllInterfaces()
    {
        // no filtering, since phantom types aren't added to interfaces
        return super.getAllInterfaces();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getAllTypes()
     */
    public IType[] getAllTypes()
    {    
        return filterPhantomTypes(super.getAllTypes());
    }
    
    // -----------------------------------------------------------------------
    // getAllSubtypes
    // -----------------------------------------------------------------------

    /**
     * Returns all explicit and implicit subtypes (direct and indirect)
     * of the given type, in no particular order, limited to the
     * types in this type hierarchy's graph. An empty array
     * is returned if there are no resolved subtypes for the
     * given type.
     * 
     * @param type the given type
     * @return all subtypes (direct and indirect) of the given type
     */
    public IType[] getAllSubtypes(IType type)
    {
        return filterDuplicates(getAllSubtypesForType(OTTypeHierarchyHelper.getJavaModelIType(type), IMPLICIT | EXPLICIT)); 
    }
       
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
    public IType[] getAllTSubtypes(IType type)
    {
        return filterDuplicates(getAllSubtypesForType(OTTypeHierarchyHelper.getJavaModelIType(type), IMPLICIT)); 
    }

    private IType[] getAllSubtypesForType(IType type, int kind)
    {
        ArrayList<IType> subs = new ArrayList<IType>();
        getAllSubtypesForType0(type, subs, kind);
        
        return subs.toArray(new IType[subs.size()]);
    }

    private void getAllSubtypesForType0(IType type, ArrayList<IType> subs, int kind)
    {
        IType[] subtypes;
        if ((kind & IMPLICIT) != 0) {
	        subtypes = getSubtypesForType(type, IMPLICIT);        
	        for (int idx = 0; idx < subtypes.length; idx++)
	        {
	        	// along the tsub-path no phantom types are added in non-phantom mode
	            if (!(isDefaultMode() && subtypes[idx] instanceof PhantomType ))
	                subs.add(subtypes[idx]);
	            
	            getAllSubtypesForType0(subtypes[idx], subs, kind);
	        }
        }
        if ((kind & EXPLICIT) != 0) {
		    subtypes = getSubtypesForType(type, EXPLICIT);
		    for (int idx = 0; idx < subtypes.length; idx++)
		    {
		        if ( isDefaultMode() && subtypes[idx] instanceof PhantomType )
		            // add real role of phantom type to represent inherited explicit sub role
		            subs.add(((PhantomType)subtypes[idx]).getRealType());                                                
		        else
		            subs.add(subtypes[idx]);
		        
		        getAllSubtypesForType0(subtypes[idx], subs, kind);
		    }
        }
    }
    
    // -----------------------------------------------------------------------
    // getAllSuperclasses
    // -----------------------------------------------------------------------
        
    /**
     * Returns all explicit and implicit superclasses (direct and indirect)
     * of the given class. An empty array is returned if there are no
     * superclasses for the given class.
     * 
     * @param type the given type
     * @return all superclasses of the given class, an empty array if none.
     */
    public IType[] getAllSuperclasses(IType type)
    {
        if (isInterface(type))
        {
            return NO_TYPE;
        }

        return filterDuplicates(filterPhantomTypes(getAllSuperclassesForType(OTTypeHierarchyHelper.getJavaModelIType(type))));
    }
    
    private IType[] getAllSuperclassesForType(IType type)
    {
        TypeVector supers = new TypeVector();
        getAllSuperclasses0(type, supers);
        
        return supers.elements();
    }
    
    private void getAllSuperclasses0(IType type, TypeVector supers)
    {
        IType[] superclasses = getSuperclasses0(type);
        supers.addAll(superclasses);
        for (int idx = 0; idx < superclasses.length; idx++)
        {
            getAllSuperclasses0(superclasses[idx], supers);
        }
    }
    
    // -----------------------------------------------------------------------
    // getAllSuperInterfaces
    // -----------------------------------------------------------------------
    
    /**
     * Returns all superinterfaces (direct and indirect) of the given type.
     * If the given type is a class, this includes all superinterfaces of all
     * superclasses.
     * An empty array is returned if there are no superinterfaces for the
     * given type.
     *
     * @param type the given type
     * @return all superinterfaces of the given type, an empty array if none
     */
    public IType[] getAllSuperInterfaces(IType type)
    {
        return filterPhantomTypes(getAllSuperInterfacesForType(OTTypeHierarchyHelper.getJavaModelIType(type)));
    }
    
    private IType[] getAllSuperInterfacesForType(IType type)
    {
        ArrayList<IType> supers = new ArrayList<IType>();
        getAllSuperInterfaces0(type, supers);
        
        return supers.toArray(new IType[supers.size()]);
    }
    
    private void getAllSuperInterfaces0(IType type, ArrayList<IType> supers)
    {
        IType[] superinterfaces = (IType[])this.typeToSuperInterfaces.get(type);
        if (superinterfaces != null && superinterfaces.length != 0) {
            addAllCheckingDuplicates(supers, superinterfaces);
            for (int i = 0; i < superinterfaces.length; i++) {
                getAllSuperInterfaces0(superinterfaces[i], supers);
            }
        }

        IType superclass =(IType) this.classToSuperclass.get(type);
        if (superclass != null) {
            getAllSuperInterfaces0(superclass, supers);
        }       

        // role interface? Add ALL implicit super types (interfaces)
        if (isInterface(type))
        {
           addAllCheckingDuplicates(supers, getAllTSuperTypes0(type));
        }
    }
        
    // -----------------------------------------------------------------------
    // getAllSupertypes
    // -----------------------------------------------------------------------
    
    /**
     * Returns all supertypes (direct and indirect) of the given type.
     * An empty array is returned if there are supertypes for the given type.
     * 
     * @param type the given type
     * @return all supertypes of the given class, an empty array if none
     */
    public IType[] getAllSupertypes(IType type)
    {
        return filterDuplicates(filterPhantomTypes(getAllSupertypes0(OTTypeHierarchyHelper.getJavaModelIType(type))));
    }
    
    private IType[] getAllSupertypes0(IType type)
    {
        TypeVector supers = new TypeVector();
        IType[] supertypes = getSupertypes0(type);
        supers.addAll(supertypes);
        for (int idx = 0; idx < supertypes.length; idx++)
        {
            supers.addAll(getAllSupertypes0(supertypes[idx]));
        }
        
        return supers.elements();
    }
    
    // -----------------------------------------------------------------------
    // getAllTSuperTypes()
    // -----------------------------------------------------------------------
    
    /**
     * Returns all implicit super types (tsupers).
     * @param type Role
     * @return Array with all tsupers or empty array.
     */
    public IType[] getAllTSuperTypes(IType type)
    {
        return filterDuplicates(filterPhantomTypes(getAllTSuperTypes0(OTTypeHierarchyHelper.getJavaModelIType(type))));
    }
    
    private IType[] getAllTSuperTypes0(IType type)
    {
        TypeVector result = new TypeVector();
        IType[] tsupers = getTSuperTypes0(type);
        result.addAll(tsupers);
        for (int idx = 0; idx < tsupers.length; idx++)
        {
            IType tsup = tsupers[idx];            
            result.addAll(getAllTSuperTypes0(tsup));
        }
        
        return result.elements();
    }

    // -----------------------------------------------------------------------
    // getSubclasses
    // -----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getSubclasses(org.eclipse.jdt.core.IType)
     */
    public IType[] getSubclasses(IType type)
    {
        if (this.isInterface(type))
        {
            return NO_TYPE;
        }
        else
        {
            return getSubtypes(OTTypeHierarchyHelper.getJavaModelIType(type));
        }
    }    

    // -----------------------------------------------------------------------
    // getSubtypes
    // -----------------------------------------------------------------------

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

    public IType[] getSubtypes(IType type)
    {
        if (isPhantomMode())
        {
            return getSubtypesForType(OTTypeHierarchyHelper.getJavaModelIType(type), EXPLICIT | IMPLICIT);
        }
        else
        {
            ArrayList<IType> realSubtypes = new ArrayList<IType>();
            getRealSubtypesForType(OTTypeHierarchyHelper.getJavaModelIType(type), realSubtypes, EXPLICIT | IMPLICIT);
            return filterDuplicates(realSubtypes.toArray(new IType[realSubtypes.size()]));
        }
    }
    
    /**
     * Returns the direct subtypes of the given type.
     * There are two kinds of subtypes:
     * EXPLICIT extending classes, interfaces or implementing classes.
     * IMPLICIT sub roles (may include PhantomTypes).
     * @param type Type
     * @param kind IMPLICIT, EXPLICIT
     * @return subtypes of the specified kind.
     */
    private IType[] getSubtypesForType(IType type, int kind)
    {
        TypeVector result = new TypeVector();
        if ((kind & EXPLICIT) != 0)
        {
            result.addAll(super.getSubtypes(type));
        }
        if ((kind & IMPLICIT) != 0)
        {
            result.addAll(getTSubtypesForType(type));
        }
        
        return result.elements();
    }

    /**
     * Returns the direct implicit subroles of a type in no particular order,
     * limited to the types in this type hierarchy's graph.
     * @param type role
     * @return a non-null array of implicit subroles
     */
    private IType[] getTSubtypesForType(IType type)
    {
        TypeVector vector = this._typeToTSubTypes.get(type);
        if (vector == null)
            return NO_TYPE;
        else 
            return vector.elements();
    }
        
    private void getRealSubtypesForType(IType type, ArrayList<IType> realSubtypes, int kind)
    {
        IType[] subtypes = getSubtypesForType(type, kind);
        for (int idx = 0; idx < subtypes.length; idx++)
        {
            IType subtype = subtypes[idx];
            if (subtype instanceof PhantomType)
            {
                
                // type is explicit supertype of phantom type
                if (type.equals(getExplicitSuperclass(subtype)))
                {
                    if ( !(realSubtypes.contains(subtype)) )
                    {
                        // add the last real imlpicit supertype
                        // as mentioned in the comments of TPX-334
                        realSubtypes.add(((PhantomType)subtype).getRealType());                        
                    }
                    // for phantom use implicit subtype of the phantomtype
                    getRealSubtypesForType(subtypes[idx], realSubtypes, IMPLICIT);                    
                }
                // type is implicit supertype of phantom type
                else
                {
                    // for phantom use explicit or implicit subtypes. 
                    getRealSubtypesForType(subtypes[idx], realSubtypes, EXPLICIT | IMPLICIT);
                }                
            }
            else
            {
                if ( !(realSubtypes.contains(subtypes[idx])) )
                {
                    realSubtypes.add(subtypes[idx]);                    
                }                
            }
        }
    }
    
    // -----------------------------------------------------------------------
    // getExplicitSuperclass
    // -----------------------------------------------------------------------
    
    /**
     * Returns the explicit superclass (extends) of the given type.
     * Non-phantom mode: returns real type of phantom type
     * @param type
     * @return explicit superclass or null (for interfaces)
     */
    public IType getExplicitSuperclass(IType type)
    {
        return replacePhantomType(getExplicitSuperclass0(OTTypeHierarchyHelper.getJavaModelIType(type)));
    }
    
    private IType getExplicitSuperclass0(IType currentType)
    {
        return super.getSuperclass(currentType);
    }
        
    // -----------------------------------------------------------------------
    // getTSuperTypes
    // -----------------------------------------------------------------------

    /**
     * Returns the direct implicit super types (tsuper) of the given type.
     */
    public IType[] getTSuperTypes(IType type)
    {
        return filterDuplicates(replacePhantomType(getTSuperTypes0(OTTypeHierarchyHelper.getJavaModelIType(type))));
    }

    private IType[] getTSuperTypes0(IType type)
    {
        TypeVector result = this._typeToTSuperTypes.get(type);
        return (result != null) ? result.elements() : NO_TYPE;
    }

    // -----------------------------------------------------------------------
    // getSuperclass
    // -----------------------------------------------------------------------

    /**
     * Returns the superclass of the given class, 
     * or <code>null</code> if the given class has no superclass,
     * or if the given type is an interface.
     * If the given type is a role, an UnsupportedOperationException
     * is thrown because, roles have no single supertype.
     * Use getExplicitSuperclass() instead.
     * 
     * @param type the given type
     * @return the superclass of the given class,
     * or <code>null</code> if the given class has no superclass.
     * @throws UnsupportedOperationException if the type is a role
     * 
     */
    public IType getSuperclass(IType type)
    {
        IOTType otType = OTModelManager.getOTElement(type);
        if(otType != null && otType.isRole())
        {
            throw new UnsupportedOperationException("OT types have no single supertype"); //$NON-NLS-1$
        }
        
        return super.getSuperclass(OTTypeHierarchyHelper.getJavaModelIType(type));
    }

    // -----------------------------------------------------------------------
    // getSuperclasses
    // -----------------------------------------------------------------------

    /**
     * Returns the superclasses for the given type.
     * For a role this means, its explicit superclass,
     * and its implicit (tsuper) superclasses.
     * @param type Type
     * @return Superclasses (explicit, tsupers)
     */
    public IType[] getSuperclasses(IType type)
    {
        if (isInterface(type))
        {
            return NO_TYPE;
        }
        else
        {
            return filterDuplicates(replacePhantomType(getSuperclasses0(OTTypeHierarchyHelper.getJavaModelIType(type))));
        }
    }
    
    private IType[] getSuperclasses0(IType type)
    {
        IType[] result = getTSuperTypes0(type);
        IType explicitSuperclass = getExplicitSuperclass0(type);
        if (explicitSuperclass != null)
        {
            result = growAndAddToArray(result, explicitSuperclass);
        }
                
        return result;
    }

    // -----------------------------------------------------------------------
    // getSuperInterfaces
    // -----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getSuperInterfaces(org.eclipse.jdt.core.IType)
     */
    public IType[] getSuperInterfaces(IType type)
    {
        return replacePhantomType(getSuperInterfaces0(OTTypeHierarchyHelper.getJavaModelIType(type)));
    }
    
    private IType[] getSuperInterfaces0(IType type)
    {
        IType[] result = super.getSuperInterfaces(type);

        // role interfaces may have implicit super interfaces.
        if (isInterface(type))
        {
            result = growAndAddToArray(result, getTSuperTypes0(type));
        }

        return result;
    }

    // -----------------------------------------------------------------------
    // getSupertypes
    // -----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getSupertypes(org.eclipse.jdt.core.IType)
     */
    public IType[] getSupertypes(IType type)
    {
        return filterDuplicates(replacePhantomType(getSupertypes0(OTTypeHierarchyHelper.getJavaModelIType(type))));
    }

    private IType[] getSupertypes0(IType type)
    {
        IType[] result = getSuperclasses0(type);
        result = growAndAddToArray(result, getSuperInterfaces0(type));
        return result;
    }
    
    // -----------------------------------------------------------------------
    // getExtendingInterfaces/getImplementingClasses
    // -----------------------------------------------------------------------    
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getExtendingInterfaces(org.eclipse.jdt.core.IType)
     */
    public IType[] getExtendingInterfaces(IType type)
    {
        return filterPhantomTypes(super.getExtendingInterfaces(OTTypeHierarchyHelper.getJavaModelIType(type)));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getImplementingClasses(org.eclipse.jdt.core.IType)
     */
    public IType[] getImplementingClasses(IType type)
    {
        return filterPhantomTypes(super.getImplementingClasses(OTTypeHierarchyHelper.getJavaModelIType(type)));
    }
    
    // -----------------------------------------------------------------------
    // misc methods
    // -----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#contains(org.eclipse.jdt.core.IType)
     */
    public boolean contains(IType type)
    {
        if (super.contains(OTTypeHierarchyHelper.getJavaModelIType(type)))
        {
            return true;
        }
        else
        {
            return this._typeToTSuperTypes.containsKey(OTTypeHierarchyHelper.getJavaModelIType(type));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getCachedFlags(org.eclipse.jdt.core.IType)
     */
    public int getCachedFlags(IType type)
    {
        int flags = super.getCachedFlags(type);
        if (flags == -1)
        {
            try
            {
                flags = type.getFlags();
            }
            catch (JavaModelException e)
            {
                return Flags.AccDefault;
            }
        }
        
        return flags;            
    }
    

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getRootClasses()
     */
    public IType[] getRootClasses()
    {
        return super.getRootClasses();        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ITypeHierarchy#getRootInterfaces()
     */
    public IType[] getRootInterfaces()
    {
        return super.getRootInterfaces();
    }


    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy#initialize(int)
     */
    protected void initialize(int size)
    {
        super.initialize(size);
        
        this._typeToTSuperTypes = new HashMap<IType, TypeVector>(size);
        this._typeToTSubTypes = new HashMap<IType, TypeVector>(size);
        this._copyInheritanceInfos = new HashMap<IType, CopyInheritanceInfo>(size);
    }

    /*
	 * make this method visible to the OT classes
 	 * (non-Javadoc)
     * @see org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy#cacheSuperclass(org.eclipse.jdt.core.IType, org.eclipse.jdt.core.IType)
     */
    protected void cacheSuperclass(IType type, IType superclass)
    {
        super.cacheSuperclass(type, superclass);
    }
    
    /* 
     * (non-Javadoc)
	 * make this method visible to the OT classes
     * @see org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy#cacheSuperInterfaces(org.eclipse.jdt.core.IType, org.eclipse.jdt.core.IType[])
     */
    protected void cacheSuperInterfaces(IType type, IType[] superinterfaces)
    {
        super.cacheSuperInterfaces(type, superinterfaces);
    }

    /**
     * Returns if this hierarchy was created with subtypes of focus type.
     * @return true, if computed with subtypes, else false.
     */
    public boolean hasSubtypes()
    {
        return this.computeSubtypes;
    }    
    
    /**
     * Switches phantom mode on or off.
     * The default mode is off.
     * In phantom mode PhantomTypes may be returned by getXXX() methods.
     * In non phantom mode all PhantomTypes are filtered or replaced.
     * @param flag true - sets phantom mode, false - sets non phantom mode 
     */
    public void setPhantomMode(boolean flag)
    {
        this._phantomMode = flag;
    }
    
    /**
     * Returns wether or not this hierarchy is in phantom mode.
     * @return phantom mode
     */
    public boolean isPhantomMode()
    {
        return this._phantomMode;
    }

    /**
     * Returns wether or not this hierarchy is in non-phantom mode.
     * @return phantom mode
     */
    public boolean isDefaultMode()
    {
        return !this._phantomMode;
    }

    public IType getFocusType()
    {
        return this.focusType;
    }
    
    protected void addTSupertype(IType type, IType tsuperType)
    {
        TypeVector tsuperTypes = this._typeToTSuperTypes.get(type);
        if (tsuperTypes == null)
        {
            tsuperTypes = new TypeVector();
            this._typeToTSuperTypes.put(type, tsuperTypes);
        }
        if (!tsuperTypes.contains(tsuperType))
        {
            tsuperTypes.add(tsuperType);
            addTSubtype(tsuperType, type);
        }
    }
    
    protected void addTSubtype(IType type, IType subtype)
    {
        TypeVector tsubTypes = this._typeToTSubTypes.get(type);
        if (tsubTypes == null)
        {
            tsubTypes = new TypeVector();
            this._typeToTSubTypes.put(type, tsubTypes);
        }
        if (!tsubTypes.contains(subtype))
        {
            tsubTypes.add(subtype);
        }
    }

    @Override
    protected void addSubtype(IType type, IType subtype) {
    	super.addSubtype(type, subtype);
    	if (type instanceof PhantomType)
    		// if querying hierarchy w/o phantoms we must shortcut this phantom:
    		addSubtype(((PhantomType)type).getRealType(), subtype);
    }

    protected void addRootClass(IType type)
    {
        super.addRootClass(type);
    }
    
    protected CopyInheritanceInfo getCopyInheritanceInfo(IType teamType)
    {
        return this._copyInheritanceInfos.get(teamType);
    }

    protected void setCopyInheritanceInfo(IType teamType, CopyInheritanceInfo info)
    {
        this._copyInheritanceInfos.put(teamType, info);
    }

    @SuppressWarnings("unchecked") // referencing raw type fields from our super class
	void integrate(OTTypeHierarchy hierarchy)
    {
        this.classToSuperclass.putAll(hierarchy.classToSuperclass);
        this.typeToSuperInterfaces.putAll(hierarchy.typeToSuperInterfaces);
        this._typeToTSuperTypes.putAll(hierarchy._typeToTSuperTypes);

        OTTypeHierarchyHelper.addAllCheckingDuplicates(
                    this.typeToSubtypes,
                    hierarchy.typeToSubtypes);
        OTTypeHierarchyHelper.addAllCheckingDuplicates(
                    this._typeToTSubTypes,
                    hierarchy._typeToTSubTypes);

        this.typeFlags.putAll(hierarchy.typeFlags);
        

        //TODO(mkr) is it useful to copy HierarchyCache, CopyInheritanceInfos?
    }

    @SuppressWarnings("unchecked") // referencing raw type fields from our super class
    protected void connect(TypeHierarchyConnector connector)
    {        
        this.typeToSuperInterfaces.putAll(connector.getTypeToSuperInterfaces());
        this.classToSuperclass.putAll(connector.getClasstoSuperclass());
        this.typeToSubtypes.putAll(connector.getTypeToSubtypes()); 
        this.interfaces = new ArrayList<IType>(Arrays.asList(connector.getAllInterfaces()));
        this.rootClasses = new TypeVector(connector.getRootClasses());
        this.typeFlags.putAll(connector.getTypeFlags());
    }
    
    protected IJavaProject getProject()
    {
        return this.project;
    }
    
    protected void compute() throws JavaModelException, CoreException
    {
        if (this.focusType != null)
        {
            OTTypeHierarchyBuilder builder = new OTTypeHierarchyBuilder(this);
            builder.build(this.computeSubtypes);
        }
    }
    
    public IOTTypeHierarchy getOTSuperTypeHierarchy(IType type) throws JavaModelException
    {
        if (type == null)
        {
            throw new IllegalArgumentException();
        }
        
        OTTypeHierarchy otHierarchy = this._hierarchyCache.getOTTypeHierarchy(OTTypeHierarchyHelper.getJavaModelIType(type));
        if (otHierarchy == null)
        {
            otHierarchy = new OTTypeHierarchy(type, type.getJavaProject(), false, this._hierarchyCache);
            otHierarchy.refresh(new NullProgressMonitor());
            this._hierarchyCache.cacheOTTypeHierarchy(otHierarchy);
        }
        
        return otHierarchy;
    }
    
    protected TypeHierarchyConnector getTypeHierarchyConnector(IType type)
            throws JavaModelException
    {
        if (type == null)
        {
            throw new IllegalArgumentException();
        }

        if (this._hierarchyCache == null)
        {
            this._hierarchyCache = new OTTypeHierarchyCache();
        }
        TypeHierarchyConnector connector = this._hierarchyCache.getTypeHierachyQuery(type);
        if (connector == null)
        {
            connector = new TypeHierarchyConnector(type, type.getJavaProject(), false);
            connector.refresh(new NullProgressMonitor());
            this._hierarchyCache.cacheTypeHierarchyQuery(connector);
        }
        
        return connector;
    }
    
    private IType replacePhantomType(IType type)
    {
        if (type != null && !isPhantomMode() && type instanceof PhantomType)
        {
            return ((PhantomType)type).getRealType();
        }
        else
        {
            return type;
        }
    }
    
    private IType[] replacePhantomType(IType[] types)
    {
        if (types == null || isPhantomMode())
        {
            return types;
        }
        else
        {
            IType[] result = new IType[types.length];
            for (int idx = 0; idx < types.length; idx++)
            {
                if (types[idx] instanceof PhantomType)
                {
                    result[idx] = ((PhantomType)types[idx]).getRealType();                
                }
                else
                {
                    result[idx] = types[idx];
                }
            }
            return result;
        }
    }

    private IType[] filterPhantomTypes(IType[] types)
    {
        if (types == null | isPhantomMode())
        {
            return types;
        }
        
        List<IType> result = new ArrayList<IType>(types.length);
        for (int idx = 0; idx < types.length; idx++)
        {
            if ( !(types[idx] instanceof PhantomType) )
            {
                result.add(types[idx]);
            }
        }
        IType[] r = result.toArray(new IType[result.size()]);
        return r;
    }

    private static IType[] filterDuplicates(IType[] types)
    {
        Set<IType> uniques = new HashSet<IType>();
        IType[] tmp = new IType[types.length];
        int j = 0;
        for (int idx = 0; idx < types.length; idx++)
        {
        	if (!uniques.contains(types[idx]))
        		tmp[j++] = types[idx];
            uniques.add(types[idx]);    
        }
        if (j == types.length)
        	return types;
        IType[] result = new IType[j];
        System.arraycopy(tmp, 0, result, 0, j);
        return result;
    }
        
    @SuppressWarnings({ "nls", "unchecked" }) // passing super fields classToSuperclass and typeToSuperInterfaces
	public String toString()
    {
    	StringBuffer buffer = new StringBuffer();
        
        buffer.append("=== OT Type Hierarchy ===\n");
        buffer.append("Focus type: " + this.focusType.getFullyQualifiedName('.') + "\n");
        buffer.append("Subtypes:" + this.computeSubtypes + "\n");
        
        buffer.append("=== class to superclass ===\n");
        buffer.append(OTTypeHierarchyHelper.toString(this.classToSuperclass));
        buffer.append("\n");
        
        buffer.append("=== type to tsuper types ===\n");
        buffer.append(OTTypeHierarchyHelper.toString(this._typeToTSuperTypes));
        buffer.append("\n");

        buffer.append("=== type to super interfaces ===\n");
        buffer.append(OTTypeHierarchyHelper.toString(this.typeToSuperInterfaces));
        buffer.append("\n");

        buffer.append("=== type to subtypes ===\n");
        buffer.append(OTTypeHierarchyHelper.toString(this.typeToSubtypes));
        buffer.append("\n");

        buffer.append("=== type to tsubtypes ===\n");
        buffer.append(OTTypeHierarchyHelper.toString(this._typeToTSubTypes));
        buffer.append("\n");

        return buffer.toString();        
    }
    
//{OT_COPY_PASTE: copied private methods:
//                TypeHierarchy.isInterface(IType)
//                TypeHierarchy.addAllCheckingDuplicates(ArrayList, IType[])

    private boolean isInterface(IType type) {
        int flags = this.getCachedFlags(type);
        if (flags == -1) {
            try {
                return type.isInterface();
            } catch (JavaModelException e) {
                return false;
            }
        } else {
            return Flags.isInterface(flags);
        }
    }
    
    /**
     * Adds all of the elements in the collection to the list if the
     * element is not already in the list.
     */
    private void addAllCheckingDuplicates(ArrayList<IType> list, IType[] collection) {
        for (int i = 0; i < collection.length; i++) {
            IType element = collection[i];
            if (!list.contains(element)) {
                list.add(element);
            }
        }
    }
//mkr}

}

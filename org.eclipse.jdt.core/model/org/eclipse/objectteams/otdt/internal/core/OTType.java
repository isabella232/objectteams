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
 * $Id: OTType.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IOTTypeHierarchy;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.search.OTSearchEngine;
import org.eclipse.objectteams.otdt.internal.core.search.matching.ReferenceToTeamPackagePattern;


/**
 * Generic OTType implementation. Actually this instance is only used to
 * represent teams.
 * 
 * @author jwloka
 * @version $Id: OTType.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class OTType extends OTJavaElement implements IOTType
{
	int _flags;
	
	public OTType(int type, IType correspondingJavaType, IJavaElement parent, int flags)
	{
		super(type, correspondingJavaType, parent);
		_flags                  = flags;
	}
	
	public OTType(int type, IType correspondingJavaType, IJavaElement parent, int flags, boolean addToParent)
	{
		super(type, correspondingJavaType, parent, addToParent);
		_flags                  = flags;
	}

	public boolean isRole()
	{
		return TypeHelper.isRole(_flags);
	}

	public boolean isTeam()
	{
		return TypeHelper.isTeam(_flags);
	}

	/**
	 * Returns the corresponding resource, if this is a toplevel type, otherwise null.
	 */
	public IResource getCorrespondingResource() throws JavaModelException
	{
		IJavaElement parent = getCorrespondingJavaElement().getParent();
		if (parent instanceof ICompilationUnit)
		{
			return ((ICompilationUnit)parent).getCorrespondingResource();
		}
		
		return null;
	}

	/**
	 * Inner types are represented (like in the JavaModel) as children. This
	 * methods filters IOTType children and returns them.
	 */
	public IType[] getInnerTypes()
	{
		List<IType> result = new LinkedList<IType>();
		IJavaElement[] children = getChildren();
		
		for (int idx = 0; idx < children.length; idx++)
		{
			if (children[idx] instanceof IOTType)
			{
				result.add((IType)children[idx]);
			}
		} 
		
		return result.toArray(new IType[result.size()]);
	}	
    
	/**
	 * Returns all roles of this team. Either the inlined, the role files or both are 
	 * returned.
	 * 
	 * @param which an ORed combination of IOTType.INLINED, IOTType.ROLEFILE, IOTType.IMPLICITLY_INHERITED,
	 * IOTType.EXPLICITLY_INHERITED and IOTType.ECLUDE_SELF
	 * @throws JavaModelException
	 * @see IOTType.getRoleTypes() for gathering all role types.
	 */
	public IType[] getRoleTypes(int which) throws JavaModelException 
	{
	    return getRoleTypes(which, null);
	}
	
	/**
	 * Returns roles named roleName of this team. Either the inlined, the role files or both are 
	 * returned.
	 * 
	 * TODO (carp): specify and implement sort order especially with respect to the hierarchy
	 * 
	 * @param which an ORed combination of IOTType.INLINED, IOTType.ROLEFILE, IOTType.IMPLICITLY_INHERITED,
	 * IOTType.EXPLICITLY_INHERITED and IOTType.ECLUDE_SELF
	 * @throws JavaModelException
	 * @see IOTType.getRoleTypes() for gathering all role types.
	 */
	public IType[] getRoleTypes(int which, String roleName) throws JavaModelException 
	{
	    ArrayList<IType> result = new ArrayList<IType>();
	    IType[] typesToConsider = null;
	    
	    final int BOTH_HIERARCHIES_MASK = IMPLICTLY_INHERITED | EXPLICITLY_INHERITED;
	    
	    if ((which & BOTH_HIERARCHIES_MASK) != 0)
	    {
	        IOTTypeHierarchy hierarchy = newSuperOTTypeHierarchy(new NullProgressMonitor());
	        switch (which & BOTH_HIERARCHIES_MASK) {
	        	case EXPLICITLY_INHERITED:
	        	    typesToConsider = new IType[] { hierarchy.getExplicitSuperclass(this) };
	        	    break;
        	    case IMPLICTLY_INHERITED:
	        	    typesToConsider = hierarchy.getAllTSuperTypes(this);
        	        break;
    	        case BOTH_HIERARCHIES_MASK:
	        	    typesToConsider = hierarchy.getAllSuperclasses(this);
    	            break;
	        }
	    }

	    if ((which & EXCLUDE_SELF) == 0)
	    {
	        int newLen = (typesToConsider != null) ? typesToConsider.length + 1 : 1;
	        IType[] ttc = new IType[newLen];
	        ttc[0] = this;
	        if (typesToConsider != null)
	            System.arraycopy(typesToConsider, 0, ttc, 1, typesToConsider.length);
	        typesToConsider = ttc;
	    }
	    else

	    if (typesToConsider == null)
	        throw new IllegalArgumentException("EXCLUDE_SELF without a hierarchy requested"); //$NON-NLS-1$
	        
	    typesToConsider = fixTypesToConsider(typesToConsider);
	    
        //TODO(haebor) consider that roles from binaries can't be differentiated since external roles are inlined
		if ((which & ROLEFILE) != 0)
		{
//			packageSearchGetRoleFiles(result);
		    result.addAll(searchEngineGetRoleFiles(typesToConsider, roleName));
		}
	
        if ((which & INLINED) != 0)
        {
            for (int i = 0; i < typesToConsider.length; i++)
            {
                IType[] roleTypes = typesToConsider[i].getTypes();
                for (int j = 0; j < roleTypes.length; j++)
                {
                    IType currentType = roleTypes[j];
                    if (roleName == null || roleName.equals(currentType.getElementName()))
	                    result.add(currentType);
                }
            }
        }
        
        return result.toArray(new IType[result.size()]);
	}
	
	private static IType[] fixTypesToConsider(IType[] typesToConsider)
    {
	    ArrayList<IType> result = new ArrayList<IType>(typesToConsider.length);
	    
	    for (int i = 0; i < typesToConsider.length; i++)
        {
            IType type = typesToConsider[i];
            if (!TypeHelper.isOrgObjectTeamsTeam(type))
            {
                IOTType role = OTModelManager.getOTElement(type);
                if (role != null)
                    result.add(role);
            }
        }
        
	    return result.toArray(new IType[result.size()]);
    }

    private List<IType> searchEngineGetRoleFiles(IType[] teamsToConsider, String roleName) throws JavaModelException
    {
        final List<IType> searchResult = new ArrayList<IType>();
        char[] role = (roleName == null) ? null : roleName.toCharArray();
        
        try
        {

	        OTSearchEngine searchEngine = new OTSearchEngine();
            IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{this.getAncestor(IJavaElement.JAVA_PROJECT)});
            SearchRequestor requestor = new SearchRequestor() {
                public void acceptSearchMatch(SearchMatch match) throws CoreException
                {
                    searchResult.add((IType)match.getElement());
                }
            };
            
            for (int i = 0; i < teamsToConsider.length; i++)
            {
                IType currentType = teamsToConsider[i];
                IOTType ottype = OTModelManager.getOTElement(currentType);
                if (ottype == null || !ottype.isTeam())
                	continue;
                
	            SearchPattern pattern = 
	                new ReferenceToTeamPackagePattern(currentType.getFullyQualifiedName().toCharArray(), role,
	                        SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	            searchEngine.search(pattern, scope, requestor, new NullProgressMonitor());
            }
        }
 		catch (CoreException exc)
        {
            throw new JavaModelException(exc);
        }
        return searchResult;
    }

    /**
	 * Returns all role types (inlined and role files) contained in this team.
	 * @throws JavaModelException
	 */
	public IType[] getRoleTypes() throws JavaModelException
	{
	    return getRoleTypes(IOTType.INLINED | IOTType.ROLEFILE);
	}
	
	public int getFlags()
	{
		return _flags;
	}
	
	public IType getRoleType(String simpleName)
	{
	    if (isTeam() && exists())
	    {
		    IType roleType = getType(simpleName);
		    if (roleType.exists())
		    {
		        return roleType;
		    }
		    
		    try
	        {
		        List<IType> roleFiles = searchEngineGetRoleFiles(new IType[] { this }, simpleName);
		        if (roleFiles.size() > 0)
		        	return (IType) roleFiles.get(0); // actually there may be more, due to multiple src-folders...
		        
// previous implementation without search engine
//			    String encTeamName = this.getFullyQualifiedName();
//			    String qualName	  = encTeamName + "." + simpleName;
//	            roleType = getJavaProject().findType(qualName);
//	            if (roleType != null)
//	            {
//	                return roleType;
//	            }
	        }
	        catch (JavaModelException ignored) {}
	    }
	    
	    return null;
	}
	
	public boolean equals(Object obj)
	{
		if (obj == this.getCorrespondingJavaElement())
			return true;
		
		if(!(obj instanceof OTType))
		{
		    return false;
		}

		OTType other = (OTType)obj;
		
		return super.equals(other)
				&& getFlags() == other.getFlags();
	}

	@SuppressWarnings("nls")
	public String toString()
	{
		return "OTType " + getElementName() + " for type: " + getCorrespondingJavaElement().toString();
	}
	
	private IType getIType()
	{
	    return (IType)super.getCorrespondingJavaElement();
	}
	
	/**
	 * @deprecated Use {@link #codeComplete(char[],int,int,char[][],char[][],int[],boolean,CompletionRequestor)} instead.
	 */
    public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, org.eclipse.jdt.core.ICompletionRequestor requestor) throws JavaModelException
    {
        getIType().codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor);
    }
    /**
	 * @deprecated Use {@link #codeComplete(char[],int,int,char[][],char[][],int[],boolean,CompletionRequestor,WorkingCopyOwner)} instead.
     */
    public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, org.eclipse.jdt.core.ICompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException
    {
        getIType().codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor, owner);
    }
    public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, CompletionRequestor requestor, IProgressMonitor monitor)
    		throws JavaModelException 
    {
    	getIType().codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor, monitor);	
    }
    public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, CompletionRequestor requestor, WorkingCopyOwner owner, IProgressMonitor monitor)
    		throws JavaModelException 
    {
    	getIType().codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor, owner, monitor);
    }
    public IField createField(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().createField(contents, sibling, force, monitor);
    }

    public IInitializer createInitializer(String contents, IJavaElement sibling, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().createInitializer(contents, sibling, monitor);
    }

    public IMethod createMethod(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().createMethod(contents, sibling, force, monitor);
    }

    public IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().createType(contents, sibling, force, monitor);
    }

    public IMethod[] findMethods(IMethod method)
    {
        return getIType().findMethods(method);
    }

    public IField getField(String name)
    {
        return getIType().getField(name);
    }

    public IField[] getFields() throws JavaModelException
    {
        return getIType().getFields();
    }

    public String getFullyQualifiedName()
    {
        return getIType().getFullyQualifiedName();
    }

    public String getFullyQualifiedName(char enclosingTypeSeparator)
    {
        return getIType().getFullyQualifiedName(enclosingTypeSeparator);
    }

    public IInitializer getInitializer(int occurrenceCount)
    {
        return getIType().getInitializer(occurrenceCount);
    }

    public IInitializer[] getInitializers() throws JavaModelException
    {
        return getIType().getInitializers();
    }

    public IMethod getMethod(String name, String[] parameterTypeSignatures)
    {
        return getIType().getMethod(name, parameterTypeSignatures);
    }

    public IMethod[] getMethods() throws JavaModelException
    {
        return getIType().getMethods();
    }

    public IAnnotation getAnnotation(String name) {
    	return getIType().getAnnotation(name);
    }

    public IAnnotation[] getAnnotations() throws JavaModelException {
    	return getIType().getAnnotations();
    }
    
    public IPackageFragment getPackageFragment()
    {
        return getIType().getPackageFragment();
    }

    public String getSuperclassName() throws JavaModelException
    {
        return getIType().getSuperclassName();
    }

    public String getSuperclassTypeSignature() throws JavaModelException
    {
        return getIType().getSuperclassTypeSignature();
    }

    public String[] getSuperInterfaceTypeSignatures() throws JavaModelException
    {
        return getIType().getSuperInterfaceTypeSignatures();
    }

    public String[] getSuperInterfaceNames() throws JavaModelException
    {
        return getIType().getSuperInterfaceNames();
    }

    public String[] getTypeParameterSignatures() throws JavaModelException
    {
        return getIType().getTypeParameterSignatures();
    }

    public IType getType(String name)
    {
        return getIType().getType(name);
    }

    public String getTypeQualifiedName()
    {
        return getIType().getTypeQualifiedName();
    }

    public String getTypeQualifiedName(char enclosingTypeSeparator)
    {
        return getIType().getTypeQualifiedName(enclosingTypeSeparator);
    }

    public IType[] getTypes() throws JavaModelException
    {
		return getIType().getTypes();
    }

    public boolean isAnonymous() throws JavaModelException
    {
        return getIType().isAnonymous();
    }

    public boolean isClass() throws JavaModelException
    {
        return getIType().isClass();
    }

    public boolean isEnum() throws JavaModelException
    {
        return getIType().isEnum();
    }

    public boolean isInterface() throws JavaModelException
    {
        return getIType().isInterface();
    }

    public boolean isAnnotation() throws JavaModelException
    {
        return getIType().isAnnotation();
    }

    public boolean isLocal() throws JavaModelException
    {
        return getIType().isLocal();
    }

    public boolean isMember() throws JavaModelException
    {
        return getIType().isMember();
    }

    public ITypeHierarchy loadTypeHierachy(InputStream input, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().loadTypeHierachy(input, monitor);
    }

    public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newSupertypeHierarchy(monitor);
    }

    public ITypeHierarchy newSupertypeHierarchy(ICompilationUnit[] workingCopies, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newSupertypeHierarchy(workingCopies, monitor);
    }
    /**
	 * @deprecated Use {@link #newSupertypeHierarchy(ICompilationUnit[], IProgressMonitor)} instead
     */
    public ITypeHierarchy newSupertypeHierarchy(org.eclipse.jdt.core.IWorkingCopy[] workingCopies, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newSupertypeHierarchy(workingCopies, monitor);
    }

    public ITypeHierarchy newSupertypeHierarchy(WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newSupertypeHierarchy(owner, monitor);
    }

    public ITypeHierarchy newTypeHierarchy(IJavaProject project, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newTypeHierarchy(project, monitor);
    }

    public ITypeHierarchy newTypeHierarchy(IJavaProject project, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newTypeHierarchy(project, owner, monitor);
    }

    public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newTypeHierarchy(monitor);
    }

    public ITypeHierarchy newTypeHierarchy(ICompilationUnit[] workingCopies, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newTypeHierarchy(workingCopies, monitor);
    }
    /**
	 * @deprecated Use {@link #newTypeHierarchy(ICompilationUnit[], IProgressMonitor)} instead
     */
    public ITypeHierarchy newTypeHierarchy(org.eclipse.jdt.core.IWorkingCopy[] workingCopies, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newTypeHierarchy(workingCopies, monitor);
    }

    public ITypeHierarchy newTypeHierarchy(WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException
    {
        return getIType().newTypeHierarchy(owner, monitor);
    }

    public String[][] resolveType(String typeName) throws JavaModelException
    {
        return getIType().resolveType(typeName);
    }

    public String[][] resolveType(String typeName, WorkingCopyOwner owner) throws JavaModelException
    {
        return getIType().resolveType(typeName, owner);
    }

    public IClassFile getClassFile()
    {
        return getIType().getClassFile();
    }

    public ICompilationUnit getCompilationUnit()
    {
        return getIType().getCompilationUnit();
    }

    public ITypeRoot getTypeRoot() {
    	return getIType().getTypeRoot();
    }
    
    public IType getDeclaringType()
    {
        return getIType().getDeclaringType();
    }

    public ISourceRange getNameRange() throws JavaModelException
    {
        return getIType().getNameRange();
    }

    public IType getType(String name, int occurrenceCount)
    {
        return getIType().getType(name, occurrenceCount);
    }

    public boolean isBinary()
    {
        return getIType().isBinary();
    }

    public String getSource() throws JavaModelException
    {
        return getIType().getSource();
    }

    public ISourceRange getSourceRange() throws JavaModelException
    {
        return getIType().getSourceRange();
    }

    public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException
    {
        getIType().copy(container, sibling, rename, replace, monitor);
    }

    public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException
    {
        getIType().delete(force, monitor);
    }

    public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException
    {
        getIType().move(container, sibling, rename, replace, monitor);
    }

    public void rename(String name, boolean replace, IProgressMonitor monitor) throws JavaModelException
    {
        getIType().rename(name, replace, monitor);
    }

    public IOTTypeHierarchy newSuperOTTypeHierarchy(IProgressMonitor monitor) throws JavaModelException
	{
	    OTTypeHierarchy hierarchy = new OTTypeHierarchy(this, getJavaProject(), false);
	    hierarchy.refresh(monitor);
	    return hierarchy;
	}
	
	public IOTTypeHierarchy newOTTypeHierarchy(IProgressMonitor monitor) throws JavaModelException
	{
	    OTTypeHierarchy hierarchy = new OTTypeHierarchy(this, getJavaProject(), true);
	    hierarchy.refresh(monitor);
	    return hierarchy;
	}

	public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, CompletionRequestor requestor) throws JavaModelException {
		getIType().codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor);
	}

	public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, CompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException {
		getIType().codeComplete(snippet, insertion, position, localVariableTypeNames, localVariableNames, localVariableModifiers, isStatic, requestor, owner);
	}

	public IJavaElement[] getChildrenForCategory(String category) throws JavaModelException {
		return getIType().getChildrenForCategory(category);
	}

	public String getFullyQualifiedParameterizedName() throws JavaModelException {
		return getIType().getFullyQualifiedParameterizedName();
	}

	public String getKey() {
		return getIType().getKey();
	}

	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		return getIType().getTypeParameters();
	}

	public ITypeParameter getTypeParameter(String name) {
		return getIType().getTypeParameter(name);
	}

	public boolean isResolved() {
		return getIType().isResolved();
	}

	public String[] getCategories() throws JavaModelException {
		return getIType().getCategories();
	}

	public ISourceRange getJavadocRange() throws JavaModelException {
		return getIType().getJavadocRange();
	}

	public int getOccurrenceCount() {
		return getIType().getOccurrenceCount();
	}

	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		return getIType().getAttachedJavadoc(monitor);
	}
}

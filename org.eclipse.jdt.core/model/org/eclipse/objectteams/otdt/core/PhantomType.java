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
 * $Id: PhantomType.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import java.io.InputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

/**
 * A PhantomType is a type in the OTTypeHierarchy. It is a place holder
 * for roles created by ObjectTeams copy inheritance. A PhantomType wraps
 * its 'nearest' declared (non-phantom) implicit super role,
 * but is located in a different team context.
 * 
 * A PhantomType wraps only it's 'nearest' tsuper (tsuper0),
 * but may copy inherit members from other tsupers (tsuper1, ...).
 * 
 * Most methods of this type are forwarded to the real role type
 * or the enclosing team type.
 * A PhantomType (as its wrappee) may also be a nested team.
 * @author Michael Krueger (mkr)
 * @version $Id: PhantomType.java 23416 2010-02-03 19:59:31Z stephan $
 */
// TODO: should this implement IRoleType rather than just IType?
public class PhantomType implements IType
{

    private IType _realType;
    private IType _enclosingTeam;
    
    public PhantomType(IType enclosingTeam, IType tsuperRole)
    {
        _enclosingTeam = enclosingTeam;
        this.setRealType(tsuperRole);        
    }
    
    /**
     * @see org.eclipse.jdt.core.IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, org.eclipse.jdt.core.ICompletionRequestor)
	 * @deprecated Use {@link #codeComplete(char[],int,int,char[][],char[][],int[],boolean,CompletionRequestor)} instead.
     */
    public void codeComplete(char[] snippet, int insertion, int position,
            char[][] localVariableTypeNames, char[][] localVariableNames,
            int[] localVariableModifiers, boolean isStatic,
            org.eclipse.jdt.core.ICompletionRequestor requestor) throws JavaModelException
    {
        handleUnsupported();
    }

    /**
     * @see org.eclipse.jdt.core.IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, org.eclipse.jdt.core.ICompletionRequestor, org.eclipse.jdt.core.WorkingCopyOwner)
	 * @deprecated Use {@link #codeComplete(char[],int,int,char[][],char[][],int[],boolean,CompletionRequestor,WorkingCopyOwner)} instead.
     */
    public void codeComplete(char[] snippet, int insertion, int position,
            char[][] localVariableTypeNames, char[][] localVariableNames,
            int[] localVariableModifiers, boolean isStatic,
            org.eclipse.jdt.core.ICompletionRequestor requestor, WorkingCopyOwner owner)
            throws JavaModelException
    {
        handleUnsupported();
    }

	public void codeComplete(char[] snippet, int insertion, int position,
			char[][] localVariableTypeNames, char[][] localVariableNames,
			int[] localVariableModifiers, boolean isStatic,
			CompletionRequestor requestor, IProgressMonitor monitor)
			throws JavaModelException 
	{
		handleUnsupported();
	}

	public void codeComplete(char[] snippet, int insertion, int position,
			char[][] localVariableTypeNames, char[][] localVariableNames,
			int[] localVariableModifiers, boolean isStatic,
			CompletionRequestor requestor, WorkingCopyOwner owner,
			IProgressMonitor monitor) throws JavaModelException 
	{
		handleUnsupported();
	}

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#createField(java.lang.String, org.eclipse.jdt.core.IJavaElement, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IField createField(String contents, IJavaElement sibling,
            boolean force, IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.createField(contents, sibling, force, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#createInitializer(java.lang.String, org.eclipse.jdt.core.IJavaElement, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IInitializer createInitializer(String contents,
            IJavaElement sibling, IProgressMonitor monitor)
            throws JavaModelException
    {
        return _realType.createInitializer(contents, sibling, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#createMethod(java.lang.String, org.eclipse.jdt.core.IJavaElement, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IMethod createMethod(String contents, IJavaElement sibling,
            boolean force, IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.createMethod(contents, sibling, force, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#createType(java.lang.String, org.eclipse.jdt.core.IJavaElement, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IType createType(String contents, IJavaElement sibling,
            boolean force, IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.createType(contents, sibling, force, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#findMethods(org.eclipse.jdt.core.IMethod)
     */
    public IMethod[] findMethods(IMethod method)
    {
        return _realType.findMethods(method);
    }

    public String getElementName()
    {
        return _realType.getElementName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getField(java.lang.String)
     */
    public IField getField(String name)
    {
        return _realType.getField(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getFields()
     */
    public IField[] getFields() throws JavaModelException
    {
        return _realType.getFields();
    }

    public String getFullyQualifiedName()
    {
        return getFullyQualifiedName('$');
    }

    public String getFullyQualifiedName(char enclosingTypeSeparator)
    {
        return _enclosingTeam.getFullyQualifiedName(enclosingTypeSeparator)
               + enclosingTypeSeparator
               + getElementName();        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getInitializer(int)
     */
    public IInitializer getInitializer(int occurrenceCount)
    {
        return _realType.getInitializer(occurrenceCount);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getInitializers()
     */
    public IInitializer[] getInitializers() throws JavaModelException
    {
        return _realType.getInitializers();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getMethod(java.lang.String, java.lang.String[])
     */
    public IMethod getMethod(String name, String[] parameterTypeSignatures)
    {
        return _realType.getMethod(name, parameterTypeSignatures);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getMethods()
     */
    public IMethod[] getMethods() throws JavaModelException
    {
        return _realType.getMethods();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getPackageFragment()
     */
    public IPackageFragment getPackageFragment()
    {
        return _enclosingTeam.getPackageFragment();
    }
    
    /**
     * If the type is a role file decide for the role files CU.
     * @see IMember#getTypeRoot()
     */
    public ITypeRoot getTypeRoot() {
    	return _realType.getTypeRoot();
    }


    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getSuperclassName()
     */
    public String getSuperclassName() throws JavaModelException
    {
        return _realType.getSuperclassName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getSuperclassTypeSignature()
     */
    public String getSuperclassTypeSignature() throws JavaModelException
    {
        return _realType.getSuperclassTypeSignature();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getSuperInterfaceTypeSignatures()
     */
    public String[] getSuperInterfaceTypeSignatures() throws JavaModelException
    {
        return _realType.getSuperInterfaceTypeSignatures();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getSuperInterfaceNames()
     */
    public String[] getSuperInterfaceNames() throws JavaModelException
    {
        return _realType.getSuperInterfaceNames();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getTypeParameterSignatures()
     */
    public String[] getTypeParameterSignatures() throws JavaModelException
    {
        return _realType.getTypeParameterSignatures();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getType(java.lang.String)
     */
    public IType getType(String name)
    {
        return _realType.getType(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getTypeQualifiedName()
     */
    public String getTypeQualifiedName()
    {
        return getTypeQualifiedName('$');
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getTypeQualifiedName(char)
     */
    public String getTypeQualifiedName(char enclosingTypeSeparator)
    {
        if (isBinary())
        {
            return _enclosingTeam.getTypeQualifiedName(enclosingTypeSeparator);
        }
        else
        {
            return _enclosingTeam.getTypeQualifiedName(enclosingTypeSeparator).concat(
                        enclosingTypeSeparator + _realType.getElementName());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#getTypes()
     */
    public IType[] getTypes() throws JavaModelException
    {
        return _realType.getTypes();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#isAnonymous()
     */
    public boolean isAnonymous() throws JavaModelException
    {
        return _realType.isAnonymous();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#isClass()
     */
    public boolean isClass() throws JavaModelException
    {
        return _realType.isClass();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#isEnum()
     */
    public boolean isEnum() throws JavaModelException
    {
        return _realType.isEnum();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#isInterface()
     */
    public boolean isInterface() throws JavaModelException
    {
        return _realType.isInterface();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#isAnnotation()
     */
    public boolean isAnnotation() throws JavaModelException
    {
        return _realType.isAnnotation();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#isLocal()
     */
    public boolean isLocal() throws JavaModelException
    {
        return _realType.isLocal();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#isMember()
     */
    public boolean isMember() throws JavaModelException
    {
        return _realType.isMember();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#loadTypeHierachy(java.io.InputStream, org.eclipse.core.runtime.IProgressMonitor)
     */
    public ITypeHierarchy loadTypeHierachy(InputStream input,
            IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.loadTypeHierachy(input, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#newSupertypeHierarchy(org.eclipse.core.runtime.IProgressMonitor)
     */
    public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor)
            throws JavaModelException
    {
        return _realType.newSupertypeHierarchy(monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#newSupertypeHierarchy(org.eclipse.jdt.core.ICompilationUnit[], org.eclipse.core.runtime.IProgressMonitor)
     */
    public ITypeHierarchy newSupertypeHierarchy(
            ICompilationUnit[] workingCopies, IProgressMonitor monitor)
            throws JavaModelException
    {
        return _realType.newSupertypeHierarchy(workingCopies, monitor);
    }

    /**
     * @see org.eclipse.jdt.core.IType#newSupertypeHierarchy(org.eclipse.jdt.core.IWorkingCopy[], org.eclipse.core.runtime.IProgressMonitor)
     * @deprecated Use {@link #newSupertypeHierarchy(ICompilationUnit[], IProgressMonitor)} instead
     */
    public ITypeHierarchy newSupertypeHierarchy(org.eclipse.jdt.core.IWorkingCopy[] workingCopies,
            IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.newSupertypeHierarchy(workingCopies, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#newSupertypeHierarchy(org.eclipse.jdt.core.WorkingCopyOwner, org.eclipse.core.runtime.IProgressMonitor)
     */
    public ITypeHierarchy newSupertypeHierarchy(WorkingCopyOwner owner,
            IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.newSupertypeHierarchy(owner, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.IJavaProject, org.eclipse.core.runtime.IProgressMonitor)
     */
    public ITypeHierarchy newTypeHierarchy(IJavaProject project,
            IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.newTypeHierarchy(project, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.WorkingCopyOwner, org.eclipse.core.runtime.IProgressMonitor)
     */
    public ITypeHierarchy newTypeHierarchy(IJavaProject project,
            WorkingCopyOwner owner, IProgressMonitor monitor)
            throws JavaModelException
    {
        return _realType.newTypeHierarchy(project, owner, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.core.runtime.IProgressMonitor)
     */
    public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor)
            throws JavaModelException
    {
        return _realType.newTypeHierarchy(monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.ICompilationUnit[], org.eclipse.core.runtime.IProgressMonitor)
     */
    public ITypeHierarchy newTypeHierarchy(ICompilationUnit[] workingCopies,
            IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.newTypeHierarchy(workingCopies, monitor);
    }

    /**
     * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.IWorkingCopy[], org.eclipse.core.runtime.IProgressMonitor)
	 * @deprecated Use {@link #newTypeHierarchy(ICompilationUnit[], IProgressMonitor)} instead
     */
    public ITypeHierarchy newTypeHierarchy(org.eclipse.jdt.core.IWorkingCopy[] workingCopies,
            IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.newTypeHierarchy(workingCopies, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.WorkingCopyOwner, org.eclipse.core.runtime.IProgressMonitor)
     */
    public ITypeHierarchy newTypeHierarchy(WorkingCopyOwner owner,
            IProgressMonitor monitor) throws JavaModelException
    {
        return _realType.newTypeHierarchy(owner, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#resolveType(java.lang.String)
     */
    public String[][] resolveType(String typeName) throws JavaModelException
    {
        return _realType.resolveType(typeName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IType#resolveType(java.lang.String, org.eclipse.jdt.core.WorkingCopyOwner)
     */
    public String[][] resolveType(String typeName, WorkingCopyOwner owner)
            throws JavaModelException
    {
        return _realType.resolveType(typeName, owner);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IMember#getClassFile()
     */
    public IClassFile getClassFile()
    {
        return _enclosingTeam.getClassFile();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IMember#getCompilationUnit()
     */
    public ICompilationUnit getCompilationUnit()
    {
        return _enclosingTeam.getCompilationUnit();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IMember#getDeclaringType()
     */
    public IType getDeclaringType()
    {
    	return this._enclosingTeam;
        //return _realType.getDeclaringType();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IMember#getFlags()
     */
    public int getFlags() throws JavaModelException
    {        
        return _realType.getFlags();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IMember#getNameRange()
     */
    public ISourceRange getNameRange() throws JavaModelException
    {
        return _realType.getNameRange();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IMember#getType(java.lang.String, int)
     */
    public IType getType(String name, int occurrenceCount)
    {
        return _realType.getType(name, occurrenceCount);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IMember#isBinary()
     */
    public boolean isBinary()
    {
        return _enclosingTeam.isBinary();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ISourceReference#exists()
     */
    public boolean exists()
    {
        return _realType.exists();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getAncestor(int)
     */
    public IJavaElement getAncestor(int ancestorType)
    {
        return _realType.getAncestor(ancestorType);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getCorrespondingResource()
     */
    public IResource getCorrespondingResource() throws JavaModelException
    {
        return _enclosingTeam.getCorrespondingResource();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getElementType()
     */
    public int getElementType()
    {
        return _realType.getElementType();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getHandleIdentifier()
     */
    public String getHandleIdentifier()
    {
        return _realType.getHandleIdentifier();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getJavaModel()
     */
    public IJavaModel getJavaModel()
    {
        return _realType.getJavaModel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getJavaProject()
     */
    public IJavaProject getJavaProject()
    {
        return _enclosingTeam.getJavaProject();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getOpenable()
     */
    public IOpenable getOpenable()
    {
        return _enclosingTeam.getOpenable();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getParent()
     */
    public IJavaElement getParent()
    {
        return _enclosingTeam;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getPath()
     */
    public IPath getPath()
    {
        String lastSegment = _realType.getPath().lastSegment();
        if (lastSegment != null)
        {
            return _enclosingTeam.getPath().append(lastSegment);
        }
        else
        {
            return _enclosingTeam.getPath();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getPrimaryElement()
     */
    public IJavaElement getPrimaryElement()
    {
        return _enclosingTeam.getPrimaryElement();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getResource()
     */
    public IResource getResource()
    {
        return _enclosingTeam.getResource();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getSchedulingRule()
     */
    public ISchedulingRule getSchedulingRule()
    {
        return _enclosingTeam.getSchedulingRule();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#getUnderlyingResource()
     */
    public IResource getUnderlyingResource() throws JavaModelException
    {
        return _enclosingTeam.getUnderlyingResource();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#isReadOnly()
     */
    public boolean isReadOnly()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IJavaElement#isStructureKnown()
     */
    public boolean isStructureKnown() throws JavaModelException
    {
        return _realType.isStructureKnown();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ISourceReference#getSource()
     */
    public String getSource() throws JavaModelException
    {
        return _realType.getSource();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ISourceReference#getSourceRange()
     */
    public ISourceRange getSourceRange() throws JavaModelException
    {
        return _realType.getSourceRange();
    }

	public IAnnotation getAnnotation(String name) {
		return this._realType.getAnnotation(name);
	}

	public IAnnotation[] getAnnotations() throws JavaModelException {
		return this._realType.getAnnotations();
	}

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ISourceManipulation#copy(org.eclipse.jdt.core.IJavaElement, org.eclipse.jdt.core.IJavaElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void copy(IJavaElement container, IJavaElement sibling,
            String rename, boolean replace, IProgressMonitor monitor)
            throws JavaModelException
    {
        _realType.copy(container, sibling, rename, replace, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ISourceManipulation#delete(boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void delete(boolean force, IProgressMonitor monitor)
            throws JavaModelException
    {
        _realType.delete(force, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ISourceManipulation#move(org.eclipse.jdt.core.IJavaElement, org.eclipse.jdt.core.IJavaElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void move(IJavaElement container, IJavaElement sibling,
            String rename, boolean replace, IProgressMonitor monitor)
            throws JavaModelException
    {
        _realType.move(container, sibling, rename, replace, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.ISourceManipulation#rename(java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void rename(String name, boolean replace, IProgressMonitor monitor)
            throws JavaModelException
    {
        _realType.rename(name, replace, monitor);

    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IParent#getChildren()
     */
    public IJavaElement[] getChildren() throws JavaModelException
    {
        return _realType.getChildren();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IParent#hasChildren()
     */
    public boolean hasChildren() throws JavaModelException
    {
        return _realType.hasChildren();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter)
    {
        return _realType.getAdapter(adapter);
    }
 
    @SuppressWarnings("nls")
	public String toString()
    {
        return "(" + getFullyQualifiedName() + "|" + _realType.getFullyQualifiedName('.') + ")";
    }
         
    public IType getRealType()
    {
        return _realType;
    }
    
    public void setRealType(IType type)
    {
        if (type instanceof PhantomType)
        {
            _realType = ((PhantomType)type).getRealType();
        }
        else
        {
            _realType = type;
        }
    }
    
	public int hashCode()
    {		
		return getFullyQualifiedName().hashCode();
	}
    
	public boolean equals(Object arg0)
    {
        if (arg0 == null || !(arg0 instanceof PhantomType) )
        {
        	return false;
        }
        
        PhantomType other = (PhantomType)arg0;
		return _enclosingTeam.equals(other._enclosingTeam)
                    && (_realType.equals(other._realType));
	}

	private void handleUnsupported()
    {
        throw new UnsupportedOperationException("Unsupported operation on PhantomType " //$NON-NLS-1$
                                                + getFullyQualifiedName());
    }
	
	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		return _realType.getTypeParameters();
	}
	
	public boolean isResolved() {
		return _realType.isResolved() && _enclosingTeam.isResolved();
	}
 
	// TODO: check whether we need to support any of those methods
	public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, CompletionRequestor requestor) throws JavaModelException {
        handleUnsupported();
	}

	public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, CompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException {
        handleUnsupported();
	}

	public IJavaElement[] getChildrenForCategory(String category) throws JavaModelException {
        handleUnsupported();
		return null;
	}

	public String getFullyQualifiedParameterizedName() throws JavaModelException {
        handleUnsupported();
		return null;
	}

	public String getKey() {
        handleUnsupported();
		return null;
	}

	public ITypeParameter getTypeParameter(String name) {
        handleUnsupported();
		return null;
	}

	public String[] getCategories() throws JavaModelException {
        handleUnsupported();
		return null;
	}

	public ISourceRange getJavadocRange() throws JavaModelException {
        handleUnsupported();
		return null;
	}

	public int getOccurrenceCount() {
        handleUnsupported();
		return 0;
	}

	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
        handleUnsupported();
		return null;
	}
}

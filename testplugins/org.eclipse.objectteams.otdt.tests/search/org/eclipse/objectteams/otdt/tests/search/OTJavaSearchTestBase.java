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
 * $Id: OTJavaSearchTestBase.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.search;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.objectteams.otdt.core.IMethodMapping;

/**
 * Tests the Java search engine where results are JavaElements and source
 * positions.
 * 
 * @version $Id: OTJavaSearchTestBase.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTJavaSearchTestBase extends FileBasedSearchTest implements
		IJavaSearchConstants
{
	/**
	 * Collects results as a string.
	 */
	public static class JavaSearchResultCollector extends SearchRequestor
	{
		public StringBuffer results = new StringBuffer();

		public boolean showAccuracy;

		public boolean showProject;

		public boolean showContext;

		public boolean showInsideDoc;

		public void acceptSearchMatch(SearchMatch match) throws CoreException
		{
			try
			{
				if (results.length() > 0)
					results.append("\n");
				IResource resource = match.getResource();
				IJavaElement element = (IJavaElement) match.getElement();
				if (resource != null)
				{
					IPath path = resource.getProjectRelativePath();
					if (path.segmentCount() == 0)
					{
						IJavaElement root = element;
						while (root != null
								&& !(root instanceof IPackageFragmentRoot))
						{
							root = root.getParent();
						}
						if (root != null)
						{
							IPackageFragmentRoot pkgFragmentRoot = (IPackageFragmentRoot) root;
							if (pkgFragmentRoot.isExternal())
							{
								results.append(pkgFragmentRoot.getPath()
										.toOSString());
							} else
							{
								results.append(pkgFragmentRoot.getPath());
							}
						}
					} else
					{
						results.append(path);
					}
				} else
				{
					results.append(element.getPath());
				}
				if (this.showProject)
				{
					IProject project = element.getJavaProject().getProject();
					results.append(" [in ");
					results.append(project.getName());
					results.append("]");
				}
				ICompilationUnit unit = null;
//{ObjectTeams: formatting of method mappings
				if (element instanceof IMethodMapping)
				{
				    results.append(" ");
				    IMethodMapping mapping = (IMethodMapping) element;
				    append(mapping);
				    unit = mapping.getCompilationUnit();
				}
//carp}
				else if (element instanceof IMethod)
				{
					results.append(" ");
					IMethod method = (IMethod) element;
					append(method);
					unit = method.getCompilationUnit();
				} else if (element instanceof IType)
				{
					results.append(" ");
					IType type = (IType) element;
					append(type);
					unit = type.getCompilationUnit();
				} else if (element instanceof IField)
				{
					results.append(" ");
					IField field = (IField) element;
					append(field);
					unit = field.getCompilationUnit();
				} else if (element instanceof IInitializer)
				{
					results.append(" ");
					IInitializer initializer = (IInitializer) element;
					append(initializer);
					unit = initializer.getCompilationUnit();
				} else if (element instanceof IPackageFragment)
				{
					results.append(" ");
					append((IPackageFragment) element);
				} else if (element instanceof ILocalVariable)
				{
					results.append(" ");
					ILocalVariable localVar = (ILocalVariable) element;
					IJavaElement parent = localVar.getParent();
					if (parent instanceof IInitializer)
					{
						IInitializer initializer = (IInitializer) parent;
						append(initializer);
					} else
					{ // IMethod
						IMethod method = (IMethod) parent;
						append(method);
					}
					results.append(".");
					results.append(localVar.getElementName());
					unit = (ICompilationUnit) localVar
							.getAncestor(IJavaElement.COMPILATION_UNIT);
				}
				if (resource instanceof IFile)
				{
					char[] contents = null;
					if ("java".equals(resource.getFileExtension()))
					{
						ICompilationUnit cu = (ICompilationUnit) element
								.getAncestor(IJavaElement.COMPILATION_UNIT);
						if (cu != null && cu.isWorkingCopy())
						{
							// working copy
							contents = unit.getBuffer().getCharacters();
						} else
						{
							contents = new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(
									null, ((IFile) resource).getLocation()
											.toFile().getPath(), null)
									.getContents();
						}
					}
					int start = match.getOffset();
					int end = start + match.getLength();
					if (start == -1 || contents != null)
					{ // retrieving attached source not implemented here
						results.append(" [");
						if (start > -1)
						{
							if (this.showContext)
							{
								int lineStart1 = CharOperation.lastIndexOf(
										'\n', contents, 0, start);
								int lineStart2 = CharOperation.lastIndexOf(
										'\r', contents, 0, start);
								int lineStart = Math
										.max(lineStart1, lineStart2) + 1;
								results.append(CharOperation.subarray(contents,
										lineStart, start));
								results.append("<");
							}
							results.append(CharOperation.subarray(contents,
									start, end));
							if (this.showContext)
							{
								results.append(">");
								int lineEnd1 = CharOperation.indexOf('\n',
										contents, end);
								int lineEnd2 = CharOperation.indexOf('\r',
										contents, end);
								int lineEnd = lineEnd1 > 0 && lineEnd2 > 0 ? Math
										.min(lineEnd1, lineEnd2)
										: Math.max(lineEnd1, lineEnd2);
								if (lineEnd == -1)
									lineEnd = contents.length;
								results.append(CharOperation.subarray(contents,
										end, lineEnd));
							}
						} else
						{
							results.append("No source");
						}
						results.append("]");
					}
				}
				if (this.showAccuracy)
				{
					results.append(" ");
					switch (match.getAccuracy())
					{
					case SearchMatch.A_ACCURATE:
						results.append("EXACT_MATCH");
						break;
					case SearchMatch.A_INACCURATE:
						results.append("POTENTIAL_MATCH");
						break;
					}
				}
				if (this.showInsideDoc)
				{
					results.append(" ");
					if (match.isInsideDocComment())
					{
						results.append("INSIDE_JAVADOC");
					} else
					{
						results.append("OUTSIDE_JAVADOC");
					}
				}
			} catch (JavaModelException e)
			{
				results.append("\n");
				results.append(e.toString());
			}
		}

		private void append(IField field) throws JavaModelException
		{
			append(field.getDeclaringType());
			results.append(".");
			results.append(field.getElementName());
		}

		private void append(IInitializer initializer) throws JavaModelException
		{
			append(initializer.getDeclaringType());
			results.append(".");
			if (Flags.isStatic(initializer.getFlags()))
			{
				results.append("static ");
			}
			results.append("{}");
		}
//{ObjectTeams: formatting of method mappings
		// 'path/to/DeclaringClass.java package.EnclosingType$InnerType roleMethod() -> baseMethod() [context]'
		// Didn't test mappings with full signatures, yet
		private void append(IMethodMapping mapping) throws JavaModelException
		{
			append(mapping.getDeclaringType());
		    results.append(" ").append(mapping.getElementName());
		}
//carp}
		private void append(IMethod method) throws JavaModelException
		{
			if (!method.isConstructor())
			{
				results.append(Signature.toString(method.getReturnType()));
				results.append(" ");
			}
			append(method.getDeclaringType());
			if (!method.isConstructor())
			{
				results.append(".");
				results.append(method.getElementName());
			}
			results.append("(");
			String[] parameters = method.getParameterTypes();
			for (int i = 0; i < parameters.length; i++)
			{
				results.append(Signature.toString(parameters[i]));
				if (i < parameters.length - 1)
				{
					results.append(", ");
				}
			}
			results.append(")");
		}

		private void append(IPackageFragment pkg)
		{
			results.append(pkg.getElementName());
		}

		private void append(IType type) throws JavaModelException
		{
			IJavaElement parent = type.getParent();
			boolean isLocal = false;
			switch (parent.getElementType())
			{
			case IJavaElement.COMPILATION_UNIT:
				IPackageFragment pkg = type.getPackageFragment();
				append(pkg);
				if (!pkg.getElementName().equals(
						IPackageFragment.DEFAULT_PACKAGE_NAME))
				{
					results.append(".");
				}
				break;
			case IJavaElement.CLASS_FILE:
				IType declaringType = type.getDeclaringType();
				if (declaringType != null)
				{
					append(type.getDeclaringType());
					results.append("$");
				} else
				{
					pkg = type.getPackageFragment();
					append(pkg);
					if (!pkg.getElementName().equals(
							IPackageFragment.DEFAULT_PACKAGE_NAME))
					{
						results.append(".");
					}
				}
				break;
			case IJavaElement.TYPE:
				append((IType) parent);
				results.append("$");
				break;
			case IJavaElement.FIELD:
				append((IField) parent);
				isLocal = true;
				break;
			case IJavaElement.INITIALIZER:
				append((IInitializer) parent);
				isLocal = true;
				break;
			case IJavaElement.METHOD:
				append((IMethod) parent);
				isLocal = true;
				break;
			}
			if (isLocal)
			{
				results.append(":");
			}
			String typeName = type.getElementName();
			if (typeName.length() == 0)
			{
				results.append("<anonymous>");
			} else
			{
				results.append(typeName);
			}
			if (isLocal)
			{
				results.append("#");
				// km: moved one level down from JavaElement to SourceRefElement
				if(type instanceof SourceRefElement)
					results.append(((SourceRefElement) type).occurrenceCount);
				else
					results.append("<element is not SourceRefElement>");
			}
		}

		public String toString()
		{
			return results.toString();
		}
	}

	public OTJavaSearchTestBase(String name)
	{
		super(name);
	}

	/**
	 * returns the OTJavaSearch project scope
	 */
	IJavaSearchScope getJavaSearchScopeFromProject()
	{
		return SearchEngine
				.createJavaSearchScope(new IJavaProject[] { getJavaProject("OTJavaSearch") });
	}
	
	IJavaSearchScope getJavaSearchScopeFromTypes(IType[] types)
	{
		return SearchEngine
				.createJavaSearchScope(types);
	}
	
	IJavaSearchScope getJavaSearchScopeFromPackage(String packageName)
	{
		IPackageFragment packageFragment = null;
		try
		{
			packageFragment = getPackageFragment(getTestProjectDir(),"src", packageName);
		} 
		catch (JavaModelException e)
		{
			return null;
		}
		
		return SearchEngine
				.createJavaSearchScope(new IPackageFragment[]{packageFragment});
	}
	
	IJavaSearchScope getJavaSearchScopeFromPackages(String[] packageNames)
	{
		IPackageFragment[] packageFragments = new IPackageFragment[packageNames.length];

		for (int idx = 0; idx < packageNames.length; idx++)
		{
			try
			{
				String packageName = packageNames[idx];
				IPackageFragment packageFragment = getPackageFragment(getTestProjectDir(),"src", packageName);
				packageFragments[idx] = packageFragment;
			} 
			catch (JavaModelException e)
			{
				return null;
			}
		}
		return SearchEngine
				.createJavaSearchScope(packageFragments);
	}

	public void setUpSuite() throws Exception
	{
		setTestProjectDir("OTJavaSearch");
		super.setUpSuite();
	}
	
	protected void setUp() throws Exception
    {
        super.setUp();
    }

	public void tearDownSuite() throws Exception
	{
		deleteProject("OTJavaSearch");

		super.tearDownSuite();
	}

	public static Test suite()
	{
		return new Suite(OTJavaSearchTestBase.class);
	}

	protected void search(SearchPattern searchPattern, IJavaSearchScope scope,
			SearchRequestor requestor) throws CoreException
	{
		new SearchEngine().search(searchPattern,
				new SearchParticipant[] { SearchEngine
						.getDefaultSearchParticipant() }, scope, requestor,
				null);
	}

	protected void searchDeclarationsOfAccessedFields(
			IJavaElement enclosingElement, SearchRequestor requestor)
			throws JavaModelException
	{
		new SearchEngine().searchDeclarationsOfAccessedFields(enclosingElement,
				requestor, null);
	}

	protected void searchDeclarationsOfReferencedTypes(
			IJavaElement enclosingElement, SearchRequestor requestor)
			throws JavaModelException
	{
		new SearchEngine().searchDeclarationsOfReferencedTypes(
				enclosingElement, requestor, null);
	}

	protected void searchDeclarationsOfSentMessages(
			IJavaElement enclosingElement, SearchRequestor requestor)
			throws JavaModelException
	{
		new SearchEngine().searchDeclarationsOfSentMessages(enclosingElement,
				requestor, null);
	}

}


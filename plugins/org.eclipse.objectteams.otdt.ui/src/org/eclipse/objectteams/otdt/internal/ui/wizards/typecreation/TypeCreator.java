/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TypeCreator.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedConstructorsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedMethodsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.TokenScanner;
import org.eclipse.jdt.internal.corext.refactoring.StubTypeContext;
import org.eclipse.jdt.internal.corext.refactoring.TypeContextChecker;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContext;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaTypeCompletionProcessor;
import org.eclipse.jdt.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.ui.OTDTUIPluginConstants;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Individual method copied from {@link org.eclipse.jdt.internal.corext.codemanipulation.StubUtility#getCompilationUnitContent(ICompilationUnit, String, String, String, String)}
 *
 * @author kaschja
 */
public abstract class TypeCreator
{

    private TypeInfo _typeInfo;
    
	private IType  _createdType;
	private String _defaultSupertypeName;

	private StubTypeContext fSuperClassStubTypeContext;
	private StubTypeContext fSuperInterfaceStubTypeContext; // FIXME(SH): use it!?! (see writeSuperClass / writeSuperInterfaces)

	
	public TypeCreator()
	{
		_defaultSupertypeName = createDefaultSupertypeName();
	}	
	
	
	public void setTypeInfo(TypeInfo typeInfo)
	{
	    _typeInfo = typeInfo;
	}	

	protected TypeInfo getTypeInfo()
	{
	    return _typeInfo;
	}
	
	
	/**
	 * Hook method. Gets called by Constructor.
	 */
	protected abstract String createDefaultSupertypeName();
	
	protected String getDefaultSupertypeName()
	{
		return _defaultSupertypeName;	
	}	
	
	private void setCreatedType(IType createdType)
	{
		_createdType = createdType;
	}
	
	public IType createType(IProgressMonitor monitor) throws CoreException, InterruptedException
	{
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}

		monitor.beginTask(NewWizardMessages.NewTypeWizardPage_operationdesc, 10); 
		
		ICompilationUnit createdWorkingCopy= null;
		
		try
		{
			validateTypeCreation();

			IPackageFragment pack = getPackageFragment();
			
			monitor.worked(1);

			IType createdType;
			ImportsManager imports;
			int indent= 0;

			String lineDelimiter= null;	
			boolean needsSave = false;
			
			Set<String> existingImports;
			
			if (!_typeInfo.isInlineType()) 
			{
				lineDelimiter= StubUtility.getLineDelimiterUsed(pack.getJavaProject());
										
				ICompilationUnit parentCU= pack.createCompilationUnit(_typeInfo.getTypeName() + ".java", "", false, new SubProgressMonitor(monitor, 2)); //$NON-NLS-1$ //$NON-NLS-2$
				// create a working copy with a new owner
				needsSave= true;
				parentCU.becomeWorkingCopy(new SubProgressMonitor(monitor, 1)); // cu is now a (primary) working copy
				createdWorkingCopy= parentCU;
				
				IBuffer buffer= parentCU.getBuffer();
				
				String cuContent= constructCUContent(parentCU, constructSimpleTypeStub(), lineDelimiter);
				buffer.setContents(cuContent);

				CompilationUnit astRoot= createASTForImports(parentCU);
				existingImports= getExistingImports(astRoot);
							
				imports= new ImportsManager(astRoot);

				// add an import that will be removed again. Having this import solves 14661
				imports.addImport(JavaModelUtil.concatenateName(pack.getElementName(), _typeInfo.getTypeName()));
				
				String typeContent= constructTypeStub(parentCU, imports, lineDelimiter);
				
				AbstractTypeDeclaration typeNode= (AbstractTypeDeclaration) astRoot.types().get(0);
				int start= ((ASTNode) typeNode.modifiers().get(0)).getStartPosition();
				int end= typeNode.getStartPosition() + typeNode.getLength();
				
				buffer.replace(start, end - start, typeContent);
				
				createdType= parentCU.getType(_typeInfo.getTypeName());
//{OTDTUI: help the compiler by saving the unit
//				createdWorkingCopy.commitWorkingCopy(true, monitor);
//				createdWorkingCopy.save(monitor, true);
//carp}
			}
			else
			{
				IType enclosingType = getEnclosingType();

				ICompilationUnit parentCU = enclosingType.getCompilationUnit();

				needsSave= !parentCU.isWorkingCopy();
				parentCU.becomeWorkingCopy(new SubProgressMonitor(monitor, 1)); // cu is now for sure (primary) a working copy
				createdWorkingCopy= parentCU;
				
				CompilationUnit astRoot= createASTForImports(parentCU);
				imports= new ImportsManager(astRoot);
				existingImports= getExistingImports(astRoot);

				// add imports that will be removed again. Having the imports solves 14661
				IType[] topLevelTypes= parentCU.getTypes();
				for (int i= 0; i < topLevelTypes.length; i++) {
					imports.addImport(topLevelTypes[i].getFullyQualifiedName('.'));
				}
				
				lineDelimiter= StubUtility.getLineDelimiterUsed(enclosingType);
				StringBuffer content= new StringBuffer();

				if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.CODEGEN_ADD_COMMENTS)) 
				{
					String comment= getTypeComment(parentCU, lineDelimiter);
					if (comment != null) 
					{
						content.append(comment);
						content.append(lineDelimiter);
					}
				}
				content.append(constructTypeStub(parentCU, imports, lineDelimiter));
				IJavaElement[] elems= enclosingType.getChildren();
				IJavaElement sibling= elems.length > 0 ? elems[0] : null;
				
//			    try
//			    {
				createdType = enclosingType.createType(
								content.toString(), 
                                sibling, 
								false, 
								new SubProgressMonitor(monitor, 2));	
//			    }
//			    catch (Exception ex)
//			    {	
//			    	OTDTUIPlugin.getExceptionHandler().logException(ex);
//			    }
			
				indent = StubUtility.getIndentUsed(enclosingType) + 1;
			}
		
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			
		
			// add imports for superclass/interfaces, so types can be resolved correctly
			
			ICompilationUnit cu= createdType.getCompilationUnit();	
			
			imports.create(false, new SubProgressMonitor(monitor, 1));
				
			JavaModelUtil.reconcile(cu);
			
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			
			// set up again
			CompilationUnit astRoot= createASTForImports(imports.getCompilationUnit());
			imports= new ImportsManager(astRoot);
			
			createTypeMembers(createdType, imports, new SubProgressMonitor(monitor, 1));
	
			// add imports
			imports.create(false, new SubProgressMonitor(monitor, 1));
			
			removeUnusedImports(cu, existingImports, false);
			
			JavaModelUtil.reconcile(cu);
			
			ISourceRange range= createdType.getSourceRange();
			
			IBuffer buf= cu.getBuffer();
			String originalContent= buf.getText(range.getOffset(), range.getLength());
			
			String formattedContent= CodeFormatterUtil.format(CodeFormatter.K_CLASS_BODY_DECLARATIONS, originalContent, indent, lineDelimiter, pack.getJavaProject());
			formattedContent= Strings.trimLeadingTabsAndSpaces(formattedContent);
			buf.replace(range.getOffset(), range.getLength(), formattedContent);
			
			
			if (!_typeInfo.isInlineType()) {
				String fileComment= getFileComment(cu);
				if (fileComment != null && fileComment.length() > 0) {
					buf.replace(0, 0, fileComment + lineDelimiter);
				}
			}

			if (needsSave) {
				cu.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
			} else {
				monitor.worked(1);
			}
			

			setCreatedType(createdType);			
			
//			if (createdWorkingCopy != null) {
//				fCreatedType= (IType) createdType.getPrimaryElement();
//			} else {
//				fCreatedType= createdType;
//			}
		}
		finally 
		{
			if (createdWorkingCopy != null) 
			{
				createdWorkingCopy.discardWorkingCopy();
			}
			monitor.done();
		}
		
		return _createdType;
	}	
	
	private CompilationUnit createASTForImports(ICompilationUnit cu) {
		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		parser.setResolveBindings(false);
		parser.setFocalPosition(0);
		return (CompilationUnit) parser.createAST(null);
	}
	
	
	private Set<String> getExistingImports(CompilationUnit root) {
		List imports= root.imports();
		Set<String> res= new HashSet<String>(imports.size());
		for (int i= 0; i < imports.size(); i++) {
			res.add(ASTNodes.asString((ImportDeclaration) imports.get(i)));
		}
		return res;
	}
	
	private boolean isValidComment(String template) {
		IScanner scanner= ToolFactory.createScanner(true, false, false, false);
		scanner.setSource(template.toCharArray());
		try {
			int next= scanner.getNextToken();
			while (TokenScanner.isComment(next)) {
				next= scanner.getNextToken();
			}
			return next == ITerminalSymbols.TokenNameEOF;
		} catch (InvalidInputException e) {
		}
		return false;
	}
	
	/**
	 * @deprecated Instead of file templates, the new type code template
	 * specifies the stub for a compilation unit.
	 */		
	protected String getFileComment(ICompilationUnit parentCU) {
		return null;
	}
	
	/**
	 * Hook method that gets called from <code>createType</code> to retrieve 
	 * a file comment. This default implementation returns the content of the 
	 * 'file comment' template or <code>null</code> if no comment should be created.
	 * 
	 * @param parentCU the parent compilation unit
	 * @param lineDelimiter the line delimiter to use
	 * @return the file comment or <code>null</code> if a file comment 
	 * is not desired
	 * @throws CoreException 
     *
     * @since 3.1
	 */		
	protected String getFileComment(ICompilationUnit parentCU, String lineDelimiter) throws CoreException {
		if (true /*isAddComments()*/) {
			return CodeGeneration.getFileComment(parentCU, lineDelimiter);
		}
		return null;
		
	}
	
	/**
	 * Hook method that gets called from <code>createType</code> to retrieve 
	 * a type comment. This default implementation returns the content of the 
	 * 'type comment' template.
	 * 
	 * @param parentCU the parent compilation unit
	 * @param lineDelimiter the line delimiter to use
	 * @return the type comment or <code>null</code> if a type comment 
	 * is not desired
     *
     * @since 3.0
	 */		
	protected String getTypeComment(ICompilationUnit parentCU, String lineDelimiter) {
		try {
			StringBuffer typeName= new StringBuffer();
//			if (isEnclosingTypeSelected()) {
//				typeName.append(JavaModelUtil.getTypeQualifiedName(getEnclosingType(page))).append('.');
//			}
			typeName.append(_typeInfo.getTypeName());
			String comment= CodeGeneration.getTypeComment(parentCU, typeName.toString(), lineDelimiter);
			if (comment != null && isValidComment(comment)) {
				return comment;
			}
		} catch (CoreException e) {
			JavaPlugin.log(e);
		}
		return null;
	}
	
	private void removeUnusedImports(ICompilationUnit cu, Set<String> existingImports, boolean needsSave) throws CoreException {
		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		parser.setResolveBindings(true);

		CompilationUnit root= (CompilationUnit) parser.createAST(null);
		if (root.getProblems().length == 0) {
			return;
		}
		
		List importsDecls= root.imports();
		if (importsDecls.isEmpty()) {
			return;
		}
		ImportsManager imports= new ImportsManager(root);
		
		int importsEnd= ASTNodes.getExclusiveEnd((ASTNode) importsDecls.get(importsDecls.size() - 1));
		IProblem[] problems= root.getProblems();
		for (int i= 0; i < problems.length; i++) {
			IProblem curr= problems[i];
			if (curr.getSourceEnd() < importsEnd) {
				int id= curr.getID();
				if (id == IProblem.UnusedImport || id == IProblem.NotVisibleType) { // not visible problems hide unused -> remove both
					int pos= curr.getSourceStart();
					for (int k= 0; k < importsDecls.size(); k++) {
						ImportDeclaration decl= (ImportDeclaration) importsDecls.get(k);
						if (decl.getStartPosition() <= pos && pos < decl.getStartPosition() + decl.getLength()) {
							if (existingImports.isEmpty() || !existingImports.contains(ASTNodes.asString(decl))) {
								String name= decl.getName().getFullyQualifiedName();
								if (decl.isOnDemand()) {
									name += ".*"; //$NON-NLS-1$
								}
								if (decl.isStatic()) {
									imports.removeStaticImport(name);
								} else {
									imports.removeImport(name);
								}
							}
							break;
						}
					}
				}
			}
		}
		imports.create(needsSave, null);
	}


	private IType getEnclosingType() throws CoreException
    {
    	IType enclosingType = null;
    	
        if (_typeInfo.getEnclosingTypeName().trim().length() != 0) 
        {
            try 
            {
        	    enclosingType = _typeInfo.getPkgFragmentRoot().getJavaProject().findType(_typeInfo.getEnclosingTypeName());
        	    
        	    if ((enclosingType == null) 
        	    		|| (enclosingType.getCompilationUnit() == null))
        	    {
					throw new Exception("The enclosing type " //$NON-NLS-1$
					                    + _typeInfo.getEnclosingTypeName()
					                    + "or its compilation unit does not exist."); //$NON-NLS-1$
        	    }
        	    
        	    if (!JavaModelUtil.isEditable(enclosingType.getCompilationUnit()))
        	    {
                    throw new Exception("The compilation unit of the enclosing type " //$NON-NLS-1$
                                        + _typeInfo.getEnclosingTypeName()
                                        + " is not editable!");						 //$NON-NLS-1$
        	    }
            }
        	catch (Exception ex) 
        	{        		
				throw new CoreException(new Status(IStatus.ERROR,
												   OTDTUIPluginConstants.UIPLUGIN_ID, 
												   IStatus.OK, 
												   ex.getMessage(),
												   null));        			    
        	}                    				
        }
        return enclosingType;
    }

    private IPackageFragment getPackageFragment()
        throws JavaModelException, CoreException
    {
    	
    	IPackageFragment pkgFragment = null;
    	IType enclosingType = getEnclosingType();
    	
    	if (enclosingType == null)
    	{
    		pkgFragment = _typeInfo.getPkgFragment();
			if (pkgFragment == null) 
			{
				pkgFragment = _typeInfo.getPkgFragmentRoot().getPackageFragment(""); //$NON-NLS-1$
			}    		
    	}
    	else
    	{
    		if (_typeInfo.isInlineType())
    		{
    			pkgFragment = enclosingType.getPackageFragment();
    		}
    		else //external defined role class (= role class with its own file)
    		{
    			String qualifiedEnclosingTypeName = getEnclosingType().getFullyQualifiedName('.');
				
    			pkgFragment = _typeInfo.getPkgFragmentRoot().getPackageFragment(qualifiedEnclosingTypeName);
    		}

    	}

		if (!pkgFragment.exists()) 
		{
			pkgFragment = _typeInfo.getPkgFragmentRoot().createPackageFragment(pkgFragment.getElementName(), true, null);
		}					    			

		return pkgFragment;
    }
	
	
	protected void validateTypeCreation()
		throws CoreException
	{
		if (_typeInfo.isInlineType() && (_typeInfo.getEnclosingTypeName().trim().length() == 0))
		{			
			throw new CoreException(new Status(IStatus.ERROR,
											   OTDTUIPluginConstants.UIPLUGIN_ID, 
											   IStatus.OK, 
											   "The class " + _typeInfo.getTypeName()  //$NON-NLS-1$
											   + " is declared to be an inner class" //$NON-NLS-1$
											   + " but fails to specify its enclosing type.", //$NON-NLS-1$
											   null));
		}		
	}

	private String constructSimpleTypeStub() {
		StringBuffer buf= new StringBuffer("public class "); //$NON-NLS-1$
		buf.append(_typeInfo.getTypeName());
		buf.append("{ }"); //$NON-NLS-1$
		return buf.toString();
	}
	
	/*
	 * Called from createType to construct the source for this type
	 */
    private String constructTypeStub(ICompilationUnit parentCU, ImportsManager imports, String lineDelimiter) throws CoreException	 
	{	
		StringBuffer buf= new StringBuffer();
			
		buf.append(Flags.toString(_typeInfo.getModifiers()));
		
		if (_typeInfo.getModifiers() != 0) 
		{
			buf.append(' ');
		}
		
		buf.append("class "); //$NON-NLS-1$
				
		buf.append(_typeInfo.getTypeName());
				
		writeInheritanceRelations(imports, buf);

		buf.append(" {").append(lineDelimiter); //$NON-NLS-1$
		String typeBody= CodeGeneration.getTypeBody(CodeGeneration.CLASS_BODY_TEMPLATE_ID, parentCU, _typeInfo.getTypeName(), lineDelimiter);
		if (typeBody != null) {
			buf.append(typeBody);
		} else {
			buf.append(lineDelimiter);
		}
		buf.append('}').append(lineDelimiter);

		return buf.toString();
	}
	



    protected void writeInheritanceRelations(ImportsManager imports, StringBuffer buf) throws CoreException
    {
		writeSuperClass(buf, imports);    	

		writeSuperInterfaces(buf, imports);	        
    }

	private void writeSuperClass(StringBuffer buf, ImportsManager imports) throws CoreException {
		String superclass= _typeInfo.getSuperClassName();
		if (superclass.length() > 0 && !"java.lang.Object".equals(superclass) //$NON-NLS-1$
//{ObjectTeams: also ignore default supertype (TPX-430)	
			&& ! String.valueOf(IOTConstants.STR_ORG_OBJECTTEAMS_TEAM).equals(superclass)
// km}
		
		) {
			buf.append(" extends "); //$NON-NLS-1$
			
			ITypeBinding binding= TypeContextChecker.resolveSuperClass(superclass, _typeInfo.getCurrentType(), getSuperClassStubTypeContext());
			if (binding != null) {
				buf.append(imports.addImport(binding));
			} else {
				buf.append(imports.addImport(superclass));
			}
		}
	}
	
	private StubTypeContext getSuperClassStubTypeContext() throws CoreException {
		if (fSuperClassStubTypeContext == null) {
			String typeName;
			if (_typeInfo.getCurrentType() != null) {
				typeName= _typeInfo.getTypeName();
			} else {
				typeName= JavaTypeCompletionProcessor.DUMMY_CLASS_NAME;
			}
			fSuperClassStubTypeContext= TypeContextChecker.createSuperClassStubTypeContext(typeName, getEnclosingType(), getPackageFragment());
		}
		return fSuperClassStubTypeContext;
	
	
	}


	private void writeSuperInterfaces(StringBuffer buf, ImportsManager imports) throws CoreException 
	{
		List<String> interfaces= _typeInfo.getSuperInterfacesNames();
		int last= interfaces.size() - 1;
		if (last >= 0) {
			buf.append(" implements "); //$NON-NLS-1$

			String[] intfs= interfaces.toArray(new String[interfaces.size()]);
			ITypeBinding[] bindings;
			IType currentType = _typeInfo.getCurrentType();
			if (currentType != null) {
				bindings= TypeContextChecker.resolveSuperInterfaces(intfs, currentType, getSuperInterfacesStubTypeContext());
			} else {
				bindings= new ITypeBinding[intfs.length];
			}
			for (int i= 0; i <= last; i++) {
				ITypeBinding binding= bindings[i];
				if (binding != null) {
					buf.append(imports.addImport(binding));
				} else {
					buf.append(imports.addImport(intfs[i]));
				}
				if (i < last) {
					buf.append(',');
				}
			}
		}
	}

	
	private StubTypeContext getSuperInterfacesStubTypeContext() throws CoreException {
		if (fSuperInterfaceStubTypeContext == null) {
			String typeName;
			if (_typeInfo != null) {
				typeName= _typeInfo.getTypeName();
			} else {
				typeName= JavaTypeCompletionProcessor.DUMMY_CLASS_NAME;
			}
			fSuperInterfaceStubTypeContext= TypeContextChecker.createSuperInterfaceStubTypeContext(typeName, getEnclosingType(), getPackageFragment());
		}
		return fSuperInterfaceStubTypeContext;
	}
	
//{OTDTUI
	private String createCUHeaderFromScratch(IPackageFragment pack, String lineDelimiter)
	{
		StringBuffer buf= new StringBuffer();
		// external role classes never have a normal package, they reference their enclosing team as package
		if (_typeInfo.isRole())
		{
		    buf.append("team "); //$NON-NLS-1$
		    buf.append("package ").append(pack.getElementName()).append(';'); //$NON-NLS-1$
		}
		else
		{
			if (!pack.isDefaultPackage()) 
			{
				buf.append("package ").append(pack.getElementName()).append(';'); //$NON-NLS-1$
			}
		}
		buf.append(lineDelimiter).append(lineDelimiter);
		return buf.toString();
	}
//carp}
	
//{OTDTUI: OT_COPY_PASTE: replacement for StubUtility.getCompilationUnitContent()
	private String getCompilationUnitContent(ICompilationUnit cu, String fileComment, String typeComment, String typeContent, String lineDelimiter) throws CoreException {
//{OTDTUI: added team modifier
		IPackageFragment pack= (IPackageFragment) cu.getParent();
		String packageString = _typeInfo.isRole() ? "team package " : "package "; //$NON-NLS-1$ //$NON-NLS-2$
		String packDecl= pack.isDefaultPackage() ? "" : packageString + pack.getElementName() + ';'; //$NON-NLS-1$ 
//carp}

		Template template= getCodeTemplate(CodeTemplateContextType.NEWTYPE_ID, cu.getJavaProject());
		if (template == null) {
			return null;
		}
		
		IJavaProject project= cu.getJavaProject();
		CodeTemplateContext context= new CodeTemplateContext(template.getContextTypeId(), project, lineDelimiter);
		context.setCompilationUnitVariables(cu);
		context.setVariable(CodeTemplateContextType.PACKAGE_DECLARATION, packDecl);
		context.setVariable(CodeTemplateContextType.TYPE_COMMENT, typeComment != null ? typeComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.FILE_COMMENT, fileComment != null ? fileComment : ""); //$NON-NLS-1$
		context.setVariable(CodeTemplateContextType.TYPE_DECLARATION, typeContent);
		context.setVariable(CodeTemplateContextType.TYPENAME, JavaCore.removeJavaLikeExtension(cu.getElementName()));
		
		String[] fullLine= { CodeTemplateContextType.PACKAGE_DECLARATION, CodeTemplateContextType.FILE_COMMENT, CodeTemplateContextType.TYPE_COMMENT };
		return evaluateTemplate(context, template, fullLine);
	}		

	private static Template getCodeTemplate(String id, IJavaProject project) {
		if (project == null)
			return JavaPlugin.getDefault().getCodeTemplateStore().findTemplateById(id);
		ProjectTemplateStore projectStore= new ProjectTemplateStore(project.getProject());
		try {
			projectStore.load();
		} catch (IOException e) {
			JavaPlugin.log(e);
		}
		return projectStore.findTemplateById(id);
	}


	private static String evaluateTemplate(CodeTemplateContext context, Template template, String[] fullLineVariables) throws CoreException {
		TemplateBuffer buffer;
		try {
			buffer= context.evaluate(template);
			if (buffer == null)
				return null;
			String str= fixEmptyVariables(buffer, fullLineVariables);
			if (Strings.containsOnlyWhitespaces(str)) {
				return null;
			}
			return str;
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
	}

	// remove lines for empty variables
	private static String fixEmptyVariables(TemplateBuffer buffer, String[] variables) throws MalformedTreeException, BadLocationException {
		IDocument doc= new Document(buffer.getString());
		int nLines= doc.getNumberOfLines();
		MultiTextEdit edit= new MultiTextEdit();
		HashSet<Integer> removedLines= new HashSet<Integer>();
		for (int i= 0; i < variables.length; i++) {
			TemplateVariable position= findVariable(buffer, variables[i]); // look if Javadoc tags have to be added
			if (position == null || position.getLength() > 0) {
				continue;
			}
			int[] offsets= position.getOffsets();
			for (int k= 0; k < offsets.length; k++) {
				int line= doc.getLineOfOffset(offsets[k]);
				IRegion lineInfo= doc.getLineInformation(line);
				int offset= lineInfo.getOffset();
				String str= doc.get(offset, lineInfo.getLength());
				if (Strings.containsOnlyWhitespaces(str) && nLines > line + 1 && removedLines.add(new Integer(line))) {
					int nextStart= doc.getLineOffset(line + 1);
					edit.addChild(new DeleteEdit(offset, nextStart - offset));
				}
			}
		}
		edit.apply(doc, 0);
		return doc.get();
	}
	private static TemplateVariable findVariable(TemplateBuffer buffer, String variable) {
		TemplateVariable[] positions= buffer.getVariables();
		for (int i= 0; i < positions.length; i++) {
			TemplateVariable curr= positions[i];
			if (variable.equals(curr.getType())) {
				return curr;
			}
		}
		return null;		
	}	


//end OT_COPY_PASTE
//carp}
	
	/**
	 * Uses the New Java file template from the code template page to generate a
	 * compilation unit with the given type content.
	 * @param cu The new created compilation unit
	 * @param typeContent The content of the type, including signature and type
	 * body.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return String Returns the result of evaluating the new file template
	 * with the given type content.
	 * @throws CoreException
	 * @since 2.1
	 */
	protected String constructCUContent(ICompilationUnit cu, String typeContent, String lineDelimiter) throws CoreException {
		String fileComment= getFileComment(cu, lineDelimiter);
		String typeComment= getTypeComment(cu, lineDelimiter);
		IPackageFragment pack= (IPackageFragment) cu.getParent();
//		String content= CodeGeneration.getCompilationUnitContent(cu, fileComment, typeComment, typeContent, lineDelimiter);
//{OTDTUI: use own own code generation (team modifier!)
		String content= getCompilationUnitContent(cu, fileComment, typeComment, typeContent, lineDelimiter);
//carp}		
		if (content != null) {
			ASTParser parser= ASTParser.newParser(AST.JLS3);
//OTDTUI: set unit name and project for role-file handling in parser -- probably obsolete!
			parser.setUnitName(cu.getPath().toString());
//carp}
			
			parser.setProject(cu.getJavaProject());
			parser.setSource(content.toCharArray());
			CompilationUnit unit= (CompilationUnit) parser.createAST(null);
			if ((pack.isDefaultPackage() || unit.getPackage() != null) && !unit.types().isEmpty()) {
				return content;
			}
		}
		
//{OTDTUI: adapted to add the "team" package modifier
		System.out.println("TypeCreator: does this ever happen?");
		StringBuffer buf= new StringBuffer();
		buf.append (createCUHeaderFromScratch(pack, lineDelimiter));
//carp}
		if (typeComment != null) {
			buf.append(typeComment).append(lineDelimiter);
		}
		buf.append(typeContent);
		return buf.toString();
	}

    private void createTypeMembers(IType type, ImportsManager imports, IProgressMonitor monitor)
		throws CoreException
	{
		createInheritedMethods(type, imports, new SubProgressMonitor(monitor, 1));
		
		if(_typeInfo.isCreateMainMethod())
		{
			StringBuffer buf = new StringBuffer();
			buf.append("public static void main("); //$NON-NLS-1$
			buf.append(imports.addImport("java.lang.String")); //$NON-NLS-1$
			buf.append("[] args) {}"); //$NON-NLS-1$
			type.createMethod(buf.toString(), null, false, null);
		}
		
		if(monitor != null)
		{
			monitor.done();
		}
	}

/**
 * Copied from NewTypeWizardPage.createInheritedMethods
 * doXYZ Parameters replaced by is-Methods in _typeInfo
 */
	protected IMethod[] createInheritedMethods(IType type, ImportsManager imports, IProgressMonitor monitor)
        throws CoreException
	{
		final ICompilationUnit cu= type.getCompilationUnit();
		JavaModelUtil.reconcile(cu);
		IMethod[] typeMethods= type.getMethods();
		Set<String> handleIds= new HashSet<String>(typeMethods.length);
		for (int index= 0; index < typeMethods.length; index++)
			handleIds.add(typeMethods[index].getHandleIdentifier());
		ArrayList<IMethod> newMethods= new ArrayList<IMethod>();
		CodeGenerationSettings settings= JavaPreferencesSettings.getCodeGenerationSettings(type.getJavaProject());
// TODO: addComments has to be set in _typeInfo		
		settings.createComments= true;
		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setSource(cu);
		CompilationUnit unit= (CompilationUnit) parser.createAST(new SubProgressMonitor(monitor, 1));
		final ITypeBinding binding= ASTNodes.getTypeBinding(unit, type);
		if (binding != null) {
			if (_typeInfo.isCreateAbstractInheritedMethods()) {
				AddUnimplementedMethodsOperation operation= new AddUnimplementedMethodsOperation(unit, binding, null, -1, false, true, false);
// TODO: addComments (parameter) has to be set in _typeInfo			
				operation.setCreateComments(true);
				operation.run(monitor);
				createImports(imports, operation.getCreatedImports());
			}
			if (_typeInfo.isCreateConstructor()) {
				AddUnimplementedConstructorsOperation operation= new AddUnimplementedConstructorsOperation(unit, binding, null, -1, false, true, false);
// TODO: addComments (parameter) has to be set in _typeInfo			
				operation.setCreateComments(true);
				operation.run(monitor);
				createImports(imports, operation.getCreatedImports());
			}
		}
		JavaModelUtil.reconcile(cu);
		typeMethods= type.getMethods();
		for (int index= 0; index < typeMethods.length; index++)
			if (!handleIds.contains(typeMethods[index].getHandleIdentifier()))
				newMethods.add(typeMethods[index]);
		IMethod[] methods= new IMethod[newMethods.size()];
		newMethods.toArray(methods);
		return methods;
	}
	
	private void createImports(ImportsManager imports, String[] createdImports) {
		for (int index= 0; index < createdImports.length; index++)
			imports.addImport(createdImports[index]);
	}
	

	/**
	 * Class used in stub creation routines to add needed imports to a 
	 * compilation unit.
	 */
	public static class ImportsManager 
	{
 		ImportRewrite fImportsRewrite;
		
		/* package */ ImportsManager(CompilationUnit astRoot) throws CoreException {
			fImportsRewrite= StubUtility.createImportRewrite(astRoot, true);
		} 		
 		
		/* package */ ImportsManager(ICompilationUnit createdWorkingCopy) throws CoreException {
			fImportsRewrite= StubUtility.createImportRewrite(createdWorkingCopy, true);
		}

		/* package */ ICompilationUnit getCompilationUnit() {
			return fImportsRewrite.getCompilationUnit();
		}
				
		/**
		 * Adds a new import declaration that is sorted in the existing imports.
		 * If an import already exists or the import would conflict with an import
		 * of an other type with the same simple name, the import is not added.
		 * 
		 * @param qualifiedTypeName The fully qualified name of the type to import
		 * (dot separated).
		 * @return Returns the simple type name that can be used in the code or the
		 * fully qualified type name if an import conflict prevented the import.
		 */				
		public String addImport(String qualifiedTypeName) {
			return fImportsRewrite.addImport(qualifiedTypeName);
		}
		
		/**
		 * Adds a new import declaration that is sorted in the existing imports.
		 * If an import already exists or the import would conflict with an import
		 * of an other type with the same simple name, the import is not added.
		 * 
		 * @param typeBinding the binding of the type to import
		 * 
		 * @return Returns the simple type name that can be used in the code or the
		 * fully qualified type name if an import conflict prevented the import.
		 */				
		public String addImport(ITypeBinding typeBinding) {
			return fImportsRewrite.addImport(typeBinding);
		}
		
		/* package */ void create(boolean needsSave, IProgressMonitor monitor) throws CoreException {
			TextEdit edit= fImportsRewrite.rewriteImports(monitor);
			JavaModelUtil.applyEdit(fImportsRewrite.getCompilationUnit(), edit, needsSave, null);
		}
		
		/* package */ void removeImport(String qualifiedName) {
			fImportsRewrite.removeImport(qualifiedName);
		}
		
		/* package */ void removeStaticImport(String qualifiedName) {
			fImportsRewrite.removeStaticImport(qualifiedName);
		}
		
	}

}

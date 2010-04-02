package org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.extractcallin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BaseCallMessageSend;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTNodeCreator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.refactoring.structure.ImportRewriteUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.internal.core.AbstractCalloutMapping;
import org.eclipse.objectteams.otdt.internal.core.CalloutMapping;
import org.eclipse.objectteams.otdt.internal.refactoring.util.RefactoringUtil;

@SuppressWarnings("restriction")
public class ExtractCallinRefactoring extends Refactoring {

	private Map<ICompilationUnit, TextFileChange> fChanges = null;
	
	private boolean fDeleteBaseMethod;

	private CompilationUnit fRootBase;

	private AST fBaseAST;

	private ICompilationUnit fBaseCUnit;

	private ICompilationUnit fRoleCUnit;

	private CompilationUnit fRootRole;

	private AST fRoleAST;

	private IType fRoleType;

	private TextFileChange fBaseTextFileChange;

	private TextFileChange fRoleTextFileChange;

	private List<ICallinMapping> fBoundCallinMappings;

	private String fRoleMethodName;

	private IType fBaseType;

	private ASTRewrite fBaseRewrite;

	private int fMappingKind;
	
	private boolean fCopyBaseMethod;

	private IMethod fBaseMethod;

	private List<IRoleType> fBoundRoles;

	private IMethod fExtractedBaseMethod;

	private MethodDeclaration fBaseMethodDeclaration;

	private ASTRewrite fRoleRewrite;

	private ImportRewrite fRoleImportRewriter;

	private MethodInvocation fExtractedMethodInvocation;

	private ImportRewrite fBaseImportRewriter;
	
	public ExtractCallinRefactoring() {
		fCopyBaseMethod = true;
	}

	public ExtractCallinRefactoring(IMethod baseMethod) {
		this();
		fBaseMethod = baseMethod;
	}

	public ExtractCallinRefactoring(IMethod baseMethod, IType role, int mappingKind) {
		this(baseMethod);
		fRoleType = role;
		fMappingKind = mappingKind;
	}

	public List<IRoleType> getCandidateRoles() {
		return fBoundRoles;
	}
	
	public void setRoleType(IType role) {
		fRoleType = role;
	}
	
	public void setCopyBaseMethod(boolean copyBaseMethod) {
		fCopyBaseMethod = copyBaseMethod;
	}
	
	public void setMappingKind(int mappingKind) {
		fMappingKind = mappingKind;
	}
	
	public int getMappingKind() {
		return fMappingKind;
	}

	public void setChanges(Map<ICompilationUnit, TextFileChange> fChanges) {
		this.fChanges = fChanges;
	}

	public Map<ICompilationUnit, TextFileChange> getChanges() {
		return fChanges;
	}

	public IMethod getBaseMethod() {
		return fBaseMethod;
	}
	
	public boolean isCopyBaseMethod() {
		return fCopyBaseMethod;
	}

	public boolean isDeleteBaseMethod() {
		return fDeleteBaseMethod;
	}

	public void setDeleteBaseMethod(boolean deleteRoleMethod) {
		fDeleteBaseMethod = deleteRoleMethod;
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		RefactoringStatus status= new RefactoringStatus();
		try {
			monitor.beginTask("Checking preconditions...", 1);
			if (fBaseMethod == null) {
				status.merge(RefactoringStatus.createFatalErrorStatus("Method has not been specified."));
			} else if (!fBaseMethod.exists()) {
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Method ''{0}'' does not exist.", new Object[] { fBaseMethod
						.getElementName() })));
			}else if (fBaseMethod.isBinary()){
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Method ''{0}'' is declared in a binary class file.", new Object[] { fBaseMethod
						.getElementName() })));
			}else if (fBaseMethod.isReadOnly()){
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Method ''{0}'' is declared in a read-only class file.", new Object[] { fBaseMethod
						.getElementName() })));
			} else if (!fBaseMethod.getCompilationUnit().isStructureKnown()) {
					status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Compilation unit ''{0}'' contains compile errors.",
							new Object[] { fBaseMethod.getCompilationUnit().getElementName() })));
			} else if (Flags.isAbstract(fBaseMethod.getFlags())){
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Method ''{0}'' is abstract, cannot extract.", new Object[] { fBaseMethod
						.getElementName() })));
			} else if (fBaseMethod instanceof AbstractCalloutMapping){
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Method ''{0}'' is an callout method and cannot be extracted.", new Object[] { fBaseMethod
						.getElementName() })));
			}else{ 
					status.merge(initialize(monitor));
			}
		} finally {
			monitor.done();
		}
		return status;
	}	
	
	private RefactoringStatus initialize(IProgressMonitor monitor) {
		RefactoringStatus status = new RefactoringStatus();
		try {
				fRoleMethodName = fBaseMethod.getElementName();
				fBaseType = fBaseMethod.getDeclaringType();
				fBaseCUnit = fBaseType.getCompilationUnit();

				if (fRootBase == null) {
					fRootBase = RefactoringASTParser.parseWithASTProvider(fBaseCUnit, true, new SubProgressMonitor(monitor, 99));
				}
				fBaseImportRewriter = StubUtility.createImportRewrite(fRootBase, true);
				fBaseMethodDeclaration = RefactoringUtil.methodToDeclaration(fBaseMethod, fRootBase);
				
				fBaseAST = fRootBase.getAST();
				
				fBoundRoles = findCandidateRoles();
			if (fBoundRoles.size() == 0) {
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("The declaring type ''{0}'' is not bound by a role.",
						new Object[] { fBaseType.getElementName() })));
			}
			
			
			
		} catch (CoreException e) {
			status.merge(createCouldNotParseStatus());
		}
		if (status.hasFatalError()) {
			return status;
		}
		return status;
	}
	
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {

		RefactoringStatus status = new RefactoringStatus();

		if (fRoleType == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus("No target role selected."));
		}else if (!fRoleType.exists()) {
			status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Type ''{0}'' does not exist.", new Object[] { fRoleType
					.getElementName() })));
		}else if (fRoleType.isBinary()){
			status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Type ''{0}'' is declared in a binary class file.", new Object[] { fRoleType
					.getElementName() })));
		}else if (fRoleType.isReadOnly()){
			status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Type ''{0}'' is declared in a read-only file.", new Object[] { fRoleType
					.getElementName() })));
		}
		
		fRoleCUnit = fRoleType.getCompilationUnit();
		if (fRootRole == null) {
			fRootRole = RefactoringASTParser.parseWithASTProvider(fRoleCUnit, true, new SubProgressMonitor(pm, 99));
		}
		fRoleImportRewriter = StubUtility.createImportRewrite(fRootRole, true);
		fRoleAST = fRootRole.getAST();
		
		if(fDeleteBaseMethod && fMappingKind != ICallinMapping.KIND_REPLACE){
			//TODO check if base method can be deleted (no references expect the extracted method invocation)
		}
		
		status.merge(checkRoleMethodName());
		
		status.merge(checkCallinKind());

		return status;
	}

	private RefactoringStatus checkCallinKind() throws JavaModelException {
		if (fMappingKind == 0) {
			return RefactoringStatus.createFatalErrorStatus("Callin kind has not been specified.");
		}
		if (fMappingKind == ICallinMapping.KIND_BEFORE && !isExtractBeforeAvailable()) {
			return RefactoringStatus.createFatalErrorStatus("The first statement in the base method must be a method invocation to extract a before callin.");
		}
		if (fMappingKind == ICallinMapping.KIND_AFTER && !isExtractAfterAvailable()) {
			return RefactoringStatus.createFatalErrorStatus("The last statement in the base method must be a method invocation to extract an after callin.");
		}
		if (fMappingKind != ICallinMapping.KIND_REPLACE && fMappingKind != ICallinMapping.KIND_BEFORE && fMappingKind != ICallinMapping.KIND_AFTER) {
			return RefactoringStatus.createFatalErrorStatus("Invalid callin kind.");
		}
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		extractCallin();
		CompositeChange change = new CompositeChange(getName(), new TextFileChange[] { fBaseTextFileChange, fRoleTextFileChange });
		return change;
	}

	@Override
	public String getName() {
		return "Extract Callin";
	}
	
	@SuppressWarnings("unchecked")
	private void extractCallin() throws CoreException {
		fBaseRewrite = ASTRewrite.create(fBaseAST);
		fRoleRewrite = ASTRewrite.create(fRoleAST);

		MethodDeclaration baseMethodDeclaration = RefactoringUtil.methodToDeclaration(fBaseMethod, fRootBase);

		List<Statement> statements = baseMethodDeclaration.getBody().statements();

		fExtractedBaseMethod = null;

		// change the strategy for the different mapping kinds
		switch (fMappingKind) {
		case ICallinMapping.KIND_BEFORE:
			fExtractedMethodInvocation = (MethodInvocation) ((ExpressionStatement) statements.get(0)).getExpression();
			IMethodBinding methodBinding = fExtractedMethodInvocation.resolveMethodBinding();
			fExtractedBaseMethod = (IMethod) methodBinding.getJavaElement();
			
			fBaseRewrite.getListRewrite(baseMethodDeclaration.getBody(), Block.STATEMENTS_PROPERTY).remove(statements.get(0), null);
			// TODO check for unused imports
			break;
		case ICallinMapping.KIND_AFTER:
			fExtractedMethodInvocation = (MethodInvocation) ((ExpressionStatement) statements.get(statements.size() - 1)).getExpression();
			methodBinding = fExtractedMethodInvocation.resolveMethodBinding();
			fExtractedBaseMethod = (IMethod) methodBinding.getJavaElement();
			
			fBaseRewrite.getListRewrite(baseMethodDeclaration.getBody(), Block.STATEMENTS_PROPERTY).remove(statements.get(statements.size() - 1), null);
			// TODO check for unused imports
			break;
		case ICallinMapping.KIND_REPLACE:
			fExtractedBaseMethod = fBaseMethod;
			break;
		default:
			break;
		}

		MethodDeclaration extractedCallin = extractCallinMethod();
		
		insertMethodIntoRole(extractedCallin);
		
		CallinMappingDeclaration callinMapping = createMethodMapping();
		
		appendMethodMappingToRole(callinMapping);
		
		if(fDeleteBaseMethod && fMappingKind != ICallinMapping.KIND_REPLACE){
			MethodDeclaration extractedBaseMethodDecl = RefactoringUtil.methodToDeclaration(fExtractedBaseMethod, fRootBase);
			AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) typeToDeclaration(fBaseType, fRootBase);
			ChildListPropertyDescriptor descriptor = typeToBodyDeclarationProperty(fBaseType, fRootBase);
			fBaseRewrite.getListRewrite(declaration, descriptor).remove(extractedBaseMethodDecl, null);
		}
			
		// collect imports
		Set<IBinding> staticImports = new HashSet<IBinding>();
		Set<ITypeBinding> imports = new HashSet<ITypeBinding>();
		MethodDeclaration extractedCallinMethodDeclaration = RefactoringUtil.methodToDeclaration(fExtractedBaseMethod, fRootBase);
		ImportRewriteUtil.collectImports(fRoleType.getJavaProject(), extractedCallinMethodDeclaration, imports, staticImports, false);
		if(fMappingKind != ICallinMapping.KIND_REPLACE)
			ImportRewriteUtil.collectImports(fRoleType.getJavaProject(), fExtractedMethodInvocation, imports, staticImports, false);
		for (ITypeBinding typeBinding : imports) {
			fRoleImportRewriter.addImport(typeBinding);
		}
		for (IBinding binding : staticImports) {
			fRoleImportRewriter.addStaticImport(binding);
		}
		
		// create the text change for the base
		MultiTextEdit baseMultiEdit = new MultiTextEdit();
		baseMultiEdit.addChild(fBaseRewrite.rewriteAST());
		fBaseTextFileChange = new TextFileChange(fBaseCUnit.getElementName(), (IFile) fBaseCUnit.getResource());
		fBaseTextFileChange.setTextType("java");
		fBaseTextFileChange.setEdit(baseMultiEdit);
		
		// create the text change for the role
		MultiTextEdit roleMultiEdit = new MultiTextEdit();
		roleMultiEdit.addChild(fRoleRewrite.rewriteAST());
		fRoleTextFileChange = new TextFileChange(fRoleCUnit.getElementName(), (IFile) fRoleCUnit.getResource());
		fRoleTextFileChange.setTextType("java");
		fRoleTextFileChange.setEdit(roleMultiEdit);
		
		// Update imports
		if (fRoleImportRewriter.hasRecordedChanges()) {
			TextEdit edit = fRoleImportRewriter.rewriteImports(null);
			roleMultiEdit.addChild(edit);
			fRoleTextFileChange.addTextEditGroup(new TextEditGroup("Update Imports", new TextEdit[] { edit }));
		}
	}
	
	@SuppressWarnings("unchecked")
	private MethodDeclaration extractCallinMethod() throws JavaModelException {
		// copy the extracted callin to the role
		MethodDeclaration extractedCallinMethodDeclaration = RefactoringUtil.methodToDeclaration(fExtractedBaseMethod, fRootBase);
		MethodDeclaration copyOfExtractedCallin = (MethodDeclaration) ASTNode.copySubtree(fRoleAST, extractedCallinMethodDeclaration);
		copyOfExtractedCallin.getName().setIdentifier(fRoleMethodName);

		if (fMappingKind == ICallinMapping.KIND_REPLACE) {
			addCallinModifier(fRoleRewrite, copyOfExtractedCallin);
			// copyOfExtractedCallin.
			if(!fCopyBaseMethod){
				// remove all statments and add a base call
				Block body = fRoleAST.newBlock();
				BaseCallMessageSend baseCall = createBaseMethodInvocation(fRoleMethodName, fExtractedBaseMethod, fBaseMethod);
				body.statements().add(fRoleAST.newExpressionStatement(baseCall));
				copyOfExtractedCallin.setBody(body);
			}
		} else if (!Flags.isPrivate(fExtractedBaseMethod.getFlags())) {
			changeToPrivateVisibility(fRoleRewrite, copyOfExtractedCallin);
		}
		return copyOfExtractedCallin;
	}
	
	private BaseCallMessageSend createBaseMethodInvocation(String roleMethodName, IMethod roleMethod, IMethod baseMethod) throws JavaModelException {
		BaseCallMessageSend baseCall = fRoleAST.newBaseCallMessageSend();
		baseCall.setName(fRoleAST.newSimpleName(roleMethodName));
		copyInvocationParameters(baseCall, roleMethod);

		// if the role method doesn't declare the same number of parameters as
		// the base method, the parameters have to be appended
		int roleMethodParameterLength = roleMethod.getParameterNames().length;
		int baseMethodParameterLength = baseMethod.getParameterNames().length;
		if (roleMethodParameterLength < baseMethodParameterLength) {
			appendInvocationParameters(baseCall, baseMethod, baseMethodParameterLength - roleMethodParameterLength);
		}

		return baseCall;
	}
	
	static MethodSpec createMethodSpec(AST ast, ImportRewrite imports, IMethodBinding methodBinding, String[] argNames) {
		List<SingleVariableDeclaration> args = new ArrayList<SingleVariableDeclaration>();
		for (int i = 0; i < methodBinding.getParameterTypes().length; i++) {
			ITypeBinding paramType = methodBinding.getParameterTypes()[i];
			args.add(ASTNodeCreator.createArgument(ast, 0/* modifiers */, imports.addImport(paramType, ast), argNames[i], 0 /* extraDimensions */, null));
		}
		ITypeBinding providedReturnType = methodBinding.getReturnType();
		Type returnType = imports.addImport(providedReturnType, ast);
		return ASTNodeCreator.createMethodSpec(ast, methodBinding.getName(), returnType, args, true);
	}

	private void appendMethodMappingToRole(CallinMappingDeclaration callinMapping) throws JavaModelException {
		AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) typeToDeclaration(fRoleType, fRootRole);
		ChildListPropertyDescriptor descriptor = typeToBodyDeclarationProperty(fRoleType, fRootRole);
		fRoleRewrite.getListRewrite(declaration, descriptor).insertLast(callinMapping, null);
	}

	@SuppressWarnings("unchecked")
	private CallinMappingDeclaration createMethodMapping() throws JavaModelException {
		CallinMappingDeclaration mapping = fRoleAST.newCallinMappingDeclaration();

		Modifier callinModifier = createCallinModifier();

		mapping.setCallinModifier(callinModifier);
		
		IMethodBinding roleMethodBinding = RefactoringUtil.methodToDeclaration(fExtractedBaseMethod, fRootBase).resolveBinding();
		MethodSpec roleMethodSpec = createMethodSpec(fRoleAST, fRoleImportRewriter, roleMethodBinding, fExtractedBaseMethod.getParameterNames());
		roleMethodSpec.setName(fRoleAST.newSimpleName(fRoleMethodName));
		mapping.setRoleMappingElement(roleMethodSpec);
		
		IMethodBinding baseMethodBinding = RefactoringUtil.methodToDeclaration(fBaseMethod, fRootBase).resolveBinding();
		MethodSpec baseMethodSpec = createMethodSpec(fRoleAST, fRoleImportRewriter, baseMethodBinding, fBaseMethod.getParameterNames());
		mapping.getBaseMappingElements().add(baseMethodSpec);
		
		if (needsParameterMapping()) {
			// parameterMapping.
			List<SingleVariableDeclaration> parameters = roleMethodSpec.parameters();
			for (int i = 0; i < parameters.size(); i++) {
				SingleVariableDeclaration varDecl = parameters.get(i);
				ParameterMapping parameterMapping = fRoleAST.newParameterMapping();
				Expression expr = (Expression) ASTNode.copySubtree(fRoleAST, (Expression) fExtractedMethodInvocation.arguments().get(i));
				
				parameterMapping.setIdentifier(fRoleAST.newSimpleName(varDecl.getName().getIdentifier()));
				parameterMapping.setExpression(expr);
				parameterMapping.setDirection("<-");
				mapping.getParameterMappings().add(parameterMapping);
			}
		}
		return mapping;
	}

	private boolean needsParameterMapping() throws JavaModelException {
		if (fMappingKind == ICallinMapping.KIND_REPLACE) {
			return false;
		}
		if (fExtractedBaseMethod.getParameterNames().length == 0) {
			return false;
		}
		if (fExtractedMethodInvocation.arguments().size() > fBaseMethod.getParameterNames().length) {
			return true;
		}
		for (int i = 0; i < fExtractedMethodInvocation.arguments().size(); i++) {
			Expression expression = (Expression) fExtractedMethodInvocation.arguments().get(i);
			if (expression instanceof SimpleName) {
				if (!((SimpleName) expression).getIdentifier().equals(fBaseMethod.getParameterNames()[i])) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	private Modifier createCallinModifier() {
		switch (fMappingKind) {
		case ICallinMapping.KIND_BEFORE:
			return fRoleAST.newModifier(ModifierKeyword.BEFORE_KEYWORD);
		case ICallinMapping.KIND_AFTER:
			return fRoleAST.newModifier(ModifierKeyword.AFTER_KEYWORD);
		case ICallinMapping.KIND_REPLACE:
			return fRoleAST.newModifier(ModifierKeyword.REPLACE_KEYWORD);
		default:
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private void addCallinModifier(ASTRewrite astRewrite, MethodDeclaration methodDeclaration) {
		Modifier callinModifier = fRoleAST.newModifier(ModifierKeyword.CALLIN_KEYWORD);

		// add the callin modifier
		astRewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY).insertLast(callinModifier, null);

		// visibility modifiers should be removed because callin methods don't
		// declare a visibility (OTJLD 4.2(d))
		List<IExtendedModifier> modifiers = methodDeclaration.modifiers();
		for (IExtendedModifier extendedModifier : modifiers) {
			if (extendedModifier instanceof Modifier) {
				Modifier modifier = (Modifier) extendedModifier;
				if (Modifier.isPublic(modifier.getKeyword().toFlagValue()) || Modifier.isProtected(modifier.getKeyword().toFlagValue())
						|| Modifier.isPrivate(modifier.getKeyword().toFlagValue())) {
					astRewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY).remove(modifier, null);
					break;
				}
			}
		}
	}

	private void insertMethodIntoRole(MethodDeclaration methodDeclaration) throws JavaModelException {
		AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) typeToDeclaration(fRoleType, fRootRole);
		ChildListPropertyDescriptor descriptor = typeToBodyDeclarationProperty(fRoleType, fRootRole);
		fRoleRewrite.getListRewrite(declaration, descriptor).insertLast(methodDeclaration, null);
	}

	/**
	 * Replaces <code>public</code> and <code>protected</code> by a
	 * <code>private</code> modifier. If the method has default visibility
	 * <code>private</code> modifier will be prepended to the modifier list.
	 * 
	 * @param rewrite
	 *            the rewrite that notes the changes
	 * @param methodDeclaration
	 *            the method declaration to be changed
	 */
	@SuppressWarnings("unchecked")
	private void changeToPrivateVisibility(ASTRewrite astRewrite, MethodDeclaration methodDeclaration) {
		Modifier privateVisibility = methodDeclaration.getAST().newModifier(ModifierKeyword.PRIVATE_KEYWORD);
		
		List<IExtendedModifier> modifiers = methodDeclaration.modifiers();
		for (IExtendedModifier extendedModifier : modifiers) {
			if (extendedModifier instanceof Modifier) {
				Modifier modifier = (Modifier) extendedModifier;
				if (Modifier.isPublic(modifier.getKeyword().toFlagValue())) {
					astRewrite.replace(modifier, privateVisibility, null);
					return;
				}
				if (Modifier.isProtected(modifier.getKeyword().toFlagValue())) {
					astRewrite.replace(modifier, privateVisibility, null);
					return;
				}
				if (Modifier.isPrivate(modifier.getKeyword().toFlagValue())) {
					// don't replace private modifiers or create a 2nd private
					// modifier
					return;
				}
			}
		}
		// no visibility modifier was found => default visibility will be
		// reduced to private
		astRewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY).insertFirst(privateVisibility, null);
	}
	
	@SuppressWarnings("unchecked")
	private void copyInvocationParameters(BaseCallMessageSend baseCall, IMethod method) throws JavaModelException {
		String[] names = method.getParameterNames();
		for (String element : names)
			baseCall.getArguments().add(fRoleAST.newSimpleName(element));
	}

	/**
	 * Appends invocation parameter names from the given method from the given
	 * offset. This method is used to extend base method calls if the role
	 * method declares fewer parameters than the base method.
	 * 
	 * @param baseCall
	 *            the base call invocation to receive the parameters
	 * @param method
	 *            the method that declares the parameters
	 * @param offset
	 *            the offset to begin to copy
	 * @throws JavaModelException
	 */
	@SuppressWarnings("unchecked")
	private void appendInvocationParameters(BaseCallMessageSend baseCall, IMethod method, int offset) throws JavaModelException {
		String[] names = method.getParameterNames();

		for (int i = offset; i < names.length; i++) {
			String name = names[i];
			baseCall.getArguments().add(fBaseAST.newSimpleName(name));
		}

	}

	@SuppressWarnings("rawtypes")
	private ASTNode getParent(ASTNode node, Class parentClass) {
		do {
			node = node.getParent();
		} while (node != null && !parentClass.isInstance(node));
		return node;
	}

	private ASTNode typeToDeclaration(IType type, CompilationUnit node) throws JavaModelException {
		Name result = (Name) NodeFinder.perform(node, type.getNameRange());
		if (type.isAnonymous())
			return getParent(result, AnonymousClassDeclaration.class);
		return getParent(result, AbstractTypeDeclaration.class);
	}
	
	private ChildListPropertyDescriptor typeToBodyDeclarationProperty(IType type, CompilationUnit node) throws JavaModelException {
		ASTNode result = typeToDeclaration(type, node);
		if (result instanceof AbstractTypeDeclaration)
			return ((AbstractTypeDeclaration) result).getBodyDeclarationsProperty();
		else if (result instanceof AnonymousClassDeclaration)
			return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;

		Assert.isTrue(false);
		return null;
	}
	
	private ArrayList<IRoleType> findCandidateRoles() throws CoreException {
		ArrayList<IRoleType> boundRoles = RefactoringUtil.getAllRolesForBase(fBaseType);
		List<IRoleType> rolesToRemove = new ArrayList<IRoleType>();
		for (IRoleType boundRole : boundRoles) {
			if(!boundRole.exists() || boundRole.isReadOnly() || boundRole.isBinary()){
				rolesToRemove.add(boundRole);
			}
		}
		boundRoles.removeAll(rolesToRemove);
		return boundRoles;
	}

	public IMethod[] getBoundBaseMethods() throws JavaModelException {
		List<IMethod> boundBaseMethods = new ArrayList<IMethod>();
		for (ICallinMapping mapping : fBoundCallinMappings) {
			boundBaseMethods.addAll(Arrays.asList(mapping.getBoundBaseMethods()));
		}
		return boundBaseMethods.toArray(new IMethod[boundBaseMethods.size()]);
	}

	public List<ICallinMapping> getBoundCallinMappings() {
		return fBoundCallinMappings;
	}

	public void setRoleMethodName(String name) {
		fRoleMethodName = name;
	}

	private RefactoringStatus checkIfMethodExists() {
		try {
			if (methodWithNameExists(fRoleType, fRoleMethodName)) {
				return RefactoringStatus.createErrorStatus(MessageFormat.format("A method with the same name already exists.", fRoleMethodName));
			}
		} catch (JavaModelException exception) {
			return RefactoringStatus.createFatalErrorStatus("Could not perform the search for binding role types.");
		}
		return new RefactoringStatus();
	}
	
	private RefactoringStatus createCouldNotParseStatus() {
		return RefactoringStatus.createFatalErrorStatus("Could not parse the declaring type.");
	}

	private boolean methodWithNameExists(IType type, String methodName) throws JavaModelException {
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			if (method.getElementName().equals(methodName))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean isExtractBeforeAvailable() {
		List<Statement> statements = fBaseMethodDeclaration.getBody().statements();
		if (statements.isEmpty()) {
			return false;
		}
		if(statements.get(0) instanceof ExpressionStatement 
			&& ((ExpressionStatement) statements.get(0)).getExpression() instanceof MethodInvocation) {
				MethodInvocation invocation = (MethodInvocation) ((ExpressionStatement) statements.get(0)).getExpression();
				Expression receiver = invocation.getExpression();
				boolean isSelfcall = false;
				if (receiver == null) {
					isSelfcall = true;
				} else if (receiver instanceof ThisExpression) {
					isSelfcall = ((ThisExpression)receiver).getQualifier() == null;
				}
				if (isSelfcall)
					return !((IMethod)invocation.resolveMethodBinding().getJavaElement()).getDeclaringType().isBinary();
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean isExtractAfterAvailable() {
		List<Statement> statements = fBaseMethodDeclaration.getBody().statements();
		if (statements.isEmpty()) {
			return false;
		}
		if (statements.get(statements.size() - 1) instanceof ExpressionStatement
				&& ((ExpressionStatement) statements.get(statements.size() - 1)).getExpression() instanceof MethodInvocation) {
			MethodInvocation invocation = (MethodInvocation) ((ExpressionStatement) statements.get(statements.size() - 1)).getExpression();
			if (((IMethod) invocation.resolveMethodBinding().getJavaElement()).getDeclaringType().equals(fBaseType)) {
				return true;
			}
		}
		return false;
	}

	RefactoringStatus checkRoleMethodName() {
		RefactoringStatus status = new RefactoringStatus();
		status.merge(checkIfMethodExists());
		status.merge(checkMethodName(fRoleMethodName));
		return status;
	}

	private RefactoringStatus checkMethodName(String name) {
		RefactoringStatus result = new RefactoringStatus();

		if (name == null)
			return RefactoringStatus.createFatalErrorStatus("No new role method name specified.");

		if ("".equals(name)) //$NON-NLS-1$
			return RefactoringStatus.createFatalErrorStatus("New role method name cannot be empty.");

		IJavaProject javaProject = this.fRoleType.getJavaProject();
		String sourceLevel= javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
		String complianceLevel= javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		IStatus status = JavaConventions.validateMethodName(name, sourceLevel, complianceLevel);
		if (status.isOK())
			return result;

		switch (status.getSeverity()) {
		case IStatus.ERROR:
			return RefactoringStatus.createFatalErrorStatus(status.getMessage());
		case IStatus.WARNING:
			return RefactoringStatus.createWarningStatus(status.getMessage());
		case IStatus.INFO:
			return RefactoringStatus.createInfoStatus(status.getMessage());
		default: // no nothing
			return new RefactoringStatus();
		}
	}
}
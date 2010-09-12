package org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.inlinecallin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BaseCallMessageSend;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.structure.ImportRewriteUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.ReferenceFinderUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.refactoring.corext.rename.BaseCallFinder;
import org.eclipse.objectteams.otdt.internal.refactoring.util.RefactoringUtil;

@SuppressWarnings("restriction")
public class InlineCallinRefactoring extends Refactoring {
	
	private IMethod fRoleMethod;
	
	private String fRoleMethodName;
	
	private CallinBaseMethodInfo[] fCallinBaseMethodInfos;

	private boolean fDeleteRoleMethod;

	private CompilationUnit fRootBase;

	private AST fBaseAST;

	private ImportRewrite fBaseImportRewriter;

	private ICompilationUnit fBaseCUnit;
	
	private CallinBaseMethodInfo[] fTargetBaseMethods;

	private ICompilationUnit fRoleCUnit;

	private CompilationUnit fRootRole;

	private AST fRoleAST;

	private IType fRoleType;

	private TextFileChange fBaseTextFileChange;

	private TextFileChange fRoleTextFileChange;

	private List<ICallinMapping> fBoundCallinMappings;

	private IType fBaseType;

	private ASTRewrite fBaseRewrite;

	private ASTRewrite fRoleRewrite;

	private Object fCachedBaseMethodInfo = null;

	private Set<String> fCachedTunneledParameters = null;
	
	public InlineCallinRefactoring() {
	}

	public InlineCallinRefactoring(IMethod roleMethod) {
		fRoleMethod = roleMethod;
	}

	public InlineCallinRefactoring(IMethod roleMethod, ICallinMapping[] callinMapping, IMethod[] baseMethods) {
		fBaseType = baseMethods[0].getDeclaringType();
		
		List<CallinBaseMethodInfo> methodInfos = new ArrayList<CallinBaseMethodInfo>();
		for (int i = 0; i < baseMethods.length; i++) {
			methodInfos.add(new CallinBaseMethodInfo(baseMethods[i], callinMapping[i]));
		}
		setBaseMethods(methodInfos.toArray(new CallinBaseMethodInfo[methodInfos.size()]));

		fRoleMethod = roleMethod;
	}

	public IMethod getRoleMethod() {
		return fRoleMethod;
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

	@Override
	public String getName() {
		return "Inline Callin";
	}

	public void setRoleMethodName(String name) {
		fRoleMethodName = name;
	}

	public CallinBaseMethodInfo[] getBaseMethodInfos() {
		return fCallinBaseMethodInfos;
	}

	public void setBaseMethods(CallinBaseMethodInfo[] baseMethods) {
		fTargetBaseMethods = baseMethods;
	}

	public void setDeleteRoleMethod(boolean deleteRoleMethod) {
		fDeleteRoleMethod = deleteRoleMethod;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		RefactoringStatus status= new RefactoringStatus();
		
		try {
			monitor.beginTask("Checking preconditions...", 1);
			if (fRoleMethod == null){
				status.merge(RefactoringStatus.createFatalErrorStatus("Method has not been specified."));
			}else if (!fRoleMethod.exists()){
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Method ''{0}'' does not exist.", new Object[] { fRoleMethod
						.getElementName() })));
			}else if (fRoleMethod.isBinary()){
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Method ''{0}'' is declared in a binary class file.", new Object[] { fRoleMethod
						.getElementName() })));
			}else if (fRoleMethod.isReadOnly()){
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Method ''{0}'' is declared in a read-only class file.", new Object[] { fRoleMethod
						.getElementName() })));
			}else if (!fRoleMethod.getCompilationUnit().isStructureKnown()){
					status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("Compilation unit ''{0}'' contains compile errors.",
							new Object[] { fRoleMethod.getCompilationUnit().getElementName() })));
			} else if (!RefactoringUtil.isRoleMethod(fRoleMethod)) {
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format("The selected method ''{0}'' is not declared in a role.",
						new Object[] { fRoleMethod.getElementName() })));
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
		fRoleMethodName = fRoleMethod.getElementName();
		fRoleType = fRoleMethod.getDeclaringType();
		fRoleCUnit = fRoleMethod.getCompilationUnit();

		if (fRootRole == null) {
			fRootRole = RefactoringASTParser.parseWithASTProvider(fRoleCUnit, true, new SubProgressMonitor(monitor, 99));
		}

		fRoleAST = fRootRole.getAST();
		
		try {
			IType baseType = ((IRoleType) OTModelManager.getOTElement(fRoleMethod.getDeclaringType())).getBaseClass();
			if (baseType.isBinary()) {
				status.merge(RefactoringStatus.createFatalErrorStatus("Base class "+baseType.getElementName()+" is a binary type, cannot modify."));
				return status;
			}
			if(baseType.isReadOnly()){
				status.merge(RefactoringStatus.createFatalErrorStatus("Base class "+baseType.getElementName()+" is read-only, cannot modify."));
				return status;
			}
			if (baseType != null) {
				fBaseType = baseType;
				fBaseCUnit = baseType.getCompilationUnit();

				if (fRootBase == null) {
					fRootBase = RefactoringASTParser.parseWithASTProvider(fBaseCUnit, true, new SubProgressMonitor(monitor, 99));
				}
				
				fBaseImportRewriter = StubUtility.createImportRewrite(fRootBase, true);
				fBaseAST = fRootBase.getAST();
			} else {
				status.merge(RefactoringStatus.createFatalErrorStatus("The declaring role class is not bound to a base class."));
			}
		} catch (JavaModelException e) {
			status.merge(createCouldNotParseStatus());
		}
		if (status.hasFatalError()) {
			return status;
		}
		
		IMethodMapping[] callinMappings = ((IRoleType) OTModelManager.getOTElement(fRoleType)).getMethodMappings(IRoleType.CALLINS);

		fBoundCallinMappings = new ArrayList<ICallinMapping>();

		for (int i = 0; i < callinMappings.length; i++) {
			if (callinMappings[i].getRoleMethod().equals(fRoleMethod)) {
				fBoundCallinMappings.add((ICallinMapping) callinMappings[i]);
			}
		}

		if (fBoundCallinMappings.size() == 0) {
			status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format(
					"The selected method ''{0}'' is not bound on the left hand side of a callin method binding.", new Object[] { fRoleMethod
							.getElementName() })));
		}
		
		List<CallinBaseMethodInfo> infos = new ArrayList<CallinBaseMethodInfo>();

		for (ICallinMapping mapping : fBoundCallinMappings) {
			try {
				for (IMethod method : mapping.getBoundBaseMethods()) {
					infos.add(new CallinBaseMethodInfo(method, mapping));
				}
				fCallinBaseMethodInfos = infos.toArray(new CallinBaseMethodInfo[infos.size()]);
			} catch (JavaModelException e) {
				status.merge(RefactoringStatus.createFatalErrorStatus("Could not parse the method mappings."));
			}
		}
		return status;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		
		status.merge(checkBaseMethods());
		
		if (status.hasFatalError()) {
			return status;
		}

		status.merge(generateNewBaseMethodNames());
		
		status.merge(checkRoleMethodName());
		
		status.merge(checkDependenciesToRole(pm));
		
		status.merge(checkRoleMethodReferences(pm));

		return status;
	}

	private RefactoringStatus checkRoleMethodReferences(IProgressMonitor pm) throws CoreException {
		// search all references for the role method
		final Set<SearchMatch> references = new HashSet<SearchMatch>();
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchPattern pattern = SearchPattern.createPattern(fRoleMethod, IJavaSearchConstants.REFERENCES, SearchPattern.R_EXACT_MATCH);
		SearchEngine engine = new SearchEngine();
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				if (match.getAccuracy() == SearchMatch.A_ACCURATE && !match.isInsideDocComment()) {
					references.add(match);
				}
			}
		}, pm);
		
		RefactoringStatus status = new RefactoringStatus();
		
		List<ICallinMapping> inlinedCallins = new ArrayList<ICallinMapping>();
		for (int i = 0; i < fCallinBaseMethodInfos.length; i++) {
			inlinedCallins.add(fCallinBaseMethodInfos[i].getCallinMapping());
		}

		for (SearchMatch match : references) {
			Object element= match.getElement();
			if (element instanceof ICallinMapping) {
				ICallinMapping mapping = (ICallinMapping) element;
				if(mapping.getRoleMethod().equals(fRoleMethod)){
					if(inlinedCallins.contains(mapping)){
						continue;
					}
				} else {
					status.addError(Messages.format("The Role Method ''{0}'' is bound in a callin binding and cannot be inlined.", new String[] { fRoleMethod
							.getElementName() }));
					continue;
				}
			}
			
			if (element instanceof ICalloutMapping) {
				status.addError(Messages.format("The Role Method ''{0}'' is bound in a callout binding and cannot be inlined.", new String[] { fRoleMethod
						.getElementName() }));
				continue;
			}
			
			if (fDeleteRoleMethod) {
				if (element instanceof IMember) {
					// try to create context informations for the search result
					IMember referencingMember = (IMember) element;
					String msg = Messages.format("The Role Method ''{0}'' is referenced by ''{1}'' and cannot be deleted.", new String[] {
							fRoleMethod.getElementName(), JavaElementLabels.getTextLabel(referencingMember, JavaElementLabels.ALL_FULLY_QUALIFIED) });
					status.addError(msg, JavaStatusContext.create(referencingMember));
				} else {
					status.addError(Messages.format("The Role Method ''{0}'' is referenced and cannot be deleted.",
							new String[] { fRoleMethod.getElementName() }));
				}
			}
		}
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

	private RefactoringStatus checkIfMethodExists() {
		try {
			if (methodWithNameExists(fBaseType, fRoleMethodName)){
				return RefactoringStatus.createErrorStatus(MessageFormat.format("A method with the same name already exists.", fRoleMethodName));
			}
		} catch (JavaModelException exception) {
			return createCouldNotParseStatus();
		}
		return new RefactoringStatus();
	}

	private RefactoringStatus checkDependenciesToRole(IProgressMonitor pm) throws JavaModelException {
		RefactoringStatus status = new RefactoringStatus();

		IMethod[] referencedMethods = ReferenceFinderUtil.getMethodsReferencedIn(new IJavaElement[] { fRoleMethod }, null /* owner */, new SubProgressMonitor(pm,
				1));
		for (int i = 0; i < referencedMethods.length; i++) {
			IMethod referencedMethod = referencedMethods[i];
			if (referencedMethod.getDeclaringType().equals(fRoleType)) {
				status.merge(RefactoringStatus.createErrorStatus(MessageFormat.format("The method to inline ''{0}'' references the role method ''{1}''.",
						fRoleMethodName, referencedMethod)));
			}
		}

		IField[] referencedFileds = ReferenceFinderUtil.getFieldsReferencedIn(new IJavaElement[] { fRoleMethod }, null /* owner */, new SubProgressMonitor(pm,
				1));
		for (int i = 0; i < referencedFileds.length; i++) {
			IField referencedFiled = referencedFileds[i];
			if (referencedFiled.getDeclaringType().equals(fRoleType)) {
				status.merge(RefactoringStatus.createErrorStatus(MessageFormat.format("The method to inline ''{0}'' references the role field ''{1}''.",
						fRoleMethodName, referencedFiled)));
			}
		}
		return status;
	}
	
	RefactoringStatus checkRoleMethodName() {
		RefactoringStatus status = new RefactoringStatus();
		status.merge(checkIfMethodExists());
		status.merge(checkMethodName(fRoleMethodName));
		return status;
	}

	RefactoringStatus checkBaseMethods() {
		RefactoringStatus status = new RefactoringStatus();
		if (fTargetBaseMethods == null || fTargetBaseMethods.length == 0) {
			status.merge(RefactoringStatus.createFatalErrorStatus("No base method selected."));
		}
		return status;
	}

	private boolean methodWithNameExists(IType type, String methodName) throws JavaModelException {
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			if (method.getElementName().equals(methodName))
				return true;
		}
		return false;
	}

	private RefactoringStatus createCouldNotParseStatus() {
		return RefactoringStatus.createFatalErrorStatus("Could not parse the declaring type.");
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		pm.beginTask("Creating Change", fCallinBaseMethodInfos.length + 2);
		inlineCallin(pm);
		Change change = new CompositeChange(getName(), new TextFileChange[] { fBaseTextFileChange, fRoleTextFileChange });
		pm.done();
		return change;
	}

	@SuppressWarnings("unchecked")
	private void inlineCallin(IProgressMonitor pm) throws CoreException {
		fBaseRewrite = ASTRewrite.create(fBaseAST);
		fRoleRewrite = ASTRewrite.create(fRoleAST);
		
		for (int i = 0; i < fTargetBaseMethods.length; i++) {

			IMethod baseMethod = fTargetBaseMethods[i].getMethod();
			CallinBaseMethodInfo methodInfo = fTargetBaseMethods[i];
			renameBaseMethod(methodInfo);
			MethodDeclaration wrapperMethodDeclaration = createWrapperMethod(methodInfo);
			Statement roleMethodInvocation = createRoleMethodInvocation(methodInfo);
			List<Statement> statements = wrapperMethodDeclaration.getBody().statements();

			// change the strategy for the different mapping kinds
			switch (methodInfo.getCallinMapping().getCallinKind()) {
			case ICallinMapping.KIND_BEFORE:
				statements.add(0, roleMethodInvocation);
				break;
			case ICallinMapping.KIND_AFTER:
				// TODO check for result parameter mappings
				if (!baseMethod.getReturnType().equals(Character.toString(Signature.C_VOID))) {
					// remove the base method invocation
					statements.clear();
		
					// insert local variable statement to save the return value
					// of
					// the base method invocation
					MethodDeclaration baseMethodDeclaration = RefactoringUtil.methodToDeclaration(baseMethod, fRootBase);
					String varName = generateResultVarName(methodInfo);
					VariableDeclarationFragment fragment = fBaseAST.newVariableDeclarationFragment();
					fragment.setName(fBaseAST.newSimpleName(varName));
					fragment.setInitializer(createBaseMethodInvocation(methodInfo));
					VariableDeclarationStatement variableDeclarationStatement = fBaseAST.newVariableDeclarationStatement(fragment);
					variableDeclarationStatement.setType((Type) ASTNode.copySubtree(fBaseAST, baseMethodDeclaration.getReturnType2()));
					statements.add(variableDeclarationStatement);

					// invoke the role method
					statements.add(roleMethodInvocation);

					// return the stored return value
					ReturnStatement returnStatement = fBaseAST.newReturnStatement();
					returnStatement.setExpression(fBaseAST.newSimpleName(varName));
					statements.add(returnStatement);
				} else {
					statements.add(roleMethodInvocation);
				}
				break;
			case ICallinMapping.KIND_REPLACE:
				statements.clear();
				statements.add(roleMethodInvocation);
				break;
			default:
				break;
			}
		
			insertMethodIntoBase(wrapperMethodDeclaration, baseMethod);
			pm.worked(1);
		}
		
		adjustMethodMappings();
		pm.worked(1);
		
		copyRoleMethodToBase(fTargetBaseMethods[0]);
		pm.worked(1);
		
		if (fDeleteRoleMethod) {
			deleteRoleMethod();
		}
		
		// create the text change for the base
		MultiTextEdit baseMultiEdit = new MultiTextEdit();
		baseMultiEdit.addChild(fBaseRewrite.rewriteAST());
		fBaseTextFileChange = new TextFileChange(fBaseCUnit.getElementName(), (IFile) fBaseCUnit.getResource());
		fBaseTextFileChange.setTextType("java");
		fBaseTextFileChange.setEdit(baseMultiEdit);
			
		if (fBaseImportRewriter.hasRecordedChanges()) {
			TextEdit edit = fBaseImportRewriter.rewriteImports(null);
			baseMultiEdit.addChild(edit);
			fBaseTextFileChange.addTextEditGroup(new TextEditGroup("Organize Imports", new TextEdit[] { edit }));
		}

		// create the text change for the role
		MultiTextEdit roleMultiEdit = new MultiTextEdit();
		roleMultiEdit.addChild(fRoleRewrite.rewriteAST());
		fRoleTextFileChange = new TextFileChange(fRoleCUnit.getElementName(), (IFile) fRoleCUnit.getResource());
		fRoleTextFileChange.setTextType("java");
		fRoleTextFileChange.setEdit(roleMultiEdit);
	}

	
	/**
	 * Renames the given base method to the name given in the info object.
	 * 
	 * @param baseMethodInfo
	 *            the info object containing the base method and the new base
	 *            method name
	 * @throws JavaModelException
	 */
	private void renameBaseMethod(CallinBaseMethodInfo baseMethodInfo) throws JavaModelException {
		ASTNode baseMethodDeclaration = RefactoringUtil.methodToDeclaration(baseMethodInfo.getMethod(), fRootBase);
		fBaseRewrite.set(baseMethodDeclaration, MethodDeclaration.NAME_PROPERTY, fBaseAST.newSimpleName(baseMethodInfo.getNewMethodName()), null);
	}

	/**
	 * Creates the method declaration for the wrapper method with the old base
	 * method name.
	 * 
	 * @param baseMethodInfo
	 *            the info object containing the base method and the new base
	 *            method name
	 * @return the generated wrapper method declaration
	 * @throws CoreException
	 */
	private MethodDeclaration createWrapperMethod(CallinBaseMethodInfo baseMethodInfo) throws CoreException {
		MethodDeclaration baseMethodDeclaration = RefactoringUtil.methodToDeclaration(baseMethodInfo.getMethod(), fRootBase);
		MethodDeclaration methodDeclaration = (MethodDeclaration) ASTNode.copySubtree(fBaseAST, baseMethodDeclaration);
		methodDeclaration.setBody(createMethodBody(baseMethodInfo));
		return methodDeclaration;
	}

	/**
	 * Creates the method body for the wrapper method that maps the inlined
	 * method binding.
	 * 
	 * @param baseMethodInfo
	 *            the info object containing the base method and the new base
	 *            method name
	 * @return the method body
	 * @throws JavaModelException
	 */
	@SuppressWarnings("unchecked")
	private Block createMethodBody(CallinBaseMethodInfo baseMethodInfo) throws JavaModelException {
		Block block = fBaseAST.newBlock();
		List<Statement> statements = block.statements();
		MethodInvocation invocation = createBaseMethodInvocation(baseMethodInfo);
		
		// create a return statement for the base method invocation if necessary
		MethodDeclaration declaration = RefactoringUtil.methodToDeclaration(baseMethodInfo.getMethod(), fRootBase);
		final Type type = declaration.getReturnType2();
		if (type == null || (type instanceof PrimitiveType && PrimitiveType.VOID.equals(((PrimitiveType) type).getPrimitiveTypeCode()))) {
			statements.add(invocation.getAST().newExpressionStatement(invocation));
		} else {
			ReturnStatement statement = invocation.getAST().newReturnStatement();
			statement.setExpression(invocation);
			statements.add(statement);
		}
		return block;
	}

	/**
	 * Removes the base methods from the inlined method mappings. If all bound
	 * base methods of a binding are removed, the whole mapping will be deleted.
	 * The change is performed on the given <code>ASTRewrite</code>.
	 * 
	 * @param rewrite
	 *            the rewrite that notes the changes
	 * @throws JavaModelException
	 */
	@SuppressWarnings("unchecked")
	private void adjustMethodMappings() throws JavaModelException {
	
		// create a map that gathers multiple methods for the same callin mapping
		Map<IMethod, ICallinMapping> methodToMapping = new HashMap<IMethod, ICallinMapping>();
		for (int i = 0; i < fTargetBaseMethods.length; i++) {
			methodToMapping.put(fTargetBaseMethods[i].getMethod(), fTargetBaseMethods[i].getCallinMapping());
		}
	
		for (ICallinMapping callinMapping : methodToMapping.values()) {
			Collection<IMethod> baseMethodsToRemove = methodToMapping.keySet();
			
			// search the current callin mapping in the ast
			CallinMappingDeclaration methodMappingDecl = (CallinMappingDeclaration) RefactoringUtil.methodMappingToDeclaration(callinMapping, fRootRole);
			
			if (baseMethodsToRemove.containsAll(Arrays.asList(callinMapping.getBoundBaseMethods()))) {
				// if all bound base methods should be removed the remove the
				// method mapping
				fRoleRewrite.remove(methodMappingDecl, null);
				
			} else {
				// otherwise remove the references to the base methods from the
				// mapping
				CallinMappingDeclaration mappingDeclaration = methodMappingDecl;
				List<MethodSpec> baseMethods = mappingDeclaration.getBaseMappingElements();
				for (MethodSpec methodSpec : baseMethods) {
					for (IMethod method : baseMethodsToRemove) {
						IMethodBinding methodBinding = methodSpec.resolveBinding();
						IMethod boundMethod = (IMethod) methodBinding.getJavaElement();
						if (method.equals(boundMethod)) {
							// TODO remove unused imports
							fRoleRewrite.remove(methodSpec, null);
						}
					}
				}
			}
		}
	}

	private void deleteRoleMethod() throws JavaModelException {
		// TODO remove unused imports
		AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) RefactoringUtil.typeToDeclaration(fRoleType, fRootRole);
		ChildListPropertyDescriptor descriptor = typeToBodyDeclarationProperty(fBaseType, fRootBase);
		MethodDeclaration roleMethodDecl = RefactoringUtil.methodToDeclaration(fRoleMethod, fRootRole);
		fRoleRewrite.getListRewrite(declaration, descriptor).remove(roleMethodDecl, null);
	}

	private boolean hasResultTunneling(CallinBaseMethodInfo baseMethodInfo) throws JavaModelException {
		// only replace callins can have result tunneling
		if (baseMethodInfo.getCallinMapping().getCallinKind() != ICallinMapping.KIND_REPLACE) {
			return false;
		}

		// only base methods with return types can produce result tunneling
		if (isVoidMethod(baseMethodInfo.getMethod())) {
			return false;
		}
		
		// only role methods without a return type can produce result tunneling
		if (!isVoidMethod(fRoleMethod)) {
			return false;
		}

		if (hasResultParameterMapping(baseMethodInfo.getCallinMapping())) {
			return false;
		}
		
		return true;
	}


	@SuppressWarnings("unchecked")
	private boolean hasResultParameterMapping(ICallinMapping callinMapping) throws JavaModelException {
		if (!hasParameterMapping(callinMapping)) {
			return false;
		}
		AbstractMethodMappingDeclaration mappingDecl = RefactoringUtil.methodMappingToDeclaration(callinMapping, fRootRole);
		List<ParameterMapping> parameterMappings = mappingDecl.getParameterMappings();
		for (ParameterMapping parameterMapping : parameterMappings) {
			if (parameterMapping.hasResultFlag()) {
				return true;
			}
		}
		return false;
	}

	private Statement createRoleMethodInvocation(CallinBaseMethodInfo baseMethodInfo) throws JavaModelException {
		MethodInvocation invocation = fBaseAST.newMethodInvocation();
		ICallinMapping callinMapping = baseMethodInfo.getCallinMapping();

		invocation.setName(fBaseAST.newSimpleName(fRoleMethodName));

		CallinMappingDeclaration callinMappingDecl = (CallinMappingDeclaration) RefactoringUtil.methodMappingToDeclaration(callinMapping, fRootRole);
		if (hasParameterMapping(callinMapping)) {
			copyRoleParameterMappingsToInvocation(invocation, callinMappingDecl, baseMethodInfo);
		} else {
			String[] parameterNames = baseMethodInfo.getMethod().getParameterNames();
			int length = callinMapping.getCallinKind() ==  ICallinMapping.KIND_REPLACE 
						? parameterNames.length 
						: this.fRoleMethod.getParameterNames().length;
			copyInvocationParameters(invocation, parameterNames, length);
		}
		if (needsReturnStatement(baseMethodInfo)) {
			ReturnStatement statement = invocation.getAST().newReturnStatement();
			statement.setExpression(invocation);
			return statement;
		} else {
			return invocation.getAST().newExpressionStatement(invocation);
		}
	}
	
	private boolean needsReturnStatement(CallinBaseMethodInfo baseMethodInfo) throws JavaModelException {
		if (isReplace(baseMethodInfo)) {
			return !isVoidMethod(baseMethodInfo.getMethod());
		} else {
			return !(isVoidMethod(fRoleMethod) || isVoidMethod(baseMethodInfo.getMethod()));
		}
	}

	private boolean isVoidMethod(IMethod method) throws JavaModelException {
		return method.getReturnType().equals(Character.toString(Signature.C_VOID));
	}

	@SuppressWarnings("unchecked")
	private void copyRoleParameterMappingsToInvocation(MethodInvocation invocation, CallinMappingDeclaration callinMappingDecl,
			CallinBaseMethodInfo baseMethodInfo)
			throws JavaModelException {	
		MethodSpec baseMethodSpec = findBaseMethodSpec(callinMappingDecl, baseMethodInfo.getMethod());
		
		List<SingleVariableDeclaration> baseMappingParams = baseMethodSpec.parameters();
		
		// create a map that maps the original param names of the base method to
		// the param names used in the mapping
		Map<String, String> callinParamNamesToBaseMethodParamNames = new HashMap<String, String>();
		String[] names = baseMethodInfo.getMethod().getParameterNames();
		for (int i = 0; i < names.length; i++) {
			callinParamNamesToBaseMethodParamNames.put(baseMappingParams.get(i).getName().getIdentifier(), names[i]);
		}
		
		List<ParameterMapping> paramMappings = callinMappingDecl.getParameterMappings();
		MethodSpec roleMethodSpec = (MethodSpec) callinMappingDecl.getRoleMappingElement();
		List<SingleVariableDeclaration> roleMappingParams = roleMethodSpec.parameters();


		for (SingleVariableDeclaration singleVariableDeclaration : roleMappingParams) {
			String paramName = singleVariableDeclaration.getName().getIdentifier();
			for (ParameterMapping mapping : paramMappings) {
				if (mapping.hasResultFlag())
					continue;
				if (mapping.getIdentifier().getIdentifier().equals(paramName)) {
					Expression expr = (Expression) ASTNode.copySubtree(fBaseAST, mapping.getExpression());
					substituteBaseParams(expr, callinParamNamesToBaseMethodParamNames, baseMethodInfo);
					invocation.arguments().add(expr);
				}
			}
		}

		
		if (isReplace(baseMethodInfo)) {
			Set<String> tunneledParams = findTunneledParameters(baseMethodInfo);
			// replace tunneled parameters in base method invocations
			for (String paramName : tunneledParams) {
				String tunneledName = generateTunneledParamName(paramName);
				invocation.arguments().add(fBaseAST.newSimpleName(tunneledName));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void copyBaseParameterMappingsToInvocation(MethodInvocation invocation, CallinMappingDeclaration callinMappingDecl,
			CallinBaseMethodInfo baseMethodInfo) throws JavaModelException {
		MethodSpec roleMethodSpec = (MethodSpec) callinMappingDecl.getRoleMappingElement();
		List<SingleVariableDeclaration> roleMappingParams = roleMethodSpec.parameters();

		// create a map that maps the original param names of the role method to
		// the param names used in the mapping
		Map<String, String> callinParamNamesToRoleMethodParamNames = new HashMap<String, String>();
		String[] roleParamNames = fRoleMethod.getParameterNames();
		for (int i = 0; i < roleParamNames.length; i++) {
			callinParamNamesToRoleMethodParamNames.put(roleMappingParams.get(i).getName().getIdentifier(), roleParamNames[i]);
		}

		List<ParameterMapping> paramMappings = callinMappingDecl.getParameterMappings();
		MethodSpec baseMethodSpec = findBaseMethodSpec(callinMappingDecl, baseMethodInfo.getMethod());
		List<SingleVariableDeclaration> baseMappingParams = baseMethodSpec.parameters();

		// create a map that maps the original param names of the base method to
		// the param names used in the mapping
		Map<String, String> callinParamNamesToBaseMethodParamNames = new HashMap<String, String>();
		String[] baseParamNames = baseMethodInfo.getMethod().getParameterNames();
		for (int i = 0; i < baseParamNames.length; i++) {
			callinParamNamesToBaseMethodParamNames.put(baseMappingParams.get(i).getName().getIdentifier(), baseParamNames[i]);
		}
		
		Map<String, Expression> baseParamToExpression = new HashMap<String, Expression>();
		

		for (SingleVariableDeclaration singleVariableDeclaration : baseMappingParams) {
			String paramName = singleVariableDeclaration.getName().getIdentifier();
			for (ParameterMapping mapping : paramMappings) {
				if (mapping.hasResultFlag())
					continue;
				if (mapping.getExpression() instanceof SimpleName) {
					SimpleName mappedName = (SimpleName) mapping.getExpression();
					if (mappedName.getIdentifier().equals(paramName)) {
						// resolve the original param names because param names
						// in the mapping can be different
						String roleMethodIdentifier = callinParamNamesToRoleMethodParamNames.get(mapping.getIdentifier().getIdentifier());
						String baseMethodIdentifier = callinParamNamesToBaseMethodParamNames.get(paramName);
						baseParamToExpression.put(baseMethodIdentifier, fBaseAST.newSimpleName(roleMethodIdentifier));
						// base parameters can only be mapped once �4.4.(b)
						continue;
					}
				}
			}
		}
		
		// pass the right statement for each base param
		for (String name : baseParamNames) {
			if(baseParamToExpression.get(name) == null){
				String tunneledName = generateTunneledParamName(name);
				invocation.arguments().add(fBaseAST.newSimpleName(tunneledName));
			}else{
				// pass the simple name of the mapped role method parameter
				invocation.arguments().add(baseParamToExpression.get(name));
			}
		}

		if (isReplace(baseMethodInfo)) {

			// parameter mappings are handled differently because they depend on
			// the mapping
			if (!hasParameterMapping(baseMethodInfo.getCallinMapping())) {
				// if the role method doesn't declare the same number of
				// parameters
				// as the base method, the parameters have to be appended
				int roleMethodParameterLength = baseMethodInfo.getCallinMapping().getRoleMethod().getParameterNames().length;
				int baseMethodParameterLength = baseMethodInfo.getMethod().getParameterNames().length;
				if (roleMethodParameterLength < baseMethodParameterLength) {
					appendInvocationParameters(invocation, baseMethodInfo.getMethod(), roleMethodParameterLength);
				}
			}
		}
	}
	
	private void substituteBaseParams(Expression expr, Map<String, String> callinParamNamesToBaseMethodParamNames, CallinBaseMethodInfo baseMethodInfo)
			throws JavaModelException {
		ArrayList<String> callinParamNames = new ArrayList<String>();
		for (String name : callinParamNamesToBaseMethodParamNames.keySet()) {
			callinParamNames.add(name);
		}
		SimpleNameFinder simpleNameFinder = new SimpleNameFinder(callinParamNames);
		expr.accept(simpleNameFinder);
		List<SimpleName> simpleNames = simpleNameFinder.getResult();
		for (SimpleName simpleName : simpleNames) {
			String baseMethodParamName = callinParamNamesToBaseMethodParamNames.get(simpleName.getIdentifier());
			simpleName.setIdentifier(baseMethodParamName);
		}
	}

	@SuppressWarnings("unchecked")
	private Set<String> findTunneledParameters(CallinBaseMethodInfo baseMethodInfo)
			throws JavaModelException {
		// try to reuse cached result
		if (fCachedBaseMethodInfo == baseMethodInfo) {
			return fCachedTunneledParameters;
		}
		fCachedBaseMethodInfo = baseMethodInfo;
		Set<String> tunneledParams = new HashSet<String>();
		CallinMappingDeclaration callinMappingDecl = (CallinMappingDeclaration) RefactoringUtil.methodMappingToDeclaration(baseMethodInfo.getCallinMapping(),
				fRootRole);
		MethodSpec baseMethodSpec = findBaseMethodSpec(callinMappingDecl, baseMethodInfo.getMethod());

		List<SingleVariableDeclaration> baseMappingParams = baseMethodSpec.parameters();

		// create a map that maps the original param names of the base method to
		// the param names used in the mapping
		Map<String, String> baseMethodParamNameToBaseMappingParamName = new HashMap<String, String>();
		String[] names = baseMethodInfo.getMethod().getParameterNames();
		for (int i = 0; i < names.length; i++) {
			baseMethodParamNameToBaseMappingParamName.put(names[i], baseMappingParams.get(i).getName().getIdentifier());
		}

		List<ParameterMapping> paramMappings = callinMappingDecl.getParameterMappings();
		tunneledParams.addAll(Arrays.asList(baseMethodInfo.getMethod().getParameterNames()));
		for (int i = 0; i < names.length; i++) {
			for (ParameterMapping mapping : paramMappings) {
				if (mapping.hasResultFlag())
					continue;
				Expression expr = (Expression) mapping.getExpression();
				if (expr instanceof SimpleName) {
					SimpleName simpleName = (SimpleName) expr;
					if (simpleName.getIdentifier().equals(baseMethodParamNameToBaseMappingParamName.get(names[i]))) {
						tunneledParams.remove(names[i]);
					}
				}
			}
		}
		fCachedTunneledParameters = tunneledParams;
		return fCachedTunneledParameters;
	}
	
	/**
	 * Copies the invocation parameter of the given base method to the given
	 * method invocation.
	 * 
	 * @param invocation
	 *            the method invocation to receive the parameters
	 * @param method
	 *            the method that declares the parameters
	 * @throws JavaModelException
	 */
	private void copyInvocationParameters(MethodInvocation invocation, IMethod method) throws JavaModelException {
		String[] parameterNames = method.getParameterNames();
		copyInvocationParameters(invocation, parameterNames, parameterNames.length);		
	}
	// this variant allows to cut off unneeded parameters (only for before/after callins):
	@SuppressWarnings("unchecked")
	private void copyInvocationParameters(MethodInvocation invocation, String[] names, int n) throws JavaModelException {
		for (int i=0; i<n; i++)
			invocation.arguments().add(fBaseAST.newSimpleName(names[i]));
	}

	/**
	 * Copies the role method to the base class and creates store variables for
	 * implicit result tunneling. If the role method was a callin method, base
	 * methods are replaced by method invocations to the new method name,
	 * specified in <code>baseMethodInfo</code>.
	 * 
	 * @param rewrite
	 *            the rewrite that notes the changes
	 * @param baseMethodInfo
	 *            the info object containing the base method, callin mapping,
	 *            and the new base method name
	 * @throws JavaModelException
	 */
	private void copyRoleMethodToBase(CallinBaseMethodInfo baseMethodInfo) throws JavaModelException {
		ASTNode roleMethodDeclaration = RefactoringUtil.methodToDeclaration(fRoleMethod, fRootRole);
		MethodDeclaration copyOfRoleMethodDeclaration = (MethodDeclaration) ASTNode.copySubtree(fBaseAST, roleMethodDeclaration);
		copyOfRoleMethodDeclaration.setName(fBaseAST.newSimpleName(fRoleMethodName));
		
		// change the visibility of the copy to private to avoid overriding and
		// hide the role method
		if (!Flags.isPrivate(fRoleMethod.getFlags()) && !Flags.isCallin(fRoleMethod.getFlags()))
			changeToPrivateVisibility(fBaseRewrite, copyOfRoleMethodDeclaration);
		
		// special treatment for replace mappings/callin methods
		if (Flags.isCallin(fRoleMethod.getFlags())) {
			handleCallinMethod(fBaseRewrite, baseMethodInfo, copyOfRoleMethodDeclaration);
		}
		
		if ((isReplace(baseMethodInfo))) {
			if (hasParameterMapping(baseMethodInfo.getCallinMapping())) {
				// append parameters that are tunneled because of a missing
				// parameter mapping �4.4.(b)
				appendTunneledParameterDeclarations(baseMethodInfo, copyOfRoleMethodDeclaration);
			} else {
				// if a callin method doesn't declare the same number of
				// parameters
				// as the base method, the parameters have to be appended
				int roleMethodParameterLength = baseMethodInfo.getCallinMapping().getRoleMethod().getParameterNames().length;
				int baseMethodParameterLength = baseMethodInfo.getMethod().getParameterNames().length;
				if (roleMethodParameterLength < baseMethodParameterLength) {
					appendParameterDeclarations(copyOfRoleMethodDeclaration, baseMethodInfo.getMethod(), roleMethodParameterLength);
				}
			}
		}
		
		findAndReplaceCallouts(fBaseRewrite, copyOfRoleMethodDeclaration);
		
		insertMethodIntoBase(copyOfRoleMethodDeclaration, baseMethodInfo.getMethod());
		
		// oragnize imports
		Set<IBinding> staticImports = new HashSet<IBinding>();
		Set<ITypeBinding> imports = new HashSet<ITypeBinding>();
		ImportRewriteUtil.collectImports(fRoleMethod.getJavaProject(), roleMethodDeclaration, imports, staticImports, false);
		for (ITypeBinding typeBinding : imports) {
			fBaseImportRewriter.addImport(typeBinding);
		}
		for (IBinding binding : staticImports) {
			fBaseImportRewriter.addStaticImport(binding);
		}
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

	/**
	 * Handles special cases for callin methods like result tunneling and base
	 * call replacement.
	 * 
	 * @param rewrite
	 *            the rewrite that notes the changes
	 * @param baseMethodInfo
	 *            the info object containing the base method, callin mapping,
	 *            and the new base method name
	 * @param methodDeclaration
	 *            the method declaration node of the role method
	 * @throws JavaModelException
	 */
	@SuppressWarnings("unchecked")
	private void handleCallinMethod(ASTRewrite astRewrite, CallinBaseMethodInfo baseMethodInfo, MethodDeclaration methodDeclaration)
			throws JavaModelException {
		List<Statement> statements = methodDeclaration.getBody().statements();
		removeCallinFlag(methodDeclaration);
		
		if (fRoleMethod.getReturnType().equals(Character.toString(Signature.C_VOID))) {
			ReturnFinder returnFinder = new ReturnFinder();
			methodDeclaration.accept(returnFinder);
			List<ReturnStatement> returns = returnFinder.getResult();
			for (ReturnStatement returnStatement : returns) {
				astRewrite.replace(returnStatement, fBaseAST.newReturnStatement(), null);
			}
		}
		
		if (hasResultTunneling(baseMethodInfo)) {
			String varName = generateResultVarName(baseMethodInfo);
			VariableDeclarationFragment fragment = fBaseAST.newVariableDeclarationFragment();
			fragment.setName(fBaseAST.newSimpleName(varName));
			VariableDeclarationStatement variableDeclarationStatement = fBaseAST.newVariableDeclarationStatement(fragment);
			MethodDeclaration baseMethodDeclaration = RefactoringUtil.methodToDeclaration(baseMethodInfo.getMethod(), fRootBase);
			variableDeclarationStatement.setType((Type) ASTNode.copySubtree(fBaseAST, baseMethodDeclaration.getReturnType2()));
			statements.add(0, variableDeclarationStatement);
			
			
			// return the stored return value
			ReturnStatement returnStatement = fBaseAST.newReturnStatement();
			returnStatement.setExpression(fBaseAST.newSimpleName(varName));
			statements.add(returnStatement);
			
			substituteBaseCalls(methodDeclaration, astRewrite, varName, baseMethodInfo);
			
		} else if(hasResultParameterMapping(baseMethodInfo.getCallinMapping())
				&& isVoidMethod(fRoleMethod)) {
			// replace callins may define a result mapping if they don't have a
			// return value
			AbstractMethodMappingDeclaration mappingDecl = RefactoringUtil.methodMappingToDeclaration(baseMethodInfo.getCallinMapping(), fRootRole);
			List<ParameterMapping> parameterMappings = mappingDecl.getParameterMappings();
			Expression resultMappingExpression = null;
			for (ParameterMapping parameterMapping : parameterMappings) {
				if (parameterMapping.hasResultFlag()) {
					resultMappingExpression = (Expression) parameterMapping.getExpression();
				}
			}

			// return the mapped return value
			ReturnStatement returnStatement = fBaseAST.newReturnStatement();
			returnStatement.setExpression((Expression) ASTNode.copySubtree(fBaseAST, resultMappingExpression));
			statements.add(returnStatement);
			substituteBaseCalls(methodDeclaration, astRewrite, null /* localStoreVarIdentifier */, baseMethodInfo);
		} else {
			substituteBaseCalls(methodDeclaration, astRewrite, null /* localStoreVarIdentifier */, baseMethodInfo);
		}
	
		Modifier privateVisibility = fBaseAST.newModifier(ModifierKeyword.PRIVATE_KEYWORD);
		methodDeclaration.modifiers().add(privateVisibility);
	
		// callin methods need the same return type as the base method
		// after inlining
		MethodDeclaration declaration = RefactoringUtil.methodToDeclaration(baseMethodInfo.getMethod(), fRootBase);
		methodDeclaration.setReturnType2((Type) ASTNode.copySubtree(fBaseAST, declaration.getReturnType2()));
	}

	/**
	 * Removes a callin modifier from the given <code>MethodDeclaration</code>.
	 * 
	 * @param methodDeclaration
	 *            the method declaration to remove the callin modifier from
	 * @return true if a callin modifier was removed - otherwise false
	 */
	@SuppressWarnings("unchecked")
	private boolean removeCallinFlag(MethodDeclaration methodDeclaration) {
		List<IExtendedModifier> modifiers = methodDeclaration.modifiers();
	
		for (IExtendedModifier extendedModifier : modifiers) {
			if (extendedModifier instanceof Modifier) {
				Modifier modifier = (Modifier) extendedModifier;
				if (Modifier.isCallin(modifier.getKeyword().toFlagValue())) {
					modifier.delete();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Substitutes base calls in the body of a callin method by method
	 * invocations to the method specified in baseMethodInfo. If a identifier
	 * for a local variable is given, the method invocations will be assigned to
	 * it.
	 * 
	 * @param methodDeclaration
	 *            the method declaration containing the base calls
	 * @param rewrite
	 *            the rewrite that notes the changes
	 * @param localStoreVarIdentifier
	 *            the name of a return store variable - <code>null</code> if no
	 *            return value must be stored
	 * @param baseMethodInfo
	 *            the info object containing the base method and the new base
	 *            method name
	 * @throws JavaModelException
	 */
	private void substituteBaseCalls(MethodDeclaration methodDeclaration, ASTRewrite astRewrite, String localStoreVarIdentifier,
			CallinBaseMethodInfo baseMethodInfo)
			throws JavaModelException {
		BaseCallFinder baseCallFinder = new BaseCallFinder();
		methodDeclaration.accept(baseCallFinder);
		BaseCallMessageSend[] baseCalls = baseCallFinder.getResult();
		
		if (localStoreVarIdentifier != null) {
			// create assignment statements if a store identifier exist
			for (int i = 0; i < baseCalls.length; i++) {
				BaseCallMessageSend basecall = baseCalls[i];
	
				Assignment assignment = fBaseAST.newAssignment();
				assignment.setLeftHandSide(fBaseAST.newSimpleName(localStoreVarIdentifier));
				assignment.setRightHandSide(createBaseMethodInvocation(baseMethodInfo));
	
				astRewrite.replace(basecall, assignment, null);
			}
		} else {
			// substitute the base calls with a simple base call
			for (int i = 0; i < baseCalls.length; i++) {
				BaseCallMessageSend basecall = baseCalls[i];
				astRewrite.replace(basecall, createBaseMethodInvocation(baseMethodInfo), null);
			}
		}
	}

	private void findAndReplaceCallouts(ASTRewrite astRewrite, MethodDeclaration copyOfRoleMethodDeclaration) throws JavaModelException {
		MethodInvocationFinder methodInvocationFinder = new MethodInvocationFinder();
		copyOfRoleMethodDeclaration.accept(methodInvocationFinder);
		List<MethodInvocation> methodInvocations = methodInvocationFinder.getResult();
		IMethodMapping[] calloutMappings = ((IRoleType) OTModelManager.getOTElement(fRoleType)).getMethodMappings(IRoleType.CALLOUTS);
	
		Map<String, IMethodMapping> calloutNameToMapping = new HashMap<String, IMethodMapping>();
		for (IMethodMapping methodMapping : calloutMappings) {
			if(methodMapping instanceof ICalloutMapping){
				ICalloutMapping calloutMapping = (ICalloutMapping)methodMapping;
				calloutNameToMapping.put(calloutMapping.getRoleMethodHandle().getSelector(), calloutMapping);
				
			}
			if(methodMapping instanceof ICalloutToFieldMapping){
				ICalloutToFieldMapping calloutToFieldMapping = (ICalloutToFieldMapping)methodMapping;
				calloutNameToMapping.put(calloutToFieldMapping.getRoleMethodHandle().getSelector(), calloutToFieldMapping);
			}
		}
		
		for (MethodInvocation invocation : methodInvocations) {
			IMethodMapping mapping = calloutNameToMapping.get(invocation.getName().getIdentifier());
			if(mapping == null){
				continue;
			}
			// TODO Parameter mappings
			switch (mapping.getMappingKind()) {
			case IOTJavaElement.CALLOUT_MAPPING:
				ICalloutMapping calloutMapping = (ICalloutMapping)mapping;
				invocation.setName(fBaseAST.newSimpleName(calloutMapping.getBoundBaseMethod().getElementName()));
				break;
			case IOTJavaElement.CALLOUT_TO_FIELD_MAPPING:
				ICalloutToFieldMapping calloutToFieldMapping = (ICalloutToFieldMapping)mapping;
				CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration) RefactoringUtil.methodMappingToDeclaration(calloutToFieldMapping, fRootRole);
				String fieldName = calloutToFieldMapping.getBoundBaseField().getElementName();
				if (Modifier.isSet(calloutDecl.bindingOperator().getBindingModifier())) {
					// set
					Assignment setAssignment = fBaseAST.newAssignment();
					setAssignment.setLeftHandSide(fBaseAST.newSimpleName(fieldName));
					Expression setExpression = (Expression) ASTNode.copySubtree(fBaseAST, (Expression) invocation.arguments().get(0));
					setAssignment.setRightHandSide(setExpression);
					astRewrite.replace(invocation, setAssignment, null);
				} else {
					// get
					astRewrite.replace(invocation, fBaseAST.newSimpleName(fieldName), null);
				}
				
				
				break;
			default:
				break;
			}
		}
	}

	private MethodInvocation createBaseMethodInvocation(CallinBaseMethodInfo baseMethodInfo) throws JavaModelException {
		MethodInvocation invocation = fBaseAST.newMethodInvocation();
		invocation.setName(fBaseAST.newSimpleName(baseMethodInfo.getNewMethodName()));
		
		
		if (isReplace(baseMethodInfo)) {	
			// parameter mappings are handled differently because they depend on
			// the mapping
			if (!hasParameterMapping(baseMethodInfo.getCallinMapping())) {
				// replace callins call the method within the role method body
				copyInvocationParameters(invocation, baseMethodInfo.getCallinMapping().getRoleMethod());
				// if the role method doesn't declare the same number of
				// parameters as the base method, the parameters have to be
				// appended
				int roleMethodParameterLength = baseMethodInfo.getCallinMapping().getRoleMethod().getParameterNames().length;
				int baseMethodParameterLength = baseMethodInfo.getMethod().getParameterNames().length;
				if (roleMethodParameterLength < baseMethodParameterLength) {
					appendInvocationParameters(invocation, baseMethodInfo.getMethod(), roleMethodParameterLength);
				}
			} else {
				CallinMappingDeclaration callinMappingDecl = (CallinMappingDeclaration) RefactoringUtil.methodMappingToDeclaration(baseMethodInfo
						.getCallinMapping(), fRootRole);
				copyBaseParameterMappingsToInvocation(invocation, callinMappingDecl, baseMethodInfo);
			}
		} else {
			// replace and after callins call the base method within the wrapper
			// method of the role
			copyInvocationParameters(invocation, baseMethodInfo.getMethod());
		}


		return invocation;
	}

	private boolean isReplace(CallinBaseMethodInfo baseMethodInfo) {
		return baseMethodInfo.getCallinMapping().getCallinKind() == ICallinMapping.KIND_REPLACE;
	}
	
	private boolean hasParameterMapping(IMethodMapping mapping) throws JavaModelException {
		AbstractMethodMappingDeclaration decl = RefactoringUtil.methodMappingToDeclaration(mapping, fRootRole);
		return decl.hasParameterMapping();
	}

	private void insertMethodIntoBase(MethodDeclaration methodDeclaration, IMethod baseMethod) throws JavaModelException {
		AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) RefactoringUtil.typeToDeclaration(fBaseType, fRootBase);
		ChildListPropertyDescriptor descriptor = typeToBodyDeclarationProperty(fBaseType, fRootBase);
		MethodDeclaration baseMethodDeclaration = RefactoringUtil.methodToDeclaration(baseMethod, fRootBase);
		fBaseRewrite.getListRewrite(declaration, descriptor).insertBefore(methodDeclaration, baseMethodDeclaration, null);
	}
	


	/**
	 * Appends the parameter declarations from the given <code>baseMethod</code>
	 * to the end of the parameter list of the given
	 * <code>roleMethodDeclaration</code>, starting at the given
	 * <code>offset</code>. This method is used if the role method declares
	 * fewer parameters than the base method.
	 * 
	 * @param roleMethodDeclaration
	 *            the method declaration of the copied role method
	 * @param baseMethod
	 *            the base method to copy the parameter declarations from
	 * @param offset
	 *            the offset to start
	 * @throws JavaModelException
	 */
	@SuppressWarnings("unchecked")
	private void appendParameterDeclarations(MethodDeclaration roleMethodDeclaration, IMethod baseMethod, int offset) throws JavaModelException {
		MethodDeclaration baseMethodDecl = RefactoringUtil.methodToDeclaration(baseMethod, fRootBase);
		List<SingleVariableDeclaration> baseParamDeclarations = baseMethodDecl.parameters();
		for (int i = offset; i < baseParamDeclarations.size(); i++) {
			SingleVariableDeclaration paramDecl = (SingleVariableDeclaration) ASTNode.copySubtree(fBaseAST, baseParamDeclarations.get(i));
			
			// generate a parameter name that does not produce a name clash
			List<String> localVarNames = new ArrayList<String>();
			localVarNames.addAll(localVarNamesInRoleMethod());
			localVarNames.addAll(Arrays.asList(fRoleMethod.getParameterNames()));
			String validParameterName = generateVarName(paramDecl.getName().getIdentifier(), localVarNames);
			paramDecl.setName(fBaseAST.newSimpleName(validParameterName));
			fBaseRewrite.getListRewrite(roleMethodDeclaration, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(paramDecl, null);
		}
	}

	@SuppressWarnings("unchecked")
	private void appendTunneledParameterDeclarations(CallinBaseMethodInfo baseMethodInfo, MethodDeclaration copyOfRoleMethodDeclaration)
			throws JavaModelException {
		// append tunneled parameters to roleMethodSignature
		Set<String> tunneledParams = findTunneledParameters(baseMethodInfo);
		// replace tunneled parameters in base method invocations
		MethodDeclaration baseMethodDecl = RefactoringUtil.methodToDeclaration(baseMethodInfo.getMethod(), fRootBase);
		List<SingleVariableDeclaration> baseParamDeclarations = baseMethodDecl.parameters();
		ListRewrite listRewrite = fBaseRewrite.getListRewrite(copyOfRoleMethodDeclaration, MethodDeclaration.PARAMETERS_PROPERTY);
		for (SingleVariableDeclaration varDecl : baseParamDeclarations) {
			String paramName = varDecl.getName().getIdentifier();
			if (tunneledParams.contains(paramName)) {
				String tunneledName = generateTunneledParamName(paramName);
				SingleVariableDeclaration paramDecl = (SingleVariableDeclaration) ASTNode.copySubtree(fBaseAST, varDecl);
				paramDecl.setName(fBaseAST.newSimpleName(tunneledName));
				listRewrite.insertLast(paramDecl, null);
			}
		}
	}

	/**
	 * Appends invocation parameter names from the given method from the given
	 * offset. This method is used to extend base method calls if the role
	 * method declares fewer parameters than the base method.
	 * 
	 * @param invocation
	 *            the method invocation to receive the parameters
	 * @param method
	 *            the method that declares the parameters
	 * @param offset
	 *            the offset to begin to copy
	 * @throws JavaModelException
	 */
	@SuppressWarnings("unchecked")
	private void appendInvocationParameters(MethodInvocation invocation, IMethod method, int offset) throws JavaModelException {
		String[] names = method.getParameterNames();
		
		for (int i = offset; i < names.length; i++) {
			String name = names[i];

			// generate a valid parameter name to prevent name clashes
			List<String> localVarNames = new ArrayList<String>();
			localVarNames.addAll(localVarNamesInRoleMethod());
			localVarNames.addAll(Arrays.asList(fRoleMethod.getParameterNames()));
			String validParameterName = generateVarName(name, localVarNames);
			invocation.arguments().add(fBaseAST.newSimpleName(validParameterName));
		}
	}

	private RefactoringStatus generateNewBaseMethodNames() {
		try {
			generateBaseMethodNames(fTargetBaseMethods);
		} catch (JavaModelException e) {
			return createCouldNotParseStatus();
		}
		return new RefactoringStatus();
	}

	private String generateResultVarName(CallinBaseMethodInfo baseMethodInfo) throws JavaModelException {
		String name = "baseResult";
		List<String> paramNames = Arrays.asList(baseMethodInfo.getMethod().getParameterNames());
		return generateVarName(name, paramNames);
	}

	/**
	 * Generates a name for a parameter or local variable that does not produce
	 * name clashes with a base field, a parameter name or a local variable
	 * name. If there are no name clashes, the method returns the desired name,
	 * otherwise it returns the desired name with an appended number.
	 * 
	 * @param desiredName
	 *            the name to be checked
	 * @param localVarNames
	 *            list of local variable names
	 * @return a name that does not produce a name clash
	 */
	private String generateVarName(String desiredName, List<String> localVarNames) {
		String varName = desiredName;
		
		int i = 2;
		while (fBaseType.getField(varName).exists() || localVarNames.contains(varName)) {
			varName = desiredName + i;
			i++;
		}
		return varName;
	}

	private void generateBaseMethodNames(CallinBaseMethodInfo[] baseMethodInfos) throws JavaModelException {
		for (int i = 0; i < baseMethodInfos.length; i++) {
			String newBaseName = "base_" + fTargetBaseMethods[i].getMethod().getElementName();
			int j = 2;
			while (methodWithNameExists(fBaseType, newBaseName)) {
				newBaseName = "base_" + fTargetBaseMethods[i].getMethod().getElementName() + j;
				j++;
			}
			baseMethodInfos[i].setNewMethodName(newBaseName);
		}
	}

	private String generateTunneledParamName(String identifier) throws JavaModelException {
		String upperCasedName = identifier.substring(0, 1).toUpperCase() + identifier.substring(1);
		List<String> localVarNames = new ArrayList<String>();
		localVarNames.addAll(localVarNamesInRoleMethod());
		localVarNames.addAll(Arrays.asList(fRoleMethod.getParameterNames()));
		String tunneledName = generateVarName("tunneled" + upperCasedName, localVarNames);
		return tunneledName;
	}

	private List<String> localVarNamesInRoleMethod() throws JavaModelException {
		List<String> localVars = new ArrayList<String>();
		MethodDeclaration declaration = RefactoringUtil.methodToDeclaration(fRoleMethod, fRootRole);
		LocalVariableFinder localVariableFinder = new LocalVariableFinder();
		declaration.accept(localVariableFinder);
		localVars = localVariableFinder.getResult();
		return localVars;
	}

	@SuppressWarnings("unchecked")
	private MethodSpec findBaseMethodSpec(CallinMappingDeclaration callinMappingDecl, IMethod baseMethod) {
		MethodSpec baseMethodSpec = null;
		List<MethodSpec> methodSpecs = callinMappingDecl.getBaseMappingElements();
		for (MethodSpec methodSpec : methodSpecs) {
			IMethodBinding methodBinding = methodSpec.resolveBinding();
			IMethod method = (IMethod) methodBinding.getJavaElement();
			if (method.equals(baseMethod)) {
				baseMethodSpec = methodSpec;
			}
		}
		return baseMethodSpec;
	}

	private ChildListPropertyDescriptor typeToBodyDeclarationProperty(IType type, CompilationUnit node) throws JavaModelException {
		ASTNode result = RefactoringUtil.typeToDeclaration(type, node);
		if (result instanceof AbstractTypeDeclaration)
			return ((AbstractTypeDeclaration) result).getBodyDeclarationsProperty();
		else if (result instanceof AnonymousClassDeclaration)
			return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
	
		Assert.isTrue(false);
		return null;
	}

	private class LocalVariableFinder extends ASTVisitor {
	
		private List<String> _locals = new ArrayList<String>();
	
		@Override
		public boolean visit(VariableDeclarationFragment node) {
			_locals.add(node.getName().getIdentifier());
			return false;
		}
	
		public List<String> getResult() {
			return _locals;
		}
	}

	private class SimpleNameFinder extends ASTVisitor {
	
		private List<SimpleName> _simpleNames = new ArrayList<SimpleName>();
		private List<String> _identifier;
	
		public SimpleNameFinder(List<String> identifier) {
			_identifier = identifier;
		}
	
		@Override
		public boolean visit(SimpleName node) {
			if (_identifier.contains(node.getIdentifier())) {
				_simpleNames.add(node);
			}
			return false;
		}
	
		public List<SimpleName> getResult() {
			return _simpleNames;
		}
	}

	private class MethodInvocationFinder extends ASTVisitor {
	
		private List<MethodInvocation> _methodInvocations = new ArrayList<MethodInvocation>();
	
		@Override
		public boolean visit(MethodInvocation node) {
			_methodInvocations.add(node);
			return false;
		}
	
		public List<MethodInvocation> getResult() {
			return _methodInvocations;
		}
	}

	private class ReturnFinder extends ASTVisitor {
	
		private List<ReturnStatement> _returns = new ArrayList<ReturnStatement>();
	
		@Override
		public boolean visit(ReturnStatement node) {
			_returns.add(node);
			return false;
		}
	
		public List<ReturnStatement> getResult() {
			return _returns;
		}
	}
}
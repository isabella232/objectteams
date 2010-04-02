package org.eclipse.objectteams.otdt.internal.refactoring.adaptor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IAmbuguityMessageCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IOverloadingMessageCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.util.RefactoringUtil;

import base org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;

/**
 * @author Johannes Gebauer
 *
 */
@SuppressWarnings("restriction")
public team class MoveInstanceMethodAdaptor {

	
	@SuppressWarnings("decapsulation")
	protected class MoveInstanceMethodProcessor playedBy MoveInstanceMethodProcessor {
		
		// callouts
		IMethod getFMethod() -> get IMethod fMethod;
		IMethod getMethod() -> IMethod getMethod();
		String getMethodName() -> String getMethodName();
		IType getTargetType() -> IType getTargetType();

		void checkFinalConditions(IProgressMonitor pm, RefactoringStatus result) throws CoreException {
			if(!result.hasFatalError())
				result.merge(RefactoringUtil.checkForExistingRoles("Move Instance Method", getFMethod().getJavaProject(), pm));
			pm.beginTask("Checking Overloading", 1);
			pm.subTask(""); //$NON-NLS-1$
			result.merge(checkOverloadingAndAmbiguity(pm));
			pm.worked(1);
			pm.done();
		}

		void checkFinalConditions(IProgressMonitor pm, RefactoringStatus status) <- after RefactoringStatus checkFinalConditions(IProgressMonitor pm,
				CheckConditionsContext context) with {
			pm <- pm,
			status <- result
		}

		private RefactoringStatus checkOverloadingAndAmbiguity(IProgressMonitor pm) throws JavaModelException {
			
			String[] paramTypes = getMethod().getParameterTypes();

			return RefactoringUtil.checkOverloadingAndAmbiguity(getTargetType(), null /* targetTypeHierarchy */, getMethodName(), paramTypes,
					new IAmbuguityMessageCreator() {

						public String createAmbiguousMethodSpecifierMsg() {
							return "Refactoring cannot be performed! There would be an ambiguous method specifier in a method binding after moving!";
						}

					}, new IOverloadingMessageCreator() {

						public String createOverloadingMessage() {
							return "Moved method will be overloaded after refactoring!";
						}

					}, pm);
		}
	}
}

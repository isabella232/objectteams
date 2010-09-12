package org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.inlinecallin;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class InlineCallinWizard extends RefactoringWizard {

	public InlineCallinWizard(InlineCallinRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	@Override
	protected void addUserInputPages() {
		addPage(new InlineCallinInputPage("InlineCallinInputPage"));
	}
}
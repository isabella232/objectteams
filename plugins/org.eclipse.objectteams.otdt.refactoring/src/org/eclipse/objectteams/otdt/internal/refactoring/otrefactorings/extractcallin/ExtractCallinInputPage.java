package org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.extractcallin;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;

public class ExtractCallinInputPage extends UserInputWizardPage {


	private Text fNameField;
	
	private Combo fTypeCombo;

	private Button fReferenceButton;

	private List<IRoleType> fCandidateRoles;

	private ExtractCallinRefactoring fRefactoring;

	private Group fExtractMode;

	private Button fReplaceRadio;

	private Button fBeforeRadio;

	private Button fAfterRadio;

	private Button fRemove;
	

	public ExtractCallinInputPage(String name) {
		super(name);
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		fRefactoring = (ExtractCallinRefactoring) getRefactoring();
		Composite result = new Composite(parent, SWT.NONE);

		setControl(result);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout(layout);

		Label label = new Label(result, SWT.NONE);
		label.setText("&Role method name:");

		fNameField = createNameField(result);
		
		// Role Selection
		label = new Label(result, SWT.NONE);
		label.setText("&Target role:");

		Composite composite = new Composite(result, SWT.NONE);
		
		fCandidateRoles = fRefactoring.getCandidateRoles();
		
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fTypeCombo = createTypeCombo(composite);
		fTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		for (IRoleType role : fCandidateRoles) {
			final String comboLabel = role.getFullyQualifiedName('.');
			fTypeCombo.add(comboLabel);
		}
		fTypeCombo.select(fCandidateRoles.size() - 1);
		
		
		createRadioButtonGroup(result);

		fReferenceButton = new Button(result, SWT.CHECK);
		fReferenceButton.setEnabled(true);
		fReferenceButton.setText("&Delete extracted base method");
		fReferenceButton.addSelectionListener(new SelectionAdapter() {
			
		@Override
		public void widgetSelected(SelectionEvent event) {
				fRefactoring.setDeleteBaseMethod(fReferenceButton.getSelection());
			}
		});
		
		
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.verticalIndent = 2;
		fReferenceButton.setLayoutData(data);

		fNameField.setText(fRefactoring.getBaseMethod().getElementName());
		fTypeCombo.setText(fRefactoring.getBaseMethod().getDeclaringType().getFullyQualifiedName());

		fNameField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				handleInputChanged();
			}
		});

		fReferenceButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				fRefactoring.setDeleteBaseMethod(fReferenceButton.getSelection());
			}
		});

		fTypeCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				handleInputChanged();
			}
		});

		fNameField.setFocus();
		fNameField.selectAll();
		fBeforeRadio.setEnabled(fRefactoring.isExtractBeforeAvailable());
		fAfterRadio.setEnabled(fRefactoring.isExtractAfterAvailable());
		handleInputChanged();
		fReferenceButton.setSelection(fRefactoring.isDeleteBaseMethod());
	}

	private void createRadioButtonGroup(Composite result) {
		fExtractMode = new Group(result, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.verticalIndent = 2;
		fExtractMode.setLayoutData(data);
		fExtractMode.setLayout(new GridLayout());
		fExtractMode.setText("Extract Mode");

		fReplaceRadio = new Button(fExtractMode, SWT.RADIO);
		fReplaceRadio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fReplaceRadio.setText("R&eplace");
		fReplaceRadio.setSelection(true);
		fRefactoring.setMappingKind(ICallinMapping.KIND_REPLACE);
		fReplaceRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (((Button) event.widget).getSelection()) {
					fRefactoring.setMappingKind(ICallinMapping.KIND_REPLACE);
					fRemove.setEnabled(true);
					handleInputChanged();
				}
			}
		});
		fRemove = new Button(fExtractMode, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = convertWidthInCharsToPixels(3);
		fRemove.setLayoutData(data);
		fRemove.setText("&Copy base method code");
		fRemove.setSelection(fRefactoring.isCopyBaseMethod());
		fRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fRefactoring.setCopyBaseMethod(((Button) e.widget).getSelection());
				handleInputChanged();
			}
		});

		fBeforeRadio = new Button(fExtractMode, SWT.RADIO);
		fBeforeRadio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fBeforeRadio.setText("&Before");
		fBeforeRadio.setSelection(false);
		fBeforeRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				fRemove.setEnabled(false);
				if (((Button) event.widget).getSelection()){
					fRefactoring.setMappingKind(ICallinMapping.KIND_BEFORE);
					handleInputChanged();
				}
			}
		});

		fAfterRadio = new Button(fExtractMode, SWT.RADIO);
		fAfterRadio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fAfterRadio.setText("&After");
		fAfterRadio.setSelection(false);
		fAfterRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				fRemove.setEnabled(false);
				if (((Button) event.widget).getSelection()){
					fRefactoring.setMappingKind(ICallinMapping.KIND_AFTER);
					handleInputChanged();
				}
			}
		});
	}

	private Text createNameField(Composite result) {
		Text field = new Text(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return field;
	}

	private Combo createTypeCombo(Composite composite) {
		Combo combo = new Combo(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setVisibleItemCount(4);
		return combo;
	}

	void handleInputChanged() {
		fReferenceButton.setEnabled(fRefactoring.getMappingKind() != ICallinMapping.KIND_REPLACE);
		fRefactoring.setRoleType(getSelectedRole());
		
		RefactoringStatus status = new RefactoringStatus();
		fRefactoring.setRoleMethodName(fNameField.getText());
		status.merge(fRefactoring.checkRoleMethodName());

		setPageComplete(!status.hasError());
		int severity = status.getSeverity();
		String message = status.getMessageMatchingSeverity(severity);
		if (severity >= RefactoringStatus.INFO) {
			setMessage(message, severity);
		} else {
			setMessage("", NONE); //$NON-NLS-1$
		}
	}
	
	public IType getSelectedRole() {
		final int index = fTypeCombo.getSelectionIndex();
		if (index >= 0)
			return fCandidateRoles.get(index);
		return null;
	}
}
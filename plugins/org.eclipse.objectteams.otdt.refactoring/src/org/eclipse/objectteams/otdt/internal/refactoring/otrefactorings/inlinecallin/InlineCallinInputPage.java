package org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.inlinecallin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jdt.internal.ui.util.TableLayoutComposite;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.objectteams.otdt.core.ICallinMapping;

@SuppressWarnings("restriction")
public class InlineCallinInputPage extends UserInputWizardPage {


	Text fNameField;

	private CheckboxTableViewer fTableViewer;

	private Label fLabel;

	private Button fReferenceButton;

	public InlineCallinInputPage(String name) {
		super(name);
	}

	public void createControl(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);

		setControl(result);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout(layout);

		Label label = new Label(result, SWT.NONE);
		label.setText("&Inlined method name:");

		fNameField = createNameField(result);

		createMemberTableLabel(result);
		createMemberTableComposite(result);
		
		fReferenceButton = new Button(result, SWT.CHECK);
		fReferenceButton.setEnabled(false);
		fReferenceButton.setText("&Delete role method");
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.verticalIndent = 2;
		fReferenceButton.setLayoutData(data);


		final InlineCallinRefactoring refactoring = getInlineCallinRefactoring();
		fNameField.setText(refactoring.getRoleMethod().getElementName());

		fNameField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				handleInputChanged();
			}
		});

		fReferenceButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				refactoring.setDeleteRoleMethod(fReferenceButton.getSelection());
			}
		});

		fNameField.setFocus();
		fNameField.selectAll();
		handleInputChanged();
		fReferenceButton.setSelection(false);
	}

	private Text createNameField(Composite result) {
		Text field = new Text(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return field;
	}

	private InlineCallinRefactoring getInlineCallinRefactoring() {
		return (InlineCallinRefactoring) getRefactoring();
	}

	void handleInputChanged() {
		fReferenceButton.setEnabled(allBaseMethodSelected());
		
		RefactoringStatus status = new RefactoringStatus();
		InlineCallinRefactoring refactoring = getInlineCallinRefactoring();
		refactoring.setRoleMethodName(fNameField.getText());
		status.merge(refactoring.checkRoleMethodName());
		
		setSelectedBaseMethods(refactoring);
		status.merge(refactoring.checkBaseMethods());

		setPageComplete(!status.hasError());
		int severity = status.getSeverity();
		String message = status.getMessageMatchingSeverity(severity);
		if (severity >= RefactoringStatus.INFO) {
			setMessage(message, severity);
		} else {
			setMessage("", NONE); //$NON-NLS-1$
		}
	}

	private void setSelectedBaseMethods(InlineCallinRefactoring refactoring) {
		CallinBaseMethodInfo[] baseMethodInfos = getTableInput();
		List<CallinBaseMethodInfo> baseMethods = new ArrayList<CallinBaseMethodInfo>();
		for (int i = 0; i < baseMethodInfos.length; i++) {
			if (fTableViewer.getChecked(baseMethodInfos[i]))
				baseMethods.add(baseMethodInfos[i]);
		}
		refactoring.setBaseMethods(baseMethods.toArray(new CallinBaseMethodInfo[baseMethods.size()]));
	}
	
	private CallinBaseMethodInfo[] getTableInput() {
		return (CallinBaseMethodInfo[]) fTableViewer.getInput();
	}

	private boolean allBaseMethodSelected() {
		boolean selected = true;
		Object[] baseMethods = (Object[]) fTableViewer.getInput();
		for (int i = 0; i < baseMethods.length; i++) {
			selected = selected && fTableViewer.getChecked(baseMethods[i]); 
		}
		return selected;
	}

	protected void createMemberTableLabel(final Composite parent) {
		fLabel = new Label(parent, SWT.NONE);
		fLabel.setText("&Select the bound base methods:");
		final GridData data = new GridData();
		data.horizontalSpan = 2;
		fLabel.setLayoutData(data);
	}
	
	protected void createMemberTableComposite(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		createBaseMethodsTable(composite);
	}
	
	private void createBaseMethodsTable(final Composite parent) {
		final TableLayoutComposite layouter = new TableLayoutComposite(parent, SWT.NONE);
		layouter.addColumnData(new ColumnWeightData(60, true));
		layouter.addColumnData(new ColumnWeightData(40, true));

		final Table table = new Table(layouter, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = SWTUtil.getTableHeightHint(table, 10);
		gd.widthHint = convertWidthInCharsToPixels(30);
		layouter.setLayoutData(gd);

		final TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);

		final TableColumn column0 = new TableColumn(table, SWT.NONE);
		column0.setText("Base Method");

		final TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText("Callin Kind");

		fTableViewer = new CheckboxTableViewer(table);
		fTableViewer.setUseHashlookup(true);
		fTableViewer.setContentProvider(new ArrayContentProvider());
		fTableViewer.setLabelProvider(new BaseMethodInfoLabelProvider());
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(final SelectionChangedEvent event) {
				handleInputChanged();
			}
		});
		fTableViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(final CheckStateChangedEvent event) {
				updateWizardPage(null, true);
			}
		});

		setTableInput();
	}

	private static class BaseMethodInfoLabelProvider extends LabelProvider implements ITableLabelProvider {

		private static final String BEFORE_LABEL = "Before";
		private static final String AFTER_LABEL = "After";
		private static final String REPLACE_LABEL = "Replace";
		
		private static final int CALLIN_KIND_COLUMN = 1;
		private static final int METHOD_COLUMN = 0;
		
		private final ILabelProvider fLabelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT
				| JavaElementLabelProvider.SHOW_SMALL_ICONS);

		public void dispose() {
			super.dispose();
			fLabelProvider.dispose();
		}

		public Image getColumnImage(final Object element, final int columnIndex) {
			final CallinBaseMethodInfo info = (CallinBaseMethodInfo) element;
			switch (columnIndex) {
			case METHOD_COLUMN:
				return fLabelProvider.getImage(info.getMethod());
			case CALLIN_KIND_COLUMN:
				return null;
			default:
				return null;
			}
		}

		public String getColumnText(final Object element, final int columnIndex) {
			final CallinBaseMethodInfo info = (CallinBaseMethodInfo) element;
			switch (columnIndex) {
			case METHOD_COLUMN:
				return fLabelProvider.getText(info.getMethod());
			case CALLIN_KIND_COLUMN:
				return createCallinLabel(info.getCallinMapping().getCallinKind());
			default:
				return null;
			}
		}

		public String createCallinLabel(int callinKind) {
			switch (callinKind) {
			case ICallinMapping.KIND_BEFORE:
				return BEFORE_LABEL;
			case ICallinMapping.KIND_AFTER:
				return AFTER_LABEL;
			case ICallinMapping.KIND_REPLACE:
				return REPLACE_LABEL;
			default:
				return null;
			}
		}
	}

	private CallinBaseMethodInfo[] getBaseMethodInfos() {
		return getInlineCallinRefactoring().getBaseMethodInfos();
	}

	private void setTableInput() {
		fTableViewer.setInput(getBaseMethodInfos());
	}

	private void updateWizardPage(final ISelection selection, final boolean displayErrors) {
		fTableViewer.refresh();
		if (selection != null) {
			fTableViewer.getControl().setFocus();
			fTableViewer.setSelection(selection);
		}
	}
}
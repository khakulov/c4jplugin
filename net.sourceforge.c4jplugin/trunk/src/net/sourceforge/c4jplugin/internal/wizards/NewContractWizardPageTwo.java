package net.sourceforge.c4jplugin.internal.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

public class NewContractWizardPageTwo extends WizardPage {

	private final static String PAGE_NAME= "NewContractWizardPage2"; //$NON-NLS-1$
	
	private final static String STORE_USE_TASKMARKER= PAGE_NAME + ".USE_TASKMARKER"; //$NON-NLS-1$
	private final static String STORE_CREATE_FINAL_METHOD_STUBS= PAGE_NAME + ".CREATE_FINAL_METHOD_STUBS"; //$NON-NLS-1$

	private final String PROP_METHOD_COL = "methods"; //$NON-NLS-1$
	private final String PROP_PRE_COL = "pre"; //$NON-NLS-1$
	private final String PROP_POST_COL = "post"; //$NON-NLS-1$
	
	private IType fClassToContract;

	private Button fCreateFinalMethodStubsButton;
	private Button fCreateTasksButton;
	private ContainerCheckedTreeViewer fMethodsTree;
	private Button fSelectAllButton;
	private Button fDeselectAllButton;
	private Label fSelectedMethodsLabel;
	private Object[] fCheckedObjects = new Object[] {};
	private boolean fCreateFinalStubs;
	private boolean fCreateTasks;
	
	/**
	 * Creates a new <code>NewContractWizardPageTwo</code>.
	 */
	public NewContractWizardPageTwo() {
		super(PAGE_NAME);
		setTitle(WizardMessages.NewContractWizardPageTwo_title); 
		setDescription(WizardMessages.NewContractWizardPageTwo_description); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite container= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		container.setLayout(layout);

		createMethodsTreeControls(container);
		createSpacer(container);
		createButtonChoices(container);	
		setControl(container);
		restoreWidgetValues();
		Dialog.applyDialogFont(container);
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(container, IJUnitHelpContextIds.NEW_TESTCASE_WIZARD_PAGE2);	
	}

	private void createButtonChoices(Composite container) {
		GridLayout layout;
		GridData gd;
		Composite prefixContainer= new Composite(container, SWT.NONE);
		gd= new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 1;
		prefixContainer.setLayoutData(gd);
		
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		prefixContainer.setLayout(layout);
		
		SelectionListener listener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doCheckBoxSelected(e.widget);
			}
		};
		fCreateFinalMethodStubsButton= createCheckBox(prefixContainer, WizardMessages.NewContractWizardPageTwo_create_final_method_stubs_text, listener); 
		fCreateTasksButton= createCheckBox(prefixContainer, WizardMessages.NewContractWizardPageTwo_create_tasks_text, listener); 
	}
	
	private Button createCheckBox(Composite parent, String name, SelectionListener listener) {
		Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(name); 
		button.setEnabled(true);
		button.setSelection(true);
		button.addSelectionListener(listener);
		GridData gd= new GridData(GridData.FILL, GridData.CENTER, false, false);
		button.setLayoutData(gd);
		return button;
	}
	
	
	private void doCheckBoxSelected(Widget widget) {
		if (widget == fCreateFinalMethodStubsButton) {
			fCreateFinalStubs= fCreateFinalMethodStubsButton.getSelection();
		} else if (widget == fCreateTasksButton) {
			fCreateTasks= fCreateTasksButton.getSelection();
		}
		saveWidgetValues();
	}
	
	private void createMethodsTreeControls(Composite container) {
		Label label= new Label(container, SWT.LEFT | SWT.WRAP);
		label.setFont(container.getFont());
		label.setText(WizardMessages.NewContractWizardPageTwo_methods_tree_label); 
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fMethodsTree= new ContainerCheckedTreeViewer(container, SWT.BORDER);
		gd= new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.heightHint= 180;
		fMethodsTree.getTree().setLayoutData(gd);
		
		fMethodsTree.getTree().setHeaderVisible(true);
		TreeColumn colMember = new TreeColumn(fMethodsTree.getTree(), SWT.NONE);
		colMember.setText(WizardMessages.NewContractWizardPageTwo_columnheader_methods);
		
		TreeColumn colPre = new TreeColumn(fMethodsTree.getTree(), SWT.NONE);
		colPre.setText(WizardMessages.NewContractWizardPageTwo_columnheader_pre);
		
		TreeColumn colPost = new TreeColumn(fMethodsTree.getTree(), SWT.NONE);
		colPost.setText(WizardMessages.NewContractWizardPageTwo_columnheader_post);
		
		colMember.setWidth(250);
		colPre.setWidth(150);
		colPost.setWidth(150);
		
		ComboBoxCellEditor cellEditorPre = new ComboBoxCellEditor(fMethodsTree.getTree(), NewContractLabelProvider.preCondLabels);
		ComboBoxCellEditor cellEditorPost = new ComboBoxCellEditor(fMethodsTree.getTree(), NewContractLabelProvider.postCondLabels);
		
		fMethodsTree.setColumnProperties(new String[] {PROP_METHOD_COL, PROP_PRE_COL, PROP_POST_COL});
		fMethodsTree.setCellEditors(new CellEditor[] {null, cellEditorPre, cellEditorPost});
		fMethodsTree.setCellModifier(new MethodCellModifier());
		
		fMethodsTree.setLabelProvider(new NewContractLabelProvider());
		fMethodsTree.setAutoExpandLevel(2);			
		fMethodsTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				doCheckedStateChanged();
			}	
		});
		fMethodsTree.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				NewContractMethodElement item = (NewContractMethodElement)element;
				if (item.getMember() instanceof IMethod) {
					IMethod method = (IMethod) item.getMember();
					return !method.getElementName().equals("<clinit>"); //$NON-NLS-1$
				}
				return true;
			}
		});


		Composite buttonContainer= new Composite(container, SWT.NONE);
		gd= new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout= new GridLayout();
		buttonLayout.marginWidth= 0;
		buttonLayout.marginHeight= 0;
		buttonContainer.setLayout(buttonLayout);

		fSelectAllButton= new Button(buttonContainer, SWT.PUSH);
		fSelectAllButton.setText(WizardMessages.NewContractWizardPageTwo_selectAll); 
		gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		fSelectAllButton.setLayoutData(gd);
		fSelectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fMethodsTree.setCheckedElements((Object[]) fMethodsTree.getInput());
				doCheckedStateChanged();
			}
		});
		//LayoutUtil.setButtonDimensionHint(fSelectAllButton);

		fDeselectAllButton= new Button(buttonContainer, SWT.PUSH);
		fDeselectAllButton.setText(WizardMessages.NewContractWizardPageTwo_deselectAll); 
		gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		fDeselectAllButton.setLayoutData(gd);
		fDeselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fMethodsTree.setCheckedElements(new Object[0]);
				doCheckedStateChanged();
			}
		});
		//LayoutUtil.setButtonDimensionHint(fDeselectAllButton);

		/* No of selected methods label */
		fSelectedMethodsLabel= new Label(container, SWT.LEFT);
		fSelectedMethodsLabel.setFont(container.getFont());
		doCheckedStateChanged();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 1;
		fSelectedMethodsLabel.setLayoutData(gd);
		
		Label emptyLabel= new Label(container, SWT.LEFT);
		gd= new GridData();
		gd.horizontalSpan= 1;
		emptyLabel.setLayoutData(gd);
	}

	private void createSpacer(Composite container) {
		Label spacer= new Label(container, SWT.NONE);
		GridData data= new GridData();
		data.horizontalSpan= 2;
		data.horizontalAlignment= GridData.FILL;
		data.verticalAlignment= GridData.BEGINNING;
		data.heightHint= 4;
		spacer.setLayoutData(data);
	}
	
	/**
	 * Sets the class under test.
	 * 
	 * @param classUnderTest the class under test
	 */
	public void setClassUnderTest(IType classUnderTest) {
		fClassToContract= classUnderTest;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			if (fClassToContract == null) {
				return;
			}
			
			ArrayList<IType> types= null;
			try {
				ITypeHierarchy hierarchy= fClassToContract.newSupertypeHierarchy(null);
				IType[] superTypes;
				if (fClassToContract.isClass())
					superTypes= hierarchy.getAllSuperclasses(fClassToContract);
				else if (fClassToContract.isInterface())
					superTypes= hierarchy.getAllSuperInterfaces(fClassToContract);
				else
					superTypes= new IType[0];
				types= new ArrayList<IType>(superTypes.length+1);
				types.add(fClassToContract);
				types.addAll(Arrays.asList(superTypes));
			} catch(JavaModelException e) {
				C4JActivator.log(e);
			}
			if (types == null)
				types= new ArrayList<IType>();
			MethodsTreeContentProvider contentProvider = new MethodsTreeContentProvider(types.toArray(new IType[] {}));
			fMethodsTree.setContentProvider(contentProvider);
			fMethodsTree.setInput(types.toArray());
			
			NewContractMethodElement selElement = contentProvider.getElementFromType(fClassToContract);
			if (selElement != null)
				fMethodsTree.setSelection(new StructuredSelection(selElement), true);
			doCheckedStateChanged();
			
			fMethodsTree.getControl().setFocus();
		}
	}
	
	

	/**
	 * Returns all checked methods in the methods tree.
	 * 
	 * @return the checked methods
	 */
	public NewContractMethodElement[] getCheckedElements() {
		int methodCount= 0;
		for (Object object : fCheckedObjects) {
			if (((NewContractMethodElement)object).getMember() instanceof IMethod)
				methodCount++;
		}
		NewContractMethodElement[] checkedElements= new NewContractMethodElement[methodCount];
		int j= 0;
		for (Object object : fCheckedObjects) {
			if (((NewContractMethodElement)object).getMember() instanceof IMethod) {
				checkedElements[j]= (NewContractMethodElement)object;
				j++;
			}
		}
		return checkedElements;
	}
	
	private static class MethodsTreeContentProvider implements ITreeContentProvider {
		private IType[] fTypes;
		private NewContractMethodElement[] fElements;
		private IMethod[] fMethods;
		private final Object[] fEmpty= new Object[0];

		public MethodsTreeContentProvider(IType[] types) {
			fTypes= types;
			fElements = new NewContractMethodElement[fTypes.length];
			for (int i = 0; i < fTypes.length; i++) {
				fElements[i] = new NewContractMethodElement(fTypes[i]);
			}
			
			Vector<IMethod> methods= new Vector<IMethod>();
			for (int i = types.length-1; i > -1; i--) {
				Object object = types[i];
				if (object instanceof IType) {
					IType type = (IType) object;
					try {
						IMethod[] currMethods= type.getMethods();
						for_currMethods:
						for (int j = 0; j < currMethods.length; j++) {
							IMethod currMethod = currMethods[j];
							int flags= currMethod.getFlags();
							if (!Flags.isPrivate(flags) && !Flags.isSynthetic(flags)) {
								for (int k = 0; k < methods.size(); k++) {
									IMethod m= ((IMethod)methods.get(k));
									if (m.getElementName().equals(currMethod.getElementName())
										&& m.getSignature().equals(currMethod.getSignature())) {
										methods.set(k,currMethod);
										continue for_currMethods;
									}
								}
								methods.add(currMethod);
							}
						}
					} catch (JavaModelException e) {
						C4JActivator.log(e);
					}
				}
			}
			fMethods= new IMethod[methods.size()];
			methods.copyInto(fMethods);
		}
		
		public NewContractMethodElement getElementFromType(IType type) {
			for (NewContractMethodElement element : fElements) {
				if (element.getMember().equals(type)) return element;
			}
			return null;
		}
		
		/*
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parentElement) {
			NewContractMethodElement parent = (NewContractMethodElement)parentElement;
			if (parent.getMember() instanceof IType) {
				IType parentType= (IType)parent.getMember();
				ArrayList<NewContractMethodElement> result= new ArrayList<NewContractMethodElement>(fMethods.length);
				for (IMethod curMethod : fMethods) {
					if (curMethod.getDeclaringType().equals(parentType)) {
						result.add(new NewContractMethodElement(curMethod));
					}
				}
				return result.toArray();
			}
			return fEmpty;
		}

		/*
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object element) {
			NewContractMethodElement item = (NewContractMethodElement)element;
			if (item.getMember() instanceof IMethod) {
				IType parentType = ((IMethod)item.getMember()).getDeclaringType();
				for (NewContractMethodElement curElement : fElements) {
					if (curElement.getMember().equals(parentType))
						return curElement;
				}
			}
			return null;
		}

		/*
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fElements;
		}

		/*
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public IMethod[] getAllMethods() {
			return fMethods;
		}
	}

	private class MethodCellModifier implements ICellModifier {
		
		public boolean canModify(Object element, String property) {
			if (((NewContractMethodElement)element).getMember() instanceof IType)
				return false;
			
			if (property.equals(PROP_METHOD_COL))
				return false;
			
			return true;
		}

		public Object getValue(Object element, String property) {
			if (property.equals(PROP_PRE_COL)) {
				return ((NewContractMethodElement)element).getPreCondition();
			}
			else if(property.equals(PROP_POST_COL)) {
				return ((NewContractMethodElement)element).getPostCondition();
			}
			
			return null;
		}

		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
		         element = ((Item) element).getData();
		    }
			
			if (property.equals(PROP_PRE_COL)) {
				((NewContractMethodElement)element).setPreCondition((Integer)value);
			}
			else if (property.equals(PROP_POST_COL)) {
				((NewContractMethodElement)element).setPostCondition((Integer)value);
			}
			
			fMethodsTree.update(element, null);
		}
	}
	
	/**
	 * Returns true if the checkbox for creating tasks is checked.
	 * 
	 * @return <code>true</code> is returned if tasks should be created
	 */
	public boolean isCreateTasks() {
		return fCreateTasks;
	}

	/**
	 * Returns true if the checkbox for final method stubs is checked.
	 * @return <code>true</code> is returned if methods should be created final
	 */
	public boolean getCreateFinalMethodStubsButtonSelection() {
		return fCreateFinalStubs;
	}
		
	private void doCheckedStateChanged() {
		Object[] checked= fMethodsTree.getCheckedElements();
		fCheckedObjects= checked;
		
		int checkedMethodCount= 0;
		for (int i= 0; i < checked.length; i++) {
			NewContractMethodElement element = (NewContractMethodElement)checked[i];
			if (element.getMember() instanceof IMethod)
				checkedMethodCount++;
		}
		String label= ""; //$NON-NLS-1$
		if (checkedMethodCount == 1)
			label= NLS.bind(WizardMessages.NewContractWizardPageTwo_selected_methods_label_one, new Integer(checkedMethodCount)); 
		else
			label= NLS.bind(WizardMessages.NewContractWizardPageTwo_selected_methods_label_many, new Integer(checkedMethodCount)); 
		fSelectedMethodsLabel.setText(label);
	}
		
	/**
	 *	Use the dialog store to restore widget values to the values that they held
	 *	last time this wizard was used to completion
	 */
	private void restoreWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			fCreateTasks= settings.getBoolean(STORE_USE_TASKMARKER);
			fCreateTasksButton.setSelection(fCreateTasks);
			fCreateFinalStubs= settings.getBoolean(STORE_CREATE_FINAL_METHOD_STUBS);
			fCreateFinalMethodStubsButton.setSelection(fCreateFinalStubs);
		}		
	}	

	private void saveWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			settings.put(STORE_USE_TASKMARKER, fCreateTasks);
			settings.put(STORE_CREATE_FINAL_METHOD_STUBS, fCreateFinalStubs);
		}
	}

}

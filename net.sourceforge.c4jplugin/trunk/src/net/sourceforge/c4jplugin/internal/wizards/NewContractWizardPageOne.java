package net.sourceforge.c4jplugin.internal.wizards;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.core.ContractReferenceModel;
import net.sourceforge.c4jplugin.internal.util.AnnotationUtil;
import net.sourceforge.c4jplugin.internal.util.C4JStubUtil;
import net.sourceforge.c4jplugin.internal.util.C4JUtils;
import net.sourceforge.c4jplugin.internal.util.C4JStubUtil.GenStubSettings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaTypeCompletionProcessor;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.SelectionDialog;

public class NewContractWizardPageOne extends NewTypeWizardPage {

	private final static String PAGE_NAME = "NewContractWizardPage"; //$NON-NLS-1$

	
	/** Field ID of the class under test field. */
	public final static String TARGET_CLASS = PAGE_NAME + ".targetclass"; //$NON-NLS-1$
	
		
	private static final String COMPLIANCE_PAGE_ID = "org.eclipse.jdt.ui.propertyPages.CompliancePreferencePage"; //$NON-NLS-1$
	private static final String BUILD_PATH_PAGE_ID = "org.eclipse.jdt.ui.propertyPages.BuildPathsPropertyPage"; //$NON-NLS-1$
	
	private final static String CONTRACT_SUFFIX = "Contract"; //$NON-NLS-1$
	
	private final static String STORE_CONSTRUCTOR = PAGE_NAME + ".USE_CONSTRUCTOR"; //$NON-NLS-1$
	private final static String STORE_SUPERCLASS = PAGE_NAME + ".USE_SUPERCLASS"; //$NON-NLS-1$
	
	private Button fConstructorButton;
	private Button fSuperClassButton;
	
	private NewContractWizardPageTwo fPage2;
	
	private String fClassUnderContractText; // model
	private IType fClassUnderContract; // resolved model, can be null
	
	private Text fClassUnderContractControl; // control
	private IStatus fClassUnderContractStatus; // status
	
	private Button fClassUnderContractButton;
	private JavaTypeCompletionProcessor fClassToContractCompletionProcessor;

	private Link fLink;
	private Label fImage;

	/**
	 * Creates a new <code>NewContractWizardPage</code>.
	 * @param page2 The second page
	 * 
	 */
	public NewContractWizardPageOne(NewContractWizardPageTwo page2) {
		super(true, PAGE_NAME);
		fPage2= page2;
		
		setTitle(WizardMessages.NewContractWizardPageOne_title); 
		setDescription(WizardMessages.NewContractWizardPageOne_description); 
		
		enableCommentControl(true);
		
		fClassToContractCompletionProcessor= new JavaTypeCompletionProcessor(false, false); 

		fClassUnderContractStatus= new C4JStatus();
		
		fClassUnderContractText= ""; //$NON-NLS-1$
	}

	/**
	 * Initialized the page with the current selection
	 * @param selection The selection
	 */
	public void init(IStructuredSelection selection) {
		IJavaElement element= getInitialJavaElement(selection);

		initContainerPage(element);
		initTypePage(element);
		// put default class to test		
		if (element != null) {
			IType classToContract= null;
			// evaluate the enclosing type
			IType typeInCompUnit= (IType) element.getAncestor(IJavaElement.TYPE);
			if (typeInCompUnit != null) {
				if (typeInCompUnit.getCompilationUnit() != null) {
					classToContract= typeInCompUnit;
				}
			} else {
				ICompilationUnit cu= (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
				if (cu != null) 
					classToContract= cu.findPrimaryType();
				else {
					if (element instanceof IClassFile) {
						try {
							IClassFile cf= (IClassFile) element;
							if (cf.isStructureKnown())
								classToContract= cf.getType();
						} catch(JavaModelException e) {
							C4JActivator.log(e);
						}
					}					
				}
			}
			if (classToContract != null) {
				try {
					if (!ContractReferenceModel.isTarget(classToContract.getCompilationUnit().getCorrespondingResource()))
						setClassUnderContract(classToContract.getFullyQualifiedName('.'));
				} catch (JavaModelException e) {
					// ignore
				}
			}
		}

		//restoreWidgetValues();
		
		updateStatus(getStatusList());
	}
	
	private void handleLinks(Object data) {
		
		IPackageFragmentRoot root= getPackageFragmentRoot();
		if (root == null) {
			return; // should not happen. Link shouldn't be visible
		}
		final IJavaProject javaProject= root.getJavaProject();
		
		if ("c".equals(data)) { // open compliance //$NON-NLS-1$
			PreferencesUtil.createPropertyDialogOn(getShell(), javaProject, COMPLIANCE_PAGE_ID, new String[] { BUILD_PATH_PAGE_ID, COMPLIANCE_PAGE_ID  }, data).open();
		}
		else if ("a".equals(data)) { // enable C4J  //$NON-NLS-1$
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				protected void execute(IProgressMonitor monitor)
						throws CoreException {
					C4JUtils.addC4JNature(javaProject.getProject());
				}
			};
			try {
				op.run(null);
			} catch (InvocationTargetException ex) {
			} catch (InterruptedException e) {
			}
		}
		
		updateC4JNatureMessage();
	}
		
	private void createEmptySpace(Composite parent, int span) {
		Label label= new Label(parent, SWT.LEFT);
		GridData gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gridData.horizontalIndent= 0;
		gridData.horizontalSpan = span;
		gridData.widthHint= 0;
		gridData.heightHint= 0;
		label.setLayoutData(gridData);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.NewContainerWizardPage#handleFieldChanged(String)
	 */
	protected void handleFieldChanged(String fieldName) {
		super.handleFieldChanged(fieldName);
		if (fieldName.equals(CONTAINER)) {
			fClassUnderContractStatus= classUnderContractChanged();
			if (fClassUnderContractButton != null && !fClassUnderContractButton.isDisposed()) {
				fClassUnderContractButton.setEnabled(getPackageFragmentRoot() != null);
			}
			
			updateC4JNatureMessage();
		} 
		updateStatus(getStatusList());
	}

	/**
	 * Returns all status to be consider for the validation. Clients can override.
	 * @return The list of status to consider for the validation.
	 */
	protected IStatus[] getStatusList() {
		return new IStatus[] {
				fContainerStatus,
				fPackageStatus,
				fClassUnderContractStatus,
				fTypeNameStatus,
				fModifierStatus,
				fSuperClassStatus
		};
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		
		int nColumns= 4;
		
		GridLayout layout= new GridLayout();
		layout.numColumns= nColumns;		
		composite.setLayout(layout);
		createContainerControls(composite, nColumns);	
		createPackageControls(composite, nColumns);
		createSeparator(composite, nColumns);
		createTypeNameControls(composite, nColumns);
		createSuperClassControls(composite, nColumns);
		createMethodStubSelectionControls(composite, nColumns);
		createCommentControls(composite, nColumns);
		createSeparator(composite, nColumns);
		createClassUnderContractControls(composite, nColumns);
		createC4JNatureConfigureControls(composite, nColumns);
		
		setControl(composite);
			
		//set default and focus
		String classUnderContract= getClassUnderContractText();
		if (classUnderContract.length() > 0) {
			setTypeName(Signature.getSimpleName(classUnderContract)+CONTRACT_SUFFIX, true);
		}

		Dialog.applyDialogFont(composite);
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IJUnitHelpContextIds.NEW_TESTCASE_WIZARD_PAGE);
		
		restoreWidgetValues();
		
		setFocus();
	}
	
	/**
	 * Creates the controls for the superclass name field. Expects a <code>GridLayout</code> 
	 * with at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createSuperClassControls(Composite composite, int nColumns) {
		Label fSuperClassLabel = new Label(composite, SWT.NONE);
		fSuperClassLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fSuperClassLabel.setText(getSuperClassLabel());
		
		fSuperClassButton = new Button(composite, SWT.CHECK);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.widthHint = getMaxFieldWidth();
		fSuperClassButton.setLayoutData(gridData);
		fSuperClassButton.setText("ContractBase<T>"); //$NON-NLS-1$
		
		createEmptySpace(composite, 1);
	}

	/**
	 * Creates the controls for the method stub selection buttons. Expects a <code>GridLayout</code> with 
	 * at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createMethodStubSelectionControls(Composite composite, int nColumns) {
		Label labelStubs = new Label(composite, SWT.LEFT | SWT.WRAP);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = nColumns;
		labelStubs.setLayoutData(gridData);
		labelStubs.setText(WizardMessages.NewContractWizardPageOne_method_Stub_label);
		
		createEmptySpace(composite, 1);
		
		fConstructorButton = new Button(composite, SWT.CHECK);
		fConstructorButton.setText(WizardMessages.NewContractWizardPageOne_methodStub_constructor);
		
		createEmptySpace(composite, 1);
	}	

	/**
	 * Creates the controls for the 'class under contract' field. Expects a <code>GridLayout</code> with 
	 * at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createClassUnderContractControls(Composite composite, int nColumns) {
		Label classUnderTestLabel= new Label(composite, SWT.LEFT | SWT.WRAP);
		classUnderTestLabel.setFont(composite.getFont());
		classUnderTestLabel.setText(WizardMessages.NewContractWizardPageOne_class_to_test_label); 
		classUnderTestLabel.setLayoutData(new GridData());

		fClassUnderContractControl= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fClassUnderContractControl.setEnabled(true);
		fClassUnderContractControl.setFont(composite.getFont());
		fClassUnderContractControl.setText(fClassUnderContractText);
		fClassUnderContractControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				internalSetClassUnderText(((Text) e.widget).getText());
			}
		});
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan= nColumns - 2;
		fClassUnderContractControl.setLayoutData(gd);
		
		fClassUnderContractButton= new Button(composite, SWT.PUSH);
		fClassUnderContractButton.setText(WizardMessages.NewContractWizardPageOne_class_to_test_browse); 
		fClassUnderContractButton.setEnabled(true);
		fClassUnderContractButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				classToContractButtonPressed();
			}
			public void widgetSelected(SelectionEvent e) {
				classToContractButtonPressed();
			}
		});	
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= false;
		gd.horizontalSpan= 1;
		//gd.widthHint = LayoutUtil.getButtonWidthHint(fClassUnderTestButton);		
		fClassUnderContractButton.setLayoutData(gd);
		
		ControlContentAssistHelper.createTextContentAssistant(fClassUnderContractControl, fClassToContractCompletionProcessor);
	}
	
	/**
	 * Creates the controls for the C4J project nature enablement.
	 * Expects a <code>GridLayout</code> with at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 * 
	 * @since 3.2
	 */
	protected void createC4JNatureConfigureControls(Composite composite, int nColumns) {
		Composite inner= new Composite(composite, SWT.NONE);
		inner.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, nColumns, 1));
		GridLayout layout= new GridLayout(2, false);
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		inner.setLayout(layout);
		
		fImage= new Label(inner, SWT.NONE);
		fImage.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));
		fImage.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));

		fLink= new Link(inner, SWT.WRAP);
		fLink.setText("\n\n"); //$NON-NLS-1$
		fLink.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleLinks(e.text);
			}
		});
		GridData gd= new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1);
		gd.widthHint= convertWidthInCharsToPixels(60);
		fLink.setLayoutData(gd);
		updateC4JNatureMessage();
	}

	private void updateC4JNatureMessage() {
		if (fLink == null || fLink.isDisposed()) {
			return;
		}
		
		String message= null;
		IPackageFragmentRoot root= getPackageFragmentRoot();
		if (root != null) {
			try {
				IJavaProject project= root.getJavaProject();
				if (project.exists()) {
					if (!C4JUtils.isJava5Project(project)) {
						message= WizardMessages.NewContractWizardPageOne_linkedtext_java5required;
					} else if (project.findType(AnnotationUtil.ANNOTATION_CONTRACT_REFERENCE) == null) {
						message= NLS.bind(WizardMessages.NewContractWizardPageOne_linkedtext_c4j_notonbuildpath, project.getElementName());
					}
				}
			} catch (JavaModelException e) {
			}
		}
		fLink.setVisible(message != null);
		fImage.setVisible(message != null);
		
		if (message != null) {
			fLink.setText(message);
		}
	}
	

	private void classToContractButtonPressed() {
		IType type= chooseClassToContractType();
		if (type != null) {
			setClassUnderContract(type.getFullyQualifiedName('.'));
		}
	}

	private IType chooseClassToContractType() {	
		IPackageFragmentRoot root= getPackageFragmentRoot();
		if (root == null) 
			return null;

		IJavaElement[] elements= new IJavaElement[] { root.getJavaProject() };
		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(elements);
		
		try {		
			SelectionDialog dialog= JavaUI.createTypeDialog(getShell(), getWizard().getContainer(), scope, IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES, false, getClassUnderContractText());
			dialog.setTitle(WizardMessages.NewContractWizardPageOne_class_to_test_dialog_title); 
			dialog.setMessage(WizardMessages.NewContractWizardPageOne_class_to_test_dialog_message); 
			if (dialog.open() == Window.OK) {
				Object[] resultArray= dialog.getResult();
				if (resultArray != null && resultArray.length > 0)
					return (IType) resultArray[0];
			}
		} catch (JavaModelException e) {
			C4JActivator.log(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.NewTypeWizardPage#packageChanged()
	 */
	protected IStatus packageChanged() {
		IStatus status= super.packageChanged();
		fClassToContractCompletionProcessor.setPackageFragment(getPackageFragment());
		return status;
	}
	
	/**
	 * Hook method that gets called when the class under contract has changed. The method class under contract
	 * returns the status of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus classUnderContractChanged() {
		C4JStatus status= new C4JStatus();
		
		fClassUnderContract= null;
		
		IPackageFragmentRoot root= getPackageFragmentRoot();
		if (root == null) {
			return status;
		}
		
		String classToContractName= getClassUnderContractText();
		if (classToContractName.length() == 0) {
			return status;
		}
		IStatus val= JavaConventions.validateJavaTypeName(classToContractName);
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(WizardMessages.NewContractWizardPageOne_error_class_to_test_not_valid); 
			return status;
		}
		
		IPackageFragment pack= getPackageFragment(); // can be null
		try {		
			IType type= resolveClassNameToType(root.getJavaProject(), pack, classToContractName);
			if (type == null) {
				status.setError(WizardMessages.NewContractWizardPageOne_error_class_to_test_not_exist); 
				return status;
			}
			if (ContractReferenceModel.isTarget(type.getCompilationUnit().getCorrespondingResource())) {
				status.setError(NLS.bind(WizardMessages.NewContractWizardPageOne_error_class_to_contract_is_target, classToContractName)); 
			}
			if (ContractReferenceModel.isContract(type.getCompilationUnit().getCorrespondingResource())) {
				status.setError(NLS.bind(WizardMessages.NewContractWizardPageOne_error_class_to_contract_is_contract, classToContractName));
			}
			
			if (pack != null && !isVisible(type, pack)) {
				status.setWarning(NLS.bind(WizardMessages.NewContractWizardPageOne_warning_class_to_test_not_visible, classToContractName)); 
			}
			fClassUnderContract= type;
			fPage2.setClassUnderTest(fClassUnderContract);
		} catch (JavaModelException e) {
			status.setError(WizardMessages.NewContractWizardPageOne_error_class_to_test_not_valid); 
		} 
		return status;
	}
	
	/*
	 * Evaluates if a member (possible from another package) is visible from
	 * elements in a package.
	 */
	protected static boolean isVisible(IMember member, IPackageFragment pack) throws JavaModelException {
		
		int type = member.getElementType();
		if  (type == IJavaElement.INITIALIZER ||  (type == IJavaElement.METHOD && member.getElementName().startsWith("<"))) { //$NON-NLS-1$
			return false;
		}
		
		return true;
		
//		int otherflags = member.getFlags();
//		IType declaringType = member.getDeclaringType();
//		if (Flags.isPublic(otherflags) || (declaringType != null && declaringType.isInterface())) {
//			return true;
//		} else if (Flags.isPrivate(otherflags)) {
//			return false;
//		}		
//		
//		IPackageFragment otherpack = (IPackageFragment) member.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
//		return (pack != null && otherpack != null && pack.getElementName().equals(otherpack.getElementName()));
	}

	public String getSuperClass() {
		String strSuperClass = "net.sourceforge.c4j.ContractBase<" + Signature.getSimpleName(getClassUnderContractText()) + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		if (fSuperClassButton != null && fSuperClassButton.getSelection()) {
			return strSuperClass;
		}
		
		// Also return a superclass value if a method is to be contracted
		// involving return values (depends on a call to ContractBase.getReturnValue())
		NewContractMethodElement[] elements = fPage2.getCheckedElements();
		if (elements.length > 0) {
			for (NewContractMethodElement element : elements) {
				int postcond = element.getPostCondition();
				if (postcond != NewContractLabelProvider.POST_COND_NONE &&
						postcond != NewContractLabelProvider.POST_COND_EMPTYSTUB) {
					return strSuperClass;
				}
			}
		}
		
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns the content of the class to test text field.
	 * 
	 * @return the name of the class to test
	 */
	public String getClassUnderContractText() {
		return fClassUnderContractText;
	}
	
	/**
	 * Returns the class to be tested.
	 * 
	 * 	@return the class under test or <code>null</code> if the entered values are not valid
	 */
	public IType getClassUnderContract() {
		return fClassUnderContract;
	}
	
	/**
	 * Sets the name of the class under test.
	 * 
	 * @param name The name to set
	 */		
	public void setClassUnderContract(String name) {
		if (fClassUnderContractControl != null && !fClassUnderContractControl.isDisposed()) {
			fClassUnderContractControl.setText(name);
		}
		internalSetClassUnderText(name);
	}
	
	private void internalSetClassUnderText(String name) {
		fClassUnderContractText= name;
		fClassUnderContractStatus= classUnderContractChanged();
		handleFieldChanged(TARGET_CLASS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.NewTypeWizardPage#createTypeMembers(org.eclipse.jdt.core.IType, org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void createTypeMembers(IType type, ImportsManager imports, IProgressMonitor monitor) throws CoreException {		
		if (fSuperClassButton.getSelection()) {
			IType targetClass = getClassUnderContract();
			if (!targetClass.getPackageFragment().getElementName().equals(
					getPackageFragment().getElementName()))
				imports.addImport(targetClass.getFullyQualifiedName());
		}
		
		if (fConstructorButton.getSelection())
			createConstructor(type, imports); 	

		if (fClassUnderContract != null) {
			createContractMethodStubs(type, imports);
		}
	}

	private void createConstructor(IType type, ImportsManager imports) throws CoreException {
		ITypeHierarchy typeHierarchy= null;
		IType[] superTypes= null;
		String content;
		IMethod methodTemplate= null;
		if (type.exists()) {
			typeHierarchy= type.newSupertypeHierarchy(null);
			superTypes= typeHierarchy.getAllSuperclasses(type);
			for (int i= 0; i < superTypes.length; i++) {
				if (superTypes[i].exists()) {
					IMethod constrMethod= superTypes[i].getMethod(superTypes[i].getElementName(), new String[] {"Ljava.lang.String;"}); //$NON-NLS-1$
					if (constrMethod.exists() && constrMethod.isConstructor()) {
						methodTemplate= constrMethod;
						break;
					}
				}
			}
		}
		GenStubSettings settings= C4JStubUtil.getCodeGenerationSettings(type.getJavaProject());
		settings.createComments= isAddComments();
		
		if (methodTemplate != null) {
			settings.callSuper= true;				
			settings.methodOverwrites= true;
			content= C4JStubUtil.genStub(type.getCompilationUnit(), getTypeName(), methodTemplate, settings, imports);
		} else {
			final String delimiter= getLineDelimiter();
			StringBuffer buffer= new StringBuffer(32);
			buffer.append("public "); //$NON-NLS-1$
			buffer.append(getTypeName());
			buffer.append('(');
			
			buffer.append(") {"); //$NON-NLS-1$
			buffer.append(delimiter);
			
			buffer.append('}');
			buffer.append(delimiter);
			content= buffer.toString();
		}
		type.createMethod(content, null, true, null);	
	}

	private void createContractMethodStubs(IType type, ImportsManager imports) throws CoreException {
		NewContractMethodElement[] elements = fPage2.getCheckedElements();
		if (elements.length == 0)
			return;
			
		for (NewContractMethodElement element : elements) {
			IMethod method = (IMethod)element.getMember();
			
			GenStubSettings settings= C4JStubUtil.getCodeGenerationSettings(type.getJavaProject());
			settings.createComments= isAddComments();
			settings.callSuper= false;
			settings.methodOverwrites= false;		
			settings.taskTag = fPage2.isCreateTasks();
			settings.finalize = fPage2.getCreateFinalMethodStubsButtonSelection();
			
			// PRE - CONDITION
			if (element.getPreCondition() > 0) {			
				settings.preCondition = element.getPreCondition();
				String content= C4JStubUtil.genStub(type.getCompilationUnit(), getTypeName(), method, settings, imports);
				
				type.createMethod(content, null, false, null);	
			}
			
			// POST - CONDITION
			if (element.getPostCondition() > 0) {
				settings.preCondition = -1;
				settings.postCondition = element.getPostCondition();
				String content= C4JStubUtil.genStub(type.getCompilationUnit(), getTypeName(), method, settings, imports);
				
				type.createMethod(content, null, false, null);
			}
		}
	}

	private String getLineDelimiter() throws JavaModelException {
		IType classToTest= getClassUnderContract();
		
		if (classToTest != null && classToTest.exists() && classToTest.getCompilationUnit() != null)
			return classToTest.getCompilationUnit().findRecommendedLineSeparator();
		
		return getPackageFragment().findRecommendedLineSeparator();
	}

	

	
	

//	private List getOverloadedMethods(List<IMethod> allMethods) {
//		List<IMethod> overloadedMethods= new ArrayList<IMethod>();
//		for (int i= 0; i < allMethods.size(); i++) {
//			IMethod current= (IMethod) allMethods.get(i);
//			String currentName= current.getElementName();
//			boolean currentAdded= false;
//			for (ListIterator iter= allMethods.listIterator(i+1); iter.hasNext(); ) {
//				IMethod iterMethod= (IMethod) iter.next();
//				if (iterMethod.getElementName().equals(currentName)) {
//					//method is overloaded
//					if (!currentAdded) {
//						overloadedMethods.add(current);
//						currentAdded= true;
//					}
//					overloadedMethods.add(iterMethod);
//					iter.remove();
//				}
//			}
//		}
//		return overloadedMethods;
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (!visible) {
			saveWidgetValues();
		}
		
		//if (visible) setFocus();
	}
	
	/**
	 * The method is called when the container has changed to validate if the project
	 * is suited for the C4J contract class.
	 * 
	 * @return the status of the validation
	 */
	protected IStatus validateIfC4JProject() {
		C4JStatus status= new C4JStatus();
		IPackageFragmentRoot root= getPackageFragmentRoot();
		if (root != null) {
			try {
				IJavaProject project= root.getJavaProject();
				if (project.exists()) {
					if (!C4JUtils.isJava5Project(project)) {
						status.setError(WizardMessages.NewContractWizardPageOne_error_java5required);
						return status;
					}
					if (project.findType(AnnotationUtil.ANNOTATION_CONTRACT_REFERENCE) == null) {
						status.setWarning(WizardMessages.NewContractWizardPageOne_error_c4jNotOnbuildpath); 
						return status;
					}		
				}
			} catch (JavaModelException e) {
			}
		}
		return status;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return super.canFlipToNextPage() && getClassUnderContract() != null;
	}

	private IType resolveClassNameToType(IJavaProject jproject, IPackageFragment pack, String classToTestName) throws JavaModelException {
		if (!jproject.exists()) {
			return null;
		}
		
		IType type= jproject.findType(classToTestName);
		
		// search in current package
		if (type == null && pack != null && !pack.isDefaultPackage()) {
			type= jproject.findType(pack.getElementName(), classToTestName);
		}
		
		// search in java.lang
		if (type == null) {
			type= jproject.findType("java.lang", classToTestName); //$NON-NLS-1$
		}
		return type;
	}
	
	/**
	 *	Use the dialog store to restore widget values to the values that they held
	 *	last time this wizard was used to completion
	 */
	private void restoreWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			fConstructorButton.setSelection(settings.getBoolean(STORE_CONSTRUCTOR));
			fSuperClassButton.setSelection(settings.getBoolean(STORE_SUPERCLASS));
		} else {
			fConstructorButton.setSelection(false); //constructor
			fSuperClassButton.setSelection(false);
		}
	}	

	/**
	 * 	Since Finish was pressed, write widget values to the dialog store so that they
	 *	will persist into the next invocation of this wizard page
	 */
	private void saveWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			settings.put(STORE_CONSTRUCTOR, fConstructorButton.getSelection());
		}
	}
	
	private class C4JStatus extends Status {
		
		public C4JStatus() {
			this(IStatus.OK, "ok"); //$NON-NLS-1$
		}
		
		public C4JStatus(int severity, String msg) {
			super(severity, C4JActivator.PLUGIN_ID, IStatus.OK, msg, null);
		}
		
		public void setError(String msg) {
			setSeverity(IStatus.ERROR);
			setMessage(msg);
		}
		
		public void setWarning(String msg) {
			setSeverity(IStatus.WARNING);
			setMessage(msg);
		}
	}

}

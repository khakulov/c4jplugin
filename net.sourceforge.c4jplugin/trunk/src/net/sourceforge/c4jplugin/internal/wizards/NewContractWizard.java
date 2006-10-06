package net.sourceforge.c4jplugin.internal.wizards;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewContractWizard extends Wizard implements INewWizard {

	private IWorkbench fWorkbench;
	protected static final String DIALOG_SETTINGS_KEY= "C4JPluginWizards"; //$NON-NLS-1$
	private IStructuredSelection fSelection;
	
	private NewContractWizardPageOne fPage1;
	private NewContractWizardPageTwo fPage2;

	public NewContractWizard() {
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, "icons/new_contract_wiz.png")); //$NON-NLS-1$
		
		initDialogSettings();
	}
	
	/*
	 * @see Wizard#createPages
	 */	
	public void addPages() {
		super.addPages();
		fPage2= new NewContractWizardPageTwo();
		fPage1= new NewContractWizardPageOne(fPage2);
		addPage(fPage1);
		fPage1.init(getSelection());
		addPage(fPage2);
	}	
	
	/*
	 * @see Wizard#performFinish
	 */		
	public boolean performFinish() {
		if (finishPage(fPage1.getRunnable())) {
			IType newClass= fPage1.getCreatedType();
			
			IType targetClass = fPage1.getClassUnderContract();
			try {
				ICompilationUnit icuTarget = targetClass.getCompilationUnit();
				
				icuTarget.createImport("net.sourceforge.c4j.ContractReference", null, null); //$NON-NLS-1$
				
				String source = icuTarget.getBuffer().getContents();
				Document document = new Document(source);
				
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setSource(targetClass.getCompilationUnit());
				CompilationUnit cu = (CompilationUnit)parser.createAST(null);
					
				AST ast = cu.getAST();
				cu.recordModifications();
				
				NormalAnnotation annotation = ast.newNormalAnnotation();
				annotation.setTypeName(ast.newSimpleName("ContractReference")); //$NON-NLS-1$
				MemberValuePair pair = ast.newMemberValuePair();
				pair.setName(ast.newSimpleName("contractClassName")); //$NON-NLS-1$
				StringLiteral value = ast.newStringLiteral();
				if (newClass.getPackageFragment().getElementName().equals(targetClass.getPackageFragment().getElementName()))
					value.setLiteralValue(newClass.getElementName());
				else value.setLiteralValue(newClass.getFullyQualifiedName());
				pair.setValue(value);
				annotation.values().add(pair);
				
				TypeDeclaration type = (TypeDeclaration)cu.types().get(0);
				type.modifiers().add(0, annotation);
				
				TextEdit edits = cu.rewrite(document, targetClass.getJavaProject().getOptions(true));
				edits.apply(document);
				
				icuTarget.getBuffer().setContents(document.get());
				
				icuTarget.reconcile(AST.JLS3, false, null, null);
				icuTarget.commitWorkingCopy(true, null);
			}
			catch (JavaModelException exc) {
				String title = WizardMessages.NewContractWizard_annotation_error_title;
				String msg = NLS.bind(WizardMessages.NewContractWizard_annotation_error_message, targetClass.getElementName());
				
				IStatus status = new Status(IStatus.ERROR, 
						C4JActivator.PLUGIN_ID, IStatus.OK,
						msg, exc);
				C4JActivator.log(status);
				
				MessageDialog.openError(getShell(), title, msg);
				
				exc.printStackTrace();
			}
			catch (BadLocationException exc) {
				exc.printStackTrace();
			}
			
			IResource resource= newClass.getCompilationUnit().getResource();
			if (resource != null) {
				selectAndReveal(resource);
				openResource(resource);
			}
			return true;
		}
		return false;		
	}
	
	/*
	 * Run a runnable
	 */	
	protected boolean finishPage(IRunnableWithProgress runnable) {
		IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(runnable);
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(getContainer(), op, ResourcesPlugin.getWorkspace().getRoot()); 
			
		} catch (InvocationTargetException e) {
			Shell shell= getShell();
			String title= WizardMessages.NewContractWizard_op_error_title; 
			String message= WizardMessages.NewContractWizard_op_error_message; 
			
		
			String msg = message;
			if (e.getMessage() != null && e.getMessage().length() > 0)
				msg += "\n\n" + e.getMessage(); //$NON-NLS-1$
			else {
				Throwable target = e.getTargetException();
				if (target.getMessage() != null && target.getMessage().length() > 0)
					msg += "\n\n" + target.getMessage(); //$NON-NLS-1$
			}
			
			C4JActivator.log(e);
			MessageDialog.openError(shell, title, msg);
			return false;
		} catch  (InterruptedException e) {
			return false;
		}
		return true;
	}

	protected void openResource(final IResource resource) {
		if (resource.getType() == IResource.FILE) {
			IWorkbench workbench = C4JActivator.getDefault().getWorkbench();
			if (workbench == null) return;
			IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
			if (activeWindow == null) return;
			final IWorkbenchPage activePage= activeWindow.getActivePage();
			if (activePage != null) {
				final Display display= Display.getDefault();
				if (display != null) {
					display.asyncExec(new Runnable() {
						public void run() {
							try {
								IDE.openEditor(activePage, (IFile)resource, true);
							} catch (PartInitException e) {
								C4JActivator.log(e);
							}
						}
					});
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		fWorkbench= workbench;
		fSelection= currentSelection;
	}
	
	public IStructuredSelection getSelection() {
		return fSelection;
	}

	protected void selectAndReveal(IResource newResource) {
		BasicNewResourceWizard.selectAndReveal(newResource, fWorkbench.getActiveWorkbenchWindow());
	} 
	
	protected void initDialogSettings() {
		IDialogSettings pluginSettings= C4JActivator.getDefault().getDialogSettings();
		IDialogSettings wizardSettings= pluginSettings.getSection(DIALOG_SETTINGS_KEY);
		if (wizardSettings == null) {
			wizardSettings= new DialogSettings(DIALOG_SETTINGS_KEY);
			pluginSettings.addSection(wizardSettings);
		}
		setDialogSettings(wizardSettings);
	}

}
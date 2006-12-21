package net.sourceforge.c4jplugin.internal.ui.actions;

import net.sourceforge.c4jplugin.internal.core.ContractReferenceModel;
import net.sourceforge.c4jplugin.internal.util.SelectionConverter;
import net.sourceforge.c4jplugin.internal.wizards.NewContractWizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class NewContractAction implements IObjectActionDelegate {

	public void run(IAction action) {
		IWizard wizard = new NewContractWizard();
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (targetPart == null) return;
		
		IStructuredSelection selection;
		try {
			selection = SelectionConverter.getStructuredSelection(targetPart);
		} catch (JavaModelException e) {
			e.printStackTrace();
			action.setEnabled(false);
			return;
		}
		
		if (selection.isEmpty()) {
			action.setEnabled(false);
			return;
		}
		
		Object sel = selection.getFirstElement();
		
		if ((sel instanceof IMethod &&
			ContractReferenceModel.isContracted(((IMethod)sel).getCompilationUnit().getResource()))
			|| (sel instanceof ICompilationUnit &&
					ContractReferenceModel.isContracted(((ICompilationUnit)sel).getResource()))) {
				action.setEnabled(true);
		}
		else {
			action.setEnabled(false);
		}
	}

}

package net.sourceforge.c4jplugin.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.core.ContractReferenceModel;
import net.sourceforge.c4jplugin.internal.util.ExceptionHandler;
import net.sourceforge.c4jplugin.internal.util.OpenContractHierarchyUtil;
import net.sourceforge.c4jplugin.internal.util.SelectionConverter;
import net.sourceforge.c4jplugin.internal.wizards.NewContractWizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.browsing.LogicalPackage;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
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
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (!action.isEnabled()) return;
		
		if (targetPart instanceof JavaEditor) {
			IJavaElement input = SelectionConverter.getInput((JavaEditor)targetPart);
			action.setEnabled(isContractable(input));
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (!action.isEnabled()) return;
		
		if (!(selection instanceof IStructuredSelection) || (selection == null)) {
			action.setEnabled(false);
			return;
		}
		
		IStructuredSelection sel = (IStructuredSelection)selection;
		if (sel.isEmpty()) {
			action.setEnabled(false);
			return;
		}
		
		// due to plugin.xml, selection is of type IJavaElement
		IJavaElement elem = (IJavaElement)sel.getFirstElement();
		action.setEnabled(isContractable(elem));
	}
	
	private boolean isContractable(IJavaElement elem) {
		if (elem == null) return false;
		
		try {
			Boolean isContracted = ContractReferenceModel.isContracted(elem.getUnderlyingResource());
			if ((isContracted == null || isContracted == false)
					|| ContractReferenceModel.isContract(elem.getUnderlyingResource())) {
				return false;
			}
		} catch (JavaModelException e) {
			return false;
		}
		return true;
	}
	
	
}

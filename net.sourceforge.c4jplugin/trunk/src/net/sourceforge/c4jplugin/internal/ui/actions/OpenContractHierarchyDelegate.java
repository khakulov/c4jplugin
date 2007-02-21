package net.sourceforge.c4jplugin.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;
import net.sourceforge.c4jplugin.internal.util.ExceptionHandler;
import net.sourceforge.c4jplugin.internal.util.OpenContractHierarchyUtil;
import net.sourceforge.c4jplugin.internal.util.SelectionConverter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.browsing.LogicalPackage;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class OpenContractHierarchyDelegate extends SelectionDispatchActionDelegate implements IObjectActionDelegate {
	
	private JavaEditor editor = null;
	
	private Shell shell = null;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (targetPart instanceof JavaEditor) {
			editor = (JavaEditor)targetPart;
			action.setEnabled(SelectionConverter.canOperateOn(editor));
		}
		else editor = null;
		
		//openHierarchyAction.setActionDefinitionId("net.sourceforge.c4jplugin.commands.open.contract.hierarchy");
		shell = targetPart.getSite().getShell();
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public JavaEditor getJavaEditor() {
		return editor;
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchActionDelegate.
	 */
	public void selectionChanged(IAction action, ITextSelection selection) {
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchActionDelegate.
	 */
	public void selectionChanged(IAction action, IStructuredSelection selection) {
		if (action.isEnabled()) action.setEnabled(isEnabled(selection));
	}
	
	private boolean isEnabled(IStructuredSelection selection) {
		
		if (selection.size() != 1)
			return false;
		Object input= selection.getFirstElement();
		
		
		if (input instanceof LogicalPackage) {
			try {
				if (((LogicalPackage)input).getJavaProject().getProject().hasNature(C4JProjectNature.NATURE_ID))
					return true;
			} catch (CoreException e) {
			}
			
			return false;
		}
		
		if (!(input instanceof IJavaElement))
			return false;
		switch (((IJavaElement)input).getElementType()) {
			case IJavaElement.INITIALIZER:
			case IJavaElement.METHOD:
			case IJavaElement.FIELD:
			case IJavaElement.TYPE:
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.JAVA_PROJECT:
			case IJavaElement.PACKAGE_FRAGMENT:
			case IJavaElement.PACKAGE_DECLARATION:
			case IJavaElement.IMPORT_DECLARATION:	
			case IJavaElement.CLASS_FILE:
			case IJavaElement.COMPILATION_UNIT:
				return true;
			case IJavaElement.LOCAL_VARIABLE:
			default:
				return false;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchActionDelegate.
	 */
	public void run(IAction action, ITextSelection selection) {
		IJavaElement input= SelectionConverter.getInput(getJavaEditor());
		if (!ActionUtil.isProcessable(getShell(), input))
			return;		
		
		try {
			IJavaElement[] elements= SelectionConverter.codeResolveOrInputForked(getJavaEditor());
			if (elements == null)
				return;
			List<IJavaElement> candidates= new ArrayList<IJavaElement>(elements.length);
			for (int i= 0; i < elements.length; i++) {
				IJavaElement[] resolvedElements= OpenContractHierarchyUtil.getCandidates(elements[i]);
				if (resolvedElements != null)	
					candidates.addAll(Arrays.asList(resolvedElements));
			}
			run((IJavaElement[])candidates.toArray(new IJavaElement[candidates.size()]));
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), getDialogTitle(), ActionMessages.SelectionConverter_codeResolve_failed);
		} catch (InterruptedException e) {
			// cancelled
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchActionDelegate.
	 */
	public void run(IAction action, IStructuredSelection selection) {
		if (selection.size() != 1)
			return;
		Object input= selection.getFirstElement();
		
		if (input instanceof LogicalPackage) {
			IPackageFragment[] fragments= ((LogicalPackage)input).getFragments();
			if (fragments.length == 0)
				return;
			input= fragments[0];
		}

		if (!(input instanceof IJavaElement)) {
			IStatus status= createStatus(ActionMessages.OpenContractHierarchyAction_messages_no_java_element); 
			ErrorDialog.openError(getShell(), getDialogTitle(), ActionMessages.OpenContractHierarchyAction_messages_title, status); 
			return;
		}
		IJavaElement element= (IJavaElement) input;
		if (!ActionUtil.isProcessable(getShell(), element))
			return;

		List<IJavaElement> result= new ArrayList<IJavaElement>(1);
		IStatus status= compileCandidates(result, element);
		if (status.isOK()) {
			run((IJavaElement[]) result.toArray(new IJavaElement[result.size()]));
		} else {
			ErrorDialog.openError(getShell(), getDialogTitle(), ActionMessages.OpenContractHierarchyAction_messages_title, status); 
		}
	}

	private void run(IJavaElement[] elements) {
		if (elements.length == 0) {
			getShell().getDisplay().beep();
			return;
		}
		OpenContractHierarchyUtil.open(elements, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}
	
	private static String getDialogTitle() {
		return ActionMessages.OpenContractHierarchyAction_dialog_title; 
	}
	
	private static IStatus compileCandidates(List<IJavaElement> result, IJavaElement elem) {
		IStatus ok= new Status(IStatus.OK, C4JActivator.PLUGIN_ID, 0, "", null); //$NON-NLS-1$		
		try {
			switch (elem.getElementType()) {
				case IJavaElement.INITIALIZER:
				case IJavaElement.METHOD:
				case IJavaElement.FIELD:
				case IJavaElement.TYPE:
				case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				case IJavaElement.JAVA_PROJECT:
					result.add(elem);
					return ok;
				case IJavaElement.PACKAGE_FRAGMENT:
					if (((IPackageFragment)elem).containsJavaResources()) {
						result.add(elem);
						return ok;
					}
					return createStatus(ActionMessages.OpenContractHierarchyAction_messages_no_java_resources); 
				case IJavaElement.PACKAGE_DECLARATION:
					result.add(elem.getAncestor(IJavaElement.PACKAGE_FRAGMENT));
					return ok;
				case IJavaElement.IMPORT_DECLARATION:	
					IImportDeclaration decl= (IImportDeclaration) elem;
					if (decl.isOnDemand()) {
						elem= JavaModelUtil.findTypeContainer(elem.getJavaProject(), Signature.getQualifier(elem.getElementName()));
					} else {
						elem= elem.getJavaProject().findType(elem.getElementName());
					}
					if (elem != null) {
						result.add(elem);
						return ok;
					}
					return createStatus(ActionMessages.OpenContractHierarchyAction_messages_unknown_import_decl);
				case IJavaElement.CLASS_FILE:
					result.add(((IClassFile)elem).getType());
					return ok;				
				case IJavaElement.COMPILATION_UNIT:
					ICompilationUnit cu= (ICompilationUnit)elem;
					IType[] types= cu.getTypes();
					if (types.length > 0) {
						result.addAll(Arrays.asList(types));
						return ok;
					}
					return createStatus(ActionMessages.OpenContractHierarchyAction_messages_no_types); 
			}
		} catch (JavaModelException e) {
			return e.getStatus();
		}
		return createStatus(ActionMessages.OpenContractHierarchyAction_messages_no_valid_java_element); 
	}
	
	private static IStatus createStatus(String message) {
		return new Status(IStatus.INFO, C4JActivator.PLUGIN_ID, 0, message, null);
	}	

}

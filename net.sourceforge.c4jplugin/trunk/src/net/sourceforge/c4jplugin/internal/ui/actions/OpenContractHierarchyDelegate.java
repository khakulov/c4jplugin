package net.sourceforge.c4jplugin.internal.ui.actions;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class OpenContractHierarchyDelegate implements IObjectActionDelegate {

	OpenContractHierarchyAction openHierarchyAction = null;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (targetPart instanceof JavaEditor)
			openHierarchyAction = new OpenContractHierarchyAction((JavaEditor)targetPart);
		else
			openHierarchyAction = new OpenContractHierarchyAction(targetPart.getSite());
		
		openHierarchyAction.setActionDefinitionId("net.sourceforge.c4jplugin.commands.open.contract.hierarchy");
		openHierarchyAction.setSpecialSelectionProvider(targetPart.getSite().getSelectionProvider());
	}

	public void run(IAction action) {
		if (openHierarchyAction != null) {
			openHierarchyAction.run();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		
	}

}

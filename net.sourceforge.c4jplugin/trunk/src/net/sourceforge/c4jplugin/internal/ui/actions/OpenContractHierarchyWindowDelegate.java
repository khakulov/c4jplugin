package net.sourceforge.c4jplugin.internal.ui.actions;

import net.sourceforge.c4jplugin.internal.util.SelectionConverter;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class OpenContractHierarchyWindowDelegate extends
		OpenContractHierarchyDelegate implements IWorkbenchWindowActionDelegate, IPartListener {
	
	IWorkbenchWindow window = null;
	
	private JavaEditor editor = null;
	
	public OpenContractHierarchyWindowDelegate() {
		
	}
	
	public void dispose() {
		window.getPartService().removePartListener(this);
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
		window.getPartService().addPartListener(this);
		IWorkbenchPart part = window.getActivePage().getActivePart();
		if (part != null && part instanceof JavaEditor && 
				SelectionConverter.canOperateOn((JavaEditor)part))
			editor = (JavaEditor)part;
		else editor = null;
	}
	
	public Shell getShell() {
		return window.getShell();
	}
	
	public JavaEditor getJavaEditor() {
		return editor;
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchActionDelegate.
	 */
	public void selectionChanged(IAction action, ITextSelection selection) {
		if (editor != null) {
			action.setEnabled(true);
		}
		
	}

	public void partActivated(IWorkbenchPart part) {
		if (part instanceof JavaEditor) {
			if (SelectionConverter.canOperateOn((JavaEditor)part)) {
				editor = (JavaEditor)part;
			}
			else editor = null;
		}
		else editor = null;
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(IWorkbenchPart part) {
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}
	
	

}

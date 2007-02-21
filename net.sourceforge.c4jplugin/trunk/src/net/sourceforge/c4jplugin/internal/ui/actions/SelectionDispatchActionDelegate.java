package net.sourceforge.c4jplugin.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;

abstract public class SelectionDispatchActionDelegate implements IActionDelegate {

	private ISelection sel = null;
	
	abstract public Shell getShell();
	
	/**
	 * Notifies this delegate that the given structured selection has changed. This default
	 * implementation calls <code>selectionChanged(IAction action, ISelection selection)</code>.
	 * 
	 * @param selection the new selection
 	 */
	public void selectionChanged(IAction action, IStructuredSelection selection) {
		selectionChanged(action, (ISelection)selection);
	}

	/**
	 * Executes this actions with the given structured selection. This default implementation
	 * calls <code>run(IAction action, ISelection selection)</code>.
	 * 
	 * @param selection the selection
	 */
	public void run(IAction action, IStructuredSelection selection) {
		run(action, (ISelection)selection);
	}
		
	/**
	 * Notifies this action that the given text selection has changed.  This default
	 * implementation calls <code>selectionChanged(IAction action, ISelection selection)</code>.
	 * 
	 * @param selection the new selection
 	 */
	public void selectionChanged(IAction action, ITextSelection selection) {
		selectionChanged(action, (ISelection)selection);
	}
	
	/**
	 * Executes this actions with the given text selection. This default implementation
	 * calls <code>run(ISelection selection)</code>.
	 * 
	 * @param selection the selection
	 */
	public void run(IAction action, ITextSelection selection) {
		run(action, (ISelection)selection);
	}
	
	
	public void selectionChanged(IAction action, ISelection selection) {
		sel = selection;
		dispatchSelectionChanged(action, selection);
	}
	
	public void run(IAction action, ISelection selection) {
		
	}
	
	/**
	 * Executes this actions with the given selection. This default implementation
	 * does nothing.
	 * 
	 * @param selection the selection
	 */
	public void run(IAction action) {
		dispatchRun(action, sel);
	}

	private void dispatchSelectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			selectionChanged(action, (IStructuredSelection)selection);
		} else if (selection instanceof ITextSelection) {
			selectionChanged(action, (ITextSelection)selection);
		}
	}

	private void dispatchRun(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			run(action, (IStructuredSelection)selection);
		} else if (selection instanceof ITextSelection) {
			run(action, (ITextSelection)selection);
		} else {
			run(action, selection);
		}
	}

}

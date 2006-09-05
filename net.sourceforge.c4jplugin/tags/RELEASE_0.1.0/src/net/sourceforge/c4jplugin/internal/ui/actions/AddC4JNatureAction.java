package net.sourceforge.c4jplugin.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.c4jplugin.internal.util.C4JUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class AddC4JNatureAction implements IObjectActionDelegate {

	private Vector<IProject> selected = new Vector<IProject>();
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

	public void run(IAction action) {
		for (final IProject project : selected) {
			// wrap up the operation so that an autobuild is not triggered in the
			// middle of the conversion
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				protected void execute(IProgressMonitor monitor)
						throws CoreException {
					C4JUtils.addC4JNature(project);
				}
			};
			try {
				op.run(null);
			} catch (InvocationTargetException ex) {
			} catch (InterruptedException e) {
			}
			
		}
	}
	
	

	public void selectionChanged(IAction action, ISelection sel) {
		selected.clear();
		boolean enable = true;
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object object = iter.next();
				if (object instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable)object).getAdapter(IProject.class);	
					if(project != null) {
						selected.add(project);
					} else {
						enable = false;
						break;
					}
				} else {
					enable = false;
					break;
				}
			}
			action.setEnabled(enable);
		}
	}

}

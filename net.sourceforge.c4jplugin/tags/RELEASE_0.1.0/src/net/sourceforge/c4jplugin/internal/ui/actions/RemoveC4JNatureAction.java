package net.sourceforge.c4jplugin.internal.ui.actions;

import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.c4jplugin.internal.util.C4JUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RemoveC4JNatureAction implements IObjectActionDelegate {

	private Vector<IProject> selected = new Vector<IProject>();
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

	public void run(IAction action) {
		for (IProject project : selected) {
			try {
				C4JUtils.removeC4JNature(project);
			}
			catch (CoreException e) {
				e.printStackTrace();
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

package net.sourceforge.c4jplugin.internal.ui.actions;

import java.util.HashSet;
import java.util.Iterator;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RefreshC4JModelAction implements IObjectActionDelegate {

	private ISelection sel = null;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

	public void run(IAction action) {
		HashSet<IProject> c4jProjects = new HashSet<IProject>();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object object = iter.next();
				if (object instanceof IAdaptable) {
					IResource resource = (IResource)((IAdaptable)object).getAdapter(IResource.class);	
					if (resource != null) { 
						IProject project = resource.getProject();
						if (project != null)
							c4jProjects.add(project);
					}
				}
			}
		}
		
		System.out.println("project count: " + c4jProjects.size());
		if (c4jProjects.size() > 0)
			C4JActivator.getDefault().refreshContractReferenceModel(c4jProjects.toArray(new IProject[] {}));
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.sel = selection;
	}

}

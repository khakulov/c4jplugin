package net.sourceforge.c4jplugin.internal.builder;

import java.util.Map;

import net.sourceforge.c4jplugin.internal.core.ContractReferenceModel;
import net.sourceforge.c4jplugin.internal.util.ContractReferenceUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class C4JBuilder extends IncrementalProjectBuilder {

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		if (kind == FULL_BUILD) {
			ContractReferenceUtil.refreshModel(getProject(), monitor, true);
		}
		
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		ContractReferenceUtil.deleteMarkers(project);
		ContractReferenceModel.clearModel(project);
	}
	
}

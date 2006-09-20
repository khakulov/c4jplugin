package net.sourceforge.c4jplugin.internal.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.WorkspaceModelManager;

public class C4JWorkspaceModelManager extends WorkspaceModelManager {
	
	public IPluginModelBase getWorkspacePluginModel(IProject project) {
		return super.getWorkspacePluginModel(project);
	}
}

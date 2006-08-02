package net.sourceforge.c4jplugin.internal.nature;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class C4JProjectNature implements IProjectNature {
	
	static final public String ID_NATURE = C4JActivator.PLUGIN_ID + ".c4jnature"; 

	private IProject project = null;
	
	public void configure() throws CoreException {
		// TODO Auto-generated method stub

	}

	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub

	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}

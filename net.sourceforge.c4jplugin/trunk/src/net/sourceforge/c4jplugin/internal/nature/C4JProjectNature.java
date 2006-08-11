package net.sourceforge.c4jplugin.internal.nature;

import java.util.ArrayList;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class C4JProjectNature implements IProjectNature {
	
	static final public String NATURE_ID = C4JActivator.PLUGIN_ID + ".c4jnature"; 
	static final public String BUILDER_ID = C4JActivator.PLUGIN_ID + ".c4jbuilder";

	private IProject project = null;
	
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		boolean found = false;

		for (ICommand command : commands) {
			if (command.getBuilderName().equals(BUILDER_ID)) {
				found = true;
				break;
			}
		}
		if (!found) { 
			//add builder to project
			ICommand command = desc.newCommand();
			command.setBuilderName(BUILDER_ID);
			ICommand[] newCommands = new ICommand[commands.length + 1];

			// Add it before other builders.
			System.arraycopy(commands, 0, newCommands, 0, commands.length);
			newCommands[commands.length] = command;
			desc.setBuildSpec(newCommands);
			project.setDescription(desc, null);
		}
	}

	public void deconfigure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		
		ArrayList<ICommand> newCommands = new ArrayList<ICommand>();

		// remove builder from project
		for (ICommand command : commands) {
			if (command.getBuilderName().equals(BUILDER_ID)) continue;
			newCommands.add(command);
		}
		
		desc.setBuildSpec(newCommands.toArray(new ICommand[] {}));
		project.setDescription(desc, null);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}

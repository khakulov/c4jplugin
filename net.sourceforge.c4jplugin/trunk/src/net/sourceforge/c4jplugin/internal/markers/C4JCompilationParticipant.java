package net.sourceforge.c4jplugin.internal.markers;

import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;

public class C4JCompilationParticipant extends CompilationParticipant {

	

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch) {
		System.out.println("[C4JCompilationParticipant]");
		for (BuildContext context : files) {
			System.out.println(context.getFile().getName());
			//context.recordDependencies(typeNameDependencies)
		}

		super.buildStarting(files, isBatch);
	}

	@Override
	public boolean isActive(IJavaProject project) {
		try {
			if (project.getProject().isNatureEnabled(C4JProjectNature.NATURE_ID))
				return true;
		} catch (CoreException e) {}
		return false;
	}

}

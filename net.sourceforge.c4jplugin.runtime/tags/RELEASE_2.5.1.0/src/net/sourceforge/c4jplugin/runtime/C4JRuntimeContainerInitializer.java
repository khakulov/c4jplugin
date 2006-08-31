package net.sourceforge.c4jplugin.runtime;



import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class C4JRuntimeContainerInitializer extends
		ClasspathContainerInitializer {
	
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		int size = containerPath.segmentCount();
		if (size > 0) {
			if (containerPath.segment(0).equals(C4JRuntimeContainer.C4JRT_CONTAINER)) {
				C4JRuntimeContainer container = new C4JRuntimeContainer();
				JavaCore.setClasspathContainer(containerPath,
				new IJavaProject[] { project },
				new IClasspathContainer[] { container }, null);
			}
		}
	}

}

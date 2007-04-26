package net.sourceforge.c4jplugin.runtime;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

public class C4JRuntimeClasspathVariableInitializer extends ClasspathVariableInitializer {

	@Override
	public void initialize(String variable) {
		if (variable.equals(C4JRuntime.CLASSPATH_VARIABLE_NAME)) {
			try {
				JavaCore.setClasspathVariable(variable, C4JRuntimeContainer.getC4JRtClasspath(), null);
			} catch (JavaModelException e) {
				Bundle bundle = Platform.getBundle(C4JRuntime.ID_PLUGIN);
				Platform.getLog(bundle).log(
						new Status(IStatus.ERROR, 
								bundle.getSymbolicName(), 
								IStatus.OK,
								NLS.bind(Messages.c4jClasspathVariableError, C4JRuntime.CLASSPATH_VARIABLE_NAME), 
								e)
						);
			}
		}
	}

}

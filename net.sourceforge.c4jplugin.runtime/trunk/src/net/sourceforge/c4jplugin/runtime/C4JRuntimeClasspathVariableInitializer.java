package net.sourceforge.c4jplugin.runtime;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class C4JRuntimeClasspathVariableInitializer extends ClasspathVariableInitializer {

	@Override
	public void initialize(String variable) {
		if (variable.equals("C4J_RUNTIME_LIB")) {
			try {
				JavaCore.setClasspathVariable(variable, new Path(C4JRuntimeContainer.getC4JRtClasspath()), null);
			} catch (JavaModelException e) {}
		}
	}

}

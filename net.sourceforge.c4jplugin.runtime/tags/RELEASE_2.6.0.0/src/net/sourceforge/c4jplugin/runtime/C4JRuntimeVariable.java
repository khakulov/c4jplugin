package net.sourceforge.c4jplugin.runtime;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.IValueVariableInitializer;
import org.osgi.framework.Bundle;

public class C4JRuntimeVariable implements IValueVariableInitializer {
	
	public void initialize(IValueVariable variable) {
		try {
			variable.setValue((new File(C4JRuntimeContainer.getC4JRtClasspath().toOSString())).getCanonicalPath());
		} catch (IOException e) {
			Bundle bundle = Platform.getBundle(C4JRuntime.ID_PLUGIN);
			Platform.getLog(bundle).log(
					new Status(IStatus.ERROR, 
							bundle.getSymbolicName(), 
							IStatus.OK,
							Messages.c4jRuntimeVariableError, 
							e)
					);
		}
	}
}

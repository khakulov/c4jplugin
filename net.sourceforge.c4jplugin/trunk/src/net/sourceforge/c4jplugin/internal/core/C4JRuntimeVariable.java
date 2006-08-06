package net.sourceforge.c4jplugin.internal.core;

import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.IValueVariableInitializer;

public class C4JRuntimeVariable implements IValueVariableInitializer {
	
	public void initialize(IValueVariable variable) {
		variable.setValue(C4JRuntimeContainer.getC4JRtClasspath());
	}
}

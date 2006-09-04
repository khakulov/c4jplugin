package net.sourceforge.c4jplugin.internal.ui.viewers;

import net.sourceforge.c4jplugin.internal.util.C4JUtils;

import org.eclipse.debug.core.ILaunchConfiguration;

public class LaunchConfigurationTreeElement {

	private ILaunchConfiguration config;
	private LaunchConfigurationTypeTreeElement parent;
	
	private boolean c4jEnabledOrig = false;
	private boolean c4jEnabled = false;
	private String vmargs = "";
	
	public LaunchConfigurationTreeElement(ILaunchConfiguration config, LaunchConfigurationTypeTreeElement parent) {
		this.parent = parent;
		this.config = config;

		this.c4jEnabledOrig = C4JUtils.isC4JEnabled(config);
		this.vmargs = C4JUtils.getVMArgs(config);
		
		c4jEnabled = c4jEnabledOrig;
	}
	
	public LaunchConfigurationTypeTreeElement getParent() {
		return parent;
	}

	public void setDefaults() {
		c4jEnabled = c4jEnabledOrig;
		vmargs = C4JUtils.getVMArgs(config);
	}
	
	public boolean isC4JEnabled() {
		return c4jEnabled;
	}
	
	public String getVMArguments() {
		return vmargs;
	}

	public void setC4JEnabled(boolean enabled) {
		if (enabled) {
			if (!vmargs.matches(".*-ea.*")) vmargs = "-ea " + vmargs;
			if (!vmargs.matches(C4JUtils.REGEXP_C4J_JAVAAGENT))
				vmargs = C4JUtils.C4J_JAVAAGENT + vmargs;
		}
		else {
			String[] segments = vmargs.split("\\s+");
			String newArgs = "";
			for (String segment : segments) {
				if (segment.matches(C4JUtils.REGEXP_C4J_JAVAAGENT)) continue;
				newArgs += segment + " ";
			}
			vmargs = newArgs;
		}
		c4jEnabled = enabled;
	}

	public ILaunchConfiguration getLaunchConfiguration() {
		return config;
	}
	
	public boolean isDirty() {
		if (c4jEnabled == c4jEnabledOrig) return false;
		return true;
	}
	
	public String toString() {
		return config.getName();
	}
	
}

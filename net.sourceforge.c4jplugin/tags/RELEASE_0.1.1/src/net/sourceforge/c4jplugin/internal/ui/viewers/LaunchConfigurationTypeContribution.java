package net.sourceforge.c4jplugin.internal.ui.viewers;

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.ui.IPluginContribution;

public class LaunchConfigurationTypeContribution implements IPluginContribution {
	
	protected ILaunchConfigurationType type;
	
	/**
	 * Creates a new plug-in contribution for the given type
	 * 
	 * @param type the launch configuration type
	 */
	public LaunchConfigurationTypeContribution(ILaunchConfigurationType type) {
		this.type= type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return type.getIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return type.getPluginIdentifier();
	}
	
	public String toString() {
		return type.getName();
	}
	
}
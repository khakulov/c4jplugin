package net.sourceforge.c4jplugin.internal.util;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.ui.viewers.LaunchConfigurationTreeElement;
import net.sourceforge.c4jplugin.internal.ui.viewers.LaunchConfigurationTypeTreeElement;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class C4JImages {

	public static final String CHECKBOX_YES = "icons/yes_check.gif";
	public static final String CHECKBOX_NO = "icons/no_check.gif";
	
	protected ImageRegistry registry = new ImageRegistry();
	
	private static C4JImages instance = null;
	
	private C4JImages() {
		registry.put(CHECKBOX_YES, C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, CHECKBOX_YES));
		registry.put(CHECKBOX_NO, C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, CHECKBOX_NO));
	}
	
	private static C4JImages getInstance() {
		if (instance == null)
			instance = new C4JImages();
		
		return instance;
	}
	
	public static Image getImage(String name) {
		Image img = getInstance().registry.get(name);
		
		return img;
	}
	
	public static Image getImage(Object object) {
		Image img = null;
		
		if (object instanceof LaunchConfigurationTypeTreeElement) {
			ILaunchConfigurationType type = (ILaunchConfigurationType)((LaunchConfigurationTypeTreeElement)object).getLaunchConfigurationType();
			img = getInstance().registry.get(type.getIdentifier());
			if (img == null) {
				getInstance().registry.put(type.getIdentifier(), DebugUITools.getDefaultImageDescriptor(type));
				img = getInstance().registry.get(type.getIdentifier());
			}
		}
		else if (object instanceof LaunchConfigurationTreeElement) {
			ILaunchConfiguration config = (ILaunchConfiguration)((LaunchConfigurationTreeElement)object).getLaunchConfiguration();
			String key = config.getName();
			img = getInstance().registry.get(key);
			if (img == null) {
				getInstance().registry.put(key, 
						DebugUITools.getDefaultImageDescriptor(config));
				img = getInstance().registry.get(key);
			}
		}
		
		return img;
	}
	
	public static ImageDescriptor getImageDescriptor(String name) {
		return getInstance().registry.getDescriptor(name);
	}
}

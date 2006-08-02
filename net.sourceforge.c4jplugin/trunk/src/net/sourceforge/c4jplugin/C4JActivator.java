package net.sourceforge.c4jplugin;

import net.sourceforge.c4jplugin.internal.core.C4JRuntimeContainer;
import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;
import net.sourceforge.c4jplugin.internal.util.C4JUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class C4JActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.sourceforge.c4j.plugin";
	
	public static final String RUNTIME_PLUGIN_ID = "net.sourceforge.c4j.runtime_plugin";
	
	public static final String C4JRT_CONTAINER = PLUGIN_ID + ".C4JRT_CONTAINER";

	// The shared instance
	private static C4JActivator plugin;
	
	private LaunchListener listener = null;
	
	private class LaunchListener implements ILaunchListener {

		private String originalVMArgs = "";
		
		public void launchAdded(ILaunch launch) {
			System.out.println("Launch with config " + launch.getLaunchConfiguration().getName() + " added");
			try {
				ILaunchConfigurationWorkingCopy launchConfig = launch.getLaunchConfiguration().getWorkingCopy();
				IResource[] resources = launchConfig.getMappedResources();
				for (IResource resource : resources) {
					if (resource.getProject().hasNature(C4JProjectNature.ID_NATURE)) {
						String originalVMArgs = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
						launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-javaagent:" + C4JRuntimeContainer.getC4JRtClasspath() + " -ea");
						launchConfig.doSave();
						break;
					}
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		public void launchChanged(ILaunch launch) {
			// TODO Auto-generated method stub
			System.out.println("Launch with config " + launch.getLaunchConfiguration().getName() + " changed");
		}

		public void launchRemoved(ILaunch launch) {
			// TODO Auto-generated method stub
			System.out.println("Launch with config " + launch.getLaunchConfiguration().getName() + " removed");
		}
		
	}
	
	/**
	 * The constructor
	 */
	public C4JActivator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		listener = new LaunchListener();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(listener);
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static C4JActivator getDefault() {
		return plugin;
	}

}

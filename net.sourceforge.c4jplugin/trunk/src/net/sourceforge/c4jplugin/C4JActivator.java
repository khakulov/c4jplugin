package net.sourceforge.c4jplugin;

import net.sourceforge.c4jplugin.internal.decorators.ResourceChangeListener;
import net.sourceforge.c4jplugin.internal.ui.preferences.C4JPreferences;
import net.sourceforge.c4jplugin.internal.util.C4JUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class C4JActivator extends AbstractUIPlugin implements ILaunchListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.sourceforge.c4jplugin";
	
	public static final String RUNTIME_PLUGIN_ID = "net.sourceforge.c4jplugin.runtime";
	
	public static final String C4JRT_CONTAINER = PLUGIN_ID + ".C4JRT_CONTAINER";

	// The shared instance
	private static C4JActivator plugin;
	
	private ResourceChangeListener resourceChangeListener = null;
	
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
		
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		
		resourceChangeListener = new ResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_BUILD);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		
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


	public void launchAdded(ILaunch launch) {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		IProject project = C4JUtils.getC4JProjectFromLaunchConfig(config);
		try {
			if (project != null && 	C4JPreferences.doChangeLaunchConfig(project, config.getType().getIdentifier())) {
				C4JUtils.addVMArgsToLaunchConfig(config, launch.getLaunchMode());
			}
		} catch (CoreException e) {}
	}

	public void launchChanged(ILaunch launch) {}

	public void launchRemoved(ILaunch launch) {}

}

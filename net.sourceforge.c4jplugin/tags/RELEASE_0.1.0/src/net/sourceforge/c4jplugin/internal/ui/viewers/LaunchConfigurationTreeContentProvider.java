package net.sourceforge.c4jplugin.internal.ui.viewers;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.ui.preferences.C4JPreferences;
import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;
import net.sourceforge.c4jplugin.internal.util.C4JUtils;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.ui.launcher.AbstractPDELaunchConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

public class LaunchConfigurationTreeContentProvider  implements ITreeContentProvider {

	/**
	 * Empty Object array
	 */
	private static final Object[] EMPTY_ARRAY = new Object[0];	
	
	private ArrayList<LaunchConfigurationTypeTreeElement> elements = new ArrayList<LaunchConfigurationTypeTreeElement>();
	
	
	/**
	 * The mode in which the tree is being shown, one of <code>RUN_MODE</code> 
	 * or <code>DEBUG_MODE</code> defined in <code>ILaunchManager</code>.
	 * If this is <code>null</code>, then it means both modes are being shown.
	 */
	private String fMode;
	
	/**
	 * The Shell context
	 */
	private Shell fShell;
	
	public LaunchConfigurationTreeContentProvider(String mode, Shell shell) {
		setMode(mode);
		setShell(shell);
	}

	/**
	 * Actual launch configurations have no children.  Launch configuration types have
	 * all configurations of that type as children, minus any configurations that are 
	 * marked as private.
	 * <p>
	 * In 2.1, the <code>category</code> attribute was added to launch config
	 * types. The debug UI only displays those configs that do not specify a
	 * category.
	 * </p>
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof LaunchConfigurationTreeElement) {
			return EMPTY_ARRAY;
		} else if (parentElement instanceof LaunchConfigurationTypeTreeElement) {
			try {
				LaunchConfigurationTypeTreeElement parentItem = (LaunchConfigurationTypeTreeElement)parentElement;
				Object[] children = parentItem.getChildren();
				if (children == null) {
					ILaunchConfigurationType type = (parentItem).getLaunchConfigurationType();
					for (ILaunchConfiguration config : getLaunchManager().getLaunchConfigurations(type)) {
						if (C4JUtils.isC4JLaunchConfig(config)) {
							LaunchConfigurationTreeElement child = new LaunchConfigurationTreeElement(config, (LaunchConfigurationTypeTreeElement)parentElement);
							parentItem.addChild(child);
						}
					}
					children = parentItem.getChildren();
				}
				if (children == null) return EMPTY_ARRAY;
				return children;
			} catch (CoreException e) {
				IStatus status = new Status(IStatus.ERROR, C4JActivator.PLUGIN_ID, IStatus.OK, UIMessages.DialogMsg_launchConfig_errorRetrievingConfigs, e);
				C4JActivator.getDefault().getLog().log(status);
				ErrorDialog.openError(getShell(), null, null, status); 
			}
		} else {
			return getElements(null);
		}
		return EMPTY_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof LaunchConfigurationTreeElement) {
			ILaunchConfiguration config = ((LaunchConfigurationTreeElement)element).getLaunchConfiguration();
			if (!config.exists()) {
				return null;
			}
			return ((LaunchConfigurationTreeElement)element).getParent();
		} else if (element instanceof LaunchConfigurationTypeTreeElement) {
			return ResourcesPlugin.getWorkspace().getRoot();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof LaunchConfigurationTreeElement) {
			return false;
		} 
		else {
			return getChildren(element).length > 0;
		}
	}

	/**
	 * Return only the launch configuration types that support the current mode AND
	 * are marked as 'public'.
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (elements != null && elements.size() > 0) return elements.toArray();
		
		ILaunchConfigurationType[] allTypes = getLaunchManager().getLaunchConfigurationTypes();
		
		for (ILaunchConfigurationType type : filterTypes(allTypes)) {
			LaunchConfigurationTypeTreeElement element = new LaunchConfigurationTypeTreeElement(type);
			element.setChangeVMArguments(C4JPreferences.doChangeLaunchConfig(type.getIdentifier()));
			element.setAskChangeVMArguments(C4JPreferences.askChangeLaunchConfig(type.getIdentifier()));
			elements.add(element);
		}
		
		return elements.toArray();
	}

	/**
	 * Returns a list containing the given types minus any types that
	 * should not be visible. A type should not be visible if it doesn't match
	 * the current mode or if it matches a disabled activity.
	 * 
	 * @param allTypes the types
	 * @return the given types minus any types that should not be visible.
	 */
	private List<ILaunchConfigurationType> filterTypes(ILaunchConfigurationType[] allTypes) {
		List<ILaunchConfigurationType> filteredTypes= new ArrayList<ILaunchConfigurationType>();
		String mode = getMode();
		LaunchConfigurationTypeContribution contribution;
		for (ILaunchConfigurationType type : allTypes) {
			contribution= new LaunchConfigurationTypeContribution(type);
			if (isVisible(type, mode) && !WorkbenchActivityHelper.filterItem(contribution)) {
				filteredTypes.add(type);
			}
		}
		return filteredTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	/**
	 * Return <code>true</code> if the specified launch configuration type should
	 * be visible in the specified mode, <code>false</code> otherwise.
	 */
	private boolean isVisible(ILaunchConfigurationType configType, String mode) {
		if (!configType.isPublic()) {
			return false;
		}
		
		ILaunchConfigurationDelegate configDelegate = null;
		try {
			if (mode == null)
				configDelegate = configType.getDelegate(ILaunchManager.RUN_MODE);
			else
				configDelegate = configType.getDelegate(mode);
		} catch (CoreException e1) {
			try {
				configDelegate = configType.getDelegate(ILaunchManager.DEBUG_MODE);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		if (configDelegate instanceof AbstractJavaLaunchConfigurationDelegate ||
				configDelegate instanceof AbstractPDELaunchConfiguration) {
			if (mode == null) return true;
			return configType.supportsMode(mode);
		}
		
		return false;
	}

	/**
	 * Convenience method to get the singleton launch manager.
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Write accessor for the mode value
	 */
	private void setMode(String mode) {
		fMode = mode;
	}
	
	/**
	 * Read accessor for the mode value
	 */
	private String getMode() {
		return fMode;
	}

	/**
	 * Write accessor for the shell value
	 */
	private void setShell(Shell shell) {
		fShell = shell;
	}
	
	/**
	 * Read accessor for the shell value
	 */
	private Shell getShell() {
		return fShell;
	}
}

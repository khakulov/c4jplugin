package net.sourceforge.c4jplugin.internal.ui.preferences;

import java.util.ArrayList;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.decorators.ContractDecorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

public class C4JPreferences {
		
	// Automation Preferences
	// ------------------------------------
	public static final String ASK_PDE_AUTO_IMPORT = C4JActivator.PLUGIN_ID + ".preferences.askPdeAutoImport"; //$NON-NLS-1$
	public static final String DO_PDE_AUTO_IMPORT = C4JActivator.PLUGIN_ID + ".preferences.doPdeAutoImport"; //$NON-NLS-1$
	public static final String ASK_PDE_AUTO_REMOVE_IMPORT = C4JActivator.PLUGIN_ID + ".preferences.askPdeAutoRemoveImport"; //$NON-NLS-1$
	public static final String DO_PDE_AUTO_REMOVE_IMPORT = C4JActivator.PLUGIN_ID + ".preferences.doPdeAutoRemoveImport"; //$NON-NLS-1$
	public static final String DO_APT_AUTO_ENABLE = C4JActivator.PLUGIN_ID + ".preferences.doAptAutoEnable";
	public static final String ASK_APT_AUTO_ENABLE = C4JActivator.PLUGIN_ID + ".preferences.askAptAutoEnable";
	public static final String APT_AUTO_ENABLE_DONE = C4JActivator.PLUGIN_ID + ".preferences.aptAutoEnableDone";
	public static final String DO_APT_AUTO_DISABLE = C4JActivator.PLUGIN_ID + ".preferences.doAptAutoDisable";
	public static final String ASK_APT_AUTO_DISABLE = C4JActivator.PLUGIN_ID + ".preferences.askAptAutoDisable";
	
	// Decoration Preferences
	// ------------------------------
	public static final String DECORATION_CLASSES = C4JActivator.PLUGIN_ID + ".preferences.decorationClasses";
	public static final String DECORATION_METHODS = C4JActivator.PLUGIN_ID + ".preferences.decorationMethods";
	public static final String DECORATION_POSITION = C4JActivator.PLUGIN_ID + ".preferences.decorationPosition";
	
	private static ArrayList<String> configTypes = new ArrayList<String>();
	
	static {
		configTypes.add(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);
		configTypes.add(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		configTypes.add(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);
		configTypes.add("org.eclipse.pde.ui.RuntimeWorkbench");
		configTypes.add("org.eclipse.pde.ui.EquinoxLauncher");
		configTypes.add("org.eclipse.pde.ui.swtLaunchConfig");
	}
	
	
	/**
	 * Helper set method
	 * 
	 * @param ask
	 *            true if the user wants to be asked again about having auto
	 *            import c4j library upon adding c4j nature to PDE project.
	 */
	static public void setAskPDEAutoImport(boolean ask) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(ASK_PDE_AUTO_IMPORT, ask);
	}

	/**
	 * Helper get method used to determine whether to ask the user if they want
	 * to auto import the c4j library from the appropriate plugin.
	 * 
	 * @return boolean true if user is to be asked
	 */
	static public boolean askPDEAutoImport() {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		return store.getBoolean(ASK_PDE_AUTO_IMPORT);
	}
	
	static public boolean defaultAskPDEAutoImport() {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		return store.getDefaultBoolean(ASK_PDE_AUTO_IMPORT);
	}
	
	static public void setDoPDEAutoImport(boolean doImport) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(DO_PDE_AUTO_IMPORT, doImport);
	}

	static public boolean doPDEAutoImport() {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		return store.getBoolean(DO_PDE_AUTO_IMPORT);
	}
	
	static public boolean defaultDoPDEAutoImport() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(DO_PDE_AUTO_IMPORT);
	}
	
	/**
	 * Helper set method
	 * 
	 * @param ask
	 *            true if the user wants to be asked again about having auto
	 *            removal of aspectj runtime library import upon removing aspectj nature
	 *            from PDE projects.
	 */
	static public void setAskPDEAutoRemoveImport(boolean ask) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(ASK_PDE_AUTO_REMOVE_IMPORT, ask);
	}
	
	/**
	 * Helper get method used to determine whether to ask the user if they want
	 * to automatically remove the the aspectj runtime library import from the 
	 * appropriate plugin.
	 * 
	 * @return boolean true if user is to be asked
	 */
	static public boolean askPDEAutoRemoveImport() {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		return store.getBoolean(ASK_PDE_AUTO_REMOVE_IMPORT);
	}
	
	static public boolean defaultAskPDEAutoRemoveImport() {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		return store.getDefaultBoolean(ASK_PDE_AUTO_REMOVE_IMPORT);
	}

	static public void setDoPDEAutoRemoveImport(boolean doImport) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(DO_PDE_AUTO_REMOVE_IMPORT, doImport);
	}

	static public boolean doPDEAutoRemoveImport() {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		return store.getBoolean(DO_PDE_AUTO_REMOVE_IMPORT);
	}
	
	static public boolean defaultDoPDEAutoRemoveImport() {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		return store.getDefaultBoolean(DO_PDE_AUTO_REMOVE_IMPORT);
	}
	
	static public boolean doAptAutoEnable() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getBoolean(DO_APT_AUTO_ENABLE);
	}
	
	static public boolean defaultDoAptAutoEnable() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(DO_APT_AUTO_ENABLE);
	}
	
	static public void setDoAptAutoEnable(boolean enable) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(DO_APT_AUTO_ENABLE, enable);
	}
	
	static public boolean askAptAutoEnable() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getBoolean(ASK_APT_AUTO_ENABLE);
	}
	
	static public boolean defaultAskAptAutoEnable() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(ASK_APT_AUTO_ENABLE);
	}
	
	static public void setAskAptAutoEnable(boolean enable) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(ASK_APT_AUTO_ENABLE, enable);
	}
	
	static public boolean doAptAutoDisable() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getBoolean(DO_APT_AUTO_DISABLE);
	}
	
	static public boolean defaultDoAptAutoDisable() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(DO_APT_AUTO_DISABLE);
	}
	
	static public void setDoAptAutoDisable(boolean enable) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(DO_APT_AUTO_DISABLE, enable);
	}
	
	static public boolean askAptAutoDisable() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getBoolean(ASK_APT_AUTO_DISABLE);
	}
	
	static public boolean defaultAskAptAutoDisable() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(ASK_APT_AUTO_DISABLE);
	}
	
	static public void setAskAptAutoDisable(boolean enable) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(ASK_APT_AUTO_DISABLE, enable);
	}
	
	
	// Decoration Preferences
	// -----------------------------------------------
	
	static public int getDecorationPosition() {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		return store.getInt(DECORATION_POSITION);
	}
	
	static public int getDefaultDecorationPosition() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultInt(DECORATION_POSITION);
	}
	
	static public void setDecorationPosition(int pos) {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		store.setValue(DECORATION_POSITION, pos);
		ContractDecorator.setPosition(pos);
	}
	
	static public boolean getDecorateClasses() {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		return store.getBoolean(DECORATION_CLASSES);
	}
	
	static public boolean getDefaultDecorateClasses() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(DECORATION_CLASSES);
	}
	
	static public void setDecorateClasses(boolean value) {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		store.setValue(DECORATION_CLASSES, value);
		ContractDecorator.setDecorateClasses(value);
	}
	
	static public boolean getDecorateMethods() {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		return store.getBoolean(DECORATION_METHODS);
	}
	
	static public boolean getDefaultDecorateMethods() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(DECORATION_METHODS);
	}
	
	static public void setDecorateMethods(boolean value) {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		store.setValue(DECORATION_METHODS, value);
		ContractDecorator.setDecorateMethods(value);
	}
	
	// Project Scope Preferences
	// ------------------------------------------------
	
	static public boolean doChangeLaunchConfig(IProject project, String identifier) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(C4JActivator.PLUGIN_ID);
		return projectNode.getBoolean(identifier, configTypes.contains(identifier));
	}
	
	static public boolean askChangeLaunchConfig(IProject project, String identifier) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(C4JActivator.PLUGIN_ID);
		return projectNode.getBoolean(identifier + "_ask", configTypes.contains(identifier));
	}
	
	static public boolean defaultDoChangeLaunchConfig(String identifier) {
		return configTypes.contains(identifier);
	}
	
	static public boolean defaultAskChangeLaunchConfig(String identifier) {
		return configTypes.contains(identifier);
	}
	
	static public void setDoChangeLaunchConfig(IProject project, String identifier, boolean value) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(C4JActivator.PLUGIN_ID);
		projectNode.putBoolean(identifier, value);
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}
	
	static public void setAskChangeLaunchConfig(IProject project, String identifier, boolean value) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(C4JActivator.PLUGIN_ID);
		projectNode.putBoolean(identifier + "_ask", value);
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}
	
	static public boolean isAptAutoEnableDone(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(C4JActivator.PLUGIN_ID);
		return projectNode.getBoolean(APT_AUTO_ENABLE_DONE, false);
	}
	
	static public void setAptAutoEnableDone(IProject project, boolean value) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(C4JActivator.PLUGIN_ID);
		projectNode.putBoolean(APT_AUTO_ENABLE_DONE, value);
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}
	
}

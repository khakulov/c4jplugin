package net.sourceforge.c4jplugin.internal.ui.preferences;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.decorators.C4JDecorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
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
	public static final String DECORATION_CONTRACTED_CLASSES = C4JActivator.PLUGIN_ID + ".preferences.decorationContractedClasses";
	public static final String DECORATION_CONTRACTED_METHODS = C4JActivator.PLUGIN_ID + ".preferences.decorationContractedMethods";
	public static final String DECORATION_CONTRACTS = C4JActivator.PLUGIN_ID + ".preferences.decorationContracts";
	public static final String DECORATION_CONTRACT_METHODS = C4JActivator.PLUGIN_ID + ".preferences.decorationContractMethods";
	public static final String DECORATION_POSITION = C4JActivator.PLUGIN_ID + ".preferences.decorationPosition";
	
	
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
		C4JDecorator.setPosition(pos);
	}
	
	static public boolean getDecorateContractedClasses() {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		return store.getBoolean(DECORATION_CONTRACTED_CLASSES);
	}
	
	static public boolean getDefaultDecorateContractedClasses() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(DECORATION_CONTRACTED_CLASSES);
	}
	
	static public void setDecorateContractedClasses(boolean value) {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		store.setValue(DECORATION_CONTRACTED_CLASSES, value);
		C4JDecorator.setDecorateContractedClasses(value);
	}
	
	static public boolean getDecorateContractedMethods() {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		return store.getBoolean(DECORATION_CONTRACTED_METHODS);
	}
	
	static public boolean getDefaultDecorateContractedMethods() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(DECORATION_CONTRACTED_METHODS);
	}
	
	static public void setDecorateContractedMethods(boolean value) {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		store.setValue(DECORATION_CONTRACTED_METHODS, value);
		C4JDecorator.setDecorateContractedMethods(value);
	}
	
	static public boolean getDecorateContracts() {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		return store.getBoolean(DECORATION_CONTRACTS);
	}
	
	static public boolean getDefaultDecorateContracts() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(DECORATION_CONTRACTS);
	}
	
	static public void setDecorateContracts(boolean value) {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		store.setValue(DECORATION_CONTRACTS, value);
		C4JDecorator.setDecorateContracts(value);
	}
	
	static public boolean getDecorateContractMethods() {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		return store.getBoolean(DECORATION_CONTRACT_METHODS);
	}
	
	static public boolean getDefaultDecorateContractMethods() {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(DECORATION_CONTRACT_METHODS);
	}
	
	static public void setDecorateContractMethods(boolean value) {
		IPreferenceStore store = C4JActivator.getDefault()
			.getPreferenceStore();
		store.setValue(DECORATION_CONTRACT_METHODS, value);
		C4JDecorator.setDecorateContractMethods(value);
	}
	
	static public boolean doChangeLaunchConfig(String identifier) {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getBoolean(identifier);
	}
	
	static public boolean askChangeLaunchConfig(String identifier) {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getBoolean(identifier + "_ask");
	}
	
	static public boolean defaultDoChangeLaunchConfig(String identifier) {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(identifier);
	}
	
	static public boolean defaultAskChangeLaunchConfig(String identifier) {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		return store.getDefaultBoolean(identifier);
	}
	
	static public void setDoChangeLaunchConfig(String identifier, boolean value) {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		store.setValue(identifier, value);
	}
	
	static public void setAskChangeLaunchConfig(String identifier, boolean value) {
		IPreferenceStore store = C4JActivator.getDefault()
		.getPreferenceStore();
		store.setValue(identifier + "_ask", value);
	}
	
	
	
	// Project Scope Preferences
	// -------------------------------------------------------------
	
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

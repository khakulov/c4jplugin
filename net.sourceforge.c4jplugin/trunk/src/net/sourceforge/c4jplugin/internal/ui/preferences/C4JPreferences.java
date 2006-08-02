package net.sourceforge.c4jplugin.internal.ui.preferences;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.jface.preference.IPreferenceStore;

public class C4JPreferences {
	
	public static final String ASK_PDE_AUTO_IMPORT = C4JActivator.PLUGIN_ID + ".preferences.askPdeAutoImport"; //$NON-NLS-1$

	public static final String DO_PDE_AUTO_IMPORT = C4JActivator.PLUGIN_ID + ".preferences.doPdeAutoImport"; //$NON-NLS-1$

	public static final String ASK_PDE_AUTO_REMOVE_IMPORT = C4JActivator.PLUGIN_ID + ".preferences.askPdeAutoRemoveImport"; //$NON-NLS-1$

	public static final String DO_PDE_AUTO_REMOVE_IMPORT = C4JActivator.PLUGIN_ID + ".preferences.doPdeAutoRemoveImport"; //$NON-NLS-1$

	public static final String PDE_AUTO_IMPORT_CONFIG_DONE = C4JActivator.PLUGIN_ID + ".preferences.pdeAutoImportConfigDone"; //$NON-NLS-1$

	public static final String PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE = C4JActivator.PLUGIN_ID + ".preferences.pdeAutoRemoveImportConfigDone"; //$NON-NLS-1$
	
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

	static public void setPDEAutoRemoveImportConfigDone(boolean done) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE, done);
	}

	static public boolean isPDEAutoRemoveImportConfigDone() {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		return store.getBoolean(PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE);
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
	
	static public void setPDEAutoImportConfigDone(boolean done) {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		store.setValue(PDE_AUTO_IMPORT_CONFIG_DONE, done);
	}

	public static boolean isPDEAutoImportConfigDone() {
		IPreferenceStore store = C4JActivator.getDefault()
				.getPreferenceStore();
		return store.getBoolean(PDE_AUTO_IMPORT_CONFIG_DONE);
	}
}

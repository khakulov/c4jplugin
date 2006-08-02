package net.sourceforge.c4jplugin.internal.ui.preferences;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class C4JPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = C4JActivator.getDefault().getPreferenceStore();
		
		store.setDefault(C4JPreferences.PDE_AUTO_IMPORT_CONFIG_DONE, false);
		store.setDefault(C4JPreferences.ASK_PDE_AUTO_IMPORT, true);
		store.setDefault(C4JPreferences.DO_PDE_AUTO_IMPORT, false);
		store.setDefault(C4JPreferences.PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE, false);
		store.setDefault(C4JPreferences.ASK_PDE_AUTO_REMOVE_IMPORT, true);
		store.setDefault(C4JPreferences.DO_PDE_AUTO_REMOVE_IMPORT, false);
	}

}

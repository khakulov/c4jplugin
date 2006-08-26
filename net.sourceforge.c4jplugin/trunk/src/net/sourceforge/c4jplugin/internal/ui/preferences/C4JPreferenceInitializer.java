package net.sourceforge.c4jplugin.internal.ui.preferences;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IDecoration;

public class C4JPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = C4JActivator.getDefault().getPreferenceStore();
		
		store.setDefault(C4JPreferences.ASK_PDE_AUTO_IMPORT, true);
		store.setDefault(C4JPreferences.DO_PDE_AUTO_IMPORT, true);
		store.setDefault(C4JPreferences.ASK_PDE_AUTO_REMOVE_IMPORT, true);
		store.setDefault(C4JPreferences.DO_PDE_AUTO_REMOVE_IMPORT, true);
		
		store.setDefault(C4JPreferences.DO_APT_AUTO_ENABLE, true);
		store.setDefault(C4JPreferences.ASK_APT_AUTO_ENABLE, true);
		
		store.setDefault(C4JPreferences.DO_APT_AUTO_DISABLE, true);
		store.setDefault(C4JPreferences.ASK_APT_AUTO_DISABLE, true);
		
		store.setDefault(C4JPreferences.DECORATION_POSITION, IDecoration.TOP_LEFT);
		store.setDefault(C4JPreferences.DECORATION_CONTRACTED_CLASSES, true);
		store.setDefault(C4JPreferences.DECORATION_CONTRACTED_METHODS, true);
		store.setDefault(C4JPreferences.DECORATION_CONTRACTS, true);
		store.setDefault(C4JPreferences.DECORATION_CONTRACT_METHODS, true);
	}

}

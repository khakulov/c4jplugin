package net.sourceforge.c4jplugin.internal.ui.text;

import org.eclipse.osgi.util.NLS;

public class UIMessages extends NLS {

	private static final String BUNDLE_NAME = UIMessages.class.getName();
	
	private UIMessages() {
		
	}
	
	public static String utils_refresh_explorer_job;
	
	// Dialogs
	public static String PluginImportDialog_importConfirmTitle;
	public static String PluginImportDialog_importConfirmMsg;
	public static String PluginImportDialog_importConfirmToggleMsg;

	public static String NoAutoPluginImportDialog_title;
	public static String NoAutoPluginImportDialog_message;
	public static String AutoPluginImportDialog_noEditor_title;
	public static String AutoPluginImportDialog_noEditor_message;
	
	public static String PluginImportDialog_removeImportConfirmTitle;
	public static String PluginImportDialog_removeImportConfirmMsg;
	public static String PluginImportDialog_removeImportConfirmToggleMsg;
	
	public static String AutoPluginRemoveErrorDialog_title;
	public static String AutoPluginRemoveErrorDialog_message;
	
	public static String AutoPluginRemoveDialog_noEditor_title;
	public static String AutoPluginRemoveDialog_noEditor_message;
	
	public static String AutoPluginEnableApt_title;
	public static String AutoPluginEnableApt_message;
	public static String AutoPluginEnableApt_toggleMsg;

	public static String AutoPluginDisableApt_title;
	public static String AutoPluginDisableApt_message;
	public static String AutoPluginDisableApt_toggleMsg;
	
	// Project Preference Pages
	public static String ProjectPreferencesLaunch_title;
	public static String ProjectPreferencesLaunch_tableHeader_type;
	public static String ProjectPreferencesLaunch_tableHeader_confirm;
	
	public static String ProjectPreferencesLaunch_buttonSelectAll;
	public static String ProjectPreferencesLaunch_buttonSelectNone;
	public static String ProjectPreferencesLaunch_buttonConfirmAll;
	public static String ProjectPreferencesLaunch_buttonConfirmNone;
	
	// Window Preference Pages
	public static String PreferencesMain_title;
	public static String PreferencesMain_hint;
	
	public static String PreferencesAutomation_title;
	public static String PreferencesAutomation_descr;
	public static String PreferencesAutomation_enableHeader;
	public static String PreferencesAutomation_disableHeader;
	public static String PreferencesAutomation_importDependency;
	public static String PreferencesAutomation_removeDependency;
	public static String PreferencesAutomation_enableApt;
	public static String PreferencesAutomation_disableApt;
	public static String PreferencesAutomation_confirmDialog;

	public static String PreferencesDecorations_title;
	public static String PreferencesDecorations_decoHeader;
	public static String PreferencesDecorations_decoClasses;
	public static String PreferencesDecorations_decoMethods;
	public static String PreferencesDecorations_posHeader;
	public static String PreferencesDecorations_posUL;
	public static String PreferencesDecorations_posLL;
	public static String PreferencesDecorations_posUR;
	public static String PreferencesDecorations_posLR;
	public static String PreferencesDecorations_preview;
	
	public static String PreferencesDecorations_warningMsg_disabledDeco;
	public static String PreferencesDecorations_infoMsg_willDisableDeco;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, UIMessages.class);
	}
}

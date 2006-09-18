package net.sourceforge.c4jplugin.internal.ui.text;

import org.eclipse.osgi.util.NLS;

public class UIMessages extends NLS {

	private static final String BUNDLE_NAME = UIMessages.class.getName();
	
	private UIMessages() {
		
	}
	
	public static String Msg_error;
	
	// Jobs
	public static String utils_refresh_explorer_job;
	public static String Builder_jobTitle;
	public static String Builder_startModelJob;
	public static String Builder_startRefreshContractModel;
	public static String Builder_checkingContractedClasses;
	public static String Builder_creatingContractMarkers;
	
	// Annotation Processor
	public static String AnnotationProcessor_error_contractNotFound;
	public static String AnnotationProcessor_error_contractAmbiguous;
	public static String AnnotationProcessor_warning_contractHasErrors;
	
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
	
	public static String DialogMsg_changeLaunchConfig_title;
	public static String DialogMsg_changeLaunchConfig_message;
	public static String DialogMsg_changeLaunchConfig_toggleMsg;
	
	public static String DialogMsg_launchConfig_errorRetrievingConfigs;
	
	// Window Preference Pages
	public static String PreferencesLaunch_title;
	public static String PreferencesLaunch_tableHeader_label;
	public static String PreferencesLaunch_tableHeader_enable;
	public static String PreferencesLaunch_tableHeader_confirm;
	public static String PreferencesLaunch_labelVMArgs;
	
	public static String PreferencesLaunch_buttonSelectAll;
	public static String PreferencesLaunch_buttonSelectNone;
	public static String PreferencesLaunch_buttonConfirmAll;
	public static String PreferencesLaunch_buttonConfirmNone;
	
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
	public static String PreferencesDecorations_decoContractedClasses;
	public static String PreferencesDecorations_decoContractedMethods;
	public static String PreferencesDecorations_decoContracts;
	public static String PreferencesDecorations_decoContractMethods;
	public static String PreferencesDecorations_posHeader;
	public static String PreferencesDecorations_posUL;
	public static String PreferencesDecorations_posLL;
	public static String PreferencesDecorations_posUR;
	public static String PreferencesDecorations_posLR;
	
	public static String PreferencesDecorations_warningMsg_disabledDeco;
	public static String PreferencesDecorations_infoMsg_willDisableDeco;
	
	// Marker Messages
	public static String MarkerMessage_contract_methodIsContracting;
	public static String MarkerMessage_contract_classInvariant;
	public static String MarkerMessage_contracted_classInvariant;
	public static String MarkerMessage_contracted_preMethod;
	public static String MarkerMessage_contracted_postMethod;
	public static String MarkerMessage_contracted_prepostMethod;
	public static String MarkerMessage_problem_methodNotContracting;
	
	// Log Messages
	public static String LogMessage_writingStateFailed;
	public static String LogMessage_readingStateFailed;
	
	public static String LogMessage_updatingVMArgsFailed;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, UIMessages.class);
	}
}

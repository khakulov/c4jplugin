package net.sourceforge.c4jplugin.internal.ui.text;

import org.eclipse.osgi.util.NLS;

public class UIMessages extends NLS {

	private static final String BUNDLE_NAME = UIMessages.class.getName();
	
	private UIMessages() {
		
	}
	
	public static String utils_refresh_explorer_job;
	
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
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, UIMessages.class);
	}
}

package net.sourceforge.c4jplugin.internal.ui.actions;

import org.eclipse.osgi.util.NLS;

public class ActionMessages extends NLS {

	private static final String BUNDLE_NAME= "net.sourceforge.c4jplugin.internal.ui.actions.ActionMessages";//$NON-NLS-1$

	static {
		NLS.initializeMessages(BUNDLE_NAME, ActionMessages.class);
	}
	
	private ActionMessages() {
		// Do not instantiate
	}
	
	public static String OpenContractHierarchyAction_label;
	public static String OpenContractHierarchyAction_tooltip;
	public static String OpenContractHierarchyAction_description;
	public static String OpenContractHierarchyAction_dialog_title;
	public static String OpenContractHierarchyAction_messages_title;
	public static String OpenContractHierarchyAction_messages_no_java_element;
	public static String OpenContractHierarchyAction_messages_no_java_resources;
	public static String OpenContractHierarchyAction_messages_no_types;
	public static String OpenContractHierarchyAction_messages_no_valid_java_element;
	public static String OpenContractHierarchyAction_messages_unknown_import_decl;
	
	public static String SelectionConverter_codeResolve_failed;
	
}

package net.sourceforge.c4jplugin.internal.wizards;

import org.eclipse.osgi.util.NLS;

public class WizardMessages extends NLS {

	private static final String BUNDLE_NAME= WizardMessages.class.getName();

	private WizardMessages() {
		
	}

	public static String NewContractWizardPageOne_error_junit4NotOnbuildpath;
	public static String NewContractWizardPageOne_error_java5required;
	public static String NewContractWizardPageOne_linkedtext_java5required;
	public static String NewContractWizardPageOne_linkedtext_junit3_notonbuildpath;
	public static String NewContractWizardPageOne_not_yet_implemented_string;
	
	public static String CheckedTableSelectionDialog_emptyListMessage;
	public static String CheckedTableSelectionDialog_selectAll;
	public static String CheckedTableSelectionDialog_deselectAll;
	
	public static String NewContractWizardPageOne_title;
	public static String NewContractWizardPageOne_description;
	public static String NewContractWizardPageOne_methodStub_constructor;
	public static String NewContractWizardPageOne_method_Stub_label;
	public static String NewContractWizardPageOne_class_to_test_label;
	public static String NewContractWizardPageOne_class_to_test_browse;
	public static String NewContractWizardPageOne_class_to_test_dialog_title;
	public static String NewContractWizardPageOne_class_to_test_dialog_message;
	public static String NewContractWizardPageOne_error_class_to_contract_is_target;
	public static String NewContractWizardPageOne_error_class_to_contract_is_contract;
	public static String NewContractWizardPageOne_error_class_to_test_not_valid;
	public static String NewContractWizardPageOne_error_class_to_test_not_exist;
	public static String NewContractWizardPageOne_warning_class_to_test_not_visible;
	public static String NewContractWizardPageOne_comment_class_to_test;
	public static String NewContractWizardPageOne_error_junitNotOnbuildpath;
	
	public static String NewContractWizardPageTwo_selected_methods_label_one;
	public static String NewContractWizardPageTwo_selected_methods_label_many;
	public static String NewContractWizardPageTwo_title;
	public static String NewContractWizardPageTwo_description;
	public static String NewContractWizardPageTwo_create_tasks_text;
	public static String NewContractWizardPageTwo_create_final_method_stubs_text;
	public static String NewContractWizardPageTwo_methods_tree_label;
	public static String NewContractWizardPageTwo_selectAll;
	public static String NewContractWizardPageTwo_deselectAll;
	
	
	// New Contract Wizard
	public static String NewContractWizard_op_error_title;
	public static String NewContractWizard_op_error_message;
	
	public static String NewContractWizard_annotation_error_title;
	public static String NewContractWizard_annotation_error_message;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, WizardMessages.class);
	}
}
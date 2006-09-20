package net.sourceforge.c4jplugin.internal.ui.preferences;

import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class C4JAutomationPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener {

	private Button buttonAutoImport;
	private Button buttonConfirmAutoImport;
	private Button buttonAutoRemoveImport;
	private Button buttonConfirmAutoRemove;
	private Button buttonEnableApt;
	private Button buttonConfirmApt;
	private Button buttonDisableApt;
	private Button buttonConfirmDisableApt;

	public void init(IWorkbench workbench) {
		
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite compDialogs = new Composite(parent, SWT.NONE);
		compDialogs.setLayout(new GridLayout(1, false));
		
		Label labelDescr = new Label(compDialogs, SWT.WRAP);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		labelDescr.setLayoutData(gridData);
		labelDescr.setText(UIMessages.PreferencesAutomation_descr);
		
		Label labelEnable = new Label(compDialogs, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 20;
		labelEnable.setLayoutData(gridData);
		labelEnable.setText(UIMessages.PreferencesAutomation_enableHeader);
		
		buttonAutoImport = new Button(compDialogs, SWT.CHECK);
		buttonConfirmAutoImport = new Button(compDialogs, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalIndent = 20;
		buttonConfirmAutoImport.setLayoutData(gridData);
		
		buttonEnableApt = new Button(compDialogs, SWT.CHECK);
		buttonConfirmApt = new Button(compDialogs, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalIndent = 20;
		buttonConfirmApt.setLayoutData(gridData);
		
		Label labelDisable = new Label(compDialogs, SWT.NONE);
		labelDisable.setText(UIMessages.PreferencesAutomation_disableHeader);
		gridData = new GridData();
		gridData.verticalIndent = 20;
		labelDisable.setLayoutData(gridData);
		
		buttonAutoRemoveImport = new Button(compDialogs, SWT.CHECK);
		buttonConfirmAutoRemove = new Button(compDialogs, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalIndent = 20;
		buttonConfirmAutoRemove.setLayoutData(gridData);
		
		buttonDisableApt = new Button(compDialogs, SWT.CHECK);
		buttonConfirmDisableApt = new Button(compDialogs, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalIndent = 20;
		buttonConfirmDisableApt.setLayoutData(gridData);
		
		buttonAutoImport.addSelectionListener(this);
		buttonAutoRemoveImport.addSelectionListener(this);
		buttonEnableApt.addSelectionListener(this);
		buttonDisableApt.addSelectionListener(this);
		
		buttonAutoImport.setText(UIMessages.PreferencesAutomation_importDependency);
		buttonConfirmAutoImport.setText(UIMessages.PreferencesAutomation_confirmDialog);
		buttonAutoRemoveImport.setText(UIMessages.PreferencesAutomation_removeDependency);
		buttonConfirmAutoRemove.setText(UIMessages.PreferencesAutomation_confirmDialog);
		buttonEnableApt.setText(UIMessages.PreferencesAutomation_enableApt);
		buttonConfirmApt.setText(UIMessages.PreferencesAutomation_confirmDialog);
		buttonDisableApt.setText(UIMessages.PreferencesAutomation_disableApt);
		buttonConfirmDisableApt.setText(UIMessages.PreferencesAutomation_confirmDialog);
		
		buttonAutoImport.setSelection(C4JPreferences.doPDEAutoImport());
		buttonConfirmAutoImport.setSelection(C4JPreferences.askPDEAutoImport());
		buttonAutoRemoveImport.setSelection(C4JPreferences.doPDEAutoRemoveImport());
		buttonConfirmAutoRemove.setSelection(C4JPreferences.askPDEAutoRemoveImport());
		buttonEnableApt.setSelection(C4JPreferences.doAptAutoEnable());
		buttonConfirmApt.setSelection(C4JPreferences.askAptAutoEnable());
		buttonDisableApt.setSelection(C4JPreferences.doAptAutoDisable());
		buttonConfirmDisableApt.setSelection(C4JPreferences.askAptAutoDisable());
		
		widgetSelected(null);
		
		return compDialogs;
	}

	@Override
	protected void performDefaults() {
		
		buttonAutoImport.setSelection(C4JPreferences.defaultDoPDEAutoImport());
		buttonConfirmAutoImport.setSelection(C4JPreferences.defaultAskPDEAutoImport());
		buttonAutoRemoveImport.setSelection(C4JPreferences.defaultDoPDEAutoRemoveImport());
		buttonConfirmAutoRemove.setSelection(C4JPreferences.defaultAskPDEAutoRemoveImport());
		buttonEnableApt.setSelection(C4JPreferences.defaultDoAptAutoEnable());
		buttonConfirmApt.setSelection(C4JPreferences.defaultAskAptAutoEnable());
		buttonDisableApt.setSelection(C4JPreferences.defaultDoAptAutoDisable());
		buttonConfirmDisableApt.setSelection(C4JPreferences.defaultAskAptAutoDisable());
		
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		
		C4JPreferences.setDoPDEAutoImport(buttonAutoImport.getSelection());
		C4JPreferences.setAskPDEAutoImport(buttonConfirmAutoImport.getSelection());
		C4JPreferences.setDoPDEAutoRemoveImport(buttonAutoRemoveImport.getSelection());
		C4JPreferences.setAskPDEAutoRemoveImport(buttonConfirmAutoRemove.getSelection());
		C4JPreferences.setDoAptAutoEnable(buttonEnableApt.getSelection());
		C4JPreferences.setAskAptAutoEnable(buttonConfirmApt.getSelection());
		C4JPreferences.setDoAptAutoDisable(buttonDisableApt.getSelection());
		C4JPreferences.setAskAptAutoDisable(buttonConfirmDisableApt.getSelection());
		
		return super.performOk();
	}

	public void widgetDefaultSelected(SelectionEvent e) {}

	public void widgetSelected(SelectionEvent e) {
		buttonConfirmAutoImport.setEnabled(buttonAutoImport.getSelection());
		buttonConfirmAutoRemove.setEnabled(buttonAutoRemoveImport.getSelection());
		buttonConfirmApt.setEnabled(buttonEnableApt.getSelection());
		buttonConfirmDisableApt.setEnabled(buttonDisableApt.getSelection());
	}

}

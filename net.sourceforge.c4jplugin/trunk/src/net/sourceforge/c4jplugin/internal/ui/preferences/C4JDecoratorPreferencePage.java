package net.sourceforge.c4jplugin.internal.ui.preferences;

import net.sourceforge.c4jplugin.internal.decorators.ContractDecorator;
import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class C4JDecoratorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener {

	private IDecoratorManager decoManager = PlatformUI.getWorkbench().getDecoratorManager();
	
	private Button buttonClasses;
	private Button buttonMethods;
	private Button buttonTL;
	private Button buttonBL;
	private Button buttonTR;
	private Button buttonBR;
	
	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 40;
		comp.setLayout(gridLayout);
		
		Label labelDeco = new Label(comp, SWT.NONE);
		
		buttonClasses = new Button(comp, SWT.CHECK);
		buttonMethods = new Button(comp, SWT.CHECK);
		
		Label labelPos = new Label(comp, SWT.NONE);
		
		Composite compPos = new Composite(comp, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		compPos.setLayout(gridLayout);
		buttonTL = new Button(compPos, SWT.RADIO);
		buttonTR = new Button(compPos, SWT.RADIO);
		buttonBL = new Button(compPos, SWT.RADIO);
		buttonBR = new Button(compPos, SWT.RADIO);
		
		Label labelPreview = new Label(comp, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.verticalIndent = 10;
		labelPreview.setLayoutData(gridData);
		Table tablePreview = new Table(comp, SWT.BORDER);
		tablePreview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		labelDeco.setText(UIMessages.PreferencesDecorations_decoHeader);
		buttonClasses.setText(UIMessages.PreferencesDecorations_decoClasses);
		buttonMethods.setText(UIMessages.PreferencesDecorations_decoMethods);
		labelPos.setText(UIMessages.PreferencesDecorations_posHeader);
		buttonTL.setText(UIMessages.PreferencesDecorations_posUL);
		buttonBL.setText(UIMessages.PreferencesDecorations_posLL);
		buttonTR.setText(UIMessages.PreferencesDecorations_posUR);
		buttonBR.setText(UIMessages.PreferencesDecorations_posLR);
		labelPreview.setText(UIMessages.PreferencesDecorations_preview);
		
		buttonClasses.addSelectionListener(this);
		buttonMethods.addSelectionListener(this);
		buttonTL.addSelectionListener(this);
		buttonTR.addSelectionListener(this);
		buttonBL.addSelectionListener(this);
		buttonBR.addSelectionListener(this);
		
		buttonClasses.setSelection(C4JPreferences.getDecorateClasses());
		buttonMethods.setSelection(C4JPreferences.getDecorateMethods());
		
		getButtonPosition(C4JPreferences.getDecorationPosition()).setSelection(true);
		
		return comp;
	}

	public void init(IWorkbench workbench) {

	}

	@Override
	protected void performDefaults() {
		buttonClasses.setSelection(C4JPreferences.getDefaultDecorateClasses());
		buttonMethods.setSelection(C4JPreferences.getDefaultDecorateMethods());
		
		getButtonPosition(C4JPreferences.getDefaultDecorationPosition()).setSelection(true);
		
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		C4JPreferences.setDecorateClasses(buttonClasses.getSelection());
		C4JPreferences.setDecorateMethods(buttonMethods.getSelection());
		
		int pos = C4JPreferences.getDecorationPosition();
		if (buttonTL.getSelection()) pos = IDecoration.TOP_LEFT;
		else if (buttonBL.getSelection()) pos = IDecoration.BOTTOM_LEFT;
		else if (buttonTR.getSelection()) pos = IDecoration.TOP_RIGHT;
		else if (buttonBR.getSelection()) pos = IDecoration.BOTTOM_RIGHT;
		C4JPreferences.setDecorationPosition(pos);
		
		if (!buttonClasses.getSelection() && !buttonMethods.getSelection()) {
			try {
				decoManager.setEnabled(ContractDecorator.ID, false);
			} catch (CoreException e) {}
		}
		else {
			((ContractDecorator)decoManager.getBaseLabelProvider(ContractDecorator.ID)).refreshAll();
		}
		return super.performOk();
	}

	public void setVisible(boolean visible) {
		widgetSelected(null);
		super.setVisible(visible);
	}
	
	private Button getButtonPosition(int pos) {
		switch (pos) {
		case IDecoration.BOTTOM_LEFT:
			return buttonBL;
		case IDecoration.TOP_LEFT:
			return buttonTL;
		case IDecoration.BOTTOM_RIGHT:
			return buttonBR;
		case IDecoration.TOP_RIGHT:
			return buttonTR;
		}
		
		return buttonTL;
	}
	
	private boolean isPreferencesChanged() {
		if (buttonClasses.getSelection() != C4JPreferences.getDecorateClasses())
			return true;
		if (buttonMethods.getSelection() != C4JPreferences.getDecorateMethods())
			return true;
		
		int pos = C4JPreferences.getDecorationPosition();
		if (!getButtonPosition(pos).getSelection()) return true;
		
		return false;
	}

	public void widgetDefaultSelected(SelectionEvent e) {}

	public void widgetSelected(SelectionEvent e) {
		if (decoManager.getEnabled(ContractDecorator.ID)) {
			if (buttonClasses.getSelection() || buttonMethods.getSelection())
				setMessage(null);
			else if (isPreferencesChanged()) {
				setMessage(UIMessages.PreferencesDecorations_infoMsg_willDisableDeco, INFORMATION);
			}
		}
		else if (isPreferencesChanged()) {
			setMessage(UIMessages.PreferencesDecorations_warningMsg_disabledDeco, WARNING);
		}
		else setMessage(null);
	}
	
}

package net.sourceforge.c4jplugin.internal.ui.preferences;

import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class C4JPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		
		Label labelTitle = new Label(comp, SWT.NONE);
		labelTitle.setText(UIMessages.PreferencesMain_title);
		
		Label labelHint = new Label(comp, SWT.WRAP);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.verticalIndent = 15;
		labelHint.setLayoutData(gridData);
		labelHint.setText(UIMessages.PreferencesMain_hint);
		
		return comp;
	}

	public void init(IWorkbench workbench) {

	}

}

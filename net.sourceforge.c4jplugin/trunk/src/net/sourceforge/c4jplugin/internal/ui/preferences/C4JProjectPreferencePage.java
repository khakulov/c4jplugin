package net.sourceforge.c4jplugin.internal.ui.preferences;

import java.util.Arrays;
import java.util.Comparator;

import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.PropertyPage;

public class C4JProjectPreferencePage extends PropertyPage implements SelectionListener {

	private IProject project = null;
	
	//private Vector<LaunchConfigItem> input = new Vector<LaunchConfigItem>();

	private Table table;

	private Button buttonSelectAll;

	private Button buttonSelectNone;

	private Button buttonConfirmAll;

	private Button buttonConfirmNone;
	
	@Override
	protected Control createContents(Composite parent) {
		project = (IProject)getElement();
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(UIMessages.ProjectPreferencesLaunch_title);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		
		table = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.CHECK | SWT.INHERIT_FORCE);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.verticalSpan = 4;
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		TableColumn columnType = new TableColumn(table, SWT.NONE);
		columnType.setText(UIMessages.ProjectPreferencesLaunch_tableHeader_type);
		
		TableColumn columnConfirm = new TableColumn(table, SWT.NONE);
		columnConfirm.setText(UIMessages.ProjectPreferencesLaunch_tableHeader_confirm);
		
		ILaunchConfigurationType[] types = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes();
		Arrays.sort(types, new Comparator<ILaunchConfigurationType>() {
			public int compare(ILaunchConfigurationType o1, ILaunchConfigurationType o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		for (ILaunchConfigurationType type : types) {
			if (type.isPublic() && 
				(type.supportsMode(ILaunchManager.RUN_MODE) || type.supportsMode(ILaunchManager.DEBUG_MODE))) {
				TableItem item = new TableItem(table, SWT.INHERIT_FORCE);
				item.setText(0, type.getName());
				item.setImage(DebugUITools.getDefaultImageDescriptor(type).createImage());
				item.setChecked(C4JPreferences.doChangeLaunchConfig(project, type.getIdentifier()));
				item.setData(type.getIdentifier());
				
				TableEditor editor = new TableEditor(table);				
				Button button = new Button(table, SWT.CHECK);
				item.setData("button", button);
				//button.setBackground(table.getBackground());
				button.setSelection(C4JPreferences.askChangeLaunchConfig(project, type.getIdentifier()));
				button.pack();
                Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                editor.minimumWidth = size.x;
                editor.minimumHeight = size.y;
                editor.horizontalAlignment = SWT.CENTER;
                editor.verticalAlignment = SWT.CENTER;
				editor.setEditor(button, item, 1);
			}
		}
		
		columnConfirm.pack();
		columnType.pack();
		
		buttonSelectAll = new Button(composite, SWT.NONE);
		buttonSelectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		buttonSelectAll.setText(UIMessages.ProjectPreferencesLaunch_buttonSelectAll);
		buttonSelectNone = new Button(composite, SWT.NONE);
		buttonSelectNone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		buttonSelectNone.setText(UIMessages.ProjectPreferencesLaunch_buttonSelectNone);
		
		buttonConfirmAll = new Button(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.verticalIndent = 10;
		buttonConfirmAll.setLayoutData(gridData);
		buttonConfirmAll.setText(UIMessages.ProjectPreferencesLaunch_buttonConfirmAll);
		buttonConfirmNone = new Button(composite, SWT.NONE);
		buttonConfirmNone.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
		buttonConfirmNone.setText(UIMessages.ProjectPreferencesLaunch_buttonConfirmNone);
		
		buttonSelectAll.addSelectionListener(this);
		buttonSelectNone.addSelectionListener(this);
		buttonConfirmAll.addSelectionListener(this);
		buttonConfirmNone.addSelectionListener(this);
		
		return composite;
	}

	@Override
	protected void performDefaults() {
		for (TableItem item : table.getItems()) {
			String identifier = (String)item.getData();
			item.setChecked(C4JPreferences.defaultDoChangeLaunchConfig(identifier));
			((Button)item.getData("button")).setSelection(C4JPreferences.defaultAskChangeLaunchConfig(identifier));
		}
		
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		for (TableItem item : table.getItems()) {
			String identifier = (String)item.getData();
			C4JPreferences.setAskChangeLaunchConfig(project, identifier, ((Button)item.getData("button")).getSelection());
			C4JPreferences.setDoChangeLaunchConfig(project, identifier, item.getChecked());
		}
		
		return super.performOk();
	}

	public void widgetDefaultSelected(SelectionEvent e) {}

	public void widgetSelected(SelectionEvent e) {
		Widget widget = e.widget;
		if (widget == buttonSelectAll) {
			for (TableItem item : table.getItems()) {
				item.setChecked(true);
			}
		}
		else if (widget == buttonSelectNone) {
			for (TableItem item : table.getItems()) {
				item.setChecked(false);
			}
		}
		else if (widget == buttonConfirmAll) {
			for (TableItem item : table.getItems()) {
				((Button)item.getData("button")).setSelection(true);
			}
		}
		else if (widget == buttonConfirmNone) {
			for (TableItem item : table.getItems()) {
				((Button)item.getData("button")).setSelection(false);
			}
		}
	}
	
}

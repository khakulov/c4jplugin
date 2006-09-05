package net.sourceforge.c4jplugin.internal.ui.preferences;

import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;
import net.sourceforge.c4jplugin.internal.ui.viewers.LaunchConfigurationTreeContentProvider;
import net.sourceforge.c4jplugin.internal.ui.viewers.LaunchConfigurationTreeElement;
import net.sourceforge.c4jplugin.internal.ui.viewers.LaunchConfigurationTreeLabelProvider;
import net.sourceforge.c4jplugin.internal.ui.viewers.LaunchConfigurationTypeTreeElement;
import net.sourceforge.c4jplugin.internal.ui.viewers.LaunchGroupFilter;
import net.sourceforge.c4jplugin.internal.util.C4JUtils;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class C4JLaunchPreferencePage extends PreferencePage implements
				IWorkbenchPreferencePage, SelectionListener, ISelectionChangedListener {

	private final String PROP_LABEL_COL = "label";
	private final String PROP_CHANGE_COL = "change";
	private final String PROP_CONFIRM_COL = "confirm";
	
	private TreeViewer treeViewer;
	private Tree tree;
	private LaunchConfigurationTreeContentProvider contentProvider;

	private Button buttonSelectAll;
	private Button buttonSelectNone;
	private Button buttonConfirmAll;
	private Button buttonConfirmNone;
	
	private Label labelVmArgs;
	
	@Override
	protected Control createContents(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(UIMessages.PreferencesLaunch_title);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		
		treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.SINGLE);
		tree = treeViewer.getTree();
		
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.verticalSpan = 4;
		tree.setLayoutData(gridData);
		tree.setHeaderVisible(true);
		
		TreeColumn columnLabel = new TreeColumn(tree, SWT.NONE);
		columnLabel.setText(UIMessages.PreferencesLaunch_tableHeader_label);
		
		TreeColumn columnType = new TreeColumn(tree, SWT.NONE);
		columnType.setText(UIMessages.PreferencesLaunch_tableHeader_enable);
		
		TreeColumn columnConfirm = new TreeColumn(tree, SWT.NONE);
		columnConfirm.setText(UIMessages.PreferencesLaunch_tableHeader_confirm);
				
		columnConfirm.pack();
		columnType.pack();
		columnLabel.pack();
		columnLabel.setWidth(300);
		
		contentProvider = new LaunchConfigurationTreeContentProvider(null, getShell());
		
		treeViewer.setColumnProperties(new String[] {PROP_LABEL_COL, PROP_CHANGE_COL, PROP_CONFIRM_COL});
		treeViewer.setLabelProvider(new LaunchConfigurationTreeLabelProvider());
		treeViewer.setSorter(new ViewerSorter());
		treeViewer.setContentProvider(contentProvider);
		
		ILaunchGroup launchGroup = null;
		for (ILaunchGroup group : DebugUITools.getLaunchGroups()) {
			if (group.getIdentifier().equals(IDebugUIConstants.ID_RUN_LAUNCH_GROUP)) {
				launchGroup = group;
				break;
			}
		}
		
		if (launchGroup != null)
			treeViewer.addFilter(new LaunchGroupFilter(launchGroup));
		
		CheckboxCellEditor changeEditor = new CheckboxCellEditor(tree);
		CheckboxCellEditor confirmEditor = new CheckboxCellEditor(tree);
		
		treeViewer.setCellEditors(new CellEditor[] {null, changeEditor, confirmEditor});
		treeViewer.setCellModifier(new LaunchCellModifier());
		treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		treeViewer.addSelectionChangedListener(this);
		
		buttonSelectAll = new Button(composite, SWT.NONE);
		buttonSelectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		buttonSelectAll.setText(UIMessages.PreferencesLaunch_buttonSelectAll);
		buttonSelectNone = new Button(composite, SWT.NONE);
		buttonSelectNone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		buttonSelectNone.setText(UIMessages.PreferencesLaunch_buttonSelectNone);
		
		buttonConfirmAll = new Button(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.verticalIndent = 10;
		buttonConfirmAll.setLayoutData(gridData);
		buttonConfirmAll.setText(UIMessages.PreferencesLaunch_buttonConfirmAll);
		buttonConfirmNone = new Button(composite, SWT.NONE);
		buttonConfirmNone.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
		buttonConfirmNone.setText(UIMessages.PreferencesLaunch_buttonConfirmNone);
		
		Composite compVMArgs = new Composite(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		compVMArgs.setLayout(gridLayout);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		compVMArgs.setLayoutData(gridData);
		
		Label label2 = new Label(compVMArgs, SWT.NONE);
		label2.setText(UIMessages.PreferencesLaunch_labelVMArgs);
		labelVmArgs = new Label(compVMArgs, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		labelVmArgs.setLayoutData(gridData);
		
		buttonSelectAll.addSelectionListener(this);
		buttonSelectNone.addSelectionListener(this);
		buttonConfirmAll.addSelectionListener(this);
		buttonConfirmNone.addSelectionListener(this);
		
		return composite;
	}

	@Override
	protected void performDefaults() {
		for (Object object : contentProvider.getElements(null)) {
			if (object instanceof LaunchConfigurationTypeTreeElement) {
				LaunchConfigurationTypeTreeElement item = (LaunchConfigurationTypeTreeElement)object;
				item.setChangeVMArguments(C4JPreferences.defaultDoChangeLaunchConfig(item.getLaunchConfigurationType().getIdentifier()));
				item.setAskChangeVMArguments(C4JPreferences.defaultAskChangeLaunchConfig(item.getLaunchConfigurationType().getIdentifier()));
				for (Object child : contentProvider.getChildren(item)) {
					((LaunchConfigurationTreeElement)child).setDefaults();
				}
			}
		}
		
		treeViewer.refresh();
		
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		for (Object object : contentProvider.getElements(null)) {
			if (object instanceof LaunchConfigurationTypeTreeElement) {
				LaunchConfigurationTypeTreeElement item = (LaunchConfigurationTypeTreeElement)object;
				C4JPreferences.setDoChangeLaunchConfig(item.getLaunchConfigurationType().getIdentifier(), item.isChangeVMArguments());
				C4JPreferences.setAskChangeLaunchConfig(item.getLaunchConfigurationType().getIdentifier(), item.isAskChangeVMArguments());
				for (Object child : contentProvider.getChildren(item)) {
					LaunchConfigurationTreeElement childItem = (LaunchConfigurationTreeElement)child;
					if (childItem.isDirty())
						C4JUtils.setVMArgs(childItem.getLaunchConfiguration(), childItem.getVMArguments());
				}
			}
		}
		
		return super.performOk();
	}

	public void widgetDefaultSelected(SelectionEvent e) {}

	public void widgetSelected(SelectionEvent e) {
		Widget widget = e.widget;
		if (widget == buttonSelectAll) {
			for (Object object : contentProvider.getElements(null)) {
				if (object instanceof LaunchConfigurationTypeTreeElement) {
					LaunchConfigurationTypeTreeElement item = (LaunchConfigurationTypeTreeElement)object;
					item.setChangeVMArguments(true);
					for (Object child : contentProvider.getChildren(item)) {
						((LaunchConfigurationTreeElement)child).setC4JEnabled(true);
					}
				}
			}
		}
		else if (widget == buttonSelectNone) {
			for (Object object : contentProvider.getElements(null)) {
				if (object instanceof LaunchConfigurationTypeTreeElement) {
					LaunchConfigurationTypeTreeElement item = (LaunchConfigurationTypeTreeElement)object;
					item.setChangeVMArguments(false);
					for (Object child : contentProvider.getChildren(item)) {
						((LaunchConfigurationTreeElement)child).setC4JEnabled(false);
					}
				}
			}
		}
		else if (widget == buttonConfirmAll) {
			for (Object object : contentProvider.getElements(null)) {
				if (object instanceof LaunchConfigurationTypeTreeElement) {
					LaunchConfigurationTypeTreeElement item = (LaunchConfigurationTypeTreeElement)object;
					item.setAskChangeVMArguments(true);
				}
			}
		}
		else if (widget == buttonConfirmNone) {
			for (Object object : contentProvider.getElements(null)) {
				if (object instanceof LaunchConfigurationTypeTreeElement) {
					LaunchConfigurationTypeTreeElement item = (LaunchConfigurationTypeTreeElement)object;
					item.setAskChangeVMArguments(false);
				}
			}
		}
		
		treeViewer.refresh();
	}

	public void init(IWorkbench workbench) {
		
	}

	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			labelVmArgs.setText("");
		}
		else {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof LaunchConfigurationTypeTreeElement) {
				labelVmArgs.setText("");
			}
			else {
				labelVmArgs.setText(((LaunchConfigurationTreeElement)element).getVMArguments());
			}
		}
	}
	
	private class LaunchCellModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
			if (property.equals(PROP_LABEL_COL)) return false;
			if (property.equals(PROP_CHANGE_COL)) return true;
			if (property.equals(PROP_CONFIRM_COL) &&
					element instanceof LaunchConfigurationTypeTreeElement) return true;
			
			return false;
		}

		public Object getValue(Object element, String property) {
			if (property.equals(PROP_CHANGE_COL)) {
				if (element instanceof LaunchConfigurationTypeTreeElement) {
					return ((LaunchConfigurationTypeTreeElement)element).isChangeVMArguments();
				}
				else if (element instanceof LaunchConfigurationTreeElement) {
					return ((LaunchConfigurationTreeElement)element).isC4JEnabled();
				}
			}
			else if (property.equals(PROP_CONFIRM_COL) && 
					element instanceof LaunchConfigurationTypeTreeElement) {
				return ((LaunchConfigurationTypeTreeElement)element).isAskChangeVMArguments();
			}
			return null;
		}

		public void modify(Object element, String property, Object value) {
			
			if (element instanceof Item) {
				element = ((Item)element).getData();
			}
			
			if (property.equals(PROP_CHANGE_COL)) {
				if (element instanceof LaunchConfigurationTypeTreeElement) {
					((LaunchConfigurationTypeTreeElement)element).setChangeVMArguments((Boolean)value);
				}
				else if (element instanceof LaunchConfigurationTreeElement) {
					LaunchConfigurationTreeElement item = (LaunchConfigurationTreeElement)element;
					item.setC4JEnabled((Boolean)value);
					labelVmArgs.setText(item.getVMArguments());
				}
			}
			else if (property.equals(PROP_CONFIRM_COL) && 
					element instanceof LaunchConfigurationTypeTreeElement) {
				((LaunchConfigurationTypeTreeElement)element).setAskChangeVMArguments((Boolean)value);
			}
			
			treeViewer.update(element, null);
		}
		
	}
	
}

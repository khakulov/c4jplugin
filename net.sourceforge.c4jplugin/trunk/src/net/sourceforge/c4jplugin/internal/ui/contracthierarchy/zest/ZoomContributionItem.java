package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.zest;

import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.mylar.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;

public class ZoomContributionItem extends ControlContribution implements ZoomListener {
	/**
	 * Zooms to fit the width.
	 */
	public static final String FIT_WIDTH = ZoomManager.FIT_WIDTH;
	/**
	 * Zooms to fit the height.
	 */
	public static final String FIT_HEIGHT = ZoomManager.FIT_HEIGHT;
	/**
	 * Zooms to fit entirely within the viewport.
	 */
	public static final String FIT_ALL = ZoomManager.FIT_ALL;
	
	
	private String[] zoomLevels;
	private ZoomManager zoomManager;
	private Combo combo;
	private Menu fMenu;
	
	
	public ZoomContributionItem(ZoomManager zm) {
		this(zm, new String[] {"50%", "100%", "150%", "200%", FIT_WIDTH, FIT_HEIGHT, FIT_ALL});
	}
	
	/**
	 * Creates a new contribution item that will work on the given part
	 * service.initialZooms will be used to populate the combo or the menu.
	 * Valid values for initialZooms are percentage numbers (e.g., "100%"),
	 * or FIT_WIDTH, FIT_HEIGHT, FIT_ALL.
	 * 
	 * @param partService service used to see whether the view is zoomable.
	 * @param initialZooms the initial zoom values.
	 */
	public ZoomContributionItem(ZoomManager zm, String[] initialZooms) {
		super("net.sourceforge.c4jplugin.zoomitem");
		this.zoomManager = zm;
		this.zoomLevels = initialZooms;
		
	}
	
	private Combo createCombo(Composite parent) {
		this.combo = new Combo(parent, SWT.DROP_DOWN | SWT.FLAT);
		this.combo.setItems(zoomLevels);
		this.combo.setSize(computeWidth(this.combo), 20);
		this.combo.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				int selection = combo.getSelectionIndex();
				if (selection >0) {
					doZoom(combo.getItem(selection));
				} else {
					doZoom(combo.getItem(0));
				}
			}
		});
		return this.combo;
	}
	
	private void doZoom(String zoom) {
		if (zoomManager != null) {
			zoomManager.setZoomAsText(zoom);
		}
	}
	
	private void refresh(boolean rebuild) {
		if (combo != null && !combo.isDisposed()) {
			refreshCombo(rebuild);
		} else if (fMenu != null && fMenu.isDisposed()) {
			refreshMenu(rebuild);
		}
	}
	
	/**
	 * @param rebuild
	 */
	private void refreshMenu(boolean rebuild) {
		fMenu.setEnabled(false);
		if (zoomManager == null) {
			return;
		}
		if (rebuild) {
			zoomLevels = zoomManager.getZoomLevelsAsText();
			MenuItem[] oldItems = fMenu.getItems();
			for (int i = 0; i < oldItems.length; i++) {
				oldItems[i].dispose();
			}
			for (int i = 0; i < zoomLevels.length;i++) {
				MenuItem item = new MenuItem(fMenu, SWT.RADIO);
				item.setText(zoomLevels[i]);
				item.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e) {
						MenuItem source = (MenuItem)e.getSource();
						doZoom(source.getText());
					}
				});
			}
		}
		MenuItem[] items = fMenu.getItems();
		String zoom = zoomManager.getZoomAsText();
		for (int i = 0; i < items.length; i++) {
			items[i].setSelection(false);
			if (zoom.equalsIgnoreCase(items[i].getText())) {
				items[i].setSelection(true);
			}
		}
		fMenu.setEnabled(true);
	}

	/**
	 * @param rebuild
	 */
	private void refreshCombo(boolean rebuild) {
		combo.setEnabled(false);
		if (zoomManager == null) {
			return;
		}
		if (rebuild) {
			combo.setItems(zoomManager.getZoomLevelsAsText());
		}	
		String zoom = zoomManager.getZoomAsText();
		int index = combo.indexOf(zoom);
		if (index > 0) {
			combo.select(index);
		}
		combo.setEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.ZoomListener#zoomChanged(double)
	 */
	public void zoomChanged(double z) {
		refresh(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	
	public void dispose() {
		if (combo != null) {
			combo = null;
		}
		if (fMenu != null) {
			fMenu = null;
		}

		super.dispose();
	}

	@Override
	protected Control createControl(Composite parent) {
		return createCombo(parent);
	}
	
	public void setVisible(boolean visible) {
		if (combo != null) {
			combo.setVisible(visible);
			super.setVisible(visible);
		}
	}
}

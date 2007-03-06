package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.zest;

import java.util.ArrayList;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyLifeCycle;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.HierarchyLabelProvider;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.HierarchyViewerSorter;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.IContractHierarchyViewer;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.tree.TreeContractHierarchyContentProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.util.JavaUIHelp;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.ResourceToItemsMapper;
import org.eclipse.jdt.ui.IWorkingCopyProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.ProblemsLabelDecorator.ProblemsLabelChangedEvent;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylar.zest.core.ZestStyles;
import org.eclipse.mylar.zest.core.viewers.StaticGraphViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

public class ZestContractHierarchyViewer extends StaticGraphViewer implements
		IContractHierarchyViewer, ResourceToItemsMapper.IContentViewerAccessor {

	private OpenAction fOpen;
	private HierarchyLabelProvider fLabelProvider;
	
	protected ResourceToItemsMapper fResourceToItemsMapper;
	
	public ZestContractHierarchyViewer(Composite composite, ContractHierarchyLifeCycle lifeCycle, IWorkbenchPart part) {
		super(composite, ZestStyles.PANNING | 
				ZestStyles.NO_OVERLAPPING_NODES | ZestStyles.ENFORCE_BOUNDS);
		
		setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
		
		fLabelProvider= new HierarchyLabelProvider(lifeCycle);
		
		setLabelProvider(new DecoratingJavaLabelProvider(fLabelProvider, true));
		setUseHashlookup(true);
		setContentProvider(new ZestContractHierarchyContentProvider(lifeCycle));
		setComparator(new HierarchyViewerSorter(lifeCycle));
		
		fOpen= new OpenAction(part.getSite());
		addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				fOpen.run();
			}
		});
		
		addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				fOpen.run();
			}
		});
		
		//JavaUIHelp.setHelp(this, IJavaHelpContextIds.TYPE_HIERARCHY_VIEW);
		
		initMapper();
	}
	
	public Object containsElements() {
		ZestContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {
			Object[] elements= contentProvider.getElements(null);
			if (elements.length > 0) {
				return elements[0];
			}
		}
		return null;
	}

	public void contributeToContextMenu(IMenuManager menu) {
		
	}

	public IType getRootType() {
		ZestContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {		
			Object[] elements=  contentProvider.getElements(null);
			if (elements.length > 0 && elements[0] instanceof IType) {
				return (IType) elements[0];
			}
		}
		return null;
	}

	public String getTitle() {
		return "Zest Contract Hierarchy";
	}

	public void initContextMenu(IMenuListener menuListener, String popupId,
			IWorkbenchPartSite viewSite) {
		MenuManager menuMgr= new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(menuListener);
		Menu menu= menuMgr.createContextMenu(getControl());
		getControl().setMenu(menu);
		viewSite.registerContextMenu(popupId, menuMgr, this);
	}

	public boolean isElementShown(Object element) {
		return findItem(element) != null;
	}

	public boolean isMethodFiltering() {
		ZestContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {
			return contentProvider.getMemberFilter() != null;
		}
		return false;
	}

	public void setMemberFilter(IMember[] memberFilter) {
		ZestContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {
			contentProvider.setMemberFilter(memberFilter);
		}
	}

	public void setQualifiedTypeName(boolean on) {
		if (on) {
			fLabelProvider.setTextFlags(fLabelProvider.getTextFlags() | JavaElementLabels.T_POST_QUALIFIED);
		} else {
			fLabelProvider.setTextFlags(fLabelProvider.getTextFlags() & ~JavaElementLabels.T_POST_QUALIFIED);
		}
		refresh();
	}

	public void setWorkingSetFilter(ViewerFilter filter) {
		fLabelProvider.setFilter(filter);
		ZestContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {
			contentProvider.setWorkingSetFilter(filter);
		}	
	}

	public void updateContent(boolean doExpand) {
		//getControl().setRedraw(true);
		refresh();
		applyLayout();
	}
	
	protected ZestContractHierarchyContentProvider getHierarchyContentProvider() {
		return (ZestContractHierarchyContentProvider)getContentProvider();
	}
	
	public void doUpdateItem(Widget item) {
		doUpdateItem(item, item.getData(), true);
	}
	
	protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {
		if (event instanceof ProblemsLabelChangedEvent) {
			ProblemsLabelChangedEvent e= (ProblemsLabelChangedEvent) event;
			if (!e.isMarkerChange() && canIgnoreChangesFromAnnotionModel()) {
				return;
			}
		}
		Object[] changed= event.getElements();
		
		if (changed != null && !fResourceToItemsMapper.isEmpty()) {
			ArrayList others= new ArrayList();
			for (int i= 0; i < changed.length; i++) {
				Object curr= changed[i];
				if (curr instanceof IResource) {
					fResourceToItemsMapper.resourceChanged((IResource) curr);
				} else {
					others.add(curr);
				}
			}
			if (others.isEmpty()) {
				return;
			}
			event= new LabelProviderChangedEvent((IBaseLabelProvider) event.getSource(), others.toArray());
		} else {
			// we have modified the list of changed elements via add additional parents.
			if (event.getElements() != changed)
				event= new LabelProviderChangedEvent((IBaseLabelProvider) event.getSource(), changed);
		}
		super.handleLabelProviderChanged(event);
	}
	
	/**
	 * Answers whether this viewer can ignore label provider changes resulting from
	 * marker changes in annotation models
	 */
	private boolean canIgnoreChangesFromAnnotionModel() {
		Object contentProvider= getContentProvider();
		return contentProvider instanceof IWorkingCopyProvider && !((IWorkingCopyProvider)contentProvider).providesWorkingCopies();
	}
	
	private void initMapper() {
		fResourceToItemsMapper= new ResourceToItemsMapper(this);
	}
	
	/*
	 * @see StructuredViewer#mapElement(Object, Widget)
	 */
	protected void mapElement(Object element, Widget item) {
		super.mapElement(element, item);
		if (item instanceof Item) {
			fResourceToItemsMapper.addToMap(element, (Item) item);
		}
	}

	/*
	 * @see StructuredViewer#unmapElement(Object, Widget)
	 */
	protected void unmapElement(Object element, Widget item) {
		if (item instanceof Item) {
			fResourceToItemsMapper.removeFromMap(element, (Item) item);
		}		
		super.unmapElement(element, item);
	}

	/*
	 * @see StructuredViewer#unmapAllElements()
	 */
	protected void unmapAllElements() {
		fResourceToItemsMapper.clearMap();
		super.unmapAllElements();
	}

}

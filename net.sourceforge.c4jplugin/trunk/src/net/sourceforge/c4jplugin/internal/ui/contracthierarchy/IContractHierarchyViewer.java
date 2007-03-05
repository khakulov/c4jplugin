package net.sourceforge.c4jplugin.internal.ui.contracthierarchy;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;

public interface IContractHierarchyViewer {
	
	public void setQualifiedTypeName(boolean on);
	public void setInput(Object input);
	public Control getControl();
	public void initContextMenu(IMenuListener menuListener, String popupId, IWorkbenchPartSite viewSite);
	public void contributeToContextMenu(IMenuManager menu);
	public void setMemberFilter(IMember[] memberFilter);
	public void setWorkingSetFilter(ViewerFilter filter);
	
	/**
	 * Returns true if the hierarchy contains elements. Returns one of them
	 * With member filtering it is possible that no elements are visible
	 */ 
	public Object containsElements();
	
	/**
	 * Updates the content of this viewer: refresh and expanding the tree in the way wanted.
	 */
	public abstract void updateContent(boolean doExpand);
	
	/**
	 * Returns the title for the current view
	 */
	public abstract String getTitle();
	
	/**
	 * Returns true if the hierarchy contains element the element.
	 */ 
	public boolean isElementShown(Object element);
	
	/**
	 * Returns if method filtering is enabled.
	 */	
	public boolean isMethodFiltering();
	
	/**
	 * Returns true if the hierarchy contains elements. Returns one of them
	 * With member filtering it is possible that no elements are visible
	 */ 
	public IType getRootType();
}

package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.zest;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.IContractHierarchyViewer;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylar.zest.core.viewers.StaticGraphViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

public class ZestContractHierarchyViewer extends StaticGraphViewer implements
		IContractHierarchyViewer {

	public ZestContractHierarchyViewer(Composite composite, int style) {
		super(composite, style);
	}
	
	public Object containsElements() {
		// TODO Auto-generated method stub
		return null;
	}

	public void contributeToContextMenu(IMenuManager menu) {
		// TODO Auto-generated method stub

	}

	public IType getRootType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	public void initContextMenu(IMenuListener menuListener, String popupId,
			IWorkbenchPartSite viewSite) {
		// TODO Auto-generated method stub

	}

	public boolean isElementShown(Object element) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isMethodFiltering() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setMemberFilter(IMember[] memberFilter) {
		// TODO Auto-generated method stub

	}

	public void setQualifiedTypeName(boolean on) {
		// TODO Auto-generated method stub

	}

	public void setWorkingSetFilter(ViewerFilter filter) {
		// TODO Auto-generated method stub

	}

	public void updateContent(boolean doExpand) {
		// TODO Auto-generated method stub

	}

}

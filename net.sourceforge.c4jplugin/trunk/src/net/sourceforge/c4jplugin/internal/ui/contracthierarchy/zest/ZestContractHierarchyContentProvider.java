package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.zest;

import java.util.List;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyContentProvider;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyLifeCycle;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IWorkingCopyProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylar.zest.core.viewers.IGraphContentProvider;

public class ZestContractHierarchyContentProvider 
		extends ContractHierarchyContentProvider implements IGraphContentProvider {
	
	
	public ZestContractHierarchyContentProvider(ContractHierarchyLifeCycle lifecycle) {
		super(lifecycle);
	}

	public Object getDestination(Object rel) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] getElements(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getSource(Object rel) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getWeight(Object connection) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void getContractsInHierarchy(IType type, List<IType> res) {
		// TODO Auto-generated method stub
		
	}

}

package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.zest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyContentProvider;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyLifeCycle;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.IContractHierarchy;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IWorkingCopyProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylar.zest.core.viewers.IGraphContentProvider;
import org.eclipse.mylar.zest.core.viewers.IGraphEntityContentProvider;

public class ZestContractHierarchyContentProvider 
		extends ContractHierarchyContentProvider implements IGraphEntityContentProvider {
	
	public ZestContractHierarchyContentProvider(ContractHierarchyLifeCycle lifecycle) {
		super(lifecycle);
	}

	@Override
	protected void getContractsInHierarchy(IType type, List<IType> res) {
		IContractHierarchy hierarchy= getHierarchy();
		if (hierarchy != null) {
			IType[] types= hierarchy.getSubcontracts(type);
			for (IType curr : types) {
				res.add(curr);
			}
		}
	}

	public Object[] getConnectedTo(Object entity) {
		IContractHierarchy hierarchy = getHierarchy();
		if (hierarchy == null) return NO_ELEMENTS;
		
		IType[] subs = hierarchy.getSubcontracts((IType)entity);
		IType[] supers = new IType[0]; //hierarchy.getSupercontracts((IType)entity);
		IType[] all = new IType[subs.length + supers.length];
		System.arraycopy(subs, 0, all, 0, subs.length);
		System.arraycopy(supers, 0, all, subs.length, supers.length);
		//System.out.println(((IType)entity).getElementName() + " is connected to " + all.length + " types");
		return all;
	}

	public Object[] getElements(Object inputElement) {
		IContractHierarchy hierarchy = getHierarchy();
		if (hierarchy != null) {
			IType[] all = hierarchy.getAllContracts();
			/*System.out.print("HIERARCHY NOT NULL: " + all.length + " types: ");
			for (IType type : all) {
				System.out.print(type.getElementName() + " ");
			}
			System.out.println("")*/;
			return all;
		}
		else {
			//System.out.println("HIERARCHY NULL");
			return NO_ELEMENTS;
		}
	}
	
	public double getWeight(Object entity1, Object entity2) {
		return -1;
	}

}

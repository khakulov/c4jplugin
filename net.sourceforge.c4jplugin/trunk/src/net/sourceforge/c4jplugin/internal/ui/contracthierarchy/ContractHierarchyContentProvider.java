package net.sourceforge.c4jplugin.internal.ui.contracthierarchy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IWorkingCopyProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Base class for content providers for contract hierarchy viewers.
 * Implementors must override 'getContractsInHierarchy'.
 * Java delta processing is also performed by the content provider
 */
public abstract class ContractHierarchyContentProvider implements IStructuredContentProvider, IWorkingCopyProvider {
	protected static final Object[] NO_ELEMENTS= new Object[0];
	
	protected ContractHierarchyLifeCycle fTypeHierarchy;
	protected IMember[] fMemberFilter;
	
	protected TreeViewer fViewer;

	private ViewerFilter fWorkingSetFilter;
	private ConditionOverrideTester fMethodOverrideTester;
	private IContractHierarchyLifeCycleListener fTypeHierarchyLifeCycleListener;
	
	
	public ContractHierarchyContentProvider(ContractHierarchyLifeCycle lifecycle) {
		fTypeHierarchy= lifecycle;
		fMemberFilter= null;
		fWorkingSetFilter= null;
		fMethodOverrideTester= null;
		fTypeHierarchyLifeCycleListener= new IContractHierarchyLifeCycleListener() {
			public void contractHierarchyChanged(ContractHierarchyLifeCycle typeHierarchyProvider, IType[] changedTypes) {
				if (changedTypes == null) {
					fMethodOverrideTester= null;
				}
			}
		};
		lifecycle.addChangedListener(fTypeHierarchyLifeCycleListener);
	}
	
	/**
	 * Sets members to filter the hierarchy for. Set to <code>null</code> to disable member filtering.
	 * When member filtering is enabled, the hierarchy contains only types that contain
	 * an implementation of one of the filter members and the members themself.
	 * The hierarchy can be empty as well.
	 */
	public final void setMemberFilter(IMember[] memberFilter) {
		fMemberFilter= memberFilter;
	}	

	private boolean initializeConditionOverrideTester(IMethod filterCondition, IType typeToFindIn) {
		IType filterType= filterCondition.getDeclaringType();
		IContractHierarchy hierarchy= fTypeHierarchy.getContractHierarchy();
		
		boolean filterOverrides= hierarchy.isSupercontract(typeToFindIn, filterType);
		IType focusType= filterOverrides ? filterType : typeToFindIn;
		
		if (fMethodOverrideTester == null || !fMethodOverrideTester.getFocusType().equals(focusType)) {
			fMethodOverrideTester= new ConditionOverrideTester(focusType, hierarchy);
		}
		return filterOverrides;
	}
	
	protected boolean hasCompatibleCondition(IMethod filterMethod, IType typeToFindIn) throws JavaModelException {
		boolean filterMethodOverrides= initializeConditionOverrideTester(filterMethod, typeToFindIn);
		IMethod[] methods= typeToFindIn.getMethods();
		for (int i= 0; i < methods.length; i++) {
			if (isCompatibleCondition(filterMethod, methods[i], filterMethodOverrides)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isCompatibleCondition(IMethod filterMethod, IMethod method, boolean filterOverrides) throws JavaModelException {
//		if (filterOverrides) {
//			return fMethodOverrideTester.isSubsignature(filterMethod, method);
//		} else {
//			return fMethodOverrideTester.isSubsignature(method, filterMethod);
//		}
		return filterMethod.getSignature().equals(method.getSignature());
	}

	/**
	 * The members to filter or <code>null</code> if member filtering is disabled.
	 */
	public IMember[] getMemberFilter() {
		return fMemberFilter;
	}
	
	/**
	 * Sets a filter representing a working set or <code>null</code> if working sets are disabled.
	 */
	public void setWorkingSetFilter(ViewerFilter filter) {
		fWorkingSetFilter= filter;
	}
		
	
	protected final IContractHierarchy getHierarchy() {
		return fTypeHierarchy.getContractHierarchy();
	}
	
	
	/* (non-Javadoc)
	 * @see IReconciled#providesWorkingCopies()
	 */
	public boolean providesWorkingCopies() {
		return true;
	}		
	
	
	/*
	 * Called for the root element
	 * @see IStructuredContentProvider#getElements	 
	 */
	public Object[] getElements(Object parent) {
		ArrayList<IType> types= new ArrayList<IType>();
		getRootTypes(types);
		for (int i= types.size() - 1; i >= 0; i--) {
			IType curr= (IType) types.get(i);
			try {
				if (!isInControl(curr)) {
					types.remove(i);
				}
			} catch (JavaModelException e) {
				// ignore
			}
		}
		return types.toArray();
	}
	
	protected void getRootTypes(List<IType> res) {
		IContractHierarchy hierarchy= getHierarchy();
		if (hierarchy != null) {
			IType input= hierarchy.getType();
			if (input != null) {
				res.add(input);
			}
			// opened on a region: dont show
		}
	}
	
	protected void addCompatibleMethods(IMethod filterMethod, IType typeToFindIn, List children) throws JavaModelException {
		boolean filterMethodOverrides= initializeConditionOverrideTester(filterMethod, typeToFindIn);
		IMethod[] methods= typeToFindIn.getMethods();
		for (int i= 0; i < methods.length; i++) {
			IMethod curr= methods[i];
			if (isCompatibleCondition(filterMethod, curr, filterMethodOverrides) && !children.contains(curr)) {
				children.add(curr);
			}
		}
	}
	
	/**
	 * Hook to overwrite. Filter will be applied on the returned types
	 */	
	protected abstract void getContractsInHierarchy(IType type, List<IType> res);
	
	
	
	
	private boolean isInScope(IType type) {
		if (fWorkingSetFilter != null && !fWorkingSetFilter.select(null, null, type)) {
			return false;
		}
		
		IJavaElement input= fTypeHierarchy.getInputElement();
		int inputType= input.getElementType();
		if (inputType ==  IJavaElement.TYPE) {
			return true;
		}
		
		IJavaElement parent= type.getAncestor(input.getElementType());
		if (inputType == IJavaElement.PACKAGE_FRAGMENT) {
			if (parent == null || parent.getElementName().equals(input.getElementName())) {
				return true;
			}
		} else if (input.equals(parent)) {
			return true;
		}
		return false;
	}
	
	protected final boolean isInControl(IType type) throws JavaModelException {
		if (isInScope(type)) {
			if (fMemberFilter != null) {
				return hasMemberFilterChildren(type) || hasTypeChildren(type);
			} else {
				return true;
			}
		}
		return hasTypeChildren(type);
	}
	
	private boolean hasMemberFilterChildren(IType type) throws JavaModelException {
		for (int i= 0; i < fMemberFilter.length; i++) {
			IMember member= fMemberFilter[i];
			if (type.equals(member.getDeclaringType())) {
				return true;
			} else if (member instanceof IMethod) {
				if (hasCompatibleCondition((IMethod) member, type)) {
					return true;
				}
			}
		}
		return false;
	}
		
	private boolean hasTypeChildren(IType type) throws JavaModelException {
		ArrayList<IType> types= new ArrayList<IType>();
		getContractsInHierarchy(type, types);
		for (IType curr : types) {
			if (isInControl(curr)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * @see IContentProvider#inputChanged
	 */
	public void inputChanged(Viewer part, Object oldInput, Object newInput) {
		Assert.isTrue(part instanceof TreeViewer);
		fViewer= (TreeViewer)part;
	}
	
	/*
	 * @see IContentProvider#dispose
	 */	
	public void dispose() {
		fTypeHierarchy.removeChangedListener(fTypeHierarchyLifeCycleListener);
		
	}
	
	protected final boolean isAnonymous(IType type) {
		return type.getElementName().length() == 0;
	}
	
	/*
	protected final boolean isAnonymousFromInterface(IType type) {
		return isAnonymous(type) && fTypeHierarchy.getHierarchy().getSuperInterfaces(type).length != 0;
	}
	*/
	
	protected final boolean isObject(IType type) {
		return "Object".equals(type.getElementName()) && type.getDeclaringType() == null && "java.lang".equals(type.getPackageFragment().getElementName());  //$NON-NLS-1$//$NON-NLS-2$
	}
	
}
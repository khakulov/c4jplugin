package net.sourceforge.c4jplugin.internal.ui.contracthierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.c4jplugin.internal.core.ContractReferenceModel;
import net.sourceforge.c4jplugin.internal.util.ContractReferenceUtil;
import net.sourceforge.c4jplugin.internal.util.ExceptionHandler;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeHierarchyChangedListener;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;


public class ContractHierarchy implements IContractHierarchy,
						IElementChangedListener, ITypeHierarchyChangedListener {

	public static boolean DEBUG = true;
	
	
	protected ITypeHierarchy typeHierarchy;
	
	protected Map<IType, Vector<IType>> contractToSupercontracts;
	protected Map<IType, Vector<IType>> contractToSubcontracts;
	protected Map<IType, Integer> typeFlags;
	protected Vector<IType> rootContracts;
	
	protected static final IType[] NO_TYPE = new IType[0];
	
	/*
	 * Whether this hierarchy needs refresh
	 */
	public boolean needsRefresh = true;
	
	protected boolean computeSubcontracts = true;
	
	protected IType inputContract = null;
	
	/**
	 * The progress monitor to report work completed too.
	 */
	//protected IProgressMonitor progressMonitor = null;
	
	
	/**
	 * Change listeners - null if no one is listening.
	 */
	protected ArrayList<IContractHierarchyChangedListener> changeListeners = null;
	
	public ContractHierarchy(IType type, IProgressMonitor pm) throws JavaModelException {
		this(type, pm, true);
	}
	
	public ContractHierarchy(IType type, IProgressMonitor pm, boolean computeSubcontracts) throws JavaModelException {
		IResource res = ContractReferenceModel.getDirectContract(type.getUnderlyingResource());
		if (res != null) {
			this.inputContract = ContractReferenceUtil.getType(JavaCore.create(res));
		}
		
		this.computeSubcontracts = computeSubcontracts;
		
		try {
			
			IProgressMonitor subpm1 = null;
			
			if (pm != null) {
				pm.beginTask("Creating Contract Hierarchy", 300);
				new SubProgressMonitor(pm, 200);
			}
			
			if (computeSubcontracts)
				typeHierarchy = type.newTypeHierarchy(subpm1);
			else
				typeHierarchy = type.newSupertypeHierarchy(subpm1);
			
			if (DEBUG) {
				System.out.print("TYPE HIERARCHY FOR " + type.getElementName() + " contains: ");
				for (IType t : typeHierarchy.getAllTypes()) {
					System.out.print(t.getElementName() + " ");
				}
				System.out.println("");
				for (IType t : typeHierarchy.getAllTypes()) {
					System.out.print("SUPER TYPES FOR " + t.getElementName() + " are: ");
					for (IType s : typeHierarchy.getAllSupertypes(t)) {
						System.out.print(s.getElementName() + " ");
					}
					System.out.println("");
					
					System.out.print("SUB TYPES FOR " + t.getElementName() + " are: ");
					for (IType s : typeHierarchy.getAllSubtypes(t)) {
						System.out.print(s.getElementName() + " ");
					}
					System.out.println("");
				}
				
				
				
			}
			
			
			IProgressMonitor subpm2 = null;
			if (pm != null) subpm2 = new SubProgressMonitor(pm, 100);
			refresh(subpm2);
		}
		finally {
			if (pm != null)	pm.done();
		}
		
		typeHierarchy.addTypeHierarchyChangedListener(this);
	}
	
	/**
	 * Initializes this hierarchy's internal tables with the given size.
	 */
	protected void initialize(int size) {
		if (size < 10) {
			size = 10;
		}
		int smallSize = (size / 2);
		
		this.rootContracts = new Vector<IType>();
		this.contractToSubcontracts = new HashMap<IType, Vector<IType>>(smallSize);
		this.contractToSupercontracts = new HashMap<IType, Vector<IType>>(smallSize);
		this.typeFlags = new HashMap<IType, Integer>(smallSize);
		
		//this.projectRegion = new Region();
		//this.packageRegion = new Region();
		//this.files = new HashMap(5);
	}
	
	public synchronized void addContractHierarchyChangedListener(
			IContractHierarchyChangedListener listener) {
		ArrayList listeners = this.changeListeners;
		if (listeners == null) {
			this.changeListeners = listeners = new ArrayList<ITypeHierarchyChangedListener>();
		}
		
		// register with JavaCore to get Java element delta on first listener added
		if (listeners.size() == 0) {
			JavaCore.addElementChangedListener(this);
		}
		
		// add listener only if it is not already present
		if (listeners.indexOf(listener) == -1) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Notifies listeners that this hierarchy has changed and needs
	 * refreshing. Note that listeners can be removed as we iterate
	 * through the list.
	 */
	protected void fireChange() {
		ArrayList<IContractHierarchyChangedListener> listeners = this.changeListeners;
		if (listeners == null) {
			return;
		}
		if (DEBUG) {
			System.out.println("FIRING hierarchy change ["+Thread.currentThread()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.inputContract != null) {
				System.out.println("    for hierarchy focused on " + this.inputContract.toString()); //$NON-NLS-1$
			}
		}
		// clone so that a listener cannot have a side-effect on this list when being notified
		listeners = (ArrayList<IContractHierarchyChangedListener>)listeners.clone();
		for (int i= 0; i < listeners.size(); i++) {
			final IContractHierarchyChangedListener listener= listeners.get(i);
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					ExceptionHandler.log(exception, "Exception occurred in listener of Type hierarchy change notification"); //$NON-NLS-1$
				}
				public void run() throws Exception {
					listener.contractHierarchyChanged(ContractHierarchy.this);
				}
			});
		}
	}
	
	protected void cacheFlags(IType type, int flags) {
		this.typeFlags.put(type, new Integer(flags));
	}
	
	
	
	/**
	 * Adds the given subcontract to the contract.
	 */
	protected void addSubcontract(IType type, IType subtype) {
		Vector<IType> subtypes = this.contractToSubcontracts.get(type);
		if (subtypes == null) {
			subtypes = new Vector<IType>();
			this.contractToSubcontracts.put(type, subtypes);
		}
		if (!subtypes.contains(subtype)) {
			subtypes.add(subtype);
		}
	}

	public boolean contains(IType type) {
		//	root classes
		if (this.rootContracts.contains(type)) return true;
		
		// classes
		Vector<IType> supers = this.contractToSupercontracts.get(type);
		if (supers != null) {
			return true;
		}

		return false;
	}

	public boolean exists() {
		if (!this.needsRefresh) return true;
		
		return typeHierarchy.exists();
	}

	public IType[] getAllContracts() {
		Vector<IType> classes = (Vector<IType>)this.rootContracts.clone();
		for (IType root : this.rootContracts) {
			addAllCheckingDuplicates(classes, Arrays.asList(getAllSubcontracts(root)));
		}
		
		return classes.toArray(new IType[classes.size()]);
	}

	public IType[] getAllSubcontracts(IType type) {
		return getAllSubcontractsForContract(type);
	}
	
	/**
	 * @see #getAllSubtypes(IType)
	 */
	private IType[] getAllSubcontractsForContract(IType type) {
		ArrayList<IType> subTypes = new ArrayList<IType>();
		getAllSubcontractsForContract0(type, subTypes);
		IType[] subClasses = new IType[subTypes.size()];
		subTypes.toArray(subClasses);
		return subClasses;
	}
	/**
	 */
	private void getAllSubcontractsForContract0(IType type, ArrayList<IType> subs) {
		IType[] subTypes = getSubcontractsForContract(type);
		if (subTypes.length != 0) {
			for (IType subType : subTypes) {
				subs.add(subType);
				getAllSubcontractsForContract0(subType, subs);
			}
		}
	}

	public IType[] getAllSupercontracts(IType type) {
		ArrayList<IType> supers = new ArrayList<IType>();
		if (this.contractToSupercontracts.get(type) == null) {
			return NO_TYPE;
		}
		getAllSupercontracts0(type, supers);
		IType[] superinterfaces = new IType[supers.size()];
		supers.toArray(superinterfaces);
		return superinterfaces;
	}
	
	private void getAllSupercontracts0(IType type, ArrayList<IType> supers) {
		Vector<IType> superinterfaces = this.contractToSupercontracts.get(type);
		if (superinterfaces != null && superinterfaces.size() != 0) {
			addAllCheckingDuplicates(supers, superinterfaces);
			for (IType superinterface : superinterfaces) {
				getAllSupercontracts0(superinterface, supers);
			}
		}
	}
	
	/**
	 * Adds all of the elements in the collection to the list if the
	 * element is not already in the list.
	 */
	private void addAllCheckingDuplicates(Collection<IType> dest, Collection<IType> src) {
		if (dest == null || src == null) return;
		
		for (IType element : src) {
			if (!dest.contains(element)) {
				dest.add(element);
			}
		}
	}
	
	/*
	private void getAllSupertypes0(IType type, ArrayList<IType> supers) {
		Vector<IType> superinterfaces = this.typeToSuperInterfaces.get(type);
		if (superinterfaces != null && superinterfaces.size() != 0) {
			addAllCheckingDuplicates(supers, superinterfaces);
			for (IType superinterface : superinterfaces) {
				getAllSuperInterfaces0(superinterface, supers);
			}
		}
		IType superclass = this.classToSuperclass.get(type);
		if (superclass != null) {
			supers.add(superclass);
			getAllSupertypes0(superclass, supers);
		}
	}
	*/

	/*
	public IType[] getAllTypes() {
		IType[] classes = getAllClasses();
		int classesLength = classes.length;
		IType[] allInterfaces = getAllInterfaces();
		int interfacesLength = allInterfaces.length;
		IType[] all = new IType[classesLength + interfacesLength];
		System.arraycopy(classes, 0, all, 0, classesLength);
		System.arraycopy(allInterfaces, 0, all, classesLength, interfacesLength);
		return all;
	}
	*/

	public int getCachedFlags(IType type) {
		Integer flagObject = this.typeFlags.get(type);
		if (flagObject != null){
			return flagObject.intValue();
		}
		return -1;
	}


	public IType[] getRootContracts() {
		return this.rootContracts.toArray(new IType[this.rootContracts.size()]);
	}


	public IType[] getSubcontracts(IType type) {
		return getSubcontractsForContract(type);
	}
	
	/**
	 * Returns an array of subtypes for the given type - will never return null.
	 */
	private IType[] getSubcontractsForContract(IType type) {
		Vector<IType> vector = this.contractToSubcontracts.get(type);
		if (vector == null)
			return NO_TYPE;
		else 
			return vector.toArray(new IType[vector.size()]);
	}

	public IType[] getSupercontracts(IType type) {
		Vector<IType> types = this.contractToSupercontracts.get(type);
		if (types == null) {
			return NO_TYPE;
		}
		return types.toArray(new IType[types.size()]);
	}

	public IType getType() {
		return this.inputContract;
	}

	public synchronized void refresh(IProgressMonitor monitor) throws JavaModelException {
		try {
			//this.progressMonitor = monitor;
			if (monitor != null) {
				if (this.inputContract != null) {
					monitor.beginTask(ContractHierarchyMessages.bind(ContractHierarchyMessages.hierarchy_creatingOnType, this.inputContract.getFullyQualifiedName()), 100); 
				} else {
					monitor.beginTask(ContractHierarchyMessages.hierarchy_creating, 100); 
				}
			}
			long start = -1;
			if (DEBUG) {
				start = System.currentTimeMillis();
				if (this.computeSubcontracts) {
					System.out.println("CREATING CONTRACT HIERARCHY [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					System.out.println("CREATING SUPER CONTRACT HIERARCHY [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (this.inputContract != null) {
					System.out.println("  on type " + this.inputContract.toString()); //$NON-NLS-1$
				}
			}

			compute();
			//initializeRegions();
			this.needsRefresh = false;
			//this.changeCollector = null;

			if (DEBUG) {
				if (this.computeSubcontracts) {
					System.out.println("CREATED TYPE HIERARCHY in " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					System.out.println("CREATED SUPER TYPE HIERARCHY in " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				System.out.println(this.toString());
			}
		} catch (JavaModelException e) {
			throw e;
		/*} catch (CoreException e) {
			throw new JavaModelException(e);*/
		} finally {
			if (monitor != null) {
				monitor.done();
			}
			//this.progressMonitor = null;
		}

	}
	
	protected void compute() throws JavaModelException {
		initialize(1);
		
		if (DEBUG) System.out.println("BEGIN COMPUTING CONTRACT HIERARCHY");
		
		// root contracts
		computeRootContracts(this.typeHierarchy, this.rootContracts);
		
		if (DEBUG) {
			System.out.print("ROOT CONTRACTS ARE : ");
			for (IType type : getRootContracts()) {
				System.out.print(type.getElementName() + " ");
			}
			System.out.println("");
		}
				
		IType[] classes = typeHierarchy.getAllTypes();
		for (IType clazz : classes) {
			IResource contract = ContractReferenceModel.getDirectContract(clazz.getUnderlyingResource());
			if (contract == null) continue;
			
			IType contractType = ContractReferenceUtil.getType(JavaCore.create(contract));
			if (contractType == null) continue;
			
			//cache flags
			cacheFlags(contractType, contractType.getFlags());
			
			// contract to supercontracts
			if (!this.rootContracts.contains(contractType)) {
				Vector<IType> supercontracts = new Vector<IType>();
				computeSupercontracts(clazz, supercontracts);
				if (supercontracts.size() > 0)
					this.contractToSupercontracts.put(contractType, supercontracts);
			}
			
			if (DEBUG) {
				System.out.print("SUPER CONTRACTS FOR " + contractType.getElementName() + " ARE : ");
				for (IType type : getSupercontracts(contractType)) {
					System.out.print(type.getElementName() + " ");
				}
				System.out.println("");
			}
			
			// contract to subcontracts
			Vector<IType> subcontracts = new Vector<IType>();
			computeSubcontracts(clazz, subcontracts);
			if (subcontracts.size() > 0)
				this.contractToSubcontracts.put(contractType, subcontracts);
			
			if (DEBUG) {
				System.out.print("SUB CONTRACTS FOR " + contractType.getElementName() + " ARE : ");
				for (IType type : getSubcontracts(contractType)) {
					System.out.print(type.getElementName() + " ");
				}
				System.out.println("");
			}
			
		}
		
		if (DEBUG) {
			System.out.print("Contract hierarchy of " + (getType() == null ? "null" : getType().getElementName()) + " contains: ");
			for (IType type : getAllContracts()) {
				System.out.print(type.getElementName() + " ");
			}
			System.out.println("");
		}

	}
	
	private void computeSubcontracts(IType type, Vector<IType> res) throws JavaModelException {
		Vector<IType> subTypes = new Vector<IType>();
		Collections.addAll(subTypes, this.typeHierarchy.getAllSubtypes(type));
		
		for (IType subType : subTypes) {
			
			IResource subContract = ContractReferenceModel.getDirectContract(subType.getUnderlyingResource());
			if (subContract == null) continue;
			
			boolean bIsSub = true;
			for (IType superType : this.typeHierarchy.getAllSupertypes(subType)) {
				if (!subTypes.contains(superType)) continue;
				
				if (ContractReferenceModel.getDirectContract(superType.getUnderlyingResource()) != null) {
					bIsSub = false;
					break;
				}
			}
			
			if (bIsSub) {
				res.add(ContractReferenceUtil.getType(JavaCore.create(subContract)));
			}
		}
	}
	
	private void computeSupercontracts(IType type, Vector<IType> res) throws JavaModelException {
		Vector<IType> superTypes = new Vector<IType>();
		Collections.addAll(superTypes, this.typeHierarchy.getAllSupertypes(type));
		
		for (IType superType : superTypes) {
			
			IResource superContract = ContractReferenceModel.getDirectContract(superType.getUnderlyingResource());
			if (superContract == null) continue;
			
			boolean bIsSuper = true;
			for (IType subType : this.typeHierarchy.getAllSubtypes(superType)) {
				if (!superTypes.contains(subType)) continue;
				
				if (ContractReferenceModel.getDirectContract(subType.getUnderlyingResource()) != null) {
					bIsSuper = false;
					break;
				}
			}
			
			if (bIsSuper) {
				res.add(ContractReferenceUtil.getType(JavaCore.create(superContract)));
			}
		}
		
	}
	
	private void computeRootContracts(ITypeHierarchy hierarchy, Vector<IType> res) throws JavaModelException {
		IType[] allTypes = hierarchy.getAllTypes();
		for (IType type : allTypes) {
			IResource contract = ContractReferenceModel.getDirectContract(type.getUnderlyingResource());
			if (contract == null) continue;
			
			boolean isRoot = true;
			IType[] supers = hierarchy.getAllSupertypes(type);
			for (IType superType : supers) {
				if (ContractReferenceModel.isContracted(superType.getUnderlyingResource())) {
					isRoot = false;
					break;
				}
			}
			
			if (isRoot) {
				res.add(ContractReferenceUtil.getType(JavaCore.create(contract)));
			}
		}
	}
	
		
	public void removeContractHierarchyChangedListener(
			IContractHierarchyChangedListener listener) {
		// TODO Auto-generated method stub

	}

	public void elementChanged(ElementChangedEvent event) {
		// type hierarchy change has already been fired
		if (this.needsRefresh) return;
		
		/*
		if (isAffected(event.getDelta())) {
			this.needsRefresh = true;
			fireChange();
		}
		*/
	}
	
	/**
	 * Returns true if the given delta could change this contract hierarchy
	 */
	/*
	protected synchronized boolean isAffected(IJavaElementDelta delta) {
		IJavaElement element= delta.getElement();
		switch (element.getElementType()) {
			case IJavaElement.JAVA_MODEL:
				return isAffectedByJavaModel(delta, element);
			case IJavaElement.JAVA_PROJECT:
				return isAffectedByJavaProject(delta, element);
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				return isAffectedByPackageFragmentRoot(delta, element);
			case IJavaElement.PACKAGE_FRAGMENT:
				return isAffectedByPackageFragment(delta, (PackageFragment) element);
			case IJavaElement.CLASS_FILE:
			case IJavaElement.COMPILATION_UNIT:
				return isAffectedByOpenable(delta, element);
		}
		return false;
		
	}*/

	public void typeHierarchyChanged(ITypeHierarchy typeHierarchy) {
		fireChange();
	}

	public ITypeHierarchy getTypeHierarchy() {
		return typeHierarchy;
	}

	public boolean isSupercontract(IType possibleSupercontract, IType type) {
		IType[] superContracts = getSupercontracts(type);
		
		for (IType superContract : superContracts) {
			if (possibleSupercontract.equals(superContract) || isSupercontract(possibleSupercontract, superContract)) {
				return true;
			}
		}
		
		return false;
	}

}

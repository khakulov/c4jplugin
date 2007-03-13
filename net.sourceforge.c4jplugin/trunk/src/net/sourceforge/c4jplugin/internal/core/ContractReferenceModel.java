package net.sourceforge.c4jplugin.internal.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.exceptions.OldContractModelException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.IMemento;

public class ContractReferenceModel {
	
	static private final String MODEL_VERSION = "0.2.0";
	
	// session properties
	static private final QualifiedName QN_CONTRACTED_PROPERTY = new QualifiedName(C4JActivator.PLUGIN_ID, "isContracted");
	static private final QualifiedName QN_CONTRACT_PROPERTY = new QualifiedName(C4JActivator.PLUGIN_ID, "isContract");
	static private final QualifiedName QN_DIRECTCONTRACT_PROPERTY = new QualifiedName(C4JActivator.PLUGIN_ID, "directContract");
	static private final QualifiedName QN_TARGET_PROPERTY = new QualifiedName(C4JActivator.PLUGIN_ID, "target");
	
	static private ConcurrentHashMap<IResource, Collection<IResource>> mapClassToContracts = new ConcurrentHashMap<IResource, Collection<IResource>>();
	//static private ConcurrentHashMap<IResource, Collection<IResource>> mapContractToClasses = new ConcurrentHashMap<IResource, Collection<IResource>>();
	
	//static private ConcurrentHashMap<IResource, IResource> mapTargetToContract = new ConcurrentHashMap<IResource, IResource>();
	//static private ConcurrentHashMap<IResource, IResource> mapContractToTarget = new ConcurrentHashMap<IResource, IResource>();
	
	static private IResourceVisitor clearSessionProperties  = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getName().endsWith(".java"))
				clearSessionProperties(resource);
			return true;
		}
	};
	
	private ContractReferenceModel() {
		
	}
	
	synchronized static public void loadModel(final IMemento memento) throws CoreException, OldContractModelException {
		clearModel();
		
		final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		String rootID = memento.getID();
		if (rootID == null) rootID = "0.1.0";
		
		if (rootID.compareTo(MODEL_VERSION) < 0)
			throw new OldContractModelException(CoreMessages.ContractReferenceModel_oldModel, rootID);
		
		IMemento[] children = memento.getChildren("contractedClass");
		if (children == null) return;
		
		for (IMemento child : children) {
			String classPath = child.getID();
			IResource resource = wsRoot.findMember(new Path(classPath));
			if (resource != null && resource.exists()) {
				loadDirectContract(resource, child, wsRoot);
				loadSuperContracts(resource, child, wsRoot);
			}
		}
		
		// setting the contracted state of all other resources to false
		wsRoot.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (resource.getName().endsWith(".java")) {
					if (resource.getSessionProperty(QN_CONTRACTED_PROPERTY) == null) {
						resource.setSessionProperty(QN_CONTRACTED_PROPERTY, false);
					}
					return false;
				}
				return true;
			}	
		});
	}
	
	private static void loadDirectContract(IResource target, IMemento memento, IWorkspaceRoot wsRoot) 
													throws CoreException {
		IMemento mDirectContract = memento.getChild("directContract");
		if (mDirectContract != null) {
			String directContract = mDirectContract.getID();
			if (directContract != null) {
				IResource resourceDirectContract = wsRoot.findMember(new Path(directContract));
				if (resourceDirectContract != null && resourceDirectContract.exists()) {
					target.setSessionProperty(QN_CONTRACTED_PROPERTY, true);
					target.setSessionProperty(QN_DIRECTCONTRACT_PROPERTY, resourceDirectContract);
					
					resourceDirectContract.setSessionProperty(QN_CONTRACT_PROPERTY, true);
					resourceDirectContract.setSessionProperty(QN_TARGET_PROPERTY, target);
					
					Collection<IResource> contracts = mapClassToContracts.get(target);
					if (contracts == null) {
						contracts = new HashSet<IResource>();
						mapClassToContracts.put(target, contracts);
					}
					contracts.add(resourceDirectContract);
				}
			}
		}
	}
	
	private static void loadSuperContracts(IResource target, IMemento memento, IWorkspaceRoot wsRoot) 
											throws CoreException {
		IMemento[] children = memento.getChildren("superContract");
		if (children == null || children.length == 0) return;
		
		Collection<IResource> setContracts = new HashSet<IResource>();
		for (IMemento child : children) {
			String superContract = child.getID();
			IResource resourceSuperContract = wsRoot.findMember(new Path(superContract));
			if (resourceSuperContract != null && resourceSuperContract.exists()) {
				setContracts.add(resourceSuperContract);
				resourceSuperContract.setSessionProperty(QN_CONTRACT_PROPERTY, true);
			}
		}
		
		if (setContracts.size() > 0) {
			target.setSessionProperty(QN_CONTRACTED_PROPERTY, true);
			
			Collection<IResource> contracts = mapClassToContracts.get(target);
			if (contracts == null) {
				contracts = new HashSet<IResource>();
				mapClassToContracts.put(target, contracts);
			}
			contracts.addAll(setContracts);
		}
	}
	
	synchronized static public void saveModel(IMemento memento) {
		
		memento.putString(IMemento.TAG_ID, MODEL_VERSION);
		
		for (IResource target : mapClassToContracts.keySet()) {
			IMemento mTarget = memento.createChild("contractedClass");
			mTarget.putString(IMemento.TAG_ID, target.getFullPath().toString());
			
			IResource directContract = getDirectContract(target);
			if (directContract != null) {
				IMemento mDirectContract = mTarget.createChild("directContract");
				mDirectContract.putString(IMemento.TAG_ID, directContract.getFullPath().toString());
			}
			
			Collection<IResource> contracts = mapClassToContracts.get(target);
			if (contracts != null) {
				for (IResource contract : contracts) {
					if (directContract != null && contract.equals(directContract)) continue;
					IMemento mSuperContract = mTarget.createChild("superContract");
					mSuperContract.putString(IMemento.TAG_ID, contract.getFullPath().toString());
				}
			}
		}
	}
	
	/**
	 * Completely resets the model.
	 *
	 */
	synchronized static public void clearModel() {
		mapClassToContracts.clear();
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		for (IProject project : projects) {
			try {
				//if (project.isNatureEnabled(C4JProjectNature.NATURE_ID))
					project.accept(clearSessionProperties);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Clears the project specific model data. This may leave projects
	 * which depend on <em>project</em> in an inconsistent state. It is
	 * the responsibility of the caller to track down dependencies.
	 * 
	 * @param project
	 */
	synchronized static public void clearModel(IProject project) {	
		
		for (IResource resource : mapClassToContracts.keySet()) {
			if (resource.getProject().equals(project)) {
				mapClassToContracts.remove(resource);
			}
		}
				
		for (Collection<IResource> references : mapClassToContracts.values()) {
			Iterator<IResource> iter = references.iterator();
			while (iter.hasNext()) {
				IResource reference = iter.next();
				if (reference.getProject().equals(project))
					iter.remove();
			}
		}
		
		try {
			project.accept(clearSessionProperties);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	synchronized static private void clearSessionProperties(IResource resource) {
		clearSessionProperty(resource, QN_CONTRACTED_PROPERTY);
		clearSessionProperty(resource, QN_CONTRACT_PROPERTY);
		clearSessionProperty(resource, QN_TARGET_PROPERTY);
		clearSessionProperty(resource, QN_DIRECTCONTRACT_PROPERTY);
	}
	
	synchronized static private void clearSessionProperty(IResource resource, QualifiedName property) {
		try {
			resource.setSessionProperty(property, null);
		}
		catch (CoreException e) {}
	}
	
	synchronized static public IResource getDirectContract(IResource target) {
		if (target == null) return null;
		
		try {
			IResource direct = (IResource)target.getSessionProperty(QN_DIRECTCONTRACT_PROPERTY);
			return direct;
		} catch (CoreException e) {
			//e.printStackTrace();
			return null;
		}
	}
	
	synchronized static public Boolean isContracted(IResource resource, boolean checkAgainstModel) {
		if (checkAgainstModel) {
			return mapClassToContracts.containsKey(resource);
		}
		return isContracted(resource);
	}
	
	synchronized static public Boolean isContracted(IResource resource) {
		if (resource == null) return false;
		
		Boolean contracted = null;
		try {
			contracted = (Boolean)resource.getSessionProperty(QN_CONTRACTED_PROPERTY);
		} catch (CoreException e) {}
				
		return contracted;
	}
	
	synchronized static public Boolean isTarget(IResource resource) {
		IResource directContract = getDirectContract(resource);
		return directContract != null;
	}
	
	/**
	 * Returns true if <em>resource</em> is a contract. Calling
	 * <em>checkAgainstModel = false</em> is the same as calling
	 * <em>isContract(resource)</em> and is usuallay a lot faster
	 * than using true.
	 * 
	 * @param resource
	 * @param checkAgainstModel
	 * @return
	 */
	synchronized static public boolean isContract(IResource resource, boolean checkAgainstModel) {
		if (checkAgainstModel) {
			for (Collection<IResource> contracts : mapClassToContracts.values()) {
				if (contracts.contains(resource)) return true;
			}
			return false;
		}
		return isContract(resource);
	}
	
	synchronized static public boolean isContract(IResource resource) {
		Object contract = null;
		try {
			contract = resource.getSessionProperty(QN_CONTRACT_PROPERTY);
		} catch (CoreException e) {}
		
		if (contract == null) return false;
		return (Boolean)contract;
	}
	
	/**
	 * Removes the contract <em>contract</em> from the model. This may leave
	 * the classes which have been contracted by this contract in an inconsistent
	 * state. Use the returned collection to update the affected classes.
	 * 
	 * @param contract
	 * @return All classes which where contracted by the removed contract
	 * (includes subtypes as well)
	 */
	synchronized static public Collection<IResource> removeContract(IResource contract) {
		Collection<IResource> contractedClasses = getContractedClasses(contract);
		for (IResource contractedClass : contractedClasses) {
			Collection<IResource> contracts = mapClassToContracts.get(contractedClass);
			if (contracts.remove(contract)) {
				IResource directContract = getDirectContract(contractedClass);
				if (directContract != null && directContract.equals(contract)) {
					clearSessionProperty(contractedClass, QN_DIRECTCONTRACT_PROPERTY);
				}
				if (contracts.size() == 0)
					clearSessionProperty(contractedClass, QN_CONTRACTED_PROPERTY);
			}
		}
		
		clearSessionProperties(contract);
		
		return contractedClasses;
	}
	
	/**
	 * Removes the contracted class from the model. This may leave its
	 * contracts and subtypes in an inconsistent state.
	 * 
	 * @param resource
	 * @return Returns the contracts which guarded this class.
	 */
	synchronized static public Collection<IResource> removeContractedClass(IResource resource) {
		Collection<IResource> contracts = getContracts(resource);
		
		for (IResource contract : contracts) {
			IResource target = getTarget(contract);
			if (target.equals(resource))
				clearSessionProperties(contract);
		}
		
		mapClassToContracts.remove(resource);
		clearSessionProperties(resource);
		
		return contracts;
	}
	
	/**
	 * The caller should update the state of the subtypes of <em>resource</em>
	 * and the added super-contracts.
	 * 
	 * @param resource
	 * @param contracts
	 * @return
	 */
	synchronized static public void addSuperContracts(IResource resource, Collection<IResource> contracts) {
		addContracts(resource, contracts);
	}
	
	/**
	 * The caller should update the state of the subtypes of <em>target</em>
	 * and the added direct contract.
	 * 
	 * @param target
	 * @param contract
	 */
	synchronized static public void addDirectContract(IResource target, IResource contract) {
		try {
			contract.setSessionProperty(QN_TARGET_PROPERTY, target);
			target.setSessionProperty(QN_DIRECTCONTRACT_PROPERTY, contract);
			
			Collection<IResource> contracts = new Vector<IResource>();
			contracts.add(contract);
			
			addContracts(target, contracts);
		} catch (CoreException e) {}
		
	}
	
	synchronized static private void addContracts(IResource resource, Collection<IResource> contracts) {
		if (contracts == null || contracts.size() == 0) return;
		
		try {
			for (IResource contract : contracts)
				contract.setSessionProperty(QN_CONTRACT_PROPERTY, true);
		} catch (CoreException e) {}
		
		Collection<IResource> allContracts = mapClassToContracts.get(resource);
		try {
			resource.setSessionProperty(QN_CONTRACTED_PROPERTY, true);
			if (allContracts == null) {
				allContracts = new HashSet<IResource>();
				mapClassToContracts.put(resource, allContracts);
			}
			
			allContracts.addAll(contracts);
		} catch (CoreException e) {}
	}
	
	/**
	 * Returns all contracts for <em>resource</em>
	 * 
	 * @param resource
	 * @return
	 */
	synchronized static public Collection<IResource> getContracts(IResource resource) {
		Collection<IResource> contractReferences = mapClassToContracts.get(resource);
		
		if (contractReferences == null)
			return Collections.emptyList();
		
		return Collections.unmodifiableCollection(contractReferences);
	}
	
	/**
	 * Returns all classes which are contracted by <em>contract</em>
	 * @param contract
	 * @return
	 */
	synchronized static public Collection<IResource> getContractedClasses(IResource contract) {
		Collection<IResource> contractedClasses = new HashSet<IResource>();
		
		for (IResource contractedClass : mapClassToContracts.keySet()) {
			Collection<IResource> contracts = mapClassToContracts.get(contractedClass);
			if (contracts.contains(contract))
				contractedClasses.add(contractedClass);
		}
		
		return Collections.unmodifiableCollection(contractedClasses);
	}
	
	/**
	 * Returns the target for the given contract, i. e. the class which
	 * directly references this contract.
	 * 
	 * @param contract
	 * @return The target or null in case something went wrong
	 */
	synchronized static public IResource getTarget(IResource contract) {
		if (contract == null) return null;
		
		try {
			IResource target = (IResource)contract.getSessionProperty(QN_TARGET_PROPERTY);
			return target;
		} catch (CoreException e) {}
		
		return null;
	}
	
	synchronized static public Collection<IResource> getAllContracts() {
		Collection<IResource> allContracts = new HashSet<IResource>();
		
		for (Collection<IResource> contracts : mapClassToContracts.values()) {
			allContracts.addAll(contracts);
		}
		
		return Collections.unmodifiableCollection(allContracts);
	}
	
	synchronized static public Collection<IResource> getAllContractedClasses() {
		return Collections.unmodifiableCollection(mapClassToContracts.keySet());
	}
	
}

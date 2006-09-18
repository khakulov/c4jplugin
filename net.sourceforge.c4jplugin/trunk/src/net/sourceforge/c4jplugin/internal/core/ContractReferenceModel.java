package net.sourceforge.c4jplugin.internal.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.c4jplugin.C4JActivator;

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
	
	// session properties
	static private final QualifiedName QN_CONTRACTED_PROPERTY = new QualifiedName(C4JActivator.PLUGIN_ID, "isContracted");
	static private final QualifiedName QN_CONTRACT_PROPERTY = new QualifiedName(C4JActivator.PLUGIN_ID, "isContract");

	static private ConcurrentHashMap<IResource, Collection<IResource>> mapClassContracts = new ConcurrentHashMap<IResource, Collection<IResource>>();
	static private ConcurrentHashMap<IResource, Collection<IResource>> mapContractClasses = new ConcurrentHashMap<IResource, Collection<IResource>>();
	
	private ContractReferenceModel() {
		
	}
	
	synchronized static public void loadModel(final IMemento memento) throws CoreException {
		clearModel();
		
		final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		IMemento[] children = memento.getChildren("contractedClass");
		for (IMemento child : children) {
			String classPath = child.getID();
			IResource resource = wsRoot.findMember(new Path(classPath));
			if (resource != null && resource.exists()) {
				String contracts = child.getTextData();
				
				HashSet<IResource> setContracts = new HashSet<IResource>();
				for (String contract : contracts.split(":")) {
					IResource resourceContract = wsRoot.findMember(new Path(contract));
					if (resourceContract != null && resourceContract.exists()) {
						setContracts.add(resourceContract);
					}
				}
				
				if (setContracts.size() > 0) {
					mapClassContracts.put(resource, setContracts);
					resource.setSessionProperty(QN_CONTRACTED_PROPERTY, true);
					
					for (IResource contract : setContracts) {
						Collection<IResource> setContractedClasses = mapContractClasses.get(contract);
						if (setContractedClasses == null) {
							setContractedClasses = new HashSet<IResource>();
							mapContractClasses.put(contract, setContractedClasses);
							contract.setSessionProperty(QN_CONTRACT_PROPERTY, true);
						}
						setContractedClasses.add(resource);
					}
				}
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
	
	synchronized static public void saveModel(IMemento memento) {
		for (IResource resource : mapClassContracts.keySet()) {
			Collection<IResource> contracts = mapClassContracts.get(resource);
			String strContracts = "";
			for (IResource contract : contracts) {
				strContracts += contract.getFullPath() + ":";
			}
			strContracts = strContracts.substring(0, strContracts.length()-1);
			IMemento child = memento.createChild("contractedClass", resource.getFullPath().toString());
			child.putTextData(strContracts);
		}
	}
	
	synchronized static public void clearModel() {
		mapClassContracts.clear();
		mapContractClasses.clear();
	}
	
	synchronized static public void clearModel(IProject project) {
		for (IResource resource : mapClassContracts.keySet()) {
			if (resource.getProject().equals(project)) {
				mapClassContracts.remove(resource);
			}
		}
		
		for (IResource contract : mapContractClasses.keySet()) {
			if (contract.getProject().equals(project)) {
				mapContractClasses.remove(contract);
			}
		}
		
		for (Collection<IResource> references : mapClassContracts.values()) {
			Iterator<IResource> iter = references.iterator();
			while (iter.hasNext()) {
				IResource reference = iter.next();
				if (reference.getProject().equals(project))
					iter.remove();
			}
		}
		
		for (Collection<IResource> contractedClasses : mapContractClasses.values()) {
			Iterator<IResource> iter = contractedClasses.iterator();
			while (iter.hasNext()) {
				IResource contracted = iter.next();
				if (contracted.getProject().equals(project))
					iter.remove();
			}
		}
	}
	
	synchronized static public void clearResource(IResource resource) {
		clearResource(resource, QN_CONTRACTED_PROPERTY);
		clearResource(resource, QN_CONTRACT_PROPERTY);
	}
	
	synchronized static public void clearResource(IResource resource, QualifiedName property) {
		try {
			resource.setSessionProperty(property, null);
		}
		catch (CoreException e) {}
	}
	
	synchronized static public Boolean isContracted(IResource resource, boolean checkAgainstModel) {
		if (checkAgainstModel) {
			return mapClassContracts.containsKey(resource);
		}
		return isContracted(resource);
	}
	
	synchronized static public Boolean isContracted(IResource resource) {
		Boolean contracted = null;
		try {
			contracted = (Boolean)resource.getSessionProperty(QN_CONTRACTED_PROPERTY);
		} catch (CoreException e) {}
		
		return contracted;
	}
	
	synchronized static public boolean isContract(IResource resource, boolean checkAgainstModel) {
		if (checkAgainstModel) {
			return mapContractClasses.containsKey(resource);
		}
		return isContract(resource);
	}
	
	synchronized static public boolean isContract(IResource resource) {
		Boolean contract = null;
		try {
			contract = (Boolean)resource.getSessionProperty(QN_CONTRACT_PROPERTY);
		} catch (CoreException e) {}
		
		if (contract == null) return false;
		return contract;
	}
	
	synchronized static public Collection<IResource> removeContract(IResource contract) {
		Collection<IResource> contractedClasses = getContractedClasses(contract);
		for (IResource contractedClass : contractedClasses) {
			Collection<IResource> contracts = mapClassContracts.get(contractedClass);
			contracts.remove(contract);
			if (contracts.size() == 0) {
				mapClassContracts.remove(contractedClass);
				clearResource(contractedClass, QN_CONTRACTED_PROPERTY);
			}
		}
		mapContractClasses.remove(contract);
		
		return contractedClasses;
	}
	
	synchronized static public Collection<IResource> removeContractedClass(IResource resource) {
		Collection<IResource> contracts = getContractReferences(resource);
		for (IResource contract : contracts) {
			Collection<IResource> contracted = mapContractClasses.get(contract);
			contracted.remove(resource);
			if (contracted.size() == 0) {
				mapContractClasses.remove(contract);
				clearResource(contract, QN_CONTRACT_PROPERTY);
			}
		}
		mapClassContracts.remove(resource);
		
		return contracts;
	}
	
	synchronized static public boolean addContractReference(IResource resource, IResource contract) {
		if (contract == null) {
			try {
				resource.setSessionProperty(QN_CONTRACTED_PROPERTY, false);
			} catch (CoreException e) {}
			return false;
		}
		else {
			try {
				contract.setSessionProperty(QN_CONTRACT_PROPERTY, true);
			} catch (CoreException e) {}
		}
		
		Collection<IResource> contracts = mapClassContracts.get(resource);
		Collection<IResource> contractedClasses = mapContractClasses.get(contract);
		try {
			if (contracts == null) {
				contracts = new HashSet<IResource>();
				resource.setSessionProperty(QN_CONTRACTED_PROPERTY, true);
				mapClassContracts.put(resource, contracts);
			}
			
			if (contractedClasses == null) {
				contractedClasses = new HashSet<IResource>();
				mapContractClasses.put(contract, contractedClasses);
			}
			
			return contracts.add(contract) && contractedClasses.add(resource);
		} catch (CoreException e) {}
		
		return false;
	}
	
	synchronized static public boolean addContractReferences(IResource resource, Collection<IResource> contracts) {
		if (contracts == null || contracts.size() == 0) {
			try {
				resource.setSessionProperty(QN_CONTRACTED_PROPERTY, false);
			} catch (CoreException e) {}
			return false;
		}
		else {
			try {
				for (IResource contract : contracts)
					contract.setSessionProperty(QN_CONTRACT_PROPERTY, true);
			} catch (CoreException e) {}
		}
		
		Collection<IResource> allContracts = mapClassContracts.get(resource);
		try {
			if (allContracts == null) {
				allContracts = new HashSet<IResource>();
				resource.setSessionProperty(QN_CONTRACTED_PROPERTY, true);
				mapClassContracts.put(resource, allContracts);
			}
			
			boolean addedContract = false;
			for (IResource contract : contracts) {
				Collection<IResource> contractedClasses = mapContractClasses.get(contract);
				if (contractedClasses == null) {
					contractedClasses = new HashSet<IResource>();
					mapContractClasses.put(contract, contractedClasses);
				}
				addedContract |= contractedClasses.add(resource);
			}
			
			return allContracts.addAll(contracts) && addedContract;
		} catch (CoreException e) {}
		
		return false;
	}
	
	synchronized static public void clearContractReferences(IResource resource) {
		Collection<IResource> allContracts = mapClassContracts.get(resource);
		if (allContracts == null) {
			return;
		}
		
		for (IResource contract : allContracts) {
			Collection<IResource> contractedClasses = mapContractClasses.get(contract);
			if (contractedClasses != null) {
				contractedClasses.remove(resource);
				if (contractedClasses.size() == 0) {
					mapContractClasses.remove(contract);
					clearResource(contract, QN_CONTRACT_PROPERTY);
				}
			}
		}
		
		clearResource(resource, QN_CONTRACTED_PROPERTY);
		mapClassContracts.remove(resource);
	}
	
	synchronized static public boolean setContractReferences(IResource resource, Collection<IResource> contracts) {
		clearContractReferences(resource);
		if (contracts == null || contracts.size() == 0) return true;
		return addContractReferences(resource, contracts);
	}
	
	synchronized static public Collection<IResource> getContractReferences(IResource resource) {
		Collection<IResource> contractReferences = mapClassContracts.get(resource);
		
		if (contractReferences == null)
			return Collections.emptyList();
		
		return new Vector<IResource>(contractReferences);
	}
	
	synchronized static public Collection<IResource> getContractedClasses(IResource contract) {
		Collection<IResource> contractedClasses = mapContractClasses.get(contract);
		
		if (contractedClasses == null)
			return Collections.emptyList();
		
		return new Vector<IResource>(contractedClasses);
	}
	
	synchronized static public Collection<IResource> getAllContracts() {
		return Collections.unmodifiableCollection(mapContractClasses.keySet());
	}
	
}

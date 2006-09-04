package net.sourceforge.c4jplugin.internal.core;

import java.util.Collection;
import java.util.HashSet;

import net.sourceforge.c4jplugin.internal.decorators.C4JDecorator;
import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;
import net.sourceforge.c4jplugin.internal.util.ContractReferenceUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

public class ResourceChangeListener implements IResourceChangeListener {
	
	private IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
	private ResourceDeltaVisitor deltaVisitor = new ResourceDeltaVisitor();
	

	public void resourceChanged(IResourceChangeEvent event) {
		C4JDecorator decorator = (C4JDecorator)decoratorManager.getBaseLabelProvider(C4JDecorator.ID);
		
		IResourceDelta delta = event.getDelta();
		
		try {
			
			delta.accept(deltaVisitor);
			
			Collection<IResource> visitedResources = deltaVisitor.getVisitedResources();
			Collection<IResource> visitedContracts = deltaVisitor.getVisitedContracts();
			
			for (IResource visitedResource : visitedResources) {
				ContractReferenceUtil.deleteMarkers(visitedResource);
				ContractReferenceUtil.createContractedClassMarkers(visitedResource);
			}
			
			for (IResource visitedContract : visitedContracts) {
				ContractReferenceUtil.deleteMarkers(visitedContract);
				ContractReferenceUtil.createContractMarkers(visitedContract);
			}
			
			if (decorator != null) {
				decorator.refresh(visitedResources);
				decorator.refresh(visitedContracts);
			}
			
			deltaVisitor.clear();
		} catch (CoreException e2) { }
		
	}
	
	private class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		
		private HashSet<IResource> visitedResources = new HashSet<IResource>();
		private HashSet<IResource> allContracts = new HashSet<IResource>();
		
		public Collection<IResource> getVisitedResources() {
			return visitedResources;
		}
		
		public Collection<IResource> getVisitedContracts() {
			return allContracts;
		}
		
		public void clear() {
			visitedResources.clear();
			allContracts.clear();
		}
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			
			IResource resource = delta.getResource();
			IProject project = resource.getProject();
			
			if (project == null) return true;
			if (!project.isNatureEnabled(C4JProjectNature.NATURE_ID)) return false;
			
			if (resource instanceof IFile && resource.getName().endsWith(".java")) {
				if (delta.getKind() == IResourceDelta.REMOVED) {
					if (ContractReferenceModel.isContract(resource, true)) {
						handleRemovedContract(resource);
					}
					else {
						Boolean contracted = ContractReferenceModel.isContracted(resource, true);
						if (contracted != null && contracted == true) {
							handleRemovedContractedClass(resource);
						}
					}
				}
				// resource changes or additions
				else {
					// check if the resource is a contract
					if (ContractReferenceModel.isContract(resource)) {
						handleChangedContract(resource);
					}
					else {
						handleChangedResource(resource);
					}
				}
			}
			return true;
		}
		
		synchronized private void handleRemovedContract(IResource contract) {
			Collection<IResource> changedResources = ContractReferenceModel.removeContract(contract);
			visitedResources.addAll(changedResources);
		}
		
		synchronized private void handleRemovedContractedClass(IResource resource) {
			Collection<IResource> changedContracts = ContractReferenceModel.removeContractedClass(resource);
			for (IResource changedContract : changedContracts) {
				Collection<IResource> contractedClasses = ContractReferenceModel.getContractedClasses(changedContract);
				for (IResource contractedClass : contractedClasses) {
					ContractReferenceModel.clearResource(contractedClass);
					ContractReferenceUtil.checkResourceForContracts(contractedClass);
				}
				visitedResources.addAll(contractedClasses);
			}
			allContracts.addAll(changedContracts);
		}
		
		synchronized private void handleChangedContract(IResource contract) {
			Collection<IResource> contractedClasses = ContractReferenceModel.getContractedClasses(contract);
			ContractReferenceModel.clearResource(contract);
			
			for (IResource contractedClass : contractedClasses) {
				ContractReferenceModel.clearResource(contractedClass);
				ContractReferenceUtil.checkResourceForContracts(contractedClass);
			}
			allContracts.add(contract);
			visitedResources.addAll(contractedClasses);
		}
		
		synchronized private void handleChangedResource(IResource resource) throws CoreException {
			Collection<IResource> oldContracts = ContractReferenceModel.getContractReferences(resource);
			ContractReferenceModel.clearResource(resource);
			Collection<IResource> contracts = ContractReferenceUtil.checkResourceForContracts(resource);
			visitedResources.add(resource);
			
			// check if the contracts have changed
			boolean bChanged = true;
			if (oldContracts.size() == contracts.size()) {
				if (contracts.containsAll(oldContracts)) bChanged = false;
			}
			
			if (bChanged) {
				// yes, check all subtypes
				visitedResources.addAll(ContractReferenceUtil.checkAllSubtypes(resource, true));
			}
			
			allContracts.addAll(oldContracts);
			allContracts.addAll(contracts);
		}
	}

}

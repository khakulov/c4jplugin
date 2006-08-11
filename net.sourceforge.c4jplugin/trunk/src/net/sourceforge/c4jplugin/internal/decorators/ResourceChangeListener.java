package net.sourceforge.c4jplugin.internal.decorators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;
import net.sourceforge.c4jplugin.internal.util.AnnotationUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

public class ResourceChangeListener implements IResourceChangeListener {
	
	private IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
	
	public Set<IResource> getChangedResources(IResourceDelta delta) {
		HashSet<IResource> units = new HashSet<IResource>();
		int flags = delta.getFlags();
		if ((flags & IResourceDelta.CONTENT) != 0) {
			units.add(delta.getResource());
		}
		
		IResourceDelta[] deltaChildren = delta.getAffectedChildren(IResourceDelta.CHANGED);
		if (deltaChildren != null) {
			for (IResourceDelta deltaChild : deltaChildren) {
				units.addAll(getChangedResources(deltaChild));
			}
		}
		
		return units;
	}
	

	public void resourceChanged(IResourceChangeEvent event) {
		
		IResourceDelta delta = event.getDelta();
		
		System.out.println("resource changed: " + delta.getResource().getName());
		if (delta.getKind() != IResourceDelta.CHANGED) return;
		
		try {
			String isContract = delta.getResource().getPersistentProperty(AnnotationUtil.QN_CONTRACT_PROPERTY);
			if (isContract == null) {
				//AnnotationUtil.checkContractedResources(delta.getResource());
				return;
			}
		} catch (CoreException e1) {}
		
		Set<IResource> changedResources = getChangedResources(delta);
		for (IResource changedResource : changedResources) {
			try {
				if (changedResource.getName().endsWith(".class")
						|| !changedResource.getProject().isNatureEnabled(C4JProjectNature.NATURE_ID))
					continue;
				AnnotationUtil.checkResourceForContracts(changedResource);
				Collection<IResource> resources = AnnotationUtil.checkAllSubtypes(changedResource);
				if (resources.size() > 0) {
					ContractDecorator contractDecorator = (ContractDecorator)decoratorManager.getBaseLabelProvider(ContractDecorator.ID);
					contractDecorator.refresh(resources);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	

}

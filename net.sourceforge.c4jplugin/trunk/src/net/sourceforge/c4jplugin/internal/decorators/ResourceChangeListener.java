package net.sourceforge.c4jplugin.internal.decorators;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;
import net.sourceforge.c4jplugin.internal.util.AnnotationUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
		
		if (delta.getKind() != IResourceDelta.CHANGED) return;
		
		Set<IResource> changedResources = getChangedResources(delta);
		for (IResource changedResource : changedResources) {
			try {
				if (changedResource.getName().endsWith(".class")
						|| !changedResource.getProject().hasNature(C4JProjectNature.ID_NATURE))
					continue;
				AnnotationUtil.checkContract(changedResource);
				checkAllSubTypes(changedResource);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void checkAllSubTypes(IResource resource) throws JavaModelException {
		IType type = AnnotationUtil.getType(JavaCore.create(resource));
		if (type == null) return;
		IType[] subTypes = type.newTypeHierarchy(type.getJavaProject(), null).getAllSubtypes(type);
		Vector<IResource> resources = new Vector<IResource>();
		for (IType subType : subTypes) {
			AnnotationUtil.checkContract(subType);
			resources.add(subType.getResource());
		}
		if (resources.size() > 0) {
			((ContractDecorator)decoratorManager.getBaseLabelProvider(ContractDecorator.ID)).refresh(resources);
		}
	}

}

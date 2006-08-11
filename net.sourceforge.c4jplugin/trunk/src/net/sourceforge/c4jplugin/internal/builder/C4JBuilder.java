package net.sourceforge.c4jplugin.internal.builder;

import java.util.Map;
import java.util.Vector;

import net.sourceforge.c4jplugin.internal.decorators.ContractDecorator;
import net.sourceforge.c4jplugin.internal.util.AnnotationUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

public class C4JBuilder extends IncrementalProjectBuilder {

	ResourceDeltaVisitor deltaVisitor = new ResourceDeltaVisitor();
	IResourceVisitor resourceVisitor = new ResourceVisitor();
	IResourceVisitor cleanResourceVisitor = new CleanResourceVisitor();
	
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		IResourceDelta delta = getDelta(project);
		if (delta == null) doFullBuild(project);
		else doIncrementalBuild(delta);
		
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		project.accept(cleanResourceVisitor);
	}



	private void doFullBuild(IProject project) throws CoreException {
		project.accept(resourceVisitor);
	}
	
	private void doIncrementalBuild(IResourceDelta delta) throws CoreException {
		System.out.println("[C4JBUILD] doing incremental build");
		delta.accept(deltaVisitor);
		ContractDecorator decorator = (ContractDecorator)PlatformUI.getWorkbench().getDecoratorManager().getBaseLabelProvider(ContractDecorator.ID);
		decorator.refresh(deltaVisitor.getVisitedResources());
		deltaVisitor.clear();
	}
	
	private class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		
		private Vector<IResource> visitedResources = new Vector<IResource>();
		
		public Vector<IResource> getVisitedResources() {
			return visitedResources;
		}
		
		public void clear() {
			visitedResources.clear();
		}
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			System.out.println("[DELTA VISITOR] " + delta.getResource().getName());
			IResource resource = delta.getResource();
			if (resource instanceof IFile && resource.getName().endsWith(".java")) {
				String wasContracted = resource.getPersistentProperty(AnnotationUtil.QN_CONTRACT_PROPERTY);
				boolean isContracted = AnnotationUtil.checkResourceForContracts(resource);
				if ((wasContracted != null && wasContracted.equals(AnnotationUtil.PROPERTY_IS_CONTRACTED))
						!= isContracted)
					visitedResources.add(resource);	
			}
			return true;
		}
	}
	
	private class ResourceVisitor implements IResourceVisitor {

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile && resource.getName().endsWith(".java")) {
				AnnotationUtil.checkResourceForContracts(resource);
			}
			return true;
		}
		
	}
	
	private class CleanResourceVisitor implements IResourceVisitor {

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				if (resource.getName().endsWith(".java")) {
					AnnotationUtil.cleanResource(resource);
				}
			}
			return true;
		}
		
	}
}

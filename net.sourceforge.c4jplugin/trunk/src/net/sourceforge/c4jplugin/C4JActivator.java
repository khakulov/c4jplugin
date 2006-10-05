package net.sourceforge.c4jplugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

import net.sourceforge.c4jplugin.internal.core.C4JSaveParticipant;
import net.sourceforge.c4jplugin.internal.core.ContractReferenceModel;
import net.sourceforge.c4jplugin.internal.core.ResourceChangeListener;
import net.sourceforge.c4jplugin.internal.decorators.C4JDecorator;
import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;
import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;
import net.sourceforge.c4jplugin.internal.util.C4JUtils;
import net.sourceforge.c4jplugin.internal.util.ContractReferenceUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class C4JActivator extends AbstractUIPlugin implements ILaunchListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.sourceforge.c4jplugin";
	
	public static final String RUNTIME_PLUGIN_ID = "net.sourceforge.c4jplugin.runtime";
	
	// The shared instance
	private static C4JActivator plugin;
	
	private ResourceChangeListener resourceChangeListener = null;
	
	/**
	 * The constructor
	 */
	public C4JActivator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		ISaveParticipant saveParticipant = new C4JSaveParticipant();
		ISavedState lastState = ResourcesPlugin.getWorkspace().addSaveParticipant(this, saveParticipant);

		if (lastState != null) {    
			String saveFileName = lastState.lookup(new Path(C4JSaveParticipant.SAVE_FILENAME)).toString();
			File file = getStateLocation().append(saveFileName).toFile();
			try {
				readState(file);
			}
			catch (Exception e) {
				C4JActivator.getDefault().getLog().log(
						new Status(IStatus.ERROR, C4JActivator.PLUGIN_ID, 
								IStatus.OK, UIMessages.LogMessage_readingStateFailed, e));
				refreshContractReferenceModel();
			}
		}
		else {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {
				if (project.isNatureEnabled(C4JProjectNature.NATURE_ID) && project.isOpen()) {
					refreshContractReferenceModel();
					break;
				}
			}
		}
		
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		
		resourceChangeListener = new ResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_BUILD);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static C4JActivator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}
	
	public static void writeState(File file) throws IOException {
		XMLMemento memento = XMLMemento.createWriteRoot("contractMapping"); //$NON-NLS-1$
		ContractReferenceModel.saveModel(memento);
		memento.save(new FileWriter(file));
	}
	
	public static void readState(File file) throws FileNotFoundException, WorkbenchException, CoreException {
		IMemento memento = XMLMemento.createReadRoot(new FileReader(file));
		ContractReferenceModel.loadModel(memento);
	}
	
	public static void refreshContractReferenceModel() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		// get all open c4j projects
		Vector<IProject> vecProjects = new Vector<IProject>();
		for (IProject project : projects) {
			try {
				if (project.isOpen() && project.isNatureEnabled(C4JProjectNature.NATURE_ID))
					vecProjects.add(project);
			} catch (CoreException e) {}
		}
		
		refreshContractReferenceModel(vecProjects.toArray(new IProject[] {}));
	}
	
	public static void refreshContractReferenceModel(final IProject[] projects) {
		WorkspaceJob wsJob = new WorkspaceJob(UIMessages.Builder_jobTitle) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				
				// add projects which are referenced by this projects
				HashSet<IProject> depProjects = new HashSet<IProject>();
				for (IProject project : projects) {
					depProjects.add(project);
					try {
						IProject[] refProjects = project.getReferencedProjects();
						for (IProject refProject : refProjects) {
							if (refProject.isOpen())
								depProjects.add(refProject);
						}
					}
					catch (CoreException e) {}
				}
				
				try {
					monitor.beginTask(UIMessages.Builder_startModelJob, depProjects.size()*20 + 2);
					
					boolean clearProject = true;
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					HashSet<IProject> setProjects = new HashSet<IProject>();
					for (IProject project : projects) {
						try {
							if (project.isOpen() && project.isNatureEnabled(C4JProjectNature.NATURE_ID)) {
								setProjects.add(project);
								IProject[] refProjects = project.getReferencedProjects();
								for (IProject refProject : refProjects) {
									if (refProject.isOpen())
										setProjects.add(refProject);
								}
							}
						} catch (CoreException e) {}
					}
					if (setProjects.size() == depProjects.size()) {
						clearProject = false;
						ContractReferenceModel.clearModel();
						IResourceVisitor visitor = new IResourceVisitor() {
							public boolean visit(IResource resource) throws CoreException {
								if (resource.getName().endsWith(".java"))
									ContractReferenceModel.clearResource(resource);
								
								return true;
							}	
						};
						for (IProject project : setProjects) {
							project.accept(visitor);
						}
					}
					monitor.worked(1);
					if (monitor.isCanceled()) throw new OperationCanceledException();
					
					for (IProject project : depProjects) {
						ContractReferenceUtil.deleteMarkers(project);
					}
					monitor.worked(1);
					if (monitor.isCanceled()) throw new OperationCanceledException();
					
					for (IProject project : depProjects) {
						if (monitor.isCanceled()) throw new OperationCanceledException();
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 20);
						ContractReferenceUtil.refreshModel(project, subMonitor, clearProject);
					}
				}
				finally {
					monitor.done();
				}
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						IWorkbench workbench = PlatformUI.getWorkbench();
						if (workbench != null)
							((C4JDecorator)workbench.getDecoratorManager().getBaseLabelProvider(C4JDecorator.ID)).refreshAll();
					}
				});
				
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
				return Status.OK_STATUS;
			}
		};
		
		wsJob.setUser(true);
		wsJob.schedule();
	}
	
	public void launchAdded(final ILaunch launch) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				ILaunchConfiguration config = launch.getLaunchConfiguration();
				C4JUtils.changeLaunchConfig(config);
			}
		});
	}

	public void launchChanged(ILaunch launch) {}

	public void launchRemoved(ILaunch launch) {}

}

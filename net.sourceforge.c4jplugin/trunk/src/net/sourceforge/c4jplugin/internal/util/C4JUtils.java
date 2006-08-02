package net.sourceforge.c4jplugin.internal.util;

import java.util.ArrayList;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;
import net.sourceforge.c4jplugin.internal.ui.preferences.C4JPreferences;
import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.internal.ui.editor.plugin.DependenciesPage;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public class C4JUtils {
	
	private static Job refreshJob;

	private static int previousExecutionTime;
	
	/**
	 * Computed classpath to c4j.jar
	 */
	private static String c4jrtPath = null;
	
	static public void addC4JNature(IProject project) throws CoreException {
		// add the C4J Nature
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 1, prevNatures.length);
		newNatures[0] = C4JProjectNature.ID_NATURE;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);

		// just add plugin dependency if there is a plugin.xml file
		// also consider bundles without a plugin.xml
		if (PDE.hasPluginNature(project)
		        && (WorkspaceModelManager.hasPluginManifest(project)
		        		|| WorkspaceModelManager.hasBundleManifest(project))) {
			// Dealing with a plugin project. In that case the
			// c4j.jar should be added to the classpath container
			// that lists jars imported from dependent plugins. In order
			// to do this, should add a dependency on the plugin
			// net.sourceforge.c4jplugin to the current plugin project.

			// Checks if the plugin already has the plugin dependency
			// before adding it, this avoids duplication
			if (!hasC4JPluginDependency(project)) {
				getAndPrepareToChangePDEModel(project);
				addC4JPluginDependency(project);
			}
		} else {
			// A Java project that is not a plugin project. Just add
			// the c4j.jar to the build path.
			addC4JToBuildPath(project);
		}
		
		refreshPackageExplorer();
	}
	
	static public void removeC4JNature(IProject project) throws CoreException {
		// MarkerUpdating.deleteAllMarkers(project);
		
		// remove the C4J Nature
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length - 1];
		int newPosition = 0;
		for (String prevNature : prevNatures) {
			if (!prevNature.equals(C4JProjectNature.ID_NATURE)) {
				// guard against array out of bounds which will occur if we
				// get to here in a project that DOES NOT have the c4j nature
				// (should never happen).
				if (newPosition < newNatures.length) {
					newNatures[newPosition++] = prevNature;
				} else {
					// exception... atempt to remove c4jnature from a project
					// that
					// doesn't have it. Leave the project natures unchanged.
					newNatures = prevNatures;
					break;
				}// end else
			}// end if
		}// end for
		description.setNatureIds(newNatures);
		project.setDescription(description, null);

		// just remove plugin dependency if there is a plugin.xml file
		// also consider bundles without a plugin.xml
		if (project.hasNature(PDE.PLUGIN_NATURE) 
		        && (WorkspaceModelManager.hasPluginManifest(project)
		        		|| WorkspaceModelManager.hasBundleManifest(project))) {
			// Checks if it was c4j that added the c4j dependancy and removes
			// it if it was
			if (hasC4JPluginDependency(project)) {
				getAndPrepareToChangePDEModel(project);
				removeC4JPluginDependency(project);
			}
		} else {
			// Update the build classpath to try and remove the c4j.jar
			removeC4JFromBuildPath(project);
		}
		
		//removeMarkerOnReferencingProjects(project);
		
		refreshPackageExplorer();
	}
	
	/**
	 * Attempt to update the project's build classpath with the AspectJ runtime
	 * library.
	 * 
	 * @param project
	 */
	private static void addC4JToBuildPath(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		try {
			IClasspathEntry[] originalCP = javaProject.getRawClasspath();
			IClasspathEntry c4jrtLIB = JavaCore.newContainerEntry(new Path(
					C4JActivator.C4JRT_CONTAINER), false);
			// Update the raw classpath with the new c4jCP entry.
			int originalCPLength = originalCP.length;
			IClasspathEntry[] newCP = new IClasspathEntry[originalCPLength + 1];
			System.arraycopy(originalCP, 0, newCP, 0, originalCPLength);
			newCP[originalCPLength] = c4jrtLIB;
			javaProject.setRawClasspath(newCP, new NullProgressMonitor());
		} catch (JavaModelException e) {
		}
	}
	
	//	 This method checks whether the project already has
	// net.sourceforge.c4jplugin.runtime imported. Returns true if it does.
	static private boolean hasC4JPluginDependency(IProject project) {

		ManifestEditor manEd = getPDEManifestEditor(project);
		IPluginModel model = null;
		IPluginImport[] imports = null;

		if (manEd != null) {
			model = (IPluginModel) manEd.getAggregateModel();
			imports = model.getPluginBase().getImports();
		} else {
			try {
				//checks the classpath for plugin dependencies
				IPackageFragmentRoot[] dependencies = JavaCore.create(project)
						.getPackageFragmentRoots();
				for (IPackageFragmentRoot dependency : dependencies) {
					if (dependency.getElementName().equals(
							"c4j.jar")) //$NON-NLS-1$
						return true;
				}
			} catch (JavaModelException e) {
			}
			return false;
		}

		for (IPluginImport importObj : imports) {
			if (importObj.getId().equals(C4JActivator.RUNTIME_PLUGIN_ID)) {
				return true;
			}
		}
		return false;
	}
	
	private static void addC4JPluginDependency(IProject project) {
		IWorkbenchWindow window = C4JActivator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();

		boolean autoImport = false;
		if ((C4JPreferences.askPDEAutoImport() && confirmPDEAutoAddImport(window))
				|| (C4JPreferences.doPDEAutoImport())) {
			autoImport = true;
		}

		if (autoImport) {
			importRuntimePlugin(project);
		} else {
			MessageDialog
					.openWarning(
							window.getShell(),
							UIMessages.NoAutoPluginImportDialog_title,
							UIMessages.NoAutoPluginImportDialog_message);
		}
	}
	
	/**
	 * Prompts the user for whether to auto import c4j plugin when
	 * giving c4j nature to PDE project.
	 * 
	 * @return <code>true</code> if it's OK to import, <code>false</code>
	 *         otherwise
	 */
	private static boolean confirmPDEAutoAddImport(IWorkbenchWindow window) {
		IPreferenceStore store = C4JActivator.getDefault().getPreferenceStore();
		
		MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
						window.getShell(),
						UIMessages.PluginImportDialog_importConfirmTitle,
						UIMessages.PluginImportDialog_importConfirmMsg,
						UIMessages.PluginImportDialog_importConfirmToggleMsg,
						false, // toggle is initially unchecked
						store,
						C4JPreferences.ASK_PDE_AUTO_IMPORT); 

		int result = dialog.getReturnCode();

		if (dialog.getToggleState()) {
			if (result == IDialogConstants.YES_ID) {
				// User chose Yes/Don't ask again, so always switch
				C4JPreferences.setDoPDEAutoImport(true);
			} else {
				// User chose No/Don't ask again, so never switch
				C4JPreferences.setDoPDEAutoImport(false);
			}
		}// end if
		
		return result == IDialogConstants.YES_ID;
	}
	
	private static void importRuntimePlugin(IProject project) {
		ManifestEditor manEd = getAndPrepareToChangePDEModel(project);
		if (manEd != null) {
			IPluginModel model = (IPluginModel) manEd.getAggregateModel();
			try {
				addImportToPDEModel(model, C4JActivator.RUNTIME_PLUGIN_ID);
				manEd.doSave(new NullProgressMonitor());

				// Forced build necessary here. When the project has the new
				// nature given to it a build occurs and - in the scenario
				// where the user is contemplating the "automatically add
				// dependency for you ?" dialog - a build error will occur
				// because the runtime jar cannot be located. If they agree
				// to the automatic dependency import then this build should
				// remove that compile error from their problems view.
				// The above scenario will not occur in the future if the
				// user tells the dialog not to ask again.
//				project.build(IncrementalProjectBuilder.FULL_BUILD,
//						AspectJPlugin.ID_BUILDER, null, null);

			} catch (CoreException e) {
			}
		}// end if we got a reference to the manifest editor
		else {
			MessageDialog
					.openError(
							C4JActivator.getDefault().getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
									UIMessages.AutoPluginImportDialog_noEditor_title,
									UIMessages.AutoPluginImportDialog_noEditor_message);
		}
	}
	
	/**
	 * @param model
	 * @param importId
	 * @throws CoreException
	 */
	private static void addImportToPDEModel(IPluginModel model, String importId)
			throws CoreException {
		IPluginImport importNode = model.getPluginFactory().createImport();
		importNode.setId(importId);
		model.getPluginBase().add(importNode);
		IFile manifestFile = (IFile) model.getUnderlyingResource();
		manifestFile.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
	}
	
	/**
	 * It is necessary to call this method before updating the pde model
	 * otherwise the changes may not be consistant across the pages.
	 */
	private static ManifestEditor getAndPrepareToChangePDEModel(IProject project) {
		// Must have already been validated as a PDE project
		// to get to this method. Now get the id of the plugin
		// being developed in current project.
		String pluginId = (new C4JWorkspaceModelManager().getWorkspacePluginModel(project))
								.getPluginBase().getId();

		// Open the manifest editor if it is not already open.
		ManifestEditor.openPluginEditor(pluginId);
		ManifestEditor manEd = getPDEManifestEditor(project);

		// IMPORTANT
		// Necessary to force the active page to be the dependency management
		// page. If this is not done then there is a chance that the model
		// will not be updated consistently across the pages.
		if (manEd != null) {
			manEd.setActivePage(DependenciesPage.PAGE_ID);
		}
		return manEd;
	}
	
	private static void removeC4JPluginDependency(IProject project) {
		IWorkbenchWindow window = C4JActivator.getDefault().getWorkbench()
		.getActiveWorkbenchWindow();
		if ((C4JPreferences.askPDEAutoRemoveImport() && confirmPDEAutoRemoveImport(window))
				|| (C4JPreferences.doPDEAutoRemoveImport())) {

			// Attempt to get hold of the open manifest editor
			// for the current project.
			ManifestEditor manEd = getPDEManifestEditor(project);
	
			if (manEd != null) {
				IPluginModel model = (IPluginModel) manEd.getAggregateModel();
				try {
					removeImportFromPDEModel(model,C4JActivator.RUNTIME_PLUGIN_ID);
					manEd.doSave(new NullProgressMonitor());
				} catch (CoreException e) {
					C4JActivator.getDefault().getLog().log(e.getStatus());
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					if (shell != null) {
						ErrorDialog.openError(shell, 
							UIMessages.AutoPluginRemoveErrorDialog_title,
							UIMessages.AutoPluginRemoveErrorDialog_message, e.getStatus());
					}
				}
			}// end if we got a reference to the manifest editor
			else {
				MessageDialog
						.openError(
								C4JActivator.getDefault().getWorkbench()
										.getActiveWorkbenchWindow().getShell(),
										UIMessages.AutoPluginRemoveDialog_noEditor_title,
										UIMessages.AutoPluginRemoveDialog_noEditor_message);
			}
		}
	}
	
	/**
	 * @param model
	 * @param importId
	 * @throws CoreException
	 */
	private static void removeImportFromPDEModel(IPluginModel model,
			String importId) throws CoreException {
		IPluginImport[] imports = model.getPluginBase().getImports();
		IPluginImport doomed = null;

		for (IPluginImport importObj : imports) {
			if (importObj.getId().equals(importId)) {
				doomed = importObj;
				break;
			}
		}// end for

		if (doomed != null) {
			model.getPluginBase().remove(doomed);
		}

		IFile manifestFile = (IFile) model.getUnderlyingResource();
		manifestFile.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
	}
	
	/**
	 * Returns the manifest editor if it is open in the workspace. Note: You
	 * should switch to the PDE dependency management page before changing the
	 * dependencies to avoid update inconsistencies across the pages. To do this
	 * use the C4JUtils.getAndPrepareToChangePDEModel(IProject) method.
	 * 
	 * @param project
	 * @return
	 */
	private static ManifestEditor getPDEManifestEditor(IProject project) {
		// Must have already been validated as a PDE project
		// to get to this method. Now get the id of the plugin
		// being developed in current project.
		String pluginId = (new C4JWorkspaceModelManager().getWorkspacePluginModel(project))
								.getPluginBase().getId();

		// Attempt to get hold of the open manifest editor
		// for the current project.
		ManifestEditor manEd = null;

		IEditorReference[] eRefs = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		for (IEditorReference er : eRefs) {
			if (er.getId().equals(IPDEUIConstants.MANIFEST_EDITOR_ID)
					&& er.getPartName().equals(pluginId)) {
				IEditorReference manEdRef = er;
				manEd = (ManifestEditor) manEdRef.getPart(true);
				break;
			}
		}// end for

		return manEd;
	}
	
	/**
	 * Attempt to update the project's build classpath by removing any occurance
	 * of the C4J library.
	 * 
	 * @param project
	 */
	private static void removeC4JFromBuildPath(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		try {
			IClasspathEntry[] originalCPs = javaProject.getRawClasspath();
			ArrayList<IClasspathEntry> tempCP = new ArrayList<IClasspathEntry>();

			// Go through each current classpath entry one at a time. If it
			// is not a reference to the c4j.jar then do not add it
			// to the collection of new classpath entries.
			for (IClasspathEntry originalCP : originalCPs) {
				IPath path = originalCP.getPath();
				if (!(originalCP.getEntryKind() == IClasspathEntry.CPE_CONTAINER && 
						path.segment(0).equals(C4JActivator.C4JRT_CONTAINER))) {
					tempCP.add(originalCP);
				}
			}// end for

			// Set the classpath with only those elements that survived the
			// above filtration process.
			if (originalCPs.length != tempCP.size()) {
				IClasspathEntry[] newCP = (IClasspathEntry[]) tempCP
						.toArray(new IClasspathEntry[tempCP.size()]);
				javaProject.setRawClasspath(newCP, new NullProgressMonitor());
			}// end if at least one classpath element removed
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Prompts the user for whether to automatically remove the C4J runtime plug-in 
	 * dependency when removing C4J nature from a PDE project.
	 * 
	 * @return <code>true</code> if it's OK to remove, <code>false</code>
	 *         otherwise
	 */
	private static boolean confirmPDEAutoRemoveImport(IWorkbenchWindow window) {

		IPreferenceStore store = C4JActivator.getDefault().getPreferenceStore();
		
		MessageDialogWithToggle dialog = MessageDialogWithToggle
				.openYesNoQuestion(
						window.getShell(),
						UIMessages.PluginImportDialog_removeImportConfirmTitle,
						UIMessages.PluginImportDialog_removeImportConfirmMsg,
						UIMessages.PluginImportDialog_removeImportConfirmToggleMsg,
						false, // toggle is initially unchecked
						store,
						C4JPreferences.ASK_PDE_AUTO_REMOVE_IMPORT);

		int result = dialog.getReturnCode();

		if (dialog.getToggleState()) {
			if (result == IDialogConstants.YES_ID) {
				// User chose Yes/Don't ask again, so always switch
				C4JPreferences.setDoPDEAutoRemoveImport(true);
			} else {
				// User chose No/Don't ask again, so never switch
				C4JPreferences.setDoPDEAutoRemoveImport(false);
			}
		}// end if
		return result == IDialogConstants.YES_ID;
	}
	
	private static void refreshPackageExplorer() {
		int delay = 5*previousExecutionTime;
		if (delay < 250) {
			delay = 250;
		} else if (delay > 5000) {
			delay = 5000;
		}
		getRefreshPackageExplorerJob().schedule(delay);
	}

	// reuse the same Job to avoid excessive updates
	private static Job getRefreshPackageExplorerJob() {
		if (refreshJob == null) {
			refreshJob = new RefreshPackageExplorerJob();
		}
		return refreshJob;
	}

	private static class RefreshPackageExplorerJob extends UIJob {
		RefreshPackageExplorerJob() {
			super(UIMessages.utils_refresh_explorer_job);
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			long start = System.currentTimeMillis();
			PackageExplorerPart pep = PackageExplorerPart
					.getFromActivePerspective();
			if (pep != null) {
				pep.getTreeViewer().refresh();
			}
			previousExecutionTime = (int)(System.currentTimeMillis() - start);
			//System.out.println("refresh explorer: elapsed="+previousExecutionTime);
			return Status.OK_STATUS;
		}
	}
}

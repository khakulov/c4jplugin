package net.sourceforge.c4jplugin.internal.util;

import java.util.ArrayList;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;
import net.sourceforge.c4jplugin.internal.ui.preferences.C4JPreferences;
import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;
import net.sourceforge.c4jplugin.runtime.C4JRuntimeContainer;

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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.internal.ui.editor.plugin.DependenciesPage;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.pde.ui.launcher.AbstractPDELaunchConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public class C4JUtils {
	
	public static final String REGEXP_C4J_JAVAAGENT = "(.*-javaagent:\\S*c4j.jar.*)|(.*-javaagent:\\S*\\$\\{c4j_library\\}.*)";
	public static final String C4J_JAVAAGENT = "-javaagent:${c4j_library} ";
	
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
		newNatures[0] = C4JProjectNature.NATURE_ID;
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
			// net.sourceforge.c4jplugin.runtime to the current plugin project.

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
		
		// Enabling annotation processing
		IWorkbenchWindow window = C4JActivator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		IJavaProject jproject = JavaCore.create(project);
		if (!AptConfig.isEnabled(jproject) && C4JPreferences.doAptAutoEnable()) {
			boolean autoEnable = true;
			if (C4JPreferences.askAptAutoEnable()) {
				if (!confirmAptAutoEnable(window)) autoEnable = false;
			}
			
			if (autoEnable) {
				AptConfig.setEnabled(jproject, true);
				C4JPreferences.setAptAutoEnableDone(project, true);
			}
		}
		
		C4JActivator.getDefault().refreshContractReferenceModel(new IProject[] { project });
		
		refreshPackageExplorer();
	}
	
	static public void removeC4JNature(IProject project) throws CoreException {
		
		// remove the C4J Nature
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length - 1];
		int newPosition = 0;
		for (String prevNature : prevNatures) {
			if (!prevNature.equals(C4JProjectNature.NATURE_ID)) {
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
		if (project.isNatureEnabled(PDE.PLUGIN_NATURE) 
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
		
		// Disabling annotation processing
		if (C4JPreferences.isAptAutoEnableDone(project)) {
			// only disable it if it was the C4J plug-in who enabled it
			IWorkbenchWindow window = C4JActivator.getDefault().getWorkbench().getActiveWorkbenchWindow();
			IJavaProject jproject = JavaCore.create(project);
			if (AptConfig.isEnabled(jproject) && C4JPreferences.doAptAutoDisable()) {
				boolean autoDisable = true;
				if (C4JPreferences.askAptAutoDisable()) {
					if (!confirmAptAutoDisable(window)) {
						autoDisable = false;
					}
				}
				
				C4JPreferences.setAptAutoEnableDone(project, false);
				if (autoDisable) {
					AptConfig.setEnabled(jproject, false);
				}
			}
		}
		
		ContractReferenceUtil.deleteMarkers(project);
		
		// Recalculate contract dependencies
		C4JActivator.getDefault().refreshContractReferenceModel();
		
		refreshPackageExplorer();
	}
	
	public static boolean isC4JLaunchConfig(ILaunchConfiguration launchConfig) {
		
		ILaunchConfigurationDelegate configDelegate = null;
		try {
			configDelegate = launchConfig.getType().getDelegate(ILaunchManager.RUN_MODE);
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {}
		
		if (configDelegate == null) return false;
		
		if (configDelegate instanceof AbstractJavaLaunchConfigurationDelegate) {
			try {
				IProject project = ((AbstractJavaLaunchConfigurationDelegate)configDelegate).getJavaProject(launchConfig).getProject();
				if (project.isOpen() && project.isNatureEnabled(C4JProjectNature.NATURE_ID))
					return true;
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (NullPointerException e) { }
		}
		else if (configDelegate instanceof AbstractPDELaunchConfiguration) {
			return true;
		}
		
		return false;
	}
	
	public static void changeLaunchConfig(ILaunchConfiguration config) {
		if (!isC4JLaunchConfig(config)) return;
		if (isC4JEnabled(config)) return;
		
		try {
			String id = config.getType().getIdentifier();
			IWorkbenchWindow window = C4JActivator.getDefault().getWorkbench().getActiveWorkbenchWindow();
			if (C4JPreferences.doChangeLaunchConfig(id)) {
				if (C4JPreferences.askChangeLaunchConfig(id) && confirmChangeLaunchConfig(window, id)) {
					setVMArgs(config, null);
				}
				else if (!C4JPreferences.askChangeLaunchConfig(id)) {
					setVMArgs(config, null);
				}
			}
		} catch (CoreException e) {}
	}
	
	private static boolean confirmChangeLaunchConfig(IWorkbenchWindow window, String id) {
		MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
				window.getShell(),
				UIMessages.DialogMsg_changeLaunchConfig_title,
				UIMessages.DialogMsg_changeLaunchConfig_message,
				UIMessages.DialogMsg_changeLaunchConfig_toggleMsg,
				false, // toggle is initially unchecked
				null, null); 

		int result = dialog.getReturnCode();

		C4JPreferences.setAskChangeLaunchConfig(id, !dialog.getToggleState());
		if (dialog.getToggleState())
			C4JPreferences.setDoChangeLaunchConfig(id, result == IDialogConstants.YES_ID);

		return result == IDialogConstants.YES_ID;
	}
	
	/* Before calling this method, you must check if configuration belongs to a
	 * project with a C4J nature and that the project preferences are set to
	 * allow a change of the VM arguments for this type of configuration.
	 */
	public static void setVMArgs(ILaunchConfiguration configuration, String vmargs) {
				
		if (vmargs == null) {
			if (isC4JEnabled(configuration)) return;
			vmargs =  getVMArgs(configuration);
			if (!vmargs.matches(".*-ea.*")) vmargs = "-ea " + vmargs;
			if (!vmargs.matches(REGEXP_C4J_JAVAAGENT))
				vmargs = C4J_JAVAAGENT + vmargs;
		}
		
		if (vmargs == null) {
			return;
		}
		
		try {
			ILaunchConfigurationWorkingCopy wcConfig = configuration.getWorkingCopy();
			wcConfig.setAttribute("vmargs", vmargs);
			wcConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmargs);
			wcConfig.doSave();
		} catch (CoreException e) {
			IStatus status = new Status(IStatus.WARNING, 
					C4JActivator.PLUGIN_ID, IStatus.OK, 
					NLS.bind(UIMessages.LogMessage_updatingVMArgsFailed, configuration.getName()),
					e);
			C4JActivator.getDefault().getLog().log(status);
		}
	}
	
	public static boolean isC4JEnabled(ILaunchConfiguration configuration) {
		String vmargs = getVMArgs(configuration);
		if (vmargs == null) return false;
		
		if (!vmargs.matches(".*-ea.*")) return false;
		
		if (vmargs.matches(REGEXP_C4J_JAVAAGENT)) return true;
		
		return false;
	}
	
	public static String getVMArgs(ILaunchConfiguration configuration) {
		String vmargs = null;
		try {
			vmargs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
			if (vmargs == null) {
				// Backward compatibility
				vmargs = configuration.getAttribute("vmargs", (String)null);
			}
		} catch (CoreException e) {}
		
		return vmargs;
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
					C4JRuntimeContainer.C4JRT_CONTAINER), false);
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

		if (C4JPreferences.doPDEAutoImport()) {
			if (C4JPreferences.askPDEAutoImport() && confirmPDEAutoAddImport(window))
				importRuntimePlugin(project);
			else if (!C4JPreferences.askPDEAutoImport())
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
		MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
						window.getShell(),
						UIMessages.PluginImportDialog_importConfirmTitle,
						UIMessages.PluginImportDialog_importConfirmMsg,
						UIMessages.PluginImportDialog_importConfirmToggleMsg,
						false, // toggle is initially unchecked
						null, null); 

		int result = dialog.getReturnCode();

		C4JPreferences.setAskPDEAutoImport(!dialog.getToggleState());
		if (dialog.getToggleState())
			C4JPreferences.setDoPDEAutoImport(result == IDialogConstants.YES_ID);
		
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
		
		boolean autoRemove = false;
		if (C4JPreferences.doPDEAutoRemoveImport()) {
			if (C4JPreferences.askPDEAutoRemoveImport()) {
				if (confirmPDEAutoRemoveImport(window)) autoRemove = true;
			}
			else autoRemove = true;
		}
		
		if (autoRemove) {

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
		
		// Disable annotation processing if it was previously enabled
		// by the C4J Plug-in
		IJavaProject jproject = JavaCore.create(project);
		if (AptConfig.isEnabled(jproject) && C4JPreferences.isAptAutoEnableDone(project)) {
			if ((C4JPreferences.askAptAutoDisable() && confirmAptAutoDisable(window))
					|| C4JPreferences.doAptAutoDisable()) {
				AptConfig.setEnabled(jproject, false);
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
						path.segment(0).equals(C4JRuntimeContainer.C4JRT_CONTAINER))) {
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
		MessageDialogWithToggle dialog = MessageDialogWithToggle
				.openYesNoQuestion(
						window.getShell(),
						UIMessages.PluginImportDialog_removeImportConfirmTitle,
						UIMessages.PluginImportDialog_removeImportConfirmMsg,
						UIMessages.PluginImportDialog_removeImportConfirmToggleMsg,
						false, // toggle is initially unchecked
						null, null);

		int result = dialog.getReturnCode();

		C4JPreferences.setAskPDEAutoRemoveImport(!dialog.getToggleState());
		if (dialog.getToggleState())
			C4JPreferences.setDoPDEAutoRemoveImport(result == IDialogConstants.YES_ID);
		
		return result == IDialogConstants.YES_ID;
	}
	
	/**
	 * Prompts the user for whether to automatically enable annotation processing.
	 * 
	 * @return <code>true</code> if it's OK to enable, <code>false</code>
	 *         otherwise
	 */
	private static boolean confirmAptAutoEnable(IWorkbenchWindow window) {
		MessageDialogWithToggle dialog = MessageDialogWithToggle
				.openYesNoQuestion(
						window.getShell(),
						UIMessages.AutoPluginEnableApt_title,
						UIMessages.AutoPluginEnableApt_message,
						UIMessages.AutoPluginEnableApt_toggleMsg,
						false, // toggle is initially unchecked
						null, null);

		int result = dialog.getReturnCode();

		C4JPreferences.setAskAptAutoEnable(!dialog.getToggleState());
		if (dialog.getToggleState())
			C4JPreferences.setDoAptAutoEnable(result == IDialogConstants.YES_ID);
		
		return result == IDialogConstants.YES_ID;
	}

	/**
	 * Prompts the user for whether to automatically disable annotation processing.
	 * 
	 * @return <code>true</code> if it's OK to disable, <code>false</code>
	 *         otherwise
	 */
	private static boolean confirmAptAutoDisable(IWorkbenchWindow window) {
		MessageDialogWithToggle dialog = MessageDialogWithToggle
				.openYesNoQuestion(
						window.getShell(),
						UIMessages.AutoPluginDisableApt_title,
						UIMessages.AutoPluginDisableApt_message,
						UIMessages.AutoPluginDisableApt_toggleMsg,
						false, // toggle is initially unchecked
						null, null);

		int result = dialog.getReturnCode();

		C4JPreferences.setAskAptAutoDisable(!dialog.getToggleState());
		if (dialog.getToggleState())
			C4JPreferences.setDoAptAutoDisable(result == IDialogConstants.YES_ID);
		
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
			
			return Status.OK_STATUS;
		}
	}
}

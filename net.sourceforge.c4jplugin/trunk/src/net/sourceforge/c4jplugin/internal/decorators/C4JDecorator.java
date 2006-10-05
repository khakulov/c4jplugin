/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.c4jplugin.internal.decorators;

import java.util.Collection;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.core.ContractReferenceModel;
import net.sourceforge.c4jplugin.internal.markers.IContractedMethodMarker;
import net.sourceforge.c4jplugin.internal.markers.IMethodMarker;
import net.sourceforge.c4jplugin.internal.nature.C4JProjectNature;
import net.sourceforge.c4jplugin.internal.ui.preferences.C4JPreferences;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

/**
 * An example showing how to control when an element is decorated. This example
 * decorates only elements that are instances of IResource and whose attribute
 * is 'Read-only'.
 * 
 * @see ILightweightLabelDecorator
 */
public class C4JDecorator extends LabelProvider implements ILightweightLabelDecorator {
	
	static public final String ID = "net.sourceforge.c4j.plugin.decorators.C4JDecorator";

	private static int DECO_POS = C4JPreferences.getDecorationPosition();
	private static boolean DECO_CONTRACTED_CLASS = C4JPreferences.getDecorateContractedClasses();
	private static boolean DECO_CONTRACTED_METHOD = C4JPreferences.getDecorateContractedMethods();
	private static boolean DECO_CONTRACT = C4JPreferences.getDecorateContracts();
	private static boolean DECO_CONTRACT_METHOD = C4JPreferences.getDecorateContractMethods();
	
	/** The icon image location in the project folder */
	private String contractedIcon = "icons/decorators/contracted.gif"; //NON-NLS-1
	private String contractIcon = "icons/decorators/contract.gif";
	private String preMethodIcon = "icons/decorators/pre_method.gif";
	private String postMethodIcon = "icons/decorators/post_method.gif";
	private String classInvariantIcon = "icons/decorators/class_invariant.gif";
	private String contractedPreMethodIcon = "icons/decorators/contracted_pre_method.gif";
	private String contractedPostMethodIcon = "icons/decorators/contracted_post_method.gif";
	private String contractedPrePostMethodIcon = "icons/decorators/contracted_prepost_method.gif";

	/**
	 * The image description used in
	 * <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor contractedDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, contractedIcon);
	private ImageDescriptor contractDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, contractIcon);
	private ImageDescriptor preMethodDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, preMethodIcon);
	private ImageDescriptor postMethodDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, postMethodIcon);
	private ImageDescriptor classInvariantDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, classInvariantIcon);
	private ImageDescriptor contractedPreMethodDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, contractedPreMethodIcon);
	private ImageDescriptor contractedPostMethodDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, contractedPostMethodIcon);
	private ImageDescriptor contractedPrePostMethodDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, contractedPrePostMethodIcon);
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		
		if (element instanceof IFile) {
			if (DECO_CONTRACTED_CLASS) {
				Boolean contracted = ContractReferenceModel.isContracted((IResource)element);
				if (contracted != null && contracted == true)
					overlay(decoration, contractedDescriptor);
			}
			
			if (DECO_CONTRACT && ContractReferenceModel.isContract((IResource)element)) {
				overlay(decoration, contractDescriptor);
			}
		}
		else if (element instanceof IMethod) {
			IMethod method = (IMethod)element;
			IJavaProject jproject = method.getJavaProject();
			IResource resource = null;
			try {
				if (!jproject.getProject().isNatureEnabled(C4JProjectNature.NATURE_ID)) return;
				ICompilationUnit cu = method.getCompilationUnit();
				if (cu == null) return;
				resource = cu.getCorrespondingResource();
			} catch (CoreException e1) {}
			
			if (resource == null) return;
			
			if (DECO_CONTRACTED_METHOD) {
				try {
					for (IMarker marker : resource.findMarkers(IContractedMethodMarker.ID, true, IResource.DEPTH_ZERO)) {
						if (marker.getAttribute(IContractedMethodMarker.ATTR_HANDLE_IDENTIFIER, "").equals(method.getHandleIdentifier())) {
							String type = marker.getAttribute(IContractedMethodMarker.ATTR_CONTRACT_TYPE, "");
							if (type.equals(IContractedMethodMarker.VALUE_PREPOST_METHOD)) {
								overlay(decoration, contractedPrePostMethodDescriptor);
							}
							else if (type.equals(IContractedMethodMarker.VALUE_POST_METHOD)) {
								overlay(decoration, contractedPostMethodDescriptor);
							}
							else if (type.equals(IContractedMethodMarker.VALUE_PRE_METHOD)) {
								overlay(decoration, contractedPreMethodDescriptor);
							}
							break;
						}
					}
				} catch (CoreException e) {}
			}
			
			if (DECO_CONTRACT_METHOD) {
				try {
					for (IMarker marker : resource.findMarkers(IMethodMarker.ID, true, IResource.DEPTH_ZERO)) {
						if (marker.getAttribute(IMethodMarker.ATTR_HANDLE_IDENTIFIER, "").equals(method.getHandleIdentifier())) {
							String type = marker.getAttribute(IMethodMarker.ATTR_CONTRACT_TYPE, "");
							if (type.equals(IMethodMarker.VALUE_POST_METHOD)) {
								overlay(decoration, postMethodDescriptor);
							}
							else if (type.equals(IMethodMarker.VALUE_PRE_METHOD)) {
								overlay(decoration, preMethodDescriptor);
							}
							else if (type.equals(IMethodMarker.VALUE_CLASS_INVARIANT)) {
								overlay(decoration, classInvariantDescriptor);
							}
							break;
						}
					}
				} catch (CoreException e) {}
			}
		}
	}
	
	public void refresh(Collection<IResource> resources) {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this, resources.toArray()));
	}
	
	public void refreshAll() {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}
	
	private void overlay(IDecoration decoration, ImageDescriptor descriptor) {
		decoration.addOverlay(descriptor, DECO_POS);
	}
	
	static public void setPosition(int pos) {
		DECO_POS = pos;
	}
	
	static public void setDecorateContractedClasses(boolean value) {
		DECO_CONTRACTED_CLASS = value;
	}
	
	static public void setDecorateContractedMethods(boolean value) {
		DECO_CONTRACTED_METHOD = value;
	}
	
	static public void setDecorateContracts(boolean value) {
		DECO_CONTRACT = value;
	}
	
	static public void setDecorateContractMethods(boolean value) {
		DECO_CONTRACT_METHOD = value;
	}
}
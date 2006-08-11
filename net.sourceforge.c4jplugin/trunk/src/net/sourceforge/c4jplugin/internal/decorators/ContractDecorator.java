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
import net.sourceforge.c4jplugin.internal.ui.preferences.C4JPreferences;
import net.sourceforge.c4jplugin.internal.util.AnnotationUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
public class ContractDecorator extends LabelProvider implements ILightweightLabelDecorator {
	
	static public final String ID = "net.sourceforge.c4j.plugin.decorators.Contract";

	private static int DECO_POS = C4JPreferences.getDecorationPosition();
	private static boolean DECO_CLASS = C4JPreferences.getDecorateClasses();
	private static boolean DECO_METHOD = C4JPreferences.getDecorateMethods();
	
	
	/** The icon image location in the project folder */
	private String classIcon = "icons/contract2.gif"; //NON-NLS-1
	private String preMethodIcon = "icons/pre_method.gif";
	private String postMethodIcon = "icons/post_method.gif";
	private String prepostMethodIcon = "icons/pre_post_method.gif";

	/**
	 * The image description used in
	 * <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor classDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, classIcon);
	private ImageDescriptor preMethodDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, preMethodIcon);
	private ImageDescriptor postMethodDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, postMethodIcon);
	private ImageDescriptor prepostMethodDescriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, prepostMethodIcon);
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		
		if (element instanceof IFile && DECO_CLASS) {
			try {
				String contracted = ((IFile)element).getPersistentProperty(AnnotationUtil.QN_CONTRACT_PROPERTY);
				if (contracted != null && contracted.equals(AnnotationUtil.PROPERTY_IS_CONTRACTED)) {
					overlay(decoration, classDescriptor);
				}
			} catch (CoreException e) {}
		}
		/*else if (element instanceof IMethod && DECO_METHOD) {
			IMethod method = (IMethod)element;
			IJavaProject jproject = method.getJavaProject();
			try {
				if (!jproject.getProject().isNatureEnabled(C4JProjectNature.ID_NATURE)) return;
				Integer contracted = (Integer)method.getResource().getSessionProperty(AnnotationUtil.QN_CONTRACT_PROPERTY);
				if (contracted == null || contracted == AnnotationUtil.PROPERTY_NOT_CONTRACTED)
					return;
				String[] references = (String[])method.getResource().getSessionProperty(AnnotationUtil.QN_CONTRACT_REFERENCE);
				boolean postMethod = false;
				boolean preMethod = false;
				for (String reference : references) {
					//System.out.println("[METHOD] contract in " + reference);
					IJavaElement jelement = JavaCore.create(reference);
					IType refType = AnnotationUtil.getType(jelement);
					if (!postMethod)
						postMethod = refType.getMethod("post_" + method.getElementName(), method.getParameterTypes()).exists();
					if (!preMethod)
						preMethod = refType.getMethod("pre_" + method.getElementName(), method.getParameterTypes()).exists();
				}
				
				if (postMethod && preMethod)
					overlay(decoration, prepostMethodDescriptor);
				else if (postMethod)
					overlay(decoration, postMethodDescriptor);
				else if (preMethod)
					overlay(decoration, preMethodDescriptor);
				
			} catch (CoreException e) {
				return;
			}
			
		}*/
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
	
	static public void setDecorateClasses(boolean value) {
		DECO_CLASS = value;
	}
	
	static public void setDecorateMethods(boolean value) {
		DECO_METHOD = value;
	}
}
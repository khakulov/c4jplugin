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
	private String iconPath = "icons/contract2.gif"; //NON-NLS-1

	/**
	 * The image description used in
	 * <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor descriptor = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, iconPath);
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		
		IResource resource = (IResource) element;
		
		if (resource instanceof IFile && !DECO_CLASS) return;
		
		try {
			Integer contracted = (Integer)resource.getSessionProperty(AnnotationUtil.QN_CONTRACT_PROPERTY);
			if (contracted == null) {
				if (AnnotationUtil.checkContract(resource))
					overlay(decoration);
			}
			else if (contracted.equals(AnnotationUtil.PROPERTY_IS_CONTRACTED)) {
				overlay(decoration);
			}
		} catch (CoreException e1) {}
	}
	
	public void refresh(Collection<IResource> resources) {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this, resources.toArray()));
	}
	
	public void refreshAll() {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}
	
	private void overlay(IDecoration decoration) {
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
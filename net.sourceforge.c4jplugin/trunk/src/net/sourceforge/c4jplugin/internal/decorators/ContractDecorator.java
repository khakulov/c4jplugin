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

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.util.AnnotationUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * An example showing how to control when an element is decorated. This example
 * decorates only elements that are instances of IResource and whose attribute
 * is 'Read-only'.
 * 
 * @see ILightweightLabelDecorator
 */
public class ContractDecorator implements ILightweightLabelDecorator {
	
	static public final String ID = "net.sourceforge.c4j.plugin.decorators.Contract";
	
	/**
	 * String constants for the various icon placement options from the template
	 * wizard.
	 */
	public static final String TOP_RIGHT = "TOP_RIGHT";

	public static final String TOP_LEFT = "TOP_LEFT";

	public static final String BOTTOM_RIGHT = "BOTTOM_RIGHT";

	public static final String BOTTOM_LEFT = "BOTTOM_LEFT";

	public static final String UNDERLAY = "UNDERLAY";

	/** The integer value representing the placement options */
	private int quadrant;

	/** The icon image location in the project folder */
	private String iconPath = "icons/contract2.gif"; //NON-NLS-1

	/**
	 * The image description used in
	 * <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor descriptor;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		/**
		 * Checks that the element is an IResource with the 'Read-only' attribute
		 * and adds the decorator based on the specified image description and the
		 * integer representation of the placement option.
		 */
		IResource resource = (IResource) element;
		IJavaElement javaElement = JavaCore.create(resource);
		if (javaElement != null) {
			try {
				IType type = AnnotationUtil.getType(javaElement);
				if (type != null && type.exists()) {
					//System.out.println("[Decoration] Checking \"" + type.getFullyQualifiedName() + "\" for contracts");
					String strContract = AnnotationUtil.getContractReference(type, "ContractReference");
					if (strContract != null && !AnnotationUtil.hasUnresolvedContract(type.getFullyQualifiedName())) {
						overlay(decoration);
					}
					else {
						//System.out.println("[Decoration] Checking its supertypes");
						IType[] superTypes = type.newSupertypeHierarchy(null).getAllSupertypes(type);
						for (IType superType : superTypes) {
							String strSuperContract = AnnotationUtil.getContractReferenceFromJE(superType, "ContractReference");
							if (strSuperContract != null && !AnnotationUtil.hasUnresolvedContract(superType.getFullyQualifiedName())) {
								overlay(decoration);
								break;
							}
						}
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}	
		}
		
	}
	
	private void overlay(IDecoration decoration) {
		ImageDescriptor descr = C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, iconPath);
		decoration.addOverlay(descr, IDecoration.TOP_LEFT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
	
	
}
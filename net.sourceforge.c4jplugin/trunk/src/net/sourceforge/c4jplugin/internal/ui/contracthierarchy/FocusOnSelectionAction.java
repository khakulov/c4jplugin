/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.c4jplugin.internal.ui.contracthierarchy;

import net.sourceforge.c4jplugin.internal.util.SelectionUtil;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;

/**
 * Refocuses the type hierarchy on the currently selection type.
 */
public class FocusOnSelectionAction extends Action {
		
	private ContractHierarchyViewPart fViewPart;
	
	public FocusOnSelectionAction(ContractHierarchyViewPart part) {
		super(ContractHierarchyMessages.FocusOnSelectionAction_label); 
		setDescription(ContractHierarchyMessages.FocusOnSelectionAction_description); 
		setToolTipText(ContractHierarchyMessages.FocusOnSelectionAction_tooltip); 
		fViewPart= part;
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.FOCUS_ON_SELECTION_ACTION);
	}
	
	private ISelection getSelection() {
		ISelectionProvider provider= fViewPart.getSite().getSelectionProvider();
		if (provider != null) {
			return provider.getSelection();
		}
		return null;
	}
	

	/*
	 * @see Action#run
	 */
	public void run() {
		Object element= SelectionUtil.getSingleElement(getSelection());
		if (element instanceof IType) {
			fViewPart.setInputElement((IType)element);
		}
	}	
	
	public boolean canActionBeAdded() {
		Object element= SelectionUtil.getSingleElement(getSelection());
		if (element instanceof IType) {
			IType type= (IType)element;
			setText(NLS.bind(
					ContractHierarchyMessages.FocusOnSelectionAction_label, 
					JavaElementLabels.getTextLabel(type, 0))); 
			return true;
		}
		return false;
	}
}

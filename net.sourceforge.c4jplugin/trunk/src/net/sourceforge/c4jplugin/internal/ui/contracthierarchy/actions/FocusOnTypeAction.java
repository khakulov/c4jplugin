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
package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.actions;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyMessages;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyViewPart;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog2;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Refocuses the contract hierarchy on a type selection from a all types dialog.
 */
public class FocusOnTypeAction extends Action {
			
	private ContractHierarchyViewPart fViewPart;
	
	public FocusOnTypeAction(ContractHierarchyViewPart part) {
		super(ContractHierarchyMessages.FocusOnTypeAction_label); 
		setDescription(ContractHierarchyMessages.FocusOnTypeAction_description); 
		setToolTipText(ContractHierarchyMessages.FocusOnTypeAction_tooltip); 
		
		fViewPart= part;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,	IJavaHelpContextIds.FOCUS_ON_TYPE_ACTION);
	}

	/*
	 * @see Action#run
	 */
	public void run() {
		Shell parent= fViewPart.getSite().getShell();
		TypeSelectionDialog2 dialog= new TypeSelectionDialog2(parent, false, 
			PlatformUI.getWorkbench().getProgressService(), 
			SearchEngine.createWorkspaceScope(), IJavaSearchConstants.TYPE);
	
		dialog.setTitle(ContractHierarchyMessages.FocusOnTypeAction_dialog_title); 
		dialog.setMessage(ContractHierarchyMessages.FocusOnTypeAction_dialog_message); 
		if (dialog.open() != IDialogConstants.OK_ID) {
			return;
		}
		
		Object[] types= dialog.getResult();
		if (types != null && types.length > 0) {
			IType type= (IType)types[0];
			fViewPart.setInputElement(type);
		}
	}	
}

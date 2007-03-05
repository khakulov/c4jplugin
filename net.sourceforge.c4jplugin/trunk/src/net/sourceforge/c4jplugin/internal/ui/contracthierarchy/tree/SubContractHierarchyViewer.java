/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.tree;

import java.util.List;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyLifeCycle;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyMessages;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.IContractHierarchy;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A viewer including the content provider for the subcontract hierarchy.
 * Used by the ContractHierarchyViewPart which has to provide a ContractHierarchyLifeCycle
 * on construction (shared contract hierarchy)
 */
public class SubContractHierarchyViewer extends TreeContractHierarchyViewer {
	
	public SubContractHierarchyViewer(Composite parent, ContractHierarchyLifeCycle lifeCycle, IWorkbenchPart part) {
		super(parent, new SubContractHierarchyContentProvider(lifeCycle), lifeCycle, part);
	}

	/*
	 * @see ContractHierarchyViewer#getTitle
	 */	
	public String getTitle() {
		if (isMethodFiltering()) {
			return ContractHierarchyMessages.SubTypeHierarchyViewer_filtered_title; 
		} else {
			return ContractHierarchyMessages.SubTypeHierarchyViewer_title; 
		}
	}
	
	/*
	 * @see ContractHierarchyViewer#updateContent
	 */
	public void updateContent(boolean expand) {
		getTree().setRedraw(false);
		refresh();
		
		if (expand) {
			int expandLevel= 2;
			if (isMethodFiltering()) {
				expandLevel++;
			}
			expandToLevel(expandLevel);
		}
		getTree().setRedraw(true);
	}
	
	/**
	 * Content provider for the subcontract hierarchy
	 */
	public static class SubContractHierarchyContentProvider extends TreeContractHierarchyContentProvider {
		public SubContractHierarchyContentProvider(ContractHierarchyLifeCycle lifeCycle) {
			super(lifeCycle);
		}
		
		protected final void getContractsInHierarchy(IType type, List<IType> res) {
			IContractHierarchy hierarchy= getHierarchy();
			if (hierarchy != null) {
				IType[] types= hierarchy.getSubcontracts(type);
				for (IType subtype : types) {
					res.add(subtype);
				}
			}
		}
	}
	
}
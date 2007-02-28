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

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A viewer including the content provider for the supertype hierarchy.
 * Used by the TypeHierarchyViewPart which has to provide a TypeHierarchyLifeCycle
 * on construction (shared type hierarchy)
 */
public class SuperContractHierarchyViewer extends ContractHierarchyViewer {
	
	public SuperContractHierarchyViewer(Composite parent, ContractHierarchyLifeCycle lifeCycle, IWorkbenchPart part) {
		super(parent, new SuperTypeHierarchyContentProvider(lifeCycle), lifeCycle, part);
	}

	/*
	 * @see TypeHierarchyViewer#getTitle
	 */	
	public String getTitle() {
		if (isMethodFiltering()) {
			return ContractHierarchyMessages.SuperTypeHierarchyViewer_filtered_title; 
		} else {
			return ContractHierarchyMessages.SuperTypeHierarchyViewer_title; 
		}
	}

	/*
	 * @see TypeHierarchyViewer#updateContent
	 */	
	public void updateContent(boolean expand) {
		getTree().setRedraw(false);
		refresh();
		if (expand) {
			expandAll();
		}
		getTree().setRedraw(true);
	}
	
	/*
	 * Content provider for the supertype hierarchy
	 */
	public static class SuperTypeHierarchyContentProvider extends ContractHierarchyContentProvider {
		public SuperTypeHierarchyContentProvider(ContractHierarchyLifeCycle lifeCycle) {
			super(lifeCycle);
		}
		
		protected final void getContractsInHierarchy(IType type, List<IType> res) {
			IContractHierarchy hierarchy= getHierarchy();
			if (hierarchy != null) {
				IType[] types= hierarchy.getSupercontracts(type);
				for (IType supertype : types) {
					res.add(supertype);
				}
			}
		}
		
	}		

}

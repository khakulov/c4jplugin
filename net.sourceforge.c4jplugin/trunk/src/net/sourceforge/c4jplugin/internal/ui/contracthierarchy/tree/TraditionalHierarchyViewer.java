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

import java.util.Collections;
import java.util.List;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyLifeCycle;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyMessages;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.IContractHierarchy;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A TypeHierarchyViewer that looks like the type hierarchy view of VA/Java:
 * Starting form Object down to the element in focus, then all subclasses from
 * this element.
 * Used by the TypeHierarchyViewPart which has to provide a TypeHierarchyLifeCycle
 * on construction (shared type hierarchy)
 */
public class TraditionalHierarchyViewer extends TreeContractHierarchyViewer {	
	
	public TraditionalHierarchyViewer(Composite parent, ContractHierarchyLifeCycle lifeCycle, IWorkbenchPart part) {
		super(parent, new TraditionalHierarchyContentProvider(lifeCycle), lifeCycle, part);
	}
	
	/*
	 * @see TypeHierarchyViewer#getTitle
	 */	
	public String getTitle() {
		if (isMethodFiltering()) {
			return ContractHierarchyMessages.TraditionalHierarchyViewer_filtered_title; 
		} else {
			return ContractHierarchyMessages.TraditionalHierarchyViewer_title; 
		}
	}

	/*
	 * @see TypeHierarchyViewer#updateContent
	 */		
	public void updateContent(boolean expand) {
		getTree().setRedraw(false);
		refresh();
		
		if (expand) {
			TraditionalHierarchyContentProvider contentProvider= (TraditionalHierarchyContentProvider) getContentProvider();
			int expandLevel= contentProvider.getExpandLevel();
			if (isMethodFiltering()) {
				expandLevel++;
			}
			expandToLevel(expandLevel);
		}
		getTree().setRedraw(true);
	}	

	/**
	 * Content provider for the 'traditional' type hierarchy.
	 */	
	public static class TraditionalHierarchyContentProvider extends TreeContractHierarchyContentProvider {
		
			
		public TraditionalHierarchyContentProvider(ContractHierarchyLifeCycle provider) {
			super(provider);
		}
		
		public int getExpandLevel() {
			IContractHierarchy hierarchy= getHierarchy();
			if (hierarchy != null) {
				IType input= hierarchy.getType();
				if (input != null) {
					return getDepth(hierarchy, input) + 2;
				} else {
					return 5;
				}
			}
			return 2;
		}
		
		private int getDepth(IContractHierarchy hierarchy, IType input) {
			return getDepth0(hierarchy, input);
		}
		
		private int getDepth0(IContractHierarchy hierarchy, IType type) {
			IType[] supercontracts = hierarchy.getSupercontracts(type);
			if (supercontracts != null) {
				int max = 0;
				for (IType supercontract : supercontracts) {
					int depth = getDepth0(hierarchy, supercontract);
					if (depth > max)
						max = depth;
				}
				return max+1;
			}
			else return 0;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jdt.internal.ui.typehierarchy.TypeHierarchyContentProvider#getRootTypes(java.util.List)
		 */
		protected final void getRootTypes(List<IType> res) {
			IContractHierarchy hierarchy= getHierarchy();
			if (hierarchy != null) {
				IType[] classes= hierarchy.getRootContracts();
				Collections.addAll(res, classes);
			}
		}
				
		/*
		 * @see ContractHierarchyContentProvider.getTypesInHierarchy
		 */	
		protected final void getContractsInHierarchy(IType type, List<IType> res) {
			IContractHierarchy hierarchy= getHierarchy();
			if (hierarchy != null) {
				IType[] types= hierarchy.getSubcontracts(type);
				for (IType curr : types) {
					res.add(curr);
				}
			}
		}
		
	}
}

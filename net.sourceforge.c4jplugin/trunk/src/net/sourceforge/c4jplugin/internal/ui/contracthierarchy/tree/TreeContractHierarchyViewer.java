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
package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.tree;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyLifeCycle;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.HierarchyLabelProvider;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.HierarchyViewerSorter;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.IContractHierarchyViewer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.util.JavaUIHelp;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.ProblemTreeViewer;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
 
public abstract class TreeContractHierarchyViewer extends ProblemTreeViewer
							implements IContractHierarchyViewer {
	
	private OpenAction fOpen;
	private HierarchyLabelProvider fLabelProvider;
			
	public TreeContractHierarchyViewer(Composite parent, IContentProvider contentProvider, ContractHierarchyLifeCycle lifeCycle,  IWorkbenchPart part) {
		super(new Tree(parent, SWT.SINGLE));

		fLabelProvider= new HierarchyLabelProvider(lifeCycle);
	
		setLabelProvider(new DecoratingJavaLabelProvider(fLabelProvider, true));
		setUseHashlookup(true);
		setContentProvider(contentProvider);
		setComparator(new HierarchyViewerSorter(lifeCycle));
		
		fOpen= new OpenAction(part.getSite());
		addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				fOpen.run();
			}
		});
		
		JavaUIHelp.setHelp(this, IJavaHelpContextIds.TYPE_HIERARCHY_VIEW);
	}
	
	public void setQualifiedTypeName(boolean on) {
		if (on) {
			fLabelProvider.setTextFlags(fLabelProvider.getTextFlags() | JavaElementLabels.T_POST_QUALIFIED);
		} else {
			fLabelProvider.setTextFlags(fLabelProvider.getTextFlags() & ~JavaElementLabels.T_POST_QUALIFIED);
		}
		refresh();
	}
	
	/**
	 * Attaches a contextmenu listener to the tree
	 */
	public void initContextMenu(IMenuListener menuListener, String popupId, IWorkbenchPartSite viewSite) {
		MenuManager menuMgr= new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(menuListener);
		Menu menu= menuMgr.createContextMenu(getTree());
		getTree().setMenu(menu);
		viewSite.registerContextMenu(popupId, menuMgr, this);
	}

	/**
	 * Fills up the context menu with items for the hierarchy viewer
	 * Should be called by the creator of the context menu
	 */	
	public void contributeToContextMenu(IMenuManager menu) {
	}

	/**
	 * Set the member filter
	 */
	public void setMemberFilter(IMember[] memberFilter) {
		TreeContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {
			contentProvider.setMemberFilter(memberFilter);
		}
	}

	/**
	 * Returns if method filtering is enabled.
	 */	
	public boolean isMethodFiltering() {
		TreeContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {
			return contentProvider.getMemberFilter() != null;
		}
		return false;
	}

	public void setWorkingSetFilter(ViewerFilter filter) {
		fLabelProvider.setFilter(filter);
		TreeContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {
			contentProvider.setWorkingSetFilter(filter);
		}		
	}
	
	/**
	 * Returns true if the hierarchy contains elements. Returns one of them
	 * With member filtering it is possible that no elements are visible
	 */ 
	public Object containsElements() {
		TreeContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {
			Object[] elements= contentProvider.getElements(null);
			if (elements.length > 0) {
				return elements[0];
			}
		}
		return null;
	}	
	
	/**
	 * Returns true if the hierarchy contains elements. Returns one of them
	 * With member filtering it is possible that no elements are visible
	 */ 
	public IType getRootType() {
		TreeContractHierarchyContentProvider contentProvider= getHierarchyContentProvider();
		if (contentProvider != null) {		
			Object[] elements=  contentProvider.getElements(null);
			if (elements.length > 0 && elements[0] instanceof IType) {
				return (IType) elements[0];
			}
		}
		return null;
	}	
			
	/**
	 * Returns true if the hierarchy contains element the element.
	 */ 
	public boolean isElementShown(Object element) {
		return findItem(element) != null;
	}
	
	/**
	 * Updates the content of this viewer: refresh and expanding the tree in the way wanted.
	 */
	public abstract void updateContent(boolean doExpand);	
	
	/**
	 * Returns the title for the current view
	 */
	public abstract String getTitle();
	
	/*
	 * @see StructuredViewer#setContentProvider
	 * Content provider must be of type ContractHierarchyContentProvider
	 */
	public void setContentProvider(IContentProvider cp) {
		Assert.isTrue(cp instanceof TreeContractHierarchyContentProvider);
		super.setContentProvider(cp);
	}

	protected TreeContractHierarchyContentProvider getHierarchyContentProvider() {
		return (TreeContractHierarchyContentProvider)getContentProvider();
	}
	
}
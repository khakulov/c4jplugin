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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyContentProvider;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyLifeCycle;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.IContractHierarchy;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * Base class for tree content providers for contract hierarchy viewers.
 * Implementors must override 'getContractsInHierarchy'.
 */
public abstract class TreeContractHierarchyContentProvider 
			extends ContractHierarchyContentProvider implements ITreeContentProvider {
	
	public TreeContractHierarchyContentProvider(ContractHierarchyLifeCycle lifecycle) {
		super(lifecycle);
	}
	
	public Object[] getElements(Object parent) {
		ArrayList<IType> types= new ArrayList<IType>();
		getRootTypes(types);
		for (int i= types.size() - 1; i >= 0; i--) {
			IType curr= (IType) types.get(i);
			try {
				if (!isInControl(curr)) {
					types.remove(i);
				}
			} catch (JavaModelException e) {
				// ignore
			}
		}
		return types.toArray();
	}
	
	protected void getRootTypes(List<IType> res) {
		IContractHierarchy hierarchy= getHierarchy();
		if (hierarchy != null) {
			IType input= hierarchy.getType();
			if (input != null) {
				res.add(input);
			}
			// opened on a region: dont show
		}
	}
	
	/*
	 * Called for the tree children.
	 * @see ITreeContentProvider#getChildren
	 */	
	public Object[] getChildren(Object element) {
		if (element instanceof IType) {
			try {
				IType type= (IType)element;
	
				List<IMember> children= new ArrayList<IMember>();
				if (fMemberFilter != null) {
					addFilteredMemberChildren(type, children);
				}
	
				addTypeChildren(type, children);
				
				return children.toArray();
			} catch (JavaModelException e) {
				// ignore
			}
		}
		return NO_ELEMENTS;
	}
	
	/*
	 * @see ITreeContentProvider#hasChildren
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof IType) {
			try {
				IType type= (IType) element;
				return hasTypeChildren(type) || (fMemberFilter != null && hasMemberFilterChildren(type));
			} catch (JavaModelException e) {
				return false;
			}			
		}
		return false;
	}	
	
	private void addFilteredMemberChildren(IType parent, List<IMember> children) throws JavaModelException {
		for (int i= 0; i < fMemberFilter.length; i++) {
			IMember member= fMemberFilter[i];
			if (parent.equals(member.getDeclaringType())) {
				if (!children.contains(member)) {
					children.add(member);
				}
			} else if (member instanceof IMethod) {
				addCompatibleMethods((IMethod) member, parent, children);
			}
		}		
	}
		
	private void addTypeChildren(IType type, List<IMember> children) throws JavaModelException {
		ArrayList<IType> types= new ArrayList<IType>();
		getContractsInHierarchy(type, types);
		for (IType curr : types) {
			if (isInControl(curr)) {
				children.add(curr);
			}
		}
	}
	
	
	
	private boolean hasMemberFilterChildren(IType type) throws JavaModelException {
		for (int i= 0; i < fMemberFilter.length; i++) {
			IMember member= fMemberFilter[i];
			if (type.equals(member.getDeclaringType())) {
				return true;
			} else if (member instanceof IMethod) {
				if (hasCompatibleCondition((IMethod) member, type)) {
					return true;
				}
			}
		}
		return false;
	}
		
	private boolean hasTypeChildren(IType type) throws JavaModelException {
		ArrayList<IType> types= new ArrayList<IType>();
		getContractsInHierarchy(type, types);
		for (IType curr : types) {
			if (isInControl(curr)) {
				return true;
			}
		}
		return false;
	}
	
	
	/*
	 * @see ITreeContentProvider#getParent
	 */
	public Object getParent(Object element) {
		if (element instanceof IMember) {
			IMember member= (IMember) element;
			if (member.getElementType() == IJavaElement.TYPE) {
				return getParentContract((IType)member);
			}
			return member.getDeclaringType();
		}
		return null;
	}
	
	/**
	 * Hook to overwrite. Return null if parent is ambiguous.
	 */	
	protected IType getParentContract(IType type) {
		IContractHierarchy hierarchy= getHierarchy();
		if (hierarchy != null) {
			IType[] supers = hierarchy.getSupercontracts(type);
			if (supers != null && supers.length == 1) return supers[0];
		}
		return null;
	}

	
}
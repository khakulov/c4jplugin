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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * Manages a type hierarchy, to keep it refreshed, and to allow it to be shared.
 */
public class ContractHierarchyLifeCycle implements IContractHierarchyChangedListener, IElementChangedListener {
	
	private boolean fHierarchyRefreshNeeded;
	private IContractHierarchy fHierarchy;
	private IJavaElement fInputElement;
	private boolean fIsSuperTypesOnly;
	
	private List<IContractHierarchyLifeCycleListener> fChangeListeners;
	
	public ContractHierarchyLifeCycle() {
		this(false);
	}	
	
	public ContractHierarchyLifeCycle(boolean isSuperTypesOnly) {
		fHierarchy= null;
		fInputElement= null;
		fIsSuperTypesOnly= isSuperTypesOnly;
		fChangeListeners= new ArrayList<IContractHierarchyLifeCycleListener>(2);
	}
	
	public IContractHierarchy getContractHierarchy() {
		return fHierarchy;
	}
	
	public ITypeHierarchy getTypeHierarchy() {
		return fHierarchy.getTypeHierarchy();
	}
	
	public IJavaElement getInputElement() {
		return fInputElement;
	}
	
	
	public void freeHierarchy() {
		if (fHierarchy != null) {
			fHierarchy.removeContractHierarchyChangedListener(this);
			JavaCore.removeElementChangedListener(this);
			fHierarchy= null;
			fInputElement= null;
		}
	}
	
	public void removeChangedListener(IContractHierarchyLifeCycleListener listener) {
		fChangeListeners.remove(listener);
	}
	
	public void addChangedListener(IContractHierarchyLifeCycleListener listener) {
		if (!fChangeListeners.contains(listener)) {
			fChangeListeners.add(listener);
		}
	}
	
	private void fireChange(IType[] changedTypes) {
		for (int i= fChangeListeners.size()-1; i>=0; i--) {
			IContractHierarchyLifeCycleListener curr= (IContractHierarchyLifeCycleListener) fChangeListeners.get(i);
			curr.contractHierarchyChanged(this, changedTypes);
		}
	}
			
	public void ensureRefreshedContractHierarchy(final IJavaElement element, IRunnableContext context) throws InvocationTargetException, InterruptedException {
		if (element == null || !element.exists()) {
			freeHierarchy();
			return;
		}
		boolean hierachyCreationNeeded= (fHierarchy == null || !element.equals(fInputElement));
		
		if (hierachyCreationNeeded || fHierarchyRefreshNeeded) {
			
			IRunnableWithProgress op= new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) throws InvocationTargetException, InterruptedException {
					try {
						doHierarchyRefresh(element, pm);
					} catch (JavaModelException e) {
						throw new InvocationTargetException(e);
					} catch (OperationCanceledException e) {
						throw new InterruptedException();
					}
				}
			};
			fHierarchyRefreshNeeded= true;
			context.run(true, true, op);
			fHierarchyRefreshNeeded= false;
		}
	}
	
	private IContractHierarchy createContractHierarchy(IJavaElement element, IProgressMonitor pm) throws JavaModelException {
		if (element.getElementType() == IJavaElement.TYPE) {
			IType type= (IType) element;
			if (fIsSuperTypesOnly) {
				return new ContractHierarchy(type, pm, false);
			} else {
				return new ContractHierarchy(type, pm);
			}
		}
		else throw new JavaModelException(new JavaModelStatus(IStatus.ERROR, "Input for contract hierarchy ist not a java type"));
		/*else {
			IRegion region= JavaCore.newRegion();
			if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
				// for projects only add the contained source folders
				IPackageFragmentRoot[] roots= ((IJavaProject) element).getPackageFragmentRoots();
				for (int i= 0; i < roots.length; i++) {
					if (!roots[i].isExternal()) {
						region.add(roots[i]);
					}
				}
			} else if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				IPackageFragmentRoot[] roots= element.getJavaProject().getPackageFragmentRoots();
				String name= element.getElementName();
				for (int i= 0; i < roots.length; i++) {
					IPackageFragment pack= roots[i].getPackageFragment(name);
					if (pack.exists()) {
						region.add(pack);
					}
				}
			} else {
				region.add(element);
			}
			IJavaProject jproject= element.getJavaProject();
			return jproject.newTypeHierarchy(region, pm);
		}*/
	}
	
	
	public synchronized void doHierarchyRefresh(IJavaElement element, IProgressMonitor pm) throws JavaModelException {
		boolean hierachyCreationNeeded= (fHierarchy == null || !element.equals(fInputElement));
		// to ensure the order of the two listeners always remove / add listeners on operations
		// on type hierarchies
		if (fHierarchy != null) {
			fHierarchy.removeContractHierarchyChangedListener(this);
			JavaCore.removeElementChangedListener(this);
		}
		if (hierachyCreationNeeded) {
			fHierarchy= createContractHierarchy(element, pm);
			if (pm != null && pm.isCanceled()) {
				throw new OperationCanceledException();
			}
			fInputElement= element;
		} else {
			fHierarchy.refresh(pm);
		}
		fHierarchy.addContractHierarchyChangedListener(this);
		JavaCore.addElementChangedListener(this);
		fHierarchyRefreshNeeded= false;
	}		
	
	/*
	 * @see ITypeHierarchyChangedListener#typeHierarchyChanged
	 */
	public void contractHierarchyChanged(IContractHierarchy typeHierarchy) {
	 	fHierarchyRefreshNeeded= true;
 		fireChange(null);
	}		

	/*
	 * @see IElementChangedListener#elementChanged(ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
		if (fChangeListeners.isEmpty()) {
			return;
		}
		
		if (fHierarchyRefreshNeeded) {
			return;
		} else {
			ArrayList<IType> changedTypes= new ArrayList<IType>();
			processDelta(event.getDelta(), changedTypes);
			if (changedTypes.size() > 0) {
				fireChange((IType[]) changedTypes.toArray(new IType[changedTypes.size()]));
			}
		}
	}
	
	/*
	 * Assume that the hierarchy is intact (no refresh needed)
	 */					
	private void processDelta(IJavaElementDelta delta, ArrayList<IType> changedTypes) {
		IJavaElement element= delta.getElement();
		switch (element.getElementType()) {
			case IJavaElement.TYPE:
				processTypeDelta((IType) element, changedTypes);
				processChildrenDelta(delta, changedTypes); // (inner types)
				break;
			case IJavaElement.JAVA_MODEL:
			case IJavaElement.JAVA_PROJECT:
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.PACKAGE_FRAGMENT:
				processChildrenDelta(delta, changedTypes);
				break;
			case IJavaElement.COMPILATION_UNIT:
				ICompilationUnit cu= (ICompilationUnit)element;
				if (cu.getOwner() != null) {
					return;
				}
				
				if (delta.getKind() == IJavaElementDelta.CHANGED && isPossibleStructuralChange(delta.getFlags())) {
					try {
						if (cu.exists()) {
							IType[] types= cu.getAllTypes();
							for (int i= 0; i < types.length; i++) {
								processTypeDelta(types[i], changedTypes);
							}
						}
					} catch (JavaModelException e) {
						C4JActivator.log(e);
					}
				} else {
					processChildrenDelta(delta, changedTypes);
				}
				break;
			case IJavaElement.CLASS_FILE:	
				if (delta.getKind() == IJavaElementDelta.CHANGED) {
					try {
						IType type= ((IClassFile) element).getType();
						processTypeDelta(type, changedTypes);
					} catch (JavaModelException e) {
						C4JActivator.log(e);
					}
				} else {
					processChildrenDelta(delta, changedTypes);
				}
				break;				
		}
	}
	
	private boolean isPossibleStructuralChange(int flags) {
		return (flags & (IJavaElementDelta.F_CONTENT | IJavaElementDelta.F_FINE_GRAINED)) == IJavaElementDelta.F_CONTENT;
	}
	
	private void processTypeDelta(IType type, ArrayList<IType> changedTypes) {
		if (getTypeHierarchy().contains(type) || getContractHierarchy().contains(type)) {
			changedTypes.add(type);
		}
	}
	
	private void processChildrenDelta(IJavaElementDelta delta, ArrayList<IType> changedTypes) {
		IJavaElementDelta[] children= delta.getAffectedChildren();
		for (int i= 0; i < children.length; i++) {
			processDelta(children[i], changedTypes); // recursive
		}
	}
	

}

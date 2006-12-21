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
package net.sourceforge.c4jplugin.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.util.ExceptionHandler;
import net.sourceforge.c4jplugin.internal.util.OpenContractHierarchyUtil;
import net.sourceforge.c4jplugin.internal.util.SelectionConverter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.IJavaStatusConstants;

import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.browsing.LogicalPackage;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;

/**
 * This action opens a contract hierarchy on the selected type.
 * <p>
 * The action is applicable to selections containing elements of type
 * <code>IType</code>.
 * 
 */
public class OpenContractHierarchyAction extends SelectionDispatchAction {
	
	private JavaEditor fEditor;
	
	/**
	 * Creates a new <code>OpenTypeHierarchyAction</code>. The action requires
	 * that the selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site the site providing context information for this action
	 */
	public OpenContractHierarchyAction(IWorkbenchSite site) {
		super(site);
		setText(ActionMessages.OpenContractHierarchyAction_label); 
		setToolTipText(ActionMessages.OpenContractHierarchyAction_tooltip); 
		setDescription(ActionMessages.OpenContractHierarchyAction_description); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.OPEN_TYPE_HIERARCHY_ACTION);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 * @param editor the Java editor
	 */
	public OpenContractHierarchyAction(JavaEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
		setEnabled(SelectionConverter.canOperateOn(fEditor));
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(ITextSelection selection) {
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(isEnabled(selection));
	}
	
	private boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() != 1)
			return false;
		Object input= selection.getFirstElement();
		
		
		if (input instanceof LogicalPackage)
			return true;
		
		if (!(input instanceof IJavaElement))
			return false;
		switch (((IJavaElement)input).getElementType()) {
			case IJavaElement.INITIALIZER:
			case IJavaElement.METHOD:
			case IJavaElement.FIELD:
			case IJavaElement.TYPE:
				return true;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.JAVA_PROJECT:
			case IJavaElement.PACKAGE_FRAGMENT:
			case IJavaElement.PACKAGE_DECLARATION:
			case IJavaElement.IMPORT_DECLARATION:	
			case IJavaElement.CLASS_FILE:
			case IJavaElement.COMPILATION_UNIT:
				return true;
			case IJavaElement.LOCAL_VARIABLE:
			default:
				return false;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void run(ITextSelection selection) {
		IJavaElement input= SelectionConverter.getInput(fEditor);
		if (!ActionUtil.isProcessable(getShell(), input))
			return;		
		
		try {
			IJavaElement[] elements= SelectionConverter.codeResolveOrInputForked(fEditor);
			if (elements == null)
				return;
			List<IJavaElement> candidates= new ArrayList<IJavaElement>(elements.length);
			for (int i= 0; i < elements.length; i++) {
				IJavaElement[] resolvedElements= OpenContractHierarchyUtil.getCandidates(elements[i]);
				if (resolvedElements != null)	
					candidates.addAll(Arrays.asList(resolvedElements));
			}
			run((IJavaElement[])candidates.toArray(new IJavaElement[candidates.size()]));
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), getDialogTitle(), ActionMessages.SelectionConverter_codeResolve_failed);
		} catch (InterruptedException e) {
			// cancelled
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void run(IStructuredSelection selection) {
		if (selection.size() != 1)
			return;
		Object input= selection.getFirstElement();
		
		if (input instanceof LogicalPackage) {
			IPackageFragment[] fragments= ((LogicalPackage)input).getFragments();
			if (fragments.length == 0)
				return;
			input= fragments[0];
		}

		if (!(input instanceof IJavaElement)) {
			IStatus status= createStatus(ActionMessages.OpenContractHierarchyAction_messages_no_java_element); 
			ErrorDialog.openError(getShell(), getDialogTitle(), ActionMessages.OpenContractHierarchyAction_messages_title, status); 
			return;
		}
		IJavaElement element= (IJavaElement) input;
		if (!ActionUtil.isProcessable(getShell(), element))
			return;

		List<IJavaElement> result= new ArrayList<IJavaElement>(1);
		IStatus status= compileCandidates(result, element);
		if (status.isOK()) {
			run((IJavaElement[]) result.toArray(new IJavaElement[result.size()]));
		} else {
			ErrorDialog.openError(getShell(), getDialogTitle(), ActionMessages.OpenContractHierarchyAction_messages_title, status); 
		}
	}

	/*
	 * No Javadoc since the method isn't meant to be public but is
	 * since the beginning
	 */
	public void run(IJavaElement[] elements) {
		if (elements.length == 0) {
			getShell().getDisplay().beep();
			return;
		}
		OpenContractHierarchyUtil.open(elements, getSite().getWorkbenchWindow());
	}
	
	private static String getDialogTitle() {
		return ActionMessages.OpenContractHierarchyAction_dialog_title; 
	}
	
	private static IStatus compileCandidates(List<IJavaElement> result, IJavaElement elem) {
		IStatus ok= new Status(IStatus.OK, C4JActivator.PLUGIN_ID, 0, "", null); //$NON-NLS-1$		
		try {
			switch (elem.getElementType()) {
				case IJavaElement.INITIALIZER:
				case IJavaElement.METHOD:
				case IJavaElement.FIELD:
				case IJavaElement.TYPE:
				case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				case IJavaElement.JAVA_PROJECT:
					result.add(elem);
					return ok;
				case IJavaElement.PACKAGE_FRAGMENT:
					if (((IPackageFragment)elem).containsJavaResources()) {
						result.add(elem);
						return ok;
					}
					return createStatus(ActionMessages.OpenContractHierarchyAction_messages_no_java_resources); 
				case IJavaElement.PACKAGE_DECLARATION:
					result.add(elem.getAncestor(IJavaElement.PACKAGE_FRAGMENT));
					return ok;
				case IJavaElement.IMPORT_DECLARATION:	
					IImportDeclaration decl= (IImportDeclaration) elem;
					if (decl.isOnDemand()) {
						elem= JavaModelUtil.findTypeContainer(elem.getJavaProject(), Signature.getQualifier(elem.getElementName()));
					} else {
						elem= elem.getJavaProject().findType(elem.getElementName());
					}
					if (elem != null) {
						result.add(elem);
						return ok;
					}
					return createStatus(ActionMessages.OpenContractHierarchyAction_messages_unknown_import_decl);
				case IJavaElement.CLASS_FILE:
					result.add(((IClassFile)elem).getType());
					return ok;				
				case IJavaElement.COMPILATION_UNIT:
					ICompilationUnit cu= (ICompilationUnit)elem;
					IType[] types= cu.getTypes();
					if (types.length > 0) {
						result.addAll(Arrays.asList(types));
						return ok;
					}
					return createStatus(ActionMessages.OpenContractHierarchyAction_messages_no_types); 
			}
		} catch (JavaModelException e) {
			return e.getStatus();
		}
		return createStatus(ActionMessages.OpenContractHierarchyAction_messages_no_valid_java_element); 
	}
	
	private static IStatus createStatus(String message) {
		return new Status(IStatus.INFO, C4JActivator.PLUGIN_ID, 0, message, null);
	}	
}

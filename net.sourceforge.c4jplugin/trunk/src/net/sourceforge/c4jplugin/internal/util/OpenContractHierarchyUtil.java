package net.sourceforge.c4jplugin.internal.util;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyViewPart;
import net.sourceforge.c4jplugin.internal.ui.preferences.C4JPreferences;
import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class OpenContractHierarchyUtil {
	
	private OpenContractHierarchyUtil() {
	}

	public static ContractHierarchyViewPart open(IJavaElement element, IWorkbenchWindow window) {
		IJavaElement[] candidates= getCandidates(element);
		if (candidates != null) {
			return open(candidates, window);
		}
		return null;
	}	
	
	public static ContractHierarchyViewPart open(IJavaElement[] candidates, IWorkbenchWindow window) {
		Assert.isTrue(candidates != null && candidates.length != 0);
			
		IJavaElement input= null;
		if (candidates.length > 1) {
			String title= UIMessages.OpenContractHierarchyUtil_selectionDialog_title;  
			String message= UIMessages.OpenContractHierarchyUtil_selectionDialog_message; 
			input= SelectionConverter.selectJavaElement(candidates, window.getShell(), title, message);			
		} else {
			input= candidates[0];
		}
		if (input == null)
			return null;
			
		try {
			if (C4JPreferences.doOpenContractHierarchyInPerspective()) {
				return openInPerspective(window, input);
			} else {
				return openInViewPart(window, input);
			}
				
		} catch (WorkbenchException e) {
			ExceptionHandler.handle(e, window.getShell(),
				UIMessages.OpenContractHierarchyUtil_error_open_perspective, 
				e.getMessage());
		} catch (JavaModelException e) {
			ExceptionHandler.handle(e, window.getShell(),
				UIMessages.OpenContractHierarchyUtil_error_open_editor, 
				e.getMessage());
		}
		return null;
	}

	private static ContractHierarchyViewPart openInViewPart(IWorkbenchWindow window, IJavaElement input) {
		IWorkbenchPage page= window.getActivePage();
		try {
			ContractHierarchyViewPart result= (ContractHierarchyViewPart) page.findView(C4JActivator.ID_CONTRACT_HIERARCHY);
			if (result != null) {
				result.clearNeededRefresh(); // avoid refresh of old hierarchy on 'becomes visible'
			}
			result= (ContractHierarchyViewPart) page.showView(C4JActivator.ID_CONTRACT_HIERARCHY);
			result.setInputElement(input);
			return result;
		} catch (CoreException e) {
			ExceptionHandler.handle(e, window.getShell(), 
				UIMessages.OpenContractHierarchyUtil_error_open_view, e.getMessage()); 
		}
		return null;		
	}
	
	private static ContractHierarchyViewPart openInPerspective(IWorkbenchWindow window, IJavaElement input) throws WorkbenchException, JavaModelException {
		IWorkbench workbench= PlatformUI.getWorkbench();
		// The problem is that the input element can be a working copy. So we first convert it to the original element if
		// it exists.
		IJavaElement perspectiveInput= input;
		
		if (input instanceof IMember) {
			if (input.getElementType() != IJavaElement.TYPE) {
				perspectiveInput= ((IMember)input).getDeclaringType();
			} else {
				perspectiveInput= input;
			}
		}
		IWorkbenchPage page= workbench.showPerspective(JavaUI.ID_HIERARCHYPERSPECTIVE, window, perspectiveInput);
		
		ContractHierarchyViewPart part= (ContractHierarchyViewPart) page.findView(C4JActivator.ID_CONTRACT_HIERARCHY);
		if (part != null) {
			part.clearNeededRefresh(); // avoid refresh of old hierarchy on 'becomes visible'
		}		
		part= (ContractHierarchyViewPart) page.showView(C4JActivator.ID_CONTRACT_HIERARCHY);
		part.setInputElement(input);
		if (input instanceof IMember) {
			if (page.getEditorReferences().length == 0) {
				JavaUI.openInEditor(input); // for 3.3 ==> , false, false); // only open when the perspecive has been created
			}
		}
		return part;
	}


	/**
	 * Converts the input to a possible input candidates
	 */	
	public static IJavaElement[] getCandidates(Object input) {
		if (!(input instanceof IJavaElement)) {
			return null;
		}
		try {
			IJavaElement elem= (IJavaElement) input;
			switch (elem.getElementType()) {
				case IJavaElement.INITIALIZER:
				case IJavaElement.METHOD:
				case IJavaElement.FIELD:
				case IJavaElement.TYPE:
				case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				case IJavaElement.JAVA_PROJECT:
					return new IJavaElement[] { elem };
				case IJavaElement.PACKAGE_FRAGMENT:
					if (((IPackageFragment)elem).containsJavaResources())
						return new IJavaElement[] {elem};
					break;
				case IJavaElement.PACKAGE_DECLARATION:
					return new IJavaElement[] { elem.getAncestor(IJavaElement.PACKAGE_FRAGMENT) };
				case IJavaElement.IMPORT_DECLARATION:	
					IImportDeclaration decl= (IImportDeclaration) elem;
					if (decl.isOnDemand()) {
						IJavaProject jproject = elem.getJavaProject();
						String typeContainerName = Signature.getQualifier(elem.getElementName());
						elem = jproject.findType(typeContainerName);
						if (elem == null) {
							// find it as package
							IPath path= new Path(typeContainerName.replace('.', '/'));
							elem = jproject.findElement(path);
							if (!(elem instanceof IPackageFragment)) {
								elem = null;
							}
						}
					} else {
						elem= elem.getJavaProject().findType(elem.getElementName());
					}
					if (elem == null)
						return null;
					return new IJavaElement[] {elem};
					
				case IJavaElement.CLASS_FILE:
					return new IJavaElement[] { ((IClassFile)input).getType() };				
				case IJavaElement.COMPILATION_UNIT: {
					ICompilationUnit cu= (ICompilationUnit) elem.getAncestor(IJavaElement.COMPILATION_UNIT);
					if (cu != null) {
						IType[] types= cu.getTypes();
						if (types.length > 0) {
							return types;
						}
					}
					break;
				}					
				default:
			}
		} catch (JavaModelException e) {
			C4JActivator.log(e);
		}
		return null;	
	}
}

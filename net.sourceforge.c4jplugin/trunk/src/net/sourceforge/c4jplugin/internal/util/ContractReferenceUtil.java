package net.sourceforge.c4jplugin.internal.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import net.sourceforge.c4jplugin.internal.core.ContractReferenceModel;
import net.sourceforge.c4jplugin.internal.markers.IClassInvariantMarker;
import net.sourceforge.c4jplugin.internal.markers.IContractedClassInvariantMarker;
import net.sourceforge.c4jplugin.internal.markers.IContractedMethodMarker;
import net.sourceforge.c4jplugin.internal.markers.IMethodMarker;
import net.sourceforge.c4jplugin.internal.markers.IProblemMarker;
import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.osgi.util.NLS;

public class ContractReferenceUtil {
	
	static public void deleteMarkers(IProject project) throws CoreException {
		project.deleteMarkers(IClassInvariantMarker.ID, true, IResource.DEPTH_INFINITE);
		project.deleteMarkers(IContractedClassInvariantMarker.ID, true, IResource.DEPTH_INFINITE);
		project.deleteMarkers(IContractedMethodMarker.ID, true, IResource.DEPTH_INFINITE);
		project.deleteMarkers(IMethodMarker.ID, true, IResource.DEPTH_INFINITE);
		project.deleteMarkers(IProblemMarker.ID, true, IResource.DEPTH_INFINITE);
	}
	
	static public void deleteMarkers(IResource resource) throws CoreException{
		resource.deleteMarkers(IClassInvariantMarker.ID, true, IResource.DEPTH_ZERO);
		resource.deleteMarkers(IContractedClassInvariantMarker.ID, true, IResource.DEPTH_ZERO);
		resource.deleteMarkers(IMethodMarker.ID, true, IResource.DEPTH_ZERO);
		resource.deleteMarkers(IContractedMethodMarker.ID, true, IResource.DEPTH_ZERO);
		resource.deleteMarkers(IProblemMarker.ID, true, IResource.DEPTH_ZERO);
	}
	
	
	static public void createContractMarkers(IResource contract) {
		if (!ContractReferenceModel.isContract(contract)) return;
		
		IJavaElement jelement = JavaCore.create(contract);
		try {
			IType type = getType(jelement);
			if (type != null) {
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setSource(type.getCompilationUnit());
				CompilationUnit cu = (CompilationUnit)parser.createAST(null);
				
				// cache all IContractedMethodMarker makers
				Vector<IMarker> methodMarkers = new Vector<IMarker>();
				for (IResource contractedRes : ContractReferenceModel.getContractedClasses(contract)) {
					IMarker[] markers = contractedRes.findMarkers(IContractedMethodMarker.ID, true, IResource.DEPTH_ZERO);
					methodMarkers.addAll(Arrays.asList(markers));
				}
				
				// check every method in the contract if it really enforces a contract
				// on another method in some class or if it is a classInvariant method
				for (IMethod method : type.getMethods()) {
					String name = method.getElementName();
					String contractType = null;
					String methodName = null;
					if (name.startsWith("pre_")) {
						contractType = IMethodMarker.VALUE_PRE_METHOD;
						methodName = name.substring("pre_".length());
					}
					else if (name.startsWith("post_")) {
						contractType = IMethodMarker.VALUE_POST_METHOD;
						methodName = name.substring("post_".length());
					}
					else if (name.equals("classInvariant") && method.getSignature().equals("()V")) {
						IMarker methodMarker = contract.createMarker(IClassInvariantMarker.ID);
						methodMarker.setAttribute(IMarker.LINE_NUMBER, cu.getLineNumber(method.getNameRange().getOffset()));			
						methodMarker.setAttribute(IMethodMarker.ATTR_CONTRACT_TYPE, IMethodMarker.VALUE_CLASS_INVARIANT);
						methodMarker.setAttribute(IMethodMarker.ATTR_HANDLE_IDENTIFIER, method.getHandleIdentifier());
						methodMarker.setAttribute(IMarker.MESSAGE, UIMessages.MarkerMessage_contract_classInvariant);
					}
					
					if (contractType != null) {
						// the contract method is a "pre_" or "post_" method
						// check for methods which are guarded by it
						boolean isContracting = false;
						String methodHandle = null;
						for (IMarker marker : methodMarkers) {
							String markerType = marker.getAttribute(IContractedMethodMarker.ATTR_CONTRACT_TYPE, "");
							if (markerType.equals(contractType) ||
									markerType.equals(IContractedMethodMarker.VALUE_PREPOST_METHOD)) {
								methodHandle = marker.getAttribute(IContractedMethodMarker.ATTR_HANDLE_IDENTIFIER, null);
								
								// check if the found method has the same paramter types
								// and the same name without "pre_" or "post_"
								IMethod contractedMethod = (IMethod)JavaCore.create(methodHandle);
								if (Arrays.equals(contractedMethod.getParameterTypes(), method.getParameterTypes()) &&
										contractedMethod.getElementName().equals(methodName)) {
									// yes, create a marker for the contract method
									IMarker methodMarker = contract.createMarker(IMethodMarker.ID);
									methodMarker.setAttribute(IMethodMarker.ATTR_CONTRACT_TYPE, contractType);
									methodMarker.setAttribute(IMarker.LINE_NUMBER, cu.getLineNumber(method.getNameRange().getOffset()));
									methodMarker.setAttribute(IMethodMarker.ATTR_HANDLE_IDENTIFIER, method.getHandleIdentifier());
									methodMarker.setAttribute(IMarker.MESSAGE, UIMessages.MarkerMessage_contract_methodIsContracting);
									isContracting = true;
									break;
								}
							}
						}
						if (!isContracting) {
							// the method contract is not enforced on any method
							IMarker methodMarker = contract.createMarker(IProblemMarker.ID);
							methodMarker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
							methodMarker.setAttribute(IMarker.CHAR_START, method.getNameRange().getOffset());
							methodMarker.setAttribute(IMarker.CHAR_END, method.getNameRange().getOffset()+method.getNameRange().getLength());
							methodMarker.setAttribute(IMarker.LINE_NUMBER, cu.getLineNumber(method.getNameRange().getOffset()));
							methodMarker.setAttribute(IMarker.MESSAGE, UIMessages.MarkerMessage_problem_methodNotContracting);
						}
					}
				}
			}
		} catch (JavaModelException e) {}
		catch (CoreException e) {}
	}
	
	static public void createContractedClassMarkers(IResource resource) {
		Collection<IResource> references = ContractReferenceModel.getContractReferences(resource);
		if (references == null || references.size() == 0) return;
		
		IJavaElement jelement = JavaCore.create(resource);
		IType type = null;
		try {
			type = getType(jelement);
		
			if (type != null) {
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setSource(type.getCompilationUnit());
				CompilationUnit cu = (CompilationUnit)parser.createAST(null);
				IMarker markerClassInvariant = null;
				int invariantCounter = 0;
				Vector<IType> typeCache = new Vector<IType>();
				
				// checking for class invariants
				String invariantList = "";
				for (IResource reference : references) {
					IJavaElement refElement = JavaCore.create(reference);
					IType refType = getType(refElement);
					typeCache.add(refType);
				
					IMethod methodClassInvariant = refType.getMethod("classInvariant", new String[] {});
					if (methodClassInvariant != null && methodClassInvariant.exists()) {
						invariantCounter++;
						invariantList += " - " + reference.getName() + "\n";
						try {
							if (markerClassInvariant == null) {
								markerClassInvariant = resource.createMarker(IContractedClassInvariantMarker.ID);
								markerClassInvariant.setAttribute(IMarker.LINE_NUMBER, cu.getLineNumber(refType.getNameRange().getOffset()));
							}
						} catch (CoreException e) {}
					}
				}
				if (markerClassInvariant != null) {
					try {
						if (invariantList.length() > 0) invariantList = "\n\n" + invariantList;
						markerClassInvariant.setAttribute(IMarker.MESSAGE, NLS.bind(UIMessages.MarkerMessage_contracted_classInvariant, invariantCounter) + invariantList);
					} catch (CoreException e) {}
				}
				
				// checking for pre and post methods
				for (IMethod method : type.getMethods()) {
					boolean postMethod = false;
					boolean preMethod = false;
					String contractList = "";
					for (IType refType : typeCache) {
						boolean refAdded = false;
						if (refType.getMethod("post_" + method.getElementName(), method.getParameterTypes()).exists()) {
							postMethod = true;
							contractList += " - " + refType.getCompilationUnit().getCorrespondingResource().getName() + "\n";
							refAdded = true;
						}
						if (refType.getMethod("pre_" + method.getElementName(), method.getParameterTypes()).exists()) {
							preMethod = true;
							if (!refAdded) {
								contractList += " - " + refType.getCompilationUnit().getCorrespondingResource().getName() + "\n";
							}
						}
					}
					
					if (contractList.length() > 0)
						contractList = "\n\n" + contractList;
				
					try {
						IMarker marker = null;
						if (postMethod && preMethod) {
							marker = resource.createMarker(IContractedMethodMarker.ID);
							marker.setAttribute(IContractedMethodMarker.ATTR_CONTRACT_TYPE, IContractedMethodMarker.VALUE_PREPOST_METHOD);
							marker.setAttribute(IMarker.MESSAGE, UIMessages.MarkerMessage_contracted_prepostMethod + contractList);
						}
						else if (postMethod) {
							marker = resource.createMarker(IContractedMethodMarker.ID);
							marker.setAttribute(IContractedMethodMarker.ATTR_CONTRACT_TYPE, IContractedMethodMarker.VALUE_POST_METHOD);
							marker.setAttribute(IMarker.MESSAGE, UIMessages.MarkerMessage_contracted_postMethod + contractList);
						}
						else if (preMethod) {
							marker = resource.createMarker(IContractedMethodMarker.ID);
							marker.setAttribute(IContractedMethodMarker.ATTR_CONTRACT_TYPE, IContractedMethodMarker.VALUE_PRE_METHOD);
							marker.setAttribute(IMarker.MESSAGE, UIMessages.MarkerMessage_contracted_preMethod + contractList);
						}
						
						if (marker != null) {
							marker.setAttribute(IMarker.LINE_NUMBER, cu.getLineNumber(method.getNameRange().getOffset()));
							marker.setAttribute(IContractedMethodMarker.ATTR_HANDLE_IDENTIFIER, method.getHandleIdentifier());
						}
					} catch (CoreException e) {}
				}
			}
		} catch (JavaModelException e) {}
	}
	
	static public Collection<IResource> checkResourceForContracts(IResource resource) {
		try {
			Boolean contracted = ContractReferenceModel.isContracted(resource);
			if (contracted != null && contracted == true)
				return ContractReferenceModel.getContractReferences(resource);
			else if (contracted != null && contracted == false)
				return Collections.emptyList();
			
			IJavaElement javaElement = JavaCore.create(resource);
			IType type = getType(javaElement);
			if (type != null) {
				
				Vector<IResource> contractReferences = new Vector<IResource>();
				IResource ref = AnnotationUtil.getContractReference(resource);
				if (ref != null) {
					// we found a class which is directly contracted
					contractReferences.add(ref);
				}
				
				// search all super types for contracts
				IType[] superTypes = type.newSupertypeHierarchy(null).getSupertypes(type);
				for (IType superType : superTypes) {
					IResource superResource = superType.getResource();
					if (superResource == null) continue;
					
					contracted = ContractReferenceModel.isContracted(superResource);
					if (contracted == null) {
						// the supertype has not yet been checked for contracts
						contractReferences.addAll(checkResourceForContracts(superResource));
					}
					else if (contracted == true) {
						contractReferences.addAll(ContractReferenceModel.getContractReferences(superResource));
					}
				}
				
				if (contractReferences.size() > 0) {
					// this type is contracted
					ContractReferenceModel.setContractReferences(resource, contractReferences);
					return contractReferences;
				}
				else {
					ContractReferenceModel.setContractReferences(resource, null);
				}
			}
		} 
		catch (JavaModelException e) {}
		
		return Collections.emptyList();
	}
	
	static public Collection<IResource> checkAllSubtypes(IResource resource, boolean clean) {
		Vector<IResource> resources = new Vector<IResource>();
		try {
			IType type = getType(JavaCore.create(resource));
			if (type == null)
				return Collections.emptyList();
			IType[] subTypes = type.newTypeHierarchy(type.getJavaProject(),
					null).getAllSubtypes(type);
			for (IType subType : subTypes) {
				IResource subResource = subType.getCompilationUnit().getCorrespondingResource();
				Boolean wasContracted = ContractReferenceModel.isContracted(subResource);
				if (clean) {
					deleteMarkers(subResource);
					ContractReferenceModel.clearResource(subResource);
				}
				boolean isContracted = !checkResourceForContracts(subResource).isEmpty();
				if ((wasContracted != null && wasContracted == true) != isContracted) {
					resources.add(subType.getResource());
				}
			}
		}
		catch (JavaModelException e) {}
		catch (CoreException e) {}
		
		return resources;
	}
	
	static public void refreshModel(IProject project, IProgressMonitor monitor, boolean clear) {
		monitor.beginTask(UIMessages.Builder_startRefreshContractModel, 102);
		
		if (clear)
			ContractReferenceModel.clearModel(project);
		
		monitor.worked(1);
		
		int sourceCount = getNumberOfSourceFiles(project);
		monitor.worked(2);
		
		IProgressMonitor submonitor = new SubProgressMonitor(monitor, 50);
		submonitor.beginTask(UIMessages.Builder_checkingContractedClasses, sourceCount);
		IResourceVisitor contractVisitor = new ContractResourceVisitor(submonitor);
		try {
			project.accept(contractVisitor);
		} catch (CoreException e) {
			// TODO error handling in case something went wrong refreshing the model
			e.printStackTrace();
		}
		submonitor.done();
		
		submonitor = new SubProgressMonitor(monitor, 50);
		Collection<IResource> contracts = ContractReferenceModel.getAllContracts();
		submonitor.beginTask(UIMessages.Builder_creatingContractMarkers, contracts.size());
		for (IResource contract : contracts) {
			createContractMarkers(contract);
			submonitor.worked(1);
		}
		submonitor.done();
		
		monitor.done();
		
	}
	
	static public IType getType(IJavaElement element) throws JavaModelException {
        IType type = null;
        if (element instanceof ICompilationUnit) {
            type = ((ICompilationUnit) element).findPrimaryType();
        }
        else if (element instanceof IClassFile) {
            type = ((IClassFile)element).getType();
        }
        else if (element instanceof IType) {
            type = (IType) element;
        }
        else if (element instanceof IMember) {
            type = ((IMember)element).getDeclaringType();
        }
        return type;
    }
	
	static public boolean hasJavaErrors(IType type) {
		try {
			IMarker[] markers = type.getCompilationUnit().getCorrespondingResource().findMarkers(
					IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, 
					IResource.DEPTH_INFINITE);
			if (markers.length > 0) return true;
			return false;
		} 
		catch (JavaModelException e) {
			e.printStackTrace();
		} 
		catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static private int getNumberOfSourceFiles(IProject project) {
		
		CountingResourceVisitor countingResourceVisitor = new CountingResourceVisitor();
		
		try {
			project.accept(countingResourceVisitor);
		} catch (CoreException e) {}
		
		return countingResourceVisitor.counter;
	}
	
	private static class CountingResourceVisitor implements IResourceVisitor {
		int counter = 0;
		
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getName().endsWith(".java")) {
				counter++;
				return false;
			}
			return true;
		}
	}
	
	private static class ContractResourceVisitor implements IResourceVisitor {

		private IProgressMonitor monitor;
		
		public ContractResourceVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}
		
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getName().endsWith(".java")) {
				checkResourceForContracts(resource);
				createContractedClassMarkers(resource);
				monitor.worked(1);
				return false;
			}
			return true;
		}
		
	}
}

package net.sourceforge.c4jplugin.internal.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import org.eclipse.core.runtime.OperationCanceledException;
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
	
	private static boolean DEBUG = false;
	
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
		
		//System.out.println("creating contract markers");
		IJavaElement jelement = JavaCore.create(contract);
		try {
			IType type = getType(jelement);
			if (type != null) {
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setSource(type.getCompilationUnit());
				CompilationUnit cu = (CompilationUnit)parser.createAST(null);
				
				// cache all IContractedMethodMarker makers of the target
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
					if (name.startsWith("pre_")) { //$NON-NLS-1$
						contractType = IMethodMarker.VALUE_PRE_METHOD;
						methodName = name.substring("pre_".length()); //$NON-NLS-1$
					}
					else if (name.startsWith("post_")) { //$NON-NLS-1$
						contractType = IMethodMarker.VALUE_POST_METHOD;
						methodName = name.substring("post_".length()); //$NON-NLS-1$
					}
					else if (name.equals("classInvariant") && method.getSignature().equals("()V")) { //$NON-NLS-1$ //$NON-NLS-2$
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
						// checks all methods in sources which are located inside
						// the current workspace
						for (IMarker marker : methodMarkers) {
							String markerType = marker.getAttribute(IContractedMethodMarker.ATTR_CONTRACT_TYPE, ""); //$NON-NLS-1$
							if (markerType.equals(contractType) ||
									markerType.equals(IContractedMethodMarker.VALUE_PREPOST_METHOD)) {
								methodHandle = marker.getAttribute(IContractedMethodMarker.ATTR_HANDLE_IDENTIFIER, null);
								
								// check if the found method has the same paramter types
								// and the same name without "pre_" or "post_"
								IMethod contractedMethod = (IMethod)JavaCore.create(methodHandle);
								if (Arrays.equals(contractedMethod.getParameterTypes(), method.getParameterTypes()) &&
										contractedMethod.getElementName().equals(methodName)) {
									// yes, create a marker for the contract method
									isContracting = true;
									break;
								}
							}
						}
						
						// try the supertype hierarchy of the contracts target
						boolean bSuperMethod = false;
						if (!isContracting) {
							IResource target = ContractReferenceModel.getTarget(contract);
							if (target != null) {
								IType typeTarget = getType(JavaCore.create(target));
								if (typeTarget != null) {
									IType[] superTypes = typeTarget.newSupertypeHierarchy(null).getAllSupertypes(typeTarget);
									for (IType superType : superTypes) {
										for (IMethod superMethod : superType.getMethods()) {
											if (Arrays.equals(superMethod.getParameterTypes(), method.getParameterTypes()) &&
													superMethod.getElementName().equals(methodName)) {
												isContracting = true;
												bSuperMethod = true;
												break;
											}
										}
										if (isContracting) break;
									}
								}
							}
						}
						
						if (isContracting) {
							IMarker methodMarker = contract.createMarker(IMethodMarker.ID);
							methodMarker.setAttribute(IMethodMarker.ATTR_CONTRACT_TYPE, contractType);
							methodMarker.setAttribute(IMarker.LINE_NUMBER, cu.getLineNumber(method.getNameRange().getOffset()));
							methodMarker.setAttribute(IMethodMarker.ATTR_HANDLE_IDENTIFIER, method.getHandleIdentifier());
							if (bSuperMethod)
								methodMarker.setAttribute(IMarker.MESSAGE, UIMessages.MarkerMessage_contract_methodIsContracting_super);
							else
								methodMarker.setAttribute(IMarker.MESSAGE, UIMessages.MarkerMessage_contract_methodIsContracting);
						}
						else {	
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
		Collection<IResource> references = ContractReferenceModel.getContracts(resource);
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
				String invariantList = ""; //$NON-NLS-1$
				for (IResource reference : references) {
					IJavaElement refElement = JavaCore.create(reference);
					IType refType = getType(refElement);
					typeCache.add(refType);
				
					IMethod methodClassInvariant = refType.getMethod("classInvariant", new String[] {}); //$NON-NLS-1$
					if (methodClassInvariant != null && methodClassInvariant.exists()) {
						invariantCounter++;
						invariantList += " " + reference.getName(); //$NON-NLS-1$
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
						if (invariantList.length() > 0) invariantList = "\n\n" + invariantList; //$NON-NLS-1$
						markerClassInvariant.setAttribute(IMarker.MESSAGE, NLS.bind(UIMessages.MarkerMessage_contracted_classInvariant, invariantCounter) + invariantList);
					} catch (CoreException e) {}
				}
				
				// checking for pre and post methods
				for (IMethod method : type.getMethods()) {
					boolean postMethod = false;
					boolean preMethod = false;
					String contractList = ""; //$NON-NLS-1$
					for (IType refType : typeCache) {
						boolean refAdded = false;
						if (refType.getMethod("post_" + method.getElementName(), method.getParameterTypes()).exists()) { //$NON-NLS-1$
							postMethod = true;
							contractList += " " + refType.getCompilationUnit().getCorrespondingResource().getName(); //$NON-NLS-1$
							refAdded = true;
						}
						if (refType.getMethod("pre_" + method.getElementName(), method.getParameterTypes()).exists()) { //$NON-NLS-1$
							preMethod = true;
							if (!refAdded) {
								contractList += " " + refType.getCompilationUnit().getCorrespondingResource().getName(); //$NON-NLS-1$
							}
						}
					}
					
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
	
	/**
	 * Checks the given resource for contracts. Does nothing if the resource
	 * is already known to the underlying model (remove it first if you want
	 * to force a recheck).
	 * 
	 * @param resource
	 * @return All contracts for the given resource
	 */
	static public Collection<IResource> checkResourceForContracts(IResource resource) {
		try {
			if (DEBUG) System.out.println("Checking for contracts: " + resource.getName());
			Boolean contracted = ContractReferenceModel.isContracted(resource);
			if (contracted != null && contracted == true)
				return ContractReferenceModel.getContracts(resource);
			else if (contracted != null && contracted == false)
				return Collections.emptyList();
			
			
			if (DEBUG) System.out.println("Creating Java Element from resource");
			IJavaElement javaElement = JavaCore.create(resource);
			IType type = getType(javaElement);
			if (type != null) {
				
				if (DEBUG) System.out.println("Trying to get direct contract reference");
				IResource ref = AnnotationUtil.getContractReference(resource);
				if (ref != null) {
					// we found a class which is directly contracted
					if (DEBUG) System.out.println("Found direct contract " + ref.getName());
					ContractReferenceModel.addDirectContract(resource, ref);
				}
				
				if (DEBUG) System.out.println("Searching for super contracts");
				// search all super types for contracts
				Vector<IResource> contractReferences = new Vector<IResource>();
				IType[] superTypes = type.newSupertypeHierarchy(null).getSupertypes(type);
				for (IType superType : superTypes) {
					IResource superResource = superType.getResource();
					if (superResource == null) continue;
					
					if (DEBUG) System.out.println("Checking supertype " + superResource.getName() + " for contracts");
					
					contracted = ContractReferenceModel.isContracted(superResource);
					if (contracted == null) {
						// the supertype has not yet been checked for contracts
						contractReferences.addAll(checkResourceForContracts(superResource));
					}
					else if (contracted == true) {
						contractReferences.addAll(ContractReferenceModel.getContracts(superResource));
					}
				}
				
				if (contractReferences.size() > 0) {
					// this type has supercontracts
					if (DEBUG) System.out.println("Resource " + resource.getName() + " has " + contractReferences.size() + " contracts");
					ContractReferenceModel.addSuperContracts(resource, contractReferences);
				}
				
				if (ref != null) contractReferences.add(ref);
				return contractReferences;
			}
		} 
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Checks all subtypes of <em>resource</em> for contracts.
	 * 
	 * @param resource
	 * @return All subtypes for which contracts have changed
	 */
	static public Collection<IResource> checkAllSubtypes(IResource resource) {
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
				
				deleteMarkers(subResource);
				ContractReferenceModel.removeContractedClass(subResource);
				//ContractReferenceModel.clearSessionProperties(subResource);
				
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
		
		try {
			monitor.beginTask(UIMessages.Builder_startRefreshContractModel, 102);
			
			if (clear)
				ContractReferenceModel.clearModel(project);
			
			monitor.worked(1);
			
			if (monitor.isCanceled()) throw new OperationCanceledException();
			
			int sourceCount = getNumberOfSourceFiles(project);
			monitor.worked(2);
			
			if (monitor.isCanceled()) throw new OperationCanceledException();
			
			IProgressMonitor submonitor = new SubProgressMonitor(monitor, 50);
			ContractResourceVisitor contractVisitor = new ContractResourceVisitor(submonitor);
			try {
				submonitor.beginTask(UIMessages.Builder_checkingContractedClasses, sourceCount);
				project.accept(contractVisitor);
			} catch (CoreException e) {
				// TODO error handling in case something went wrong refreshing the model
				e.printStackTrace();
			}
			finally {
				submonitor.done();
			}
			
			if (submonitor.isCanceled()) throw new OperationCanceledException();
			
			submonitor = new SubProgressMonitor(monitor, 50);
			Collection<IResource> contracts = contractVisitor.getContracts();
			if (DEBUG) System.out.println("found " + contracts.size() + " contracts:");
			try {
				submonitor.beginTask(UIMessages.Builder_creatingContractMarkers, contracts.size());
				for (IResource contract : contracts) {
					if (DEBUG) System.out.println(contract.getName());
					if (submonitor.isCanceled()) throw new OperationCanceledException();
					
					createContractMarkers(contract);
					submonitor.worked(1);
				}
			}
			finally {
				submonitor.done();
			}
		}
		finally {
			monitor.done();
		}
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
			for (IMarker marker : markers) {
				if (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) == IMarker.SEVERITY_ERROR)
					return true;
			}
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
			if (resource.getName().endsWith(".java")) { //$NON-NLS-1$
				counter++;
				return false;
			}
			return true;
		}
	}
	
	private static class ContractResourceVisitor implements IResourceVisitor {

		private IProgressMonitor monitor;
		private Collection<IResource> contracts = new HashSet<IResource>();
		
		public ContractResourceVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}
		
		public Collection<IResource> getContracts() {
			return contracts;
		}
		
		public boolean visit(IResource resource) throws CoreException {
			if (monitor.isCanceled()) throw new OperationCanceledException();
			
			//System.out.println("Checking resource: " + resource.getName());
			if (resource.getName().endsWith(".java")) { //$NON-NLS-1$
				contracts.addAll(checkResourceForContracts(resource));
				createContractedClassMarkers(resource);
				monitor.worked(1);
				return false;
			}
			return true;
		}
		
	}
}

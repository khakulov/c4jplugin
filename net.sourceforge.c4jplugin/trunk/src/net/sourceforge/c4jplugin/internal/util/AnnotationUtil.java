package net.sourceforge.c4jplugin.internal.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

/**
 * @author sascha
 *
 */
/**
 * @author sascha
 *
 */
public class AnnotationUtil {
	
	static public final String PROPERTY_NOT_CONTRACTED = "false";
	static public final String PROPERTY_IS_CONTRACTED = "true";
	
	// persistent property
	static public final QualifiedName QN_CONTRACT_PROPERTY = new QualifiedName(C4JActivator.PLUGIN_ID, "isContracted");
	// session property
	static public final QualifiedName QN_CONTRACT_REFERENCES = new QualifiedName(C4JActivator.PLUGIN_ID, "contractReferences");
	
	static private final String ANNOTATION_CONTRACT_REFERENCE = "ContractReference";
	
	static private final ContractedClassVisitor contractedClassVisitor = new ContractedClassVisitor();
	
	/**
	 * Determines if the java element contains a type with a specific annotation.
     * <p>
     * The syntax for the property tester is of the form: qualified or unqualified annotation name
     * <li>qualified or unqualified annotation name, required. For example,
     *  <code>org.junit.JUnit</code>.</li>
     * </ol>
	 * @param element the element to check for the method 
	 * @param annotationName the qualified or unqualified name of the annotation to look for
	 * @return true if the type is found in the element, false otherwise
	 */
	static private String getContractReference(IJavaElement element) {
		try {
			IType type = getType(element);
			return getContractReferenceHandle(type);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	static private String getContractReferenceHandle(IType type) {
		try {			
			if (type == null || !type.exists()) {
				return null;
			}
			
			IBuffer buffer= null;
			IOpenable openable= type.getOpenable();
			if (openable instanceof ICompilationUnit) {
				buffer= ((ICompilationUnit) openable).getBuffer();
			} else if (openable instanceof IClassFile) {
				buffer= ((IClassFile) openable).getBuffer();
			}
			if (buffer == null) return null;
			
			ISourceRange sourceRange= type.getSourceRange();
			ISourceRange nameRange= type.getNameRange();
			if (sourceRange != null && nameRange != null) {
				IScanner scanner= ToolFactory.createScanner(false, false, true, false);
				scanner.setSource(buffer.getCharacters());
				scanner.resetTo(sourceRange.getOffset(), nameRange.getOffset());
				String arg = findAnnotation(scanner, ANNOTATION_CONTRACT_REFERENCE);
				if (arg == null || arg.length() == 0) return null;
				
				String[][] matches = type.resolveType(arg);
				if (matches == null || matches.length > 1) {
					// the contractValue cannot be resolved or is ambiguous
					return null;
				}
				
				IType resolvedType = type.getJavaProject().findType(matches[0][0], matches[0][1]);
				// check if the resolved resource has java errors
				if (hasJavaErrors(resolvedType))
					return null;
				
				// resolved type has no java errors
				return resolvedType.getHandleIdentifier();
			}
		} 
		catch (JavaModelException e) {}
		catch (InvalidInputException e) {}
		
		return null;
	}
	
	static public void cleanResource(IResource resource) throws CoreException {
		resource.setPersistentProperty(QN_CONTRACT_PROPERTY, null);
		resource.setSessionProperty(QN_CONTRACT_REFERENCES, null);
	}
	
	
	/**
	 * @param resource
	 * @return true if the contract status has changed
	 */
	static public boolean checkResourceForContracts(IResource resource) {
		IJavaElement javaElement = JavaCore.create(resource);
		return checkResourceForContracts(javaElement);
	}
	
	static private boolean checkResourceForContracts(IJavaElement javaElement) {
		try {
			if (javaElement != null) {
				System.out.print("checking contract of: " + javaElement.getElementName() + "... ");
				
				IResource resource = javaElement.getResource();
				IType type = AnnotationUtil.getType(javaElement);
				if (type != null) {
					Vector<String> contractReferences = new Vector<String>();
					String ref = getContractReferenceHandle(type);
					//System.out.print("[" + ref + "]");
					if (ref != null) {
						// we found a class which is directly contracted
						contractReferences.add(ref);
					}
					
					// search all super types for contracts
					IType[] superTypes = type.newSupertypeHierarchy(null).getSupertypes(type);
					for (IType superType : superTypes) {
						System.out.print("checking supertype: " + superType.getElementName());
						IResource superResource = superType.getResource();
						if (superResource == null) continue;
						
						String contracted = superResource.getPersistentProperty(QN_CONTRACT_PROPERTY);
						boolean superTypeIsContracted = false;
						if (contracted == null) {
							// the supertype has not yet been checked for contracts
							superTypeIsContracted = checkResourceForContracts(superType);
						}
						else if (contracted.equals(PROPERTY_IS_CONTRACTED)) {
							superTypeIsContracted = true;
							if (superResource.getSessionProperty(QN_CONTRACT_REFERENCES) == null) {
								// the session property has not yet been set
								superTypeIsContracted = checkResourceForContracts(superType);
							}
						}
						
						if (superTypeIsContracted) {
							// the supertype is contracted, add its contract reference handle
							String[] superRefs = (String[])superType.getResource().getSessionProperty(QN_CONTRACT_REFERENCES);
							for (String superRef : superRefs)
								contractReferences.add(superRef);
						}
					}
					
					if (contractReferences.size() > 0) {
						// this type is contracted
						resource.setPersistentProperty(QN_CONTRACT_PROPERTY, PROPERTY_IS_CONTRACTED);
						resource.setSessionProperty(QN_CONTRACT_REFERENCES, contractReferences.toArray(new String[] {}));
						System.out.println("TRUE");
					}
					else {
						resource.setPersistentProperty(QN_CONTRACT_PROPERTY, PROPERTY_NOT_CONTRACTED);
						resource.setSessionProperty(QN_CONTRACT_REFERENCES, null);
						System.out.println("FALSE");
					}
						
				}
				else {
					resource.setPersistentProperty(QN_CONTRACT_PROPERTY, PROPERTY_NOT_CONTRACTED);
					resource.setSessionProperty(QN_CONTRACT_REFERENCES, null);
					System.out.println("FALSE");
				}
				
				String isContracted = resource.getPersistentProperty(QN_CONTRACT_PROPERTY);
				if (isContracted == null || isContracted.equals(PROPERTY_NOT_CONTRACTED)) {
					return false;
				}
				else return true;
			}
		} 
		catch (JavaModelException e) {}
		catch (CoreException e) {}
		
		return false;
	}
	
	static public Collection<IResource> checkAllSubtypes(IResource resource) {
		Vector<IResource> resources = new Vector<IResource>();
		try {
			IType type = getType(JavaCore.create(resource));
			if (type == null)
				return Collections.emptyList();
			IType[] subTypes = type.newTypeHierarchy(type.getJavaProject(),
					null).getAllSubtypes(type);
			for (IType subType : subTypes) {
				checkResourceForContracts(subType);
				resources.add(subType.getResource());
			}
			return resources;
		} catch (JavaModelException e) {
			return resources;
		} 
		
	}
	
	static public IResource[] getResourcesContractedBy(IProject project, String handleIdentifier) {
		try {
			contractedClassVisitor.setContractHandle(handleIdentifier);
			project.accept(contractedClassVisitor);
			return contractedClassVisitor.getContractedResources();
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
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
	
	static private String findAnnotation(IScanner scanner, String annotationName) throws InvalidInputException {
		String simpleName= Signature.getSimpleName(annotationName);
		StringBuffer buf= new StringBuffer();
		int tok= scanner.getNextToken();
		while (tok != ITerminalSymbols.TokenNameEOF) {
			if (tok == ITerminalSymbols.TokenNameAT) {
				buf.setLength(0);
				tok= readName(scanner, buf);
				String name= buf.toString();
				if (name.equals(annotationName) || name.equals(simpleName) || name.endsWith('.' + simpleName)) {
					StringBuffer bufArg = new StringBuffer();
					bufArg.setLength(0);
					readArgument(scanner, bufArg);
					return bufArg.toString();
				}
			} else {
				tok= scanner.getNextToken();
			}
		}
		return null;
	}
	
	static private int readName(IScanner scanner, StringBuffer buf) throws InvalidInputException {
		int tok= scanner.getNextToken();
		while (tok == ITerminalSymbols.TokenNameIdentifier) {
			buf.append(scanner.getCurrentTokenSource());
			tok= scanner.getNextToken();
			if (tok != ITerminalSymbols.TokenNameDOT) {
				return tok;
			}
			buf.append('.');
			tok= scanner.getNextToken();
		}
		return tok;
	}
	
	static private int readArgument(IScanner scanner, StringBuffer buf) throws InvalidInputException {
		int tok = scanner.getNextToken();
		while (tok != ITerminalSymbols.TokenNameRBRACE) {
			if (tok == ITerminalSymbols.TokenNameStringLiteral) {
				char[] literal = scanner.getCurrentTokenSource();
				buf.append(literal, 1, literal.length-2);
				return tok;
			}
			tok = scanner.getNextToken();
		}
		return tok;
	}
	
	static private class ContractedClassVisitor implements IResourceVisitor {
		
		private String contractHandle;
		private Vector<IResource> contractedResources = new Vector<IResource>();
		
		public void setContractHandle(String contractHandle) {
			this.contractHandle = contractHandle;
			contractedResources.clear();
		}
		
		public IResource[] getContractedResources() {
			return contractedResources.toArray(new IResource[] {});
		}
		
		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile && resource.getName().endsWith(".java")) {
				String[] contractHandles = (String[])resource.getSessionProperty(AnnotationUtil.QN_CONTRACT_REFERENCES);
				if (contractHandles != null) {
					for (String handle : contractHandles) {
						if (handle.equals(contractHandle)) {
							contractedResources.add(resource);
						}
					}
				}
			}
			return true;
		}
		
	}
}

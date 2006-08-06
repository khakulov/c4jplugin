package net.sourceforge.c4jplugin.internal.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
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

public class AnnotationUtil {
	
	static public final int PROPERTY_NOT_CONTRACTED = 0;
	static public final int PROPERTY_IS_CONTRACTED = 1;
	
	static public final QualifiedName QN_CONTRACT_PROPERTY = new QualifiedName("net.sourceforge.c4jplugin", "contracted");
	
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
	static public boolean hasContractReference(IJavaElement element, String annotationType) {
		try {
			IType type = getType(element);
			return hasContractReference(type, annotationType);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	static public boolean hasContractReference(IType type, String annotationType) {
		try {			
			if (type == null || !type.exists()) {
				return false;
			}
			
			IBuffer buffer= null;
			IOpenable openable= type.getOpenable();
			if (openable instanceof ICompilationUnit) {
				buffer= ((ICompilationUnit) openable).getBuffer();
			} else if (openable instanceof IClassFile) {
				buffer= ((IClassFile) openable).getBuffer();
			}
			if (buffer == null) return false;
			
			ISourceRange sourceRange= type.getSourceRange();
			ISourceRange nameRange= type.getNameRange();
			if (sourceRange != null && nameRange != null) {
				IScanner scanner= ToolFactory.createScanner(false, false, true, false);
				scanner.setSource(buffer.getCharacters());
				scanner.resetTo(sourceRange.getOffset(), nameRange.getOffset());
				String arg = findAnnotation(scanner, annotationType);
				if (arg == null || arg.length() == 0) return false;
				
				boolean resolved = true;
				String[][] matches = type.resolveType(arg);
				if (matches == null) {
					// could not resolve the contractValue as a type
					System.out.println("Could not resolve contract " + arg + " in: " + type.getElementName());
					resolved = false;
				}
				else if (matches.length > 1) {
					// the contractValue is ambigious
					System.out.println("Ambigouties of " + arg + " in: " + type.getElementName());
					resolved = false;
				}
				
				if (resolved) return true;
				return false;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (InvalidInputException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static public boolean checkContract(IResource resource) {
		IJavaElement javaElement = JavaCore.create(resource);
		return checkContract(javaElement);
	}
	
	static public boolean checkContract(IJavaElement javaElement) {
		try {
			if (javaElement != null) {
				System.out.print("checking contract of: " + javaElement.getElementName() + "... ");
				
				IResource resource = javaElement.getResource();
				IType type = AnnotationUtil.getType(javaElement);
				if (type != null) {
					if (AnnotationUtil.hasContractReference(type, "ContractReference")) {
						// we found a class which is directly contracted
						resource.setSessionProperty(QN_CONTRACT_PROPERTY, PROPERTY_IS_CONTRACTED);
						System.out.println("TRUE");
						return true;
					}
					else {
						// search all super types for contracts
						IType[] superTypes = type.newSupertypeHierarchy(null).getAllSupertypes(type);
						for (IType superType : superTypes) {
							IResource superResource = superType.getResource();
							if (superResource == null) continue;
							Integer contracted = (Integer)superResource.getSessionProperty(QN_CONTRACT_PROPERTY);
							if (contracted == null) {
								// the supertype has not yet been checked for contracts
								if (AnnotationUtil.hasContractReference(superType, "ContractReference")) {
									resource.setSessionProperty(QN_CONTRACT_PROPERTY, PROPERTY_IS_CONTRACTED);
									System.out.println("TRUE");
									return true;
								}
							}
							else if (contracted == AnnotationUtil.PROPERTY_IS_CONTRACTED) {
								// the supertype is contracted => this type too
								resource.setSessionProperty(QN_CONTRACT_PROPERTY, PROPERTY_IS_CONTRACTED);
								System.out.println("TRUE");
								return true;
							}
						}
					}
				}
				resource.setSessionProperty(QN_CONTRACT_PROPERTY, PROPERTY_NOT_CONTRACTED);
				System.out.println("FALSE");
			}
		} 
		catch (JavaModelException e) {}
		catch (CoreException e) {}
		
		return false;
	}
	
	static public IType getType(IJavaElement element) throws JavaModelException {
        IType type = null;
        if (element instanceof ICompilationUnit) {
            type= ((ICompilationUnit) element).findPrimaryType();
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
}

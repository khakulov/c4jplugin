package net.sourceforge.c4jplugin.internal.util;

import java.util.HashSet;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

public class AnnotationUtil {
	
	static private HashSet<String> listUnresolvedReferences = new HashSet<String>();
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
	static public String getContractReferenceFromJE(IJavaElement element, String annotationType) {
		try {
			IType type = getType(element);
			return getContractReference(type, annotationType);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	static public String getContractReference(IType type, String annotationType) {
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
			if (buffer == null) {
				return null;
			}
			
			ISourceRange sourceRange= type.getSourceRange();
			ISourceRange nameRange= type.getNameRange();
			if (sourceRange != null && nameRange != null) {
				IScanner scanner= ToolFactory.createScanner(false, false, true, false);
				scanner.setSource(buffer.getCharacters());
				scanner.resetTo(sourceRange.getOffset(), nameRange.getOffset());
				String arg = findAnnotation(scanner, annotationType);
				if (arg == null || arg.length() == 0) return null;
				return arg;
				//String[][] strFullArg = type.resolveType(arg);
				//if (strFullArg == null) {
				//	System.out.println("could not resolve argument " + arg);
				//	return null;
				//}
				//for (int i = 0; i < strFullArg.length; i++) {
				//	for (int j = 0; j < strFullArg[i].length; j++) {
				//		System.out.println("[" + i + "][" + j + "] " + strFullArg[i][j]);
				//	}
				//}
				//return strFullArg[0][0] + "." + strFullArg[0][1];
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (InvalidInputException e) {
			e.printStackTrace();
		}
		return null;
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
	
	static public void addUnresolvedContract(String target) {
		System.out.println(target + "has unresolved contract references");
		listUnresolvedReferences.add(target);
	}
	
	static public void removeUnresolvedContract(String target) {
		System.out.println(target + "has NO unresolved contract references");
		listUnresolvedReferences.remove(target);
	}
	
	static public boolean hasUnresolvedContract(String target) {
		System.out.println(target + " has unresolved contracts: " + listUnresolvedReferences.contains(target));
		return listUnresolvedReferences.contains(target);
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
				buf.append(scanner.getCurrentTokenSource());
				//System.out.println("[readArgument] " + buf.toString());
				return tok;
			}
			tok = scanner.getNextToken();
		}
		return tok;
	}
}

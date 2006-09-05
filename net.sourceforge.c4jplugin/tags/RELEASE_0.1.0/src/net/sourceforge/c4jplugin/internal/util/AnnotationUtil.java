package net.sourceforge.c4jplugin.internal.util;


import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
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
		
	static private final String ANNOTATION_CONTRACT_REFERENCE = "ContractReference";
	
	static public IResource getContractReference(IResource resource) {
		try {
			IJavaElement jelement = JavaCore.create(resource);
			IType type = ContractReferenceUtil.getType(jelement);
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
				if (ContractReferenceUtil.hasJavaErrors(resolvedType))
					return null;
				
				// resolved type has no java errors
				return resolvedType.getCompilationUnit().getCorrespondingResource();
			}
		} 
		catch (JavaModelException e) {}
		catch (InvalidInputException e) {}
		
		return null;
	}
	
	
	
	
	
	/**
	 * @param resource
	 * @return true if the contract status has changed
	 */
	
	
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

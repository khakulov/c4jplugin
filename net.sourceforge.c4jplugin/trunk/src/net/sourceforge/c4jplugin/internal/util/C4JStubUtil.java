package net.sourceforge.c4jplugin.internal.util;

import net.sourceforge.c4jplugin.internal.wizards.NewContractLabelProvider;
import net.sourceforge.c4jplugin.internal.wizards.WizardMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class C4JStubUtil {
	public static GenStubSettings getCodeGenerationSettings(IJavaProject project) {
		return new GenStubSettings(project);
	}

	public static class GenStubSettings {
		public boolean callSuper;
		public boolean methodOverwrites;
		public boolean noBody;
		
		public boolean taskTag;
		public boolean finalize;
		
		public int preCondition = -1;
		public int postCondition = -1;
		
		public boolean createComments;
		public boolean useKeywordThis;
					
		public final int tabWidth;
		
		public GenStubSettings(IJavaProject project) {
			this.createComments= Boolean.valueOf(PreferenceConstants.getPreference(PreferenceConstants.CODEGEN_ADD_COMMENTS, project)).booleanValue();
			this.useKeywordThis= Boolean.valueOf(PreferenceConstants.getPreference(PreferenceConstants.CODEGEN_KEYWORD_THIS, project)).booleanValue();
			this.tabWidth= IndentManipulation.getTabWidth(project.getOptions(true));
		}
	}	

	public static String formatCompilationUnit(IJavaProject project, String sourceString, String lineDelim) {
		return codeFormat(project, sourceString, CodeFormatter.K_COMPILATION_UNIT, 0, lineDelim);
	}
	
	
	public static String codeFormat(IJavaProject project, String sourceString, int kind, int initialIndentationLevel, String lineDelim) {
		CodeFormatter formatter= ToolFactory.createCodeFormatter(project.getOptions(true));
		TextEdit edit= formatter.format(kind, sourceString, 0, sourceString.length(), initialIndentationLevel, lineDelim);
		if (edit != null) {
			Document doc= new Document(sourceString);
			try {
				edit.apply(doc);
				return doc.get();
			} catch (MalformedTreeException e) {
			} catch (BadLocationException e) {
			}
		}
		return sourceString;
	}

	/**
	 * Generates a stub. Given a template method, a stub with the same parameter signature
	 * and name (appended with "pre_" or "post_" according to the settings)
	 * will be constructed so it can be added to a type.
	 * @param destTypeName The name of the type to which the method will be added to (Used for the constructor)
	 * @param method A method template (method belongs to different type than the parent)
	 * @param settings Options as defined above (GENSTUB_*)
	 * @param imports Imports required by the sub are added to the imports structure
	 * @return The unformatted stub
	 * @throws JavaModelException
	 */
	public static String genStub(ICompilationUnit compilationUnit, String destTypeName, IMethod method, GenStubSettings settings, ImportsManager imports) throws CoreException {
		IType declaringtype= method.getDeclaringType();
		StringBuffer buf= new StringBuffer();
		String[] paramTypes= method.getParameterTypes();
		String[] paramNames= method.getParameterNames();
		
		int lastParam= paramTypes.length -1;		
		
		if (settings.createComments) {
			if (!method.isConstructor()) {
				boolean precond = true;
				if (settings.postCondition > 0) precond = false;
				appendMethodComment(buf, method, precond);
			}
		}
		
		
		if (settings.finalize) buf.append("final "); //$NON-NLS-1$
		buf.append("public void "); //$NON-NLS-1$
		
		if (settings.preCondition >= 0) buf.append("pre_"); //$NON-NLS-1$
		else if (settings.postCondition >= 0) buf.append("post_"); //$NON-NLS-1$
		
		buf.append(method.getElementName());
		
		buf.append('(');
		for (int i= 0; i <= lastParam; i++) {
			String paramTypeSig= paramTypes[i];
			String paramTypeFrm= Signature.toString(paramTypeSig);
			if (!isBuiltInType(paramTypeSig)) {
				resolveAndAdd(paramTypeSig, declaringtype, imports);
			}
			buf.append(Signature.getSimpleName(paramTypeFrm));
			buf.append(' ');
			buf.append(paramNames[i]);
			if (i < lastParam) {
				buf.append(", "); //$NON-NLS-1$
			}
		}
		buf.append(')');
		
		if (settings.noBody) {
			buf.append(";\n\n"); //$NON-NLS-1$
		} else {
			buf.append(" {\n\t"); //$NON-NLS-1$
			if (settings.callSuper && method.isConstructor()) {
				buf.append('\t');
				
				buf.append("super"); //$NON-NLS-1$
				
				buf.append('(');			
				for (int i= 0; i <= lastParam; i++) {
					buf.append(paramNames[i]);
					if (i < lastParam) {
						buf.append(", "); //$NON-NLS-1$
					}
				}
				buf.append(");\n\t"); //$NON-NLS-1$
			}
			
			// PRE condition code
			if (settings.preCondition > 1) {
				if (settings.preCondition == NewContractLabelProvider.PRE_COND_NONNULL) {
					for (int i = 0; i < paramTypes.length; i++) {
						String paramTypeSig = paramTypes[i];
						if (!isBuiltInType(paramTypeSig)) {
							buf.append("\tassert "); //$NON-NLS-1$
							buf.append(paramNames[i]);
							buf.append(" != null;\n\t"); //$NON-NLS-1$
						}
					}
				}
				else if (settings.preCondition == NewContractLabelProvider.PRE_COND_NONEMPTY) {
					for (int i = 0; i < paramTypes.length; i++) {
						String paramTypeSig = paramTypes[i];
						if (!isBuiltInType(paramTypeSig)) {
							buf.append("\tassert "); //$NON-NLS-1$
							buf.append(paramNames[i]);
							buf.append(" != null"); //$NON-NLS-1$
							
							String simpleTypeName = Signature.getSimpleName(Signature.toString(paramTypeSig));
							if ("String".equals(simpleTypeName)) { //$NON-NLS-1$
								buf.append(" && ").append(paramNames[i]).append(".length() > 0"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
							}
							
							buf.append(";\n\t"); //$NON-NLS-1$
						}
					}
				}
			}
			// POST condition code
			else if (settings.postCondition > 1) {
				if (settings.postCondition == NewContractLabelProvider.POST_COND_NONNULL) {
					if (!isBuiltInType(method.getReturnType()))
						buf.append("\tassert getReturnValue() != null;\n\t"); //$NON-NLS-1$
				}
				else if (settings.postCondition == NewContractLabelProvider.POST_COND_NONEMPTY) {
					String returnType = method.getReturnType();
					if (!isBuiltInType(returnType)) {
						if ("String".equals(Signature.getSimpleName(Signature.toString(returnType)))) { //$NON-NLS-1$
							buf.append("\tString strReturnValue = (String)getReturnValue();\n\t"); //$NON-NLS-1$
							buf.append("\tassert strReturnValue != null && strReturnValue.length() > 0;\n\t"); //$NON-NLS-1$
						}
						else
							buf.append("\tassert getReturnValue() != null;\n\t"); //$NON-NLS-1$
					}
				}
			}
			else if (settings.taskTag) {
				String taskTag = getTodoTaskTag(compilationUnit.getJavaProject());
				if (taskTag != null) {
					buf.append("\t// ").append(taskTag).append("\n\t"); //$NON-NLS-1$ $NON-NLS-2$
				}
			}
			buf.append("}\n\n");			 //$NON-NLS-1$
		}
		return buf.toString();
	}
	
	private static void appendMethodComment(StringBuffer buffer, IMethod method, boolean precond) throws JavaModelException {
		final String delimiter= "\n"; //$NON-NLS-1$
		final StringBuffer buf= new StringBuffer("{@link "); //$NON-NLS-1$	
		JavaElementLabels.getTypeLabel(method.getDeclaringType(), JavaElementLabels.T_FULLY_QUALIFIED, buf);
		buf.append('#');
		buf.append(method.getElementName());
		buf.append('(');
		String[] paramTypes= C4JStubUtil.getParameterTypeNamesForSeeTag(method);
		for (int i= 0; i < paramTypes.length; i++) {
			if (i != 0) {
				buf.append(", "); //$NON-NLS-1$
			}
			buf.append(paramTypes[i]);
			
		}
		buf.append(')');
		buf.append('}');
		
		buffer.append("/**");//$NON-NLS-1$
		buffer.append(delimiter);
		buffer.append(" * ");//$NON-NLS-1$
		if (precond)
			buffer.append(NLS.bind(WizardMessages.NewContractWizardPageOne_comment_class_to_contract_pre, buf.toString()));
		else
			buffer.append(NLS.bind(WizardMessages.NewContractWizardPageOne_comment_class_to_contract_post, buf.toString()));
		buffer.append(delimiter);
		buffer.append(" */");//$NON-NLS-1$
		buffer.append(delimiter);
	}
	
	public static boolean isBuiltInType(String typeName) {
		char first= Signature.getElementType(typeName).charAt(0);
		return (first != Signature.C_RESOLVED && first != Signature.C_UNRESOLVED);
	}

	private static void resolveAndAdd(String refTypeSig, IType declaringType, ImportsManager imports) throws JavaModelException {
		String resolvedTypeName= JavaModelUtil.getResolvedTypeName(refTypeSig, declaringType);
		if (resolvedTypeName != null) {
			imports.addImport(resolvedTypeName);		
		}
	}
	
	public static String getTodoTaskTag(IJavaProject project) {
		String markers= null;
		if (project == null) {
			markers= JavaCore.getOption(JavaCore.COMPILER_TASK_TAGS);
		} else {
			markers= project.getOption(JavaCore.COMPILER_TASK_TAGS, true);
		}
		
		if (markers != null && markers.length() > 0) {
			int idx= markers.indexOf(',');
			if (idx == -1) {
				return markers;
			}
			return markers.substring(0, idx);
		}
		return null;
	}

	/*
	 * Evaluates if a member (possible from another package) is visible from
	 * elements in a package.
	 */
	public static boolean isVisible(IMember member, IPackageFragment pack) throws JavaModelException {
		
		int type= member.getElementType();
		if  (type == IJavaElement.INITIALIZER ||  (type == IJavaElement.METHOD && member.getElementName().startsWith("<"))) { //$NON-NLS-1$
			return false;
		}
		
		int otherflags= member.getFlags();
		IType declaringType= member.getDeclaringType();
		if (Flags.isPublic(otherflags) || (declaringType != null && declaringType.isInterface())) {
			return true;
		} else if (Flags.isPrivate(otherflags)) {
			return false;
		}		
		
		IPackageFragment otherpack= (IPackageFragment) member.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		return (pack != null && otherpack != null && pack.getElementName().equals(otherpack.getElementName()));
	}
	
	private static boolean isVersionLessThan(String version1, String version2) {
		return version1.compareTo(version2) < 0;
	}
	
	public static boolean is50OrHigher(IJavaProject project) {
		return !isVersionLessThan(project.getOption(JavaCore.COMPILER_COMPLIANCE, true), JavaCore.VERSION_1_5);
	}
	
	public static String[] getParameterTypeNamesForSeeTag(IMethod overridden) {
		try {
			ASTParser parser= ASTParser.newParser(AST.JLS3);
			parser.setProject(overridden.getJavaProject());
			IBinding[] bindings= parser.createBindings(new IJavaElement[] { overridden }, null);
			if (bindings.length == 1 && bindings[0] instanceof IMethodBinding) {
				return getParameterTypeNamesForSeeTag((IMethodBinding) bindings[0]);
			}
		} catch (IllegalStateException e) {
			// method does not exist
		}
		// fall back code. Not good for generic methods!
		String[] paramTypes= overridden.getParameterTypes();
		String[] paramTypeNames= new String[paramTypes.length];
		for (int i= 0; i < paramTypes.length; i++) {
			paramTypeNames[i]= Signature.toString(Signature.getTypeErasure(paramTypes[i]));
		}
		return paramTypeNames;
	}
	
	private static String[] getParameterTypeNamesForSeeTag(IMethodBinding binding) {
		ITypeBinding[] typeBindings= binding.getParameterTypes();
		String[] result= new String[typeBindings.length];
		for (int i= 0; i < result.length; i++) {
			ITypeBinding curr= typeBindings[i];
			if (curr.isTypeVariable()) {
				curr= curr.getErasure(); // in Javadoc only use type variable erasure
			}
			curr= curr.getTypeDeclaration(); // no parameterized types
			result[i]= curr.getQualifiedName();
		}
		return result;
	}

}

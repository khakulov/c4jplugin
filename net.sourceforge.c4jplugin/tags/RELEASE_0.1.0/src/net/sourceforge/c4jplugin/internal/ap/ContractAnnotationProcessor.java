package net.sourceforge.c4jplugin.internal.ap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sourceforge.c4j.ContractReference;
import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;
import net.sourceforge.c4jplugin.internal.util.ContractReferenceUtil;

import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.osgi.util.NLS;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.Declaration;

public class ContractAnnotationProcessor implements AnnotationProcessor {

	private AnnotationProcessorEnvironment env;
	
	public ContractAnnotationProcessor(AnnotationProcessorEnvironment env) {
		this.env = env;
	}
	
	public void process() {
		
		// obtain the declaration of the annotation we want to process
		AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration)env.getTypeDeclaration(ContractReference.class.getName());
		
		// get the annotated types
		Collection<Declaration> annotatedTypes = env.getDeclarationsAnnotatedWith(annoDecl);
		
		for (Declaration decl : annotatedTypes) {
			Collection<AnnotationMirror> mirrors = decl.getAnnotationMirrors();
			
			// for each annotation found, get a map of element name/value pairs
			for (AnnotationMirror mirror : mirrors) {
				Map<AnnotationTypeElementDeclaration, AnnotationValue> valueMap = mirror.getElementValues();
				Set<Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue>> valueSet = valueMap.entrySet();
				
				for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> annoKeyValue : valueSet) {					
					AnnotationValue annoValue = annoKeyValue.getValue();	// get the name
					if (annoKeyValue.getKey().getSimpleName().equals("contractClassName")) {
						Object contractValue = annoValue.getValue();			// get the value
						if (contractValue instanceof String) {
							// check if the contractValue represents a valid class-name
							CompilationUnit compilationUnit = getEclipseAPE().getAST();
							IJavaElement javaElement = compilationUnit.getJavaElement();
							try {
								IType type = ContractReferenceUtil.getType(javaElement);
								String[][] matches = type.resolveType((String)contractValue);
								
								if (matches == null) {
									// could not resolve the contractValue as a type
									env.getMessager().printError(annoValue.getPosition(), NLS.bind(UIMessages.AnnotationProcessor_error_contractNotFound, contractValue));
								}
								else if (matches.length > 1) {
									// the contractValue is ambiguous
									env.getMessager().printError(annoValue.getPosition(), NLS.bind(UIMessages.AnnotationProcessor_error_contractAmbiguous, contractValue));
								}
								else {
									// check if the resolved contract has compilation errors
									IType resolvedType = type.getJavaProject().findType(matches[0][0], matches[0][1]);									
									if (ContractReferenceUtil.hasJavaErrors(resolvedType))
										env.getMessager().printWarning(annoValue.getPosition(), NLS.bind(UIMessages.AnnotationProcessor_warning_contractHasErrors, contractValue));
								}
							} catch (JavaModelException e) {}
						}
					}
				}
			}
		}
	}
	
	private EclipseAnnotationProcessorEnvironment getEclipseAPE() {
		return (EclipseAnnotationProcessorEnvironment)env;
	}
	
}

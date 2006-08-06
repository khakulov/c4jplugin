package net.sourceforge.c4jplugin.internal.ap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sourceforge.c4j.ContractReference;
import net.sourceforge.c4jplugin.internal.util.AnnotationUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

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
								IType type = AnnotationUtil.getType(javaElement);
								String[][] matches = type.resolveType((String)contractValue);
								boolean resolved = true;
								if (matches == null) {
									// could not resolve the contractValue as a type
									env.getMessager().printError(annoValue.getPosition(), "\"" + contractValue + "\" cannot be resolved");
									resolved = false;
								}
								else if (matches.length > 1) {
									// the contractValue is ambigious
									env.getMessager().printError(annoValue.getPosition(), "\"" + contractValue + "\" cannot uniquely be resolved");
									resolved = false;
								}
								
								if (resolved) {
									type.getResource().setSessionProperty(AnnotationUtil.QN_CONTRACT_PROPERTY, AnnotationUtil.PROPERTY_IS_CONTRACTED);
								}
								else {
									type.getResource().setSessionProperty(AnnotationUtil.QN_CONTRACT_PROPERTY, null);
									
								}
								
							} catch (JavaModelException e) {
								e.printStackTrace();
							} catch (CoreException e) {
								e.printStackTrace();
							}
							
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

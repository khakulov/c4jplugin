package net.sourceforge.c4jplugin.internal.ap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sourceforge.c4j.ContractReference;
import net.sourceforge.c4jplugin.internal.util.AnnotationUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.env.Phase;
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
							CompilationUnit compilationUnit = getEclipseAPE().getAST();
							IJavaElement javaElement = compilationUnit.getJavaElement();
							try {
								IType type = AnnotationUtil.getType(javaElement);
								String[][] matches = type.resolveType((String)contractValue);
								if (matches == null) {
									env.getMessager().printError(annoValue.getPosition(), "\"" + contractValue + "\" cannot be resolved");
									AnnotationUtil.addUnresolvedContract(type.getFullyQualifiedName());
									return;
								}
								
								if (matches.length > 1) {
									env.getMessager().printError(annoValue.getPosition(), "\"" + contractValue + "\" cannot uniquely be resolved");
									AnnotationUtil.addUnresolvedContract(type.getFullyQualifiedName());
									return;
								}
								
								if (getEclipseAPE().getPhase() == Phase.BUILD) {
									AnnotationUtil.removeUnresolvedContract(type.getFullyQualifiedName());
									IType[] subTypes = type.newTypeHierarchy(javaElement.getJavaProject(), null).getAllSubtypes(type);
									for (IType subType : subTypes) {
										try {
											subType.getResource().getParent().refreshLocal(2, null);
										} catch (CoreException e) {
											e.printStackTrace();
										}
									}
								}
							} catch (JavaModelException e) {
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

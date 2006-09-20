package net.sourceforge.c4jplugin.internal.ap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.c4j.ContractReference;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class ContractAnnotationProcessorFactory implements
		AnnotationProcessorFactory {

	private ArrayList<String> suppAnnotations = new ArrayList<String>();
	
	public ContractAnnotationProcessorFactory() {
		suppAnnotations.add(ContractReference.class.getName());
	}
	
	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> arg0,
			AnnotationProcessorEnvironment env) {
		return new ContractAnnotationProcessor(env);
	}

	public Collection<String> supportedAnnotationTypes() {
		return suppAnnotations;
	}

	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}

}

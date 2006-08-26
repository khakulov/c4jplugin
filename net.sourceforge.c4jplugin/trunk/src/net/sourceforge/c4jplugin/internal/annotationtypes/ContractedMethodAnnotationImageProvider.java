package net.sourceforge.c4jplugin.internal.annotationtypes;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.markers.IContractedMethodMarker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

public class ContractedMethodAnnotationImageProvider implements IAnnotationImageProvider {

	private ImageRegistry imageRegistry = new ImageRegistry();
	
	public ContractedMethodAnnotationImageProvider() {
		imageRegistry.put(IContractedMethodMarker.VALUE_POST_METHOD, 
				C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, 
						"icons/markers/contracted_post_method.gif"));
		imageRegistry.put(IContractedMethodMarker.VALUE_PRE_METHOD, 
				C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, 
						"icons/markers/contracted_pre_method.gif"));
		imageRegistry.put(IContractedMethodMarker.VALUE_PREPOST_METHOD, 
				C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, 
						"icons/markers/contracted_prepost_method.gif"));
	}
	
	public Image getManagedImage(Annotation annotation) {
		if (annotation.getType().equals(IC4JAnnotations.TYPE_CONTRACTED_METHOD)) {
			if (annotation instanceof SimpleMarkerAnnotation) {
				IMarker marker = ((SimpleMarkerAnnotation)annotation).getMarker();
				try {
					return imageRegistry.get((String)marker.getAttribute(IContractedMethodMarker.ATTR_CONTRACT_TYPE));
				} catch (CoreException e) {
					return null;
				}
			}
		}
		return null;
	}

	
	public String getImageDescriptorId(Annotation annotation) {
		return null;
	}

	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		return null;
	}

}

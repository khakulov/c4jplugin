package net.sourceforge.c4jplugin.internal.annotationtypes;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.markers.IMethodMarker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

public class MethodAnnotationImageProvider implements IAnnotationImageProvider {

	private ImageRegistry imageRegistry = new ImageRegistry();
	
	public MethodAnnotationImageProvider() {
		imageRegistry.put(IC4JAnnotations.TYPE_CLASS_INVARIANT, 
				C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, 
						"icons/class_invariant.gif"));
		imageRegistry.put(IMethodMarker.VALUE_POST_METHOD, 
				C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, 
						"icons/post_method.gif"));
		imageRegistry.put(IMethodMarker.VALUE_PRE_METHOD, 
				C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, 
						"icons/pre_method.gif"));
		imageRegistry.put(IMethodMarker.VALUE_PREPOST_METHOD, 
				C4JActivator.imageDescriptorFromPlugin(C4JActivator.PLUGIN_ID, 
						"icons/pre_post_method.gif"));
	}
	
	public Image getManagedImage(Annotation annotation) {
		String strType = annotation.getType();
		if (strType.equals(IC4JAnnotations.TYPE_CLASS_INVARIANT)) {
			return imageRegistry.get(strType);
		}
		else if (strType.equals(IC4JAnnotations.TYPE_CONTRACTED_METHOD)) {
			if (annotation instanceof SimpleMarkerAnnotation) {
				IMarker marker = ((SimpleMarkerAnnotation)annotation).getMarker();
				try {
					return imageRegistry.get((String)marker.getAttribute(IMethodMarker.ATTR_CONTRACT_TYPE));
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

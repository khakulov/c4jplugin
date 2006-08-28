package net.sourceforge.c4jplugin.runtime;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

private static final String BUNDLE_NAME = Messages.class.getName();
	
	private Messages() {
		
	}
	
	public static String c4jRuntimeContainerName;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
}

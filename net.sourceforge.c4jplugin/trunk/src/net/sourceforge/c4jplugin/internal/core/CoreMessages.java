package net.sourceforge.c4jplugin.internal.core;

import org.eclipse.osgi.util.NLS;

public class CoreMessages extends NLS {

private static final String BUNDLE_NAME = CoreMessages.class.getName();
	
	private CoreMessages() {
		
	}
	
	public static String c4jRuntimeContainerName;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, CoreMessages.class);
	}
	
}

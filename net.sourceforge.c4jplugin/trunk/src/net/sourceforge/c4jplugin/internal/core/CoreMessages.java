package net.sourceforge.c4jplugin.internal.core;

import org.eclipse.osgi.util.NLS;

public class CoreMessages extends NLS {

private static final String BUNDLE_NAME = CoreMessages.class.getName();
	
	public static String ContractReferenceModel_oldModel;

	private CoreMessages() {
		
	}
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, CoreMessages.class);
	}
	
}

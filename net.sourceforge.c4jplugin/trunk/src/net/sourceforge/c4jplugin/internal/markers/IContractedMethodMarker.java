package net.sourceforge.c4jplugin.internal.markers;

import net.sourceforge.c4jplugin.C4JActivator;

public interface IContractedMethodMarker {
	public static final String ID = C4JActivator.PLUGIN_ID + ".contractedmethodmarker";
	
	public static final String ATTR_CONTRACT_TYPE = "contractType";
	public static final String ATTR_HANDLE_IDENTIFIER = "handleIdentifier";
	public static final String ATTR_CONTRACT_REFERENCES = "contractReferences";
	
	public static final String VALUE_PRE_METHOD = "pre_method";
	public static final String VALUE_POST_METHOD = "post_method";
	public static final String VALUE_PREPOST_METHOD = "prepost_method";
}

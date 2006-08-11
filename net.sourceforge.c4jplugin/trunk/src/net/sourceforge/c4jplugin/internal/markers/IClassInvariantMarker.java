package net.sourceforge.c4jplugin.internal.markers;

import net.sourceforge.c4jplugin.C4JActivator;

public interface IClassInvariantMarker {
	public static final String ID = C4JActivator.PLUGIN_ID + ".classinvariantmarker";
	
	public static final String ATTR_CONTRACT_REFERENCES = "contractReferences";
}

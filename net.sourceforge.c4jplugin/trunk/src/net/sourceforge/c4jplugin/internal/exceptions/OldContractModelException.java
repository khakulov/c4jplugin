package net.sourceforge.c4jplugin.internal.exceptions;

public class OldContractModelException extends Exception {

	private String oldVersion;
	
	public OldContractModelException(String message, String oldVersion) {
		super(message);
		
		this.oldVersion = oldVersion;
	}
	
	public String getOldVersion() {
		return oldVersion;
	}
}

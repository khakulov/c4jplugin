package net.sourceforge.c4jplugin.internal.ui.viewers;

import java.util.ArrayList;

import org.eclipse.debug.core.ILaunchConfigurationType;

public class LaunchConfigurationTypeTreeElement {

	private ILaunchConfigurationType type;
	private ArrayList<LaunchConfigurationTreeElement> children = null;
	
	private boolean changeArgs = false;
	private boolean askChangeArgs = false;
	
	public LaunchConfigurationTypeTreeElement(ILaunchConfigurationType type) {
		this.type = type;
	}
	
	public ILaunchConfigurationType getLaunchConfigurationType() {
		return type;
	}
	
	public boolean isAskChangeVMArguments() {
		return askChangeArgs;
	}

	public void setAskChangeVMArguments(boolean askChangeArgs) {
		this.askChangeArgs = askChangeArgs;
	}

	public boolean isChangeVMArguments() {
		return changeArgs;
	}

	public void setChangeVMArguments(boolean changeArgs) {
		this.changeArgs = changeArgs;
	}
	
	public void addChild(LaunchConfigurationTreeElement child) {
		if (children == null) children = new ArrayList<LaunchConfigurationTreeElement>();
		children.add(child);
	}
	
	public Object[] getChildren() {
		if (children == null) return null;
		return children.toArray();
	}
	
	public boolean hasChildren() {
		if (children == null) return false;
		return children.size() > 0;
	}
	
	public String toString() {
		return type.getName();
	}
	
}

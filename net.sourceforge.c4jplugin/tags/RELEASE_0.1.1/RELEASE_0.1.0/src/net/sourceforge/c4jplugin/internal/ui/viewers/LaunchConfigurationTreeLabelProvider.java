package net.sourceforge.c4jplugin.internal.ui.viewers;

import net.sourceforge.c4jplugin.internal.util.C4JImages;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class LaunchConfigurationTreeLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return C4JImages.getImage(element);
		case 1:
			if (element instanceof LaunchConfigurationTypeTreeElement) {
				LaunchConfigurationTypeTreeElement item = (LaunchConfigurationTypeTreeElement)element;
				if (item.isChangeVMArguments())
					return C4JImages.getImage(C4JImages.CHECKBOX_YES);
				return C4JImages.getImage(C4JImages.CHECKBOX_NO);
			}
			else if (element instanceof LaunchConfigurationTreeElement) {
				LaunchConfigurationTreeElement item = (LaunchConfigurationTreeElement)element;
				if (item.isC4JEnabled())
					return C4JImages.getImage(C4JImages.CHECKBOX_YES);
				return C4JImages.getImage(C4JImages.CHECKBOX_NO);
			}
			break;
		case 2:
			if (element instanceof LaunchConfigurationTypeTreeElement) {
				LaunchConfigurationTypeTreeElement item = (LaunchConfigurationTypeTreeElement)element;
				if (item.isAskChangeVMArguments())
					return C4JImages.getImage(C4JImages.CHECKBOX_YES);
				return C4JImages.getImage(C4JImages.CHECKBOX_NO);
			}
			break;
		}
		
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 0) {
			if (element instanceof LaunchConfigurationTypeTreeElement) {
				return ((LaunchConfigurationTypeTreeElement)element).getLaunchConfigurationType().getName();
			}
			
			if (element instanceof LaunchConfigurationTreeElement) {
				return ((LaunchConfigurationTreeElement)element).getLaunchConfiguration().getName();
			}
		}
		
		return null;
	}

}

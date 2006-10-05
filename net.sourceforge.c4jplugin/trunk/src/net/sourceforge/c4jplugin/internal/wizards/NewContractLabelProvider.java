package net.sourceforge.c4jplugin.internal.wizards;

import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class NewContractLabelProvider extends JavaElementLabelProvider
										implements ITableLabelProvider {

	public final static int PRE_COND_NONE = 0;
	public final static int PRE_COND_NONNULL = 1;
	public final static int PRE_COND_NONEMPTY = 2;
	
	public final static int POST_COND_NONE = 0;
	public final static int POST_COND_NONNULL = 1;
	public final static int POST_COND_NONEMPTY = 2;
	
	public final static String[] preCondLabels = {"", "Non-null args", "Non-null and non-empty args"};
	public final static String[] postCondLabels = {"", "Non-null return", "Non-null and non-empty return"};
	
	public NewContractLabelProvider() {
		super(SHOW_DEFAULT | SHOW_RETURN_TYPE);
	}
	
	public Image getColumnImage(Object element, int columnIndex) {
		NewContractMethodElement item = (NewContractMethodElement)element;
		switch (columnIndex) {
		case 0:
			return super.getImage(item.getMember());
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		NewContractMethodElement item = (NewContractMethodElement)element;
		switch (columnIndex) {
		case 0:
			return super.getText(item.getMember());
		case 1:
			return preCondLabels[item.getPreCondition()];
		case 2:
			return postCondLabels[item.getPostCondition()];
		}
		return null;
	}

}

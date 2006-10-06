package net.sourceforge.c4jplugin.internal.wizards;

import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class NewContractLabelProvider extends JavaElementLabelProvider
										implements ITableLabelProvider {

	public final static int PRE_COND_NONE = 0;
	public final static int PRE_COND_EMPTYSTUB = 1;
	public final static int PRE_COND_NONNULL = 2;
	public final static int PRE_COND_NONEMPTY = 3;
	
	public final static int POST_COND_NONE = 0;
	public final static int POST_COND_EMPTYSTUB = 1;
	public final static int POST_COND_NONNULL = 2;
	public final static int POST_COND_NONEMPTY = 3;
	
	public final static String[] preCondLabels = {"", WizardMessages.NewContractLabelProvider_empty_condition, WizardMessages.NewContractLabelProvider_nonnull_args, WizardMessages.NewContractLabelProvider_nonnull_nonempty_args}; //$NON-NLS-1$
	public final static String[] postCondLabels = {"", WizardMessages.NewContractLabelProvider_empty_condition, WizardMessages.NewContractLabelProvider_nonnull_return, WizardMessages.NewContractLabelProvider_nonnull_nonempty_return}; //$NON-NLS-1$
	
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
			if (item.getPreCondition() >= 0)
				return preCondLabels[item.getPreCondition()];
		case 2:
			if (item.getPostCondition() >= 0)
				return postCondLabels[item.getPostCondition()];
		}
		return null;
	}

}

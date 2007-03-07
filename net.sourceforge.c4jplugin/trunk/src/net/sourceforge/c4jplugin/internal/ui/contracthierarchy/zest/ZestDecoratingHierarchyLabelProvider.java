package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.zest;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.mylar.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.swt.graphics.Color;

public class ZestDecoratingHierarchyLabelProvider 
		extends DecoratingJavaLabelProvider implements IEntityStyleProvider {

	/**
     * Decorating label provider for Java. Combines a JavaUILabelProvider
     * with problem and override indicuator with the workbench decorator (label
     * decorator extension point).
     */
    public ZestDecoratingHierarchyLabelProvider(JavaUILabelProvider labelProvider) {
            super(labelProvider, true);
    }
	
	public Color getAdjacentEntityHighlightColor(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getBackgroundColour(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getBorderColor(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getBorderHighlightColor(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getBorderWidth(Object entity) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Color getForegroundColour(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getHighlightColor(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFigure getTooltip(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean highlightAdjacentEntities(Object entity) {
		// TODO Auto-generated method stub
		return false;
	}

	public Color getForeground(Object element) {
        return getForegroundColour(element);
	}


	public Color getBackground(Object element) {
        return getBackgroundColour(element);
	}
}

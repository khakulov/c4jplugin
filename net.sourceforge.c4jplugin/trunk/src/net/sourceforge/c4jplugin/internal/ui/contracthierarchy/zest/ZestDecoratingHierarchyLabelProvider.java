package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.zest;

import net.sourceforge.c4jplugin.internal.core.ContractReferenceModel;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyLifeCycle;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyViewPart;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.HierarchyLabelProvider;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.IContractHierarchy;

import org.eclipse.core.resources.IResource;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.mylar.zest.core.ZestStyles;
import org.eclipse.mylar.zest.core.viewers.IEntityConnectionStyleProvider;
import org.eclipse.mylar.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ZestDecoratingHierarchyLabelProvider 
		extends DecoratingJavaLabelProvider 
		implements IEntityStyleProvider, IEntityConnectionStyleProvider {

	private ContractHierarchyLifeCycle fHierarchy;
	private int fHierarchyMode;
	
	private Color fColorBgEnabled;
	private Color fColorBgDisabled;
	private Color fColorFgEnabled;
	private Color fColorFgDisabled;
	private Color fColorHighlight;
	
	private Object fCurrEntity = null;
	private Color fColorCurrBg = null;
	private Color fColorCurrFg = null;
	
	/**
     * Decorating label provider for Java. Combines a JavaUILabelProvider
     * with problem and override indicuator with the workbench decorator (label
     * decorator extension point).
     */
    public ZestDecoratingHierarchyLabelProvider(HierarchyLabelProvider labelProvider) {
            super(labelProvider, true);
            
            fHierarchy = labelProvider.getHierarchyLifeCycle();
            
            Display display = Display.getCurrent();
            fColorBgEnabled = new Color(display, 248, 178,43);
            fColorBgDisabled = new Color(display, 211, 229, 236);
            fColorFgEnabled = display.getSystemColor(SWT.COLOR_BLACK);
            fColorFgDisabled = new Color(display, 160, 160, 160);
            fColorHighlight = display.getSystemColor(SWT.COLOR_YELLOW);
    }
	
    public void dispose() {
    	super.dispose();
    	
    	fColorBgEnabled.dispose();
    	fColorBgDisabled.dispose();
    	fColorFgDisabled.dispose();
    }
    
    private void updateColors(Object entity) {
    	if (fCurrEntity == null || !fCurrEntity.equals(entity)) {
    		fCurrEntity = entity;
    		IContractHierarchy hierarchy = fHierarchy.getContractHierarchy();
    		if (hierarchy == null || fHierarchyMode == ContractHierarchyViewPart.HIERARCHY_MODE_CLASSIC) {
    			fColorCurrBg = fColorBgEnabled;
    			fColorCurrFg = fColorFgEnabled;
    		}
    		else if (fHierarchyMode == ContractHierarchyViewPart.HIERARCHY_MODE_SUBTYPES) {
    			IType input = hierarchy.getType();
    			if ((input != null && entity.equals(input)) || hierarchy.isSubcontract((IType)entity)) {
    				fColorCurrBg = fColorBgEnabled;
    				fColorCurrFg = fColorFgEnabled;
    			}
    			else {
    				fColorCurrBg = fColorBgDisabled;
    				fColorCurrFg = fColorFgDisabled;
    			}
    		}
    		else if (fHierarchyMode == ContractHierarchyViewPart.HIERARCHY_MODE_SUPERTYPES) {
    			IType input = hierarchy.getType();
    			if ((input != null && entity.equals(input)) || hierarchy.isSupercontract((IType)entity)) {
    				fColorCurrBg = fColorBgEnabled;
    				fColorCurrFg = fColorFgEnabled;
    			}
    			else {
    				fColorCurrBg = fColorBgDisabled;
    				fColorCurrFg = fColorFgDisabled;
    			}
    		}
    		else {
    			fColorCurrBg = null;
    			fColorCurrFg = null;
    		}
    	}
    }
    
    public void setHierarchyMode(int mode) {
    	fHierarchyMode = mode;
    	fCurrEntity = null;
    }
    
	public Color getAdjacentEntityHighlightColor(Object entity) {
		return null;
	}

	public Color getBackgroundColour(Object entity) {
		updateColors(entity);
		return fColorCurrBg;
	}

	public Color getBorderColor(Object entity) {
		return getForegroundColour(entity);
	}

	public Color getBorderHighlightColor(Object entity) {
		return getBorderColor(entity);
	}

	public int getBorderWidth(Object entity) {
		return 1;
	}

	public Color getForegroundColour(Object entity) {
		updateColors(entity);
		return fColorCurrFg;
	}

	public Color getHighlightColor(Object entity) {
		return fColorHighlight;
	}

	public IFigure getTooltip(Object entity) {
		if (entity == null || !(entity instanceof IType)) return null;
		
		IResource res;
		try {
			res = ((IType)entity).getUnderlyingResource();
			IResource target = ContractReferenceModel.getTarget(res);
			if (target != null) {
				return new Label("Target: " + target.getName());
			}
		} catch (JavaModelException e) {
			return null;
		}
		return null;
	}

	public boolean highlightAdjacentEntities(Object entity) {
		return false;
	}

	public Color getForeground(Object element) {
        return getForegroundColour(element);
	}


	public Color getBackground(Object element) {
        return getBackgroundColour(element);
	}

	public Color getColor(Object src, Object dest) {
		return getForegroundColour(dest).equals(fColorFgDisabled) ? getForegroundColour(dest) : getForegroundColour(src);
	}

	public int getConnectionStyle(Object src, Object dest) {
		if (getForegroundColour(dest).equals(fColorFgEnabled)) {
			return ZestStyles.CONNECTIONS_DIRECTED;
		}
		
		return ZestStyles.CONNECTIONS_DIRECTED | ZestStyles.CONNECTIONS_DASH;
	}

	public Color getHighlightColor(Object src, Object dest) {
		return null;
	}

	public int getLineWidth(Object src, Object dest) {
		return 1;
	}
}

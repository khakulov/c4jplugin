package net.sourceforge.c4jplugin.internal.ui.contracthierarchy.actions;

import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyMessages;
import net.sourceforge.c4jplugin.internal.ui.contracthierarchy.ContractHierarchyViewPart;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

public class ToggleRepresentationAction extends Action {

	private ContractHierarchyViewPart fView;	
	private int fActionRepresentation;
	
	public ToggleRepresentationAction(ContractHierarchyViewPart v, int representation) {
		super("", AS_RADIO_BUTTON);
		if (representation == ContractHierarchyViewPart.REPRESENTATION_MODE_GRAPH) {
			setText("Graph Representation"); 
			setDescription("Graph Representation"); 
			setToolTipText("Visualizes the full contract hierarchy as a graph"); 
		}
		else if (representation == ContractHierarchyViewPart.REPRESENTATION_MODE_TREE) {
			setText("Tree Representation"); 
			setDescription("Tree Representation"); 
			setToolTipText("Visualizes the contract hierarchy as a traditional tree"); 
		}
		else {
			Assert.isTrue(false);
		}
		fView = v;
		fActionRepresentation = representation;
		
		// TODO ToggleRepresentationAction Help
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.TOGGLE_ORIENTATION_ACTION);
	}
	
	public int getRepresentation() {
		return fActionRepresentation;
	}	
	
	/*
	 * @see Action#actionPerformed
	 */		
	public void run() {
		if (isChecked()) {
			fView.setRepresentationMode(fActionRepresentation, true);
		}
	}
}

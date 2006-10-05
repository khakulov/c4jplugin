package net.sourceforge.c4jplugin.internal.wizards;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class NewContractMethodElement {

	private IMember member = null;
	private Integer precond = NewContractLabelProvider.PRE_COND_NONE;
	private Integer postcond = NewContractLabelProvider.POST_COND_NONE;
	
	public NewContractMethodElement(IMember member) {
		this.member = member;
		
		if (member instanceof IMethod) {
			IMethod method = (IMethod)member;
			
			// pre-conditions
			if (method.getNumberOfParameters() > 0) {
				String[] paramTypes = method.getParameterTypes();
				precond = NewContractLabelProvider.PRE_COND_NONNULL; // non-null
				for (String paramType : paramTypes) {
					if (paramType.contains("String")) { // non-null and non-empty
						precond = NewContractLabelProvider.PRE_COND_NONEMPTY;
						break;
					}
				}
			}
			
			// post-conditions
			try {
				String returnType = method.getReturnType();
				if (returnType.contains("String")) 
					postcond = NewContractLabelProvider.POST_COND_NONEMPTY;
				else if (returnType.contains("Q") || returnType.contains("L"))
					postcond = NewContractLabelProvider.POST_COND_NONNULL;
			} catch (JavaModelException e) {}
			
		}
	}
	
	public IMember getMember() {
		return member;
	}
	
	public Integer getPreCondition() {
		return precond;
	}
	
	public Integer getPostCondition() {
		return postcond;
	}
	
	public void setPreCondition(Integer condition) {
		precond = condition;
	}
	
	public void setPostCondition(Integer condition) {
		postcond = condition;
	}
}

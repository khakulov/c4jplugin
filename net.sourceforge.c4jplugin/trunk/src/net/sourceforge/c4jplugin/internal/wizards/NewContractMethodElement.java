package net.sourceforge.c4jplugin.internal.wizards;

import net.sourceforge.c4jplugin.internal.util.C4JStubUtil;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

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
				precond = NewContractLabelProvider.PRE_COND_EMPTYSTUB;
				String[] paramTypes = method.getParameterTypes();
				for (String paramType : paramTypes) {
					String paramTypeName = Signature.getSimpleName(Signature.toString(paramType));
					if ("String".equals(paramTypeName)) { // non-null and non-empty //$NON-NLS-1$
						precond = NewContractLabelProvider.PRE_COND_NONEMPTY;
						break;
					}
					else if (!C4JStubUtil.isBuiltInType(paramType)) {
						precond = NewContractLabelProvider.PRE_COND_NONNULL;
					}
				}
			}
			
			// post-conditions
			try {
				String returnType = Signature.toString(method.getReturnType());
				String returnTypeName = Signature.getSimpleName(returnType);
				if (returnTypeName.equals("String"))  //$NON-NLS-1$
					postcond = NewContractLabelProvider.POST_COND_NONEMPTY;
				else if (!C4JStubUtil.isBuiltInType(method.getReturnType()))
					postcond = NewContractLabelProvider.POST_COND_NONNULL;
				else if (!"void".equals(returnTypeName)) //$NON-NLS-1$
					postcond = NewContractLabelProvider.POST_COND_EMPTYSTUB;
				
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

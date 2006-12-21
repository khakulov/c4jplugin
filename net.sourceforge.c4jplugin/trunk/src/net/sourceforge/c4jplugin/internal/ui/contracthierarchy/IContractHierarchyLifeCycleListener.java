/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.c4jplugin.internal.ui.contracthierarchy;

import org.eclipse.jdt.core.IType;


/**
 * Used by the ContractHierarchyLifeCycle to inform listeners about a change in the
 * type hierarchy
 */
public interface IContractHierarchyLifeCycleListener {
	
	/**
	 * A Java element changed. 
	 */
	void contractHierarchyChanged(ContractHierarchyLifeCycle contractHierarchyProvider, IType[] changedTypes);

}

/*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
 * Technische Universitaet Muenchen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 ******************************************************************************/
package edu.tum.in.bruegge.epd.emfstore.handler.merge.util;

import java.util.List;

import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Switch for case study.
 * 
 * @author wesendon
 */
public class CaseStudySwitch {

	/**
	 * Flatten changepackages.
	 * 
	 * @param myChangePackage my cp
	 * @param theirChangePackages their cps
	 */
	public void flattenChangePackages(ChangePackage myChangePackage, List<ChangePackage> theirChangePackages) {
		boolean openQuestion = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
			"Remove CompositeOperations?", "Do you want to remove the composite operations for testing purposes?");

		if (!openQuestion) {
			return;
		}

		if (myChangePackage != null) {
			flattenComposites(myChangePackage.getOperations());
		}
		for (ChangePackage cp : theirChangePackages) {
			flattenComposites(cp.getOperations());
		}
	}

	private void flattenComposites(List<AbstractOperation> operations) {
		for (int i = 0; i < operations.size(); i++) {
			AbstractOperation abstractOperation = operations.get(i);
			if (OperationUtil.isComposite(abstractOperation)) {
				operations.remove(i);
				CompositeOperation composite = (CompositeOperation) abstractOperation;
				operations.addAll(i, composite.getSubOperations());
				// for (AbstractOperation subOp : composite.getSubOperations())
				// {
				// }
			}
		}
	}

}

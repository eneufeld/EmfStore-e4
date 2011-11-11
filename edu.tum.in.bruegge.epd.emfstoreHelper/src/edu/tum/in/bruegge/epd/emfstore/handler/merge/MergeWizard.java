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
package edu.tum.in.bruegge.epd.emfstore.handler.merge;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * Container of the Merge Dialog.
 * 
 * @author wesendon
 */
public class MergeWizard extends Wizard {

	private DecisionManager decisionManager;

	/**
	 * Default constructor.
	 * 
	 * @param decisionManager
	 *            decisionManager
	 */
	public MergeWizard(DecisionManager decisionManager) {
		super();
		setWindowTitle("Merge Wizard");
		setDefaultPageImageDescriptor(DecisionUtil
				.getImageDescriptor("merge_wizard2.gif"));

		this.decisionManager = decisionManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		super.addPages();
		addPage(new MergeWizardPage(decisionManager));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		if (decisionManager.isResolved()) {
			decisionManager.calcResult();
			return true;
		}

		MessageDialog.openInformation(getShell(),
				"Resolve all conflicts first",
				"You have to resolve all conflicts in order to finish."
						+ "\nTherefore choose an option for every conflict.");
		return false;
	}
}

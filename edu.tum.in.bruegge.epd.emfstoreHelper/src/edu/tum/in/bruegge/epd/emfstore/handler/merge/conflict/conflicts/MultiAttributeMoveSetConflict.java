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
package edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts;

// BEGIN COMPLEX CODE
//
// WORK IN PROGRESS !
//

import java.util.List;

import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictDescription;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption.OptionType;

public class MultiAttributeMoveSetConflict extends Conflict {

	public MultiAttributeMoveSetConflict(List<AbstractOperation> opsA, List<AbstractOperation> opsB,
		DecisionManager decisionManager, boolean isMySet) {
		super(opsA, opsB, decisionManager, isMySet, true);
	}

	/**
	 * LEFT: Set, RIGHT: Move
	 */

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.ui.dialogs.merge.conflict.Conflict#initConflictDescription()
	 */
	@Override
	protected ConflictDescription initConflictDescription(ConflictDescription description) {

		if (isLeftMy()) {
			description
				.setDescription("You have changed an element from the [feature] attribute of [modelelement], which was moved in the repository.");
		} else {
			description
				.setDescription("You have moved an element  from the [feature] attribute of [modelelement], which was changed in the repository. ");
		}

		return description;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.ui.dialogs.merge.conflict.Conflict#initConflictOptions(java.util.List)
	 */
	@Override
	protected void initConflictOptions(List<ConflictOption> options) {
		ConflictOption myOption = new ConflictOption("", OptionType.MyOperation);
		myOption.addOperations(getMyOperations());
		ConflictOption theirOption = new ConflictOption("", OptionType.TheirOperation);
		theirOption.addOperations(getTheirOperations());

		if (isLeftMy()) {
			myOption.setOptionLabel("Change element");
			theirOption.setOptionLabel("Move element");
		} else {
			myOption.setOptionLabel("Move element");
			theirOption.setOptionLabel("Change element");
		}

		options.add(myOption);
		options.add(theirOption);
	}
}

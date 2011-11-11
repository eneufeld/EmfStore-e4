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

import static edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil.getClassAndName;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.MultiReferenceOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictDescription;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption.OptionType;

/**
 * Conflict between two {@link MultiReferenceConflict}.
 * 
 * @author wesendon
 */
public class MultiReferenceConflict extends Conflict {

	private boolean containmentConflict;

	/**
	 * Default constructor.
	 * 
	 * @param addingOperation list of operations, with leading adding multiref operation
	 * @param removingOperation list of operations, with leading removing multiref operation
	 * @param decisionManager decisionmanager
	 * @param meAdding true, if merging user has adding multiref
	 */
	public MultiReferenceConflict(List<AbstractOperation> addingOperation, List<AbstractOperation> removingOperation,
		DecisionManager decisionManager, boolean meAdding) {
		super(addingOperation, removingOperation, decisionManager, meAdding, false);
		containmentConflict = getMyOperation(MultiReferenceOperation.class).isAdd()
			&& getTheirOperation(MultiReferenceOperation.class).isAdd();
		init();
	}

	/**
	 * LEFT: Adding RIGHT: Removing
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ConflictDescription initConflictDescription(ConflictDescription description) {
		String descriptionTxt = "";

		if (containmentConflict) {
			descriptionTxt = "You have moved [target] to the [feature] reference of [modelelement], on the repository it was moved to [othercontainer].";
		} else if (isLeftMy()) {
			descriptionTxt = "You have added [target] to the [feature]" + " reference of the [modelelement]."
				+ " This item was removed on the repository.";
		} else {
			descriptionTxt = "The [target] was added to the [feature] reference"
				+ " of the [modelelement] on the repository." + " You chose to remove it, please decide.";
		}
		description.setDescription(descriptionTxt);
		description.add("target", getMyOperation(MultiReferenceOperation.class).getReferencedModelElements().get(0));
		description.add("othercontainer", getTheirOperation(MultiReferenceOperation.class).getModelElementId());

		description.setImage("multiref.gif");
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initConflictOptions(List<ConflictOption> options) {
		ConflictOption myOption = new ConflictOption("", OptionType.MyOperation);
		myOption.addOperations(getMyOperations());
		ConflictOption theirOption = new ConflictOption("", OptionType.TheirOperation);
		theirOption.addOperations(getTheirOperations());

		EObject target = getDecisionManager().getModelElement(
			getMyOperation(MultiReferenceOperation.class).getReferencedModelElements().get(0));

		if (containmentConflict) {
			myOption.setOptionLabel("Move " + getClassAndName(target) + "to"
				+ getClassAndName(getDecisionManager().getModelElement(getMyOperation().getModelElementId())));
			theirOption.setOptionLabel("Move " + getClassAndName(target) + " to"
				+ getClassAndName(getDecisionManager().getModelElement(getTheirOperation().getModelElementId())));
		} else {
			myOption.setOptionLabel((isLeftMy()) ? "Add" : "Remove" + " " + getClassAndName(target));
			theirOption.setOptionLabel((!isLeftMy()) ? "Add" : "Remove" + " " + getClassAndName(target));
		}

		options.add(myOption);
		options.add(theirOption);
	}
}

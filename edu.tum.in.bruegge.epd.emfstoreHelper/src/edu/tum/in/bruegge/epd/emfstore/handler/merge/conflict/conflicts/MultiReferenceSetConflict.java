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
import static edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil.getClassAndName;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.MultiReferenceOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.MultiReferenceSetOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictDescription;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption.OptionType;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

public class MultiReferenceSetConflict extends Conflict {

	private boolean containmentConflict;

	/**
	 * Default constructor.
	 * 
	 * @param multiRef multireference in conflict
	 * @param multiRefSet multireference set in conflict
	 * @param decisionManager decisionmanager
	 * @param myMultiRef is my multireference
	 */
	public MultiReferenceSetConflict(List<AbstractOperation> multiRef, List<AbstractOperation> multiRefSet,
		DecisionManager decisionManager, boolean myMultiRef) {
		super(multiRef, multiRefSet, decisionManager, myMultiRef, false);
		containmentConflict = ((MultiReferenceOperation) getLeftOperation()).isAdd()
			&& !getLeftOperation().getModelElementId().equals(getRightOperation().getModelElementId());
		init();
	}

	/**
	 * LEFT MultiRef, Right MultiRefSet
	 */

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.ui.dialogs.merge.conflict.Conflict#initConflictDescription()
	 */
	@Override
	protected ConflictDescription initConflictDescription(ConflictDescription description) {

		if (containmentConflict) {
			description
				.setDescription("You have moved [target] to the [feature] reference of [modelelement], on the repository it was moved to [othercontainer].");
		} else if (isLeftMy()) {
			description
				.setDescription("You have removed [target] from the [feature] reference of [modelelement], which was set in the repository");
		} else {
			description
				.setDescription("You have set [target] in the [feature] reference of [modelelement], which was removed in the repository.");
		}

		description.add("target", isLeftMy() ? getMyOperation(MultiReferenceOperation.class)
			.getReferencedModelElements().get(0) : getMyOperation(MultiReferenceSetOperation.class).getNewValue());
		description.add("othercontainer", getLeftOperation().getModelElementId());
		description.setImage("multiref.gif");

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

		if (containmentConflict) {
			EObject target = getDecisionManager().getModelElement(
				((MultiReferenceOperation) getLeftOperation()).getReferencedModelElements().get(0));

			myOption.setOptionLabel("Move " + getClassAndName(target) + "to"
				+ getClassAndName(getDecisionManager().getModelElement(getMyOperation().getModelElementId())));
			theirOption.setOptionLabel("Move " + getClassAndName(target) + " to"
				+ getClassAndName(getDecisionManager().getModelElement(getTheirOperation().getModelElementId())));

		} else if (isLeftMy()) {
			EObject target = getDecisionManager().getModelElement(
				getMyOperation(MultiReferenceOperation.class).getReferencedModelElements().get(0));

			myOption.setOptionLabel("Remove " + DecisionUtil.getClassAndName(target));
			theirOption.setOptionLabel("Set " + DecisionUtil.getClassAndName(target));
		} else {
			EObject target = getDecisionManager().getModelElement(
				getTheirOperation(MultiReferenceOperation.class).getReferencedModelElements().get(0));

			myOption.setOptionLabel("Set " + DecisionUtil.getClassAndName(target));
			theirOption.setOptionLabel("Remove " + DecisionUtil.getClassAndName(target));
		}

		options.add(myOption);
		options.add(theirOption);

	}
}

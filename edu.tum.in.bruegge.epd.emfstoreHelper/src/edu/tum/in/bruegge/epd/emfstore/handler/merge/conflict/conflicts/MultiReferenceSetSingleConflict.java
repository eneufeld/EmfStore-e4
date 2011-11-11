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
import org.eclipse.emf.emfstore.server.model.versioning.operations.SingleReferenceOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictDescription;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption.OptionType;

/**
 * @author wesendon
 */
public class MultiReferenceSetSingleConflict extends Conflict {

	/**
	 * Default constructor.
	 * 
	 * @param leftOperations multi set ref
	 * @param rightOperations single ref
	 * @param decisionManager decisionmanager
	 * @param setLeft multi set ref is left
	 */
	public MultiReferenceSetSingleConflict(List<AbstractOperation> leftOperations,
		List<AbstractOperation> rightOperations, DecisionManager decisionManager, boolean setLeft) {
		super(leftOperations, rightOperations, decisionManager, setLeft, true);
	}

	/**
	 * LEFT: MultiReferenceSet, RIGHT: SingleReference
	 */

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.ui.dialogs.merge.conflict.Conflict#initConflictDescription(org.eclipse.emf.emfstore.client.ui.dialogs.merge.conflict.ConflictDescription)
	 */
	@Override
	protected ConflictDescription initConflictDescription(ConflictDescription description) {
		String descriptionTxt = "You have moved [target] to the [feature] reference of [modelelement], on the repository it was moved to [othercontainer].";

		description.setDescription(descriptionTxt);
		description.add("target", ((SingleReferenceOperation) getRightOperation()).getNewValue());
		description.add("othercontainer", getTheirOperation().getModelElementId());

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

		EObject target = getDecisionManager().getModelElement(
			((SingleReferenceOperation) getLeftOperation()).getNewValue());

		myOption.setOptionLabel("Move " + getClassAndName(target) + "to"
			+ getClassAndName(getDecisionManager().getModelElement(getMyOperation().getModelElementId())));
		theirOption.setOptionLabel("Move " + getClassAndName(target) + " to"
			+ getClassAndName(getDecisionManager().getModelElement(getTheirOperation().getModelElementId())));

		options.add(myOption);
		options.add(theirOption);
	}

}

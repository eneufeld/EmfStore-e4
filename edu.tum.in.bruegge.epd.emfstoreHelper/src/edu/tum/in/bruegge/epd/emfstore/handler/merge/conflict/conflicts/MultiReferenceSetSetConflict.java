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
import org.eclipse.emf.emfstore.server.model.versioning.operations.MultiReferenceSetOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictDescription;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption.OptionType;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

public class MultiReferenceSetSetConflict extends Conflict {

	private boolean containmentConflict;

	public MultiReferenceSetSetConflict(List<AbstractOperation> opsA, List<AbstractOperation> opsB,
		DecisionManager decisionManager) {
		super(opsA, opsB, decisionManager, true, false);
		// is this rule enough?
		containmentConflict = getMyOperation().getModelElementId().equals(getTheirOperation().getModelElementId());
		init();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.ui.dialogs.merge.conflict.Conflict#initConflictDescription()
	 */
	@Override
	protected ConflictDescription initConflictDescription(ConflictDescription description) {
		String txt = "";
		if (!containmentConflict) {
			txt = "You have set the value [value] to the [feature] reference of [modelelement], it was set to [ovalue] on the repository";
		} else {
			txt = "You have moved the element [value] to [modelelement], it was moved to [othercontainer] on the repository.";
		}

		description.add("value", getMyOperation(MultiReferenceSetOperation.class).getNewValue());
		description.add("ovalue", getTheirOperation(MultiReferenceSetOperation.class).getNewValue());
		description.add("othercontainer", getTheirOperation().getModelElementId());
		description.setDescription(txt);
		description.setImage("multiref.gif");
		return description;
	}

	@Override
	protected void initConflictOptions(List<ConflictOption> options) {
		ConflictOption myOption = new ConflictOption("", OptionType.MyOperation);
		myOption.addOperations(getMyOperations());
		ConflictOption theirOption = new ConflictOption("", OptionType.TheirOperation);
		theirOption.addOperations(getTheirOperations());

		if (!containmentConflict) {
			myOption.setOptionLabel(DecisionUtil.getClassAndName(getDecisionManager().getModelElement(
				getMyOperation(MultiReferenceSetOperation.class).getNewValue())));
			theirOption.setOptionLabel(DecisionUtil.getClassAndName(getDecisionManager().getModelElement(
				getTheirOperation(MultiReferenceSetOperation.class).getNewValue())));
		} else {
			EObject target = getDecisionManager().getModelElement(
				getMyOperation(MultiReferenceSetOperation.class).getNewValue());

			myOption.setOptionLabel("Move " + getClassAndName(target) + "to"
				+ getClassAndName(getDecisionManager().getModelElement(getMyOperation().getModelElementId())));
			theirOption.setOptionLabel("Move " + getClassAndName(target) + " to"
				+ getClassAndName(getDecisionManager().getModelElement(getTheirOperation().getModelElementId())));
		}

		options.add(myOption);
		options.add(theirOption);
	}
}

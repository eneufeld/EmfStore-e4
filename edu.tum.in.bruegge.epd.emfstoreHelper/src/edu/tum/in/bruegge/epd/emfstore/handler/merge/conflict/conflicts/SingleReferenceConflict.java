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

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.emfstore.common.model.ModelElementId;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.SingleReferenceOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.UnkownFeatureException;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictDescription;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption.OptionType;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * Conflict for two {@link SingleReferenceOperation}.
 * 
 * @author wesendon
 */
public class SingleReferenceConflict extends Conflict {

	/**
	 * Default constructor.
	 * 
	 * @param myOperations list of my operations
	 * @param theirOperations list of their operations
	 * @param decisionManager decisionmanager
	 */
	public SingleReferenceConflict(List<AbstractOperation> myOperations, List<AbstractOperation> theirOperations,
		DecisionManager decisionManager) {
		super(myOperations, theirOperations, decisionManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ConflictDescription initConflictDescription(ConflictDescription description) {
		String descriptionTxt = "";
		if (isContainmentFeature()) {
			descriptionTxt = "You have moved the [modelelement] to the [myvalue]."
				+ "This element was moved to [theirvalue] on the repository. Please decide.";
		} else {
			descriptionTxt = "You have changed the reference [feature] of [modelelement] to [myvalue]."
				+ "This reference was set to [theirvalue] on the repository. Please decide.";
		}
		description.setDescription(descriptionTxt);
		EObject myNewValue = getDecisionManager().getModelElement(
			getMyOperation(SingleReferenceOperation.class).getNewValue());
		description.add("myvalue", (myNewValue == null) ? "(unset)" : myNewValue);
		EObject theirNewValue = getDecisionManager().getModelElement(
			getTheirOperation(SingleReferenceOperation.class).getNewValue());
		description.add("theirvalue", (theirNewValue == null) ? "(unset)" : theirNewValue);

		description.setImage("singleref.gif");

		return description;
	}

	private boolean isContainmentFeature() {
		EObject modelElement = getDecisionManager().getModelElement(getMyOperation().getModelElementId());
		if (modelElement == null) {
			return false;
		}
		try {
			if (((EReference) getMyOperation(SingleReferenceOperation.class).getFeature(modelElement)).isContainer()) {
				return true;
			}
		} catch (UnkownFeatureException e) {
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initConflictOptions(List<ConflictOption> options) {

		// My Option
		ModelElementId newValue = getMyOperation(SingleReferenceOperation.class).getNewValue();
		ConflictOption myOption = new ConflictOption((newValue == null) ? "(unset)"
			: DecisionUtil.getClassAndName(getDecisionManager().getModelElement(newValue)), OptionType.MyOperation);
		myOption.addOperations(getMyOperations());

		// Their Option
		ModelElementId theirNewValue = getTheirOperation(SingleReferenceOperation.class).getNewValue();
		ConflictOption theirOption = new ConflictOption(DecisionUtil.getLabel(
			DecisionUtil.getClassAndName(getDecisionManager().getModelElement(theirNewValue)), "(unset)"),
			OptionType.TheirOperation);
		theirOption.addOperations(getTheirOperations());

		options.add(myOption);
		options.add(theirOption);
	}
}

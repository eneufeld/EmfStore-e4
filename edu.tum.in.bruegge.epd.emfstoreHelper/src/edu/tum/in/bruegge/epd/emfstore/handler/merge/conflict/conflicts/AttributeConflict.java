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

import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AttributeOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictDescription;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionConfig;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * Conflict for two attribute operations.
 * 
 * @author wesendon
 */
public class AttributeConflict extends Conflict {

	/**
	 * Default constructor.
	 * 
	 * @param myOperations myOperations, with leading {@link AttributeOperation}
	 * @param theirOperations theirOperations, with leading {@link AttributeOperation}
	 * @param decisionManager decisionmanager
	 */
	public AttributeConflict(List<AbstractOperation> myOperations, List<AbstractOperation> theirOperations,
		DecisionManager decisionManager) {
		super(myOperations, theirOperations, decisionManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ConflictDescription initConflictDescription(ConflictDescription description) {
		description.setDescription("You have changed the [feature] attribute of [modelelement] to [myvalue]."
			+ " This attribute was changed to [theirvalue] on the repository.");
		description.add("myvalue", getMyOperation(AttributeOperation.class).getNewValue());
		description.add("theirvalue", getTheirOperation(AttributeOperation.class).getNewValue());
		description.setImage("attribute.gif");

		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initConflictOptions(List<ConflictOption> options) {
		initOptionsWithOutMerge(options, true);
	}

	/**
	 * Allows to init options, without adding a merge text option.
	 * 
	 * @param options list of options
	 * @param withMerge true, if merge text option ({@link MergeTextOption}) should be added
	 */
	protected void initOptionsWithOutMerge(List<ConflictOption> options, boolean withMerge) {
		ConflictOption myOption = new ConflictOption(getMyOperation(AttributeOperation.class).getNewValue(),
			ConflictOption.OptionType.MyOperation);
		myOption.setDetailProvider(DecisionConfig.WIDGET_MULTILINE);
		myOption.addOperations(getMyOperations());
		options.add(myOption);

		ConflictOption theirOption = new ConflictOption(getTheirOperation(AttributeOperation.class).getNewValue(),
			ConflictOption.OptionType.TheirOperation);
		theirOption.setDetailProvider(DecisionConfig.WIDGET_MULTILINE);
		theirOption.addOperations(getTheirOperations());
		options.add(theirOption);

		if (withMerge && DecisionUtil.detailsNeeded(this)) {
			MergeTextOption mergeOption = new MergeTextOption();
			mergeOption.add(myOption);
			mergeOption.add(theirOption);
			options.add(mergeOption);
		}
	}
}

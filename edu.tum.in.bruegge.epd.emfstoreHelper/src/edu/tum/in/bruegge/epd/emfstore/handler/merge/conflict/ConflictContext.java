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
package edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.FeatureOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;

/**
 * Holding the data for the context of an conflict.
 * 
 * @author wesendon
 */
public class ConflictContext {

	private final EObject modelElement;
	private final String attribute;
	private final String opponent;

	/**
	 * Default constructor.
	 * 
	 * @param modelElement element
	 * @param attribute attribute
	 * @param opponent opponent
	 */
	public ConflictContext(EObject modelElement, String attribute, String opponent) {
		this.modelElement = modelElement;
		this.attribute = attribute;
		this.opponent = opponent;
	}

	/**
	 * Alternative constructor.
	 * 
	 * @param modelElement element
	 * @param opponent opponent
	 */
	public ConflictContext(EObject modelElement, String opponent) {
		this.modelElement = modelElement;
		this.attribute = null;
		this.opponent = opponent;
	}

	/**
	 * Alternative constructor.
	 * 
	 * @param manager decisionmanager
	 * @param myOperation my op
	 * @param theirOperation their op
	 */
	public ConflictContext(DecisionManager manager, AbstractOperation myOperation, AbstractOperation theirOperation) {
		this(manager.getModelElement(myOperation.getModelElementId()),
			(myOperation instanceof FeatureOperation) ? ((FeatureOperation) myOperation).getFeatureName() : "", manager
				.getAuthorForOperation(theirOperation));
	}

	/**
	 * Get ModelELement.
	 * 
	 * @return element
	 */
	public EObject getModelElement() {
		return modelElement;
	}

	/**
	 * Get Attribute.
	 * 
	 * @return attribute
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * Get Opponent.
	 * 
	 * @return opponent
	 */
	public String getOpponent() {
		return opponent;
	}

	/**
	 * Get label for Modelelement.
	 * 
	 * @return label
	 */
	public String getModelElementTitleLabel() {
		return "ModelElement";
	}

	/**
	 * Get label of attribute.
	 * 
	 * @return label
	 */
	public String getAttributeTitleLabel() {
		return "Attribute";
	}

	/**
	 * Get label of opponent.
	 * 
	 * @return label
	 */
	public String getOpponentTitleLabel() {
		return "Opponent";
	}
}

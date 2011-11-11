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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.FeatureOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption.OptionType;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * Main class representing a conflict. it offers all kind of convenience methods
 * and organizes the conflicts initialization. Read the constructor's
 * description for further implemenation details (
 * {@link #Conflict(List, List, DecisionManager)})
 * 
 * @author wesendon
 */
public abstract class Conflict extends Observable {

	private DecisionManager decisionManager;
	private ArrayList<ConflictOption> options;
	private ConflictOption solution;
	private ConflictContext conflictContext;
	private ConflictDescription conflictDescription;

	/**
	 * List of operations.
	 * 
	 * @see #Conflict(List, List, DecisionManager)
	 */
	private List<AbstractOperation> leftOperations;
	private List<AbstractOperation> rightOperations;
	private boolean leftIsMy;

	/**
	 * Default constructor for conflicts. Many conflicts only need one operation
	 * for my and their side. But in order to use a suitable upper class for all
	 * conflicts, conflicts requires a list of operations. opsA ~ myOperations,
	 * opsB ~ theirOperations, but again, to keep it general, it's called A and
	 * B. These fields are protected so the implementing Conflict should create
	 * it's own getter method.
	 * 
	 * @param leftOperations
	 *            first list of operations (often: myOperations)
	 * @param rightOperations
	 *            second list of operations (often: theirOperations)
	 * @param decisionManager
	 *            decision manager
	 */
	public Conflict(List<AbstractOperation> leftOperations,
			List<AbstractOperation> rightOperations,
			DecisionManager decisionManager) {
		this(leftOperations, rightOperations, decisionManager, true, true);
	}

	/**
	 * Determines whether left operations are my.
	 * 
	 * @return boolean
	 */
	public boolean isLeftMy() {
		return leftIsMy;
	}

	/**
	 * Additional constructor, which allows deactivating initialization.
	 * 
	 * @see #Conflict(List, List, DecisionManager)
	 * @param leftOperations
	 *            first list of operations (often: myOperations)
	 * @param rightOperations
	 *            second list of operations (often: theirOperations)
	 * @param decisionManager
	 *            decision manager
	 * @param leftIsMy
	 *            left operations are my changes
	 * @param init
	 *            allows to deactivate initialization, has to be done manually
	 *            otherwise.
	 */
	public Conflict(List<AbstractOperation> leftOperations,
			List<AbstractOperation> rightOperations,
			DecisionManager decisionManager, boolean leftIsMy, boolean init) {
		this.leftIsMy = leftIsMy;
		this.leftOperations = leftOperations;
		this.rightOperations = rightOperations;
		this.decisionManager = decisionManager;
		if (init) {
			init();
		}
	}

	/**
	 * Initiates the conflict.
	 */
	protected void init() {
		conflictContext = initConflictContext();
		conflictDescription = initConflictDescription();
		options = new ArrayList<ConflictOption>();
		initConflictOptions(options);
		initAdditionalConflictOptions(options);
	}

	private void initAdditionalConflictOptions(
			ArrayList<ConflictOption> options2) {
		if (!allowOtherOptions()) {
			return;
		}
		IConfigurationElement[] config = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(
						"org.eclipse.emf.emfstore.client.ui.merge.customoption");

		for (IConfigurationElement e : config) {
			try {
				Object object = e.createExecutableExtension("class");
				if (object instanceof CustomConflictOptionFactory) {

					CustomConflictOptionFactory factory = (CustomConflictOptionFactory) object;
					if (factory.isApplicableConflict(this)) {
						CustomConflictOption customConflictOption = factory
								.createCustomConflictOption(this);
						if (customConflictOption != null) {
							options.add(customConflictOption);
						}
					}

				}
			} catch (CoreException e1) {
				WorkspaceUtil.logException(
						"Couldn't load merge option extension point.", e1);
				// fail silently
			}
		}
	}

	/**
	 * Defines whether other option should be allowed via extension. E.g. Issue
	 * option.
	 * 
	 * @return true, if other options are allowed
	 */
	protected boolean allowOtherOptions() {
		return true;
	}

	/**
	 * Is called in order to init the options.
	 * 
	 * @param options
	 *            list of options
	 */
	protected abstract void initConflictOptions(List<ConflictOption> options);

	/**
	 * Init conflict description.
	 * 
	 * @param description
	 *            pre initialized description
	 * @return description
	 */
	protected abstract ConflictDescription initConflictDescription(
			ConflictDescription description);

	private ConflictDescription initConflictDescription() {
		ConflictDescription description = new ConflictDescription("");
		description.setImage("notset.gif");
		EObject modelElement = getDecisionManager().getModelElement(
				getMyOperation().getModelElementId());
		if (modelElement != null) {
			description.add("modelelement", modelElement);
		}
		if (getMyOperation() instanceof FeatureOperation) {
			description.add("feature",
					((FeatureOperation) getMyOperation()).getFeatureName());
		}
		description.setDecisionManager(getDecisionManager());
		return initConflictDescription(description);
	}

	/**
	 * Inits the ConflictContext.
	 * 
	 * @return context.
	 */
	protected ConflictContext initConflictContext() {
		return new ConflictContext(getDecisionManager(), getMyOperation(),
				getTheirOperation());
	}

	/**
	 * Returns the conflict context.
	 * 
	 * @return context.
	 */
	public ConflictContext getConflictContext() {
		return conflictContext;
	}

	/**
	 * Returns the conflict description.
	 * 
	 * @return conflict description
	 */
	public ConflictDescription getConflictDescription() {
		return conflictDescription;
	}

	/**
	 * Returns the list of options.
	 * 
	 * @return list options
	 */
	public List<ConflictOption> getOptions() {
		return options;
	}

	/**
	 * Returns whether this conflict is resolved.
	 * 
	 * @return true if resolved
	 */
	public boolean isResolved() {
		return (solution != null);
	}

	/**
	 * Checks whether the related options have details.
	 * 
	 * @return true, if at least one got details.
	 */
	public boolean hasDetails() {
		for (ConflictOption option : getOptions()) {
			if (option.isDetailsProvider()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets an options as solution for this conflict.
	 * 
	 * @param conflictOption
	 *            option
	 */
	public void setSolution(ConflictOption conflictOption) {
		solution = conflictOption;
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the {@link DecisionManager}.
	 * 
	 * @return decisionManager
	 */
	public DecisionManager getDecisionManager() {
		return decisionManager;
	}

	/**
	 * Returns the solution.
	 * 
	 * @return solution
	 */
	public ConflictOption getSolution() {
		return solution;
	}

	/**
	 * This method is used by {@link DecisionManager} in order to create the
	 * resulting operations.
	 * 
	 * @return list of ops.
	 */
	public List<AbstractOperation> getRejectedTheirs() {
		if (!isResolved()) {
			throw new IllegalStateException(
					"Can't call this method, unless conflict is resolved.");
		}
		if (solution.getType() == OptionType.TheirOperation) {
			return new ArrayList<AbstractOperation>();
		} else {
			for (ConflictOption options : getOptions()) {
				if (options.getType() == OptionType.TheirOperation) {
					return options.getOperations();
				}
			}
		}
		throw new IllegalStateException("No TheirOperations found.");
		// return new ArrayList<AbstractOperation>();
	}

	/**
	 * This method is used by {@link DecisionManager} in order to create the
	 * resulting operations.
	 * 
	 * @return list of ops
	 */
	public List<AbstractOperation> getAcceptedMine() {
		if (!isResolved()) {
			throw new IllegalStateException(
					"Can't call this method, unless conflict is resolved.");
		}
		if (solution.getType() == OptionType.TheirOperation) {
			return new ArrayList<AbstractOperation>();
		} else {
			return solution.getOperations();
		}
	}

	/**
	 * Get an option by its type.
	 * 
	 * @param type
	 *            type
	 * @return option or null
	 */
	public ConflictOption getOptionOfType(OptionType type) {
		return DecisionUtil.getConflictOptionByType(getOptions(), type);
	}

	/**
	 * Get my operations.
	 * 
	 * @return list of operations
	 */
	protected List<AbstractOperation> getMyOperations() {
		return ((leftIsMy) ? leftOperations : rightOperations);
	}

	/**
	 * Get their operations.
	 * 
	 * @return list of operations
	 */
	protected List<AbstractOperation> getTheirOperations() {
		return ((!leftIsMy) ? leftOperations : rightOperations);
	}

	/**
	 * Get left operations.
	 * 
	 * @return list of operations
	 */
	protected List<AbstractOperation> getLeftOperations() {
		return leftOperations;
	}

	/**
	 * get right operations.
	 * 
	 * @return list of operations
	 */
	protected List<AbstractOperation> getRightOperations() {
		return rightOperations;
	}

	/**
	 * Get first left operation.
	 * 
	 * @return operation
	 */
	protected AbstractOperation getLeftOperation() {
		return leftOperations.get(0);
	}

	/**
	 * get first right operation.
	 * 
	 * @return operation
	 */
	protected AbstractOperation getRightOperation() {
		return rightOperations.get(0);
	}

	/**
	 * Get my operation.
	 * 
	 * @return operation
	 */
	protected AbstractOperation getMyOperation() {
		return getMyOperations().get(0);
	}

	/**
	 * Get their operation.
	 * 
	 * @return operation
	 */
	protected AbstractOperation getTheirOperation() {
		return getTheirOperations().get(0);
	}

	/**
	 * Get my operation and cast.
	 * 
	 * @param <T>
	 *            cast type
	 * @param clazz
	 *            {@link AbstractOperation} class to which will be casted
	 * @return operation
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getMyOperation(Class<T> clazz) {
		return (T) getMyOperation();
	}

	/**
	 * Get their operation and cast.
	 * 
	 * @param <T>
	 *            cast type
	 * @param clazz
	 *            {@link AbstractOperation} class to which will be casted
	 * @return operation
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getTheirOperation(Class<T> clazz) {
		return (T) getTheirOperation();
	}

}

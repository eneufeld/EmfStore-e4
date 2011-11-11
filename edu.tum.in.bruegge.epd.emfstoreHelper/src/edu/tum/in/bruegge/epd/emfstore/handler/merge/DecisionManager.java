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

import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isAttribute;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isComposite;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isCompositeRef;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isDelete;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isDiagramLayout;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isMultiAtt;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isMultiAttMove;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isMultiAttSet;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isMultiRef;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isMultiRefSet;
import static org.eclipse.emf.emfstore.server.model.versioning.operations.util.OperationUtil.isSingleRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.emfstore.common.model.ModelElementId;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.conflictDetection.ConflictDetector;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.CreateDeleteOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.MultiAttributeOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.MultiReferenceOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.AttributeConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.CompositeConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.DeletionConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.DiagramLayoutConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiAttributeConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiAttributeMoveConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiAttributeMoveSetConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiAttributeSetConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiAttributeSetSetConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiReferenceConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiReferenceSetConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiReferenceSetSetConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiReferenceSetSingleConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.MultiReferenceSingleConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.ReferenceConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.conflicts.SingleReferenceConflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.ChangePackageVisualizationHelper;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * DecisionManager is the controller for the merge dialog and therefore it's
 * main component. It calculates the conflicts from incoming changes and can
 * execute resolved conflicts.
 * 
 * @author wesendon
 */
public class DecisionManager {

	private final Project project;
	private final ChangePackage myChangePackage;
	private final List<ChangePackage> theirChangePackages;
	private ConflictDetector conflictDetector;

	private ArrayList<Conflict> conflicts;
	private ArrayList<AbstractOperation> notInvolvedInConflict;
	private ArrayList<AbstractOperation> acceptedMine;
	private ArrayList<AbstractOperation> rejectedTheirs;
	private final PrimaryVersionSpec baseVersion;
	private final PrimaryVersionSpec targetVersion;
	private ChangePackageVisualizationHelper visualizationHelper;

	/**
	 * Default constructor.
	 * 
	 * @param project
	 *            the related project
	 * @param myChangePackage
	 *            my changes
	 * @param theirChangePackages
	 *            incoming changes
	 * @param baseVersion
	 *            baseversion
	 * @param targetVersion
	 *            new target version
	 */
	public DecisionManager(Project project, ChangePackage myChangePackage,
			List<ChangePackage> theirChangePackages,
			PrimaryVersionSpec baseVersion, PrimaryVersionSpec targetVersion) {
		this.project = project;
		this.myChangePackage = myChangePackage;
		this.theirChangePackages = theirChangePackages;
		this.baseVersion = baseVersion;
		this.targetVersion = targetVersion;
		conflictDetector = new ConflictDetector();
		init();
	}

	private void init() {
		// flatten operations
		List<AbstractOperation> myOperations = myChangePackage.getOperations();
		List<AbstractOperation> theirOperations = new ArrayList<AbstractOperation>();
		for (ChangePackage cp : theirChangePackages) {
			theirOperations.addAll(cp.getOperations());
		}

		acceptedMine = new ArrayList<AbstractOperation>();
		rejectedTheirs = new ArrayList<AbstractOperation>();
		notInvolvedInConflict = new ArrayList<AbstractOperation>();

		conflicts = new ArrayList<Conflict>();
		ArrayList<Conflicting> conflicting = new ArrayList<Conflicting>();

		// Collect all conflicting
		ListIterator<AbstractOperation> myIterator = myOperations
				.listIterator(myOperations.size());
		while (myIterator.hasPrevious()) {
			AbstractOperation myOperation = myIterator.previous();
			boolean involved = false;
			ListIterator<AbstractOperation> theirIterator = theirOperations
					.listIterator(theirOperations.size());
			while (theirIterator.hasPrevious()) {
				AbstractOperation theirOperation = theirIterator.previous();
				if (conflictDetector.doConflict(myOperation, theirOperation)) {
					involved = true;
					boolean conflictingYet = false;
					List<Conflicting> tmpConf = new ArrayList<Conflicting>();
					// check against conflicting
					for (Conflicting conf : conflicting) {
						if (conf.add(myOperation, theirOperation)) {
							tmpConf.add(conf);
							conflictingYet = true;
						}
					}
					// merge conflicting
					if (tmpConf.size() > 1) {
						Conflicting main = tmpConf.get(0);
						for (int i = 1; i < tmpConf.size(); i++) {
							Conflicting conf = tmpConf.get(i);
							main.addMyOps(conf.getMyOperations());
							main.addTheirOps(conf.getTheirOperations());
							conflicting.remove(conf);
						}
					}
					if (!conflictingYet) {
						conflicting.add(new Conflicting(myOperation,
								theirOperation));
					}
				}
			}
			if (!involved) {
				notInvolvedInConflict.add(myOperation);
			}
		}

		createConflicts(conflicting);
	}

	/**
	 * BEGIN FACTORY TODO EXTRACT FACTORY CLASS.
	 */

	// BEGIN COMPLEX CODE
	private void createConflicts(ArrayList<Conflicting> conflicting) {
		// Create Conflicts from Conflicting
		for (Conflicting conf : conflicting) {
			AbstractOperation my = conf.getMyOperation();
			AbstractOperation their = conf.getTheirOperation();

			if (isDiagramLayout(my) && isDiagramLayout(their)) {

				addConflict(createDiagramLayoutDecision(conf));
				continue;

			} else if (isAttribute(my) && isAttribute(their)) {

				addConflict(createAttributeAttributeDecision(conf));
				continue;

			} else if (isSingleRef(my) && isSingleRef(their)) {

				addConflict(createSingleSingleConflict(conf));
				continue;

			} else if (isMultiRef(my) && isMultiRef(their)) {

				addConflict(createMultiMultiConflict(conf));
				continue;

			} else if ((isMultiRef(my) && isSingleRef(their))
					|| (isMultiRef(their) && isSingleRef(my))) {

				addConflict(createMultiSingle(conf));
				continue;

			} else if (isCompositeRef(my) && isCompositeRef(their)) {

				addConflict(createReferenceConflict(conf));
				continue;

			} else if ((isCompositeRef(my) && (isMultiRef(their) || isSingleRef(their)))
					|| ((isMultiRef(my) || isSingleRef(my)) && isCompositeRef(their))) {

				addConflict(createReferenceCompVSSingleMulti(conf));
				continue;

			} else if ((isMultiRef(my) && isMultiRefSet(their))
					|| (isMultiRef(their) && isMultiRefSet(my))) {

				addConflict(createMultiRefMultiSet(conf));
				continue;

			} else if (isMultiRefSet(my) && isMultiRefSet(their)) {

				addConflict(createMultiRefSetSet(conf));
				continue;

			} else if ((isMultiRefSet(my) && isSingleRef(their))
					|| (isMultiRefSet(their) && isSingleRef(my))) {

				addConflict(createMultiSetSingle(conf));
				continue;

			} else if (isMultiAtt(my) && isMultiAtt(their)) {

				addConflict(createMultiAtt(conf));
				continue;

			} else if ((isMultiAtt(my) && isMultiAttSet(their))
					|| (isMultiAtt(their) && isMultiAttSet(my))) {

				addConflict(createMultiAttSet(conf));
				continue;

			} else if ((isMultiAtt(my) && isMultiAttMove(their))
					|| (isMultiAtt(their) && isMultiAttMove(my))) {

				addConflict(createMultiAttMove(conf));
				continue;

			} else if ((isMultiAttSet(my) && isMultiAttMove(their))
					|| (isMultiAttSet(their) && isMultiAttMove(my))) {

				addConflict(createMultiAttMoveSet(conf));
				continue;

			} else if (isMultiAttSet(my) && isMultiAttSet(their)) {

				addConflict(createMultiAttSetSet(conf));
				continue;

			} else if (isComposite(my) || isComposite(their)) {

				addConflict(createCompositeConflict(conf));
				continue;

			} else if (isDelete(my) || isDelete(their)) {

				addConflict(createDeleteOtherConflict(conf));

			}
		}
	}

	private void addConflict(Conflict conflict) {
		if (conflict == null) {
			return;
		}
		conflicts.add(conflict);
	}

	// END COMPLEX CODE
	private Conflict createMultiRefMultiSet(Conflicting conf) {
		if (isMultiRef(conf.getMyOperation())) {
			return new MultiReferenceSetConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, true);
		} else {
			return new MultiReferenceSetConflict(conf.getTheirOperations(),
					conf.getMyOperations(), this, false);
		}
	}

	private Conflict createMultiSetSingle(Conflicting conf) {
		if (isMultiRefSet(conf.getMyOperation())) {
			return new MultiReferenceSetSingleConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, true);
		} else {
			return new MultiReferenceSetSingleConflict(
					conf.getTheirOperations(), conf.getMyOperations(), this,
					false);
		}
	}

	private Conflict createMultiSingle(Conflicting conf) {
		if (isMultiRef(conf.getMyOperation())) {
			return new MultiReferenceSingleConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, true);
		} else {
			return new MultiReferenceSingleConflict(conf.getTheirOperations(),
					conf.getMyOperations(), this, false);
		}
	}

	private Conflict createMultiRefSetSet(Conflicting conf) {
		return new MultiReferenceSetSetConflict(conf.getMyOperations(),
				conf.getTheirOperations(), this);
	}

	private Conflict createMultiAttSetSet(Conflicting conf) {
		return new MultiAttributeSetSetConflict(conf.getMyOperations(),
				conf.getTheirOperations(), this);
	}

	private Conflict createMultiAtt(Conflicting conf) {
		if (((MultiAttributeOperation) conf.getMyOperation()).isAdd()) {
			return new MultiAttributeConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, true);
		} else {
			return new MultiAttributeConflict(conf.getTheirOperations(),
					conf.getMyOperations(), this, false);

		}
	}

	private Conflict createMultiAttSet(Conflicting conf) {
		if (isMultiAtt(conf.getMyOperation())) {
			return new MultiAttributeSetConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, true);
		} else {
			return new MultiAttributeSetConflict(conf.getTheirOperations(),
					conf.getMyOperations(), this, false);
		}
	}

	private Conflict createMultiAttMove(Conflicting conf) {
		if (isMultiAtt(conf.getMyOperation())) {
			return new MultiAttributeMoveConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, true);
		} else {
			return new MultiAttributeMoveConflict(conf.getTheirOperations(),
					conf.getMyOperations(), this, false);
		}
	}

	private Conflict createMultiAttMoveSet(Conflicting conf) {
		if (isMultiAttSet(conf.getMyOperation())) {
			return new MultiAttributeMoveSetConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, true);
		} else {
			return new MultiAttributeMoveSetConflict(conf.getTheirOperations(),
					conf.getMyOperations(), this, false);
		}
	}

	private Conflict createReferenceCompVSSingleMulti(Conflicting conf) {
		if (isCompositeRef(conf.getMyOperation())) {
			return createRefFromSub(conf,
					((CompositeOperation) conf.getMyOperation())
							.getSubOperations(), Arrays.asList(conf
							.getTheirOperation()));
		} else {
			return createRefFromSub(conf, Arrays.asList(conf.getMyOperation()),
					((CompositeOperation) conf.getTheirOperation())
							.getSubOperations());
		}
	}

	private Conflict createReferenceConflict(Conflicting conf) {
		EList<AbstractOperation> myOperations = ((CompositeOperation) conf
				.getMyOperation()).getSubOperations();
		EList<AbstractOperation> theirOperations = ((CompositeOperation) conf
				.getTheirOperation()).getSubOperations();

		return createRefFromSub(conf, myOperations, theirOperations);
	}

	private Conflict createRefFromSub(Conflicting conf,
			List<AbstractOperation> myOperations,
			List<AbstractOperation> theirOperations) {

		for (AbstractOperation myOp : myOperations) {
			for (AbstractOperation theirOp : theirOperations) {
				if (conflictDetector.doConflict(myOp, theirOp)) {
					if (isSingleRef(myOp)) {

						return new ReferenceConflict(
								createSingleSingleConflict(myOp, theirOp),
								conf.getMyOperations(),
								conf.getTheirOperations());

					} else if (isMultiRef(myOp)) {

						return new ReferenceConflict(createMultiMultiConflict(
								myOp, theirOp), conf.getMyOperations(),
								conf.getTheirOperations());

					} else {
						return null;
					}
				}
			}
		}
		return null;
	}

	private Conflict createAttributeAttributeDecision(Conflicting conflicting) {
		return new AttributeConflict(conflicting.getMyOperations(),
				conflicting.getTheirOperations(), this);
	}

	private Conflict createDiagramLayoutDecision(Conflicting conflicting) {
		return new DiagramLayoutConflict(conflicting.getMyOperations(),
				conflicting.getTheirOperations(), this);
	}

	private Conflict createSingleSingleConflict(Conflicting conflicting) {
		return new SingleReferenceConflict(conflicting.getMyOperations(),
				conflicting.getTheirOperations(), this);
	}

	private Conflict createSingleSingleConflict(AbstractOperation my,
			AbstractOperation their) {
		return new SingleReferenceConflict(Arrays.asList(my),
				Arrays.asList(their), this);
	}

	private Conflict createMultiMultiConflict(Conflicting conf) {
		if (((MultiReferenceOperation) conf.getMyOperation()).isAdd()) {
			return new MultiReferenceConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, true);
		} else {
			return new MultiReferenceConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, false);
		}
	}

	private Conflict createMultiMultiConflict(AbstractOperation my,
			AbstractOperation their) {
		if (((MultiReferenceOperation) my).isAdd()) {
			return new MultiReferenceConflict(Arrays.asList(my),
					Arrays.asList(their), this, true);
		} else {
			return new MultiReferenceConflict(Arrays.asList(their),
					Arrays.asList(my), this, false);
		}
	}

	private Conflict createDeleteOtherConflict(Conflicting conf) {
		if (isDelete(conf.getMyOperation())) {
			return new DeletionConflict(conf.getMyOperations(),
					conf.getTheirOperations(), true, this);
		} else {
			return new DeletionConflict(conf.getTheirOperations(),
					conf.getMyOperations(), false, this);
		}
	}

	private Conflict createCompositeConflict(Conflicting conf) {
		if (isComposite(conf.getMyOperation())) {
			return new CompositeConflict(conf.getMyOperations(),
					conf.getTheirOperations(), this, true);
		} else {
			return new CompositeConflict(conf.getTheirOperations(),
					conf.getMyOperations(), this, false);
		}
	}

	/**
	 * FACTORY END
	 */

	/**
	 * Returns the conflicts.
	 * 
	 * @return list of conflicts.
	 */
	public ArrayList<Conflict> getConflicts() {
		return conflicts;
	}

	/**
	 * Checks whether all conflicts are resolved.
	 * 
	 * @return true if all are resolved
	 */
	public boolean isResolved() {
		boolean isResolved = true;
		for (Conflict conflict : conflicts) {
			isResolved = isResolved && conflict.isResolved();
		}
		return isResolved;
	}

	/**
	 * Get "my" accepted operations. This list will be empty, if
	 * {@link #calcResult()} hasn't been called before.
	 * 
	 * @return list of operations
	 */
	public List<AbstractOperation> getAcceptedMine() {
		return acceptedMine;
	}

	/**
	 * Get "their" accepted operations. This list will be empty, if
	 * {@link #calcResult()} hasn't been called before.
	 * 
	 * @return list of operations
	 */
	public List<AbstractOperation> getRejectedTheirs() {
		return rejectedTheirs;
	}

	/**
	 * If all conflicts are resolved this method will generate the resulting
	 * operations from the conflicts. Then call {@link #getAcceptedMine()} and
	 * {@link #getRejectedTheirs()}.
	 */
	public void calcResult() {
		if (!isResolved()) {
			return;
		}
		// collect my acknowledge operations
		for (AbstractOperation myOp : myChangePackage.getOperations()) {
			if (notInvolvedInConflict.contains(myOp)) {
				acceptedMine.add(myOp);
			} else {
				for (Conflict conflict : conflicts) {
					if (conflict.getAcceptedMine().contains(myOp)) {
						acceptedMine.add(myOp);
					}
				}
			}
		}

		// Collect other accepted, which were generated in the merge process
		for (Conflict conflict : conflicts) {
			for (AbstractOperation ao : conflict.getAcceptedMine()) {
				if (!acceptedMine.contains(ao)) {
					acceptedMine.add(ao);
				}
			}
		}

		for (ChangePackage theirCP : theirChangePackages) {
			for (AbstractOperation theirOp : theirCP.getOperations()) {
				for (Conflict conflict : conflicts) {
					if (conflict.getRejectedTheirs().contains(theirOp)) {
						rejectedTheirs.add(theirOp);
					}
				}
			}
		}
	}

	/**
	 * Returns the conflictdetector.
	 * 
	 * @return conflictdetector
	 */
	public ConflictDetector getConflictDetector() {
		return conflictDetector;
	}

	/**
	 * Get the Name of an model element by modelelement id.
	 * 
	 * @param modelElementId
	 *            id of element
	 * @return name as string
	 */
	public String getModelElementName(ModelElementId modelElementId) {
		return getModelElementName(getModelElement(modelElementId));
	}

	/**
	 * Get the Name of an model element.
	 * 
	 * @param modelElement
	 *            element
	 * @return name as string
	 */
	public String getModelElementName(EObject modelElement) {
		AdapterFactoryLabelProvider adapterFactory = DecisionUtil
				.getAdapterFactory();
		return adapterFactory.getText(modelElement);
	}

	/**
	 * Returns the modelelement. Therefore the project as well as creation and
	 * deletion operations are searched.
	 * 
	 * @param modelElementId
	 *            id of element.
	 * @return modelelement
	 */
	public EObject getModelElement(ModelElementId modelElementId) {
		EObject modelElement = project.getModelElement(modelElementId);
		if (modelElement == null) {
			modelElement = searchForCreatedME(modelElementId,
					myChangePackage.getOperations());
			if (modelElement == null) {
				for (ChangePackage cp : theirChangePackages) {
					modelElement = searchForCreatedME(modelElementId,
							cp.getOperations());
					if (modelElement != null) {
						break;
					}
				}
			}
		}
		return modelElement;
	}

	private EObject searchForCreatedME(ModelElementId modelElementId,
			List<AbstractOperation> operations) {
		for (AbstractOperation operation : operations) {
			EObject result = null;
			if (operation instanceof CreateDeleteOperation) {
				result = searchCreateAndDelete(
						(CreateDeleteOperation) operation, modelElementId);

			} else if (operation instanceof CompositeOperation) {
				EList<AbstractOperation> subOperations = ((CompositeOperation) operation)
						.getSubOperations();
				result = searchForCreatedME(modelElementId, subOperations);
			} else {
				continue;
			}
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private EObject searchCreateAndDelete(CreateDeleteOperation cdo,
			ModelElementId modelElementId) {
		EObject modelElement = cdo.getModelElement();
		if (modelElement == null) {
			return null;
		}
		Set<EObject> containedModelElements = ModelUtil
				.getAllContainedModelElements(modelElement, false);
		containedModelElements.add(modelElement);

		for (EObject child : containedModelElements) {
			ModelElementId childId = ModelUtil.clone(cdo.getEObjectToIdMap()
					.get(child));
			if (childId != null && childId.equals(modelElementId)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Returns the name of the author for a operation in list of their
	 * operations.
	 * 
	 * @param theirOperation
	 *            operation
	 * @return name as string or ""
	 */
	public String getAuthorForOperation(AbstractOperation theirOperation) {
		for (ChangePackage cp : theirChangePackages) {
			for (AbstractOperation op : cp.getOperations()) {
				List<AbstractOperation> tmpList = new ArrayList<AbstractOperation>();
				if (op instanceof CompositeOperation) {
					tmpList.add(op);
					tmpList.addAll(((CompositeOperation) op).getSubOperations());
				} else {
					tmpList.add(op);
				}
				for (AbstractOperation ao : tmpList) {
					if (ao.equals(theirOperation)) {
						LogMessage log = cp.getLogMessage();
						if (log == null) {
							return "";
						}
						return (log.getAuthor() == null) ? "" : log.getAuthor();

					}
				}
			}
		}
		return "";
	}

	/**
	 * Return the related project.
	 * 
	 * @return project
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * Returns the visualizationhelper.
	 * 
	 * @return visualizationhelper
	 */
	public ChangePackageVisualizationHelper getChangePackageVisualizationHelper() {
		if (visualizationHelper == null) {
			ArrayList<ChangePackage> list = new ArrayList<ChangePackage>();
			list.add(myChangePackage);
			list.addAll(theirChangePackages);
			visualizationHelper = new ChangePackageVisualizationHelper(list,
					project);
		}
		return visualizationHelper;
	}

	/**
	 * Returns the baseVersion of the project which is updating.
	 * 
	 * @return version
	 */
	public PrimaryVersionSpec getBaseVersion() {
		return baseVersion;
	}

	/**
	 * Returns the targetVersion of the update which caused merging.
	 * 
	 * @return version
	 */
	public PrimaryVersionSpec getTargetVersion() {
		return targetVersion;
	}

	/**
	 * Container for connected, conflicting operations.
	 * 
	 * @author wesendon
	 */
	private class Conflicting {

		private ArrayList<AbstractOperation> myOps;
		private ArrayList<AbstractOperation> theirOps;

		public Conflicting(AbstractOperation myOp, AbstractOperation theirOp) {
			myOps = new ArrayList<AbstractOperation>();
			myOps.add(myOp);
			theirOps = new ArrayList<AbstractOperation>();
			theirOps.add(theirOp);
		}

		public AbstractOperation getTheirOperation() {
			return theirOps.get(0);
		}

		public AbstractOperation getMyOperation() {
			return myOps.get(0);
		}

		public List<AbstractOperation> getTheirOperations() {
			return theirOps;
		}

		public List<AbstractOperation> getMyOperations() {
			return myOps;
		}

		public boolean add(AbstractOperation myOp, AbstractOperation theirOp) {
			for (AbstractOperation ao : getTheirOperations()) {
				if (conflictDetector.doConflict(myOp, ao)) {
					addToList(myOp, theirOp);
					return true;
				}
			}
			for (AbstractOperation ao : getMyOperations()) {
				if (conflictDetector.doConflict(ao, theirOp)) {
					addToList(myOp, theirOp);
					return true;
				}
			}
			return false;
		}

		private void addToList(AbstractOperation my, AbstractOperation their) {
			addMyOp(my);
			addTheirOp(their);
		}

		private void addMyOp(AbstractOperation my) {
			if (!myOps.contains(my)) {
				myOps.add(my);
			}
		}

		private void addTheirOp(AbstractOperation their) {
			if (!theirOps.contains(their)) {
				theirOps.add(their);
			}
		}

		public void addMyOps(List<AbstractOperation> ops) {
			for (AbstractOperation ao : ops) {
				addMyOp(ao);
			}
		}

		public void addTheirOps(List<AbstractOperation> ops) {
			for (AbstractOperation ao : ops) {
				addTheirOp(ao);
			}
		}
	}
}

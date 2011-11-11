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
package edu.tum.in.bruegge.epd.emfstore.dialog.util.scm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.common.model.ModelElementId;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.ChangePackageVisualizationHelper;

/**
 * Content provider for the scm views.
 * 
 * @author Shterev
 */
public abstract class SCMContentProvider implements ITreeContentProvider {

	private static ChangePackageVisualizationHelper changePackageVisualizationHelper;
	private boolean showRootNodes = true;
	private boolean reverseNodes = true;
	private AdapterFactoryContentProvider contentProvider;
	private ProjectSpace projectSpace;

	/**
	 * Default constructor.
	 * 
	 * @param treeViewer the tree viewer. the project.
	 */
	protected SCMContentProvider(TreeViewer treeViewer) {
		contentProvider = new AdapterFactoryContentProvider(new ComposedAdapterFactory(
			ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
	}

	/**
	 * Sets the flag to reverse the order of the nodes. Default value is true - i.e. the more recent operations are on
	 * top.
	 * 
	 * @param reverseNodes the new value
	 */
	public void setReverseNodes(boolean reverseNodes) {
		this.reverseNodes = reverseNodes;
	}

	/**
	 * Returns if the nodes should be reversed.
	 * 
	 * @return true if the nodes should be reversed in order
	 */
	public boolean isReverseNodes() {
		return reverseNodes;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object node) {
		TreeNode treeNode = (TreeNode) node;
		Object element = treeNode.getValue();
		if (element instanceof HistoryInfo) {
			HistoryInfo historyInfo = (HistoryInfo) element;
			return getChildren(historyInfo, treeNode);
		} else if (element instanceof ChangePackage) {
			ChangePackage cp = (ChangePackage) element;
			return getChildren(cp, treeNode);
		} else if (element instanceof EObject) {
			EObject me = (EObject) element;
			// show only model element that are contained in a project
			// and have an ID
			ModelElementId modelElementId = projectSpace.getProject().getModelElementId(me);
			if (modelElementId != null) {
				return getChildren(me, treeNode);
			}
		}
		return nodify(treeNode, Arrays.asList(contentProvider.getChildren(element))).toArray();
	}

	/**
	 * @param visualizationHelper the visualizationHelper to set.
	 */
	public void setChangePackageVisualizationHelper(ChangePackageVisualizationHelper visualizationHelper) {
		changePackageVisualizationHelper = visualizationHelper;
	}

	/**
	 * @param changePackage the changePackage
	 * @param treeNode the parent TreeNode
	 * @return the subelements for this change package
	 */
	protected abstract Object[] getChildren(ChangePackage changePackage, TreeNode treeNode);

	/**
	 * @param modelElement the modelElement
	 * @param treeNode the parent TreeNode
	 * @return the subelements of the modelElement
	 */
	protected abstract Object[] getChildren(EObject modelElement, TreeNode treeNode);

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren(Object element) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ChangePackage) {
			return nodify(null, Arrays.asList((ChangePackage) inputElement)).toArray();
		}
		if (!(inputElement instanceof List) || ((List) inputElement).isEmpty()) {
			return new Object[0];
		}
		List inputList = (List) inputElement;
		Object firstElement = inputList.get(0);
		if (firstElement == null) {
			return new Object[0];
		}
		if (firstElement instanceof ChangePackage) {
			if (showRootNodes) {
				return nodify(null, inputList).toArray();
			} else {
				ArrayList<Object> elements = new ArrayList<Object>();
				List<ChangePackage> changePackages = inputList;
				for (ChangePackage cp : changePackages) {
					elements.addAll(Arrays.asList(getChildren(cp, new TreeNode(cp))));
				}
				return elements.toArray();
			}
		} else if (firstElement instanceof HistoryInfo) {
			List<HistoryInfo> historyInfos = (List<HistoryInfo>) inputElement;
			if (showRootNodes) {
				return nodify(null, historyInfos).toArray();
			} else {
				ArrayList<Object> elements = new ArrayList<Object>();
				for (HistoryInfo hi : historyInfos) {
					if (hi.getChangePackage() != null) {
						elements.addAll(Arrays.asList(getChildren(hi, new TreeNode(hi))));
					}
				}
				return elements.toArray();
			}
		}
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object element) {
		if (element instanceof TreeNode) {
			return ((TreeNode) element).getParent();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	/**
	 * Creates a TreeNode wrapper list from the given object list.
	 * 
	 * @param treeNode the parent tree node
	 * @param list the list of childern objects.
	 * @return a new wrapped {@link ArrayList}.
	 */
	protected List<TreeNode> nodify(TreeNode treeNode, List<? extends Object> list) {
		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		for (Object o : list) {
			Object content = o;
			if (o instanceof ModelElementId) {
				ModelElementId modelElementId = (ModelElementId) o;
				EObject modelElement = changePackageVisualizationHelper.getModelElement(modelElementId);
				if (modelElement != null) {
					content = modelElement;
				}
			}
			SCMTreeNode meNode = new SCMTreeNode(content);
			meNode.setParent(treeNode);
			meNode.setProjectSpace(projectSpace);
			nodes.add(meNode);
		}
		return nodes;
	}

	/**
	 * @return if the root nodes should be shown.
	 */
	public boolean showRootNodes() {
		return showRootNodes;
	}

	/**
	 * Sets if the root nodes should be shown.
	 * 
	 * @param show the new value.
	 */
	public void setShowRootNodes(boolean show) {
		showRootNodes = show;
	}

	/**
	 * Get the children nodes of a history info.
	 * 
	 * @return an array of {@link AbstractOperation}s
	 */
	private Object[] getChildren(HistoryInfo historyInfo, TreeNode treeNode) {
		if (historyInfo.getChangePackage() == null) {
			return new Object[0];
		}
		return getChildren(historyInfo.getChangePackage(), treeNode);
	}

	/**
	 * Content provider displaying the scm item in the following order: HistoryInfo > ChangePackage > Operation(s) >
	 * ModelElement(s).
	 * 
	 * @author Shterev
	 */
	public static class Detailed extends SCMContentProvider {

		/**
		 * Default constructor.
		 * 
		 * @param viewer the viewer.
		 * @param project
		 */
		public Detailed(TreeViewer viewer) {
			super(viewer);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @return an array of {@link AbstractOperation}s
		 */
		@Override
		protected Object[] getChildren(ChangePackage changePackage, TreeNode treeNode) {
			EList<AbstractOperation> operations = changePackage.getOperations();
			List<TreeNode> nodes = nodify(treeNode, operations);
			if (isReverseNodes()) {
				Collections.reverse(nodes);
			}
			return nodes.toArray();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @return an empty array
		 */
		@Override
		protected Object[] getChildren(EObject modelElement, TreeNode treeNode) {
			Object[] children = super.contentProvider.getChildren(modelElement);
			List<TreeNode> result = nodify(treeNode, Arrays.asList(children));
			return result.toArray();
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return an array of {@link EObject}s
	 */
	protected Object[] getChildren(AbstractOperation op, TreeNode treeNode) {

		return nodify(treeNode, Arrays.asList(contentProvider.getChildren(op))).toArray();
	}

	/**
	 * Content provider displaying the scm item in the following order: HistoryInfo > ChangePackage > ModelElement(s) >
	 * Operation(s).
	 * 
	 * @author Shterev
	 */
	public static class Compact extends SCMContentProvider {

		/**
		 * Default constructor.
		 * 
		 * @param viewer the viewer.
		 */
		public Compact(TreeViewer viewer) {
			super(viewer);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @return an array of {@link EObject}s
		 */
		@Override
		protected Object[] getChildren(ChangePackage changePackage, TreeNode treeNode) {
			ArrayList<EObject> modelElements = changePackageVisualizationHelper.getModelElements(
				changePackage.getAllInvolvedModelElements(), new ArrayList<EObject>());
			List<TreeNode> nodes = nodify(treeNode, modelElements);
			return nodes.toArray();

		}

		/**
		 * {@inheritDoc}
		 * 
		 * @return an array of {@link AbstractOperation}s
		 */
		@Override
		protected Object[] getChildren(EObject modelElement, TreeNode treeNode) {
			ChangePackage changePackage;
			if (treeNode.getParent().getValue() instanceof HistoryInfo) {
				HistoryInfo historyInfo = (HistoryInfo) treeNode.getParent().getValue();
				changePackage = historyInfo.getChangePackage();
			} else if (treeNode.getParent().getValue() instanceof ChangePackage) {
				changePackage = (ChangePackage) treeNode.getParent().getValue();
			} else {
				return new Object[0];
			}
			List<AbstractOperation> operations = changePackage.getTouchingOperations(ModelUtil.getProject(modelElement)
				.getModelElementId(modelElement));
			List<TreeNode> nodes = nodify(treeNode, operations);
			if (isReverseNodes()) {
				Collections.reverse(nodes);
			}
			return nodes.toArray();

		}

	}

	/**
	 * Sets the ProjectSpace.
	 * 
	 * @param projectSpace the projectspace
	 */
	public void setProjectSpace(ProjectSpace projectSpace) {
		this.projectSpace = projectSpace;
	}

}

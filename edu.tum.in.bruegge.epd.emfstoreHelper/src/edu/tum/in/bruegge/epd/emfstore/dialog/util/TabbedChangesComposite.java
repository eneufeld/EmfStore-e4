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
package edu.tum.in.bruegge.epd.emfstore.dialog.util;

import java.util.List;

import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import edu.tum.in.bruegge.epd.emfstore.dialog.util.scm.SCMContentProvider;
import edu.tum.in.bruegge.epd.emfstore.dialog.util.scm.SCMLabelProvider;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.ChangePackageVisualizationHelper;

/**
 * A composite that contains multiple tabs displaying the operation from a different view - e.g. grouped by model
 * element, or ungrouped.
 * 
 * @author Shterev
 */
public class TabbedChangesComposite extends Composite implements ChangesComposite {

	private TabFolder folder;
	private List<ChangePackage> changePackages;
	private Composite detailedTabComposite;
	private Composite compactTabComposite;
	private TreeViewer compactTabTreeViewer;
	private TreeViewer detailedTabTreeViewer;
	private SCMContentProvider.Compact compactContentProvider;
	private SCMContentProvider.Detailed detailedContentProvider;

	/**
	 * Default constructor.
	 * 
	 * @param parent the composite's parent
	 * @param style the style
	 * @param changePackages the input of change packages as a list
	 * @param project the project
	 */
	public TabbedChangesComposite(Composite parent, int style, List<ChangePackage> changePackages, Project project) {
		super(parent, style);

		setLayout(new GridLayout());
		folder = new TabFolder(this, style);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(folder);

		// -----------------------Detailed -----------------------------
		detailedTabComposite = new Composite(folder, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(detailedTabComposite);

		detailedTabTreeViewer = new TreeViewer(detailedTabComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(detailedTabTreeViewer.getControl());

		detailedContentProvider = new SCMContentProvider.Detailed(detailedTabTreeViewer);
		detailedContentProvider.setShowRootNodes(true);
		SCMLabelProvider detailedLabelProvider = new SCMLabelProvider(project);
		detailedLabelProvider.setChangePackageVisualizationHelper(new ChangePackageVisualizationHelper(changePackages,
			project));
		detailedContentProvider.setChangePackageVisualizationHelper(new ChangePackageVisualizationHelper(
			changePackages, project));
		detailedTabTreeViewer.setContentProvider(detailedContentProvider);
		detailedTabTreeViewer.setLabelProvider(detailedLabelProvider);
		detailedTabTreeViewer.setInput(changePackages);
		detailedTabTreeViewer.expandToLevel(2);

		TabItem opTab = new TabItem(folder, style);
		opTab.setText("Operations");
		opTab.setControl(detailedTabComposite);

		// -----------------------Compact -----------------------------
		compactTabComposite = new Composite(folder, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(compactTabComposite);
		compactTabTreeViewer = new TreeViewer(compactTabComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(compactTabTreeViewer.getControl());

		compactContentProvider = new SCMContentProvider.Compact(compactTabTreeViewer);
		compactContentProvider.setShowRootNodes(true);
		SCMLabelProvider compactLabelProvider = new SCMLabelProvider(project);
		compactLabelProvider.setChangePackageVisualizationHelper(new ChangePackageVisualizationHelper(changePackages,
			project));
		compactContentProvider.setChangePackageVisualizationHelper(new ChangePackageVisualizationHelper(changePackages,
			project));
		compactTabTreeViewer.setContentProvider(compactContentProvider);
		compactTabTreeViewer.setLabelProvider(compactLabelProvider);
		compactTabTreeViewer.setInput(changePackages);
		compactTabTreeViewer.expandToLevel(2);

		TabItem meTab = new TabItem(folder, style);
		meTab.setText("ModelElements");
		meTab.setControl(compactTabComposite);
	}

	/**
	 * Sets if the root nodes should be shown.
	 * 
	 * @param showRootNodes the new value
	 */
	public void setShowRootNodes(boolean showRootNodes) {
		compactContentProvider.setShowRootNodes(showRootNodes);
		detailedContentProvider.setShowRootNodes(showRootNodes);
	}

	/**
	 * Sets if the root nodes should be reversed.
	 * 
	 * @see SCMContentProvider#setReverseNodes(boolean)
	 * @param reverseNodes wheter to reverse the nodes or not
	 */
	public void setReverseNodes(boolean reverseNodes) {
		compactContentProvider.setReverseNodes(reverseNodes);
		detailedContentProvider.setReverseNodes(reverseNodes);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ChangePackage> getChangePackages() {
		return changePackages;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(List<ChangePackage> changes) {
		this.changePackages = changes;
		compactTabTreeViewer.setInput(changes);
		detailedTabTreeViewer.setInput(changes);
	}

}

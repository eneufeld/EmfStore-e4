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
package edu.tum.in.bruegge.epd.emfstore.handler.merge.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * Generic container for conflicts.
 * 
 * @author wesendon
 */
public class DecisionBox extends Composite {

	private final Conflict conflict;
	private final DecisionManager decisionManager;
	private OptionComponent optionComponent;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            parent
	 * @param decisionManager
	 *            decisionManager
	 * @param color
	 *            background color
	 * @param conflict
	 *            the conflict
	 */
	public DecisionBox(Composite parent, DecisionManager decisionManager, Color color, Conflict conflict) {
		super(parent, SWT.BORDER);
		this.decisionManager = decisionManager;
		this.conflict = conflict;
		init(color);
	}

	private void init(Color color) {

		GridLayout decisionLayout = new GridLayout(2, true);
		this.setLayout(decisionLayout);
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		if (color != null) {
			setBackground(color);
		}

		new ContextComponent(this, conflict);
		optionComponent = new OptionComponent(this, conflict);
		new DescriptionComponent(this, conflict);

		if (DecisionUtil.detailsNeeded(conflict)) {
			new DetailsComponent(this, conflict);
		}

		for (Control control : getChildren()) {
			control.setBackground(getBackground());
		}
	}

	/**
	 * Returns the decisionmanager.
	 * 
	 * @return decisionmanager
	 */
	public DecisionManager getDecisionManager() {
		return decisionManager;
	}

	/**
	 * Set the solution of this conflict.
	 * 
	 * @param option
	 *            the option
	 */
	public void setSolution(ConflictOption option) {
		conflict.setSolution(option);
		optionComponent.refreshButtonColor();
	}

	/**
	 * Relayouts the box. Needed when box is dynamically resized. This is NOT a
	 * nice solution.
	 * 
	 * @param heightSizeChange
	 *            size delta
	 */
	public void layoutPage(int heightSizeChange) {
		ScrolledComposite scrollArea = (ScrolledComposite) getParent().getParent();
		scrollArea.setMinSize(scrollArea.getMinWidth(), scrollArea.getMinHeight() + heightSizeChange);
		scrollArea.layout();
	}

	/**
	 * Returns the conflict of the box.
	 * 
	 * @return conflict
	 */
	public Conflict getConflict() {
		return conflict;
	}
}

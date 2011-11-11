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

import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictContext;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * Displays the context bar in the decision box.
 * 
 * @author wesendon
 */
public class ContextComponent extends Composite {

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            parent
	 * @param conflict
	 *            conflict
	 */
	public ContextComponent(DecisionBox parent, Conflict conflict) {
		super(parent, SWT.NONE);

		ConflictContext context = conflict.getConflictContext();

		GridLayout layout = new GridLayout(3, false);
		layout.verticalSpacing = 1;
		layout.horizontalSpacing = 20;
		this.setLayout(layout);
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setBackground(parent.getBackground());

		FontRegistry fontRegistry = DecisionUtil.getFontRegistry();

		Label meTitle = new Label(this, SWT.NONE);
		meTitle.setText(context.getModelElementTitleLabel());
		meTitle.setFont(fontRegistry.get("titleLabel"));
		meTitle.setBackground(getBackground());

		Label attTitle = new Label(this, SWT.NONE);
		attTitle.setText(context.getAttributeTitleLabel());
		attTitle.setFont(fontRegistry.get("titleLabel"));
		attTitle.setBackground(getBackground());

		Label oppTitle = new Label(this, SWT.NONE);
		oppTitle.setText(context.getOpponentTitleLabel());
		oppTitle.setFont(fontRegistry.get("titleLabel"));
		oppTitle.setBackground(getBackground());

		AdapterFactoryLabelProvider provider = DecisionUtil.getLabelProvider();

		CLabel meLabel = new CLabel(this, SWT.NONE);
		meLabel.setImage(provider.getImage(context.getModelElement()));
		meLabel.setText(DecisionUtil.cutString(provider.getText(context.getModelElement()), 40, true));
		meLabel.setToolTipText(DecisionUtil.getClassAndName(context.getModelElement()));
		meLabel.setFont(fontRegistry.get("content"));
		meLabel.setBackground(getBackground());

		Label attLabel = new Label(this, SWT.NONE);
		attLabel.setText(context.getAttribute());
		attLabel.setFont(fontRegistry.get("content"));
		attLabel.setBackground(getBackground());

		Label oppLable = new Label(this, SWT.NONE);
		oppLable.setText(context.getOpponent());
		oppLable.setFont(fontRegistry.get("content"));
		oppLable.setBackground(getBackground());
	}

}

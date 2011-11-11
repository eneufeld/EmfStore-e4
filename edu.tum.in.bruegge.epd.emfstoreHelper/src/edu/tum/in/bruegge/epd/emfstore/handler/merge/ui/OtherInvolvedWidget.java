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

import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.ChangePackageVisualizationHelper;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * This details widget shows other involved operations using default representation.
 * 
 * @author wesendon
 */
public class OtherInvolvedWidget extends Composite {

	private static final int COLUMNS = 1;

	/**
	 * Default constructor.
	 * 
	 * @param parent parent
	 * @param decisionManager decisionManager
	 * @param option option
	 */
	public OtherInvolvedWidget(Composite parent, DecisionManager decisionManager, ConflictOption option) {
		super(parent, SWT.None);
		TableWrapLayout wrapLayout = new TableWrapLayout();
		wrapLayout.numColumns = COLUMNS;
		wrapLayout.makeColumnsEqualWidth = true;
		setLayout(wrapLayout);
		setBackground(parent.getBackground());

		Label label = new Label(this, SWT.NONE);
		label.setText("Other Involved Changes: ");
		label.setBackground(parent.getBackground());
		TableWrapData wrapData = new TableWrapData();
		wrapData.colspan = COLUMNS;
		label.setLayoutData(wrapData);

		ChangePackageVisualizationHelper visualizationHelper = decisionManager.getChangePackageVisualizationHelper();

		for (AbstractOperation ao : option.getOperations()) {
			Image image = visualizationHelper.getImage(DecisionUtil.getAdapterFactory(), ao);

			CLabel meLabel = new CLabel(this, SWT.WRAP);
			if (image != null) {
				meLabel.setImage(image);
			}
			meLabel.setText(visualizationHelper.getDescription(ao));
			meLabel.setBackground(parent.getBackground());
		}
	}
}

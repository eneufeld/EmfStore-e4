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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.Conflict;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictDescription;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.ChangePackageVisualizationHelper;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * Displays the description in the decision box.
 * 
 * @author wesendon
 */
public class DescriptionComponent extends Composite {

	/**
	 * Default constructor.
	 * 
	 * @param parent parent
	 * @param conflict conflict
	 */
	public DescriptionComponent(DecisionBox parent, Conflict conflict) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 20;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_BOTH));

		Label image = new Label(this, SWT.NONE);
		image.setImage(DecisionUtil.getImage(conflict.getConflictDescription().getImage()));
		image.setToolTipText(conflict.getClass().getSimpleName());
		image.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		image.setBackground(parent.getBackground());

		ArrayList<StyleRange> styleRanges = new ArrayList<StyleRange>();
		String description = "";
		for (String tmp : splitText(parent, conflict.getConflictDescription())) {
			if (tmp.startsWith("::")) {
				styleRanges.add(createStyleRange(description.length(), tmp.length() - 2));
				description += tmp.substring(2);
			} else {
				description += tmp;
			}
		}

		Group group = new Group(this, SWT.NONE);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		FillLayout groupLayout = new FillLayout();
		groupLayout.marginHeight = 5;
		groupLayout.marginWidth = 6;
		group.setLayout(groupLayout);
		group.setBackground(parent.getBackground());
		group.setText("Conflict Description:");

		StyledText styledDescription = new StyledText(group, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		styledDescription.setEditable(false);
		styledDescription.setEnabled(false);
		styledDescription.setText(description + "\n");
		styledDescription.setWordWrap(true);
		styledDescription.setStyleRanges(styleRanges.toArray(new StyleRange[styleRanges.size()]));
		styledDescription.setBackground(parent.getBackground());
	}

	private StyleRange createStyleRange(int start, int length) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = length;
		styleRange.fontStyle = SWT.BOLD;
		return styleRange;
	}

	private List<String> splitText(DecisionBox box, ConflictDescription conflict) {
		String description = conflict.getDescription();
		// for(String string : description.split("\\["+"[a-zA-Z]*"+"\\]")) {
		ChangePackageVisualizationHelper visualHelper = box.getDecisionManager().getChangePackageVisualizationHelper();
		ArrayList<String> result = new ArrayList<String>();
		for (String string : description.split("\\[")) {
			String[] split = string.split("\\]");
			if (split.length > 1) {
				Object obj = conflict.getValues().get(split[0]);
				String tmp = "";
				if (obj instanceof EObject) {
					tmp = DecisionUtil.getClassAndName((EObject) obj);
					tmp = DecisionUtil.cutString(tmp, 45, true);
				} else if (obj instanceof AbstractOperation) {
					tmp = visualHelper.getDescription((AbstractOperation) obj);
				} else if (obj != null) {
					tmp = obj.toString();
					tmp = DecisionUtil.cutString(tmp, 85, true);
				} else {
					tmp = "";
				}
				tmp = DecisionUtil.stripNewLine(tmp);
				split[0] = "::" + tmp;
			}
			result.addAll(Arrays.asList(split));
		}
		return result;
	}
}

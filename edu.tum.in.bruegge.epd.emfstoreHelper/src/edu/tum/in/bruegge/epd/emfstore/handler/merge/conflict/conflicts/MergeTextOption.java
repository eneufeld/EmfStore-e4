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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AttributeOperation;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.conflict.ConflictOption.OptionType;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionConfig;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * Special option to merge longer texts.
 * 
 * @author wesendon
 */
public class MergeTextOption extends ConflictOption {

	private List<ConflictOption> list;
	private String text;

	/**
	 * Default constructor.
	 */
	public MergeTextOption() {
		super("Select Edited/Merged Value", OptionType.MergeText);
		list = new ArrayList<ConflictOption>();
		setDetailProvider(DecisionConfig.WIDGET_MULTILINE_EDITABLE);
	}

	/**
	 * Add options which should be merged. ATM: only "my" and "their" are supported
	 * 
	 * @param option other option
	 */
	public void add(ConflictOption option) {
		list.add(option);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFullOptionLabel() {
		String result = "";
		for (ConflictOption option : list) {
			result += " " + option.getFullOptionLabel();
		}
		return result;
	}

	/**
	 * Returns "my" text.
	 * 
	 * @return text
	 */
	public String getMyText() {
		ConflictOption option = DecisionUtil.getConflictOptionByType(list, OptionType.MyOperation);
		return (option == null) ? "" : option.getFullOptionLabel();
	}

	/**
	 * Returns "their" text.
	 * 
	 * @return text
	 */
	public String getTheirString() {
		ConflictOption option = DecisionUtil.getConflictOptionByType(list, OptionType.TheirOperation);
		return (option == null) ? "" : option.getFullOptionLabel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AbstractOperation> getOperations() {
		if (text != null) {
			for (ConflictOption option : list) {
				if (option.getType().equals(OptionType.MyOperation)) {
					if (option.getOperations().size() == 0) {
						continue;
					}
					AbstractOperation tmp = option.getOperations().get(0);
					if (tmp instanceof AttributeOperation) {
						option.getOperations().remove(0);
						AttributeOperation mergedOp = (AttributeOperation) EcoreUtil.copy(tmp);
						mergedOp.setIdentifier(EcoreUtil.generateUUID());
						mergedOp.setNewValue(text);
						option.getOperations().add(0, mergedOp);
						return option.getOperations();
					}
				}
			}
		}

		return super.getOperations();
	}

	/**
	 * Set resulted merged text.
	 * 
	 * @param text text
	 */
	public void setMergedText(String text) {
		this.text = text;
	}

	/**
	 * Get merged text.
	 * 
	 * @return text
	 */
	public String getMergedText() {
		return text;
	}
}

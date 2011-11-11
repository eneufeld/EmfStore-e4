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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.common.model.ModelElementId;

import edu.tum.in.bruegge.epd.emfstore.handler.merge.DecisionManager;
import edu.tum.in.bruegge.epd.emfstore.handler.merge.util.DecisionUtil;

/**
 * Container for the conflict description. The description contains placeholder marked through surrounding [ ]. The
 * values for the placeholders are stored in a hashmap. This separation is done in order to support formated text. Use
 * {@link #getResolvedDescription()} in order to get a simple string.
 * 
 * @author wesendon
 */
public class ConflictDescription {

	private Map<String, Object> values;
	private String description;
	private String imageName;
	private DecisionManager decisionManager;

	/**
	 * Default constructor.
	 * 
	 * @param description description
	 */
	public ConflictDescription(String description) {
		this.description = description;
		values = new HashMap<String, Object>();
	}

	/**
	 * Add a place holder.
	 * 
	 * @param key key
	 * @param value value
	 */
	public void add(String key, Object value) {
		if (value instanceof ModelElementId) {
			value = decisionManager.getModelElement((ModelElementId) value);
		}
		values.put(key, value);
	}

	/**
	 * Get description with placeholders unreplaced.
	 * 
	 * @see #getResolvedDescription()
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns values for placeholders.
	 * 
	 * @return map
	 */
	public Map<String, Object> getValues() {
		return values;
	}

	/**
	 * Return name of image.
	 * 
	 * @return name of image.
	 */
	public String getImage() {
		return imageName;
	}

	/**
	 * Set image.
	 * 
	 * @param name name of file
	 */
	public void setImage(String name) {
		this.imageName = name;

	}

	/**
	 * Set description.
	 * 
	 * @param description text
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Resolves description, by replacing the placeholders with it's values.
	 * 
	 * @return text
	 */
	public String getResolvedDescription() {
		String result = description;
		for (String token : values.keySet()) {
			String tmp = "[" + token + "]";
			Object value = values.get(token);
			if (value instanceof EObject) {
				value = DecisionUtil.getClassAndName((EObject) value);
			}
			result = result.replace(tmp, (value != null) ? value.toString() : "");
		}
		return result;
	}

	/**
	 * Sets the decisionManager.
	 * 
	 * @param decisionManager manager.
	 */
	public void setDecisionManager(DecisionManager decisionManager) {
		this.decisionManager = decisionManager;
	}
}

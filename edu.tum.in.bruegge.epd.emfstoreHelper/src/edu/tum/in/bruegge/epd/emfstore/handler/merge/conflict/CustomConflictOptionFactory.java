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

/**
 * Interface for extension point. This is used to register new options for a
 * merge decision.
 * 
 * @author wesendon
 */
public interface CustomConflictOptionFactory {

	/**
	 * Check whether custom option is applicable for given conflict.
	 * 
	 * @param conflict
	 *            to check
	 * @return true if applicable
	 */
	boolean isApplicableConflict(Conflict conflict);

	/**
	 * Creates the custom option. This method is called if {@link #isApplicableConflict(Conflict)} has returned true and
	 * only then.
	 * 
	 * @param conflict
	 *            parent conflict
	 * @return custom option
	 */
	CustomConflictOption createCustomConflictOption(Conflict conflict);

}

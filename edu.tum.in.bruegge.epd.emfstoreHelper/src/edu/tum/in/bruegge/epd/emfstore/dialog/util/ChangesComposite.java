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

import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;

/**
 * An interface for all types of chnages tree composites.
 * 
 * @author Shterev
 */
public interface ChangesComposite {

	/**
	 * Getter for the change packages.
	 * 
	 * @return input ChangePackages
	 */
	List<ChangePackage> getChangePackages();

	/**
	 * Sets the input for this composite.
	 * 
	 * @param changes the new ChangePackages
	 */
	void setInput(List<ChangePackage> changes);

}

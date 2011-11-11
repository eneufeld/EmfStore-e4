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
package edu.tum.in.bruegge.epd.emfstore.dialog;

import org.eclipse.jface.dialogs.DialogTray;

/**
 * This class defines DialogTrays which will be added to the CommitDialog.
 * 
 * @author Shterev
 */
public abstract class CommitDialogTray extends DialogTray {

	/**
	 * Default initialization.
	 * 
	 * @param commitDialog
	 *            the commit dialog
	 */
	public abstract void init(CommitDialog commitDialog);

	/**
	 * Disposes the tray and its contents.
	 */
	public void dispose() {
	}

	/**
	 * Notifies that the OK button of the CommitDialog has been pressed.
	 */
	public void okPressed() {

	}
}

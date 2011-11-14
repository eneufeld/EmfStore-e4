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
package edu.tum.in.bruegge.epd.emfstore.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.tum.in.bruegge.epd.emfstore.dialog.LoginDialog;

/**
 * Resolves the ACOrgUnit, opens the login dialog and handles any login
 * procedures.
 * 
 * @author Shterev
 */
public class LoginHandler extends AbstractHandler {

	private Usersession usersession;

	/**
	 * Default constructor.
	 * 
	 * @param usersession
	 *            the usersession
	 */
	public LoginHandler(Usersession usersession) {
		this.usersession = usersession;
	}

	/**
	 * A constructor with a projectspace as an argument.
	 * 
	 * @param projectSpace
	 *            the projectspace
	 */
	public LoginHandler(ProjectSpace projectSpace) {
		if (projectSpace.getUsersession() == null) {
			throw new IllegalArgumentException("The project space is not associated with a usersession");
		}
		usersession = projectSpace.getUsersession();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return true if the login was successful, false if the login was
	 *         canceled.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		LoginDialog loginDialog = new LoginDialog(parentShell, usersession);
		loginDialog.open();
		return loginDialog.getReturnCode();
	}

	/**
	 * Returns the usersession.
	 * 
	 * @return usersession
	 */
	public Usersession getUsersession() {
		return usersession;
	}

}

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

import java.util.HashSet;

import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.emfstore.client.model.ServerInfo;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.Workspace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;

/**
 * Content provider to load the saved sessions for a server info.
 * 
 * @author shterev
 */
public class UsersessionsContentProvider extends AdapterFactoryContentProvider {

	private static Workspace currentWorkspace = WorkspaceManager.getInstance().getCurrentWorkspace();
	private ServerInfo serverInfo;

	/**
	 * Default constructor.
	 * 
	 * @param serverInfo limits the displayed usersession to this server info.
	 */
	public UsersessionsContentProvider(ServerInfo serverInfo) {
		super(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
		this.serverInfo = serverInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getElements(Object object) {
		if (viewer.getInput() instanceof Workspace) {
			HashSet<Usersession> tempList = new HashSet<Usersession>();
			for (Usersession session : currentWorkspace.getUsersessions()) {
				if (session.getServerInfo() != null && session.getServerInfo().equals(serverInfo)) {
					tempList.add(session);
				}
			}
			return tempList.toArray();
		}
		return super.getElements(object);
	}
}

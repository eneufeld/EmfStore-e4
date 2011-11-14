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

import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.emfstore.client.model.Usersession;

/**
 * Label provider for the saved sessions for a server info.
 * 
 * @author shterev
 */
public class UsersessionsLabelProvider extends AdapterFactoryLabelProvider {

	/**
	 * Default constructor.
	 */
	public UsersessionsLabelProvider() {
		super(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getColumnText(Object object, int columnIndex) {

		if (object instanceof Usersession) {
			Usersession session = (Usersession) object;
			return session.getUsername();
		}
		return super.getColumnText(object, columnIndex);
	}
}

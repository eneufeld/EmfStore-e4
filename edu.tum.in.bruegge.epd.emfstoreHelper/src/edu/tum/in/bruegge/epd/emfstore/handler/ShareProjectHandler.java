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

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.emf.ecp.common.util.DialogHandler;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.Workspace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.accesscontrol.AccessControlHelper;
import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.tum.in.bruegge.epd.emfstore.helper.EmfStoreHelper;

/**
 * Share a project with the server.
 * 
 * @author koegel
 */
public class ShareProjectHandler extends AbstractHandler {
	
	@Override
	@Execute
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		
		Usersession session=EmfStoreHelper.INSTANCE.getUserSession();
		if(!session.isLoggedIn()){
			LoginHandler lh=new LoginHandler(EmfStoreHelper.INSTANCE.getUserSession());
			lh.execute(event);
		}
		try {
			AccessControlHelper accessControlHelper = new AccessControlHelper(
					session);
			try {
				accessControlHelper.checkServerAdminAccess();
			} catch (org.eclipse.emf.emfstore.server.exceptions.AccessControlException e) {
				MessageDialog
						.openError(HandlerUtil.getActiveShell(event), "",
								"Only administrators can create new projects on the server.");
				return null;
			}
			
				EmfStoreHelper.INSTANCE.shareProject(((IStructuredSelection)HandlerUtil.getCurrentSelection(event)).getFirstElement());
			
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event), null,
					"Your project was successfully shared!");
			// BEGIN SUPRESS CATCH EXCEPTION
		} catch (RuntimeException e) {
			DialogHandler.showExceptionDialog(e);
			WorkspaceUtil.logWarning("RuntimeException in "
					+ ShareProjectHandler.class.getName(), e);
			// throw e;
		}
		
		
		return null;
	}
	

//	protected void initUsersession() {
//
//		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
//				.getShell();
//		ElementListSelectionDialog dlg = new ElementListSelectionDialog(shell,
//				new AdapterFactoryLabelProvider(new ComposedAdapterFactory(
//						ComposedAdapterFactory.Descriptor.Registry.INSTANCE)));
//		Workspace currentWorkspace = WorkspaceManager.getInstance()
//				.getCurrentWorkspace();
//		Collection<Usersession> allSessions = currentWorkspace
//				.getUsersessions();
//		dlg.setElements(allSessions.toArray());
//		dlg.setTitle("Select Usersession");
//		dlg.setBlockOnOpen(true);
//		if (dlg.open() == Window.OK) {
//			Object result = dlg.getFirstResult();
//			if (result instanceof Usersession) {
//				EmfStoreHelper.INSTANCE.setUserSession((Usersession) result);
//			}
//		}
//	}	

}

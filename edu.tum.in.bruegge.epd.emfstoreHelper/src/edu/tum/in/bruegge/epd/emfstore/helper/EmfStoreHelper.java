package edu.tum.in.bruegge.epd.emfstore.helper;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.emfstore.client.model.ModelFactory;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.ServerInfo;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectInfo;

import edu.tum.in.bruegge.epd.emfstorehelper.Activator;
/**
 * 
 * @author Eugen Neufeld
 * Helper class for e4 Application
 *
 */

public class EmfStoreHelper {

	public static EmfStoreHelper INSTANCE = new EmfStoreHelper();

	private ProjectSpace projectSpace;

	private EmfStoreHelper() {
		try {
			Usersession userSession = WorkspaceManager.getInstance()
					.getCurrentWorkspace().getUsersessions().get(0);
			userSession.logIn();

			for (ProjectSpace ps : WorkspaceManager.getInstance()
					.getCurrentWorkspace().getProjectSpaces()) {
				// TODO check if project already here
				// projectSpace=ps;
			}
			//try to update, perhaps check local version vs remote version
			if (projectSpace != null) {
				projectSpace.update();
			} else { // no local project available -> try to checkout from emfstore
				List<ProjectInfo> projects = WorkspaceManager.getInstance()
						.getConnectionManager()
						.getProjectList(userSession.getSessionId());

				ProjectInfo relevant = null;
				for (ProjectInfo info : projects) {
					// TODO do some chekcing
					relevant = info;
					break;
				}
				if (relevant != null) {
					projectSpace = WorkspaceManager.getInstance()
							.getCurrentWorkspace()
							.checkout(userSession, relevant);
				}
			}
		} catch (EmfStoreException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e
							.getMessage()));
		}
	}

	private static ServerInfo getServerInfo() {
		ServerInfo serverInfo = ModelFactory.eINSTANCE.createServerInfo();
		serverInfo.setPort(8080);
		// serverInfo.setUrl("127.0.0.1");
		serverInfo.setUrl("localhost");
		serverInfo
				.setCertificateAlias("emfstore test certificate (do not use in production!)");

		return serverInfo;
	}

	public EObject getRoot() {
		int size = projectSpace.getProject().getModelElements().size();
		return projectSpace.getProject().getModelElements().get(size - 1);
	}

	public void commit() {
		try {
			projectSpace.commit();
		} catch (EmfStoreException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e
							.getMessage()));
		}
	}

	public void update() {
		try {
			projectSpace.update();
		} catch (EmfStoreException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e
							.getMessage()));
		}
	}

	public void share() {
		//create Projectspace
		projectSpace = org.eclipse.emf.emfstore.client.model.ModelFactory.eINSTANCE
				.createProjectSpace();
		projectSpace
				.setProject(org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
						.createProject());
		projectSpace.setProjectName("");
		projectSpace.setProjectDescription("");
		projectSpace
				.setLocalOperations(org.eclipse.emf.emfstore.client.model.ModelFactory.eINSTANCE
						.createOperationComposite());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getLoadOptions().putAll(ModelUtil.getResourceLoadOptions());
		projectSpace.initResources(resourceSet);

		Usersession userSession = WorkspaceManager.getInstance()
				.getCurrentWorkspace().getUsersessions().get(0);
		projectSpace.setUsersession(userSession);
		projectSpace.eResource().getContents().add(userSession);
		projectSpace.eResource().getContents().add(userSession.getServerInfo());
		//add model to project
		//projectSpace.getProject().addModelElement(null);
		try {
			projectSpace.shareProject(userSession);
		} catch (EmfStoreException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e
							.getMessage()));
		}
	}

	public ProjectSpace getProjectSpace() {
		return projectSpace;
	}

}

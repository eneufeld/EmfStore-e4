package edu.tum.in.bruegge.epd.emfstore.helper;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
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
 * @author Eugen Neufeld Helper class for e4 Application
 * 
 */

public class EmfStoreHelper {

	public static EmfStoreHelper INSTANCE = new EmfStoreHelper();

	private ProjectSpace projectSpace;
	private Usersession userSession;

	private ResourceSetImpl resourceSetImpl;
	@Inject
	private Logger logger;

	private EmfStoreHelper() {
		initResourceSet();
		try {
			userSession = WorkspaceManager.getInstance().getCurrentWorkspace()
					.getUsersessions().get(0);
			userSession.logIn();

			for (ProjectSpace ps : WorkspaceManager.getInstance()
					.getCurrentWorkspace().getProjectSpaces()) {
				// TODO check if project already here
				// projectSpace=ps;
			}
			// try to update, perhaps check local version vs remote version
			if (projectSpace != null) {
				projectSpace.update();
			} else { // no local project available -> try to checkout from
						// emfstore
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

	private void initResourceSet() {
		resourceSetImpl = new ResourceSetImpl();
		resourceSetImpl.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new E4XMIResourceFactory());

		resourceSetImpl.getPackageRegistry().put(ApplicationPackageImpl.eNS_URI,
				ApplicationPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(CommandsPackageImpl.eNS_URI,
				CommandsPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(UiPackageImpl.eNS_URI, UiPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry()
				.put(MenuPackageImpl.eNS_URI, MenuPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(BasicPackageImpl.eNS_URI,
				BasicPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(AdvancedPackageImpl.eNS_URI,
				AdvancedPackageImpl.eINSTANCE);
		resourceSetImpl
				.getPackageRegistry()
				.put(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI,
						org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);
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
		if (projectSpace != null) {
			int size = projectSpace.getProject().getModelElements().size();
			return projectSpace.getProject().getModelElements().get(size - 1);
		}
		return null;
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

	

	public ProjectSpace getProjectSpace() {
		return projectSpace;
	}

	public Usersession getUserSession() {
		return userSession;
	}

	private String projectName = "test";
	private String projectDescription = "E4 Project";

	public void shareProject(Object element) {
		projectSpace = org.eclipse.emf.emfstore.client.model.ModelFactory.eINSTANCE
				.createProjectSpace();
		projectSpace
				.setProject(org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
						.createProject());
		projectSpace.setProjectName(projectName);
		projectSpace.setProjectDescription(projectDescription);
		projectSpace
				.setLocalOperations(org.eclipse.emf.emfstore.client.model.ModelFactory.eINSTANCE
						.createOperationComposite());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getLoadOptions().putAll(ModelUtil.getResourceLoadOptions());
		projectSpace.initResources(resourceSet);

		projectSpace.setUsersession(userSession);
		projectSpace.eResource().getContents().add(userSession);
		projectSpace.eResource().getContents().add(userSession.getServerInfo());
		
		
		URI uri=URI.createFileURI(((File)element).getLocation().toString());
		Resource resource;
		try {
			resource = resourceSetImpl.getResource(uri, true);
		} catch (Exception e) {
			// TODO We could use diagnostics for better analyzing the error
			logger.error(e);
			resource = resourceSetImpl.getResource(uri, false);
		}
		projectSpace.getProject().addModelElement( resource.getContents().get(0));
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
	

	public void setUserSession(Usersession userSession) {
		this.userSession = userSession;
	}

}

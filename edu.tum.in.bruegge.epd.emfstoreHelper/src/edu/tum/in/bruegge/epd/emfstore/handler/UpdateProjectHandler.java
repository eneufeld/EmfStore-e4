package edu.tum.in.bruegge.epd.emfstore.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.exceptions.ChangeConflictException;
import org.eclipse.emf.emfstore.client.model.exceptions.NoChangesOnServerException;
import org.eclipse.emf.emfstore.client.model.observers.UpdateObserver;
import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.tum.in.bruegge.epd.emfstore.dialog.UpdateDialog;
import edu.tum.in.bruegge.epd.emfstore.helper.EmfStoreHelper;

public class UpdateProjectHandler extends AbstractHandler implements
		UpdateObserver {
	private Shell shell;

	@Execute
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// EmfStoreHelper.INSTANCE.update();
		shell = HandlerUtil.getActiveShell(event);
		try {
			update(EmfStoreHelper.INSTANCE.getProjectSpace());
		} catch (EmfStoreException e) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "ERROR",
					e.getMessage());
		}
		return null;
	}

	/**
	 * Updates the {@link ProjectSpace}.
	 * 
	 * @param projectSpace
	 *            the target project space
	 * @throws EmfStoreException
	 *             if any.
	 */
	public void update(final ProjectSpace projectSpace)
			throws EmfStoreException {
		Usersession usersession = projectSpace.getUsersession();
		if (usersession == null) {
			MessageDialog
					.openInformation(getShell(), null,
							"This project is not yet shared with a server, you cannot update.");
			return;
		}

		try {
			projectSpace.getBaseVersion();
			projectSpace.update(VersionSpec.HEAD_VERSION,
					UpdateProjectHandler.this);
			if(PlatformUI.getWorkbench()!=null){
				
			// explicitly refresh the decorator since no simple attribute has
			// been changed
			// (as opposed to committing where the dirty property is being set)
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					PlatformUI
							.getWorkbench()
							.getDecoratorManager()
							.update("org.eclipse.emf.emfstore.client.ui.decorators.VersionDecorator");
				}
			});
			}
		} catch (ChangeConflictException e1) {
			handleChangeConflictException(e1);
		} catch (NoChangesOnServerException e) {
			MessageDialog.openInformation(getShell(), "No need to update",
					"Your project is up to date, you do not need to update.");
		}
	}

	private void handleChangeConflictException(
			ChangeConflictException conflictException) {
		ProjectSpace projectSpace = conflictException.getProjectSpace();
		try {
			PrimaryVersionSpec targetVersion = projectSpace
					.resolveVersionSpec(VersionSpec.HEAD_VERSION);
			projectSpace.merge(targetVersion, new MergeProjectHandler(
					conflictException));
		} catch (EmfStoreException e) {
			WorkspaceUtil
					.logException("Exception when merging the project!", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean inspectChanges(ProjectSpace projectSpace,
			List<ChangePackage> changePackages) {
		UpdateDialog updateDialog = new UpdateDialog(getShell(), projectSpace,
				changePackages);
		int returnCode = updateDialog.open();
		if (returnCode == Window.OK) {
			return true;
		}
		return false;
	}

	private Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.model.observers.UpdateObserver#updateCompleted()
	 */
	public void updateCompleted(ProjectSpace projectSpace) {
	}
}

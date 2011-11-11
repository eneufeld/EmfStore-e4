package edu.tum.in.bruegge.epd.emfstore.handler;

import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.exceptions.CommitCanceledException;
import org.eclipse.emf.emfstore.client.model.exceptions.NoLocalChangesException;
import org.eclipse.emf.emfstore.client.model.observers.CommitObserver;
import org.eclipse.emf.emfstore.server.exceptions.BaseVersionOutdatedException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.tum.in.bruegge.epd.emfstore.dialog.CommitDialog;
import edu.tum.in.bruegge.epd.emfstore.helper.EmfStoreHelper;

public class CommitProjectHandler extends AbstractHandler implements
		CommitObserver {
	private Shell shell;
	private LogMessage logMessage = VersioningFactory.eINSTANCE
			.createLogMessage();
	private String predefinedCommitMessage="E4 Commit";
	private Shell getShell() {
		return shell;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// EmfStoreHelper.INSTANCE.update();
		shell = HandlerUtil.getActiveShell(event);
		try {
			handleCommit(EmfStoreHelper.INSTANCE.getProjectSpace());
		} catch (EmfStoreException e) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "ERROR",
					e.getMessage());
		}
		return null;
	}

	public void handleCommit(ProjectSpace projectSpace)
			throws EmfStoreException {
		try {
			ChangePackage changePackage = handlePrepareCommit(projectSpace);
			handleFinalizeCommit(projectSpace, changePackage);

		} catch (CommitCanceledException e) {
			// nothing to do. can be ignored
		}
	}

	/**
	 * Shows the user the current dirty changes in the project space that should
	 * be committed. If there are some conflicts a conflict merger will be
	 * shown. At the end a merged change package will be returned.
	 * 
	 * @param projectSpace
	 *            the project space that should be committed
	 * @return a change package
	 * @throws EmfStoreException
	 *             if the preparation fails
	 * @throws CommitCanceledException
	 *             if the user cancels the commit
	 */
	public ChangePackage handlePrepareCommit(ProjectSpace projectSpace)
			throws EmfStoreException, CommitCanceledException {
		Usersession usersession = projectSpace.getUsersession();

		if (usersession == null) {
			MessageDialog
					.openInformation(getShell(), null,
							"This project is not yet shared with a server, you cannot commit.");
		}

		try {
			return projectSpace.prepareCommit(CommitProjectHandler.this);

		} catch (BaseVersionOutdatedException e) {
			return handleBaseVersionException(projectSpace);

		} catch (NoLocalChangesException e) {
			MessageDialog.openInformation(getShell(), null,
					"No local changes in your project. No need to commit.");
			throw new CommitCanceledException(
					"No local changes in project space.");
		}
	}
	private ChangePackage handleBaseVersionException(
			final ProjectSpace projectSpace) throws CommitCanceledException,
			EmfStoreException {
		MessageDialog dialog = new MessageDialog(
				null,
				"Confirmation",
				null,
				"Your project is outdated, you need to update before commit. Do you want to update now?",
				MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
		int result = dialog.open();
		if (result == 0) {
			UpdateProjectHandler projectHandler = new UpdateProjectHandler();
			projectHandler.setShell(getShell());
			projectHandler.update(projectSpace);
			return handlePrepareCommit(projectSpace);
		}
		throw new CommitCanceledException(
				"Changes have been canceled by the user.");
	}
	/**
	 * A change package that has been returned by the handlePrepareCommit method
	 * will be now committed and a new revision will be created on the server.
	 * 
	 * @param projectSpace
	 *            a project space
	 * @param changePackage
	 *            a change package
	 * @throws EmfStoreException
	 *             if any error in the EmfStore occurs
	 */
	public void handleFinalizeCommit(ProjectSpace projectSpace,
			ChangePackage changePackage) throws EmfStoreException {
		projectSpace.finalizeCommit(changePackage, logMessage,
				CommitProjectHandler.this);
	}

	public boolean inspectChanges(ProjectSpace projectSpace,
			ChangePackage changePackage) {
		if (changePackage.getOperations().isEmpty()) {
			MessageDialog
					.openInformation(
							getShell(),
							"No local changes",
							"Your local changes were mutually exclusive.\nThey are no changes pending for commit.");
			return false;
		}
		CommitDialog commitDialog = new CommitDialog(getShell(), changePackage,
				projectSpace);
		if (predefinedCommitMessage != null) {
			if (changePackage.getLogMessage() == null) {
				changePackage.setLogMessage(logMessage);
			}

			changePackage.getLogMessage().setMessage(predefinedCommitMessage);
		}
		int returnCode = commitDialog.open();
		if (returnCode == Window.OK) {
//			logMessage.setAuthor(usersession.getUsername());
			logMessage.setClientDate(new Date());
			logMessage.setMessage(commitDialog.getLogText());
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.model.observers.CommitObserver#commitCompleted()
	 */
	public void commitCompleted(ProjectSpace projectSpace,
			PrimaryVersionSpec versionSpec) {
	}
}

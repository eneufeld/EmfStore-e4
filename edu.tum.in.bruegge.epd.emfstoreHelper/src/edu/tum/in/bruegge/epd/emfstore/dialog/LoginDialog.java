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

import org.eclipse.emf.emfstore.client.model.ModelFactory;
import org.eclipse.emf.emfstore.client.model.ServerInfo;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.Workspace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import edu.tum.in.bruegge.epd.emfstore.dialog.util.UsersessionsContentProvider;
import edu.tum.in.bruegge.epd.emfstore.dialog.util.UsersessionsLabelProvider;
import edu.tum.in.bruegge.epd.emfstorehelper.Activator;

/**
 * Creates a new login dialog.
 * 
 * @author shterev
 */
public class LoginDialog extends TitleAreaDialog {

	/**
	 * A EMFStoreCommand for logging in.
	 * 
	 * @author shterev
	 */
	private final class LoginEMFStoreCommand extends EMFStoreCommand {
		@Override
		protected void doRun() {
			if (usersession == null) {
				setErrorMessage("Please select a usersession");
				return;
			}
			usersession.setSavePassword(savePassButton.getSelection());
			if (userText.isEnabled()) {
				usersession.setUsername(userText.getText());
			}
			if (isPasswordModified) {
				usersession.setPassword(passText.getText());
			}
			currentWorkspace.save();
			try {
				usersession.logIn();
				usersession.getServerInfo().setLastUsersession(usersession);
				setReturnCode(OK);
				close();
			} catch (EmfStoreException e) {
				setErrorMessage(e.getMessage());
			}
		}
	}

	/**
	 * A Mouse Adapter for deleting serverinfos.
	 * 
	 * @author shterev
	 */
	private final class RemoveServerInfoMouseAdapter extends MouseAdapter {
		private final TableViewer tableViewer;

		private RemoveServerInfoMouseAdapter(TableViewer tableViewer) {
			this.tableViewer = tableViewer;
		}

		@Override
		public void mouseUp(MouseEvent e) {
			IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
			if (!selection.isEmpty()) {
				Object firstElement = selection.getFirstElement();
				final Usersession session = (Usersession) firstElement;
				for (ServerInfo info : currentWorkspace.getServerInfos()) {
					if (info.getLastUsersession() != null && info.getLastUsersession().equals(session)) {
						MessageDialog.openError(getShell(), "Cannot remove the usersession",
							"The session is acssociated with one or more servers and cannot be deleted!");
						return;
					}
				}
				Boolean confirm = MessageDialog.openConfirm(getShell(), "Confirm deletion",
					"Are you sure you want to remove this session?");
				if (confirm) {
					new EMFStoreCommand() {
						@Override
						protected void doRun() {
							currentWorkspace.getUsersessions().remove(session);
							currentWorkspace.save();
							tableViewer.setInput(currentWorkspace);
						}
					}.run();
				}
			}
		}
	}

	private static final String NEW_SESSION_NAME = "new session";
	private static final String FIXE_PW_TEXT = "sysiphus";
	private ServerInfo serverInfo;
	private Usersession usersession;
	private Composite contents;
	private boolean singleSession;
	private Workspace currentWorkspace;
	private Text passText;
	private Text userText;
	private Button savePassButton;
	private boolean isPasswordModified;
	private String exception;

	private LoginDialog(Shell parent) {
		super(parent);
		setBlockOnOpen(true);
		this.currentWorkspace = WorkspaceManager.getInstance().getCurrentWorkspace();
	}

	/**
	 * Default constructor - initialized with a usersession. This dialog will
	 * try to log on the server using the specified usersession. Adding or
	 * choosing a new session is not permitted.
	 * 
	 * @param parentShell
	 *            the parent shell.
	 * @param usersession
	 *            the usersession
	 */
	public LoginDialog(Shell parentShell, Usersession usersession) {
		this(parentShell);
		this.singleSession = false;
		this.usersession = usersession;
		this.serverInfo = usersession.getServerInfo();
	}

	/**
	 * This dialog will show all saved usersessions for this ServerInfo.
	 * Creating or deleting usersessions is allowed.
	 * 
	 * @param parentShell
	 *            the parent shell.
	 * @param pServerInfo
	 *            the server info
	 */
	public LoginDialog(Shell parentShell, ServerInfo pServerInfo) {
		this(parentShell);
		this.singleSession = false;
		this.serverInfo = pServerInfo;
		// look for the last used usersession
		if (pServerInfo.getLastUsersession() != null) {
			usersession = pServerInfo.getLastUsersession();
		} else {
			// look for the first usersession associated with this ServerInfo
			for (Usersession s : currentWorkspace.getUsersessions()) {
				if (pServerInfo.equals(s.getServerInfo())) {
					usersession = s;
					break;
				}
			}
		}
		// if still null - i.e. if there are no sessions for this server info,
		// create a new one
		if (usersession == null) {
			usersession = createNewSession();
		}
	}

	private Usersession createNewSession() {
		final Usersession session = ModelFactory.eINSTANCE.createUsersession();
		session.setUsername(NEW_SESSION_NAME);
		new EMFStoreCommand() {
			@Override
			protected void doRun() {
				currentWorkspace.getUsersessions().add(session);
				session.setServerInfo(serverInfo);
				currentWorkspace.save();
			}
		}.run();
		return session;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		contents = new Composite(parent, SWT.NONE);
		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(contents);
		if (exception != null) {
			setErrorMessage(exception);
		}

		getShell().setText("Authentication required");
		setTitle("Log in to " + getServerInfoName());
		setMessage("Please enter your username and password");
		setTitleImage(Activator.getImageDescriptor("icons/login_icon.png").createImage());

		if (!singleSession) {
			Composite sessionsComposite = new Composite(contents, SWT.NONE);
			GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(sessionsComposite);
			GridDataFactory.fillDefaults().grab(false, true).applyTo(sessionsComposite);
			createSessionsList(sessionsComposite);
		}
		createInputFields(contents);

		return contents;
	}

	private String getServerInfoName() {
		return serverInfo.getName() + " [" + serverInfo.getUrl() + "]";
	}

	private void createInputFields(Composite root) {
		Composite parent = new Composite(root, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 5).applyTo(parent);

		Label userLabel = new Label(parent, SWT.WRAP);
		userLabel.setText("Username");

		userText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(userText);

		Label passLabel = new Label(parent, SWT.WRAP);
		passLabel.setText("Password");

		passText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(passText);

		Label savePassLabel = new Label(parent, SWT.WRAP);
		savePassLabel.setText("Save password");

		savePassButton = new Button(parent, SWT.CHECK);
		loadSession(usersession);
		passText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				isPasswordModified = true;
			}
		});

		userText.setFocus();

	}

	private void createSessionsList(Composite parent) {
		final TableViewer tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().hint(100, -1).grab(false, true).applyTo(tableViewer.getControl());
		tableViewer.setContentProvider(new UsersessionsContentProvider(serverInfo));
		tableViewer.setLabelProvider(new UsersessionsLabelProvider());
		tableViewer.setInput(currentWorkspace);
		tableViewer.setSelection(new StructuredSelection(usersession));
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			/**
			 * {@inheritDoc}
			 */
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = tableViewer.getSelection();
				if (loadSessionFromSelection(selection)) {
					okPressed();
				}
			}
		});

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			/**
			 * {@inheritDoc}
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = tableViewer.getSelection();
				loadSessionFromSelection(selection);
			}
		});

		Composite toolbar = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).spacing(0, 0).applyTo(toolbar);

		ImageHyperlink addButton = new ImageHyperlink(toolbar, SWT.TOP);
		addButton.setImage(Activator.getImageDescriptor("icons/add.png").createImage());
		addButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				Usersession newSession = createNewSession();
				loadSession(newSession);
				tableViewer.setInput(currentWorkspace);
			}
		});

		ImageHyperlink removeButton = new ImageHyperlink(toolbar, SWT.TOP);
		removeButton.setImage(Activator.getImageDescriptor("icons/remove.png").createImage());
		removeButton.addMouseListener(new RemoveServerInfoMouseAdapter(tableViewer));
	}

	/**
	 * Selects the given session in the list.
	 * 
	 * @param session
	 *            the usersession
	 */
	private void loadSession(final Usersession session) {
		if (session != null && session.getUsername() == null) {
			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					session.setUsername("");
				}
			}.run();
		}
		usersession = session;
		if (session == null || NEW_SESSION_NAME.equals(session.getUsername())) {
			userText.setText(NEW_SESSION_NAME);
			userText.setEnabled(true);
			passText.setText("");
			savePassButton.setSelection(false);
		} else {
			userText.setText(session.getUsername());
			userText.setEnabled(false);
			String pass = "";
			if (session.getPassword() != null) {
				pass = FIXE_PW_TEXT;
			}
			passText.setText(pass);
			savePassButton.setSelection(session.isSavePassword());
		}
	}

	/**
	 * Commences the login.
	 */
	@Override
	protected void okPressed() {
		new LoginEMFStoreCommand().run();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				PlatformUI.getWorkbench().getDecoratorManager()
					.update("org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.LoginDecorator");
			}
		});
	}

	private boolean loadSessionFromSelection(ISelection selection) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Usersession session = null;
			if (structuredSelection.getFirstElement() instanceof Usersession) {
				session = (Usersession) structuredSelection.getFirstElement();
				loadSession(session);
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int open() {
		return open(true);
	}

	/**
	 * @see #open()
	 * @param autologin
	 *            if a login request should be performed automatically in case
	 *            the password for the last used usersession was saved.
	 * @return @see {@link #open()}
	 */
	public int open(boolean autologin) {
		if (usersession != null && usersession.getUsername() != null && usersession.getPassword() != null && autologin) {
			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					try {
						usersession.logIn();
					} catch (EmfStoreException e) {
						exception = e.getMessage();
					}
				}
			}.run();
			close();
			setReturnCode(OK);
			return OK;
		}
		int ret = super.open();
		return ret;

	}

}

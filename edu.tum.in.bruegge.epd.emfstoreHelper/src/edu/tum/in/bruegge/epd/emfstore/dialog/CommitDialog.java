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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.server.model.notification.ESNotification;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.tum.in.bruegge.epd.emfstore.dialog.util.TabbedChangesComposite;
import edu.tum.in.bruegge.epd.emfstorehelper.Activator;

/**
 * This class shows a ChangesTreeComposite and a Text control to enter commit message.
 * 
 * @author Hodaie
 * @author Shterev
 */
public class CommitDialog extends TitleAreaDialog implements KeyListener {

	private Text txtLogMsg;
	private String logMsg = "";
	private ChangePackage changes;
	private EList<String> oldLogMessages;
	private HashMap<AbstractOperation, ArrayList<ESNotification>> operationsMap;
	private ProjectSpace activeProjectSpace;
	private HashMap<String, CommitDialogTray> trays;

	/**
	 * Constructor.
	 * 
	 * @param parentShell shell
	 * @param changes the {@link ChangePackage} to be displayed
	 * @param activeProjectSpace ProjectSpace that will be committed
	 */
	public CommitDialog(Shell parentShell, ChangePackage changes, ProjectSpace activeProjectSpace) {
		super(parentShell);
		this.setShellStyle(this.getShellStyle() | SWT.RESIZE);
		this.changes = changes;
		this.activeProjectSpace = activeProjectSpace;
		trays = new HashMap<String, CommitDialogTray>();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
			"org.eclipse.emf.emfstore.client.ui.commitdialog.tray");
		for (IConfigurationElement c : config) {
			try {
				CommitDialogTray tray = (CommitDialogTray) c.createExecutableExtension("class");
				String name = c.getAttribute("name");
				tray.init(this);
				trays.put(name, tray);
			} catch (CoreException e) {

			}
		}
	}

	/**
	 * @return the change package
	 */
	public ChangePackage getChangePackage() {
		return changes;
	}

	/**
	 * @return the active project space
	 */
	public ProjectSpace getActiveProjectSpace() {
		return activeProjectSpace;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		oldLogMessages = activeProjectSpace.getOldLogMessages();

		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		contents.setLayout(new GridLayout(2, false));

		setTitle("Commit your changes");
		setMessage("Don't forget the commit message!");
		setTitleImage(Activator.getImageDescriptor("icons/dontForget.png").createImage());

		// Log message
		Label lblLogMsg = new Label(contents, SWT.NONE);
		lblLogMsg.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		lblLogMsg.setText("Log message:");

		txtLogMsg = new Text(contents, SWT.MULTI | SWT.LEAD | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.TOP).hint(1, 75)
			.applyTo(txtLogMsg);
		String logMsg = "";
		LogMessage logMessage = changes.getLogMessage();
		if (logMessage != null && logMessage.getMessage() != null) {
			logMsg = logMessage.getMessage();
		} else if (oldLogMessages != null && oldLogMessages.size() > 0) {
			logMsg = oldLogMessages.get(oldLogMessages.size() - 1);
		}
		txtLogMsg.setText(logMsg);
		txtLogMsg.selectAll();
		// to implement a shortcut for submitting the commit
		txtLogMsg.addKeyListener(this);

		// previous log messages
		Label oldLabel = new Label(contents, SWT.NONE);
		oldLabel.setText("Previous messages:");
		final Combo oldMsg = new Combo(contents, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(oldMsg);

		ArrayList<String> oldLogMessagesCopy = new ArrayList<String>();
		oldLogMessagesCopy.addAll(oldLogMessages);
		Collections.reverse(oldLogMessagesCopy);
		oldMsg.setItems(oldLogMessagesCopy.toArray(new String[0]));
		oldMsg.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing to do here
			}

			public void widgetSelected(SelectionEvent e) {
				txtLogMsg.setText(oldMsg.getItem(oldMsg.getSelectionIndex()));
			}

		});

		// ChangesTree
		ArrayList<ChangePackage> changePackages = new ArrayList<ChangePackage>();
		changePackages.add(changes);
		TabbedChangesComposite changesComposite = new TabbedChangesComposite(contents, SWT.BORDER, changePackages,
			getActiveProjectSpace().getProject());
		changesComposite.setShowRootNodes(false);
		changesComposite.setReverseNodes(false);
		changesComposite.setInput(changePackages);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).applyTo(changesComposite);

		operationsMap = new HashMap<AbstractOperation, ArrayList<ESNotification>>();
		for (AbstractOperation op : changes.getOperations()) {
			operationsMap.put(op, new ArrayList<ESNotification>());
		}
		return contents;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureShell(Shell newShell) {

		super.configureShell(newShell);
		newShell.setText("Commit");
		Rectangle area = newShell.getShell().getParent().getClientArea();
		int width = area.width * 2 / 3;
		int height = area.height * 2 / 3;
		newShell.setBounds((area.width - width) / 2, (area.height - height) / 2, width, height);
		newShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for (CommitDialogTray tray : trays.values()) {
					tray.dispose();
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void okPressed() {
		logMsg = txtLogMsg.getText();

		// suppress duplicates
		if (!oldLogMessages.contains(logMsg)) {
			oldLogMessages.add(logMsg);
		}

		// remove older messages
		if (oldLogMessages.size() > 10) {
			// the list can only grow one element at a time,
			// so only one element should be deleted
			oldLogMessages.remove(0);
		}

		for (CommitDialogTray t : trays.values()) {
			t.okPressed();
		}
		// add the newly created notifications to the change package
		for (ArrayList<ESNotification> list : operationsMap.values()) {
			changes.getNotifications().addAll(list);
		}

		super.okPressed();
	}

	/**
	 * @return the log message that has been set by the user.
	 */
	public String getLogText() {
		return logMsg.equals("") ? "<Empty log message>" : logMsg;
	}

	/**
	 * handles the pressing of Ctrl+ENTER: OKpressed() is called. {@inheritDoc}
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		if (e.keyCode == SWT.CR && (e.stateMask & SWT.MOD1) != 0) {
			this.okPressed();
		}
	}

	/**
	 * does nothing. {@inheritDoc}
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// final String notifyUsers = "Notify users";
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
			"org.eclipse.emf.emfstore.client.ui.commitdialog.tray");
		for (IConfigurationElement c : config) {
			final String name = c.getAttribute("name");
			final CommitDialogTray tray = trays.get(name);
			if (tray != null) {
				final Button notificationsButton = createButton(parent, 2138, name + " >>", false);
				notificationsButton.addSelectionListener(new SelectionAdapter() {
					private boolean isOpen;

					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!isOpen) {
							openTray(tray);
							notificationsButton.setText(name + " <<");
							Rectangle bounds = getShell().getBounds();
							bounds.x -= 100;
							getShell().setBounds(bounds);
						} else {
							closeTray();
							notificationsButton.setText(name + " >>");
							Rectangle bounds = getShell().getBounds();
							bounds.x += 100;
							getShell().setBounds(bounds);
						}
						isOpen = !isOpen;
					}
				});
			}
		}
		super.createButtonsForButtonBar(parent);
	}

	/**
	 * @return the operations.
	 */
	public List<AbstractOperation> getOperations() {
		return changes.getOperations();
	}

}

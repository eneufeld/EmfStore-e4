/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.PatternFilter;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractCommandSelectionDialog extends TitleAreaDialog {
	private IModelResource resource;
	private TableViewer viewer;
	protected Messages Messages;

	public AbstractCommandSelectionDialog(Shell parentShell, IModelResource resource, Messages Messages) {
		super(parentShell);
		this.resource = resource;
		this.Messages = Messages;
	}

	protected abstract String getShellTitle();

	protected abstract String getDialogTitle();

	protected abstract String getDialogMessage();

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		getShell().setText(getShellTitle());
		setTitle(getDialogTitle());
		setMessage(getDialogMessage());

		final Image titleImage = new Image(composite.getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/wizban/newexp_wiz.png")); //$NON-NLS-1$
		setTitleImage(titleImage);
		getShell().addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				titleImage.dispose();
			}
		});

		Composite container = new Composite(composite, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.AbstractCommandSelectionDialog_Label_CommandId);

		Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(container, SWT.NONE);
		viewer = new TableViewer(container);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProviderImpl());
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		List<EObject> commands = new ArrayList<EObject>();
		TreeIterator<EObject> it = EcoreUtil.getAllContents((EObject) resource.getRoot().get(0), true);
		while (it.hasNext()) {
			EObject o = it.next();
			if (o.eClass() == CommandsPackageImpl.Literals.COMMAND) {
				commands.add(o);
			}
		}
		viewer.setInput(commands);

		final PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isParentMatch(Viewer viewer, Object element) {
				return viewer instanceof AbstractTreeViewer && super.isParentMatch(viewer, element);
			}
		};
		viewer.addFilter(filter);

		ControlFactory.attachFiltering(searchText, viewer, filter);

		return composite;
	}

	@Override
	protected void okPressed() {
		IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if (!s.isEmpty()) {
			Command cmd = createStoreCommand(resource.getEditingDomain(), (MCommand) s.getFirstElement());
			if (cmd.canExecute()) {
				resource.getEditingDomain().getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
	}

	protected abstract Command createStoreCommand(EditingDomain editingDomain, MCommand command);

	private static class LabelProviderImpl extends StyledCellLabelProvider implements ILabelProvider {

		public void update(final ViewerCell cell) {
			MCommand cmd = (MCommand) cell.getElement();

			StyledString styledString = new StyledString();
			if (cmd.getCommandName() != null) {
				styledString.append(cmd.getCommandName());
			}
			if (cmd.getDescription() != null) {
				styledString.append(" - " + cmd.getDescription(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			}
			if (cmd.getElementId() != null) {
				styledString.append(" - " + cmd.getElementId(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			}
			cell.setText(styledString.getString());
			cell.setStyleRanges(styledString.getStyleRanges());
		}

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			MCommand command = (MCommand) element;
			String s = ""; //$NON-NLS-1$
			if (command.getCommandName() != null) {
				s += command.getCommandName();
			}

			if (command.getDescription() != null) {
				s += " " + command.getDescription(); //$NON-NLS-1$
			}

			if (command.getElementId() != null) {
				s += " " + command.getElementId(); //$NON-NLS-1$
			}

			return s;
		}
	}
}

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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.e4.tools.emf.ui.common.IExtensionLookup;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.PatternFilter;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.SetCommand;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ExpressionIdDialog extends TitleAreaDialog {
	private IExtensionLookup lookup;
	private TableViewer viewer;
	private EditingDomain domain;
	private MCoreExpression expression;
	private boolean liveModel;
	private Messages Messages;

	public ExpressionIdDialog(Shell parentShell, IExtensionLookup lookup, MCoreExpression expression, EditingDomain domain, boolean liveModel, Messages Messages) {
		super(parentShell);
		this.lookup = lookup;
		this.expression = expression;
		this.domain = domain;
		this.liveModel = liveModel;
		this.Messages = Messages;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.ExpressionIdDialog_ShellTitle);
		setTitle(Messages.ExpressionIdDialog_DialogTitle);
		setMessage(Messages.ExpressionIdDialog_DialogMessage);
		Composite comp = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.ExpressionIdDialog_Id);

		Text idField = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		idField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isParentMatch(Viewer viewer, Object element) {
				return viewer instanceof AbstractTreeViewer && super.isParentMatch(viewer, element);
			}
		};

		ControlFactory.attachFiltering(idField, viewer, filter);

		l = new Label(container, SWT.NONE);
		viewer = new TableViewer(container);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProviderImpl());
		viewer.addFilter(filter);
		viewer.setInput(getElements(lookup));
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		return comp;
	}

	@Override
	protected void okPressed() {
		if (!viewer.getSelection().isEmpty()) {
			IConfigurationElement el = (IConfigurationElement) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			Command cmd = SetCommand.create(domain, expression, UiPackageImpl.Literals.CORE_EXPRESSION__CORE_EXPRESSION_ID, el.getAttribute("id")); //$NON-NLS-1$
			if (cmd.canExecute()) {
				domain.getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
	}

	private List<IConfigurationElement> getElements(IExtensionLookup lookup) {
		List<IConfigurationElement> list = new ArrayList<IConfigurationElement>();
		for (IExtension ext : lookup.findExtensions("org.eclipse.core.expressions.definitions", liveModel)) { //$NON-NLS-1$
			for (IConfigurationElement el : ext.getConfigurationElements()) {
				list.add(el);
			}
		}
		return list;
	}

	static class LabelProviderImpl extends StyledCellLabelProvider implements ILabelProvider {
		@Override
		public void update(ViewerCell cell) {
			IConfigurationElement el = (IConfigurationElement) cell.getElement();
			StyledString str = new StyledString(el.getAttribute("id")); //$NON-NLS-1$
			str.append(" - " + el.getContributor().getName(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			cell.setText(str.getString());
			cell.setStyleRanges(str.getStyleRanges());
		}

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			IConfigurationElement el = (IConfigurationElement) element;
			return el.getAttribute("id") + " " + el.getContributor().getName(); //$NON-NLS-1$//$NON-NLS-2$
		}
	}
}
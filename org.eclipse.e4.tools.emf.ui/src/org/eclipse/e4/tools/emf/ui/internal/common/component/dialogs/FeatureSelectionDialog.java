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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.Util.InternalClass;
import org.eclipse.e4.tools.emf.ui.common.Util.InternalFeature;
import org.eclipse.e4.tools.emf.ui.common.Util.InternalPackage;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.StringMatcher;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
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

public class FeatureSelectionDialog extends TitleAreaDialog {
	private TreeViewer viewer;
	private MStringModelFragment fragment;
	private EditingDomain editingDomain;
	private Messages Messages;

	public FeatureSelectionDialog(Shell parentShell, EditingDomain editingDomain, MStringModelFragment fragment, Messages Messages) {
		super(parentShell);
		this.fragment = fragment;
		this.editingDomain = editingDomain;
		this.Messages = Messages;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.FeatureSelectionDialog_ShellTitle);
		setTitle(Messages.FeatureSelectionDialog_DialogTitle);
		setMessage(Messages.FeatureSelectionDialog_DialogMessage);

		Composite composite = (Composite) super.createDialogArea(parent);

		final Image packageImage = new Image(getShell().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/obj16/EPackage.gif")); //$NON-NLS-1$
		final Image classImage = new Image(getShell().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/obj16/class_obj.gif")); //$NON-NLS-1$
		final Image featureImage = new Image(getShell().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/obj16/field_public_obj.gif")); //$NON-NLS-1$
		final Image newTitleImage = new Image(getShell().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/wizban/fieldrefact_wiz.png")); //$NON-NLS-1$

		setTitleImage(newTitleImage);

		composite.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				packageImage.dispose();
				classImage.dispose();
				featureImage.dispose();
				newTitleImage.dispose();
			}
		});

		Composite container = new Composite(composite, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.FeatureSelectionDialog_Filter);

		final Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(container, SWT.NONE);
		viewer = new TreeViewer(container);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		viewer.getControl().setLayoutData(gd);
		viewer.setContentProvider(new ContentProviderImpl());
		viewer.setLabelProvider(new LabelProviderImpl(packageImage, classImage, featureImage));
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1.getClass() == InternalPackage.class) {
					return ((InternalPackage) e1).ePackage.getNsURI().compareTo(((InternalPackage) e2).ePackage.getNsURI());
				} else if (e1.getClass() == InternalClass.class) {
					return ((InternalClass) e1).eClass.getName().compareTo(((InternalClass) e2).eClass.getName());
				} else if (e1.getClass() == InternalFeature.class) {
					return ((InternalFeature) e1).feature.getName().compareTo(((InternalFeature) e2).feature.getName());
				}
				return super.compare(viewer, e1, e2);
			}
		});

		final ViewerFilterImpl filter = new ViewerFilterImpl();

		viewer.addFilter(filter);

		viewer.setInput(Util.loadPackages());

		return composite;
	}

	@Override
	protected void okPressed() {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (!sel.isEmpty() && sel.getFirstElement().getClass() == InternalFeature.class) {
			InternalFeature f = (InternalFeature) sel.getFirstElement();
			Command cmd = SetCommand.create(editingDomain, fragment, FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__FEATURENAME, f.feature.getName());

			if (cmd.canExecute()) {
				editingDomain.getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
	}

	static class ContentProviderImpl implements ITreeContentProvider {

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		public Object[] getElements(Object inputElement) {
			return ((List<?>) inputElement).toArray();
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement.getClass() == InternalPackage.class) {
				return ((InternalPackage) parentElement).classes.toArray();
			} else if (parentElement.getClass() == InternalClass.class) {
				return ((InternalClass) parentElement).features.toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if (element.getClass() == InternalFeature.class) {
				return ((InternalFeature) element).clazz;
			} else if (element.getClass() == InternalClass.class) {
				return ((InternalClass) element).pack;
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

	}

	static class LabelProviderImpl extends StyledCellLabelProvider implements ILabelProvider {
		private final Image packageImage;
		private final Image classImage;
		private final Image featureImage;

		public LabelProviderImpl(Image packageImage, Image classImage, Image featureImage) {
			this.packageImage = packageImage;
			this.classImage = classImage;
			this.featureImage = featureImage;
		}

		public void update(final ViewerCell cell) {
			if (cell.getElement().getClass() == InternalPackage.class) {
				InternalPackage o = (InternalPackage) cell.getElement();
				StyledString styledString = new StyledString(o.ePackage.getName());
				styledString.append(" - " + o.ePackage.getNsURI(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
				cell.setImage(packageImage);
			} else if (cell.getElement().getClass() == InternalClass.class) {
				InternalClass o = (InternalClass) cell.getElement();
				cell.setText(o.eClass.getName());
				cell.setImage(classImage);
			} else {
				InternalFeature o = (InternalFeature) cell.getElement();
				StyledString styledString = new StyledString(o.feature.getName());

				EClassifier type = ModelUtils.getTypeArgument(o.clazz.eClass, o.feature.getEGenericType());
				if (o.feature.isMany()) {
					styledString.append(" : List<" + type.getName() + ">", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					styledString.append(" : " + type.getName(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				}

				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
				cell.setImage(featureImage);
			}
		}

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			if (element.getClass() == InternalPackage.class) {
				InternalPackage o = (InternalPackage) element;
				return o.ePackage.getName();
			} else if (element.getClass() == InternalClass.class) {
				InternalClass o = (InternalClass) element;
				return o.eClass.getName();
			} else {
				InternalFeature o = (InternalFeature) element;
				return o.feature.getName();
			}
		}
	}

	static class ViewerFilterImpl extends ViewerFilter {
		private StringMatcher matcher;

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			if (element.getClass() == InternalPackage.class) {
				ILabelProvider pv = (ILabelProvider) ((StructuredViewer) viewer).getLabelProvider();
				for (InternalClass c : ((InternalPackage) element).classes) {
					if (match(pv.getText(c))) {
						return true;
					}
				}
				return false;
			} else if (element.getClass() == InternalPackage.class) {
				ILabelProvider pv = (ILabelProvider) ((StructuredViewer) viewer).getLabelProvider();
				return match(pv.getText(element));
			}

			return true;
		}

		protected boolean wordMatches(String text) {
			if (text == null) {
				return false;
			}

			// If the whole text matches we are all set
			if (match(text)) {
				return true;
			}

			// Otherwise check if any of the words of the text matches
			String[] words = getWords(text);
			for (int i = 0; i < words.length; i++) {
				String word = words[i];
				if (match(word)) {
					return true;
				}
			}

			return false;
		}

		/**
		 * Answers whether the given String matches the pattern.
		 * 
		 * @param string
		 *            the String to test
		 * 
		 * @return whether the string matches the pattern
		 */
		private boolean match(String string) {
			if (matcher == null) {
				return true;
			}
			return matcher.match(string);
		}

		/**
		 * The pattern string for which this filter should select elements in
		 * the viewer.
		 * 
		 * @param patternString
		 */
		public void setPattern(String patternString) {

			if (patternString == null || patternString.equals("")) { //$NON-NLS-1$
				matcher = null;
			} else {
				String pattern = patternString + "*"; //$NON-NLS-1$
				// if (includeLeadingWildcard) {
				//					pattern = "*" + pattern; //$NON-NLS-1$
				// }
				matcher = new StringMatcher(pattern, true, false);
			}
		}

		/**
		 * Take the given filter text and break it down into words using a
		 * BreakIterator.
		 * 
		 * @param text
		 * @return an array of words
		 */
		private String[] getWords(String text) {
			List<String> words = new ArrayList<String>();
			// Break the text up into words, separating based on whitespace and
			// common punctuation.
			// Previously used String.split(..., "\\W"), where "\W" is a regular
			// expression (see the Javadoc for class Pattern).
			// Need to avoid both String.split and regular expressions, in order
			// to
			// compile against JCL Foundation (bug 80053).
			// Also need to do this in an NL-sensitive way. The use of
			// BreakIterator
			// was suggested in bug 90579.
			BreakIterator iter = BreakIterator.getWordInstance();
			iter.setText(text);
			int i = iter.first();
			while (i != java.text.BreakIterator.DONE && i < text.length()) {
				int j = iter.following(i);
				if (j == java.text.BreakIterator.DONE) {
					j = text.length();
				}
				// match the word
				if (Character.isLetterOrDigit(text.charAt(i))) {
					String word = text.substring(i, j);
					words.add(word);
				}
				i = j;
			}
			return (String[]) words.toArray(new String[words.size()]);
		}
	}
}

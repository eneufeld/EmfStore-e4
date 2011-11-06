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
package org.eclipse.e4.tools.emf.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

public class Util {
	public static final boolean isNullOrEmpty(String element) {
		return element == null || element.trim().length() == 0;
	}

	public static final boolean isImport(EObject object) {
		return object.eContainingFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS;
	}

	public static final void addClasses(EPackage ePackage, List<FeatureClass> list) {
		for (EClassifier c : ePackage.getEClassifiers()) {
			if (c instanceof EClass) {
				EClass eclass = (EClass) c;
				if (eclass != ApplicationPackageImpl.Literals.APPLICATION && !eclass.isAbstract() && !eclass.isInterface() && eclass.getEAllSuperTypes().contains(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT)) {
					list.add(new FeatureClass(eclass.getName(), eclass));
				}
			}
		}

		for (EPackage eSubPackage : ePackage.getESubpackages()) {
			addClasses(eSubPackage, list);
		}
	}

	// TODO In future support different name formats something like
	// ${project}.${classname}.${counter}
	public static final String getDefaultElementId(Resource resource, MApplicationElement element, IProject project) {
		try {
			EObject o = (EObject) element;
			String className = o.eClass().getName();
			String projectName = project.getName();

			String prefix = projectName + "." + className; //$NON-NLS-1$

			TreeIterator<EObject> it = resource.getAllContents();
			SortedSet<Integer> numbers = new TreeSet<Integer>();

			while (it.hasNext()) {
				EObject tmp = it.next();
				if (tmp instanceof MApplicationElement) {
					String elementId = ((MApplicationElement) tmp).getElementId();
					if (elementId != null && elementId.length() > prefix.length() && elementId.startsWith(prefix)) {
						String suffix = elementId.substring(prefix.length());
						if (suffix.startsWith(".") && suffix.length() > 1) { //$NON-NLS-1$
							try {
								numbers.add(Integer.parseInt(suffix.substring(1)));
							} catch (Exception e) {
								// TODO: handle exception
							}
						}
					}
				}
			}

			int lastNumber = -1;
			for (Integer number : numbers) {
				if ((lastNumber + 1) != number) {
					break;
				}
				lastNumber = number;
			}

			return prefix + "." + ++lastNumber; //$NON-NLS-1$
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	public static List<InternalPackage> loadPackages() {
		List<InternalPackage> packs = new ArrayList<InternalPackage>();

		for (Entry<String, Object> regEntry : EPackage.Registry.INSTANCE.entrySet()) {
			if (regEntry.getValue() instanceof EPackage) {
				EPackage ePackage = (EPackage) regEntry.getValue();
				InternalPackage iePackage = new InternalPackage(ePackage);
				boolean found = false;
				for (EClassifier cl : ePackage.getEClassifiers()) {
					if (cl instanceof EClass) {
						EClass eClass = (EClass) cl;
						if (eClass.getEAllSuperTypes().contains(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT)) {
							if (!eClass.isInterface() && !eClass.isAbstract()) {
								found = true;
								InternalClass ieClass = new InternalClass(iePackage, eClass);
								iePackage.classes.add(ieClass);
								for (EReference f : eClass.getEAllReferences()) {
									ieClass.features.add(new InternalFeature(ieClass, f));
								}
							}
						}
					}
				}
				if (found) {
					packs.add(iePackage);
				}
			}
		}

		return packs;
	}

	public static boolean moveElementByIndex(EditingDomain editingDomain, MUIElement element, boolean liveModel, int index, EStructuralFeature feature) {
		if (liveModel) {
			EObject container = ((EObject) element).eContainer();
			List<Object> l = (List<Object>) container.eGet(feature);
			l.remove(element);

			if (index >= 0) {
				l.add(index, element);
			} else {
				l.add(element);
			}

			return true;
		} else {
			EObject container = ((EObject) element).eContainer();
			Command cmd = MoveCommand.create(editingDomain, container, feature, element, index);

			if (cmd.canExecute()) {
				editingDomain.getCommandStack().execute(cmd);
				return true;
			}
			return false;
		}
	}

	public static boolean moveElementByIndex(EditingDomain editingDomain, MUIElement element, boolean liveModel, int index) {
		if (liveModel) {
			MElementContainer<MUIElement> container = element.getParent();
			container.getChildren().remove(element);

			if (index >= 0) {
				container.getChildren().add(index, element);
			} else {
				container.getChildren().add(element);
			}

			container.setSelectedElement(element);
			return true;
		} else {
			MElementContainer<MUIElement> container = element.getParent();
			Command cmd = MoveCommand.create(editingDomain, container, UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, element, index);

			if (cmd.canExecute()) {
				editingDomain.getCommandStack().execute(cmd);
				return true;
			}
			return false;
		}
	}

	public static final void addDecoration(Control control, Binding binding) {
		final ControlDecoration dec = new ControlDecoration(control, SWT.BOTTOM);
		binding.getValidationStatus().addValueChangeListener(new IValueChangeListener() {

			public void handleValueChange(ValueChangeEvent event) {
				IStatus s = (IStatus) event.getObservableValue().getValue();
				if (s.isOK()) {
					dec.setDescriptionText(null);
					dec.setImage(null);
				} else {
					dec.setDescriptionText(s.getMessage());

					String fieldDecorationID = null;
					switch (s.getSeverity()) {
					case IStatus.INFO:
						fieldDecorationID = FieldDecorationRegistry.DEC_INFORMATION;
						break;
					case IStatus.WARNING:
						fieldDecorationID = FieldDecorationRegistry.DEC_WARNING;
						break;
					case IStatus.ERROR:
					case IStatus.CANCEL:
						fieldDecorationID = FieldDecorationRegistry.DEC_ERROR;
						break;
					}
					FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(fieldDecorationID);
					dec.setImage(fieldDecoration == null ? null : fieldDecoration.getImage());
				}
			}
		});
	}

	public static class InternalPackage {
		public final EPackage ePackage;
		public List<InternalClass> classes = new ArrayList<InternalClass>();

		public InternalPackage(EPackage ePackage) {
			this.ePackage = ePackage;
		}

		@Override
		public String toString() {
			return ePackage.toString();
		}

		public List<EClass> getAllClasses() {
			ArrayList<EClass> rv = new ArrayList<EClass>(classes.size());
			for (InternalClass c : classes) {
				rv.add(c.eClass);
			}
			return rv;
		}
	}

	public static class InternalClass {
		public final InternalPackage pack;
		public final EClass eClass;
		public List<InternalFeature> features = new ArrayList<InternalFeature>();

		public InternalClass(InternalPackage pack, EClass eClass) {
			this.eClass = eClass;
			this.pack = pack;
		}
	}

	public static class InternalFeature {
		public final InternalClass clazz;
		public final EStructuralFeature feature;

		public InternalFeature(InternalClass clazz, EStructuralFeature feature) {
			this.clazz = clazz;
			this.feature = feature;
		}

	}
}

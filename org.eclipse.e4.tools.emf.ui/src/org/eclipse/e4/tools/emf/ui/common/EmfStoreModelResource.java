package org.eclipse.e4.tools.emf.ui.common;

import edu.tum.in.bruegge.epd.emfstore.helper.EmfStoreHelper;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;

public class EmfStoreModelResource implements IModelResource {
	private EditingDomain editingDomain;
	private List<ModelListener> listeners = new ArrayList<IModelResource.ModelListener>();
	private boolean dirty;
	private IObservableList list;

	public EmfStoreModelResource() {
		ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		ResourceSet resourceSet = new ResourceSetImpl();
		// resourceSet.getLoadOptions().putAll(ModelUtil.getResourceLoadOptions());
		BasicCommandStack commandStack = new BasicCommandStack();
		commandStack.addCommandStackListener(new CommandStackListener() {

			public void commandStackChanged(EventObject event) {
				dirty = true;
				fireDirtyChanged();
				fireCommandStackChanged();
			}
		});

		// resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION,
		// new XMIResourceFactoryImpl());

		Map<Resource, Boolean> map = new HashMap<Resource, Boolean>();
		// map.put(projectSpace.getProject().eResource(), false);
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, map);
	}

	public IObservableList getRoot() {
		if (list != null) {
			return list;
		}
		list = new WritableList();
		list.add(EmfStoreHelper.INSTANCE.getRoot());
		// int size = projectSpace.getProject().getModelElements().size();
		// list.add(projectSpace.getProject().getModelElements().get(size - 1));

		return list;
	}

	public IStatus save() {
		try {

			// resource.save(map);
			EmfStoreHelper.INSTANCE.commit();
			editingDomain.getCommandStack().flush();
			dirty = false;
			fireDirtyChanged();
			fireCommandStackChanged();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return Status.OK_STATUS;
	}

	public void replaceRoot(EObject eObject) {
		// E4XMIResource resource = (E4XMIResource) eObject.eResource();
		// Map<EObject, String> idMap = new HashMap<EObject, String>();
		// idMap.put(eObject, resource.getID(eObject));
		//
		// TreeIterator<EObject> it = EcoreUtil.getAllContents(eObject, true);
		// while (it.hasNext()) {
		// EObject o = it.next();
		// resource = (E4XMIResource) o.eResource();
		// idMap.put(o, resource.getID(o));
		// }
		//
		// XMIResource resource2 = (XMIResource)
		// (projectSpace.getProject().eContents().get(0)).eResource();
		//
		// Command cmdRemove = new RemoveCommand(getEditingDomain(),
		// resource.getContents(),
		// projectSpace.getProject().eContents().get(0));
		// Command cmdAdd = new AddCommand(getEditingDomain(),
		// resource.getContents(), eObject);
		// CompoundCommand cmd = new CompoundCommand(Arrays.asList(cmdRemove,
		// cmdAdd));
		// getEditingDomain().getCommandStack().execute(cmd);
		//
		// for (Entry<EObject, String> e : idMap.entrySet()) {
		// resource2.setID(e.getKey(), e.getValue());
		// }
		// Command cmdRemove = new RemoveCommand(getEditingDomain(),
		// eObject.eContents(), projectSpace.getProject().eContents().get(0));
		// Command cmdAdd = new AddCommand(getEditingDomain(),
		// eObject.eContents(), eObject);
		// CompoundCommand cmd = new CompoundCommand(Arrays.asList(cmdRemove,
		// cmdAdd));
		// getEditingDomain().getCommandStack().execute(cmd);
		// list = null;
		// projectSpace.getProject().addModelElement(eObject);
		// dirty = true;
		// fireDirtyChanged();
		// fireCommandStackChanged();

	}

	private void fireDirtyChanged() {
		for (ModelListener listener : listeners) {
			listener.dirtyChanged();
		}
	}

	private void fireCommandStackChanged() {
		for (ModelListener listener : listeners) {
			listener.commandStackChanged();
		}
	}

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	public boolean isSaveable() {
		return true;
	}

	public void addModelListener(ModelListener listener) {
		listeners.add(listener);
	}

	public void removeModelListener(ModelListener listener) {
		listeners.remove(listener);
	}

	public boolean isDirty() {
		return dirty && getEditingDomain().getCommandStack().canUndo();
	}

}

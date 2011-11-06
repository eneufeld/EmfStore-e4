package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.editor3x.emf.EditUIUtil;
import org.eclipse.e4.tools.emf.ui.common.IModelResource.ModelListener;
import org.eclipse.e4.tools.emf.ui.common.EmfStoreModelResource;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.XMIModelResource;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IEditorInput;

public class XMIResourceFunction extends ContextFunction {

	@Override
	public Object compute(final IEclipseContext context) {
//		final IEditorInput input = context.get(IEditorInput.class);
		final IDirtyProviderService dirtyProvider = context.get(IDirtyProviderService.class);
//		
//		if( input != null ) {
//			URI resourceURI = EditUIUtil.getURI(input);
//			final XMIModelResource resource = new XMIModelResource(resourceURI);
			final IModelResource resource=new EmfStoreModelResource();
			resource.addModelListener(new ModelListener() {
				
				public void dirtyChanged() {
					dirtyProvider.setDirtyState(resource.isDirty());
				}

				public void commandStackChanged() {
					
				}
			});
			return resource;
//		}

//		return null;
	}

}

package edu.tum.in.bruegge.epd.emfstore4appmodel;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.exceptions.NoChangesOnServerException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;

import edu.tum.in.bruegge.epd.emfstore.helper.EmfStoreHelper;

public class UpdateHandler {
	
	@Execute
	public void execute(IEclipseContext context) {
		EmfStoreHelper.INSTANCE.update();
	}
}

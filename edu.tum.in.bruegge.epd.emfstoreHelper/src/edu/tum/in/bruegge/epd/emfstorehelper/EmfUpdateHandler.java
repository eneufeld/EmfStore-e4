package edu.tum.in.bruegge.epd.emfstorehelper;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import edu.tum.in.bruegge.epd.emfstore.helper.EmfStoreHelper;

public class EmfUpdateHandler extends AbstractHandler{
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EmfStoreHelper.INSTANCE.update();
		return null;
	}
	
}

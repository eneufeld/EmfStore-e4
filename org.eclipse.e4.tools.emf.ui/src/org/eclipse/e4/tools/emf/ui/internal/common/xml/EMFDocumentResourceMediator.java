package org.eclipse.e4.tools.emf.ui.internal.common.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.common.model.util.SerializationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.xml.sax.InputSource;

public class EMFDocumentResourceMediator {
	private IModelResource modelResource;
	private Document document;
	private boolean updateFromEMF;
	private List<Diagnostic> errorList = new ArrayList<Diagnostic>();
	private Runnable documentValidationChanged;

	public EMFDocumentResourceMediator(final IModelResource modelResource) {
		this.modelResource = modelResource;
		this.document = new Document();
		this.document.addDocumentListener(new IDocumentListener() {

			public void documentChanged(DocumentEvent event) {
				if (updateFromEMF) {
					return;
				}

				String doc = document.get();
				E4XMIResource res = new E4XMIResource();
				try {
					res.load(new InputSource(new StringReader(doc)), null);
					try {
						modelResource.replaceRoot(ModelUtil.stringToEObject(doc));
					} catch (SerializationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					errorList.clear();
					if (documentValidationChanged != null) {
						documentValidationChanged.run();
					}
				} catch (IOException e) {
					errorList = res.getErrors();
					if (documentValidationChanged != null) {
						documentValidationChanged.run();
					}

				}
			}

			public void documentAboutToBeChanged(DocumentEvent event) {

			}
		});
		updateFromEMF();
	}

	public void setValidationChangedCallback(Runnable runnable) {
		documentValidationChanged = runnable;
	}

	public List<Diagnostic> getErrorList() {
		return Collections.unmodifiableList(errorList);
	}

	public void updateFromEMF() {
		try {
			updateFromEMF = true;
			this.document.set(toXMI((EObject) modelResource.getRoot().get(0)));
		} finally {
			updateFromEMF = false;
		}
	}

	public Document getDocument() {
		return document;
	}

	private String toXMI(EObject root) {
		try {
			return ModelUtil.eObjectToString(root);
		} catch (SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		// E4XMIResource resource = (E4XMIResource) root.eResource();
		// if (resource == null)
		// try {
		// return ModelUtil.eObjectToString(root);
		// } catch (SerializationException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// // resource.getContents().add(EcoreUtil.copy(root));
		// StringWriter writer = new StringWriter();
		// try {
		//
		// resource.save(writer, null);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return writer.toString();
	}
}

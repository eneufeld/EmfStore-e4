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
package edu.tum.in.bruegge.epd.emfstore.dialog.util.scm;

import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.jface.viewers.TreeNode;

/**
 * A TreeNode class which also contains a reference to the ProjectSpace.
 * 
 * @author shterev
 */
public class SCMTreeNode extends TreeNode {

	private ProjectSpace projectSpace;

	/**
	 * Default Constructor.
	 * 
	 * @param value the contained object
	 */
	public SCMTreeNode(Object value) {
		super(value);
	}

	/**
	 * @param projectSpace the projectSpace to set
	 */
	public void setProjectSpace(ProjectSpace projectSpace) {
		this.projectSpace = projectSpace;
	}

	/**
	 * @return the projectSpace
	 */
	public ProjectSpace getProjectSpace() {
		return projectSpace;
	}

}

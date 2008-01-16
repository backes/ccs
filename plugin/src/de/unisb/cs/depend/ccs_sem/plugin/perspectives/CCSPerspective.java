/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package de.unisb.cs.depend.ccs_sem.plugin.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;


/**
 *  This class is meant to serve as an example for how various contributions
 *  are made to a perspective. Note that some of the extension point id's are
 *  referred to as API constants while others are hardcoded and may be subject
 *  to change.
 */
public class CCSPerspective implements IPerspectiveFactory {

	private IPageLayout factory;

	public CCSPerspective() {
		super();
	}

	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;
        factory.setFixed(true);
		addViews();
		addActionSets();
		addNewWizardShortcuts();
		addPerspectiveShortcuts();
		addViewShortcuts();
	}

	private void addViews() {
		// Creates the overall folder layout.
		// Note that each new Folder uses a percentage of the remaining EditorArea.

        final IFolderLayout bottom =
            factory.createFolder(
                "bottom",
                IPageLayout.BOTTOM,
                0.7f,
                factory.getEditorArea());
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        //bottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
        bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);

        final IFolderLayout center =
            factory.createFolder(
                "center",
                IPageLayout.BOTTOM,
                0.5f,
                factory.getEditorArea());
        center.addPlaceholder("de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView");

        final IFolderLayout topLeft =
            factory.createFolder("topLeft", IPageLayout.LEFT, 0.2f, factory.getEditorArea());
        //topLeft.addView(IPageLayout.ID_RES_NAV);
        topLeft.addView(IPageLayout.ID_OUTLINE);

	}

	private void addActionSets() {
		//factory.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET); //NON-NLS-1
	}

	private void addPerspectiveShortcuts() {
        // nothing
	}

	private void addNewWizardShortcuts() {
        // nothing
	}

	private void addViewShortcuts() {
		factory.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
	}

}

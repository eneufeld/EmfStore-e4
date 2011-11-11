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
package edu.tum.in.bruegge.epd.emfstore.handler.merge.util;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * Configuration class for the merge dialog.
 * 
 * @author wesendon
 */
public final class DecisionConfig {

	private DecisionConfig() {
	}

	/**
	 * Length of option label.
	 */
	public static final int OPTION_LENGTH = 50;

	/**
	 * Seperator symbol for detail proivder.
	 */
	public static final String SEPERATOR = "#";

	/**
	 * Editable detail provider.
	 */
	public static final String EDITABLE = "editable";

	/**
	 * Multiline widget detail provider.
	 */
	public static final String WIDGET_MULTILINE = "org.eclipse.emf.emfstore.client.ui.merge.widget.multiline";

	/**
	 * Multiline editable widget detail provider.
	 */
	public static final String WIDGET_MULTILINE_EDITABLE = WIDGET_MULTILINE + SEPERATOR + EDITABLE;

	/**
	 * Option for other involved detail provider.
	 */
	public static final String WIDGET_OTHERINVOLVED = "org.eclipse.emf.emfstore.client.ui.merge.widget.otherinvolved";

	/**
	 * Inits the fonts for the dialog.
	 * 
	 * @param fontRegistry
	 *            fontRegistry
	 */
	public static void initFonts(FontRegistry fontRegistry) {
		FontData[] fontData = JFaceResources.getDialogFontDescriptor().getFontData();
		if (fontData.length > 0) {
			fontData[0].setStyle(SWT.ITALIC);
			fontData[0].setHeight(fontData[0].getHeight() - 1);
		}
		fontRegistry.put("titleLabel", fontData);
		fontRegistry.put("content", JFaceResources.getDialogFontDescriptor().getFontData());
	}

	/**
	 * Color for selected option background.
	 * 
	 * @return color
	 */
	public static Color getOptionSelectedBack() {
		return new Color(Display.getCurrent(), 0, 127, 14);
	}

	/**
	 * Color for selected option background if mouse entered.
	 * 
	 * @return color
	 */
	public static Color getOptionSelectedBackEnter() {
		return new Color(Display.getCurrent(), 165, 255, 142);
	}

	/**
	 * Color for selected option text.
	 * 
	 * @return color
	 */
	public static Color getOptionSelectedFor() {
		return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	}

	/**
	 * Color for selected option text if mouse entered.
	 * 
	 * @return color
	 */
	public static Color getOptionEnteredColor() {
		return new Color(Display.getCurrent(), 250, 230, 95);
	}

	/**
	 * Default background color.
	 * 
	 * @return color
	 */
	public static Color getDefaultColor() {
		return new Color(Display.getCurrent(), 240, 240, 240);
	}

	/**
	 * Default text color.
	 * 
	 * @return color
	 */
	public static Color getDefaultTextColor() {
		return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	}

	/**
	 * Color 1 for {@link edu.tum.in.bruegge.epd.emfstore.handler.merge.ui.ui.dialogs.merge.ui.DecisionBox} . Every other box has this
	 * background color.
	 * 
	 * @return color
	 */
	public static Color getFirstDecisionBoxColor() {
		return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	}

	/**
	 * Color 2 for {@link edu.tum.in.bruegge.epd.emfstore.handler.merge.ui.ui.dialogs.merge.ui.DecisionBox} . Every other box has this
	 * background color
	 * 
	 * @return color
	 */
	public static Color getSecondDecisionBoxColor() {
		return new Color(Display.getCurrent(), 226, 233, 255);

	}
}

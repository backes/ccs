package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;


public class Constants {

    private static final RGB commentForegroundRGB = new RGB(63, 127, 95);
    private static final RGB commentBackgroundRGB = null;
    private static final int commentFontStyle = SWT.ITALIC;

    private static final RGB keywordForegroundRGB = new RGB(127, 0, 85);
    private static final RGB keywordBackgroudRGB = null;
    private static final int keywordFontStyle = SWT.BOLD;

    private static final RGB operatorForegroundRGB = new RGB(42, 42, 67);
    private static final RGB operatorBackgroudRGB = null;
    private static final int operatorFontStyle = SWT.NORMAL;

    private static final RGB parameterReferenceForegroundRGB = new RGB(0, 0, 192);
    private static final RGB parameterReferenceBackgroudRGB = null;
    private static final int parameterReferenceFontStyle = SWT.ITALIC;

	public static RGB getCommentForegroundRGB() {
		return commentForegroundRGB;
	}

	public static RGB getCommentBackgroundRGB() {
		return commentBackgroundRGB;
	}

	public static int getCommentFontStyle() {
		return commentFontStyle;
	}

	public static RGB getKeywordForegroundRGB() {
		return keywordForegroundRGB;
	}

	public static RGB getKeywordBackgroudRGB() {
		return keywordBackgroudRGB;
	}

	public static int getKeywordFontStyle() {
		return keywordFontStyle;
	}

	public static RGB getOperatorForegroundRGB() {
		return operatorForegroundRGB;
	}

	public static RGB getOperatorBackgroudRGB() {
		return operatorBackgroudRGB;
	}

	public static int getOperatorFontStyle() {
		return operatorFontStyle;
	}

	public static RGB getParameterReferenceForegroundRGB() {
		return parameterReferenceForegroundRGB;
	}

	public static RGB getParameterReferenceBackgroudRGB() {
		return parameterReferenceBackgroudRGB;
	}

	public static int getParameterReferenceFontStyle() {
		return parameterReferenceFontStyle;
	}

}

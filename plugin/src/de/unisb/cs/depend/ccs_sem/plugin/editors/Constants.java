package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;


public class Constants {

    private static final RGB commentForegroundRGB = new RGB(63, 127, 95);
    private static final RGB commentBackgroundRGB = null;
    private static final int commentFontStyle = SWT.ITALIC;

    private static final RGB keywordForegroundRGB = new RGB(127, 0, 85);
    private static final RGB keywordBackgroundRGB = null;
    private static final int keywordFontStyle = SWT.BOLD;

    private static final RGB operatorForegroundRGB = new RGB(42, 42, 67);
    private static final RGB operatorBackgroundRGB = null;
    private static final int operatorFontStyle = SWT.NORMAL;

    private static final RGB parameterReferenceForegroundRGB = new RGB(0, 0, 192);
    private static final RGB parameterReferenceBackgroundRGB = null;
    private static final int parameterReferenceFontStyle = SWT.ITALIC;

    private static final RGB processReferenceForegroundRGB = new RGB(10, 36, 106);
    private static final RGB processReferenceBackgroundRGB = null;
    private static final int processReferenceFontStyle = SWT.BOLD;

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

    public static RGB getKeywordBackgroundRGB() {
        return keywordBackgroundRGB;
    }

    public static int getKeywordFontStyle() {
        return keywordFontStyle;
    }

    public static RGB getOperatorForegroundRGB() {
        return operatorForegroundRGB;
    }

    public static RGB getOperatorBackgroundRGB() {
        return operatorBackgroundRGB;
    }

    public static int getOperatorFontStyle() {
        return operatorFontStyle;
    }

    public static RGB getParameterReferenceForegroundRGB() {
        return parameterReferenceForegroundRGB;
    }

    public static RGB getParameterReferenceBackgroundRGB() {
        return parameterReferenceBackgroundRGB;
    }

    public static int getParameterReferenceFontStyle() {
        return parameterReferenceFontStyle;
    }

    public static RGB getProcessReferenceForegroundRGB() {
        return processReferenceForegroundRGB;
    }

    public static RGB getProcessReferenceBackgroundRGB() {
        return processReferenceBackgroundRGB;
    }

    public static int getProcessReferenceFontStyle() {
        return processReferenceFontStyle;
    }

}

package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;


public class Constants {

    private static final RGB commentForegroundRGB = new RGB(0, 255, 0);
    private static final RGB commentBackgroundRGB = null;
    private static final int commentStyle = SWT.ITALIC;
    private static final Font commentFont = null;

    public static TextAttribute createCommentTextAttribute(ColorManager colorManager) {
        final TextAttribute att = new TextAttribute(
            commentForegroundRGB == null ? null : colorManager.getColor(commentForegroundRGB),
            commentBackgroundRGB == null ? null : colorManager.getColor(commentBackgroundRGB),
            commentStyle, commentFont);
        return att;
    }

}

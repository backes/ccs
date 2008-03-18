package de.unisb.cs.depend.ccs_sem.plugin.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {

    protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>();

    public void dispose() {
        for (final Color c: fColorTable.values())
            c.dispose();
    }

    public Color getColor(RGB rgb) {
        if (rgb == null)
            return null;

        Color color = fColorTable.get(rgb);
        if (color == null) {
            color = new Color(Display.getCurrent(), rgb);
            fColorTable.put(rgb, color);
        }
        return color;
    }
}

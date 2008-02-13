package de.unisb.cs.depend.ccs_sem.plugin.grappa;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import att.grappa.Graph;
import att.grappa.GrappaSupport;
import de.unisb.cs.depend.ccs_sem.plugin.Global;


public class GraphHelper {

    public static final Color START_NODE_COLOR = Color.LIGHT_GRAY;
    public static final Color WARN_NODE_COLOR = Color.RED;
    public static final Color ERROR_NODE_COLOR = Color.RED;

    private GraphHelper() {
        // prohibit instantiation
    }

    public static boolean filterGraph(Graph graph) {
        return filterGraph(graph, true);
    }

    public static boolean filterGraph(Graph graph, boolean showDialogOnError) {
        // start dot
        final List<String> command = new ArrayList<String>();
        command.add(getDotExecutablePath());

        final ProcessBuilder pb = new ProcessBuilder(command);
        Process dotFilter = null;
        boolean success = true;
        try {
            dotFilter = pb.start();
        } catch (final IOException e) {
            success = false;
        }

        try {
            if (success)
                success &= GrappaSupport.filterGraph(graph, dotFilter);

            if (!success && showDialogOnError) {
                MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    "Error layouting graph",
                    "The graph could not be layout, most probably there was an error with starting the dot tool.\n" +
                    "You can configure the path for this tool in your preferences on the \"CCS\" page.");
            }
        } finally {
            dotFilter.destroy();
        }

        return success;
    }

    private static String getDotExecutablePath() {
        final String dotExecutable = Global.getPreferenceDot();
        return dotExecutable;
    }

}

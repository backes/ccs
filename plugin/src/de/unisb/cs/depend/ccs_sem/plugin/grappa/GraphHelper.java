package de.unisb.cs.depend.ccs_sem.plugin.grappa;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import att.grappa.Graph;
import att.grappa.Grappa;
import att.grappa.GrappaSupport;
import de.unisb.cs.depend.ccs_sem.plugin.Global;


public class GraphHelper {

    static {
        // don't show this ugly Exception windows
        Grappa.doDisplayException = false;
    }

    public static final Color START_NODE_COLOR = Color.LIGHT_GRAY;
    public static final Color WARN_NODE_COLOR = Color.RED;
    public static final Color ERROR_NODE_COLOR = Color.RED;

    private GraphHelper() {
        // prohibit instantiation
    }

    public static boolean filterGraph(Graph graph) throws InterruptedException {
        return filterGraph(graph, true);
    }

    public static boolean filterGraph(final Graph graph, boolean showDialogOnError) throws InterruptedException {
        // start dot
        final List<String> command = new ArrayList<String>();
        command.add(getDotExecutablePath());

        final ProcessBuilder pb = new ProcessBuilder(command);
        Process dotFilter = null;
        boolean success = true;

        try {
            dotFilter = pb.start();
            final Process finalDotFilter = dotFilter;

            // graph filtering in another thread, for that we can control it and
            // terminate it
            final Callable<Boolean> filterGraphCall = new Callable<Boolean>() {
                public Boolean call() {
                    return GrappaSupport.filterGraph(graph, finalDotFilter);
                }
            };
            final FutureTask<Boolean> filterGraphTask = new FutureTask<Boolean>(filterGraphCall);
            new Thread(filterGraphTask, "filterGraph").start();

            while (true) {
                try {
                    success &= filterGraphTask.get(100, TimeUnit.MILLISECONDS);
                    break;
                } catch (final ExecutionException e) {
                    // should not occure (GrappaSupport.filterGraph does not
                    // throw any Exception)
                    throw new RuntimeException(e);
                } catch (final TimeoutException e) {
                    if (Thread.currentThread().isInterrupted()) {
                        filterGraphTask.cancel(true);
                        return false;
                    }
                }
            }

            if (!success && showDialogOnError) {
                MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    "Error layouting graph",
                    "The graph could not be layout, most probably there was an error with starting the dot tool.\n" +
                    "You can configure the path for this tool in your preferences on the \"CCS\" page.");
            }
        } catch (final IOException e) {
            success = false;
        } finally {
            if (dotFilter != null)
                dotFilter.destroy();
        }

        return success;
    }

    private static String getDotExecutablePath() {
        final String dotExecutable = Global.getPreferenceDot();
        return dotExecutable;
    }

}

package de.unisb.cs.depend.ccs_sem.plugin.grappa;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import att.grappa.Graph;
import att.grappa.Grappa;
import att.grappa.GrappaSupport;
import de.unisb.cs.depend.ccs_sem.plugin.MyPreferenceStore;
import de.unisb.cs.depend.ccs_sem.plugin.preferencePage.CCSPreferencePage;


public class GraphHelper {

    static {
        // don't show this ugly Exception window
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
        command.add(MyPreferenceStore.getDot());

        final ProcessBuilder pb = new ProcessBuilder(command);
        Process dotFilter = null;
        boolean success = true;
        String startingError = null;

        try {
            dotFilter = pb.start();
        } catch (final IOException e) {
            startingError = e.getMessage();
            success = false;
        }

        if (success) {
            final Process finalDotFilter = dotFilter;

            // graph filtering in another thread, for that we can control it and
            // terminate it
            final Callable<Boolean> filterGraphCall = new Callable<Boolean>() {
                public Boolean call() {
                    try {
                        return GrappaSupport.filterGraph(graph, finalDotFilter);
                    } catch (IOException e) {
                        return false;
                    }
                }
            };
            final FutureTask<Boolean> filterGraphTask = new FutureTask<Boolean>(filterGraphCall);
            new Thread(filterGraphTask, "filterGraph").start();

            try {
                success &= filterGraphTask.get();
            } catch (final ExecutionException e) {
                if (e.getCause() instanceof IOException)
                    success = false;
                else {
                    // should not occure (GrappaSupport.filterGraph does only
                    // throw IOException)
                    throw new RuntimeException(e);
                }
            } finally {
                filterGraphTask.cancel(true);
                if (dotFilter != null)
                    dotFilter.destroy();
            }
        }

        if (!success && showDialogOnError) {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            final Display display = workbench == null ? null : workbench.getDisplay();
            final String errorMessage;
            if (startingError == null) {
                errorMessage = "The graph could not be layout, most probably there was an error with starting the dot tool.\n" +
                    "Do you want to configure the path for this tool now?";
            } else {
                errorMessage = "Could not start the dot-Tool.\n\n" +
                    "Error Message: " + startingError + "\n\n" +
                    "Do you want to configure the path for this tool now?";
            }
            if (display != null) {
                final AtomicBoolean reparse = new AtomicBoolean(false);
                display.syncExec(new Runnable() {
                    public void run() {
                        final IWorkbenchWindow activeWorkbenchWindow = workbench == null ? null :
                            workbench.getActiveWorkbenchWindow();
                        final Shell shell = activeWorkbenchWindow == null ? null :
                            activeWorkbenchWindow.getShell();
                        if (shell != null &&
                                MessageDialog.openQuestion(shell, "Error layouting graph", errorMessage)) {
                            final PreferenceManager preferenceManager = new PreferenceManager();
                            preferenceManager.addToRoot(new PreferenceNode("CCS", new CCSPreferencePage()));
                            final Dialog dialog = new PreferenceDialog(shell, preferenceManager);
                            if (dialog.open() == Window.OK)
                                reparse.set(true);
                        }
                    }
                });
                if (reparse.get())
                    return filterGraph(graph, showDialogOnError);
            }
        }

        return success;
    }

}

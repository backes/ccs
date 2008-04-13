package de.unisb.cs.depend.ccs_sem.plugin.dotExporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import att.grappa.Graph;
import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.plugin.MyPreferenceStore;


public class AbstractDotExporter {

    private final String format;

    public AbstractDotExporter(String format) {
        this.format = format;
    }

    public void export(File outputFile, final Graph graph) throws ExportException {
        // start dot
        final List<String> command = new ArrayList<String>();
        command.add(MyPreferenceStore.getDot());
        command.add("-T" + format);
        command.add("-o");
        command.add(outputFile.getAbsolutePath());

        final ProcessBuilder pb = new ProcessBuilder(command);
        Process dotFilter = null;
        try {
            dotFilter = pb.start();
        } catch (final IOException e) {
            throw new ExportException("Error starting dot: " + e, e);
        }

        try {
            final Process finalDotFilter = dotFilter;

            // graph filtering in another thread, for that we can control it and
            // terminate it
            final Callable<Boolean> filterGraphCall = new Callable<Boolean>() {
                public Boolean call() {
                    try {
                        OutputStream toFilterRaw = finalDotFilter.getOutputStream();
                        final BufferedWriter toFilter =
                                new BufferedWriter(new OutputStreamWriter(toFilterRaw));
                        graph.printGraph(toFilterRaw);
                        toFilter.close();
                        try {
                            finalDotFilter.waitFor();
                        } catch (InterruptedException e) {
                            finalDotFilter.destroy();
                            return false;
                        }
                        return finalDotFilter.exitValue() == 0;
                    } catch (IOException e) {
                        return false;
                    }
                }
            };
            final FutureTask<Boolean> filterGraphTask = new FutureTask<Boolean>(filterGraphCall);
            new Thread(filterGraphTask, "layout graph").start();

            boolean success = false;
            try {
                success = filterGraphTask.get();
            } catch (final ExecutionException e) {
                if (!(e.getCause() instanceof IOException)) {
                    // should not occure (GrappaSupport.filterGraph does only
                    // throw IOException)
                    throw new RuntimeException(e);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExportException("Interrupted");
            }
            if (!success)
                throw new ExportException("Error laying out the graph using dot.");
        } finally {
            if (dotFilter != null)
                dotFilter.destroy();
        }
    }

    public String getIdentifier() {
        return "dot export (" + format + " format)";
    }

}

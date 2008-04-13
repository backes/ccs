package de.unisb.cs.depend.ccs_sem.exporters.helpers;

import java.io.File;

import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.exporters.Exporter;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class FileWrapperExporter implements Exporter {

    private final File file;
    private final Exporter exporter;

    public FileWrapperExporter(File file, Exporter exporter) {
        this.file = file;
        this.exporter = exporter;
    }

    public void export(Program program) throws ExportException {
        export(file, program);
    }

    public void export(File file, Program program) throws ExportException {
        exporter.export(file, program);
    }

    public String getIdentifier() {
        return exporter.getIdentifier() + " to file " + file.getPath();
    }

}

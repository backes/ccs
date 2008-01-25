package de.unisb.cs.depend.ccs_sem.exporters;

import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public interface Exporter {

    void export(Program program) throws ExportException;

    String getIdentifier();
}

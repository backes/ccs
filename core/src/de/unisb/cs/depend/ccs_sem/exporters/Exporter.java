package de.unisb.cs.depend.ccs_sem.exporters;

import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public interface Exporter {

    void export(Expression expr) throws ExportException;
}

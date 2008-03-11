package de.unisb.cs.depend.ccs_sem.plugin.editors;

import org.eclipse.jface.text.IDocument;

import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public interface IParsingListener {
    public void parsingDone(IDocument document, Program parsedProgram);
}

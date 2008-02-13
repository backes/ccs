package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class ParseCCSProgramJob extends Job {

    private final CCSEditor editor;

    private final static int WORK_LEXING = 1;
    private final static int WORK_PARSING = 3;

    public ParseCCSProgramJob(CCSEditor editor) {
        super("Parse CCS Term");
        this.editor = editor;
        setSystem(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        final int totalWork = WORK_LEXING + WORK_PARSING;

        monitor.beginTask(getName(), totalWork);

        Program ccsProgram = null;
        String warning = null;
        try {
            monitor.subTask("Lexing");
            final List<Token> tokens = new CCSLexer().lex(editor.getText());

            monitor.subTask("Parsing");
            ccsProgram = new CCSParser().parse(tokens);

            monitor.subTask("Checking Expression");

            if (!ccsProgram.isGuarded())
                throw new ParseException("Your recursive definitions are not guarded.");
            if (!ccsProgram.isRegular())
                throw new ParseException("Your recursive definitions are not regular.");
        } catch (final LexException e) {
            warning = "Error lexing: " + e.getMessage() + "\\n"
                + "(around this context: " + e.getEnvironment() + ")";
        } catch (final ParseException e) {
            warning = "Error parsing: " + e.getMessage() + "\\n"
                + "(around this context: " + e.getEnvironment() + ")";
        }

        // TODO ParseResult, highlighting, ...
        // TODO additional parameter for the parsing result (map expression->position)
        //editor.setProgram(ccsProgram);
        // TODO Auto-generated method stub
        return null;
    }

}

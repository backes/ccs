package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.lexer.LoggingCCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Identifier;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.parser.LoggingCCSParser;
import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult.ReadProcessVariable;
import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class ParseCCSProgramJob extends Job {

    private final CCSDocument ccsDocument;

    public volatile boolean shouldRunImmediately = false;
    public volatile boolean syncExec = false;

    // the mod count of the text to parse.
    // is used when parsing is done to check if the text is still up-to-date
    public long docModCount = -1;

    private final static int WORK_LEXING = 2;
    private final static int WORK_PARSING = 5;
    private final static int WORK_CHECKING = 1;

    private static final ISchedulingRule rule = new IdentityRule();

    public ParseCCSProgramJob(CCSDocument document) {
        super("Parse CCS Term");
        this.ccsDocument = document;
        setRule(rule);
        setSystem(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        final int totalWork = WORK_LEXING + WORK_PARSING + WORK_CHECKING;

        monitor.beginTask(getName(), totalWork);

        Program ccsProgram = null;
        String warning = null;
        final ParsingResult result = new ParsingResult();
        try {
            monitor.subTask("Lexing...");
            String text;
            ccsDocument.lock();
            try {
                docModCount = ccsDocument.getModificationStamp();
                text = ccsDocument.get();
            } finally {
                ccsDocument.unlock();
            }
            final List<Token> tokens = new LoggingCCSLexer(result).lex(text);
            monitor.worked(WORK_LEXING);

            monitor.subTask("Parsing...");
            ccsProgram = new LoggingCCSParser(result).parse(tokens);
            monitor.worked(WORK_PARSING);

            if (!result.hasParsingErrors()) {
                monitor.subTask("Checking Expression");
                // TODO let user decide if error or warning
                final int unguardedProblemType = ParsingProblem.ERROR;
                final int unregularProblemType = ParsingProblem.WARNING;
                assert result.processVariables.size() == ccsProgram.getProcessVariables().size();
                for (final ReadProcessVariable proc: result.processVariables) {
                    if (!proc.processVariable.isGuarded()) {
                        final Token firstToken = searchFirstToken(result.tokens, proc.getStartPosition());
                        assert firstToken instanceof Identifier && firstToken.getStartPosition() == proc.getStartPosition();
                        result.parsingProblems.add(new ParsingProblem(unguardedProblemType,
                            "This process definition is unguarded.", firstToken));
                    }
                    if (!proc.processVariable.isRegular()) {
                        final Token firstToken = searchFirstToken(result.tokens, proc.getStartPosition());
                        assert firstToken instanceof Identifier && firstToken.getStartPosition() == proc.getStartPosition();
                        result.parsingProblems.add(new ParsingProblem(unregularProblemType,
                            "This process definition is not regular.", firstToken));
                    }
                }
            }
            monitor.worked(WORK_CHECKING);
        } catch (final LexException e) {
            warning = "Error lexing: " + e.getMessage();
        }

        final ParseStatus status;
        if (warning == null)
            status = new ParseStatus(IStatus.OK, "", ccsProgram, result);
        else
            status = new ParseStatus(IStatus.INFO, warning, ccsProgram, result);


        // TODO ParseResult, highlighting, ...
        // TODO additional parameter for the parsing result (map expression->position)

        return status;
    }

    private Token searchFirstToken(List<Token> tokens, int position) {
        // binary search
        int left = 0;
        int right = tokens.size();
        while (left < right) {
            final int mid = (left+right)/2;
            final Token midToken = tokens.get(mid);
            if (position < midToken.getStartPosition())
                right = mid;
            else if (position > midToken.getEndPosition())
                left = mid+1;
            else
                return midToken;
        }

        return null;
    }

    public class ParseStatus extends Status {

        private Program parsedProgram;
        private ParsingResult parsingResult;

        public ParseStatus(int severity, String message) {
            super(severity, Global.getPluginID(), IStatus.OK, message, null);
        }

        public ParseStatus(int severity, String message, Program parsedProgram,
                ParsingResult result) {
            this(severity, message);
            this.parsedProgram = parsedProgram;
            this.parsingResult = result;
        }

        public Program getParsedProgram() {
            return parsedProgram;
        }

        public long getDocModCount() {
            return docModCount;
        }

        public ParsingResult getParsingResult() {
            return parsingResult;
        }

        public boolean isSyncExec() {
            return syncExec;
        }
    }

}

package de.unisb.cs.depend.ccs_sem.lexer;

import java.io.Reader;
import java.util.List;

import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;


/**
 * A lexer that logs some additional information usefull e.g. for syntax highlighting.
 *
 * If a lexing error occures, the error is reported to the {@link ParsingResult}, and
 * additionally, the exception is thrown.
 *
 * @author Clemens Hammahcer
 */
public class LoggingCCSLexer extends CCSLexer {

    private final ParsingResult result;

    public LoggingCCSLexer() {
        this.result = new ParsingResult();
    }

    public LoggingCCSLexer(ParsingResult result) {
        if (result == null)
            throw new NullPointerException();
        this.result = result;
    }

    public ParsingResult getResult() {
        return result;
    }

    @Override
    public List<Token> lex(Reader input) throws LexException {
        try {
            return super.lex(input);
        } catch (LexException e) {
            result.parsingProblems.add(new ParsingProblem(ParsingProblem.ERROR, "Error lexing: " + e.getMessage(), e.getPosition(), e.getPosition()));
            throw e;
        } finally {
            result.inputLength = position;
        }
    }

    @Override
    protected void commentRead(int startPosition, int endPosition) {
        result.newComment(startPosition, endPosition);
        super.commentRead(startPosition, endPosition);
    }

    @Override
    protected void completeLine() {
        result.lineStarts.add(position+1);
        super.completeLine();
    }

}

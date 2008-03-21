package de.unisb.cs.depend.ccs_sem.junit;


import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.parser.IParsingProblemListener;
import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ExpressionRepository;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


/**
 * This is a JUnit4 testcase that checks if a ccs expression creates the correct
 * amount of errors and warnings.
 *
 * @author Clemens Hammacher
 */
public abstract class FailingIntegrationTest implements IParsingProblemListener {

    private List<ParsingProblem> parsingWarnings;
    private List<ParsingProblem> parsingErrors;
    private Program program;

    @Before
    public void initialize() {
        ExpressionRepository.reset();

        // evaluate the expression
        final String expressionString = getExpressionString();
        final CCSParser parser = new CCSParser();
        parsingWarnings = new ArrayList<ParsingProblem>();
        parsingErrors = new ArrayList<ParsingProblem>();
        parser.addProblemListener(this);
        program = parser.parse(expressionString);
    }

    @After
    public void cleanUp() {
        ExpressionRepository.reset();
    }

    @Test
    public void checkErrorsAndWarnings() {
        final StringBuilder sb = new StringBuilder();
        final String newLine = Globals.getNewline();
        if (parsingErrors.size() != getExpectedParsingErrors()) {
            sb.append("Number of parsing errors differs from expected number.").append(newLine);
            sb.append("Expected ").append(getExpectedParsingErrors());
            sb.append(", got ").append(parsingErrors.size()).append(newLine);
            for (final ParsingProblem problem: parsingErrors) {
                sb.append("    --> ").append(problem).append(newLine);
            }
        }
        if (parsingWarnings.size() != getExpectedParsingWarnings()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Number of parsing warnings differs from expected number.").append(newLine);
            sb.append("Expected ").append(getExpectedParsingWarnings());
            sb.append(", got ").append(parsingWarnings.size()).append(newLine);
            for (final ParsingProblem problem: parsingWarnings) {
                sb.append("    --> ").append(problem).append(newLine);
            }
        }
        if (program == null && expectParsedProgram()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Program could not be parsed, but it should.");
        }
        if (program != null && !expectParsedProgram()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Program could be parsed, but it should not.");
        }
        if (program != null && program.isGuarded() && !expectGuardedness()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Program is guarded, but it should not be.");
        }
        if (program != null && !program.isGuarded() && expectGuardedness()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Program is not guarded, but it should be.");
        }
        if (program != null && program.isRegular() && !expectRegularity()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Program is regular, but it should not be.");
        }
        if (program != null && !program.isRegular() && expectRegularity()) {
            if (sb.length() > 0)
                sb.append(newLine);
            sb.append("Program is not regular, but it should be.");
        }
        if (sb.length() > 0) {
            fail(sb.toString());
        }
    }

    protected boolean expectParsedProgram() {
        return true;
    }

    protected boolean expectGuardedness() {
        return true;
    }

    protected boolean expectRegularity() {
        return true;
    }

    protected int getExpectedParsingWarnings() {
        return 0;
    }

    protected int getExpectedParsingErrors() {
        return 0;
    }

    public void reportParsingProblem(ParsingProblem problem) {
        if (problem.getType() == ParsingProblem.ERROR)
            parsingErrors.add(problem);
        if (problem.getType() == ParsingProblem.WARNING)
            parsingWarnings.add(problem);
    }


    // the method to be implemented by subclasses:
    protected abstract String getExpressionString();

}

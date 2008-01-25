package de.unisb.cs.depend.ccs_sem.junit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.unisb.cs.depend.ccs_sem.evalutators.Evaluator;
import de.unisb.cs.depend.ccs_sem.evalutators.SequentialEvaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ExpressionRepository;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


/**
 * This is a JUnit4 testcase that checks for any ccs expression if it creates
 * the correct transition system.
 *
 * @author Clemens Hammacher
 */
public abstract class IntegrationTest {

    private static class SimpleTrans {
        public String label;
        int endNodeNr;

        public SimpleTrans(String label, int endNodeNr) {
            this.label = label;
            this.endNodeNr = endNodeNr;
        }
    }

    private List<String> states;
    private List<List<SimpleTrans>> transitions;

    @Before
    public void setUp() throws Exception {
        ExpressionRepository.reset();
        states = new ArrayList<String>();
        transitions = new ArrayList<List<SimpleTrans>>();
    }

    @After
    public void tearDown() throws Exception {
        ExpressionRepository.reset();
        states = null;
        transitions = null;
    }

    // may be overridden to use another evaluator
    protected Evaluator getEvaluator() {
        return new SequentialEvaluator();
    }

    @Test
    public void runTest() throws ParseException, LexException {
        // first, evaluate the expression
        final String expressionString = getExpressionString();
        final Program program = new CCSParser().parse(expressionString);
        program.evaluate(getEvaluator());

        addStates();
        addTransitions();

        if (states.size() == 0)
            fail("This testcase contains no nodes.");

        // now check if the transition systems are equal
        // (starting at node 0)

        // the queue of expressions to check
        final Deque<Integer> queue = new ArrayDeque<Integer>();
        queue.add(0);

        // mapping from stateNr to expression in the program
        final List<Expression> generatedExpr = new ArrayList<Expression>(states.size());
        generatedExpr.add(program.getMainExpression());

        // first check if the starting state is the same
        assertEquals("The starting states are different",
            states.get(0).toString(), program.getMainExpression().toString());

        while (!queue.isEmpty()) {
            final int stateNr = queue.pollFirst();
            final List<SimpleTrans> expectedTrans = transitions.get(stateNr);
            final Expression expr = generatedExpr.get(stateNr);
            final List<Transition> foundTrans = expr.getTransitions();

            // now compare outTrans with the outgoing transitions of expr
            if (expectedTrans.size() != foundTrans.size())
                fail("Nr of outgoing transitions of state \"" + expr + "\" does not match" +
                		" (expected " + expectedTrans.size() + ", found " + foundTrans.size() + ")");

            outer:
            for (final Transition trans: foundTrans) {
                final String transLabel = trans.getAction().getLabel();
                final String targetLabel = trans.getTarget().toString();
                for (final SimpleTrans sTrans: expectedTrans) {
                    if (sTrans.label.equals(transLabel) &&
                            states.get(sTrans.endNodeNr).equals(targetLabel)) {
                        while (generatedExpr.size() <= sTrans.endNodeNr)
                            generatedExpr.add(null);
                        generatedExpr.set(sTrans.endNodeNr, trans.getTarget());
                        continue outer;
                    }
                }
                fail("Found a transition (\"" + expr.toString() + "\" --\"" + transLabel
                    + "\"-> \"" + targetLabel + "\") that shouldn't be there.");
            }
        }
    }

    protected String decode(String str) {
        final StringBuilder sb = new StringBuilder(str.length() * 3 / 2);

        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];

            if (c == '%') {
                int k = 0;
                while (++i < chars.length && (c = chars[i]) >= '0' && c <= '9')
                    k = 10*k + c - '0';
                --i;
                sb.append((char)k);
            } else
                sb.append(c);
        }

        return sb.toString();
    }

    protected void addState(String label) {
        states.add(label);
        transitions.add(new ArrayList<SimpleTrans>(3));
    }

    protected void addTransition(int startNodeNr, int endNodeNr, String label) {
        if (startNodeNr >= states.size())
            fail("Error in the testcase itself. Node nr " + startNodeNr
                + " is greater/equal to the number of nodes (" + states.size() + ")");
        if (endNodeNr >= states.size())
            fail("Error in the testcase itself. Node nr " + endNodeNr
                + " is greater/equal to the number of nodes (" + states.size() + ")");
        transitions.get(startNodeNr).add(new SimpleTrans(label, endNodeNr));
    }


    // the methods to be implemented by subclasses:

    protected abstract String getExpressionString();

    protected abstract void addStates();

    protected abstract void addTransitions();

}

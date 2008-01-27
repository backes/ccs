package de.unisb.cs.depend.ccs_sem.semantics.types;

import java.util.List;

import de.unisb.cs.depend.ccs_sem.evalutators.EvaluationMonitor;
import de.unisb.cs.depend.ccs_sem.evalutators.Evaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public class Program {

    private final List<Declaration> declarations;
    private final Expression mainExpression;

    public Program(List<Declaration> declarations, Expression expr) throws ParseException {
        this.mainExpression = expr.replaceRecursion(declarations);
        this.declarations = declarations;
        for (final Declaration decl: declarations)
            decl.replaceRecursion(declarations);
    }

    @Override
    public String toString() {
        final String newLine = System.getProperty("line.separator");
        assert newLine != null;

        final StringBuilder sb = new StringBuilder();
        for (final Declaration decl: declarations) {
            sb.append(decl).append(';').append(newLine);
        }
        if (declarations.size() > 0)
            sb.append(newLine);

        sb.append(mainExpression);

        return sb.toString();
    }

    public Expression getMainExpression() {
        return mainExpression;
    }

    /**
     * A program is regular iff every recursive definition is regular.
     * See {@link Declaration#isRegular(List)}.
     */
    public boolean isRegular() {
        for (final Declaration decl: declarations)
            if (!decl.isRegular())
                return false;

        return true;
    }

    /**
     * A program is guarded iff every recursive definition is regular.
     * See {@link Declaration#isGuarded(List)}.
     */
    public boolean isGuarded() {
        for (final Declaration decl: declarations)
            if (!decl.isGuarded())
                return false;

        return true;
    }

    public void evaluate(Evaluator eval) {
        evaluate(eval, null);
    }

    public void evaluate(Evaluator eval, EvaluationMonitor monitor) {
        eval.evaluateAll(mainExpression, monitor);
    }

    public List<Transition> getTransitions() {
        return mainExpression.getTransitions();
    }

    public boolean isEvaluated() {
        return mainExpression.isEvaluated();
    }

    public void minimizeTransitions() {
        mainExpression.minimize();
    }

}

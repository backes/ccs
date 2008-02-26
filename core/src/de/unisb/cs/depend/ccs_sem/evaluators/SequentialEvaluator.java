package de.unisb.cs.depend.ccs_sem.evaluators;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.utils.UniqueQueue;


public class SequentialEvaluator implements Evaluator {

    public boolean evaluate(Expression expr) throws InterruptedException {
        if (expr.isEvaluated())
            return true;

        final LinkedList<Expression> toEvaluate = new LinkedList<Expression>();
        toEvaluate.add(expr);

        Expression e;
        while ((e = toEvaluate.peek()) != null) {
            if (Thread.interrupted())
                throw new InterruptedException();
            boolean ready = true;
            for (final Expression child: e.getChildren()) {
                if (!child.isEvaluated()) {
                    toEvaluate.addFirst(child);
                    ready = false;
                }
            }
            if (!ready)
                continue;
            toEvaluate.remove();
            e.evaluate();
        }

        return true;
    }

    public boolean evaluateAll(Expression expr, EvaluationMonitor monitor)
            throws InterruptedException {
        final Queue<Expression> toEvaluate = new UniqueQueue<Expression>();
        toEvaluate.add(expr);
        final Stack<Expression> childrenToEvaluate = new Stack<Expression>();

        while (true) {
            boolean isChild = !childrenToEvaluate.isEmpty();
            Expression e;
            if (isChild)
                e = childrenToEvaluate.peek();
            else {
                e = toEvaluate.peek();
                if (e == null)
                    break;
            }
            if (Thread.interrupted())
                throw new InterruptedException();
            if (!e.isEvaluated()) {
                boolean ready = true;
                for (final Expression child: e.getChildren()) {
                    if (!child.isEvaluated()) {
                        childrenToEvaluate.add(child);
                        ready = false;
                    }
                }
                if (!ready)
                    continue;
                e.evaluate();
                if (!isChild) {
                    if (monitor != null)
                        monitor.newState(e.getTransitions().size());
                    for (final Transition trans: e.getTransitions()) {
                        final Expression succ = trans.getTarget();
                        toEvaluate.add(succ);
                    }
                }
            }
            if (isChild)
                childrenToEvaluate.pop();
            else
                toEvaluate.remove();
        }

        if (monitor != null)
            monitor.ready();

        return true;
    }

}

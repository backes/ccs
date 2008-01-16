package de.unisb.cs.depend.ccs_sem.exporters.bcg;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.exporters.Exporter;
import de.unisb.cs.depend.ccs_sem.exporters.helpers.StateNumberComparator;
import de.unisb.cs.depend.ccs_sem.exporters.helpers.StateNumerator;
import de.unisb.cs.depend.ccs_sem.exporters.helpers.TransitionsTargetNumberComparator;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class BCGExporter implements Exporter {

    final File bcgFile;

    public BCGExporter(File bcgFile) throws ExportException {
        super();
        this.bcgFile = bcgFile;
        if (!BCGWriter.initialize()) {
            throw new ExportException("Could not initialize BCG library.");
        }
    }

    public void export(Expression expr) throws ExportException {
        // ensure that only one BCG file is written concurrently
        synchronized (BCGWriter.class) {
            final Map<Expression, Integer> stateNumbers =
                    StateNumerator.numerateStates(expr, 1);

            BCGWriter.open(bcgFile.getAbsolutePath(), 0);

            // write the transitions
            final PriorityQueue<Expression> queue =
                    new PriorityQueue<Expression>(11,
                            new StateNumberComparator(stateNumbers));
            queue.add(expr);

            final Set<Expression> written =
                    new HashSet<Expression>(stateNumbers.size() * 3 / 2);
            written.add(expr);

            while (!queue.isEmpty()) {
                final Expression e = queue.poll();
                final Collection<Transition> transitions = e.getTransitions();
                final PriorityQueue<Transition> transQueue =
                        new PriorityQueue<Transition>(transitions.size(),
                                new TransitionsTargetNumberComparator(
                                        stateNumbers));
                transQueue.addAll(transitions);
                final int sourceStateNr = stateNumbers.get(e);
                while (!transQueue.isEmpty()) {
                    final Transition trans = transQueue.poll();
                    final Expression targetExpr = trans.getTarget();
                    final int targetStateNr = stateNumbers.get(targetExpr);
                    BCGWriter.writeTransition(sourceStateNr, targetStateNr,
                        trans.getAction().getLabel());
                    if (written.add(targetExpr))
                        queue.add(targetExpr);
                }
            }

            // close the bcg file
            BCGWriter.close();
        }
    }

    public String getIdentifier() {
        return "BCG file export to " + bcgFile.getPath();
    }

}

package de.unisb.cs.depend.ccs_sem.exporters;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.exporters.helpers.StateNumberComparator;
import de.unisb.cs.depend.ccs_sem.exporters.helpers.StateNumerator;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class AiSeeGraphExporter implements Exporter {

    final File aiSeeFile;

    public AiSeeGraphExporter(File aiSeeFile) {
        super();
        this.aiSeeFile = aiSeeFile;
    }

    public void export(Expression expr) throws ExportException {
        final PrintWriter aiSeeWriter;
        try {
            aiSeeWriter = new PrintWriter(aiSeeFile);
        } catch (final IOException e) {
            throw new ExportException("Error opening AiSee-File: "
                    + e.getMessage(), e);
        }

        final Map<Expression, Integer> stateNumbers =
                StateNumerator.numerateStates(expr);

        // write the header
        aiSeeWriter.println("graph: {");

        // write the states and transitions
        final PriorityQueue<Expression> queue =
                new PriorityQueue<Expression>(11, new StateNumberComparator(
                        stateNumbers));
        queue.add(expr);

        final Set<Expression> written = new HashSet<Expression>(stateNumbers.size()*3/2);
        written.add(expr);

        while (!queue.isEmpty()) {
            final Expression e = queue.poll();
            final Collection<Transition> transitions = e.getTransitions();

            final int sourceStateNr = stateNumbers.get(e);
            aiSeeWriter.print("node: { title: \"");
            aiSeeWriter.print(sourceStateNr);
            aiSeeWriter.print("\" label: \"");
            aiSeeWriter.print(e);
            aiSeeWriter.println("\" }");

            for (final Transition trans: transitions) {
                final Expression targetExpr = trans.getTarget();
                final int targetStateNr = stateNumbers.get(targetExpr);
                aiSeeWriter.print("edge: { source: \"");
                aiSeeWriter.print(sourceStateNr);
                aiSeeWriter.print("\" target: \"");
                aiSeeWriter.print(targetStateNr);
                aiSeeWriter.print("\" label: \"");
                aiSeeWriter.print(trans.getAction().getLabel());
                aiSeeWriter.println("\" }");
                if (written.add(targetExpr))
                    queue.add(targetExpr);
            }
        }

        // close the graph
        aiSeeWriter.println("}");

        // close the tra file
        aiSeeWriter.close();

    }

    public String getIdentifier() {
        return "aiSee Graph File export to " + aiSeeFile.getPath();
    }

}

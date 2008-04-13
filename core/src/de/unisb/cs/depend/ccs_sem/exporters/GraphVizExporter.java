package de.unisb.cs.depend.ccs_sem.exporters;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.exporters.helpers.StateNumberComparator;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.utils.StateNumerator;

public class GraphVizExporter implements Exporter {

    public GraphVizExporter() {
        super();
    }

    public void export(File dotFile, Program program) throws ExportException {
        final Expression expr = program.getExpression();

        final PrintWriter writer;
        try {
            writer = new PrintWriter(dotFile);
        } catch (final IOException e) {
            throw new ExportException("Error opening .dot-File: "
                    + e.getMessage(), e);
        }

        final Map<Expression, Integer> stateNumbers =
                StateNumerator.numerateStates(expr, 1);

        // begin graph
        writer.println("digraph {");

        // write the transitions (nodes are generated implicitly by graphviz)
        final PriorityQueue<Expression> queue =
                new PriorityQueue<Expression>(11, new StateNumberComparator(
                        stateNumbers));
        queue.add(expr);

        final Set<Expression> written = new HashSet<Expression>(stateNumbers.size()*3/2);
        written.add(expr);

        while (!queue.isEmpty()) {
            final Expression e = queue.poll();

            final int sourceStateNo = stateNumbers.get(e);
            writer.println(sourceStateNo + " [label=\"" + quote(e.toString()) + "\"];");

            for (final Transition trans: e.getTransitions()) {
                final Expression targetExpr = trans.getTarget();
                final int targetStateNo = stateNumbers.get(targetExpr);

                writer.println(sourceStateNo + "->" + targetStateNo
                            + " [label=\"" + quote(trans.getAction().getLabel()) + "\"];");
                if (written.add(targetExpr))
                    queue.add(targetExpr);
            }
        }

        writer.println("} // digraph");

        // close the dot file
        writer.close();
    }

    private String quote(String string) {
        final StringBuilder sb = new StringBuilder(string.length() + 2);
        for (int i = 0; i < string.length(); ++i) {
            final char c = string.charAt(i);
            switch (c) {
            case '\\':
                sb.append("\\\\");
                break;
            case '"':
                sb.append("\\\"");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    public String getIdentifier() {
        return "dot File export";
    }


}

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


public class AiSeeGraphExporter implements Exporter {

    final File aiSeeFile;

    public AiSeeGraphExporter(File aiSeeFile) {
        super();
        this.aiSeeFile = aiSeeFile;
    }

    public void export(Program program) throws ExportException {
        final Expression expr = program.getExpression();

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
            final int sourceStateNo = stateNumbers.get(e);
            aiSeeWriter.print("node: { title: \"");
            aiSeeWriter.print(sourceStateNo);
            aiSeeWriter.print("\" label: \"");
            aiSeeWriter.print(quote(e.toString()));
            aiSeeWriter.println("\" }");

            for (final Transition trans: e.getTransitions()) {
                final Expression targetExpr = trans.getTarget();
                final int targetStateNo = stateNumbers.get(targetExpr);
                aiSeeWriter.print("edge: { source: \"");
                aiSeeWriter.print(sourceStateNo);
                aiSeeWriter.print("\" target: \"");
                aiSeeWriter.print(targetStateNo);
                aiSeeWriter.print("\" label: \"");
                aiSeeWriter.print(quote(trans.getAction().getLabel()));
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

    private String quote(String string) {
        final StringBuilder sb = new StringBuilder();
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
        return "aiSee Graph File export to " + aiSeeFile.getPath();
    }

}

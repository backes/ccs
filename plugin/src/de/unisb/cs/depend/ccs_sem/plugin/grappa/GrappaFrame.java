package de.unisb.cs.depend.ccs_sem.plugin.grappa;

import java.awt.Frame;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.GrappaPanel;
import att.grappa.GrappaSupport;
import att.grappa.Node;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class GrappaFrame extends Composite {

    // TODO ask user
    private static final String DOT_EXECUTABLE = "E:\\Graphviz2.17\\bin\\dot.exe";
    private final GrappaPanel grappaPanel;
    private final Graph graph = new Graph("CSS-Graph");
    private final CCSEditor ccsEditor;

    public GrappaFrame(Composite parent, int style, CCSEditor editor) {
        super(parent, style | SWT.EMBEDDED);
        this.ccsEditor = editor;
        setLayout(new FillLayout(SWT.VERTICAL));

        final Frame grappaFrame = SWT_AWT.new_Frame(this);
        grappaPanel = new GrappaPanel(graph);
        grappaPanel.setScaleToFit(true);
        grappaFrame.setLayout(new GridLayout(1, 1));
        grappaFrame.add(grappaPanel);

        final Node node = new Node(graph, "warn_node");
        graph.addNode(node);
        node.setAttribute("label", "Not initialized...");
        graph.repaint();
    }

    @Override
    public void update() {

        // parse ccs term
        Program ccsProgram = null;
        String warning = null;
        try {
            ccsProgram = ccsEditor.getCCSProgram(true);
        } catch (final ParseException e) {
            warning = "Error lexing: " + e.getMessage();
        } catch (final LexException e) {
            warning = "Error parsing: " + e.getMessage();
        }

        graph.reset();

        if (warning != null) {
            final Node node = new Node(graph, "warn_node");
            graph.addNode(node);
            node.setAttribute("label", warning);
            graph.repaint();
            return;
        }


        final Queue<Expression> queue = new ArrayDeque<Expression>();
        queue.add(ccsProgram.getMainExpression());

        final Set<Expression> written = new HashSet<Expression>();
        written.add(ccsProgram.getMainExpression());

        final Map<Expression, Node> nodes = new HashMap<Expression, Node>();

        // first, create all nodes
        int cnt = 0;
        while (!queue.isEmpty()) {
            final Expression e = queue.poll();
            final Node node = new Node(graph, "node_" + cnt++);
            node.setAttribute("label", e.toString());
            nodes.put(e, node);
            graph.addNode(node);
            for (final Transition trans: e.getTransitions())
                if (written.add(trans.getTarget()))
                    queue.add(trans.getTarget());
        }

        // then, create the edges
        queue.add(ccsProgram.getMainExpression());
        written.clear();
        written.add(ccsProgram.getMainExpression());
        cnt = 0;

        while (!queue.isEmpty()) {
            final Expression e = queue.poll();
            final Node headNode = nodes.get(e);

            for (final Transition trans: e.getTransitions()) {
                final Node tailNode = nodes.get(trans.getTarget());
                final Edge edge = new Edge(graph, tailNode, headNode, "edge_" + cnt++);
                edge.setAttribute("label", trans.getAction().getLabel());
                graph.addEdge(edge);
                if (written.add(trans.getTarget()))
                    queue.add(trans.getTarget());
            }
        }

        if (!filterGraph(graph)) {
            System.err.println("Could not layout graph.");
            // TODO
        }
        graph.repaint();
    }

    private boolean filterGraph(Graph graph) {
        // start dot
        final List<String> command = new ArrayList<String>();
        command.add(DOT_EXECUTABLE);

        final ProcessBuilder pb = new ProcessBuilder(command);
        Process dotFilter;
        try {
            dotFilter = pb.start();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return GrappaSupport.filterGraph(graph, dotFilter);
    }

    public Graph getGraph() {
        return graph;
    }

    public CCSEditor getCcsEditor() {
        return ccsEditor;
    }

}

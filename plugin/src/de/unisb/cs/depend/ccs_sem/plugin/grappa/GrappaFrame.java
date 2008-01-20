package de.unisb.cs.depend.ccs_sem.plugin.grappa;

import java.awt.Color;
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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.Grappa;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaConstants;
import att.grappa.GrappaPanel;
import att.grappa.GrappaSupport;
import att.grappa.Node;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class GrappaFrame extends Composite {

    private static final Color startNodeColor = Color.LIGHT_GRAY;
    private static final Color warnNodeColor = Color.RED;

    private final GrappaPanel grappaPanel;
    private final Graph graph = new Graph("CSS-Graph");
    private final CCSEditor ccsEditor;

    public GrappaFrame(Composite parent, int style, CCSEditor editor) {
        super(parent, style);
        this.ccsEditor = editor;
        setLayout(new FillLayout(SWT.VERTICAL));

        final Composite controlsComposite = new Composite(this, SWT.None);
        controlsComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
        final Text text = new Text(controlsComposite, SWT.None);
        text.setText("Controls go here...");
        text.setEnabled(false);

        final Composite graphComposite = new Composite(this, SWT.EMBEDDED);
        graphComposite.setLayout(new org.eclipse.swt.layout.GridLayout(1, true));

        final Frame grappaFrame = SWT_AWT.new_Frame(graphComposite);
        grappaPanel = new GrappaPanel(graph);
        grappaPanel.addGrappaListener(new GrappaAdapter());
        grappaPanel.setScaleToFit(true);
        grappaFrame.setLayout(new GridLayout(1, 1));
        grappaFrame.add(grappaPanel);

        Grappa.antiAliasText = true;
        Grappa.useAntiAliasing = true;
        Grappa.elementSelection = GrappaConstants.NODE | GrappaConstants.EDGE;

        final Node node = new Node(graph, "warn_node");
        graph.addNode(node);
        node.setAttribute(GrappaConstants.LABEL_ATTR, "Not initialized...");
        node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
        node.setAttribute(GrappaConstants.COLOR_ATTR, warnNodeColor);
        filterGraph(graph);
        graph.repaint();
    }

    @Override
    public void update() {

        // parse ccs term
        Program ccsProgram = null;
        String warning = null;
        try {
            ccsProgram = ccsEditor.getCCSProgram(true);
        } catch (final LexException e) {
            warning = "Error lexing: " + e.getMessage()
                + " (around this context: " + e.getEnvironment() + ")";
        } catch (final ParseException e) {
            warning = "Error parsing: " + e.getMessage()
                + " (around this context: " + e.getEnvironment() + ")";
        }

        graph.reset();

        if (warning != null) {
            final Node node = new Node(graph, "warn_node");
            graph.addNode(node);
            node.setAttribute(GrappaConstants.LABEL_ATTR, warning);
            node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
            node.setAttribute(GrappaConstants.FILLCOLOR_ATTR, warnNodeColor);
            node.setAttribute(GrappaConstants.TIP_ATTR,
                "The graph could not be built. This is the reason why.");
            filterGraph(graph);
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
            node.setAttribute(GrappaConstants.LABEL_ATTR, e.toString());
            if (cnt == 1) {
                node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
                node.setAttribute(GrappaConstants.FILLCOLOR_ATTR, startNodeColor);
            }
            node.setAttribute(GrappaConstants.TIP_ATTR, "Node: " + e.toString());
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
            final Node tailNode = nodes.get(e);

            for (final Transition trans: e.getTransitions()) {
                final Node headNode = nodes.get(trans.getTarget());
                final Edge edge = new Edge(graph, tailNode, headNode, "edge_" + cnt++);
                final String label = trans.getAction().getLabel();
                edge.setAttribute(GrappaConstants.LABEL_ATTR, label);
                edge.setAttribute(GrappaConstants.TIP_ATTR, "Transition: " + label);
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
        command.add(getDotExecutablePath());

        final ProcessBuilder pb = new ProcessBuilder(command);
        Process dotFilter;
        try {
            dotFilter = pb.start();
        } catch (final IOException e) {
            if (MessageDialog.openQuestion(getShell(), "Error layouting graph",
                "The graph could not be layout, because the 'dot' tool could not be started.\n" +
                "Do you want to configure the path for this tool now?")) {

                // TODO show preferences page
                // now, try again
                return filterGraph(graph);
            }
            return false;
        }
        return GrappaSupport.filterGraph(graph, dotFilter);
    }

    private String getDotExecutablePath() {
        final String dotExecutable = Global.getPreferenceDot();
        return dotExecutable;
    }

    public Graph getGraph() {
        return graph;
    }

    public CCSEditor getCcsEditor() {
        return ccsEditor;
    }

}

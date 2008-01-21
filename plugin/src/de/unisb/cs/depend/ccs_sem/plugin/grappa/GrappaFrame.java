package de.unisb.cs.depend.ccs_sem.plugin.grappa;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagLayout;
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

import javax.swing.JScrollPane;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

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

    protected final GrappaPanel grappaPanel;
    protected final Graph graph = new Graph("CSS-Graph");
    private final CCSEditor ccsEditor;
    private boolean showEdgeLabels = true;
    private boolean showNodeLabels = true;
    protected boolean layoutLeftToRight = true;

    public GrappaFrame(Composite parent, int style, CCSEditor editor) {
        super(parent, style);
        this.ccsEditor = editor;
        setLayout(new org.eclipse.swt.layout.GridLayout(1, true));

        final Composite controlsComposite = new Composite(this, SWT.None);
        controlsComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
        controlsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        /*
        final ScrolledComposite scrolledGraphComposite = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledGraphComposite.setExpandVertical(true);
        scrolledGraphComposite.setExpandHorizontal(true);
        scrolledGraphComposite.setLayout(new org.eclipse.swt.layout.GridLayout(1, true));
        scrolledGraphComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledGraphComposite.setAlwaysShowScrollBars(true);
        */

        //final Composite graphComposite = new Composite(scrolledGraphComposite, SWT.EMBEDDED);
        final Composite graphComposite = new Composite(this, SWT.EMBEDDED);
        //scrolledGraphComposite.setContent(graphComposite);
        graphComposite.setLayout(new org.eclipse.swt.layout.GridLayout(1, true));
        graphComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        final Frame grappaFrame = SWT_AWT.new_Frame(graphComposite);
        grappaPanel = new GrappaPanel(graph);
        grappaPanel.addGrappaListener(new GrappaAdapter());
        grappaPanel.setScaleToFit(true);
        grappaFrame.setLayout(new GridBagLayout());
        final JScrollPane scroll = new JScrollPane(grappaPanel);
        grappaFrame.setLayout(new GridLayout(1,1));
        grappaFrame.add(scroll);

        Grappa.antiAliasText = true;
        Grappa.useAntiAliasing = true;
        Grappa.elementSelection = GrappaConstants.NODE | GrappaConstants.EDGE;

        final Node node = new Node(graph, "warn_node");
        graph.addNode(node);
        node.setAttribute(GrappaConstants.LABEL_ATTR, "Not initialized...");
        node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
        node.setAttribute(GrappaConstants.COLOR_ATTR, warnNodeColor);
        node.setAttribute(GrappaConstants.TIP_ATTR, "In the CCS Editor, click the \"Show Graph\" button to generate the graph.");
        filterGraph(graph);
        graph.repaint();

        // add control components
        final Button buttonScaleToFit = new Button(controlsComposite, SWT.CHECK);
        buttonScaleToFit.setSelection(true);
        buttonScaleToFit.setText("Scale to fit");

        final Button buttonZoomIn = new Button(controlsComposite, SWT.PUSH);
        buttonZoomIn.setEnabled(false);
        buttonZoomIn.setText("Zoom in");

        final Button buttonZoomOut = new Button(controlsComposite, SWT.PUSH);
        buttonZoomOut.setEnabled(false);
        buttonZoomOut.setText("Zoom out");

        final Button buttonShowNodeLabels = new Button(controlsComposite, SWT.CHECK);
        buttonShowNodeLabels.setSelection(true);
        buttonShowNodeLabels.setText("Show node labels");

        final Button buttonShowEdgeLabels = new Button(controlsComposite, SWT.CHECK);
        buttonShowEdgeLabels.setSelection(true);
        buttonShowEdgeLabels.setText("Show edge labels");

        final Button buttonLayoutTopToBottom = new Button(controlsComposite, SWT.RADIO);
        buttonLayoutTopToBottom.setSelection(false);
        buttonLayoutTopToBottom.setText("Layout top to bottom");

        final Button buttonLayoutLeftToRight = new Button(controlsComposite, SWT.RADIO);
        buttonLayoutLeftToRight.setSelection(true);
        buttonLayoutLeftToRight.setText("Layout left to right");

        buttonScaleToFit.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                boolean scale = buttonScaleToFit.getSelection();
                buttonZoomIn.setEnabled(!scale);
                buttonZoomOut.setEnabled(!scale);
                /*
                final Point clientArea = scrolledGraphComposite.getSize();
                grappaPanel.setSize(clientArea.x, clientArea.y);
                */
                grappaPanel.setScaleToFit(scale);
                grappaPanel.repaint();
            }

        });
        buttonZoomIn.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                grappaPanel.multiplyScaleFactor(1.25);
                grappaPanel.repaint();
            }

        });
        buttonZoomOut.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                grappaPanel.multiplyScaleFactor(0.8);
                grappaPanel.repaint();
            }

        });

        buttonShowNodeLabels.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                setShowNodeLabels(buttonShowNodeLabels.getSelection());
                update();
            }

        });
        buttonShowEdgeLabels.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                setShowEdgeLabels(buttonShowEdgeLabels.getSelection());
                update();
            }

        });

        buttonLayoutTopToBottom.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (buttonLayoutTopToBottom.getSelection()) {
                    layoutLeftToRight = false;
                    update();
                }
            }

        });
        buttonLayoutLeftToRight.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (buttonLayoutLeftToRight.getSelection()) {
                    layoutLeftToRight = true;
                    update();
                }
            }

        });
    }

    protected void setShowEdgeLabels(boolean showEdgeLabels) {
        this.showEdgeLabels = showEdgeLabels;
    }

    protected void setShowNodeLabels(boolean showNodeLabels) {
        this.showNodeLabels = showNodeLabels;
    }

    @Override
    public synchronized void update() {

        // parse ccs term
        Program ccsProgram = null;
        String warning = null;
        try {
            ccsProgram = ccsEditor.getCCSProgram(true);
        } catch (final LexException e) {
            warning = "Error lexing: " + e.getMessage() + "\\n"
                + "(around this context: " + e.getEnvironment() + ")";
        } catch (final ParseException e) {
            warning = "Error parsing: " + e.getMessage() + "\\n"
                + "(around this context: " + e.getEnvironment() + ")";
        }

        graph.reset();
        graph.setAttribute("root", "node_0");
        graph.setAttribute(GrappaConstants.TIP_ATTR, "");
        // set layout direction to "left to right"
        if (layoutLeftToRight)
            graph.setAttribute(GrappaConstants.RANKDIR_ATTR, "LR");

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
            node.setAttribute(GrappaConstants.LABEL_ATTR,
                showNodeLabels ? e.toString() : "");
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
                final String label = showEdgeLabels ? trans.getAction().getLabel() : "";
                edge.setAttribute(GrappaConstants.LABEL_ATTR, label);
                final StringBuilder tipBuilder = new StringBuilder(230);
                tipBuilder.append("<html><table border=0>");
                tipBuilder.append("<tr><td align=right><i>Transition:</i></td><td>").append(label).append("</td></tr>");
                tipBuilder.append("<tr><td align=right><i>from:</i></td><td>").append(e.toString()).append("</td></tr>");
                tipBuilder.append("<tr><td align=right><i>to:</i></td><td>").append(trans.getTarget().toString()).append("</td></tr>");
                tipBuilder.append("</table></html>.");
                edge.setAttribute(GrappaConstants.TIP_ATTR, tipBuilder.toString());
                graph.addEdge(edge);
                if (written.add(trans.getTarget()))
                    queue.add(trans.getTarget());
            }
        }

        if (!filterGraph(graph))
            System.err.println("Could not layout graph.");

        graph.repaint();
    }

    private boolean filterGraph(Graph graph) {
        // start dot
        final List<String> command = new ArrayList<String>();
        command.add(getDotExecutablePath());

        final ProcessBuilder pb = new ProcessBuilder(command);
        Process dotFilter = null;
        boolean success = true;
        try {
            dotFilter = pb.start();
        } catch (final IOException e) {
            success = false;
        }

        if (success)
            success &= GrappaSupport.filterGraph(graph, dotFilter);

        if (!success) {
            MessageDialog.openError(getShell(), "Error layouting graph",
                "The graph could not be layout, most probably there was an error with starting the dot tool.\n" +
                "You can configure the path for this tool in your preferences on the \"CCS\" page.");
        }

        return success;
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

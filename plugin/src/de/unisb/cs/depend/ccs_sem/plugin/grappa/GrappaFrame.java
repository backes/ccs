package de.unisb.cs.depend.ccs_sem.plugin.grappa;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JScrollPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import att.grappa.Graph;
import att.grappa.Grappa;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaConstants;
import att.grappa.GrappaPanel;
import att.grappa.Node;
import att.grappa.Subgraph;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.GraphUpdateJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.GraphUpdateJob.GraphUpdateStatus;


public class GrappaFrame extends Composite implements Observer {

    protected GrappaPanel grappaPanel;
    protected final Graph graph;
    private final CCSEditor ccsEditor;
    protected boolean showEdgeLabels = true;
    protected boolean showNodeLabels = true;
    protected boolean layoutLeftToRight = true;
    protected boolean minimizeGraph = false;

    protected Lock graphLock = new ReentrantLock();

    private GraphUpdateJob graphUpdateJob;
    protected final JScrollPane grappaPanelScrollPane;
    protected Button buttonScaleToFit;
    protected Button buttonZoomIn;
    protected Button buttonZoomOut;
    protected Button buttonShowNodeLabels;
    protected Button buttonShowEdgeLabels;
    protected Button buttonMinimize;
    protected Button buttonLayoutTopToBottom;
    protected Button buttonLayoutLeftToRight;
    protected boolean scaleToFit = true;

    public GrappaFrame(Composite parent, int style, CCSEditor editor) {
        super(parent, style);

        this.ccsEditor = editor;
        setLayout(new org.eclipse.swt.layout.GridLayout());

        graphLock.lock();
        try {
            graph = new Graph("CSS-Graph");
            final Node node = new Node(graph, "warn_node");
            node.setAttribute(GrappaConstants.LABEL_ATTR, "Not initialized...");
            node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
            node.setAttribute(GrappaConstants.COLOR_ATTR, GraphHelper.WARN_NODE_COLOR);
            node.setAttribute(GrappaConstants.TIP_ATTR, "In the CCS Editor, click the \"Show Graph\" button to generate the graph.");
            graph.addNode(node);
            try {
                GraphHelper.filterGraph(graph);
            } catch (final InterruptedException ignore) {
                // ignore
            }
            graph.repaint();
        } finally {
            graphLock.unlock();
        }

        final Composite controlsComposite = new Composite(this, SWT.None);
        controlsComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
        controlsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Composite graphComposite = new Composite(this, SWT.EMBEDDED);
        graphComposite.setLayout(new org.eclipse.swt.layout.GridLayout());
        graphComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        final Frame grappaFrame = SWT_AWT.new_Frame(graphComposite);
        grappaPanel = new SynchronizedGrappaPanel(graph);
        grappaPanel.addGrappaListener(new GrappaAdapter());
        grappaPanel.setScaleToFit(true);
        graph.addPanel(grappaPanel);
        grappaPanelScrollPane = new JScrollPane(grappaPanel);
        grappaFrame.setLayout(new GridLayout(1,1));
        grappaFrame.add(grappaPanelScrollPane);

        // Eclipse <3.3 workaround:
        graphComposite.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                graphLock.lock();
                try {
                    grappaFrame.setSize(graphComposite.getSize().x, graphComposite.getSize().y);
                } finally {
                    graphLock.unlock();
                }
            }

        });

        Grappa.antiAliasText = true;
        Grappa.useAntiAliasing = true;
        Grappa.elementSelection = GrappaConstants.NODE | GrappaConstants.EDGE;

        buttonScaleToFit = new Button(controlsComposite, SWT.CHECK);
        buttonScaleToFit.setSelection(true);
        buttonScaleToFit.setText("Scale to fit");

        buttonZoomIn = new Button(controlsComposite, SWT.PUSH);
        buttonZoomIn.setEnabled(false);
        buttonZoomIn.setText("Zoom in");

        buttonZoomOut = new Button(controlsComposite, SWT.PUSH);
        buttonZoomOut.setEnabled(false);
        buttonZoomOut.setText("Zoom out");

        buttonShowNodeLabels = new Button(controlsComposite, SWT.CHECK);
        buttonShowNodeLabels.setSelection(true);
        buttonShowNodeLabels.setText("Show node labels");

        buttonShowEdgeLabels = new Button(controlsComposite, SWT.CHECK);
        buttonShowEdgeLabels.setSelection(true);
        buttonShowEdgeLabels.setText("Show edge labels");

        buttonMinimize = new Button(controlsComposite, SWT.CHECK);
        buttonMinimize.setSelection(false);
        buttonMinimize.setText("Minimize LTS");

        buttonLayoutTopToBottom = new Button(controlsComposite, SWT.RADIO);
        buttonLayoutTopToBottom.setSelection(false);
        buttonLayoutTopToBottom.setText("Layout top to bottom");

        buttonLayoutLeftToRight = new Button(controlsComposite, SWT.RADIO);
        buttonLayoutLeftToRight.setSelection(true);
        buttonLayoutLeftToRight.setText("Layout left to right");

        buttonScaleToFit.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                graphLock.lock();
                try {
                    scaleToFit = buttonScaleToFit.getSelection();
                    buttonZoomIn.setEnabled(!scaleToFit);
                    buttonZoomOut.setEnabled(!scaleToFit);
                    grappaPanel.setScaleToFit(scaleToFit);
                    grappaPanel.repaint();
                } finally {
                    graphLock.unlock();
                }
            }

        });
        buttonZoomIn.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                graphLock.lock();
                try {
                    grappaPanel.multiplyScaleFactor(1.25);
                    grappaPanel.repaint();
                } finally {
                    graphLock.unlock();
                }
            }

        });
        buttonZoomOut.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                graphLock.lock();
                try {
                    grappaPanel.multiplyScaleFactor(0.8);
                    grappaPanel.repaint();
                } finally {
                    graphLock.unlock();
                }
            }

        });

        buttonShowNodeLabels.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                graphLock.lock();
                try {
                    showNodeLabels = buttonShowNodeLabels.getSelection();
                    updateGraph();
                } finally {
                    graphLock.unlock();
                }
            }

        });
        buttonShowEdgeLabels.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                graphLock.lock();
                try {
                    showEdgeLabels = buttonShowEdgeLabels.getSelection();
                    updateGraph();
                } finally {
                    graphLock.unlock();
                }
            }

        });

        buttonMinimize.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                graphLock.lock();
                try {
                    minimizeGraph  = buttonMinimize.getSelection();
                    updateGraph();
                } finally {
                    graphLock.unlock();
                }
            }

        });

        buttonLayoutTopToBottom.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (!buttonLayoutTopToBottom.getSelection())
                    return;
                graphLock.lock();
                try {
                    layoutLeftToRight = false;
                    updateGraph();
                } finally {
                    graphLock.unlock();
                }
            }

        });
        buttonLayoutLeftToRight.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (!buttonLayoutLeftToRight.getSelection())
                    return;
                graphLock.lock();
                try {
                    layoutLeftToRight = true;
                    updateGraph();
                } finally {
                    graphLock.unlock();
                }
            }

        });
    }

    public synchronized void updateGraph() {
        if (graphUpdateJob != null)
            graphUpdateJob.cancel();
        graphUpdateJob = new GraphUpdateJob(ccsEditor.getText(), minimizeGraph,
            layoutLeftToRight, showNodeLabels, showEdgeLabels);
        graphUpdateJob.addObserver(this);
        graphUpdateJob.schedule();
    }

    public CCSEditor getCcsEditor() {
        return ccsEditor;
    }

    public void update(Observable o, Object arg) {
        if (arg instanceof GraphUpdateStatus) {
            final GraphUpdateStatus status = (GraphUpdateStatus) arg;
            if (!status.isOK())
                return;
            setGraph(status);
        }
    }

    private void setGraph(GraphUpdateStatus status) {
        graphLock.lock();
        try {
            final Graph newGraph = status.getGraph();
            grappaPanel = new SynchronizedGrappaPanel(newGraph);
            grappaPanel.addGrappaListener(new GrappaAdapter());
            grappaPanel.setScaleToFit(scaleToFit);
            newGraph.addPanel(grappaPanel);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    grappaPanelScrollPane.setViewportView(grappaPanel);
                }
            });

            // update buttons
            final GraphUpdateJob job = status.getJob();
            getDisplay().asyncExec(new Runnable() {
                public void run() {
                    buttonMinimize.setSelection(job.isMinimize());
                    buttonLayoutLeftToRight.setSelection(job.isLayoutLeftToRight());
                    buttonLayoutTopToBottom.setSelection(!job.isLayoutLeftToRight());
                    buttonShowEdgeLabels.setSelection(job.isShowEdgeLabels());
                    buttonShowNodeLabels.setSelection(job.isShowNodeLabels());
                }
            });
        } finally {
            graphLock.unlock();
        }
    }

    private final class SynchronizedGrappaPanel extends GrappaPanel {

        private static final long serialVersionUID =
                368128314339198689L;

        protected SynchronizedGrappaPanel(Subgraph subgraph) {
            super(subgraph);
        }

        @Override
        public void paintComponent(Graphics g) {
            graphLock.lock();
            try {
                super.paintComponent(g);
            } finally {
                graphLock.unlock();
            }
        }
    }

}

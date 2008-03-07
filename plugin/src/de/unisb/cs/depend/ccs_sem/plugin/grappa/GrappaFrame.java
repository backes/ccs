package de.unisb.cs.depend.ccs_sem.plugin.grappa;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import att.grappa.Graph;
import att.grappa.Grappa;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaConstants;
import att.grappa.GrappaPanel;
import att.grappa.Node;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.GraphUpdateJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.GraphUpdateJob.GraphUpdateStatus;


public class GrappaFrame extends Composite implements Observer {

    protected volatile GrappaPanel grappaPanel;
    private final CCSEditor ccsEditor;
    protected volatile boolean showEdgeLabels = true;
    protected volatile boolean showNodeLabels = true;
    protected volatile boolean layoutLeftToRight = true;
    protected volatile boolean minimizeGraph = false;
    protected volatile boolean scaleToFit = true;

    protected Lock graphLock = new ReentrantLock();

    private GraphUpdateJob graphUpdateJob;
    protected Frame bridgeFrame;
    protected ScrolledComposite scrollComposite;
    protected Graph graph;

    public GrappaFrame(Composite parent, int style, CCSEditor editor) {
        super(parent, style);

        this.ccsEditor = editor;
        setLayout(new org.eclipse.swt.layout.GridLayout());

        graphLock.lock();
        try {
            graph = createGraph();
            final Node node = new Node(graph, "warn_node");
            node.setAttribute(GrappaConstants.LABEL_ATTR,
            		"Click the \"Show Graph\" button to create the LTS.");
            node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
            node.setAttribute(GrappaConstants.COLOR_ATTR, GraphHelper.WARN_NODE_COLOR);
            node.setAttribute(GrappaConstants.TIP_ATTR, "In the CCS Editor, click the \"Show Graph\" button to generate the graph.");
            node.setAttribute(GrappaConstants.SHAPE_ATTR, "plaintext");
            graph.addNode(node);
            try {
                GraphHelper.filterGraph(graph);
            } catch (final InterruptedException ignore) {
                // reset interrupted flag
                Thread.currentThread().interrupt();
            }
            graph.repaint();
        } finally {
            graphLock.unlock();
        }

        scrollComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setMinWidth(100);
        scrollComposite.setMinHeight(100);
        scrollComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        final Composite embeddedComposite = new Composite(scrollComposite, SWT.EMBEDDED);
        scrollComposite.setContent(embeddedComposite);
        embeddedComposite.setLayout(new GridLayout());

        bridgeFrame = SWT_AWT.new_Frame(embeddedComposite);
        grappaPanel = createGrappaPanel(graph);
        bridgeFrame.add(grappaPanel);

        scrollComposite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                graphLock.lock();
                try {
                    final Rectangle rect = scrollComposite.getClientArea();
                    Dimension dim = new Dimension(rect.width, rect.height);

                    grappaPanel.overrideParentSize(dim);
                    dim = grappaPanel.getDesiredSize();
                    grappaPanel.setSize(dim);
                    grappaPanel.setPreferredSize(dim);
                } finally {
                    graphLock.unlock();
                }
            }
        });

        Grappa.antiAliasText = true;
        Grappa.useAntiAliasing = true;
        Grappa.elementSelection = GrappaConstants.NODE | GrappaConstants.EDGE;
    }

    private Graph createGraph() {
        final Graph newGraph = new Graph("CSS-Graph");
        //newGraph.setAttribute(GrappaConstants.MARGIN_ATTR, "0.1,0.1");
        return newGraph;
    }

    private GrappaPanel createGrappaPanel(Graph newGraph) {
        final GrappaPanel newGrappaPanel = new GrappaPanel(newGraph) {

            private static final long serialVersionUID = 1142753635531033476L;

            @Override
            public void paintComponent(Graphics g) {
                graphLock.lock();
                try {
                    super.paintComponent(g);
                } finally {
                    graphLock.unlock();
                }
            }

        };
        newGrappaPanel.addGrappaListener(new GrappaAdapter());
        newGrappaPanel.setScaleToFit(scaleToFit);
        newGraph.addPanel(newGrappaPanel);
        newGrappaPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        graphLock.lock();
                        try {
                            final Dimension dim = scaleToFit ? new Dimension(1, 1)
                                : grappaPanel.getDesiredSize();
                            scrollComposite.setMinSize(dim.width, dim.height);
                        } finally {
                            graphLock.unlock();
                        }
                    }
                });
            }
        });

        // set the parent size
        getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (newGrappaPanel.getOverriddenParentSize() != null)
                    return;
                final Rectangle rect = scrollComposite.getClientArea();
                newGrappaPanel.overrideParentSize(new Dimension(rect.width, rect.height));
            }
        });

        return newGrappaPanel;
    }

    public synchronized void updateGraph() {
        if (graphUpdateJob != null)
            graphUpdateJob.cancel();
        graphUpdateJob = new GraphUpdateJob(ccsEditor.getText(), minimizeGraph,
            layoutLeftToRight, showNodeLabels, showEdgeLabels);
        graphUpdateJob.addObserver(this);
        graphUpdateJob.schedule();
    }

    public CCSEditor getCCSEditor() {
        return ccsEditor;
    }

    public void update(Observable o, Object arg) {
        if (arg instanceof GraphUpdateStatus) {
            final GraphUpdateStatus status = (GraphUpdateStatus) arg;
            /*
            if (!status.isOK())
                return;
            */
            setGraph(status);
        }
    }

    private void setGraph(GraphUpdateStatus status) {
        graphLock.lock();
        try {
            final Graph graph = status.getGraph();
            grappaPanel = createGrappaPanel(graph);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    bridgeFrame.removeAll();
                    bridgeFrame.add(grappaPanel);
                    bridgeFrame.validate();
                }
            });
        } finally {
            graphLock.unlock();
        }
    }

	public void setScaleToFit(boolean scaleToFit) {
        graphLock.lock();
        try {
        	this.scaleToFit = scaleToFit;
            grappaPanel.setScaleToFit(scaleToFit);
            /*
            if (scaleToFit) {
                final Rectangle rect = scrollComposite.getClientArea();
                grappaPanel.setSize(rect.width, rect.height);
            }
            */
            grappaPanel.repaint();
        } finally {
            graphLock.unlock();
        }
	}

	public void zoomIn() {
        graphLock.lock();
        try {
            grappaPanel.multiplyScaleFactor(1.25);
            grappaPanel.repaint();
        } finally {
            graphLock.unlock();
        }
	}

	public void zoomOut() {
        graphLock.lock();
        try {
            grappaPanel.multiplyScaleFactor(0.8);
            grappaPanel.repaint();
        } finally {
            graphLock.unlock();
        }
	}

	public void setShowNodes(boolean showNodeLabels, boolean updateGraph) {
        graphLock.lock();
        try {
            this.showNodeLabels = showNodeLabels;
            if (updateGraph)
            	updateGraph();
        } finally {
            graphLock.unlock();
        }
	}

	public void setShowEdges(boolean showEdgeLabels, boolean updateGraph) {
        graphLock.lock();
        try {
            this.showEdgeLabels = showEdgeLabels;
            if (updateGraph)
            	updateGraph();
        } finally {
            graphLock.unlock();
        }
	}

	public void setMinimize(boolean minimize, boolean updateGraph) {
        graphLock.lock();
        try {
            minimizeGraph  = minimize;
            if (updateGraph)
            	updateGraph();
        } finally {
            graphLock.unlock();
        }
	}

	public void setLayoutLeftToRight(boolean layoutLeftToRight, boolean updateGraph) {
        graphLock.lock();
        try {
            this.layoutLeftToRight = layoutLeftToRight;
            if (updateGraph)
            	updateGraph();
        } finally {
            graphLock.unlock();
        }
	}

}

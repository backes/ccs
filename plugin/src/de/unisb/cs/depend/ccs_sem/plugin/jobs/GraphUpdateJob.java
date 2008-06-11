package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.GrappaConstants;
import att.grappa.Node;
import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GraphHelper;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.utils.UniqueQueue;


public class GraphUpdateJob extends Job {

    protected final boolean layoutLeftToRight;

    protected final boolean showNodeLabels;
    protected final boolean showEdgeLabels;
    protected final EvaluationStatus evalStatus;

    private static final ISchedulingRule rule = new IdentityRule();

    private final static int WORK_CREATE_GRAPH = 5;
    private final static int WORK_LAYOUT_GRAPH = 50;

    public GraphUpdateJob(EvaluationStatus evalStatus,
            boolean layoutLeftToRight, boolean showNodeLabels,
            boolean showEdgeLabels) {
        super("Update Graph");
        this.evalStatus = evalStatus;
        this.layoutLeftToRight = layoutLeftToRight;
        this.showNodeLabels = showNodeLabels;
        this.showEdgeLabels = showEdgeLabels;
        setUser(true);
        setPriority(INTERACTIVE);
        setRule(rule);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        final ConcurrentJob job = new ConcurrentJob(monitor);
        final FutureTask<GraphUpdateStatus> status = new FutureTask<GraphUpdateStatus>(job);
        final Thread executingThread = new Thread(status, getClass().getSimpleName() + " worker");
        executingThread.start();

        while (true) {
            try {
                final GraphUpdateStatus graphUpdateStatus = status.get(100, TimeUnit.MILLISECONDS);
                return graphUpdateStatus;
            } catch (final InterruptedException e) {
                // the *job* should not be interrupted. If cancel is pressed,
                // the inner thread is interrupted, but not this one!
                throw new RuntimeException(e);
            } catch (final ExecutionException e) {
                // an abnormal exception: let eclipse show it to the user
                throw new RuntimeException(e);
            } catch (final TimeoutException e) {
                if (monitor.isCanceled()) {
                    status.cancel(true);
                    try {
                        executingThread.join();
                    } catch (final InterruptedException e1) {
                        // restore and ignore
                        Thread.currentThread().interrupt();
                    }
                    return new GraphUpdateStatus(IStatus.CANCEL, "Cancelled.");
                }
            }
        }
    }

    public boolean isLayoutLeftToRight() {
        return layoutLeftToRight;
    }

    public boolean isShowNodeLabels() {
        return showNodeLabels;
    }

    public boolean isShowEdgeLabels() {
        return showEdgeLabels;
    }


    private class ConcurrentJob implements Callable<GraphUpdateStatus> {

        private final IProgressMonitor monitor;
        private Map<Expression, Node> nodes;
        private int nodeCnt;
        private int edgeCnt;
        private Graph graph;

        public ConcurrentJob(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        public GraphUpdateStatus call() throws Exception {
            final int totalWork = WORK_CREATE_GRAPH + WORK_LAYOUT_GRAPH;

            monitor.beginTask(getName(), totalWork);

            if (monitor.isCanceled())
                return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

            monitor.subTask("Creating graph...");

            final Graph graph;
            if (evalStatus.getSeverity() == IStatus.CANCEL) {
                graph = createWarningGraph("Graph creation cancelled.");
            } else if (evalStatus.getWarning() != null) {
                graph = createWarningGraph(evalStatus.getWarning());
            } else if (evalStatus.isOK()) {
                graph = createGraph(evalStatus.getCcsProgram().getExpression());
            } else {
                graph = createWarningGraph("Unknown error.");
            }

            monitor.worked(WORK_CREATE_GRAPH);

            if (monitor.isCanceled())
                return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

            monitor.subTask("Layouting graph...");
            final FutureTask<Boolean> filterTask = new FutureTask<Boolean>(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return GraphHelper.filterGraph(graph, true);
                }
            });
            new Thread(filterTask, "layout graph").start();
            final long startTime = System.currentTimeMillis();
            while (true) {
                try {
                    if (!filterTask.get(500, TimeUnit.MILLISECONDS)) {
                        return new GraphUpdateStatus(IStatus.ERROR,
                            "The graph could not be layout, most probably there was an error starting the dot tool.\n" +
                            "You can configure the path for this tool in your preferences on the \"CCS\" page.");
                    }
                    break;
                } catch (final TimeoutException e) {
                    final long endTime = System.currentTimeMillis();
                    int sec = (int) ((endTime-startTime)/1000);
                    final int min = sec / 60;
                    sec %= 60;
                    monitor.subTask("Layouting graph... (" + (min > 0 ? min+" min, " : "")
                        + sec + " sec)");
                } catch (final InterruptedException e) {
                    filterTask.cancel(true);
                    throw e;
                }
            }

            monitor.worked(WORK_LAYOUT_GRAPH);

            if (monitor.isCanceled())
                return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

            monitor.done();

            final GraphUpdateStatus status = new GraphUpdateStatus(IStatus.OK, "", graph);

            return status;
        }

        private Graph createWarningGraph(String warning) {
            final Graph graph;
            graph = new Graph("WARNING");
            graph.setToolTipText("");

            final Node node = new Node(graph, "warn_node");
            node.setAttribute(GrappaConstants.LABEL_ATTR, warning);
            node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
            node.setAttribute(GrappaConstants.FILLCOLOR_ATTR, GraphHelper.WARN_NODE_COLOR);
            node.setAttribute(GrappaConstants.TIP_ATTR,
                "The graph could not be built. This is the reason why.");
            node.setAttribute(GrappaConstants.SHAPE_ATTR, "plaintext");
            graph.addNode(node);
            return graph;
        }

        private Graph createGraph(Expression mainExpression) throws InterruptedException {
            graph = new Graph("CCS Graph");
            graph.setAttribute("root", "node_0");
            // set layout direction to "left to right"
            if (layoutLeftToRight)
                graph.setAttribute(GrappaConstants.RANKDIR_ATTR, "LR");
            graph.setToolTipText("");

            final Queue<Expression> queue = new UniqueQueue<Expression>();
            queue.add(mainExpression);

            nodes = new HashMap<Expression, Node>();
            nodeCnt = 0;
            edgeCnt = 0;

            while (!queue.isEmpty()) {
                if (Thread.interrupted())
                    throw new InterruptedException();
                final Expression e = queue.poll();
                final Node tailNode = getNode(nodes, e);

                for (final Transition trans: e.getTransitions()) {
                    final Node headNode = getNode(nodes, trans.getTarget());
                    final Edge edge = new Edge(graph, tailNode, headNode, "edge_" + edgeCnt++);
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
                    queue.add(trans.getTarget());
                }
            }
            nodes = null;

            return graph;
        }

        private Node getNode(final Map<Expression, Node> nodes,
                final Expression e) {
            Node node = nodes.get(e);
            if (node != null)
                return node;

            node = new Node(graph, "node_" + nodeCnt++);
            final String label = showNodeLabels ? e.toString() : "";
            node.setAttribute(GrappaConstants.LABEL_ATTR,
                label);
            node.setAttribute(GrappaConstants.TIP_ATTR, "Node: " + e.toString());
            if (nodeCnt == 1) {
                node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
                node.setAttribute(GrappaConstants.FILLCOLOR_ATTR, GraphHelper.START_NODE_COLOR);
            }
            if (e.isError()) {
                node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
                node.setAttribute(GrappaConstants.FILLCOLOR_ATTR, GraphHelper.ERROR_NODE_COLOR);
                node.setAttribute(GrappaConstants.SHAPE_ATTR, "octagon");
                node.setAttribute(GrappaConstants.TIP_ATTR, "Error node: " + e.toString());
            }
            nodes.put(e, node);
            graph.addNode(node);

            return node;
        }
    }

    public class GraphUpdateStatus extends Status {

        private Graph graph;

        public GraphUpdateStatus(int severity, String message) {
            super(severity, Global.getPluginID(), IStatus.OK, message, null);
        }

        public GraphUpdateStatus(int severity, String message, Graph graph) {
            this(severity, message);
            this.graph = graph;
        }

        public Graph getGraph() {
            return graph;
        }

        public void setGraph(Graph graph) {
            this.graph = graph;
        }

        public GraphUpdateJob getJob() {
            return GraphUpdateJob.this;
        }

        public EvaluationStatus getEvalStatus() {
            return evalStatus;
        }

    }

}

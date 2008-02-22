package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.GrappaConstants;
import att.grappa.Node;
import de.unisb.cs.depend.ccs_sem.evaluators.EvaluationMonitor;
import de.unisb.cs.depend.ccs_sem.evaluators.Evaluator;
import de.unisb.cs.depend.ccs_sem.evaluators.SequentialEvaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GraphHelper;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class GraphUpdateJob extends Job {


    protected final boolean minimize;

    protected final String ccsCode;

    protected final boolean layoutLeftToRight;

    protected final boolean showNodeLabels;
    protected final boolean showEdgeLabels;

    protected final Set<Observer> observers = new HashSet<Observer>();

    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    private final static int WORK_LEXING = 1;
    private final static int WORK_PARSING = 3;
    private final static int WORK_CHECKING = 1;
    private final static int WORK_EVALUATING = 20;
    private final static int WORK_MINIMIZING = 60;
    private final static int WORK_CREATE_GRAPH = 5;
    private final static int WORK_LAYOUT_GRAPH = 50;
    private final static int WORK_SUPPLY = 5;

    public GraphUpdateJob(String ccsCode, boolean minimize,
            boolean layoutLeftToRight, boolean showNodeLabels, boolean showEdgeLabels) {
        super("Update Graph");
        this.ccsCode = ccsCode;
        this.minimize = minimize;
        this.layoutLeftToRight = layoutLeftToRight;
        this.showNodeLabels = showNodeLabels;
        this.showEdgeLabels = showEdgeLabels;
        setUser(true);
        addJobChangeListener(new JobChangeAdapter() {
        
            @Override
            public void done(IJobChangeEvent event) {
                super.done(event);
                // workaround for eclipse <3.3, because there was no canceling method
                if (event.getResult().equals(Status.CANCEL_STATUS))
                    canceling();
            }
        
        });
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        final ConcurrentJob job = new ConcurrentJob(monitor);
        final FutureTask<GraphUpdateStatus> status = new FutureTask<GraphUpdateStatus>(job);
        new Thread(status, getClass().getSimpleName() + " worker").start();

        while (true) {
            try {
                final GraphUpdateStatus graphUpdateStatus = status.get(100, TimeUnit.MILLISECONDS);
                isCancelled.set(false);
                return graphUpdateStatus;
            } catch (final InterruptedException e) {
                // the *job* should not be interrupted. If cancel is pressed,
                // the inner thread is interrupted, but not this one!
                throw new RuntimeException(e);
            } catch (final ExecutionException e) {
                // an abnormal exception: let eclipse show it to the user
                throw new RuntimeException(e);
            } catch (final TimeoutException e) {
                if (getAndResetCancelled()) {
                    status.cancel(true);
                    return new GraphUpdateStatus(IStatus.CANCEL, "Cancelled.");
                }
            }
        }
    }

    protected boolean getAndResetCancelled() {
        return isCancelled.getAndSet(false);
    }

    // before eclipse 3.3, there was no canceling method, so we cannot "override" it
    protected void canceling() {
        isCancelled.set(true);
    }

    public class EvalMonitor implements EvaluationMonitor {

        private String error;
        private final String prefix;
        private final IProgressMonitor monitor;
        private int states = 0;
        private int transitions = 0;
        private final int outputNum;

        public EvalMonitor(IProgressMonitor monitor, String prefix, int outputNum) {
            this.monitor = monitor;
            this.prefix = prefix;
            this.outputNum = outputNum;
        }

        public String getErrorString() {
            return error;
        }

        public void error(String errorString) {
            this.error = errorString;
        }

        public synchronized void newState() {
            ++states;
            if (states % outputNum == 0) {
                monitor.subTask(prefix + states + " States, " + transitions + " Transitions");
            }
        }

        public synchronized void newTransitions(int count) {
            transitions += count;
        }

        public void ready() {
            monitor.subTask(prefix + states + " ready");
        }

    }

    public void addObserver(Observer obs) {
        synchronized (observers ) {
            observers.add(obs);
        }
    }

    public boolean isMinimize() {
        return minimize;
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

        public ConcurrentJob(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        public GraphUpdateStatus call() throws Exception {
            int totalWork = WORK_LEXING + WORK_PARSING + WORK_CHECKING
            + WORK_EVALUATING + WORK_CREATE_GRAPH + WORK_LAYOUT_GRAPH
            + WORK_SUPPLY;
            if (minimize)
                totalWork += WORK_MINIMIZING;

            monitor.beginTask(getName(), totalWork);

            if (getAndResetCancelled())
                return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

            // parse ccs term
            Program ccsProgram = null;
            String warning = null;
            try {
                monitor.subTask("Lexing...");
                final List<Token> tokens = new CCSLexer().lex(ccsCode);
                monitor.worked(WORK_LEXING);

                if (getAndResetCancelled())
                    return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

                monitor.subTask("Parsing...");
                ccsProgram = new CCSParser().parse(tokens);
                monitor.worked(WORK_PARSING);

                if (getAndResetCancelled())
                    return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

                monitor.subTask("Checking expression...");
                if (!ccsProgram.isGuarded())
                    throw new ParseException("Your recursive definitions are not guarded.");
                if (!ccsProgram.isRegular())
                    throw new ParseException("Your recursive definitions are not regular.");
                monitor.worked(WORK_CHECKING);

                if (getAndResetCancelled())
                    return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

                monitor.subTask("Evaluating...");
                final Evaluator evaluator = new SequentialEvaluator();
                final EvalMonitor evalMonitor = new EvalMonitor(monitor, "Evaluating... ", 100);
                if (!ccsProgram.evaluate(evaluator, evalMonitor)) {
                    final String error = evalMonitor.getErrorString();
                    return new GraphUpdateStatus(IStatus.ERROR,
                        "Error evaluating: " + error);
                }
                monitor.worked(WORK_EVALUATING);

                if (getAndResetCancelled())
                    return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

                if (minimize) {
                    monitor.subTask("Minimizing...");
                    final EvalMonitor minimizationMonitor = new EvalMonitor(monitor, "Minimizing... ", 100);
                    if (!ccsProgram.minimizeTransitions(evaluator, minimizationMonitor, false)) {
                        final String error = evalMonitor.getErrorString();
                        return new GraphUpdateStatus(IStatus.ERROR,
                            "Error minimizing: " + error);
                    }
                    monitor.worked(WORK_MINIMIZING);
                }
            } catch (final LexException e) {
                warning = "Error lexing: " + e.getMessage() + "\\n"
                    + "(around this context: " + e.getEnvironment() + ")";
            } catch (final ParseException e) {
                warning = "Error parsing: " + e.getMessage() + "\\n"
                    + "(around this context: " + e.getEnvironment() + ")";
            }

            if (getAndResetCancelled())
                return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

            monitor.subTask("Creating graph...");

            final Graph graph = new Graph("CCS Graph");

            graph.setAttribute("root", "node_0");
            // set layout direction to "left to right"
            if (layoutLeftToRight)
                graph.setAttribute(GrappaConstants.RANKDIR_ATTR, "LR");
            graph.setToolTipText("");

            if (warning != null) {
                final Node node = new Node(graph, "warn_node");
                node.setAttribute(GrappaConstants.LABEL_ATTR, warning);
                node.setAttribute(GrappaConstants.STYLE_ATTR, "filled");
                node.setAttribute(GrappaConstants.FILLCOLOR_ATTR, GraphHelper.WARN_NODE_COLOR);
                node.setAttribute(GrappaConstants.TIP_ATTR,
                    "The graph could not be built. This is the reason why.");
                graph.addNode(node);
                GraphHelper.filterGraph(graph);
                return new GraphUpdateStatus(IStatus.ERROR, warning);
            }


            final Queue<Expression> queue = new LinkedList<Expression>();
            queue.add(ccsProgram.getExpression());

            final Set<Expression> written = new HashSet<Expression>();
            written.add(ccsProgram.getExpression());

            final Map<Expression, Node> nodes = new HashMap<Expression, Node>();

            // first, create all nodes
            int cnt = 0;
            while (!queue.isEmpty()) {
                final Expression e = queue.poll();
                final Node node = new Node(graph, "node_" + cnt++);
                node.setAttribute(GrappaConstants.LABEL_ATTR,
                    showNodeLabels ? e.toString() : "");
                node.setAttribute(GrappaConstants.TIP_ATTR, "Node: " + e.toString());
                if (cnt == 1) {
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
                for (final Transition trans: e.getTransitions())
                    if (written.add(trans.getTarget()))
                        queue.add(trans.getTarget());
            }

            // then, create the edges
            queue.add(ccsProgram.getExpression());
            written.clear();
            written.add(ccsProgram.getExpression());
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

            monitor.worked(WORK_CREATE_GRAPH);

            if (getAndResetCancelled())
                return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

            monitor.subTask("Layouting graph...");
            if (!GraphHelper.filterGraph(graph))
                return new GraphUpdateStatus(IStatus.ERROR, "Error layouting graph.");
            monitor.worked(WORK_LAYOUT_GRAPH);

            if (getAndResetCancelled())
                return new GraphUpdateStatus(IStatus.CANCEL, "cancelled");

            monitor.subTask("Supplying changes...");
            final GraphUpdateStatus status = new GraphUpdateStatus(IStatus.OK, "", graph);

            synchronized (observers) {
                for (final Observer obs: observers)
                    obs.update(null, status);
            }

            monitor.worked(WORK_SUPPLY);

            monitor.done();

            return status;
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

    }

}

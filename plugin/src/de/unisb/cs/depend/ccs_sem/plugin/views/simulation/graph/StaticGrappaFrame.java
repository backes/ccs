package de.unisb.cs.depend.ccs_sem.plugin.views.simulation.graph;

import java.awt.Color;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import att.grappa.Edge;
import att.grappa.GrappaConstants;
import att.grappa.Node;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.GraphUpdateJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.StaticGraphUpdateJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.GraphUpdateJob.GraphUpdateStatus;
import de.unisb.cs.depend.ccs_sem.plugin.views.simulation.IUndoListener;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.StopExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;

public class StaticGrappaFrame extends GrappaFrame implements IUndoListener {

	private Expression mainExp;
	
	private Node currentNode;
	
	private LinkedList<Edge> markedEdges;
	private LinkedList<Node> nodeHistory;
	private HashMap<String,Expression> nodeMap;	
	
	public StaticGrappaFrame(Composite parent, int style, Expression mainExp) {
		super(parent, style, null);
		
		Program prog = null;
		try {
			prog = new Program(new LinkedList<ProcessVariable>(), StopExpression.get() );
		} catch (ParseException e) { e.printStackTrace(); }
		
		FakeEvaluationJob job = new FakeEvaluationJob();
		EvaluationStatus status =job.new EvaluationStatus(IStatus.OK,"",null, prog );
		job.setStatus(status);
		super.lastEvalStatus = status;
		
		this.mainExp = mainExp;
		
		super.scrollComposite.setMinHeight(300);
		super.scrollComposite.setMinWidth(300);
		this.setSize(300, 300);
		
		currentNode = null;
		markedEdges = new LinkedList<Edge>();
		nodeHistory = new LinkedList<Node>();
	}
	
	@Override
	public GraphUpdateJob createGraphUpdateJob(EvaluationStatus status) {
		final StaticGraphUpdateJob job = new StaticGraphUpdateJob(
				status,super.layoutLeftToRight);
		job.setMainExpression(mainExp);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if( event.getResult() instanceof GraphUpdateStatus ) {
					graph = ((GraphUpdateStatus) event.getResult()).getGraph();
				}
				nodeMap = job.getNodeMap();
				
				initCurrentNode();
				updateMarker();
			}
		});
		return job;
	}

	@Override
	public void updateGraph() {
		update(null);
	}
	
	@Override
	public void markTrace() {} // do nothing
	
	@Override
	public void selectTrace(String[] trace) {} // do nothing
	
	@Override
	public Point computeSize(int whint, int hhint) {
		return getSize();
	}
	
	@Override
	public Point computeSize(int whint, int hhint, boolean changed) {
		return computeSize(whint, hhint);
	}
	
	private void initCurrentNode() {
		Enumeration<Node> nodes = graph.nodeElements();
		
		while(nodes.hasMoreElements()) {
			Node n = nodes.nextElement();
			if( n.getName().equals("node_0") ) {
				currentNode = n;
				break;
			}
		}
		nodeHistory.add(currentNode);
	}
	
	private void updateMarker() {
		try {
			super.graphLock.lock();
			if( nodeHistory.size()>1 ) {
				nodeHistory.get(nodeHistory.size()-2).setAttribute(
						GrappaConstants.COLOR_ATTR, Color.BLACK);
			}
			currentNode.setAttribute(GrappaConstants.COLOR_ATTR, Color.RED);
			
			// mark possible transitions
			for( Edge e : markedEdges ) {
				e.setAttribute(GrappaConstants.COLOR_ATTR, Color.BLACK);
			}
			markedEdges.clear();
			
			Enumeration<Edge> edges = currentNode.outEdgeElements();
			
			while( edges.hasMoreElements() ) {
				Edge e = edges.nextElement();
				markedEdges.add(e);
				e.setAttribute(GrappaConstants.COLOR_ATTR, Color.RED);			
			}
		} finally {
			graph.repaint();
			super.graphLock.unlock();
		}
	}
	
	public void takeTransition(Transition trans) {		
		Enumeration<Edge> edges = currentNode.outEdgeElements();
		while(edges.hasMoreElements()) {
			Edge e = edges.nextElement();
			if( trans.getTarget().equals(
				nodeMap.get(e.getHead().toString())
			) ) {
				currentNode = e.getHead();
				break;
			}
		}
		
		nodeHistory.addLast(currentNode);
		updateMarker();
	}

	public void notifyUndo() {
		nodeHistory.getLast().setAttribute(GrappaConstants.COLOR_ATTR, Color.BLACK);
		nodeHistory.removeLast();
		currentNode = nodeHistory.getLast();
		updateMarker();
	}
}

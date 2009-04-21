package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import java.util.HashMap;

import att.grappa.Edge;
import att.grappa.Node;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;

public class StaticGraphUpdateJob extends GraphUpdateJob {

	private Expression mainExpr;
	private int nodeNr = 0;
	
	private HashMap<Node,Expression> nodeMap;
	
	public StaticGraphUpdateJob(EvaluationStatus evalStatus,
			boolean layoutLeftToRight) {
		super(evalStatus, layoutLeftToRight, true, true);
		nodeMap = new HashMap<Node, Expression>();
	}

	@Override
	protected Expression getMainExpression() {
		return mainExpr;
	}
	
	public void setMainExpression(Expression mainExpr) {
		this.mainExpr = mainExpr;
	}
	
	@Override
	protected void initNode(Node tailNode, Node headNode, Transition trans, Expression currentExp, Edge edge) {
		super.initNode(tailNode, headNode, trans, currentExp, edge);
		
		// create some meta-data
		nodeMap.put(headNode, currentExp);
		System.out.println(headNode.getName());
	}
	
	@Override
	protected String getNodeLabel(Expression e) {
		return "" + nodeNr++;
	}
	
	public HashMap<Node,Expression> getNodeMap() {
		return nodeMap;
	}
	
	public int getNodeNr() {
		return nodeNr;
	}
}

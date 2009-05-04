package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import java.util.HashMap;

import att.grappa.Node;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;

public class StaticGraphUpdateJob extends GraphUpdateJob {

	private Expression mainExpr;
	private int nodeNr = 0;
	
	private HashMap<String,Expression> nodeMap;
	
	public StaticGraphUpdateJob(EvaluationStatus evalStatus,
			boolean layoutLeftToRight) {
		super(evalStatus, layoutLeftToRight, true, true);
		nodeMap = new HashMap<String, Expression>();
	}

	@Override
	protected Expression getMainExpression() {
		return mainExpr;
	}
	
	public void setMainExpression(Expression mainExpr) {
		this.mainExpr = mainExpr;
	}
		
	@Override
	protected void initNode(Node node, Expression currentExp) {
		super.initNode(node,currentExp);
		nodeMap.put(node.getName(),currentExp);
	}
	
	@Override
	protected String getNodeLabel(Expression e) {
		return "" + nodeNr++;
	}
	
	public HashMap<String,Expression> getNodeMap() {
		return nodeMap;
	}
	
	public int getNodeNr() {
		return nodeNr;
	}
}

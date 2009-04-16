package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;

public class StaticGraphUpdateJob extends GraphUpdateJob {

	private Expression mainExpr;
	
	public StaticGraphUpdateJob(EvaluationStatus evalStatus,
			boolean layoutLeftToRight, boolean showNodeLabels,
			boolean showEdgeLabels) {
		super(evalStatus, layoutLeftToRight, showNodeLabels, showEdgeLabels);
	}

	@Override
	protected Expression getMainExpression() {
		return mainExpr;
	}
	
	public void setMainExpression(Expression mainExpr) {
		this.mainExpr = mainExpr;
	}
}

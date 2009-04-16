package de.unisb.cs.depend.ccs_sem.plugin.views.simulation.graph;

import java.util.LinkedList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;

import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.GraphUpdateJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.StaticGraphUpdateJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.StopExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;

public class StaticGrappaFrame extends GrappaFrame {

	private Expression mainExp;
	
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
	}
	
	@Override
	public GraphUpdateJob createGraphUpdateJob(EvaluationStatus status) {
		StaticGraphUpdateJob job = new StaticGraphUpdateJob(status,super.layoutLeftToRight, super.showNodeLabels, super.showEdgeLabels);
		job.setMainExpression(mainExp);
		return job;
	}
	
	@Override
	public void pack() {
		return;
	}
	
	@Override
	public void pack(boolean ch) {
		return;
	}
	
	@Override
	public void updateGraph() {
		update(null);
	}
	
	@Override
	public void markTrace() {} // do nothing
	
	@Override
	public void selectTrace(String[] trace) {} // do nothing

}

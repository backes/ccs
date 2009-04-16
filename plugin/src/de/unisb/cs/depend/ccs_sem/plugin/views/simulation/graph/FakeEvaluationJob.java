package de.unisb.cs.depend.ccs_sem.plugin.views.simulation.graph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob;

public class FakeEvaluationJob extends EvaluationJob {

	private EvaluationStatus status;
	
	public FakeEvaluationJob() {
		super("", false);
	}
	
	public void setStatus( EvaluationStatus status ) {
		this.status = status;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		return status;
	}
}

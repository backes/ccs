package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import ltlcheck.Counterexample;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob;
import de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView;
import de.unisb.cs.depend.ccs_sem.plugin.Global;

public class CounterExampleFrame extends SashForm {

	private List ceList;
	private CCSGraphView currentGraphView;
	
	public CounterExampleFrame(Composite parent, Counterexample ce,
			IWorkbenchWindow window) {
		super(parent, SWT.VERTICAL);
		if( ce == null || window == null ) // Should'nt happen 
			throw new IllegalArgumentException("Counterexample oder Window is null!");
		
		for( IViewReference vRef : window.getActivePage().getViewReferences() ) {
			IViewPart v = vRef.getView(false);
			if (v instanceof CCSGraphView) {
				currentGraphView = (CCSGraphView) v;
			}
		}
		if( currentGraphView == null ) {
			try {
				currentGraphView = 
					(CCSGraphView) window.getActivePage().showView( Global.getGraphViewId() );
			} catch (PartInitException e) {}
		}
		ceList = new List(this,SWT.BORDER | SWT.V_SCROLL);
		
		init(ce);
	}
	
	public void removeCounterExample() {
		currentGraphView.getGrappaFrame().selectTrace(null);
	}
	
	/**
	 * initialises the CounterExample Frame
	 * @param ce
	 */
	private void init(final Counterexample ce) {
		ceList.add("- Prefix:");
		for( gov.nasa.ltl.graph.Edge edge : ce.getPrefix() ) {
			ceList.add( edge.getGuard() );
		}
		
		ceList.add("- Cycle:");
		for( gov.nasa.ltl.graph.Edge edge : ce.getCycle() ) {
			ceList.add( edge.getGuard() );
		}
		
		// init grappa frame concurrently
		currentGraphView.getGrappaFrame().selectTrace(ce.getTrace());
		final EvaluationJob evaluationJob = currentGraphView.getUpdateJob();

		evaluationJob.schedule();
		getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					currentGraphView.getSite().getPage().showView(Global.getGraphViewId());
				} catch (PartInitException e) {e.printStackTrace();}		
			}
		});	
	}
}
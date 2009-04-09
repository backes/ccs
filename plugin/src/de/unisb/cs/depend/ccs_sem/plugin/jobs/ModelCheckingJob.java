package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import gov.nasa.ltl.trans.ParseErrorException;

import ltlcheck.Counterexample;
import ltlcheck.IModelCheckingMonitor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.utils.Globals;
import de.unisb.cs.depend.ltlchecker.ExpressionLTLChecker;
import de.unisb.cs.depend.ltlchecker.LTLSyntaxChecker;

public class ModelCheckingJob extends Job {

	private final int WORK_GETEXPRESSION = 100,
		WORK_CHECK_INPUT = 100,
		WORK_MODELCHECKING = 1000;
	
	private String formula;
	private CCSDocument doc;
	private ModelCheckingStatus status;
	
	public ModelCheckingJob(String formula, CCSDocument doc, int index) {
		super("ModelChecking Job");
		this.formula = formula;
		this.doc = doc;
		status = new ModelCheckingStatus(0,"Everything ok",index,
				doc.getModificationStamp());
		setUser(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		final ConcurrentJob job = new ConcurrentJob(monitor);
        final FutureTask<ModelCheckingStatus> status = new FutureTask<ModelCheckingStatus>(job);
        final Thread executingThread = new Thread(status, getClass().getSimpleName() + " worker");
        executingThread.start();

        while (true) {
            try {
                final ModelCheckingStatus modelCheckStatus = status.get(100, TimeUnit.MILLISECONDS);
                return modelCheckStatus;
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
                    return new ModelCheckingStatus(IStatus.CANCEL, "Cancelled.");
                }
            }
        }
	}
	
	private class ConcurrentJob implements Callable<ModelCheckingStatus> {
		private IProgressMonitor monitor;
		
		public ConcurrentJob(IProgressMonitor monitor) {
			this.monitor = monitor;
		}
		
		public ModelCheckingStatus call() throws Exception {
			final int totalWork = WORK_GETEXPRESSION + WORK_CHECK_INPUT +
			WORK_MODELCHECKING;
		
		monitor.beginTask(getName(), totalWork);
		
		monitor.subTask("Checking input...");
		if( formula == null || formula.equals("") ) 
			return new ModelCheckingStatus(IStatus.ERROR,"There is no formula to check!");
		if( !LTLSyntaxChecker.correctSyntax(formula ) ) {
			return new ModelCheckingStatus(IStatus.ERROR, "Invalid LTL formula!");
		}
		if( doc == null ) {
			return new ModelCheckingStatus(IStatus.ERROR, "There is no CCSDocument to check!");
		}
		monitor.worked(WORK_CHECK_INPUT);
		
		if (monitor.isCanceled())
            return new ModelCheckingStatus(IStatus.CANCEL, "cancelled");
		
		// Get expression
		monitor.subTask("Get expression...");
		Expression exp = null;
		final ParseStatus result = doc.reparseIfNecessary();
		Program prog = result.getParsedProgram();
		if( !prog.isGuarded() )
			return new ModelCheckingStatus(IStatus.ERROR, "The CCS Expression is unguarded!");
		
		try {
			prog.evaluate(Globals.getDefaultEvaluator());
			monitor.subTask("Minimizing CCS Graph...");
			prog.minimizeTransitions(Globals.getDefaultEvaluator(),null,true);
		} catch (InterruptedException e) { // Should'nt happen
			e.printStackTrace();
		}
		exp = prog.getMinimizedExpression();
		if( exp == null )
			return new ModelCheckingStatus(IStatus.ERROR, "Expresion is null!");
		monitor.worked(WORK_GETEXPRESSION);
		
		if (monitor.isCanceled())
            return new ModelCheckingStatus(IStatus.CANCEL, "cancelled");
		
		monitor.subTask("Run model-checking...");
		ModelCheckMonitor modelMonitor = new ModelCheckMonitor(monitor, 8, WORK_MODELCHECKING);
		try {
			status.setCounterexample(
					ExpressionLTLChecker.check(exp, formula, modelMonitor)
			);
		} catch (ParseErrorException e) { // Should'nt happen
			e.printStackTrace();
		}
		monitor.worked(WORK_MODELCHECKING-modelMonitor.totalWorkDone);
		
		if (monitor.isCanceled())
            return new ModelCheckingStatus(IStatus.CANCEL, "cancelled");
		
		monitor.done();
		
		return status;
		}
	}

	public class ModelCheckingStatus extends Status {

		private Counterexample ce = null;
		private int index;
		private long starttime;
		
		public ModelCheckingStatus(int servity, String message) {
			super(servity,Global.getPluginID(),IStatus.OK,message,null);
		}
		
		public ModelCheckingStatus(int severity, String message, int index, long starttime) {
			super(severity, Global.getPluginID(), IStatus.OK, message, null);
			this.index = index;
			this.starttime = starttime;
		}
		
		public void setCounterexample(Counterexample ce) {
			this.ce = ce;
		}
		
		public Counterexample getCounterexample() {
			return ce;
		}
		
		public int getIndex() {
			return index;
		}
		
		public long getStarttime() {
			return starttime;
		}
	}
	
	public class ModelCheckMonitor implements IModelCheckingMonitor {

		private IProgressMonitor monitor;
		private int workPerSubTask;
		private int totalWorkDone;
		
		public ModelCheckMonitor(IProgressMonitor monitor, int anzahl, int totalWork) {
			this.monitor = monitor;
			totalWorkDone = 0;
			workPerSubTask = (int) Math.floor(totalWork / anzahl);
		}
		
		public void subTask(String str) {
			monitor.subTask(str);
			monitor.worked(workPerSubTask);
			totalWorkDone += workPerSubTask;
		}		
	}
}

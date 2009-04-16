package de.unisb.cs.depend.ccs_sem.plugin.jobs;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import de.unisb.cs.depend.ccs_sem.evaluators.EvaluationMonitor;
import de.unisb.cs.depend.ccs_sem.evaluators.Evaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.parser.IParsingProblemListener;
import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.MyPreferenceStore;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.utils.Globals;

public class EvaluationJob extends Job {

    protected final boolean minimize;
    protected boolean reset;

    protected final String ccsCode;

    private static final ISchedulingRule rule = new IdentityRule();

    private final static int WORK_LEXING = 1;
    private final static int WORK_PARSING = 3;
    private final static int WORK_CHECKING = 1;
    private final static int WORK_EVALUATING = 20;
    private final static int WORK_MINIMIZING = 60;

    public EvaluationJob(String ccsCode, boolean minimize) {
        super("Evaluate CCS");
        this.ccsCode = ccsCode;
        this.minimize = minimize;
        setUser(true);
        setPriority(INTERACTIVE);
        setRule(rule);
        reset = false;
    }
    
    public void setResetEval(boolean r) {
    	reset = true;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        final ConcurrentJob job = new ConcurrentJob(monitor);
        final FutureTask<EvaluationStatus> status = new FutureTask<EvaluationStatus>(job);
        final Thread executingThread = new Thread(status, getClass().getSimpleName() + " worker");
        executingThread.start();

        while (true) {
            try {
                final EvaluationStatus graphUpdateStatus = status.get(100, TimeUnit.MILLISECONDS);
                return graphUpdateStatus;
            } catch (final InterruptedException e) {
                // the *job* should not be interrupted. If cancel is pressed,
                // the inner thread is interrupted, but not this one!
                throw new RuntimeException(e);
            } catch (final ExecutionException e) {
                // an abnormal exception: let eclipse show it to the user
            	e.printStackTrace();
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
                    return new EvaluationStatus(IStatus.CANCEL, "Cancelled.");
                }
            }
        }
    }

    public boolean isMinimize() {
        return minimize;
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
            monitor.subTask(prefix + " ready");
        }

        public synchronized void newState(int numTransitions) {
            newTransitions(numTransitions);
            newState();
        }

    }

    private class ConcurrentJob implements Callable<EvaluationStatus> {

        private final IProgressMonitor monitor;

        public ConcurrentJob(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        public EvaluationStatus call() throws Exception {
            int totalWork = WORK_LEXING + WORK_PARSING + WORK_CHECKING
                + WORK_EVALUATING;
            if (minimize)
                totalWork += WORK_MINIMIZING;

            monitor.beginTask(getName(), totalWork);

            if (monitor.isCanceled())
                return new EvaluationStatus(IStatus.CANCEL, "cancelled");

            // parse ccs term
            Program ccsProgram = null;
            String warning = null;
            try {
                monitor.subTask("Lexing...");
                final List<Token> tokens = new CCSLexer().lex(ccsCode);
                monitor.worked(WORK_LEXING);

                if (monitor.isCanceled())
                    return new EvaluationStatus(IStatus.CANCEL, "cancelled");

                if (tokens == null) {
                    monitor.done();
                    return new EvaluationStatus(IStatus.OK, "", "There are errors in your code", null);
                }

                monitor.subTask("Parsing...");
                final CCSParser parser = new CCSParser();
                // we just need a "boolean holder"
                final AtomicBoolean errorsOccured = new AtomicBoolean(false);
                parser.addProblemListener(new IParsingProblemListener() {
                    public void reportParsingProblem(ParsingProblem problem) {
                        if (problem.getType() == ParsingProblem.ERROR)
                            errorsOccured.set(true);
                    }
                });
                ccsProgram = parser.parse(tokens);
                monitor.worked(WORK_PARSING);

                if (monitor.isCanceled())
                    return new EvaluationStatus(IStatus.CANCEL, "cancelled");

                if (errorsOccured.get() || ccsProgram == null) {
                    monitor.done();
                    return new EvaluationStatus(IStatus.INFO, "", "There are errors in your code", null);
                }

                monitor.subTask("Checking expression...");
                if (!ccsProgram.isGuarded()
                        && MyPreferenceStore.getUnguardedErrorType() == ParsingProblem.ERROR)
                    throw new ParseException("Your recursive definitions are not guarded.", -1, -1);
                if (!ccsProgram.isRegular()
                        && MyPreferenceStore.getUnregularErrorType() == ParsingProblem.ERROR)
                    throw new ParseException("Your recursive definitions are not regular.", -1, -1);
                monitor.worked(WORK_CHECKING);

                if (monitor.isCanceled())
                    return new EvaluationStatus(IStatus.CANCEL, "cancelled");

                monitor.subTask("Evaluating...");
                final Evaluator evaluator = Globals.getDefaultEvaluator();
                final EvalMonitor evalMonitor = new EvalMonitor(monitor, "Evaluating... ", 100);
                if( reset ) {
                	ccsProgram.resetEvaluation();
                }
                if (!ccsProgram.evaluate(evaluator, evalMonitor)) {
                    final String error = evalMonitor.getErrorString();
                    return new EvaluationStatus(IStatus.ERROR,
                        "Error evaluating: " + error);
                }
                monitor.worked(WORK_EVALUATING);

                if (monitor.isCanceled())
                    return new EvaluationStatus(IStatus.CANCEL, "cancelled");

                if (minimize) {
                    monitor.subTask("Minimizing...");
                    final EvalMonitor minimizationMonitor = new EvalMonitor(monitor, "Minimizing... ", 100);
                    if (!ccsProgram.minimizeTransitions(evaluator, minimizationMonitor, false)) {
                        final String error = evalMonitor.getErrorString();
                        return new EvaluationStatus(IStatus.ERROR,
                            "Error minimizing: " + error);
                    }
                    monitor.worked(WORK_MINIMIZING);
                }
            } catch (final LexException e) {
                warning = "Error lexing: " + e.getMessage();
            } catch (final ParseException e) {
                warning = "Error parsing: " + e.getMessage();
            }

            if (monitor.isCanceled())
                return new EvaluationStatus(IStatus.CANCEL, "cancelled");

            monitor.done();

            final EvaluationStatus status = new EvaluationStatus(
                warning == null ? IStatus.OK : IStatus.INFO, "", warning, ccsProgram);

            return status;
        }
    }

    public class EvaluationStatus extends Status {

        private String warning;
        private Program ccsProgram;

        public EvaluationStatus(int severity, String message) {
            super(severity, Global.getPluginID(), IStatus.OK, message, null);
        }

        public EvaluationStatus(int severity, String message, String warning, Program ccsProgram) {
            this(severity, message);
            this.warning = warning;
            this.ccsProgram = ccsProgram;
        }

        public String getWarning() {
            return warning;
        }

        public Program getCcsProgram() {
            return ccsProgram;
        }

        public EvaluationJob getJob() {
            return EvaluationJob.this;
        }

    }

}

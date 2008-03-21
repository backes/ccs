package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.unisb.cs.depend.ccs_sem.plugin.jobs.IdentityRule;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class InformationTab extends CTabItem implements SelectionListener {


    protected final ISchedulingRule updateRule = new IdentityRule();

    public class UpdateJob extends Job {

        private final EvaluationStatus evalStatus;

        public UpdateJob(EvaluationStatus evalStatus) {
            super("Update Graph Informations");
            this.evalStatus = evalStatus;
            setPriority(Job.SHORT);
            setRule(updateRule);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            final String newStatesText;
            final String newTransitionsText;
            final String newFanOutText;
            final String newFanInText;
            if (evalStatus.getSeverity() != IStatus.OK) {
                newStatesText = newTransitionsText = newFanOutText = newFanInText = "--";
            } else {
                final Expression expression = evalStatus.getCcsProgram().getExpression();
                final Queue<Expression> queue = new LinkedList<Expression>();
                queue.add(expression);

                final Map<Expression, Integer> numbers = new HashMap<Expression, Integer>();
                numbers.put(expression, 0);

                int[] fanIn = new int[16];

                int stateCount = 1;
                int transCount = 0;
                int minFanOut = Integer.MAX_VALUE;
                int maxFanOut = 0;
                int minFanIn = Integer.MAX_VALUE;
                int maxFanIn = 0;

                Expression expr;
                while ((expr = queue.poll()) != null) {
                    assert expr.isEvaluated();

                    final List<Transition> transitions = expr.getTransitions();
                    final int fanOut = transitions.size();
                    transCount += fanOut;
                    if (fanOut > maxFanOut)
                        maxFanOut = fanOut;
                    if (fanOut < minFanOut)
                        minFanOut = fanOut;

                    for (final Transition trans: transitions) {
                        final Expression succ = trans.getTarget();
                        Integer stateNo = numbers.get(succ);
                        if (stateNo == null) {
                            numbers.put(succ, stateNo = numbers.size());
                            queue.add(succ);
                            ++stateCount;
                        }
                        if (fanIn.length <= stateNo) {
                            final int[] oldFanIn = fanIn;
                            fanIn = new int[fanIn.length*2];
                            System.arraycopy(oldFanIn, 0, fanIn, 0, oldFanIn.length);
                        }
                        ++fanIn[stateNo];
                    }
                }

                for (int i = 0; i < numbers.size(); ++i) {
                    if (fanIn[i] > maxFanIn)
                        maxFanIn = fanIn[i];
                    if (fanIn[i] < minFanIn)
                        minFanIn = fanIn[i];
                }

                minFanIn = Math.min(minFanIn, maxFanIn);
                minFanOut = Math.min(minFanOut, maxFanOut);

                newStatesText = Integer.toString(stateCount);
                newTransitionsText = Integer.toString(transCount);
                newFanOutText = minFanOut + " / " + maxFanOut;
                newFanInText = minFanIn + " / " + maxFanIn;
            }
            getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (isDisposed())
                        return;
                    noStates.setText(newStatesText);
                    noTransitions.setText(newTransitionsText);
                    fanOut.setText(newFanOutText);
                    fanIn.setText(newFanInText);
                    setScrollMinSize();
                    upToDate = true;
                }
            });
            return Status.OK_STATUS;
        }

    }

    protected final Label noStates;
    protected final Label noTransitions;
    protected final Label fanOut;
    protected final Label fanIn;
    protected volatile UpdateJob updateJob;
    protected volatile boolean upToDate = false;
    private final ScrolledComposite informationScrollComp;
    private final Composite informationComp;

    public InformationTab(CTabFolder parent, int style) {
        super(parent, style);

        parent.addSelectionListener(this);

        setText("Information");
        informationScrollComp = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        informationScrollComp.setExpandHorizontal(true);
        informationScrollComp.setExpandVertical(true);
        informationComp = new Composite(informationScrollComp, SWT.NONE);
        informationScrollComp.setContent(informationComp);
        setControl(informationScrollComp);

        final GridLayout informationCompGridLayout = new GridLayout(2, false);
        informationCompGridLayout.horizontalSpacing = 10;
        informationComp.setLayout(informationCompGridLayout);

        final Label noStatesLabel = new Label(informationComp, SWT.NONE);
        noStatesLabel.setText("No States:");
        noStates = new Label(informationComp, SWT.NONE);
        noStates.setText("--");

        final Label noTransitionsLabel = new Label(informationComp, SWT.NONE);
        noTransitionsLabel.setText("No Transitions:");
        noTransitions = new Label(informationComp, SWT.NONE);
        noTransitions.setText("--");

        final Label fanOutLabel = new Label(informationComp, SWT.NONE);
        fanOutLabel.setText("Fan-out (min/max):");
        fanOut = new Label(informationComp, SWT.NONE);
        fanOut.setText("--");

        final Label fanInLabel = new Label(informationComp, SWT.NONE);
        fanInLabel.setText("Fan-in (min/max):");
        fanIn = new Label(informationComp, SWT.NONE);
        fanIn.setText("--");

        setScrollMinSize();
    }

    protected void setScrollMinSize() {
        informationComp.layout();
        informationScrollComp.setMinSize(informationComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    public synchronized void update(EvaluationStatus evalStatus) {
        if (updateJob != null)
            updateJob.cancel();
        updateJob = new UpdateJob(evalStatus);
        upToDate = false;
        getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (isDisposed())
                    return;
                noStates.setText("... updating ...");
                noTransitions.setText("... updating ...");
                if (getParent().getSelection() == InformationTab.this)
                    updateJob.schedule();
            }
        });
    }

    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }

    public synchronized void widgetSelected(SelectionEvent e) {
        if (e.widget == getParent() && getParent().getSelection() == this
                && !upToDate && updateJob != null) {
            if (updateJob.getState() == Job.NONE)
                updateJob.schedule();
        }
    }

}

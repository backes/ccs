package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.List;

import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.IdentityRule;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.utils.StateNumerator;


public class TransitionListTab extends CTabItem implements SelectionListener {


    protected final ISchedulingRule updateRule = new IdentityRule();

    public class UpdateJob extends Job {

        private final EvaluationStatus evalStatus;

        public UpdateJob(EvaluationStatus evalStatus) {
            super("Update Transition List");
            this.evalStatus = evalStatus;
            setPriority(Job.SHORT);
            setRule(updateRule);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            final boolean error = evalStatus.getSeverity() != IStatus.OK;
            String[] items = null;
            if (!error) {
                final Expression expression = evalStatus.getCcsProgram().getExpression();
                final Set<String> actionLabels = new HashSet<String>();
                for (final Expression expr: StateNumerator.numerateStates(expression).keySet())
                    for (final Transition trans: expr.getTransitions())
                        actionLabels.add(trans.getAction().getLabel());

                items = actionLabels.toArray(new String[actionLabels.size()]);
                Arrays.sort(items);
            }
            final String[] finalItems = items;
            getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (isDisposed())
                        return;
                    if (error) {
                        transitionList.setItems(new String[] { "-- error --" });
                    } else {
                        transitionList.setItems(finalItems);
                    }
                    transitionList.setEnabled(!error);
                    transitionList.setCursor(null);
                    upToDate = true;
                }
            });
            return Status.OK_STATUS;
        }

    }

    protected volatile UpdateJob updateJob;
    protected volatile boolean upToDate = false;
    protected List transitionList;

    public TransitionListTab(CTabFolder parent, int style, final GrappaFrame gFrame) {
        super(parent, style);

        parent.addSelectionListener(this);

        setText("Transition List");

        transitionList = new List(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        transitionList.add(" -- ");
        transitionList.setEnabled(false);
        transitionList.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget == transitionList) {
                    final String[] selection = transitionList.getSelection();
                    gFrame.selectTransitions(selection);
                    gFrame.redraw();
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        setControl(transitionList);

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
                transitionList.setItems(new String[] { "... updating ..." });
                transitionList.setEnabled(false);
                transitionList.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
                if (getParent().getSelection() == TransitionListTab.this)
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

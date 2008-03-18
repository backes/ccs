package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import java.util.Arrays;
import java.util.Map;

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
import de.unisb.cs.depend.ccs_sem.utils.StateNumerator;


public class StateListTab extends CTabItem implements SelectionListener {


    protected final ISchedulingRule updateRule = new IdentityRule();

    public class UpdateJob extends Job {

        private final EvaluationStatus evalStatus;

        public UpdateJob(EvaluationStatus evalStatus) {
            super("Update State List");
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
                final Map<Expression, Integer> map = StateNumerator.numerateStates(expression);
                int i = 0;
                items = new String[map.size()];
                for (final Expression expr: map.keySet()) {
                    items[i++] = expr.toString();
                }
                assert i == map.size();
                Arrays.sort(items);
            }
            final String[] finalItems = items;
            getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (isDisposed())
                        return;
                    if (error) {
                        stateList.setItems(new String[] { "-- error --" });
                    } else {
                        stateList.setItems(finalItems);
                    }
                    stateList.setEnabled(!error);
                    stateList.setCursor(null);
                    upToDate = true;
                }
            });
            return Status.OK_STATUS;
        }

    }

    protected volatile UpdateJob updateJob;
    protected volatile boolean upToDate = false;
    protected List stateList;

    public StateListTab(CTabFolder parent, int style, final GrappaFrame gFrame) {
        super(parent, style);

        parent.addSelectionListener(this);

        setText("State List");

        stateList = new List(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        stateList.add(" -- ");
        stateList.setEnabled(false);
        stateList.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                if (e.widget == stateList) {
                    final String[] selection = stateList.getSelection();
                    gFrame.selectNodes(selection);
                    gFrame.redraw();
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        setControl(stateList);

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
                stateList.setItems(new String[] { "... updating ..." });
                stateList.setEnabled(false);
                stateList.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
                if (getParent().getSelection() == StateListTab.this)
                    updateJob.schedule();
            }
        });
    }

    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }

    public void widgetSelected(SelectionEvent e) {
        if (e.widget == getParent() && getParent().getSelection() == this
                && !upToDate && updateJob != null) {
            if (updateJob.getState() == Job.NONE)
                updateJob.schedule();
        }
    }

}

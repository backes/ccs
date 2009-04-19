package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

import att.grappa.Graph;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
import de.unisb.cs.depend.ccs_sem.plugin.utils.ISemanticDependend;

public class CCSFrame extends SashForm implements ISemanticDependend{

    protected final GrappaFrame gFrame;
    private EvaluationJob evaluationJob;
    private final CCSEditor ccsEditor;
    private boolean minimize = false;
    protected final OptionsTab optionsTab;
    protected final InformationTab informationTab;
    protected final StateListTab stateListTab;
    protected final TransitionListTab transitionListTab;

    public CCSFrame(Composite parent, CCSEditor editor) {
        super(parent, SWT.HORIZONTAL | SWT.SMOOTH);

        this.ccsEditor = editor;

        final CTabFolder tabs = new CTabFolder(this, SWT.BORDER);
        gFrame = new GrappaFrame(this, SWT.NONE, editor);

        setWeights(new int[] { 2, 8 });

        optionsTab = new OptionsTab(tabs, SWT.NONE, gFrame, this);
        tabs.showItem(optionsTab);
        tabs.setSelection(optionsTab);
        tabs.setSimple(false);

        informationTab = new InformationTab(tabs, SWT.NONE);
        stateListTab = new StateListTab(tabs, SWT.NONE, gFrame);
        transitionListTab = new TransitionListTab(tabs, SWT.NONE, gFrame);
    }

    public void showGraph(boolean updateGraph) {
        updateEvaluation(false);
        /*
        if (updateGraph)
            gFrame.updateGraph();
        */
    }

    public synchronized void updateEvaluation(boolean resetEval) {
        getUpdateJob(resetEval).schedule();
    }
    
    public synchronized EvaluationJob getUpdateJob(boolean resetEval) {
    	if (evaluationJob != null)
            evaluationJob.cancel();
        evaluationJob = new EvaluationJob(ccsEditor.getText(), minimize);
        evaluationJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                final IStatus result = event.getResult();
                if (result instanceof EvaluationStatus) {
                    final EvaluationStatus evalStatus = (EvaluationStatus) result;
                    optionsTab.update(evalStatus);
                    informationTab.update(evalStatus);
                    stateListTab.update(evalStatus);
                    transitionListTab.update(evalStatus);
                    gFrame.update(evalStatus);
                }
            }
        });
        
        evaluationJob.setResetEval(resetEval);
        return evaluationJob;
    }

    public void setMinimize(boolean minimize, boolean update) {
        if (this.minimize == minimize)
            return;

        this.minimize = minimize;
        if (update)
            updateEvaluation(false);
    }

    public boolean isMinimize() {
        return minimize;
    }

    public Graph getGraph() {
        return gFrame.getGraph();
    }

	public GrappaFrame getGrappaFrame() {
		return gFrame;
	}

	public void updateSemantic() {
		optionsTab.updateSemantic();
		updateEvaluation(true);
	}
}

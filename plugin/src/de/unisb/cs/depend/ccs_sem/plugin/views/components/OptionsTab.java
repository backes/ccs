package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;


public class OptionsTab extends CTabItem {

    protected GrappaFrame gFrame;

    public OptionsTab(CTabFolder parent, int style, GrappaFrame grappaFrame, final CCSFrame ccsFrame) {
        super(parent, style);

        this.gFrame = grappaFrame;

        setText("Options");
        final ScrolledComposite optionsScrollComp = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        optionsScrollComp.setExpandHorizontal(true);
        optionsScrollComp.setExpandVertical(true);
        final Composite optionsComp = new Composite(optionsScrollComp, SWT.NONE);
        optionsScrollComp.setContent(optionsComp);
        setControl(optionsScrollComp);

        optionsComp.setLayout(new GridLayout(1, true));

        final Group scalingGroup = new Group(optionsComp, SWT.NONE);
        scalingGroup.setText("Scaling");
        scalingGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        scalingGroup.setLayout(new GridLayout(2, true));

        final Button buttonScaleToFit = new Button(scalingGroup, SWT.CHECK);
        buttonScaleToFit.setSelection(true);
        buttonScaleToFit.setText("Scale to fit");
        final GridData buttonScaleToFitGridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        buttonScaleToFitGridData.horizontalSpan = 2;
        buttonScaleToFit.setLayoutData(buttonScaleToFitGridData);

        final Button buttonZoomIn = new Button(scalingGroup, SWT.PUSH);
        buttonZoomIn.setEnabled(false);
        buttonZoomIn.setText("Zoom in");
        buttonZoomIn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        final Button buttonZoomOut = new Button(scalingGroup, SWT.PUSH);
        buttonZoomOut.setEnabled(false);
        buttonZoomOut.setText("Zoom out");
        buttonZoomOut.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));


        final Button buttonMinimize = new Button(optionsComp, SWT.CHECK);
        buttonMinimize.setSelection(false);
        buttonMinimize.setText("Minimize LTS");
        buttonMinimize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Group layoutGroup = new Group(optionsComp, SWT.NONE);
        layoutGroup.setText("Layout");
        layoutGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        final FillLayout layoutGroupLayout = new FillLayout(SWT.VERTICAL);
        layoutGroupLayout.marginHeight = layoutGroupLayout.marginWidth = 5;
        layoutGroupLayout.spacing = 3;
        layoutGroup.setLayout(layoutGroupLayout);

        final Button buttonShowNodeLabels = new Button(layoutGroup, SWT.CHECK);
        buttonShowNodeLabels.setSelection(true);
        buttonShowNodeLabels.setText("Show node labels");

        final Button buttonShowEdgeLabels = new Button(layoutGroup, SWT.CHECK);
        buttonShowEdgeLabels.setSelection(true);
        buttonShowEdgeLabels.setText("Show edge labels");

        final Button buttonLayoutTopToBottom = new Button(layoutGroup, SWT.RADIO);
        buttonLayoutTopToBottom.setSelection(false);
        buttonLayoutTopToBottom.setText("Layout top to bottom");

        final Button buttonLayoutLeftToRight = new Button(layoutGroup, SWT.RADIO);
        buttonLayoutLeftToRight.setSelection(true);
        buttonLayoutLeftToRight.setText("Layout left to right");

        optionsScrollComp.setMinSize(optionsComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        buttonScaleToFit.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                boolean scaleToFit = buttonScaleToFit.getSelection();
                buttonZoomIn.setEnabled(!scaleToFit);
                buttonZoomOut.setEnabled(!scaleToFit);
                gFrame.setScaleToFit(scaleToFit);
            }

        });
        buttonZoomIn.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                gFrame.zoomIn();
            }

        });
        buttonZoomOut.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                gFrame.zoomOut();
            }

        });

        buttonShowNodeLabels.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                gFrame.setShowNodes(buttonShowNodeLabels.getSelection(), true);
            }

        });
        buttonShowEdgeLabels.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                gFrame.setShowEdges(buttonShowEdgeLabels.getSelection(), true);
            }

        });

        buttonMinimize.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                ccsFrame.setMinimize(buttonMinimize.getSelection(), true);
            }

        });

        buttonLayoutTopToBottom.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (!buttonLayoutTopToBottom.getSelection())
                    return;
                gFrame.setLayoutLeftToRight(!buttonLayoutTopToBottom.getSelection(), true);
            }

        });
        buttonLayoutLeftToRight.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (!buttonLayoutLeftToRight.getSelection())
                    return;
                gFrame.setLayoutLeftToRight(buttonLayoutLeftToRight.getSelection(), true);
            }

        });
    }

    public void update(EvaluationStatus evalStatus) {
        // TODO Auto-generated method stub

    }

}

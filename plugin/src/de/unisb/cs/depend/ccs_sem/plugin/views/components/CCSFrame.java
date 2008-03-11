package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.grappa.GrappaFrame;

public class CCSFrame extends SashForm {

	protected final GrappaFrame gFrame;

	public CCSFrame(Composite parent, CCSEditor editor) {
		super(parent, SWT.HORIZONTAL);
		final CTabFolder tabs = new CTabFolder(this, SWT.NONE);
		gFrame = new GrappaFrame(this, SWT.NONE, editor);

		setWeights(new int[] { 2, 8 });

		final CTabItem optionsTab = new CTabItem(tabs, SWT.NONE);
		optionsTab.setText("Options");
		final Composite optionsComp = new Composite(tabs, SWT.NONE);
		optionsTab.setControl(optionsComp);
		tabs.showItem(optionsTab);

		optionsComp.setLayout(new GridLayout(1, true));

        final Group scalingGroup = new Group(optionsComp, SWT.NONE);
        scalingGroup.setText("Scaling");
        final FillLayout scalingGroupLayout = new FillLayout(SWT.VERTICAL);
        scalingGroupLayout.marginHeight = scalingGroupLayout.marginWidth = 5;
        scalingGroupLayout.spacing = 3;
        scalingGroup.setLayout(scalingGroupLayout);

        final Button buttonScaleToFit = new Button(scalingGroup, SWT.CHECK);
        buttonScaleToFit.setSelection(true);
        buttonScaleToFit.setText("Scale to fit");

        final Composite zoomingButtons = new Composite(scalingGroup, SWT.NONE);
        zoomingButtons.setLayout(new GridLayout(2, true));

        final Button buttonZoomIn = new Button(zoomingButtons, SWT.PUSH);
        buttonZoomIn.setEnabled(false);
        buttonZoomIn.setText("Zoom in");

        final Button buttonZoomOut = new Button(zoomingButtons, SWT.PUSH);
        buttonZoomOut.setEnabled(false);
        buttonZoomOut.setText("Zoom out");


        final Button buttonMinimize = new Button(optionsComp, SWT.CHECK);
        buttonMinimize.setSelection(false);
        buttonMinimize.setText("Minimize LTS");

        final Group layoutGroup = new Group(optionsComp, SWT.NONE);
        layoutGroup.setText("Layout");
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
            	gFrame.setMinimize(buttonMinimize.getSelection(), true);
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

	public void showGraph(boolean updateGraph) {
		gFrame.updateGraph();
	}

}

package de.unisb.cs.depend.ccs_sem.plugin.views.simulation;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.plugin.views.simulation.graph.StaticGrappaFrame;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;

public class TopLevelGraphView extends ViewPart {

	PageBook myPages;
	HashMap<Integer,StaticGrappaFrame> processNoToFrame;
	Composite main, defaultComp;
	ScrolledComposite scrollComp;
	
	public TopLevelGraphView() {
		processNoToFrame = new HashMap<Integer, StaticGrappaFrame>();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		myPages = new PageBook(parent, SWT.None);
		
		defaultComp = new Composite(myPages,SWT.None);
		new Label(defaultComp,SWT.None).setText("Test TopLevelGraphView");
		
		myPages.showPage(defaultComp);
		
		scrollComp = new ScrolledComposite(myPages,SWT.H_SCROLL | SWT.V_SCROLL);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		main = new Composite(scrollComp, SWT.None);
		scrollComp.setContent(main);
		
		main.setLayout(new GridLayout(2,false));
		myPages.showPage(scrollComp);
	}

	@Override
	public void setFocus() {
		myPages.setFocus();
	}
	
	private void updateFrame() {
		main.pack();
		scrollComp.setMinSize(main.computeSize(SWT.DEFAULT, SWT.DEFAULT));		
		scrollComp.update();
	}
	
	public void addProcess(int no, Expression mainExp) {
		final Composite comp = new Composite(main,SWT.BORDER);
		comp.setLayout(new GridLayout(1,false));
		new Label(comp,SWT.None).setText("Prozess "+no);
		
		final StaticGrappaFrame grappaFrame = new StaticGrappaFrame(comp,
				SWT.BORDER, mainExp);
		grappaFrame.updateGraph();
		processNoToFrame.put(no, grappaFrame);
		
		Composite buttons = new Composite(comp,SWT.None);
		buttons.setLayout(new RowLayout(SWT.HORIZONTAL));
		Button button = new Button(buttons,SWT.None);
		button.setText("+");
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				grappaFrame.setSize(
						grappaFrame.getSize().x + 10, 
						grappaFrame.getSize().y + 10);
				updateFrame();
			}

			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
			
		});
		button = new Button(buttons,SWT.None);
		button.setText("-");
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				grappaFrame.setSize(
						grappaFrame.getSize().x - 10, 
						grappaFrame.getSize().y - 10);
				updateFrame();
			}

			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		});
	}
	
	public void undo(int i) {
		if( processNoToFrame.get(i) == null )
			return;
		
		processNoToFrame.get(i).notifyUndo();
	}
	
	public void doAction(int i, Transition trans) {
		if( processNoToFrame.get(i) == null )
			return;
		
		processNoToFrame.get(i).takeTransition(trans);
	}
}
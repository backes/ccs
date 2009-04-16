package de.unisb.cs.depend.ccs_sem.plugin.views.simulation;

import java.util.HashMap;

import org.eclipse.swt.SWT;
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

public class TopLevelGraphView extends ViewPart {

	PageBook myPages;
	HashMap<Integer,StaticGrappaFrame> processNoToFrame;
	Composite main, defaultComp;
	
	public TopLevelGraphView() {
		processNoToFrame = new HashMap<Integer, StaticGrappaFrame>();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		myPages = new PageBook(parent, SWT.None);
		
		defaultComp = new Composite(myPages,SWT.None);
		new Label(defaultComp,SWT.None).setText("Test TopLevelGraphView");
		
		myPages.showPage(defaultComp);
		
		main = new Composite(myPages,SWT.None);
		main.setLayout(new GridLayout(2,false));
	}

	@Override
	public void setFocus() {
		myPages.setFocus();
	}
	
	public void addProcess(int no, Expression mainExp) {
		final Composite comp = new Composite(main,SWT.BORDER);
		comp.setLayout(new GridLayout(1,false));
		new Label(comp,SWT.None).setText("Prozess "+no);
		
		final StaticGrappaFrame grappaFrame = new StaticGrappaFrame(comp,SWT.BORDER,mainExp);
		grappaFrame.updateGraph();
		
		Composite buttons = new Composite(comp,SWT.None);
		buttons.setLayout(new RowLayout(SWT.HORIZONTAL));
		Button button = new Button(buttons,SWT.None);
		button.setText("+");
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println(grappaFrame.getSize());
				grappaFrame.setSize(
						grappaFrame.getSize().x + 10, 
						grappaFrame.getSize().y + 10);
				main.pack();
//				comp.update();
				main.update();
			}

			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
			
		});
		button = new Button(buttons,SWT.None);
		button.setText("-");
		
		myPages.showPage(main);
	}
}
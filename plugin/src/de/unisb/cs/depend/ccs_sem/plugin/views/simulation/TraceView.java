package de.unisb.cs.depend.ccs_sem.plugin.views.simulation;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;

public class TraceView extends ViewPart implements SelectionListener, IUndoObservable {
	
	private List traceList;
	private LinkedList<IUndoListener> observer;
	
	private class MyComponent extends SashForm {

		public MyComponent(Composite parent, TraceView view) {
			super(parent, SWT.VERTICAL);
			traceList = new List(this, SWT.BORDER | SWT.V_SCROLL);
			Button removeButton = new Button(this, SWT.None);
			removeButton.setText("Undo");
			removeButton.addSelectionListener(view);
			
			this.setWeights(new int[] {10,1});
		}
		
	}
	
	public TraceView() {
		observer = new LinkedList<IUndoListener> ();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		new MyComponent(parent,this);
	}

	@Override
	public void setFocus() {}

	public void widgetDefaultSelected(SelectionEvent e) {
		traceList.remove(traceList.getItemCount()-1);

		for( IUndoListener listener : observer ) {
			listener.notifyUndo();
		}
	}

	public void widgetSelected(SelectionEvent e) {}

	public void addAction(Action act) {
		traceList.add(act.toString());
	}

	public void addUndoListener(IUndoListener listener) {
		observer.add(listener);
	}

	public void removeUndoListener(IUndoListener listener) {
		observer.add(listener);
	}
}

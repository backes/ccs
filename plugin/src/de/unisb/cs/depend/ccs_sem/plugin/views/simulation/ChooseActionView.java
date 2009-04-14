package de.unisb.cs.depend.ccs_sem.plugin.views.simulation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.evaluators.Evaluator;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.ParallelExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RecursiveExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.adapters.TopMostExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.utils.Globals;

//TODO ChooseAction syntax check
/*
 * TODO ChooseAction doAction
 * -> kombiniere wer mit wem syncen kann
 * => Map: Position -> Expression (2 for sync)
 */
public class ChooseActionView extends ViewPart implements IUndoListener, SelectionListener {

	private final String process = "Prozess ";
	
	private Composite mainComp;
	private List list;
	private Button doButton;
	
	private PageBook myPageBook;
	private TraceView traceView;
	
	private LinkedList<Expression> parallelExps;
	private LinkedList<LinkedList<Expression>> history;
	private HashMap<Integer,Transition> listToTransMap;
	
	private class MyComponent extends SashForm implements SelectionListener {

		public MyComponent(Composite parent) {
			super(parent, SWT.VERTICAL);
			
			list = new List(this,SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			list.addSelectionListener(this);
			doButton = new Button(this,SWT.None);
			doButton.setText("Do action");
			
			this.setWeights(new int[] {10,1});
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			LinkedList<Integer> newSelection = new LinkedList<Integer>();
			for(int i : list.getSelectionIndices()) {
				if( !list.getItem(i).startsWith(process)) {
					newSelection.add(i);
				}
			}
			int[] arr = new int[newSelection.size()];
			int size = newSelection.size();
			for( int i=0; i<size; i++) {
				arr[i] = newSelection.poll();
			}
			list.setSelection(arr);
		}

		public void widgetSelected(SelectionEvent e) {
			widgetDefaultSelected(e);
		}
		
	}
	
	public ChooseActionView() {
		history = new LinkedList<LinkedList<Expression>> ();
		listToTransMap = new HashMap<Integer, Transition> ();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		myPageBook = new PageBook(parent, SWT.None);
		
		mainComp = new Composite(myPageBook, SWT.None);
		IWorkbenchPartSite site = getSite();
		IWorkbenchPage page = site == null ? null : site.getPage();
		if(page != null && page.getActiveEditor() != null) {
			if (page.getActiveEditor() instanceof CCSEditor) {
				initPage( (CCSEditor) page.getActiveEditor() );
			} else {
				new Label(mainComp,SWT.None).setText("Active Editor isn't a CCS Editor.");
			}
		} else {
			new Label(mainComp,SWT.None).setText("No active Page or no Editor.");	
		}
		myPageBook.showPage(mainComp);
		
		// TODO ChooseActionView addWorkbenchListener
	}
	
	public void initPage(final CCSEditor editor) {
		assert editor != null;
		
		mainComp.setLayout(new FillLayout(SWT.VERTICAL));
		mainComp = new MyComponent(myPageBook);
		doButton.addSelectionListener(this);
		
		actualizeActions(editor);
	}

	private void actualizeActions(CCSEditor editor) {		
		CCSDocument doc = ((CCSDocument) editor.getDocument());
		ParseStatus status = doc.reparseIfNecessary();
		Expression mainExp = status.getParsedProgram().getMainExpression();
		
		// Get the parallel Expressions
		if(mainExp instanceof TopMostExpression) {
			mainExp = ((TopMostExpression) mainExp).getInnerExpression();	
		}
		if (mainExp instanceof RestrictExpression) {
			Iterator<Expression> iter = ((RestrictExpression) mainExp).getChildren().iterator();
			mainExp = iter.next(); // restrict expression has exactly one child
		}
		Evaluator evaluator = Globals.getDefaultEvaluator();
		try {
			evaluator.evaluate(mainExp);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		parallelExps = new LinkedList<Expression>();
		LinkedList<Expression> toCheck = new LinkedList<Expression> ();
		toCheck.add(mainExp);
		while(!toCheck.isEmpty()) {
			 Expression exp = toCheck.poll();
			 if (exp instanceof ParallelExpression) {
				ParallelExpression para = (ParallelExpression) exp;
				toCheck.addAll(para.getChildren());
			} else if(exp instanceof RecursiveExpression) {
				RecursiveExpression rec = (RecursiveExpression) exp;
				Expression temp = rec.getInstantiatedExpression();
				if( temp instanceof ParallelExpression) {
					toCheck.add(temp);
				} else {
					parallelExps.add(temp);
				}
			} else {
				parallelExps.add(exp);
			}
		}
		
		mainExp.resetEval();
		history.add(parallelExps);
		
		list.getDisplay().syncExec(new Runnable() {
			public void run() {
				fillList();
			}
		});
	}
	
	private void fillList() {		
		int i=0;
		for(Expression e : parallelExps) {
			if( !e.isEvaluated() ) {
				try {
					Globals.getDefaultEvaluator().evaluate(e);
				} catch (InterruptedException exc) {
					exc.printStackTrace();
				}
			}
			
			listToTransMap.put(list.getItemCount(),null); // the process+i
			list.add(process+i+":");
			for(Transition t : e.getTransitions() ) {
				// Before add the next element -> getItemCount = nr of next added item
				listToTransMap.put(list.getItemCount(),t);
				
				list.add( "  " + t.getAction().toString());
			}
			i++;
		}
	}
	
	private void refillList() {
		list.removeAll();
		listToTransMap.clear();
		
		fillList();
	}

	@Override
	public void setFocus() {
		myPageBook.setFocus();
	}

	public void notifyUndo() {
		if( history.size() > 1 ) {
			history.removeLast();
			parallelExps = history.getLast();
			
			refillList();
		}
	}

	public void widgetSelected(SelectionEvent e) {
		// Do selected action
		int[] selected = list.getSelectionIndices();
		
		if( selected.length == 1
				&& !list.getSelection()[0].startsWith(process) ) {
			doAction( selected[0] );
		} else if( selected.length == 2 
				&& !list.getSelection()[0].startsWith(process) 
				&& !list.getSelection()[1].startsWith(process) ) {
			doSynchronousAction( selected[0], selected[1] );
		} else {
			// TODO ChooseActionView notifyUser about bad usage of the view
		}
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {} // ignore


	private void doSynchronousAction(int i, int j) {
		// TODO Auto-generated method stub
	}

	private void doAction(int selectedItem) {
		Transition t = listToTransMap.get(selectedItem);
		
		int prozessNr = 0;
		for( int j=0; j<selectedItem; j++ ) {
			if(list.getItem(j).startsWith(process)) {
				prozessNr++;
			}
		}
		LinkedList<Expression> next = new LinkedList<Expression> (parallelExps);
		next.remove(prozessNr-1);
		Expression target = t.getTarget();
		if( !target.isEvaluated() ) {
			try {
				Globals.getDefaultEvaluator().evaluate(target);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		next.add(prozessNr-1, target);
		history.add(next);
		parallelExps = next;
		
		reportToTrace( list.getItem(selectedItem) );
		
		refillList();
	}
	
	private void reportToTrace(String act) {
		if( traceView == null ) {
			// TODO NullPointer-Exception possible
			
			for( IViewReference viewRef : getSite().getPage().getViewReferences() ) {
				IViewPart viewPart = viewRef.getView(false);
				if( viewPart instanceof TraceView ) {
					traceView = (TraceView) viewPart;
					traceView.addUndoListener(this);
				}
			}
		}
		
		if(traceView != null) {
			traceView.addAction(act);
		}
	}
}

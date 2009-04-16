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
import org.eclipse.swt.widgets.Table;
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
import de.unisb.cs.depend.ccs_sem.semantics.expressions.RestrictExpression;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.adapters.TopMostExpression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
import de.unisb.cs.depend.ccs_sem.utils.Globals;

public class ChooseActionView extends ViewPart implements IUndoListener, SelectionListener {

	private final String process = "Prozess ";
	
	private Composite mainComp;
	private Table table;
	private Button doButton;
	
	private PageBook myPageBook;
	private TraceView traceView;
	private TopLevelGraphView topLevelGraphView;
	
	private LinkedList<Expression> parallelExps;
	private LinkedList<LinkedList<Expression>> history;
	private HashMap<Integer,Transition> listToTransMap;
	private HashMap<Integer,Integer> indexToProcessNr;
		
	private class MyComponent extends SashForm implements SelectionListener {

		public MyComponent(Composite parent) {
			super(parent, SWT.VERTICAL);
			
			table = new Table(this,SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			table.addSelectionListener(this);
			doButton = new Button(this,SWT.None);
			doButton.setText("Do action");
			
			this.setWeights(new int[] {10,1});
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			for(int i : table.getSelectionIndices()) {
				if( table.getItem(i).getText().startsWith(process)) {
					table.deselect(i);
				}
			}
			
			// at most 2 elements may be selected
			if( table.getSelectionCount() > 2 ) {
				table.deselectAll();
			}
			
			// mark for synchronisation
			if( table.getSelectionCount()==1 ) {
				markSynchronisationPartner( table.getSelectionIndex() );
			} else if( table.getSelectionCount()==2 ) {
				int selection[] = table.getSelectionIndices();
				Transition t1 = listToTransMap.get(selection[0]);
				Transition t2 = listToTransMap.get(selection[1]);
				
				assert t1 != null && t2 != null; // should be checked before
				
				// no synchronisation possible or the same process
				if( !t1.isSynchronizableWith(t2) || indexToProcessNr.get(selection[0])==indexToProcessNr.get(selection[1]) ) {
					table.deselectAll();
				}
			}
		}
		
		private void markSynchronisationPartner(int selection) {
			
		}

		public void widgetSelected(SelectionEvent e) {
			widgetDefaultSelected(e);
		}
		
	}
	
	public ChooseActionView() {
		history = new LinkedList<LinkedList<Expression>> ();
		listToTransMap = new HashMap<Integer, Transition> ();
		indexToProcessNr = new HashMap<Integer,Integer> ();
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
				
				initReferences(page);
			} else {
				new Label(mainComp,SWT.None).setText("Active Editor isn't a CCS Editor.");
			}
		} else {
			new Label(mainComp,SWT.None).setText("No active Page or no Editor.");	
		}
		myPageBook.showPage(mainComp);
		
		// TODO ChooseActionView addWorkbenchListener
	}
	
	private void initReferences(IWorkbenchPage page) {
		for( IViewReference ref : page.getViewReferences() ) {
			IViewPart viewPart = ref.getView(false);
			if( viewPart instanceof TraceView) { // init traceView
				traceView = (TraceView) viewPart;
			} else if( viewPart instanceof TopLevelGraphView ) {
				topLevelGraphView = (TopLevelGraphView) viewPart;
			}
			
			if( traceView != null && topLevelGraphView != null ) break;
		}
		
		// init TopLevelGraphView
		if( topLevelGraphView != null ) {
			Iterator<Expression> iter = parallelExps.iterator();
			Expression temp;
			for(int i=0; i<parallelExps.size(); i++) {
				temp = iter.next();
								
				topLevelGraphView.addProcess(i, temp);
			}
		}
	}
	
	public void initPage(final CCSEditor editor) {
		assert editor != null;
		
		mainComp.setLayout(new FillLayout(SWT.VERTICAL));
		mainComp = new MyComponent(myPageBook);
		doButton.addSelectionListener(this);
		
		updateActions(editor);
	}

	private void updateActions(CCSEditor editor) {		
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
			} else {
				parallelExps.add(exp);
			}
		}

		mainExp.resetEval();
		try {
			for( Expression e : parallelExps ) {
				evaluator.evaluate(e);
			}
		} catch(InterruptedException e) { e.printStackTrace(); }
		
		history.add(parallelExps);
		
		fillList();
	}
	
	private void fillList() {		
		int i=0;
		for(Expression e : parallelExps) {
			listToTransMap.put(table.getItemCount(),null); // the process+i
			indexToProcessNr.put(table.getItemCount(), -1); // the "process i" doesn't belong to any process 
			addToTable( process+i+":" );

			for(Transition t : e.getTransitions() ) {
				// Before add the next element -> getItemCount = nr of next added item
				listToTransMap.put(table.getItemCount(),t);
				indexToProcessNr.put(table.getItemCount(), i);
				
				addToTable( "  " + t.getAction().toString() );
			}
			i++;
		}
	}
	
	private void refillList() {
		table.removeAll();
		listToTransMap.clear();
		indexToProcessNr.clear();
		
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

	/*
	 * This method is called when the doAction-Button is pressed
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		// Do selected action
		int[] selected = table.getSelectionIndices();
		
		if( selected.length == 1
				&& !table.getSelection()[0].getText().startsWith(process) ) {
			doAction( selected[0] );
		} else if( selected.length == 2 
				&& !table.getSelection()[0].getText().startsWith(process) 
				&& !table.getSelection()[1].getText().startsWith(process) ) {
			doSynchronousAction( selected[0], selected[1] );
		} else {
			// TODO ChooseActionView notifyUser about bad usage of the view
		}
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {} // ignore


	private void doSynchronousAction(int i, int j) {
		// TODO ChooseActionView implement
	}

	private void doAction(int selectedItem) {
		Transition t = listToTransMap.get(selectedItem);
		
		int prozessNr = 0;
		for( int j=0; j<selectedItem; j++ ) {
			if(table.getItem(j).getText().startsWith(process)) {
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
		
		reportToTrace( table.getItem(selectedItem).getText() );
		
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
	
	private void addToTable(String str) {
		table.setItemCount(table.getItemCount()+1);
		table.getItem(table.getItemCount()-1).setText(str);
	}
}

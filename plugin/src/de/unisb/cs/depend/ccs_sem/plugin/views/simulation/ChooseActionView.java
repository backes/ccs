package de.unisb.cs.depend.ccs_sem.plugin.views.simulation;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
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
 * update von wo bis wo welche exp aus der liste ist
 * refuelle liste
 * -> kombiniere wer mit wem syncen kann
 * => Map: Position -> Expression (2 for sync)
 */
public class ChooseActionView extends ViewPart implements IUndoListener {

	private Composite mainComp;
	private List list;
	private LinkedList<Expression> parallelExps;
	private PageBook myPageBook;
	private LinkedList<LinkedList<Expression>> history;
	
	private class MyComponent extends SashForm {

		public MyComponent(Composite parent) {
			super(parent, SWT.VERTICAL);
			
			list = new List(this,SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
			Button doButton = new Button(this,SWT.None);
			doButton.setText("Do action");
			
			this.setWeights(new int[] {10,1});
		}
		
	}
	
	public ChooseActionView() {
		history = new LinkedList<LinkedList<Expression>> ();
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
	}
	
	public void initPage(final CCSEditor editor) {
		assert editor != null;
		
		mainComp.setLayout(new FillLayout(SWT.VERTICAL));
		mainComp = new MyComponent(myPageBook);
		
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
		for( Expression e : parallelExps ) {
			e.evaluate();
		}
		history.add(parallelExps);
		
		list.getDisplay().syncExec(new Runnable() {
			
			public void run() {
				int i=0;
				for(Expression e : parallelExps) {
					if( !e.isEvaluated() )
						e.evaluate();
					
					list.add("Prozess "+i+":");
					for(Transition t : e.getTransitions() ) {
						list.add( "  " + t.getAction().toString());
					}
					i++;
				}
			}
		});
	}

	@Override
	public void setFocus() {
		myPageBook.setFocus();
	}

	public void notifyUndo() {
		// TODO ChooseActionView undo
	}

}

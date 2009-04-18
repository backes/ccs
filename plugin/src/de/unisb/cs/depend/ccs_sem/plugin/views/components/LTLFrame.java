package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import java.util.HashMap;
import ltlcheck.Counterexample;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ModelCheckingJob;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ModelCheckingJob.ModelCheckingStatus;
import de.unisb.cs.depend.ccs_sem.plugin.utils.LTLPropertyHandler;
import de.unisb.cs.depend.ccs_sem.plugin.views.CounterExampleView;
import de.unisb.cs.depend.ltlchecker.LTLSyntaxChecker;

public class LTLFrame extends SashForm implements IDocumentListener {	
	private Text theLine = null;
	private List theList = null;
	private Label statusLabel = null;
	
	private Button counterExampleButton = null;
	private Counterexample counterEx = null;
	private IWorkbenchWindow window;
	
	private CCSEditor editor;
	private HashMap<String,Counterexample> alreadyChecked;
	private LTLPropertyHandler propertyHandler;
	
	private class LeftSide extends SashForm implements SelectionListener {

		public LeftSide(Composite parent) {
			super(parent, SWT.VERTICAL );
		
			final List ltlList = new List(this,SWT.BORDER | SWT.V_SCROLL);
			theList = ltlList;
			ltlList.addSelectionListener(this);
			
			final Text formula = new Text(this,SWT.BORDER);
			formula.setText("Hier LTL Formel eingeben");
			theLine = formula;
			
			this.setWeights(new int[] { 10,1 });
		}
		// TODO deny resize of the sash form

		public void widgetDefaultSelected(SelectionEvent e) {}

		public void widgetSelected(SelectionEvent e) {
			// set some labeling thread to "awake" or to "sleep"
			
			String[] str = theList.getSelection();
			if( str.length == 1 ) {
				if( alreadyChecked.containsKey(str[0]) ) {
					handleModelCheckingResult(alreadyChecked.get(str[0]), theList.getSelectionIndex() );
				} else {
					synchronized (statusLabel) {
						statusLabel.setForeground(new Color(getDisplay(), 0,0,0) );
						statusLabel.setText("Property unchecked.");	
						counterExampleButton.setEnabled(false);
					}	
				}
			}
		}
	}
	
	private class RightSide extends SashForm {

		public RightSide(Composite parent) {
			super(parent, SWT.VERTICAL);
			
			Composite topButtons = new Composite(this, SWT.NONE);
			topButtons.setLayout(new GridLayout(1,false));
			
			// Init "Check"-button
			Button button = new Button(topButtons,SWT.None);
			button.setText("Check");
			button.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// nothing to do
				}
				public void widgetSelected(SelectionEvent e) {
					checkProperty();
				}
			});
			
			button = new Button(topButtons,SWT.None);
			button.setText("Edit");
			button.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// nothing to do
				}

				public void widgetSelected(SelectionEvent e) {
					editActiveSelection();
				}
			});
			
			// Init "Remove"-button
			button = new Button(topButtons,SWT.None);
			button.setText("Remove");
			button.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// nothing to do
				}
				public void widgetSelected(SelectionEvent e) {
					removeFromList();
				}
			});
			Label check = new Label(topButtons, SWT.None);
			check.setText("No LTL-Property selected");
			statusLabel = check;
			
			counterExampleButton = new Button( topButtons, SWT.None);
			counterExampleButton.setText("Get Counterexample");
			counterExampleButton.setEnabled(false);
			counterExampleButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// ignore
				}
				public void widgetSelected(SelectionEvent e) {
					getCounterExample();
				}
			});
			
			// Init "Add"-button
			button = new Button(this, SWT.None);
			button.setText("Add");
			button.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// nothing to do
				}
				public void widgetSelected(SelectionEvent e) {
					addToList();
				}
			});
			setWeights( new int[] {10,1} );
		}
	}
	
	private void addToList() {
		if( theLine == null || theList == null ) return; // shouldn't happen
		
		String toAdd = theLine.getText();
		if( toAdd.equals("") ) return; // nothing in the text-field
		
		for( String toTest : theList.getItems() ) {
			if( toTest.equals(toAdd) ) return; // already in list
		}
		
		if( LTLSyntaxChecker.correctSyntax(toAdd) ) {
			theList.add( toAdd );
			
			propertyHandler.add(theList.getItem(
					theList.getItemCount()-1
					));
		} else {
			handleLTLFormulaError();
		}
	}
	
	private void removeFromList() {
		if( theList == null ) return; // Error occurred
		
		int index = theList.getSelectionIndex();
		if( index == -1 ) return; // nothing selected
		
		theList.remove(index);
		propertyHandler.remove(index);
	}
	
	public void editActiveSelection() {
		if( theList.getSelectionCount() == 0 )
			return;
		
		InputDialog inDia = new InputDialog(getShell(),
				"Titel","Message",theList.getSelection()[0], new IInputValidator() {
					public String isValid(String newText) {
						if( LTLSyntaxChecker.correctSyntax(newText) ) {
							return null;
						} else {
							return "Not the correct LTL syntax.";
						}
					}
		});
		inDia.setBlockOnOpen(true);
			// BlockOnOpen means in the line after inDia.open(), the dialog is already closed (by user)
		inDia.open();
		
		if( inDia.getReturnCode()==InputDialog.OK) {
			int selected = theList.getSelectionIndex();
			theList.setItem(selected, inDia.getValue()); 
			propertyHandler.edit(selected, inDia.getValue());
		}
	}
	
	private void checkProperty() {
		if( theList == null || theLine == null ) return; // error occurred
		
		// get formula to check
		final int index = theList.getSelectionIndex();
		final String formula = index==-1 ? theLine.getText() : theList.getItem(index);
		
		if (!(editor.getDocument() instanceof CCSDocument)) {
			return;
		}
		ModelCheckingJob mcJobTM = new ModelCheckingJob(formula,
				(CCSDocument) editor.getDocument(),index);
		
		// use LTLChecker, could take some time -> own thread
		mcJobTM.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if( event.getResult() instanceof ModelCheckingStatus) {
					ModelCheckingStatus status = (ModelCheckingStatus) event.getResult();
					
					if( status.getSeverity() == IStatus.OK && 
							((CCSDocument) editor.getDocument()).getModificationStamp()
							== status.getStarttime() ) {
						handleModelCheckingResult(status.getCounterexample(),
								status.getIndex());
					}
					// TODO LTLFrame -> ErrorStatus, Doc modified,etc...
				}
			}
		});
		mcJobTM.schedule();
	}
	
	public void handleModelCheckingResult(final Counterexample ce, final int index) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				synchronized (statusLabel) {
					if( theList.getSelectionIndex() != index ) return; 
					
					if( ce == null ) {
						statusLabel.setForeground( new Color(getDisplay(), 0,255,0) );
						statusLabel.setText("Satisfied");
						counterExampleButton.setEnabled(false);
					} else {
						statusLabel.setForeground( new Color(getDisplay(), 255,0,0) );
						statusLabel.setText("Violated");
						counterEx = ce;
						counterExampleButton.setEnabled(true);
					}
				}
			}
		});
		propertyHandler.save(index, ce);
	}
	
	private void getCounterExample() {
		// Open the CE view
		if( counterEx == null ) return; // in this case, the button is disabled
		
		CounterExampleView ceview = null;
		try {
			ceview = (CounterExampleView) window.getActivePage().showView(Global.getCounterExampleViewId());
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		ceview.changeFrame(counterEx);
	}
	
	private void handleLTLFormulaError() {		
		statusLabel.setForeground(new Color(getDisplay(), 125,0,0));
		statusLabel.setText("LTL Syntax error");
	}
	
	private LTLFrame(Composite parent) {
		super(parent, SWT.HORIZONTAL | SWT.SMOOTH);
		this.setLayout(new GridLayout(2,false));
		
		// List & TextField
		new LeftSide(this);

		// Add Buttons
		new RightSide(this);
		
		this.setWeights(new int[] { 5,1 });
	}

	public LTLFrame(Composite parent, CCSEditor editor, IWorkbenchWindow window) {
		this(parent);
		
		this.editor = editor;
		this.window = window;
		editor.getDocument().addDocumentListener(this);
		alreadyChecked = new HashMap<String, Counterexample>();
		
		// Init LTLProperty handler
		IFile relatedFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		propertyHandler = new LTLPropertyHandler(relatedFile);
		propertyHandler.loadAll(theList, alreadyChecked);
	}

	@Override
	public void dispose() {
		if( editor != null && editor.getDocument() != null )
			editor.getDocument().removeDocumentListener(this);
	}
	
	public void documentAboutToBeChanged(DocumentEvent event) {}
 
	public void documentChanged(DocumentEvent event) {			
		alreadyChecked.clear();
		theList.deselectAll();
		statusLabel.setForeground(new Color(getDisplay(), 0,0,0));
		statusLabel.setText("No LTL-Property selected");
		counterExampleButton.setEnabled(false);
		
		// remove checked property
		propertyHandler.removeAllResults();
	}
}
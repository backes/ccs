package de.unisb.cs.depend.ccs_sem.plugin.views;

import ltlcheck.Counterexample;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.views.components.CounterExampleFrame;

public class CounterExampleView extends ViewPart 
	implements ISelectionListener, IPartListener, IDocumentListener {

	private PageBook myPageBook;
	private Composite loading;
	private Composite defaultComp;
	private CounterExampleFrame currentFrame;
	private CCSEditor currentEditor;
	private IWorkbenchWindow window;
	
	@Override
	public void createPartControl(Composite parent) {
		myPageBook = new PageBook(parent, SWT.None);
		
		defaultComp = new Composite(myPageBook, SWT.None);
		defaultComp.setLayout(new GridLayout(1, true));
		Label l = new Label(defaultComp,SWT.None);
		l.setText("No violated LTL-Property selected.");
		
		loading = new Composite( myPageBook, SWT.None);
		loading.setLayout(new GridLayout(1,true));
		l = new Label( loading, SWT.None);
		l.setText("Loading Counter-example...");
		
		myPageBook.showPage(defaultComp);
		
		// Workbenchlisten etc.
		final IWorkbenchPartSite site = getSite();
	    final IWorkbenchPage page = site == null ? null : site.getPage();
	    if (page != null) {
	        page.addSelectionListener(this);
	        page.addPartListener(this);
	    }
	    window = site == null ? null : site.getWorkbenchWindow();
	    
	    currentFrame = null;
	    currentEditor = null;
	}

	@Override
	public void setFocus() {
		myPageBook.setFocus();
	}

	public void changeFrame(final Counterexample ce) {
		myPageBook.showPage(loading);
		 
		currentFrame = new CounterExampleFrame(myPageBook,ce,window);
		myPageBook.getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				
				myPageBook.showPage(currentFrame);
			}
		});
		this.setFocus();
	}

	// Clean up if CCSEditor is changed or document is edited
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof CCSEditor) {
			if( currentEditor != part ) {
				if(currentEditor != null)
					currentEditor.getDocument().removeDocumentListener(this);
				currentEditor = (CCSEditor) part;
				currentEditor.getDocument().addDocumentListener(this);
				
				if( currentFrame != null ) {
					currentFrame.removeCounterExample();
					currentFrame = null;
				}
			}
		}
	}

	public void documentChanged(DocumentEvent event) {
		if( currentFrame != null ) {
			currentFrame.removeCounterExample();
		}
		myPageBook.showPage(defaultComp);
	}


	public void partClosed(IWorkbenchPart part) {
		if (part instanceof CCSEditor) {
			myPageBook.showPage(defaultComp);
		}
	}

	public void partDeactivated(IWorkbenchPart part) {}
	public void partOpened(IWorkbenchPart part) {}
	public void partBroughtToTop(IWorkbenchPart part) {}
	public void partActivated(IWorkbenchPart part) {}
	public void documentAboutToBeChanged(DocumentEvent event) {}
}

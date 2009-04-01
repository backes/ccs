package de.unisb.cs.depend.ccs_sem.plugin.views;

import java.util.HashMap;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import de.unisb.cs.depend.ccs_sem.plugin.actions.ExportAll;
import de.unisb.cs.depend.ccs_sem.plugin.actions.Help;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.views.components.LTLFrame;

public class LTLCheckerView extends ViewPart implements IPartListener, ISelectionListener {

	private PageBook myPages;
	private Composite defaultComp;
	private HashMap<CCSEditor, LTLFrame> frames;
	private IWorkbenchWindow window;
	private CCSEditor currentCCSEditor = null;
	
	@Override
	public void createPartControl(Composite parent) {
		frames = new HashMap<CCSEditor, LTLFrame> ();
		myPages = new PageBook(parent, SWT.None);
		
		// replaced by LTLCheckerFrame if an editor is opened and selected
		defaultComp = new Composite(myPages, SWT.NONE);
        defaultComp.setLayout(new GridLayout(1, true));

        final Label defaultLabel = new Label(defaultComp, SWT.None);
        defaultLabel.setText("No CCS-File selected.");
        defaultLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		 final IWorkbenchPartSite site = getSite();
	     final IWorkbenchPage page = site == null ? null : site.getPage();
	     window = page.getWorkbenchWindow();
	     if (page != null) {
	         page.addSelectionListener(this);
	         page.addPartListener(this);
	     }

	     final IEditorPart activeEditor = page.getActiveEditor();
	     if (activeEditor != null)
	         changeEditor(activeEditor);
	     
	     IActionBars bars = getViewSite().getActionBars();
	     fillToolbar(bars.getToolBarManager() );
	}
	
	@Override
	public void dispose() {
		IWorkbenchSite site = getSite();
		IWorkbenchPage page = site == null ? null : site.getPage();
		if( page != null ) {
			page.removeSelectionListener(this);
			page.removePartListener(this);
		}
		frames.clear();
	}
	
	private void fillToolbar(IToolBarManager toolBarManager) {
		toolBarManager.add(new ExportAll());
		toolBarManager.add(new Help("LTLView"));
	}

	private void changeEditor( IEditorPart editor ) {
		if (editor instanceof CCSEditor) {
			currentCCSEditor = (CCSEditor) editor;
			
			LTLFrame fr = frames.get(currentCCSEditor);
			
			if( fr == null ) { // editor unknown
				fr = new LTLFrame(myPages,currentCCSEditor, window);
				
				frames.put(currentCCSEditor, fr);
			} else {
				// perhaps check if property checking is running and stop if neccessary
			}
			
			myPages.showPage(fr);
		} else {
			myPages.showPage(defaultComp);
		}
	}

	@Override
	public void setFocus() {
		myPages.setFocus();		
	}

	public void partActivated(IWorkbenchPart part) {
		// ignore
	}

	public void partBroughtToTop(IWorkbenchPart part) {
		// ignore
	}

	public void partClosed(IWorkbenchPart part) {
		// or changeEditor to the current editor
		myPages.showPage(defaultComp);
	}

	public void partDeactivated(IWorkbenchPart part) {
		// ignore
	}

	public void partOpened(IWorkbenchPart part) {
		// ignore
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IEditorPart) {
			changeEditor( (IEditorPart) part );
		}
	}
	
	public LTLFrame getCurrentFrame() {
		return frames.get(currentCCSEditor);
	}

}

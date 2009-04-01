package de.unisb.cs.depend.ccs_sem.plugin.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.unisb.cs.depend.ccs_sem.plugin.Global;
import de.unisb.cs.depend.ccs_sem.plugin.views.LTLCheckerView;
import de.unisb.cs.depend.ccs_sem.plugin.views.components.LTLFrame;

public class ExportWizardPage extends WizardPage {

	private Button checkLTLButton;
	
	protected ExportWizardPage() {
		super("Export");
		LTLFrame frame = null;
		try {
			LTLCheckerView ltlView = (LTLCheckerView) PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getActivePage().
				showView(Global.getLTLCheckerViewId());
			frame = ltlView.getCurrentFrame();
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		// TODO WIZARD -> complete
		setDescription("Hier kurz einstellen was, wie und wohin exportiert werden soll.");
		setTitle("Einstellungen");
	}

	public void createControl(final Composite parent) {
		Composite toplevel = new Composite(parent,SWT.None);
		toplevel.setLayout(new GridLayout(1,false));
		
		Group groupPlace = new Group(toplevel,SWT.SHADOW_OUT);
		groupPlace.setLayout(new GridLayout(2,false));
		groupPlace.setText("Speicherort");
		final Text text = new Text(groupPlace,SWT.SINGLE);
		
		Button button = new Button(groupPlace,SWT.PUSH);
		button.setText("Durchsuchen...");
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore		
			}

			public void widgetSelected(SelectionEvent e) {	
				FileDialog dia = new FileDialog(
						parent.getShell()
						,SWT.APPLICATION_MODAL);
				text.setText(dia.open());
			}
		});
		
		button = new Button(toplevel,SWT.CHECK);
		button.setText("Export Graph");
		
		button = new Button(toplevel, SWT.CHECK);
		button.setText("Export LTL results");
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.getSource();
				if( checkLTLButton != null && b != null ) {
					checkLTLButton.setEnabled(b.getSelection());
				}
			}
			
		});
		
		checkLTLButton = new Button(toplevel, SWT.CHECK);
		checkLTLButton.setText("Check unchecked LTL-Properties");
		checkLTLButton.setEnabled(false);
		checkLTLButton.setSelection(true);
		
		setControl(toplevel);
	}

}

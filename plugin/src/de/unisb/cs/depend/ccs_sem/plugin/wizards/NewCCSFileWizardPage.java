package de.unisb.cs.depend.ccs_sem.plugin.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (ccs).
 */

public class NewCCSFileWizardPage extends WizardNewFileCreationPage {

    private Button addNoTextButton;
    private Button addTextButton;

    public NewCCSFileWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle("New CCS File");
        setDescription("This wizard creates a new file with *.ccs extension.");
        setFileExtension("ccs");
        setFileName("new_file.ccs");
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        final Composite comp = (Composite) getControl();

        final Group myGroup = new Group(comp, SWT.NONE);
        myGroup.setLayout(new GridLayout());
        myGroup.setText("File creation settings");
        myGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

        addTextButton = new Button(myGroup, SWT.RADIO);
        addTextButton.setText("Add sample text");
        addTextButton.setSelection(true);

        addNoTextButton = new Button(myGroup, SWT.RADIO);
        addNoTextButton.setText("Add no text");
        addNoTextButton.setSelection(false);

    }

    public boolean finish() {
        final IFile file = createNewFile();
        getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        final IWorkbenchPage page =
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IDE.openEditor(page, file, true);
                    }
                });
            }
        });
        return true;
    }

    @Override
    protected InputStream getInitialContents() {
        final String contents;
        if (addTextButton.getSelection())
            contents = "(* This is an example CCS file *)\n\n"
                + "// some process declarations:\n"
                + "X = x.y.X;\n"
                + "Y[c,a,b] = c!a.c!b.Y[c,b,a];\n\n"
                + "// the main expression:\n"
                + "X | Y[out, 0, 1]\n";
        else
            contents = "";
        return new ByteArrayInputStream(contents.getBytes());
    }

}
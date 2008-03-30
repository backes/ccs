package de.unisb.cs.depend.ccs_sem.plugin.preferencePage;

import java.io.File;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


public class ExecutableFieldEditor extends FileFieldEditor {

    public ExecutableFieldEditor(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
    }

    @Override
    protected boolean checkState() {

        String msg = null;

        String path = getTextControl().getText();
        if (path != null) {
            path = path.trim();
        } else {
            path = "";//$NON-NLS-1$
        }
        if (path.length() == 0) {
            if (!isEmptyStringAllowed()) {
                msg = getErrorMessage();
            }
        } else {
            // check if it's on the path
            final String pathEnvString = System.getenv("PATH");
            String[] pathDirs = new String[0];
            if (pathEnvString != null && path.indexOf(File.separator) == -1)
                pathDirs = pathEnvString.split(Pattern.quote(File.pathSeparator));
            final String[] executableExtensions = "win32".equals(SWT.getPlatform())
                ? new String[] {
                        "", ".com", ".exe", ".bat", ".cmd", ".pif", ".scf", ".scr" }
                : new String[] { "" };

            // ensure that "" is on the path
            String[] oldPathDirs = pathDirs;
            pathDirs = new String[oldPathDirs.length+1];
            pathDirs[0] = "";
            System.arraycopy(oldPathDirs, 0, pathDirs, 1, oldPathDirs.length);
            oldPathDirs = null;

            boolean valid = false;

            searching:
            for (String pathDir: pathDirs) {
                if (pathDir.length() > 0 && !pathDir.endsWith(File.separator))
                    pathDir = pathDir + File.separator;
                for (final String extension: executableExtensions) {
                    final File testFile = new File(pathDir + path + extension);
                    if (testFile.isFile()) {
                        valid = true;
                        break searching;
                    }
                }
            }
            if (!valid)
                msg = getErrorMessage();
        }

        if (msg != null) { // error
            showErrorMessage(msg);
            return false;
        }

        // OK!
        clearErrorMessage();
        return true;
    }

}

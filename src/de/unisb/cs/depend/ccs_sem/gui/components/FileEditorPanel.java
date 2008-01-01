package de.unisb.cs.depend.ccs_sem.gui.components;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

import de.unisb.cs.depend.ccs_sem.exceptions.FileReadException;


public class FileEditorPanel extends JTabbedPane {

    private static final long serialVersionUID = -373223814165043387L;

    private static final Icon ICON_CSS_FILE = new ImageIcon("images/icons/css_file.png", "CSS File");
    
    private List<File> openedFiles = new LinkedList<File>();
    
    public FileEditorPanel() {
        super();
    }
    
    public FileEditorPanel(Collection<File> filesToOpen) throws FileReadException {
        this();
        for (File file: filesToOpen)
            openFile(file);
    }

    public void openFile(File file) throws FileReadException {
        int index = openedFiles.indexOf(file);
        if (index != -1) {
            setSelectedIndex(index);
            return;
        }
        
        // file is not opened yet: open it
        
        CSSEditorPanel newPanel = new CSSEditorPanel(file);
        openedFiles.add(file);
        insertTab(file.getName(), ICON_CSS_FILE, newPanel, file.getAbsolutePath(), getTabCount());
    }

    public void newFile() {
        CSSEditorPanel newPanel = new CSSEditorPanel();
        insertTab("new file", ICON_CSS_FILE, newPanel, "new file", getTabCount());
    }
    
    public CSSEditorPanel getCurrentlyOpenedPanel() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex == -1)
            return null;
        
        return (CSSEditorPanel) getComponentAt(selectedIndex);
    }

}

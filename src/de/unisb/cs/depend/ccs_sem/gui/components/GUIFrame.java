package de.unisb.cs.depend.ccs_sem.gui.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;



public abstract class GUIFrame
        extends JFrame
        implements ActionListener, WindowListener {
    
    private FileEditorPanel fileEditorPanel;
    
    public GUIFrame() {
        super("CSS Evaluator");
        initialize();
    }

    private void initialize() {
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(600, 400));
        setMinimumSize(new Dimension(200, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        fileEditorPanel = new FileEditorPanel();
        
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(fileEditorPanel, gbc);
        
        /**
         * THE MENU
         */
        
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        
        JMenu fileMenu = new JMenu("File");
        menubar.add(fileMenu);
        
        JMenuItem newFileItem = new JMenuItem("New File");
        newFileItem.setActionCommand("newFile");
        newFileItem.addActionListener(this);
        fileMenu.add(newFileItem);
        
        JMenuItem fileOpenItem = new JMenuItem("Open File...");
        fileOpenItem.setActionCommand("openFile");
        fileOpenItem.addActionListener(this);
        fileMenu.add(fileOpenItem);
        
        fileMenu.addSeparator();
        
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setActionCommand("quit");
        quitItem.addActionListener(this);
        quitItem.setMnemonic(KeyEvent.VK_F4);
        fileMenu.add(quitItem);
        
        JMenu processMenu = new JMenu("Process");
        menubar.add(processMenu);
        
        JMenuItem processItem = new JMenuItem("process...");
        processItem.setActionCommand("process");
        processItem.addActionListener(this);
        processMenu.add(processItem);

    }

    public FileEditorPanel getFileEditorPanel() {
        return fileEditorPanel;
    }

}

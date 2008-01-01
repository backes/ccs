package de.unisb.cs.depend.ccs_sem.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;



public abstract class GUIFrame extends JFrame {
    
    private FileEditorPanel fileEditorPanel;
    
    public GUIFrame() {
        super("CSS Evaluator");
        initialize();
    }

    private void initialize() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        fileEditorPanel = new FileEditorPanel();
        
        add(fileEditorPanel, gbc);

    }

}

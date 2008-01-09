package de.unisb.cs.depend.ccs_sem.gui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import de.unisb.cs.depend.ccs_sem.exceptions.FileReadException;
import de.unisb.cs.depend.ccs_sem.exceptions.InternalSystemException;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.gui.components.GUIFrame;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;


public class GUI extends GUIFrame {
    
    private static final long serialVersionUID = 4087823123116329077L;

    public GUI() {
        super();
        addWindowListener(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.setVisible(true);
    }

    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if ("quit".equals(command)) {
            windowClosing(null);
        } else if ("openFile".equals(command)) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                try {
                    getFileEditorPanel().openFile(selectedFile);
                } catch (FileReadException e) {
                    JOptionPane.showMessageDialog(this, "File \"" + selectedFile.getAbsolutePath()
                        + "\" could not be opened:\n" + e.getMessage(),
                        "Error opening file", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if ("newFile".equals(command)) {
            getFileEditorPanel().newFile();
        } else if ("process".equals(command)) {
            String text = getFileEditorPanel().getCurrentlyOpenedPanel().getText();
            try {
                List<Token> tokens = new CCSLexer().lex(text);
                Program program = new CCSParser().parse(tokens);
                List<Transition> trans = program.evaluate();
            } catch (LexException e) {
                throw new InternalSystemException(e);
            } catch (ParseException e) {
                throw new InternalSystemException(e);
            }
            
        } else {
            throw new InternalSystemException("Unknown action command: " + command);
        }
    }

    public void windowActivated(WindowEvent e) {
        // nothing yet
    }

    public void windowClosed(WindowEvent e) {
        // nothing yet
    }

    public void windowClosing(WindowEvent e) {
        // TODO check for unsaved changes, etc.
        System.exit(0);
    }

    public void windowDeactivated(WindowEvent e) {
        // nothing yet
    }

    public void windowDeiconified(WindowEvent e) {
        // nothing yet
    }

    public void windowIconified(WindowEvent e) {
        // nothing yet
    }

    public void windowOpened(WindowEvent e) {
        // nothing yet
    }

}

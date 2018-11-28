/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 *
 * @author vot2
 */
public class TextEditDemo extends JPanel implements ActionListener {
    JTextField textField;
    JTextArea textArea;
    JTextArea displayArea;
 
    public TextEditDemo() {
        super(new GridBagLayout());
        GridBagLayout gridbag = (GridBagLayout)getLayout();
        GridBagConstraints c = new GridBagConstraints();
 
        JButton button = new JButton("Clear");
        button.addActionListener(this);
 
        textField = new JTextField(20);
        textField.addActionListener(new MyTextActionListener());
        textField.getDocument().addDocumentListener(new MyDocumentListener());
        textField.getDocument().putProperty("name", "Text Field");
 
        textArea = new JTextArea();
        textArea.getDocument().addDocumentListener(new MyDocumentListener());
        textArea.getDocument().putProperty("name", "Text Area");
 
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(200, 75));
 
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane displayScrollPane = new JScrollPane(displayArea);
        displayScrollPane.setPreferredSize(new Dimension(200, 75));
 
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(textField, c);
        add(textField);
 
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(scrollPane, c);
        add(scrollPane);
 
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(displayScrollPane, c);
        add(displayScrollPane);
 
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.0;
        c.gridheight = 1;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(button, c);
        add(button);
 
        setPreferredSize(new Dimension(450, 250));
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    }
 
    class MyDocumentListener implements DocumentListener {
        final String newline = "\n";
 
        public void insertUpdate(DocumentEvent e) {
            updateLog(e, "inserted into");
        }
        public void removeUpdate(DocumentEvent e) {
            updateLog(e, "removed from");
        }
        public void changedUpdate(DocumentEvent e) {
            //Plain text components don't fire these events.
        }
 
        public void updateLog(DocumentEvent e, String action) {
            Document doc = e.getDocument();
            int changeLength = e.getLength();
            displayArea.append(
                changeLength + " character"
              + ((changeLength == 1) ? " " : "s ")
              + action + " " + doc.getProperty("name") + "."
              + newline
              + "  Text length = " + doc.getLength() + newline);
            displayArea.setCaretPosition(displayArea.getDocument().getLength());
        }
    }
 
    class MyTextActionListener implements ActionListener {
        /** Handle the text field Return. */
        public void actionPerformed(ActionEvent e) {
            int selStart = textArea.getSelectionStart();
            int selEnd = textArea.getSelectionEnd();
 
            textArea.replaceRange(textField.getText(),
                                  selStart, selEnd);
            textField.selectAll();
        }
    }
 
    /** Handle button click. */
    public void actionPerformed(ActionEvent e) {
        displayArea.setText("");
        textField.requestFocus();
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("DocumentEventDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        JComponent newContentPane = new TextEditDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
/*0
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run.gui;

import simulator.nondelay.gillespie.DM;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import run.gui.checkablecombobox.CheckableItem;
import run.gui.checkablecombobox.CheckedComboBox;
import run.gui.separatorcombobox.SeparatorComboBoxRenderer;
import sbml.SBMLConverter;
import simulator.IAlgorithm;
import simulator.delay.delayed_gillespie.ModifiedDelayedDM;
import simulator.delay.delayed_mnrm.DelayedMNRM;
import simulator.delay.delayed_rssa.ModifiedDelayedRSSA;
import simulator.nondelay.gillespie.cr.SSA_CR;
import simulator.nondelay.gillespie.sorting.SDM;
import simulator.nondelay.gillespie.tree_search.TreeSSA;
import simulator.nondelay.nrm.NRM;
import simulator.nondelay.pdm.PDM;
import simulator.nondelay.pdm.cr.PSSA_CR;
import simulator.nondelay.pdm.sorting.SPDM;
import simulator.nondelay.prssa.PRSSA;
import simulator.nondelay.rssa.RSSA;
import simulator.nondelay.rssa.cr.RSSA_CR;
import simulator.nondelay.rssa.lookup.RSSA_LookupSearch;
import simulator.nondelay.rssa.tree_search.RSSA_BinarySearch;

/**
 * GUISim: GUI interface for RSSALib
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class GUISim extends javax.swing.JFrame {

    private JFileChooser fileChooser;
    private File openedFile;
    private String unknownFile = "Model";

    private Document document = null;
    private String editedDoc = "Edited";

    private String findingInfo = null;
    private int startFindingIndex = 0;

    //undo/redo helpers
    protected UndoAction undoAction;
    protected RedoAction redoAction;
    protected UndoManager undoManager = new UndoManager();

    private IAlgorithm simulator = null;
    private Hashtable<String, Vector<Double>> simOutput = null;

    private CheckedComboBox checkedcombox;
    private Vector<CheckableItem> m = new Vector<>();

    private Object[] nondelayAlgorithmNames = new Object[]{
        "RSSA", "RSSA-Binary", "RSSA-CR", "RSSA-Lookup", "PRSSA",
        new JSeparator(JSeparator.HORIZONTAL),
        "DM", "SDM", "DM-Binary", "DM-CR",
        new JSeparator(JSeparator.HORIZONTAL),
        "NRM",
        new JSeparator(JSeparator.HORIZONTAL),
        "PDM", "SPDM", "PDM-CR"};
    private DefaultComboBoxModel nondelayModel = new DefaultComboBoxModel(nondelayAlgorithmNames);

    private Object[] delayAlgorithmNames = new Object[]{"DRSSA", "DSSA", "DNRM"};
    private DefaultComboBoxModel delayModel = new DefaultComboBoxModel(delayAlgorithmNames);

    private XYSeriesCollection dataCollection = new XYSeriesCollection();
    private JFreeChart chart;

    //action for text pane
    private HashMap<Object, Action> actions;

    /**
     * Creates new form VisualSim
     */
    public GUISim() {
        initComponents();

        createActions();

        addBinding();

        loadAlgorithmGroup();

//        dropboxAlgorithm.setRenderer(new SeparatorComboBoxRenderer());
//            dropboxAlgorithm.addActionListener(new SeparatorComboBoxListener(dropboxAlgorithm));
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        chart = ChartFactory.createXYLineChart("RSSA", "time", "#Molecules", dataCollection,
                PlotOrientation.VERTICAL, true, true, false);
//        XYPlot plot = chart.getXYPlot();
//        ValueAxis yAxis = plot.getRangeAxis();
//        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());   

        displayDynamics.add(new ChartPanel(chart), BorderLayout.CENTER);

        txtDisplayText.requestFocusInWindow();
        document = txtDisplayText.getDocument();
        registerDocument(document);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        displayDynamics = new javax.swing.JPanel();
        btnRun = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDisplayText = new javax.swing.JEditorPane();
        lblModelName = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        btnLoad = new javax.swing.JButton(new ImageIcon("./figs/open.png"));
        btnSave = new javax.swing.JButton(new ImageIcon("./figs/save.png"));
        jLabel3 = new javax.swing.JLabel();
        dropboxAlgorithm = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        txtSimulationTime = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtLogInterval = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        btnSpeciesDynamics = new javax.swing.JButton();
        dropboxPane = new javax.swing.JPanel();
        chkDelay = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        mnuLoad = new javax.swing.JMenuItem();
        mnuSave = new javax.swing.JMenuItem();
        mnuSaveAs = new javax.swing.JMenuItem();
        jSeparator = new javax.swing.JPopupMenu.Separator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mnuClear = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuExit = new javax.swing.JMenuItem();
        mnuEdit = new javax.swing.JMenu();
        mnuCut = new javax.swing.JMenuItem();
        mnuCopy = new javax.swing.JMenuItem();
        mnuPaste = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mnuCopyAll = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mnuHelp = new javax.swing.JMenu();
        mnuAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("RSSALib");
        setResizable(false);

        displayDynamics.setBackground(java.awt.Color.white);
        displayDynamics.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        displayDynamics.setForeground(java.awt.Color.white);
        displayDynamics.setAutoscrolls(true);
        displayDynamics.setLayout(new java.awt.BorderLayout());

        btnRun.setText("Run simulation");
        btnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunActionPerformed(evt);
            }
        });

        txtDisplayText.setName("txtDisplayText"); // NOI18N
        jScrollPane1.setViewportView(txtDisplayText);

        lblModelName.setText("[Model]");

        jToolBar1.setRollover(true);
        jToolBar1.setToolTipText("");

        btnLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/figs/open.png"))); // NOI18N
        btnLoad.setToolTipText("Load model");
        btnLoad.setFocusable(false);
        btnLoad.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLoad.setName("btnLoad"); // NOI18N
        btnLoad.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadActionPerformed(evt);
            }
        });
        jToolBar1.add(btnLoad);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/figs/save.png"))); // NOI18N
        btnSave.setToolTipText("Save model");
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setName("btnSave"); // NOI18N
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        jToolBar1.add(btnSave);

        jLabel3.setText("Algorithm");

        dropboxAlgorithm.setRenderer(new SeparatorComboBoxRenderer());
        dropboxAlgorithm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dropboxAlgorithmActionPerformed(evt);
            }
        });

        jLabel1.setText("Simulation time");

        jLabel4.setText("Log interval");

        jLabel5.setText("Species ");

        btnSpeciesDynamics.setText("Display");
        btnSpeciesDynamics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSpeciesDynamicsActionPerformed(evt);
            }
        });

        dropboxPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        dropboxPane.setLayout(new java.awt.BorderLayout());

        chkDelay.setText("Delay");
        chkDelay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDelayActionPerformed(evt);
            }
        });

        menuFile.setText("File");

        mnuLoad.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        mnuLoad.setForeground(java.awt.Color.black);
        mnuLoad.setMnemonic(KeyEvent.VK_L);
        mnuLoad.setText("Load model");
        mnuLoad.setToolTipText("");
        mnuLoad.setName("mnuLoad"); // NOI18N
        mnuLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLoadActionPerformed(evt);
            }
        });
        menuFile.add(mnuLoad);

        mnuSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        mnuSave.setForeground(java.awt.Color.black);
        mnuSave.setMnemonic(KeyEvent.VK_S);
        mnuSave.setText("Save");
        mnuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveActionPerformed(evt);
            }
        });
        menuFile.add(mnuSave);

        mnuSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        mnuSaveAs.setForeground(java.awt.Color.black);
        mnuSaveAs.setMnemonic(KeyEvent.VK_M);
        mnuSaveAs.setText("Save model as");
        mnuSaveAs.setName("mnuSaveAs"); // NOI18N
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(mnuSaveAs);
        menuFile.add(jSeparator);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setForeground(new java.awt.Color(0, 0, 0));
        jMenuItem2.setMnemonic(KeyEvent.VK_B);
        jMenuItem2.setText("SBML converter");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLoadandConvertSBML(evt);
            }
        });
        menuFile.add(jMenuItem2);
        menuFile.add(jSeparator4);

        mnuClear.setForeground(java.awt.Color.black);
        mnuClear.setText("Clear");
        mnuClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuClearActionPerformed(evt);
            }
        });
        menuFile.add(mnuClear);
        menuFile.add(jSeparator1);

        mnuExit.setForeground(java.awt.Color.black);
        mnuExit.setText("Exit");
        mnuExit.setName("mnuExit"); // NOI18N
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        menuFile.add(mnuExit);

        jMenuBar1.add(menuFile);

        mnuEdit.setText("Edit");

        mnuCut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        mnuCut.setForeground(java.awt.Color.black);
        mnuCut.setMnemonic(KeyEvent.VK_X);
        mnuCut.setText("Cut");
        mnuEdit.add(mnuCut);

        mnuCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        mnuCopy.setForeground(java.awt.Color.black);
        mnuCopy.setMnemonic(KeyEvent.VK_C);
        mnuCopy.setText("Copy");
        mnuEdit.add(mnuCopy);

        mnuPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        mnuPaste.setForeground(java.awt.Color.black);
        mnuPaste.setMnemonic(KeyEvent.VK_V);
        mnuPaste.setText("Paste");
        mnuEdit.add(mnuPaste);
        mnuEdit.add(jSeparator2);

        mnuCopyAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        mnuCopyAll.setForeground(java.awt.Color.black);
        mnuCopyAll.setMnemonic(KeyEvent.VK_A);
        mnuCopyAll.setText("Copy all");
        mnuEdit.add(mnuCopyAll);
        mnuEdit.add(jSeparator3);

        jMenuBar1.add(mnuEdit);

        mnuHelp.setText("Help");

        mnuAbout.setForeground(java.awt.Color.black);
        mnuAbout.setText("About");
        mnuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuAbout);

        jMenuBar1.add(mnuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblModelName)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 124, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dropboxAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(chkDelay, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtLogInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtSimulationTime, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(101, 101, 101))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(250, 250, 250)
                                .addComponent(btnRun))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(85, 85, 85)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(dropboxPane, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnSpeciesDynamics, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(displayDynamics, javax.swing.GroupLayout.PREFERRED_SIZE, 524, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblModelName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 741, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(58, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chkDelay, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dropboxAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSimulationTime, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtLogInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addComponent(btnRun)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnSpeciesDynamics, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(dropboxPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(displayDynamics, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
        // TODO add your handling code here:     
        if (txtDisplayText.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Please load the model!", "Model Missing", JOptionPane.ERROR_MESSAGE);
            txtDisplayText.requestFocusInWindow();
            return;
        }
        
        boolean isEditedDoc = (boolean) document.getProperty(editedDoc);        
        if (isEditedDoc) {
            int response = JOptionPane.showConfirmDialog(this, "Do you want to save your model before runing simulation?", "Confirm",
                                                         JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                try {
                    doSaveFile();                    
                } catch (Exception ex) {
                    Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (response == JOptionPane.NO_OPTION) {
                if(openedFile == null){
                    JOptionPane.showMessageDialog(this, "The model must be saved before running simulation!", "Saving Model ", JOptionPane.ERROR_MESSAGE);
                    txtDisplayText.requestFocusInWindow();
                    return;
                }                
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(openedFile));

                    txtDisplayText.read(reader, null);

                    document = txtDisplayText.getDocument();
                    registerDocument(document);

                } catch (IOException ex) {
                    Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (response == JOptionPane.CLOSED_OPTION) {
                txtDisplayText.requestFocusInWindow();
                return;
            }
        }

        if (txtSimulationTime.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Simulation time must be set!", "Simulation Time Missing", JOptionPane.ERROR_MESSAGE);
            txtSimulationTime.requestFocusInWindow();
            return;

        }

        if (txtLogInterval.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Logging time interval must be set!", "Logging Time Missing", JOptionPane.ERROR_MESSAGE);
            txtLogInterval.requestFocusInWindow();
            return;
        }

        if(openedFile == null){
            JOptionPane.showMessageDialog(this, "The model must be saved before running simulation!", "Model Missing", JOptionPane.ERROR_MESSAGE);
            txtDisplayText.requestFocusInWindow();
            return;
        }               
        
        try {            
            simulator = null;
            String algorithmName = (String) dropboxAlgorithm.getSelectedItem();
            String trackingFile = null;
            double simTime = Double.parseDouble(txtSimulationTime.getText().trim());
            double logInterval = Double.parseDouble(txtLogInterval.getText().trim());

            if (algorithmName.equals("RSSA")) {
                trackingFile = "RSSA_" + openedFile.getName();
                simulator = new RSSA();
            } else if (algorithmName.equals("RSSA-Binary")) {
                trackingFile = "RSSA_Binary" + openedFile.getName();
                simulator = new RSSA_BinarySearch();
            } else if (algorithmName.equals("RSSA-CR")) {
                trackingFile = "RSSA_CR" + openedFile.getName();
                simulator = new RSSA_CR();
            } else if (algorithmName.equals("RSSA-Lookup")) {
                trackingFile = "RSSA_Lookup" + openedFile.getName();
                simulator = new RSSA_LookupSearch();
            } else if (algorithmName.equals("PRSSA")) {
                trackingFile = "PRSSA" + openedFile.getName();
                simulator = new PRSSA();
            } else if (algorithmName.equals("DM")) {
                trackingFile = "DM_" + openedFile.getName();
                simulator = new DM();
            } else if (algorithmName.equals("SDM")) {
                trackingFile = "SDM_" + openedFile.getName();
                simulator = new SDM();
            } else if (algorithmName.equals("DM-Binary")) {
                trackingFile = "DM_Binary_" + openedFile.getName();
                simulator = new TreeSSA();
            } else if (algorithmName.equals("DM-CR")) {
                trackingFile = "DM_CR_" + openedFile.getName();
                simulator = new SSA_CR();
            } else if (algorithmName.equals("NRM")) {
                trackingFile = "NRM_" + openedFile.getName();
                simulator = new NRM();
            } else if (algorithmName.equals("PDM")) {
                trackingFile = "PDM_" + openedFile.getName();
                simulator = new PDM();
            } else if (algorithmName.equals("SPDM")) {
                trackingFile = "SPDM_" + openedFile.getName();
                simulator = new SPDM();
            } else if (algorithmName.equals("PDM-CR")) {
                trackingFile = "PDM_CR_" + openedFile.getName();
                simulator = new PSSA_CR();
            } else if (algorithmName.equals("DRSSA")) {
                trackingFile = "DRSSA_" + openedFile.getName();
                simulator = new ModifiedDelayedRSSA();
            } else if (algorithmName.equals("DSSA")) {
                trackingFile = "DSSA_" + openedFile.getName();
                simulator = new ModifiedDelayedDM();
            } else if (algorithmName.equals("DNRM")) {
                trackingFile = "DNRM_" + openedFile.getName();
                simulator = new DelayedMNRM();
            } else {
                JOptionPane.showMessageDialog(null, "The selecetd algorithm has not been supported yet!", "Simulator error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (simulator != null) {
                simulator.loadModel(openedFile.getAbsolutePath());
                simOutput = simulator.runSim(simTime, logInterval, true, trackingFile);

                m.clear();
                for (Enumeration<String> names = simOutput.keys(); names.hasMoreElements();) {
                    String name = names.nextElement();

                    if (!name.equals("t")) {
                        m.add(new CheckableItem(name, true));
                    }
                }
                checkedcombox = new CheckedComboBox<>(new DefaultComboBoxModel<>(m));
                dropboxPane.add(checkedcombox, BorderLayout.CENTER);

                JOptionPane.showMessageDialog(null, "Simulation Complete!");

                chart.setTitle(algorithmName);
                dataCollection.removeAllSeries();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.toString(), "Simulator error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_btnRunActionPerformed

    private void btnLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadActionPerformed
        try {
            // TODO add your handling code here:
            doLoadFile();
        } catch (Exception ex) {
            Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnLoadActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            // TODO add your handling code here:
            doSaveFileAs();
        } catch (Exception ex) {
            Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void mnuLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLoadActionPerformed
        // TODO add your handling code here:
        try {
            // TODO add your handling code here:
            doLoadFile();
        } catch (Exception ex) {
            Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_mnuLoadActionPerformed

    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveAsActionPerformed
        // TODO add your handling code here:
        try {
            // TODO add your handling code here:
            doSaveFileAs();
        } catch (Exception ex) {
            Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_mnuSaveAsActionPerformed

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExitActionPerformed
        // TODO add your handling code here:
        if(document != null){
            boolean isEditted = (boolean) document.getProperty(editedDoc);
            if (isEditted) {
                int response = JOptionPane.showConfirmDialog(this, "Do you want to save your model before closing?", "Confirm",
                                                             JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CLOSED_OPTION) {
                    txtDisplayText.requestFocusInWindow();
                    return;
                }

                if (response == JOptionPane.YES_OPTION) {
                    try {
                        doSaveFileAs();
                    } catch (Exception ex) {
                        Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        this.setVisible(false);
        this.dispose();
        System.exit(0);
    }//GEN-LAST:event_mnuExitActionPerformed

    private void btnSpeciesDynamicsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSpeciesDynamicsActionPerformed
        // TODO add your handling code here:  
        dataCollection.removeAllSeries();

        Vector<Double> timeData = simOutput.get("t");
        Vector<Double> speciesData;

        if (timeData.isEmpty()) {
            return;
        }

//        String mess = "Selected species: ";
        for (CheckableItem x : m) {
            if (x.isSelected()) {
                String speciesName = x.text;
//                mess += speciesName + " ";

                speciesData = simOutput.get(speciesName);
                XYSeries speciesSeries = new XYSeries(speciesName);

                for (int i = 0; i < timeData.size(); i++) {
                    speciesSeries.add(timeData.get(i), speciesData.get(i));
                }
                dataCollection.addSeries(speciesSeries);
            }
        }

//        XYPlot plot = chart.getXYPlot();
//        ValueAxis yAxis = plot.getRangeAxis();
//        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); 

    }//GEN-LAST:event_btnSpeciesDynamicsActionPerformed

    private void mnuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAboutActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(this, "<html>RSSALis - A simulation package for stochastic simulation of biochemical reaction.<br>Developed by Vo Hong Thanh</html>");
    }//GEN-LAST:event_mnuAboutActionPerformed

    private void chkDelayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDelayActionPerformed
        // TODO add your handling code here:
        loadAlgorithmGroup();
        displayAlgorithm();
    }//GEN-LAST:event_chkDelayActionPerformed

    private void mnuClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuClearActionPerformed
        // TODO add your handling code here:
        txtDisplayText.setText("");
        lblModelName.setText("[Model]");
        document = null;
        findingInfo = null;
        startFindingIndex = 0;
    }//GEN-LAST:event_mnuClearActionPerformed

    private void mnuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveActionPerformed
        try {
            // TODO add your handling code here:
            doSaveFile();
        } catch (Exception ex) {
            Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_mnuSaveActionPerformed

    private void dropboxAlgorithmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropboxAlgorithmActionPerformed
        // TODO add your handling code here:
        displayAlgorithm();
    }//GEN-LAST:event_dropboxAlgorithmActionPerformed

    private void mnuLoadandConvertSBML(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLoadandConvertSBML
        // TODO add your handling code here:
        fileChooser.setDialogTitle("Load SBML model");
        fileChooser.setFileFilter(new ModelFileFilter(".xml", "SBML model file"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedSBML = fileChooser.getSelectedFile();
            try
            {
                String convertedFile = SBMLConverter.convert(selectedSBML.getName());
                
                openedFile = new File(convertedFile);                        
                lblModelName.setText(convertedFile);
                BufferedReader reader = new BufferedReader(new FileReader(openedFile));

                txtDisplayText.read(reader, null);
                document = txtDisplayText.getDocument();
                registerDocument(document);

                reader.close();

                findingInfo = null;
                startFindingIndex = 0;
            }catch(Exception ex)
            {
                //Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);                
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error reading SBML", JOptionPane.ERROR_MESSAGE);
                txtSimulationTime.requestFocusInWindow();
            }
        }
    }//GEN-LAST:event_mnuLoadandConvertSBML

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUISim.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUISim.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUISim.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUISim.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                GUISim display = new GUISim();

                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                display.setLocation(dim.width / 2 - display.getSize().width / 2, dim.height / 2 - display.getSize().height / 2);

                display.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLoad;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSpeciesDynamics;
    private javax.swing.JCheckBox chkDelay;
    private javax.swing.JPanel displayDynamics;
    private javax.swing.JComboBox<String> dropboxAlgorithm;
    private javax.swing.JPanel dropboxPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblModelName;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem mnuAbout;
    private javax.swing.JMenuItem mnuClear;
    private javax.swing.JMenuItem mnuCopy;
    private javax.swing.JMenuItem mnuCopyAll;
    private javax.swing.JMenuItem mnuCut;
    private javax.swing.JMenu mnuEdit;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuLoad;
    private javax.swing.JMenuItem mnuPaste;
    private javax.swing.JMenuItem mnuSave;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JEditorPane txtDisplayText;
    private javax.swing.JTextField txtLogInterval;
    private javax.swing.JTextField txtSimulationTime;
    // End of variables declaration//GEN-END:variables

    private void createActions() {
        //Get actions from the default editor kit 
        actions = new HashMap<Object, Action>();
        Action[] actionsArray = txtDisplayText.getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
    }

    private void addBinding() {
        //stick default actions from editor kit to the menu.
        mnuCut.addActionListener(actions.get(DefaultEditorKit.cutAction));
        mnuCopy.addActionListener(actions.get(DefaultEditorKit.copyAction));
        mnuPaste.addActionListener(actions.get(DefaultEditorKit.pasteAction));
        mnuCopyAll.addActionListener(actions.get(DefaultEditorKit.selectAllAction));
        
        //Undo and redo are actions of our own creation.
        undoAction = new UndoAction();
        mnuEdit.add(undoAction);

        redoAction = new RedoAction();
        mnuEdit.add(redoAction);

        //put key map to text display
        InputMap inputMap = txtDisplayText.getInputMap();
        //load model: Ctrl + L
        KeyStroke keyCtrL = KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK);
        inputMap.put(keyCtrL, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // TODO add your handling code here:
                try {
                    // TODO add your handling code here:
                    doLoadFile();
                } catch (Exception ex) {
                    Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        //Save model: Ctrl + S
        KeyStroke keyCtrS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
        inputMap.put(keyCtrS, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // TODO add your handling code here:
                try {
                    // TODO add your handling code here:
                    doSaveFile();
                } catch (Exception ex) {
                    Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        //Save model as: Ctrl + M
        KeyStroke keyCtrM = KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK);
        inputMap.put(keyCtrM, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // TODO add your handling code here:
                try {
                    // TODO add your handling code here:
                    doSaveFileAs();
                } catch (Exception ex) {
                    Logger.getLogger(GUISim.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        //Find: Ctrl + F
        KeyStroke keyCtrF = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap.put(keyCtrF, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // TODO add your handling code here:
                String newFindInfo = JOptionPane.showInputDialog(null, "Find?");
                if (newFindInfo == null) {
                    return;
                }

                if ((findingInfo == null) || (findingInfo != null && !findingInfo.equals(newFindInfo))) {
                    findingInfo = newFindInfo;
                    startFindingIndex = 0;
                }
                findText();
            }
        });

        //Find: F3
        KeyStroke keyF3 = KeyStroke.getKeyStroke(("F3"));
        inputMap.put(keyF3, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // TODO add your handling code here:
                if (findingInfo != null) {
                    findText();
                }
            }
        });

        //Undo: Ctrl + U
        KeyStroke keyCtrU = KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK);
        inputMap.put(keyCtrU, undoAction);

        //Redo: Ctrl + R
        KeyStroke keyCtrR = KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK);
        inputMap.put(keyCtrR, redoAction);

        //Cut: Ctrl + X
        KeyStroke keyCtrX = KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK);
        inputMap.put(keyCtrX, actions.get(DefaultEditorKit.cutAction));

        //Copy: Ctrl + C
        KeyStroke keyCtrC = KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK);
        inputMap.put(keyCtrC, actions.get(DefaultEditorKit.copyAction));

        //Paste: Ctrl + V
        KeyStroke keyCtrV = KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK);
        inputMap.put(keyCtrV, actions.get(DefaultEditorKit.pasteAction));

        //Copy all: Ctrl + A
        KeyStroke keyCtrA = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);
        inputMap.put(keyCtrA, actions.get(DefaultEditorKit.pasteAction));
    }

    private void loadAlgorithmGroup() {
        if (!chkDelay.isSelected()) {
            dropboxAlgorithm.setModel(nondelayModel);
        } else {
            dropboxAlgorithm.setModel(delayModel);
        }
    }
    
    private void displayAlgorithm() {
        if (!chkDelay.isSelected()) {
            chart.setTitle((String)dropboxAlgorithm.getSelectedItem());
        } else {
            chart.setTitle((String)dropboxAlgorithm.getSelectedItem());
        }
    }

    private void doLoadFile() throws Exception {
        fileChooser.setDialogTitle("Load model");
        fileChooser.setFileFilter(new ModelFileFilter(".txt", "Reaction model file"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            openedFile = fileChooser.getSelectedFile();

            lblModelName.setText(openedFile.getName());

            BufferedReader reader = new BufferedReader(new FileReader(openedFile));

            txtDisplayText.read(reader, null);
            document = txtDisplayText.getDocument();
            registerDocument(document);

            reader.close();

            findingInfo = null;
            startFindingIndex = 0;
        }
    }

    private void doSaveFile() throws Exception{
        if (openedFile == null) {
            doSaveFileAs();
        } else {
            boolean isEditted = (boolean) document.getProperty(editedDoc);

            if (isEditted) {
                document.putProperty(editedDoc, false);
                lblModelName.setText(openedFile.getName());

                FileWriter writer;

                writer = new FileWriter(openedFile.getAbsolutePath());
                writer.write(txtDisplayText.getText());
                writer.flush();
                writer.close();

                findingInfo = null;
                startFindingIndex = 0;
            }
        }        
    }
    
    private void doSaveFileAs() throws Exception {
        if (txtDisplayText.getText().trim().equals("")) {
            return;
        }

        fileChooser.setDialogTitle("Save model");
        fileChooser.setFileFilter(new ModelFileFilter(".txt", "Reaction model file"));
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            openedFile = fileChooser.getSelectedFile();

            document.putProperty(editedDoc, false);
            lblModelName.setText(openedFile.getName());

            FileWriter writer = new FileWriter(openedFile.getAbsolutePath());
            writer.write(txtDisplayText.getText());
            writer.flush();
            writer.close();

            findingInfo = null;
            startFindingIndex = 0;
        }
    }

    private void registerDocument(Document document) {
        document.putProperty(editedDoc, false);

        document.addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
                undoAction.updateUndoState();
                redoAction.updateRedoState();
            }
        });

        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDisplayText();                
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDisplayText();   
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDisplayText();   
            }
        });
    }

    private void updateDisplayText(){
        document.putProperty(editedDoc, true);
                
        if(openedFile != null){
            lblModelName.setText(openedFile.getName() + "*");
            
        }else{
            lblModelName.setText("[" + unknownFile + "*]");            
        }
        
        if(findingInfo != null){
            findingInfo = null;
            startFindingIndex = 0;                
        }
        
    }
    
    private void findText() {
        String modelData = txtDisplayText.getText();

//        System.out.println("Inside find text with data length " + modelData.length()  );
        if (modelData.trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Model is empty");
            findingInfo = null;
            return;
        }

        int occurrenceIndex = modelData.indexOf(findingInfo, startFindingIndex);

//        System.out.println(" Starting index " + startFindingIndex);
//        System.out.println(" Found " + findingInfo + " at position " + occurrenceIndex);
        if (occurrenceIndex != -1) {
            txtDisplayText.setSelectionStart(occurrenceIndex);
            txtDisplayText.setSelectionEnd(occurrenceIndex + findingInfo.length());

            startFindingIndex = (occurrenceIndex + findingInfo.length());

            if (startFindingIndex > modelData.length()) {
                JOptionPane.showMessageDialog(this, "Finish searching!");
                startFindingIndex = 0;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Not found " + findingInfo + " in the model");
            findingInfo = null;
        }
    }   

    class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo Ctrl + U");
            
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undoManager.undo();
            } catch (CannotUndoException ex) {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState() {
            if (undoManager.canUndo()) {
                setEnabled(true);
            } else {
                setEnabled(false);

                lblModelName.setText(openedFile.getName());
                document.putProperty(editedDoc, false);
            }
            putValue(Action.NAME, "Undo");
        }
    }

    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undoManager.redo();
            } catch (CannotRedoException ex) {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
            if (undoManager.canRedo()) {
                setEnabled(true);
            } else {
                setEnabled(false);
            }
            putValue(Action.NAME, "Redo");
        }
    }
}

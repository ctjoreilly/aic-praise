/*
 * Copyright (c) 2013, SRI International
 * All rights reserved.
 * Licensed under the The BSD 3-Clause License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://opensource.org/licenses/BSD-3-Clause
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the aic-praise nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sri.ai.praise.demo;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.google.common.annotations.Beta;
import com.sri.ai.expresso.core.DefaultSymbol;
import com.sri.ai.grinder.GrinderConfiguration;
import com.sri.ai.praise.demo.model.Example;
import com.sri.ai.praise.demo.model.Example1;
import com.sri.ai.praise.demo.model.Example2;
import com.sri.ai.praise.demo.model.Example3;
import com.sri.ai.praise.demo.model.Example4;
import com.sri.ai.praise.demo.model.Example5;
import com.sri.ai.praise.demo.model.Example6;
import com.sri.ai.praise.demo.model.Example7;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import java.awt.Dimension;

import javax.swing.JTabbedPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * 
 * @author oreilly
 *
 */
@Beta
public class PRAiSEDemoApp {
	public static final int DISPLAY_PRECISION          = 6;
	public static final int DISPLAY_SCIENTIFIC_GREATER = 6;
	public static final int DISPLAY_SCIENTIFIC_AFTER   = 6; 
	public static final int DEAD_ENDS_CACHE_SIZE       = 100;
	public static final int REWRITER_CACHE_SIZE        = 100;

	JFrame frame;
	ToolBarPanel toolBar = new ToolBarPanel();
	JTabbedPane editorsTabbedPane;
	RuleEditor modelEditPanel;
	RuleEditor evidenceEditPanel;
	OptionsPanel optionsPanel;
	QueryPanel queryPanel;
	OutputPanel outputPanel;
	//
	Controller controller = null;
	//
	private JMenuItem mntmNew;
	private JMenuItem mntmOpenFile;
	private JMenuItem mntmSave;
	private JMenuItem mntmSaveAs;
	private JMenuItem mntmSaveAll;
	private JMenuItem mntmExport;
	private JMenuItem mntmExit;
	private JMenuItem mntmUndo;
	private JMenuItem mntmRedo;
	private JMenuItem mntmValidate;
	private JMenuItem mntmExecuteQuery;
	private JMenuItem mntmClearOutput;
	private JMenuItem mntmNewWindow;
	private JMenuItem mntmHideToolBar;
	private JSplitPane inputSplitPane;
	private JMenuItem mntmCut;
	private JMenuItem mntmCopy;
	private JMenuItem mntmPaste;
	private JMenuItem mntmDelete;
	private JMenuItem mntmSelectAll;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// Explicitly turn both of these off up front.
		// The LBPQueryEngine will re-enable them if necessary.
		// This improves performance by not generating trace
		// and justification formatted messages, even if no 
		// relevant appenders are registered.
		GrinderConfiguration.disableTrace();
		GrinderConfiguration.disableJustification();
		
		// Assign display defaults for numerics
		DefaultSymbol.setNumericDisplayPrecision(DISPLAY_PRECISION);
		DefaultSymbol.setDisplayScientificGreaterNIntegerPlaces(DISPLAY_SCIENTIFIC_GREATER);
		DefaultSymbol.setDisplayScientificAfterNDecimalPlaces(DISPLAY_SCIENTIFIC_AFTER);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GrinderConfiguration.setProperty(GrinderConfiguration.KEY_REWRITE_DEAD_ENDS_CACHE_MAXIMUM_SIZE, ""+DEAD_ENDS_CACHE_SIZE);
					GrinderConfiguration.setProperty(GrinderConfiguration.KEY_REWRITING_PROCESS_CACHE_MAXIMUM_SIZE, ""+REWRITER_CACHE_SIZE);
					
					String configuredLookAndFeel = GrinderConfiguration.getDemoAppDefaultLookAndFeel();
				    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				        if (configuredLookAndFeel.equals(info.getName())) {
				            UIManager.setLookAndFeel(info.getClassName());
				            break;
				        }
				    }
				} catch (Exception e) {
				    // If the configured look and feel is not available, the default system look and feel will remain in place.
				}
				
				try {
					PRAiSEDemoApp window = new PRAiSEDemoApp();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PRAiSEDemoApp() {
		initialize();
		postGUIInitialization();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				controller.exit();
			}
		});
		frame.setBounds(100, 100, 1000, 720);
		frame.setTitle("PRAiSE");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel backgroundPanel = new JPanel();
		frame.getContentPane().add(backgroundPanel, BorderLayout.CENTER);
		backgroundPanel.setLayout(new BorderLayout(0, 0));
		
		JSplitPane inputOutputSplitPane = new JSplitPane();
		inputOutputSplitPane.setResizeWeight(0.65);
		inputOutputSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		inputOutputSplitPane.setOneTouchExpandable(true);
		backgroundPanel.add(inputOutputSplitPane, BorderLayout.CENTER);
		
		JPanel inputPanel = new JPanel();
		inputPanel.setPreferredSize(new Dimension(400, 550));
		inputOutputSplitPane.setLeftComponent(inputPanel);
		inputPanel.setLayout(new BorderLayout(0, 0));
		
		inputSplitPane = new JSplitPane();
		inputSplitPane.setResizeWeight(1.0);
		inputSplitPane.setOneTouchExpandable(true);
		inputPanel.add(inputSplitPane, BorderLayout.CENTER);
		
		JPanel editorPanel = new JPanel();
		editorPanel.setPreferredSize(new Dimension(400, 370));
		inputSplitPane.setLeftComponent(editorPanel);
		editorPanel.setLayout(new BorderLayout(0, 0));
		
		editorsTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		editorsTabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (controller != null) {
					if (editorsTabbedPane.getSelectedIndex() == 0) {
						controller.setActiveEditor(modelEditPanel);
					}
					else {
						controller.setActiveEditor(evidenceEditPanel);
					}
				}
			}
		});
		editorPanel.add(editorsTabbedPane);
		
		modelEditPanel = new RuleEditor();
		editorsTabbedPane.addTab("Model", null, modelEditPanel, null);
		
		evidenceEditPanel = new RuleEditor();
		editorsTabbedPane.addTab("Evidence", null, evidenceEditPanel, null);
		
		queryPanel = new QueryPanel();
		editorPanel.add(queryPanel, BorderLayout.SOUTH);
		
		optionsPanel = new OptionsPanel();
		optionsPanel.setPreferredSize(new Dimension(200, 370));
		inputSplitPane.setRightComponent(optionsPanel);
		
		outputPanel = new OutputPanel();
		outputPanel.setPreferredSize(new Dimension(400, 40));
		inputOutputSplitPane.setRightComponent(outputPanel);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout(0, 0));
		controlPanel.add(toolBar, BorderLayout.CENTER);
		backgroundPanel.add(controlPanel, BorderLayout.NORTH);
		
		JMenuBar menuBar = new JMenuBar();
		frame.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('F');
		menuBar.add(mnFile);
		
		mntmNew = new JMenuItem("New");
		mnFile.add(mntmNew);
		
		mntmOpenFile = new JMenuItem("Open File...");
		mnFile.add(mntmOpenFile);
		
		mnFile.addSeparator();
		
		mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		mntmSaveAs = new JMenuItem("Save As...");
		mnFile.add(mntmSaveAs);
		
		mntmSaveAll = new JMenuItem("Save All");
		mnFile.add(mntmSaveAll);
		
		mnFile.addSeparator();
		
		mntmExport = new JMenuItem("Export...");
		mnFile.add(mntmExport);
		
		mnFile.addSeparator();
		
		mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		mnEdit.setMnemonic('E');
		menuBar.add(mnEdit);
		
		mntmUndo = new JMenuItem("Undo");
		mnEdit.add(mntmUndo);
		
		mntmRedo = new JMenuItem("Redo");
		mnEdit.add(mntmRedo);
		
		mnEdit.addSeparator();
		
		mntmCut = new JMenuItem("Cut");
		mnEdit.add(mntmCut);
		
		mntmCopy = new JMenuItem("Copy");
		mnEdit.add(mntmCopy);
		
		mntmPaste = new JMenuItem("Paste");
		mnEdit.add(mntmPaste);
		
		mnEdit.addSeparator();
		
		mntmDelete = new JMenuItem("Delete");
		mnEdit.add(mntmDelete);
		
		mntmSelectAll = new JMenuItem("Select All");
		mnEdit.add(mntmSelectAll);
		
		JMenu mnRun = new JMenu("Run");
		mnRun.setMnemonic('R');
		menuBar.add(mnRun);
		
		mntmValidate = new JMenuItem("Validate Input...");
		mnRun.add(mntmValidate);
		
		mntmExecuteQuery = new JMenuItem("Execute Query");
		mnRun.add(mntmExecuteQuery);
		
		mntmClearOutput = new JMenuItem("Clear Output");
		mnRun.add(mntmClearOutput);
		
		JMenu mnWindow = new JMenu("Window");
		mnWindow.setMnemonic('W');
		menuBar.add(mnWindow);
		
		mntmNewWindow = new JMenuItem("New Window");
		mnWindow.add(mntmNewWindow);
		
		mntmHideToolBar = new JMenuItem("Hide Tool Bar");
		mnWindow.add(mntmHideToolBar);
	}
	
	
	private void postGUIInitialization() {		
		// Wire up the Controller
		controller = new Controller(this);
		
		controller.setActiveEditor(modelEditPanel);
		
		outputPanel.setOptions(optionsPanel);
		
		// Ensure the options panel is hidden by default
		inputSplitPane.setDividerLocation(frame.getBounds().width);
		
		// 
		// Wire up the Action handlers.
		// New
		mntmNew.setAction(controller.getNewAction());
		toolBar.btnNew.setAction(controller.getNewAction());
		// Open File...
		mntmOpenFile.setAction(controller.getOpenFileAction());
		toolBar.btnOpen.setAction(controller.getOpenFileAction());
		// Save
		mntmSave.setAction(controller.getSaveAction());
		toolBar.btnSave.setAction(controller.getSaveAction());
		// Save As...
		mntmSaveAs.setAction(controller.getSaveAsAction());
		// Save All
		mntmSaveAll.setAction(controller.getSaveAllAction());
		toolBar.btnSaveAll.setAction(controller.getSaveAllAction());		
		// Export...
		mntmExport.setAction(controller.getExportAction());
		// Exit
		mntmExit.setAction(controller.getExitAction());
		// Undo
		mntmUndo.setAction(modelEditPanel.getUndoAction());
		toolBar.btnUndo.setAction(modelEditPanel.getUndoAction());
		// Redo
		mntmRedo.setAction(modelEditPanel.getRedoAction());
		toolBar.btnRedo.setAction(modelEditPanel.getRedoAction());
		// Cut
		mntmCut.setAction(modelEditPanel.getCutAction());
		// Copy
		mntmCopy.setAction(modelEditPanel.getCopyAction());
		// Paste
		mntmPaste.setAction(modelEditPanel.getPasteAction());
		// Delete
		mntmDelete.setAction(modelEditPanel.getDeleteAction());
		// Select All
		mntmSelectAll.setAction(modelEditPanel.getSelectAllAction());
		// Validate
		mntmValidate.setAction(controller.getValidateAction());
		toolBar.btnValidate.setAction(controller.getValidateAction());
		// Execute Query
		mntmExecuteQuery.setAction(controller.getExecuteQueryAction());
		toolBar.btnExecuteQuery.setAction(controller.getExecuteQueryAction());
		queryPanel.btnExecuteQuery.setAction(controller.getExecuteQueryAction());
		controller.getExecuteQueryAction().addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (controller.getExecuteQueryAction().isRunQueryState()) {
					queryPanel.progressBar.setEnabled(false);
					queryPanel.progressBar.setIndeterminate(false);
				}
				else {
					queryPanel.progressBar.setEnabled(true);
					queryPanel.progressBar.setIndeterminate(true);
				}	
			}
		});
		
		// Clear Output
		mntmClearOutput.setAction(controller.getClearOutputAction());
		toolBar.btnClearOutput.setAction(controller.getClearOutputAction());
		// New Window
		mntmNewWindow.setAction(controller.getNewWindowAction());
		toolBar.btnNewWindow.setAction(controller.getNewWindowAction());
		// Hide/Show Tool Bar
		mntmHideToolBar.setAction(controller.getHideToolBarAction());
		
		
		//
		// Setup the examples
		toolBar.exampleComboBox.addItem(new Example1());
		toolBar.exampleComboBox.addItem(new Example2());
		toolBar.exampleComboBox.addItem(new Example3());
		toolBar.exampleComboBox.addItem(new Example4());
		toolBar.exampleComboBox.addItem(new Example5());
		toolBar.exampleComboBox.addItem(new Example6());
		toolBar.exampleComboBox.addItem(new Example7());
		toolBar.exampleComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = toolBar.exampleComboBox.getSelectedIndex();
				if (selectedIndex >= 0) {
					controller.setExample((Example)toolBar.exampleComboBox.getItemAt(selectedIndex));
				}
			}
		});
		toolBar.exampleComboBox.setSelectedIndex(0);
	}

}

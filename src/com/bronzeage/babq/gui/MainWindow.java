// *****************************************************************************
//
// Copyright (C) 2008 Bronze Age Software Corp.
// 8B-250 Greenbank Road, Suite 219, Ottawa, ON, K2H 1E9
// All Rights Reserved.
//
// This software is the confidential and proprietary information of Bronze Age
// Software Corp. ("Confidential Information"). You shall not disclose such
// Confidential Information and shall use it only in accordance with the terms
// of the license agreement you entered into with Bronze Age Software Corp.
//
// *****************************************************************************
package com.bronzeage.babq.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.jdesktop.swingworker.SwingWorker;

import com.bronzeage.babq.common.BabqConfig;
import com.bronzeage.babq.common.BabqWarningList;
import com.bronzeage.babq.common.IBabqProgress;
import com.bronzeage.babq.processing.IBabqProcessor;

/**
 * 
 */
public class MainWindow extends JFrame {
	enum ActionType {
		CLEAR_DB, LOAD_PAPT, MERGE, LOAD_BILLING, SAVE_BILLING, VALIDATE, CHANGE_SETTINGS, EDIT_EXCEPTIONS, SAVE_COMBO_TBL, CREATE_BILLING_SHEETS, GEN_TEAM_REPORTS
	}

	private void openInExplorer(File f) throws IOException {
		// Did not work at hospital - asked to move, then copy, then opened file
		// Desktop.getDesktop().open(f);

		// This is windows specific, but should be OK
		Runtime runtime = Runtime.getRuntime();
		runtime.exec("explorer.exe \"" + f.getAbsolutePath() + "\"");

	}

	private class GuiProgressTracker implements IBabqProgress {
		public void setProgressString(final String progressString) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusValueLabel_m.setText(progressString);
				}
			});
		}
	};

	private static final long serialVersionUID = 1L;

	private JTextArea logTextArea_m;
	private JLabel statusValueLabel_m;
	private JLabel outputDirLabel_m = new JLabel("");

	final JLabel numAppointmentsLabel_m = new JLabel("0");
	final JLabel numPatientsLabel_m = new JLabel("0");
	final JLabel numProvidersLabel_m = new JLabel("0");
	final JLabel numBillingsLabel_m = new JLabel("0");
	final JLabel numNameExcLabel_m = new JLabel("0");

	private final IBabqProgress progressTracker_m;
	private final IBabqProcessor processor_m;
	private final BabqWarningList warningList_m;

	private ActionType type_m;

	public MainWindow(final IBabqProcessor processor) throws Exception {
		super("BABQ System - Version " + BabqConfig.getVersionCode()
				+ " released " + BabqConfig.getDateCode());

		System.out.println("Invoked parent constructor");

		java.net.URL imageURL = Thread.currentThread().getContextClassLoader()
				.getResource("resources/Money.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());

		warningList_m = new BabqWarningList();
		progressTracker_m = new GuiProgressTracker();
		processor_m = processor;
		processor_m.setProgressTracker(progressTracker_m);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		System.out.println("Creating leftColPanel");

		final JPanel leftColPanel = new JPanel(new GridBagLayout());

		leftColPanel.add(createTaskPanel(), new Gbc(0, 0).setInsets(4)
				.setAnchor(GridBagConstraints.NORTHWEST));

		leftColPanel.add(new JPanel(), new Gbc(0, 1).setFill(
				GridBagConstraints.VERTICAL).setWeight(0, 100));

		final JPanel summaryAndStatusPanel = new JPanel(new GridBagLayout());

		summaryAndStatusPanel.add(createSummaryPanel(), new Gbc(0, 0)
				.setInsets(4).setAnchor(GridBagConstraints.NORTHWEST).setFill(
						GridBagConstraints.NONE));

		summaryAndStatusPanel.add(createStatusPanel(), new Gbc(1, 0).setInsets(
				4).setAnchor(GridBagConstraints.NORTHWEST).setFill(
				GridBagConstraints.BOTH).setWeight(100, 100));

		System.out.println("Creating rightColPanel");
		final JPanel rightColPanel = new JPanel(new GridBagLayout());

		// rightColPanel.add(summaryAndStatusPanel,
		// new Gbc(0, 0).setAnchor(GridBagConstraints.NORTHWEST).setFill(
		// GridBagConstraints.HORIZONTAL).setWeight(100, 0));

		rightColPanel.add(createLogPanel(), new Gbc(0, 1).setInsets(4)
				.setAnchor(GridBagConstraints.NORTHWEST).setFill(
						GridBagConstraints.BOTH).setWeight(100, 100));

		System.out.println("Creating centerPanel");
		final JPanel centerPanel = new JPanel(new GridBagLayout());

		centerPanel.add(leftColPanel, new Gbc(0, 0).setAnchor(
				GridBagConstraints.NORTHWEST).setFill(
				GridBagConstraints.VERTICAL).setWeight(0, 100));
		centerPanel.add(rightColPanel, new Gbc(1, 0).setAnchor(
				GridBagConstraints.NORTHWEST).setFill(GridBagConstraints.BOTH)
				.setWeight(100, 100));

		final Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(centerPanel, BorderLayout.CENTER);
		contentPane.add(summaryAndStatusPanel, BorderLayout.SOUTH);

		setMinimumSize(new Dimension(800, 600));
		pack();

		Utils.centerWindow(this);

		System.out.println("Checking initialization");

		if (BabqConfig.getPref(BabqConfig.PREFS_INITIALIZED).equals("false")) {
			progressTracker_m
					.setProgressString("Initializing default settings");
			final InitialSettingsDialog dlg = new InitialSettingsDialog(
					MainWindow.this, "OK and Exit");
			if (dlg.showDialog()) {
				while (BabqConfig.getPref(BabqConfig.DIR_ROOT) == null) {
					System.out.println("Waiting for DIR_ROOT to be set");
					Thread.sleep(100);
				}
				processor_m.setUpDirs(warningList_m);
			}
			System.out.println("Exiting after init dialog");
			System.exit(0);
		}

		final File outputDir = new File(BabqConfig
				.getPref(BabqConfig.OUTPUT_DIR_ROOT));

		outputDirLabel_m.setText("Output Folder: "
				+ BabqConfig.getOutputPathNow().getAbsolutePath());

		final File inputDirRoot = new File(outputDir.getParentFile(),
				"inputFiles");
		final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
		final GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.MONTH, -1);
		final String dirName = fmt.format(cal.getTime());
		final File inputDirNow = new File(inputDirRoot, dirName);
		if (!inputDirNow.isDirectory()) {
			if (!inputDirNow.mkdirs()) {
				processor_m.setInitError ("Failed to create input directory "
						+ inputDirNow.getAbsolutePath());
			}
		}


		if (processor_m.getInitError() != null) {
			progressTracker_m.setProgressString(processor_m.getInitError());
			log(processor_m.getInitError());
			log("Application disabled.");

		} else {
			// Set sensible defaults for the input files
			if (BabqConfig.getPref(BabqConfig.APPT_TBL_NAME) == null) {
				BabqConfig.setPref(BabqConfig.APPT_TBL_NAME, inputDirNow
						.getAbsolutePath());
			}
			if (BabqConfig.getPref(BabqConfig.PATIENT_TBL_NAME) == null) {
				BabqConfig.setPref(BabqConfig.PATIENT_TBL_NAME, inputDirNow
						.getAbsolutePath());
			}
			if (BabqConfig.getPref(BabqConfig.PROVIDER_TBL_NAME) == null) {
				BabqConfig.setPref(BabqConfig.PROVIDER_TBL_NAME, inputDirRoot
						.getAbsolutePath());
			}
			if (BabqConfig.getPref(BabqConfig.BILLING_CODE_TBL_NAME) == null) {
				BabqConfig.setPref(BabqConfig.BILLING_CODE_TBL_NAME, inputDirNow
						.getAbsolutePath());
			}

			if (BabqConfig.getPref(BabqConfig.AUTO_CLEAR_DATA).equals("true")) {
				log("Clearing databases...");
				processor_m.clearAllTables(warningList_m);
				writeWarnings();
			}
			updateRowCounts();

			progressTracker_m.setProgressString("Initialization complete");
			log("Application started");
		}
		System.out.println("Finished MainWindown constructor");
	}

	private JPanel createLogPanel() {
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Log"));

		logTextArea_m = new JTextArea();
		logTextArea_m.setFont(GuiFont.FIXED);
		logTextArea_m.setEditable(false);
		logTextArea_m.setLineWrap(true);
		logTextArea_m.setWrapStyleWord(true);
		final JScrollPane logScrollPane = new JScrollPane(logTextArea_m);
		logScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		panel.add(logScrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createStatusPanel() {
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Status"));

		statusValueLabel_m = new JLabel("");
		statusValueLabel_m.setFont(GuiFont.BOLD);

		panel.add(statusValueLabel_m, new Gbc(0, 0).setInsets(4).setAnchor(
				GridBagConstraints.NORTHWEST));
		JPanel panel2 = new JPanel();
		panel.add(panel2, new Gbc(0, 2).setFill(GridBagConstraints.SOUTHWEST)
				.setWeight(50, 50));

		outputDirLabel_m.setFont(GuiFont.BOLD);
		panel.add(outputDirLabel_m, new Gbc(0, 1).setInsets(4).setAnchor(
				GridBagConstraints.NORTHWEST));

		JButton button = new JButton("<html>Clear<br>Database</html>");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				doAction(ActionType.CLEAR_DB);
			}
		});
		panel2.add(button, new Gbc(0, 0).setInsets(4).setAnchor(
				GridBagConstraints.SOUTHWEST));

		button = new JButton("<html>Clear<br>Log</html>");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				logTextArea_m.setText(null);
			}
		});
		panel2.add(button, new Gbc(0, 0).setInsets(4).setAnchor(
				GridBagConstraints.SOUTHWEST));

		button = new JButton("<html>Open Output<br>Folder</html>");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					openInExplorer(BabqConfig.getOutputPathNow());
					progressTracker_m
							.setProgressString("Output folder opened in explorer");

				} catch (final Exception e1) {
					warningList_m.addExc(e1);
					writeWarnings();
				}
			}

		});
		panel2.add(button, new Gbc(0, 0).setInsets(4).setAnchor(
				GridBagConstraints.SOUTHWEST));

		button = new JButton("<html>Open Updated<br>Folder</html>");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					openInExplorer(new File(BabqConfig
							.getPref(BabqConfig.SUMMARY_FILE_NAME))
							.getParentFile());
					progressTracker_m
							.setProgressString("Updated folder opened in explorer");

				} catch (final Exception e1) {
					warningList_m.addExc(e1);
					writeWarnings();
				}
			}
		});
		panel2.add(button, new Gbc(0, 0).setInsets(4).setAnchor(
				GridBagConstraints.SOUTHWEST));

		button = new JButton("<html>Help<br>Website</html>");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					Desktop.getDesktop().browse(BabqConfig.getHelpUri());
					progressTracker_m
							.setProgressString("Help file opened in browser");

				} catch (final Exception e1) {
					warningList_m.addExc(e1);
					writeWarnings();
				}
			}
		});
		panel2.add(button, new Gbc(0, 0).setInsets(4).setAnchor(
				GridBagConstraints.SOUTHWEST));

		return panel;
	}

	private JPanel createSummaryPanel() {
		final JLabel tableHeading = new JLabel("<html><u>Table</u></html>");
		tableHeading.setFont(GuiFont.BOLD);

		final JLabel numRecordsHeading = new JLabel("<html><u>Count</u></html>");
		numRecordsHeading.setFont(GuiFont.BOLD);

		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Summary"));

		panel.add(tableHeading, new Gbc(0, 0).setInsets(4).setAnchor(
				GridBagConstraints.EAST));
		panel.add(new JLabel("Appointments"), new Gbc(0, 1).setInsets(4)
				.setAnchor(GridBagConstraints.EAST));
		panel.add(new JLabel("Patients"), new Gbc(0, 2).setInsets(4).setAnchor(
				GridBagConstraints.EAST));
		panel.add(new JLabel("Providers"), new Gbc(0, 3).setInsets(4)
				.setAnchor(GridBagConstraints.EAST));
		panel.add(new JLabel("Billings"), new Gbc(0, 4).setInsets(4).setAnchor(
				GridBagConstraints.EAST));
		panel.add(new JLabel("Name Excep"), new Gbc(0, 5).setInsets(4)
				.setAnchor(GridBagConstraints.EAST));

		panel.add(numRecordsHeading, new Gbc(1, 0).setInsets(4).setAnchor(
				GridBagConstraints.WEST).setInsets(0, 0, 0, 40));
		panel.add(numAppointmentsLabel_m, new Gbc(1, 1).setInsets(4).setAnchor(
				GridBagConstraints.WEST));
		panel.add(numPatientsLabel_m, new Gbc(1, 2).setInsets(4).setAnchor(
				GridBagConstraints.WEST));
		panel.add(numProvidersLabel_m, new Gbc(1, 3).setInsets(4).setAnchor(
				GridBagConstraints.WEST));
		panel.add(numBillingsLabel_m, new Gbc(1, 4).setInsets(4).setAnchor(
				GridBagConstraints.WEST));
		panel.add(numNameExcLabel_m, new Gbc(1, 5).setInsets(4).setAnchor(
				GridBagConstraints.WEST));

		return panel;
	}

	private JPanel createTaskPanel() {
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Tasks"));

		final List<JButton> buttonList = new ArrayList<JButton>();
		{
			JButton button;

			button = new JButton("Load Patient/Appt Tables");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final LoadPAPTablesDialog dlg = new LoadPAPTablesDialog(
							MainWindow.this);
					if (dlg.showDialog()) {
						doAction(ActionType.LOAD_PAPT);
					}
				}
			});
			buttonList.add(button);

			button = new JButton("Merge Tables to Create Billing Table");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					doAction(ActionType.MERGE);
				}
			});
			buttonList.add(button);

			button = new JButton("Save Billing Table & Reports");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					doAction(ActionType.SAVE_BILLING);
				}
			});
			buttonList.add(button);

			button = new JButton("Edit Name Exceptions");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final EditNameExcDialog dlg = new EditNameExcDialog(
							MainWindow.this, processor_m);
					if (dlg.showDialog()) {
						doAction(ActionType.EDIT_EXCEPTIONS);
					}
				}
			});
			buttonList.add(button);
			button = new JButton("Load Billing Table");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final LoadBillingTableDialog dlg = new LoadBillingTableDialog(
							MainWindow.this);
					if (dlg.showDialog()) {
						doAction(ActionType.LOAD_BILLING);
					}
				}
			});
			buttonList.add(button);

			button = new JButton("Create Billing Spreadsheets");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					doAction(ActionType.CREATE_BILLING_SHEETS);
				}
			});
			buttonList.add(button);

			button = new JButton("Validate Health Card Numbers");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					doAction(ActionType.VALIDATE);
				}
			});
			buttonList.add(button);

			button = new JButton("Save Combo Table");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					doAction(ActionType.SAVE_COMBO_TBL);
				}
			});
			buttonList.add(button);

			button = new JButton("Generate Team/Role Reports");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					doAction(ActionType.GEN_TEAM_REPORTS);
				}
			});
			buttonList.add(button);

			button = new JButton("Change Settings");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final ChangeSettingsDialog dlg = new ChangeSettingsDialog(
							MainWindow.this, "OK");
					if (dlg.showDialog()) {
						doAction(ActionType.CHANGE_SETTINGS);
					}
				}
			});

			buttonList.add(button);

		}

		int i = 0;
		for (final JButton b : buttonList) {
			panel.add(b, new Gbc(0, i++).setInsets(4).setAnchor(
					GridBagConstraints.NORTHWEST));
		}

		return panel;
	}

	private void doAction(final ActionType type) {
		type_m = type;
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			/*
			 * Executed in worker thread. Call publish(V) to send results to
			 * method process(V).
			 */
			@Override
			protected Void doInBackground() throws Exception {
				try {
					switch (type_m) {
					case VALIDATE:
						log("Starting validating health numbers...");
						processor_m.checkQuebecHealthNumbers("patientTbl",
								warningList_m);
						Collection<String> cardlessQcBabies = processor_m.getQuebecBabiesWithoutNumbers();
						processor_m.checkQuebecBabiesWithoutNumbers(cardlessQcBabies, warningList_m);
						break;
					case CHANGE_SETTINGS:
						log("Setting up new directories...");
						processor_m.setUpDirs(warningList_m);
						break;
					case CLEAR_DB:
						log("Clearing internal database tables...");
						processor_m.clearAllTables(warningList_m);
						break;
					case CREATE_BILLING_SHEETS:
						log("Creating new billing spreadsheets...");
						processor_m.makeBillingSheets(warningList_m);
						break;
					case EDIT_EXCEPTIONS:
						processor_m.loadNameExcTbl(warningList_m);
						break;
					case LOAD_BILLING:
						log("Loading new billing records...");
						processor_m.loadBillingTbl(warningList_m);
						break;
					case SAVE_BILLING:
						log("Saving billing records...");
						processor_m.outputBillingTbl(warningList_m);
						break;
					case LOAD_PAPT:
						log("Loading appt/patients/providers...");
						processor_m.doLoadData(warningList_m);
						break;
					case MERGE:
						log("Merging appt/patients/providers...");
						processor_m.doMakeQbBillingTbl(warningList_m);
						break;
					case SAVE_COMBO_TBL:
						log("Saving reports...");
						processor_m.saveComboTable(warningList_m);
						break;
					case GEN_TEAM_REPORTS:
						log("Generating team reports...");
						processor_m.generateTeamReports(warningList_m);
						break;
					}
				} catch (final Throwable e1) {
					warningList_m.addExc(e1);
					statusValueLabel_m.setText("Task aborted.");
				}
				return null;
			}

			/*
			 * Executed in AWT-Event thread. Allows app to do final processing.
			 */
			@Override
			protected void done() {
				updateRowCounts();
				writeWarnings();
			}

			/*
			 * Executed in AWT-Event thread. Allows app to process intermediate
			 * results.
			 */
			@Override
			protected void process(List<Void> chunks) {
			}
		};
		worker.execute();
	}

	public IBabqProgress getProgressTracker() {
		return progressTracker_m;
	}

	public BabqWarningList getWarningList() {
		return warningList_m;
	}

	/**
	 * Note: This method is AWT-Event safe.
	 */
	public void log(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String s = text;
				if (!s.endsWith(Utils.NEWLINE)) {
					s += Utils.NEWLINE;
				}

				logTextArea_m.append(s);
			}
		});
	}

	/**
	 * Note: This method is AWT-Event safe.
	 */
	private void updateRowCounts() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				numAppointmentsLabel_m.setText(""
						+ processor_m.getRowCount("apptTbl"));
				numPatientsLabel_m.setText(""
						+ processor_m.getRowCount("patientTbl"));
				numProvidersLabel_m.setText(""
						+ processor_m.getRowCount("providerTbl"));
				numBillingsLabel_m.setText(""
						+ processor_m.getRowCount("billingTbl"));
				numNameExcLabel_m.setText(""
						+ processor_m.getRowCount("nameExcTbl"));
			}
		});
	}

	/**
	 * Note: This method is AWT-Event safe.
	 */
	private void writeWarnings() {
		final int lineCount = warningList_m.getLineCount();
		if (lineCount > 0) {
			log(warningList_m.toString());
			if (warningList_m.getWarningCount() > 0) {
				final File warningFile = new File(
						BabqConfig.getOutputPathNow(), "warnings.txt");
				try {
					warningList_m.writeWarningsToFile(warningFile);
					log("** Wrote " + warningList_m.getWarningCount()
							+ " warnings to " + warningFile.getAbsolutePath());
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					log("Failed to write warning file to " + warningFile);
				}

			} else {
				log("** No warnings.");
			}
		} else {
			log("** No warnings.");
		}

		warningList_m.clear();

	}
}

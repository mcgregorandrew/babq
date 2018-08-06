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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.BackingStoreException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.bronzeage.babq.common.BabqConfig;

/**
 * 
 */
public class ChangeSettingsDialog extends BaseDialog {
	private static final long serialVersionUID = 1L;

	private final JTextField priceField_m;

	private JTextField billingSpreadSheetTemplate_m;

	private JTextField nameExceptionFile_m;

	private JCheckBox fixExpiryDates_m;

	private JCheckBox ignoreExpiryDates_m;

	private JTextField jdbcUri_m;

	private JCheckBox autoClearData_m;

	private JCheckBox ignoreFamilyMdErrors_m;

	public ChangeSettingsDialog(final MainWindow mainWindow, String okButton) {
		super(mainWindow, "Change Settings", okButton, new GridBagLayout());
		int row = 0;

		billingSpreadSheetTemplate_m = new JTextField(40);
		billingSpreadSheetTemplate_m.setText(BabqConfig
				.getPref(BabqConfig.QB_BILLING_TEMPLATE_FILE));
		addFileSetting(row++, billingSpreadSheetTemplate_m,
				JFileChooser.FILES_ONLY, "Billing Sheet Template:");

		nameExceptionFile_m = new JTextField(40);
		nameExceptionFile_m.setText(BabqConfig
				.getPref(BabqConfig.EXC_NAME_FILE_NAME));
		addFileSetting(row++, nameExceptionFile_m, JFileChooser.FILES_ONLY,
				"Name Exception File:");

		row++;
		priceField_m = new JTextField(10);
		priceField_m.setText(BabqConfig.getPref(BabqConfig.SERVICE_PRICE));
		add(new JLabel("Price Per Visit:"), new Gbc(0, row).setInsets(4)
				.setAnchor(GridBagConstraints.EAST));
		add(priceField_m, new Gbc(1, row).setInsets(4).setAnchor(
				GridBagConstraints.WEST));

		row++;
		fixExpiryDates_m = new JCheckBox();
		fixExpiryDates_m.setSelected(BabqConfig.fixExpiryDates());
		add(new JLabel("Fix Expiry Dates:"), new Gbc(0, row).setInsets(4)
				.setAnchor(GridBagConstraints.EAST));
		add(fixExpiryDates_m, new Gbc(1, row).setInsets(4).setAnchor(
				GridBagConstraints.WEST));

		row++;
		ignoreExpiryDates_m = new JCheckBox();
		ignoreExpiryDates_m.setSelected(BabqConfig.ignoreExpiryDates());
		add(new JLabel("Ignore Expiry Date Errors:"), new Gbc(0, row)
				.setInsets(4).setAnchor(GridBagConstraints.EAST));
		add(ignoreExpiryDates_m, new Gbc(1, row).setInsets(4).setAnchor(
				GridBagConstraints.WEST));

		row++;
		autoClearData_m = new JCheckBox();
		autoClearData_m.setSelected(BabqConfig.getPref(BabqConfig.AUTO_CLEAR_DATA).equals("true"));
		add(new JLabel("Clear Data Automatically:"), new Gbc(0, row).setInsets(
				4).setAnchor(GridBagConstraints.EAST));
		add(autoClearData_m, new Gbc(1, row).setInsets(4).setAnchor(
				GridBagConstraints.WEST));


		row++;
		ignoreFamilyMdErrors_m = new JCheckBox();
		ignoreFamilyMdErrors_m.setSelected(BabqConfig.getPref(BabqConfig.IGNORE_FAMILYMD_ERRORS).equals("true"));
		add(new JLabel("Ignore Family MD Errors:"), new Gbc(0, row).setInsets(
				4).setAnchor(GridBagConstraints.EAST));
		add(ignoreFamilyMdErrors_m, new Gbc(1, row).setInsets(4).setAnchor(
				GridBagConstraints.WEST));

		row++;
		jdbcUri_m = new JTextField(40);
		jdbcUri_m.setText(BabqConfig.getPref(BabqConfig.JDBC_URI));
		add(new JLabel("Database Config (req restart):"), new Gbc(0, row).setInsets(4)
				.setAnchor(GridBagConstraints.EAST));
		add(jdbcUri_m, new Gbc(1, row).setInsets(4).setAnchor(
				GridBagConstraints.WEST));

		row++;
		JButton button = new JButton("Clear Settings (exits automatically)");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					BabqConfig.clearPrefs();
					System.exit(0);
				} catch (BackingStoreException e1) {
					getMainWindow().getWarningList().addExc(e1);
				}
			}
		});
		add(button, new Gbc(1, row).setInsets(4).setAnchor(
				GridBagConstraints.WEST));
	}

	private void addFileSetting(int row, final JTextField textField,
			final int selMode, String name) {

		JButton browseButton = new JButton("Browse");
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = new File(textField.getText());
				if (!f.exists()) {
					f = f.getParentFile();
				}
				final JFileChooser chooser = new JFileChooser(f
						.getAbsolutePath());
				chooser.setFileSelectionMode(selMode);
				chooser.setMultiSelectionEnabled(false);
				chooser.setDialogTitle("Select Folder");
				final int result = chooser.showDialog(
						ChangeSettingsDialog.this, "Select");

				if (result == JFileChooser.APPROVE_OPTION) {
					final File selectedFile = chooser.getSelectedFile();
					textField.setText(selectedFile.getPath());
				}
			}
		});

		add(new JLabel(name), new Gbc(0, row).setInsets(4).setAnchor(
				GridBagConstraints.EAST));
		add(textField, new Gbc(1, row).setInsets(4).setAnchor(
				GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL)
				.setWeight(100, 0));
		add(browseButton, new Gbc(2, row).setInsets(4).setAnchor(
				GridBagConstraints.WEST));
	}

	@Override
	protected void accept() {
		BabqConfig.setPref(BabqConfig.SERVICE_PRICE, priceField_m.getText());
		BabqConfig.setPref(BabqConfig.QB_BILLING_TEMPLATE_FILE,
				billingSpreadSheetTemplate_m.getText());
		BabqConfig.setPref(BabqConfig.EXC_NAME_FILE_NAME, nameExceptionFile_m
				.getText());
		BabqConfig.setPref(BabqConfig.FIX_EXPIRY_ERRORS, ""
				+ fixExpiryDates_m.isSelected());
		BabqConfig.setPref(BabqConfig.IGNORE_EXPIRY_ERRORS, ""
				+ ignoreExpiryDates_m.isSelected());
		BabqConfig.setPref(BabqConfig.IGNORE_FAMILYMD_ERRORS, ""
				+ ignoreFamilyMdErrors_m.isSelected());
		BabqConfig.setPref(BabqConfig.JDBC_URI, jdbcUri_m.getText());
		BabqConfig.setPref(BabqConfig.AUTO_CLEAR_DATA, ""
				+ autoClearData_m.isSelected());

	}

	@Override
	protected void cancel() {
		// TODO
	}
}

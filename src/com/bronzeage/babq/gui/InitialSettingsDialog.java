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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.bronzeage.babq.common.BabqConfig;

/**
 * 
 */
public class InitialSettingsDialog extends BaseDialog {
	private static final long serialVersionUID = 1L;

	private final JTextField rootFolderName_m;

	private JTextField jdbcUri_m;

	public InitialSettingsDialog(final MainWindow mainWindow, String okButton) {
		super(mainWindow, "Change Settings", okButton, new GridBagLayout());
		int row = 0;
		rootFolderName_m = new JTextField(40);
		rootFolderName_m.setText(System.getProperty("user.home"));
		addFileSetting(row++, rootFolderName_m, JFileChooser.DIRECTORIES_ONLY,
				"Root Folder for Babq:");

		row++;
		jdbcUri_m = new JTextField(40);
		jdbcUri_m.setText("");
		add(new JLabel("Database Config :"), new Gbc(0, row).setInsets(4)
				.setAnchor(GridBagConstraints.EAST));
		add(jdbcUri_m, new Gbc(1, row).setInsets(4).setAnchor(
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
						InitialSettingsDialog.this, "Select");

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
		BabqConfig.setPref(BabqConfig.DIR_ROOT, new File(rootFolderName_m
				.getText(), "Babq").getAbsolutePath());
		if (jdbcUri_m.getText().length() > 0)
			BabqConfig.setPref(BabqConfig.JDBC_URI, jdbcUri_m.getText());
	}

	@Override
	protected void cancel() {
		// TODO
	}
}

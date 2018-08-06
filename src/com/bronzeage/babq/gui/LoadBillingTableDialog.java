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
public class LoadBillingTableDialog extends BaseDialog
{
    private static final long serialVersionUID = 1L;

    private final JTextField billingTableFileName_m;

    public LoadBillingTableDialog(final MainWindow mainWindow)
    {
        super(mainWindow, "Load Billing Table", "Load", new GridBagLayout());

        billingTableFileName_m = new JTextField(BabqConfig.getPref(BabqConfig.BILLING_TBL_FILE_NAME), 40);

        JButton outputFolderButton = new JButton("Browse");
        outputFolderButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    final JFileChooser chooser =
                        new JFileChooser(billingTableFileName_m.getText());
                    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setDialogTitle("Select Billing Table File");
                    final int result =
                        chooser.showDialog(LoadBillingTableDialog.this,
                            "Select");

                    if (result == JFileChooser.APPROVE_OPTION)
                    {
                        final File selectedFile = chooser.getSelectedFile();
                        billingTableFileName_m.setText(selectedFile.getPath());
                    }
                }
            });

        add(new JLabel("Billing Table File:"),
            new Gbc(0, 0).setInsets(4).setAnchor(GridBagConstraints.EAST));
        add(
            billingTableFileName_m,
            new Gbc(1, 0).setInsets(4).setAnchor(GridBagConstraints.WEST).setFill(
                GridBagConstraints.HORIZONTAL).setWeight(100, 0));
        add(outputFolderButton, new Gbc(2, 0).setInsets(4).setAnchor(
            GridBagConstraints.WEST));
    }

    @Override
    protected void accept()
    {
        BabqConfig.setPref(BabqConfig.BILLING_TBL_FILE_NAME,
                billingTableFileName_m.getText());
    }

    @Override
    protected void cancel()
    {
    // TODO
    }
}

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
public class LoadPAPTablesDialog extends BaseDialog
{
    private static final long serialVersionUID = 1L;

    private final JTextField patientFileName_m;
    private final JTextField appointmentFileName_m;
    private final JTextField providerFileName_m;
    private final JTextField billingCodeFileName_m;

    public LoadPAPTablesDialog(final MainWindow mainWindow)
    {
        super(mainWindow, "Load Patient/Appointment/Provider Tables", "Load",
            new GridBagLayout());

        patientFileName_m =
            new JTextField(BabqConfig.getPref(BabqConfig.PATIENT_TBL_NAME), 40);
        appointmentFileName_m =
            new JTextField(BabqConfig.getPref(BabqConfig.APPT_TBL_NAME), 40);
        providerFileName_m =
                new JTextField(BabqConfig.getPref(BabqConfig.PROVIDER_TBL_NAME), 40);
        billingCodeFileName_m =
                new JTextField(BabqConfig.getPref(BabqConfig.BILLING_CODE_TBL_NAME), 40);

        JButton patientFileButton = new JButton("Browse");
        patientFileButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    final JFileChooser chooser =
                        new JFileChooser(patientFileName_m.getText());
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setDialogTitle("Select Patient File");
                    final int result =
                        chooser.showDialog(LoadPAPTablesDialog.this, "Select");

                    if (result == JFileChooser.APPROVE_OPTION)
                    {
                        final File selectedFile = chooser.getSelectedFile();
                        patientFileName_m.setText(selectedFile.getPath());
                    }
                }
            });

        JButton appointmentFileButton = new JButton("Browse");
        appointmentFileButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    final JFileChooser chooser =
                        new JFileChooser(appointmentFileName_m.getText());
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setDialogTitle("Select Appointment File");
                    final int result =
                        chooser.showDialog(LoadPAPTablesDialog.this, "Select");

                    if (result == JFileChooser.APPROVE_OPTION)
                    {
                        final File selectedFile = chooser.getSelectedFile();
                        appointmentFileName_m.setText(selectedFile.getPath());
                    }
                }
            });

        JButton providerFileButton = new JButton("Browse");
        providerFileButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    final JFileChooser chooser =
                        new JFileChooser(providerFileName_m.getText());
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setDialogTitle("Select Provider File");
                    final int result =
                        chooser.showDialog(LoadPAPTablesDialog.this, "Select");

                    if (result == JFileChooser.APPROVE_OPTION)
                    {
                        final File selectedFile = chooser.getSelectedFile();
                        providerFileName_m.setText(selectedFile.getPath());
                    }
                }
            });

        JButton billingCodeFileButton = new JButton("Browse");
        billingCodeFileButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    final JFileChooser chooser =
                        new JFileChooser(billingCodeFileName_m.getText());
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setDialogTitle("Select Billing Code File");
                    final int result =
                        chooser.showDialog(LoadPAPTablesDialog.this, "Select");

                    if (result == JFileChooser.APPROVE_OPTION)
                    {
                        final File selectedFile = chooser.getSelectedFile();
                        billingCodeFileName_m.setText(selectedFile.getPath());
                    }
                }
            });

        int row = 0;

        add(new JLabel("Patient File:"),
            new Gbc(0, row).setInsets(4).setAnchor(GridBagConstraints.EAST));
        add(
            patientFileName_m,
            new Gbc(1, row).setInsets(4).setAnchor(GridBagConstraints.WEST).setFill(
                GridBagConstraints.HORIZONTAL).setWeight(100, 0));
        add(patientFileButton, new Gbc(2, row).setInsets(4).setAnchor(
            GridBagConstraints.WEST));
        ++row;

        add(new JLabel("Appointment File:"),
            new Gbc(0, row).setInsets(4).setAnchor(GridBagConstraints.EAST));
        add(
            appointmentFileName_m,
            new Gbc(1, row).setInsets(4).setAnchor(GridBagConstraints.WEST).setFill(
                GridBagConstraints.HORIZONTAL).setWeight(100, 0));
        add(appointmentFileButton, new Gbc(2, row).setInsets(4).setAnchor(
            GridBagConstraints.WEST));
        ++row;

        add(new JLabel("Provider File:"),
            new Gbc(0, row).setInsets(4).setAnchor(GridBagConstraints.EAST));
        add(
            providerFileName_m,
            new Gbc(1, row).setInsets(4).setAnchor(GridBagConstraints.WEST).setFill(
                GridBagConstraints.HORIZONTAL).setWeight(100, 0));
        add(providerFileButton, new Gbc(2, row).setInsets(4).setAnchor(
            GridBagConstraints.WEST));
        ++row;

        add(new JLabel("Billing Code File:"),
                new Gbc(0, row).setInsets(4).setAnchor(GridBagConstraints.EAST));
        add(billingCodeFileName_m,
        		new Gbc(1, row).setInsets(4).setAnchor(GridBagConstraints.WEST).setFill(
                    GridBagConstraints.HORIZONTAL).setWeight(100, 0));
        add(billingCodeFileButton, new Gbc(2, row).setInsets(4).setAnchor(
                GridBagConstraints.WEST));
        ++row;

    }

    @Override
    protected void accept()
    {
        BabqConfig.setPref(BabqConfig.PATIENT_TBL_NAME,
            patientFileName_m.getText());
        BabqConfig.setPref(BabqConfig.PROVIDER_TBL_NAME,
            providerFileName_m.getText());
        BabqConfig.setPref(BabqConfig.APPT_TBL_NAME,
                appointmentFileName_m.getText());
        BabqConfig.setPref(BabqConfig.BILLING_CODE_TBL_NAME,
                billingCodeFileName_m.getText());
     
    }

    @Override
    protected void cancel()
    {
    // TODO
    }
}

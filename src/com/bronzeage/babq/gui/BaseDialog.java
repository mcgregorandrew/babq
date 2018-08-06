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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * 
 */
public abstract class BaseDialog extends JPanel
{
    private static final long serialVersionUID = 1L;

    private final String name_m;
    private final String acceptButtonText_m;
    private final MainWindow mainWindow_m;

    public BaseDialog(MainWindow mainWindow, String name,
        String acceptButtonText, LayoutManager layout)
    {
        super(layout);

        name_m = name;
        acceptButtonText_m = acceptButtonText;
        mainWindow_m = mainWindow;
    }

    protected MainWindow getMainWindow()
    {
        return mainWindow_m;
    }

    protected abstract void accept();

    protected abstract void cancel();

    public boolean showDialog()
    {
        class Result
        {
            public boolean value_m = false;
        }
        final Result result = new Result();

        final JDialog dlg = new JDialog(mainWindow_m, name_m, true);

        final JButton acceptButton = new JButton(acceptButtonText_m);
        acceptButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    result.value_m = true;
                    dlg.setVisible(false);
                    accept();
                }
            });

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent e)
                {
                    result.value_m = false;
                    dlg.setVisible(false);
                    cancel();
                }
            });

        final JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(acceptButton);
        buttonPanel.add(cancelButton);

        dlg.setLayout(new GridBagLayout());
        int row = 0;
        dlg.add(this,
            new Gbc(0, row++).setAnchor(GridBagConstraints.NORTHWEST).setFill(
                GridBagConstraints.HORIZONTAL).setWeight(100, 0).setInsets(10,
                10, 4, 10));
        dlg.add(new JPanel(), // Padding - consumes extra vertical space
            new Gbc(0, row++).setAnchor(GridBagConstraints.WEST).setFill(
                GridBagConstraints.BOTH).setWeight(100, 100));
        dlg.add(buttonPanel,
            new Gbc(0, row++).setAnchor(GridBagConstraints.SOUTHWEST).setFill(
                GridBagConstraints.HORIZONTAL).setWeight(100, 0).setInsets(4));
        dlg.pack();
        Utils.centerDialog(dlg);
        dlg.setVisible(true);

        return result.value_m;
    }
}

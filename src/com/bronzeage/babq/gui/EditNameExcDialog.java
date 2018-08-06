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
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import com.bronzeage.babq.common.BabqConfig;
import com.bronzeage.babq.common.BabqUtils;
import com.bronzeage.babq.processing.IBabqProcessor;

/**
 * 
 */
public class EditNameExcDialog extends BaseDialog
{
    private static final long serialVersionUID = 1L;
	private File realExcFile_m;
	private File tempExcFile_m;
	private IBabqProcessor processor_m;

    public EditNameExcDialog(final MainWindow mainWindow, IBabqProcessor processor)
    {
        super(mainWindow, "Edit Name Exceptions", "Use Edited File",
            new GridBagLayout());

        processor_m = processor;
        JLabel para1Label =
            new JLabel(
                "<html>"
                    + "BABQ has opened the folder \"updatedFiles\" in another window.<br>"
                    + "Double-click on the file titled \"tempExcNames\" and add the<br>"
                    + "names reported in previous errors." + "</html>");

        JLabel para2Label =
            new JLabel(
                "<html>"
                    + "Once new names have been added or modified and the file has been<br>"
                    + "saved and closed in Excel, click the 'Use Edited File' to use the new<br>"
                    + "exceptions file or 'Cancel' to discard the edited file."
                    + "</html>");

        add(para1Label, new Gbc(0, 0).setInsets(4, 12).setAnchor(
            GridBagConstraints.WEST));
        add(para2Label, new Gbc(0, 1).setInsets(4, 12).setAnchor(
            GridBagConstraints.WEST));
    }
	
    private void openInExplorer(File f) throws IOException {
		// Did not work at hospital - asked to move, then copy, then opened file
		// Desktop.getDesktop().open(f);

		// This is windows specific, but should be OK
		Runtime runtime = Runtime.getRuntime();
		runtime.exec("explorer.exe \"" + f.getAbsolutePath() + "\"");

	}

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.gui.BaseDialog#showDialog()
     */
    @Override
    public boolean showDialog()
    {
        /*
         * Open the name exceptions CSV file.
         */
        try
        {
        	realExcFile_m = new File (BabqConfig.getPref(BabqConfig.EXC_NAME_FILE_NAME));
        	if (!realExcFile_m.exists())
        	{
        		if (!realExcFile_m.getParentFile().isDirectory())
        			realExcFile_m.getParentFile().mkdirs();
        		BabqConfig.createEmptyExcNameFile (realExcFile_m);
        	}
			tempExcFile_m = new File(realExcFile_m.getParentFile(), "tempExcNames.csv");
			tempExcFile_m.deleteOnExit();
			processor_m.createTempNameExcFile (realExcFile_m, tempExcFile_m);
			try {
				openInExplorer(new File(BabqConfig
						.getPref(BabqConfig.SUMMARY_FILE_NAME))
						.getParentFile());
			} catch (final Exception e1) {			
			}

        }
        catch (Exception e) 
        {
            getMainWindow().getWarningList().addExc(e);
        }

        return super.showDialog();
    }

    @Override
    protected void accept()
    {
    	try {
			BabqUtils.copyFile(tempExcFile_m, realExcFile_m);
		} catch (IOException e) {
            getMainWindow().getWarningList().addExc(e);
		}
    	tempExcFile_m.delete();
    }

    @Override
    protected void cancel()
    {
    	tempExcFile_m.delete();
    }
}

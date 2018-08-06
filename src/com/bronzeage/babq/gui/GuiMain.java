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

import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.bronzeage.babq.common.BabqConfig;
import com.bronzeage.babq.processing.BabqProcessor;
import com.bronzeage.babq.processing.BabqTestProcessor;
import com.bronzeage.babq.processing.IBabqProcessor;

/**
 * 
 */
public class GuiMain {
	public static void main(String[] args) {
		try {
			Locale.setDefault(Locale.CANADA);
			Locale l = Locale.getDefault();
			System.err.println ("Locale country is " + l.getCountry());
			
			final String lnfClassName = UIManager
					.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lnfClassName);
			JFrame.setDefaultLookAndFeelDecorated(false);
			JDialog.setDefaultLookAndFeelDecorated(false);

			System.out.println("Creating processor object");
			IBabqProcessor processor = null;

			if ((args.length > 0) && (args[0].equals("-tp"))) {
				processor = new BabqTestProcessor();
			} else {
				try {
					processor = new BabqProcessor();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if ((args.length > 0) && (args[0].equals("-c"))) 
			{
				System.out.println("Clearing preferences due to -c flag");
				BabqConfig.clearPrefs();
			}

			System.out.println("Creating MainWindown");
			// Sometimes the window would not come up
			// esp with data on network drives. This delay seems to fix that.
			Thread.sleep(2000);
			MainWindow mw = new MainWindow(processor);
			Thread.sleep(1000);
			mw.setVisible(true);
			System.out.println("Done MainWindown");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}

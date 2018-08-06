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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * 
 */
public class Utils
{
    public static final String NEWLINE;
    static
    {
        NEWLINE = System.getProperty("line.separator");
    }

    /**
     * This method centers a dialog on the screen.
     * 
     * @param dialog The dialog to be centered.
     */
    public static void centerDialog(final JDialog dialog)
    {
        final Toolkit tk = Toolkit.getDefaultToolkit();

        final Dimension screenSize = tk.getScreenSize();
        final int screenHeight = screenSize.height;
        final int screenWidth = screenSize.width;
        final Point point =
            new Point((screenWidth - dialog.getWidth()) / 2,
                (screenHeight - dialog.getHeight()) / 2);
        dialog.setLocation(point);
    }

    /**
     * This method centers a window on the screen.
     * 
     * @param window The window to be centered.
     */
    public static void centerWindow(final JFrame window)
    {
        final Toolkit tk = Toolkit.getDefaultToolkit();

        final Dimension screenSize = tk.getScreenSize();
        final int screenHeight = screenSize.height;
        final int screenWidth = screenSize.width;
        final Point point =
            new Point((screenWidth - window.getWidth()) / 2,
                (screenHeight - window.getHeight()) / 2);
        window.setLocation(point);
    }
}

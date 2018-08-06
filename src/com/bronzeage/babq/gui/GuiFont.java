// *****************************************************************************
//
// Copyright (C) 2007 Bronze Age Software Corp.
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

import java.awt.Font;

/**
 * This class provides a collection of commonly used fonts.
 */
public class GuiFont
{
    public static final Font REGULAR;
    public static final Font BOLD;
    public static final Font ITALIC;
    public static final Font SMALL;
    public static final Font SMALL_ITALIC;
    public static final Font FIXED;

    static
    {
        REGULAR = new Font("Dialog", Font.PLAIN, 12);
        BOLD = new Font("Dialog", Font.BOLD, 12);
        ITALIC = new Font("Dialog", Font.ITALIC, 12);
        SMALL = new Font("Dialog", Font.PLAIN, 10);
        SMALL_ITALIC = new Font("Dialog", Font.ITALIC, 10);
        FIXED = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    }
}

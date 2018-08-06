// *****************************************************************************
//
// Copyright (C) 2006 Bronze Age Software Corp.
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
import java.awt.Insets;

/**
 * This class augments the Gbc class, providing methods that allow the
 * constraints to be set and passed to the layout manager in a single line of
 * code.
 */
@SuppressWarnings("serial")
public class Gbc extends GridBagConstraints
{
    public Gbc(int gridx, int gridy)
    {
        this.gridx = gridx;
        this.gridy = gridy;
    }

    public Gbc setAnchor(int anchor)
    {
        this.anchor = anchor;
        return this;
    }

    public Gbc setFill(int fill)
    {
        this.fill = fill;
        return this;
    }

    public Gbc setInsets(int inset)
    {
        this.insets = new Insets(inset, inset, inset, inset);
        return this;
    }

    public Gbc setInsets(int horizontal, int vertical)
    {
        this.insets = new Insets(vertical, horizontal, vertical, horizontal);
        return this;
    }

    public Gbc setInsets(int top, int left, int bottom, int right)
    {
        this.insets = new Insets(top, left, bottom, right);
        return this;
    }

    public Gbc setIpad(int ipadx, int ipady)
    {
        this.ipadx = ipadx;
        this.ipady = ipady;
        return this;
    }

    public Gbc setSpan(int gridwidth, int gridheight)
    {
        this.gridwidth = gridwidth;
        this.gridheight = gridheight;
        return this;
    }

    public Gbc setWeight(double weightx, double weighty)
    {
        this.weightx = weightx;
        this.weighty = weighty;
        return this;
    }
}

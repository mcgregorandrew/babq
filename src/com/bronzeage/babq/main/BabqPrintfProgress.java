/**
 * 
 */
package com.bronzeage.babq.main;

import com.bronzeage.babq.common.IBabqProgress;

/**
 * @author andrew
 */
public class BabqPrintfProgress implements IBabqProgress
{
    public void setProgressString(String status)
    {
        System.out.println(status);
    }
}

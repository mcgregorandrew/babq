/**
 * 
 */
package com.bronzeage.babq.processing;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;

import com.bronzeage.babq.common.BabqWarningList;
import com.bronzeage.babq.common.IBabqProgress;

/**
 * @author andrew
 */
public class BabqTestProcessor implements IBabqProcessor
{
    private IBabqProgress progressTracker_m;
    public String warningText_m = "Test warning";
    public int numWarnings_m = 1;
    public int numProgressMsgs_m = 5;
    public String progressString_m = "Test Progress: ";

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.processing.IBabqProcessor#checkQuebecHealthNumbers(java.lang.String,
     *      com.bronzeage.babq.common.BabqWarningList)
     */
    public void checkQuebecHealthNumbers(String tableToCheck,
        BabqWarningList warningList) throws IOException, SQLException,
        ParseException
    {
        makeOutput(warningList);
    }

    private void makeOutput(BabqWarningList warningList)
    {
        for (int i = 0; i < numWarnings_m; i++)
        {
            warningList.addWarning("testFile", i, warningText_m);
        }

        for (int i = 0; i < numProgressMsgs_m; i++)
        {
            progressTracker_m.setProgressString(progressString_m + i);
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.processing.IBabqProcessor#clearAllTables()
     */
    public void clearAllTables(BabqWarningList warningList) throws SQLException
    {
        makeOutput(warningList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.processing.IBabqProcessor#doLoadData(java.io.File,
     *      java.io.File, java.io.File,
     *      com.bronzeage.babq.common.BabqWarningList)
     */
    public void doLoadData(BabqWarningList warningList) throws Exception
    {
        makeOutput(warningList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.processing.IBabqProcessor#doMakeQbBillingTbl(com.bronzeage.babq.common.BabqWarningList)
     */
    public void doMakeQbBillingTbl(BabqWarningList warningList)
        throws SQLException
    {
        makeOutput(warningList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.processing.IBabqProcessor#getRowCount(java.lang.String)
     */
    public int getRowCount(String tblName)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.processing.IBabqProcessor#loadBillingTbl(java.io.File,
     *      com.bronzeage.babq.common.BabqWarningList)
     */
    public void loadBillingTbl(BabqWarningList warningList) throws IOException,
        SQLException, ParseException
    {
        makeOutput(warningList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.processing.IBabqProcessor#makeBillingSheets(com.bronzeage.babq.common.BabqWarningList)
     */
    public void makeBillingSheets(BabqWarningList warningList)
        throws IOException, SQLException
    {
        makeOutput(warningList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.processing.IBabqProcessor#outputBillingTbl(com.bronzeage.babq.common.BabqWarningList)
     */
    public void outputBillingTbl(BabqWarningList warningList) throws Exception
    {
        makeOutput(warningList);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bronzeage.babq.processing.IBabqProcessor#setProgressTracker(com.bronzeage.babq.common.IBabqProgress)
     */
    public void setProgressTracker(IBabqProgress progressTracker)
    {
        progressTracker_m = progressTracker;
    }

    public void setUpDirs(BabqWarningList warningList)
    {
        makeOutput(warningList);

    }

    public void saveComboTable(BabqWarningList warningList_m)
    {
    // TODO Auto-generated method stub

    }

    public void createTempNameExcFile(File realExcFile, File tempExcFile)
    {
    // TODO Auto-generated method stub

    }
     
    public void loadNameExcTbl(BabqWarningList warningList) throws Exception
    {
    // TODO Auto-generated method stub

    }

	public void generateTeamReports(BabqWarningList warningList_m)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	public String getInitError() {
		return null;
	}

	public void setInitError(String string) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkQuebecBabiesWithoutNumbers(
			Collection<String> cardlessQcBabies, BabqWarningList warningList)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public Collection<String> getQuebecBabiesWithoutNumbers()
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}

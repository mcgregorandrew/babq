package com.bronzeage.babq.processing;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;

import com.bronzeage.babq.common.BabqWarningList;
import com.bronzeage.babq.common.IBabqProgress;

public interface IBabqProcessor {

	public abstract void doLoadData(BabqWarningList warningList)
			throws Exception;

	public abstract void doMakeQbBillingTbl(BabqWarningList warningList)
			throws Exception;

	public abstract void loadBillingTbl(BabqWarningList warningList)
			throws Exception;

	public abstract void checkQuebecHealthNumbers(String tableToCheck,
			BabqWarningList warningList) throws IOException, SQLException,
			ParseException;

	public abstract void makeBillingSheets(BabqWarningList warningList)
			throws Exception;

	public abstract int getRowCount(String tblName);

	public abstract void setProgressTracker(IBabqProgress progressTracker);

	public abstract void outputBillingTbl(BabqWarningList warningList)
			throws Exception;

	void clearAllTables(BabqWarningList warningList) throws SQLException;

	public abstract void setUpDirs(BabqWarningList warningList)
			throws Exception;

	public abstract void saveComboTable(BabqWarningList warningList_m)
			throws Exception;

	public abstract void createTempNameExcFile(File realExcFile,
			File tempExcFile) throws Exception;

	public abstract void loadNameExcTbl(BabqWarningList warningList)
			throws Exception;

	public abstract void generateTeamReports(BabqWarningList warningList)
			throws Exception;

	public abstract String getInitError();

	public abstract void setInitError(String string);

	void checkQuebecBabiesWithoutNumbers(Collection<String> cardlessQcBabies,
			BabqWarningList warningList) throws SQLException;

	public abstract Collection<String> getQuebecBabiesWithoutNumbers() throws SQLException;

}
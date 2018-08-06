/**
 * 
 */
package com.bronzeage.babq.db;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.bronzeage.babq.common.BabqUtils;
import com.bronzeage.babq.common.BabqWarningList;
import com.bronzeage.babq.common.IBabqProgress;

/**
 * @author andrew
 * 
 */
public class BabqDbNameExceptions extends BabqDbBase {

	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy");

	public BabqDbNameExceptions(Connection conn) {
		super("nameExcTbl", conn);
	}

	public void addRecord(int lineNumber, String healthNum, String QcSurname,
			String QcFirstName, String babySurname, String babyFirstName,
			String healthCardExpDate) throws SQLException, ParseException {
		PreparedStatement prep = null;
		try {

			prep = makePrepStmt("INSERT INTO  " + tblName_m
					+ " VALUES (?, ?, ?, ?, ?, ?)");
			setString(prep, 1, healthNum);
			setString(prep, 2, QcSurname);
			setString(prep, 3, QcFirstName);
			setString(prep, 4, babySurname);
			setString(prep, 5, babyFirstName);
			if (healthCardExpDate == null) {
				prep.setDate(6, null);
			} else {
				java.util.Date date = df.parse(healthCardExpDate);
				prep.setDate(6, new Date(date.getTime()));
			}
			logger_m.info("BabqDbNameExceptions: " +healthNum+" "+QcSurname+" "+QcFirstName
					+" "+babySurname+" "+babyFirstName+" "+healthCardExpDate+" " );
			prep.execute();
		} finally {
			prep.close();
		}
	}

	public void addRecord(int lineNumber, String strings[],
			BabqWarningList warningList) throws SQLException, ParseException {
		if (!strings[0].isEmpty()) {
			if (strings.length < 6)
				addRecord(lineNumber, strings[0], strings[1], strings[2], null,
						null, null);
			else
				addRecord(lineNumber, strings[0], strings[1], strings[2],
						getNonBlankString(strings[3]), getNonBlankString(strings[4]), 
						getNonBlankString(strings[5]));
		}

	}

	public boolean validateRecord(int lineNumber, String[] strings,
			BabqWarningList warningList) {
		boolean returnValue = true;
		if (!strings[0].trim().isEmpty()) {

			if (strings[1].trim().isEmpty()) {
				warningList.addWarning(lineNumber, "Surname field is blank");
				returnValue = false;
			}
			if (strings[2].trim().isEmpty()) {
				warningList.addWarning(lineNumber, "First name field is blank");
				returnValue = false;
			}

			String babySurname = null;
			if (strings.length > 3)
				babySurname = getNonBlankString(strings[3]);
			String babyFirstName = null;
			if (strings.length > 4)
				babyFirstName = getNonBlankString(strings[4]);
			String expiry = null;
			if (strings.length > 5)
				expiry = getNonBlankString(strings[5]);

			if ((babyFirstName != null) || (babySurname != null)
					|| (expiry != null)) {
				try {
					if ((babyFirstName == null) || (babySurname == null)) {
						warningList.addWarning(lineNumber,
								"Baby name must not be blank");
						returnValue = false;
					}

					if (expiry == null) {
						warningList.addWarning(lineNumber,
								"Expiry date must not be blank");
						returnValue = false;
					}
					else
					 {
						java.util.Date d = df.parse(expiry);
						if (!BabqUtils.isLastDayOfMonth(d.getTime())) {
							warningList.addWarning(lineNumber,
									" Expiry date must be the last day in the month.  It is: "
											+ d);
							returnValue = false;
						}
					}
				} catch (Throwable e) {
					warningList.addWarning(lineNumber,
							"Health card expiry date invalid or other error");
					returnValue = false;
				}

			}
		}
		return returnValue;
	}

	private String getNonBlankString(String string) {
		if (string == null)
			return null;
		if (string.trim().length() == 0)
			return null;
		return string;
	}

	public String[] getQcNames(String healthNumber) throws SQLException {
		ResultSet rs = doQuery("SELECT * FROM " + tblName_m
				+ " WHERE HealthNumber = " + healthNumber);
		if (rs.next())
			return null;
		return new String[] { rs.getString("QcSurname"),
				rs.getString("QcFirstName") };
	}

	public void ouputRsToCsv(ResultSet rs, File file,
			IBabqProgress progressTracker) throws SQLException, IOException {
		BufferedOutputStream os = new BufferedOutputStream(
				new FileOutputStream(file));
		PrintWriter pw = new PrintWriter(os);
		int count = 0;

		String s = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
				"HealthNumber", "QcSurname", "QcFirstName", "BabySurname",
				"BabyFirstName", "HealthCardExpiry");
		pw.printf(s);

		while ((rs != null) && rs.next()) {
			pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n", //
					rs.getString("HealthNumber"), //
					rs.getString("QcSurname"), //
					rs.getString("QcFirstName"), rs.getString("BabySurname"), //
					rs.getString("BabyFirstName"), rs
							.getString("HealthCardExpiry"));
			if (((++count % 100) == 0) && (progressTracker != null))
				progressTracker.setProgressString(String.format(
						"Outputing record %06d to %s", count, file.getName()));
		}
		pw.close();
		os.close();
	}

	public void createTbl() throws SQLException {
		dropTbl();
		doUpdate("create table " + tblName_m
				+ " ("
				+ "HealthNumber VARCHAR(50) NOT NULL," //
				+ "QcSurname VARCHAR(128) NOT NULL,"
				+ "QcFirstName VARCHAR(128) NOT NULL,"//
				+ "BabySurname VARCHAR(128)," //
				+ "BabyFirstName VARCHAR(128)," //
				+ "HealthCardExpiry DATE" //
				+ ")");
		doUpdate("CREATE INDEX NameExcHealthNumIndex ON " + tblName_m
				+ " (HealthNumber)");
	}

}

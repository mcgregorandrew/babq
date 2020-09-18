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

import com.bronzeage.babq.common.BabqWarningList;
import com.bronzeage.babq.common.IBabqProgress;

/**
 * @author andrew
 * 
 */
public class BabqDbBillings extends BabqDbBase {

	public BabqDbBillings(Connection conn) {
		super("billingTbl", conn);
	}

	public void addRecord(String sourceFile, int lineNumber, String healthNum,
			Date expiryDate, String surname, String firstName,
			Date dateOfBirth, String sex, String site, Date dateOfService,
			boolean usingParentHealthNum, String healthCardProvince, String mailingAddress, String resAddress,
			String billingCode) throws SQLException {
		PreparedStatement prep = makePrepStmt("INSERT INTO billingTbl "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		prep.setString(1, healthNum);
		prep.setDate(2, expiryDate);
		setString(prep, 3, surname);
		setString(prep, 4, firstName);
		prep.setDate(5, new java.sql.Date(dateOfBirth.getTime()));
		prep.setString(6, sex);
		prep.setDate(7, new java.sql.Date(dateOfService.getTime()));
		prep.setString(8, site);
		prep.setString(9, sourceFile);
		prep.setInt(10, lineNumber);
		prep.setString(11, usingParentHealthNum ? "T" : "F");
		
		/**
		 * @Since version 3.1. Address address to clinic output data
		 */
		prep.setString(12, healthCardProvince);
		prep.setString(13, mailingAddress);
		prep.setString(14, resAddress);
		prep.setString(15, billingCode);
		
		prep.execute();
		prep.close();

	}

	public void addRecord(String sourceFile, int lineNumber, String strings[],
			BabqWarningList warningList) throws SQLException, ParseException {
		try {
			if (strings.length == 12) { // File from old version of software
				addRecord(sourceFile, lineNumber, strings[0],
						stringYMDToDate(strings[1]), // DoExp
						strings[2], strings[3], // firstName
						stringYMDToDate(strings[4]), // DoB
						strings[5], // sex
						strings[7], // site
						stringYMDToDate(strings[6]), // DoS
						strings[8].equals("T"), // Using parent
						strings[9], //HealthCardProvince
						strings[10], //Mailing address
						strings[11], //Residential address
						null
				);
			} else {
				addRecord(sourceFile, lineNumber, strings[0],
						stringYMDToDate(strings[1]), // DoExp
						strings[2], strings[3], // firstName
						stringYMDToDate(strings[4]), // DoB
						strings[5], // sex
						strings[7], // site
						stringYMDToDate(strings[6]), // DoS
						strings[8].equals("T"), // Using parent
						strings[9], //HealthCardProvince
						strings[10], //Mailing address
						strings[11], //Residential address
						strings[12] // Billing code (may be empty)
				);
			}				
		} catch (Throwable t) {
			warningList.addWarning(sourceFile, lineNumber,
					"Error in loading record: " + t.toString());
		}
	}

	public void ouputRsToCsv(ResultSet rs, File file,
			IBabqProgress progressTracker, BabqWarningList warningList) throws SQLException, IOException {
		BufferedOutputStream os = new BufferedOutputStream(
				new FileOutputStream(file));
		PrintWriter pw = new PrintWriter(os);
		int count = 0;

		pw.printf("\"%s\"," + 
				"\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," + 
				"\"%s\",\"%s\",\"%s\",\"%s\"," + 
				"\"%s\",\"%s\",\"%s\"%n", 
				"HealthNumber",
				"ExpiryDate", "Surname", "FirstName", "DateOfBirth", "Sex",
				"DateOfService", "Site", "UsingParentHealthNum", "HealthCardProvince", 
				"MailingAddress", "ResidentialAddress", "BillingCode");
		while (rs.next()) {
			pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," + //
					"\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," + //
					"\"%s\",\"%s\",\"%s\"%n", //
					rs.getString("HealthNumber"), //
					rs.getDate("DateOfCardExpiry"), //
					rs.getString("Surname"), //
					rs.getString("FirstName"), //
					rs.getDate("DateOfBirth"), // 5
					rs.getString("Sex"), //
					rs.getDate("DateOfService"), //
					rs.getString("Site"), // 
					rs.getString("UsingParentHealthNum"), // 
					rs.getString("HealthCardProvince"), // 10
					rs.getString("MailingAddress"), //
					rs.getString("ResAddress"), //
					rs.getString("BillingCode") //
					);
			if ((++count % 100) == 0)
				if (progressTracker != null)
					progressTracker.setProgressString(String.format(
							"Outputing record %06d to %s", count, file
									.getName()));
		}
		pw.close();
		os.close();
		warningList.addLog(file.getName(), -1, "Wrote file " + file.getAbsolutePath());
	}

	public void createTbl() throws SQLException {
		dropTbl();
		doUpdate("create table "
				+ tblName_m
				+ " ("
				+ "HealthNumber VARCHAR(50) NOT NULL," //
				+ "DateOfCardExpiry DATE ," //
				+ "Surname VARCHAR(128) NOT NULL,"
				+ "FirstName VARCHAR(128) NOT NULL,"
				+ "DateOfBirth DATE NOT NULL,"//
				+ "Sex VARCHAR(8) NOT NULL," //
				+ "DateOfService DATE NOT NULL," //
				+ "Site VARCHAR(8) NOT NULL," + "SourceFile VARCHAR(128),"// 
				+ "SourceLineNumber INT4,"// 
				+ "UsingParentHealthNum VARCHAR(8) NOT NULL," 
				/**
				 * @Since version 3.1. Address address to clinic output data
				 */
				+ "HealthCardProvince VARCHAR(250), "
				+ "MailingAddress VARCHAR(1255), "
				+ "ResAddress VARCHAR(1255), "
				+ "BillingCode VARCHAR(12) "
				+ ")");
		doUpdate("CREATE INDEX HealthNumIndex ON " + tblName_m
				+ " (HealthNumber)");
	}

	public long getCountOfRecords(String conditionString) throws SQLException {
		ResultSet rs = doQuery("SELECT count(*) FROM " + tblName_m + " " + conditionString);
		rs.next();
		return rs.getLong(1);
	}
}

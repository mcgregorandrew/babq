/**
 * 
 */
package com.bronzeage.babq.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bronzeage.babq.common.BabqWarningList;
import com.bronzeage.babq.formatter.DateFormatter;

/**
 * This table contains the table which holds the optional table of billing codes for the appointments.
 * 
 * This is used to exclude certain types of appointments which are not allowed to be billed to the 
 * Quebec health plan.
 * 
 * @author andrew
 * 
 */
public class BabqDbBillingCodes extends BabqDbBase {

	public BabqDbBillingCodes(Connection conn) {
		super("billingCodesTbl", conn);
	}

	public boolean validateRecord(int lineNumber, String tokens[],
			BabqWarningList warningList) {

		return true;
	}

	public void addRecord(int lineNumber, String tokens[], BabqWarningList warnings) {
		PreparedStatement prep = null;
		try {
			prep = makePrepStmt("INSERT INTO  " + tblName_m
					+ " VALUES (?, ?, ?)");
			prep.setDate(1, DateFormatter.stringToDate(tokens[21]));
			String healthNumber = tokens[7].substring(2);  // Remove the QC or ON prefix that NetMedical puts on the health numbers in this file
			prep.setString(2, healthNumber);
			prep.setString(3, tokens[22]);

			prep.execute();
		} catch (Throwable e) {
			warnings.addWarning(lineNumber, e.toString());
		} finally {
			try {
				if (prep != null)
					prep.close();
			} catch (SQLException e) {
			}
		}
	}

	public void createTbl() throws SQLException {
		dropTbl();

		doUpdate("CREATE TABLE  "
				+ tblName_m
				+ "( "
				+ "ApptDate DATE NOT NULL, "
				+ "HealthNumber VARCHAR(50) NOT NULL, "
				+ "BillingCode VARCHAR(50) NOT NULL"
				+ ") ");
		
		doUpdate("CREATE INDEX ApptDateIndex ON " + tblName_m + "(ApptDate)");
		doUpdate("CREATE INDEX HealthNumberIndex ON " + tblName_m + "(HealthNumber)");
	}

	
	/**
	 * Minor working class.
	 * 
	 * @author andrewmcgregor
	 *
	 */
	public static class DateAndHealthNumber {
		public String apptDate_m;
		public String healthNumber_m;

		public DateAndHealthNumber(String apptDate, String healthNumber) {
			apptDate_m = apptDate;
			healthNumber_m = healthNumber;
		}
	}

	
	/**
	 * This returns a list of records from the billing table which have more than one entry
	 * on a given date for a given health card number.  This is used to delete duplicates
	 * so that the join we do with the appointment table will work successfully.  
	 * See {@link #deleteMatchingRecords(String, String, List)}.
	 * 
	 * @param warnings
	 * @return a list of appointment date and health cards which have more than one record for a date.
	 */
	public List<DateAndHealthNumber> getListOfRecordsWithMoreThanOneCodeInDay(BabqWarningList warnings) {
		PreparedStatement prep = null;
		try {
			prep = makePrepStmt("SELECT ApptDate, HealthNumber, count(*) AS c FROM " + tblName_m + " GROUP BY ApptDate, HealthNumber HAVING c > 1");

			
			ResultSet rs = prep.executeQuery();
			List<DateAndHealthNumber> l = new ArrayList<DateAndHealthNumber> ();
			while (rs.next()) {
				l.add(new DateAndHealthNumber (rs.getString(1), rs.getString(2)));
			}
			
			return l;
		} catch (Throwable e) {
			warnings.addWarning(-1, e.toString());
			return null;
		} finally {
			try {
				if (prep != null)
					prep.close();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * 
	 * @param apptDate
	 * @param healthNumber
	 * @param billingCodesToDelete
	 */
	public void deleteMatchingRecords(String apptDate, String healthNumber, List<String> billingCodesToDelete, BabqWarningList warnings) {
		PreparedStatement prep = null;
		try {
			prep = makePrepStmt("DELETE FROM  " + tblName_m
					+ " WHERE  ApptDate = ? AND HealthNumber = ? AND BillingCode IN ('" + String.join("','", billingCodesToDelete) + "')");
			prep.setString(1, apptDate);
			prep.setString(2, healthNumber);
			
			prep.execute();
		} catch (Throwable e) {
			warnings.addWarning(-1, e.toString());
		} finally {
			try {
				if (prep != null)
					prep.close();
			} catch (SQLException e) {
			}
		}

	}

	public List<String> getBillingCodesForDateAndHn(String apptDate, String healthNumber, BabqWarningList warnings) {
		PreparedStatement prep = null;
		List<String> billingCodeList = new ArrayList<String> ();
		try {
			prep = makePrepStmt("SELECT BillingCode FROM " + tblName_m + " WHERE ApptDate = ? AND HealthNumber = ?");

			prep.setString(1,  apptDate);
			prep.setString(2,  healthNumber);
			ResultSet rs = prep.executeQuery();
			while (rs.next()) {
				billingCodeList.add(rs.getString(1));
			}
			
		} catch (Throwable e) {
			warnings.addWarning(-1, e.toString());
			return null;
		} finally {
			try {
				if (prep != null)
					prep.close();
			} catch (SQLException e) {
			}
		}
		return billingCodeList;
	}
	
	public long getCountOfRecords(String conditionString) throws SQLException {
		ResultSet rs = doQuery("SELECT count(*) FROM " + tblName_m + " " + conditionString);
		rs.next();
		return rs.getLong(1);
	}

}

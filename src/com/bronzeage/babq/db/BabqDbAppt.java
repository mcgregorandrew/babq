/**
 * 
 */
package com.bronzeage.babq.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.bronzeage.babq.common.BabqWarningList;

/**
 * @author andrew
 * 
 */
public class BabqDbAppt extends BabqDbBase {

	public BabqDbAppt(Connection conn) {
		super("apptTbl", conn);
	}

	public boolean validateRecord(int lineNumber, String tokens[],
			BabqWarningList warningList) {

		return true;
	}

	public void addRecord(int lineNumber, String tokens[], BabqWarningList warnings) {
		PreparedStatement prep = null;
		try {
			prep = makePrepStmt("INSERT INTO  " + tblName_m
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			prep.setDate(1, stringToDate(tokens[0]));
			prep.setDate(2, stringToDate(tokens[1]));
			prep.setInt(3, Integer.parseInt(tokens[3]));
			prep.setInt(4, Integer.parseInt(tokens[4]));
			prep.setString(5, tokens[5]);
			prep.setString(6, tokens[7]);
			setString(prep, 7, tokens[8]);
			prep.setString(8, tokens[9]);
			prep.setString(9, tokens[10]);
			prep.setString(10, tokens[12]);
			prep.setString(11, tokens[13]);
			prep.setString(12, tokens[14]);
			prep.setString(13, tokens[17]);
			prep.setString(14, tokens[22]);

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

		doUpdate("create table  "
				+ tblName_m
				+ "( "
				+ "ApptDate DATE NOT NULL, "
				+ "BookingDate DATE, "
				+ "PatientNum INT4 NOT NULL, "
				+ "ProviderID INT4 NOT NULL, "
				+ "ProviderName VARCHAR(250) NOT NULL, "
				+ // 5
				"Duration VARCHAR(50) NOT NULL, "
				+ "Details VARCHAR(10240) NOT NULL, "
				+ "Status VARCHAR(50) NOT NULL, "
				+ "NoShow VARCHAR(50) NOT NULL, "
				+ "Billed VARCHAR(50) NOT NULL, "
				+ // 10
				"Repeats VARCHAR(50) NOT NULL, "
				+ "Deleted VARCHAR(50) NOT NULL, "
				+ "BookingInitials VARCHAR(50) NOT NULL, "
				+ "Type VARCHAR(50) " //2015 Xmas Project
				+ ") ");
		
		doUpdate("CREATE INDEX PatientNumIndex ON " + tblName_m + "(PatientNum)");
		doUpdate("CREATE INDEX ProviderIDIndex ON " + tblName_m + "(ProviderID)");
	}
}

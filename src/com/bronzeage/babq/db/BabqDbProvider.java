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
public class BabqDbProvider extends BabqDbBase {

	public BabqDbProvider(Connection conn) {
		super("providerTbl", conn);
	}

	public boolean validateRecord(int lineNumber, String tokens[],
			BabqWarningList warningList) {

		return true;
	}

	public void addRecord(int lineNumber, String tokens[],
			BabqWarningList warningList) throws SQLException {
		PreparedStatement prep = makePrepStmt("INSERT INTO  " + tblName_m
				+ " VALUES (?, ?, ?, ?, ?, ?, ?)");
		try {
			try {
				prep.setInt(1, Integer.parseInt(tokens[0]));

			} catch (Throwable t) {
				warningList.addWarning(lineNumber,
						"No provider number in the first column");
				return;
			}

			try {
				if (tokens.length < 7)
					throw new Exception ("Too few columns");
				if (tokens[1].trim().length() == 0)
					throw new Exception ("Invalid surname");
				if (tokens[6].trim().length() == 0)
					throw new Exception ("Invalid location");
				if (tokens[4].trim().length() == 0)
					throw new Exception ("Invalid team");
				if (tokens[5].trim().length() == 0)
					throw new Exception ("Invalid role");
				prep.setString(2, tokens[1]); // surname
				prep.setString(3, tokens[2]); // firstname
				prep.setString(4, tokens[6]); // Location
				prep.setString(5, tokens[4]); // Team

				prep.setString(6, tokens[5]); // Role
				if ((tokens[3] != null) && (tokens[3].trim().length() > 0))
					try {
						prep.setInt(7, Integer.parseInt(tokens[3])); // OHIP ID
					} catch (Throwable t) {
						warningList.addWarning(lineNumber,
								"Invalid characters in OHIP number");
						return;
					}
				else
					prep.setObject(7, null);

				prep.execute();
			} catch (Throwable t) {
				warningList.addWarning(lineNumber,
						"Missing or invalid field in provider record");
			}

		} finally {
			prep.close();
		}

	}

	public void createTbl() throws SQLException {
		dropTbl();

		doUpdate("CREATE TABLE " + tblName_m + " ("
				+ " `ProviderID` INT4 NOT NULL,"
				+ " `ProviderSurname` varchar(128) NOT NULL default '',"
				+ " `ProviderFirstName` varchar(128) NOT NULL default '',"
				+ " `Location` varchar(32) default NULL," //
				+ " `Team` varchar(64) default NULL," //
				+ " `Role` varchar(64) default NULL," //
				+ " `OhipId` INT4 default NULL" //
				+ ")");

		doUpdate("CREATE INDEX ProvProviderIdIndex ON " + tblName_m
				+ " (ProviderId)");
		doUpdate("CREATE INDEX ProvOhipIndex ON " + tblName_m + " (OhipId)");
	}
}

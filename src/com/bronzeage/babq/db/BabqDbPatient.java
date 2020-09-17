/**
 * 
 */
package com.bronzeage.babq.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bronzeage.babq.common.BabqUtils;
import com.bronzeage.babq.common.BabqWarningList;
import com.bronzeage.babq.formatter.DateFormatter;

/**
 * @author andrew
 * 
 */
public class BabqDbPatient extends BabqDbBase {

	public BabqDbPatient(Connection conn) {
		super("patientTbl", conn);
	}

	public void addRecords(String source, int lineNumber, List<String[]> lines,
			BabqWarningList warningList) {
		
		PreparedStatement prep = null;
		List<String> buf = new ArrayList<String> ();
		for (int i = 0; i < lines.size(); i++) {
			buf.add("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		}
		String cmd = "INSERT INTO  " + tblName_m + " VALUES " + String.join(",", buf);
		
		try {
			prep = makePrepStmt(cmd);
			int offset = 0;
			for (String tokens[]: lines) {
				prep.setInt(1+offset, Integer.parseInt(tokens[0])); // pt num
				setString(prep, 2+offset, tokens[1]); // surname
				setString(prep, 3+offset, tokens[4]); // first name
				
				//Changed in version 5.0
				//Changed in version 6.0
				setString(prep, 4+offset, tokens[69]); // health card province
				
				/**
				 * @Since version 3.1. Address address to clinic output data
				 */
				setString(prep, 5+offset, tokens[8]); // mailing address line 1
				setString(prep, 6+offset, tokens[9]); // mailing address line 2
				setString(prep, 7+offset, tokens[12]); // mailing city
				setString(prep, 8+offset, tokens[13]); // mailing province
				setString(prep, 9+offset, tokens[14]); // mailing country
				setString(prep, 10+offset, tokens[15]); // mailing postal code
				
				/**
				 * @Since version 3.1. Address address to clinic output data
				 */
				setString(prep, 11+offset, tokens[20]); // residential address line 1
				setString(prep, 12+offset, tokens[21]); // residential address line 2
				setString(prep, 13+offset, tokens[24]); // residential city
				setString(prep, 14+offset, tokens[25]); // residential province
				setString(prep, 15+offset, tokens[26]); // residential country
				setString(prep, 16+offset, tokens[27]); // residential postal code
				
				prep.setDate(17+offset, DateFormatter.stringToDate(tokens[31])); // Birth Date
				setString(prep, 18+offset, tokens[32]); //Sex
				
				//Changed in version 5.0
				//Changed in version 6.0
				setString(prep, 19+offset, tokens[70]); // Health Card Number
				setString(prep, 20+offset, tokens[72]); // Health Card Version Code
				Date expiryDate = DateFormatter.stringToDate(tokens[74]); // Card Expire Date
				prep.setDate(21+offset, expiryDate);
				if ((expiryDate == null)
						|| (BabqUtils.isLastDayOfMonth(expiryDate.getTime()))) {
					prep.setDate(22+offset, expiryDate);
				} else {
					prep.setDate(22+offset, new Date(BabqUtils
							.getDateAtLastDayOfMonth(expiryDate.getTime())));
				}
	
				//Changed in version 5.0
				String familyMd = tokens[52]; // Family Doctor
				try {
					prep.setInt(23+offset, Integer.parseInt(familyMd));
				} catch (NumberFormatException e) {
					if (familyMd.trim().length() != 0)
						warningList
								.addWarning(
										lineNumber,
										"Error in FamilyMD number (found "
												+ familyMd
												+ ") - may need to re-export with references");
					prep.setObject(23+offset, null);
				}
				
				prep.setString(24+offset, tokens[57]); // Doc Num
	
				setString(prep, 25+offset, tokens[33]); // mDeleted
				
				//Changed in version 6.0
				setString(prep, 26+offset, tokens[79]); // mMember status
				prep.setString(27+offset, source);
				prep.setInt(28+offset, lineNumber);
				offset += 28;
				//System.out.println("Patient number: "+tokens[0]+" patient name: "+ tokens[4]+" "+tokens[1]+" memberStatus: "+tokens[78]);
				
	//			logger_m.info("PatientNum Surname FirstName Province MailProvince DateOfBirth Sex HealthNumber HNVersionCode DateOfCardExpiry DateOfCardExpiryCor " +
	//					"FamilyDoctor PatientDoctor ActiveStatus MemberStatus SourceFile SourceLineNumber \n" +
	//					tokens[0]+" // " + tokens[1]+" // " + tokens[4]+" // " + tokens[65]+" // "
	//					+ tokens[13]+" // " + tokens[31]+" // " + tokens[32]+" // " + tokens[66] +" // "
	//			        + tokens[68]+" // " + tokens[70]+" // " + tokens[70]+" // " + tokens[49]+" // "
	//			        + tokens[54]+" // " + tokens[33]+" // " + tokens[75]+" // " +source+" // " + lineNumber);
			}
			prep.execute();
		} catch (Throwable e) {
			warningList.addWarning(lineNumber, e.toString());
		} finally

		{
			if (prep != null) {
				try {
					prep.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public void createTbl() throws SQLException {
		dropTbl();
		doUpdate("create table "
				+ tblName_m
				+ "( "
				+ "PatientNum INT4 NOT NULL, "
				+ "Surname VARCHAR(128) NOT NULL, "
				+ "FirstName VARCHAR(128) NOT NULL, "
				+ "Province VARCHAR(50) NOT NULL, "
				
				/**
				 * @Since version 3.1. Address address to clinic output data
				 */
				+ "MailAddrLine1 VARCHAR(500) NOT NULL, "
				+ "MailAddrLine2 VARCHAR(500) NOT NULL, "
				+ "MailCity VARCHAR(100) NOT NULL, "
				+ "MailProvince VARCHAR(50) NOT NULL, "
				+ "MailCountry VARCHAR(50) NOT NULL, "
				+ "MailPostal VARCHAR(50) NOT NULL, "
				
				/**
				 * @Since version 3.1. Address address to clinic output data
				 */
				+ "ResAddrLine1 VARCHAR(500) NOT NULL, "
				+ "ResAddrLine2 VARCHAR(500) NOT NULL, "
				+ "ResCity VARCHAR(100) NOT NULL, "
				+ "ResProvince VARCHAR(50) NOT NULL, "
				+ "ResCountry VARCHAR(50) NOT NULL, "
				+ "ResPostal VARCHAR(50) NOT NULL, "
				
				+ "DateOfBirth DATE, " // 5
				+ "Sex VARCHAR(50) NOT NULL, "
				+ "HealthNumber VARCHAR(50) NOT NULL, "
				+ "HNVersionCode VARCHAR(50) NOT NULL, "
				+ "DateOfCardExpiry DATE, " // 9
				// Date of card expiry forced to last day in month
				+ "DateOfCardExpiryCor DATE, " // 10
				+ "FamilyDoctor INT4," //
				+ "PatientDoctor VARCHAR(50)," //
				+ "ActiveStatus VARCHAR(128)," // mDeleted column (Active or
												// other)
				+ "MemberStatus VARCHAR(64)," // mMemberStatus column
				+ "SourceFile VARCHAR(128),"// 
				+ "SourceLineNumber INT4,"// 
				+ "PRIMARY KEY (PatientNum) " + ")");

		doUpdate("CREATE INDEX PatPatientNumIndex ON " + tblName_m
				+ " (PatientNum)");
		doUpdate("CREATE INDEX PatFamilyDoctorIndex ON " + tblName_m
				+ " (FamilyDoctor)");

	}
	
	public void dropTbl() {
		PreparedStatement stmt = null;
		try {
			stmt = makePrepStmt("DROP VIEW patientWithApptView");
			try {
				executePrepStmt(stmt);
			}
			catch (SQLException e) {
			}
			stmt = makePrepStmt("DROP VIEW PATIENTAGERANGEVIEW");
			try {
				executePrepStmt(stmt);
			}
			catch (SQLException e) {
			}

		} catch (Throwable e) {
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		super.dropTbl();
	}

}
